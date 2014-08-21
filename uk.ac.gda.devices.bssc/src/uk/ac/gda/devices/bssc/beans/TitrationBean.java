/*-
 * Copyright © 2011-2013 Diamond Light Source Ltd.
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

import java.util.HashMap;
import java.util.Map;
import uk.ac.gda.beans.IRichBean;

public class TitrationBean implements IRichBean {
	
	public enum Viscosity {
		LOW("low", "l"),
		MEDIUM("medium", "med", "m"),
		HIGH("high", "h");
		
		private static Map<String, Viscosity> acceptedStrings = new HashMap<>();
		static {
			for (Viscosity vis : Viscosity.values()) {
				for (String name : vis.accepted) {
					acceptedStrings.put(name.toUpperCase(), vis);
				}
			}
		}
		
		private final String[] accepted;
		
		private Viscosity(String... names) {
			this.accepted = names;
		}
		public static Viscosity fromString(String vis) {
			Viscosity found = acceptedStrings.get(vis.toUpperCase());
			if (found != null) {
				return found;
			}
			throw new IllegalArgumentException(vis + " is not a valid viscosity");
		}
		
		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}
	
	LocationBean location = new LocationBean(BSSCSessionBean.BSSC_PLATES);
	LocationBean bufferLocation = new LocationBean(BSSCSessionBean.BSSC_PLATES);
	LocationBean recouperateLocation = null;
	String sampleName = "sample";
	boolean yellowSample = true;
	Viscosity viscosity = Viscosity.HIGH;
	double concentration = 1;
	double timePerFrame = 1;
	int frames = 1;
	float exposureTemperature = 22;
	double molecularWeight;
	
	public LocationBean getLocation() {
		return location;
	}
	public void setLocation(LocationBean location) {
		location.setConfig(BSSCSessionBean.BSSC_PLATES);
		if (!location.isValid()) {
			throw new IllegalArgumentException("Location is not valid");
		}
		this.location = location;
	}
	public LocationBean getRecouperateLocation() {
		return recouperateLocation;
	}
	public void setRecouperateLocation(LocationBean recouperateLocation) {
		if (recouperateLocation != null) {
			recouperateLocation.setConfig(BSSCSessionBean.BSSC_PLATES);
			if (!recouperateLocation.isValid()) {
				throw new IllegalArgumentException("Recouperation Location is not valid");
			}
			if (recouperateLocation.equals(location)) {
				throw new IllegalArgumentException("Recouperation location can't be the same as sample location");
			}
		}
		this.recouperateLocation = recouperateLocation;
	}
	public String getSampleName() {
		return sampleName;
	}
	public void setSampleName(String sampleName) {
		this.sampleName = sampleName;
	}
	public boolean isYellowSample() {
		return yellowSample;
	}
	public void setYellowSample(boolean yellowsample) {
		this.yellowSample = yellowsample;
	}
	public String getViscosity() {
		return viscosity.toString();
	}
	public void setViscosity(String viscosity) {
		this.viscosity = Viscosity.fromString(viscosity);
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
	public double getMolecularWeight() {
		return molecularWeight;
	}
	public void setMolecularWeight(double molecularWeight) {
		this.molecularWeight = molecularWeight;
	}
	public LocationBean getBufferLocation() {
		return bufferLocation;
	}
	public void setBufferLocation(LocationBean bufferLocation) {
		bufferLocation.setConfig(BSSCSessionBean.BSSC_PLATES);
		if (!bufferLocation.isValid()) {
			throw new IllegalArgumentException("Buffer location is not valid");
		}
		this.bufferLocation = bufferLocation;
	}
	@Override
	public void clear() {
	}
}