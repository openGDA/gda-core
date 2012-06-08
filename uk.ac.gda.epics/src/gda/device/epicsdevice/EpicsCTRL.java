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
 * EpicsCTRL Class
 */
public class EpicsCTRL extends EpicsGR implements Serializable {
	@SuppressWarnings("hiding")
	final static long serialVersionUID = 1;
	/**
	 * 
	 */
	final public Number _ucl;
	/**
	 * 
	 */
	final public Number _lcl;

	EpicsCTRL(CTRL obj) {
		super(obj);
		_lcl = obj.getLowerCtrlLimit();
		_ucl = obj.getUpperCtrlLimit();
	}

	/**
	 * @see gda.device.epicsdevice.EpicsGR#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + "\n" + "lcl = " + _lcl.toString() + " " + "ucl = " + _ucl.toString() + "\n";
	}
}
