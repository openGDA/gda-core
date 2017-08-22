/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package gda.device.spin;

import gda.device.DeviceException;
import gda.device.ISpin;
import gda.device.scannable.ScannableBase;

public class DummySpinner extends ScannableBase implements ISpin {

	private boolean running;
	private double speed;

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

	@Override
	public void on() throws DeviceException {
		running = true;
	}

	@Override
	public void off() throws DeviceException {
		running = false;
	}

	@Override
	public void setSpeed(double speed) throws DeviceException {
		this.speed = speed;
	}

	@Override
	public double getSpeed() throws DeviceException {
		return speed;
	}

	@Override
	public String getState() throws DeviceException {
		return running ? "Enabled" : "Disabled";
	}

}
