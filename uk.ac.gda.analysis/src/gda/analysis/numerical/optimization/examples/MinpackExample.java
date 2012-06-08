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
import gda.analysis.numerical.linefunction.BGAsymmetricGaussian1D;
import gda.analysis.numerical.linefunction.CompositeFunction;
import gda.analysis.numerical.optimization.objectivefunction.chisquared;
import gda.analysis.numerical.optimization.optimizers.leastsquares.minpackOptimizer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * MinpackExample Class
 */
public class MinpackExample {
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

		function.addFunction("Gaussian1", new BGAsymmetricGaussian1D(1.0, 1.0, 1.0, 1.0, 1.0));
		// Set the upper and lower bounds on the parameters
		function.getFunction("Gaussian1").getParameter("area").setLowerLimit(1.0E-12);
		function.getFunction("Gaussian1").getParameter("area").setUpperLimit(1.0E8);
		function.getFunction("Gaussian1").getParameter("position").setLowerLimit(9.37);
		function.getFunction("Gaussian1").getParameter("position").setUpperLimit(9.50);
		function.getFunction("Gaussian1").getParameter("sigma1").setLowerLimit(0.0);
		function.getFunction("Gaussian1").getParameter("sigma1").setUpperLimit(6.0);
		function.getFunction("Gaussian1").getParameter("sigma2").setLowerLimit(0.0);
		function.getFunction("Gaussian1").getParameter("sigma2").setUpperLimit(6.0);
		function.getFunction("Gaussian1").getParameter("background").setLowerLimit(-1.0E6);
		function.getFunction("Gaussian1").getParameter("background").setUpperLimit(1.0E6);

		// Make some fake to fit data.....
		DataVector xaxis = new DataVector();
		DataVector yaxis = new DataVector();
		// Create a data set with two gaussians
		// createDataSet2(xaxis, yaxis);
		readfile(xaxis, yaxis);
		// yaxis=Differentiate.standardDerivative(xaxis,yaxis,1);
		// for (int i = 0; i < yaxis.size(); i++) {
		// yaxis.set(i, yaxis.get(i)/50000.0);
		// }

		function.getFunction("Gaussian1").getParameter("background").setValue(1.0E-5);
		function.getFunction("Gaussian1").getParameter("area").setValue(10000.0 / 100.0);
		function.getFunction("Gaussian1").getParameter("position").setValue(9.43);
		function.getFunction("Gaussian1").getParameter("sigma1").setValue(0.003);
		function.getFunction("Gaussian1").getParameter("sigma2").setValue(0.003);

		// Cost function
		chisquared func = new chisquared(function, xaxis, yaxis);
		// Create instance of Minimisation
		minpackOptimizer min = new minpackOptimizer(func);
		// NelderMeadOptimizer min = new NelderMeadOptimizer(func);

		// Non-threaded
		min.optimize();

		System.out.println("Minimum\t" + min.getMinimum());
		System.out.println("Info\t" + min.getInfo());
		for (int i = 0; i < min.getBest().length; i++) {
			System.out.println("Minimum point\t" + i + "\t" + min.getBest()[i]);
		}
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("outtest.dat"));
			for (int i = 0; i < yaxis.size(); i++) {
				out.write(xaxis.get(i) + "\t" + yaxis.get(i) + "\t" + function.val(xaxis.get(i)) + "\n");
			}
			out.close();
		} catch (IOException e) {
		}

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

	/**
	 * @param xaxis
	 * @param yaxis
	 */
	public static void readfile(DataVector xaxis, DataVector yaxis) {
		Scanner sc = null;
		try {
			sc = new Scanner(new File("/home/pq67/testdata2.dat"));
			while (sc.hasNext()) {
				String line = sc.nextLine();
				Scanner s = new Scanner(line);
				xaxis.add(s.nextDouble());
				yaxis.add(s.nextDouble());
				System.out.println("x y\t" + xaxis.get(xaxis.size() - 1) + "\t" + yaxis.get(yaxis.size() - 1));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
