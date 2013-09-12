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

/**
 * Class representing the "Reading List" of an account, that is, the list of all items from all
 * subscriptions.
 */
public class ReadingList extends Feed {

    private static final String READING_LIST_SUFFIX = "user/-/state/com.google/reading-list";

    private String feedAddress;
    private String title;

    public ReadingList(Model parentModel, String title, int unreadCount) {
        super(parentModel, unreadCount);
        this.title = title;
        this.feedAddress = READING_LIST_SUFFIX;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    String getFeedAddress() {
        return this.feedAddress;
    }
}
