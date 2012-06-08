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

package gda.analysis.numerical.optimization.objectivefunction;

import gda.analysis.numerical.linefunction.IParameter;

/**
 * An optimization algorithm needs a function to minimise For curve fitting this is generally something like the sum of
 * the squares of the differences between the data and your theoretical line i.e. a chisquared function However for
 * different experiments etc. people often prefer to employ specialised functions. (often refereed to as R-factors) This
 * often comes about when the chi squared emphasises parts of the data you're not really interested in. For example you
 * may really be interested in peak positions not peak amplitudes or sometimes chisquared fits emphasis fitting to the
 * high intensity regions of a curve. Anyway there are lots of R-factor functions out their for comparing computer
 * generated curves to experimental data.
 */
public abstract class AbstractLSQObjectiveFunction extends AbstractObjectiveFunction {
	@Override
	public double evaluate(double... parameters) {
		double[] data = LMEvaluate(parameters);
		double sum = 0.0;
		for (int i = 0; i < data.length; i++) {
			sum = sum + data[i] * data[i];
		}
		return Math.sqrt(sum) / data.length;
	}

	/**
	 * @param parameters
	 * @return double[]
	 */
	public abstract double[] LMEvaluate(double... parameters);

	/**
	 * @return int Number of data points
	 */
	public abstract int getNoOfDataPoints();

	@Override
	public abstract IParameter[] getParameters();

}
