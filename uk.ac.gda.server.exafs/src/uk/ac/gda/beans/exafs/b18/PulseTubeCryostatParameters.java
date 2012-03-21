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

package uk.ac.gda.beans.exafs.b18;

import java.io.Serializable;

public class PulseTubeCryostatParameters implements Serializable {
	double temperature1;
	double temperature2;
	double pressure;
	double setPoint;
	double tolerance;
	double time;
	boolean controlFlag;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result;
		long temp1, temp2, pres, set, tol, t;
		temp1 = Double.doubleToLongBits(temperature1);
		result = prime * result + (int) (temp1 ^ (temp1 >>> 32));
		temp2 = Double.doubleToLongBits(temperature2);
		result = prime * result + (int) (temp2 ^ (temp2 >>> 32));
		pres = Double.doubleToLongBits(pressure);
		result = prime * result + (int) (pres ^ (pres >>> 32));
		set = Double.doubleToLongBits(setPoint);
		result = prime * result + (int) (set ^ (set >>> 32));
		tol = Double.doubleToLongBits(tolerance);
		result = prime * result + (int) (tol ^ (tol >>> 32));
		t = Double.doubleToLongBits(time);
		result = prime * result + (int) (t ^ (t >>> 32));
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
		PulseTubeCryostatParameters other = (PulseTubeCryostatParameters) obj;
		if (Double.doubleToLongBits(temperature1) != Double.doubleToLongBits(other.temperature1))
			return false;
		if (Double.doubleToLongBits(time) != Double.doubleToLongBits(other.time))
			return false;
		if (Double.doubleToLongBits(tolerance) != Double.doubleToLongBits(other.tolerance))
			return false;
		return true;
	}

	public double getTemperature1() {
		return temperature1;
	}

	public void setTemperature1(double temperature) {
		this.temperature1 = temperature;
	}
	
	public double getTemperature2() {
		return temperature2;
	}

	public void setTemperature2(double temperature2) {
		this.temperature2 = temperature2;
	}

	public double getPressure() {
		return pressure;
	}

	public void setPressure(double pressure) {
		this.pressure = pressure;
	}

	public double getSetPoint() {
		return setPoint;
	}

	public void setSetPoint(double setPoint) {
		this.setPoint = setPoint;
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

	public void clear() {
		temperature1 = temperature2 = time = tolerance = 0;
	}
	
	public boolean isControlFlag() {
		return controlFlag;
	}

	public void setControlFlag(boolean control) {
		this.controlFlag = control;
	}
}
