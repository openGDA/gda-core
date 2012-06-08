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
 * A polynomial point interpolation algorithm
 */

public class Polynomial {
	/**
	 * <summary> Given arrays xa[0..n-1] and ya[0..n-1], and given a value x, this routine returns a value y, and an
	 * error estimate dy. If P(x) is the polynomial of degree n - 1 such that P(xai) = yai, i = 0,..., n-1, then the
	 * returned value y = P(x). </summary> see numerical recipies polint
	 * 
	 * @param xa
	 * @param ya
	 * @param x
	 * @return a double array containing the interpolated value of y and dy at x
	 */

	public static double[] polynomialInterpolation(double[] xa, double[] ya, double x) // input
	{
		int i, n, m, ns = 0;
		double den, dif, dift, ho, hp, w, y, dy = 0.0;
		n = xa.length;
		double[] c = new double[n];
		double[] d = new double[n];
		dif = Math.abs(x - xa[0]);

		for (i = 0; i < n; i++) {
			if ((dift = Math.abs(x - xa[i])) < dif) {
				ns = i;
				dif = dift;
			}
			c[i] = ya[i];
			d[i] = ya[i];
		}
		y = ya[ns--];
		for (m = 0; m < n - 1; m++) {
			for (i = 0; i < n - m - 1; i++) {
				ho = xa[i] - x;
				hp = xa[i + m + 1] - x;
				w = c[i + 1] - d[i];
				if ((den = ho - hp) == 0.0) {
					throw new IllegalArgumentException("Error in routine polynomialInterpolation");
				}
				den = w / den;
				d[i] = hp * den;
				c[i] = ho * den;
			}
			// dy is the error
			y += (dy = (2 * (ns + 1) < (n - m - 1) ? c[ns + 1] : d[ns--]));
			// y += ((2 * (ns + 1) < (n - m - 1) ? c[ns + 1] : d[ns--]));
		}
		return new double[] { y, dy };
	}
}
