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

import gda.jython.InterfaceProvider;
import gda.jython.batoncontrol.ClientDetails;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.gda.beans.IRichBean;
import uk.ac.gda.devices.hatsaxs.beans.LocationBean;

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

	public static Map<String, Boolean> MODES;
	public static void setModes(Map<String, Boolean> modes) {
		MODES = modes;
	}
	
	LocationBean location = new LocationBean(BSSCSessionBean.BSSC_PLATES);
	LocationBean recouperateLocation = null;
	String buffers = "";
	String bufferCache = "";//save buffer list when switching between isBuffer t/f
	boolean buffer = false;
	String mode = MODES.keySet().iterator().next();
	String key = "";
	String sampleName = "sample";
	boolean yellowSample = true;
	Viscosity viscosity = Viscosity.HIGH;
	double concentration = 1;
	double timePerFrame = 1;
	int frames = 1;
	float exposureTemperature = 22;
	double molecularWeight;
	String visit = "";
	String username = "";
	boolean isStaff;

	public TitrationBean() {
		ClientDetails myDetails = InterfaceProvider.getBatonStateProvider().getMyDetails();
		this.visit = myDetails.getVisitID();
		this.username = myDetails.getUserID();
		this.isStaff = myDetails.getAuthorisationLevel() >= 3;
	}
	
	public String getVisit() {
		return visit;
	}
	public void setVisit(String visit) {
		if (!(isStaff || this.visit.equals(visit))) {
			throw new UnsupportedOperationException("User does not have permission to change username/visit");
		}
		this.visit = visit;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		if (!(isStaff || this.username.equals(username))) {
			throw new UnsupportedOperationException("User does not have permission to change username/visit");
		}
		this.username = username;
	}
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
		}
		this.recouperateLocation = recouperateLocation;
	}
	public boolean isBuffer() {
		return buffer;
	}
	public void setBuffer(boolean buffer) {
		this.buffer = buffer;
		if (buffer) {
			bufferCache = buffers;
			buffers = "";
		} else {
			buffers = bufferCache;
		}
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
	public String getBuffers() {
		return buffers;
	}
	public void setBuffers(String buffers) {
		Pattern validCell = Pattern.compile("\\s*([0-9]+)\\s*([a-zA-Z])\\s*([0-9]+)\\s*");
		Matcher matcher; 
		String[] cells = buffers.split("[;,]+");
		for (int i = 0; i < cells.length; i++ ) {
			matcher = validCell.matcher(cells[i]);
			if (matcher.matches()) {
				try {
					LocationBean loc = new LocationBean(BSSCSessionBean.BSSC_PLATES);
					String plate = matcher.group(1).toLowerCase();
					String row = matcher.group(2).toLowerCase();
					String col = matcher.group(3).toLowerCase();
					//validate
					loc.setPlate(Short.valueOf(plate));
					loc.setRow(Character.valueOf(row.charAt(0)));
					loc.setColumn(Short.valueOf(col));
					//clean
					cells[i] = String.format("%s%s%s",plate, row, col);
				} catch (IllegalArgumentException iae) {
					throw iae;
				}
			} else if (cells[i].equals("")) {
				this.buffers = "";
				return;
			} else {
				throw new IllegalArgumentException(String.format("Buffer plate %s is not valid", cells[i]));
			}
		}
		this.buffers = Arrays.toString(cells).replaceAll("[\\[\\]]", "");
		if (!buffers.equals("")) {
			buffer = false;
		}
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		if (mode == null || !MODES.containsKey(mode = mode.toUpperCase())) {
			throw new IllegalArgumentException("Mode " + mode + " is not valid");
		}
		this.mode = mode;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key.toLowerCase();
	}
	@Override
	public void clear() {
	}
}