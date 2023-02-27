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
import gda.device.scannable.scannablegroup.ScannableMotionWithScannableFieldsBase;
import gda.factory.FactoryException;
import uk.ac.diamond.daq.diffcalc.gda.DiffcalcContext;

public class HklScannable extends ScannableMotionWithScannableFieldsBase {

	private static final String OUTPUT_FORMAT = "%7.5f";

	protected DiffcalcContext diffcalcContext;

	@Override
	public void configure() throws FactoryException {
		String[] inputNames = { "h", "k", "l" };
		String[] formats = Collections.nCopies(3, OUTPUT_FORMAT).toArray(String[]::new);

		setInputNames(inputNames);
		setOutputFormat(formats);
		setConfigured(true);
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		return diffcalcContext.getHklPosition().toArray(Double[]::new);
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		if (position instanceof Double[] p) {
			diffcalcContext.moveToHkl(p[0], p[1], p[2]);
		} else if (position instanceof List) {
			List<Double> hkl = (List<Double>) position;
			diffcalcContext.moveToHkl(hkl.get(0), hkl.get(1), hkl.get(2));
		} else {
			throw new IllegalArgumentException("Position object: " + position + " is not supported");
		}
	}

	public String simulateMoveTo(Object position) throws DeviceException {
		String result = null;

		if (position instanceof Double[] p) {
			result = diffcalcContext.simulateMoveTo(Arrays.asList(p[0], p[1], p[2]));
		} else if (position instanceof List) {
			List<Double> hkl = (List<Double>) position;
			result = diffcalcContext.simulateMoveTo(hkl);
		} else {
			throw new IllegalArgumentException("Position object: " + position + " is not supported");
		}

		return result;
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return diffcalcContext.isDiffractometerBusy();
	}

	public DiffcalcContext getDiffcalcContext() {
		return diffcalcContext;
	}

	public void setDiffcalcContext(DiffcalcContext diffcalcContext) {
		this.diffcalcContext = diffcalcContext;
	}

}
