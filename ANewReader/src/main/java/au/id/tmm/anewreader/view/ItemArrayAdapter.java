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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DateFormat;

import au.id.tmm.anewreader.R;
import au.id.tmm.anewreader.model.Item;
import au.id.tmm.anewreader.model.ReadStatus;

public class ItemArrayAdapter extends ArrayAdapter<Item> {

    public ItemArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Item currentItem = super.getItem(position);

        LayoutInflater inflater = LayoutInflater.from(this.getContext());

        View rowView = inflater.inflate(R.layout.adapter_view_item_list_item, parent, false);

        ((TextView) rowView.findViewById(R.id.item_list_item_layout_title))
                .setText(currentItem.getTitle().replace('\n', ' '));
        ((TextView) rowView.findViewById(R.id.item_list_item_layout_subscription_title))
                .setText(currentItem.getParentSubscription().getTitle());
        ((TextView) rowView.findViewById(R.id.item_list_item_layout_date))
                .setText(DateFormat.getDateTimeInstance()
                        .format(currentItem.getCrawlTimestamp()));

        ViewGroup rootViewGroup = (ViewGroup) rowView.findViewById(R.id.item_list_item_layout_root);

        if (currentItem.getReadStatus() == ReadStatus.READ) {
            rootViewGroup.setBackgroundColor(this.getContext().getResources().getColor(R.color.read_background_colour));
        }

        return rowView;
    }
}
