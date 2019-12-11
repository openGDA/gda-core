package uk.ac.diamond.daq.client.gui.camera.liveview.state;

import java.util.UUID;

import uk.ac.diamond.daq.client.gui.camera.event.ChangeActiveCameraEvent;
import uk.ac.diamond.daq.client.gui.camera.liveview.StreamControlData;
import uk.ac.gda.client.live.stream.LiveStreamException;
import uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy;

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
	
	private final UUID rootUUID;

	/**
	 * @param controlData The connection data 
	 * @param rootUUID The container managing the connection
	 */
	public StreamController(StreamControlData controlData, UUID rootUUID) {
		this.rootUUID = rootUUID;
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
		publishCameraChange();
	}
	
	public void idle() throws LiveStreamException {
		getState().idleState(this);
		publishCameraChange();
	}
	
	public void listen() throws LiveStreamException {
		getState().listeningState(this);
		publishCameraChange();
	}
	
	private void publishCameraChange() {
		SpringApplicationContextProxy.publishEvent(
				new ChangeActiveCameraEvent(getState(), getControlData().getCamera(), rootUUID));
	}
}
