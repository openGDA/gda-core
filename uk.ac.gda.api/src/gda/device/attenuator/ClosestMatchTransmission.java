/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.attenuator;

import java.io.Serializable;

/**
 * Instances wrap the nearest achievable transmission together with the energy that was used to calculate it:
 * As some beamline attenuation systems provides discrete states, where only specific transmission levels are available.
 */
public final class ClosestMatchTransmission implements Serializable {

	private static final String DISPLAY_FORMAT = "%s (transmission %.6f ( %.4f percent ), energy %.2f keV)";

	/** Closest transmission value that could be achieved (in 0.0 to 1.0 unit range )*/
	private double closestAchievableTransmission;

	/** Energy / keV which was used in the calculation (may be different to the supplied energy) */
	private double energy;

	@Override
	public String toString() {
		return String.format(DISPLAY_FORMAT,
			getClass().getSimpleName(),
			getClosestAchievableTransmission(),
			getClosestAchievableTransmissionAsPercentage(),
			getEnergy());
	}

	/**
	 * Provides energy at which transmission matching was calculated.
	 * @return corresponding energy in keV
	 */
	public double getEnergy() {
		return energy;
	}

	public void setEnergy(double energyInKev) {
		energy = energyInKev;
	}

	/**
	 * @return transmission in 0.0 to 1.0 scale
	 */
	public double getClosestAchievableTransmission() {
		return closestAchievableTransmission;
	}

	/**
	 * Sets transmission in 0.0 to 1.0 scale
	 */
	public void setClosestAchievableTransmission(double transmission) {
		closestAchievableTransmission = transmission;
	}

	public double getClosestAchievableTransmissionAsPercentage() {
		return closestAchievableTransmission * 100.0;
	}
}
