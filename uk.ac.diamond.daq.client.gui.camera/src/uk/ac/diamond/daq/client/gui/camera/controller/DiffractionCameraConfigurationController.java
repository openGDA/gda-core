package uk.ac.diamond.daq.client.gui.camera.controller;

import java.util.ArrayList;
import java.util.List;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.EnumPositionerStatus;
import gda.factory.Finder;
import gda.observable.IObserver;

public class DiffractionCameraConfigurationController extends AbstractCameraConfigurationController {
	private class CameraPositionListener implements IObserver {

		@Override
		public void update(Object source, Object arg) {
			if (source instanceof EnumPositioner && arg instanceof EnumPositionerStatus) {
				EnumPositionerStatus status = (EnumPositionerStatus)arg;
				if (status == EnumPositionerStatus.IDLE || status == EnumPositionerStatus.MOVING) {
					if (status == EnumPositionerStatus.IDLE) {
						cameraPositionFrom = cameraPositionTo;
						moving = false;
					}
					for (DiffractionCameraConfigurationListener listener : listeners) {
						listener.setCameraPosition(moving, cameraPositionFrom, cameraPositionTo);
					}
				}
			}
		}
		
	}
	
	private EnumPositioner cameraPosition;
	private boolean moving;
	private String cameraPositionFrom;
	private String cameraPositionTo;
	private CameraPositionListener cameraPositionListener;
	private List<DiffractionCameraConfigurationListener> listeners = new ArrayList<>();
	
	public DiffractionCameraConfigurationController(String cameraName, String cameraPositionName) throws DeviceException {
		super(cameraName);

		cameraPosition = Finder.getInstance().find(cameraPositionName);
		
		moving=false;
		cameraPositionFrom = cameraPosition.getPosition().toString();
		cameraPositionTo = cameraPositionFrom;

		cameraPositionListener = new CameraPositionListener();
		cameraPosition.addIObserver(cameraPositionListener);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		
		cameraPosition.deleteIObserver(cameraPositionListener);
	}
	
	public void addListener (DiffractionCameraConfigurationListener listener) {
		super.addListener(listener);
		listeners.add(listener);
	}
	
	public void removeListener (DiffractionCameraConfigurationListener listener) {
		super.removeListener(listener);
		listeners.remove(listener);
	}
	
	public String[] getPosibleCameraPositions () throws DeviceException {
		return cameraPosition.getPositions();
	}

	public String getCameraPosition () throws DeviceException {
		return cameraPosition.getPosition().toString();
	}
	
	public boolean isMoving() {
		return moving;
	}
	
	public void setCameraPosition (String cameraPosition) throws DeviceException {
		moving = true;
		cameraPositionTo = cameraPosition;
		this.cameraPosition.asynchronousMoveTo(cameraPosition);
	}
}
