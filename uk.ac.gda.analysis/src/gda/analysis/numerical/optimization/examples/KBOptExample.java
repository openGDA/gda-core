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

package gda.analysis.numerical.optimization.examples;

import gda.analysis.numerical.optimization.objectivefunction.AbstractObjectiveFunction;
import gda.analysis.numerical.optimization.optimizers.simplex.NelderMeadOptimizer;

/**
 * KBOptExample Class
 */
public class KBOptExample {
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		double[] x = new double[] { 200, 200, 0, 200, 0, 0, 0, 0 };
		// double[] l = new double[]
		// {-1000,-1000,-1000,-1000,-1000,-1000,-1000,-1000};
		double[] l = new double[] { -800, -800, -800, -800, -800, -800, -800, -800 };

		double[] u = new double[] { 800, 800, 800, 800, 800, 800, 800, 800 };
		AbstractObjectiveFunction func = new KBObjectiveFunction(x, l, u);
		//
		NelderMeadOptimizer min = new NelderMeadOptimizer(func);

		// Non-threaded
		min.optimize();

		System.out.println("Minimum\t" + min.getMinimum());
		for (int i = 0; i < min.getBest().length; i++) {
			System.out.println("Minimum point\t" + i + "\t" + min.getBest()[i]);
		}

	}
}
