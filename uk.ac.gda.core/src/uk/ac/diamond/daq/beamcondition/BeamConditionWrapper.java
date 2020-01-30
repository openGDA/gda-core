/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.beamcondition;

import java.util.function.BooleanSupplier;

/**
 * A {@link BeamCondition} that accepts any function that returns a boolean.
 *
 * The function should return true when the correct conditions are met.
 */
public class BeamConditionWrapper extends BeamConditionBase {

	private final BooleanSupplier check;

	public BeamConditionWrapper(String name, BooleanSupplier check) {
		this.check = check;
		setName(name);
	}

	@Override
	public boolean beamOn() {
		return check.getAsBoolean();
	}
}
