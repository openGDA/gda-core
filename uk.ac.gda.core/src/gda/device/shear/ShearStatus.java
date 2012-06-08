/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device.shear;

import java.io.Serializable;

/**
 * A data class for the status of the Shear cell.
 */
final public class ShearStatus implements Serializable {
	private double current;

	private double gamma;

	private double amplitude;

	/**
	 * Constructor
	 */
	public ShearStatus() {
	}

	/**
	 * @param current
	 * @param gamma
	 * @param amplitude
	 */
	public ShearStatus(double current, double gamma, double amplitude) {
		this.current = current;
		this.gamma = gamma;
		this.amplitude = amplitude;
	}

	/**
	 * @param current
	 */
	public void setCurrent(double current) {
		this.current = current;
	}

	/**
	 * @return current
	 */
	public double getCurrent() {
		return current;
	}

	/**
	 * @param gamma
	 */
	public void setGamma(double gamma) {
		this.gamma = gamma;
	}

	/**
	 * @return gamma
	 */
	public double getGamma() {
		return gamma;
	}

	/**
	 * @param amplitude
	 */
	public void setAmplitude(double amplitude) {
		this.amplitude = amplitude;
	}

	/**
	 * @return amplitude
	 */
	public double getAmplitude() {
		return amplitude;
	}
}
