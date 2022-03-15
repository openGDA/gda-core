/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.gasrig;

import java.io.Serializable;

import uk.ac.diamond.daq.gasrig.api.IGasFlow;

/**
 * Represents a flow of a single gas within a mixture and implements calculations
 * relating to that flow.
 */
public class GasFlow implements IGasFlow, Serializable  {

	private Gas gas;
	private GasMix gasMix;

	private double pressure;

	public GasFlow(Gas gas, GasMix gasMix) {
		this.gas = gas;
		this.gasMix = gasMix;
	}

	@Override
	public double getPressure() {
		return pressure;
	}

	@Override
	public void setPressure(double pressure) {
		this.pressure = pressure;
	}

	@Override
	public double getPressurePercentage() {
		if (gasMix.getTotalPressure() == 0) return 0;

		return (pressure / gasMix.getTotalPressure()) * 100;
	}

	@Override
	public double getMassFlow() {
		if (getPressure() == 0) return 0;

		return gasMix.getMaximumTotalWeightedFlow() * (getPressure() / gasMix.getTotalPressure()) * (1/Math.sqrt(gas.getMolarMass()));
	}

	@Override
	public int getGasId() {
		return gas.getId();
	}

	@Override
	public double getNormalisedFlowRate() {
		if (getPressure() == 0) return 0;

		return gas.getMaximumMassFlow() * (Math.sqrt(gas.getMolarMass()) / getPressure());
	}
}
