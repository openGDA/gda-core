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

import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.factory.Findable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton class to control communication between the Aerotech 3200 software motor control envronment and an Aerotech
 * motor class.
 */
public class Aerotech3200Controller extends DeviceBase implements Findable {

	private static final Logger logger = LoggerFactory.getLogger(Aerotech3200Controller.class);

	final private int NOERROR = 0;

	// final private int SERVEROPEN = 0;

	final private int SERVERON = 1;

	// final private int RESET_NOTRESET = 0;
	// final private int RESET_RUNNING = 1;
	// final private int RESET_DONE_AX = 2;
	// final private int RESET_DONE_ALL = 3;

	// final private int MOTORNOTHOMED = 0;

	final private int MOTORHOMED = 1;

	private String paramFile = null;

	private int numMotors = 0;

	/**
	 *
	 */
	final public int READY = 0;

	/**
	 *
	 */
	final public int BUSY = 1;

	/**
	 *
	 */
	final public int CWLIMIT = 2;

	/**
	 *
	 */
	final public int CCWLIMIT = 3;

	// Native JNI function declarations -these can be found in teh
	// jAerSys.dll
	// file
	private native int jAerSysInitialise(String parameterFile);

	private native int jAerSysOpen();

	private native int jAerSysClose();

	private native int jAerSysStop();

	private native int jAerSysServerIsOpen();

	// private native int jAerSysIsResetDone(int[] resetCode);

	private native void jAerGetErrorString(int ErrorCode, StringBuffer Buffer);

	private native int jAerMoveEnable(int axis);

	private native int jAerMoveDisable(int axis);

	private native int jAerMoveAbsolute(int axis, double target, int speed);

	private native int jAerMoveRelative(int axis, double steps, int speed);

	private native int jAerMoveHome(int axis);

	private native int jAerMoveAbort(int axis);

	private native int jAerMoveHalt(int axis);

	// private native int jAerWaitMoveDone(int axis, int waitMsecs);

	private native int jAerGetMotorCmdPos(int axis, double[] position);

	private native int jAerGetMotorEncPos(int axis, double[] position);

	private native int jAerSetMotorPos(int axis, double position);

	// private native int jAerSetMotorSpeed(int axis, double speed);

	// private native int jAerGetMotorSpeed(int axis, double[] position);

	private native int jAerGetAxisStatusWord(int axis, int[] statusWord);

	private native int jAerIsAxisHomed(int axis);

	// private native int jAerCNCCommandRun(String cncComand, int
	// taskNumber);

	// private native int jAerCNCFileRun(String cncFile, int taskNumber);

	// Load the 'jAerotech3200.dll" library which interfaces this Java class
	// to the Aerotech 3200 system Windows dll file via the
	// Java native interface.
	static {
		try {
			System.loadLibrary("jAerotech3200");
			logger.info("Loaded jAerotech3200.dll file");
		} catch (Exception e) {
			logger.error("Error loading jAerotech3200.dll file");
		}
	}

	/**
	 * Constructor.
	 */
	public Aerotech3200Controller() {
	}

	@Override
	public void configure() {
		this.initialiseSystem();
	}

	@Override
	public void finalize() {
		logger.info("Closing connection to Aerotech server");
		jAerSysClose();
	}

	/**
	 * @return Returns the numMotors.
	 */
	public int getNumMotors() {
		return numMotors;
	}

	/**
	 * @param numMotors
	 *            The numMotors to set.
	 */
	public void setNumMotors(int numMotors) {
		this.numMotors = numMotors;
	}

	/**
	 * @return Returns the paramFile.
	 */
	public String getParamFile() {
		return paramFile;
	}

	/**
	 * @param paramFile
	 *            The paramFile to set.
	 */
	public void setParamFile(String paramFile) {
		this.paramFile = paramFile;
	}

