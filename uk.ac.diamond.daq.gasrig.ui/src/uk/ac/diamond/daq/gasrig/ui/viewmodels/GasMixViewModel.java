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

import java.util.List;
import java.util.stream.Collectors;

import uk.ac.diamond.daq.gasrig.api.GasRigException;
import uk.ac.diamond.daq.gasrig.api.IGasMix;

/**
 * A viewmodel which represents an {@link IGasMix}. It provides information about the gas mix,
 * methods for manipulating the gas mix, and property change support for UI databinding.
 *
 * It also constructs and contains a set of {@link GasFlowViewModel}s.
 */
public class GasMixViewModel extends PropertyChangeSupportViewModel {

	// Property names for data-binding
	public static final String TOTAL_PRESSURE = "totalPressure";
	public static final String TOTAL_MASS_FLOW = "totalMassFlow";
	public static final String LOWEST_NORMALISED_FLOW_RATE = "lowestNormalisedFlowRate";
	public static final String TOTAL_WEIGHTED_FLOW_LIMIT = "totalWeightedFlowLimit";
	public static final String MAXIMUM_TOTAL_WEIGHTED_FLOW = "maximumTotalWeightedFlow";
	public static final String LOWEST_WEIGHTED_FLOW = "lowestWeightedFlow";

	private IGasMix gasMix;
	private int lineNumber;

	private List<GasFlowViewModel> gasFlowViewModels;
	private double previousTotalPressure;
	private double previousTotalMassFlow;
	private double previousLowestNormalisedFlowRate;
	private double previousTotalWeightedFlowLimit;
	private double previousMaximumTotalWeighedFlow;
	private double previousLowestWeightedFlow;

	public GasMixViewModel(int lineNumber, IGasMix gasMix) {
		this.lineNumber = lineNumber;
		this.gasMix = gasMix;

		gasFlowViewModels = gasMix.getAllGasFlows().stream()
			.map(flow -> new GasFlowViewModel(this, flow))
			.collect(Collectors.toList());

		previousTotalPressure = gasMix.getTotalPressure();
		previousTotalMassFlow = gasMix.getTotalMassFlow();
		previousLowestNormalisedFlowRate = gasMix.getLowestNormalisedFlowRate();
		previousTotalWeightedFlowLimit = gasMix.getTotalWeightedFlowLimit();
		previousMaximumTotalWeighedFlow = gasMix.getMaximumTotalWeightedFlow();
		previousLowestWeightedFlow = gasMix.getLowestWeightedFlow();
	}

	protected void fireAllPressureAndMassFlowUpdates() {
		gasFlowViewModels.forEach(GasFlowViewModel::firePressurePercentageChange);
		gasFlowViewModels.forEach(GasFlowViewModel::fireMassFlowChange);
		fireTotalPressureChange();
		fireTotalMassFlowChange();
		fireLowestNormalisedFlowRateChange();
		fireLowestWeightedFlowChange();
		fireMaximumTotalWeightedFlowChange();
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public GasFlowViewModel getGasFlowViewModelByGasId(int gasId) throws GasRigException {
		return gasFlowViewModels.stream()
				.filter(wrapper -> wrapper.getGasId() == gasId)
				.findFirst()
				.orElseThrow(() -> (new GasRigException("No gas flow with specified gas id found.")));
	}

	public double getTotalPressure() {
		return gasMix.getTotalPressure();
	}

	public double getTotalMassFlow() {
		return gasMix.getTotalMassFlow();
	}

	public double getLowestNormalisedFlowRate() {
		return gasMix.getLowestNormalisedFlowRate();
	}

	public double getTotalWeightedFlowLimit() {
		return gasMix.getTotalWeightedFlowLimit();
	}

	public void setTotalWeightedFlowLimit(double totalWeightedFlowLimit) {
		gasMix.setTotalWeightedFlowLimit(totalWeightedFlowLimit);
		fireTotalWeightedFlowLimitChange();
		gasFlowViewModels.forEach(GasFlowViewModel::fireMassFlowChange);
		fireTotalMassFlowChange();
		fireMaximumTotalWeightedFlowChange();
	}

	public double getMaximumTotalWeightedFlow() {
		return gasMix.getMaximumTotalWeightedFlow();
	}

	public double getLowestWeightedFlow() {
		return gasMix.getLowestWeightedFlow();
	}

	private void fireTotalPressureChange() {
		double totalPressure = getTotalPressure();
		firePropertyChange(TOTAL_PRESSURE, previousTotalPressure, totalPressure);
		previousTotalPressure = totalPressure;
	}

	private void fireTotalMassFlowChange() {
		double totalMassFlow = getTotalMassFlow();
		firePropertyChange(TOTAL_MASS_FLOW, previousTotalMassFlow, totalMassFlow);
		previousTotalMassFlow = totalMassFlow;
	}

	private void fireLowestNormalisedFlowRateChange() {
		double lowestFlowRate = getLowestNormalisedFlowRate();
		firePropertyChange(LOWEST_NORMALISED_FLOW_RATE, previousLowestNormalisedFlowRate, lowestFlowRate);
		previousLowestNormalisedFlowRate = lowestFlowRate;
	}

	private void fireTotalWeightedFlowLimitChange() {
		double totalWeightedFlowLimit = getTotalWeightedFlowLimit();
		firePropertyChange(TOTAL_WEIGHTED_FLOW_LIMIT, previousTotalWeightedFlowLimit, totalWeightedFlowLimit);
		previousTotalWeightedFlowLimit = totalWeightedFlowLimit;
	}

	private void fireMaximumTotalWeightedFlowChange() {
		double maximumTotalWeightedFlow = getMaximumTotalWeightedFlow();
		firePropertyChange(MAXIMUM_TOTAL_WEIGHTED_FLOW, previousMaximumTotalWeighedFlow, maximumTotalWeightedFlow);
		previousMaximumTotalWeighedFlow = maximumTotalWeightedFlow;
	}

	private void fireLowestWeightedFlowChange() {
		double lowestWeightedFlow = getLowestWeightedFlow();
		firePropertyChange(LOWEST_WEIGHTED_FLOW, previousLowestWeightedFlow, lowestWeightedFlow);
		previousLowestWeightedFlow = lowestWeightedFlow;
	}

	public IGasMix getGasMix() {
		return gasMix;
	}

}
