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

package gda.analysis.numerical.linearalgebra;

/**
 * Cholesky Class
 */
public class Cholesky {
	/**
	 * Given a positive-definite symmetric matrix a[0..n-1][0..n-1], this routine constructs its Cholesky decomposition,
	 * A = L ¡¤ L^T . On input, only the upper triangle of a need be given; it is not modified. The Cholesky factor L is
	 * returned in the lower triangle of a, except for its diagonal elements which are returned in p[0..n-1].
	 * 
	 * @param a
	 *            a positive-definite symmetric matrix (input)
	 * @param p
	 *            diagonal (output)
	 */

	public void choldc(double[][] a, double[] p) {
		int i, j, k, n;
		double sum;
		if (a.length != a[0].length) {
			throw new IllegalArgumentException("matrix a in choldc must be square");
		}
		n = a.length;
		for (i = 0; i < n; i++) {
			for (j = i; j < n; j++) {
				for (sum = a[i][j], k = i - 1; k >= 0; k--) {
					sum -= a[i][k] * a[j][k];
				}
				if (i == j) {
					if (sum <= 0.0) {
						throw new IllegalArgumentException("choldc failed: Not positive definite ?");
					}
					p[i] = Math.sqrt(sum);
				} else {
					a[j][i] = sum / p[i];
				}
			}
		}
	}

	/**
	 * Solves the set of n linear equations A ¡¤ x = b, where a is a positive-definite symmetric matrix.
	 * a[0..n-1][0..n-1] and p[0..n-1] are input as the output of the routine choldc. Only the lower subdiagonal portion
	 * of a is accessed. b[0..n-1] is input as the right-hand side vector. The solution vector is returned in x[0..n-1].
	 * a, n, and p are not modified and can be left in place for successive calls with different right-hand sides b. b
	 * is not modified unless you identify b and x in the calling sequence, which is allowed.
	 * 
	 * @param a
	 * @param p
	 * @param b
	 * @param x
	 */
	public void cholsl(double[][] a, double[] p, double[] b, double[] x) {
		int i, k, n;
		double sum;

		if (a.length != a[0].length) {
			throw new IllegalArgumentException("matrix a in cholsl must be square");
		}
		n = a.length;

		for (i = 0; i <= n - 1; i++) {
			for (sum = b[i], k = i - 1; k >= 0; k--) {
				sum -= a[i][k] * x[k];
			}
			x[i] = sum / p[i];
		}
		for (i = n - 1; i >= 0; i--) {
			for (sum = x[i], k = i + 1; k <= n - 1; k++) {
				sum -= a[k][i] * x[k];
			}
			x[i] = sum / p[i];
		}
	}
}
