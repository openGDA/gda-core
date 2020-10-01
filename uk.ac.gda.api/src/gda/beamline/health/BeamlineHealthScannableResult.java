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
 * Indicates the state of an individual scannable that is relevant to the beamline's health.<br>
 * It is generated from the corresponding BeamlineHealthCondition and is intended for display to the user.
 */
public class BeamlineHealthScannableResult {

	private String scannableName;

	/** Current position of the scannable */
	private String position;

	/** The state of the scannable: whether or not it fulfils the configured condition and, if not, how serious this is. */
	private BeamlineHealthState scannableHealthState;

	/** A message that the GUI can display to the user if the condition for this scannable is not met. */
	private String errorMessage;

	public BeamlineHealthScannableResult() {
		// required for JSON dserialisation
	}

	public BeamlineHealthScannableResult(String scannableName, String position, BeamlineHealthState scannableHealthState, String errorMessage) {
		this.scannableName = scannableName;
		this.position = position;
		this.scannableHealthState = scannableHealthState;
		this.errorMessage = errorMessage;
	}

	public String getScannableName() {
		return scannableName;
	}

	public void setScannableName(String scannableName) {
		this.scannableName = scannableName;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public BeamlineHealthState getScannableHealthState() {
		return scannableHealthState;
	}

	public void setBeamlineHealthState(BeamlineHealthState scannableHealthState) {
		this.scannableHealthState = scannableHealthState;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@Override
	public String toString() {
		return "BeamlineHealthScannableResult [scannableName=" + scannableName + ", position=" + position
				+ ", scannableHealthState=" + scannableHealthState + ", errorMessage=" + errorMessage + "]";
	}
}
