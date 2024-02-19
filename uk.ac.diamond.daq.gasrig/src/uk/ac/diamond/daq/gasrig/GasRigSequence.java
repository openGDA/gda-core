/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.gasrig;

public enum GasRigSequence {

	INITIALISE(1, "Initialise"),
	EVACUATE_LINE(2, "Evacuate Line X"),
	EVACUATE_ENDSTATION(3, "Evacuate Endstation"),
	ADMIT_GAS_TO_LINE(4, "Admit Gas to Line"),
	ADMIT_LINE_TO_ENDSTATION(5, "Admit line to Endstation"),
	ADMIT_LINES_TO_ENDSTATION(6, "Admit lines to Endstation"),
	ADMIT_LINES_TO_EXHAUST(7, "Admit lines to Exhaust"),
	DUMMY(20, "Dummy Sequence");

	private final int sequenceId;
	private final String description;

	private GasRigSequence(int sequenceId, String description) {
		this.sequenceId = sequenceId;
		this.description = description;
	}

	public static GasRigSequence getByDescription(String description) {
		for (GasRigSequence sequence : GasRigSequence.values()) {
			if (sequence.description.equals(description)) {
				return sequence;
			}
		}
		return null;
	}

	public int getSequenceId() {
		return sequenceId;
	}

	public String getDescription() {
		return description;
	}
}
