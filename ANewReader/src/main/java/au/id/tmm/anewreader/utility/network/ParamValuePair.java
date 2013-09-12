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

/**
 * Utility class for storing a parameter-value pair, particularly for HTTP headers and parameters
 * in a GET or POST request.
 */
public class ParamValuePair {
    String param;
    String value;

    /*** Constructors ***/

    /**
     * Full constructor, defines pair with given parameter and value.
     */
    public ParamValuePair(String param, String value) {
        this.param = param;
        this.value = value;
    }

    /*** Utility methods ***/

    /**
     * Returns object by a string of the form "param=value".
     */
    @Override
    public String toString() {
        return this.param + "=" + this.value;
    }

    /*** Static members ***/

    /**
     * Static utility method for combining a set of parameter-value pairs into a single string
     * suitable for a GET request. Returns a string of the form "param1=value1&param2=value2".
     */
    public static String combineAsString(ParamValuePair... pairs) {
        StringBuilder returnedString = new StringBuilder();

        for (ParamValuePair currentPair : pairs) {
            returnedString.append(currentPair.toString()).append("&");
        }

        returnedString.deleteCharAt(returnedString.length() - 1);

        return returnedString.toString();
    }
}

