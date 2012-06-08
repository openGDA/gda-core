/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.jython.scriptcontroller.logging;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ScriptControllerLogResults implements Serializable, Comparable<ScriptControllerLogResults> {

	String uniqueID;
	String scriptName;
	Timestamp started;
	Timestamp updated;

	public ScriptControllerLogResults(String uniqueID, String scriptName, Timestamp started, Timestamp updated) {
		super();
		this.uniqueID = uniqueID;
		this.scriptName = scriptName;
		this.started = started;
		this.updated = updated;
	}

	public String getScriptName() {
		return scriptName;
	}

	public Timestamp getStarted() {
		return started;
	}

	public Timestamp getUpdated() {
		return updated;
	}

	public String getUniqueID() {
		return uniqueID;
	}

	@Override
	public String toString() {
		return getScriptName() + " - started:" + formatDate(started,false) + ", updated:" + formatDate(updated,true);
	}

	private String formatDate(Timestamp updated2, boolean timeonly) {

		if (timeonly) {
			DateFormat format = new SimpleDateFormat("h:mm a");
			return format.format(updated2);
		}

		Calendar calNow = Calendar.getInstance();
		Calendar calTimestamp = Calendar.getInstance();
		calTimestamp.setTime(updated2);

		if (calTimestamp.get(Calendar.YEAR) == calNow.get(Calendar.YEAR)
				&& calTimestamp.get(Calendar.MONTH) == calNow.get(Calendar.MONTH)
				&& calTimestamp.get(Calendar.DAY_OF_MONTH) == calNow.get(Calendar.DAY_OF_MONTH)) {
			DateFormat format = new SimpleDateFormat("h:mm a");
			return format.format(updated2) + " today";
		} else if (calTimestamp.get(Calendar.YEAR) == calNow.get(Calendar.YEAR)
				&& calTimestamp.get(Calendar.MONTH) == calNow.get(Calendar.MONTH)
				&& calTimestamp.get(Calendar.DAY_OF_MONTH) == calNow.get(Calendar.DAY_OF_MONTH) - 1) {
			DateFormat format = new SimpleDateFormat("h:mm a");
			return format.format(updated2) + " yesterday";
		} else if (calTimestamp.get(Calendar.YEAR) == calNow.get(Calendar.YEAR)
				&& calTimestamp.get(Calendar.MONTH) == calNow.get(Calendar.MONTH)
				&& calTimestamp.get(Calendar.DAY_OF_MONTH) >= calNow.get(Calendar.DAY_OF_MONTH) - 7) {
			DateFormat format = new SimpleDateFormat("h:mm a EEE");
			return format.format(updated2);
		}
		DateFormat format = new SimpleDateFormat("h:mm a EEE, MMM d");
		return format.format(updated2);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((scriptName == null) ? 0 : scriptName.hashCode());
		result = prime * result + ((started == null) ? 0 : started.hashCode());
		result = prime * result + ((uniqueID == null) ? 0 : uniqueID.hashCode());
		result = prime * result + ((updated == null) ? 0 : updated.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScriptControllerLogResults other = (ScriptControllerLogResults) obj;
		if (scriptName == null) {
			if (other.scriptName != null)
				return false;
		} else if (!scriptName.equals(other.scriptName))
			return false;
		if (started == null) {
			if (other.started != null)
				return false;
		} else if (!started.equals(other.started))
			return false;
		if (uniqueID == null) {
			if (other.uniqueID != null)
				return false;
		} else if (!uniqueID.equals(other.uniqueID))
			return false;
		if (updated == null) {
			if (other.updated != null)
				return false;
		} else if (!updated.equals(other.updated))
			return false;
		return true;
	}

	@Override
	public int compareTo(ScriptControllerLogResults o) {
		return o.getStarted().compareTo(started);
	}
}
