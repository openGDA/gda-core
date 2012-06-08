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

package gda.device.robot;

import java.io.Serializable;

/**
 * SampleState Class
 */
public class SampleState implements Serializable {

	private static SampleState[] values_ = new SampleState[4];

	private int value_;
	/** */

	/** */
	public final static int _CAROUSEL = 0;

	/** */
	public final static SampleState CAROUSEL = new SampleState(_CAROUSEL);

	/** */
	public final static int _INJAWS = 1;

	/** */
	public final static SampleState INJAWS = new SampleState(_INJAWS);

	/** */
	public final static int _DIFF = 2;

	/** */
	public final static SampleState DIFF = new SampleState(_DIFF);

	/** */
	public final static int _UNKNOWN = 3;

	/** */
	public final static SampleState UNKNOWN = new SampleState(_UNKNOWN);

	protected SampleState(int value) {
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
	public static SampleState from_int(int value) {
		return values_[value];
	}
}
