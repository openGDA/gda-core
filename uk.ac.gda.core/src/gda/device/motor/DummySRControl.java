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

import gda.device.DeviceBase;
import gda.device.DeviceException;

/**
 * A simple device class for Windows systems to interface to the SR control system at DL. It uses the Java native
 * interface to provide access to the fastRPC.dll windows driver file supplied by the control group. Both the
 * fastRPC.dll and the Java wrapper dll, jFastRPC.dll, need to be in the windows system directory. The current user/pc
 * combination needs also needs to be registered with the control network.
 */
public class DummySRControl extends DeviceBase implements SRControlInterface {
	
	private static final Logger logger = LoggerFactory.getLogger(DummySRControl.class);
	
	// Unless these values are reset by XML an exception will be thrown
	// roughly
	// every 200th (1 / exceptionLevel) call of setValue or getValue. Once
	// this
	// has happened then exceptions will be thrown for the next 5
	// (numberOfBrokenExceptions) calls before returning to normal.
	private double exceptionLevel = 0.005;

	private int numberOfBrokenExceptions = 5;

	private int brokenCounter;

	private boolean simulatingBroken = false;

	private MotorTypeParameter[] mtps = { new MotorTypeParameter("TST.PARM.01"), new MotorTypeParameter("TST.PARM.02") };

	String[] arrayOne = { "NOTHING", "ENABLE", "BLOCK" };

	String[] arrayTwo = { "NOTHING", "MUTUAL", "OPPOSING" };

	private SwitchTypeParameter[] stps = { new SwitchTypeParameter("U5.USERS", arrayOne),
			new SwitchTypeParameter("U5.MODE", arrayTwo) };

	private int exceptionCounter = 0;

	@Override
	public void configure(){
		// no configuration required
	}

	/**
	 * Initialise call to Control network, not strictly necessary though can be used to see if network operating
	 * correctly.
	 * 
	 * @throws DeviceException
	 */
	@Override
	public void initialise() throws DeviceException {
	}

	/**
	 * Writes the given data value for the specified property of a control system parameter
	 * 
	 * @param parameter -
	 *            string containing name of control systeme parameter
	 * @param setProperty -
	 *            name of parameter's property to set (use public constants)
	 * @param data -
	 *            value to set, needs to be in a double array element
	 * @throws DeviceException
	 */
	@Override
	public void setValue(String parameter, String setProperty, double[] data) throws DeviceException {
		if (simulatingBroken) {
			if (++brokenCounter > numberOfBrokenExceptions) {
				simulatingBroken = false;
			}
			logger.error("throwing exception with message: "
					+ "DummySRControl setValue() simulating broken state brokenCounter is " + brokenCounter);
			throw new DeviceException("DummySRControl setValue() simulating broken state brokenCounter is "
					+ brokenCounter);
		}
		if (Math.random() < exceptionLevel) {
			simulatingBroken = true;
			brokenCounter = 0;
			logger.error("throwing exception with message: "
					+ "Randomly thrown by DummySRControl setValue() this is number " + (exceptionCounter + 1));
			throw new DeviceException("Randomly thrown by DummySRControl setValue() this is number "
					+ exceptionCounter++);
		}

		for (int i = 0; i < mtps.length; i++) {
			if (mtps[i].getName().equals(parameter)) {
				mtps[i].setValue(setProperty, data);
			}
		}
		for (int i = 0; i < stps.length; i++) {
			if (stps[i].getName().equals(parameter)) {
				stps[i].setValue(setProperty, data);
			}
		}
	}

	/**
	 * Reads the data value for the specified property of a control system parameter
	 * 
	 * @param parameter -
	 *            string containing name of control systeme parameter
	 * @param getProperty -
	 *            name of parameter's property to get (use public constants)
	 * @param data -
	 *            value to get, needs to be a double array element
	 * @throws DeviceException
	 */
	@Override
	public void getValue(String parameter, String getProperty, double[] data) throws DeviceException {
		if (simulatingBroken) {
			if (++brokenCounter > numberOfBrokenExceptions) {
				simulatingBroken = false;
			}
			logger.error("throwing exception with message: "
					+ "DummySRControl getValue() simulating broken state brokenCounter is " + brokenCounter);
			throw new DeviceException("DummySRControl getValue() simulating broken state brokenCounter is "
					+ brokenCounter);
		}
		if (Math.random() < exceptionLevel) {
			simulatingBroken = true;
			brokenCounter = 0;
			logger.error("throwing exception with message: "
					+ "Randomly thrown by DummySRControl getValue() this is number " + (exceptionCounter + 1));
			throw new DeviceException("Randomly thrown by DummySRControl getValue() this is number "
					+ exceptionCounter++);
		}

		for (int i = 0; i < mtps.length; i++) {
			if (mtps[i].getName().equals(parameter)) {
				data[0] = mtps[i].getValue(getProperty)[0];
			}
		}
		for (int i = 0; i < stps.length; i++) {
			if (stps[i].getName().equals(parameter)) {
				data[0] = stps[i].getValue(getProperty)[0];
			}
		}
	}

	/**
	 * Returns a string description of the input status code for the control parameter in question
	 * 
	 * @param parameter -
	 *            string containing name of control systeme parameter
	 * @param code -
	 *            value of parameter's status code
	 * @param statusString -
	 *            stringbuffer to return description in
	 * @param length -
	 *            length of statusString buffer passed in
	 * @throws DeviceException
	 */
	@Override
	public void getStatusString(String parameter, double code, StringBuffer statusString, int length)
			throws DeviceException {
		for (int i = 0; i < mtps.length; i++) {
			if (mtps[i].getName().equals(parameter)) {
				statusString.insert(0, mtps[i].getStatusString(code));
			}
		}
		for (int i = 0; i < stps.length; i++) {
			if (stps[i].getName().equals(parameter)) {
				statusString.insert(0, stps[i].getStatusString(code));
			}
		}
	}

