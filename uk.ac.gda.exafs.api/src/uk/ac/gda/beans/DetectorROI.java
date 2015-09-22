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

package uk.ac.gda.beans;

import java.io.Serializable;

import org.apache.commons.beanutils.BeanUtils;

public class DetectorROI implements Serializable {

	private static final long serialVersionUID = -213975250331620456L;

	private int roiStart;
	private int roiEnd;
	private String roiName = "<need to set a name>";

	public DetectorROI() {
	}

	/**
	 * Copy constructor
	 */
	public DetectorROI(DetectorROI roiToCopy) {
		this(roiToCopy.getRoiName(), roiToCopy.getRoiStart(), roiToCopy.getRoiEnd());
	}

	public DetectorROI(String roiName, int roiStart, int roiEnd) {
		this.roiName = roiName;
		this.roiStart = roiStart;
		this.roiEnd = roiEnd;
	}

	@Override
	public String toString() {
		try {
			return BeanUtils.describe(this).toString();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	public String getRoiName() {
		return roiName;
	}

	public void setRoiName(String roiName) {
		this.roiName = roiName;
	}

	public void setRoiStart(int roiStart) {
		this.roiStart = roiStart;
	}

	public void setRoiEnd(int roiEnd) {
		this.roiEnd = roiEnd;
	}

	public int getRoiStart() {
		return roiStart;
	}

	public int getRoiEnd() {
		return roiEnd;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + roiEnd;
		result = prime * result + ((roiName == null) ? 0 : roiName.hashCode());
		result = prime * result + roiStart;
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
		DetectorROI other = (DetectorROI) obj;
		if (roiEnd != other.roiEnd)
			return false;
		if (roiName == null) {
			if (other.roiName != null)
				return false;
		} else if (!roiName.equals(other.roiName))
			return false;
		if (roiStart != other.roiStart)
			return false;
		return true;
	}

}
