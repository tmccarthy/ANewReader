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

import java.io.Serializable;
import java.util.Set;

public class Category extends Feed implements Comparable<Category>, Serializable {

    private String id;
    private String label;

    public Category(String id, String label, int unreadCount, Model parentModel) {
        super(parentModel, unreadCount);
        this.id = id;
        this.label = label;
    }

    public String getId() {
        return this.id;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String newLabel) {
        throw new UnsupportedOperationException();
    }

    public Set<Subscription> getSubscriptions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo(Category otherCategory) {
        if (this.label.compareTo(otherCategory.label) != 0) {
            return this.label.compareTo(otherCategory.label);
        } else {
            return this.id.compareTo(otherCategory.id);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Category category = (Category) o;

        return !(id != null ? !id.equals(category.id) : category.id != null);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String getTitle() {
        return this.label;
    }

    @Override
    String getFeedAddress() {
        return this.id;
    }
}
