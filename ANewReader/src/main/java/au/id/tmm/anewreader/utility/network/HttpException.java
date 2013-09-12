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

/**
 * Custom HttpException class, includes the HTTP status code associated with the error represented
 * using the {@link au.id.tmm.anewreader.utility.network.HttpStatusCode} class.
 */
public class HttpException extends IOException {

    private HttpStatusCode errorCode;

    /*** Constructors ***/

    public HttpException() {
    }

    public HttpException(HttpStatusCode errorCode) {
        this.errorCode = errorCode;
    }

    public HttpException(String detailMessage) {
        super(detailMessage);
    }

    public HttpException(String detailMessage, HttpStatusCode errorCode) {
        this(detailMessage);
        this.errorCode = errorCode;
    }

    public HttpException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Full constructor, takes a message, an
     * {@link au.id.tmm.anewreader.utility.network.HttpStatusCode} and a Throwable as the
     * cause of the Exception.
     */
    public HttpException(String message, HttpStatusCode errorCode, Throwable cause) {
        this(message, cause);
        this.errorCode = errorCode;
    }

    public HttpException(Throwable cause) {
        super(cause);
    }

    /*** Accessors and Mutators ***/

    public HttpStatusCode getHttpStatus() {
        return errorCode;
    }
}
