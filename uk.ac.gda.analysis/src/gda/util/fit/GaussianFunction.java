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
public class GaussianFunction implements Function {

	// Returns the value of the gaussian at x
	// Gaussian parameters are passed through a
	private double norm;

	private boolean vary_norm = true;

	private double centre;

	private boolean vary_centre = true;

	private double width;

	private boolean vary_width = true;

	/**
	 * @param _norm
	 * @param _x
	 * @param _width
	 */
	public GaussianFunction(double _norm, double _x, double _width) {
		norm = _norm;
		centre = _x;
		width = _width;
	}

	@Override
	public double val(double x, double... parms) {
		// Check x and a size for correctness
		// initialize y to zero
		double y = 0.;
		// Sum up the gaussians
		double arg = (x - parms[1]) / parms[2];
		double ex = Math.exp(-arg * arg);
		y += (parms[0] * ex);
		return y;
	} // val

	@Override
	public double[] getParameters() {
		return new double[] { norm, centre, width };
	}

	@Override
	public boolean[] getFixedParameters() {
		return new boolean[] { vary_norm, vary_centre, vary_width };
	}

	@Override
	public void setParameters(double... parameters) {
		norm = parameters[0];
		centre = parameters[1];
		width = parameters[2];
	}

	@Override
	public void setFixedParameters(boolean... parameters) {
		vary_norm = parameters[0];
		vary_centre = parameters[1];
		vary_width = parameters[2];
	}
}
