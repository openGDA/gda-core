/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.device.scannable;

import java.io.IOException;

import gda.device.DeviceException;
import gda.epics.LazyPVFactory;
import gda.epics.PVWithSeparateReadback;

public class PIE725ConstantVelocityRasterScannable3Axis extends PIE725ConstantVelocityRasterScannable {

	private PVWithSeparateReadback<Double> pvZpair;

	private Double[] lastRasterTarget = new Double[3];

	public PIE725ConstantVelocityRasterScannable3Axis(String name, String pvName) {
		super(name, pvName);
		pvZpair = new PVWithSeparateReadback<Double>(LazyPVFactory.newDoublePV(pvName + "Z:MOV:WR"),
				LazyPVFactory.newReadOnlyDoublePV(pvName + "Z:POS:RD"));
		setInputNames(new String[] { name + 'X', name + 'Y', name + 'Z' });
		setOutputFormat(new String[] { "%.4f", "%.4f", "%.4f" });
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws gda.device.DeviceException {
		Double[] xyztarget = PositionConvertorFunctions.toDoubleArray(position);
		if (xyztarget.length != 3) {
			throw new AssertionError("Target position must have 3 fields, not " + xyztarget.length);
		}
		if (isOperatingContinously()) {
			// Record position for the subsequent getPosition() call
			for (int i = 0; i < 3; i++) {
				if (xyztarget[i] != null) lastRasterTarget[i] = xyztarget[i];
			}
		} else {
			try {
				if (xyztarget[0] != null) {
					pvXpair.putWait(xyztarget[0]);
				}
				if (xyztarget[1] != null) {
					pvYpair.putWait(xyztarget[1]);
				}
				if (xyztarget[2] != null) {
					pvZpair.putWait(xyztarget[2]);
				}
			} catch (IOException e) {
				throw new DeviceException(e);
			}
		}
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		if (isOperatingContinously()) {
			if (lastRasterTarget == null) {
				throw new NullPointerException("lastRasterTargetNotSet");
			}
			return lastRasterTarget;
		}
		try {
			return new Double[] { pvXpair.get(), pvYpair.get(), pvZpair.get() };
		} catch (IOException e) {
			throw new DeviceException(e);
		}
	}

}
