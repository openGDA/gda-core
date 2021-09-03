/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.odin;

import gda.device.DeviceBase;
import gda.device.DeviceException;

/**
 * Odin detectors share many common components but each individual detector behaves slightly differently.
 * This decorator can be used to define specific detector behaviour
 *
 */
public abstract class OdinDetectorDecorator extends DeviceBase implements OdinDetectorController {

	private final OdinDetectorController controller;


	protected OdinDetectorDecorator(OdinDetectorController delegate) {
		this.controller = delegate;
	}


	@Override
	public void setDataOutput(String directory, String filePrefix) throws DeviceException {
		controller.setDataOutput(directory, filePrefix);

	}

	@Override
	public void startCollection() throws DeviceException {
		controller.startCollection();

	}

	@Override
	public void stopCollection() throws DeviceException {
		controller.stopCollection();
	}

	@Override
	public void startRecording() throws DeviceException {
		controller.startRecording();

	}

	@Override
	public void endRecording() {
		controller.endRecording();

	}

	@Override
	public void prepareCamera(int frames, double requestedLiveTime, double requestedDeadTime, String imageMode,
			String triggerMode) throws DeviceException {
		controller.prepareCamera(frames, requestedLiveTime, requestedDeadTime, imageMode, triggerMode);

	}

	@Override
	public String getLatestFilename() {
		return controller.getLatestFilename();
	}

	@Override
	public int getStatus() {
		return controller.getStatus();
	}

	@Override
	public void waitWhileAcquiring() {
		controller.waitWhileAcquiring();

	}

	@Override
	public void waitWhileWriting() {
		controller.waitWhileWriting();

	}

	@Override
	public void prepareDataWriter(int frames) throws DeviceException {
		controller.prepareDataWriter(frames);

	}

	@Override
	public Double getAcquireTime() throws DeviceException {
		return controller.getAcquireTime();
	}

	@Override
	public void setOffsetAndUid(int offset, int uid) throws DeviceException {
		controller.setOffsetAndUid(offset, uid);
	}

	@Override
	public int getNumFramesCaptured() {
		return controller.getNumFramesCaptured();
	}

	@Override
	public void waitForWrittenFrames(int noFrames) {
		controller.waitForWrittenFrames(noFrames);
	}

}
