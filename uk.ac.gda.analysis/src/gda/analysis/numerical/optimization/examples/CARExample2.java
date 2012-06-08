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

import gda.analysis.datastructure.DataVector;
import gda.analysis.numerical.linefunction.CompositeFunction;
import gda.analysis.numerical.optimization.objectivefunction.chisquared;
import gda.analysis.numerical.optimization.optimizers.leastsquares.minpackOptimizer;

/**
 * C.A. Ramsdale Example of fitting an angulular distribution of photo ions produced from a gas phase sample in
 * incompletely polarized light to the Beta equation (below). i.e. fit equation to data I(q) = s/4p [b/4 (1 + 3P (cos
 * 2q))]
 */

/*
 * Need to create a function to be optimized
 */

public class CARExample2 {
	/**
	 * Just a method to create some fake data to fit
	 * 
	 * @return x,y data
	 */
	public static DataVector[] createFakeData() {
		CARLineExample CARLine = new CARLineExample();
		//
		// Set the parameters of the function
		//
		CARLine.getParameter("const").setValue(2.0);
		CARLine.getParameter("bigp").setValue(2.0);

		DataVector x = new DataVector();
		DataVector y = new DataVector();
		double startx = 0.0;
		double stepx = 0.01;
		int noOfPoints = 100;
		double currentx = startx, currenty = CARLine.val(startx);
		for (int i = 0; i < noOfPoints; i++) {
			x.add(currentx);
			y.add(currenty);
			currentx = currentx + stepx;
			currenty = CARLine.val(currentx);
			// System.out.println(x.get(i)+"\t"+y.get(i));

		}
		return new DataVector[] { x, y };

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// initialise your line function
		CARLineExample CARLine = new CARLineExample();
		for (int i = 0; i < CARLine.getNoOfParameters(); i++) {
			CARLine.getParameter(i).setLowerLimit(0.0);
			CARLine.getParameter(i).setUpperLimit(10.0);
		}
		// Often you want to fit more than one line to data
		// maybe two or three gaussians + background
		// The compositie function is a may of adding lots of functions
		// together to make a composite
		CompositeFunction comp = new CompositeFunction();
		// Adding your function to the composite
		comp.addFunction("CAR1", CARLine);
		// You can add more of your own line functions or one of the common ones
		// already
		// defined in the linefunction package
		DataVector[] data = createFakeData();
		// Lets create some fake data to fit
		// I use DataVectors to store data (basically vectors of type double
		// with
		// some
		// additional functions)

		// Now you need a function which has to be minimized.
		// The standard one is chi-squared
		chisquared mychi = new chisquared(comp, data);

		minpackOptimizer CARmin = new minpackOptimizer(mychi);
		// NelderMeadOptimizer CARmin = new NelderMeadOptimizer(mychi);
		// DEOptimizer CARmin = new DEOptimizer(mychi);

		// Fit the data
		CARmin.optimize();

		System.out.println("Minimum\t" + CARmin.getMinimum());

		for (int i = 0; i < CARmin.getBest().length; i++) {
			System.out.println("Minimum point\t" + i + "\t" + CARmin.getBest()[i]);
		}
	}

}
