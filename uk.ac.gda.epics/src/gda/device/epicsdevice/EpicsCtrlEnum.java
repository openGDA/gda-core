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
 * EpicsCtrlEnum class
 */
public class EpicsCtrlEnum extends EpicsLabelsEnum implements Serializable {
	@SuppressWarnings("hiding")
	final static long serialVersionUID = 1;

	EpicsCtrlEnum(DBR_CTRL_Enum ctrlEnum) {
		super(ctrlEnum);
	}

	/**
	 * @return value as a string
	 */
	public String getValueAsString() {
		if (super._value instanceof short[]) {
			return super.strings[((short[]) super._value)[0]];
		}
		return "Unknown";
	}

	/**
	 * @see gda.device.epicsdevice.EpicsLabelsEnum#toString()
	 */
	@Override
	public String toString() {
		return super.toString() + "\n" + "Value = " + getValueAsString();
	}
}