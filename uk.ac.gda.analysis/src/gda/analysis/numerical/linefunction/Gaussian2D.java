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
 * A 2D Gaussian Function There are 5 parameters for the gaussian Parameter 0 : area Parameter 1 : xposition (centre)
 * Parameter 2 : xsigma (variance) Parameter 3 : yposition (centre) Parameter 4 : ysigma (variance)
 */
public class Gaussian2D extends AbstractFunction {
	private static String[] parameterNames = { "area", "xposition", "xsigma", "yposition", "ysigma" };

	/**
	 * Constructor.
	 * 
	 * @param parms
	 */
	public Gaussian2D(double... parms) {
		super(parameterNames);
		if (parms.length != 5 && parms.length != 0) {
			throw new IllegalArgumentException("No .of parameters should be 0 or 5");
		}
		if (parms.length == 0) {
			// Set area to 1 by default
			getParameter("area").setValue(1.0);
			// Set position to 0.0 by default
			getParameter("xposition").setValue(0.0);
			// Set sigma to 1.0 by default
			getParameter("xsigma").setValue(1.0);
			// Set sigma to 1.0 by default
			getParameter("yposition").setValue(0.0);
			// Set sigma to 1.0 by default
			getParameter("ysigma").setValue(1.0);
		}

		// Default set lower bounds on area and sigma to zero
		// Because they can't be less than zero!!!
		getParameter("area").setLowerLimit(0.0);
		getParameter("xsigma").setLowerLimit(0.0);
		getParameter("ysigma").setLowerLimit(0.0);
	}

	@Override
	public double val(double... positions) {
		if (positions.length != 2) {
			throw new IllegalArgumentException("The 2D Gaussian requires x and y position values");
		}
		// Some temporary variables
		double y = 0.;
		double area = getParameter("area").getValue();
		double xpos = getParameter("xposition").getValue();
		double xsig = getParameter("xsigma").getValue();
		double ypos = getParameter("yposition").getValue();
		double ysig = getParameter("ysigma").getValue();

		double norm = 2.0 * Math.PI * xsig * ysig;
		double arg1 = (positions[0] - xpos) / xsig;
		double arg2 = (positions[1] - ypos) / ysig;
		double ex = Math.exp(-0.5 * arg1 * arg1) * Math.exp(-0.5 * arg2 * arg2);
		y += (area * ex / norm);
		return y;
	}
}
