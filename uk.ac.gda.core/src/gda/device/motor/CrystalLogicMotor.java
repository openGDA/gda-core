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

import gda.device.Motor;
import gda.device.MotorException;
import gda.device.MotorStatus;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.observable.IObservable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CrystalLogicMotor Class
 */
public class CrystalLogicMotor extends MotorBase implements IObservable, Motor, Runnable {

	private static final Logger logger = LoggerFactory.getLogger(CrystalLogicMotor.class);

	private enum commandStatus {
		/**
		 *
		 */
		SUCCESS, /**
		 *
		 */
		TIMEOUT
	}

	private String crystalLogicControllerName = null;

	private CrystalLogicController crystalLogicController = null;

	private String mnemonic;

	private boolean motorMoving = false;

	private double currentPosition = 0;

	private MotorStatus status = MotorStatus.READY;

	private Thread runner;

	private boolean monitor = false;

	private int offset = Integer.valueOf("800000", 16);

	private int[] motorPosition = new int[3];

	private String string;

	/**
	 * Constructor
	 */
	public CrystalLogicMotor() {
	}

	@Override
	public void configure() throws FactoryException {
		if ((crystalLogicController = (CrystalLogicController) Finder.getInstance().find(crystalLogicControllerName)) == null) {
			logger.error("CrystalLogicController " + crystalLogicControllerName + " not found");
		}
		try {
			updateMotorPositions();
		} catch (InterruptedException e) {
			logger.error("Thread interrupted while configuring", e);
			Thread.currentThread().interrupt();
			throw new FactoryException("Thread interrupted while configuring", e);
		}
	}

	private synchronized commandStatus sendCommand(String command) {
		commandStatus status = commandStatus.SUCCESS;

		crystalLogicController.sendCommand(command);

		return status;
	}

	private void setup() {
		sendCommand("wv m0 0");
		sendCommand("wv m1 0");
		sendCommand("wv m2 0");
		sendCommand("wv m3 0");
		sendCommand("wv m4 0");
		sendCommand("wv m5 0");
	}

	private void setVerbosity(int level) {
		sendCommand("wv v7 " + level);
	}

	private void updateMotorPositions() throws InterruptedException {
		int i;
		int motorId = 14;

		for (i = 0; i < 3; i++) {
			sendCommand("px" + motorId++);
			Thread.sleep(100);
			string = crystalLogicController.getPositionReply();
			extractMotorPosition(false);
		}
	}

	private void calculatePosition() {
		if (mnemonic.equals("X")) {
			currentPosition = motorPosition[2];
			currentPosition -= ((motorPosition[0] + motorPosition[1]) / 2.0);
			currentPosition /= 3.0;
		} else if (mnemonic.equals("Y")) {
			currentPosition = (motorPosition[1] - motorPosition[0]) / 2.0;
		} else if (mnemonic.equals("Z")) {
			currentPosition = (motorPosition[0] + motorPosition[1] + motorPosition[2]) / -3.0;
		}
	}

	private void extractMotorPosition(boolean print) {
		int i;

		try {
			i = Integer.valueOf(string.substring(1, 2));
			motorPosition[i] = Integer.valueOf(string.substring(2, 8), 16);
			motorPosition[i] -= offset;
			if (print) {
				logger.debug("Motor Position " + i + ": " + motorPosition[i]);
			}

			calculatePosition();
		} catch (NumberFormatException nfe) {
			logger.debug("Unexpected string returned by Crystal Logic Controller");
			logger.debug("Content " + string);
		}
	}

	private void startMonitor() {
		runner = uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName());
		monitor = true;
		runner.start();
	}

	// Motor implementation

	@Override
	public void moveBy(double steps) throws MotorException {
		setup();
		setVerbosity(1);

		sendCommand("px120 " + mnemonic + ">" + (int) steps);
		motorMoving = true;
		status = MotorStatus.BUSY;

		startMonitor();
	}

	@Override
	public void moveTo(double steps) throws MotorException {
		moveBy(currentPosition - steps);
	}

	@Override
	public void moveContinuously(int direction) throws MotorException {
		int code = 0;

		if (mnemonic.equals("X")) {
			code = 61;
		} else if (mnemonic.equals("Y")) {
			code = 63;
		} else if (mnemonic.equals("Z")) {
			code = 65;
		}

		if (direction > 0) {
			code += 1;
		}

		setVerbosity(1);
		sendCommand("px " + code);

		motorMoving = true;
		status = MotorStatus.BUSY;

		startMonitor();
	}

	@Override
	public void setPosition(double steps) throws MotorException {
		currentPosition = steps;
	}

	@Override
	public double getPosition() throws MotorException {
		return currentPosition;
	}

	@Override
	public void setSpeed(double speed) throws MotorException {
		int intSpeed = Double.valueOf(speed).intValue();
		sendCommand("sly");
		sendCommand("wv c0 0");
		sendCommand("wv s1 4");
		sendCommand("wv r0 2");
		sendCommand("wv s6 " + intSpeed);
	}

	@Override
	public double getSpeed() throws MotorException {
		return 0.0;
	}

	@Override
	public void stop() throws MotorException {
		crystalLogicController.sendEscape();
		status = MotorStatus.READY;
		monitor = false;
	}

	@Override
	public void panicStop() throws MotorException {
		stop();
	}

	@Override
	public MotorStatus getStatus() throws MotorException {
		return status;
	}

	@Override
	public boolean isMoving() throws MotorException {
		return motorMoving;
	}

	// XML getters and setters

	/**
	 * @return mnemonic
	 */
	public String getMnemonic() {
		return mnemonic;
	}

	/**
	 * @param mnemonic
	 */
	public void setMnemonic(String mnemonic) {
		this.mnemonic = mnemonic;
	}

	/**
	 * @return String crystalLogicControllerName
	 */
	public String getCrystalLogicControllerName() {
		return crystalLogicControllerName;
	}

	/**
	 * @param crystalLogicControllerName
	 */
	public void setCrystalLogicControllerName(String crystalLogicControllerName) {
		this.crystalLogicControllerName = crystalLogicControllerName;
	}

	@Override
	public void run() {
		char c;
		boolean extract = false;

		string = "";
		while (monitor && ((c = crystalLogicController.getChar()) != ':')) {
			if (c == '\012') {
			} else if (c == '\015') {
				extract = true;
			} else if (c == 'M') {
				string = "";
				string += c;
			} else {
				string += c;
			}

			if (extract) {
				extractMotorPosition(false);
				extract = false;
				string = "";
			}
		}

		/*
		 * This seems to be one way to stop the controller producing Control Stack Overflow messages after a few moves.
		 */
		crystalLogicController.sendEscape();
		try {
			updateMotorPositions();
		} catch (InterruptedException e) {
			logger.error("Thread interrupted while configuring", e);
			Thread.currentThread().interrupt();
			throw new RuntimeException("Thread interrupted while updating motor positions", e);
		}

		setVerbosity(0);
		monitor = false;
		motorMoving = false;
		status = MotorStatus.READY;
	}
}
