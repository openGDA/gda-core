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
import gda.device.motor.DummyMotor;
import gda.device.scannable.ScannableMotor;
import gda.factory.FactoryException;
import gda.observable.IObserver;

public class DummySyringePumpController extends DeviceBase implements SyringePumpController, IObserver {

	private double force;
	private double targetTime;
	private double diameter;
	private double infuseRate;
	private double withdrawRate;

	private boolean configured;
	private boolean enabled;


	private DummyMotor syringe; // use dummy motor to simulate slow moves
	private ScannableMotor syringeScannable;
	private double capacity;
	private String name;

	@Override
	public void configure() throws FactoryException {
		if (!enabled || configured) {
			return;
		}
		syringe = new DummyMotor();
		syringe.setName(getName() + "syringePumpDummyMotor");
		syringe.configure();
		syringeScannable = new ScannableMotor();
		syringeScannable.setMotor(syringe);
		syringeScannable.setName(getName() + "syringeDummyMotor");
		syringeScannable.configure();
		syringe.addIObserver(this);
		configured = true;
	}

	private void checkEnabled() throws DeviceException {
		if (!enabled) {
			throw new DeviceException("DummySyringePumpController " + getName() + " is not enabled");
		}
	}

	@Override
	public boolean isBusy() throws DeviceException {
		checkEnabled();
		return syringeScannable.isBusy();
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enable) {
		enabled = enable;
		notifyIObservers(this, null);
	}

	@Override
	public void stop() throws DeviceException {
		checkEnabled();
		syringeScannable.stop();
	}

	@Override
	public void setForce(double percent) throws DeviceException {
		checkEnabled();
		force = percent;
		syringeScannable.setSpeed(percent);
	}

	@Override
	public double getForce() throws DeviceException {
		checkEnabled();
		return force;
	}

	@Override
	public void setTargetTime(double seconds) throws DeviceException {
		checkEnabled();
		targetTime = seconds;
	}

	@Override
	public double getTargetTime() throws DeviceException {
		checkEnabled();
		return targetTime;
	}

	@Override
	public void setDiameter(double millimeters) throws DeviceException {
		checkEnabled();
		diameter = millimeters;
	}

	@Override
	public double getDiameter() throws DeviceException {
		checkEnabled();
		return diameter;
	}

	@Override
	public void setInfuseRate(double mlps) throws DeviceException {
		checkEnabled();
		infuseRate = mlps;
		notifyIObservers(this, null);
	}

	@Override
	public double getInfuseRate() throws DeviceException {
		checkEnabled();
		return infuseRate;
	}

	@Override
	public void setWithdrawRate(double mlps) throws DeviceException {
		checkEnabled();
		withdrawRate = mlps;
		notifyIObservers(this, null);
	}

	@Override
	public double getWithdrawRate() throws DeviceException {
		checkEnabled();
		return withdrawRate;
	}

	@Override
	public void infuse(double ml) throws DeviceException {
		checkEnabled();
		if (ml < 0) {
			syringe.setSpeed(withdrawRate);
		} else {
			syringe.setSpeed(infuseRate);
		}
		if (syringe.getSpeed() <= 0) {
			throw new DeviceException("Rate is out of allowed range (must be positive)");
		}
		double newVolume = syringe.getPosition() - ml;
		if (newVolume < 0 || newVolume > capacity) {
			throw new DeviceException("Cannot infuse to outside range of (0, capacity)");
		}
		syringeScannable.asynchronousMoveTo(syringe.getPosition() - ml);
	}

	@Override
	public double getCapacity() {
		return capacity;
	}

	public void setCapacity(double ml) {
		capacity = ml;
		notifyIObservers(this, null);
	}

	@Override
	public double getVolume() throws DeviceException {
		checkEnabled();
		return syringe.getPosition();
	}

	@Override
	public void setVolume(double ml) throws DeviceException {
		checkEnabled();
		if (syringeScannable.isBusy()) {
			throw new DeviceException("Could not move dummy pump");
		}
		if (ml > getCapacity()) {
			throw new DeviceException("Volume cannot be greater than capacity");
		}
		syringe.setPosition(ml);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void update(Object source, Object arg) {
		try {
			notifyIObservers(this, Double.valueOf(getVolume()));
		} catch (DeviceException e) {
			// c'est la vie
		}
	}
}
