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

package gda.analysis.numerical.optimization.optimizers.sd;

import gda.analysis.numerical.linefunction.IParameter;
import gda.analysis.numerical.optimization.objectivefunction.AbstractObjectiveFunction;

/**
 * SteepestDescent Class
 */
public class SteepestDescent {
	AbstractObjectiveFunction func = null;

	/**
	 * @param function
	 */
	public SteepestDescent(AbstractObjectiveFunction function) {

		func = function;

	}

	/**
	 * @param NumberOfSteps
	 */
	public void Optimize(int NumberOfSteps) {

		// get the parameters
		IParameter[] params = func.getParameters();

		double[] pvals = new double[params.length];
		double[] original = new double[params.length];
		double[] deriv = new double[params.length];

		for (int i = 0; i < pvals.length; i++) {
			pvals[i] = params[i].getValue();
		}

		// find out the first Value
		double minval = func.evaluate(pvals);

		System.out.println(minval);

		double Distance = 0.01;

		for (int s = 0; s < NumberOfSteps; s++) {

			for (int i = 0; i < pvals.length; i++) {
				original[i] = params[i].getValue();
			}

			double delta = 0.0000001;
			// generate the differential
			for (int i = 0; i < pvals.length; i++) {

				pvals[i] = pvals[i] - delta;

				// get a value
				double start = func.evaluate(pvals);

				pvals[i] = pvals[i] + 2 * delta;

				// get a value
				double end = func.evaluate(pvals);

				pvals[i] = pvals[i] - delta;

				deriv[i] = (end - start) / (2 * delta);

			}

			// now the derivative has been found, move along that direction
			// by a
			// bit

			double x1 = 0;
			double y1 = func.evaluate(pvals);

			double x2 = Distance;

			double[] tmpvals = new double[params.length];

			for (int i = 0; i < pvals.length; i++) {
				tmpvals[i] = pvals[i] - deriv[i] * x2;

				if (tmpvals[i] > params[i].getUpperLimit()) {
					tmpvals[i] = params[i].getUpperLimit();
				}
				if (tmpvals[i] < params[i].getLowerLimit()) {
					tmpvals[i] = params[i].getLowerLimit();
				}

			}
			double y2 = func.evaluate(tmpvals);

			double x3 = Distance * 1.8;

			for (int i = 0; i < pvals.length; i++) {
				tmpvals[i] = pvals[i] - deriv[i] * x3;

				if (tmpvals[i] > params[i].getUpperLimit()) {
					tmpvals[i] = params[i].getUpperLimit();
				}
				if (tmpvals[i] < params[i].getLowerLimit()) {
					tmpvals[i] = params[i].getLowerLimit();
				}

			}
			double y3 = func.evaluate(tmpvals);

			double top = (((x2 - x1) * (x2 - x1) * (y2 - y3)) - ((x2 - x3) * (x2 - x3) * (y2 - y1)));
			double bottom = (((x2 - x1) * (y2 - y3)) - ((x2 - x3) * (y2 - y1)));

			double minpos = x2 - 0.5 * (top / bottom);

			for (int i = 0; i < pvals.length; i++) {
				pvals[i] = pvals[i] - deriv[i] * minpos;

				if (pvals[i] > params[i].getUpperLimit()) {
					pvals[i] = params[i].getUpperLimit();
				}
				if (pvals[i] < params[i].getLowerLimit()) {
					pvals[i] = params[i].getLowerLimit();
				}

			}

			minval = func.evaluate(pvals);

			// take the lowest, should be minval, but if its not, do
			// differnt
			// things
			if (minval < y1) {
				minval = func.evaluate(pvals);
			} else {
				Distance = Distance / 2.0;
				minval = func.evaluate(original);
				System.out.println("New distance = " + Distance);
			}

			System.out.println("pos = " + minpos + " minval = " + minval + " y1 = " + y1 + " y2 = " + y2 + " y3 = "
					+ y3);
			System.out.println(deriv[0] + " " + deriv[1] + " " + deriv[2] + " " + deriv[3] + " " + deriv[4]);

		}

		// make sure the optimised positions are set
		// minval = func.evaluate(pvals);
		System.out.println(minval);
	}
}
