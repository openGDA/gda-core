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

import gda.analysis.numerical.linefunction.IParameter;
import gda.analysis.numerical.linefunction.Parameter;
import gda.analysis.numerical.optimization.objectivefunction.AbstractObjectiveFunction;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import Jama.Matrix;

/**
 * KBObjectiveFunction Class
 */
public class KBObjectiveFunction extends AbstractObjectiveFunction {
	private double[][] mymatrix;

	private double[] myoffsets;

	IParameter[] par = new IParameter[8];

	/**
	 * @param x
	 * @param l
	 * @param u
	 */
	public KBObjectiveFunction(double[] x, double[] l, double[] u) {
		for (int i = 0; i < x.length; i++) {
			par[i] = new Parameter(x[i], l[i], u[i]);
		}
		mymatrix = readMatrix("/home/pq67/optdata.txt");
		myoffsets = readOffsets();
	}

	private static double[][] readMatrix(String filename) {
		double[][] data = new double[8][8];
		String datastring = null;
		Scanner sc = null;
		try {
			sc = new Scanner(new File(filename));
			int row = 0;
			while (sc.hasNextLine()) {
				datastring = sc.nextLine();
				Scanner s = new Scanner(datastring);
				for (int i = 0; i < 8; i++) {
					data[row][i] = s.nextDouble();
					System.out.println("row\t" + row + "\ti\t" + i + "\tdata\t" + data[row][i]);
				}
				s.close();
				row++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return data;
	}

	private double[] readOffsets() {
		return new double[] { 587.9515107041607, 591.8380894628535, 606.8724061125358, 615.337546813058,
				632.8785668564633, 671.299978015486, 688.0185859411977, 693.5960075902185 };
	}

	@Override
	public double evaluate(double... parameters) {
		for (int i = 0; i < 8; i++) {
			par[i].setValue(parameters[i]);
		}
		Matrix b = new Matrix(parameters, 8);
		Matrix offset = new Matrix(myoffsets, 8);
		Matrix mat = new Matrix(mymatrix);
		Matrix mat_result = mat.times(b).plus(offset);
		double mean = 0.0;
		for (int i = 0; i < 8; i++) {
			mean += mat_result.get(i, 0);
		}

		mean = mean / 8.0;
		double sum = 0.0;
		for (int i = 0; i < 8; i++) {
			sum += Math.pow((mat_result.get(i, 0) - mean), 2.0);
		}
		sum = sum / 8.0;
		sum = Math.sqrt(sum);
		return Math.pow((630 - mat_result.get(1, 0)), 2.0);

	}

	@Override
	public IParameter[] getParameters() {
		return par;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// double[] params = new double[] {310,10,110,390,60,260,260,260};
		// KBObjectiveFunction kb = new KBObjectiveFunction();
		// kb.evaluate(params);

	}
}
