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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

public abstract class Feed {

    private static final int DEFAULT_NUM_ITEM_LIMIT = 20;

    private Model parentModel;

    private int unreadCount;

    protected Feed(Model parentModel, int unreadCount) {
        this.parentModel = parentModel;
        this.unreadCount = unreadCount;
    }

    public int getUnreadCount() {
        return this.unreadCount;
    }

    protected void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    protected Model getParentModel() {
        return this.parentModel;
    }

    public abstract String getTitle();

    abstract String getFeedAddress();

    String getEncodedFeedAddress() {
        try {
            return URLEncoder.encode(this.getFeedAddress(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // This should never occur, so thrown as a RuntimeException
            throw new RuntimeException(e);
        }
    }

    public ListWithContinuation<Item> getItems() throws IOException {
        return this.getItems((Continuation) null);
    }

    public ListWithContinuation<Item> getItems(ReadStatus readStatus) throws IOException {
        return this.getItems(readStatus, (Date) null);
    }

    public ListWithContinuation<Item> getItems(ReadStatus readStatus, Date olderThan) throws IOException {
        return this.getItems(readStatus, olderThan, DEFAULT_NUM_ITEM_LIMIT);
    }

    public ListWithContinuation<Item> getItems(ReadStatus readStatus, Date olderThan, int numItemsLimit) throws IOException {
        return this.getItems(readStatus, olderThan, numItemsLimit, null);
    }

    public ListWithContinuation<Item> getItems(Continuation continuation) throws IOException {
        return this.getItems(null, continuation);
    }

    public ListWithContinuation<Item> getItems(ReadStatus readStatus, Continuation continuation) throws IOException {
        return this.getItems(readStatus, null, continuation);
    }

    public ListWithContinuation<Item> getItems(ReadStatus readStatus, Date olderThan, Continuation continuation) throws IOException {
        return this.getItems(readStatus, olderThan, DEFAULT_NUM_ITEM_LIMIT, continuation);
    }

    private ListWithContinuation<Item> getItems(ReadStatus readStatus, int numItemLimit, Continuation continuation) throws IOException {
        return this.getItems(readStatus, null, numItemLimit, continuation);
    }

    public ListWithContinuation<Item> getItems(ReadStatus readStatus, Date olderThan, int numItemsLimit, Continuation continuation) throws IOException {
        return this.parentModel.getItems(this, readStatus, numItemsLimit, olderThan, continuation);
    }

}
