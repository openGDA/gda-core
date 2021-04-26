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

package uk.ac.gda.ui.tool.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import uk.ac.gda.api.camera.BinningFormat;
import uk.ac.gda.api.camera.CameraState;
import uk.ac.gda.api.camera.ImageMode;
import uk.ac.gda.client.exception.GDAClientRestException;
import uk.ac.gda.ui.tool.rest.device.camera.EpicsCameraControlClient;
import uk.ac.gda.ui.tool.rest.device.camera.NDROIClient;


/**
 * Exposes the same CameraOperation methods but from the client point of view.
 *
 * <p>
 * The class is internally implemented as prototype spring bean.
 * </p>
 *
 *
 * @author Maurizio Nagni
 */
@Controller("cameraControlClient")
@Scope("prototype")
public class CameraControlClient {

	@Autowired
	private NDROIClient ndROIClient;

	@Autowired
	private EpicsCameraControlClient epicsCameraClient;

	private final String cameraControlName;

	public CameraControlClient() {
		this.cameraControlName = null;
	}

	public CameraControlClient(String cameraControlName) {
		this.cameraControlName = cameraControlName;
	}

	public String getName() throws GDAClientRestException {
		return getEpicsCameraClient().getName(cameraControlName);
	}

	public double getAcquireTime() throws GDAClientRestException {
		return getEpicsCameraClient().getAcquireTime(cameraControlName);
	}

	public void setAcquireTime(double acquiretime) throws GDAClientRestException {
		getEpicsCameraClient().setAcquireTime(cameraControlName, acquiretime);
	}

	public void startAcquiring() throws GDAClientRestException {
		getEpicsCameraClient().startAcquiring(cameraControlName);
	}

	public void stopAcquiring() throws GDAClientRestException {
		getEpicsCameraClient().stopAcquiring(cameraControlName);
	}

	public CameraState getAcquireState() throws GDAClientRestException {
		return getEpicsCameraClient().getAcquireState(cameraControlName);
	}

	public BinningFormat getBinningPixels() throws GDAClientRestException {
		return getEpicsCameraClient().getBinningPixels(cameraControlName);
	}

	public void setBinningPixels(BinningFormat binningFormat) throws GDAClientRestException {
		getEpicsCameraClient().setBinningPixels(cameraControlName, binningFormat);
	}

	public int[] getFrameSize() throws GDAClientRestException {
		return getEpicsCameraClient().getFrameSize(cameraControlName);
	}

	public int[] getRoi() throws GDAClientRestException {
		return getEpicsCameraClient().getRoi(cameraControlName);
	}

	public void setRoi(int minX, int minY, int sizeX, int sizeY) throws GDAClientRestException {
		getNDROIClient().setMinX(cameraControlName, minX);
		getNDROIClient().setMinY(cameraControlName, minY);
		getNDROIClient().setSizeX(cameraControlName, sizeX);
		getNDROIClient().setSizeY(cameraControlName, sizeY);
	}

	public void clearRoi() throws GDAClientRestException {
		getEpicsCameraClient().clearRoi(cameraControlName);
	}

	public int getOverlayCentreX() throws GDAClientRestException {
		return getEpicsCameraClient().getOverlayCentreX(cameraControlName);
	}

	public int getOverlayCentreY() throws GDAClientRestException {
		return getEpicsCameraClient().getOverlayCentreY(cameraControlName);
	}

	public void setImageMode(ImageMode imageMode) throws GDAClientRestException {
		getEpicsCameraClient().setImageMode(cameraControlName, imageMode);
	}

	public ImageMode getImageMode() throws GDAClientRestException {
		return getEpicsCameraClient().setImageMode(cameraControlName);
	}

	public void setTriggerMode(short triggerMode) throws GDAClientRestException {
		getEpicsCameraClient().setTriggerMode(cameraControlName, triggerMode);
	}

	public short getTriggerMode() throws GDAClientRestException {
		return getEpicsCameraClient().getTriggerMode(cameraControlName);
	}

	public void enableProcessingFilter() throws GDAClientRestException {
		getEpicsCameraClient().enableProcessingFilter(cameraControlName);
	}

	public void disableProcessingFilter() throws GDAClientRestException {
		getEpicsCameraClient().disableProcessingFilter(cameraControlName);

	}

	public void setProcessingFilterType(int filterType) throws GDAClientRestException {
		getEpicsCameraClient().setProcessingFilterType(cameraControlName, filterType);
	}

	public void resetFilter() throws GDAClientRestException {
		getEpicsCameraClient().resetFilter(cameraControlName);
	}

	public int getImageSizeX() throws GDAClientRestException {
		return getEpicsCameraClient().getImageSizeX(cameraControlName);
	}


	public int getImageSizeY() throws GDAClientRestException {
		return getEpicsCameraClient().getImageSizeY(cameraControlName);
	}

	private NDROIClient getNDROIClient() {
		return ndROIClient;
	}

	private EpicsCameraControlClient getEpicsCameraClient() {
		return epicsCameraClient;
	}
}
