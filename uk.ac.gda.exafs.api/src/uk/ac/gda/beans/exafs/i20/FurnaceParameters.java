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

package uk.ac.gda.beans.exafs.i20;

import java.io.Serializable;

import org.apache.commons.beanutils.BeanUtils;

public class FurnaceParameters  implements Serializable{
	private Double x = 0.;
	private Double y = 0.;
	private Double z = 0.;
	private String temperature = "";
	private Double tolerance = 0.;
	private Double time = 0.;

	public Double getX() {
		return x;
	}

	public void setX(Double x) {
		this.x = x;
	}

	public Double getY() {
		return y;
	}

	public void setY(Double y) {
		this.y = y;
	}

	public Double getZ() {
		return z;
	}

	public void setZ(Double z) {
		this.z = z;
	}

	public String getTemperature() {
		return temperature;
	}

	public void setTemperature(String temperature) {
		this.temperature = temperature;
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
		result = prime * result + ((temperature == null) ? 0 : temperature.hashCode());
		long temp;
		temp = Double.doubleToLongBits(time);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(tolerance);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((x == null) ? 0 : x.hashCode());
		result = prime * result + ((y == null) ? 0 : y.hashCode());
		result = prime * result + ((z == null) ? 0 : z.hashCode());
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
		FurnaceParameters other = (FurnaceParameters) obj;
		if (temperature == null) {
			if (other.temperature != null)
				return false;
		} else if (!temperature.equals(other.temperature))
			return false;
		if (Double.doubleToLongBits(time) != Double.doubleToLongBits(other.time))
			return false;
		if (Double.doubleToLongBits(tolerance) != Double.doubleToLongBits(other.tolerance))
			return false;
		if (x == null) {
			if (other.x != null)
				return false;
		} else if (!x.equals(other.x))
			return false;
		if (y == null) {
			if (other.y != null)
				return false;
		} else if (!y.equals(other.y))
			return false;
		if (z == null) {
			if (other.z != null)
				return false;
		} else if (!z.equals(other.z))
			return false;
		return true;
	}

}