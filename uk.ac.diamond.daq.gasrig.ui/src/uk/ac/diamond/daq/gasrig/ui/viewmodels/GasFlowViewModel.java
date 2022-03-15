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

package uk.ac.diamond.daq.gasrig.ui.viewmodels;

import uk.ac.diamond.daq.gasrig.api.IGasFlow;

/**
 * A viewmodel which represents an {@link IGasFlow}. It provides information about the gas flow,
 * methods for manipulating the gas flow, and property change support for UI databinding.
 *
 */
public class GasFlowViewModel extends PropertyChangeSupportViewModel {

	// Property names for data-binding
	public static final String PRESSURE = "pressure";
	public static final String PRESSURE_PERCENTAGE = "pressurePercentage";
	public static final String NORMALISED_FLOW_RATE = "normalisedFlowRate";
	public static final String MASS_FLOW = "massFlow";

	private GasMixViewModel gasMixViewModel;
	private IGasFlow gasFlow;

	private double previousPressurePercentage = 0;
	private double previousMassFlow = 0;
	private double previousNormalisedFlowRate = 0;

	public GasFlowViewModel(GasMixViewModel gasMixViewModel, IGasFlow gasFlow) {
		this.gasMixViewModel = gasMixViewModel;
		this.gasFlow = gasFlow;
	}

	public double getPressure() {
		return gasFlow.getPressure();
	}

	public void setPressure(double pressure) {
		double previousPressure = gasFlow.getPressure();
		gasFlow.setPressure(pressure);
		firePropertyChange(PRESSURE, previousPressure, pressure);
		fireNormalisedFlowRateChange();
		gasMixViewModel.fireAllPressureAndMassFlowUpdates();
	}

	public double getPressurePercentage() {
		return gasFlow.getPressurePercentage();
	}

	public int getGasId() {
		return gasFlow.getGasId();
	}

	public double getMassFlow() {
		return gasFlow.getMassFlow();
	}

	public double getNormalisedFlowRate() {
		return gasFlow.getNormalisedFlowRate();
	}

	protected void firePressurePercentageChange() {
		var newPressurePercentage = getPressurePercentage();
		firePropertyChange(PRESSURE_PERCENTAGE, previousPressurePercentage, newPressurePercentage);
		previousPressurePercentage = newPressurePercentage;
	}

	protected void fireMassFlowChange() {
		var newMassFlow = getMassFlow();
		firePropertyChange(MASS_FLOW, previousMassFlow, newMassFlow);
		previousMassFlow = newMassFlow;
	}

	protected void fireNormalisedFlowRateChange() {
		var newNormalisedFlowRate = getNormalisedFlowRate();
		firePropertyChange(NORMALISED_FLOW_RATE, previousNormalisedFlowRate, newNormalisedFlowRate);
		previousNormalisedFlowRate = newNormalisedFlowRate;
	}
}
