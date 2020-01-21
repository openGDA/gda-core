package uk.ac.diamond.daq.client.gui.camera.liveview.state;

import java.util.UUID;

import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.gda.client.live.stream.ILiveStreamConnection;
import uk.ac.gda.client.live.stream.LiveStreamConnectionManager;
import uk.ac.gda.client.live.stream.LiveStreamException;
import uk.ac.gda.client.live.stream.api.ILiveStreamConnectionManager;
import uk.ac.gda.client.live.stream.event.ListenToConnectionEvent;
import uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy;

/**
 * Defines a state where a consumer do not listen to a stream 
 * 
 * @author Maurizio Nagni
 */
public class IdleState implements StreamControlState {

	/**
	 * The consumer id 
	 */
	private final UUID rootUUID;
	
	/**
	 * Creates an Idle state
	 * @param rootUUID the consumer creating this state
	 */
	public IdleState(final UUID rootUUID) {
		super();
		this.rootUUID = rootUUID;
	}

	/* (non-Javadoc)
	 * @see uk.ac.diamond.daq.client.gui.camera.liveview.state.StreamControlState#idleState(uk.ac.diamond.daq.client.gui.camera.liveview.state.StreamController)
	 */
	@Override
	public void idleState(StreamController streamController) throws LiveStreamException {
		// Do nothing - already idle
	}

	/* (non-Javadoc)
	 * @see uk.ac.diamond.daq.client.gui.camera.liveview.state.StreamControlState#listeningState(uk.ac.diamond.daq.client.gui.camera.liveview.state.StreamController)
	 */
	@Override
	public void listeningState(StreamController streamController) throws LiveStreamException {
		ILiveStreamConnectionManager manager = LiveStreamConnectionManager.getInstance();
		UUID streamUUID = manager.getIStreamConnection(
				CameraHelper.getCameraConfiguration(streamController.getControlData().getCamera()),
				streamController.getControlData().getStreamType());
		ILiveStreamConnection stream = manager.getIStreamConnection(streamUUID);
		streamController.setState(new ListeningState(stream, rootUUID));
		SpringApplicationContextProxy.publishEvent(new ListenToConnectionEvent(stream, rootUUID));
	}

	/* (non-Javadoc)
	 * @see uk.ac.diamond.daq.client.gui.camera.liveview.state.StreamControlState#sameState(uk.ac.diamond.daq.client.gui.camera.liveview.state.StreamController)
	 */
	@Override
	public void sameState(StreamController streamControlData) throws LiveStreamException {
		// Do nothing - remain idle
	}
}
