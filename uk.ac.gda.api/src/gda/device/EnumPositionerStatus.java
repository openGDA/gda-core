/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device;

import java.io.Serializable;

/**
 * A Enum Positioner Status class - provides a set of allowed status values
 */
final public class EnumPositionerStatus implements Serializable {
	private static EnumPositionerStatus[] values_ = new EnumPositionerStatus[3];
	private int value_;

	/** Possible status value, indicates device is idle. */
	public static final int _IDLE = 0;
	/** Possible status value, indicates device is moving. */
	public static final int _MOVING = _IDLE + 1;
	/** Possible status value, indicates device has an error. */
	public static final int _ERROR = _MOVING + 1;

	/**
	 * 
	 */
	public static final EnumPositionerStatus IDLE = new EnumPositionerStatus(_IDLE);
	/**
	 * 
	 */
	public static final EnumPositionerStatus MOVING = new EnumPositionerStatus(_MOVING);
	/**
	 * 
	 */
	public static final EnumPositionerStatus ERROR = new EnumPositionerStatus(_ERROR);

	protected EnumPositionerStatus(int value) {
		values_[value] = this;
		value_ = value;
	}

	/**
	 * @return the internal value
	 */
	public int value() {
		return value_;
	}

	/**
	 * @param value
	 * @return the EnumPositionerStatus instance corresponding to value
	 */
	public static EnumPositionerStatus from_int(int value) {
		return values_[value];
	}
	
	@Override
	public String toString() {
		if (value_ == _IDLE){
			return "idle";
		} else if (value_ == _MOVING){
			return "moving";
		} else if (value_ ==_ERROR){
			return "error";
		}
		return "undefined";
	}

}
