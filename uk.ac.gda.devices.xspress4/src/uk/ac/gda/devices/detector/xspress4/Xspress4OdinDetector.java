/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.detector.xspress4;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.factory.FactoryException;

public class Xspress4OdinDetector extends Xspress4Detector {

	private static final Logger logger = LoggerFactory.getLogger(Xspress4OdinDetector.class);

	@Override
	public void configure() throws FactoryException {
		super.configure();
	}

	@Override
	public void atScanStart() throws DeviceException {
		super.atScanStart();
	}

	@Override
	public void setupNumFramesToCollect(int numberOfFramesToCollect) throws DeviceException {
		getController().setNumImages(numberOfFramesToCollect);
		//set the number of frames in the Odin writer (if using)
		if (isWriteHDF5Files()) {
		}
	}

	@Override
	public void atScanLineStart() throws DeviceException {
		// Start Acquire if using hardware triggering (i.e. detector waits for external trigger for each frame)
		if (getTriggerMode() != TriggerMode.Software) {
			getController().startAcquire();
		}
	}

	@Override
	public void atPointStart() throws DeviceException {
		super.atPointStart();
	}

	@Override
	public void atScanEnd() throws DeviceException {
//		super.atScanEnd();
		// Stop the file writer
		// Do some stuff here - make link to hdf file(s) in Nexus etc.

	}

	@Override
	public void atCommandFailure() throws DeviceException {
		getController().stopAcquire();
		// Stop the hdf writer (if used)
		atScanEnd();
	}

	@Override
	public void stop() throws DeviceException {
//		xspress3Controller.doStop();
		atScanEnd();
	}

	@Override
	public void acquireFrameAndWait(double collectionTimeMillis, double timeoutMillis) throws DeviceException {
		int numFramesBeforeAcquire = getController().getTotalFramesAvailable();
		logger.info(":Acquire called");
		getController().startAcquire();
		try {
			Thread.sleep(100);
			getController().sendSoftwareTrigger();
			Thread.sleep((long)collectionTimeMillis);
			getController().waitForCounterToIncrement(numFramesBeforeAcquire, (long)timeoutMillis);
		} catch (InterruptedException e) {
			// Reset interrupt status
			Thread.currentThread().interrupt();

			logger.warn("Interrupted while waiting for acquire");
		}
		logger.info("Wait for acquire finished");
		if (getController().getTotalFramesAvailable()==numFramesBeforeAcquire) {
			logger.warn("Acquire not finished after waiting for {} secs", timeoutMillis*0.001);
		}
	}

	@Override
	public double[][] getMCAData(double timeMillis) throws DeviceException {
		double mcaArrayData[][] = null;
		try{
			getController().stopAcquire();

			// Store the currently set trigger mode
			TriggerMode trigMode = getTriggerMode();

			//Set software trigger mode, collection for 1 frame of data
			setTriggerMode(TriggerMode.Software);
			getController().setNumImages(1);
			setAcquireTime(timeMillis*0.001);

			// Record frame of data on detector
			acquireFrameAndWait(timeMillis, timeMillis);
//			getController().stopAcquire();

			// Reset trigger mode to original value
			setTriggerMode(trigMode);

			// Convert to 2d array of doubles [num channels][num mca bins]
			mcaArrayData = getController().getMcaData();
		} catch (DeviceException e) {
			logger.error("Problem getting MCA data", e);
			throw e;
		}
		return mcaArrayData;
	}
}
