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

package uk.ac.gda.beans.exafs;

import java.net.URL;

public class MythenParameters implements IExperimentDetectorParameters {
	public static final URL mappingURL = MythenParameters.class.getResource("ExafsParameterMapping.xml");
	public static final URL schemaUrl = MythenParameters.class.getResource("ExafsParameterMapping.xsd");

	private double mythenEnergy;
	private double mythenTime;
	private int mythenFrames;

	public MythenParameters() {
	}

	@Override
	public double getMythenEnergy() {
		return mythenEnergy;
	}
	public void setMythenEnergy(double energy) {
		this.mythenEnergy = energy;
	}
	@Override
	public double getMythenTime() {
		return mythenTime;
	}
	public void setMythenTime(double timePerFrame) {
		this.mythenTime = timePerFrame;
	}
	@Override
	public int getMythenFrames() {
		return mythenFrames;
	}
	public void setMythenFrames(int numFrames) {
		this.mythenFrames = numFrames;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(mythenEnergy);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + mythenFrames;
		temp = Double.doubleToLongBits(mythenTime);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public Double getWorkingEnergy() {
		return null;
	}

	@Override
	public String getDetectorType() {
		return "";
	}

	@Override
	public boolean isCollectDiffractionImages() {
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MythenParameters other = (MythenParameters) obj;
		if (Double.doubleToLongBits(mythenEnergy) != Double.doubleToLongBits(other.mythenEnergy))
			return false;
		if (mythenFrames != other.mythenFrames)
			return false;
		if (Double.doubleToLongBits(mythenTime) != Double.doubleToLongBits(other.mythenTime))
			return false;
		return true;
	}


}
