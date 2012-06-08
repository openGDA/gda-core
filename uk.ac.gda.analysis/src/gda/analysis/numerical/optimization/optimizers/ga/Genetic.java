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

package gda.analysis.numerical.optimization.optimizers.ga;

import gda.analysis.numerical.linefunction.IParameter;
import gda.analysis.numerical.optimization.objectivefunction.AbstractObjectiveFunction;

import java.util.Random;

/**
 * Genetic Class
 */
public class Genetic {
	AbstractObjectiveFunction func = null;

	double mutantProportion = 0.5;

	double mutantScaling = 0.1;

	/**
	 * @param function
	 */
	public Genetic(AbstractObjectiveFunction function) {

		func = function;

	}

	/**
	 * @param epocSize
	 * @param Numberofepocs
	 * @param mP
	 * @param mS
	 */
	public void Optimize(int epocSize, int Numberofepocs, double mP, double mS) {

		// set some factore
		mutantProportion = mP;
		mutantScaling = mS;

		// get the parameters
		IParameter[] params = func.getParameters();

		// find out the number of functions in the fit
		int numberOfFunctions = params[(params.length - 1)].getGroup() + 1;

		double[] pvals = new double[params.length];

		for (int i = 0; i < pvals.length; i++) {
			pvals[i] = params[i].getValue();
		}

		Random rand = new Random();

		// generate the first epoch, each member will be a random pertabateion
		// around the original

		double epoc[][] = new double[epocSize][params.length + 1];
		double nextepoc[][] = new double[epocSize][params.length + 1];

		// first one should be the orighinal
		for (int j = 0; j < params.length; j++) {
			epoc[0][j] = pvals[j];
		}

		// the others explore space in the bounded regions
		for (int i = 1; i < epocSize; i++) {
			for (int j = 0; j < params.length; j++) {
				epoc[i][j] = (rand.nextDouble() * (params[j].getUpperLimit() - params[j].getLowerLimit()))
						+ params[j].getLowerLimit();
			}
		}

		// now the first epoc has been created, check it inside the limits, and
		// calculate the fitness
		for (int i = 0; i < epocSize; i++) {
			for (int j = 0; j < params.length; j++) {
				if (epoc[i][j] > params[j].getUpperLimit()) {
					epoc[i][j] = (2 * params[j].getUpperLimit()) - epoc[i][j];
				}
				if (epoc[i][j] < params[j].getLowerLimit()) {
					epoc[i][j] = (2 * params[j].getLowerLimit()) - epoc[i][j];
				}
				pvals[j] = epoc[i][j];
			}
			epoc[i][params.length] = func.evaluate(pvals);
		}

		// now do the epocs,
		for (int k = 0; k < Numberofepocs; k++) {

			double mean = 0;

			// the first member of the new epoc, should be the best member
			// of the
			// last
			double minvalue = epoc[0][params.length];
			int minposition = 0;

			for (int i = 1; i < epocSize; i++) {
				if (epoc[i][params.length] < minvalue) {
					minvalue = epoc[0][params.length];
					minposition = i;
				}
			}

			for (int m = 0; m < params.length; m++) {
				pvals[m] = epoc[minposition][m];
				nextepoc[0][m] = epoc[minposition][m];
			}

			nextepoc[0][params.length] = func.evaluate(pvals);

			// now go on and get the rest of the population
			System.out.println("Best Member = " + nextepoc[0][params.length]);

			for (int j = 1; j < epocSize; j++) {

				// get mum and dad
				int mum = 0;
				int dad = 0;

				int c1 = Math.abs(rand.nextInt()) % epocSize;
				int c2 = Math.abs(rand.nextInt()) % epocSize;
				int c3 = Math.abs(rand.nextInt()) % epocSize;
				int c4 = Math.abs(rand.nextInt()) % epocSize;

				while (((Double) epoc[c1][params.length]).isNaN()) {
					c1 = Math.abs(rand.nextInt()) % epocSize;
				}
				while (((Double) epoc[c2][params.length]).isNaN()) {
					c2 = Math.abs(rand.nextInt()) % epocSize;
				}
				while (((Double) epoc[c3][params.length]).isNaN()) {
					c3 = Math.abs(rand.nextInt()) % epocSize;
				}
				while (((Double) epoc[c4][params.length]).isNaN()) {
					c4 = Math.abs(rand.nextInt()) % epocSize;
				}

				if (epoc[c1][params.length] < epoc[c2][params.length]) {
					mum = c1;
				} else {
					mum = c2;
				}

				if (epoc[c3][params.length] < epoc[c4][params.length]) {
					dad = c3;
				} else {
					dad = c4;
				}

				// crossbreed at a point, between 2 differnt functions.
				int point = Math.abs(rand.nextInt()) % numberOfFunctions;

				for (int i = 0; i < pvals.length; i++) {
					if (params[i].getGroup() < point) {
						nextepoc[j][i] = epoc[mum][i];
					} else {
						nextepoc[j][i] = epoc[dad][i];
					}
				}
				// add in random mutation
				if (rand.nextDouble() > mutantProportion) {

					c1 = Math.abs(rand.nextInt()) % epocSize;
					c2 = Math.abs(rand.nextInt()) % epocSize;

					for (int i = 0; i < params.length; i++) {
						nextepoc[j][i] = nextepoc[j][i] + (epoc[c1][i] - epoc[c2][i]) * mutantScaling;
					}
				}
			}

			// at the end of the epoc, flush the nextepoc to the epoc
			for (int i = 0; i < epocSize; i++) {
				for (int j = 0; j < params.length; j++) {
					epoc[i][j] = nextepoc[i][j];

					// then clipit

					if (epoc[i][j] > params[j].getUpperLimit()) {
						epoc[i][j] = (2 * params[j].getUpperLimit()) - epoc[i][j];
					}
					if (epoc[i][j] < params[j].getLowerLimit()) {
						epoc[i][j] = (2 * params[j].getLowerLimit()) - epoc[i][j];
					}
				}

				// finaly calculate the fitness and put it in the last digit
				for (int m = 0; m < params.length; m++) {
					pvals[m] = epoc[i][m];
				}

				epoc[i][params.length] = func.evaluate(pvals);

				mean = mean + epoc[i][params.length];
			}

			System.out.println(mean / epocSize);

		}

		// at the end find the best solution, and evaluate it, to fix the values
		// into the model

		double minval = epoc[0][params.length];
		int minpos = 0;

		for (int i = 1; i < epocSize; i++) {
			if (epoc[i][params.length] < minval) {
				minval = epoc[0][params.length];
				minpos = i;
			}
		}

		for (int m = 0; m < params.length; m++) {
			pvals[m] = epoc[minpos][m];
		}

		minval = func.evaluate(pvals);
		System.out.println(minval);
	}
}
