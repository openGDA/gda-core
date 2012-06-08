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

import gda.analysis.datastructure.DataVector;
import gda.analysis.datastructure.DataVectorMath;
import gda.analysis.numerical.differentiation.Differentiate;

/**
 * Methods for interpolation of DataVectors
 */
public class Interpolator {

	/**
	 * @param input_x
	 * @param input_y
	 * @param desired_x
	 * @return interpolated point y
	 */
	public static double linearInterpolatedPoint(DataVector input_x, DataVector input_y, double desired_x) {
		return Linear.linearInterpolation(input_x, input_y, desired_x);
	}

	/**
	 * Interpolate an x,y data set to a desired x spacing
	 * 
	 * @param input_x
	 * @param input_y
	 * @param desired_x
	 * @return y values at desired spacing
	 */
	public static DataVector cubicSpline(DataVector input_x, DataVector input_y, DataVector desired_x) {

		// spline
		double[] y2 = new double[input_y.size()];
		DataVector result = new DataVector(desired_x.size());
		double yp1 = Differentiate.forwardDifference(input_x, input_y, 0);
		double ypn = Differentiate.backwardDifference(input_x, input_y, input_y.size() - 1);
		double xin[] = input_x.doubleArray();
		double yin[] = input_y.doubleArray();
		int n = yin.length;
		CubicSpline.spline(xin, yin, n, yp1, ypn, y2);
		for (int i = 0; i < desired_x.size(); i++) {
			double y = CubicSpline.splint(xin, yin, y2, n, desired_x.get(i));
			result.set(i, y);
		}
		return result;
	}

	/**
	 * Find y for a given point desired_x in the dataset an input_x,input_y Using polynomial interpolation This routine
	 * will try to use 7 points adjacent to the desired x point
	 * 
	 * @param input_x
	 * @param input_y
	 * @param desired_x
	 * @param noOfPoints
	 * @return y value and error in y at desired_x value
	 */
	public static double[] polyInterpolatedPoint(DataVector input_x, DataVector input_y, double desired_x,
			int noOfPoints) {
		double[] result = null;
		int left = 1, right = 1;
		// size of the fitted polynomial
		// find seven points around the de
		int n = Math.min(input_x.size(), noOfPoints);
		int centre = 0;
		// extract this data from the datavector
		int index = DataVectorMath.nearestLowerElementIndex(input_x, desired_x);
		if (n % 2 != 0) {
			centre = (n - 1) / 2;
		} else {
			centre = n / 2;

		}
		if (index < centre) {
			left = index;
			right = n - left - 1;
		} else if (index > input_x.size() - 1 - centre) {
			right = input_x.size() - 1 - index;
			left = n - 1 - right;
		} else {

			if (n % 2 != 0) {
				left = (n - 1) / 2;
				right = (n - 1) / 2;
			} else {
				left = n / 2;
				right = n - left - 1;
			}
		}
		/*
		 * if (i < centre) { nLeftIndex = 0; nLeft = i; nRight = noOfPoints - nLeft - 1; repeat = true; } else if (i >
		 * g.size() - 1 - centre) { nRight = g.size() - 1 - i; nLeft = noOfPoints - 1 - nRight; nLeftIndex=i-nLeft;
		 * repeat = true; } else { nLeft = (noOfPoints - 1) / 2; nRight = (noOfPoints - 1) / 2; nLeftIndex=i-nLeft; if
		 * (i == centre) { repeat = true; } else { repeat = false; } }
		 */
		/*
		 * if (index > n && index < input_x.size() - n) { if (n % 2 == 0) { left = n / 2 - 1; right = n / 2 + 1; } else {
		 * left = right = (n - 1) / 2; } } else if (index < n) { left = index; right = n - left; } else if (index >
		 * input_x.size() - 1 - n) { right = input_x.size() - 1 - index; left = n - right; }
		 */
		result = Polynomial.polynomialInterpolation(input_x.getSubset(index - left, index + right).doubleArray(),
				input_y.getSubset(index - left, index + right).doubleArray(), desired_x);

		return result;
	}

	/**
	 * Find y for a given point desired_x in the dataset an input_x,input_y Using rational interpolation
	 * 
	 * @param input_x
	 * @param input_y
	 * @param desired_x
	 * @param noOfPoints
	 * @return y values at desired_x value
	 */
	public static double[] ratInterpolatedPoint(DataVector input_x, DataVector input_y, double desired_x, int noOfPoints) {
		double[] result = null;
		int left = 1, right = 1;
		// size of the fitted polynomial
		// find seven points around the de
		int n = Math.min(input_x.size(), noOfPoints);

		// extract this data from the datavector
		int index = DataVectorMath.nearestElementIndex(input_x, desired_x);
		// index = Math.min(input_x.size() - 1, index);
		// index = Math.max(0, index);
		int centre = 0;
		/*
		 * if (i < centre) { nLeftIndex = 0; nLeft = i; nRight = noOfPoints - nLeft - 1; repeat = true; } else if (i >
		 * g.size() - 1 - centre) { nRight = g.size() - 1 - i; nLeft = noOfPoints - 1 - nRight; nLeftIndex=i-nLeft;
		 * repeat = true; } else { nLeft = (noOfPoints - 1) / 2; nRight = (noOfPoints - 1) / 2; nLeftIndex=i-nLeft; if
		 * (i == centre) { repeat = true; } else { repeat = false; } }
		 */
		if (n % 2 != 0) {
			centre = (n - 1) / 2;
		} else {
			centre = n / 2;

		}
		if (index < centre) {
			left = index;
			right = n - left - 1;
		} else if (index > input_x.size() - 1 - centre) {
			right = input_x.size() - 1 - index;
			left = n - 1 - right;
		} else {

			if (n % 2 != 0) {
				left = (n - 1) / 2;
				right = (n - 1) / 2;
			} else {
				left = n / 2;
				right = n - left - 1;
			}
		}

		result = Rational.ratint(input_x.getSubset(index - left, index + right).doubleArray(), input_y.getSubset(
				index - left, index + right).doubleArray(), desired_x);

		return result;

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		DataVector y = new DataVector(100);
		DataVector x = new DataVector(100);
		double x_start = -10.0;
		double x_step = 20.0 / 100.0;
		double xc = x_start;
		for (int i = 0; i < 100; i++) {
			x.set(i, xc);
			double val = 5.0 * xc * xc - 1.235 * xc + 96.3;
			y.set(i, val);
			xc += x_step;
		}
		// extrapolation to x = 55.0
		double myx = 10.0;
		// Rational interpolation/extrapolation
		double result[] = ratInterpolatedPoint(x, y, myx, 5);
		// What the answer should be
		double val = 5.0 * myx * myx - 1.235 * myx + 96.3;

		System.out.println("result of rational interpolation\t" + result[0] + "actual\t" + val);

		result = polyInterpolatedPoint(x, y, myx, 5);

		System.out.println("result of polynomial interpolation\t" + result[0] + "actual\t" + val);

	}

}
