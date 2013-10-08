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

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import au.id.tmm.anewreader.R;
import au.id.tmm.anewreader.model.Account;
import au.id.tmm.anewreader.model.Feed;
import au.id.tmm.anewreader.model.Item;
import au.id.tmm.anewreader.model.PreviousAccountInfoFile;
import au.id.tmm.anewreader.utility.network.AuthenticationException;
import au.id.tmm.anewreader.utility.network.HttpException;
import au.id.tmm.anewreader.utility.network.HttpStatusCode;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

/**
 * Main activity, allows the user to choose which Feed to view, and presents a summary of individual
 * Items in that feed.
 */
public class MainActivity extends FragmentActivity {

    private static final int AUTHENTICATE_ACTIVITY_REQUEST_CODE = 0;
    private static final String DEFAULT_INFOFILE_FILENAME = "account-info";

    private FeedArrayAdapter feedArrayAdapter;
    private ItemArrayAdapter itemArrayAdapter;

    private DisplayFeeds displayFeeds;
    private Account account;

    private PullToRefreshAttacher pullToRefreshAttacher;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main_activity);

        ListView feedListView = (ListView) this.findViewById(R.id.drawer_layout_feed_list);
        this.feedArrayAdapter = new FeedArrayAdapter(this, android.R.layout.simple_list_item_1);
        feedListView.setAdapter(this.feedArrayAdapter);
        feedListView.setOnItemClickListener(onFeedListItemClickListener);

        ListView itemListView = (ListView) this.findViewById(R.id.item_list);
        this.itemArrayAdapter = new ItemArrayAdapter(this, android.R.layout.simple_list_item_1);
        itemListView.addFooterView(this.getLayoutInflater()
                .inflate(R.layout.adapter_view_item_list_loading_footer, null));
        itemListView.setAdapter(this.itemArrayAdapter);
        itemListView.setOnItemClickListener(onItemListItemClickListener);
        itemListView.setOnScrollListener(onItemListScrollListener);

        DisplayItems.getInstance().registerListener(this.displayItemsListener);

        pullToRefreshAttacher = PullToRefreshAttacher.get(this);
        pullToRefreshAttacher.addRefreshableView(itemListView, this.onPullToRefreshListener);
        pullToRefreshAttacher.setEnabled(false);

        this.findViewById(R.id.main_activity_error_panel)
                .setOnClickListener(onErrorPanelClickListener);

        this.retrieveAccount();

    }

    /**
     * Retrieves an Account object by first attempting to retrieve one from a previous session, then
     * if that fails launching an AuthenticateActivity to retrieve an Account from the user.
     */
    private void retrieveAccount() {

        final PreviousAccountInfoFile infoFile = this.getPreviousAccountInfoFile();

        new AsyncTask<PreviousAccountInfoFile, Void, Account>() {

            @Override
            protected Account doInBackground(PreviousAccountInfoFile... previousAccountInfoFiles) {

                return previousAccountInfoFiles[0].getPreviousAccount();
            }

            @Override
            protected void onPostExecute(Account accountFromPreviousSession) {
                if (accountFromPreviousSession != null) {
                    onHaveAuthenticatedAccount(accountFromPreviousSession);
                } else {
                    launchAuthenticateActivity();
                }
            }
        }.execute(infoFile);
    }

    /**
     * Launch an AuthenticateActivity to retrieve an Account from the user.
     */
    private void launchAuthenticateActivity() {
        ActivityEscrow.getInstance().put(AuthenticateActivity.PREVIOUS_ACCOUNT_INFO_FILE_CODE,
                this.getPreviousAccountInfoFile());
        Intent authenticationActivityIntent = new Intent(this, AuthenticateActivity.class);
        this.startActivityForResult(authenticationActivityIntent,
                AUTHENTICATE_ACTIVITY_REQUEST_CODE);
    }

    /**
     * Retrieve the file containing information about an Account stored in a recent session.
     */
    private PreviousAccountInfoFile getPreviousAccountInfoFile() {
        return PreviousAccountInfoFile
                .getInfoFile(getApplicationContext().getFilesDir(), DEFAULT_INFOFILE_FILENAME);
    }

    /**
     * Handles the result returned from the AuthenticateActivity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTHENTICATE_ACTIVITY_REQUEST_CODE) {

            Account accountFromAuthenticateActivity
                    = (Account) ActivityEscrow.getInstance()
                    .getAndDeallocate(AuthenticateActivity.RETURNED_ACCOUNT_CODE);

            if (resultCode == RESULT_OK
                    && accountFromAuthenticateActivity != null
                    && accountFromAuthenticateActivity.hasToken()) {
                this.onHaveAuthenticatedAccount(accountFromAuthenticateActivity);
            } else {
                this.retrieveAccount();
            }
        }
    }

    /**
     * Called once we have an account that has been authenticated (ie it is logged in).
     */
    private void onHaveAuthenticatedAccount(Account authenticatedAccount) {
        this.account = authenticatedAccount;
        this.populateViews();
    }

    /**
     * Populate the Item list and the Feeds list.
     */
    private void populateViews() {
        DisplayItems.getInstance()
                .setFeed(account.getReadingList(this.getString(R.string.all_items)));
        DisplayItems.getInstance().populate();
        this.displayFeeds = new DisplayFeeds(this.account, this);
        this.displayFeeds.registerListener(this.displayFeedsListener);
        this.displayFeeds.populate();
    }

    /**
     * Called when we experience an AuthenticationException. Currently this is handled by simply
     * launching an AuthenticateActivity and prompting the user to log in again.
     */
    private void handleAuthenticationException(AuthenticationException e) {
        this.launchAuthenticateActivity();
    }

    /**
     * Handles an IOException on loadMore event.
     */
    private void handleIOExceptionOnLoadMore(IOException e) {
        if (e instanceof HttpException &&
                (((HttpException) e).getHttpStatus() == HttpStatusCode.FORBIDDEN
                        || ((HttpException) e).getHttpStatus() == HttpStatusCode.UNAUTHORIZED)) {
            this.handleAuthenticationException(new AuthenticationException(e));
        } else {
            // TODO prevent this from firing multiple times when the user scrolls to the bottom of
            // the item list.
            Toast.makeText(this,
                    this.getString(R.string.connection_error) + " " + e.getLocalizedMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handles an IOException on populate or refresh events.
     */
    private void handleIOExceptionOnPopulateOrRefresh(IOException e) {
        if (e instanceof HttpException &&
                (((HttpException) e).getHttpStatus() == HttpStatusCode.FORBIDDEN
                        || ((HttpException) e).getHttpStatus() == HttpStatusCode.UNAUTHORIZED)) {
            this.handleAuthenticationException(new AuthenticationException(e));
        } else {
            this.findViewById(R.id.drawer_layout_parent).setVisibility(View.GONE);

            TextView errorText = (TextView) this.findViewById(R.id.main_activity_error_panel_text);
            errorText.setText(this.getString(R.string.connection_error)
                    + "\n" + e.getLocalizedMessage());

            this.findViewById(R.id.main_activity_error_panel).setVisibility(View.VISIBLE);
        }
    }

    /**
     * Handle a Throwable that we have otherwise failed to handle. Currently this is simply
     * re-thrown in a wrapping RuntimeException.
     */
    private void handleUnexpectedThrowable(Throwable e) {
        throw new RuntimeException(e);
    }

    /**
     * ItemClickListener for handling an itemClick event on a feed item. This object
     * is registered as a listener in the onCreate method.
     */
    private AdapterView.OnItemClickListener onFeedListItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            ((DrawerLayout) findViewById(R.id.drawer_layout_parent)).closeDrawers();
            DisplayItems.getInstance().setFeed((Feed) adapterView.getItemAtPosition(position));
            DisplayItems.getInstance().populate();
        }
    };

    /**
     * ItemClickListener for handling an itemClick event on an Item. This object
     * is registered as a listener in the onCreate method.
     */
    private AdapterView.OnItemClickListener onItemListItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Intent itemDetailActivity = new Intent(MainActivity.this, ItemDetailActivity.class);

            itemDetailActivity.putExtra(ItemDetailActivity.INITIAL_ITEM, position);

            startActivity(itemDetailActivity);

        }
    };

    /**
     * ScrollListener for handling scroll events, so as to fire a loadMore request
     * when the user scrolls to the bottom of the list. This object is registered as a
     * listener in the onCreate method.
     */
    private AbsListView.OnScrollListener onItemListScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                             int totalItemCount) {

            if (!DisplayItems.getInstance().isLoadingItems()
                    && firstVisibleItem + visibleItemCount >= totalItemCount
                    && totalItemCount > 1) {

                DisplayItems.getInstance().loadMoreItems();
            }

        }
    };

    /**
     * DisplayFeedsListener for handling changes in the displayed Feeds. This object is registered
     * as a listener in the onCreate method.
     */
    private DisplayFeeds.DisplayFeedsListener displayFeedsListener = new DisplayFeeds.DisplayFeedsListener() {
        @Override
        public void onPopulatePreExecute() {
            // TODO show progressbar in drawer
        }

        @Override
        public void onPopulateProgress(DisplayFeeds.PopulateProgress progress) {
            // TODO show progress in drawer
        }

        @Override
        public void onPopulateComplete() {
            feedArrayAdapter.clear();
            feedArrayAdapter.addAll(displayFeeds.getLoadedFeeds());
            feedArrayAdapter.notifyDataSetChanged();
        }

        @Override
        public void onPopulateError(Throwable cause) {
            if (cause instanceof AuthenticationException) {
                handleAuthenticationException((AuthenticationException) cause);
            } else if (cause instanceof IOException) {
                handleIOExceptionOnPopulateOrRefresh((IOException) cause);
            } else {
                handleUnexpectedThrowable(cause);
            }
        }
    };

    /**
     * DisplayItemsListener for handling changes to the displayed Items. This object is registered
     * as a listener in the onCreate method.
     */
    private DisplayItems.DisplayItemsListener displayItemsListener = new DisplayItems.DisplayItemsListener() {

        @Override
        public void onPopulatePreExecute() {
            itemArrayAdapter.clear();
            itemArrayAdapter.notifyDataSetChanged();
            pullToRefreshAttacher.setEnabled(false);
        }

        @Override
        public void onPopulateProgress(DisplayItems.PopulateProgress progress) {
        }

        @Override
        public void onPopulateComplete() {
            itemArrayAdapter.clear();
            itemArrayAdapter.addAll(DisplayItems.getInstance().getLoadedItems());
            itemArrayAdapter.notifyDataSetChanged();
            pullToRefreshAttacher.setEnabled(true);
        }

        @Override
        public void onPopulateError(Throwable cause) {
            if (cause instanceof AuthenticationException) {
                handleAuthenticationException((AuthenticationException) cause);
            } else if (cause instanceof IOException) {
                handleIOExceptionOnPopulateOrRefresh((IOException) cause);
            } else {
                handleUnexpectedThrowable(cause);
            }
        }

        @Override
        public void onLoadMoreItemsPreExecute() {

        }

        @Override
        public void onLoadMoreItemsProgress(DisplayItems.LoadMoreProgress progress) {

        }

        @Override
        public void onLoadMoreItemsComplete(List<Item> newItems) {
            itemArrayAdapter.addAll(newItems);
            itemArrayAdapter.notifyDataSetChanged();
        }

        @Override
        public void onLoadMoreItemsError(Throwable cause) {
            if (cause instanceof AuthenticationException) {
                handleAuthenticationException((AuthenticationException) cause);
            } else if (cause instanceof IOException) {
                handleIOExceptionOnLoadMore((IOException) cause);
            } else {
                handleUnexpectedThrowable(cause);
            }
        }

        @Override
        public void onRefreshPreExecute() {
            itemArrayAdapter.clear();
            itemArrayAdapter.notifyDataSetChanged();
        }

        @Override
        public void onRefreshProgress(DisplayItems.RefreshProgress progress) {

        }

        @Override
        public void onRefreshComplete() {
            pullToRefreshAttacher.setRefreshComplete();
            itemArrayAdapter.clear();
            itemArrayAdapter.addAll(DisplayItems.getInstance().getLoadedItems());
            itemArrayAdapter.notifyDataSetChanged();
        }

        @Override
        public void onRefreshError(Throwable cause) {
            pullToRefreshAttacher.setRefreshComplete();
            if (cause instanceof AuthenticationException) {
                handleAuthenticationException((AuthenticationException) cause);
            } else if (cause instanceof IOException) {
                handleIOExceptionOnPopulateOrRefresh((IOException) cause);
            } else {
                handleUnexpectedThrowable(cause);
            }
        }

        @Override
        public void onChangeFeed() {

        }
    };

    /**
     * OnRefreshListener for handling the pull-to-refresh onRefresh event. This is registered as a
     * listener in the onCreate method.
     */
    private PullToRefreshAttacher.OnRefreshListener onPullToRefreshListener
            = new PullToRefreshAttacher.OnRefreshListener() {
        @Override
        public void onRefreshStarted(View view) {
            DisplayItems.getInstance().refresh();
        }
    };

    /**
     * Handles a click event on the "error panel" view which is displayed when certain errors occur.
     * This is registered as a listener in the onCreate method.
     */
    private View.OnClickListener onErrorPanelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            findViewById(R.id.main_activity_error_panel).setVisibility(View.GONE);
            findViewById(R.id.drawer_layout_parent).setVisibility(View.VISIBLE);
            populateViews();
        }
    };
}