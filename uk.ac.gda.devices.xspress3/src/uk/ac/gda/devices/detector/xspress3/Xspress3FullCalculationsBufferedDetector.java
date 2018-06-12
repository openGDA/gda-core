/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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
import org.springframework.beans.factory.InitializingBean;

import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import uk.ac.gda.devices.detector.xspress3.fullCalculations.Xspress3WithFullCalculationsDetector;

/* This class is used for testing Xspress3 v2 and will replace in the long run Xspress3BufferedDetector. A new class was needed in order not to interfere with
 * other beamlines that are using Xspress3. Here remove the two cases B18/I18 and only use Xspress3WithFullCalculationsDetectorv2 for simplicity and less error prone.
 */

public class Xspress3FullCalculationsBufferedDetector extends Xspress3BufferedDetector implements InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(Xspress3FullCalculationsBufferedDetector.class);
	private Xspress3WithFullCalculationsDetector fullCalcsDetector;

	@Override
	public void setXspress3Detector(Xspress3 xspress3Detector) {
		super.setXspress3Detector(xspress3Detector);
		fullCalcsDetector = (Xspress3WithFullCalculationsDetector) xspress3Detector;
	}

	@Override
	public void clearMemory() throws DeviceException {
		logger.debug("clearAndStart() called, for which there is no implementation in {}", this.getClass().getName());
	}

	@Override
	public void atScanStart() throws DeviceException {
		// we are doing the same work as in a step scan, but need to do the operations at this point
		// as the number of points may have changed and also atScanLineStart is not called in ContinuousScans
		fullCalcsDetector.setReadDataFromFile(true);
		super.atScanStart();
	}

	@Override
	public void setContinuousMode(boolean on) throws DeviceException {
		super.setContinuousMode(on);
		if (on) {
			// we are doing the same work as in a step scan, but need to do the operations at this point
			// as the number of points may have changed and also atScanLineStart is not called in ContinuousScans
			((Xspress3WithFullCalculationsDetector) xspress3Detector).setReadDataFromFile(true);
			xspress3Detector.atScanLineStart();
		}
	}

	@Override
	public int getNumberFrames() throws DeviceException {
		if (xspress3Detector.getController().isSavingFiles()) {
			return 0;
		}
		return super.getNumberFrames();
	}

	@Override
	public NXDetectorData[] readFrames(int startFrame, int finalFrame) throws DeviceException {
		return fullCalcsDetector.readFrames(startFrame, finalFrame, getName());
	}

	@Override
	public NXDetectorData[] readAllFrames() throws DeviceException {
		return fullCalcsDetector.readFrames(0, xspress3Detector.getController().getNumFramesToAcquire(),
					getName());
	}

	@Override
	public void atScanEnd() throws DeviceException {
		fullCalcsDetector.setReadDataFromFile(false);
		super.atScanEnd();
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		fullCalcsDetector.setReadDataFromFile(false);
		super.atCommandFailure();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (!(xspress3Detector instanceof Xspress3WithFullCalculationsDetector)) {
			throw new IllegalArgumentException("'xspress3Detector must be an instance of Xspress3WithFullCalculationsDetectorv2");
		}
	}

}
