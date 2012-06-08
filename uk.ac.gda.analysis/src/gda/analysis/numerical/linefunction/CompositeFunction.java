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

package gda.analysis.numerical.linefunction;

import gda.analysis.datastructure.DataVector;

/**
 * Fit a sum of Gaussians to data. The function is K y(x) = sum A_k exp(-((x - E_k)) / sigma_k)^2) 1 i.e. for a given no
 * of gaussians K each with a variable for position (E_k), amplitude (A_k) and width or variance sigma_k produce y(x).
 * This is our model which we'll fit against the data. Author : Paul Quinn
 */
public class CompositeFunction extends AbstractCompositeFunction {

	/**
	 * Constructor.
	 */
	public CompositeFunction() {

	}

	@Override
	public double val(double... positions) {
		// Check x and a size for correctness
		int noOfFunctions = getNoOfFunctions();
		// initialize y to zero

		double y = 0.;
		// Just sum over the individual functions
		for (int j = 0; j < noOfFunctions; j++)
			y += functions.get(j).val(positions);

		return y;
	}

	/**
	 * @param min
	 * @param max
	 * @param steps
	 * @return DataVector
	 */
	public DataVector makeDataVector(double min, double max, int steps) {

		// make the datavector
		int size[] = new int[2];
		size[0] = steps;
		size[1] = this.getNoOfFunctions() + 2;
		DataVector output = new DataVector(size);

		double delta = (max - min) / (steps);
		// add the xaxis
		for (int i = 0; i < steps; i++) {
			output.add(min + (i * delta));
		}
		// now add the data
		for (int i = 0; i < steps; i++) {
			double value = val(min + (i * delta));
			output.add(value);
		}

		// now add the data for each bit in turn
		for (int j = 0; j < this.getNoOfFunctions(); j++) {
			for (int i = 0; i < steps; i++) {
				double value = this.getFunction(j).val(min + (i * delta));
				output.add(value);
			}
		}

		return output;

	}

}
