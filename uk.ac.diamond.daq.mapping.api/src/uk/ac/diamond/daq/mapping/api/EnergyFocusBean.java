/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.api;

import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;

import gda.function.ILinearFunction;

/**
 * Parameters required to integrate the display and updating of an energy/focus coupling function into the focus scan
 * wizard
 */
public class EnergyFocusBean {
	/**
	 * File into which the function parameters will be serialised & from which they will be deserialised
	 */
	private String energyFocusConfigPath;
	/**
	 * Path to the CSV file where the energy focus configuration logs will be appended.
	 */
	private String csvFilePath;

	/**
	 * Reference to a GDA object holding the function used to couple energy and focus position
	 */
	private ILinearFunction<Energy, Length> energyFocusFunction;

	/**
	 * Number of decimal places to show in the "Change interception" pop-up message
	 */
	private int changeInterceptionDecimalPlaces = 2;

	public String getEnergyFocusConfigPath() {
		return energyFocusConfigPath;
	}

	public void setEnergyFocusConfigPath(String energyFocusConfigPath) {
		this.energyFocusConfigPath = energyFocusConfigPath;
	}

	public ILinearFunction<Energy, Length> getEnergyFocusFunction() {
		return energyFocusFunction;
	}

	public void setEnergyFocusFunction(ILinearFunction<Energy, Length> energyFocusFunction) {
		this.energyFocusFunction = energyFocusFunction;
	}

	public int getChangeInterceptionDecimalPlaces() {
		return changeInterceptionDecimalPlaces;
	}

	public void setChangeInterceptionDecimalPlaces(int changeInterceptionDecimalPlaces) {
		this.changeInterceptionDecimalPlaces = changeInterceptionDecimalPlaces;
	}

	public String getCsvFilePath() {
		return csvFilePath;
	}

	public void setCsvFilePath(String csvFilePath) {
		this.csvFilePath = csvFilePath;
	}

	@Override
	public String toString() {
		return "EnergyFocusBean [energyFocusConfigPath=" + energyFocusConfigPath + ", csvFilePath=" + csvFilePath
				+ ", energyFocusFunction=" + energyFocusFunction + ", changeInterceptionDecimalPlaces="
				+ changeInterceptionDecimalPlaces + "]";
	}
}
