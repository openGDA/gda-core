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

package gda.util.fit;

/**
 * Fit a sum of Gaussians to data. The function is K y(x) = sum A_k exp(-((x - E_k)) / sigma_k)^2) 1 i.e. for a given no
 * of gaussians K each with a variable for position (E_k), amplitude (A_k) and width or variance sigma_k produce y(x).
 * This is our model which we'll fit against the data. Author : Paul Quinn
 */
public class GaussianMultiModel extends MultiFunction {

	// Returns the value of the gaussian at x
	// Gaussian parameters are passed through a

	@Override
	public double val(double x, double... parms) {
		// Check x and a size for correctness
		int K = functions.size();
		// initialize y to zero
		int totalNoOfParameters = 0;
		for (int j = 0; j < K; j++)
			totalNoOfParameters = totalNoOfParameters + functions.get(j).getParameters().length;
		// The last two parameters of the parms list are the energy offset and
		// slope
		double energySlope = parms[totalNoOfParameters];
		double energyOffset = parms[totalNoOfParameters + 1];
		double xInEnergy = energyOffset + x * energySlope;
		// Message.debug("x xinenergy\t"+x+"\t"+xInEnergy);
		double y = 0.;
		int currentIndex = 0;
		// Sum up the gaussians
		for (int j = 0; j < K; j++) {
			int noOfParameters = functions.get(j).getParameters().length;
			double[] newParameters = new double[noOfParameters];
			int l = 0;
			for (int i = currentIndex; i < currentIndex + noOfParameters; i++) {
				newParameters[l] = parms[i];
				l++;
			}
			// Message.debug("newParameters\t"+newParameters[0]+"\t"+newParameters[1]+"\t"+newParameters[2]);
			y += functions.get(j).val(xInEnergy, newParameters);
			currentIndex = currentIndex + noOfParameters;
		}
		return y;
	} // val

	@Override
	public void addFunction(Function function) {
		functions.add(function);

	}

	@Override
	public void removeFunction(int index) {
		functions.remove(index);

	}

	@Override
	public int getNoOfFunctions() {
		return functions.size();
	}

	@Override
	public double[] getFunctionParameters(int index) {
		return functions.get(index).getParameters();
	}

	@Override
	public void setFunctionParameters(int index, double... parms) {
		functions.get(index).setParameters(parms);

	}

	@Override
	public boolean[] getFixedParametersForFunction(int index) {
		return functions.get(index).getFixedParameters();
	}

	@Override
	public void setFixedParametersForFunction(int index, boolean... parms) {
		functions.get(index).setFixedParameters(parms);
	}

}
