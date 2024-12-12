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
			List.of(Sequence.AIR_TO_VACUUM)),

    VACUUM_TO_AIR(State.IN_AIR,
    		List.of(Sequence.VACUUM_TO_AIR)),

    HOTEL_TO_DOME(State.IN_DOME,
    		List.of(
    				Sequence.HOTEL_TO_DOME_PREPARE,
    				Sequence.HOTEL_TO_DOME_GRIP,
    				Sequence.SAMPLE_INTO_DOME)),

    DOME_TO_HOTEL(State.IN_HOTEL,
    		List.of(
    				Sequence.REMOVE_SAMPLE,
    				Sequence.PARK_SAMPLE_IN_HOTEL));

	private State nextState;
    private List<Sequence> sequences;

	private Transition(State nextState, List<Sequence> sequences) {
		this.nextState = nextState;
		this.sequences = sequences;
	}

	public State getNextState() {
        return nextState;
    }

	public List<Sequence> getSequences() {
		return sequences;
	}
}
