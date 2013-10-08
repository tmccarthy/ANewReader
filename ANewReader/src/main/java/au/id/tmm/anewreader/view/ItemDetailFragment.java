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
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import au.id.tmm.anewreader.R;
import au.id.tmm.anewreader.model.Item;

/**
 * Actual fragment that displays the detail of a particular Item (it's title, content etc).
 */
public class ItemDetailFragment extends Fragment {

    private Item item;

    public ItemDetailFragment(Item item) {
        this.item = item;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.adapter_view_item_detail, container, false);

        TextView itemTitle = (TextView) view.findViewById(R.id.item_detail_title);
        TextView subscriptionTitle = (TextView) view.findViewById(R.id.item_detail_subscription_title);
        TextView content = (TextView) view.findViewById(R.id.item_detail_content);

        itemTitle.setText(Html.fromHtml(linkWithText(item.getTitle(), item.getCanonicalLink())));
        itemTitle.setMovementMethod(LinkMovementMethod.getInstance());

        subscriptionTitle.setText(Html.fromHtml(linkWithText(item.getParentSubscription().getTitle(),
                item.getParentSubscription().getHtmlUrl())));
        subscriptionTitle.setMovementMethod(LinkMovementMethod.getInstance());

        UrlImageGetter imageGetter = new UrlImageGetter(content, this.getActivity());

        Spanned htmlSpan = Html.fromHtml(item.getSummary(), imageGetter, null);
        content.setText(htmlSpan);
        content.setMovementMethod(LinkMovementMethod.getInstance());

        return view;

    }

    private String linkWithText(String text, String link) {
        return "<a href=" + link + ">" + text + "</a>";
    }
}
