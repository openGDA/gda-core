/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package gda.data.scan.nexus.device;

import org.eclipse.dawnsci.nexus.NXdetector;

import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.detector.DummyDetector;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NexusDetector;

/**
 * A very simple dummy {@link NexusDetector} that writes a single dataset consisting of
 * .tif filenames (the .tif files themselves are not created).
 * The use case for this detector is to simulate a bug that occurs due to the axis
 * dataset being added, while the dataset itself is flattened to be scalar (or rather
 * to have rank equal to the scan rank).
 */
public class SimpleDummyNexusDetector extends DummyDetector implements NexusDetector {

	public static final String DETECTOR_NAME = "simpleNexusDet";
	public static final String AXIS_NAME_SUFFIX = "_axis";
	private static final String TIF_FILENAME_PATTERN = "%s-%04d.tif";

	private int pointNumber = 1;

	public SimpleDummyNexusDetector() {
		setName(DETECTOR_NAME);
	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		return (NexusTreeProvider) super.readout();
	}

	@Override
	protected Object acquireData() {
		final NXDetectorData data = new NXDetectorData(this);

		final NexusGroupData detData = new NexusGroupData(Math.random() * Double.MAX_VALUE);
		data.addData(getName(), NXdetector.NX_DATA, detData);

		// add axis to simulate behaviour of ADDetector.appendDataAxes, where dims is always new int[] { 1 }
		final NexusGroupData axisData = new NexusGroupData(TIF_FILENAME_PATTERN.formatted(getName(), pointNumber++));
		data.addAxis(getName(), NXdetector.NX_DATA + AXIS_NAME_SUFFIX + "1", axisData, 1, 1, "pixels", false);

		return data;
	}

}
