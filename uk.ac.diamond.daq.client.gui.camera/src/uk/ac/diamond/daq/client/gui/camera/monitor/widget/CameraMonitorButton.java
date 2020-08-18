package uk.ac.diamond.daq.client.gui.camera.monitor.widget;

import static uk.ac.diamond.daq.client.gui.camera.monitor.widget.CameraMonitorButtonHelper.CAMERA_CONTROL;
import static uk.ac.diamond.daq.client.gui.camera.monitor.widget.CameraMonitorButtonHelper.getButtonCameraControl;
import static uk.ac.diamond.daq.client.gui.camera.monitor.widget.CameraMonitorButtonHelper.getButtonCameraState;
import static uk.ac.diamond.daq.client.gui.camera.monitor.widget.CameraMonitorButtonHelper.getButtonLayout;
import static uk.ac.diamond.daq.client.gui.camera.monitor.widget.CameraMonitorButtonHelper.getCameraState;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientButton;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;

import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import uk.ac.diamond.daq.client.gui.camera.ICameraConfiguration;
import uk.ac.diamond.daq.client.gui.camera.event.CameraControlSpringEvent;
import uk.ac.diamond.daq.client.gui.camera.monitor.widget.CameraMonitorButtonHelper.ButtonLayout;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.api.camera.CameraState;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.ui.tool.ClientMessages;
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
	
	private Button button;
	private final ICameraConfiguration cameraConfiguration;

	/**
	 * Creates the button and place it in the parent Composite 
	 * @param parent the composite where draw the button
	 * @param cameraControl the control associated with the camera
	 * @throws GDAClientException if {@link ICameraConfiguration#getCameraControl()} is {@link Optional#empty()}
	 */
	public CameraMonitorButton(Composite parent, ICameraConfiguration cameraConfiguration) throws GDAClientException {
		this.cameraConfiguration = cameraConfiguration;
		CameraControl cameraControl = cameraConfiguration.getCameraControl()
				.orElseThrow(() -> new GDAClientException("Cannot create a button without a CameraControl"));
		ButtonLayout layout = getButtonLayout(cameraControl);
		button = createClientButton(parent, SWT.NONE, ClientMessages.EMPTY_MESSAGE, layout.getMessage(),
				layout.getImage());
		createClientGridDataFactory().align(SWT.CENTER, SWT.CENTER).grab(false, false).applyTo(button);
		button.setEnabled(true);
		button.setData(CAMERA_CONTROL, cameraControl);
		updateButtonLayoutAndListener(button);
		try {
			SpringApplicationContextProxy.addDisposableApplicationListener(button, cameraControlSpringEventListener);
		} catch (GDAClientException e) {
			logger.error("Cannot add CameraControlSpringEventListener to CameraMonitorButton for {}",
					cameraControl.getName());
		}
	}

	/**
	 * This method takes all the necessary information from the, already initialized, button.
	 * It is used exclusively to synch the layout and listener for a brand new created "monitoring" button.
	 * @param button
	 */
	static void updateButtonLayoutAndListener(Button button) {
		CameraControl cameraControl = getButtonCameraControl(button);
		CameraState cameraState = getCameraState(cameraControl);
		CameraMonitorButtonHelper.updateButtonLayoutAndListener(button, cameraControl.getName(), cameraState);
	}
	
	// At the moment is not possible to use anonymous lambda expression because it
	// generates a class cast exception
	private ApplicationListener<CameraControlSpringEvent> cameraControlSpringEventListener = new ApplicationListener<CameraControlSpringEvent>() {
		@Override
		public void onApplicationEvent(CameraControlSpringEvent event) {
			Display.getDefault().asyncExec(() -> {
				if (!CameraMonitorButtonHelper.getButtonCameraControl(button).getName().equals(event.getName()))
					return;
				updateButtonLayoutAndListener(button, event);
			});
		}

		private void updateButtonLayoutAndListener(Button button, CameraControlSpringEvent event) {
			logger.debug("CameraControlSpringEvent {}", event);
			ButtonLayout buttonLayout = getButtonCameraState(button);
			logger.debug("buttonLayout {}", buttonLayout);
			CameraState cameraState = event.getCameraState();
			ButtonLayout eventLayout = getButtonLayout(cameraState);
			logger.debug("eventLayout {}", eventLayout);
			if (buttonLayout != null && eventLayout.equals(buttonLayout)) return;
			CameraMonitorButtonHelper.updateButtonLayoutAndListener(button, cameraConfiguration.getCameraProperties().getName(), cameraState);
		}
	};
}
