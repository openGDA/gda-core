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
 * A Motor status enum - provides a set of allowed status values.
 */
public enum MotorStatus {
	// Do not change the order - ordinal values must match CorbaMotorStatus
	UPPER_LIMIT,
	LOWER_LIMIT,
	FAULT,
	READY,
	BUSY,
	UNKNOWN,
	SOFT_LIMIT_VIOLATION;

	// Cache values for efficiency
	private static final MotorStatus[] values = MotorStatus.values();

	/**
	 * @return the internal value
	 */
	public int value() {
		return this.ordinal();
	}

	/**
	 * @param value (ordinal)
	 * @return the MotorStatus instance corresponding to value
	 */
	public static MotorStatus fromInt(int value) {
		return values[value];
	}
}