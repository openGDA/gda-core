/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.scan;

import gda.device.Detector;
import gda.device.Scannable;

/**
 * This 'scan' does not move anything but does instruct detectors to collect data, it calls getPosition() or readout()
 * once and saves the data to a normal scan file.
 */
public class StaticScan extends ScanBase {

	public StaticScan(Scannable[] args) throws IllegalArgumentException {
		for (Scannable device : args) {
			if (device instanceof Detector detector) {
				super.allDetectors.add(detector);
			} else {
				super.allScannables.add(device);
			}
		}
		super.setUp();
		currentPointCount = 0;
		totalNumberOfPoints = 1;
		command = "static readout";
	}


	@Override
	public void doCollection() throws Exception {
		collectData();
	}

	@Override
	public int getDimension() {
		// this is what we do
		return 1;
	}
}