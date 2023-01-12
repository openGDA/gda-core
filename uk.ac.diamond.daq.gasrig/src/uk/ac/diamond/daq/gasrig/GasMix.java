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
import java.util.List;
import java.util.stream.Collectors;

import uk.ac.diamond.daq.gasrig.api.GasRigException;
import uk.ac.diamond.daq.gasrig.api.IGasMix;

/**
 * Represents a set of {@link GasFlow}s making up a gas mixture and implements
 * calculations relating to that mixture.
 */
public class GasMix implements IGasMix, Serializable {

	private List<GasFlow> gasFlows;
	private double totalWeightedFlowLimit = 100;

	public GasMix(List<Gas> gases) {
		gasFlows = gases.stream()
				.map(gas -> new GasFlow(gas, this))
				.collect(Collectors.toList());
	}

	@Override
	public double getTotalPressure() {
		return gasFlows.stream()
				.collect(Collectors.summingDouble(GasFlow::getPressure));
	}

	@Override
	public GasFlow getGasFlowByGasId(int id) throws GasRigException {
		return gasFlows.stream()
				.filter(gas -> gas.getGasId() == id)
				.findFirst()
				.orElseThrow(() -> new GasRigException("Could not find gas flow with gas id " + id));
	}

	@Override
	public List<GasFlow> getAllGasFlows() {
		return gasFlows;
	}

	@Override
	public double getTotalMassFlow() {
		return gasFlows.stream()
				.collect(Collectors.summingDouble(GasFlow::getMassFlow));
	}

	@Override
	public double getTotalWeightedFlowLimit() {
		return totalWeightedFlowLimit;
	}

	@Override
	public void setTotalWeightedFlowLimit(double totalWeightedFlowLimit) {
		this.totalWeightedFlowLimit = totalWeightedFlowLimit;
	}

	@Override
	public double getMaximumTotalWeightedFlow() {
		return Math.min(totalWeightedFlowLimit, getLowestWeightedFlow());
	}

	@Override
	public double getLowestNormalisedFlowRate() {
		return gasFlows.stream()
			.filter(flow -> flow.getPressure() > 0)
			.map(GasFlow::getNormalisedFlowRate)
			.sorted()
			.findFirst()
			.orElse(0d);
	}

	@Override
	public double getLowestWeightedFlow() {
		return getLowestNormalisedFlowRate() * getTotalPressure();
	}
}
