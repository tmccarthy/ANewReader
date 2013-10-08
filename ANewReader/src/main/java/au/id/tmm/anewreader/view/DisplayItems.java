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

package au.id.tmm.anewreader.view;

import android.os.AsyncTask;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import au.id.tmm.anewreader.model.Continuation;
import au.id.tmm.anewreader.model.Feed;
import au.id.tmm.anewreader.model.Item;
import au.id.tmm.anewreader.model.ListWithContinuation;
import au.id.tmm.anewreader.model.ReadStatus;

/**
 * Singleton class for simplifying the handling of the Items to displayed. Items are loaded and
 * stored in this singleton, which notifies its listeners when the dataset changes.
 */
public class DisplayItems {

    private static final String LOAD_MORE_ERROR_MESSAGE = "Unable to load more items, invalid " +
            "continuation or previous request";

    private static final DisplayItems INSTANCE = new DisplayItems();

    private static final int DEFAULT_NUM_ITEMS_PER_LOAD = 20;

    private List<DisplayItemsListener> listeners = new LinkedList<DisplayItemsListener>();

    private boolean loadingItems = false;

    private List<Item> loadedItems;
    private Feed feed;
    private ModelRequestParams previousModelRequestParams;
    private Continuation continuation;

    public static DisplayItems getInstance() {
        return INSTANCE;
    }

    private DisplayItems() {
    }

    public void populate() {
        this.populate(null);
    }

    public void populate(ReadStatus readStatus) {
        this.populate(readStatus, null);
    }

    public void populate(final ReadStatus readStatus, final Date olderThan) {
        this.populate(readStatus, olderThan, DEFAULT_NUM_ITEMS_PER_LOAD);
    }

    /**
     * Populate the list of displayed Items with Items matching the given parameters.
     */
    public void populate(final ReadStatus readStatus, final Date olderThan, final int numItemsLimit) {
        new AsyncTask<Void, PopulateProgress, ListWithContinuation<Item>>() {

            private Throwable onCancelCause;

            @Override
            protected void onPreExecute() {

                loadingItems = true;

                for (DisplayItemsListener currentListener : listeners) {
                    currentListener.onPopulatePreExecute();
                }
            }

            @Override
            protected ListWithContinuation<Item> doInBackground(Void... params) {
                try {
                    return feed.getItems(readStatus, olderThan, numItemsLimit);
                } catch (Throwable t) {
                    this.onCancelCause = t;
                    this.cancel(false);
                    return null;
                }
            }

            @Override
            protected void onProgressUpdate(PopulateProgress... progress) {
                for (DisplayItemsListener currentListener : listeners) {
                    currentListener.onPopulateProgress(progress[0]);
                }
            }

            @Override
            protected void onPostExecute(ListWithContinuation<Item> itemsWithContinuation) {

                continuation = itemsWithContinuation.getContinuation();
                loadedItems = itemsWithContinuation.getList();
                setPreviousModelRequestParams(readStatus, olderThan);

                for (DisplayItemsListener currentListener : listeners) {
                    currentListener.onPopulateComplete();
                }

                loadingItems = false;
            }

            @Override
            protected void onCancelled() {
                for (DisplayItemsListener currentListener : listeners) {
                    currentListener.onPopulateError(this.onCancelCause);
                }
                loadingItems = false;
            }
        }.execute();
    }

    /**
     * Load more Items into the list of displayed Items.
     */
    public void loadMoreItems() {

        if (this.continuation == null
                || this.previousModelRequestParams == null
                || !this.continuation.getAssociatedFeed().equals(this.feed)) {
            throw new IllegalStateException(LOAD_MORE_ERROR_MESSAGE);
        } else {

            new AsyncTask<Void, LoadMoreProgress, ListWithContinuation<Item>>() {

                private Throwable onCancelCause;

                @Override
                protected void onPreExecute() {
                    loadingItems = true;
                    for (DisplayItemsListener currentListener : listeners) {
                        currentListener.onLoadMoreItemsPreExecute();
                    }
                }

                @Override
                protected ListWithContinuation<Item> doInBackground(Void... params) {
                    try{
                        return feed.getItems(previousModelRequestParams.readStatus, previousModelRequestParams.olderThan, continuation);
                    } catch (Throwable t) {
                        this.onCancelCause = t;
                        this.cancel(false);
                        return null;
                    }
                }

                @Override
                protected void onProgressUpdate(LoadMoreProgress... progress) {
                    for (DisplayItemsListener currentListener : listeners) {
                        currentListener.onLoadMoreItemsProgress(progress[0]);
                    }
                }

                @Override
                protected void onPostExecute(ListWithContinuation<Item> newItemsWithContinuation) {
                    continuation = newItemsWithContinuation.getContinuation();
                    loadedItems.addAll(newItemsWithContinuation.getList());

                    for (DisplayItemsListener currentListener : listeners) {
                        currentListener.onLoadMoreItemsComplete(newItemsWithContinuation.getList());
                    }
                    loadingItems = false;
                }

                @Override
                protected void onCancelled() {
                    for (DisplayItemsListener currentListener : listeners) {
                        currentListener.onLoadMoreItemsError(this.onCancelCause);
                    }
                    loadingItems = false;
                }
            }.execute();

        }

    }

