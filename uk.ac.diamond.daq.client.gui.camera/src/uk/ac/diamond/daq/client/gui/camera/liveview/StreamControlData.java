package uk.ac.diamond.daq.client.gui.camera.liveview;

import uk.ac.diamond.daq.client.gui.camera.CameraComboItem;
import uk.ac.gda.client.live.stream.view.StreamType;

/**
 * Represents a stream connection configuration.
 * 
 * @author Mauriizio Nagni
 *
 */
public class StreamControlData {

	private final CameraComboItem camera;
	private final StreamType streamType;
	
	public StreamControlData(CameraComboItem camera, StreamType streamType) {
		this.camera = camera;
		this.streamType = streamType;
	}

	public CameraComboItem getCamera() {
		return camera;
	}

	public StreamType getStreamType() {
		return streamType;
	}

}
