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

/**
 * Diagonal Rational interpolation Class.
 */
public class Rational {
	static double TINY = 1.0e-25;

	/**
	 * Given arrays xa[0..n-1] and ya[0..n-1], and given a value of x, this routine returns a value of y and an accuracy
	 * estimate dy. The value returned is that of the diagonal rational function, evaluated at x, which passes through
	 * the n points (xai, yai), i = 0,...,n-1. (see numerical recipies ratint)
	 * 
	 * @param xa
	 * @param ya
	 * @param x
	 * @return a double array containing the interpolated value of y and dy at x
	 */

	public static double[] ratint(double[] xa, double[] ya, double x) {
		int n = xa.length;
		double y = 0.0, dy = 0.0;
		int m, i, ns = 0;
		double w, t, hh, h, dd;
		double[] c = new double[n];
		double[] d = new double[n];

		hh = Math.abs(x - xa[0]);
		for (i = 0; i < n; i++) {
			h = Math.abs(x - xa[i]);
			if (h == 0.0) {
				y = ya[i];
				dy = 0.0;
				return new double[] { y, dy };
			} else if (h < hh) {
				ns = i;
				hh = h;
			}
			c[i] = ya[i];
			d[i] = ya[i] + TINY;
		}
		y = ya[ns--];
		for (m = 0; m < n - 1; m++) {
			for (i = 0; i < n - m - 1; i++) {
				w = c[i + 1] - d[i];
				h = xa[i + m + 1] - x;
				t = (xa[i] - x) * d[i] / h;
				dd = t - c[i + 1];
				if (dd == 0.0) {
					throw new IllegalArgumentException("Error in routine rationalInterpolation");
				}
				dd = w / dd;
				d[i] = c[i + 1] * dd;
				c[i] = t * dd;
			}
			y += (dy = (2 * (ns + 1) < (n - m - 1) ? c[ns + 1] : d[ns--]));
		}
		return new double[] { y, dy };
	}
}
