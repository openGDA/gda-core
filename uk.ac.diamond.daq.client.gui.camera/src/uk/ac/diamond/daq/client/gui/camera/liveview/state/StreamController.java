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

import static uk.ac.gda.core.tool.spring.SpringApplicationContextFacade.publishEvent;

import java.util.Optional;
import java.util.UUID;

import uk.ac.diamond.daq.client.gui.camera.event.ChangeActiveCameraEvent;
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
		publishEvent(new ChangeActiveCameraEvent(getState(),
				getControlData().getCameraConfigurationProperties(), Optional.ofNullable(rootUUID)));
	}
}
