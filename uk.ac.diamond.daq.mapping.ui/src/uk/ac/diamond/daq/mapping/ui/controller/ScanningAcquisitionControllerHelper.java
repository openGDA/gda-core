package uk.ac.diamond.daq.mapping.ui.controller;

import org.springframework.context.ApplicationEvent;

import gda.device.DeviceException;
import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.diamond.daq.client.gui.camera.event.ChangeActiveCameraEvent;
import uk.ac.diamond.daq.client.gui.camera.event.ExposureChangeEvent;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.gda.api.acquisition.AcquisitionController;
import uk.ac.gda.api.acquisition.parameters.DetectorDocument;
import uk.ac.gda.api.camera.CameraControl;

/**
 * Handles {@link ScanningAcquisitionController} events
 *
 * @author Maurizio Nagni
 */
class ScanningAcquisitionControllerHelper {

	private static int activeCamera;

	private ScanningAcquisitionControllerHelper() {
	}

	public static void onApplicationEvent(ApplicationEvent event,
			AcquisitionController<ScanningAcquisition> controller) {
		if (ExposureChangeEvent.class.isInstance(event)) {
			onApplicationEvent(ExposureChangeEvent.class.cast(event), controller);
		} else if (ChangeActiveCameraEvent.class.isInstance(event)) {
			onApplicationEvent(ChangeActiveCameraEvent.class.cast(event), controller);
		}
	}

	private static void onApplicationEvent(ExposureChangeEvent event,
			AcquisitionController<ScanningAcquisition> controller) {
		setAcquisitionExposure(event.getExposureTime(), controller);
	}

	private static void onApplicationEvent(ChangeActiveCameraEvent event,
			AcquisitionController<ScanningAcquisition> controller) {
		activeCamera = event.getActiveCamera().getIndex();
	}

	/**
	 * Updates the acquisition exposure contacting the active camera
	 *
	 * @param controller
	 *            the controller that wants to be update its internal acquisition configuration
	 * @throws DeviceException
	 */
	public static void updateExposure(AcquisitionController<ScanningAcquisition> controller) throws DeviceException {
		setAcquisitionExposure(getExposure(), controller);
	}

	/**
	 * Returns the acquisition exposure contacting the active camera
	 *
	 * @throws DeviceException
	 */
	public static double getExposure() throws DeviceException {
		CameraControl cc = CameraHelper.getCameraControl(activeCamera)
				.orElseThrow(() -> new DeviceException("No exposure available"));
		return cc.getAcquireTime();
	}

	private static void setAcquisitionExposure(double exposure, AcquisitionController<ScanningAcquisition> controller) {
		ScanningParameters tp = controller.getAcquisition().getAcquisitionConfiguration().getAcquisitionParameters();
		tp.setDetector(new DetectorDocument(tp.getDetector().getName(), exposure));
	}
}
