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
import gda.analysis.numerical.statistical.Smooth;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Example Class.
 */
public class Example {
	/**
	 * Main method
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// Create some nosiy data
		double dx = 0.2;
		DataVector x = new DataVector(200);
		DataVector y = new DataVector(200);
		MT rand = new MT();
		double xvalue = 0.0;
		for (int i = 0; i < 200; i++) {
			x.set(i, xvalue);
			double value = 5.0 * Math.sin(0.4 * Math.PI * x.get(i)) + rand.nextGaussian();
			y.set(i, value); // Sinusoid with noise
			xvalue += dx;
		}

		// A lowess smooth
		DataVector result = Smooth.lowessSmooth(x, y, 10, 2, 0.0);
		// and output to a file
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("out_lowess_test.dat"));
			for (int i = 0; i < 200; i++) {
				out.write(x.get(i) + "\t" + y.get(i) + "\t" + result.get(i) + "\n");
			}
			out.close();
		} catch (IOException e) {
		}
		// SG smooth

		result = Smooth.sgSmooth(y, 21, 4);
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("out_sg_test.dat"));
			for (int i = 0; i < 200; i++) {
				out.write(x.get(i) + "\t" + y.get(i) + "\t" + result.get(i) + "\n");
			}
			out.close();
		} catch (IOException e) {
		}

		// normal derivative

		result = Differentiate.standardDerivative(x, y, 2);
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("out_deriv_test.dat"));
			for (int i = 0; i < 200; i++) {
				out.write(x.get(i) + "\t" + y.get(i) + "\t" + result.get(i) + "\n");
			}
			out.close();
		} catch (IOException e) {
		}
		// normal derivative

		result = Differentiate.SGDerivative(x, y, 21, 1);
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("out_sgderiv_test.dat"));
			for (int i = 0; i < 200; i++) {
				out.write(x.get(i) + "\t" + y.get(i) + "\t" + result.get(i) + "\n");
			}
			out.close();
		} catch (IOException e) {
		}

	}

}
