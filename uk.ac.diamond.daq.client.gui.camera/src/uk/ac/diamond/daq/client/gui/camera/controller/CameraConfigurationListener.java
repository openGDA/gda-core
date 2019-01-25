package uk.ac.diamond.daq.client.gui.camera.controller;

import uk.ac.gda.api.camera.BinningFormat;
import uk.ac.gda.api.camera.CameraRegionOfInterest;

public interface CameraConfigurationListener {
	void setCameraConfigurationMode (CameraConfigurationMode cameraConfigurationMode);
	
	void setRegionOfInterest(CameraRegionOfInterest regionOfInterest);

	void clearRegionOfInterest();
	
	void setRatio (int highRegion, int lowRegion, double ratio);
	
	void setBinningFormat (BinningFormat binningFormat);
}
