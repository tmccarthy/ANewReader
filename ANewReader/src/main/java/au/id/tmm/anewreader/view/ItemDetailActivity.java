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

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import au.id.tmm.anewreader.R;

/**
 * Activity for displaying actual Items and their content. Implemented using a PagerAdapter
 * populated with individual Fragments containing the content for an Item.
 */
public class ItemDetailActivity extends FragmentActivity {

    private final static int DEFAULT_INITIAL_ITEM = 0;

    protected final static String INITIAL_ITEM = "INITIAL_ITEM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail_activity);

        int initialItem = this.getIntent().getIntExtra(INITIAL_ITEM, DEFAULT_INITIAL_ITEM);

        ItemDetailPagerAdapter itemAdapter = new ItemDetailPagerAdapter(this.getSupportFragmentManager());

        ViewPager pager = (ViewPager) this.findViewById(R.id.item_view_pager);
        pager.setAdapter(itemAdapter);
        pager.setCurrentItem(initialItem);

    }
}
