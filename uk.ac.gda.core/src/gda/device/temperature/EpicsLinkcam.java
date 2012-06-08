/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device.temperature;

import gda.device.DeviceException;
import gda.util.PollerEvent;

/**
 * Class to control a LinkamCI Those computer interface boxes control the Linkam range of heating/freezing stages. They
 * offer a serial connection.
 */
public class EpicsLinkcam extends TemperatureBase {

	@Override
	protected void doStart() throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doStop() throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void sendRamp(int ramp) throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setHWLowerTemp(double lowerTemp) throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setHWUpperTemp(double upperTemp) throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void startNextRamp() throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void startTowardsTarget() throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public double getCurrentTemperature() throws DeviceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void pollDone(PollerEvent pe) {
		// TODO Auto-generated method stub

	}

	@Override
	public void hold() throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void runRamp() throws DeviceException {
		// TODO Auto-generated method stub

	}
}
