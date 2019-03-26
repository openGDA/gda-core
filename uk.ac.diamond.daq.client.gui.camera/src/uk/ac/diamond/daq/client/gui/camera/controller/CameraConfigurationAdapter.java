package uk.ac.diamond.daq.client.gui.camera.controller;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;

import uk.ac.gda.api.camera.BinningFormat;

public class CameraConfigurationAdapter implements CameraConfigurationListener {

	@Override
	public void setCameraConfigurationMode(CameraConfigurationMode cameraConfigurationMode) {
		//do nothing
	}

	@Override
	public void setROI(RectangularROI roi) {
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
	
	@Override
	public void refreshSnapshot() {
		//do nothing
	}
}
