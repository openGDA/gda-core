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

public class HistogramDefaultRange {

	private double lower;
	private double upper;

	public HistogramDefaultRange(double lower, double upper) {
		this.lower = lower;
		this.upper = upper;
	}

	@Override
	public String toString() {
		return BigDecimal.valueOf(lower).toString() + " - " + BigDecimal.valueOf(upper).toString();
	}

	public double getLower() {
		return lower;
	}

	public double getUpper() {
		return upper;
	}
}
