/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.client.gui.camera.beam.event;

import uk.ac.diamond.daq.client.gui.camera.beam.state.BeamMappingStateContext;
import uk.ac.diamond.daq.client.gui.camera.event.CameraEvent;

/**
 * Reports about the beam camera mapping {@link uk.ac.diamond.daq.client.gui.camera.beam.state.BeamMappingStateContext.State}
 *
 * @author Maurizio Nagni
 *
 */
public class BeamMappingStateEvent extends CameraEvent {

	/**
	 *
	 */
	private static final long serialVersionUID = -1939646061274410504L;
	private final BeamMappingStateContext.State state;
	private final BeamMappingStateContext.Outcome outcome;

	/**
	 * @param source the object publishing this event
	 * @param state the beam mapping process state
	 */
	public BeamMappingStateEvent(Object source, BeamMappingStateContext.State state, BeamMappingStateContext.Outcome outcome) {
		super(source);
		this.state = state;
		this.outcome = outcome;
	}

	/**
	 * The mapping process state
	 * @return the actual state
	 */
	public BeamMappingStateContext.State getState() {
		return state;
	}

	public BeamMappingStateContext.Outcome getOutcome() {
		return outcome;
	}
}