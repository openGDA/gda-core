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

package uk.ac.diamond.daq.client.gui.camera.monitor.widget;

import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import gda.device.DeviceException;
import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.diamond.daq.client.gui.camera.ICameraConfiguration;
import uk.ac.diamond.daq.client.gui.camera.event.CameraControlSpringEvent;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.api.camera.CameraState;
import uk.ac.gda.api.camera.ImageMode;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.client.properties.camera.CameraConfigurationProperties;
import uk.ac.gda.client.properties.camera.StreamConfiguration;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy;

/**
 * Widget to monitor the state of a camera.
 */
public class CameraMonitorButton {

	private static final Logger logger = LoggerFactory.getLogger(CameraMonitorButton.class);

	private Button button;
	private final ICameraConfiguration detectorConfiguration;

	public CameraMonitorButton(ICameraConfiguration detectorConfiguration) {
		this.detectorConfiguration = detectorConfiguration;
	}

	public Button draw(Composite parent) {
		button = new Button(parent, SWT.PUSH);

		updateButtonLayout();

		button.addListener(SWT.Selection, event -> toggleStream());

		try {
			SpringApplicationContextProxy.addDisposableApplicationListener(button, new DetectorEventsListener());
		} catch (GDAClientException e) {
			logger.error("Error attaching listener", e);
		}

		return button;
	}

	private void toggleStream() {
		try {
			switch (getDetectorControl().getAcquireState()) {
			case ACQUIRING:
				stopStream();
				break;
			case IDLE:
				startStream();
				break;
			case UNAVAILABLE:
			default:
				break;

			}
		} catch (DeviceException e) {
			logger.error("Error toggling detector streaming", e);
		}

		updateButtonLayout();
	}

	private CameraControl getDetectorControl() {
		return detectorConfiguration.getCameraControl().orElseThrow();
	}

	private void startStream() throws DeviceException {
		try {
			var detectorControl = getDetectorControl();
			detectorControl.setImageMode(getImageMode());
			detectorControl.setTriggerMode(getTriggerMode());
			detectorControl.startAcquiring();
		} catch (Exception e) {
			throw new DeviceException("Could not start streaming", e);
		}
	}

	private void stopStream() throws DeviceException {
		getDetectorControl().stopAcquiring();
	}

	private class DetectorEventsListener implements ApplicationListener<CameraControlSpringEvent> {
		@Override
		public void onApplicationEvent(CameraControlSpringEvent event) {
			if (detectorConfiguration.getCameraConfigurationProperties().getId().equals(event.getCameraId())) {
				Display.getDefault().asyncExec(CameraMonitorButton.this::updateButtonLayout);
			}
		}

	}

	private ButtonLayout getButtonLayout(CameraControl cameraControl) {
		return getButtonLayout(CameraHelper.getCameraState(cameraControl));
	}

	/**
	 * Maps between the camera state and ButtonLayout
	 * @param state the camera state
	 * @return the button relative layout
	 */
	private ButtonLayout getButtonLayout(CameraState state) {
		if (state == null)
			return ButtonLayout.OTHER;

		switch (state) {
		case ACQUIRING:
			return ButtonLayout.ACQUIRING;
		case IDLE:
			return ButtonLayout.IDLE;
		case UNAVAILABLE:
			return ButtonLayout.UNAVAILABLE;
		default:
			return ButtonLayout.OTHER;
		}
	}

	private void updateButtonLayout() {

		var detectorControl = getDetectorControl();
		var buttonLayout = getButtonLayout(detectorControl);

		var tooltip = String.format("Camera: %s, State: %s", detectorControl.getName(), buttonLayout.getMessage());
		switch (buttonLayout) {
		case ACQUIRING:
			tooltip = tooltip + " \n Push to stop acquiring";
			break;
		case IDLE:
			tooltip = tooltip + " \n Push to start acquiring";
			break;
		case OTHER:
		case UNAVAILABLE:
		default:
			tooltip = tooltip + " \n Please request assistance";
			button.setEnabled(false);
			break;
		}
		button.setToolTipText(tooltip);

		var oldImage = button.getImage();
		if (oldImage != null) oldImage.dispose();

		button.setImage(ClientSWTElements.getImage(buttonLayout.getImage()));
	}

	private ImageMode getImageMode() {
		return Optional.ofNullable(detectorConfiguration.getCameraConfigurationProperties())
						.map(CameraConfigurationProperties::getStreamingConfiguration)
						.map(StreamConfiguration::getImageMode)
						.orElse(ImageMode.CONTINUOUS);
	}

	private short getTriggerMode() {
		return Optional.ofNullable(detectorConfiguration.getCameraConfigurationProperties())
						.map(CameraConfigurationProperties::getStreamingConfiguration)
						.map(StreamConfiguration::getTriggerMode)
						.orElse((short)-1);
	}
}
