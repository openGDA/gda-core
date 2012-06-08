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

package gda.device.motor.corba.impl;

import gda.device.Motor;
import gda.device.MotorException;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceImpl;
import gda.device.motor.corba.CorbaMotorException;
import gda.device.motor.corba.CorbaMotorPOA;
import gda.device.motor.corba.CorbaMotorStatus;
import gda.factory.corba.CorbaFactoryException;

/**
 * A server side implementation for a distributed Motor class
 */
public class MotorImpl extends CorbaMotorPOA {
	//
	// Private reference to implementation object
	//
	private Motor motor;

	private DeviceImpl deviceImpl;

	//
	// Private reference to POA
	//
	private org.omg.PortableServer.POA poa;

	/**
	 * Create server side implementation to the CORBA package.
	 * 
	 * @param motor
	 *            the Motor implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public MotorImpl(Motor motor, org.omg.PortableServer.POA poa) {
		this.motor = motor;
		this.poa = poa;
		deviceImpl = new DeviceImpl(motor, poa);
	}

	/**
	 * Get the implementation object
	 * 
	 * @return the Motor implementation object
	 */
	public Motor _delegate() {
		return motor;
	}

	/**
	 * Set the implementation object.
	 * 
	 * @param motor
	 *            set the Motor implementation object
	 */
	public void _delegate(Motor motor) {
		this.motor = motor;
	}

	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	@Override
	public void moveBy(double steps) throws CorbaMotorException {
		try {
			motor.moveBy(steps);
		} catch (MotorException ex) {
			CorbaMotorStatus ms = CorbaMotorStatus.from_int(ex.status.value());
			throw new CorbaMotorException(ms, ex.getMessage());
		}
	}

	@Override
	public void moveTo(double steps) throws CorbaMotorException {
		try {
			motor.moveTo(steps);
		} catch (MotorException ex) {
			CorbaMotorStatus ms = CorbaMotorStatus.from_int(ex.status.value());
			throw new CorbaMotorException(ms, ex.getMessage());
		}
	}

	@Override
	public void moveContinuously(int direction) throws CorbaMotorException {
		try {
			motor.moveContinuously(direction);
		} catch (MotorException ex) {
			CorbaMotorStatus ms = CorbaMotorStatus.from_int(ex.status.value());
			throw new CorbaMotorException(ms, ex.getMessage());
		}
	}

	@Override
	public void setPosition(double steps) throws CorbaMotorException {
		try {
			motor.setPosition(steps);
		} catch (MotorException ex) {
			CorbaMotorStatus ms = CorbaMotorStatus.from_int(ex.status.value());
			throw new CorbaMotorException(ms, ex.getMessage());
		}
	}

	@Override
	public double getPosition() throws CorbaMotorException {
		try {
			return motor.getPosition();
		} catch (MotorException ex) {
			CorbaMotorStatus ms = CorbaMotorStatus.from_int(ex.status.value());
			throw new CorbaMotorException(ms, ex.getMessage());
		}
	}

	@Override
	public void setSpeed(double speed) throws CorbaMotorException {
		try {
			motor.setSpeed(speed);
		}

		catch (MotorException ex) {
			CorbaMotorStatus ms = CorbaMotorStatus.from_int(ex.status.value());
			throw new CorbaMotorException(ms, ex.getMessage());
		}
	}

	@Override
	public double getSpeed() throws CorbaMotorException {
		try {
			return motor.getSpeed();
		} catch (MotorException ex) {
			CorbaMotorStatus ms = CorbaMotorStatus.from_int(ex.status.value());
			throw new CorbaMotorException(ms, ex.getMessage());
		}
	}

	@Override
	public double getRetryDeadband() throws CorbaMotorException {
		try {
			return motor.getRetryDeadband();
		} catch (MotorException ex) {
			CorbaMotorStatus ms = CorbaMotorStatus.from_int(ex.status.value());
			throw new CorbaMotorException(ms, ex.getMessage());
		}
	}

	@Override
	public double getTimeToVelocity() throws CorbaMotorException {
		try {
			return motor.getTimeToVelocity();
		} catch (MotorException ex) {
			throw corbaEquivalentOf(ex);
		}
	}

	@Override
	public void setTimeToVelocity(double timeToVelocity) throws CorbaMotorException {
		try {
			motor.setTimeToVelocity(timeToVelocity);
		} catch (MotorException ex) {
			throw corbaEquivalentOf(ex);
		}
	}

	@Override
	public void stop() throws CorbaMotorException {
		try {
			motor.stop();
		} catch (MotorException ex) {
			CorbaMotorStatus ms = CorbaMotorStatus.from_int(ex.status.value());
			throw new CorbaMotorException(ms, ex.getMessage());
		}
	}

	@Override
	public void panicStop() throws CorbaMotorException {
		try {
			motor.panicStop();
		} catch (MotorException ex) {
			CorbaMotorStatus ms = CorbaMotorStatus.from_int(ex.status.value());
			throw new CorbaMotorException(ms, ex.getMessage());
		}
	}

	@Override
	public CorbaMotorStatus getStatus() throws CorbaMotorException {
		CorbaMotorStatus status;
		try {
			status = CorbaMotorStatus.from_int(motor.getStatus().value());
		} catch (MotorException ex) {
			CorbaMotorStatus ms = CorbaMotorStatus.from_int(ex.status.value());
			throw new CorbaMotorException(ms, ex.getMessage());
		}
		return status;
	}

