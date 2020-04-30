/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package gda.device.scannable.iterator;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.IScannableMotor;

/**
 * Allows to drive in a {@link Iterator} fashion an {@link IScannableMotor}. However an {@link IScannableMotor} may
 * throw a @link {@link DeviceException} because of an internal error in the motor, consequently it was not possible to
 * implements a {@code java.util.Iterator}. The {@link #next()} and {@link #previous()} steps are controlled by an
 * internal {@link ContinuousLineIterator}.
 */
public class ScannableIterator {
	private final IScannableMotor motor;
	private long scaleResolution = 1L;
	private ContinuousLineIterator driver;

	private final Logger logger = LoggerFactory.getLogger(ScannableIterator.class);

	public ScannableIterator(IScannableMotor motor) throws DeviceException {
		this.motor = motor;
		initialise();
	}

	public double getDriverPosition() {
		try {
			return (double) motor.getPosition();
		} catch (DeviceException e) {
			return Double.NaN;
		}
	}

	public double getActualResolution() {
		return driver.actualResolution();
	}

	public void scaleResolution(long scaleResolution) {
		this.scaleResolution = scaleResolution;
	}

	public double getLowerLimit() throws DeviceException {
		return motor.getLowerInnerLimit();
	}

	public double getUpperLimit() throws DeviceException {
		return motor.getUpperInnerLimit();
	}

	public boolean isBusy() throws DeviceException {
		return motor.isBusy();
	}

	public void moveToRandom() throws DeviceException {
		double rndPoint = getLowerLimit() + Math.abs((getUpperLimit() - getLowerLimit())) * Math.random();
		driver.set(rndPoint);
		set(rndPoint);
	}

	public void forceToPosition(double position) throws DeviceException {
		set(position);
		driver.set(position);
	}

	public boolean hasNext() {
		return driver.hasNext();
	}

	public Double next() throws DeviceException {
		driver.scaleResolution(scaleResolution);
		set(driver.next());
		return getDriverPosition();
	}

	public boolean hasPrevious() {
		return driver.hasPrevious();
	}

	public Double previous() {
		return driver.previous();
	}

	public String getScannableName() {
		return motor.getName();
	}

	public void set(Double position) throws DeviceException {
		try {
			motor.moveTo(position);
			while (isBusy()) {
				Thread.sleep(100);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.error("Failed to set motor position", e);
			throw new DeviceException("Cannot set motor position to " + position);
		}
	}

	private void initialise() throws DeviceException {
		driver = new ContinuousLineIterator(motor.getLowerInnerLimit(), motor.getUpperInnerLimit(),
				motor.getMotorResolution(), (double) motor.getPosition());

	}
}
