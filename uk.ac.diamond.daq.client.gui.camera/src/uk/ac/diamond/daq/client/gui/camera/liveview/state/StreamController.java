package uk.ac.diamond.daq.client.gui.camera.liveview.state;

import java.util.UUID;

import uk.ac.diamond.daq.client.gui.camera.liveview.StreamControlData;
import uk.ac.gda.client.live.stream.LiveStreamException;

/**
 * Control the state of a stream connection
 * 
 * @author Maurizio Nagni
 *
 */
public class StreamController {

	/**
	 * The connection data
	 */
	private StreamControlData controlData;
	/**
	 * The connection state
	 */
	private StreamControlState state;

	/**
	 * @param controlData The connection data 
	 * @param rootUUID The container managing the connection
	 */
	public StreamController(StreamControlData controlData, UUID rootUUID) {
		this.controlData = controlData;
		this.state = new IdleState(rootUUID);
	}

	public StreamControlData getControlData() {
		return controlData;
	}

	public void setControlData(StreamControlData controlData) {
		this.controlData = controlData;
	}

	public StreamControlState getState() {
		return state;
	}

	protected void setState(StreamControlState state) {
		this.state = state;
	}

	public void update() throws LiveStreamException {
		getState().sameState(this);
	}
	
	public void idle() throws LiveStreamException {
		getState().idleState(this);
	}
	
	public void listen() throws LiveStreamException {
		getState().listeningState(this);
	}
}
