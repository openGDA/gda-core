/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.detector.xspress3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;

public class DummyXspress3MiniDetector extends DummyXspress3Detector {

	private static final Logger logger = LoggerFactory.getLogger(DummyXspress3MiniDetector.class);

	private final String[] extraNames = new String[] { "Chan1","FF" };


	@Override
	public String[] getExtraNames() {
		return extraNames;
	}

	@Override
	public Object readout() throws DeviceException {
		NXDetectorData data = new NXDetectorData(this);
		data.setPlottableValue(extraNames[0], 1.0);
		data.setPlottableValue(extraNames[1], 2.0);
		return data;
	}
}
