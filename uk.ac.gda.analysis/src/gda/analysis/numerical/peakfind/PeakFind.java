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

package gda.analysis.numerical.peakfind;

// import gda.analysis.datastructure.DataVector;
import gda.analysis.datastructure.DataVector;
import gda.analysis.numerical.linefunction.Gaussian1D;
import gda.analysis.numerical.statistical.SavitzkyGolay;
import gda.analysis.numerical.utilities.Utility;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

/**
 * Peak Finding class.
 */
public class PeakFind {

	private static int err = 2;

	private static double gamma = 1.0;

	/**
	 * Mariscotti peak search algorithm... implemented based on original paper "A method for automatic identification of
	 * peaks" Nucl. Instruments and Methods 1967 (50) page 309-320 This is very slow but its worth looking at to
	 * understand the paper....
	 * 
	 * @param x
	 * @param y
	 * @param g
	 * @param gamma
	 * @return An int list of the index of the peak positions
	 */
	/*
	 * public static double[] mariscotti(DataVector x, DataVector y, double gamma) { Vector<Integer> noOfPeaks = new
	 * Vector<Integer>(); int i1 = 1, i2 = 1, i3 = 1, i5 = 1; double w = 0.6; // compute smoothed second derivative and
	 * standard deviation of the // derivative // convert gamma to a no of integer steps int window = (int) (y.size()*(w *
	 * gamma)/( DataVectorMath.getMax(x) - DataVectorMath.getMin(x) )); PeakFind.gamma = window; // must be at least 2
	 * window = Math.max(window, 2); // make sure its odd if ((window % 2) == 0) { window = window + 1; } int step =
	 * (window - 1) / 2; double cij = 0.0, fij = 0.0; // Compute s(z,w) int count = 0; // compute f(z,w) //step=2;
	 * double[] secondDer = new double[y.size() - 10 * step]; double[] standardDev = new double[y.size() - 10 * step];
	 * for (int i = step*5; i < y.size() - step*5; i++) { cij=0.0; fij=0.0; count =0; // z = 5 ... five smoothed
	 * derivative iterations // Could I replace this with sg smooth ? for (int j = i - step; j <=i + step; j++) { for
	 * (int k = j - step; k <=j + step; k++) { for (int l = k - step; l <=k + step; l++) { for (int m = l - step; m <=l +
	 * step; m++) { for (int n = m - step; n <=m + step; n++) { double temp = getCIJCentral(n,m ); double value =
	 * y.get(n); cij = cij+(temp * value); fij = fij+temp*temp*value; count += temp*temp; } } } } } // set second
	 * derivative secondDer[i - step*5] = cij; // set standard deviation // normalization not in original paper but it
	 * doesn't // work or make sense without it.... standardDev[i - step*5] = (Math.sqrt(fij)/(1.0*count)); } try {
	 * BufferedWriter out = new BufferedWriter(new FileWriter("outtest_old.dat")); for (int i = 0; i < secondDer.length;
	 * i++) { out.write(i+ "\t" + y.getData(i)+"\t"+secondDer[i] + "\t" + standardDev[i]+"\n"); } out.close(); } catch
	 * (IOException e) { } // So now we've got the standard deviation and smoothed second derivative int m = 1; for (int
	 * i = 1; i < secondDer.length; i++) { if (secondDer[i] > standardDev[i]) { m = 1; } else { if (secondDer[i] > 0) {
	 * m = 2; } else { m = 3; } } if (secondDer[i - 1] > standardDev[i - 1]) { if (m == 3 || m ==2 ) { i3 = i; i2 = i -
	 * 1; } } else { if (secondDer[i - 1] > 0) { if (m == 3) { i3 = i; } else if (m == 1) { i1 = i; } } else { if (m ==
	 * 1 || m == 2) { i5 = i - 1; // check conditions conditionsMet(i1, i2, i3, i5, secondDer, standardDev, noOfPeaks); } } } }
	 * double[] result = new double[noOfPeaks.size()]; for(int i=0;i<noOfPeaks.size();i++) { result[i] =
	 * x.getData(noOfPeaks.get(i)+step*5); } return result; }
	 */
	/**
	 * Mariscotti peak search algorithm... implemented based on original paper "A method for automatic identification of
	 * peaks" Nucl. Instruments and Methods 1967 (50) page 309-320 So I updated the algorithm to use the Savitky Golay
	 * smoothed second derivative The algorithm calculates the second derivative and compares this to the standard
	 * deviation of the second derivative. If the second derivative isn't >2 times the standard deviation then its not a
	 * peak. There are other bounding checks as well. The problem is just what is the standard deviation ? In some cases
	 * it is Sqrt(signal) (which is what is assumed in the original paper) but in others it won't. So I introduced a
	 * figure called noiselevel. standard dev = Math.sqrt (Sum of the filter coeffs * noiselevel * y(i)) Try values <
	 * 1e-4 to start with. Overall the difficulty will be defining this level.
	 * 
	 * @param x
	 * @param y
	 * @param noiselevel
	 * @return An int list of the index of the peak positions
	 */
	public static double[] mariscotti(DataVector x, DataVector y, double noiselevel) {

		Vector<Integer> noOfPeaks = new Vector<Integer>();
		int i1 = 1, i2 = 1, i3 = 1, i5 = 1;
		double[] secondDer = new double[y.size()];
		double[] standardDev = new double[y.size()];

		double f = 0.0, factor = 2.0;
		int nLeft = 0;
		int nRight = 0;
		int nLeftIndex = 0;
		boolean repeat = false;
		int noOfSGPoints = 7;
		double[] coeffs = new double[noOfSGPoints];
		int noOfPoints = noOfSGPoints;

		int centre = (noOfPoints - 1 / 2);
		for (int i = 0; i < y.size(); i++) {
			if (i < centre) {
				nLeftIndex = i;
				nLeft = i;
				nRight = noOfPoints - nLeft - 1;
				repeat = true;
			} else if (i > y.size() - 1 - centre) {
				nRight = y.size() - 1 - i;
				nLeft = noOfPoints - 1 - nRight;
				repeat = true;
			} else {
				nLeft = (noOfPoints - 1) / 2;
				nRight = (noOfPoints - 1) / 2;
				if (i == centre) {
					repeat = true;
				} else {
					repeat = false;
				}
			}
			if (repeat) {
				SavitzkyGolay.savgol(coeffs, noOfPoints, nLeft, nRight, 2, 4);
			}

			nLeftIndex = i - nLeft;
			f = 0.0;
			double sd = 0.0;
			for (int j = 0; j < noOfPoints; j++) {
				f += factor * coeffs[j] * y.get(nLeftIndex + j);
				sd += (factor * coeffs[j]) * (factor * coeffs[j]);
			}
			secondDer[i] = f;
			standardDev[i] = Math.sqrt(sd) * Math.abs(noiselevel * y.get(i));
		}

		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("outtest_pdq.dat"));
			for (int i = 0; i < secondDer.length; i++) {
				out.write(i + "\t" + y.getIndex(i) + "\t" + secondDer[i] + "\t" + standardDev[i] + "\n");
			}
			out.close();
		} catch (IOException e) {
		}
		// So now we've got the standard deviation and smoothed second
		// derivative
		int m = 1;

		for (int i = 1; i < secondDer.length; i++) {
			if (secondDer[i] > standardDev[i]) {
				m = 1;
			} else {
				if (secondDer[i] > 0) {
					m = 2;
				} else {
					m = 3;
				}
			}
			if (secondDer[i - 1] > standardDev[i - 1]) {
				if (m == 3 || m == 2) {
					i3 = i;
					i2 = i - 1;
				}
			} else {
				if (secondDer[i - 1] > 0) {
					if (m == 3) {
						i3 = i;
					} else if (m == 1) {
						i1 = i;
					}
				} else {
					if (m == 1 || m == 2) {
						i5 = i - 1;
						// check conditions
						conditionsMet(i1, i2, i3, i5, secondDer, standardDev, noOfPeaks);
					}
				}

			}

		}
		double[] result = new double[noOfPeaks.size()];
		for (int i = 0; i < noOfPeaks.size(); i++) {
			result[i] = x.getIndex(noOfPeaks.get(i));
		}
		return result;
	}

	private static void conditionsMet(int i1, int i2, int i3, int i5, double[] s, double[] f, Vector<Integer> peaks) {
		// n1
		int n1_min = (int) (1.22 * PeakFind.gamma + 0.5) - PeakFind.err;
		int n1_max = (int) (1.22 * PeakFind.gamma + 0.5) + PeakFind.err;

		int count = 0;
		// i4 lies between i3 and i5 and is the minimum point.

		int i4 = 1;
		if (i5 > i3) {
			double[] section = Utility.section(s, i3, i5);
			i4 = i3 + Utility.nearestElementIndex(section, Utility.minimum(section));
		} else {
			i4 = i5;
		}

		// calculate i4

		// eqn 14 in paper

		if (Math.abs(s[i4]) > 2.0 * f[i4]) {
			count++;
		}
		// eqn 19

		if (i5 - i3 + 1 >= n1_min || i5 - i3 + 1 < +n1_max) {
			count++;
		}
		double ratio = Math.abs(f[i4] / s[i4]);
		// eqn 21
		// n2
		int n2_min = (int) Math.abs((ratio * 0.5 * n1_min + 0.5));
		int n2_max = (int) Math.round(ratio * 0.5 * n1_max + 0.5);
		if (n2_min >= 1 || n2_max >= 1) {
			if ((i3 - i2 - 1) <= n2_min || (i3 - i2 - 1) <= n2_max) {
				count++;
			}
		} else if (n2_min == 0 || n2_max == 0) {
			if ((i3 - i2 - 1) <= 1) {
				count++;
			}
		}
		int n3_min = (int) (n1_min * (1.0 - 2.0 * ratio) + 0.2);
		int n3_max = (int) (n1_min * (1.0 - 2.0 * ratio) + 0.2);

		if (i2 - i1 + 1 >= n3_min || i2 - i1 + 1 >= n3_max) {
			count++;
		}
		if (count == 4) {
			peaks.add(i4);
			System.out.println("i values\t" + "\t" + i1 + "\t" + i2 + "\t" + i3 + "\t" + i4 + "\t" + i5);
		}
	}

	/**
	 * Main method used for testing.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		Gaussian1D g1d = new Gaussian1D(5.0, 10.0, 1.0);
		Gaussian1D g2d = new Gaussian1D(2.0, 12.0, 1.0);
		Gaussian1D g3d = new Gaussian1D(1.0, 15.0, 1.0);
		double x = 0.0;
		double range = 20.0;
		int size = 500;
		double step = range / size;
		DataVector g1 = new DataVector(size);
		DataVector x1 = new DataVector(size);
		for (int i = 0; i < size; i++) {
			x1.set(i, x);
			g1.set(i, 100.0 * (g1d.val(x) + g2d.val(x) + g3d.val(x)));
			x += step;
		}
		double[] res = mariscotti(x1, g1, 1.0E-4);
		System.out.println("No of peaks\t" + res.length);
		for (int i = 0; i < res.length; i++) {
			System.out.println("res\t" + res[i]);
		}

	}

}
