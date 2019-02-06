package uk.ac.diamond.daq.client.gui.camera.controller;

public interface DiffractionCameraConfigurationListener extends CameraConfigurationListener {
	
	void setCameraPosition (boolean moving, String from, String to);

}
