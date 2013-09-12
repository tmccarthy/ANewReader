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

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import au.id.tmm.anewreader.R;
import au.id.tmm.anewreader.model.Account;
import au.id.tmm.anewreader.model.Feed;

public class DisplayFeeds {

    private static final String POPULATE_ON_NO_FEED_ERROR_MESSAGE = "Illegal state, populate called" +
            "when no feed has been set.";

    private Set<DisplayFeedsListener> listeners = new HashSet<DisplayFeedsListener>();

    private boolean loadingFeeds = false;

    private List<Feed> loadedFeeds;
    private Account account;
    private Context context;

    public DisplayFeeds(Account account, Context context) {
        this.account = account;
        this.context = context;
    }

    public void populate() {

        if (this.account == null) {
            throw new IllegalStateException(POPULATE_ON_NO_FEED_ERROR_MESSAGE);
        }

        new AsyncTask<Void, PopulateProgress, List<Feed>>() {

            private Throwable onCancelCause;

            @Override
            protected void onPreExecute() {
                for (DisplayFeedsListener currentListener : listeners) {
                    currentListener.onPopulatePreExecute();
                }
            }

            @Override
            protected List<Feed> doInBackground(Void... voids) {
                try {
                    List<Feed> returnedFeeds = new ArrayList<Feed>();
                    returnedFeeds.add(account.getReadingList(context.getString(R.string.all_items)));
                    returnedFeeds.addAll(account.getCategories());
                return returnedFeeds;
                } catch (Throwable t) {
                    this.onCancelCause = t;
                    this.cancel(false);
                    return null;
                }
            }

            @Override
            protected void onProgressUpdate(PopulateProgress... progress) {
                for (DisplayFeedsListener currentListener : listeners) {
                    currentListener.onPopulateProgress(progress[0]);
                }
            }

            @Override
            protected void onPostExecute(List<Feed> feeds) {
                loadedFeeds = feeds;
                for (DisplayFeedsListener currentListener : listeners) {
                    currentListener.onPopulateComplete();
                }

                loadingFeeds = false;
            }

            @Override
            protected void onCancelled() {
                for (DisplayFeedsListener currentListener : listeners) {
                    currentListener.onPopulateError(this.onCancelCause);
                }

                loadingFeeds = false;
            }
        }.execute();
    }

    public void registerListener(DisplayFeedsListener displayFeedsListener) {
        this.listeners.add(displayFeedsListener);
    }

    public void unRegisterListener(DisplayFeedsListener displayFeedsListener) {
        this.listeners.remove(displayFeedsListener);
    }

    public enum PopulateProgress {
        // TODO sensible values
    }

    public List<Feed> getLoadedFeeds() {
        return loadedFeeds;
    }

    public boolean isLoadingFeeds() {
        return loadingFeeds;
    }
}
