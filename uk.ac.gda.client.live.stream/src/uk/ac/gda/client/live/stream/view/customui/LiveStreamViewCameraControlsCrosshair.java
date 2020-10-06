/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.gda.client.live.stream.view.customui;

import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.client.livecontrol.LiveControl;

public class LiveStreamViewCameraControlsCrosshair implements LiveStreamViewCameraControlsExtension {

	private static final Logger logger = LoggerFactory.getLogger(LiveStreamViewCameraControlsCrosshair.class);

	private final LiveControl centreX;
	private final LiveControl centreY;
	private Scannable sizeX;
	private Scannable sizeY;
	private double crosshairSizeFactor = 2;

	public LiveStreamViewCameraControlsCrosshair(LiveControl centreX, LiveControl centreY,
			Scannable sizeX, Scannable sizeY) {
		this.centreX = centreX;
		this.centreY = centreY;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
	}


	@Override
	public void createUi(Composite composite, CameraControl cameraControl) {

		try {
			sizeX.moveTo(cameraControl.getImageSizeX()*crosshairSizeFactor);
			sizeY.moveTo(cameraControl.getImageSizeY()*crosshairSizeFactor);
		} catch (DeviceException e) {
			logger.error("Error with setting crosshair size");
		}
		centreX.createControl(composite);
		centreY.createControl(composite);
	}

	public void setCrosshairSizeFactor(Double crosshairSizeFactor) {
		this.crosshairSizeFactor = crosshairSizeFactor;
	}

}
