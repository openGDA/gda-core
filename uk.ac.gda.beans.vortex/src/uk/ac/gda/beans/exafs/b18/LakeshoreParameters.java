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

public class LakeshoreParameters  implements Serializable{
	double temp0;
	double temp1;
	double temp2;
	double temp3;
	boolean tempSelect0;
	boolean tempSelect1;
	boolean tempSelect2;
	boolean tempSelect3;
	double setPointSet;
	double tolerance;
	double time;
	boolean controlFlag;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(temp0);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(temp1);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(temp2);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(temp3);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(setPointSet);
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
		LakeshoreParameters other = (LakeshoreParameters) obj;
		if (Double.doubleToLongBits(temp0) != Double.doubleToLongBits(other.temp0))
			return false;
		if (Double.doubleToLongBits(temp1) != Double.doubleToLongBits(other.temp1))
			return false;
		if (Double.doubleToLongBits(temp2) != Double.doubleToLongBits(other.temp2))
			return false;
		if (Double.doubleToLongBits(temp3) != Double.doubleToLongBits(other.temp3))
			return false;
		if (Double.doubleToLongBits(setPointSet) != Double.doubleToLongBits(other.setPointSet))
			return false;
		return true;
	}
	
	public double getTemp0() {
		return temp0;
	}
	public void setTemp0(double temp0) {
		this.temp0 = temp0;
	}
	public double getTemp1() {
		return temp1;
	}
	public void setTemp1(double temp1) {
		this.temp1 = temp1;
	}
	public double getTemp2() {
		return temp2;
	}
	public void setTemp2(double temp2) {
		this.temp2 = temp2;
	}
	public double getTemp3() {
		return temp3;
	}
	public void setTemp3(double temp3) {
		this.temp3 = temp3;
	}
	public double getSetPointSet() {
		return setPointSet;
	}
	public void setSetPointSet(double setPointSet) {
		this.setPointSet = setPointSet;
	}
	public double getTolerance() {
		return tolerance;
	}
	public void setTolerance(double tolerance) {
		this.tolerance = tolerance;
	}
	public double getTime() {
		return time;
	}
	public void setTime(double time) {
		this.time = time;
	}
	public boolean isControlFlag() {
		return controlFlag;
	}
	public void setControlFlag(boolean control) {
		this.controlFlag = control;
	}
	public boolean isTempSelect0() {
		return tempSelect0;
	}
	public void setTempSelect0(boolean tempSelect0) {
		this.tempSelect0 = tempSelect0;
	}
	public boolean isTempSelect1() {
		return tempSelect1;
	}
	public void setTempSelect1(boolean tempSelect1) {
		this.tempSelect1 = tempSelect1;
	}
	public boolean isTempSelect2() {
		return tempSelect2;
	}
	public void setTempSelect2(boolean tempSelect2) {
		this.tempSelect2 = tempSelect2;
	}
	public boolean isTempSelect3() {
		return tempSelect3;
	}
	public void setTempSelect3(boolean tempSelect3) {
		this.tempSelect3 = tempSelect3;
	}
	public void clear() {
		temp0 = temp1 = temp2 = temp3 = setPointSet = 0.0;
	}
}
