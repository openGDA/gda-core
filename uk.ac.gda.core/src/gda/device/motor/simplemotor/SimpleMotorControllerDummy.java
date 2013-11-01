/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.device.motor.simplemotor;

import gda.device.DeviceException;

public class SimpleMotorControllerDummy implements SimpleMotorController {

	@Override
	public double getMotorPosition() throws DeviceException {
		return position;
	}

	double position=0;
	private boolean busy=false;
	@Override
	public void moveTo(double position) throws DeviceException {
		busy = true;
		this.position=position;
		try {
			Thread.sleep(2);
		} catch (InterruptedException e) {
			throw new DeviceException("Interrupted",e);
		}
		busy=false;
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return busy;
	}

	@Override
	public void stop() throws DeviceException {
		//nothing to do
	}

	@Override
	public void setSpeed(double speed) throws DeviceException {
		//nothing to do
	}

	@Override
	public double getSpeed() throws DeviceException {
		return 1.;
	}

}
