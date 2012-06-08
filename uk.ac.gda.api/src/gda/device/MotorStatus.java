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
 * A Motor status class - provides a set of allowed status values.
 */
final public class MotorStatus implements Serializable {
	private static MotorStatus[] values_ = new MotorStatus[7];

	private int value_;
	String label;
	/** */

	/** */
	public final static int _UPPERLIMIT = 0;

	/** */
	public final static MotorStatus UPPERLIMIT = new MotorStatus(_UPPERLIMIT, "UPPERLIMIT");

	/** */
	public final static int _LOWERLIMIT = 1;

	/** */
	public final static MotorStatus LOWERLIMIT = new MotorStatus(_LOWERLIMIT, "LOWERLIMIT");

	/** */
	public final static int _FAULT = 2;

	/** */
	public final static MotorStatus FAULT = new MotorStatus(_FAULT, "FAULT");

	/** */
	public final static int _READY = 3;

	/** */
	public final static MotorStatus READY = new MotorStatus(_READY, "READY");

	/** */
	public final static int _BUSY = 4;

	/** */
	public final static MotorStatus BUSY = new MotorStatus(_BUSY, "BUSY");

	/** */
	public final static int _UNKNOWN = 5;

	/** */
	public final static MotorStatus UNKNOWN = new MotorStatus(_UNKNOWN, "UNKNOWN");

	/** */
	public final static int _SOFTLIMITVIOLATION = 6;

	/** */
	public final static MotorStatus SOFTLIMITVIOLATION = new MotorStatus(_SOFTLIMITVIOLATION, "SOFTLIMITVIOLATION");

	protected MotorStatus(int value, String label) {
		values_[value] = this;
		value_ = value;
		this.label = label;
	}

	/**
	 * @return the internal value
	 */
	public int value() {
		return value_;
	}

	/**
	 * @param value
	 * @return the MotorStatus instance corresponding to value
	 */
	public static MotorStatus from_int(int value) {
		return values_[value];
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + value_;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MotorStatus other = (MotorStatus) obj;
		if (value_ != other.value_)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return label != null ? label : Integer.toString(value());

	}


	
}
