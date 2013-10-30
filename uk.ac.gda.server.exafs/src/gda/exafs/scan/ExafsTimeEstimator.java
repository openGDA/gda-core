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

import java.util.List;

import org.python.core.PyObject;
import org.python.core.PyTuple;

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
	@SuppressWarnings({ "cast", "unchecked" })
	public static long getTime(final IScanParameters scanParameters) throws Exception {
		
		PyTuple points= null;
		if (scanParameters instanceof XanesScanParameters) {
			points = XanesScanPointCreator.calculateEnergies((XanesScanParameters)scanParameters);
		} else if (scanParameters instanceof XasScanParameters) {
			points = ExafsScanPointCreator.calculateEnergies((XasScanParameters)scanParameters);
		}
		
		if (points==null) return 0l;
		
		// safe to perform an unchecked cast as PyTuple is a List<PyObject>
		return getTime((List<PyObject[]>) points);

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
	public static long getTime(List<PyObject[]> points) {
		if (points==null) 
			return 0l;
		
		// We read the monchromator energy rate (eV / ms)
		final String monoString    = LocalProperties.get("gda.exafs.mono.energy.rate");
		final double monoRate      = monoString!=null ? Double.parseDouble(monoString) : 1; // Default fast mono.
		
		// We read the xspress fudging factor
		final String readoutString = LocalProperties.get("gda.exafs.read.out.time");
		final double readoutConst  = readoutString!=null ? Double.parseDouble(readoutString) : 1; // Default fast detector.
		
		long time = 0l;
		double lastEnergy = points.get(0)[0].asDouble();
		
		for (PyObject[] fa : points) {
			double energy = fa[0].asDouble();
			double diff   = energy-lastEnergy;
			time+=diff*monoRate; // ms for moving the mono
			time+=fa[1].asDouble()*1000;
			time+=readoutConst;
			lastEnergy = energy;
		}
		
		return time;
	}

}
