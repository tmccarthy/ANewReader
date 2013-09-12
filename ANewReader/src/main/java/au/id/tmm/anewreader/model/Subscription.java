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

import java.io.Serializable;
import java.util.Set;

/**
 * Class representing a subscription, the most basic kind of Feed. A subscription can be a member
 * of a Category.
 */
public class Subscription extends Feed implements Comparable<Subscription>, Serializable {

    private static final String FEED_ADDRESS_PREFIX = "feed/";

    private String title;
    private String id;
    private Set<Category> categories;
    private String url;
    private String htmlUrl;
    private String iconUrl;

    public Subscription(String id, String title, Set<Category> categories,
                        String url, String htmlUrl, String iconUrl, int unreadCount,
                        Model parentModel) {
        super(parentModel, unreadCount);
        this.id = id;
        this.title = title;
        this.categories = categories;
        this.url = url;
        this.htmlUrl = htmlUrl;
        this.iconUrl = iconUrl;
    }

    public String getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    @Override
    String getFeedAddress() {
        return FEED_ADDRESS_PREFIX + this.getId();
    }

    public void setTitle(String newTitle) {
        throw new UnsupportedOperationException();
    }

    public Set<Category> getCategories() {
        return this.categories;
    }

    public String getUrl() {
        return this.url;
    }

    public String getHtmlUrl() {
        return this.htmlUrl;
    }

    public String getIconUrl() {
        return this.iconUrl;
    }
    @Override
    public int compareTo(Subscription otherSubscription) {

        if (this.title.compareTo(otherSubscription.title) != 0) {
            return this.title.compareTo(otherSubscription.title);
        } else {
            return this.id.compareTo(otherSubscription.id);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Subscription that = (Subscription) o;

        return !(id != null ? !id.equals(that.id) : that.id != null);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
