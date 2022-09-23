/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.gda.beans.exafs;

import java.io.Serializable;
import java.util.Objects;

public class SpectrometerScanParameters implements Serializable {

	private String scannableName;

	// Energy scan parameters SCAN_XES_FIXED_MONO, SCAN_XES_SCAN_MONO
	private Double initialEnergy;
	private Double finalEnergy;
	private Double stepSize;
	private Double integrationTime;

	// Energy for FIXED_XES_SCAN_XAS, FIXED_XES_SCAN_XANES
	private Double fixedEnergy;

	// XAS/XANES file (SCAN_XES_REGION_FIXED_MONO)
	private String scanFileName;
	private String offsetsStoreName;


	public SpectrometerScanParameters() {
		// No arg constuctor is needed for serialization
	}

	public String getScannableName() {
		return scannableName;
	}

	public void setScannableName(String scannableName) {
		this.scannableName = scannableName;
	}

	public Double getInitialEnergy() {
		return initialEnergy;
	}

	public void setInitialEnergy(Double initialEnergy) {
		this.initialEnergy = initialEnergy;
	}

	public Double getFinalEnergy() {
		return finalEnergy;
	}

	public void setFinalEnergy(Double finalEnergy) {
		this.finalEnergy = finalEnergy;
	}

	public Double getStepSize() {
		return stepSize;
	}

	public void setStepSize(Double stepSize) {
		this.stepSize = stepSize;
	}

	public Double getIntegrationTime() {
		return integrationTime;
	}

	public void setIntegrationTime(Double integrationTime) {
		this.integrationTime = integrationTime;
	}

	public String getScanFileName() {
		return scanFileName;
	}

	public void setScanFileName(String scanFileName) {
		this.scanFileName = scanFileName;
	}

	public String getOffsetsStoreName() {
		return offsetsStoreName;
	}

	public void setOffsetsStoreName(String offsetsStoreName) {
		this.offsetsStoreName = offsetsStoreName;
	}

	public Double getFixedEnergy() {
		return fixedEnergy;
	}

	public void setFixedEnergy(Double fixedEnergy) {
		this.fixedEnergy = fixedEnergy;
	}

	@Override
	public int hashCode() {
		return Objects.hash(finalEnergy, fixedEnergy, initialEnergy, integrationTime, offsetsStoreName, scanFileName,
				scannableName, stepSize);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SpectrometerScanParameters other = (SpectrometerScanParameters) obj;
		return Double.doubleToLongBits(finalEnergy) == Double.doubleToLongBits(other.finalEnergy)
				&& Double.doubleToLongBits(fixedEnergy) == Double.doubleToLongBits(other.fixedEnergy)
				&& Double.doubleToLongBits(initialEnergy) == Double.doubleToLongBits(other.initialEnergy)
				&& Double.doubleToLongBits(integrationTime) == Double.doubleToLongBits(other.integrationTime)
				&& Objects.equals(offsetsStoreName, other.offsetsStoreName)
				&& Objects.equals(scanFileName, other.scanFileName)
				&& Objects.equals(scannableName, other.scannableName)
				&& Double.doubleToLongBits(stepSize) == Double.doubleToLongBits(other.stepSize);
	}
}
