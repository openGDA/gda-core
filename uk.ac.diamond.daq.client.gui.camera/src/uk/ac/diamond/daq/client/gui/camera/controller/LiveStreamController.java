package uk.ac.diamond.daq.client.gui.camera.controller;

import uk.ac.gda.client.live.stream.LiveStreamConnection;
import uk.ac.gda.client.live.stream.LiveStreamConnectionManager;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.StreamType;

public class LiveStreamController {

	private final LiveStreamConnectionManager manager;
	private LiveStreamConnection liveStream;
	
	public LiveStreamController(LiveStreamConnectionManager manager) {
		this.manager = manager;
	}
	
	public void connect(final CameraConfiguration cameraConfig, final StreamType streamType) {
	}
	
}
