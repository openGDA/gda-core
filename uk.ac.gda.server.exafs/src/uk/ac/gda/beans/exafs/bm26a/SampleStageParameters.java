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

package uk.ac.gda.beans.exafs.bm26a;

import java.io.Serializable;

import org.apache.commons.beanutils.BeanUtils;

import uk.ac.gda.doe.DOEField;

/**
 * class to hold sample stage parameters
 */
public class SampleStageParameters  implements Serializable{
	
	@DOEField(DOEField.DEFAULT_LEVEL)
	private String x;
	@DOEField(DOEField.DEFAULT_LEVEL)
	private String y;
	@DOEField(DOEField.DEFAULT_LEVEL)
	private String z;
	@DOEField(DOEField.DEFAULT_LEVEL)
	private String rotation;
	@DOEField(DOEField.DEFAULT_LEVEL)
	private String roll;
	@DOEField(DOEField.DEFAULT_LEVEL)
	private String yaw;

	/**
	 * @return the x
	 */
	public String getX() {
		return x;
	}

	/**
	 * @param x
	 *            the x to set
	 */
	public void setX(String x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public String getY() {
		return y;
	}

	/**
	 * @param y
	 *            the y to set
	 */
	public void setY(String y) {
		this.y = y;
	}

	/**
	 * @return the z
	 */
	public String getZ() {
		return z;
	}

	/**
	 * @param z
	 *            the z to set
	 */
	public void setZ(String z) {
		this.z = z;
	}

	/**
	 * @return the rotation
	 */
	public String getRotation() {
		return rotation;
	}

	/**
	 * @param rotation
	 *            the rotation to set
	 */
	public void setRotation(String rotation) {
		this.rotation = rotation;
	}

	/**
	 * @return the roll
	 */
	public String getRoll() {
		return roll;
	}

	/**
	 * @param roll
	 *            the roll to set
	 */
	public void setRoll(String roll) {
		this.roll = roll;
	}

	/**
	 * @return the yaw
	 */
	public String getYaw() {
		return yaw;
	}

	/**
	 * @param yaw
	 *            
	 */
	public void setYaw(String yaw) {
		this.yaw = yaw;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((roll == null) ? 0 : roll.hashCode());
		result = prime * result + ((rotation == null) ? 0 : rotation.hashCode());
		result = prime * result + ((x == null) ? 0 : x.hashCode());
		result = prime * result + ((y == null) ? 0 : y.hashCode());
		result = prime * result + ((yaw == null) ? 0 : yaw.hashCode());
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
		SampleStageParameters other = (SampleStageParameters) obj;
		if (roll == null) {
			if (other.roll != null)
				return false;
		} else if (!roll.equals(other.roll))
			return false;
		if (rotation == null) {
			if (other.rotation != null)
				return false;
		} else if (!rotation.equals(other.rotation))
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
		if (yaw == null) {
			if (other.yaw != null)
				return false;
		} else if (!yaw.equals(other.yaw))
			return false;
		if (z == null) {
			if (other.z != null)
				return false;
		} else if (!z.equals(other.z))
			return false;
		return true;
	}
	@Override
	public String toString() {
		try {
			return BeanUtils.describe(this).toString();
		} catch (Exception e) {
			return e.getMessage();
		}
	}
}
