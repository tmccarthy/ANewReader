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

package au.id.tmm.anewreader.utility;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Utility class providing access to some algorithms through static methods.
 */
public class Algorithms {

    private Algorithms() {
    }

    /**
     * Calculates the relative complement of two sets and returns them in the same order as they
     * are iterated through in collection1. That is, it returns an ArrayList of the items in
     * collection1 that are also in collection2, in the order in which they are iterated over in
     * collection1.
     *
     * This method allows the setting of the initial capacity of the ArrayList.
     */
    public static <E> ArrayList<E> computeOrderedComplement(Collection<E> collection1, Collection<E> collection2, int initialCapacity) {

        ArrayList<E> returnedList = new ArrayList<E>(initialCapacity);

        for (E currentElement : collection1) {
            if (!collection2.contains(currentElement)) {
                returnedList.add(currentElement);
            }
        }

        return returnedList;
    }

    /**
     * Calculates the relative complement of two sets and returns them in the same order as they
     * are iterated through in collection1. That is, it returns an ArrayList of the items in
     * collection1 that are also in collection2, in the order in which they are iterated over in
     * collection1.
     */
    public static <E> ArrayList<E> computeOrderedComplement(Collection<E> set1, Collection<E> set2) {
        return Algorithms.computeOrderedComplement(set1, set2, set1.size());
    }

}
