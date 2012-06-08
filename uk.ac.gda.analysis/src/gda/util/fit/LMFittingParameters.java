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

package gda.util.fit;

/**
 * LMFittingParameters Class
 */
public class LMFittingParameters {
	private double lamda = 0.1;

	// Used to determine when to stop the fitting (e.g. chisquared <
	// epsilon)
	private double epsilon = 1E-6;

	// Maximum no of iterations in the fitting procedure
	private int maxNoOfIterations = 200;

	// Print out fitting information
	private int verbose = 2;

	/**
	 * Constructor
	 */
	public LMFittingParameters() {

	}

	/**
	 * @param _lamda
	 */
	public void setLamda(double _lamda) {
		lamda = _lamda;
	}

	/**
	 * @return lamda
	 */
	public double getLamda() {
		return lamda;
	}

	/**
	 * @param _epsilon
	 */
	public void setEpsilon(double _epsilon) {
		epsilon = _epsilon;
	}

	/**
	 * @return epsilon
	 */
	public double getEpislon() {
		return epsilon;
	}

	/**
	 * @param _maxNoOfIterations
	 */
	public void setMaxNoOfIterations(int _maxNoOfIterations) {
		maxNoOfIterations = _maxNoOfIterations;
	}

	/**
	 * @return maxNoOfIterations
	 */
	public int getMaxNoOfIterations() {
		return maxNoOfIterations;
	}

	/**
	 * @param _verbose
	 */
	public void setVerbos(int _verbose) {
		verbose = _verbose;
	}

	/**
	 * @return int verbose
	 */
	public int getVerbose() {
		return verbose;
	}

}
