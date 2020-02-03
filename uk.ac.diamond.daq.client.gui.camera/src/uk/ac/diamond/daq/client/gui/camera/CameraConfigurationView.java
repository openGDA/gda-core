package uk.ac.diamond.daq.client.gui.camera;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.client.gui.camera.controller.ImagingCameraConfigurationController;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.ui.tool.ClientSWTElements;

/**
 * Displays the CameraConfiguration as view.
 * 
 * @author Maurizio Nagni
 *
 */
public class CameraConfigurationView extends ViewPart {
	private int cameraIndex = CameraHelper.getDefaultCameraProperties().getIndex();

	private static final Logger logger = LoggerFactory.getLogger(CameraConfigurationView.class);

	@Override
	public void createPartControl(Composite parent) {
		try {
			CameraConfigurationDialog ccd = new CameraConfigurationDialog(
					ClientSWTElements.createComposite(parent, SWT.NONE, 1),
					(ImagingCameraConfigurationController) CameraHelper.getCameraControlInstance(cameraIndex)
							.orElseThrow(GDAClientException::new));
			ccd.createComposite();
		} catch (GDAClientException e) {
			logger.error("Cannot find Cameraconfiguration for camera index {}", cameraIndex);
		}
	}

	@Override
	public void setFocus() {

	}

}
