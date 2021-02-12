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

import static uk.ac.gda.ui.tool.ClientSWTElements.createClientButton;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;
import static uk.ac.gda.ui.tool.WidgetUtilities.getDataObject;

import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.WidgetUtilities;
import uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy;

/**
 * Widget to monitor the state of a camera.
 *
 * <p>
 * Listening at {@link CameraControlSpringEvent} this widget can dynamically change its properties according to the {@link CameraControlSpringEvent#getCameraState()}.
 * For each state, the button provides:
 * <ul>
 * <li>
 * 	A colour in line with EPICS standards. See {@link ButtonLayout}
 * </li>
 * <li>
 * 	A tooltip with essential information as camera name and state
 * </li>
 * <li>
 * 	For {@code CameraState#IDLE} and {@code CameraState#ACQUIRING} an associated action allows to turn off and on the acquisition
 * </li>
 * </ul>
 * </p>
 *
 *
 * @author Maurizio Nagni
 *
 */
public class CameraMonitorButton {

	private static final Logger logger = LoggerFactory.getLogger(CameraMonitorButton.class);

	/**
	 * A key for a CameraControlSpringEvent listener
	 */
	private static final String LISTENER = "listener";

	/**
	 * A key for a ButtonLayout
	 */
	private static final String BUTTON_STATE = "buttonState";

	/**
	 * A key for a camera control
	 */
	static final String CAMERA_CONTROL = "cameraControl";

	private Button button;
	private final ICameraConfiguration iCameraConfiguration;

	private static final String ERROR_STOPPING = "Error stopping acquisition for {} ";
	private static final String ERROR_STARTING = "Error starting acquisition for {} ";

	/**
	 * Creates the button and place it in the parent Composite
	 * @param parent the composite where draw the button
	 * @param cameraConfiguration the configuration associated with the camera
	 * @throws GDAClientException if {@link ICameraConfiguration#getCameraControl()} is {@link Optional#empty()}
	 */
	public CameraMonitorButton(Composite parent, ICameraConfiguration cameraConfiguration) throws GDAClientException {
		this.iCameraConfiguration = cameraConfiguration;
		var cameraControl = cameraConfiguration.getCameraControl()
				.orElseThrow(() -> new GDAClientException("Cannot create a button without a CameraControl"));

		createButton(parent, cameraControl);

		updateButtonLayoutAndListener(CameraHelper.getCameraState(cameraControl));
		try {
			SpringApplicationContextProxy.addDisposableApplicationListener(button, cameraControlSpringEventListener);
		} catch (GDAClientException e) {
			logger.error("Cannot add CameraControlSpringEventListener to CameraMonitorButton for {}",
					cameraControl.getName());
		}
	}

	/**
	 * The camera monitoring button
	 * @return the internal {@code Button}
	 */
	public Button getButton() {
		return button;
	}

	private void createButton(Composite parent, CameraControl cameraControl) {
		var layout = getButtonLayout(cameraControl);
		button = createClientButton(parent, SWT.NONE, ClientMessages.EMPTY_MESSAGE, layout.getMessage(),
				layout.getImage());
		createClientGridDataFactory().align(SWT.CENTER, SWT.CENTER).grab(false, false).applyTo(button);
		button.setEnabled(true);
		// Any other call to the camera control will be assumed as already done
		button.setData(CAMERA_CONTROL, cameraControl);
	}

	// At the moment is not possible to use anonymous lambda expression because it
	// generates a class cast exception
	private ApplicationListener<CameraControlSpringEvent> cameraControlSpringEventListener = new ApplicationListener<CameraControlSpringEvent>() {
		@Override
		public void onApplicationEvent(CameraControlSpringEvent event) {
			Display.getDefault().asyncExec(() -> {
				if (iCameraConfiguration.getCameraConfigurationProperties().getId().equals(event.getCameraId()))
					update(event);
			});
		}

		private void update(CameraControlSpringEvent event) {
			logger.debug("CameraControlSpringEvent {}", event);
			var buttonLayout = getButtonCameraState(button);
			logger.debug("buttonLayout {}", buttonLayout);

			var newCameraState = event.getCameraState();
			var eventLayout = getButtonLayout(newCameraState);
			logger.debug("eventLayout {}", eventLayout);

			if (buttonLayout != null && eventLayout.equals(buttonLayout)) return;
			updateButtonLayoutAndListener(newCameraState);
		}

		/**
		 * Utility to cast a BUTTON_STATE data object
		 * @param button
		 * @return the button associated CameraControlSpringEvent listener, otherwise {@code null}
		 */
		private ButtonLayout getButtonCameraState(Button button) {
			return getDataObject(button, ButtonLayout.class, BUTTON_STATE);
		}
	};

