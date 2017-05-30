/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.specs.phoibos;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * This enum maps to the status returned from EPICS
 * <p>
 * It provides the convenience method {@link #get(int)} method to create a enum from the integer returned by EPICS CA.
 *
 * @author James Mudd
 */
public enum SpecsPhoibosStatus {
	IDLE(0),
	ACQUIRE(1),
	READOUT(2),
	CORRECT(3),
	SAVING(4),
	ABORTING(5),
	ERROR(6),
	WAITING(7),
	INITIALIZING(8),
	DISCONNECTED(9),
	ABORTED(10);

	private final int value;
	private static final Map<Integer, SpecsPhoibosStatus> lookup;

	static {
		Map<Integer, SpecsPhoibosStatus> map = new HashMap<>();
		for (SpecsPhoibosStatus s : EnumSet.allOf(SpecsPhoibosStatus.class)) {
			map.put(s.getValue(), s);
		}
		lookup = Collections.unmodifiableMap(map);
	}

	private SpecsPhoibosStatus(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static SpecsPhoibosStatus get(int value) {
		return lookup.get(value);
	}

}