	/**
	 * Initialise the Aerotech 3200 software system - takes a few seconds
	 */
	private void initialiseSystem() {
		int error;
		StringBuffer buffer = null;

		if (SERVERON == jAerSysServerIsOpen()) {
			// Server is already running no need to re-iniitialise
			logger.info("Opening communicaton with Aerotech3200 software");
			error = jAerSysOpen();
			if (error != NOERROR) {
				jAerGetErrorString(error, buffer);
				logger.warn("Error opening Aerotech handle - " + buffer);
			}
		} else {
			logger.info("Initialising Aerotech3200 software - please wait here");
			error = jAerSysInitialise(paramFile);
			logger.info("Aerotech3200 software initialised");
			if (error != NOERROR) {
				// Negative error code values are returned when starting up from
				// scratch jAerGetErrorString cannot handle them.
				// Thank you Aerotech.
				if (error < NOERROR) {
					logger.warn("Initialising Aerotech software - returned a negative error" + error);
				} else {
					jAerGetErrorString(error, buffer);
					logger.warn("Error initialising Aerotech 3200 software - " + buffer);
				}
			}
		}
		// Enable motors
		for (int i = 0; i < numMotors; i++) {
			error = jAerMoveEnable(i);
			if (error > NOERROR) {
				jAerGetErrorString(error, buffer);
				logger.info("Error enabling Aerotech axis " + i + " - " + buffer);

			}
		}

	}

	/**
	 * Stops the Aerotech 3200 software system
	 *
	 * @throws DeviceException
	 */
	public void stopSystem() throws DeviceException {
		// Stop Aerotech system
		StringBuffer buffer = null;
		int error = NOERROR;
		for (int i = 1; i < numMotors; i++) {
			jAerMoveDisable(i);
			if (error != NOERROR) {
				jAerGetErrorString(error, buffer);
				throw new DeviceException("Error disabling Aerotech axis " + i + " - " + buffer);
			}
		}
		error = jAerSysStop();
		if (error != NOERROR) {
			jAerGetErrorString(error, buffer);
			throw new DeviceException("Error stopping Aerotech 3200 software - " + buffer);
		}
	}

	/**
	 * Moves chosen motor axis to absolute target position at requested speed
	 *
	 * @param axis
	 *            the axis
	 * @param target
	 *            the absolute target position
	 * @param speed
	 *            the requested speed
	 * @throws InterruptedException
	 */
	public void moveAbsolute(int axis, double target, int speed) throws InterruptedException {
		// StringBuffer buffer = null;
		int i;
		int error;

		for (i = 0; i < 10; i++) {
			error = jAerMoveAbsolute(axis, target, speed);

			// FIXME - when Aerotech sort out their negative error problem
			// we can
			// remove this retry bit
			if (error < 0) {
				logger.debug("!!!!!!!!!!!!!!!!!!!!NEGATIVE ERROR!!!!!!!!!!!!!!!");
			}
			if (error > NOERROR) {
				Thread.sleep(2000);
				/*
				 * int option = JOptionPane .showOptionDialog( frame, i + "tries, error code" + error, "Motor error
				 * warning", JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE, null, new String[]{"OK"}, "OK");
				 * jAerGetErrorString(error, buffer); throw new DeviceException( "XXXXXXXXXXXXXXError during absolute
				 * move on Aerotech motor " + axis + " - "
				 */
			} else {
				i = 11;
			}
		}
	}

	/**
	 * Moves chosen motor axis by required steps at requested speed
	 *
	 * @param axis
	 *            the axis
	 * @param steps
	 *            the required number of steps
	 * @param speed
	 *            the requested speed
	 * @throws DeviceException
	 *             on error
	 */
	public void moveRelative(int axis, double steps, int speed) throws DeviceException {
		StringBuffer buffer = null;
		int error = jAerMoveRelative(axis, steps, speed);
		if (error != NOERROR) {
			jAerGetErrorString(error, buffer);
			throw new DeviceException("Error during absolute move on Aerotech motor " + axis + " - " + buffer);
		}
	}

	/**
	 * Performs emergency abort on chosen motor axis
	 *
	 * @param axis
	 *            the axis
	 * @throws DeviceException
	 *             on error
	 */
	public void abortMove(int axis) throws DeviceException {
		StringBuffer buffer = null;
		int error = jAerMoveAbort(axis);
		if (error != NOERROR) {
			jAerGetErrorString(error, buffer);
			throw new DeviceException("Error during abort move on Aerotech motor " + axis + " - " + buffer);
		}
	}

