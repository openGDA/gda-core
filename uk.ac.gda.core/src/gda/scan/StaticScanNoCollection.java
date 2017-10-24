/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.jython.InterfaceProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This 'scan' does not move anything or instruct detectors to collect data, it just calls getPosition() or readout()
 * once and safes the data to a normal scan file. This is mainly (only?) useful to just save the data when the user has
 * performed the data collection in the GUI or through a Timer based scan on NCD detector hardware.
 */
public class StaticScanNoCollection extends ScanBase {
	private static final Logger logger = LoggerFactory.getLogger(StaticScanNoCollection.class);

	/**
	 * Expects detectors, scannables, or monitors in any order
	 *
	 * @param args
	 *            Device[]
	 * @throws IllegalArgumentException
	 */
	public StaticScanNoCollection(Scannable[] args) throws IllegalArgumentException {
		for (Scannable device : args) {
			if (device instanceof Detector) {
				super.allDetectors.add((Detector) device);
			} else {
				super.allScannables.add(device);
			}
		}
		super.setUp();
		currentPointCount = 0;
		TotalNumberOfPoints = 1;
		command = "static readout";
	}

	@Override
	public void prepareForCollection() throws Exception {
		try {
			prepareScanForCollection();
		} catch (Exception ex) {
			String error = "Error during prepareForCollection: " + ex.getMessage();
			logger.info(error, ex);
			InterfaceProvider.getTerminalPrinter().print(error);
			throw ex;
		}
	}

	@Override
	public void doCollection() throws Exception {
		try {
			readDevicesAndPublishScanDataPoint();
		} catch (DeviceException ex) {
			logger.error("Error doing collection", ex);
			throw ex;
		} catch (InterruptedException ex) {
			logger.error("Collection interrupted", ex);
			throw ex;
		}
	}

	@Override
	public int getDimension() {
		// this is what we do
		return 1;
	}
}
