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

package uk.ac.gda.beans.xspress;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;

import uk.ac.gda.beans.exafs.IDetectorElement;

/**
 * Class which holds the data (window, gain etc) for a single detector element in an Xspress or Xspress2 system.
 * Communication with the real detector element is left in the XspressSystem class because it makes sense to speak to
 * all detector elements at once sometimes.
 */
public class DetectorElement implements Serializable, IDetectorElement {

	private static String[] labels = { "allCounts", "resets", "inWindow", "time" };

	private String name;

	// Each detector knows its pixel number starting at 1. This pixel number is 
	// used along with the pixelMap to determine electronics channel. Use the 
	// method getElectronicsChannel(DetectorElement) in Xspress2System.
	private int number;

	// The windowStart and windowEnd are the start and end channel numbers of the
	// data which should be summed to produce fluorescence counts.
	private int windowStart;
	private int windowEnd;

	
	private boolean excluded = false;

	/**
	 * list of regions of interest (must add up to 2^n - 1) // TODO must it really?
	 */
	private List<XspressROI> regionList;

	/**
	 * default constructor for Castor
	 */
	public DetectorElement() {
		regionList = new ArrayList<XspressROI>();
	}

	public DetectorElement(String name, int number, int windowStart, int windowEnd,
			 boolean excluded,
			ArrayList<XspressROI> regionList) {
		this.name = name;
		this.number = number;
		this.windowStart = windowStart;
		this.windowEnd = windowEnd;
		this.excluded = excluded;
		this.regionList = regionList;
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

	/**
	 * @return a list of scaler names
	 */
	public String[] getLabels() {
		return labels;
	}

	/**
	 * Sets the window.
	 * 
	 * @param windowStart
	 * @param windowEnd
	 */
	public void setWindow(int windowStart, int windowEnd) {
		this.windowStart = windowStart;
		this.windowEnd = windowEnd;
	}

	/**
	 * @return windowStart
	 */
	public int getWindowStart() {
		return windowStart;
	}

	/**
	 * @return windowEnd
	 */
	public int getWindowEnd() {
		return windowEnd;
	}


	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param number
	 *            The number to set.
	 */
	public void setNumber(int number) {
		this.number = number;
	}

	/**
	 * @param windowStart
	 *            The windowStart to set.
	 */
	public void setWindowStart(int windowStart) {
		this.windowStart = windowStart;
	}

	/**
	 * @param windowEnd
	 *            The windowEnd to set.
	 */
	public void setWindowEnd(int windowEnd) {
		this.windowEnd = windowEnd;
	}

	/**
	 * NOTE number = the pixel number
	 * 
	 * A value starting at 1 used to reference pixels on the detector.
	 * 
	 * @return number
	 */
	public int getNumber() {
		return number;
	}

	@Override
	public boolean isExcluded() {
		return excluded;
	}

	/**
	 * @param excluded
	 *            The excluded to set.
	 */
	public void setExcluded(boolean excluded) {
		this.excluded = excluded;
	}

	/**
	 * @param region
	 *            the xspress region
	 */
	public void addRegion(XspressROI region) {
		regionList.add(region);
	}

	/**
	 * @return value
	 */
	public List<XspressROI> getRegionList() {
		return regionList;
	}

	/**
	 * @param regionList
	 */
	public void setRegionList(List<XspressROI> regionList) {
		this.regionList = regionList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (excluded ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + number;
		result = prime * result + ((regionList == null) ? 0 : regionList.hashCode());
		result = prime * result + windowEnd;
		result = prime * result + windowStart;
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
		DetectorElement other = (DetectorElement) obj;
		if (excluded != other.excluded)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (number != other.number)
			return false;
		
		if (regionList == null) {
			if (other.regionList != null)
				return false;
		} else if (!regionList.equals(other.regionList))
			return false;
		if (windowEnd != other.windowEnd)
			return false;
		if (windowStart != other.windowStart)
			return false;
		return true;
	}
}
