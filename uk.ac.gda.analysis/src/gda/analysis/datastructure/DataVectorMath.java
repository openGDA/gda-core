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

package gda.analysis.datastructure;

import gda.analysis.utilities.ObjectCloner;

import java.util.Arrays;

/**
 * A collection of methods to before basic math operations on and with DataVectors.. with proper care given to errors.
 * This code is based on JAIDA but with alterations to suit our format..
 */

public class DataVectorMath {

	final double relPrec = 1E-10;

	// ----------------------------------------------------------------1D
	// case------------------
	/**
	 * Checks for compatability of two data sets if they're not the same size you can't add or delete them..etc..
	 * 
	 * @param g1
	 * @param g2
	 * @throws IllegalArgumentException
	 */
	private static void checkCompatibility(DataVector g1, DataVector g2) throws IllegalArgumentException {
		if (!(g2.getDimensions().length == g1.getDimensions().length))
			throw new IllegalArgumentException("Incompatible dimensions");
		if (!Arrays.equals(g1.getDimensions(), g2.getDimensions())) {
			throw new IllegalArgumentException("Incompatible dimensions");
		}
	}

	/**
	 * Adds two DataVectors
	 * 
	 * @param g1 :
	 *            dataset 1
	 * @param g2 :
	 *            dataset 2
	 * @return g1 + g2
	 * @throws IllegalArgumentException
	 *             if DataVectors binnings are incompatible
	 */
	public static DataVector sum(DataVector g1, DataVector g2) throws IllegalArgumentException {
		checkCompatibility(g1, g2);

		DataVector newData = clone(g1);

		for (int i = 0; i < g1.size(); i++) {
			double value = g1.getIndex(i) + g2.getIndex(i);
			newData.set(i, value);
		}
		return newData;
	}

	/**
	 * Adds two DataVectors
	 * 
	 * @param g :
	 *            dataset
	 * @param val :
	 *            value to be added
	 * @return g + val
	 */
	public static DataVector sum(DataVector g, double val) {

		DataVector newData = new DataVector(g.getDimensions());

		for (int i = 0; i < g.size(); i++) {
			double value = g.getIndex(i) + val;
			newData.set(i, value);
		}
		return newData;
	}

	/**
	 * Subtracts two 1D Histogram
	 * 
	 * @param g1 :
	 *            dataset 1
	 * @param g2 :
	 *            dataset 2
	 * @return g1 - g2
	 * @throws IllegalArgumentException
	 *             if DataVector dimensions are incompatible
	 */

	public static DataVector sub(DataVector g1, DataVector g2) {
		checkCompatibility(g1, g2);

		DataVector newData = clone(g1);

		for (int i = 0; i < g1.size(); i++) {
			double value = g1.getIndex(i) - g2.getIndex(i);
			newData.set(i, value);
		}
		return newData;
	}

	/**
	 * Subtracts two 1D Histogram
	 * 
	 * @param g :
	 *            dataset
	 * @param val :
	 *            value to be subtracted
	 * @return g - val
	 */

	public static DataVector sub(DataVector g, double val) {
		DataVector newData = new DataVector(g.getDimensions());

		for (int i = 0; i < g.size(); i++) {
			double value = g.getIndex(i) - val;
			newData.set(i, value);
		}
		return newData;
	}

	/**
	 * Multiplies two DataVectors
	 * 
	 * @param g1 :
	 *            dataset 1
	 * @param g2 :
	 *            dataset 2
	 * @return g1 * g2
	 * @throws IllegalArgumentException
	 *             if data vectors are incompatible
	 */

	public static DataVector mul(DataVector g1, DataVector g2) {
		checkCompatibility(g1, g2);

		DataVector newData = clone(g1);

		for (int i = 0; i < g1.size(); i++) {
			double value = g1.getIndex(i) * g2.getIndex(i);
			newData.set(i, value);
		}
		return newData;
	}

	/**
	 * Multiplies a DataVector by a constant
	 * 
	 * @param g :
	 *            dataset 1
	 * @param val :
	 *            value to multiply by
	 * @return g1 * g2
	 */

	public static DataVector mul(DataVector g, double val) {

		DataVector newData = new DataVector(g.getDimensions());

		for (int i = 0; i < g.size(); i++) {
			double value = g.getIndex(i) * val;
			newData.set(i, value);
		}
		return newData;
	}

