/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.mbs;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.stream.Collectors;

public enum MbsAnalyserStatus {
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

	private MbsAnalyserStatus(int value) {
		this.value = value;
	}

	private static final Map<Integer, MbsAnalyserStatus> lookup;

	static {
		Map<Integer, MbsAnalyserStatus> map = EnumSet
				.allOf(MbsAnalyserStatus.class)
				.stream()
				.collect(Collectors.toMap(MbsAnalyserStatus::getValue, status -> status));

		lookup = Collections.unmodifiableMap(map);
	}

	private int getValue() {
		return this.value;
	}

	public static MbsAnalyserStatus get(int value) {
		return lookup.get(value);
	}
}
