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
		if (isWriteHDF5Files()) {
//			try {
//				// Wait a short time for file writing settings previously set (e.g. number of frames)
//				// to be applied - otherwise metawriter often fails to start.
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//				// do nothing
//			}
			getController().startHdfWriter();
		}
		getController().startAcquire();
		waitForCounterToReset();
	}

	private void waitForCounterToReset() throws DeviceException {
		int currentNumFrames = getController().getTotalFramesAvailable();
		if (currentNumFrames != 0) {
			logger.debug("Waiting for array counter to reset to zero");
			try {
				getController().waitForCounterToIncrement(currentNumFrames, 1000L);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new DeviceException(e);
			}
		}
	}
	@Override
	public void atPointStart() throws DeviceException {
		super.atPointStart();
	}

	@Override
	public void atScanEnd() throws DeviceException {
		if (!isWriteHDF5Files()) {
			return;
		}
		waitForFileWriter(); // hdf writer
		//also check the number of frames in the Meta writer?
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		getController().stopAcquire();
		// Stop the hdf writer (if used)
		atScanEnd();
	}

	@Override
	public void stop() throws DeviceException {
		getController().stopAcquire();
		atScanEnd();
	}

	@Override
	public void acquireFrameAndWait(double collectionTimeMillis, double timeoutMillis) throws DeviceException {
		int numFramesBeforeAcquire = getController().getTotalFramesAvailable();
		logger.info("acquireFrameAndWait called. Current number of frames = {}", numFramesBeforeAcquire);
		// Don't call getControll().startAcquire() here - detector is already acquiring,
		// just need to send a 'software trigger' and wait for the counter to change
		getController().sendSoftwareTrigger();
		try {
			getController().waitForCounterToIncrement(numFramesBeforeAcquire, (long)timeoutMillis);
		} catch (InterruptedException e) {
			// Reset interrupt status
			Thread.currentThread().interrupt();
			logger.warn("Interrupted while waiting for acquire");
		}
		logger.info("Wait for acquire finished. {} frames available", getController().getTotalFramesAvailable());
		if (getController().getTotalFramesAvailable()==numFramesBeforeAcquire) {
			logger.warn("Acquire not finished after waiting for {} secs", timeoutMillis*0.001);
		}
	}

	@Override
	public double[][] getMCAData(double timeMillis) throws DeviceException {
		double mcaArrayData[][] = null;
		try {
			getController().stopAcquire();

			// Store the currently set trigger mode
			TriggerMode trigMode = getTriggerMode();

			//Set software trigger mode, collection for 1 frame of data
			setTriggerMode(TriggerMode.Software);
			getController().setNumImages(1);
			setAcquireTime(timeMillis*0.001);

			// Record frame of data on detector
			getController().startAcquire();
			waitForCounterToReset();
			acquireFrameAndWait(timeMillis, 2*timeMillis);
			waitWhileBusy();

			// Reset trigger mode to original value
			setTriggerMode(trigMode);

			mcaArrayData = getController().getMcaData();
		} catch (DeviceException e) {
			throw new DeviceException("Problem collecting MCA data : "+e.getMessage(), e);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.error("Interrupted while reading MCA data from {}", getName(), e);
		}
		return mcaArrayData;
	}
}
