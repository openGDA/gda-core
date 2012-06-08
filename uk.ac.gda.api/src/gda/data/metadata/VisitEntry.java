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

import org.springframework.util.StringUtils;

/**
 * Holds information about a single visit ID which can be displayed to the user to help them choose which visit to
 * collect data under if several are valid for that user at that time.
 */
public class VisitEntry {

	private String visitID;
	private String title;

	public VisitEntry(String visitID, String title) {
		super();
		this.visitID = visitID;
		this.title = title;
	}

	public String getVisitID() {
		return visitID;
	}

	public void setVisitID(String visitID) {
		this.visitID = visitID;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	@Override
	public String toString() {
		return String.format("VisitEntry(visitID=%s, title=%s)",
			StringUtils.quote(visitID),
			StringUtils.quote(title));
	}

}
