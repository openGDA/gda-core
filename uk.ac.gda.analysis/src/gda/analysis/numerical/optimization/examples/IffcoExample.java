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
import gda.analysis.numerical.linefunction.AbstractCompositeFunction;
import gda.analysis.numerical.linefunction.CompositeFunction;
import gda.analysis.numerical.linefunction.Gaussian1D;
import gda.analysis.numerical.optimization.objectivefunction.chisquared;
import gda.analysis.numerical.optimization.optimizers.simplex.NelderMeadOptimizer;

// import
// gda.analysis.numerical.optimization.optimizers.differentialevolution.DEOptimizer;
// import gda.analysis.numerical.optimization.optimizers.filtering.iffco;

/**
 * IffcoExample Class
 */
public class IffcoExample {
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		AbstractCompositeFunction function = new CompositeFunction();
		//
		// Creating a composite function which is the sum onf two gaussians.
		// I then define lower and upper bounds on the parameters.
		// Defining lower and upper limits often makes the code run a bit better
		// and in the
		// 

		function.addFunction("Gaussian1", new Gaussian1D(3.0, 3.0, 3.0));
		// Set the upper and lower bounds on the parameters
		function.getFunction("Gaussian1").getParameter("area").setLowerLimit(0.0001);
		function.getFunction("Gaussian1").getParameter("area").setUpperLimit(6.0);
		function.getFunction("Gaussian1").getParameter("position").setLowerLimit(0.0001);
		function.getFunction("Gaussian1").getParameter("position").setUpperLimit(6.0);
		function.getFunction("Gaussian1").getParameter("sigma").setLowerLimit(0.0001);
		function.getFunction("Gaussian1").getParameter("sigma").setUpperLimit(6.0);

		function.addFunction("Gaussian2", new Gaussian1D(3.0, 3.0, 3.0));
		// Set the upper and lower bounds on the parameters
		function.getFunction("Gaussian2").getParameter("area").setLowerLimit(0.0001);
		function.getFunction("Gaussian2").getParameter("area").setUpperLimit(6.0);
		function.getFunction("Gaussian2").getParameter("position").setLowerLimit(0.0001);
		function.getFunction("Gaussian2").getParameter("position").setUpperLimit(6.0);
		function.getFunction("Gaussian2").getParameter("sigma").setLowerLimit(0.0001);
		function.getFunction("Gaussian2").getParameter("sigma").setUpperLimit(6.0);

		// Make some fake to fit data.....
		DataVector xaxis = new DataVector();
		DataVector yaxis = new DataVector();
		// Create a data set with two gaussians
		createDataSet2(xaxis, yaxis);

		// Cost function
		chisquared func = new chisquared(function, xaxis, yaxis);
		// Create instance of Minimisation
		// iffco min = new iffco(func);
		NelderMeadOptimizer min = new NelderMeadOptimizer(func);
		min.setStepOption(2);
		min.setSteps(new double[] { 1.0, 1.0, 1.0, 1.0, 1.0, 1.0 });
		min.reset();
		// DEOptimizer min = new DEOptimizer(func);
		// Non-threaded
		min.optimize();

		System.out.println("Minimum\t" + min.getMinimum());
		for (int i = 0; i < min.getBest().length; i++) {
			System.out.println("Minimum point\t" + i + "\t" + min.getBest()[i]);
		}
		// 
		// // change the data set to one a single gaussian
		// createDataSet1(xaxis, yaxis);
		// // Remove a gaussian from our function
		// //function.removeFunction("Gaussian2");
		// // Reset the optimizer..because we've changed the function
		// //min.reset();
		// chisquared func = new chisquared(function, xaxis, yaxis);
		// iffco min = new iffco(func);
		// // Non-threaded
		// min.optimize();
		//
		// System.out.println("Minimum\t" + min.getMinimum());
		// for (int i = 0; i < min.getBest().length; i++)
		// {
		// System.out.println("Minimum point\t" + i + "\t" + min.getBest()[i]);
		// }

	}

	/**
	 * @param xaxis
	 * @param yaxis
	 */
	public static void createDataSet1(DataVector xaxis, DataVector yaxis) {
		double x = -3.0;
		double step = 10.0 / 500.0;
		//
		double area1 = 2.0;
		double position1 = 1.75;
		double sigma1 = 0.5;

		for (int i = 0; i < 500; i++) {
			double y = 0.0;
			xaxis.add(x);
			double norm = Math.sqrt(2.0 * Math.PI * sigma1 * sigma1);
			double arg = (x - position1) / sigma1;
			double ex = Math.exp(-0.5 * arg * arg);
			y += (area1 * ex / norm);

			yaxis.add(y);
			x += step;
		}
	}

	/**
	 * @param xaxis
	 * @param yaxis
	 */
	public static void createDataSet2(DataVector xaxis, DataVector yaxis) {
		double x = -3.0;
		double step = 10.0 / 500.0;
		// data 1
		double area1 = 2.0;
		double position1 = 0.75;
		double sigma1 = 0.5;

		for (int i = 0; i < 500; i++) {
			double y = 0.0;
			xaxis.add(x);
			double norm = Math.sqrt(2.0 * Math.PI * sigma1 * sigma1);
			double arg = (x - position1) / sigma1;
			double ex = Math.exp(-0.5 * arg * arg);
			y += (area1 * ex / norm);
			yaxis.add(y);
			x += step;
		}
		// data 2
		double area2 = 1.0;

		double position2 = 3.55;
		double sigma2 = 0.65;

		x = -3.0;
		for (int i = 0; i < 500; i++) {
			double y = 0.0;
			double norm = Math.sqrt(2.0 * Math.PI * sigma2 * sigma2);
			double arg = (x - position2) / sigma2;
			double ex = Math.exp(-0.5 * arg * arg);
			y += (area2 * ex / norm);
			double ytemp = y + yaxis.getIndex(i);
			yaxis.set(i, ytemp);
			x += step;
		}
	}

}
