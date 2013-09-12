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

package au.id.tmm.anewreader.model.net;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

import au.id.tmm.anewreader.model.ReaderService;
import au.id.tmm.anewreader.utility.network.AuthenticationException;
import au.id.tmm.anewreader.utility.network.AuthenticationHelper;
import au.id.tmm.anewreader.utility.network.HttpException;
import au.id.tmm.anewreader.utility.network.HttpStatusCode;
import au.id.tmm.anewreader.utility.network.ParamValuePair;

/**
 * {@link au.id.tmm.anewreader.utility.network.AuthenticationHelper} for simplifying the
 * process of authenticating to a {@link au.id.tmm.anewreader.model.ReaderService}.
 */
public class ReaderServiceAuthenticationHelper implements AuthenticationHelper {

    private static final String CLIENT_NAME_PARAM = "client";
    private static final String ACCOUNT_TYPE_PARAM = "accountType";
    private static final String LOGIN_SERVICE_PARAM = "service";
    private static final String EMAIL_PARAM = "Email";
    private static final String PASSWORD_PARAM = "Passwd";
    private static final String OUTPUT_TYPE_PARAM = "output";

    private static final String DEFAULT_CLIENT_NAME_VALUE = "ANewReader";
    private static final AccountType DEFAULT_ACCOUNT_TYPE = AccountType.HOSTED;
    private static final String DEFAULT_LOGIN_SERVICE_VALUE = "reader";
    private static final String DEFAULT_OUTPUT_TYPE_VALUE = "json";

    private static final String AUTHORISATION_FIELD_TITLE = "Authorization";
    private static final String AUTHORISATION_FIELD_PREFIX = "GoogleLogin auth=";

    private static final String RESPONSE_AUTH_FIELD_TITLE = "Auth";

    private ReaderService readerService;
    private String clientName;
    private AccountType accountType;
    private String loginService;
    private String outputType;

    private String authToken;

    /*** Constructors ***/

    /**
     * Full constructor.
     */
    private ReaderServiceAuthenticationHelper(ReaderService readerService, String clientName,
                                              AccountType accountType, String loginService,
                                              String outputType) {
        this.readerService = readerService;
        this.clientName = clientName;
        this.accountType = accountType;
        this.loginService = loginService;
        this.outputType = outputType;
    }

    /**
     * Returns an instance for the given ReaderService.
     */
    public static ReaderServiceAuthenticationHelper generate(ReaderService readerService) {
        return new ReaderServiceAuthenticationHelper(readerService, DEFAULT_CLIENT_NAME_VALUE,
                DEFAULT_ACCOUNT_TYPE, DEFAULT_LOGIN_SERVICE_VALUE, DEFAULT_OUTPUT_TYPE_VALUE);
    }

    /**
     * Returns an instance for the given ReaderService, while setting the authentication token to
     * the given token. This can be used to maintain an authentication session by (for example)
     * storing the token to disk, then reconstructing an AuthenticationHelper from the old token.
     */
    public static ReaderServiceAuthenticationHelper generate(ReaderService readerService, String token) {
        ReaderServiceAuthenticationHelper authHelper
                = ReaderServiceAuthenticationHelper.generate(readerService);
        authHelper.authToken = token;
        return authHelper;
    }

    /**
     * Authenticates this instance to the ReaderService using the given authentication credentials.
     */
    public void authenticate(String username, String password) throws IOException {

        String authenticateResponse = null;

        try {
            authenticateResponse = new ReaderServiceRequestHelper()
                    .performPostRequest(this.readerService.getBaseUrl() + "/accounts/ClientLogin",
                            new ParamValuePair(CLIENT_NAME_PARAM, clientName),
                            new ParamValuePair(ACCOUNT_TYPE_PARAM, accountType.toString()),
                            new ParamValuePair(LOGIN_SERVICE_PARAM, loginService),
                            new ParamValuePair(EMAIL_PARAM, username),
                            new ParamValuePair(PASSWORD_PARAM, password),
                            new ParamValuePair(OUTPUT_TYPE_PARAM, outputType));

            JSONObject jsonResponse = new JSONObject(authenticateResponse);

            this.authToken = jsonResponse.getString(RESPONSE_AUTH_FIELD_TITLE);
        } catch (JSONException e) {
            if (authenticateResponse != null) {
                throw new AuthenticationException(authenticateResponse);
            } else {
                throw new AuthenticationException(e);
            }
        }
    }

    /**
     * Queries the service for the current token, confirming it is the same as the one stored in
     * this instance. Returns true if it is, false otherwise.
     */
    public boolean isLoggedIn() throws IOException {
        if (this.hasToken()) {
            try {
                if (this.getCurrentTokenFromService().equals(this.authToken)) {
                    return true;
                } else {
                    this.invalidateToken();
                    return false;
                }
            } catch (IOException e) {
                if (e instanceof HttpException) {
                    HttpException httpException = (HttpException) e;
                    if (httpException.getHttpStatus() == HttpStatusCode.UNAUTHORIZED
                            || httpException.getHttpStatus() == HttpStatusCode.FORBIDDEN) {
                        this.invalidateToken();
                        return false;
                    }
                }
                throw e;
            }
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpURLConnection addAuthenticationHeaders(HttpURLConnection connection) {
        if (this.hasToken()) {
            connection.addRequestProperty(AUTHORISATION_FIELD_TITLE, AUTHORISATION_FIELD_PREFIX + this.authToken);
            return connection;
        } else {
            throw new IllegalStateException("Attempted to add authentication headers with out " +
                    "retrieving token");
        }
    }

    public void invalidateToken() {
        this.authToken = null;
    }

    public boolean hasToken() {
        return this.authToken != null;
    }

    public String getToken() {
        return this.authToken;
    }

    /**
     * Enumerates the different types of accounts. A Hosted account authenticates with a username
     * and password, other account types are not currently supported.
     */
    public enum AccountType {
        HOSTED, GOOGLE;

        private static final String HOSTED_STRING = "HOSTED";
        private static final String GOOGLE_STRING = "GOOGLE";

        @Override
        public String toString() {
            switch (this) {
                case HOSTED:
                    return HOSTED_STRING;
                case GOOGLE:
                    return GOOGLE_STRING;
                default:
                    throw new UnsupportedOperationException();
            }
        }
    }

    /**
     * Utility function for querying the ReaderService for the current token, and returning it.
     * @throws IOException
     */
    private String getCurrentTokenFromService() throws IOException {
        ReaderServiceRequestHelper requestHelper = new ReaderServiceRequestHelper(this);

        return requestHelper.performGetRequest(this.readerService.getBaseUrl() + "/reader/api/0/token");
    }

}
