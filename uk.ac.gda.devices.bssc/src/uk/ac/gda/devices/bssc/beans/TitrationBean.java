/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.bssc.beans;

import uk.ac.gda.beans.IRichBean;

public class TitrationBean implements IRichBean {
	
	LocationBean location = new LocationBean();
	String sampleName = "sample", bufferName = "buffer";
	boolean yellowSample = true;
	String viscosity = "high";
	double concentration = 1;
	double timePerFrame = 1;
	int frames = 1;
	float exposureTemperature = 22;
	
	public LocationBean getLocation() {
		return location;
	}
	public void setLocation(LocationBean location) {
		this.location = location;
	}
	public String getSampleName() {
		return sampleName;
	}
	public void setSampleName(String sampleName) {
		this.sampleName = sampleName;
	}
	public String getBufferName() {
		return bufferName;
	}
	public void setBufferName(String bufferName) {
		this.bufferName = bufferName;
	}
	public boolean isYellowSample() {
		return yellowSample;
	}
	public void setYellowSample(boolean yellowsample) {
		this.yellowSample = yellowsample;
	}
	public String getViscosity() {
		return viscosity;
	}
	public void setViscosity(String viscosity) {
		this.viscosity = viscosity;
	}
	public double getConcentration() {
		return concentration;
	}
	public void setConcentration(double concentration) {
		this.concentration = concentration;
	}
	public double getTimePerFrame() {
		return timePerFrame;
	}
	public void setTimePerFrame(double timePerFrame) {
		this.timePerFrame = timePerFrame;
	}
	public int getFrames() {
		return frames;
	}
	public void setFrames(int frames) {
		this.frames = frames;
	}
	public float getExposureTemperature() {
		return exposureTemperature;
	}
	public void setExposureTemperature(float exposureTemperature) {
		this.exposureTemperature = exposureTemperature;
	}
	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}
}