	/**
	 * Divides two DataVectors
	 * 
	 * @param g1
	 *            First data vector
	 * @param g2
	 *            Second data vector
	 * @return g1 / g2
	 * @throws IllegalArgumentException
	 *             if datavectors are incompatible
	 */
	public static DataVector div(DataVector g1, DataVector g2) throws IllegalArgumentException {
		checkCompatibility(g1, g2);

		DataVector newData = clone(g1);

		for (int i = 0; i < g1.size(); i++) {
			double value = g1.getIndex(i) / g2.getIndex(i);
			newData.set(i, value);
		}
		return newData;
	}

	/**
	 * Divides a DataVector by a number
	 * 
	 * @param g
	 * @param val
	 * @return g/val
	 */
	public static DataVector div(DataVector g, double val) {

		DataVector newData = new DataVector(g.getDimensions());

		for (int i = 0; i < g.size(); i++) {
			double value = g.getIndex(i) / val;
			newData.set(i, value);
		}
		return newData;
	}

	/**
	 * log 10 of a data vector
	 * 
	 * @param g
	 * @return log10 of a data vector
	 */
	public static DataVector log10(DataVector g) {

		if (g.getMin() <= 0) {
			throw new IllegalArgumentException("Cannot log elements of the array are less than 0");
		}

		DataVector newData = new DataVector(g.getDimensions());

		for (int i = 0; i < g.size(); i++) {
			double value = Math.log10(g.getIndex(i));
			newData.set(i, value);
		}
		return newData;
	}

	/**
	 * @param g
	 * @return log10 of a data vector
	 */
	public static DataVector ln(DataVector g) {

		if (g.getMin() <= 0) {
			throw new IllegalArgumentException("Cannot ln the data vector as there are elements less than 0");
		}

		DataVector newData = new DataVector(g.getDimensions());

		for (int i = 0; i < g.size(); i++) {
			double value = Math.log(g.getIndex(i));
			newData.set(i, value);
		}
		return newData;
	}

	/**
	 * @param g
	 * @param pow
	 *            value to raise g by
	 * @return data vector to the power of pow
	 */
	public static DataVector pow(DataVector g, double pow) {

		DataVector newData = new DataVector(g.getDimensions());

		for (int i = 0; i < g.size(); i++) {
			double value = Math.pow(g.getIndex(i), pow);
			newData.set(i, value);
		}
		return newData;
	}

	/**
	 * @param g
	 * @return data vector with absolute values of g
	 */
	public static DataVector abs(DataVector g) {

		DataVector newData = new DataVector(g.getDimensions());

		for (int i = 0; i < g.size(); i++) {
			double value = Math.abs(g.getIndex(i));
			newData.set(i, value);
		}
		return newData;
	}

	/**
	 * @param g
	 * @return cos of data vector
	 */
	public static DataVector cos(DataVector g) {

		DataVector newData = new DataVector(g.getDimensions());

		for (int i = 0; i < g.size(); i++) {
			double value = Math.cos(g.getIndex(i));
			newData.set(i, value);
		}
		return newData;
	}

	/**
	 * @param g
	 * @return sin of data vector
	 */
	public static DataVector sin(DataVector g) {

		DataVector newData = new DataVector(g.getDimensions());

		for (int i = 0; i < g.size(); i++) {
			double value = Math.sin(g.getIndex(i));
			newData.set(i, value);
		}
		return newData;
	}

	/**
	 * @param g
	 * @return exp of data vector
	 */
	public static DataVector exp(DataVector g) {

		DataVector newData = new DataVector(g.getDimensions());

		for (int i = 0; i < g.size(); i++) {
			double value = Math.exp(g.getIndex(i));
			newData.set(i, value);
		}
		return newData;
	}

	/**
	 * @param g
	 * @return The max of the data set
	 */
	public static double getMin(DataVector g) {
		double min = Double.MAX_VALUE;
		for (Double x : g) {
			min = Math.min(x, min);
		}
		return min;
	}

	/**
	 * @param g
	 * @return The max of the data set g
	 */
	public static double getMax(DataVector g) {
		double max = Double.MIN_VALUE;
		for (Double x : g) {
			max = Math.max(x, max);
		}
		return max;
	}

