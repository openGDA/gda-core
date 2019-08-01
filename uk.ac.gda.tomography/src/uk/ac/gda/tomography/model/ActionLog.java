/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.tomography.model;

import java.util.Date;

/**
 * A time signed note
 * @author Maurizio Nagni
 */
public class ActionLog {
	private Date date;
	private String note;

	public ActionLog() {
		super();
	}

	/**
	 * Clones an ActionLog
	 * @param actionLog
	 */
	public ActionLog(ActionLog actionLog) {
		super();
		this.date = actionLog.getDate();
		this.note = actionLog.getNote();
	}

	@Override
	public String toString() {
		return "ActionLog [date=" + date + ", note=" + note + "]";
	}

	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
}
