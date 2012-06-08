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

import java.io.Serializable;
import gov.aps.jca.dbr.*;

/**
 * EpicsGR Class
 */
public class EpicsGR extends EpicsTime implements Serializable {
	/**
	 * 
	 */
	final public String _unit = "";
	/**
	 * 
	 */
	/**
	 * 
	 */
	final public Number _udl;
	/**
	 * 
	 */
	final public Number _ldl;
	/**
	 * 
	 */
	final public Number _ual;
	/**
	 * 
	 */
	final public Number _uwl;
	/**
	 * 
	 */
	final public Number _lwl;
	/**
	 * 
	 */
	final public Number _lal;
	/**
	 * 
	 */
	final public short _precision;

	EpicsGR(GR obj) {
		super(obj);
		_udl = obj.getUpperDispLimit();
		_ldl = obj.getLowerDispLimit();
		_ual = obj.getUpperAlarmLimit();
		_uwl = obj.getUpperWarningLimit();
		_lwl = obj.getLowerWarningLimit();
		_lal = obj.getLowerAlarmLimit();
		if (obj instanceof PRECISION) {
			_precision = ((PRECISION) obj).getPrecision();
		} else
			_precision = 0;
	}

	/**
	 * @see gda.device.epicsdevice.EpicsTime#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + "\n" + "ldl = " + _ldl.toString() + " " + "udl = " + _udl.toString() + "\n"
				+ "lwl = " + _lwl.toString() + " " + "uwl = " + _uwl.toString() + "\n" + "lal = " + _lal.toString()
				+ " " + "ual = " + _ual.toString() + "\n" + "precision = " + _precision + ". ";

	}

}
