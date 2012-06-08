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

package gda.analysis.numerical.differentiation;

import gda.analysis.datastructure.DataVector;
import gda.analysis.numerical.random.MT;
import gda.analysis.numerical.statistical.SavitzkyGolay;
import gda.analysis.numerical.statistical.Smooth;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Differentiate using a standard numerical recipe.
 */
public class Differentiate {
	/**
	 * Differentiate using a standard numerical recipe.
	 * 
	 * @param x
	 *            x data
	 * @param y
	 *            y data
	 * @param option
	 *            choice of numerical differentation 0 = 2 point forward difference 1 = 3 point centred difference 2 = 5
	 *            point centred difference
	 * @return DataVector which is the derivative of y with respect to x
	 */
	public static DataVector standardDerivative(DataVector x, DataVector y, int option) {
		if (y.size() < 10)
			option = 0;

		DataVector temp = new DataVector(y.size());
		switch (option) {
		case 0:
			for (int i = 0; i < y.size(); i++) {
				if (i == y.size() - 1) {
					// forward
					temp.set(i, backwardDifference(x, y, i));
				} else {
					// use backward difference at end point
					temp.set(i, forwardDifference(x, y, i));
				}
			}

			break;
		case 1:
			for (int i = 0; i < y.size(); i++) {
				if (i == 0) {
					// forward
					temp.set(i, forwardDifference(x, y, i));
				} else if (i == y.size() - 1) {
					// use backward difference at end point
					temp.set(i, backwardDifference(x, y, i));
				} else {
					// centred difference
					temp.set(i, threePointCentredDifference(x, y, i));
				}
			}
			break;
		case 2:
			for (int i = 0; i < y.size(); i++) {
				if (i <= 1) {
					// forward
					temp.set(i, forwardDifference(x, y, i));
				} else if (i >= y.size() - 2) {
					// use backward difference at end point
					temp.set(i, backwardDifference(x, y, i));
				} else {
					// centred difference
					temp.set(i, fivePointCentredDifference(x, y, i));
				}
			}
			break;

		}
		return temp;
	}

	/**
	 * Differentiate using a standard numerical recipie
	 * 
	 * @param x
	 *            x data
	 * @param y
	 *            y data
	 * @param option
	 *            choice of numerical differentation 0 = 2 point forward difference 1 = 3 point centred difference 2 = 5
	 *            point centred difference
	 * @return secondderivative of y with respect to x
	 */
	public static DataVector secondDerivative(DataVector x, DataVector y, @SuppressWarnings("unused") int option) {
		// 1st Derivative
		DataVector temp = standardDerivative(x, y, 1);
		// Derivative again
		temp = standardDerivative(x, temp, 1);
		return temp;

	}

	/**
	 * Differentiate using Savitzky Galoy
	 * 
	 * @param x
	 * @param y
	 * @param noOfPoints
	 * @param order
	 * @return Derivative of a curve calculated using Savitzky Golay method
	 */

	public static DataVector SGDerivative(DataVector x, DataVector y, int noOfPoints, int order) {
		DataVector temp = new DataVector(y.size());
		double dx, f = 0.0, factor = 1.0;
		int nLeft = 0;
		int nRight = 0;
		int nLeftIndex = 0;
		boolean repeat = false;
		double[] coeffs = new double[noOfPoints];

		if (order < 1 || order > 2) {
			order = 1;
		}
		switch (order) {
		case 2:
			factor = 2.0;
			dx = Math.pow((x.get(1) - x.get(0)), 2.0);
			break;
		case 1:
		default:
			factor = 1.0;
			dx = x.get(1) - x.get(0);
		}

		int centre = (noOfPoints - 1) / 2;
		for (int i = 0; i < y.size(); i++) {
			if (i < centre) {
				nLeftIndex = 0;
				nLeft = i;
				nRight = noOfPoints - nLeft - 1;
				repeat = true;
			} else if (i > y.size() - 1 - centre) {
				nRight = y.size() - 1 - i;
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
				SavitzkyGolay.savgol(coeffs, noOfPoints, nLeft, nRight, order, 2);
			}

			f = 0.0;
			for (int j = 0; j < noOfPoints; j++) {
				f += factor * coeffs[j] * y.get(nLeftIndex + j);
			}
			f = f / dx;
			temp.set(i, f);
		}
		return temp;
	}

	/**
	 * @param x
	 * @param y
	 * @param order
	 * @return SG Derivative
	 */
	public static DataVector SGDerivative(DataVector x, DataVector y, int order) {
		return SGDerivative(x, y, 21, order);
	}

	/**
	 * @param x
	 * @param y
	 * @param i
	 * @return forward difference
	 */
	public static double forwardDifference(DataVector x, DataVector y, int i) {
		double h = 0.0;
		h = Math.abs(x.get(i + 1) - x.get(i));
		return (y.get(i + 1) - y.get(i)) / h;
	}

	/**
	 * @param x
	 * @param y
	 * @param i
	 * @return backward difference
	 */
	public static double backwardDifference(DataVector x, DataVector y, int i) {
		double h = 0.0;
		h = Math.abs(x.get(i) - x.get(i - 1));
		return (y.get(i) - y.get(i - 1)) / h;
	}

	/**
	 * @param x
	 * @param y
	 * @param i
	 * @return three point centred difference
	 */
	public static double threePointCentredDifference(DataVector x, DataVector y, int i) {
		double h = 0.0;
		h = Math.abs(x.get(i + 1) - x.get(i - 1));
		return (y.get(i + 1) - y.get(i - 1)) / (2.0 * h);
	}

	/**
	 * @param x
	 * @param y
	 * @param i
	 * @return five point centred difference
	 */
	public static double fivePointCentredDifference(DataVector x, DataVector y, int i) {
		double h = 0.0;
		h = Math.abs(x.get(i + 1) - x.get(i - 1));
		return (y.get(i - 2) - 8.0 * y.get(i - 1) + 8.0 * y.get(i + 1) - y.get(i + 2)) / (12.0 * h);
	}

	/**
	 * Test Main method.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		double dx = 0.2;
		DataVector x = new DataVector(200);
		DataVector y = new DataVector(200);
		MT rand = new MT();
		double xvalue = 0.0;
		for (int i = 0; i < 200; i++) {
			x.set(i, xvalue);
			double value = 5.0 * Math.sin(0.4 * Math.PI * x.getIndex(i)) + rand.nextGaussian();
			y.set(i, value); // Sinusoid with noise
			xvalue += dx;
		}
		// DataVector result = SGDerivative(x, y, 1);
		DataVector result = Smooth.lowessSmooth(x, y, 10, 2, 0.0);
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("outtest.dat"));
			for (int i = 0; i < 200; i++) {
				out.write(x.get(i) + "\t" + y.get(i) + "\t" + result.get(i) + "\n");
			}
			out.close();
		} catch (IOException e) {
		}
	}

}
