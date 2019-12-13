package uk.ac.diamond.daq.client.gui.camera.diffraction;

import uk.ac.diamond.daq.client.gui.camera.controller.CameraConfigurationListener;

public interface DiffractionCameraConfigurationListener extends CameraConfigurationListener {
	
	void setCameraPosition (boolean moving, String from, String to);

}
