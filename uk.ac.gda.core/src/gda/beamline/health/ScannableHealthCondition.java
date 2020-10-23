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

package gda.beamline.health;

import gda.device.Scannable;
import uk.ac.diamond.daq.beamcondition.BeamCondition;

/**
 * A {@link ComponentHealthCondition} based on the position of a {@link Scannable}
 */
public abstract class ScannableHealthCondition extends ComponentHealthConditionBase {
	/**
	 * Return the health state for the wrapped scannable, based on the state of the scannable and whether it is critical
	 * for the functioning of the beamline.
	 *
	 * @return health state of the scannable
	 */
	@Override
	public BeamlineHealthState getHealthState() {
		if (getCondition().beamOn()) {
			return BeamlineHealthState.OK;
		} else if (isCritical()) {
			return BeamlineHealthState.ERROR;
		} else {
			return BeamlineHealthState.WARNING;
		}
	}

	protected abstract BeamCondition getCondition();
}
