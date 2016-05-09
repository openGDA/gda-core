/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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
import java.net.URL;

import org.apache.commons.beanutils.BeanUtils;

public class XesScanParameters implements Serializable, IScanParameters {

	static public final URL mappingURL = XesScanParameters.class.getResource("ExafsParameterMapping.xml");

	static public final URL schemaURL = XesScanParameters.class.getResource("ExafsParameterMapping.xsd");

	public static final String EF_OUTER_MONO_INNER = "Ef outer, E0 inner";
	public static final String MONO_OUTER_EF_INNER = "E0 outer, Ef inner";

	public static String[] LOOPOPTIONS = new String[] { EF_OUTER_MONO_INNER, MONO_OUTER_EF_INNER };

	// The types of scan performed under XES
	public static final int SCAN_XES_FIXED_MONO = 1;
	public static final int FIXED_XES_SCAN_XAS = 2;
	public static final int FIXED_XES_SCAN_XANES = 3;
	public static final int SCAN_XES_SCAN_MONO = 4;

	private boolean shouldValidate = true;

	// Type of scan
	private int scanType;

	// SCAN_XES_FIXED_MONO
	private String element;
	private String edge;
	private Double monoEnergy;

	// SCAN_XES_FIXED_MONO and SCAN_XES_SCAN_MONO
	private Double xesInitialEnergy;
	private Double xesFinalEnergy;
	private Double xesStepSize;
	private Double xesIntegrationTime;

	// SCAN_XES_SCAN_MONO
	private Double monoInitialEnergy;
	private Double monoFinalEnergy;
	private Double monoStepSize;
	private Double xesEnergy;

	// Reference to XAS or XANEs file. Only required for FIXED_XES_SCAN_XAS and
	// FIXED_XES_SCAN_XANES
	private String scanFileName;

	// NOTE *NOT* energy probably it is the Bragg Angle of the XES
	private String scannableName;

	private boolean additionalCrystal0;
	private boolean additionalCrystal1;
	private boolean additionalCrystal2;
	private boolean additionalCrystal3;

	// order of the 2D scan
	private String loopChoice;

	// the name of the set of offsets (the 'store') which to apply to the XES spectrometer for this scan
	private String offsetsStoreName;

