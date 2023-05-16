/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.odin;

import gda.device.DeviceException;
import gda.factory.FindableConfigurableBase;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import uk.ac.gda.api.camera.BinningFormat;
import uk.ac.gda.api.camera.CameraControl;
import uk.ac.gda.api.camera.CameraState;
import uk.ac.gda.api.camera.ImageMode;
import uk.ac.gda.api.remoting.ServiceInterface;
import uk.ac.gda.devices.odin.control.OdinDetectorExcalibur;

@ServiceInterface(CameraControl.class)
public class ExcaliburOdinCameraControl extends FindableConfigurableBase implements CameraControl {

	private static final String IMAGE_MODE = "Multiple";
	private static final String TRIGGER_MODE = "Internal";

	private final ObservableComponent observableComponent = new ObservableComponent();
	private OdinDetectorExcalibur controller;


	@Override
	public void addIObserver(IObserver observer) {
		observableComponent.addIObserver(observer);
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		observableComponent.deleteIObserver(observer);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}

	@Override
	public void startAcquiring() throws DeviceException {
		controller.prepareCamera(1, controller.getAcquireTime(), 0, IMAGE_MODE, TRIGGER_MODE);
		controller.startCollection();
	}

	@Override
	public void stopAcquiring() throws DeviceException {
		controller.stopCollection();
	}

	@Override
	public CameraState getAcquireState() throws DeviceException {
		// Mapping from Detector constants to Camera State
		var controllerState = controller.getStatus();
		switch (controllerState) {
			case 0:
				return CameraState.IDLE;
			case 1:
				return CameraState.ACQUIRING;
			default:
				return CameraState.UNAVAILABLE;
		}

	}

	@Override
	public double getAcquireTime() throws DeviceException {
		return controller.getAcquireTime();
	}

	@Override
	public void setAcquireTime(double acquiretime) throws DeviceException {
		controller.setAcquireTime(acquiretime);
	}

	@Override
	public BinningFormat getBinningPixels() throws DeviceException {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void setBinningPixels(BinningFormat binningFormat) throws DeviceException {
		throw new UnsupportedOperationException("Not implemented");

	}

	@Override
	public int[] getFrameSize() throws DeviceException {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public int[] getRoi() throws DeviceException {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void setRoi(int left, int top, int width, int height) throws DeviceException {
		throw new UnsupportedOperationException("Not implemented");

	}

	@Override
	public void clearRoi() throws DeviceException {
		throw new UnsupportedOperationException("Not implemented");

	}

	@Override
	public int getOverlayCentreX() throws DeviceException {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public int getOverlayCentreY() throws DeviceException {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void setImageMode(ImageMode imageMode) throws Exception {
		throw new UnsupportedOperationException("Not implemented");

	}

	@Override
	public ImageMode getImageMode() throws Exception {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void setTriggerMode(short triggerMode) throws Exception {
		throw new UnsupportedOperationException("Not implemented");

	}

	@Override
	public short getTriggerMode() throws Exception {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void enableProcessingFilter() throws Exception {
		throw new UnsupportedOperationException("Not implemented");

	}

	@Override
	public void disableProcessingFilter() throws Exception {
		throw new UnsupportedOperationException("Not implemented");

	}

	@Override
	public void setProcessingFilterType(int filterType) throws Exception {
		throw new UnsupportedOperationException("Not implemented");

	}

	@Override
	public void resetFilter() throws Exception {
		throw new UnsupportedOperationException("Not implemented");

	}

	@Override
	public int getImageSizeX() throws DeviceException {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public int getImageSizeY() throws DeviceException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public OdinDetectorExcalibur getController() {
		return controller;
	}

	public void setController(OdinDetectorExcalibur controller) {
		this.controller = controller;
	}

	@Override
	public void setNumImages(int numImages) throws Exception {
		controller.setNumImages(numImages);
	}

	@Override
	public int getNumImages() throws Exception {
		return controller.getNumImages();
	}

}
