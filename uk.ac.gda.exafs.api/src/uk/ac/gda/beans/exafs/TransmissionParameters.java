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

public class TransmissionParameters implements IExperimentDetectorParameters, Serializable {

	private List<IonChamberParameters> ionChamberParameters;
	private double workingEnergy = 10000;;
	private String detectorType;
	private double mythenEnergy = 1000;
	private double mythenTime = 1.0;
	private int mythenFrames = 1;
	private boolean collectDiffractionImages;

	@Override
	public Double getWorkingEnergy() {
		return workingEnergy;
	}

	public void setWorkingEnergy(Double workingEnergy) {
		this.workingEnergy = workingEnergy;
	}

	public TransmissionParameters() {
		ionChamberParameters = new ArrayList<IonChamberParameters>();
	}

	public List<IonChamberParameters> getIonChamberParameters() {
		return ionChamberParameters;
	}

	public void addIonChamberParameter(IonChamberParameters ionChamberParameters) {
		this.ionChamberParameters.add(ionChamberParameters);
	}

	public void setIonChamberParameters(List<IonChamberParameters> ionChamberParameters) {
		this.ionChamberParameters = ionChamberParameters;
	}

	@Override
	public String getDetectorType() {
		return detectorType;
	}

	public void setDetectorType(String detectorType) {
		this.detectorType = detectorType;
	}

	@Override
	public double getMythenEnergy() {
		return mythenEnergy;
	}

	public void setMythenEnergy(Double mythenEnergy) {
		this.mythenEnergy = mythenEnergy;
	}

	@Override
	public double getMythenTime() {
		return mythenTime;
	}

	public void setMythenTime(Double mythenTime) {
		this.mythenTime = mythenTime;
	}

	@Override
	public int getMythenFrames() {
		return mythenFrames;
	}

	public void setMythenFrames(int mythenFrames) {
		this.mythenFrames = mythenFrames;
	}

	@Override
	public boolean isCollectDiffractionImages() {
		return collectDiffractionImages;
	}

	public void setCollectDiffractionImages(boolean collectDiffractionImages) {
		this.collectDiffractionImages = collectDiffractionImages;
	}

	public void clear() {
		ionChamberParameters.clear();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (collectDiffractionImages ? 1231 : 1237);
		result = prime * result + ((detectorType == null) ? 0 : detectorType.hashCode());
		result = prime * result + ((ionChamberParameters == null) ? 0 : ionChamberParameters.hashCode());
		long temp;
		temp = Double.doubleToLongBits(mythenEnergy);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + mythenFrames;
		temp = Double.doubleToLongBits(mythenTime);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(workingEnergy);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		if (collectDiffractionImages != other.collectDiffractionImages)
			return false;
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
		if (Double.doubleToLongBits(mythenEnergy) != Double.doubleToLongBits(other.mythenEnergy))
			return false;
		if (mythenFrames != other.mythenFrames)
			return false;
		if (Double.doubleToLongBits(mythenTime) != Double.doubleToLongBits(other.mythenTime))
			return false;
		if (Double.doubleToLongBits(workingEnergy) != Double.doubleToLongBits(other.workingEnergy))
			return false;
		return true;
	}

}
