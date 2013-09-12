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

import au.id.tmm.anewreader.R;
import au.id.tmm.anewreader.model.Feed;

/**
 * ArrayAdapter for the list of Feeds to be displayed to the user.
 */
public class FeedArrayAdapter extends ArrayAdapter<Feed> {

    public FeedArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) super.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.adapter_view_feed_list_item, parent, false);

        TextView feedNameTextView = (TextView) rowView.findViewById(R.id.feed_layout_feed_name);
        feedNameTextView.setText(super.getItem(position).getTitle());

        TextView numUnreadTextView = (TextView) rowView.findViewById(R.id.feed_layout_feed_unread_count);
        numUnreadTextView.setText(String.valueOf(super.getItem(position).getUnreadCount()));

        return rowView;
    }
}
