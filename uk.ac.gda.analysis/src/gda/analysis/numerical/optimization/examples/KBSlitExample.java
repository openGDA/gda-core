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
import gda.analysis.numerical.linefunction.AsymmetricGaussian1D;
import gda.analysis.numerical.linefunction.CompositeFunction;
import gda.analysis.numerical.linefunction.Polynomial;
import gda.analysis.numerical.optimization.objectivefunction.chisquared;
import gda.analysis.numerical.optimization.optimizers.filtering.iffco;

/**
 * KBSlitExample Class
 */
public class KBSlitExample {

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
		AsymmetricGaussian1D gauss = new AsymmetricGaussian1D(2.0, 3.5, 2.0, 2.0);
		function.addFunction("Gaussian", gauss);
		// Set the upper and lower bounds on the parameters
		function.getFunction("Gaussian").getParameter("area").setLowerLimit(0.0001);
		function.getFunction("Gaussian").getParameter("area").setUpperLimit(6.0);
		function.getFunction("Gaussian").getParameter("position").setLowerLimit(0.0001);
		function.getFunction("Gaussian").getParameter("position").setUpperLimit(6.0);
		function.getFunction("Gaussian").getParameter("sigma1").setLowerLimit(0.0001);
		function.getFunction("Gaussian").getParameter("sigma1").setUpperLimit(6.0);

		function.getFunction("Gaussian").getParameter("sigma2").setLowerLimit(0.0001);
		function.getFunction("Gaussian").getParameter("sigma2").setUpperLimit(6.0);
		Polynomial poly = new Polynomial(0.1, 1.0E-4, 1.0E-8);
		function.addFunction("Polynomial", poly);
		// Set the upper and lower bounds on the parameters
		function.getFunction("Polynomial").getParameter(0).setLowerLimit(1E-8);
		function.getFunction("Polynomial").getParameter(0).setUpperLimit(0.5);
		function.getFunction("Polynomial").getParameter(1).setLowerLimit(-1.0);
		function.getFunction("Polynomial").getParameter(1).setUpperLimit(1.0);
		function.getFunction("Polynomial").getParameter(2).setLowerLimit(-1.0);
		function.getFunction("Polynomial").getParameter(2).setUpperLimit(1.0);

		// Make some fake to fit data.....
		DataVector xaxis = new DataVector();
		DataVector yaxis = new DataVector();
		// Create a data set with two gaussians
		createDataSet1(xaxis, yaxis);

		// Cost function
		chisquared func = new chisquared(function, xaxis, yaxis);
		// Create instance of Minimisation
		iffco min = new iffco(func);
		// NelderMeadOptimizer min = new NelderMeadOptimizer(func);

		// minpackOptimizer min = new minpackOptimizer(func);

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
