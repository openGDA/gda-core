/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.arpes.calculator;

import java.util.Arrays;
import java.util.List;

import uk.ac.gda.api.virtualaxis.IVirtualAxisCombinedCalculator;

public class SmPerpCalculator implements IVirtualAxisCombinedCalculator {

	@Override
	public Double getRBV(List<Double> values) {

		double smz = values.get(0);
		double smhor = values.get(1);
		double smpolar = Math.toRadians(values.get(2));

		return smz * Math.sin(smpolar) + smhor * Math.cos(smpolar);
	}

	@Override
	public List<Double> getDemands(Double smperp, List<Double> values) {

		double smz = values.get(0);
		double smhor = values.get(1);
		final double smpolar = Math.toRadians(values.get(2));
		
		// Find smlong
		double smlong = smz * Math.cos(smpolar) - smhor * Math.sin(smpolar);
		
		// Calculate the smhor and smz positions
		smhor = -1 * smlong * Math.sin(smpolar) + smperp * Math.cos(smpolar);
		smz = smlong * Math.cos(smpolar) + smperp * Math.sin(smpolar);

		// Only return the values to move order must match values order
		return Arrays.asList(new Double[] { smz, smhor });
	}
}