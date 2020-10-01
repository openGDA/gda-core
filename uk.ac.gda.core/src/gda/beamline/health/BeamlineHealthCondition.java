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
 * This class wraps a {@link BeamCondition} for each scannable that is configured as being relevant to the beamline's
 * health.
 */
public class BeamlineHealthCondition {
	/**
	 * The scannable whose position we want to check.<br>
	 * This must be the same scannable used in {@link #condition}
	 */
	private Scannable scannable;

	/** A user-friendly description of the scannable e.g. for display in the GUI */
	private String description = "(Unknown)";

	/**
	 * The condition we want to check.<br>
	 * The concrete class will depend on the type of scannable and condition
	 */
	private BeamCondition condition;

	/** Indicates whether this is critical for the overall health of the beamline */
	private boolean critical;

	/** A message that can be displayed to the user is the corresponding condition is not met */
	private String errorMessage = "Condition not satisfied";

	/**
	 * Return the health state for the wrapped scannable, based on the state of the scannable and whether it is critical
	 * for the functioning of the beamline.
	 *
	 * @return health state of the scannable
	 */
	public BeamlineHealthState getHealthState() {
		if (condition.beamOn()) {
			return BeamlineHealthState.OK;
		} else if (critical) {
			return BeamlineHealthState.ERROR;
		} else {
			return BeamlineHealthState.WARNING;
		}
	}

	public Scannable getScannable() {
		return scannable;
	}

	public void setScannable(Scannable scannable) {
		this.scannable = scannable;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BeamCondition getCondition() {
		return condition;
	}

	public void setCondition(BeamCondition condition) {
		this.condition = condition;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public boolean isCritical() {
		return critical;
	}

	public void setCritical(boolean critical) {
		this.critical = critical;
	}

	@Override
	public String toString() {
		return "BeamlineHealthCondition [scannable=" + scannable + ", description=" + description + ", condition="
				+ condition + ", critical=" + critical + ", errorMessage=" + errorMessage + "]";
	}
}
