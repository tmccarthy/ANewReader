/*******************************************************************************
 * This file is part of A New Reader
 * Copyright (C) 2013 Timothy McCarthy
 *
 * A New Reader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * A New Reader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with A New Reader.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package au.id.tmm.anewreader.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import au.id.tmm.anewreader.model.net.ReaderServiceRequestHelper;

/**
 * Class handling the retrieval and construction of Items and Feeds.
 * <p/>
 * In terms of design, this class leaves much to be desired. It is too large, with too many
 * responsibilities. In the medium term, the Model class will be extracted out as an interface, with
 * concrete instantiations available for different purposes (eg one for online retrieval of Items
 * and Feeds, and another for offline storage). Instantiation of Feeds and Items should be delegated
 * to dedicated factories, their products accessible through the Model interface.
 */
class Model {

    private Map<String, Subscription> subscriptions = new HashMap<String, Subscription>();
    private Map<String, Category> categories = new HashMap<String, Category>();
    private ReadingList readingList;

    private ItemCache itemCache = new ItemCache();

    private Account parentAccount;

    public Model(Account parentAccount) {
        this.parentAccount = parentAccount;
    }

    protected Account getParentAccount() {
        return parentAccount;
    }

    /**
     * Get the subscriptions associated with this model.
     */
    public SortedSet<Subscription> getSubscriptions() throws IOException {
        final String SUBSCRIPTIONS_URL
                = this.parentAccount.getReaderService().getBaseUrl()
                + "/reader/api/0/subscription/list?output=json";

        this.subscriptions.clear();

        ReaderServiceRequestHelper requestHelper
                = new ReaderServiceRequestHelper(this.parentAccount.getAuthHelper());

        try {

            JSONObject jsonResponse;

            jsonResponse
                    = new JSONObject(requestHelper.performGetRequest(SUBSCRIPTIONS_URL));

            JSONArray responseArray = jsonResponse.getJSONArray("subscriptions");

            for (int i = 0; i < responseArray.length(); i++) {
                Subscription newSubscription
                        = this.getSubscriptionFromJson(responseArray.getJSONObject(i),
                        this.parentAccount);
                this.subscriptions.put(newSubscription.getId(), newSubscription);
            }

        } catch (JSONException e) {
            throw new ApiParseException(e);
        }

        return new TreeSet<Subscription>(this.subscriptions.values());

    }

    /**
     * Get the categories associated with this model.
     */
    public SortedSet<Category> getCategories() throws IOException {

        final String CATEGORIES_BASE_URL = this.parentAccount.getReaderService().getBaseUrl()
                + "/reader/api/0/tag/list?output=json";

        ReaderServiceRequestHelper requestHelper
                = new ReaderServiceRequestHelper(this.parentAccount.getAuthHelper());

        this.categories.clear();

        try {
            JSONArray categoriesArray
                    = new JSONObject(requestHelper.performGetRequest(CATEGORIES_BASE_URL))
                    .getJSONArray("tags");

            for (int i = 0; i < categoriesArray.length(); i++) {
                Category newCategory
                        = this.getCategoryFromJson(categoriesArray.getJSONObject(i),
                        this.parentAccount);
                this.categories.put(newCategory.getId(), newCategory);
            }
        } catch (JSONException e) {
            throw new ApiParseException(e);
        }

        return new TreeSet<Category>(this.categories.values());

    }

