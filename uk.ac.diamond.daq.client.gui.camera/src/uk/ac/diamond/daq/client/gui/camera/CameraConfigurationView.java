package uk.ac.diamond.daq.client.gui.camera;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import uk.ac.diamond.daq.client.gui.camera.controller.ImagingCameraConfigurationController;
import uk.ac.gda.ui.tool.ClientSWTElements;

/**
 * Displays the CameraConfiguration as view.
 * 
 * @author Maurizio Nagni
 *
 */
public class CameraConfigurationView extends ViewPart {
	private int cameraIndex = 0;

	private static final Logger logger = LoggerFactory.getLogger(CameraConfigurationView.class);

	@Override
	public void createPartControl(Composite parent) {
		try {
			// Casting in this way is horrible but thats the problem until is possible to
			// properly refactor CameraConfigurationDialog
			ImagingCameraConfigurationController cameraControl = (ImagingCameraConfigurationController) CameraHelper
					.getCameraControlInstance(cameraIndex);
			CameraConfigurationDialog ccd = new CameraConfigurationDialog(
					ClientSWTElements.createComposite(parent, SWT.NONE, 1), cameraControl);
			ccd.createComposite(false);
		} catch (DeviceException e) {
			logger.error("DeviceException", e);
		}
	}

	@Override
	public void setFocus() {
		logger.debug("hello");
	}

}
