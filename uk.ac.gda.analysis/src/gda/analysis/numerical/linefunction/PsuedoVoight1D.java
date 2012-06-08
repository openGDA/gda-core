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

package gda.analysis.numerical.linefunction;

/**
 * 
 */
public class PsuedoVoight1D extends AbstractFunction {
	// The parameter names
	private static String[] parameterNames = { "area", "position", "mixing", "gaussian_sigma", "lorentzian_gamma" };

	/**
	 * @param parms
	 */
	public PsuedoVoight1D(double... parms) {
		super(parameterNames);
		if (parms.length != 5 && parms.length != 0) {
			throw new IllegalArgumentException("No .of parameters should be 0 or 5");
		}

		if (parms.length == 0) {
			// Set area to 1 by default
			getParameter("area").setValue(1.0);
			// Set position to 0.0 by default
			getParameter("position").setValue(0.0);
			// Set sigma to 1.0 by default
			getParameter("sigma").setValue(1.0);
			// Set mixing to 1.0 by default
			getParameter("mixing").setValue(0.8);
			// Set mixing to 1.0 by default
			getParameter("gaussian_sigma").setValue(1.0);
			// Set mixing to 1.0 by default
			getParameter("lorentzian_gamma").setValue(1.0);
		}

		// Default set lower bounds on area and gamma to zero
		getParameter("area").setLowerLimit(0.0);
		getParameter("mixing").setLowerLimit(0.0);
		getParameter("gaussian_sigma").setLowerLimit(0.0);
		getParameter("lorentzian_gamma").setLowerLimit(0.0);

	}

	@Override
	public double val(double... positions) {
		if (positions.length != 1) {
			throw new IllegalArgumentException("The 1D Psuedo Voight can only be calculated at one position value");
		}
		double y = 0.;
		double area = getParameter("area").getValue();
		double pos = getParameter("position").getValue();
		double mixing = getParameter("mixing").getValue();
		double sigma = getParameter("gaussian_sigma").getValue();
		double gamma = getParameter("lorentzian_gamma").getValue();
		// Lorentzian part
		double norm = 0.5 * gamma * area / Math.PI;
		double arg = Math.pow((positions[0] - pos), 2.0) - Math.pow(0.5 * gamma, 2.0);
		y += (1.0 - mixing) * norm / arg;
		// Gaussian part
		norm = Math.sqrt(2.0 * Math.PI * sigma * sigma);
		arg = (positions[0] - pos) / sigma;
		double ex = Math.exp(-0.5 * arg * arg);
		y += mixing * area * ex / norm;

		return y;
	}

}
