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

/**
 * Simpson Class
 */
public class Simpson {
	/**
	 * simpsonNE approximates the integral of an unevenly spaced data. The routine repeatedly interpolates a 3-point
	 * Lagrangian polynomial to the data and integrates that exactly. Based on Reference: Philip Davis, Philip
	 * Rabinowitz, Methods of Numerical Integration, Second Edition, Dover, 2007, ISBN: 0486453391, LC: QA299.3.D28.
	 * 
	 * @param x
	 *            x values
	 * @param y
	 *            y values
	 * @return result
	 */
	public static double simpsonNE(double[] x, double[] y) {

		double e, f, feints, sum1, x1, x2, x3;
		double[] del = new double[3];
		double[] g = new double[3];
		double[] pi = new double[3];
		int i, n, ntab;
		double result = 0.0;

		ntab = x.length;

		if (ntab <= 2) {
			throw new IllegalArgumentException("Error no of points < 0");
		}

		n = 0;

		while (true) {

			x1 = x[n];
			x2 = x[n + 1];
			x3 = x[n + 2];
			e = x3 * x3 - x1 * x1;
			f = x3 * x3 * x3 - x1 * x1 * x1;
			feints = x3 - x1;
			del[0] = x3 - x2;
			del[1] = x1 - x3;
			del[2] = x2 - x1;
			g[0] = x2 + x3;
			g[1] = x1 + x3;
			g[2] = x1 + x2;
			pi[0] = x2 * x3;
			pi[1] = x1 * x3;
			pi[2] = x1 * x2;

			sum1 = 0.0;
			for (i = 0; i < 3; i++) {
				sum1 = sum1 + y[n + i] * del[i] * (f / 3.0 - g[i] * 0.50 * e + pi[i] * feints);
			}
			result = result - sum1 / (del[0] * del[1] * del[2]);

			n = n + 2;

			if (ntab - 1 <= n + 1) {
				break;
			}

		}

		if ((ntab % 2) != 0) {
			return result;
		}

		n = ntab - 3;
		x3 = x[ntab - 1];
		x2 = x[ntab - 2];
		x1 = x[ntab - 3];
		e = x3 * x3 - x2 * x2;
		f = x3 * x3 * x3 - x2 * x2 * x2;
		feints = x3 - x2;
		del[0] = x3 - x2;
		del[1] = x1 - x3;
		del[2] = x2 - x1;
		g[0] = x2 + x3;
		g[1] = x1 + x3;
		g[2] = x1 + x2;
		pi[0] = x2 * x3;
		pi[1] = x1 * x3;
		pi[2] = x1 * x2;

		sum1 = 0.00;
		for (i = 0; i < 3; i++) {
			sum1 = sum1 + y[n + i] * del[i] * (f / 3.0 - g[i] * 0.5 * e + pi[i] * feints);
		}

		result = result - sum1 / (del[0] * del[1] * del[2]);

		return result;
	}

	/**
	 * @param x
	 * @param y
	 * @return double
	 */
	public static double simpson(double[] x, double[] y) {
		return simpson(Math.abs(x[1] - x[0]), y);
	}

	/**
	 * @param h
	 * @param y
	 * @return double
	 */
	public static double simpson(double h, double[] y) {

		double f, sum1;
		double[] del = new double[3];
		double[] g = new double[3];
		double[] pi = new double[3];
		int i, n, ntab;
		double result = 0.0;
		ntab = y.length;

		if (ntab <= 2) {
			throw new IllegalArgumentException("Error no of points < 0");
		}

		if ((ntab % 2) == 0) {
			n = ntab - 1;
		} else {
			n = ntab;
		}

		result = y[0] + y[n - 1] + 4.00 * y[n - 2];
		for (i = 1; i < n - 3; i = i + 2) {
			result = result + 4.0 * y[i] + 2.0 * y[i + 1];
		}
		result = h * result / 3.0;

		if (ntab % 2 == 1) {
			return result;
		}

		f = h * h * h;
		del[0] = h;
		del[1] = -2.0 * h;
		del[2] = h;
		g[0] = h;
		g[0] = 0.0;
		g[0] = -h;
		pi[0] = 0.0;
		pi[1] = -h * h;
		pi[2] = 0.0;
		n = n - 1;

		sum1 = 0.0;
		for (i = 0; i < 3; i++) {
			sum1 = sum1 + y[n - 2 + i] * del[i] * (f / 3.0 - g[i] * 0.5 * h * h + pi[i] * h);
		}

		result = result + 0.5 * sum1 / (h * h * h);

		return result;
	}

}
