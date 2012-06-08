/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

import java.util.Date;

/**
 * a scannable that isBusy for a given amount of time after every movement
 */
public class TimeTravelScannable extends ScannableBase {

	private long scanstart = 0;
	private double position = 0;

	/**
	 * create an instance with a delay time of pause
	 */
	public TimeTravelScannable() {
		this.setName("time");
		this.setInputNames(new String[] { "time" });
	}

	/**
	 * @see gda.device.Scannable#asynchronousMoveTo(java.lang.Object)
	 */
	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		this.position = Double.parseDouble(position.toString());
	}

	/**
	 * @see gda.device.Scannable#getPosition()
	 */
	@Override
	public Object getPosition() throws DeviceException {
		if (scanstart == 0)
			return new Double(0);
		return new Double((new Date().getTime() - scanstart)) / 1000;
	}

	/**
	 * @see gda.device.Scannable#isBusy()
	 */
	@Override
	public boolean isBusy() throws DeviceException {
		// not moved? we can't be busy then
		if (scanstart == 0)
			return false;
		return ((scanstart + (position * 1000)) >= ((new Date()).getTime()));
	}

	/**
	 * @see gda.device.scannable.ScannableBase#atScanEnd()
	 */
	@Override
	public void atScanEnd() throws DeviceException {
		scanstart = 0;
	}

	/**
	 * @see gda.device.scannable.ScannableBase#atScanStart()
	 */
	@Override
	public void atScanStart() throws DeviceException {
		scanstart = (new Date()).getTime();
	}
}
