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

package gda.analysis.numerical.random;

import gda.analysis.datastructure.DataVector;

/**
 * Class for DataVector random number operations
 */
public class Random {
	/**
	 * @param n
	 *            are the dimensions of the DataVector
	 * @return Returns a DataVector of dimensions n filled with random numbers
	 */
	public static DataVector randomData(int... n) {
		MT rand = new MT();

		DataVector result = new DataVector(n);
		for (int i = 0; i < result.size(); i++) {
			result.set(i, rand.nextDouble());
		}
		return result;
	}

}
