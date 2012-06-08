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
 * A gaussian There are three parameters are named Parameter 0 : area Parameter 1 : position Parameter 2 : sigma To
 * retrieve an the area parameter you can use getAreaParameters() or getParameter("area")
 */
public class Gaussian1D extends AbstractFunction {
	private static String[] parameterNames = { "area", "position", "sigma" };

	/**
	 * Initialise the Gaussian. To set up a gaussian you can specify with no parameters e.g. Gaussian1D = new
	 * Gaussian1D() will set up a gaussian with area =1.0,position=0.0,sigma=1.0 or e.g. Gaussian1D = new
	 * Gaussian1D(1.0,0.0,1.0) corresponding to area, position or sigma The lower bounds of the area and sigma
	 * parameters are set to zero
	 * 
	 * @param parms
	 *            The parameters
	 */
	public Gaussian1D(double... parms) {
		super(parameterNames);
		// Can have 0 or 3 parameters .. i.e. you specify them all
		// or none at all
		if (parms.length != 3 && parms.length != 0) {
			throw new IllegalArgumentException("No .of parameters should be 0 or 3");
		}
		if (parms.length == 0) {
			// Set area to 1 by default
			getParameter("area").setValue(1.0);
			// Set position to 0.0 by default
			getParameter("position").setValue(1.0);
			// Set sigma to 1.0 by default
			getParameter("sigma").setValue(1.0);
		} else {
			for (int i = 0; i < parms.length; i++) {
				getParameter(i).setValue(parms[i]);
			}
		}

		// Default set lower bounds on area and sigma to zero
		// Because they can't be less than zero!!!
		getParameter("area").setLowerLimit(0.0);
		getParameter("sigma").setLowerLimit(0.0);
	}

	/**
	 * This constructor generates a gausian with a good set of limits, and sets the values to be in the centre of the
	 * limits. This should enable a quick setup of constrained boundrys
	 * 
	 * @param minPeakPosition
	 *            The minimum possible position of the peak
	 * @param maxPeakPosition
	 *            The maximum possible position of the peak
	 * @param maxArea
	 *            The maximum area of the peak
	 * @param maxSigma
	 *            The maximum Sigma, or spread of the peak
	 */
	public Gaussian1D(double minPeakPosition, double maxPeakPosition, double maxArea, double maxSigma) {
		super(parameterNames);

		getParameter("area").setLowerLimit(0.0);
		getParameter("area").setUpperLimit(maxArea);
		getParameter("area").setValue(maxArea / 2.0);

		getParameter("sigma").setLowerLimit(0.0);
		getParameter("sigma").setUpperLimit(maxSigma);
		// better fitting is generaly found if sigma expands into the peak.
		getParameter("sigma").setValue(maxSigma / 10.0);

		getParameter("position").setLowerLimit(minPeakPosition);
		getParameter("position").setUpperLimit(maxPeakPosition);
		getParameter("position").setValue(minPeakPosition + ((maxPeakPosition - minPeakPosition) / 2.0));

	}

	@Override
	public double val(double... positions) {
		if (positions.length != 1) {
			throw new IllegalArgumentException("The 1D Gaussian can only be calculated at one position value");
		}
		double y = 0.;
		double area = getParameter("area").getValue();
		double pos = getParameter("position").getValue();
		double sigma = getParameter("sigma").getValue();
		double norm = Math.sqrt(2.0 * Math.PI * sigma * sigma);
		double arg = (positions[0] - pos) / sigma;
		double ex = Math.exp(-0.5 * arg * arg);
		y = (area * ex / norm);
		return y;
	}

	@Override
	public String toString() {
		String output = "";

		// output the three parameters in order, we can lable them in the
		// composite function
		output = "area\t" + getParameter("area").getValue() + " [" + getParameter("area").getLowerLimit() + ","
				+ getParameter("area").getUpperLimit() + "]\n";
		output = output + "Position\t" + getParameter("position").getValue() + " ["
				+ getParameter("position").getLowerLimit() + "," + getParameter("position").getUpperLimit() + "]\n";
		output = output + "Sigma\t" + getParameter("sigma").getValue() + " [" + getParameter("sigma").getLowerLimit()
				+ "," + getParameter("sigma").getUpperLimit() + "]\n";

		return output;
	}

}
