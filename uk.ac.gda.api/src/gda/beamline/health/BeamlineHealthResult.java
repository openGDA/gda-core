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

import java.util.List;

/**
 * This class indicates the overall health state of the beamline.<br>
 * This will typically have been determined from the state of the individual scannables that are configured.
 */
public class BeamlineHealthResult {
	/** Command to send to server status port to retrieve beamline health result. */
	public static final String COMMAND = "beamline_state";

	/** Overall state of the beamline */
	private BeamlineHealthState beamlineHealthState;

	/** Message to be displayed to the user in connection with this result. */
	private String message;

	/** Results for the individual scannables that are configured as relevant for the beamline's health */
	private List<BeamlineHealthScannableResult> scannableResults;

	public BeamlineHealthResult() {
		// required for JSON deserialisation
	}

	public BeamlineHealthResult(BeamlineHealthState beamlineHealthState, String message, List<BeamlineHealthScannableResult> scannableResults) {
		this.beamlineHealthState = beamlineHealthState;
		this.message = message;
		this.scannableResults = scannableResults;
	}

	public BeamlineHealthState getBeamlineHealthState() {
		return beamlineHealthState;
	}

	public void setBeamlineHealthState(BeamlineHealthState beamlineHealthState) {
		this.beamlineHealthState = beamlineHealthState;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<BeamlineHealthScannableResult> getScannableResults() {
		return scannableResults;
	}

	public void setScannableResults(List<BeamlineHealthScannableResult> scannableResults) {
		this.scannableResults = scannableResults;
	}

	@Override
	public String toString() {
		return "BeamlineHealthResult [beamlineHealthState=" + beamlineHealthState + ", message=" + message
				+ ", scannableResults=" + scannableResults + "]";
	}
}
