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

package gda.device.monitor;

import gda.device.DeviceException;
import gda.device.Monitor;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.ScannableUtils;
import java.lang.System;

import org.jscience.physics.units.Unit;

/**
 * Base class for objects which need to operate in scans, but which have no
 * control over the values returned by getPosition()
 * <P>
 * This base class is important to help to distinguish types of scannables,
 * especially when storing or presenting data from scans.
 */
public abstract class MonitorBase extends ScannableBase implements Monitor {

	double waitForSystemTimeInMillis = 0.0;

	/**
	 * If called with a position this triggers a delay. Very useful if the
	 * monitor is hooked up to something that calculates a running average, or
	 * that is not updated very often. {@inheritDoc}
	 * 
	 * @see gda.device.Scannable#asynchronousMoveTo(java.lang.Object)
	 */
	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		// // This method is for operating the object and telling it to do some
		// // work, but monitors should be passive, so if you are trying to
		// operate
		// // it then you are doing something wrong! So throw an exception.
		// throw new
		// DeviceException("You should not call asynchronousMoveTo on "
		// + getName() + " as this is a Monitor so this method does nothing.");

		double waitTime = ScannableUtils.objectToArray(position)[0];
		waitForSystemTimeInMillis = System.currentTimeMillis() + 1000
				* waitTime;
	}

	@Override
	public boolean isBusy() {
		return (System.currentTimeMillis() < waitForSystemTimeInMillis);
	}

	@Override
	public int getElementCount() throws DeviceException {
		return inputNames.length + extraNames.length;
	}

	@Override
	public String getUnit() throws DeviceException {
		// just a number
		return Unit.ONE.toString();
	}

}
