/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.data.metadata;

/**
 * Holds information about a single visit ID which can be displayed to the user to help them choose which visit to
 * collect data under if several are valid for that user at that time.
 */
public class VisitEntry {

	private final String visitID;
	private final String title;

	public VisitEntry(String visitID, String title) {
		this.visitID = visitID;
		this.title = title;
	}

	public String getVisitID() {
		return visitID;
	}

	public String getTitle() {
		return title;
	}

	@Override
	public String toString() {
		return "VisitEntry [visitID=" + visitID + ", title=" + title + "]";
	}

}
