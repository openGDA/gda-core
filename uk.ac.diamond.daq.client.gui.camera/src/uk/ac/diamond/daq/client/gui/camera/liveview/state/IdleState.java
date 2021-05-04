/*-
 * Copyright © 2020 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.daq.client.gui.camera.liveview.state;

import java.util.UUID;

import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.gda.client.live.stream.ILiveStreamConnection;
import uk.ac.gda.client.live.stream.LiveStreamConnectionBuilder;
import uk.ac.gda.client.live.stream.LiveStreamException;
import uk.ac.gda.client.live.stream.event.ListenToConnectionEvent;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.live.stream.view.StreamType;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;

/**
 * Defines a state where a consumer do not listen to a stream
 *
 * Publishes states to Spring context
 * <ul>
 * <li>going idle -> nothing</li>
 * <li>going listening -> {@link ListenToConnectionEvent}</li>
 * <li>going same -> nothing</li> *
 * </ul>
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
	 *
	 * @param rootUUID the consumer creating this state
	 */
	public IdleState(final UUID rootUUID) {
		super();
		this.rootUUID = rootUUID;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.ac.diamond.daq.client.gui.camera.liveview.state.StreamControlState#
	 * idleState(uk.ac.diamond.daq.client.gui.camera.liveview.state.
	 * StreamController)
	 */
	@Override
	public void idleState(StreamController streamController) throws LiveStreamException {
		// Do nothing - already idle
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.ac.diamond.daq.client.gui.camera.liveview.state.StreamControlState#
	 * listeningState(uk.ac.diamond.daq.client.gui.camera.liveview.state.
	 * StreamController)
	 */
	@Override
	public void listeningState(StreamController streamController) throws LiveStreamException {
		final CameraConfiguration cc = CameraHelper.getCameraConfiguration(streamController);
		final StreamType streamType = streamController.getControlData().getStreamType();
		final ILiveStreamConnection stream = new LiveStreamConnectionBuilder(cc, streamType).buildAndConnect();
		streamController.setState(new ListeningState(stream, rootUUID));
		SpringApplicationContextFacade.publishEvent(new ListenToConnectionEvent(stream, rootUUID));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.ac.diamond.daq.client.gui.camera.liveview.state.StreamControlState#
	 * sameState(uk.ac.diamond.daq.client.gui.camera.liveview.state.
	 * StreamController)
	 */
	@Override
	public void sameState(StreamController streamControlData) throws LiveStreamException {
		// Do nothing - remain idle
	}
}
