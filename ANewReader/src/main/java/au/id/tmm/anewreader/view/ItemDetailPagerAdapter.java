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

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

import au.id.tmm.anewreader.model.Item;

/**
 * PagerAdapter for managing the Fragments that make up the ItemDetailActivity.
 */
public class ItemDetailPagerAdapter extends FragmentStatePagerAdapter {

    public ItemDetailPagerAdapter(FragmentManager fm) {
        super(fm);
        DisplayItems.getInstance().registerListener(this.displayItemsListener);
    }

    @Override
    public Fragment getItem(int i) {
        return new ItemDetailFragment(DisplayItems.getInstance().getLoadedItems().get(i));
    }

    @Override
    public int getCount() {
        return DisplayItems.getInstance().getLoadedItems().size();
    }

    /**
     * Listener for changes in the list of displayed Items, so that such changes can be represented
     * in the Activity.
     */
    private DisplayItemsListener displayItemsListener = new DisplayItemsListener() {

        @Override
        public void onPopulatePreExecute() {
        }

        @Override
        public void onPopulateProgress(DisplayItems.PopulateProgress progress) {
        }

        @Override
        public void onPopulateComplete() {
            notifyDataSetChanged();
        }

        @Override
        public void onPopulateError(Throwable cause) {

        }

        @Override
        public void onLoadMoreItemsPreExecute() {

        }

        @Override
        public void onLoadMoreItemsProgress(DisplayItems.LoadMoreProgress progress) {

        }

        @Override
        public void onLoadMoreItemsComplete(List<Item> newItems) {
            notifyDataSetChanged();
        }

        @Override
        public void onLoadMoreItemsError(Throwable cause) {

        }

        @Override
        public void onRefreshPreExecute() {

        }

        @Override
        public void onRefreshProgress(DisplayItems.RefreshProgress progress) {

        }

        @Override
        public void onRefreshComplete() {
            notifyDataSetChanged();
        }

        @Override
        public void onRefreshError(Throwable cause) {

        }

        @Override
        public void onChangeFeed() {

        }
    };

}
