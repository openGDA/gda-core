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

public class SaLongCalculator extends SaPerpCalculator implements IVirtualAxisCombinedCalculator {

	@Override
	public Double getRBV(List<Double> values) {

		Double sax = values.get(0);
		Double say = values.get(1);
		Double salong = say * Math.cos(Math.toRadians(angle)) - sax * Math.sin(Math.toRadians(angle));

		return salong;
	}

	@Override
	public List<Double> getDemands(Double value, List<Double> values) {

		Double sax = values.get(0);
		Double say = values.get(1);

		Double saperp = say * Math.sin(Math.toRadians(angle)) + sax * Math.cos(Math.toRadians(angle));
		Double salong = value;

		sax = -1 * salong * Math.sin(Math.toRadians(angle)) + saperp * Math.cos(Math.toRadians(angle));
		say = salong * Math.cos(Math.toRadians(angle)) + saperp * Math.sin(Math.toRadians(angle));

		return Arrays.asList(new Double[] {sax, say});
	}
}