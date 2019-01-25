/*-
 * Copyright © 2018 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.gda.epics.camera;

import static uk.ac.gda.api.camera.CameraState.ACQUIRING;
import static uk.ac.gda.api.camera.CameraState.IDLE;

import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.ADBase;
import gda.factory.FindableBase;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import uk.ac.gda.api.camera.BinningFormat;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.api.camera.CameraControllerEvent;
import uk.ac.gda.api.camera.CameraRegionOfInterest;
import uk.ac.gda.api.camera.CameraState;
import uk.ac.gda.api.remoting.ServiceInterface;

@ServiceInterface(CameraControl.class)
public class EpicsCameraControl extends FindableBase implements CameraControl {

	private final ObservableComponent observableComponent = new ObservableComponent();
	private final ADBase adBase;

	public EpicsCameraControl(ADBase adBase) {
		this.adBase = adBase;
	}

	private void notifyObservers () throws DeviceException {
		if (observableComponent.isBeingObserved()) {
			CameraControllerEvent event = new CameraControllerEvent();
			event.setBinningFormat(getBinningPixels());
			event.setCameraState(getAcquireState());
			event.setAcquireTime(getAcquireTime());
			event.setRegionOfInterest(getRegionOfInterest());

			observableComponent.notifyIObservers(this, event);
		}
	}

	@Override
	public double getAcquireTime() throws DeviceException {
		try {
			return adBase.getAcquireTime();
		} catch (Exception e) {
			throw new DeviceException("Error getting camera acquire time", e);
		}
	}

	@Override
	public void setAcquireTime(double acquiretime) throws DeviceException {
		try {
			adBase.setAcquireTime(acquiretime);
			notifyObservers();
		} catch (Exception e) {
			throw new DeviceException("Error setting camera acquire time", e);
		}
	}

	@Override
	public void startAcquiring() throws DeviceException {
		try {
			adBase.startAcquiring();
		} catch (Exception e) {
			throw new DeviceException("Error starting data acquisition", e);
		}
	}

	@Override
	public void stopAcquiring() throws DeviceException {
		try {
			adBase.stopAcquiring();
		} catch (Exception e) {
			throw new DeviceException("Error stopping data acquisition", e);
		}
	}

	@Override
	public CameraState getAcquireState() throws DeviceException {
		try {
			return adBase.getAcquireState() == 1 ? ACQUIRING : IDLE;
		} catch (Exception e) {
			throw new DeviceException("Error getting camera acquire state", e);
		}
	}

	@Override
	public void addIObserver(IObserver observer) {
		observableComponent.addIObserver(observer);
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		observableComponent.addIObserver(observer);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}

	@Override
	public BinningFormat getBinningPixels() throws DeviceException {
		try {
			BinningFormat result = new BinningFormat ();
			result.setX(adBase.getBinX());
			result.setY(adBase.getBinY());
			return result;
		} catch (Exception e) {
			throw new DeviceException("Unable to get Binning info from camera", e);
		}
	}

	@Override
	public void setBinningPixels(BinningFormat binningFormat) throws DeviceException {
		try {
			adBase.setBinX(binningFormat.getX());
			adBase.setBinY(binningFormat.getY());
			notifyObservers();
		} catch (Exception e) {
			throw new DeviceException("Unable to set binning format", e);
		}
	}

	@Override
	public CameraRegionOfInterest getRegionOfInterest() throws DeviceException {
		try {
			return CameraRegionOfInterest.getInstanceFromWidthHeight(adBase.getMinX(),
					adBase.getMinY(), adBase.getSizeX(), adBase.getSizeY());
		} catch (Exception e) {
			throw new DeviceException("Unable to get Camera ROI", e);
		}
	}

	@Override
	public void setRegionOfInterest(CameraRegionOfInterest region) throws DeviceException {
		try {
			adBase.setMinX(region.getLeft());
			adBase.setSizeX(region.getWidth());
			adBase.setMinY(region.getLeft());
			adBase.setSizeY(region.getHeight());
			notifyObservers();
		} catch (Exception e) {
			throw new DeviceException("Unable to set ROI", e);
		}
	}

	@Override
	public void clearRegionOfInterest() throws DeviceException {
		try {
			adBase.setMinX(0);
			adBase.setSizeX(adBase.getMaxSizeX_RBV());
			adBase.setMinY(0);
			adBase.setSizeY(adBase.getMaxSizeY_RBV());
			notifyObservers();
		} catch (Exception e) {
			throw new DeviceException ("Unable to clear ROI", e);
		}
	}
}