	@Override
	public String toString() {
		try {
			return BeanUtils.describe(this).toString();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	public int getScanType() {
		return scanType;
	}

	public void setScanType(int scanType) {
		this.scanType = scanType;
	}

	public boolean isShouldValidate() {
		return shouldValidate;
	}

	public void setShouldValidate(boolean shouldValidate) {
		this.shouldValidate = shouldValidate;
	}

	public Double getXesIntegrationTime() {
		return xesIntegrationTime;
	}

	public void setXesIntegrationTime(Double integrationTime) {
		this.xesIntegrationTime = integrationTime;
	}

	public String getScanFileName() {
		return scanFileName;
	}

	public void setScanFileName(String fileName) {
		this.scanFileName = fileName;
	}

	@Override
	public String getScannableName() {
		return scannableName;
	}

	public void setScannableName(String scannableName) {
		this.scannableName = scannableName;
	}

	public String getElement() {
		return element;
	}

	public void setElement(String element) {
		this.element = element;
	}

	public String getEdge() {
		return edge;
	}

	public void setEdge(String edge) {
		this.edge = edge;
	}

	public Double getXesInitialEnergy() {
		return xesInitialEnergy;
	}

	public void setXesInitialEnergy(Double xesInitialEnergy) {
		this.xesInitialEnergy = xesInitialEnergy;
	}

	public Double getXesFinalEnergy() {
		return xesFinalEnergy;
	}

	public void setXesFinalEnergy(Double xesFinalEnergy) {
		this.xesFinalEnergy = xesFinalEnergy;
	}

	public Double getXesStepSize() {
		return xesStepSize;
	}

	public void setXesStepSize(Double xesStepSize) {
		this.xesStepSize = xesStepSize;
	}

	public String getLoopChoice() {
		return loopChoice;
	}

	public void setLoopChoice(String loopChoice) {
		this.loopChoice = loopChoice;
	}

	public Double getMonoInitialEnergy() {
		return monoInitialEnergy;
	}

	public void setMonoInitialEnergy(Double monoInitialEnergy) {
		this.monoInitialEnergy = monoInitialEnergy;
	}

	public Double getMonoFinalEnergy() {
		return monoFinalEnergy;
	}

	public void setMonoFinalEnergy(Double monoFinalEnergy) {
		this.monoFinalEnergy = monoFinalEnergy;
	}

	public Double getMonoStepSize() {
		return monoStepSize;
	}

	public void setMonoStepSize(Double monoStepSize) {
		this.monoStepSize = monoStepSize;
	}

	public Double getXesEnergy() {
		return xesEnergy;
	}

	public void setXesEnergy(Double xesEnergy) {
		this.xesEnergy = xesEnergy;
	}

	public Double getMonoEnergy() {
		return monoEnergy;
	}

	public void setMonoEnergy(Double monoEnergy) {
		this.monoEnergy = monoEnergy;
	}

	public boolean isAdditionalCrystal0() {
		return additionalCrystal0;
	}

	public void setAdditionalCrystal0(boolean additionalCrystal0) {
		this.additionalCrystal0 = additionalCrystal0;
	}

	public boolean isAdditionalCrystal1() {
		return additionalCrystal1;
	}

	public void setAdditionalCrystal1(boolean additionalCrystal1) {
		this.additionalCrystal1 = additionalCrystal1;
	}

	public boolean isAdditionalCrystal2() {
		return additionalCrystal2;
	}

	public void setAdditionalCrystal2(boolean additionalCrystal2) {
		this.additionalCrystal2 = additionalCrystal2;
	}

	public boolean isAdditionalCrystal3() {
		return additionalCrystal3;
	}

	public void setAdditionalCrystal3(boolean additionalCrystal3) {
		this.additionalCrystal3 = additionalCrystal3;
	}

	public String getOffsetsStoreName() {
		return offsetsStoreName;
	}

	public void setOffsetsStoreName(String offsetsStoreName) {
		this.offsetsStoreName = offsetsStoreName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (additionalCrystal0 ? 1231 : 1237);
		result = prime * result + (additionalCrystal1 ? 1231 : 1237);
		result = prime * result + (additionalCrystal2 ? 1231 : 1237);
		result = prime * result + (additionalCrystal3 ? 1231 : 1237);
		result = prime * result + ((edge == null) ? 0 : edge.hashCode());
		result = prime * result + ((element == null) ? 0 : element.hashCode());
		result = prime * result + ((loopChoice == null) ? 0 : loopChoice.hashCode());
		result = prime * result + ((monoEnergy == null) ? 0 : monoEnergy.hashCode());
		result = prime * result + ((monoFinalEnergy == null) ? 0 : monoFinalEnergy.hashCode());
		result = prime * result + ((monoInitialEnergy == null) ? 0 : monoInitialEnergy.hashCode());
		result = prime * result + ((monoStepSize == null) ? 0 : monoStepSize.hashCode());
		result = prime * result + ((offsetsStoreName == null) ? 0 : offsetsStoreName.hashCode());
		result = prime * result + ((scanFileName == null) ? 0 : scanFileName.hashCode());
		result = prime * result + scanType;
		result = prime * result + ((scannableName == null) ? 0 : scannableName.hashCode());
		result = prime * result + (shouldValidate ? 1231 : 1237);
		result = prime * result + ((xesEnergy == null) ? 0 : xesEnergy.hashCode());
		result = prime * result + ((xesFinalEnergy == null) ? 0 : xesFinalEnergy.hashCode());
		result = prime * result + ((xesInitialEnergy == null) ? 0 : xesInitialEnergy.hashCode());
		result = prime * result + ((xesIntegrationTime == null) ? 0 : xesIntegrationTime.hashCode());
		result = prime * result + ((xesStepSize == null) ? 0 : xesStepSize.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		XesScanParameters other = (XesScanParameters) obj;
		if (additionalCrystal0 != other.additionalCrystal0)
			return false;
		if (additionalCrystal1 != other.additionalCrystal1)
			return false;
		if (additionalCrystal2 != other.additionalCrystal2)
			return false;
		if (additionalCrystal3 != other.additionalCrystal3)
			return false;
		if (edge == null) {
			if (other.edge != null)
				return false;
		} else if (!edge.equals(other.edge))
			return false;
		if (element == null) {
			if (other.element != null)
				return false;
		} else if (!element.equals(other.element))
			return false;
		if (loopChoice == null) {
			if (other.loopChoice != null)
				return false;
		} else if (!loopChoice.equals(other.loopChoice))
			return false;
		if (monoEnergy == null) {
			if (other.monoEnergy != null)
				return false;
		} else if (!monoEnergy.equals(other.monoEnergy))
			return false;
		if (monoFinalEnergy == null) {
			if (other.monoFinalEnergy != null)
				return false;
		} else if (!monoFinalEnergy.equals(other.monoFinalEnergy))
			return false;
		if (monoInitialEnergy == null) {
			if (other.monoInitialEnergy != null)
				return false;
		} else if (!monoInitialEnergy.equals(other.monoInitialEnergy))
			return false;
		if (monoStepSize == null) {
			if (other.monoStepSize != null)
				return false;
		} else if (!monoStepSize.equals(other.monoStepSize))
			return false;
		if (offsetsStoreName == null) {
			if (other.offsetsStoreName != null)
				return false;
		} else if (!offsetsStoreName.equals(other.offsetsStoreName))
			return false;
		if (scanFileName == null) {
			if (other.scanFileName != null)
				return false;
		} else if (!scanFileName.equals(other.scanFileName))
			return false;
		if (scanType != other.scanType)
			return false;
		if (scannableName == null) {
			if (other.scannableName != null)
				return false;
		} else if (!scannableName.equals(other.scannableName))
			return false;
		if (shouldValidate != other.shouldValidate)
			return false;
		if (xesEnergy == null) {
			if (other.xesEnergy != null)
				return false;
		} else if (!xesEnergy.equals(other.xesEnergy))
			return false;
		if (xesFinalEnergy == null) {
			if (other.xesFinalEnergy != null)
				return false;
		} else if (!xesFinalEnergy.equals(other.xesFinalEnergy))
			return false;
		if (xesInitialEnergy == null) {
			if (other.xesInitialEnergy != null)
				return false;
		} else if (!xesInitialEnergy.equals(other.xesInitialEnergy))
			return false;
		if (xesIntegrationTime == null) {
			if (other.xesIntegrationTime != null)
				return false;
		} else if (!xesIntegrationTime.equals(other.xesIntegrationTime))
			return false;
		if (xesStepSize == null) {
			if (other.xesStepSize != null)
				return false;
		} else if (!xesStepSize.equals(other.xesStepSize))
			return false;
		return true;
	}
}
