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
 *
 */
public class TransmissionParameters  implements Serializable{
	
	private List<IonChamberParameters> ionChamberParameters;
	private Double                     workingEnergy;
	private String                     detectorType;
	private Double mythenEnergy;
	private Double mythenTime;
	private boolean collectDiffractionImages; 
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
	 * 
	 */
	public TransmissionParameters() {
		ionChamberParameters = new ArrayList<IonChamberParameters>();
	}
	/**
	 * @return the customParameters
	 */
	public List<IonChamberParameters> getIonChamberParameters() {
		return ionChamberParameters;
	}

	/**
	 * @param ionChamberParameters the ionChamberParameters to set
	 */
	public void addIonChamberParameter(IonChamberParameters ionChamberParameters) {
		this.ionChamberParameters.add(ionChamberParameters);
	}
	/**
	 * @param ionChamberParameters the ionChamberParameters to set
	 */
	public void setIonChamberParameters(List<IonChamberParameters> ionChamberParameters) {
		this.ionChamberParameters = ionChamberParameters;
	}

	/**
	 * 
	 */
	public void clear() {
		ionChamberParameters.clear();
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((detectorType == null) ? 0 : detectorType.hashCode());
		result = prime * result + ((ionChamberParameters == null) ? 0 : ionChamberParameters.hashCode());
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
		TransmissionParameters other = (TransmissionParameters) obj;
		if (detectorType == null) {
			if (other.detectorType != null)
				return false;
		} else if (!detectorType.equals(other.detectorType))
			return false;
		if (ionChamberParameters == null) {
			if (other.ionChamberParameters != null)
				return false;
		} else if (!ionChamberParameters.equals(other.ionChamberParameters))
			return false;
		if (workingEnergy == null) {
			if (other.workingEnergy != null)
				return false;
		} else if (!workingEnergy.equals(other.workingEnergy))
			return false;
		return true;
	}

	/**
	 * @return Returns the detectorType.
	 */
	public String getDetectorType() {
		return detectorType;
	}

	/**
	 * @param detectorType The detectorType to set.
	 */
	public void setDetectorType(String detectorType) {
		this.detectorType = detectorType;
	}
	

	public Double getMythenEnergy() {
		return mythenEnergy;
	}

	public void setMythenEnergy(Double mythenEnergy) {
		this.mythenEnergy = mythenEnergy;
	}

	public Double getMythenTime() {
		return mythenTime;
	}

	public void setMythenTime(Double mythenTime) {
		this.mythenTime = mythenTime;
	}
	
	public boolean isCollectDiffractionImages() {
		return collectDiffractionImages;
	}

	public void setCollectDiffractionImages(boolean collectDiffractionImages) {
		this.collectDiffractionImages = collectDiffractionImages;
	}

}
