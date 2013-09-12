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
import java.util.Date;
import java.util.Set;

/**
 * Class representing an item in a Feed, ie an individual post.
 */
public class Item implements Comparable<Item>, Serializable {

    private String id;
    private String title;
    private String canonicalLink;
    private String alternateLink;
    private Date publishedTimestamp;
    private Date updatedTimestamp;
    private Date crawlTimestamp;
    private String summary;
    private String author;
    private Set<Category> categories;
    private Subscription parentSubscription;
    private ReadStatus readStatus;

    /**
     * Full constructor.
     */
    public Item(String id, String title, String canonicalLink,
                String alternateLink, Date publishedTimestamp,
                Date updatedTimestamp, Date crawlTimestamp,
                String summary, String author,
                Set<Category> categories,
                Subscription parentSubscription,
                ReadStatus readStatus) {
        this.id = id;
        this.title = title;
        this.canonicalLink = canonicalLink;
        this.alternateLink = alternateLink;
        this.publishedTimestamp = publishedTimestamp;
        this.updatedTimestamp = updatedTimestamp;
        this.crawlTimestamp = crawlTimestamp;
        this.summary = summary;
        this.author = author;
        this.categories = categories;
        this.parentSubscription = parentSubscription;
        this.readStatus = readStatus;
    }

    /*** Accessors and mutators ***/

    public String getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public String getCanonicalLink() {
        return this.canonicalLink;
    }

    public String getAlternateLink() {
        return this.alternateLink;
    }

    public Date getPublishedTimestamp() {
        return this.publishedTimestamp;
    }

    public Date getUpdatedTimestamp() {
        return this.updatedTimestamp;
    }

    public Date getCrawlTimestamp() {
        return this.crawlTimestamp;
    }

    public String getSummary() {
        return this.summary;
    }

    public Set<Category> getCategories() {
        return this.categories;
    }

    public String getAuthor() {
        return this.author;
    }

    public Subscription getParentSubscription() {
        return this.parentSubscription;
    }

    public void setReadStatus(ReadStatus readStatus) {
        this.readStatus = readStatus;
    }

    public ReadStatus getReadStatus() {
        return readStatus;
    }

    /*** Utility methods ***/

    @Override
    public int compareTo(Item otherItem) {
        if (this.publishedTimestamp.compareTo(otherItem.publishedTimestamp) != 0) {
            return this.publishedTimestamp.compareTo(otherItem.publishedTimestamp);
        } else {
            return this.id.compareTo(otherItem.id);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Item item = (Item) o;

        return !(id != null ? !id.equals(item.id) : item.id != null);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
