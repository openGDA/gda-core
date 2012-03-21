/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.beans.exafs.b18;

import java.io.Serializable;

public class SXCryoStageParameters  implements Serializable {
	
	private boolean manual;
	private double height;
	private double rotation;
	private double calibHeight;
	private int sampleNumber;
	
	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public double getRot() {
		return rotation;
	}

	public void setRot(double rotation) {
		this.rotation = rotation;
	}

	public double getCalibHeight() {
		return calibHeight;
	}

	public void setCalibHeight(double calibHeight) {
		this.calibHeight = calibHeight;
	}

	public int getSampleNumber() {
		return sampleNumber;
	}

	public void setSampleNumber(int sampleNumber) {
		this.sampleNumber = sampleNumber;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(calibHeight);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(height);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (manual ? 1231 : 1237);
		result = prime * result + sampleNumber;
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
		SXCryoStageParameters other = (SXCryoStageParameters) obj;
		if (Double.doubleToLongBits(calibHeight) != Double.doubleToLongBits(other.calibHeight))
			return false;
		if (Double.doubleToLongBits(height) != Double.doubleToLongBits(other.height))
			return false;
		if (manual != other.manual)
			return false;
		if (sampleNumber != other.sampleNumber)
			return false;
		return true;
	}
	
	public void clear() {
		height = calibHeight = sampleNumber = 0;
	}

	public boolean isManual() {
		return manual;
	}

	public void setManual(boolean manual) {
		this.manual = manual;
	}
	
	
}
