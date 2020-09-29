package uk.ac.diamond.daq.client.gui.camera.monitor.widget;

import static uk.ac.gda.ui.tool.WidgetUtilities.getDataObject;

import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.api.camera.CameraState;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.WidgetUtilities;
import uk.ac.gda.ui.tool.images.ClientImages;

/**
 * Supports the {@link CameraMonitorButton} activities
 * 
 * @author Maurizio Nagni
 *
 */
class CameraMonitorButtonHelper {
	
	private static final Logger logger = LoggerFactory.getLogger(CameraMonitorButtonHelper.class);
	
	/**
	 * Pairs of (message, image), per state 
	 */
	enum ButtonLayout {
		UNAVAILABLE(ClientMessages.CAMERA_UNAVAILABLE, ClientImages.STATE_ERROR),
		IDLE(ClientMessages.CAMERA_IDLE, ClientImages.STATE_IDLE),
		ACQUIRING(ClientMessages.CAMERA_ACQUIRING, ClientImages.STATE_ACTIVE),
		OTHER(ClientMessages.CAMERA_OTHER, ClientImages.STATE_WARNING);

		private final ClientMessages message;
		private final ClientImages image;

		ButtonLayout(ClientMessages message, ClientImages image) {
			this.message = message;
			this.image = image;
		}

		public ClientMessages getMessage() {
			return message;
		}

		public ClientImages getImage() {
			return image;
		}
	}

	/**
	 * A key for a CameraControlSpringEvent listener
	 */
	static final String LISTENER = "listener";
	/**
	 * A key for a camera control
	 */
	static final String CAMERA_CONTROL = "cameraControl";
	/**
	 * A key for a ButtonLayout
	 */
	static final String BUTTON_STATE = "buttonState";

	
	/**
	 * Returns the camera state but more crucially, catches when the camera is not available, 
	 * typically when the IOC is unavailable, and return a {@link CameraState#UNAVAILABLE} 
	 * @param cameraControl
	 * @return the camera state
	 */
	static CameraState getCameraState(final CameraControl cameraControl) {
		try {
			return cameraControl.getAcquireState();
		} catch (DeviceException e) {
			return CameraState.UNAVAILABLE;
		}
	}

	static ButtonLayout getButtonLayout(CameraControl cameraControl) {
		return getButtonLayout(getCameraState(cameraControl));
	}

	/**
	 * Maps between the camera state and ButtonLayout
	 * @param state the camera state
	 * @return the button relative layout
	 */
	static ButtonLayout getButtonLayout(CameraState state) {
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
	static void updateButtonListener(Button button, CameraState state) {
		Optional.ofNullable(getButtonListener(button)).ifPresent(button::removeSelectionListener);

		switch (state) {
		case IDLE:
			button.setData(LISTENER, SelectionListener.widgetSelectedAdapter(event -> updateAcquisition(event, CameraMonitorButtonHelper::startAcquiring)));
			WidgetUtilities.addWidgetDisposableListener(button, getButtonListener(button));
			break;
		case ACQUIRING:
			button.setData(LISTENER, SelectionListener.widgetSelectedAdapter(event -> updateAcquisition(event, CameraMonitorButtonHelper::stopAcquiring)));
			WidgetUtilities.addWidgetDisposableListener(button, getButtonListener(button));
			break;	
		default:
			break;
		}
	}

	/**
	 * Updates a button layout and its associated listener
	 * @param button the button to update
	 * @param name the camera control name
	 * @param cameraState the camera state to set
	 */
	static void updateButtonLayoutAndListener(Button button, String name, CameraState cameraState) {
		// Layout			
		updateButtonLayout(button, name, getButtonLayout(cameraState));

		// Listener
		updateButtonListener(button, cameraState);
	}
	
	private static void updateButtonLayout(final Button button, String cameraName, final ButtonLayout buttonLayout) {
		button.setData(BUTTON_STATE, buttonLayout);
		button.setEnabled(true);
		String tooltip = String.format("Camera: %s, State: %s", cameraName, buttonLayout.getMessage());
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

	
	/**
	 * Utility to cast a LISTENER data object
	 * @param button
	 * @return the button associated CameraControlSpringEvent listener, otherwise {@code null}
	 */
	private static SelectionListener getButtonListener(Button button) {
		return getDataObject(button, SelectionListener.class, LISTENER);
	}

	/**
	 * Utility to cast a CAMERA_CONTROL data object
	 * @param button
	 * @return the button associated CameraControlSpringEvent listener, otherwise {@code null}
	 */
	static CameraControl getButtonCameraControl(Button button) {
		return getDataObject(button, CameraControl.class, CAMERA_CONTROL);
	}

	/**
	 * Utility to cast a BUTTON_STATE data object
	 * @param button
	 * @return the button associated CameraControlSpringEvent listener, otherwise {@code null}
	 */
	static ButtonLayout getButtonCameraState(Button button) {
		return getDataObject(button, ButtonLayout.class, BUTTON_STATE);
	}
	
	private static void updateAcquisition(SelectionEvent event, Consumer<CameraControl> controlConsumer) {
		Button button = Button.class.cast(event.widget);
		Optional.ofNullable(getButtonCameraControl(button)).ifPresent(controlConsumer); 
	}
	
	private static void startAcquiring(CameraControl cameraControl) {
		try {
			cameraControl.startAcquiring();
		} catch (DeviceException e) {
			logger.error("Error starting acquisition for {} ", cameraControl.getName());
		}
	}
	
	private static void stopAcquiring(CameraControl cameraControl) {
		try {
			cameraControl.stopAcquiring();
		} catch (DeviceException e) {				
			logger.error("Error stopping acquisition for {} ", cameraControl.getName());
		}
	}
}
