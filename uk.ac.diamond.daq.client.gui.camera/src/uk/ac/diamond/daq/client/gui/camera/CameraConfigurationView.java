package uk.ac.diamond.daq.client.gui.camera;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import gda.device.DeviceException;
import uk.ac.diamond.daq.client.gui.camera.controller.ImagingCameraConfigurationController;
import uk.ac.gda.client.live.stream.view.StreamType;
import uk.ac.gda.ui.tool.ClientSWTElements;

/**
 * Displays the CameraConfiguration as view
 * 
 * @author Maurizio Nagni
 *
 */
public class CameraConfigurationView extends ViewPart {

	public static final String CAMERA_NAME = "imaging.camera.name";
	public static final String CAMERA_CONTROL_NAME = "imaging_camera_control";

	@Override
	public void createPartControl(Composite parent) {
		try {
			ImagingCameraConfigurationController controller = new ImagingCameraConfigurationController(
					CAMERA_CONTROL_NAME);
			CameraConfigurationDialog ccd = new CameraConfigurationDialog(
					ClientSWTElements.createComposite(parent, SWT.NONE, 1), controller,
					CameraHelper.getLiveStreamConnection(CAMERA_NAME, StreamType.EPICS_ARRAY));
			ccd.createComposite(false);
		} catch (DeviceException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void setFocus() {

	}

}
