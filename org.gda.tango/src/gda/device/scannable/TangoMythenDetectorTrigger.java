/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package gda.device.scannable;

import gda.device.DeviceException;
import gda.device.detector.mythen.TangoMythenDetector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class exists purely to trigger a Mythen diffraction image data collection at a specified energy during an Exafs
 * scan. From the given energy a scan data point is calculated from which to trigger the data collection. This class
 * could be extended to provide multiple trigger points should the need arise.
 */
public class TangoMythenDetectorTrigger extends ScannableBase {
	private static final Logger logger = LoggerFactory.getLogger(TangoMythenDetectorTrigger.class);
	private static int scanPoint;
	private int startPoint = -1;
	private TangoMythenDetector mythenDetector;

	@Override
	public void configure() {
		this.setExtraNames(new String[0]);
		this.setInputNames(new String[0]);
		this.setOutputFormat(new String[0]);
	}

	public TangoMythenDetector getMythenDetector() {
		return mythenDetector;
	}

	public void setMythenDetector(TangoMythenDetector mythenDetector) {
		this.mythenDetector = mythenDetector;
	}

	@Override
	public void atPointStart() {
		logger.debug("TangoMythenDetectorTrigger: Scan point " + scanPoint);
		logger.debug("TangoMythenDetectorTrigger: Start point " + startPoint);
		try {
			if (startPoint >= 0 && scanPoint == startPoint && mythenDetector != null) {
				mythenDetector.collectData();
				logger.debug("TangoMythenDetectorTrigger: Starting scan at point " + startPoint);
			}
			scanPoint++;
		} catch (Exception e) {

		}
	}

	public void setStartAtScanPoint(int point) {
		startPoint = point;
	}

	@Override
	public void atScanStart() {
		scanPoint = 0;
	}

	// @Override
	// public NexusTreeProvider readout() throws DeviceException {
	// return null;
	// }

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

	@Override
	public Object rawGetPosition() {
		return null;
	}

	@Override
	public String toFormattedString() {
		return "";
	}
}
