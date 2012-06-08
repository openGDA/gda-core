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
 * A 1D Lorentzian Three parameters Parameter 0 : area<BR>
 * Parameter 1 : position (x0 below)<BR>
 * Parameter 2 : width (fwhm - gamma) <BR>
 * L(x) = 0.5 GAMMA/pi * (x-x0)^2 + (0.5*GAMMA)^2
 */
public class Lorentzian1D extends AbstractFunction {
	// The parameter names
	private static String[] parameterNames = { "area", "position", "gamma" };

	/**
	 * Constructor.
	 * 
	 * @param parms
	 */
	public Lorentzian1D(double... parms) {
		super(parameterNames);
		if (parms.length != 3 && parms.length != 0) {
			throw new IllegalArgumentException("No .of parameters should be 0 or 3");
		}
		if (parms.length == 0) {
			// Set area to 1 by default
			getParameter("area").setValue(1.0);
			// Set position to 0.0 by default
			getParameter("position").setValue(0.0);
			// Set sigma to 1.0 by default
			getParameter("gamma").setValue(1.0);
		}

		// Default set lower bounds on area and gamma to zero
		getParameter("area").setLowerLimit(0.0);
		getParameter("gamma").setLowerLimit(0.0);
	}

	/**
	 * @param minPeakPosition
	 * @param maxPeakPosition
	 * @param maxArea
	 * @param maxGamma
	 */
	public Lorentzian1D(double minPeakPosition, double maxPeakPosition, double maxArea, double maxGamma) {
		super(parameterNames);

		getParameter("area").setLowerLimit(0.0);
		getParameter("area").setUpperLimit(maxArea);
		getParameter("area").setValue(maxArea / 2.0);

		getParameter("gamma").setLowerLimit(0.0);
		getParameter("gamma").setUpperLimit(maxGamma);
		// better fitting is generaly found if sigma expands into the peak.
		getParameter("gamma").setValue(maxGamma / 10.0);

		getParameter("position").setLowerLimit(minPeakPosition);
		getParameter("position").setUpperLimit(maxPeakPosition);
		getParameter("position").setValue(minPeakPosition + ((maxPeakPosition - minPeakPosition) / 2.0));

	}

	@Override
	public double val(double... positions) {
		if (positions.length != 1)
			throw new IllegalArgumentException("The 1D Lorentzian can only be calculated at one position value");

		double y = 0.;
		double area = getParameter("area").getValue();
		double pos = getParameter("position").getValue();
		double gamma = getParameter("gamma").getValue();
		double norm = 0.5 * gamma * area / Math.PI;
		double arg = Math.pow((positions[0] - pos), 2.0) - Math.pow(0.5 * gamma, 2.0);
		y = norm / arg;
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
		output = output + "Gamma\t" + getParameter("gamma").getValue() + " [" + getParameter("gamma").getLowerLimit()
				+ "," + getParameter("gamma").getUpperLimit() + "]\n";

		return output;
	}

}
