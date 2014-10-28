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

import gda.device.DeviceException;

/**
 * Scannable that isBusy for the time given in asyncMoveTo
 */
public class TimeDelayScannable extends ScannableBase {

	private double delay = 0;
	private long startTime = 0;

	public TimeDelayScannable() {
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		this.delay = Double.parseDouble(position.toString());
		this.startTime = System.currentTimeMillis();
	}

	@Override
	public Object getPosition() throws DeviceException {
		//Not sure what "getPosition" on a delay should be
		//Returning the time remaining for now - can't think of a real use case
		double timeRemaining = startTime + (delay * 1000) - System.currentTimeMillis();
		return timeRemaining > 0 ? timeRemaining / 1000 : 0;
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return (startTime + (delay * 1000)) > System.currentTimeMillis();
	}

}
