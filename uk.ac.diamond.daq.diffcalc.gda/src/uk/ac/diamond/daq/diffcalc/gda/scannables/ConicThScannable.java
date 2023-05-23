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
import uk.ac.diamond.daq.diffcalc.gda.TypeConversion;
import uk.ac.diamond.daq.diffcalc.model.HklModel;

public class ConicThScannable extends ParametrisedHklScannable {

	private double tolerance = 0.000001;
	@Override
	public void configure() throws FactoryException {
		String[] inputNames = { "r", "th", "h0", "k0" };
		String[] formats = Collections.nCopies(inputNames.length, OUTPUT_FORMAT).toArray(String[]::new);

		setNumberCachedParams(4);
		setInputNames(inputNames);
		setOutputFormat(formats);
		setConfigured(true);
	}

	@Override
	protected List<List<Double>> parametersToHkl(List<Double> paramList) throws DeviceException {

		var r = paramList.get(0);
		var th = paramList.get(1);
		var h0 = paramList.get(2);
		var k0 = paramList.get(3);


		if ((th >= 180) || (th < 0)) {
			throw new IllegalArgumentException("Value of th should be in [0, 180) range");
		}

		Double sinTh = Math.sin(Math.toRadians(th));
		Double cosTh = Math.cos(Math.toRadians(th));

		Double a = sinTh;
		Double b= -cosTh;
		Double c=0d;
		Double d = h0 * sinTh - k0* cosTh;

		HklModel hkl = TypeConversion.millerIndicesToHklModel(diffcalcContext.getHklPosition());

		return (th > 45.0) ?
				diffcalcContext.solveForIndex(hkl, "k", k0 + r*sinTh, a, b, c, d)
				: diffcalcContext.solveForIndex(hkl, "h", h0 + r*cosTh, a, b, c, d);
	}

	@Override
	protected List<Double> hklToParameters(List<Double> hkl) {

		Double h = hkl.get(0);
		Double k= hkl.get(1);

		List<Double> cachedParams = getCachedParams();

		var r0 = cachedParams.get(0);
		var th0 = cachedParams.get(1);
		var h0= cachedParams.get(2);
		var k0= cachedParams.get(3);

		Double r = Math.sqrt(Math.pow(h- h0, 2.0) + Math.pow(k - k0, 2.0));
		if (r < tolerance) {
			return Arrays.asList(r, th0, h0, k0);
		}

		Double th = Math.atan2(k-k0, h-h0);
		if (r0 < 0.0) {
			r = -r;
			if (th <0) {th += Math.PI;}
			else if (Math.abs(th- Math.PI) < tolerance) {th = 0.0;}

		}
		return Arrays.asList(r, Math.toDegrees(th), h0, k0);

	}

	public double getTolerance() {
		return tolerance;
	}

	public void setTolerance(double tolerance) {
		this.tolerance = tolerance;
	}

}
