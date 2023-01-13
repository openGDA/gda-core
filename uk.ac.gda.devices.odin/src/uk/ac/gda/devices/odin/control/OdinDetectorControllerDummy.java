/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.odin.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;

public class OdinDetectorControllerDummy implements OdinDetectorController {

	private static final Logger logger = LoggerFactory.getLogger(OdinDetectorControllerDummy.class);

	private Double collectionTime = 1.0;

	private String h5FileName = "/path/to/some/file.h5";

	@Override
	public void setDataOutput(String directory, String filePrefix) throws DeviceException {
		logger.info("setDataOutput called");

	}

	@Override
	public void startCollection() throws DeviceException {
		logger.info("startCollection called");

	}

	@Override
	public void stopCollection() throws DeviceException {
		logger.info("stopCollection called");

	}

	@Override
	public void startRecording() throws DeviceException {
		logger.info("startRecording called");

	}

	@Override
	public void endRecording() {
		logger.info("endRecording called");

	}

	@Override
	public String getLatestFilename() {
		logger.info("getLastFile called");
		return h5FileName;
	}

	@Override
	public int getStatus() {
		return 0;
	}

	@Override
	public double getAcquireTime() throws DeviceException {
		return collectionTime;
	}

	@Override
	public void prepareDataWriter(int frames) throws DeviceException {

	}

	@Override
	public void setOffsetAndUid(int offset, int uid) {

	}


	@Override
	public void prepareCamera(int frames, double requestedLiveTime, double requestedDeadTime, String imageMode,
			String triggerMode) throws DeviceException {
		logger.info("setExposureTimes called");
		collectionTime = requestedLiveTime;

	}


	@Override
	public void waitWhileAcquiring() {
	}

	@Override
	public void waitWhileWriting() {
	}

	@Override
	public int getNumFramesCaptured() {
		return 0;
	}

	@Override
	public void waitForWrittenFrames(int noFrames) {
	}

	@Override
	public void setNumImages(int numImages) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCompressionMode(String mode) throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAcquireTime(double acquireTime) throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public double getAcquirePeriod() throws DeviceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setAcquirePeriod(double acquirePeriod) throws DeviceException {
		// TODO Auto-generated method stub

	}

}
