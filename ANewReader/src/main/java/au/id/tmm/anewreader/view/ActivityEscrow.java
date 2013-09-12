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

package au.id.tmm.anewreader.view;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility singleton used to handle the passing of large amounts of data between Activities.
 *
 * Normally, data is passed between activities by serialising objects which are then passed through
 * an Intent. For passing large amounts of data, however, this leads to large performance issues as
 * the object and all its members (and all their members etc.) must first be serialised. This is
 * usually overcome by using Singletons to store global data, but this makes it difficult to
 * constrain scope.
 *
 * This class seeks to find a middle-ground. It acts as an escrow, storing data from one Activity in
 * a map and making it available to another Activity the first has called.
 *
 * It is important to note that this class (which has an indefinite lifespan since it is a
 * singleton) will maintain references to data stored in its map after the called activity no longer
 * has use for it. Thus the client must decide when to destroy the data stored in this object to
 * avoid memory being allocated for objects that will never be used again. Accordingly, the client
 * should generally use getAndDeallocate, and only use getAndRetain where the data in question
 * should still be kept in the escrow.
 */
class ActivityEscrow {

    private static final ActivityEscrow INSTANCE = new ActivityEscrow();

    public static ActivityEscrow getInstance() {
        return INSTANCE;
    }

    private Map<String, Object> objects;

    public Object put(String key, Object value) {
        if (this.objects == null) {
            this.objects = new HashMap<String, Object>();
        }
        return this.objects.put(key, value);
    }

    /**
     * Returns the object corresponding to the given key without deallocating the object.
     */
    public Object getAndRetain(String key) {
        return this.objects.get(key);
    }

    /**
     * Returns the object corresponding to the given key and removes it from the ActivityEscrow.
     */
    public Object getAndDeallocate(String key) {
        Object returnedObject = this.getAndRetain(key);

        this.objects.remove(key);

        if (this.objects.isEmpty()) {
            this.objects = null;
        }

        return returnedObject;
    }
}
