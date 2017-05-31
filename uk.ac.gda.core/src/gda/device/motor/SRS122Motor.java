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

package gda.device.motor;

import gda.device.MotorException;
import gda.device.MotorStatus;
import gda.factory.FactoryException;
import gda.factory.Finder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A SRS122 VME motor class. This implementation uses a motord process running on VME for low level control of the
 * SRS122 motor. It talks to that server process via tcpip.
 */
public class SRS122Motor extends MotorBase {
	private static final Logger logger = LoggerFactory.getLogger(MotorBase.class);

	private String mnemonic = null;

	private MotordController mc;

	private String MotordControllerName;

	/**
	 * Constructor.
	 */
	public SRS122Motor() {
	}

	@Override
	public void configure() throws FactoryException {
		Finder finder = Finder.getInstance();
		if ((mc = (MotordController) finder.find(MotordControllerName)) == null) {
			logger.error("SRS122Controller not found");
			throw new FactoryException("Controller can't be found");
		}

		// loadPosition(getName());

		// use the VME motor position file. Safer if someone has moved the
		// motors
		// with a telnet connection.
		try {
			mc.loadPosition(mnemonic);
		} catch (InterruptedException e) {
			String msg = "Thread interrupted during configuration for " + getName();
			logger.error(msg, e);
			Thread.currentThread().interrupt();
			throw new FactoryException(msg, e);
		}
	}

	/**
	 * @return Returns the motordControllerName.
	 */
	public String getMotordControllerName() {
		return MotordControllerName;
	}

	/**
	 * @param motordControllerName
	 *            The motordControllerName to set.
	 */
	public void setMotordControllerName(String motordControllerName) {
		MotordControllerName = motordControllerName;
	}

	/**
	 * @param mnemonic
	 */
	public void setMnemonic(String mnemonic) {
		this.mnemonic = mnemonic;
	}

	/**
	 * @return Returns the Mnemonic.
	 */
	public String getMnemonic() {
		return mnemonic;
	}

	@Override
	public void moveBy(double steps) throws MotorException {
		checkMotorSupported();
		try {
			mc.moveBy(getMnemonic(), (int) Math.round(addInBacklash(steps)));
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
			String msg = "Thread interrupted while moving motor " + getName();
			logger.error(msg, ie);
			throw new MotorException(MotorStatus.UNKNOWN, msg);
		}
	}

	@Override
	public void moveTo(double steps) throws MotorException {
		checkMotorSupported();
		double currentPosition = getPosition();
		double increment = steps - currentPosition;
		steps = addInBacklash(increment) + currentPosition;
		try {
			mc.moveTo(getMnemonic(), (int) Math.round(steps));
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
			String msg = "Thread interrupted while moving motor " + getName();
			logger.error(msg, ie);
			throw new MotorException(MotorStatus.UNKNOWN, msg);
		}
	}

	@Override
	public void moveContinuously(int direction) throws MotorException {
		checkMotorSupported();
		try {
			mc.moveContinuously(getMnemonic(), direction);
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
			String msg = "Thread interrupted while moving motor " + getName();
			logger.error(msg, ie);
			throw new MotorException(MotorStatus.UNKNOWN, msg);
		}
	}

	@Override
	public boolean isMoving() {
		return mc.isMoving(getMnemonic());
	}

	@Override
	public void setPosition(double steps) throws MotorException {
		checkMotorSupported();
		try {
			mc.setPosition(getMnemonic(), (int) Math.round(steps));
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
			String msg = "Thread interrupted while setting position of motor " + getName();
			logger.error(msg, ie);
			throw new MotorException(MotorStatus.UNKNOWN, msg);
		}
	}

	@Override
	public double getPosition() throws MotorException {
		checkMotorSupported();
		return mc.getPosition(getMnemonic());
	}

	@Override
	public void setSpeed(double stepsPerSecond) throws MotorException {
		checkMotorSupported();
		try {
			mc.setSpeed(getMnemonic(), (int) Math.round(stepsPerSecond));
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
			String msg = "Thread interrupted while setting speed of motor " + getName();
			logger.error(msg, ie);
			throw new MotorException(MotorStatus.UNKNOWN, msg);
		}
	}

	@Override
	public double getSpeed() throws MotorException {
		checkMotorSupported();
		try {
			return mc.getSpeed(getMnemonic());
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
			String msg = "Thread interrupted while getting speed of motor " + getName();
			logger.error(msg, ie);
			throw new MotorException(MotorStatus.UNKNOWN, msg);
		}
	}

	@Override
	public void stop() throws MotorException {
		checkMotorSupported();
		try {
			mc.halt();
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
			String msg = "Thread interrupted while stopping motor " + getName();
			logger.error(msg, ie);
			throw new MotorException(MotorStatus.UNKNOWN, msg);
		}
	}

	@Override
	public void panicStop() throws MotorException {
		checkMotorSupported();
		try {
			mc.halt();
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
			String msg = "Thread interrupted while panicStopping motor " + getName();
			logger.error(msg, ie);
			throw new MotorException(MotorStatus.UNKNOWN, msg);
		}
	}

	@Override
	public MotorStatus getStatus() {
		return mc.getStatus(getMnemonic());
	}

	private void checkMotorSupported() throws MotorException {
		// If motor not supported by our MotordController this will throw
		// an exception. The exception will be passed up and the following
		// code not executed.

		MotorStatus status = MotorStatus.UNKNOWN;
		MotorException mex = null;

		/*
		 * if (!mc.isConnected()) { mex = new MotorException(status, "Motor " + mnemonic + " not ready.\n" + "Connection
		 * to the VME motor daemon " + "failed."); }
		 */
		if (!mc.areMotorsSupported()) {
			mex = new MotorException(status, "Motor " + getMnemonic() + " not ready.\n"
					+ "A list of motors supported by the " + "VME motor\n" + "daemon has not been established.");
		} else {
			if (!mc.isMotorSupported(getMnemonic()))
				mex = new MotorException(status, "Motor " + getMnemonic() + " unsupported.");
		}
		if (mex != null) {
			throw mex;
		}
	}
}
