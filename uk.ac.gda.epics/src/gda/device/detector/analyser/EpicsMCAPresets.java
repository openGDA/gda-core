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
 * EpicsMCAPresets Class
 */
public class EpicsMCAPresets implements Serializable {
	private float presetRealTime;

	private float presetLiveTime;

	private long presetCounts;

	private long presetCountlow;

	private long presetCountHigh;

	private long presetSweeps;

	/**
	 * @param prtm
	 * @param pltm
	 * @param pct
	 * @param pctl
	 * @param pcth
	 * @param pswp
	 */
	public EpicsMCAPresets(float prtm, float pltm, long pct, long pctl, long pcth, long pswp) {
		this.presetRealTime = prtm;
		this.presetLiveTime = pltm;
		this.presetCounts = pct;
		this.presetCountlow = pctl;
		this.presetCountHigh = pcth;
		this.presetSweeps = pswp;
	}

	@Override
	public String toString() {
		String s = "presetRealTime: " + presetRealTime;
		s += "presetLiveTime" + presetLiveTime;
		s += "presetCounts" + presetCounts;
		s += "presetCountlow" + presetCountlow;
		s += "presetCountHigh" + presetCountHigh;
		s += "presetSweeps" + presetSweeps;
		return s;
	}
	
	/**
	 * @return presetCountHigh
	 */
	public long getPresetCountHigh() {
		return presetCountHigh;
	}

	/**
	 * @param presetCountHigh
	 */
	public void setPresetCountHigh(long presetCountHigh) {
		this.presetCountHigh = presetCountHigh;
	}

	/**
	 * @return presetCountlow
	 */
	public long getPresetCountlow() {
		return presetCountlow;
	}

	/**
	 * @param presetCountlow
	 */
	public void setPresetCountlow(long presetCountlow) {
		this.presetCountlow = presetCountlow;
	}

	/**
	 * @return presetCounts
	 */
	public long getPresetCounts() {
		return presetCounts;
	}

	/**
	 * @param presetCounts
	 */
	public void setPresetCounts(long presetCounts) {
		this.presetCounts = presetCounts;
	}

	/**
	 * @return presetLiveTime
	 */
	public float getPresetLiveTime() {
		return presetLiveTime;
	}

	/**
	 * @param presetLiveTime
	 */
	public void setPresetLiveTime(float presetLiveTime) {
		this.presetLiveTime = presetLiveTime;
	}

	/**
	 * @return presetRealTime
	 */
	public float getPresetRealTime() {
		return presetRealTime;
	}

	/**
	 * @param presetRealTime
	 */
	public void setPresetRealTime(float presetRealTime) {
		this.presetRealTime = presetRealTime;
	}

	/**
	 * @return presetSweeps
	 */
	public long getPresetSweeps() {
		return presetSweeps;
	}

	/**
	 * @param presetSweeps
	 */
	public void setPresetSweeps(long presetSweeps) {
		this.presetSweeps = presetSweeps;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (presetCountHigh ^ (presetCountHigh >>> 32));
		result = prime * result + (int) (presetCountlow ^ (presetCountlow >>> 32));
		result = prime * result + (int) (presetCounts ^ (presetCounts >>> 32));
		result = prime * result + Float.floatToIntBits(presetLiveTime);
		result = prime * result + Float.floatToIntBits(presetRealTime);
		result = prime * result + (int) (presetSweeps ^ (presetSweeps >>> 32));
		return result;
	}


	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof EpicsMCAPresets)) {
			return false;
		}

		EpicsMCAPresets other = (EpicsMCAPresets) o;
		return (this.presetCountHigh == other.presetCountHigh &&
				this.presetCountlow == other.presetCountlow &&
				this.presetCounts == other.presetCounts &&
				this.presetLiveTime == other.getPresetLiveTime() &&
				this.presetRealTime == other.getPresetRealTime() &&
				this.presetSweeps == other.presetSweeps );
	}


}
