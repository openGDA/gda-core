/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.rcp.plotting.sideplot;

import org.apache.commons.beanutils.BeanUtils;

/**
 * This class is an auto-generated bean to use with
 * diamond widgets (saving and remembering state).
 */
public class FitData {
	
	private int peakSelection,numberOfPeaks,smoothing,algType;
	private double accuracy;

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
		long temp;
		temp = Double.doubleToLongBits(accuracy);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + algType;
		result = prime * result + numberOfPeaks;
		result = prime * result + peakSelection;
		result = prime * result + smoothing;
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
		FitData other = (FitData) obj;
		if (Double.doubleToLongBits(accuracy) != Double.doubleToLongBits(other.accuracy))
			return false;
		if (algType != other.algType)
			return false;
		if (numberOfPeaks != other.numberOfPeaks)
			return false;
		if (peakSelection != other.peakSelection)
			return false;
		if (smoothing != other.smoothing)
			return false;
		return true;
	}
	public int getPeakSelection() {
		return peakSelection;
	}
	public void setPeakSelection(int peakSelection) {
		this.peakSelection = peakSelection;
	}
	public int getNumberOfPeaks() {
		return numberOfPeaks;
	}
	public void setNumberOfPeaks(int numberOfPeaks) {
		this.numberOfPeaks = numberOfPeaks;
	}
	public int getSmoothing() {
		return smoothing;
	}
	public void setSmoothing(int smoothing) {
		this.smoothing = smoothing;
	}
	public int getAlgType() {
		return algType;
	}
	public void setAlgType(int algType) {
		this.algType = algType;
	}
	public double getAccuracy() {
		return accuracy;
	}
	public void setAccuracy(double accuracy) {
		this.accuracy = accuracy;
	}

}
