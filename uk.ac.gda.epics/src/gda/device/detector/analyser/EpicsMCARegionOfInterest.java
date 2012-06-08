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

package gda.device.detector.analyser;

import java.io.Serializable;

/**
 * EpicsMCARegionOfInterest Class
 */
public class EpicsMCARegionOfInterest implements Serializable {

	private static final long serialVersionUID = 1L;

	private int regionIndex;

	private double regionLow;

	private double regionHigh;

	private int regionBackground;

	private double regionPreset;

	private String regionName;

	/**
	 * Constructor.
	 */
	public EpicsMCARegionOfInterest() {

	}

	/**
	 * Constructor.
	 * 
	 * @param regionIndex
	 * @param regionLow
	 * @param regionHigh
	 * @param regionBackground
	 * @param regionPreset
	 * @param name
	 */
	public EpicsMCARegionOfInterest(int regionIndex, double regionLow, double regionHigh, int regionBackground,
			double regionPreset, String name) {
		this.regionIndex = regionIndex;
		this.regionLow = regionLow;
		this.regionHigh = regionHigh;
		this.regionBackground = regionBackground;
		this.regionPreset = regionPreset;
		this.regionName = name;

	}
	/**
	 * Constructor with default values for preset and background
	 * 
	 * @param regionIndex
	 * @param regionLow
	 * @param regionHigh
	 * @param name
	 */
	public EpicsMCARegionOfInterest(int regionIndex, double regionLow, double regionHigh,  String name) {
		this.regionIndex = regionIndex;
		this.regionLow = regionLow;
		this.regionHigh = regionHigh;
		this.regionBackground = -1;
		this.regionPreset = 0;
		this.regionName = name;

	}

	/**
	 * @return regionBackground
	 */
	public int getRegionBackground() {
		return regionBackground;
	}

	/**
	 * @param regionBackground
	 */
	public void setRegionBackground(int regionBackground) {
		this.regionBackground = regionBackground;
	}

	/**
	 * @return regionHigh
	 */
	public double getRegionHigh() {
		return regionHigh;
	}

	/**
	 * @param regionHigh
	 */
	public void setRegionHigh(double regionHigh) {
		this.regionHigh = regionHigh;
	}

	/**
	 * @return regionIndex
	 */
	public int getRegionIndex() {
		return regionIndex;
	}

	/**
	 * @param regionIndex
	 */
	public void setRegionIndex(int regionIndex) {
		this.regionIndex = regionIndex;
	}

	/**
	 * @return regionLow
	 */
	public double getRegionLow() {
		return regionLow;
	}

	/**
	 * @param regionLow
	 */
	public void setRegionLow(double regionLow) {
		this.regionLow = regionLow;
	}

	/**
	 * @return regionName
	 */
	public String getRegionName() {
		return regionName;
	}

	/**
	 * @param regionName
	 */
	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}

	/**
	 * @return regionPreset
	 */
	public double getRegionPreset() {
		return regionPreset;
	}

	/**
	 * @param regionPreset
	 */
	public void setRegionPreset(double regionPreset) {
		this.regionPreset = regionPreset;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + regionBackground;
		long temp;
		temp = Double.doubleToLongBits(regionHigh);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + regionIndex;
		temp = Double.doubleToLongBits(regionLow);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((regionName == null) ? 0 : regionName.hashCode());
		temp = Double.doubleToLongBits(regionPreset);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	/**
	 * The objects are equal if only the settings are equal - not regionNetCount or regionCount
	 * @param o
	 * @return boolean
	 */
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof EpicsMCARegionOfInterest)) {
			return false;
		}

		EpicsMCARegionOfInterest other = (EpicsMCARegionOfInterest) o;
		return (this.regionBackground == other.regionBackground &&
				this.regionHigh == other.regionHigh &&
				this.regionIndex == other.regionIndex &&
				this.regionLow == other.regionLow &&
				this.regionName.equals(other.regionName) &&
				this.regionPreset == other.regionPreset );
	}
}
