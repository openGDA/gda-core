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

package uk.ac.gda.beans.vortex;

import java.io.Serializable;

import org.apache.commons.beanutils.BeanUtils;

import uk.ac.gda.beans.DetectorROI;

/**
 * RegionOfInterest bean
 */
public class RegionOfInterest implements DetectorROI, Serializable {
	
	private String roiName;
	private int    windowStart;
	private int    windowEnd;

	public int getWindowStart() {
		return windowStart;
	}

	public void setWindowStart(int windowStart) {
		this.windowStart = windowStart;
	}

	public int getWindowEnd() {
		return windowEnd;
	}

	public void setWindowEnd(int windowEnd) {
		this.windowEnd = windowEnd;
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
		result = prime * result + ((roiName == null) ? 0 : roiName.hashCode());
		result = prime * result + windowEnd;
		result = prime * result + windowStart;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RegionOfInterest other = (RegionOfInterest) obj;
		if (roiName == null) {
			if (other.roiName != null) {
				return false;
			}
		} else if (!roiName.equals(other.roiName)) {
			return false;
		}
		if (windowEnd != other.windowEnd) {
			return false;
		}
		if (windowStart != other.windowStart) {
			return false;
		}
		return true;
	}

	@Override
	public String getRoiName() {
		return roiName;
	}

	public void setRoiName(String roiName) {
		this.roiName = roiName;
	}

	@Override
	public int getRoiEnd() {
		return getWindowEnd();
	}
	@Override
	public int getRoiStart() {
		return getWindowStart();
	}

}
