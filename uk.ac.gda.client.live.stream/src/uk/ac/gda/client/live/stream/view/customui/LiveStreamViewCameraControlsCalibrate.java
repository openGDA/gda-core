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

package uk.ac.gda.client.live.stream.view.customui;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.client.live.stream.calibration.BeamPositionCalibration;

public class LiveStreamViewCameraControlsCalibrate implements LiveStreamViewCameraControlsExtension {
	private static final Logger logger = LoggerFactory.getLogger(LiveStreamViewCameraControlsCalibrate.class);

	private BeamPositionCalibration calibration;
	private CameraControl cameraControl;

	@Override
	public void createUi(Composite composite, CameraControl cameraControl) {
		if (calibration == null) {
			logger.error("Beam position calibration must not be null.");
			return;
		}
		this.cameraControl = cameraControl;

		final Composite mainComposite = new Composite(composite, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(mainComposite);
		GridLayoutFactory.swtDefaults().margins(0, 5).applyTo(mainComposite);

		final Button calibrateButton = new Button(mainComposite, SWT.PUSH);
		GridDataFactory.fillDefaults().applyTo(calibrateButton);
		calibrateButton.setText("Calibrate");
		calibrateButton.setToolTipText("Set beam position to cross overlay position");
		calibrateButton.addSelectionListener(widgetSelectedAdapter(e -> calibrate()));
	}

	private void calibrate() {
		int overlayPositionX;
		int overlayPositionY;

		if (MessageDialog.openConfirm(
				Display.getCurrent().getActiveShell(),
				"Confirm beam position calibration",
				"This will set the beam position to the current cross overlay position. Do you want to continue?")) {

			try {
				overlayPositionX = cameraControl.getOverlayCentreX();
				overlayPositionY = cameraControl.getOverlayCentreY();
			} catch (DeviceException exception) {
				logger.error("Unable to get overlay co-ordinates.", exception);
				return;
			}

			calibration.setBeamPosition(overlayPositionX, overlayPositionY, true);
		}
	}

	public void setCalibration(BeamPositionCalibration calibration) {
		this.calibration = calibration;
	}

}
