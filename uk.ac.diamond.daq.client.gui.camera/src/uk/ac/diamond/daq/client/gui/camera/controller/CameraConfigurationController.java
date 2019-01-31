package uk.ac.diamond.daq.client.gui.camera.controller;

import java.util.ArrayList;
import java.util.List;

import gda.device.DeviceException;
import gda.factory.Finder;
import gda.observable.IObserver;
import uk.ac.gda.api.camera.BinningFormat;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.api.camera.CameraControllerEvent;
import uk.ac.gda.api.camera.CameraRegionOfInterest;

public class CameraConfigurationController implements IObserver {
	private List<CameraConfigurationListener> listeners = new ArrayList<>();
	private CameraControl cameraControl;
	private CameraConfigurationMode cameraConfigurationMode;
	
	public CameraConfigurationController () {
		cameraControl = Finder.getInstance().find("imaging_camera_control");
		cameraControl.addIObserver(this);
	}
	
	public void dispose () {
		cameraControl.deleteIObserver(this);
	}
	
	public void addListener (CameraConfigurationListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener (CameraConfigurationListener listener) {
		listeners.remove(listener);
	}
	
	public CameraRegionOfInterest getROIRegion () throws DeviceException {
		return cameraControl.getRegionOfInterest();
	}
	
	public void setROIRegion (CameraRegionOfInterest region) throws DeviceException {
		cameraControl.setRegionOfInterest(region);
	}
	
	public void calculateRatio (int highRegion, int lowRegion) {
		if (lowRegion == 0) {
			lowRegion = 1;
		}
		double ratio = (double)highRegion / lowRegion;
		for (CameraConfigurationListener listener : listeners) {
			listener.setRatio(highRegion, lowRegion, ratio);
		}
	}
	
	public CameraConfigurationMode getCameraDialogMode() {
		return cameraConfigurationMode;
	}
	
	public void setMode (CameraConfigurationMode cameraConfigurationMode) {
		this.cameraConfigurationMode = cameraConfigurationMode;
		for (CameraConfigurationListener listener : listeners) {
			listener.setCameraConfigurationMode(cameraConfigurationMode);
		}
	}
	
	public BinningFormat getBinning () throws DeviceException {
		return cameraControl.getBinningPixels();
	}
	
	public void setBinning (BinningFormat binningFormat) throws DeviceException {
		cameraControl.setBinningPixels(binningFormat);
	}
	
	public double getExposure () throws DeviceException {
		return cameraControl.getAcquireTime();
	}
	
	public void setExposure (double time) throws DeviceException {
		cameraControl.setAcquireTime(time);
	}

	@Override
	public void update(Object source, Object arg) {
		if (arg instanceof CameraControllerEvent) {
			CameraControllerEvent event = (CameraControllerEvent)arg;
			event.getAcquireTime();
		}
	}
}
