/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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
 * Something implementing this interface can be set on a SimplePlot to transform the x values of the data on output
 * only.
 */
interface SimpleValueTransformer {
	/**
	 * Should return a transformed value.
	 *
	 * @param toBeTransformed
	 *            the x value in the data
	 * @return the x value to be used in output
	 */
	public double transformValue(double toBeTransformed);
	/**
	 * @param toBeTransformedBack
	 * @return the x value in the input
	 */
	public double transformValueBack(double toBeTransformedBack);
}
