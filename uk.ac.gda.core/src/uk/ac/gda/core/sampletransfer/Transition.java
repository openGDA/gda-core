/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package uk.ac.gda.core.sampletransfer;

import java.util.List;

public enum Transition {
	AIR_TO_VACUUM(State.IN_HOTEL,
			List.of(SequenceID.AIR_TO_VACUUM)),

    VACUUM_TO_AIR(State.IN_AIR,
    		List.of(SequenceID.VACUUM_TO_AIR)),

    HOTEL_TO_DOME(State.IN_DOME,
    		List.of(
    		SequenceID.HOTEL_TO_DOME_PREPARE,
    		SequenceID.HOTEL_TO_DOME_GRIP,
    		SequenceID.SAMPLE_INTO_DOME)),

    DOME_TO_HOTEL(State.IN_HOTEL,
    		List.of(
    		SequenceID.REMOVE_SAMPLE,
    		SequenceID.PARK_SAMPLE_IN_HOTEL));

	private State nextState;
    private List<SequenceID> sequences;

	private Transition(State nextState, List<SequenceID> sequences) {
		this.nextState = nextState;
		this.sequences = sequences;
	}

	public State getNextState() {
        return nextState;
    }

	public List<SequenceID> getSequences() {
		return sequences;
	}
}