    /**
     * Refresh the list of displayed items.
     */
    public void refresh() {
        new AsyncTask<Void, RefreshProgress, ListWithContinuation<Item>>() {

            private Throwable onCancelCause;

            @Override
            protected void onPreExecute() {
                loadingItems = true;

                for (DisplayItemsListener currentListener : listeners) {
                    currentListener.onRefreshPreExecute();
                }
            }

            @Override
            protected ListWithContinuation<Item> doInBackground(Void... params) {
                try {
                    return feed.getItems(previousModelRequestParams.readStatus, previousModelRequestParams.olderThan);
                } catch (Throwable t) {
                    this.onCancelCause = t;
                    this.cancel(false);
                    return null;
                }
            }

            @Override
            protected void onProgressUpdate(RefreshProgress... progress) {
                for (DisplayItemsListener currentListener : listeners) {
                    currentListener.onRefreshProgress(progress[0]);
                }
            }

            @Override
            protected void onPostExecute(ListWithContinuation<Item> itemsWithContinuation) {
                continuation = itemsWithContinuation.getContinuation();
                loadedItems = itemsWithContinuation.getList();
                for (DisplayItemsListener currentListener : listeners) {
                    currentListener.onRefreshComplete();
                }

                loadingItems = false;
            }

            @Override
            protected void onCancelled() {
                for (DisplayItemsListener currentListener : listeners) {
                    currentListener.onRefreshError(this.onCancelCause);
                }

                loadingItems = false;
            }
        }.execute();
    }

    public void registerListener(DisplayItemsListener listener) {
        this.listeners.add(listener);
    }

    public void unRegisterListener(DisplayItemsListener listener) {
        this.listeners.remove(listener);
    }

    public Feed getFeed() {
        return feed;
    }

    public void setFeed(Feed feed) {
        this.feed = feed;
        this.loadedItems = null;
        this.previousModelRequestParams = null;
        this.continuation = null;

        for (DisplayItemsListener currentListener : this.listeners) {
            currentListener.onChangeFeed();
        }
    }

    public List<Item> getLoadedItems() {
        return loadedItems;
    }

    private void setPreviousModelRequestParams(ReadStatus readStatus, Date olderThan) {
        if (this.previousModelRequestParams == null) {
            this.previousModelRequestParams = new ModelRequestParams(readStatus, olderThan);
        } else {
            this.previousModelRequestParams.readStatus = readStatus;
            this.previousModelRequestParams.olderThan = olderThan;
        }
    }

    private class ModelRequestParams {

        private ModelRequestParams(ReadStatus readStatus, Date olderThan) {
            this.readStatus = readStatus;
            this.olderThan = olderThan;
        }

        ReadStatus readStatus;
        Date olderThan;
    }

    public boolean isLoadingItems() {
        return loadingItems;
    }

    // Enums containing values indicating progress for the populate, loadMore and refresh
    // operations.

    // TODO give these meaningful members
    public enum PopulateProgress {}

    public enum LoadMoreProgress {}

    public enum RefreshProgress {}

    /**
     * Interface for listeners of the display Items list.
     */
    public static interface DisplayItemsListener {

        // Populating

        public void onPopulatePreExecute();

        public void onPopulateProgress(PopulateProgress progress);

        public void onPopulateComplete();

        public void onPopulateError(Throwable cause);

        // Loading more

        public void onLoadMoreItemsPreExecute();

        public void onLoadMoreItemsProgress(LoadMoreProgress progress);

        public void onLoadMoreItemsComplete(List<Item> newItems);

        public void onLoadMoreItemsError(Throwable cause);

        // Refreshing

        public void onRefreshPreExecute();

        public void onRefreshProgress(RefreshProgress progress);

        public void onRefreshComplete();

        public void onRefreshError(Throwable cause);

        // Change of feed

        public void onChangeFeed();

    }
}
