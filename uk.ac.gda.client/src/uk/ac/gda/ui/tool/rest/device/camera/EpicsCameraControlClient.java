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

package uk.ac.gda.ui.tool.rest.device.camera;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.gda.api.camera.BinningFormat;
import uk.ac.gda.api.camera.CameraState;
import uk.ac.gda.api.camera.ImageMode;
import uk.ac.gda.client.exception.GDAClientRestException;
import uk.ac.gda.common.entity.device.DeviceValue;
import uk.ac.gda.ui.tool.rest.device.DeviceRestServiceClientBase;

/**
 * Client to control a remote EpicsCameraControl.
 *
 * <p>
 * This client assumes that the remote camera control name is <b>detectorName + "_camera_control"</b>
 * </p>
 *
 * @author Maurizio Nagni
 */
@Service
public class EpicsCameraControlClient {

	@Autowired
	private DeviceRestServiceClientBase service;

	public static final String REMOTE_SERVICE = "camera_control";

	public String getName(String adName) throws GDAClientRestException {
		return (String) service.getDeviceValue(adName, REMOTE_SERVICE, "getName").getValue();
	}

	public double getAcquireTime(String adName) throws GDAClientRestException {
		return (double) service.getDeviceValue(adName, REMOTE_SERVICE, "getAcquireTime").getValue();
	}

	public void setAcquireTime(String adName, double acquiretime) throws GDAClientRestException {
		service.setDeviceValue(adName, REMOTE_SERVICE, "setAcquireTime", acquiretime);
	}

	public void startAcquiring(String adName) throws GDAClientRestException {
		service.commandDevice(adName, REMOTE_SERVICE, "startAcquiring");
	}

	public void stopAcquiring(String adName) throws GDAClientRestException {
		service.commandDevice(adName, REMOTE_SERVICE, "stopAcquiring");
	}

	public CameraState getAcquireState(String adName) throws GDAClientRestException {
		return Optional.ofNullable(service.getDeviceValue(adName, REMOTE_SERVICE, "getAcquireState"))
			.map(DeviceValue::getValue)
			.map(String.class::cast)
			.map(CameraState::valueOf)
			.orElseGet(() -> CameraState.UNAVAILABLE);
	}

	public BinningFormat getBinningPixels(String adName) throws GDAClientRestException {
		return (BinningFormat) service.getDeviceValue(adName, REMOTE_SERVICE, "getBinningPixels").getValue();
	}

	public void setBinningPixels(String adName, BinningFormat binningFormat) throws GDAClientRestException {
		service.setDeviceValue(adName, REMOTE_SERVICE, "setBinningPixels", binningFormat);
	}

	public int[] getFrameSize(String adName) throws GDAClientRestException {
		return (int[]) service.getDeviceValue(adName, REMOTE_SERVICE, "getFrameSize").getValue();
	}

	public int[] getRoi(String adName) throws GDAClientRestException {
		return (int[]) service.getDeviceValue(adName, REMOTE_SERVICE, "getRoi").getValue();
	}

	public void clearRoi(String adName) throws GDAClientRestException {
		service.commandDevice(adName, REMOTE_SERVICE, "clearRoi");
	}

	public int getOverlayCentreX(String adName) throws GDAClientRestException {
		return (int) service.getDeviceValue(adName, REMOTE_SERVICE, "getOverlayCentreX").getValue();
	}

	public int getOverlayCentreY(String adName) throws GDAClientRestException {
		return (int) service.getDeviceValue(adName, REMOTE_SERVICE, "getOverlayCentreY").getValue();
	}

	public void setImageMode(String adName, ImageMode imageMode) throws GDAClientRestException {
		service.setDeviceValue(adName, REMOTE_SERVICE, "setImageMode", imageMode);
	}

	public ImageMode setImageMode(String adName) throws GDAClientRestException {
		return (ImageMode) service.getDeviceValue(adName, REMOTE_SERVICE, "getImageMode").getValue();
	}

	public void setTriggerMode(String adName, short triggerMode) throws GDAClientRestException {
		service.setDeviceValue(adName, REMOTE_SERVICE, "setTriggerMode", triggerMode);
	}

	public short getTriggerMode(String adName) throws GDAClientRestException {
		return (short) service.getDeviceValue(adName, REMOTE_SERVICE, "getTriggerMode").getValue();
	}

	public void enableProcessingFilter(String adName) throws GDAClientRestException {
		service.commandDevice(adName, REMOTE_SERVICE, "enableProcessingFilter");
	}

	public void disableProcessingFilter(String adName) throws GDAClientRestException {
		service.commandDevice(adName, REMOTE_SERVICE, "disableProcessingFilter");
	}

	public void setProcessingFilterType(String adName, int filterType) throws GDAClientRestException {
		service.setDeviceValue(adName, REMOTE_SERVICE, "setProcessingFilterType", filterType);
	}

	public void resetFilter(String adName) throws GDAClientRestException {
		service.commandDevice(adName, REMOTE_SERVICE, "resetFilter");
	}

	public int getImageSizeX(String adName) throws GDAClientRestException {
		return (int) service.getDeviceValue(adName, REMOTE_SERVICE, "getImageSizeX").getValue();
	}

	public int getImageSizeY(String adName) throws GDAClientRestException {
		return (int) service.getDeviceValue(adName, REMOTE_SERVICE, "getImageSizeY").getValue();
	}
}
