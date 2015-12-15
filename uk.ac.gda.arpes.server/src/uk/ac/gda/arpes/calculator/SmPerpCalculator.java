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

import uk.ac.gda.arpes.scannable.CombinedCaculator;

public class SmPerpCalculator implements CombinedCaculator {

	@Override
	public Double getRBV(List<Double> values) {

		double smz = values.get(0);
		double smhor = values.get(1);
		double smpolar = Math.toRadians(values.get(2));

		return smz * Math.sin(smpolar) + smhor * Math.cos(smpolar);
	}

	@Override
	public List<Double> getDemands(Double value, List<Double> values) {

		double smz = values.get(0);
		double smhor = values.get(1);
		final double smpolar = values.get(2);
		final double deltaSmLong = value - getRBV(values);

		smz += deltaSmLong * Math.sin(Math.toRadians(smpolar));
		smhor += deltaSmLong * Math.cos(Math.toRadians(smpolar));

		return Arrays.asList(new Double[] { smz, smhor, smpolar });
	}
}