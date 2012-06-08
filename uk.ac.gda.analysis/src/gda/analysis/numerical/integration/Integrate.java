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

package gda.analysis.numerical.integration;

import gda.analysis.datastructure.DataVector;

/**
 * Integrate using simpsons algorithm adapted for unequally spaced data
 */
public class Integrate {
	/**
	 * Integrate using simpsons algorithm adapted for unequally spaced data
	 * 
	 * @param x
	 *            x data
	 * @param y
	 *            y data
	 * @return result
	 */
	public static double simpsonNE(DataVector x, DataVector y) {
		return Simpson.simpsonNE(x.doubleArray(), y.doubleArray());
	}

	/**
	 * Integrate using simpsons algorithm adapted for unequally spaced data
	 * 
	 * @param x
	 *            x data
	 * @param y
	 *            y data
	 * @return result
	 */
	public static double simpson(DataVector x, DataVector y) {
		return Simpson.simpson(x.get(1) - x.get(0), y.doubleArray());
	}

	/**
	 * Integrate using simpsons algorithm adapted for unequally spaced data
	 * 
	 * @param y
	 *            y data
	 * @param h
	 * @return result
	 */
	public static double simpson(DataVector y, double h) {
		return Simpson.simpson(h, y.doubleArray());
	}

}