    /**
     * Retrieve items according to the given parameters. If readStatus is null, both read and unread
     * items will be returned.
     * <p/>
     * Note the retrieval of only read items is not currently supported. The api doesn't allow this
     * directly, and it is not currently available to the end user classes. Requesting only read
     * items results in an UnsupportedOperationException being thrown.
     */
    public ListWithContinuation<Item> getItems(Feed feed, ReadStatus readStatus,
                                               int numItemsLimit, Date olderThan,
                                               Continuation continuation) throws IOException {
        try {

            if (readStatus == ReadStatus.UNREAD) {

                ListWithContinuation<String> itemIds
                        = this.getItemIdsFromApi(feed, true, numItemsLimit,
                        olderThan, continuation);
                return new ListWithContinuation<Item>(this.getItemsFromIds(itemIds.getList(),
                        ReadStatus.UNREAD), itemIds.getContinuation());

            } else if (readStatus == ReadStatus.READ) {

                // This is made very difficult by the API, and is a functionality used by the app,
                // so we just leave as unsupported.
                throw new UnsupportedOperationException();

            } else {

                ListWithContinuation<String> allItemIds
                        = this.getItemIdsFromApi(feed, false, numItemsLimit,
                        olderThan, continuation);
                ListWithContinuation<String> unreadItemIds
                        = this.getItemIdsFromApi(feed, true, numItemsLimit,
                        olderThan, continuation);
                List<Item> returnedItems = this.getItemsFromIds(allItemIds.getList());

                for (Item currentItem : returnedItems) {
                    currentItem.setReadStatus(unreadItemIds.getList().contains(currentItem.getId())
                            ? ReadStatus.UNREAD : ReadStatus.READ);
                }

                return new ListWithContinuation<Item>(returnedItems, allItemIds.getContinuation());

            }
        } catch (JSONException e) {
            throw new ApiParseException(e);
        }

    }

    private List<Item> getItemsFromIds(List<String> itemIds) throws IOException {
        return this.getItemsFromIds(itemIds, null);
    }

    /**
     * For a list of item ids, returns the corresponding Item objects. These are retrieved from the
     * ItemCache if possible, otherwise they are retrieved from the api.
     */
    private List<Item> getItemsFromIds(List<String> itemIds, ReadStatus readStatus)
            throws IOException {

        // Essentially the process here is to identify which items are not available through the
        // ItemCache, and to then retrieve these items in-bulk from the api.

        // Create a list of the same size as the list of item ids in which we will place the
        // Item objects.
        List<Item> returnedItems =
                new ArrayList<Item>(Collections.nCopies(itemIds.size(), (Item) null));

        // A map holding the ids of uncached items and their indexes in the returnedItems list.
        Map<String, Integer> idsAndIndexesOfUncachedItems = new HashMap<String, Integer>();

        for (int i = 0; i < itemIds.size(); i++) {
            Item cachedItemForThisId = this.itemCache.get(itemIds.get(i));

            if (cachedItemForThisId == null) {
                idsAndIndexesOfUncachedItems.put(itemIds.get(i), i);
            } else {
                cachedItemForThisId.setReadStatus(readStatus);
                returnedItems.add(i, cachedItemForThisId);
            }
        }

        List<Item> itemsNotFoundInCache = this.getItemsFromApi(
                new ArrayList<String>(idsAndIndexesOfUncachedItems.keySet()), readStatus);

        for (Item currentItem : itemsNotFoundInCache) {
            returnedItems.set(idsAndIndexesOfUncachedItems.get(currentItem.getId()), currentItem);
        }

        return returnedItems;
    }

    /**
     * Construct a Subscription object from a JSON object retrieved from the api.
     */
    private Subscription getSubscriptionFromJson(JSONObject subscriptionJsonObject,
                                                 Account parentAccount) throws JSONException {
        String id = this.extractSubscriptionId(subscriptionJsonObject.getString("id"));

        String title = subscriptionJsonObject.getString("title");
        String url = subscriptionJsonObject.getString("url");
        String htmlUrl = subscriptionJsonObject.getString("htmlUrl");
        String iconUrl = subscriptionJsonObject.getString("iconUrl");

        Set<Category> categories = new TreeSet<Category>();

        JSONArray categoriesArray = subscriptionJsonObject.getJSONArray("categories");

        for (int i = 0; i < categoriesArray.length(); i++) {
            categories.add(this.getCategoryFromJson(categoriesArray.getJSONObject(i),
                    parentAccount));
        }

        // TODO the unread count is set to 0 since unread counts are not currently supported
        return new Subscription(id, title, categories, url, htmlUrl, iconUrl, 0, this);
    }

