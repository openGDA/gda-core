package uk.ac.gda.tomography.ui.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;

import gda.device.DeviceException;
import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.diamond.daq.client.gui.camera.event.ChangeActiveCameraEvent;
import uk.ac.diamond.daq.client.gui.camera.event.ExposureChangeEvent;

/**
 * Handles {@link TomographyParametersAcquisitionController} events
 *
 * @author Maurizio Nagni
 */
class TomographyParametersAcquisitionControllerHelper {
	private static final Logger logger = LoggerFactory.getLogger(TomographyParametersAcquisitionControllerHelper.class);

	private static int activeCamera;

	private TomographyParametersAcquisitionControllerHelper() {
	}

	public static void onApplicationEvent(ApplicationEvent event, TomographyParametersAcquisitionController controller) {
		if (ExposureChangeEvent.class.isInstance(event)) {
			onApplicationEvent(ExposureChangeEvent.class.cast(event), controller);
		} else if (ChangeActiveCameraEvent.class.isInstance(event)) {
			onApplicationEvent(ChangeActiveCameraEvent.class.cast(event), controller);
		}
	}

	private static void onApplicationEvent(ExposureChangeEvent event, TomographyParametersAcquisitionController controller) {
		setAcquisitionExposure(event.getExposureTime(), controller);
	}

	private static void onApplicationEvent(ChangeActiveCameraEvent event, TomographyParametersAcquisitionController controller) {
		activeCamera = event.getActiveCamera().getIndex();
	}

	/**
	 * Updates the acquisition exposure contacting the active camera
	 * @param controller the controller that wants to be update its internal acquisition configuration
	 * @throws DeviceException
	 */
	public static void updateExposure(TomographyParametersAcquisitionController controller) throws DeviceException {
		setAcquisitionExposure(CameraHelper.getCameraControlInstance(activeCamera).getExposure(), controller);
	}

	private static void setAcquisitionExposure(double exposure, TomographyParametersAcquisitionController controller) {
		controller.getAcquisition().getAcquisitionConfiguration().getAcquisitionParameters().getProjections().setAcquisitionExposure(exposure);
	}
}
