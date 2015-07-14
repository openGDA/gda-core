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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;

/**
 * Class to represent changeable parameters in a softxrays (GMSD) xas scan
 *
 */
public class SoftXRaysParameters  implements Serializable{

	private String configFileName;
	private List<DrainCurrentParameters> drainCurrentParameters;
	private String detectorType;
	private Double workingEnergy;
	private boolean collectDiffractionImages;
	private double mythenEnergy;
	private double mythenTime;

	/**
	 *
	 */
	public SoftXRaysParameters() {
		drainCurrentParameters = new ArrayList<DrainCurrentParameters>();
	}

	/**
	 * @return the workingEnergy
	 */
	public Double getWorkingEnergy() {
		return workingEnergy;
	}

	/**
	 * @param workingEnergy
	 *            the workingEnergy to set
	 */
	public void setWorkingEnergy(Double workingEnergy) {
		this.workingEnergy = workingEnergy;
	}

	/**
	 * @return type
	 */
	public String getDetectorType() {
		return detectorType;
	}
	/**
	 * @param detectorType
	 */
	public void setDetectorType(String detectorType) {
		this.detectorType = detectorType;
	}
	/**
	 * @return Returns the drainCurrentParameters.
	 */
	public List<DrainCurrentParameters> getDrainCurrentParameters() {
		return drainCurrentParameters;
	}

	/**
	 *
	 */
	public void clear() {
		drainCurrentParameters.clear();
	}

	/**
	 * @param drainCurrentParameter the drainCurrentParameter to set
	 */
	public void addDrainCurrentParameter(DrainCurrentParameters drainCurrentParameter) {
		this.drainCurrentParameters.add(drainCurrentParameter);
	}

	/**
	 * @param drainCurrentParameters The drainCurrentParameters to set.
	 */
	public void setDrainCurrentParameters(List<DrainCurrentParameters> drainCurrentParameters) {
		this.drainCurrentParameters = drainCurrentParameters;
	}

	/**
	 * @return the configFileName - not the full path
	 */
	public String getConfigFileName() {
		return configFileName;
	}

	/**
	 * @param configFileName the configFileName to set
	 */
	public void setConfigFileName(String configFileName) {
		this.configFileName = configFileName;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((configFileName == null) ? 0 : configFileName.hashCode());
		result = prime * result + ((detectorType == null) ? 0 : detectorType.hashCode());
		result = prime * result + ((drainCurrentParameters == null) ? 0 : drainCurrentParameters.hashCode());
		result = prime * result + ((workingEnergy == null) ? 0 : workingEnergy.hashCode());
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
		SoftXRaysParameters other = (SoftXRaysParameters) obj;
		if (configFileName == null) {
			if (other.configFileName != null)
				return false;
		} else if (!configFileName.equals(other.configFileName))
			return false;
		if (detectorType == null) {
			if (other.detectorType != null)
				return false;
		} else if (!detectorType.equals(other.detectorType))
			return false;
		if (drainCurrentParameters == null) {
			if (other.drainCurrentParameters != null)
				return false;
		} else if (!drainCurrentParameters.equals(other.drainCurrentParameters))
			return false;
		if (workingEnergy == null) {
			if (other.workingEnergy != null)
				return false;
		} else if (!workingEnergy.equals(other.workingEnergy))
			return false;
		return true;
	}

	/**
	 *
	 */
	@Override
	public String toString() {
		try {
			return BeanUtils.describe(this).toString();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	public boolean isCollectDiffractionImages() {
		return collectDiffractionImages;
	}

	public void setCollectDiffractionImages(boolean collectDiffractionImages) {
		this.collectDiffractionImages = collectDiffractionImages;
	}



	public double getMythenEnergy() {
		return mythenEnergy;
	}

	public void setMythenEnergy(double mythenEnergy) {
		this.mythenEnergy = mythenEnergy;
	}

	public double getMythenTime() {
		return mythenTime;
	}

	public void setMythenTime(double mythenTime) {
		this.mythenTime = mythenTime;
	}
}
