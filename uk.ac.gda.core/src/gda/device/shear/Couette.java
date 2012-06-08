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

package gda.device.shear;

import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.device.Serial;
import gda.device.Shear;
import gda.factory.FactoryException;
import gda.factory.Finder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to control the Couette shear cell
 */
public class Couette extends DeviceBase implements Runnable, Shear {
	
	private static final Logger logger = LoggerFactory.getLogger(Couette.class);
	
	private final static int IDLE = 0;

	private final static int CONTINUOUS = 1;

	private final static int OSCILLATORY = 2;

	private final static double MIN_SHEAR_RATE = 0.05;

	private final static double MAX_SHEAR_RATE = 1050.0;

	private final static double MIN_AMPLITUDE = 1.0;

	private final static double MAX_AMPLITUDE = 1800.0;

	private final static double DEFAULT_THICKNESS = 0.5;

	private final static double DEFAULT_RADIUS = 25.25;

	private final static int READ_TIMEOUT = 5000;

	private final static char ACK = 0x17;

	private final static char RST = 0x1E;

	private Thread runner;

	private Serial serial;

	private int shearing;

	private boolean torque;

	private double thickness;

	private double radius;

	private double trigger;

	private ShearStatus shearStatus;

	private String serialDeviceName;

	private String parity = Serial.PARITY_NONE;

	private int baudRate = Serial.BAUDRATE_9600;

	private int stopBits = Serial.STOPBITS_1;

	private int byteSize = Serial.BYTESIZE_8;

