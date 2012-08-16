/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

public class UserStageParameters implements Serializable  {
	double axis2;
	double axis4;
	double axis5;
	double axis6;
	double axis7;
	double axis8;
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(axis2);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(axis4);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(axis5);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(axis6);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(axis7);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(axis8);
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
		UserStageParameters other = (UserStageParameters) obj;
		if (Double.doubleToLongBits(axis2) != Double.doubleToLongBits(other.axis2))
			return false;
		if (Double.doubleToLongBits(axis4) != Double.doubleToLongBits(other.axis4))
			return false;
		if (Double.doubleToLongBits(axis5) != Double.doubleToLongBits(other.axis5))
			return false;
		if (Double.doubleToLongBits(axis6) != Double.doubleToLongBits(other.axis6))
			return false;
		if (Double.doubleToLongBits(axis7) != Double.doubleToLongBits(other.axis7))
			return false;
		if (Double.doubleToLongBits(axis8) != Double.doubleToLongBits(other.axis8))
			return false;
		return true;
	}
	
	public double getAxis2() {
		return axis2;
	}
	public void setAxis2(double axis2) {
		this.axis2 = axis2;
	}
	
	public double getAxis4() {
		return axis4;
	}
	public void setAxis4(double axis4) {
		this.axis4 = axis4;
	}
	
	public double getAxis5() {
		return axis5;
	}
	public void setAxis5(double axis5) {
		this.axis5 = axis5;
	}
	
	public double getAxis6() {
		return axis6;
	}
	public void setAxis6(double axis6) {
		this.axis6 = axis6;
	}
	
	public double getAxis7() {
		return axis7;
	}
	public void setAxis7(double axis7) {
		this.axis7 = axis7;
	}
	
	public double getAxis8() {
		return axis8;
	}
	public void setAxis8(double axis8) {
		this.axis8 = axis8;
	}
	
	public void clear() {
		axis2 = axis4 = axis5 = axis6 = axis7= axis8 = 0;
	}
}