    /**
     * Extract a subscription's id from the id field in a subscription JSON object retrieved from
     * the api.
     */
    private String extractSubscriptionId(String idFromJson) {
        Pattern idPattern = Pattern.compile("^feed/(.*)$");
        Matcher matcher = idPattern.matcher(idFromJson);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new ApiParseException();
        }
    }

    /**
     * Construct a Category object from a JSON object retrieved from the api.
     */
    private Category getCategoryFromJson(JSONObject categoryJsonObject, Account parentAccount)
            throws JSONException {
        return this.getCategoryFromId(categoryJsonObject.getString("id"), parentAccount);
    }

    /**
     * Constructs a Category object from a category id retrieved from the api.
     */
    private Category getCategoryFromId(String id, Account parentAccount) {
        Pattern labelPattern = Pattern.compile("^user/-/label/(.*)$");
        Matcher matcher = labelPattern.matcher(id);
        if (matcher.find()) {
            // TODO the unread count is set to 0 since unread counts are not currently supported
            return new Category(id, matcher.group(1), 0, this);
        } else {
            throw new ApiParseException();
        }
    }

    /**
     * Constructs an Item object from a JSON object retrieved from the api.
     */
    private Item getItemFromJson(JSONObject itemJsonObject, ReadStatus readStatus,
                                 Account parentAccount) throws JSONException, IOException {
        Pattern idPattern = Pattern.compile("^tag:google\\.com,2005:reader/item/(.*)$");
        Matcher matcher = idPattern.matcher(itemJsonObject.getString("id"));
        String id;
        if (matcher.find()) {
            id = matcher.group(1);
        } else {
            throw new ApiParseException();
        }

        String title = itemJsonObject.getString("title");
        String canonicalLink
                = itemJsonObject.getJSONArray("canonical").getJSONObject(0).getString("href");
        String alternateLink
                = itemJsonObject.getJSONArray("alternate").getJSONObject(0).getString("href");

        Date publishedTimestamp = new Date(Long.parseLong(itemJsonObject.getString("published")));
        Date updatedTimestamp = new Date(Long.parseLong(itemJsonObject.getString("updated")));
        Date crawlTimestamp = new Date(Long.parseLong(itemJsonObject.getString("crawlTimeMsec")));

        String summary = itemJsonObject.getJSONObject("summary").getString("content");
        String author = itemJsonObject.getString("author");

        Set<Category> categories = new TreeSet<Category>();

        JSONArray categoriesArray = itemJsonObject.getJSONArray("categories");

        for (int i = 0; i < categoriesArray.length(); i++) {
            if (categoriesArray.getString(i).matches("\\^user/-/label/(.*)$")) {

                String currentId = categoriesArray.getString(i);

                if (this.categories.containsKey(currentId)) {
                    categories.add(this.categories.get(currentId));
                } else {
                    // The category associated with the item is not in our categories array. This
                    // problem should be fixed when this class is refactored as mentioned above.
                    // For the moment, we simply throw a RuntimeException.
                    throw new RuntimeException();
                }
            }
        }

        JSONObject originJsonObject = itemJsonObject.getJSONObject("origin");

        Subscription parentSubscription;
        String streamId = this.extractSubscriptionId(originJsonObject.getString("streamId"));
        if (this.subscriptions.containsKey(streamId)) {
            parentSubscription = this.subscriptions.get(streamId);
        } else {
            this.getSubscriptions();
            if (this.subscriptions.containsKey(streamId)) {
                parentSubscription = this.subscriptions.get(streamId);
            } else {
                // The subscription associated with the item is not in our subscriptions array. This
                // problem should be fixed when this class is refactored as mentioned above.
                // For the moment, we simply throw a RuntimeException.
                throw new RuntimeException();
            }

        }

        return new Item(id, title, canonicalLink, alternateLink,
                publishedTimestamp, updatedTimestamp, crawlTimestamp,
                summary, author, categories, parentSubscription, readStatus);
    }

    /**
     * Retrieves the item ids for the given parameters. These ids can then be used to construct
     * corresponding Item objects.
     */
    private ListWithContinuation<String> getItemIdsFromApi(Feed feed, boolean onlyUnread,
                                                           int numItemsLimit, Date olderThan,
                                                           Continuation continuation)
            throws IOException, JSONException {
        final String BASE_ITEMS_URL = this.parentAccount.getReaderService().getBaseUrl() +
                "/reader/api/0/stream/items/ids?output=json";
        final String READ_ITEMS_STREAM = "user/-/state/com.google/read";

        String itemListUrl = BASE_ITEMS_URL
                + "&s=" + feed.getEncodedFeedAddress()
                + (onlyUnread ? "&xt=" + READ_ITEMS_STREAM : "")
                + "&n=" + String.valueOf(numItemsLimit)
                + "&r=d"
                + (olderThan != null ? "&ot=" + Long.toString(olderThan.getTime()) : "")
                + (continuation != null ? "&c=" + Long.toString(continuation.getCode()) : "");

        ReaderServiceRequestHelper requestHelper = new ReaderServiceRequestHelper(
                this.parentAccount.getAuthHelper());

        JSONObject itemsResponse;
        itemsResponse = new JSONObject(requestHelper.performGetRequest(itemListUrl));

        JSONArray itemsResponseArray = itemsResponse.getJSONArray("itemRefs");

        List<String> returnedIds = new ArrayList<String>(itemsResponseArray.length());

        for (int i = 0; i < itemsResponseArray.length(); i++) {
            returnedIds.add(itemsResponseArray.getJSONObject(i).getString("id"));
        }

        Continuation returnedContinuation = null;
        if (itemsResponse.has("continuation")) {
            returnedContinuation = new Continuation(itemsResponse.getLong("continuation"), feed);
        }

        return new ListWithContinuation<String>(returnedIds, returnedContinuation);
    }

    /**
     * Constructs a list of Item objects for the given list of ids from the api.
     * @param ids
     * @param readStatus
     * @return
     * @throws IOException
     */
    private List<Item> getItemsFromApi(List<String> ids, ReadStatus readStatus) throws IOException {

        if (ids == null || ids.isEmpty()) {
            // Return empty list of items
            return new ArrayList<Item>();
        }

        final String ITEMS_BASE_URL = this.parentAccount.getReaderService().getBaseUrl() +
                "/reader/api/0/stream/items/contents?output=json";

        StringBuilder itemsUrl = new StringBuilder(ITEMS_BASE_URL);

        for (String currentId : ids) {
            itemsUrl.append("&i=").append(currentId);
        }

        ReaderServiceRequestHelper requestHelper = new ReaderServiceRequestHelper(
                this.parentAccount.getAuthHelper());

        JSONObject itemsResponse;

        try {

            itemsResponse = new JSONObject(
                    requestHelper.performGetRequest(itemsUrl.toString()));

            JSONArray itemsArray = itemsResponse.getJSONArray("items");

            List<Item> returnedItems = new ArrayList<Item>(itemsArray.length());

            for (int i = 0; i < itemsArray.length(); i++) {
                returnedItems.add(
                        this.getItemFromJson(itemsArray.getJSONObject(i), readStatus,
                                this.parentAccount));
            }

            return returnedItems;

        } catch (JSONException e) {
            throw new ApiParseException(e);
        }

    }

    private List<Item> getItemsFromApi(List<String> ids) throws IOException {
        return this.getItemsFromApi(ids, null);
    }

    public ReadingList getReadingList(String title) {
        // TODO the unread count is set to 0 since unread counts are not currently supported
        if (this.readingList == null) {
            this.readingList = new ReadingList(this, title, 0);
        } else {
            this.readingList.setTitle(title);
        }
        return this.readingList;
    }
}
