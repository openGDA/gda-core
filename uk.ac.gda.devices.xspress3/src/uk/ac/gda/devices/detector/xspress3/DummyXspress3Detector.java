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
import gda.device.detector.DetectorBase;
import uk.ac.gda.beans.DetectorROI;

public class DummyXspress3Detector extends DetectorBase {

	private static final Logger logger = LoggerFactory.getLogger(DummyXspress3Detector.class);

	@Override
	public String[] getExtraNames() {
		return new String[] { "y" };
	}

	@Override
	public void prepareForCollection() throws DeviceException {
		logger.trace("prepareForCollection()");
	}

	public void setNumberOfFramesToCollect(int numberOfFramesToCollect) {
		logger.trace("setNumberOfFramesToCollect({})", numberOfFramesToCollect);
	}

	public void setRegionsOfInterest(DetectorROI[] regionList) {
		logger.trace("setRegionsOfInterest({})", (Object[]) regionList);
	}

	public void waitUntilDetectorStateIsBusy(long timeout) {
		logger.trace("waitUntilDetectorStateIsBusy({})", timeout);
	}

	public void waitUntilDetectorStateIsNotBusy() {
		logger.trace("waitUntilDetectorStateIsNotBusy()");
	}

	@Override
	public void collectData() throws DeviceException {
		// nothing to do
	}

	@Override
	public int getStatus() throws DeviceException {
		return 0;
	}

	@Override
	public Object readout() throws DeviceException {
		return null;
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}
}
