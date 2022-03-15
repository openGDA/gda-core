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

package uk.ac.diamond.daq.gasrig;

import java.io.Serializable;

import uk.ac.diamond.daq.gasrig.api.IGas;

/**
 * Provides information about a gas in the cabinet.
 */
public class Gas implements Serializable, IGas {

	private int id;
	private String name = "Unset";
	private String massFlowScannableName;
	private double molarMass;
	private double maximumMassFlow;

	public Gas(int id, String massFlowScannableName) {
		this.id = id;
		this.massFlowScannableName = massFlowScannableName;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	protected void setName(String name) {
		this.name = name;
	}

	@Override
	public String getMassFlowScannableName() {
		return massFlowScannableName;
	}

	@Override
	public double getMolarMass() {
		return molarMass;
	}

	protected void setMolarMass(double molarMass) {
		this.molarMass = molarMass;
	}

	@Override
	public double getMaximumMassFlow() {
		return maximumMassFlow;
	}

	protected void setMaximumMassFlow(double maximumMassFlow) {
		this.maximumMassFlow = maximumMassFlow;
	}
}
