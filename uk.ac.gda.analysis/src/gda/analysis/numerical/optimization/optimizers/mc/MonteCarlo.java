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

package gda.analysis.numerical.optimization.optimizers.mc;

import gda.analysis.numerical.linefunction.IParameter;
import gda.analysis.numerical.optimization.objectivefunction.AbstractObjectiveFunction;

import java.util.Random;

/**
 * MonteCarlo Class
 */
public class MonteCarlo {

	AbstractObjectiveFunction func = null;

	/**
	 * @param function
	 */
	public MonteCarlo(AbstractObjectiveFunction function) {

		func = function;

	}

	/**
	 * @param NumberOfSteps
	 * @param T
	 */
	public void Optimize(int NumberOfSteps, double T) {

		// get the parameters
		IParameter[] params = func.getParameters();

		double[] pvals = new double[params.length];

		for (int i = 0; i < pvals.length; i++) {
			pvals[i] = params[i].getValue();
		}

		// find out the first Value
		double minval = func.evaluate(pvals);

		Random rand = new Random();

		// now loop for the number of staps
		for (int i = 0; i < NumberOfSteps; i++) {
			// pick a position at random
			int pos = Math.abs(rand.nextInt()) % pvals.length;
			double mod = (rand.nextDouble() * 2) - 1;
			double old = pvals[pos];
			pvals[pos] = pvals[pos] + mod;
			if (pvals[pos] > params[pos].getUpperLimit()) {
				pvals[pos] = params[pos].getUpperLimit();
			}
			if (pvals[pos] < params[pos].getLowerLimit()) {
				pvals[pos] = params[pos].getLowerLimit();
			}
			double testval = func.evaluate(pvals);
			if (testval < minval) {
				minval = testval;
				// System.out.println(minval);
			} else {
				double R = rand.nextDouble();
				double prob = Math.exp((testval - minval) / T);
				if (prob > R) {
					minval = testval;
					// System.out.println("Prob = "+prob+" r = "+R+" minval
					// =
					// "+minval);
				} else {
					pvals[pos] = old;
					// System.out.println(minval);
				}
			}

		}

		minval = func.evaluate(pvals);
		System.out.println(minval);
	}

}
