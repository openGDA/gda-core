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

package gda.device.detector.xmap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.detector.addetector.ADDetector;
import gda.device.detector.areadetector.v17.NDROI;
import gda.factory.FactoryException;

/**
 * <p>
 * Dummy XmapController that delegates file writing to an Area Detector
 * <p>
 * This is intended to simulate the configuration on (for example) I08, where the Xmap controller transmits data to a (partial) Area Detector on another
 * machine.<br>
 * It sets the area detector's Y dimension to 1 to simulate a spectrum: the existing Y value is restored at the end.
 * <p>
 * Before using this, check that the simulator is set up in the following way:<br>
 * - Camera image mode is set to Single
 * - ROI X size = <some fairly large number e.g. 256>
 * - ROI Y size = 1
 * - Stats array port = CAM.roi
 * - HDF array port = CAM.stat
 */
public class DummyXmapControllerAreaDetector extends DummyXmapControllerBase {

	private static final Logger logger = LoggerFactory.getLogger(DummyXmapControllerAreaDetector.class);

	private ADDetector adDetector;
	private NDROI ndRoi;

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		if (adDetector == null) {
			final String message = "adDetector not set";
			logger.error(message);
			throw new FactoryException(message);
		}
		if (ndRoi == null) {
			final String message = "ndRoi not set";
			logger.error(message);
			throw new FactoryException(message);
		}
		try {
			ndRoi.setSizeY(1);
		} catch (Exception e) {
			final String message = "Error setting ROI";
			logger.error(message, e);
			throw new FactoryException(message);
		}
		setConfigured(true);
	}

	@Override
	public void clearAndStart() throws DeviceException {
		super.clearAndStart();

		// Do the collection via ADDetector here. We must also wait for collection to finish,
		// as NexusXmap.waitWhileBusy() does not call this object.
		try {
			adDetector.collectData();
			adDetector.waitWhileBusy();
		} catch (Exception e) {
			final String message = "Error waiting for data collection";
			logger.error(message, e);
			throw new DeviceException(message, e);
		}
	}

	@Override
	public int[] getData(int mcaNumber) throws DeviceException {
		final String message = "Use DummyXmapController if you want to write data in GDA";
		logger.error(message);
		throw new DeviceException(message);
	}

	@Override
	public int[][] getData() throws DeviceException {
		final String message = "Use DummyXmapController if you want to write data in GDA";
		logger.error(message);
		throw new DeviceException(message);
	}

	public ADDetector getAdDetector() {
		return adDetector;
	}

	public void setAdDetector(ADDetector adDetector) {
		this.adDetector = adDetector;
	}

	public NDROI getNdRoi() {
		return ndRoi;
	}

	public void setNdRoi(NDROI ndRoi) {
		this.ndRoi = ndRoi;
	}

	@Override
	public String toString() {
		return "DummyXmapControllerAreaDetector [adDetector=" + adDetector + ", configured=" + isConfigured() + ", " + super.toString() + "]";
	}
}