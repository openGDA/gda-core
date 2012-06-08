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

/**
 * EpicsSimpleEnum Class
 */
public class EpicsSimpleEnum implements Serializable {
	/**
	 * 
	 */
	final public String _name;

	EpicsSimpleEnum(gov.aps.jca.Enum lblsEnum) {
		_name = lblsEnum.getName();
	}

	EpicsSimpleEnum() {
		_name = "";
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Enum.name = " + _name;
	}
}