	/**
	 * @param g
	 * @return The max of the data set g
	 */
	public static double getMean(DataVector g) {
		double sum = 0.0;
		for (Double x : g) {
			sum += x;
		}
		return sum / (g.size());
	}

	/**
	 * @param g
	 * @return The rms of the data set
	 */
	public static double getStandardDeviation(DataVector g) {
		double sum = 0.0;
		for (Double x : g) {
			sum += x * x;
		}
		return Math.sqrt(sum / (g.size()));
	}

	/**
	 * @param g
	 * @return The rms of the data set
	 */
	public static double getAverageDeviation(DataVector g) {
		double mean = getMean(g);
		double sum = 0.0;
		for (Double x : g) {
			sum += Math.abs(x - mean);
		}
		return sum / g.size();
	}

	/**
	 * @param g
	 * @return kurtosis of a 1D data set
	 */
	public static double kurtosis(DataVector g) {

		double mean = getMean(g);
		double rms = getStandardDeviation(g);
		double sum = 0.0;
		for (Double x : g) {
			sum += Math.pow(((x - mean) / rms), 4.0);
		}
		return ((sum / g.size()) - 3);
	}

	/**
	 * @param g
	 * @return skewness of a 1D data set
	 */
	public static double skew(DataVector g) {

		double mean = getMean(g);
		double rms = getStandardDeviation(g);
		double sum = 0.0;
		for (Double x : g) {
			sum += Math.pow(((x - mean) / rms), 3.0);
		}
		return sum / g.size();
	}

	/**
	 * @param g
	 * @return sum of all elements in DataVector g
	 */
	public static double count(DataVector g) {
		double sum = 0.0;
		for (int i = 0; i < g.size(); i++) {
			sum += g.getIndex(i);
		}
		return sum;
	}

	/**
	 * @param g
	 * @param start
	 * @param end
	 * @return summation of all elements between start and end indices in DataVector g
	 */
	public static double count(DataVector g, int start, int end) {
		double sum = 0.0;
		for (int i = start; i < end; i++) {
			sum += g.getIndex(i);
		}
		return sum;
	}