	/**
	 * Construct a Couette cell with default values.
	 */
	public Couette() {
		shearing = IDLE;
		torque = false;
		thickness = DEFAULT_THICKNESS;
		radius = DEFAULT_RADIUS;
		shearStatus = new ShearStatus();
		shearStatus.setCurrent(1.0);
		shearStatus.setGamma(1.0);
		shearStatus.setAmplitude(1.0);

		runner = uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName());
	}

	@Override
	public void configure() {
		logger.debug("Finding: " + serialDeviceName);
		if ((serial = (Serial) Finder.getInstance().find(serialDeviceName)) == null) {
			logger.error("Serial Device " + serialDeviceName + " not found");
		} else {
			try {
				serial.setBaudRate(baudRate);
				serial.setStopBits(stopBits);
				serial.setByteSize(byteSize);
				serial.setParity(parity);
				serial.setReadTimeout(READ_TIMEOUT);
				serial.flush();
				reset();
				runner.start();
				configured = true;
			} catch (DeviceException de) {
			}
		}
	}

	@Override
	public void reconfigure() throws FactoryException {
		if (!configured)
			configure();
	}

	@Override
	public void close() throws DeviceException {
		if (serial != null)
			serial.close();
		configured = false;
	}

	/**
	 * @param serialDeviceName
	 */
	public void setSerialDeviceName(String serialDeviceName) {
		this.serialDeviceName = serialDeviceName;
	}

	/**
	 * @return serialDeviceName
	 */
	public String getSerialDeviceName() {
		return serialDeviceName;
	}

	/**
	 * @param radius
	 */
	public void setRadius(double radius) {
		this.radius = radius;
	}

	@Override
	public double getRadius() throws DeviceException {
		return radius;
	}

	/**
	 * @param thickness
	 */
	public void setThickness(double thickness) {
		this.thickness = thickness;
	}

	@Override
	public double getThickness() throws DeviceException {
		return thickness;
	}

	@Override
	public double getShearRate() {
		return shearStatus.getGamma();
	}

	@Override
	public double getAmplitude() {
		return shearStatus.getAmplitude();
	}

	@Override
	public double getTorque() {
		return shearStatus.getCurrent();
	}

	@Override
	public void continuousShear(double gamma) throws DeviceException {
		if (shearing == OSCILLATORY)
			throw new DeviceException("Couette cell is in oscillatory shear mode");

		else if (gamma < MIN_SHEAR_RATE || gamma > MAX_SHEAR_RATE)
			throw new DeviceException("Shear rate out of range");

		double velocity = 60.0 * thickness * gamma / (Math.PI * 2.0 * radius);
		if (shearStatus.getGamma() != gamma) {
			shearStatus.setGamma(gamma);
			synchronized (this) {
				notify();
			}
		}

		if (shearing == IDLE) {
			trigger = 1.0;
			if (!torque)
				torqueSet(1.0);
		}

		// run PGM101 in Unidex: Accelerate to speed for high shear rates
		// else Step directly to speed for low shear rates

		if (velocity > 1.0) {
			modifyFloatVariable(10, trigger);
			if (shearing == IDLE)
				runStoredProgram(101);

			Double d = new Double((velocity - trigger) / 2.0);
			int ninc = Math.abs(d.intValue());
			double pedal = (velocity > trigger) ? 2.0 : -2.0;

			for (int i = 0; i < ninc; i++) {
				trigger += pedal;
				modifyFloatVariable(10, trigger);
			}
			if (trigger != velocity) {
				trigger = velocity;
				modifyFloatVariable(10, trigger);
			}
		} else {
			modifyFloatVariable(10, velocity);
			trigger = velocity;
			if (shearing == IDLE)
				runStoredProgram(101);
		}
		shearing = CONTINUOUS;
	}

	@Override
	public void oscillatoryShear(double gamma, double amplitude) throws DeviceException {
		if (shearing == CONTINUOUS)
			throw new DeviceException("Couette cell is in contiuous shear mode");

		if (gamma < MIN_SHEAR_RATE || gamma > MAX_SHEAR_RATE)
			throw new DeviceException("Shear rate out of range");
		if (amplitude < MIN_AMPLITUDE || amplitude > MAX_AMPLITUDE)
			throw new DeviceException("Shear amplitude out of range");

		double velocity = (4000.0 * gamma * thickness) / (Math.PI * 2.0 * radius);
		double moveOne = (amplitude / 360.0) * 4000.0;
		if (moveOne < 0)
			moveOne = -moveOne;
		double moveTwo = -2 * moveOne;
		double moveThree = 2 * moveOne;
		double moveFour = -moveOne;
		if (shearStatus.getGamma() != gamma || shearStatus.getAmplitude() != amplitude) {
			shearStatus.setGamma(gamma);
			shearStatus.setAmplitude(amplitude);
			synchronized (this) {
				notify();
			}
		}

		if (shearing == IDLE) {
			if (!torque)
				torqueSet(1.0);

			modifyFloatVariable(10, velocity);
			modifyFloatVariable(6, moveOne);
			modifyFloatVariable(11, moveTwo);
			modifyFloatVariable(12, moveThree);
			modifyFloatVariable(7, moveFour);
			runStoredProgram(102);
			shearing = OSCILLATORY;
		} else if (shearing == OSCILLATORY) {
			// Update parameters whilst oscillatory shearing in progress

			modifyFloatVariable(14, velocity);
			modifyFloatVariable(6, moveOne);
			modifyFloatVariable(15, moveTwo);
			modifyFloatVariable(16, moveThree);
			modifyFloatVariable(9, moveFour);
			modifyIntegerVariable(3, 1);
		}
	}

	@Override
	public void stopShear() throws DeviceException {
		if (shearing == CONTINUOUS) {
			// Modify the integer variable, BV:1 to 0. This will terminate
			// the while loop in PGM101.

			modifyIntegerVariable(1, 0);
		} else if (shearing == OSCILLATORY) {
			// Modify the integer variable, BV:2 to 0. This will terminate
			// the while loop in PGM102.

			modifyIntegerVariable(2, 0);
		}

		// Modify the value of the drive parameters for stepper running
		// current
		// and stepper holding current to 1amp and 0.5amps respectively.

		modifyDriveParameter(1, 1.0);
		modifyDriveParameter(2, 0.5);
		torque = false;
		shearing = IDLE;
	}

	@Override
	public void setTorque(double current) throws DeviceException {
		if (shearing != IDLE)
			throw new DeviceException("Cant't change torque whilst cell is shearing");

		if (current < 0.5 || current > 6.0)
			throw new DeviceException("Current rate out of range");

		torqueSet(current);
		if (shearStatus.getCurrent() != current) {
			shearStatus.setCurrent(current);
			logger.info("Setting couette torque amps to " + current);
			synchronized (this) {
				notify();

			}
		}
		torque = true;
	}

	private void torqueSet(double current) throws DeviceException {
		// Modify the value of the variables FV:1 and FV:2

		modifyFloatVariable(1, current);
		modifyFloatVariable(2, current / 2.0);
	}

	private void runStoredProgram(int progno) throws DeviceException {
		writeUnidex("#BAA" + progno);
		acksrq();
	}

	private void modifyDriveParameter(int parNum, double parVal) throws DeviceException {
		// All drive paramter are of the form PRM:2xx where xx is the parNum.
		writeUnidex("#FCAC" + parNum);
		writeUnidex("B" + parVal);
		acksrq();
	}

	private void modifyIntegerVariable(int varNum, int varVal) throws DeviceException {
		writeUnidex("#FCCA" + varNum);
		writeUnidex("B" + varVal);
		acksrq();
	}

	private void modifyFloatVariable(int varNum, double varVal) throws DeviceException {
		writeUnidex("#FCCC" + varNum);
		writeUnidex("B" + varVal);
		acksrq();
	}

	private void reset() throws DeviceException {
		logger.info("Resetting Couette");
		serial.writeChar(RST);
		acksrq();
	}

	private synchronized void acksrq() throws DeviceException {
		// Expect 3 character reply of the form (p12-2)
		// <SRQ character><SRQ status><compliment of SRQ status>

		for (int i = 0; i < 3; i++) {
			char ch = serial.readChar();
			logger.debug("Read from Unidex " + (int) ch);
		}
		serial.writeChar(ACK); // Send service request acknowledge
	}

	private synchronized void writeUnidex(String s) throws DeviceException {
		logger.debug("Sent to Unidex: " + s + ":");
		for (int i = 0; i < s.length(); i++) {
			serial.writeChar(s.charAt(i));
		}
		serial.writeChar('\r');
	}

	/**
	 * The run method implementing the Runnable interface Used to update observers of current paramters
	 */

	@Override
	public synchronized void run() {
		if (runner == null) {
			logger.warn("No thread for Couette monitor " + this.getName());
		}

		while (true) {
			try {
				wait();
				notifyIObservers(this, shearStatus);
			} catch (Exception e) {
			}
		}
	}

}
