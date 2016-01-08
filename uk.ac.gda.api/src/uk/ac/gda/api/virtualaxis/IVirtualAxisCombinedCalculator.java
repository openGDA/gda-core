/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.gda.api.virtualaxis;

import java.util.List;

/**
 * Replaces CombinedCaculator (sic) from uk.ac.gda.arpes.server. Implementing classes should provide methods that convert a requested position for a combined
 * scannable in the rotated geometry of the experiment's detector to equivalent values in the normal x and y direction of the beam and vice versa.
 */
public interface IVirtualAxisCombinedCalculator {

	/**
	 * @param value The demanded value of the combined scannable
	 * @param vector The current positions of the real scannables
	 * @return The positions of the real scannables required to move to
	 * the demanded position
	 */
	public List<Double> getDemands(Double value, List<Double> vector);


	/**
	 * @param values A list of the real scannables positions
	 * @return The calculated position of the combined scannable
	 */
	public Double getRBV(List<Double> values);

}
