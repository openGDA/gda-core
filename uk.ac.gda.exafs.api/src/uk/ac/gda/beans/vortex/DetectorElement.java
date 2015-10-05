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
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.beanutils.BeanUtils;

import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.beans.exafs.IDetectorElement;

/**
 * Class which holds the data (window, gain etc) for a single detector element in an Xspress or Xspress2 system.
 * Communication with the real detector element is left in the XspressSystem class because it makes sense to speak to
 * all detector elements at once sometimes.
 */
public class DetectorElement implements Serializable, IDetectorElement {
	private static final long serialVersionUID = -3607118505921948915L;

	private List<DetectorROI> regionList;

	private String name;

	// Each detector knows its own number (counting from 0). This has little
	// use within the software but helps to make the configuration file human
	// readable and writeable.
	private int number;

	// Gain and offset are parameters which are usually determined by
	// the detector group when the system is set up. The values supplied should
	// be hand edited into the configuration file. The gain and offset can be
	// changed subsequently using the XspressPanel or Xspress2Panel.
//	private double gain;
//
//	private double offset;

	private boolean excluded = false;

//	private double peakingTime;

	// The windowStart and windowEnd are the start and end channel numbers of the
	// data which should be summed to produce fluorescence counts.
	private int windowStart;
	private int windowEnd;


	/**
	 * default constructor for Castor
	 */
	public DetectorElement() {
		regionList = new ArrayList<DetectorROI>();
	}

	public DetectorElement(String name, int number, int windowStart, int windowEnd,
			 boolean excluded,
			ArrayList<DetectorROI> regionList) {
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
	public void clear() {
		if (regionList!=null) regionList.clear();
	}
//	/**
//	 * @return Returns the peakingTime.
//	 */
//	public double getPeakingTime() {
//		return peakingTime;
//	}
//
//	/**
//	 * @param peakingTime The peakingTime to set.
//	 */
//	public void setPeakingTime(double peakingTime) {
//		this.peakingTime = peakingTime;
//	}

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


//	/**
//	 * @return gain
//	 */
//	public double getGain() {
//		return gain;
//	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param number The number to set.
	 */
	public void setNumber(int number) {
		this.number = number;
	}

//	/**
//	 * @return offset
//	 */
//	public double getOffset() {
//		return offset;
//	}

	/**
	 * @return number
	 */
	public int getNumber() {
		return number;
	}

//	/**
//	 * Sets the gain
//	 *
//	 * @param gain
//	 *            new value
//	 */
//	public void setGain(double gain) {
//		this.gain = gain;
//	}
//
//	/**
//	 * Sets the offset
//	 *
//	 * @param offset
//	 *            new value
//	 */
//	public void setOffset(double offset) {
//		this.offset = offset;
//	}

	@Override
	public boolean isExcluded() {
		return excluded;
	}

	/**
	 * @param excluded The excluded to set.
	 */
	public void setExcluded(boolean excluded) {
		this.excluded = excluded;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (excluded ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + number;
		result = prime * result
				+ ((regionList == null) ? 0 : regionList.hashCode());
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
		return true;
	}

	/**
	 * @param r
	 */
	public void addRegion(DetectorROI r) {
		regionList.add(r);
	}
	/**
	 * @return Returns the regions.
	 */
	public List<DetectorROI> getRegionList() {
		return regionList;
	}

	/**
	 * @param regions The regions to set.
	 */
	public void setRegionList(List<DetectorROI> regions) {
		this.regionList = regions;
	}

	public DetectorElement(DetectorElement detectorElement){
		excluded = detectorElement.excluded;
//		gain = detectorElement.gain;
		name = detectorElement.name;
		number = detectorElement.number;
//		offset = detectorElement.offset;
//		peakingTime = detectorElement.peakingTime;
		Vector<DetectorROI> rois = new Vector<DetectorROI>();
		for( DetectorROI rl : detectorElement.getRegionList()){
			DetectorROI roi = new DetectorROI();
			roi.setRoiName(rl.getRoiName());
			roi.setRoiEnd(rl.getRoiEnd());
			roi.setRoiStart(rl.getRoiStart());
			rois.add(roi);
		}
		setRegionList(rois);
	}

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
}
