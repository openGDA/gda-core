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

package gda.analysis.numerical.sorting;

import gda.analysis.datastructure.DataVector;

/**
 * Data Vector sorting routines
 */
public class Sort {
	/**
	 * @param g
	 * @param lo
	 * @param hi
	 */
	public static void quicksort(DataVector g, int lo, int hi) {
		// lo is the lower index, hi is the upper index
		// of the region of array a that is to be sorted
		int i = lo, j = hi;
		double h;
		double x = g.get((lo + hi) / 2);

		// partition
		do {
			while (g.get(i) < x) {
				i++;
			}
			while (g.get(j) > x) {
				j--;
			}
			if (i <= j) {
				h = g.get(i);
				g.set(i, g.get(j));
				g.set(j, h);
				i++;
				j--;
			}
		} while (i <= j);

		// recursion
		if (lo < j)
			quicksort(g, lo, j);
		if (i < hi)
			quicksort(g, i, hi);
	}

	/**
	 * @param g
	 * @param lo
	 * @param hi
	 */
	public static void quicksort(double[] g, int lo, int hi) {
		// lo is the lower index, hi is the upper index
		// of the region of array a that is to be sorted
		int i = lo, j = hi;
		double h;
		double x = g[(lo + hi) / 2];

		// partition
		do {
			while (g[i] < x) {
				i++;
			}
			while (g[j] > x) {
				j--;
			}
			if (i <= j) {
				h = g[i];
				g[i] = g[j];
				g[j] = h;
				i++;
				j--;
			}
		} while (i <= j);

		// recursion
		if (lo < j)
			quicksort(g, lo, j);
		if (i < hi)
			quicksort(g, i, hi);
	}

}
