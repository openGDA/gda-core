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
 * Holds the closest transmission that was achievable, together with the energy that was used to calculate it.
 */
public class ClosestMatchTransmission implements Serializable {
	
	/** Closest transmission value that could be achieved */
	public double closestAchievableTransmission;
	
	/** Energy which was used in the calculation (may be different to the supplied energy) */
	public double energy;
	
	@Override
	public String toString() {
		return String.format("%s(transmission=%.6f, energy=%.2f)",
			getClass().getSimpleName(),
			closestAchievableTransmission,
			energy);
	}

}
