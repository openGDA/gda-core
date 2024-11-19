/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package uk.ac.gda.core.sampletransfer;

import java.util.HashMap;
import java.util.Map;

import gda.device.Scannable;

public class StepProperties {

	private SampleSelection sample;
	private Map<Scannable, Object> recordedPositions;

	public StepProperties() {
		recordedPositions = new HashMap<>();
	}

	public SampleSelection getSample() {
		return sample;
	}

	public void setSample(SampleSelection sample) {
		this.sample = sample;
	}

	public Map<Scannable, Object> getRecordedPositions() {
		return recordedPositions;
	}
}
