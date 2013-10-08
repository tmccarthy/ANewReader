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

package au.id.tmm.anewreader.utility.network;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;

/**
 * Interface defining the functionality of a RequestHelper. A RequestHelper simplifies the process
 * of performing HTTP requests.
 */
public interface RequestHelper {

    /**
     * Perform a GET request of the given URI.
     * @throws IOException
     * @throws URISyntaxException
     */
    public String performGetRequest(String uri)
            throws IOException, URISyntaxException;

    /**
     * Perform a POST request of the given URI, providing the given parameter-value pairs as the
     * body of the request.
     * @throws IOException
     * @throws URISyntaxException
     */
    public String performPostRequest(String uri, Collection<ParamValuePair> pairs)
            throws IOException, URISyntaxException;

    /**
     * Perform a POST request of the given URI, providing the given parameter-value pairs as the
     * body of the request.
     * @throws IOException
     * @throws URISyntaxException
     */
    public String performPostRequest(String uri, ParamValuePair... pairs)
            throws IOException, URISyntaxException;

    /**
     * Perform a POST request of the given URI, providing the given string as the body of the
     * request.
     * @throws IOException
     * @throws URISyntaxException
     */
    public String performPostRequest(String uri, String body)
            throws IOException, URISyntaxException;

    /**
     * Perform a PUT request of the given URI, providing the given parameter-value pairs as the
     * body of the request.
     * @throws IOException
     * @throws URISyntaxException
     */
    public String performPutRequest(String uri, Collection<ParamValuePair> pairs)
            throws IOException, URISyntaxException;

    /**
     * Perform a PUT request of the given URI, providing the given parameter-value pairs as the
     * body of the request.
     * @throws IOException
     * @throws URISyntaxException
     */
    public String performPutRequest(String uri, ParamValuePair... pairs)
            throws IOException, URISyntaxException;

    /**
     * Perform a PUT request of the given URI, providing the given string as the body of the
     * request.
     * @throws IOException
     * @throws URISyntaxException
     */
    public String performPutRequest(String uri, String body)
            throws IOException, URISyntaxException;

    /**
     * Perform a DELETE request of the given URI.
     * @throws IOException
     * @throws URISyntaxException
     */
    public String performDeleteRequest(String uri)
            throws IOException, URISyntaxException;

}
