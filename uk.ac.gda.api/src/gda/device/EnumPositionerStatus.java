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

/**
 * A Enum Positioner Status class - provides a set of allowed status values
 */
public enum EnumPositionerStatus {
	IDLE,
	MOVING,
	ERROR;

	// Cache values for efficiency
	private static final EnumPositionerStatus[] values = EnumPositionerStatus.values();

	/**
	 * @return the internal value
	 */
	public int value() {
		return this.ordinal();
	}

	/**
	 * @param value
	 * @return the EnumPositionerStatus instance corresponding to value
	 */
	public static EnumPositionerStatus fromInt(int value) {
		return values[value];
	}

	@Override
	public String toString() {
		switch (this) {
		case IDLE:
			return "idle";
		case MOVING:
			return "moving";
		case ERROR:
			return "error";
		}
		return "undefined";
	}
}