	@Override
	public void correctBacklash() throws CorbaMotorException {
		try {
			motor.correctBacklash();
		} catch (MotorException ex) {
			CorbaMotorStatus ms = CorbaMotorStatus.from_int(ex.status.value());
			throw new CorbaMotorException(ms, ex.getMessage());
		}
	}

	@Override
	public boolean isMoving() throws CorbaMotorException {
		try {
			return motor.isMoving();
		} catch (MotorException ex) {
			CorbaMotorStatus ms = CorbaMotorStatus.from_int(ex.status.value());
			throw new CorbaMotorException(ms, ex.getMessage());
		}
	}

	@Override
	public void setSpeedLevel(int speedLevel) throws CorbaMotorException {
		try {
			motor.setSpeedLevel(speedLevel);
		} catch (MotorException ex) {
			CorbaMotorStatus ms = CorbaMotorStatus.from_int(ex.status.value());
			throw new CorbaMotorException(ms, ex.getMessage());
		}
	}

	@Override
	public boolean isHomeable() throws CorbaMotorException {
		try {
			return motor.isHomeable();
		} catch (MotorException ex) {
			CorbaMotorStatus ms = CorbaMotorStatus.from_int(ex.status.value());
			throw new CorbaMotorException(ms, ex.getMessage());
		}
	}

	@Override
	public boolean isHomed() throws CorbaMotorException {
		try {
			return motor.isHomed();
		} catch (MotorException ex) {
			CorbaMotorStatus ms = CorbaMotorStatus.from_int(ex.status.value());
			throw new CorbaMotorException(ms, ex.getMessage());
		}
	}

	@Override
	public void home() throws CorbaMotorException {
		try {
			motor.home();
		} catch (MotorException ex) {
			CorbaMotorStatus ms = CorbaMotorStatus.from_int(ex.status.value());
			throw new CorbaMotorException(ms, ex.getMessage());
		}
	}

	@Override
	public void setSoftLimits(double min, double max) throws CorbaMotorException {
		try {
			motor.setSoftLimits(min, max);
		} catch (MotorException ex) {
			CorbaMotorStatus ms = CorbaMotorStatus.from_int(ex.status.value());
			throw new CorbaMotorException(ms, ex.getMessage());
		}
	}

	@Override
	public void setAttribute(String attributeName, org.omg.CORBA.Any value) throws CorbaDeviceException {
		deviceImpl.setAttribute(attributeName, value);
	}

	@Override
	public org.omg.CORBA.Any getAttribute(String attributeName) throws CorbaDeviceException {
		return deviceImpl.getAttribute(attributeName);
	}

	@Override
	public boolean isLimitsSettable() throws CorbaMotorException {
		try {
			return motor.isLimitsSettable();
		} catch (MotorException ex) {
			CorbaMotorStatus ms = CorbaMotorStatus.from_int(ex.status.value());
			throw new CorbaMotorException(ms, ex.getMessage());
		}
	}

	@Override
	public double getMinPosition() throws CorbaMotorException {
		try {
			return motor.getMinPosition();
		} catch (MotorException ex) {
			CorbaMotorStatus ms = CorbaMotorStatus.from_int(ex.status.value());
			throw new CorbaMotorException(ms, ex.getMessage());
		}
	}

	@Override
	public double getMaxPosition() throws CorbaMotorException {
		try {
			return motor.getMaxPosition();
		} catch (MotorException ex) {
			CorbaMotorStatus ms = CorbaMotorStatus.from_int(ex.status.value());
			throw new CorbaMotorException(ms, ex.getMessage());
		}
	}

	@Override
	public boolean isInitialised() throws CorbaMotorException {
		try {
			return motor.isInitialised();
		} catch (MotorException ex) {
			CorbaMotorStatus ms = CorbaMotorStatus.from_int(ex.status.value());
			throw new CorbaMotorException(ms, ex.getMessage());
		}
	}

	@Override
	public double getMotorResolution() throws CorbaMotorException {
		try {
			return motor.getMotorResolution();
		} catch (MotorException ex) {
			CorbaMotorStatus ms = CorbaMotorStatus.from_int(ex.status.value());
			throw new CorbaMotorException(ms, ex.getMessage());
		}
	}

	@Override
	public void reconfigure() throws CorbaFactoryException {
		deviceImpl.reconfigure();
	}

	@Override
	public void close() throws CorbaDeviceException {
		deviceImpl.close();
	}

	@Override
	public int getProtectionLevel() throws CorbaDeviceException {
		return deviceImpl.getProtectionLevel();
	}

	@Override
	public void setProtectionLevel(int newLevel) throws CorbaDeviceException {
		deviceImpl.setProtectionLevel(newLevel);
	}

	private static CorbaMotorException corbaEquivalentOf(MotorException ex) {
		CorbaMotorStatus ms = CorbaMotorStatus.from_int(ex.status.value());
		return new CorbaMotorException(ms, ex.getMessage());
	}

	@Override
	public double getUserOffset() throws CorbaMotorException {
		try {
			return motor.getUserOffset();
		} catch (MotorException ex) {
			CorbaMotorStatus ms = CorbaMotorStatus.from_int(ex.status.value());
			throw new CorbaMotorException(ms, ex.getMessage());
		}
	}
}
