/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.impl;

import uk.ac.diamond.daq.mapping.api.IBeamlineConfiguration;

public class ExampleBeamlineConfigurationImpl implements IBeamlineConfiguration {

	private double energy;
	private double exitSlitSize;
	private double idGap;
	private double attenuation;

	public double getEnergy() {
		return energy;
	}

	public void setEnergy(double energy) {
		this.energy = energy;
	}

	public double getExitSlitSize() {
		return exitSlitSize;
	}

	public void setExitSlitSize(double exitSlitSize) {
		this.exitSlitSize = exitSlitSize;
	}

	public double getIdGap() {
		return idGap;
	}

	public void setIdGap(double idGap) {
		this.idGap = idGap;
	}

	public double getAttenuation() {
		return attenuation;
	}

	public void setAttenuation(double attenuation) {
		this.attenuation = attenuation;
	}
}
