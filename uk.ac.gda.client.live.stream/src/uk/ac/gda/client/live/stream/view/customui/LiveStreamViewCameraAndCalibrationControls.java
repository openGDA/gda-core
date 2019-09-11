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

import java.util.Objects;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.client.live.stream.calibration.BeamPositionCalibration;

public class LiveStreamViewCameraAndCalibrationControls extends LiveStreamViewCameraControls {

	private static final Logger logger = LoggerFactory.getLogger(LiveStreamViewCameraAndCalibrationControls.class);

	private BeamPositionCalibration calibration;

	public LiveStreamViewCameraAndCalibrationControls(CameraControl cameraControl, BeamPositionCalibration calibration) {
		super(cameraControl);
		Objects.requireNonNull(calibration, "Beam position calibration should not be null.");
		this.calibration = calibration;
	}

	@Override
	public void createUi(Composite composite) {
		super.createUi(composite);
		GridLayoutFactory.fillDefaults().numColumns(5).applyTo(mainComposite);
		final Button calibrateButton = new Button(mainComposite, SWT.PUSH);
		GridDataFactory.swtDefaults().applyTo(calibrateButton);
		calibrateButton.setText("Calibrate");
		calibrateButton.setToolTipText("Set beam position to cross overlay position");
		calibrateButton.addSelectionListener(widgetSelectedAdapter(this::calibrate));
	}

	private void calibrate(SelectionEvent e) {
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
}