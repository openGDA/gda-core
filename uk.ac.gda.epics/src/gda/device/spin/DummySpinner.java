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
import uk.ac.gda.api.remoting.ServiceInterface;

@ServiceInterface(ISpin.class)
public class DummySpinner extends ScannableBase implements ISpin {

	private boolean running;
	private double speed;

	@Override
	public Object rawGetPosition() throws DeviceException {
		return getState();
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		int pos = Integer.parseInt(position.toString());
		if (pos == 1) {
			on();
		} else if (pos == 0) {
			off();
		} else {
			throw new IllegalArgumentException("Only takes value 1 for on or 0 for off.");
		}
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

	@Override
	public void on() throws DeviceException {
		running = true;
		notifyIObservers(this, getState());
	}

	@Override
	public void off() throws DeviceException {
		running = false;
		notifyIObservers(this, getState());
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
