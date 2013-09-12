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

/**
 * Listener interface for the display feeds, defining methods to be called on certain events
 * pertaining to the list of feeds to display.
 */
public interface DisplayFeedsListener {

    public void onPopulatePreExecute();

    public void onPopulateProgress(DisplayFeeds.PopulateProgress progress);

    public void onPopulateComplete();

    public void onPopulateError(Throwable cause);

}
