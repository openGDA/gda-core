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

package gda.device.evaporator;

/**
 * Simulated version of an {@link EvaporatorPocket} for use in testing and
 * dummy mode.
 */
public class DummyEvaporatorPocket implements EvaporatorPocket {

	private String label = "N/A";
	private EvaporatorPocket.Regulation mode = EvaporatorPocket.Regulation.CURRENT;
	private double current;
	private double emission;
	private double flux;

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public void setRegulation(String mode) {
		EvaporatorPocket.Regulation reg = EvaporatorPocket.Regulation.valueOf(mode.toUpperCase());
		if (reg == null) {
			throw new IllegalArgumentException("Unrecognised regulation mode");
		}
		this.mode = reg;
	}

	@Override
	public String getRegulation() {
		return mode.name();
	}

	@Override
	public void setCurrent(double current) {
		this.current = current;
	}

	@Override
	public double getCurrent() {
		return current;
	}

	@Override
	public void setEmission(double emission) {
		this.emission = emission;
	}

	@Override
	public double getEmission() {
		return emission;
	}

	@Override
	public void setFlux(double flux) {
		this.flux = flux;
	}

	@Override
	public double getFlux() {
		return flux;
	}

	@Override
	public String toString() {
		return "Pocket(name=" + label + ", regulation=" + mode + "(current=" + current + ", emission=" + emission + ", flux=" + flux + ")";
	}
}
