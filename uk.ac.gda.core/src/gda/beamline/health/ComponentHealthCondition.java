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
 * This represents a condition on a component of the beamline which contributes to its overall health.<br>
 * It could for example check whether a scannable is within an acceptable range, or whether a required service is
 * running.
 */
public interface ComponentHealthCondition {
	/**
	 * Return the current state of the component.<br>
	 * For a scannable, this would be its position: for a service, it could indicate whether it is running, paused etc.
	 */
	String getCurrentState();

	/**
	 * Return the health state for this component, based on the condition itself and whether it is critical for the
	 * functioning of the beamline.
	 */
	BeamlineHealthState getHealthState();

	/** Return a user-friendly description of the component e.g. for display in the GUI */
	String getDescription();

	void setDescription(String description);

	/** Return a message that can be displayed to the user is the corresponding condition is not met */
	String getErrorMessage();

	void setErrorMessage(String errorMessage);

	/** Indicate whether this is critical for the overall health of the beamline */
	boolean isCritical();

	void setCritical(boolean critical);

	/** Indicates whether the condition is enabled. */
	boolean isEnabled();

	void setEnabled(boolean enabled);
}