/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package gda.device.syringepump;

import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.factory.FactoryException;

public class SyringePump extends DeviceBase implements Syringe {

	private SyringePumpController controller;

	@Override
	public boolean isBusy() throws DeviceException {
		return controller.isBusy();
	}

	public void setController(SyringePumpController controller) {
		this.controller = controller;
	}

	public SyringePumpController getController() {
		return controller;
	}

	@Override
	public void stop() throws DeviceException {
		controller.stop();
	}

	@Override
	public void configure() throws FactoryException {
		if (controller != null && getName() != null) {
			controller.configure();
			controller.addIObserver(this);
			configured = true;
		} else {
			throw new FactoryException("SyringePump must have a name and a controller");
		}
	}

	@Override
	public void infuse(double ml) throws DeviceException {
		controller.infuse(ml);
	}
	@Override
	public double getVolume() throws DeviceException {
		return controller.getVolume();
	}

	@Override
	public double getCapacity() {
		return controller.getCapacity();
	}
	@Override
	public double getRemainingTime() throws DeviceException {
		return controller.getVolume() / controller.getInfuseRate();
	}

	@Override
	public String toString() {
		if (!configured) {
			return String.format("%s - is not configured", getName());
		}
		try {
		if (controller.isEnabled()) {
			return String.format("%s - Capacity: %.4f, currentVolume: %.4f", getName(), controller.getCapacity(), controller.getVolume());
		} else {
			return String.format("%s - Controller is not enabled", getName());
		}
		} catch (DeviceException de) {
			return String.format("%s - Could not get current volume from controller: %s", getName(), de.getMessage());
		}
	}

	@Override
	public void setVolume(double ml) throws DeviceException {
		controller.setVolume(ml);
		notifyIObservers(this, ml);
	}

	@Override
	public boolean isEnabled() {
		return configured && controller.isEnabled();
	}

	@Override
	public void update(Object source, Object arg) {
		notifyIObservers(source, arg);
	}

	@Override
	public double getInfuseRate() throws DeviceException {
		return controller.getInfuseRate();
	}

	@Override
	public double getWithdrawRate() throws DeviceException {
		return controller.getWithdrawRate();
	}
}
