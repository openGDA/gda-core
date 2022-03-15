/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

import uk.ac.diamond.daq.gasrig.api.IGas;

/**
 * A viewmodel providing information about an {@link IGas}.
 */
public class GasViewModel {

	private int id;
	private String name;
	private String massFlowScannableName;
	private double molarMass;
	private double maxMassFlow;

	public GasViewModel(IGas gas) {
		this.id = gas.getId();
		this.name = gas.getName();
		this.massFlowScannableName = gas.getMassFlowScannableName();
		this.molarMass = gas.getMolarMass();
		this.maxMassFlow = gas.getMaximumMassFlow();
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getMassFlowScannableName() {
		return massFlowScannableName;
	}

	public double getMolarMass() {
		return molarMass;
	}

	public double getMaxMassFlow() {
		return maxMassFlow;
	}
}
