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

import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.STS;

import java.io.Serializable;

/**
 * EpicsSTS Class
 */
public class EpicsSTS extends EpicsDBR implements Serializable {
	@SuppressWarnings("hiding")
	final static long serialVersionUID = 1;
	/**
	 * 
	 */
	final public EpicsValuedEnum _status;
	/**
	 * 
	 */
	final public EpicsValuedEnum _severity;

	EpicsSTS(STS obj) {
		super((DBR) obj);
		_severity = new EpicsValuedEnum(obj.getSeverity());
		_status = new EpicsValuedEnum(obj.getStatus());

	}

	/**
	 * @see gda.device.epicsdevice.EpicsDBR#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + "\n" + "EpicsSTS\n" + "status = " + _status.toString() + "\n" + "severity = "
				+ _severity.toString() + ".";
	}
}