	/**
	 * Homes chosen motor axis movemeint
	 *
	 * @param axis
	 *            the axis
	 * @throws DeviceException
	 *             on error
	 */
	public void moveHome(int axis) throws DeviceException {
		StringBuffer buffer = null;
		int error;

		/*
		 * error = jAerMoveEnable(axis); if (error > NOERROR) { jAerGetErrorString(error, buffer); Message.info("Homing
		 * error at enable stage - error is " + error + " which is supposed to mean: " + buffer); throw new
		 * DeviceException( "Error enabling motor for home move on Aerotech motor " + axis + " - " + buffer); }
		 */

		error = jAerMoveHome(axis);
		if (error > NOERROR) {
			jAerGetErrorString(error, buffer);
			logger.warn("Homing error at enable stage - error is " + error + " which is supposed to mean: " + buffer);
			throw new DeviceException("Error during home move on Aerotech motor " + axis + " - " + buffer);
		}
	}

	/**
	 * Performs controlled halt on chosen motor axis movement
	 *
	 * @param axis
	 *            the axis
	 * @throws DeviceException
	 *             on error
	 */
	public void moveHalt(int axis) throws DeviceException {
		StringBuffer buffer = null;
		int error = jAerMoveHalt(axis);
		if (error != NOERROR) {
			jAerGetErrorString(error, buffer);
			throw new DeviceException("Error during halt move on Aerotech motor " + axis + " - " + buffer);
		}
	}

	/**
	 * Retruns current motor position of chosen motor axis
	 *
	 * @param axis
	 *            the axis
	 * @return the current motor position
	 * @throws DeviceException
	 *             on error
	 */
	public double getPosition(int axis) throws DeviceException {

		StringBuffer buffer = null;
		// String response = null;
		double[] position = new double[1];
		int[] statusWord = new int[1];

		// Get motor status
		int error = jAerGetAxisStatusWord(axis, statusWord);
		if (error != NOERROR) {
			jAerGetErrorString(error, buffer);
			throw new DeviceException("Error getting status of Aerotech motor " + axis + " - " + buffer);
		}
		// If motor moving return encoder position
		if (statusWord[0] == this.BUSY) {
			error = jAerGetMotorEncPos(axis, position);
			if (error != NOERROR) {
				jAerGetErrorString(error, buffer);
				throw new DeviceException("Error getting encoder position of Aerotech motor " + axis + " - " + buffer);
			}
		}
		// return command position

		error = jAerGetMotorCmdPos(axis, position);
		if (error != NOERROR) {
			jAerGetErrorString(error, buffer);
			throw new DeviceException("Error getting command position of Aerotech motor " + axis + " - " + buffer);
		}
		return position[0];

	}

	/**
	 * Sets motor position of chosen motor axis to input value
	 *
	 * @param axis
	 *            the axis
	 * @param value
	 *            the input value
	 * @throws DeviceException
	 *             on error
	 */
	public void setPosition(int axis, double value) throws DeviceException {
		StringBuffer buffer = null;

		int error = jAerSetMotorPos(axis, value);
		if (error != NOERROR) {
			jAerGetErrorString(error, buffer);
			throw new DeviceException("Error setting position of Aerotech motor " + axis + " - " + buffer);
		}
	}

	/**
	 * Returns a string description of the input axis status
	 *
	 * @param axis
	 *            motor axis of interest
	 * @return the input axis status string
	 * @throws DeviceException
	 */

	public String getStatusString(int axis) throws DeviceException {
		StringBuffer buffer = null;
		String response = null;
		int[] statusWord = new int[1];

		int error = jAerGetAxisStatusWord(axis, statusWord);
		if (error != NOERROR) {
			jAerGetErrorString(error, buffer);
			throw new DeviceException("Error getting status of Aerotech motor " + axis + " - " + buffer);
		}
		if (statusWord[0] == this.CWLIMIT) {
			response = "UPPERLIMIT";
		} else if (statusWord[0] == this.CCWLIMIT) {
			response = "LOWERLIMIT";
		} else if (statusWord[0] == this.BUSY) {
			response = "BUSY";
		} else if (statusWord[0] == this.READY) {
			response = "READY";
		}
		return response;
	}

	/**
	 * Returns true if motor has been homed
	 *
	 * @param axis
	 *            the axis
	 * @return true if motor has been homed
	 */
	public boolean isHomed(int axis) {
		boolean homed = false;
		if (MOTORHOMED == jAerIsAxisHomed(axis)) {
			homed = true;
		}
		return homed;
	}

}
