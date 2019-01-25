package uk.ac.diamond.daq.client.gui.camera.controller;

import uk.ac.gda.api.camera.BinningFormat;
import uk.ac.gda.api.camera.CameraRegionOfInterest;

public class CameraConfigurationAdapter implements CameraConfigurationListener {

	@Override
	public void setCameraConfigurationMode(CameraConfigurationMode cameraConfigurationMode) {
		//do nothing
	}

	@Override
	public void setRegionOfInterest(CameraRegionOfInterest regionOfInterest) {
		//do nothing
	}

	@Override
	public void clearRegionOfInterest() {
		//do nothing
	}

	@Override
	public void setRatio(int highRegion, int lowRegion, double ratio) {
		//do nothing
	}

	@Override
	public void setBinningFormat(BinningFormat binningFormat) {
		//do nothing
	}
}
