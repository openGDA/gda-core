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

import gda.analysis.datastructure.DataVector;

/**
 * A static class containing a range of smoothing algortihms. The classes Lowess, SavityzkyGolay, AvgSmooth, generally
 * contain fortran-java conversions or other code. I've left these codes as is for clarity (they mainly use double
 * arrays being fortran) and only use data vectors in this class. This just means if poeple don't like the data vector
 * approach they can implement something else
 */
public class Smooth {

	/**
	 * Apply SavityzkyGolay smoothing with default parameters (5 points, order 2) Note : data must be equally spaced (or
	 * very close to..) for this algorithm to work
	 * 
	 * @param g
	 *            input datavector
	 * @return smoothed data vector
	 */
	public static DataVector sgSmooth(DataVector g) {
		return sgSmooth(g, 5, 2);
	}

	/**
	 * Apply SavityzkyGolay smoothing Note : data must be equally spaced (or very close to..) for this algorithm to work
	 * 
	 * @param g
	 *            input datavector
	 * @param noOfPoints
	 *            No of points (must be odd)
	 * @param order
	 *            (order of fitted function, usually 2 or 4)
	 * @return smoothed data vector
	 */
	public static DataVector sgSmooth(DataVector g, int noOfPoints, int order) {
		DataVector result = new DataVector();
		double[] coeffs = new double[noOfPoints];
		double f;
		int nLeft = 0;
		int nRight = 0;
		int nLeftIndex = 0;
		boolean repeat = false;

		// int ld = 0;
		int centre = (noOfPoints - 1) / 2;
		for (int i = 0; i < g.size(); i++) {
			if (i < centre) {
				nLeftIndex = 0;
				nLeft = i;
				nRight = noOfPoints - nLeft - 1;
				repeat = true;
			} else if (i > g.size() - 1 - centre) {
				nRight = g.size() - 1 - i;
				nLeft = noOfPoints - 1 - nRight;
				nLeftIndex = i - nLeft;
				repeat = true;
			} else {
				nLeft = (noOfPoints - 1) / 2;
				nRight = (noOfPoints - 1) / 2;
				nLeftIndex = i - nLeft;
				if (i == centre) {
					repeat = true;
				} else {
					repeat = false;
				}
			}
			if (repeat) {
				SavitzkyGolay.savgol(coeffs, noOfPoints, nLeft, nRight, 0, order);
			}
			f = 0.0;
			for (int j = 0; j < noOfPoints; j++) {
				f += coeffs[j] * g.get(nLeftIndex + j);
			}
			result.add(f);
		}
		return result;
	}

	/**
	 * Appky average smoothing to a data vector. A data point point is replaced by the average of n surrounding points
	 * Works for equally and unequally spaced data
	 * 
	 * @param g
	 * @param noOfPoints
	 * @return smoothed data vector
	 */

	public static DataVector averageSmooth(DataVector g, int noOfPoints) {
		return AvgSmooth.averageSmooth(g, noOfPoints);
	}

	/**
	 * Appky average smoothing to a data vector. A data point point is replaced by the average of 3 surrounding points
	 * Works for equally and unequally spaced data
	 * 
	 * @param g
	 * @return smoothed data vector
	 */
	public static DataVector averageSmooth(DataVector g) {
		return AvgSmooth.averageSmooth(g, 3);
	}

	/**
	 * Apply lowess smoothing Works for equally and unequally spaced data
	 * 
	 * @param x
	 * @param y
	 * @return smoothed data
	 */
	public static DataVector lowessSmooth(DataVector x, DataVector y) {
		return lowessSmooth(x, y, 10, 0, 0.0);
	}

	/**
	 * Apply lowess smoothing Works for equally and unequally spaced data
	 * 
	 * @param x
	 * @param y
	 * @param f
	 *            degree of smooothing (range 0.2-0.8...default 0.5)
	 * @param nIter
	 *            no of iterations (recommended range 0-5)
	 * @param delta
	 *            step size (recommended range 0.0-3.0)
	 * @return smoothed data
	 */

	public static DataVector lowessSmooth(DataVector x, DataVector y, int f, int nIter, double delta) {
		// working arrays
		double[] ys = new double[y.size()];
		double[] rw = new double[y.size()];
		double[] res = new double[y.size()];
		double[] x1 = new double[x.size()];
		double[] y1 = new double[y.size()];
		for (int i = 0; i < y.size(); i++) {
			x1[i] = x.get(i);
			y1[i] = y.get(i);
		}

		Lowess.lowess(x1, y1, f, nIter, delta, ys, rw, res);
		return new DataVector(ys);
	}

}