	/**
	 * @param g
	 * @return A new DataVector that is a clone of DataVector g
	 */
	public static DataVector clone(DataVector g) {
		DataVector newData = null;
		try {
			newData = (DataVector) ObjectCloner.deepCopy(g);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newData;
	}

	/**
	 * @param g
	 * @param value
	 * @return finds the value of nearest element value in array to the argument value
	 */
	public static double nearestElementValue(DataVector g, double value) {
		double diff = Math.abs(g.get(0) - value);
		double nearest = g.get(0);
		for (int i = 1; i < g.size(); i++) {
			if (Math.abs(g.get(i) - value) < diff) {
				diff = Math.abs(g.get(i) - value);
				nearest = g.get(i);
			}
		}
		return nearest;
	}

	/**
	 * @param g
	 * @param value
	 * @return finds the index of nearest element value in array to the argument value
	 */
	public static int nearestElementIndex(DataVector g, double value) {
		double diff = Math.abs(g.get(0) - value);
		int nearest = 0;
		for (int i = 1; i < g.size(); i++) {
			if (Math.abs(g.get(i) - value) < diff) {
				diff = Math.abs(g.get(i) - value);
				nearest = i;
			}
		}
		return nearest;
	}

	/**
	 * @param g
	 * @param value
	 * @return finds the index of nearest lower element value in DataVector g to the argument value
	 */
	public static int nearestLowerElementIndex(DataVector g, double value) {
		double diff0 = 0.0D;
		double diff1 = 0.0D;
		int nearest = 0;
		int ii = 0;
		boolean test = true;
		double min = g.get(0);
		int minI = 0;
		while (test) {
			if (g.get(ii) < min) {
				min = g.get(ii);
				minI = ii;
			}
			if ((value - g.get(ii)) >= 0.0) {
				diff0 = value - g.get(ii);
				nearest = ii;
				test = false;
			} else {
				ii++;
				if (ii > g.size() - 1) {
					nearest = minI;
					diff0 = min - value;
					test = false;
				}
			}
		}
		for (int i = 0; i < g.size(); i++) {
			diff1 = value - g.get(i);
			if (diff1 >= 0.0 && diff1 < diff0) {
				diff0 = diff1;
				nearest = i;
			}
		}
		return nearest;
	}

	/**
	 * @param g
	 * @param value
	 * @return the value of nearest lower element value in the DataVector g to the argument value
	 */
	public static double nearestLowerElementValue(DataVector g, double value) {
		double diff0 = 0.0D;
		double diff1 = 0.0D;
		double nearest = 0.0D;
		int ii = 0;
		boolean test = true;
		double min = g.get(0);
		while (test) {
			if (g.get(ii) < min)
				min = g.get(ii);
			if ((value - g.get(ii)) >= 0.0D) {
				diff0 = value - g.get(ii);
				nearest = g.get(ii);
				test = false;
			} else {
				ii++;
				if (ii > g.size() - 1) {
					nearest = min;
					diff0 = min - value;
					test = false;
				}
			}
		}
		for (int i = 0; i < g.size(); i++) {
			diff1 = value - g.get(i);
			if (diff1 >= 0.0D && diff1 < diff0) {
				diff0 = diff1;
				nearest = g.get(i);
			}
		}
		return nearest;
	}

	/**
	 * @param g
	 * @param value
	 * @return finds the index of nearest higher element value in DataVector g to the argument value
	 */
	public static int nearestHigherElementIndex(DataVector g, double value) {
		double diff0 = 0.0D;
		double diff1 = 0.0D;
		int nearest = 0;
		int ii = 0;
		boolean test = true;
		double max = g.get(0);
		int maxI = 0;
		while (test) {
			if (g.get(ii) > max) {
				max = g.get(ii);
				maxI = ii;
			}
			if ((g.get(ii) - value) >= 0.0D) {
				diff0 = value - g.get(ii);
				nearest = ii;
				test = false;
			} else {
				ii++;
				if (ii > g.size() - 1) {
					nearest = maxI;
					diff0 = value - max;
					test = false;
				}
			}
		}
		for (int i = 0; i < g.size(); i++) {
			diff1 = g.get(i) - value;
			if (diff1 >= 0.0D && diff1 < diff0) {
				diff0 = diff1;
				nearest = i;
			}
		}
		return nearest;
	}

	/**
	 * @param g
	 * @param value
	 * @return finds the value of nearest higher element value in DataVector g to the argument value
	 */
	public static double nearestHigherElementValue(DataVector g, double value) {
		double diff0 = 0.0D;
		double diff1 = 0.0D;
		double nearest = 0.0D;
		int ii = 0;
		boolean test = true;
		double max = g.get(0);
		while (test) {
			if (g.get(ii) > max)
				max = g.get(ii);
			if ((g.get(ii) - value) >= 0.0D) {
				diff0 = value - g.get(ii);
				nearest = g.get(ii);
				test = false;
			} else {
				ii++;
				if (ii > g.size() - 1) {
					nearest = max;
					diff0 = value - max;
					test = false;
				}
			}
		}
		for (int i = 0; i < g.size(); i++) {
			diff1 = g.get(i) - value;
			if (diff1 >= 0.0D && diff1 < diff0) {
				diff0 = diff1;
				nearest = g.get(i);
			}
		}
		return nearest;
	}

	/**
	 * NOT IMPLEMENTED
	 * 
	 * @param x
	 * @param y
	 * @return 0.0
	 */
	public static double centroid(@SuppressWarnings("unused") DataVector x, @SuppressWarnings("unused") DataVector y) {
		return 0.0;
	}

	/**
	 * @param x
	 * @param y
	 * @param z
	 * @return double[]
	 */
	public static double[] centroid(@SuppressWarnings("unused") DataVector x, @SuppressWarnings("unused") DataVector y, @SuppressWarnings("unused") DataVector z) {
		return null;
	}

	/**
	 * @param g
	 * @return null
	 */
	public static double[] centroid(@SuppressWarnings("unused") DataVector g) {
		return null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DataVector g1 = new DataVector(10);
		DataVector g2 = new DataVector(10);
		for (int i = 0; i < g1.size(); i++) {
			g1.set(i, 1. * i);
			g2.set(i, 1. * i);
		}
		System.out.println("sizes\t" + g1.size() + "\t" + g2.size());
		System.out.println("dimensions\t" + g1.getDimensions()[0] + "\t" + g2.getDimensions()[0]);
		DataVector pdq = DataVectorMath.sub(g1, g2);
		for (int i = 0; i < pdq.size(); i++) {
			System.out.println("pdq\t" + pdq.getIndex(i));
		}

	}

}
