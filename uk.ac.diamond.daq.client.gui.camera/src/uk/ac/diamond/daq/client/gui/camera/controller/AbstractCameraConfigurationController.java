package uk.ac.diamond.daq.client.gui.camera.controller;

import java.util.ArrayList;
import java.util.List;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.factory.Finder;
import gda.observable.IObserver;
import uk.ac.diamond.daq.client.gui.camera.event.ExposureChangeEvent;
import uk.ac.gda.api.camera.BinningFormat;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy;

/**
 * Base class for diffraction and imaging camera controllers.
 * 
 * Uses Spring to publish {@link ExposureChangeEvent} whenever the camera exposure changes
 * 
 * @author Maurizio Nagni
 * @author Elliot Hall
 *
 */
public abstract class AbstractCameraConfigurationController implements IObserver {

	/**
	 * The camera minimal exposure expressed in milliseconds
	 */
	public static final String CAMERA_MIN_EXPOSURE = "camera.min.exposure";
	/**
	 * The camera maximal exposure expressed in milliseconds
	 */
	public static final String CAMERA_MAX_EXPOSURE = "camera.max.exposure";
	private static final int MAX_EXPOSURE_DEFAULT = 1000;
	private static final int MIN_EXPOSURE_DEFAULT = 1;

	protected List<CameraConfigurationListener> listeners = new ArrayList<>();
	private CameraControl cameraControl;
	private CameraConfigurationMode cameraConfigurationMode;

	public AbstractCameraConfigurationController(String findableInstance) {
		cameraControl = Finder.find(findableInstance);
		cameraControl.addIObserver(this);
	}

	public CameraControl getCameraControl() {
		return cameraControl; 
	}
	
	public void dispose() {
		cameraControl.deleteIObserver(this);
	}

	public void addListener(CameraConfigurationListener listener) {
		listeners.add(listener);
	}

	public void removeListener(CameraConfigurationListener listener) {
		listeners.remove(listener);
	}

	public CameraConfigurationMode getCameraDialogMode() {
		return cameraConfigurationMode;
	}

	public BinningFormat getBinning() throws DeviceException {
		return cameraControl.getBinningPixels();
	}

	public void setBinning(BinningFormat binningFormat) throws DeviceException {
		cameraControl.setBinningPixels(binningFormat);
	}

	public double getMinExposure() {
		return LocalProperties.getDouble(CAMERA_MIN_EXPOSURE, MIN_EXPOSURE_DEFAULT);
	}

	public double getMaxExposure() {
		return LocalProperties.getDouble(CAMERA_MAX_EXPOSURE, MAX_EXPOSURE_DEFAULT);
	}

	public double getExposure() throws DeviceException {
		return cameraControl.getAcquireTime();
	}

	public void setExposure(double time) throws DeviceException {
		cameraControl.setAcquireTime(time);
		publishExposureChangeEvent(time);
	}

	public void refreshSnapshot() {
		for (CameraConfigurationListener listener : listeners) {
			listener.refreshSnapshot();
		}
	}

	private void publishExposureChangeEvent(double exposureTime) {
		ExposureChangeEvent event = new ExposureChangeEvent(this, exposureTime);
		SpringApplicationContextProxy.publishEvent(event);
	}
	
	@Override
	public void update(Object source, Object arg) {
		// nothing yet
	}
}
