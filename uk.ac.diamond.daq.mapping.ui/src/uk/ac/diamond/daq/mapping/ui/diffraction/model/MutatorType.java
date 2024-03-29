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

package uk.ac.diamond.daq.mapping.ui.diffraction.model;

import gda.mscan.element.Mutator;

public enum MutatorType {
	CONTINUOUS("continuous", Mutator.CONTINUOUS), ALTERNATING("alternating", Mutator.ALTERNATING), RANDOM("random", Mutator.RANDOM_OFFSET);

	private final String fieldName;
	private final Mutator mscanMutator;

	private MutatorType(String fieldName, Mutator mscanMutator) {
		this.fieldName = fieldName;
		this.mscanMutator = mscanMutator;
	}

	public String getFieldName() {
		return fieldName;
	}

	public Mutator getMscanMutator() {
		return mscanMutator;
	}
}
