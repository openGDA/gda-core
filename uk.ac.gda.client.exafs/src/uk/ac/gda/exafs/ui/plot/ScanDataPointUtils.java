/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.plot;

import gda.scan.IScanDataPoint;

import java.util.List;

public class ScanDataPointUtils {
	/**
	 * Call to extract I0 and It from data.
	 * 
	 * @param point
	 * @return double[] data
	 */
	public static double[] getI0andIt(final IScanDataPoint point) {

		if (point == null)
			return null;
		final Double[] data = point.getDetectorDataAsDoubles();
		final List<String> names = point.getDetectorHeader();
		double i0 = Double.NaN, it = Double.NaN;
		for (int i = 0; i < names.size(); i++) {
			if (names.get(i).toLowerCase().equals("i0")) {
				i0 = data[i];
			}
			if (names.get(i).toLowerCase().equals("it")) {
				it = data[i];
			}
		}
		return new double[] { i0, it };
	}

}
