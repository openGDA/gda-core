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
 * Cubic spline based on Numerical recipies code see. http://www.nrbook.com/b/bookfpdf/f3-3.pdf
 */
public class CubicSpline {

	/**
	 * Given arrays x[0..n-1] and y[0..n-1] containing a tabulated function, i.e., y_i = f(x_i), with x_0 less than x_1
	 * less than ... less than x_n-1, and given values yp_0 and yp_n-1 for the first derivative of the interpolating
	 * function at points 0 and n-1, respectively, this routine returns an array y2[0..n-1] that contains the second
	 * derivatives of the interpolating function at the tabulated points x_i. If yp_0 and/or yp_n-1 are equal to 1
	 * \times 10^30 or larger, the routine is signaled to set the corresponding boundary condition for a natural spline,
	 * with zero second derivative on that boundary.
	 * 
	 * @param x
	 *            x values
	 * @param y
	 *            y values
	 * @param n
	 *            array lengths
	 * @param yp1
	 *            first derivative at 0
	 * @param ypn
	 *            first derivative at n-1
	 * @param y2
	 *            second derivative of the interpolating function at points x[i]
	 */
	public static void spline(double[] x, double[] y, int n, double yp1, double ypn, double[] y2) {
		int i, k;
		double p, qn, sig, un;
		double[] u = new double[n - 1];

		if (yp1 > 0.99e30) {
			y2[0] = u[0] = 0.0;
		} else {
			y2[0] = -0.5;
			u[0] = (3.0 / (x[1] - x[0])) * ((y[1] - y[0]) / (x[1] - x[0]) - yp1);
		}
		for (i = 1; i < n - 1; i++) {
			sig = (x[i] - x[i - 1]) / (x[i + 1] - x[i - 1]);
			p = sig * y2[i - 1] + 2.0;
			y2[i] = (sig - 1.0) / p;
			u[i] = (y[i + 1] - y[i]) / (x[i + 1] - x[i]) - (y[i] - y[i - 1]) / (x[i] - x[i - 1]);
			u[i] = (6.0 * u[i] / (x[i + 1] - x[i - 1]) - sig * u[i - 1]) / p;
		}
		if (ypn > 0.99e30) {
			qn = un = 0.0;
		} else {
			qn = 0.5;
			un = (3.0 / (x[n - 1] - x[n - 2])) * (ypn - (y[n - 1] - y[n - 2]) / (x[n - 1] - x[n - 2]));
		}
		y2[n - 1] = (un - qn * u[n - 2]) / (qn * y2[n - 2] + 1.0);
		for (k = n - 2; k >= 0; k--) {
			y2[k] = y2[k] * y2[k + 1] + u[k];
		}
	}

	/**
	 * Given the arrays xa[0..n-1] and ya[0..n-1], which tabulate a function (with the xa_i¡¯s in order), and given the
	 * array y2a[0..n-1], which is the output from spline above, and given a value of x, this routine returns a
	 * cubic-spline interpolated value y.
	 * 
	 * @param xa
	 * @param ya
	 * @param y2a
	 * @param n
	 * @param x
	 * @return interpolated value at x
	 */

	public static double splint(double[] xa, double[] ya, double[] y2a, int n, double x) {
		int klo, khi, k;
		double h, b, a;
		double y;
		klo = 0;
		khi = n - 1;
		while (khi - klo > 1) {
			k = ((khi + klo + 2) >> 1) - 1;
			if (xa[k] > x) {
				khi = k;
			} else {
				klo = k;
			}
		}
		h = xa[khi] - xa[klo];
		if (h == 0.0) {
			throw new IllegalArgumentException("Bad xa input to routine splint");
		}
		a = (xa[khi] - x) / h;
		b = (x - xa[klo]) / h;
		y = a * ya[klo] + b * ya[khi] + ((a * a * a - a) * y2a[klo] + (b * b * b - b) * y2a[khi]) * (h * h) / 6.0;
		return y;
	}

}