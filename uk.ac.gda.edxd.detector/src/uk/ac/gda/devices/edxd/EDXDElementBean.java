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

package uk.ac.gda.devices.edxd;

import java.io.Serializable;

/**
 * A simple class to save xmap element data.
 */
public class EDXDElementBean implements Serializable {
	
	
	private double peakTime;
	
	private double dynamicRange;
	
	private double triggerThreshold;
	
	private double baseThreshold;
	
	private int baseLength;
	
	private double energyThreshold;
	
	private double binWidth;
	
	private double preampGain;
	
	private double resetDelay;
	
	private double gapTime;
	
	private double triggerPeakTime;
	
	private double triggerGapTime;
	
	private double maxWidth;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + baseLength;
		long temp;
		temp = Double.doubleToLongBits(baseThreshold);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(binWidth);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(dynamicRange);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(energyThreshold);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(gapTime);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(maxWidth);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(peakTime);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(preampGain);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(resetDelay);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(triggerGapTime);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(triggerPeakTime);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(triggerThreshold);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		EDXDElementBean other = (EDXDElementBean) obj;
		if (baseLength != other.baseLength)
			return false;
		if (Double.doubleToLongBits(baseThreshold) != Double
				.doubleToLongBits(other.baseThreshold))
			return false;
		if (Double.doubleToLongBits(binWidth) != Double
				.doubleToLongBits(other.binWidth))
			return false;
		if (Double.doubleToLongBits(dynamicRange) != Double
				.doubleToLongBits(other.dynamicRange))
			return false;
		if (Double.doubleToLongBits(energyThreshold) != Double
				.doubleToLongBits(other.energyThreshold))
			return false;
		if (Double.doubleToLongBits(gapTime) != Double
				.doubleToLongBits(other.gapTime))
			return false;
		if (Double.doubleToLongBits(maxWidth) != Double
				.doubleToLongBits(other.maxWidth))
			return false;
		if (Double.doubleToLongBits(peakTime) != Double
				.doubleToLongBits(other.peakTime))
			return false;
		if (Double.doubleToLongBits(preampGain) != Double
				.doubleToLongBits(other.preampGain))
			return false;
		if (Double.doubleToLongBits(resetDelay) != Double
				.doubleToLongBits(other.resetDelay))
			return false;
		if (Double.doubleToLongBits(triggerGapTime) != Double
				.doubleToLongBits(other.triggerGapTime))
			return false;
		if (Double.doubleToLongBits(triggerPeakTime) != Double
				.doubleToLongBits(other.triggerPeakTime))
			return false;
		if (Double.doubleToLongBits(triggerThreshold) != Double
				.doubleToLongBits(other.triggerThreshold))
			return false;
		return true;
	}

	/**
	 * @return Returns the peakTime.
	 */
	public double getPeakTime() {
		return peakTime;
	}

	/**
	 * @param peakTime The peakTime to set.
	 */
	public void setPeakTime(double peakTime) {
		this.peakTime = peakTime;
	}

	/**
	 * @return Returns the dynamicRange.
	 */
	public double getDynamicRange() {
		return dynamicRange;
	}

	/**
	 * @param dynamicRange The dynamicRange to set.
	 */
	public void setDynamicRange(double dynamicRange) {
		this.dynamicRange = dynamicRange;
	}

	/**
	 * @return Returns the triggerThreshold.
	 */
	public double getTriggerThreshold() {
		return triggerThreshold;
	}

	/**
	 * @param triggerThreshold The triggerThreshold to set.
	 */
	public void setTriggerThreshold(double triggerThreshold) {
		this.triggerThreshold = triggerThreshold;
	}

	/**
	 * @return Returns the baseThreshold.
	 */
	public double getBaseThreshold() {
		return baseThreshold;
	}

	/**
	 * @param baseThreshold The baseThreshold to set.
	 */
	public void setBaseThreshold(double baseThreshold) {
		this.baseThreshold = baseThreshold;
	}

	/**
	 * @return Returns the baseLength.
	 */
	public int getBaseLength() {
		return baseLength;
	}

	/**
	 * @param baseLength The baseLength to set.
	 */
	public void setBaseLength(int baseLength) {
		this.baseLength = baseLength;
	}

	/**
	 * @return Returns the energyThreshold.
	 */
	public double getEnergyThreshold() {
		return energyThreshold;
	}

	/**
	 * @param energyThreshold The energyThreshold to set.
	 */
	public void setEnergyThreshold(double energyThreshold) {
		this.energyThreshold = energyThreshold;
	}

	/**
	 * @return Returns the binWidth.
	 */
	public double getBinWidth() {
		return binWidth;
	}

	/**
	 * @param binWidth The binWidth to set.
	 */
	public void setBinWidth(double binWidth) {
		this.binWidth = binWidth;
	}

	/**
	 * @return Returns the preampGain.
	 */
	public double getPreampGain() {
		return preampGain;
	}

	/**
	 * @param preampGain The preampGain to set.
	 */
	public void setPreampGain(double preampGain) {
		this.preampGain = preampGain;
	}

	/**
	 * @return Returns the resetDelay.
	 */
	public double getResetDelay() {
		return resetDelay;
	}

	/**
	 * @param resetDelay The resetDelay to set.
	 */
	public void setResetDelay(double resetDelay) {
		this.resetDelay = resetDelay;
	}

	/**
	 * @return Returns the gapTime.
	 */
	public double getGapTime() {
		return gapTime;
	}

	/**
	 * @param gapTime The gapTime to set.
	 */
	public void setGapTime(double gapTime) {
		this.gapTime = gapTime;
	}

	/**
	 * @return Returns the triggerPeakTime.
	 */
	public double getTriggerPeakTime() {
		return triggerPeakTime;
	}

	/**
	 * @param triggerPeakTime The triggerPeakTime to set.
	 */
	public void setTriggerPeakTime(double triggerPeakTime) {
		this.triggerPeakTime = triggerPeakTime;
	}

	/**
	 * @return Returns the triggerGapTime.
	 */
	public double getTriggerGapTime() {
		return triggerGapTime;
	}

	/**
	 * @param triggerGapTime The triggerGapTime to set.
	 */
	public void setTriggerGapTime(double triggerGapTime) {
		this.triggerGapTime = triggerGapTime;
	}

	/**
	 * @return Returns the maxWidth.
	 */
	public double getMaxWidth() {
		return maxWidth;
	}

	/**
	 * @param maxWidth The maxWidth to set.
	 */
	public void setMaxWidth(double maxWidth) {
		this.maxWidth = maxWidth;
	}


	
}
