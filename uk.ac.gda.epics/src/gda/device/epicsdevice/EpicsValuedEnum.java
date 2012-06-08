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
 * EpicsValuedEnum Class
 */
public class EpicsValuedEnum extends EpicsSimpleEnum implements Serializable {
	final static long serialVersionUID = 1;
	/**
	 * 
	 */
	final public int _value;

	EpicsValuedEnum(gov.aps.jca.ValuedEnum lblsEnum) {
		super(lblsEnum);
		_value = lblsEnum == null ? -1 : lblsEnum.getValue();
	}

	EpicsValuedEnum() {
		super();
		_value = 0;
	}

	/**
	 * @see gda.device.epicsdevice.EpicsSimpleEnum#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + " Enum.value = " + _value;
	}
}