	/**
	 * Updates a button layout and its associated listener
	 * @param newCameraState the camera state to set
	 */
	private void updateButtonLayoutAndListener(CameraState newCameraState) {
		var newButtonLayout = getButtonLayout(newCameraState);
		// Layout
		updateButtonLayout(newButtonLayout);

		// Listener
		updateButtonListener(newCameraState);
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

	/**
	 * For each state associate the correct selection listener to the button
	 * state: IDLE --> action: startAcquire,
	 * state: ACQUIRE --> action: stopAcquire
	 *
	 * @param button
	 * @param state
	 */
	private void updateButtonListener(CameraState state) {
		Optional.ofNullable(getButtonListener(button)).ifPresent(a -> WidgetUtilities.removeWidgetDisposableListener(button, a));

		switch (state) {
		case IDLE:
			button.setData(LISTENER, SelectionListener.widgetSelectedAdapter(this::startAcquiring));
			WidgetUtilities.addWidgetDisposableListener(button, getButtonListener(button));
			break;
		case ACQUIRING:
			button.setData(LISTENER, SelectionListener.widgetSelectedAdapter(this::stopAcquiring));
			WidgetUtilities.addWidgetDisposableListener(button, getButtonListener(button));
			break;
		default:
			break;
		}
	}

	private void startAcquiring(SelectionEvent event) {
		try {
			setImageMode();
			setTriggerMode();
			getButtonCameraControl(button).startAcquiring();
		} catch (DeviceException e) {
			logger.error(ERROR_STARTING, getButtonCameraControl(button).getName());
		}
	}

	private void stopAcquiring(SelectionEvent event) {
		try {
			getButtonCameraControl(button).stopAcquiring();
		} catch (DeviceException e) {
			logger.error(ERROR_STOPPING, getButtonCameraControl(button).getName());
		}
	}

	private void updateButtonLayout(final ButtonLayout buttonLayout) {
		String cameraName = getButtonCameraControl(button).getName();
		button.setData(BUTTON_STATE, buttonLayout);
		button.setEnabled(true);
		var tooltip = String.format("Camera: %s, State: %s", cameraName, buttonLayout.getMessage());
		if (ButtonLayout.UNAVAILABLE.equals(buttonLayout)) {
			tooltip = tooltip + " \n Please request assistance";
			button.setEnabled(false);
		}
		if (ButtonLayout.IDLE.equals(buttonLayout)) {
			tooltip = tooltip + " \n Push to start acquiring";
		}
		if (ButtonLayout.ACQUIRING.equals(buttonLayout)) {
			tooltip = tooltip + " \n Push to stop acquiring";
		}
		button.setToolTipText(tooltip);
		button.setImage(ClientSWTElements.getImage(buttonLayout.getImage()));
		button.getParent().layout(true, true);
	}

	private void setImageMode() {
		var imageMode = Optional.ofNullable(iCameraConfiguration.getCameraConfigurationProperties())
				.map(CameraConfigurationProperties::getStreamingConfiguration)
				.map(StreamConfiguration::getImageMode)
				.orElse(ImageMode.CONTINUOUS);
		setImageMode(imageMode);
}

	private void setImageMode(ImageMode imageMode) {
		try {
			getButtonCameraControl(button).setImageMode(imageMode);
		} catch (Exception e) {
			logger.error(ERROR_STOPPING, getButtonCameraControl(button).getName());
		}
	}

	private void setTriggerMode() {
		Short triggerMode = Optional.ofNullable(iCameraConfiguration.getCameraConfigurationProperties())
				.map(CameraConfigurationProperties::getStreamingConfiguration)
				.map(StreamConfiguration::getTriggerMode)
				.orElse((short)-1);
		setTriggerMode(triggerMode);
	}

	private void setTriggerMode(Short triggerMode) {
		try {
			getButtonCameraControl(button).setTriggerMode(triggerMode);
		} catch (Exception e) {
			logger.error(ERROR_STOPPING, getButtonCameraControl(button).getName());
		}
	}

	/**
	 * Utility to cast a LISTENER data object
	 * @param button
	 * @return the button associated CameraControlSpringEvent listener, otherwise {@code null}
	 */
	private SelectionListener getButtonListener(Button button) {
		return getDataObject(button, SelectionListener.class, LISTENER);
	}

	/**
	 * Utility to cast a CAMERA_CONTROL data object
	 * @param button
	 * @return the button associated CameraControlSpringEvent listener, otherwise {@code null}
	 */
	private CameraControl getButtonCameraControl(Button button) {
		return getDataObject(button, CameraControl.class, CAMERA_CONTROL);
	}
}
