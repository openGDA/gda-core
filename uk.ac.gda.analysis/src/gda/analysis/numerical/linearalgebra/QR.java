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
 * QR Class
 */
public class QR {
	/**
	 * Constructs the QR decomposition of a[0..n-1][0..n-1]. The upper triangular matrix R is returned in the upper
	 * triangle of a, except for the diagonal elements of R which are returned in d[0..n-1]. The orthogonal matrix Q is
	 * represented as a product of n-1 Householder matrices Q_0 ... Q_n-2, where Qj = 1-u_j \otimes u_j/c_j. The ith
	 * component of uj is zero for i = 0,...,j-1 while the nonzero components are returned in a[i,j] for i = j, . . . ,
	 * n-1.
	 */
	public class Qrdcmp {
		private int sing;

		/**
		 * Singularity returns as true (1) if singularity is encountered during the decomposition, but the decomposition
		 * is still completed in this case; otherwise it returns false (0).
		 * 
		 * @return true(1) or false(0) if a singularity is encountered.
		 */
		public int getSingularity() {
			return sing;
		}

		/**
		 * @param a
		 * @param n
		 * @param c
		 * @param d
		 */
		public void qrdcmp(double[][] a, int n, double[] c, double[] d) {
			int i, j, k;
			double scale, sigma = 0.0, sum, tau;
			sing = 0;
			for (k = 0; k < n - 1; k++) {
				scale = 0.0;
				for (i = k; i < n; i++) {
					scale = Math.max(scale, Math.abs(a[i][k]));
				}
				if (scale == 0.0) {
					sing = 1;
					c[k] = d[k] = 0.0;
				} else {
					for (i = k; i < n; i++) {
						a[i][k] /= scale;
					}
					for (sum = 0.0, i = k; i < n; i++) {
						sum += a[i][k] * a[i][k];
					}
					if (a[k][k] == 0.0) {
						sigma = Math.sqrt(sum);
					} else if (a[k][k] < 0.0) {
						sigma = -Math.sqrt(sum);
					} else if (a[k][k] > 0.0) {
						sigma = Math.sqrt(sum);
					}
					a[k][k] += sigma;
					c[k] = sigma * a[k][k];
					d[k] = -scale * sigma;
					for (j = k + 1; j < n; j++) {
						for (sum = 0.0, i = k; i < n; i++) {
							sum += a[i][k] * a[i][j];
						}
						tau = sum / c[k];
						for (i = k; i < n; i++) {
							a[i][j] -= tau * a[i][k];
						}
					}
				}
			}
			d[n - 1] = a[n - 1][n - 1];
			if (d[n - 1] == 0.0) {
				sing = 1;
			}
		}
	}

	/**
	 * Solves the set of n linear equations A ¡¤ x = b. a[0..n-1][0..n-1], c[0..n-1], and d[0..n-1] are input as the
	 * output of the routine qrdcmp and are not modified. b[0..n-1] is input as the right-hand side vector, and is
	 * overwritten with the solution vector on output.
	 * 
	 * @param a
	 * @param n
	 * @param c
	 * @param d
	 * @param b
	 */
	public void qrsolv(double[][] a, int n, double[] c, double[] d, double[] b) {
		int i, j;
		double sum, tau;

		for (j = 0; j < n - 1; j++) {
			for (sum = 0.0, i = j; i < n; i++) {
				sum += a[i][j] * b[i];
			}
			tau = sum / c[j];
			for (i = j; i < n; i++) {
				b[i] -= tau * a[i][j];
			}
		}
		rsolv(a, n, d, b);
	}

	/**
	 * Used in qrsolv
	 * 
	 * @param a
	 * @param n
	 * @param d
	 * @param b
	 */
	public void rsolv(double[][] a, int n, double[] d, double[] b) {
		int i, j;
		double sum;

		b[n - 1] /= d[n - 1];
		for (i = n - 2; i >= 0; i--) {
			for (sum = 0.0, j = i + 1; j < n; j++) {
				sum += a[i][j] * b[j];
			}
			b[i] = (b[i] - sum) / d[i];
		}
	}
}
