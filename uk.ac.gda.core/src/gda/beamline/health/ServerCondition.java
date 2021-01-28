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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for checking that a server/service is running
 */
public abstract class ServerCondition extends ComponentHealthConditionBase {
	private static final Logger logger = LoggerFactory.getLogger(ServerCondition.class);

	@Override
	protected String getDefaultErrorMessage() {
		return String.format("%s is not available", getDescription());
	}

	@Override
	public String readCurrentState() {
		try {
			return isRunning() ? "Running" : "Not running";
		} catch (Exception e) {
			logger.error("Error reading current state of {}", getName(), e);
			return "Error";
		}
	}

	@Override
	public BeamlineHealthState calculateHealthState() {
		try {
			if (isRunning()) {
				return BeamlineHealthState.OK;
			} else if (isCritical()) {
				return BeamlineHealthState.ERROR;
			} else {
				return BeamlineHealthState.WARNING;
			}
		} catch (Exception e) {
			logger.error("Error calculating health state of {}", getName(), e);
			return BeamlineHealthState.ERROR;
		}
	}

	protected abstract boolean isRunning();
}
