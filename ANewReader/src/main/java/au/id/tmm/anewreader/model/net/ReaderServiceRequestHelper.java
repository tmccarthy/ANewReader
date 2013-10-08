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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import au.id.tmm.anewreader.utility.IOUtils;
import au.id.tmm.anewreader.utility.network.AuthenticationHelper;
import au.id.tmm.anewreader.utility.network.HttpException;
import au.id.tmm.anewreader.utility.network.HttpStatusCode;
import au.id.tmm.anewreader.utility.network.ParamValuePair;
import au.id.tmm.anewreader.utility.network.RequestHelper;

/**
 * Utility class for simplifying HTTP requests to a ReaderService.
 */
public class ReaderServiceRequestHelper implements RequestHelper {

    private static final String DEFAULT_REQUEST_ENCODING = "UTF-8";
    private static final String ASSUMED_RESPONSE_ENCODING = "UTF-8";
    private static final String CONTENT_TYPE_TITLE = "Content-Type";
    private static final String DEFAULT_CONTENT_TYPE = "application/x-www-form-urlencoded";

    private String contentType;
    private AuthenticationHelper authHelper;

    /**
     * Full constructor.
     */
    public ReaderServiceRequestHelper(AuthenticationHelper helper, String contentType) {
        this.authHelper = helper;
        this.contentType = contentType;
    }

    /**
     * This constructor does not provide an AuthenticationHelper, and so a Request Helper so
     * constructed will not add authentication headers to requests. This should typically only be
     * used by an AuthenticationHelper itself when initially retrieving a token.
     */
    private ReaderServiceRequestHelper(String contentType) {
        this(null, contentType);
    }

    /**
     * This constructor does not provide an AuthenticationHelper, and so a Request Helper so
     * constructed will not add authentication headers to requests. This should typically only be
     * used by an AuthenticationHelper itself when initially retrieving a token.
     */
    protected ReaderServiceRequestHelper() {
        this(DEFAULT_CONTENT_TYPE);
    }

    public ReaderServiceRequestHelper(AuthenticationHelper authenticationHelper) {
        this(authenticationHelper, DEFAULT_CONTENT_TYPE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String performGetRequest(String uri) throws IOException {

        HttpURLConnection urlConnection = (HttpURLConnection) new URL(uri).openConnection();

        this.addAuthenticationHeaders(urlConnection);
        this.addContentTypeHeader(urlConnection);

        String responseEncoding = ASSUMED_RESPONSE_ENCODING;

        try {
            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
            responseEncoding = this.getEncodingFromContentType(urlConnection.getContentType());

            return IOUtils.stringFromInputSteam(inputStream, responseEncoding != null ? responseEncoding : ASSUMED_RESPONSE_ENCODING);

        } catch (IOException e) {
            if (urlConnection.getResponseCode() != -1) {
                InputStream errorInputStream = new BufferedInputStream(urlConnection.getErrorStream());
                throw new HttpException(
                        IOUtils.stringFromInputSteam(errorInputStream, responseEncoding),
                        HttpStatusCode.getForIntCode(urlConnection.getResponseCode()), e);
            } else {
                throw e;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    @Override
    public String performPostRequest(String uri, Collection<ParamValuePair> pairs)
            throws IOException, URISyntaxException {
        return this.performPostRequest(uri, pairs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String performPostRequest(String uri, ParamValuePair... pairs)
            throws IOException {
        return this.performPostRequest(uri, ParamValuePair.combineAsString(pairs));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String performPostRequest(String uri, String body)
            throws IOException {

        HttpURLConnection urlConnection = (HttpURLConnection) new URL(uri).openConnection();

        this.addAuthenticationHeaders(urlConnection);
        this.addContentTypeHeader(urlConnection);


        try {
            byte[] bufferToWrite = body.getBytes(DEFAULT_REQUEST_ENCODING);

            urlConnection.setFixedLengthStreamingMode(bufferToWrite.length);
            urlConnection.setDoOutput(true);

            OutputStream outputStream = new BufferedOutputStream(urlConnection.getOutputStream());
            outputStream.write(bufferToWrite);
            outputStream.close();

            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());

            return IOUtils.stringFromInputSteam(inputStream,
                    this.getEncodingFromContentType(urlConnection.getContentType()));

        } finally {
            urlConnection.disconnect();
        }
    }

    @Override
    public String performPutRequest(String uri, Collection<ParamValuePair> pairs) throws IOException, URISyntaxException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String performPutRequest(String uri, ParamValuePair... pairs)
            throws IOException, URISyntaxException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String performPutRequest(String uri, String body)
            throws IOException, URISyntaxException {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String performDeleteRequest(String uri) throws IOException, URISyntaxException {
        throw new UnsupportedOperationException();
    }

    /**
     * Uses the AuthenticationHelper to add authentication headers to the given request.
     */
    private void addAuthenticationHeaders(HttpURLConnection connection) {
        if (this.authHelper != null) {
            this.authHelper.addAuthenticationHeaders(connection);
        }
    }

    /**
     * Adds the content-type header to the given request.
     */
    private HttpURLConnection addContentTypeHeader(HttpURLConnection connection) {
        connection.addRequestProperty(CONTENT_TYPE_TITLE, contentType);
        return connection;
    }

    /**
     * Extract the content encoding (or charset) from the Content Type string. Returns the
     * value of the charset parameter, or {@value
     * au.id.tmm.anewreader.model.net.ReaderServiceRequestHelper#ASSUMED_RESPONSE_ENCODING}
     * if there was an error retrieving it.
     */
    private String getEncodingFromContentType(String contentType) {
        Pattern extractEncodingPattern = Pattern.compile("^.*?charset=([^;]+);?(?:.*)$");
        Matcher matcher = extractEncodingPattern.matcher(contentType);

        return matcher.find() ? matcher.group(1) : ASSUMED_RESPONSE_ENCODING;
    }
}