	private class SwitchTypeParameter {
		private String name;

		private String[] statusStrings;

		private String[] setPropertyNames = { SET_VALUE, SET_STATUS, SET_MINVALUE, SET_MAXVALUE };

		private String[] getPropertyNames = { GET_VALUE, GET_STATUS, GET_MINVALUE, GET_MAXVALUE, GET_INTERLOCKS };

		private double[] properties = { 0.0, 1.0, 0.0, 0.0, 0.0 };

		/**
		 * @param name
		 * @param statusStrings
		 */
		public SwitchTypeParameter(String name, String[] statusStrings) {
			this.name = name;
			this.statusStrings = statusStrings;
			if (name.equals("U5.USERS"))
				properties[1] = 2.0; // Dummy "U5.USERS" motor always enabled
		}

		private String getName() {
			return name;
		}

		private void setValue(String valueName, double[] data) {
			if (valueName.equals(setPropertyNames[1])) {
				properties[1] = data[0];
			}
		}

		private synchronized double[] getValue(String valueName) {
			double[] values = new double[1];
			for (int i = 0; i < getPropertyNames.length; i++)
				if (valueName.equals(getPropertyNames[i]))
					values[0] = properties[i];
			return values;

		}

		private String getStatusString(double statusCode) {
			return (statusStrings[(int) statusCode]);
		}
	}

	private class MotorTypeParameter implements Runnable {
		private String[] statusStrings = { "READY", "BUSY" };

		private String[] setPropertyNames = { SET_VALUE, SET_STATUS, SET_MINVALUE, SET_MAXVALUE };

		private String[] getPropertyNames = { GET_VALUE, GET_STATUS, GET_MINVALUE, GET_MAXVALUE, GET_INTERLOCKS };

		private double[] properties = { 0.0, 0.0, 0.0, 0.0, 0.0 };

		private String name;

		private double targetPosition;

		private MotorTypeParameter(String name) {
			this.name = name;
			// Some BFI starting values for position

			if (name.equals("TST.PARM.01"))
				// properties[0] = -17.035;
				// This value is 0.0 within the ZERO_PHASE_TOLERANCE i.e. a
				// realistic value for an
				// undulator which has been left at 0.0
				properties[0] = -0.0005 + Math.random() * 0.001;
			else if (name.equals("TST.PARM.02"))
				properties[0] = 35.0;
		}

		private String getName() {
			return name;
		}

		private void setValue(String valueName, double[] data) {
			if (valueName.equals(setPropertyNames[0])) {
				// This is the equaivalent of doing a move
				if (data[0] != properties[0]) {
					targetPosition = data[0];
					logger.debug("DummySRControl " + name + " setting status to 1");
					properties[1] = 1;
					uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName() + " " + name).start();
				}
			} else if (valueName.equals(setPropertyNames[1])) {
				properties[1] = 0;
			} else {
				for (int i = 2; i < setPropertyNames.length; i++)
					if (valueName.equals(setPropertyNames[i]))
						properties[i] = data[0];
			}
		}

		private double[] getValue(String valueName) {
			double[] values = new double[1];
			for (int i = 0; i < getPropertyNames.length; i++)
				if (valueName.equals(getPropertyNames[i]))
					values[0] = properties[i];
			return values;

		}

		private String getStatusString(double statusCode) {
			return (statusStrings[(int) statusCode]);
		}

		@Override
		public synchronized void run() {
			double increment = (targetPosition - properties[0]) / 10.0;
			for (int i = 0; i < 10; i++) {
				try {
					wait(100);
				} catch (InterruptedException e) {
					// Deliberately do nothing
				}
				properties[0] += increment;
				logger.debug("DummySRControl " + name + " setting position to " + properties[0]);
			}

			// Add some noise of the correct level to the final position

			properties[0] = targetPosition - 0.0005 + Math.random() * 0.001;

			logger.debug("DummySRControl " + name + " position is now " + properties[0]);
			properties[1] = 0;
			logger.debug("DummySRControl " + name + " status is now 0");
		}
	}

	@Override
	public void getUnitsString(String parameter, StringBuffer unitsString, int length) throws DeviceException {
	}

	@Override
	public byte getDecimalPlaces(String parameter) {
		return 0;
	}

	/**
	 * Get the value used in the test to determine whether an exception is thrown.
	 * 
	 * @return exception level value which should be between 0 and 1
	 */
	public double getExceptionLevel() {
		return exceptionLevel;
	}

	/**
	 * Sets the value used in the test to determine whether an exception is thrown. An exception will be thrown roughly
	 * every 1/exceptionLevel calls to setValue and getValue.
	 * 
	 * @param exceptionLevel
	 *            a value between 0 (off) and 1.
	 */
	public void setExceptionLevel(double exceptionLevel) {
		this.exceptionLevel = exceptionLevel;
	}

	/**
	 * Get the number of broken exceptions raised.
	 * 
	 * @return the number of broken exception
	 */
	public int getNumberOfBrokenExceptions() {
		return numberOfBrokenExceptions;
	}

	/**
	 * Set the number of broken exceptions raised.
	 * 
	 * @param numberOfBrokenExceptions
	 */
	public void setNumberOfBrokenExceptions(int numberOfBrokenExceptions) {
		this.numberOfBrokenExceptions = numberOfBrokenExceptions;
	}
}