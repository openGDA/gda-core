package uk.ac.diamond.daq.client.gui.camera.liveview.state;

import java.util.UUID;

import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.gda.client.live.stream.ILiveStreamConnection;
import uk.ac.gda.client.live.stream.LiveStreamConnectionManager;
import uk.ac.gda.client.live.stream.LiveStreamException;
import uk.ac.gda.client.live.stream.event.ListenToConnectionEvent;
import uk.ac.gda.client.live.stream.event.StopListenToConnectionEvent;
import uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy;

/**
 * Defines a state where a consumer listen to a stream 
 * 
 * @author Maurizio Nagni
 */
public class ListeningState implements StreamControlState {

	/**
	 * The consumer id 
	 */
	private final UUID rootUUID;
	
	/**
	 * The stream the consumer is listening to
	 */
	private final ILiveStreamConnection stream;
	
	/**
	 * @param stream The stream the consumer is listening to
	 * @param rootUUID The consumer id 
	 */
	public ListeningState(ILiveStreamConnection stream, UUID rootUUID) {
		super();
		this.stream = stream;
		this.rootUUID = rootUUID;
	}

	/* (non-Javadoc)
	 * @see uk.ac.diamond.daq.client.gui.camera.liveview.state.StreamControlState#idleState(uk.ac.diamond.daq.client.gui.camera.liveview.state.StreamController)
	 */
	@Override
	public void idleState(StreamController streamController) throws LiveStreamException {
		SpringApplicationContextProxy.publishEvent(new StopListenToConnectionEvent(stream, rootUUID));
		streamController.setState(new IdleState(rootUUID));
	}

	/* (non-Javadoc)
	 * @see uk.ac.diamond.daq.client.gui.camera.liveview.state.StreamControlState#listeningState(uk.ac.diamond.daq.client.gui.camera.liveview.state.StreamController)
	 */
	@Override
	public void listeningState(StreamController streamController) throws LiveStreamException {
		try {
			// get the new stream
			LiveStreamConnectionManager manager = LiveStreamConnectionManager.getInstance();
			UUID streamUUID = manager.getIStreamConnection(
					CameraHelper.getCameraConfiguration(streamController.getControlData().getCamera()),
					streamController.getControlData().getStreamType());
			ILiveStreamConnection newStream = manager.getIStreamConnection(streamUUID);		
			streamController.setState(new ListeningState(newStream, rootUUID));
			SpringApplicationContextProxy.publishEvent(new ListenToConnectionEvent(stream, rootUUID));
		} catch (LiveStreamException e) {
			// eventually can't so moves to Idle
			streamController.setState(new IdleState(rootUUID));
			// and inform the caller
			throw e;
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.diamond.daq.client.gui.camera.liveview.state.StreamControlState#sameState(uk.ac.diamond.daq.client.gui.camera.liveview.state.StreamController)
	 */
	@Override
	public void sameState(StreamController streamController) throws LiveStreamException {
		// stop listening the previous stream
		SpringApplicationContextProxy.publishEvent(new StopListenToConnectionEvent(stream, rootUUID));
		// start listening the new stream
		listeningState(streamController);
	}

}
