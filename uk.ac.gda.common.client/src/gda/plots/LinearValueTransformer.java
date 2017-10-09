/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.plots;

/**
 *
 */
class LinearValueTransformer implements SimpleValueTransformer {
	private double slope;

	private double intercept;

	/**
	 * @param slope
	 * @param intercept
	 */
	LinearValueTransformer(double slope, double intercept) {
		this.slope = slope;
		this.intercept = intercept;
	}

	@Override
	public double transformValue(double toBeTransformed) {
		return slope * toBeTransformed + intercept;
	}
	@Override
	public double transformValueBack(double toBeTransformedBack) {
		return (toBeTransformedBack -intercept)/ slope;
	}
}