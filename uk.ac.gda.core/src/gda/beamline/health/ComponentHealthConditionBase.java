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

import gda.factory.FactoryException;
import gda.factory.FindableConfigurableBase;

public abstract class ComponentHealthConditionBase extends FindableConfigurableBase implements ComponentHealthCondition {

	/** A user-friendly description of the component */
	private String description = "(Unknown)";

	/** Indicates whether this is critical for the overall health of the beamline */
	private boolean critical;

	/** Indicates whether the condition is enabled. */
	boolean enabled = true;

	/** A message that can be displayed to the user is the corresponding condition is not met */
	private String errorMessage;

	@Override
	public void configure() throws FactoryException {
		if (errorMessage == null || errorMessage.isEmpty()) {
			errorMessage = getDefaultErrorMessage();
		}
		setConfigured(true);
	}

	protected String getDefaultErrorMessage() {
		return String.format("%s is in an invalid state", getDescription());
	}

	@Override
	public String getCurrentState() {
		if (enabled) {
			return readCurrentState();
		}
		return "not checked";
	}

	protected abstract String readCurrentState();

	@Override
	public BeamlineHealthState getHealthState() {
		if (enabled) {
			return calculateHealthState();
		}
		return BeamlineHealthState.NOT_CHECKED;
	}

	protected abstract BeamlineHealthState calculateHealthState();

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getErrorMessage() {
		return errorMessage;
	}

	@Override
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@Override
	public boolean isCritical() {
		return critical;
	}

	@Override
	public void setCritical(boolean critical) {
		this.critical = critical;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public String toString() {
		return "ComponentHealthConditionBase [description=" + description + ", critical=" + critical + ", enabled="
				+ enabled + ", errorMessage=" + errorMessage + "]";
	}
}
