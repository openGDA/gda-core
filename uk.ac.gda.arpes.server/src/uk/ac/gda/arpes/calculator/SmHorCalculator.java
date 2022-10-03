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

public class SmHorCalculator implements IVirtualAxisCombinedCalculator {

	@Override
	public Double getRBV(List<Double> values) {

		double smx = values.get(0);
		double smy = values.get(1);
		double smazimuth = Math.toRadians(values.get(2));

		return smx * Math.cos(smazimuth) + smy * Math.sin(smazimuth);
	}

	@Override
	public List<Double> getDemands(Double smhor, List<Double> values) {

		double smx = values.get(0);
		double smy = values.get(1);
		final double smazimuth = Math.toRadians(values.get(2));
		
		// Find smvert
		double smvert = smy * Math.cos(smazimuth) - smx * Math.sin(smazimuth);
		
		// Calculate the smx and smy positions
		smx = -1 * smvert * Math.sin(smazimuth) + smhor * Math.cos(smazimuth);
		smy = smvert * Math.cos(smazimuth) + smhor * Math.sin(smazimuth);

		// Only return the values to move order must match values order
		return Arrays.asList(new Double[] { smx, smy });
	}
}