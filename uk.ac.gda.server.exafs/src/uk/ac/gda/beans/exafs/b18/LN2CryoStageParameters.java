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

public class LN2CryoStageParameters  implements Serializable {
	boolean manual;
	boolean editCalibration;
	double height;
	double angle;
	double calibAngle;
	double calibHeight;
	int sampleNumberA;
	int sampleNumberB;
	String cylinderType;
	
	
	
	public boolean isEditCalibration() {
		return editCalibration;
	}

	public void setEditCalibration(boolean editCalibration) {
		this.editCalibration = editCalibration;
	}

	public int getSampleNumberA() {
		return sampleNumberA;
	}

	public void setSampleNumberA(int sampleNumberA) {
		this.sampleNumberA = sampleNumberA;
	}

	public int getSampleNumberB() {
		return sampleNumberB;
	}

	public void setSampleNumberB(int sampleNumberB) {
		this.sampleNumberB = sampleNumberB;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public double getAngle() {
		return angle;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}

	public double getCalibAngle() {
		return calibAngle;
	}

	public void setCalibAngle(double calibAngle) {
		this.calibAngle = calibAngle;
	}

	public double getCalibHeight() {
		return calibHeight;
	}

	public void setCalibHeight(double calibHeight) {
		this.calibHeight = calibHeight;
	}

	public String getCylinderType() {
		return cylinderType;
	}

	public void setCylinderType(String cylinderType) {
		this.cylinderType = cylinderType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(height);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(angle);
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
		LN2CryoStageParameters other = (LN2CryoStageParameters) obj;
		if (Double.doubleToLongBits(height) != Double.doubleToLongBits(other.height))
			return false;
		if (Double.doubleToLongBits(angle) != Double.doubleToLongBits(other.angle))
			return false;
		return true;
	}
	
	public void clear() {
		height = angle = calibAngle = calibHeight = 0;
		//cylinderType = "";
	}
	
	public boolean isManual() {
		return manual;
	}

	public void setManual(boolean manual) {
		this.manual = manual;
	}
}
