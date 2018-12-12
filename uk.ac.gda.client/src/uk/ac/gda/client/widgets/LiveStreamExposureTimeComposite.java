/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.gda.client.widgets;

import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import uk.ac.gda.api.camera.CameraControl;

/**
 * Control the exposure time of a camera, warning the user if they have input an invalid exposure time
 */
public class LiveStreamExposureTimeComposite extends Composite {
	private static final Logger logger = LoggerFactory.getLogger(LiveStreamExposureTimeComposite.class);

	private static final Color COLOUR_RED = Display.getCurrent().getSystemColor(SWT.COLOR_RED);

	/**
	 * Width of {@link #exposureTimeText}
	 */
	private static final int TEXT_WIDTH = 70;

	/**
	 * Text box to edit the exposure time
	 */
	private final Text exposureTimeText;

	/**
	 * Width of {@link #exposureTimeMessage}
	 */
	private static final int MESSAGE_WIDTH = 150;

	/**
	 * 	Label to show error message if exposure time is invalid
	 */
	private final Label exposureTimeMessage;

	private final CameraControl cameraControl;

	public LiveStreamExposureTimeComposite(Composite parent, int style, CameraControl cameraControl) {
		super(parent, style);
		Objects.requireNonNull(cameraControl, "Camera control must not be null");
		this.cameraControl = cameraControl;
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(this);

		final Label label = new Label(this, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(label);
		label.setText("Exposure time");

		exposureTimeText = new Text(this, SWT.BORDER);
		GridDataFactory.swtDefaults().hint(TEXT_WIDTH, SWT.DEFAULT).applyTo(exposureTimeText);
		exposureTimeText.addModifyListener(e -> {
			if (exposureTimeValid()) {
				clearError();
				setExposureTime();
			} else {
				displayError("Invalid exposure time");
			}
		});

		// Label to show error message if exposure time is invalid
		exposureTimeMessage = new Label(this, SWT.NONE);
		GridDataFactory.swtDefaults().hint(MESSAGE_WIDTH, SWT.DEFAULT).applyTo(exposureTimeMessage);
		exposureTimeMessage.setForeground(COLOUR_RED);
		exposureTimeMessage.setVisible(false);

		// Get the current exposure time from the camera and display in the page
		try {
			exposureTimeText.setText(Double.toString(cameraControl.getAcquireTime()));
		} catch (DeviceException e) {
			final String message = String.format("Error getting exposure time from camera %s", cameraControl.getName());
			logger.error(message, e);
			exposureTimeText.setText("#ERR");
			displayError(message);
		}
	}

	private double parseExposureTime() {
		return Double.parseDouble(exposureTimeText.getText());
	}

	private boolean exposureTimeValid() {
		try {
			final double exposureTime = parseExposureTime();
			return exposureTime > 0.0;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Set the exposure time on the camera to the value entered by the user
	 */
	private void setExposureTime() {
		try {
			final double exposureTime = parseExposureTime();
			cameraControl.setAcquireTime(exposureTime);
		} catch (Exception ex) {
			final String message = String.format("Error setting exposure time on camera %s", cameraControl.getName());
			logger.error(message, ex);
			displayError(message);
		}
	}

	private void displayError(String message) {
		exposureTimeMessage.setText(message);
		exposureTimeMessage.setToolTipText(message);
		exposureTimeMessage.setVisible(true);
		update();
	}

	private void clearError() {
		exposureTimeMessage.setVisible(false);
		update();
	}
}
