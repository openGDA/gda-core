/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.analysis.utils.optimisation;


/**
 * This class performs the Nelder-Mead of simplex minimisation
 * @deprecated use {@link uk.ac.diamond.scisoft.analysis.optimize.NelderMead}
 */
@Deprecated
public class Neldermead extends uk.ac.diamond.scisoft.analysis.optimize.NelderMead {

	/**
	 * The main optimisation method
	 * 
	 * @param parameters
	 * @param problemDefinition
	 * @param finishCriteria
	 * @return a double array of the parameters for the optimisation
	 * @throws Exception 
	 */
	public double[] optimise(double[] parameters,
			final ProblemDefinition problemDefinition, double finishCriteria) throws Exception {

		ProblemFunction f = new ProblemFunction(problemDefinition);

		f.setParameterValues(parameters);
		setAccuracy(finishCriteria);

		optimize(null, null, f);

		//System.out.println(problemDefinition.eval(solution));
		return f.getParameterValues();
	}

}
