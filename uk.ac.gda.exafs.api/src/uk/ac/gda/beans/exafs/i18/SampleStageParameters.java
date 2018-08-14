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

package uk.ac.gda.beans.exafs.i18;

import java.io.Serializable;

public class SampleStageParameters implements Serializable {
	private Double x;
	private Double y;
	private Double z;
	private String xName;
	private String yName;
	private String zName;

	public void setX(Double x) {
		this.x = x;
	}
	public Double getX() {
		return x;
	}
	public void setY(Double y) {
		this.y = y;
	}
	public Double getY() {
		return y;
	}
	public void setZ(Double z) {
		this.z = z;
	}
	public Double getZ() {
		return z;
	}
	public String getXName() {
		return xName;
	}
	public void setXName(String xName) {
		this.xName = xName;
	}
	public String getYName() {
		return yName;
	}
	public void setYName(String yName) {
		this.yName = yName;
	}
	public String getZName() {
		return zName;
	}
	public void setZName(String zName) {
		this.zName = zName;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((x == null) ? 0 : x.hashCode());
		result = prime * result + ((xName == null) ? 0 : xName.hashCode());
		result = prime * result + ((y == null) ? 0 : y.hashCode());
		result = prime * result + ((yName == null) ? 0 : yName.hashCode());
		result = prime * result + ((z == null) ? 0 : z.hashCode());
		result = prime * result + ((zName == null) ? 0 : zName.hashCode());
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
		SampleStageParameters other = (SampleStageParameters) obj;
		if (x == null) {
			if (other.x != null)
				return false;
		} else if (!x.equals(other.x))
			return false;
		if (xName == null) {
			if (other.xName != null)
				return false;
		} else if (!xName.equals(other.xName))
			return false;
		if (y == null) {
			if (other.y != null)
				return false;
		} else if (!y.equals(other.y))
			return false;
		if (yName == null) {
			if (other.yName != null)
				return false;
		} else if (!yName.equals(other.yName))
			return false;
		if (z == null) {
			if (other.z != null)
				return false;
		} else if (!z.equals(other.z))
			return false;
		if (zName == null) {
			if (other.zName != null)
				return false;
		} else if (!zName.equals(other.zName))
			return false;
		return true;
	}

}
