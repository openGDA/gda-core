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
 * Indicates the state of an individual component that is relevant to the beamline's health.<br>
 * It is generated from the corresponding ComponentHealthCondition and is intended for display to the user.
 */
public class BeamlineHealthComponentResult {

	/** User-friendly name for the component */
	private String componentName;

	/** Current state of the component */
	private String currentState;

	/** The state of the component: whether or not it fulfils the configured condition and, if not, how serious this is. */
	private BeamlineHealthState componentHealthState;

	/** A message that the GUI can display to the user if the condition for this component is not met. */
	private String errorMessage;

	public BeamlineHealthComponentResult() {
		// required for JSON dserialisation
	}

	public BeamlineHealthComponentResult(String componentName, String currentState, BeamlineHealthState componentHealthState, String errorMessage) {
		this.componentName = componentName;
		this.currentState = currentState;
		this.componentHealthState = componentHealthState;
		this.errorMessage = errorMessage;
	}

	public String getComponentName() {
		return componentName;
	}

	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

	public String getCurrentState() {
		return currentState;
	}

	public void setCurrentState(String currentState) {
		this.currentState = currentState;
	}

	public BeamlineHealthState getComponentHealthState() {
		return componentHealthState;
	}

	public void setComponentHealthState(BeamlineHealthState componentHealthState) {
		this.componentHealthState = componentHealthState;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@Override
	public String toString() {
		return "BeamlineHealthComponentResult [componentName=" + componentName + ", currentState=" + currentState
				+ ", componentHealthState=" + componentHealthState + ", errorMessage=" + errorMessage + "]";
	}
}
