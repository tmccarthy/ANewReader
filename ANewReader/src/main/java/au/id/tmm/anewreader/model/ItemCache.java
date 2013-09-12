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

import java.util.HashMap;
import java.util.Map;

/**
 * Cache to store {@link au.id.tmm.anewreader.model.Item}s that have already been retrieved
 * from the api, so as to minimise requests for Item content.
 */
class ItemCache {

    private Map<String, Item> items;

    /*** Constructors ***/

    protected ItemCache(Map<String, Item> items) {
        this.items = items;
    }

    protected ItemCache() {
        this(new HashMap<String, Item>());
    }

    /*** Accessors and mutators ***/

    protected Item put(Item newItem) {
        return this.items.put(newItem.getId(), newItem);
    }

    protected Item get(String id) {
        return this.items.get(id);
    }

}
