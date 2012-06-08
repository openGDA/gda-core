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

package gda.analysis.numerical.optimization.examples;

import gda.analysis.numerical.linefunction.AbstractFunction;

/**
 * Need to define a line function to fit to the data
 */
public class CARLineExample extends AbstractFunction {
	// The parameter you want to fit to the data
	// I(q) = s/4p [b/4 (1 + 3P (cos 2q))]
	// | | |
	//
	// I don't understand this functions
	//  
	// s/4p*b/4 is the same as c/16 where c = sb/p
	//
	// you can't fit s,p and b because you can have
	// infinite combinations that of s b and p such that sb/p = some value
	//
	// Anyway....I've rearranged it to
	//
	// I(q) = c/16 (1 + 3P (cos 2q))
	//
	private static String[] parameterNames = { "const", "bigp" };

	/**
	 * Initialise the Angular distribution function. To set up the function you can specify with no parameters e.g.
	 * CARLineExample = new CARLineExample() will set up a function with some default values (1.0) or e.g.
	 * CARLineExample = new CARLineExample(1.0,1.0) to set the initial values
	 * 
	 * @param parms
	 *            The parameters
	 */
	public CARLineExample(double... parms) {
		// Sets up function to have parameternames
		super(parameterNames);
		// Can have 0 or noOfParameters parameters .. i.e. you specify them all
		// or none at all
		if (parms.length != this.getNoOfParameters() && parms.length != 0) {
			throw new IllegalArgumentException("No .of parameters should be 0 or" + this.getNoOfParameters());
		}
		// if no parameters are specified set some defaults
		if (parms.length == 0) {
			// parameters are stored in an ordered hash map so they
			// can be referenced by index or name
			getParameter("const").setValue(1.0);
			getParameter("bigp").setValue(1.0);

		}
		// otherwise set the values
		else {
			for (int i = 0; i < parms.length; i++) {
				getParameter(i).setValue(parms[i]);
			}
		}
		//
		//
		// Set any default lower or upper limits here
		// +/- infinity (well Double.MAX) by default
		// but if for example a value can only be
		// from >= 0.0 set it here

		// getParameter("Littlep").setLowerLimit(0.0);
		// getParameter("BigP").setLowerLimit(0.0);
	}

	/**
	 * {@inheritDoc} This calculates I(q) assuming q is in radians
	 * 
	 * @see gda.analysis.numerical.linefunction.AbstractFunction#val(double[])
	 */
	@Override
	public double val(double... positions) {
		// generic ... to allow a range of parameters
		if (positions.length != 1) {
			throw new IllegalArgumentException("The function CARLine can only be calculated at one position value");
		}
		double y = 0.;
		// Find out
		double c = getParameter("const").getValue();
		double BigP = getParameter("bigp").getValue();
		y = (c / 16.0) * (1.0 + 3.0 * BigP * Math.cos(2.0 * positions[0]));
		return y;
	}

}
