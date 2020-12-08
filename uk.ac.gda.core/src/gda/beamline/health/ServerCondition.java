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

/**
 * Base class for checking that a server/service is running
 */
public abstract class ServerCondition extends ComponentHealthConditionBase {

	@Override
	protected String getDefaultErrorMessage() {
		return String.format("%s is not available", getDescription());
	}

	@Override
	public String getCurrentState() {
		return isRunning() ? "Running" : "Not running";
	}

	@Override
	public BeamlineHealthState getHealthState() {
		if (isRunning()) {
			return BeamlineHealthState.OK;
		} else if (isCritical()) {
			return BeamlineHealthState.ERROR;
		} else {
			return BeamlineHealthState.WARNING;
		}
	}

	protected abstract boolean isRunning();
}
