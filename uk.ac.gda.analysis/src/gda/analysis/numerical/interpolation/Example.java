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

/**
 * Example Class
 */
public class Example {

	/**
	 * Main method.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		// Create some data
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
		double myx = -10.05;
		// Rational interpolation/extrapolation
		double result[] = Interpolator.ratInterpolatedPoint(x, y, myx, 9);
		// What the answer should be
		double val = 5.0 * myx * myx - 1.235 * myx + 96.3;

		System.out.println("result of rational interpolation\t" + result[0] + "actual\t" + val);
		// Polynomial interpolation/extrapolation
		result = Interpolator.polyInterpolatedPoint(x, y, myx, 9);

		System.out.println("result of polynomial interpolation\t" + result[0] + "actual\t" + val);

		// Simple linear interpolation test
		xc = x_start;
		for (int i = 0; i < 100; i++) {
			x.set(i, xc);
			val = 5.0 * xc - 1.0;
			y.set(i, val);
			xc -= x_step;
		}
		// Polynomial interpolation/extrapolation
		double result1 = Interpolator.linearInterpolatedPoint(x, y, 10.0);
		System.out.println("result of linear interpolation\t" + result1);

	}

}
