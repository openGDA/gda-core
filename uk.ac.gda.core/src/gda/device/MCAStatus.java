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
 * Class to be used as status for MCA type devices - limited set of values available.
 */

final public class MCAStatus implements Serializable {
	private static MCAStatus[] values_ = new MCAStatus[4];

	private int value_;

	/** */
	public final static int _FAULT = 0;

	/** */
	public final static MCAStatus FAULT = new MCAStatus(_FAULT);

	/** */
	public final static int _READY = 1;

	/** */
	public final static MCAStatus READY = new MCAStatus(_READY);

	/** */
	public final static int _BUSY = 2;

	/** */
	public final static MCAStatus BUSY = new MCAStatus(_BUSY);

	/** */
	public final static int _UNKNOWN = 3;

	/** */
	public final static MCAStatus UNKNOWN = new MCAStatus(_UNKNOWN);

	/**
	 * Constructor used internally to construct the allowed instances.
	 * 
	 * @param value
	 */
	protected MCAStatus(int value) {
		values_[value] = this;
		value_ = value;
	}

	/**
	 * Returns the actual value.
	 * 
	 * @return the value
	 */
	public int value() {
		return value_;
	}

	/**
	 * Returns one of the possible instances.
	 * 
	 * @param value
	 *            the value for which the corresponding instance should be returned
	 * @return the relevant instance
	 */
	public static MCAStatus from_int(int value) {
		return values_[value];
	}
}
