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

		return smy * Math.sin(smazimuth) + smx * Math.cos(smazimuth);
	}

	@Override
	public List<Double> getDemands(Double value, List<Double> values) {

		double smx = values.get(0);
		double smy = values.get(1);
		final double smazimuth = values.get(2); // Don't change azimuth
		final double deltaSmVert = value - getRBV(values);

		smy += deltaSmVert * Math.sin(Math.toRadians(smazimuth));
		smx += deltaSmVert * Math.cos(Math.toRadians(smazimuth));

		return Arrays.asList(new Double[] { smx, smy, smazimuth });
	}
}