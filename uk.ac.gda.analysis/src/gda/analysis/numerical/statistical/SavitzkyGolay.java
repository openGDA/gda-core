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

package gda.analysis.numerical.statistical;

import gda.analysis.numerical.linearalgebra.LU;

/**
 * SavitzkyGolay Class
 */
public class SavitzkyGolay {
	/**
	 * @param c
	 *            SavitzkyGolay coefficients
	 * @param np
	 *            no of points (nl+nr+1)
	 * @param nl
	 *            no. of leftward points
	 * @param nr
	 *            no. of rightward points
	 * @param ld
	 *            order of the derivative desired (usually 0)
	 * @param m
	 *            order of the smoothing polynomial (usual values are m = 2 or m = 4)
	 */

	public static void savgol(double[] c, int np, int nl, int nr, int ld, int m) {
		int imj, ipj, j, k, kk, mm;
		double fac, sum;
		double[] temp = new double[c.length];
		int centre = np - nr - 1;
		int index = centre - 1;

		if (np < nl + nr + 1 || nl < 0 || nr < 0 || ld > m || nl + nr < m) {
			throw new IllegalArgumentException("bad args in Savitzky Golay");
		}
		int[] indx = new int[m + 1];
		double[][] a = new double[m + 1][m + 1];
		double[] b = new double[m + 1];

		for (ipj = 0; ipj <= (m << 1); ipj++) {
			sum = (ipj != 0 ? 0.0 : 1.0);
			for (k = 1; k <= nr; k++) {
				sum += Math.pow(k, ipj);
			}
			for (k = 1; k <= nl; k++) {
				sum += Math.pow(-k, ipj);
			}
			mm = Math.min(ipj, 2 * m - ipj);
			for (imj = -mm; imj <= mm; imj += 2) {
				a[1 + (ipj + imj) / 2 - 1][1 + (ipj - imj) / 2 - 1] = sum;
			}
		}

		LU.ludcmp(a);
		indx = LU.getRowPermutation();
		for (j = 1; j <= m + 1; j++) {
			b[j - 1] = 0.0;
		}
		b[ld] = 1.0;
		LU.lubksb(a, indx, b);
		for (kk = 1; kk <= np; kk++) {
			c[kk - 1] = 0.0;
		}
		for (k = -nl; k <= nr; k++) {
			sum = b[0];
			fac = 1.0;
			for (mm = 1; mm <= m; mm++) {
				sum += b[mm] * (fac *= k);
			}
			kk = ((np - k) % np) + 1;
			c[kk - 1] = sum;
		}

		// pdq added
		// coeffs c are wrapped in an odd way (0,-2,-1,1,2)...rearrange them
		// to be consistent with application i.e -2,-1,0,1,2

		temp[centre] = c[0];
		for (int i = 1; i <= nl; i++) {
			temp[index] = c[i];
			index--;
		}
		index = centre + 1;
		for (int i = np - 1; i > centre; i--) {
			temp[index] = c[i];
			index++;
		}
		for (int i = 0; i < c.length; i++) {
			// System.out.println("c and temp\t"+c[i]+"\t"+temp[i]);
			c[i] = temp[i];
		}

	}
}
