/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

import java.util.Date;

/**
 * ID, start & end time for a user visit, as returned from the database.
 */
public class VisitData {

	private final String visitId;
	private final Date startTime;
	private final Date endTime;

	public VisitData(String visitId, Date startTime, Date endTime) {
		this.visitId = visitId;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public String getVisitId() {
		return visitId;
	}

	public Date getStartTime() {
		return startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	@Override
	public String toString() {
		return "VisitData [visitId=" + visitId + ", startTime=" + startTime + ", endTime=" + endTime + "]";
	}
}
