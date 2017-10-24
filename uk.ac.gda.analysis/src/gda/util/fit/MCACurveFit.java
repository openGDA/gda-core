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

package gda.util.fit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MCACurveFit Class
 */
public class MCACurveFit {

	private static final Logger logger = LoggerFactory.getLogger(MCACurveFit.class);

	/**
	 * @param multiModel
	 * @param fitParameters
	 * @param mcaParameters
	 * @param x
	 * @param y
	 * @return double[][] LMfit
	 */
	public static double[][] LMfit(MultiFunction multiModel, LMFittingParameters fitParameters,
			MCAParameters mcaParameters, double[] x, double[] y) {

		int noOfFunctions = multiModel.getNoOfFunctions();
		int noOfParameters = multiModel.getFunctionParameters(0).length;
		int noOfMCAParameters = mcaParameters.getNoOfParameters();
		int totalNoOfParameters = (noOfFunctions * noOfParameters) + noOfMCAParameters;

		double[] aguess = new double[totalNoOfParameters];
		boolean[] vary = new boolean[totalNoOfParameters];
		for (int i = 0; i < noOfFunctions; i++)
			for (int j = 0; j < noOfParameters; j++) {
				aguess[i * noOfParameters + j] = multiModel.getFunctionParameters(i)[j];
				vary[i * noOfParameters + j] = multiModel.getFixedParametersForFunction(i)[j];
			}

		// Set up MCA parameters first
		for (int i = (noOfFunctions * noOfParameters); i < ((noOfFunctions * noOfParameters) + noOfMCAParameters); i++) {
			aguess[i] = mcaParameters.getParameters()[i - (noOfFunctions * noOfParameters)];
			vary[i] = mcaParameters.getFixedParameters()[i - (noOfFunctions * noOfParameters)];
			logger.debug("Mca parameters are " + aguess[i] + " " + vary[i]);
		}

		double[] s = new double[y.length];
		for (int i = 0; i < y.length; i++) {
			s[i] = Math.pow(y[i], mcaParameters.getChiExponent());
		}
		double h = 1.E-8;
		try {
			LM.solve(x, aguess, y, s, vary, multiModel, h, fitParameters.getLamda(), fitParameters.getEpislon(),
					fitParameters.getMaxNoOfIterations(), fitParameters.getVerbose());
		} catch (Exception ex) {
			logger.error("Exception caught", ex);
			// System.exit(1);
		}

		double[][] results = new double[noOfFunctions][noOfParameters];
		for (int i = 0; i < noOfFunctions; i++)
			for (int j = 0; j < noOfParameters; j++) {
				results[i][j] = aguess[i * noOfParameters + j];
			}

		return results;

	}

	// test program
	/**
	 * @param cmdline
	 */
	public static void main(String[] cmdline) {

		GaussianMultiModel myModel = new GaussianMultiModel();
		myModel.addFunction(new GaussianFunction(2.3, 2.2, 1.5));
		myModel.addFunction(new GaussianFunction(1.8, 2.1, 4.5));
		LMFittingParameters fitParams = new LMFittingParameters();
		MCAParameters mcaParams = new MCAParameters();
		// Create some test data to fit against
		double[][] xy = testdata();
		double[] x = new double[xy.length];
		double[] y = new double[xy.length];
		for (int i = 0; i < xy.length; i++) {
			x[i] = xy[i][0];
			y[i] = xy[i][1];
		}

		// double[][] results =
		// MCACurveFit.LMfit(myModel, fitParams, mcaParams,
		// x, y);
		MCACurveFit.LMfit(myModel, fitParams, mcaParams, x, y);

	} // main

	/**
	 * @return double[][] xy test data
	 */
	public static double[][] testdata() {
		int npts = 100;
		double[][] xy = new double[npts][2];
		for (int i = 0; i < npts; i++) {
			xy[i][0] = 0.1 * (i + 1);
			double y = 0.;
			// Sum up the gaussians
			double arg = (xy[i][0] - 5.0) / 1.0;
			double ex = Math.exp(-arg * arg);
			y += (2.0 * ex);
			arg = (xy[i][0] - 3.0) / 2.0;
			ex = Math.exp(-arg * arg);
			y += (5.0 * ex);
			xy[i][1] = y;
		}
		return xy;
	}
}
