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

package gda.exafs.scan;

import gda.configuration.properties.LocalProperties;
import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.beans.exafs.XanesScanParameters;
import uk.ac.gda.beans.exafs.XasScanParameters;

public class ExafsTimeEstimator {

	/**
	 * Estimates the time that an experiment will take.
	 *
	 * @param scanParameters
	 * @return time in ms
	 * @throws Exception
	 */
	public static long getTime(final IScanParameters scanParameters) throws Exception {
		double[][] points= null;
		if (scanParameters instanceof XanesScanParameters || scanParameters instanceof XasScanParameters) {
			points = XasScanPointCreator.build(scanParameters).getEnergies();
		}
		if (points == null) {
			return 0l;
		}

		return getTime(points);
	}

	/**
	 * Estimates the time that an experiment will take.
	 *
	 * Two system properties are read:
	 * gda.exafs.mono.energy.rate - the ms/eV rate of movement
	 * gda.exafs.read.out.time    - the constant read out time for detector(s)
	 *
	 * @param points
	 * @return time in ms
	 */
	public static long getTime(double[][] points) {
		if (points == null) {
			return 0l;
		}
		// We read the monochromator energy rate (eV / ms)
		final double monoRate = LocalProperties.getDouble("gda.exafs.mono.energy.rate", 1);
		// We read the xspress fudging factor
		final double readoutConst = LocalProperties.getDouble("gda.exafs.read.out.time", 1);
		long time = 0l;
		double lastEnergy = points[0][0];
		for (double[] fa : points) {
			double energy = fa[0];
			double diff   = energy-lastEnergy;
			time+=diff*monoRate; // ms for moving the mono
			time+=fa[1]*1000;
			time+=readoutConst;
			lastEnergy = energy;
		}
		return time;
	}

}
