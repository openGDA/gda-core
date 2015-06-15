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

package gda.device.detector.nxdetector;

import gda.device.DeviceException;
import gda.device.continuouscontroller.HardwareTriggerProvider;
import gda.factory.FactoryException;
import gda.jython.accesscontrol.MethodAccessProtected;
import gda.observable.IObserver;

public class TFG2NXHardwareTriggerProvider implements HardwareTriggerProvider{

	@Override
	@MethodAccessProtected(isProtected = true)
	public void setAttribute(String attributeName, Object value) throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	@MethodAccessProtected(isProtected = true)
	public void setProtectionLevel(int newLevel) throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public int getProtectionLevel() throws DeviceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addIObserver(IObserver observer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteIObserver(IObserver observer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteIObservers() {
		// TODO Auto-generated method stub

	}

	@Override
	public void reconfigure() throws FactoryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTriggerPeriod(double seconds) throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public int getNumberTriggers() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getTotalTime() throws DeviceException {
		// TODO Auto-generated method stub
		return 0;
	}

}
