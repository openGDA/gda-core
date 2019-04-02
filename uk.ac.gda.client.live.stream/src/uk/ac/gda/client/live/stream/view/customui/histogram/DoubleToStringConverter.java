/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.client.live.stream.view.customui.histogram;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class DoubleToStringConverter {

	private static final MathContext PRECISION = new MathContext(6, RoundingMode.HALF_UP);

	public String convert(double value) {
		BigDecimal bigDecimal = BigDecimal.valueOf(value).round(PRECISION).stripTrailingZeros();

		// stop 100 going to 1.0E2
		if (bigDecimal.precision() >= 1 &&
			bigDecimal.precision() < PRECISION.getPrecision() &&
			bigDecimal.scale() < 0 &&
			bigDecimal.scale() > (-1 * PRECISION.getPrecision())) {

			bigDecimal = bigDecimal.setScale(0);
		}

		return bigDecimal.toString();
	}
}
