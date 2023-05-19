/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.diffcalc.gda.scannables;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import gda.device.DeviceException;
import gda.factory.FactoryException;

public class CircHklScannable extends ParametrisedHklScannable {

	@Override
	public void configure() throws FactoryException {
		String[] inputNames = { "q", "pol", "az" };
		String[] formats = Collections.nCopies(inputNames.length, OUTPUT_FORMAT).toArray(String[]::new);

		setInputNames(inputNames);
		setOutputFormat(formats);
		setConfigured(true);
	}

	@Override
	protected List<List<Double>> parametersToHkl(List<Double> paramList)
			throws DeviceException {
		Double q = paramList.get(0);
		Double pol = paramList.get(1);
		Double az = paramList.get(2);

		Double h = q * Math.sin(pol) * Math.cos(az);
		Double k = q * Math.sin(pol) * Math.sin(az);
		Double l = q * Math.cos(pol);

		return Arrays.asList(Arrays.asList(h, k, l));
	}

	@Override
	protected List<Double> hklToParameters(List<Double> hkl) {

		Double h = hkl.get(0);
		Double k = hkl.get(1);
		Double l = hkl.get(2);

		Double millerNorm = Math.sqrt(Math.pow(h, 2) + Math.pow(k, 2) + Math.pow(l, 2));

		Double q = millerNorm;
		Double pol = Math.acos(l/millerNorm);
		Double az = Math.atan2(k, h);
		return Arrays.asList(q, pol, az);
	}


}
