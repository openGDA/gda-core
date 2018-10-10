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
	 * Reference to a GDA object holding the function used to couple energy and focus position
	 */
	private ILinearFunction energyFocusFunction;

	public String getEnergyFocusConfigPath() {
		return energyFocusConfigPath;
	}
	public void setEnergyFocusConfigPath(String energyFocusConfigPath) {
		this.energyFocusConfigPath = energyFocusConfigPath;
	}
	public ILinearFunction getEnergyFocusFunction() {
		return energyFocusFunction;
	}
	public void setEnergyFocusFunction(ILinearFunction energyFocusFunction) {
		this.energyFocusFunction = energyFocusFunction;
	}

	@Override
	public String toString() {
		return "EnergyFocusBean [energyFocusConfigPath=" + energyFocusConfigPath + ", energyFocusFunction="
				+ energyFocusFunction + "]";
	}
}
