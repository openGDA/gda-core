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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.MotorException;
import gda.device.MotorStatus;
import gda.factory.Finder;

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
	public void configure() {
		Finder finder = Finder.getInstance();
		if ((mc = (MotordController) finder.find(MotordControllerName)) == null) {
			logger.error("SRS122Controller not found");
		}

		// loadPosition(getName());

		// use the VME motor position file. Safer if someone has moved the
		// motors
		// with a telnet conection.
		mc.loadPosition(mnemonic);
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
		mc.moveBy(getMnemonic(), (int) Math.round(addInBacklash(steps)));
	}

	@Override
	public void moveTo(double steps) throws MotorException {
		checkMotorSupported();
		double currentPosition = getPosition();
		double increment = steps - currentPosition;
		steps = addInBacklash(increment) + currentPosition;
		mc.moveTo(getMnemonic(), (int) Math.round(steps));
	}

	@Override
	public void moveContinuously(int direction) throws MotorException {
		checkMotorSupported();
		mc.moveContinuously(getMnemonic(), direction);
	}

	@Override
	public boolean isMoving() {
		return mc.isMoving(getMnemonic());
	}

	@Override
	public void setPosition(double steps) throws MotorException {
		checkMotorSupported();
		mc.setPosition(getMnemonic(), (int) Math.round(steps));
	}

	@Override
	public double getPosition() throws MotorException {
		checkMotorSupported();
		return mc.getPosition(getMnemonic());
	}

	@Override
	public void setSpeed(double stepsPerSecond) throws MotorException {
		checkMotorSupported();
		mc.setSpeed(getMnemonic(), (int) Math.round(stepsPerSecond));
	}

	@Override
	public double getSpeed() throws MotorException {
		checkMotorSupported();
		return mc.getSpeed(getMnemonic());
	}

	@Override
	public void stop() throws MotorException {
		checkMotorSupported();
		mc.halt();
	}

	@Override
	public void panicStop() throws MotorException {
		checkMotorSupported();
		mc.halt();
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
