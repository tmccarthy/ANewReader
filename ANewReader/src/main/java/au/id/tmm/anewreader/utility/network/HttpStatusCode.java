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

import java.util.HashMap;
import java.util.Map;

/**
 * Enum class representing HTTP status codes.
 */
public enum HttpStatusCode {

    // Informational responses
    CONTINUE                        (100),
    SWITCHNG_PROTOCOL               (101),

    // Successful responses
    OK                              (200),
    CREATED                         (201),
    ACCEPTED                        (202),
    NONAUTHORITATIVE_INFORMATION    (203),
    NO_CONTENT                      (204),
    RESET_CONTENT                   (205),
    PARTIAL_CONTENT                 (206),

    // Redirection responses
    MULTIPLE_CHOICE                 (300),
    MOVED_PERMANENTLY               (301),
    FOUND                           (302),
    SEE_OTHER                       (303),
    NOT_MODIFIED                    (304),
    USE_PROXY                       (305),
    TEMPORARY_REDIRECT              (307),
    PERMANENT_REDIRECT              (308),

    // Client error responses
    BAD_REQUEST                     (400),
    UNAUTHORIZED                    (401),
    PAYMENT_REQUIRED                (402),
    FORBIDDEN                       (403),
    NOT_FOUND                       (404),
    METHOD_NOT_ALLOWED              (405),
    NOT_ACCEPTABLE                  (406),
    PROXY_AUTHENTICATION_REQUIRED   (407),
    REQUEST_TIMEOUT                 (408),
    CONFLICT                        (409),
    GONE                            (410),
    LENGTH_REQUIRED                 (411),
    PRECONDITION_FAILED             (412),
    REQUEST_ENTITY_TOO_LARGE        (413),
    REQUEST_URI_TOO_LONG            (414),
    UNSUPPORTED_MEDIA_TYPE          (415),
    REQUESTED_RANGE_NOT_SATISFIABLE (416),
    EXPECTATION_FAILED              (417),

    // Server error responses
    INTERNAL_SERVER_ERROR           (500),
    NOT_IMPLEMENTED                 (501),
    BAD_GATEWAY                     (502),
    SERVICE_UNAVAILABLE             (503),
    GATEWAY_TIMEOUT                 (504),
    VERSION_NOT_SUPPORTED           (505);

    public final int code;

    /*** Constructors ***/

    private HttpStatusCode(int code) {
        this.code = code;
    }

    /*** Accessors and mutators ***/

    public int getCode() {
        return code;
    }

    /*** Static members ***/

    private static Map<Integer, HttpStatusCode> reverseLookup;

    /**
     * Retrieves the HttpStatusCode for the given integer status code.
     */
    public static HttpStatusCode getForIntCode(int code) {
        // Creates a reverse-lookup map if one has not already been created, allowing easy lookup of
        // the enum from its associated integer code.
        if (reverseLookup == null || reverseLookup.isEmpty()) {

            reverseLookup = new HashMap<Integer, HttpStatusCode>();

            for (HttpStatusCode currentHttpStatusCode : HttpStatusCode.values()) {
                reverseLookup.put(currentHttpStatusCode.getCode(), currentHttpStatusCode);
            }

        }

        return reverseLookup.get(code);
    }
}
