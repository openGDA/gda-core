/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.api;

import uk.ac.diamond.daq.mapping.api.XanesEdgeParameters.EdgeToEnergy;

/**
 * Model for the polarisation scan submit section of the mapping view<br>
 * These are also passed to the server to execute the scan.
 */
public class PolarisationScanParameters {

	/** Absorption edge to scan, and associated energy */
	private EdgeToEnergy edgeToEnergy;

	/** Phase motor position corresponding to the given absorption edge */
	private Double phasePosition;

	/** <code>true</code> to submit a polarisation scan, <code>false</code> for a "normal" mapping scan */
	private boolean polarisationScan;

	/** The polarisation direction to run first */
	private Polarisation runFirst = Polarisation.LEFT;

	public EdgeToEnergy getEdgeToEnergy() {
		return edgeToEnergy;
	}

	public void setEdgeToEnergy(EdgeToEnergy edgeToEnergy) {
		this.edgeToEnergy = edgeToEnergy;
	}

	public Double getPhasePosition() {
		return phasePosition;
	}

	public void setPhasePosition(Double phasePosition) {
		this.phasePosition = phasePosition;
	}

	public boolean isPolarisationScan() {
		return polarisationScan;
	}

	public void setPolarisationScan(boolean polarisationScan) {
		this.polarisationScan = polarisationScan;
	}

	public Polarisation getRunFirst() {
		return runFirst;
	}

	public void setRunFirst(Polarisation runFirst) {
		this.runFirst = runFirst;
	}

	@Override
	public String toString() {
		return "PolarisationScanParameters [edgeToEnergy=" + edgeToEnergy + ", phasePosition=" + phasePosition
				+ ", polarisationScan=" + polarisationScan + ", runFirst=" + runFirst + "]";
	}

	/**
	 * Enum for polarisation direction
	 */
	public enum Polarisation {
		LEFT("Left"), RIGHT("Right");

		private String displayName;

		private Polarisation(String displayName) {
			this.displayName = displayName;
		}

		@Override
		public String toString() {
			return displayName;
		}
	}
}
