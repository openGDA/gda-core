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

package gda.analysis.numerical.statistical;

import gda.analysis.datastructure.DataVector;
import gda.analysis.datastructure.DataVectorMath;

/**
 * Sliding window averaging scheme For a given data value, smoothing is achieved by averaging over the the surrounding n
 * pixels, where n is the smoothwidth defined by the user.
 */
public class AvgSmooth {
	/**
	 * @param g
	 * @param smoothwidth
	 * @return 1D data vector of smoothed data.
	 */
	public static DataVector averageSmooth(DataVector g, int smoothwidth) {
		int w = Math.round(smoothwidth);
		int halfWidth = Math.round(w / 2);
		DataVector result = new DataVector(g.size());
		DataVector temp = new DataVector(g.size());
		// Sum first points
		double sumPoints = DataVectorMath.count(g, 0, w);
		// Loop over the data
		for (int i = 0; i < g.size() - 1 - w; i++) {
			temp.set(i + halfWidth, sumPoints);
			// iterate sumpoints (remove leftmost old point and next new
			// rightmost
			// point)
			sumPoints -= g.get(i);
			sumPoints += g.get(i + w);
		}
		result = DataVectorMath.div(temp, w);
		return result;
	}
}
