/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device;

import java.io.Serializable;

/**
 * A class for passing information on the status of a temperature controller.
 */
final public class TemperatureStatus implements Serializable {
	private double currentTemperature = -273;

	private double maximumTemperature = -273;

	private double minimumTemperature = -273;

	private double targetTemperature = -273;

	private int currentRamp = -1;

	private String status = "";

	private String additionalData = "";

	/**
	 * 
	 */
	public TemperatureStatus() {
	}

	/**
	 * Constructor
	 * 
	 * @param currentTemperature
	 *            the currentTemperature
	 */
	public TemperatureStatus(double currentTemperature) {
		this.currentTemperature = currentTemperature;
	}

	/**
	 * Constructor
	 * 
	 * @param currentTemperature
	 *            the currentTemperature
	 * @param currentRamp
	 *            the currentRamp
	 */
	public TemperatureStatus(double currentTemperature, int currentRamp) {
		this.currentTemperature = currentTemperature;
		this.currentRamp = currentRamp;
	}

	/**
	 * Constructor
	 * 
	 * @param currentTemperature
	 *            the currentTemperature
	 * @param status
	 *            the current status
	 */
	public TemperatureStatus(double currentTemperature, String status) {
		this.currentTemperature = currentTemperature;
		this.status = status;
	}

	/**
	 * Constructor
	 * 
	 * @param currentTemperature
	 *            the currentTemperature
	 * @param currentRamp
	 *            the currentRamp
	 * @param status
	 *            the current status
	 */
	public TemperatureStatus(double currentTemperature, int currentRamp, String status) {
		this.currentTemperature = currentTemperature;
		this.currentRamp = currentRamp;
		this.status = status;
	}

	/**
	 * Constructor
	 * 
	 * @param currentTemperature
	 *            the currentTemperature
	 * @param currentRamp
	 *            the currentRamp
	 * @param status
	 *            the current status
	 * @param additionalData
	 *            any additional data
	 */
	public TemperatureStatus(double currentTemperature, int currentRamp, String status, String additionalData) {
		this.currentTemperature = currentTemperature;
		this.currentRamp = currentRamp;
		this.status = status;
		this.additionalData = additionalData;
	}

	/**
	 * Constructor
	 * 
	 * @param currentTemperature
	 *            the currentTemperature
	 * @param maximumTemperature
	 *            the maximum temperature
	 * @param minimumTemperature
	 *            the minimum temperature
	 * @param targetTemperature
	 *            the target temperature
	 * @param currentRamp
	 *            the currentRamp
	 * @param status
	 *            the current status
	 * @param additionalData
	 *            any additional data
	 */
	public TemperatureStatus(double currentTemperature, double maximumTemperature, double minimumTemperature,
			double targetTemperature, int currentRamp, String status, String additionalData) {
		this.currentTemperature = currentTemperature;
		this.maximumTemperature = maximumTemperature;
		this.minimumTemperature = minimumTemperature;
		this.targetTemperature = targetTemperature;
		this.currentRamp = currentRamp;
		this.status = status;
		this.additionalData = additionalData;
	}

	/**
	 * @return the current temperature
	 */
	public double getCurrentTemperature() {
		return currentTemperature;
	}

	/**
	 * @return the maximum temperature
	 */
	public double getMaximumTemperature() {
		return maximumTemperature;
	}

	/**
	 * @return the minimum temperature
	 */
	public double getMinimumTemperature() {
		return minimumTemperature;
	}

	/**
	 * @return the target temperature
	 */
	public double getTargetTemperature() {
		return targetTemperature;
	}

	/**
	 * @return the current ramp
	 */
	public int getCurrentRamp() {
		return currentRamp;
	}

	/**
	 * @return the current status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @return any additional data
	 */
	public String getAdditionalData() {
		return additionalData;
	}

	@Override
	public String toString() {
		String rtrn = "\nTemperatureStatus:\n" + "  currentTemperature = " + currentTemperature + "\n"
				+ "  maximumTemperature = " + maximumTemperature + "\n" + "  minimumTemperature = "
				+ minimumTemperature + "\n" + "  targetTemperature  = " + targetTemperature + "\n" + "  status  = "
				+ status + "\n" + "  additionalData  = " + additionalData + "\n" + "\n";

		return rtrn;
	}
}
