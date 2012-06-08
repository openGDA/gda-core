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
 * Given a matrix a[0..n-1,0..n-1], this routine replaces it by the LU decomposition of a rowwise permutation of itself.
 * a is input. a is output, arranged as in equation (2.3.14) above; RowPermutation[0..n-1] is an output vector that
 * records the row permutation effected by the partial pivoting; Sign is output as ¡¾1 depending on whether the number
 * of row interchanges was even or odd, respectively. This routine is used in combination with lubksb to solve linear
 * equations or invert a matrix.
 */
public class LU {

	private static int[] indx;

	private static double d;

	private static double TINY = 1.0e-20;

	/**
	 * Returns the Row Permutation
	 * 
	 * @return indx[] Row Permutation
	 */
	public static int[] getRowPermutation() {
		return indx;
	}

	/**
	 * Returns the sign.
	 * 
	 * @return double sign
	 */
	public static double getSign() {
		return d;
	}

	/**
	 * @param a
	 */
	public static void ludcmp(double[][] a) // input, and a is also output
	{
		int n = a.length;
		int i, j, k;
		int imax = 0;
		double big, dum, sum, temp;
		indx = new int[n];
		double[] vv = new double[n];
		d = 1.0;
		for (i = 0; i < n; i++) {
			big = 0.0;
			for (j = 0; j < n; j++) {
				if ((temp = Math.abs(a[i][j])) > big)
					big = temp;
			}
			if (big == 0.0) {
				throw new IllegalArgumentException("Error : Singular matrix in routine ludcmp");
			}
			vv[i] = 1.0 / big;
		}
		for (j = 0; j < n; j++) {
			for (i = 0; i < j; i++) {
				sum = a[i][j];
				for (k = 0; k < i; k++) {
					sum -= a[i][k] * a[k][j];
				}
				a[i][j] = sum;
			}
			big = 0.0;
			for (i = j; i < n; i++) {
				sum = a[i][j];
				for (k = 0; k < j; k++) {
					sum -= a[i][k] * a[k][j];
				}
				a[i][j] = sum;
				if ((dum = vv[i] * Math.abs(sum)) >= big) {
					big = dum;
					imax = i;
				}
			}
			if (j != imax) {
				for (k = 0; k < n; k++) {
					dum = a[imax][k];
					a[imax][k] = a[j][k];
					a[j][k] = dum;
				}
				d = -d;
				vv[imax] = vv[j];
			}
			indx[j] = imax;
			if (a[j][j] == 0.0) {
				a[j][j] = TINY;
			}
			if (j != n) {
				dum = 1.0 / (a[j][j]);
				for (i = j + 1; i < n; i++) {
					a[i][j] *= dum;
				}
			}
		}
	}

	/**
	 * @param a
	 * @param indx
	 * @param b
	 */
	public static void lubksb(double[][] a, int[] indx, double[] b) // b
	// input
	// and also
	// output
	{
		int n = a.length;
		int i, ii = 0, ip, j;
		double sum;
		for (i = 0; i < n; i++) {
			ip = indx[i];
			sum = b[ip];
			b[ip] = b[i];
			if (ii != 0) {
				for (j = ii - 1; j <= i - 1; j++) {
					sum -= a[i][j] * b[j];
				}
			} else if (sum != 0.0) {
				ii = i + 1;
			}
			b[i] = sum;
		}
		for (i = n - 1; i >= 0; i--) {
			sum = b[i];
			for (j = i + 1; j < n; j++) {
				sum -= a[i][j] * b[j];
			}
			b[i] = sum / a[i][i];
		}
	}

	/**
	 * Test Main method.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		double[][] a = new double[][] { { 1.0, 2.0, 3.0 }, { 2.0, 2.0, 3.0 }, { 3.0, 3.0, 3.0 } };
		double[] b = new double[] { 1, 2, 3 };
		ludcmp(a);
		for (int i = 0; i < a.length; i++) {
			System.out.println("a\t" + a[i][0] + "\t" + a[i][1] + "\t" + a[i][2]);
		}

		lubksb(a, indx, b);
		for (int i = 0; i < b.length; i++) {
			System.out.println("b\t" + i + "\t" + b[i]);
		}
	}
}
