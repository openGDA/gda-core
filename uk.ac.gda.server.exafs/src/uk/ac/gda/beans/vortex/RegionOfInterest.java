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
	private int    counts; 
	private int    windowStart;
	private int    windowEnd;
	/**
	 * @return Returns the windowStart.
	 */
	public int getWindowStart() {
		return windowStart;
	}
	/**
	 * @param windowStart The windowStart to set.
	 */
	public void setWindowStart(int windowStart) {
		this.windowStart = windowStart;
	}
	/**
	 * @return Returns the windowEnd.
	 */
	public int getWindowEnd() {
		return windowEnd;
	}
	/**
	 * @param windowEnd The windowEnd to set.
	 */
	public void setWindowEnd(int windowEnd) {
		this.windowEnd = windowEnd;
	}

	/**
	 *
	 */
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
		result = prime * result + counts;
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
		if (counts != other.counts) {
			return false;
		}
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
	/**
	 * @return Returns the roiName.
	 */
	@Override
	public String getRoiName() {
		return roiName;
	}
	/**
	 * @param roiName The roiName to set.
	 */
	public void setRoiName(String roiName) {
		this.roiName = roiName;
	}
	/**
	 * @return Returns the counts.
	 */
	public int getCounts() {
		return counts;
	}
	/**
	 * @param counts The counts to set.
	 */
	public void setCounts(int counts) {
		this.counts = counts;
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
