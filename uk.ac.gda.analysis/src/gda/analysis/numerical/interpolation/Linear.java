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

package gda.analysis.numerical.interpolation;

import gda.analysis.datastructure.DataVector;
import gda.analysis.datastructure.DataVectorMath;

/**
 * Linear interpolation Class. Given 2 points (x0,y0) and y0,y1 The value of y at a point x between the points is y=
 * y0+(x-x0)(y1-y0)/(x1-x0)
 */
public class Linear {
	/**
	 * Given datavectors x and y and given a value of x, this routine returns a linear interpolated value of y
	 * 
	 * @param x
	 * @param y
	 * @param xpoint
	 * @return linearly interpolated y value
	 */

	public static double linearInterpolation(DataVector x, DataVector y, double xpoint) {
		int i = 0, j = 1;
		int nearIndex = DataVectorMath.nearestLowerElementIndex(x, xpoint);
		if (nearIndex == 0) {
			i = 0;
			j = 1;
		} else if (nearIndex == x.size() - 1) {
			i = x.size() - 1;
			j = x.size() - 1 - 1;
		} else {

			if ((Math.abs(x.get(i + 1)) > Math.abs(xpoint)) && (Math.abs(x.get(i)) < Math.abs(xpoint))) {
				i = nearIndex;
				j = i + 1;
			} else {
				i = nearIndex;
				j = i - 1;
			}
		}
		return (y.get(i) + (xpoint - x.get(i)) * (y.get(i) - y.get(j)) / (x.get(i) - x.get(j)));
	}
}
