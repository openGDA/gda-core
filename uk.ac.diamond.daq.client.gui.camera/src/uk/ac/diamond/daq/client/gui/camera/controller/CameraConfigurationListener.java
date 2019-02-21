package uk.ac.diamond.daq.client.gui.camera.controller;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;

import uk.ac.gda.api.camera.BinningFormat;

public interface CameraConfigurationListener {
	void setCameraConfigurationMode (CameraConfigurationMode cameraConfigurationMode);
	
	void setROI(RectangularROI roi);

	void clearRegionOfInterest();
	
	void setRatio (int highRegion, int lowRegion, double ratio);
	
	void setBinningFormat (BinningFormat binningFormat);
}
