/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.analysis.utils;

import gda.analysis.DataSet;

/**
 * Class to hold methods to create random datasets
 * 
 * Emulates numpy.random
 * 
 * @deprecated Use {@link uk.ac.diamond.scisoft.analysis.dataset.Random} with new generic datasets
 */
@Deprecated
public class Random {
	public static void seed(final int seed) {
		uk.ac.diamond.scisoft.analysis.dataset.Random.seed(seed);
	}

	public static void seed(final int[] seed) {
		uk.ac.diamond.scisoft.analysis.dataset.Random.seed(seed);
	}

	public static void seed(final long seed) {
		uk.ac.diamond.scisoft.analysis.dataset.Random.seed(seed);
	}

	/**
	 * @param size
	 * @return an array of values sampled from a uniform distribution between 0 and 1 (exclusive) 
	 */
	public static DataSet rand(int... size) {
		return DataSet.convertToDataSet(uk.ac.diamond.scisoft.analysis.dataset.Random.rand(size));
	}

	/**
	 * @param size
	 * @return an array of values sampled from a Gaussian distribution with mean 0 and variance 1 
	 */
	public static DataSet randn(int... size) {
		return DataSet.convertToDataSet(uk.ac.diamond.scisoft.analysis.dataset.Random.randn(size));
	}

	/**
	 * @param low 
	 * @param high
	 * @param size
	 * @return an array of values sampled from a discrete uniform distribution in range [low, high)
	 */
	public static DataSet randint(int low, int high, int[] size) {
		return DataSet.convertToDataSet(uk.ac.diamond.scisoft.analysis.dataset.Random.random_integers(low, high-1, size));
	}

	/**
	 * @param low 
	 * @param high 
	 * @param size
	 * @return an array of values sampled from a discrete uniform distribution in range [low, high]
	 */
	public static DataSet random_integers(int low, int high, int[] size) {
		return DataSet.convertToDataSet(uk.ac.diamond.scisoft.analysis.dataset.Random.random_integers(low, high, size));
	}

	/**
	 * @param beta 
	 * @param size
	 * @return an array of values sampled from an exponential distribution with mean beta
	 */
	public static DataSet exponential(double beta, int... size) {
		return DataSet.convertToDataSet(uk.ac.diamond.scisoft.analysis.dataset.Random.exponential(beta, size));
	}

	/**
	 * @param lam 
	 * @param size
	 * @return an array of values sampled from an exponential distribution with mean lambda
	 */
	public static DataSet poisson(double lam, int... size) {
		return DataSet.convertToDataSet(uk.ac.diamond.scisoft.analysis.dataset.Random.poisson(lam, size));
	}
}
