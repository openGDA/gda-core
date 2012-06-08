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

import gda.epics.connection.EpicsController;

import java.io.Serializable;

/**
 * A Motor status class - provides a set of allowed status values.
 */
final public class ReturnType implements Serializable {
	private static ReturnType[] values_ = new ReturnType[7];

	private int value_;

	/**
	 * 
	 */
	public final static int _DBR_NATIVE = 0;

	/**
	 * 
	 */
	public final static int _DBR_STS = 1;

	/**
	 * 
	 */
	public final static int _DBR_TIME = 2;

	/**
	 * 
	 */
	public final static int _DBR_CTRL = 3;

	/**
	 * 
	 */
	public final static int _DBR_GR = 4;

	/**
	 * 
	 */
	public final static int _DBR_UNKNOWN = 5;

	/**
	 * 
	 */
	public final static ReturnType DBR_NATIVE = new ReturnType(_DBR_NATIVE);

	/**
	 * 
	 */
	public final static ReturnType DBR_STS = new ReturnType(_DBR_STS);

	/**
	 * 
	 */
	public final static ReturnType DBR_TIME = new ReturnType(_DBR_TIME);

	/**
	 * 
	 */
	public final static ReturnType DBR_CTRL = new ReturnType(_DBR_CTRL);

	/**
	 * 
	 */
	public final static ReturnType DBR_GR = new ReturnType(_DBR_GR);

	/**
	 * 
	 */
	public final static ReturnType DBR_UNKNOWN = new ReturnType(_DBR_UNKNOWN);

	protected ReturnType(int value) {
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
	 * @return the MotorStatus instance corresponding to value
	 */
	public static ReturnType from_int(int value) {
		return values_[value];
	}

	@Override
	public String toString() {
		if (this == DBR_NATIVE)
			return "NATIVE";
		if (this == DBR_STS)
			return "STS";
		if (this == DBR_TIME)
			return "TIME";
		if (this == DBR_GR)
			return "GR";

		return "CTRL";
	}

	/**
	 * @return EpicsController.MonitorType
	 */
	public EpicsController.MonitorType getTrueReturnType() {
		if (this == DBR_NATIVE)
			return EpicsController.MonitorType.NATIVE;
		if (this == DBR_STS)
			return EpicsController.MonitorType.STS;
		if (this == DBR_TIME)
			return EpicsController.MonitorType.TIME;
		if (this == DBR_GR)
			return EpicsController.MonitorType.GR;

		return EpicsController.MonitorType.CTRL;
	}

	@Override
	public int hashCode() {
		return value_;
	}

	@Override
	public boolean equals(Object _other) {
		if (_other instanceof ReturnType) {
			ReturnType other = (ReturnType) _other;
			return value_ == other.value_;
		}
		return false;
	}
}
