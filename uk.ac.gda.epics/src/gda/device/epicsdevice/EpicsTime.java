/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.device.epicsdevice;

import gov.aps.jca.dbr.TIME;

import java.io.Serializable;

/**
 * EpicsTime Class
 */
public class EpicsTime extends EpicsSTS implements Serializable {
	// public final TimeStamp _stamp;
	/**
	 * 
	 */
	final public long _secPastEpoch;
	/**
	 * 
	 */
	final public long _nsec;

	EpicsTime(TIME obj) {
		super(obj);
		if (obj.getTimeStamp() != null) {
			// _stamp = ((TIME)obj).getTimeStamp();
			_secPastEpoch = obj.getTimeStamp().secPastEpoch();
			_nsec = obj.getTimeStamp().nsec();
		} else {
			// _stamp = null ; //set t((TIME)obj).getTimeStamp();
			_secPastEpoch = 0;
			_nsec = 0;
		}
	}

	/**
	 * @see gda.device.epicsdevice.EpicsSTS#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + "\n" + "secPastEpoch = " + _secPastEpoch + ". " + "nsec = " + _nsec + ".";
	}
}
