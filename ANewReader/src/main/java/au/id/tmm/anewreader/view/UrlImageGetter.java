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
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Asynchronously retrieves images from urls and handles the refreshing of views once the
 * images are loaded.
 */
public class UrlImageGetter implements Html.ImageGetter {

    private final View parent;
    private final Context context;

    public UrlImageGetter(View parent, Context context) {
        this.parent = parent;
        this.context = context;
    }

    @Override
    public Drawable getDrawable(String source) {
        UrlDrawable urlDrawable = new UrlDrawable();

        new ImageGetterAsyncTask(urlDrawable).execute(source);

        return urlDrawable;
    }

    private class ImageGetterAsyncTask extends AsyncTask<String, Void, Drawable> {

        private final UrlDrawable urlDrawable;

        public ImageGetterAsyncTask(UrlDrawable urlDrawable) {
            this.urlDrawable = urlDrawable;
        }

        @Override
        protected Drawable doInBackground(String... strings) {
            String source = strings[0];
            return fetchDrawable(source);
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            if (drawable == null) {
                drawable = context.getResources()
                        .getDrawable(android.R.drawable.stat_notify_error);
            }

            drawable.setBounds(0, 0, 0 + drawable.getIntrinsicWidth(),
                    0 + drawable.getIntrinsicHeight());

            urlDrawable.setDrawable(drawable);

            parent.setMinimumHeight(parent.getHeight() + drawable.getIntrinsicHeight());

            if (parent instanceof TextView) {
                TextView parentTextView = (TextView) parent;
                parentTextView.setText(parentTextView.getText());
            }
            parent.invalidate();
        }
    }

    public Drawable fetchDrawable(String url) {
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();

            Drawable drawable = Drawable.createFromStream(urlConnection.getInputStream(), "src");
            drawable.setBounds(0, 0, 0 + drawable.getIntrinsicWidth(),
                    0 + drawable.getIntrinsicHeight());
            return drawable;
        } catch (Exception e) {
            return null;
        }
    }

}
