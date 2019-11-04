package uk.ac.diamond.daq.client.gui.camera;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import gda.device.DeviceException;
import uk.ac.diamond.daq.client.gui.camera.controller.ImagingCameraConfigurationController;
import uk.ac.diamond.daq.client.gui.camera.event.ChangeActiveCameraEvent;
import uk.ac.gda.client.live.stream.view.StreamType;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy;

/**
 * Displays the CameraConfiguration as view.
 * 
 * Uses Spring to publish {@link ChangeActiveCameraEvent} when the active camera
 * changes.
 * 
 * @author Maurizio Nagni
 *
 */
public class CameraConfigurationView extends ViewPart {

	@Override
	public void createPartControl(Composite parent) {
		// Preliminary implementation to parametrise the active camera (0 will be a
		// parameter)
		int activeCamera = 0;
		try {
			// Casting in this way is horrible but thats the problem until is possible to
			// properly refactor CameraConfigurationDialog
			ImagingCameraConfigurationController controller = (ImagingCameraConfigurationController) CameraHelper
					.getCameraControlInstance(activeCamera);
			CameraConfigurationDialog ccd = new CameraConfigurationDialog(
					ClientSWTElements.createComposite(parent, SWT.NONE, 1), controller,
					CameraHelper.getLiveStreamConnection(activeCamera, StreamType.EPICS_ARRAY));
			ccd.createComposite(false);
			SpringApplicationContextProxy.publishEvent(new ChangeActiveCameraEvent(this, activeCamera));
		} catch (DeviceException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void setFocus() {

	}

}
