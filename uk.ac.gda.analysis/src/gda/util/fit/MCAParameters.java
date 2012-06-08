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

package gda.util.fit;

/**
 * MCAParameters Class
 */
public class MCAParameters {

	private double energySlope = 1.0;

	private boolean varyEnergySlope = false;

	private double energyOffset = 0.0;

	private boolean varyEnergyOffset = false;

	private double chiExponent = 0.0;

	/**
	 * Constructor
	 */
	public MCAParameters() {

	}

	/**
	 * @return 2
	 */
	public int getNoOfParameters() {
		return 2;
	}

	/**
	 * @return double[] of energySlope and energyOffset
	 */
	public double[] getParameters() {
		return new double[] { energySlope, energyOffset };
	}

	/**
	 * @return boolean[] of varyEnergySlope and varyEnergyOffset
	 */
	public boolean[] getFixedParameters() {
		return new boolean[] { varyEnergySlope, varyEnergyOffset };
	}

	/**
	 * @param slope
	 */
	public void setEnergySlope(double slope) {
		energySlope = slope;
	}

	/**
	 * @return energySlope
	 */
	public double getEnergySlope() {
		return energySlope;
	}

	/**
	 * @param flag
	 */
	public void setVaryEnergySlope(boolean flag) {
		varyEnergySlope = flag;
	}

	/**
	 * @param offset
	 */
	public void setEnergyOffset(double offset) {
		energyOffset = offset;
	}

	/**
	 * @param flag
	 */
	public void setVaryEnergyOffset(boolean flag) {
		varyEnergyOffset = flag;
	}

	/**
	 * @return energyOffset
	 */
	public double getEnergyOffset() {
		return energyOffset;
	}

	/**
	 * @param _chiExponent
	 */
	public void setChiExponent(double _chiExponent) {
		chiExponent = _chiExponent;
	}

	/**
	 * @return chiExponent
	 */
	public double getChiExponent() {
		return chiExponent;
	}

}
