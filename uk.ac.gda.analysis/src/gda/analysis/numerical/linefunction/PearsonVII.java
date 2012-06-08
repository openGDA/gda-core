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
 * PearsonVII Line Function Class
 */
public class PearsonVII extends AbstractFunction {
	// The parameter names
	private static String[] parameterNames = { "amplitude", "position", "fwhm", "s" };

	/**
	 * Constructor.
	 * 
	 * @param parms
	 */
	public PearsonVII(double... parms) {
		super(parameterNames);
		if (parms.length != 4 && parms.length != 0) {
			throw new IllegalArgumentException("No .of parameters should be 0 or 4");
		}
		if (parms.length == 0) {
			// Set area to 1 by default
			getParameter("amplitude").setValue(1.0);
			// Set position to 0.0 by default
			getParameter("position").setValue(0.0);
			// Set sigma to 1.0 by default
			getParameter("fwhm").setValue(1.0);
			// Set sigma to 1.0 by default
			getParameter("s").setValue(1.0);

		}

		// Default set lower bounds on area and gamma to zero
		getParameter("fwhm").setLowerLimit(0.0);
	}

	/**
	 * Purpose: Y(X;A) is Pearson VII. The amplitude. centre and widths of the Pearson VII are stored in consecutive
	 * locations of A: A(1) = H, A(2) = P, A(3) = FWHM, A(4) = S
	 * 
	 * @param positions
	 * @return pearson vii at x
	 */

	@Override
	public double val(double... positions) {
		if (positions.length != 1)
			throw new IllegalArgumentException("The 1D Lorentzian can only be calculated at one position value");

		double y = 0.;
		double amp = getParameter("amplitude").getValue();
		double pos = getParameter("position").getValue();
		double fwhm = getParameter("fwhm").getValue();
		double s = getParameter("s").getValue();
		double TINY = 1.0D - 10, HALF = 0.5, BIG = 1.0E03;

		double arg, ex, fac;

		if (fwhm < TINY) {
			fwhm = TINY;
		}
		if (s < HALF) {
			s = HALF;
		}
		if (s > BIG) {
			s = BIG;
		}
		arg = (positions[0] - pos) / fwhm;
		ex = Math.pow(2.0, (1.0 / s)) - 1.0;
		fac = (1.0 + 4.0 * arg * arg * ex);
		y = amp / Math.pow(fac, s);

		return y;
	}

}
