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
import java.io.Serializable;
import java.util.Set;

import au.id.tmm.anewreader.model.net.ReaderServiceAuthenticationHelper;

public class Account implements Serializable {

    private ReaderService readerService;

    private ReaderServiceAuthenticationHelper authHelper;

    private Model model = new Model(this);

    private String username;

    protected Account(String username, ReaderService readerService, ReaderServiceAuthenticationHelper authHelper) {
        this.username = username;
        this.readerService = readerService;
        this.authHelper = authHelper;
    }

    public Account(String username, ReaderService readerService) {
        this(username, readerService, ReaderServiceAuthenticationHelper.generate(readerService));
    }

    public void authenticate(String password) throws IOException {
        this.authHelper.invalidateToken();
        this.authHelper.authenticate(this.username, password);
    }

    /**
     * Returns true if the user is logged in, false otherwise. Note that this method will perform
     * a network operation to determine if the user is logged in, so can't be run on the UI thread.
     */
    public boolean isLoggedIn() throws IOException {
        return this.authHelper.isLoggedIn();
    }

    public boolean hasToken() {
        return this.authHelper.hasToken();
    }

    public ReaderServiceAuthenticationHelper getAuthHelper() {
        return this.authHelper;
    }

    public String getUsername() {
        return this.username;
    }

    public ReaderService getReaderService() {
        return this.readerService;
    }

    public Set<Category> getCategories() throws IOException {
        return this.model.getCategories();
    }

    public Set<Subscription> getSubscriptions() throws IOException {
        return this.model.getSubscriptions();
    }

    public ReadingList getReadingList(String title) {
        return this.model.getReadingList(title);
    }

    void setToken(String token) {
        this.authHelper = ReaderServiceAuthenticationHelper.generate(this.readerService, token);
    }

}
