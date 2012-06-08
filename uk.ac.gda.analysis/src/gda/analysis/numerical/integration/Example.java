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

import gda.analysis.datastructure.DataVector;
import gda.analysis.numerical.linefunction.Gaussian1D;

/**
 * Example Class.
 */
public class Example {
	/**
	 * Main Method.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		// Create a dataset with a guassian in it
		Gaussian1D g1d = new Gaussian1D(5.0, 10.0, 1.0);
		double x = 0.0;
		double range = 20.0;
		int size = 1000;
		double step = range / size;
		DataVector y1 = new DataVector(size);
		DataVector x1 = new DataVector(size);
		for (int i = 0; i < size; i++) {
			x1.set(i, x);
			y1.set(i, g1d.val(x));
			x += step;
		}

		double res = Integrate.simpson(x1, y1);

		System.out.println("Integrated Value using equal spaced simpson\t" + res + "Actual value is 5.0\t");

		res = Integrate.simpsonNE(x1, y1);

		System.out.println("Integrated Value using unequal spaced simpson\t" + res + "Actual value is 5.0\t");

	}

}
