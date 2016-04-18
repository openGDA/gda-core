/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package gda.device.temperature;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.ScannableUtils;
import gda.factory.FactoryException;

/**
 * A Dummy version of the class for controlling a Lakeshore 336 temperature controller through the EPICS interface.
 * <p>
 * The dummy version returns random temperatures between 0K and 500K, and random heating percentage.
 * <p>
 * Device online manual http://www.lakeshore.com/Documents/336_Manual.pdf
 * <p>
 * The class was originally written for use on i05 but is also used on i09
 *
 * @author James Mudd
 */
public class DummyEpicsLakeshore336 extends ScannableBase {

	private static final Logger logger = LoggerFactory.getLogger(DummyEpicsLakeshore336.class);

	// Random to be used to generate dummy data
	Random random = new Random();

	/**
	 * The allowed difference between the measured and demand temperatures for isBusy to return false
	 */
	private double tolerance = 0.05;

	/**
	 * If false the device never blocks
	 */
	private boolean blocking = true;

	/**
	 * This is the output currently controlled must always be between 1 and 4 inclusive
	 */
	private int activeOutput = 1;

	// These fields are just used to dummy the real device
	private int input;
	private double demandTemperaure;
	private double manualOutput;
	private double p;
	private double i;
	private double d;
	private int heaterRange;
	private double rampRate;
	private boolean rampEnabled;

	public double getTolerance() {
		return tolerance;
	}

	public void setTolerance(double tolerance) {
		this.tolerance = tolerance;
	}

	@Override
	public void configure() throws FactoryException {
		super.configure();
	}

	/**
	 * Checks if the Lakeshore is still busy to allow blocking.
	 * <p>
	 * If {@link #isBlocking()} is false this will always return <code>false</code><br>
	 * If the heater for the currently active loop is off this will return <code>false</code><br>
	 * If the current demand has not yet reached the target demand this will return <code>true</code><br>
	 * If abs(controlled temperature - target temperature) > {@link #getTolerance()} will return <code>true</code><br>
	 *
	 * @see #disableBlocking()
	 * @see #enableBlocking()
	 */
	@Override
	public boolean isBusy() throws DeviceException {
		// Check if blocking is disabled and return not busy
		if (blocking == false) {
			return false;
		}
		try {
			// If the heater is off device is not busy
			if (getHeaterRange() == 0) {
				return false;
			}
			// Special targeted demand of 0 means never busy
			if (getTargetDemandTemperature() == 0.0) {
				return false;
			}
			// Check if the current demand temperature has reached the target yet, if not the device is busy.
			// This is critical if ramping is enabled, but also useful to check it the Lakeshore has actually set the new demand yet.
			if (getCurrentDemandTemperature() != getTargetDemandTemperature()) {
				return true;
			}
			// Check if the temperature has reached the demanded temperature within tolerance
			return Math.abs(getTargetDemandTemperature() - getControlledTemperature()) > tolerance;
		} catch (Exception e) {
			logger.error("Error determining if Lakeshore 336 is busy", e);
			throw new DeviceException("Error reading from Lakeshore 336 Temperature Controller device", e);
		}
	}

	@Override
	public double[] getPosition() throws DeviceException {
		// Check the lengths are matched
		if (outputFormat.length != (inputNames.length + extraNames.length)) {
			logger.error("outputFormat.length != (inputNames.length + extraNames.length)");
			throw new DeviceException("outputFormat.length != (inputNames.length + extraNames.length)");
		}

		double[] pos = new double[outputFormat.length];
		// demand value
		pos[0] = getCurrentDemandTemperature();

		// get the temperatures
		for (int i = 1; i < (pos.length) - 2; i++) {
			pos[i] = getTemperature(i - 1);
		}

		// Add the heater settings
		pos[pos.length - 2] = getHeaterPercent();
		pos[pos.length - 1] = getHeaterRange();

		return pos;
	}

	@Override
	public void asynchronousMoveTo(Object externalPosition) throws DeviceException {
		Double[] doubles = ScannableUtils.objectToArray(externalPosition);
		setDemandTemperature(doubles[0]);
	}

	/**
	 * Gets the current heater output percentage from the currently active output.
	 *
	 * @return The current heater output percentage
	 * @throws DeviceException
	 */
	public double getHeaterPercent() throws DeviceException {
		try {
			return random.nextDouble() * 100; // In percent
		} catch (Exception e) {
			logger.error("Error trying to get heater percentage for output {}", activeOutput, e);
			throw new DeviceException("Error reading from Lakeshore 336 Temperature Controller device", e);
		}
	}

	/**
	 * Gets the current temperature from the specified channel.
	 * <p>
	 * Channel is between 0 and 3 inclusive
	 *
	 * @param channel
	 *            The channel to get
	 * @return The current temperature in K
	 * @throws DeviceException
	 */
	public double getTemperature(int channel) throws DeviceException {
		if (channel < 0 || channel > 3) {
			logger.error("Temperature channel must be between 0 and 3 inclusive");
			throw new DeviceException("Temperature channel must be between 0 and 3 inclusive");
		} else {
			try {
				return random.nextDouble() * 500; // Produces temperatures between 0 and 500 K
			} catch (Exception e) {
				logger.error("Error trying to get temperature for input {}", channel, e);
				throw new DeviceException("Error reading from Lakeshore 336 Temperature Controller device", e);
			}
		}
	}

	/**
	 * Gets the current temperature of the input used for the currently active output.
	 *
	 * @return The current temperature of the controlled input in K
	 * @throws DeviceException
	 */
	public double getControlledTemperature() throws DeviceException {
		try {
			// The channels are 1 less than the input (I know confusing!)
			int channel = getInput() - 1;
			if (channel == -1) {
				logger.error("Asked for the controlled temperature when the controlled input is none!");
				throw new DeviceException("Asked for the controlled temperature when the controlled input is none!");
			} else {
				return getTemperature(channel);
			}
		} catch (Exception e) {
			logger.error("Error trying to get the currently controlled temperature", e);
			throw new DeviceException("Error reading from Lakeshore 336 Temperature Controller device", e);
		}
	}

	/**
	 * Gets the input channel selected for the currently active output
	 * <p>
	 * 0 = None<br>
	 * 1 = Input 1 = Channel 0<br>
	 * 2 = Input 2 = Channel 1<br>
	 * 3 = Input 3 = Channel 2<br>
	 * 4 = Input 4 = Channel 2<br>
	 *
	 * @return The input selected for the currently active output
	 * @throws DeviceException
	 */
	public int getInput() throws DeviceException {
		try {
			return input;
		} catch (Exception e) {
			logger.error("Error trying to get the input for output {}", activeOutput, e);
			throw new DeviceException("Error reading from Lakeshore 336 Temperature Controller device", e);
		}
	}

	/**
	 * Sets the input channel selected for the currently active output
	 * <p>
	 * 0 = None<br>
	 * 1 = Input 1 = Channel 0<br>
	 * 2 = Input 2 = Channel 1<br>
	 * 3 = Input 3 = Channel 2<br>
	 * 4 = Input 4 = Channel 2<br>
	 *
	 * @param input
	 *            The input to use for the control
	 * @throws DeviceException
	 */
	public void setInput(int input) throws DeviceException {
		if (input < 0 || input > 4) {
			logger.error("The control input must be between 0 and 4 inclusive");
			throw new DeviceException("The control input must be between 0 and 4 inclusive");
		} else {
			try {
				this.input = input;
			} catch (Exception e) {
				logger.error("Error trying to set input to {} for output {}", input, activeOutput, e);
				throw new DeviceException("Error setting value in Lakeshore 336 Temperature Controller device", e);
			}
		}
	}

	/**
	 * Sets the demand temperature for the currently active output. If ramping is enabled then the actual demand temperature will move towards this at the
	 * ramping rate.
	 *
	 * @param demandTemperature
	 *            The demanded temperature in K
	 * @throws DeviceException
	 * @see #enableRamping()
	 * @see #disableRamping()
	 * @see #setRampRate(double)
	 */
	public void setDemandTemperature(double demandTemperature) throws DeviceException {
		try {
			logger.info("Setting demand temperature to {} for ouput {}", demandTemperature, activeOutput);
			this.demandTemperaure = demandTemperature;
		} catch (Exception e) {
			logger.error("Error setting the demand temperature to {} for output {}", demandTemperature, activeOutput, e);
			throw new DeviceException("Error setting value in Lakeshore 336 Temperature Controller device", e);
		}
	}

	/**
	 * Gets the current demand temperature for the active output in K. If ramping is disabled this should be equal to the target demand temperature. If ramping
	 * is enabled this will follow the target demand temperature at the set rate.
	 *
	 * @return The current demand temperature from the readback value
	 * @throws DeviceException
	 * @see #enableRamping()
	 * @see #disableRamping()
	 * @see #setRampRate(double)
	 */
	public double getCurrentDemandTemperature() throws DeviceException {
		try {
			return demandTemperaure;
		} catch (Exception e) {
			logger.error("Error getting current demand temperature for output {}", activeOutput, e);
			throw new DeviceException("Error reading from Lakeshore 336 Temperature Controller device", e);
		}
	}

	/**
	 * Gets the targeted demand temperature for the active output in K. If ramping is disabled this should be equal to the target demand temperature. If ramping
	 * is enabled this will follow the target demand temperature at the set rate.
	 *
	 * @return The target demand temperature set
	 * @throws DeviceException
	 * @see #setDemandTemperature(double)
	 * @see #enableRamping()
	 * @see #disableRamping()
	 * @see #setRampRate(double)
	 */
	public double getTargetDemandTemperature() throws DeviceException {
		try {
			return demandTemperaure;
		} catch (Exception e) {
			logger.error("Error getting target demand temperature for output {}", activeOutput, e);
			throw new DeviceException("Error reading from Lakeshore 336 Temperature Controller device", e);
		}
	}

	/**
	 * Sets the manual output for the currently active output.
	 *
	 * @param manualOutput
	 *            The requested manual output
	 * @throws DeviceException
	 */
	public void setManualOutput(double manualOutput) throws DeviceException {
		try {
			logger.info("Setting manual output to {} for ouput {}", manualOutput, activeOutput);
			this.manualOutput = manualOutput;
		} catch (Exception e) {
			logger.error("Error setting manual output to {} for output {}", manualOutput, activeOutput, e);
			throw new DeviceException("Error setting value in Lakeshore 336 Temperature Controller device", e);
		}
	}

	/**
	 * Gets the manual output for the currently active output.
	 *
	 * @return The current manual output
	 * @throws DeviceException
	 */
	public double getManualOutput() throws DeviceException {
		try {
			return manualOutput;
		} catch (Exception e) {
			logger.error("Error getting manual output for output {}", activeOutput, e);
			throw new DeviceException("Error reading from Lakeshore 336 Temperature Controller device", e);
		}
	}

	/**
	 * Sets the proportional term for the PID control, for the currently active output.
	 *
	 * @param demandP
	 * @throws DeviceException
	 */
	public void setP(double demandP) throws DeviceException {
		try {
			logger.info("Setting P to {} for ouput {}", demandP, activeOutput);
			this.p = demandP;
		} catch (Exception e) {
			logger.error("Error setting P to {} for output {}", demandP, activeOutput, e);
			throw new DeviceException("Error setting value in Lakeshore 336 Temperature Controller device", e);
		}
	}

	/**
	 * Gets the proportional term for the PID control, for the currently active output.
	 *
	 * @return Proportional term
	 * @throws DeviceException
	 */
	public double getP() throws DeviceException {
		try {
			return p;
		} catch (Exception e) {
			logger.error("Error getting P for output {}", activeOutput, e);
			throw new DeviceException("Error reading from Lakeshore 336 Temperature Controller device", e);
		}
	}

	/**
	 * Sets the integral term for the PID control, for the currently active output.
	 *
	 * @param demandI
	 * @throws DeviceException
	 */
	public void setI(double demandI) throws DeviceException {
		try {
			logger.info("Setting I to {} for ouput {}", demandI, activeOutput);
			this.i = demandI;
		} catch (Exception e) {
			logger.error("Error setting I to {} for output {}", demandI, activeOutput, e);
			throw new DeviceException("Error setting value in Lakeshore 336 Temperature Controller device", e);
		}
	}

	/**
	 * Gets the integral term for the PID control, for the currently active output.
	 *
	 * @return Integral term
	 * @throws DeviceException
	 */
	public double getI() throws DeviceException {
		try {
			return i;
		} catch (Exception e) {
			logger.error("Error getting I for output {}", activeOutput, e);
			throw new DeviceException("Error reading from Lakeshore 336 Temperature Controller device", e);
		}
	}

	/**
	 * Sets the derivative term for the PID control, for the currently active output.
	 *
	 * @param demandD
	 * @throws DeviceException
	 */
	public void setD(double demandD) throws DeviceException {
		try {
			logger.info("Setting D to {} for ouput {}", demandD, activeOutput);
			this.d = demandD;
		} catch (Exception e) {
			logger.error("Error setting D to {} for output {}", demandD, activeOutput, e);
			throw new DeviceException("Error setting value in Lakeshore 336 Temperature Controller device", e);
		}
	}

	/**
	 * Gets the derivative term for the PID control, for the currently active output.
	 *
	 * @return Derivative term
	 * @throws DeviceException
	 */
	public double getD() throws DeviceException {
		try {
			return d;
		} catch (Exception e) {
			logger.error("Error getting D for output {}", activeOutput, e);
			throw new DeviceException("Error reading from Lakeshore 336 Temperature Controller device", e);
		}
	}

	/**
	 * Sets the heater range for the currently active output.
	 * <p>
	 * 0=Off, 1=Low, 2=Med, 3=High
	 *
	 * @param heaterRange
	 *            The demanded heater range
	 * @throws DeviceException
	 */
	public void setHeaterRange(int heaterRange) throws DeviceException {
		if (heaterRange < 0 || heaterRange > 3) {
			logger.error("The heater range must be between 0 and 3 (Off, Low, Med, High)");
			throw new DeviceException("The heater range must be between 0 and 3 (Off, Low, Med, High)");
		} else {
			try {
				logger.info("Setting heater range to {} for output loop {}", heaterRange, activeOutput);
				this.heaterRange = heaterRange;
			} catch (Exception e) {
				logger.error("Error trying to set heater range to {} for output {}", heaterRange, activeOutput, e);
				throw new DeviceException("Error setting value in Lakeshore 336 Temperature Controller device", e);
			}
		}
	}

	/**
	 * Gets the heater range for the currently active output.
	 * <p>
	 * 0=Off, 1=Low, 2=Med, 3=High
	 *
	 * @return The heater range
	 * @throws DeviceException
	 */
	public int getHeaterRange() throws DeviceException {
		try {
			return heaterRange;
		} catch (Exception e) {
			logger.error("Error getting heater range for output {}", activeOutput, e);
			throw new DeviceException("Error reading from Lakeshore 336 Temperature Controller device", e);
		}
	}

	/**
	 * Sets the ramping rate in K/sec for the currently active output.
	 *
	 * @param rampRate
	 *            The desired ramping rate in K/sec
	 * @throws DeviceException
	 * @see #enableRamping()
	 * @see #disableRamping()
	 */
	public void setRampRate(double rampRate) throws DeviceException {
		try {
			logger.info("Setting ramping rate to {} for output loop {}", rampRate, activeOutput);
			this.rampRate = rampRate;
		} catch (Exception e) {
			logger.error("Error trying to set ramp rate {} K/s for output {}", rampRate, activeOutput, e);
			throw new DeviceException("Error setting value in Lakeshore 336 Temperature Controller device", e);
		}
	}

	/**
	 * Get the ramping rate for the currently active output in K/sec.
	 *
	 * @return The ramping rate for the currently active output in K/sec
	 * @throws DeviceException
	 */
	public double getRampRate() throws DeviceException {
		try {
			return rampRate;
		} catch (Exception e) {
			logger.error("Error getting ramping rate for output {}", activeOutput, e);
			throw new DeviceException("Error reading from Lakeshore 336 Temperature Controller device", e);
		}
	}

	/**
	 * Enables or disables ramping for the currently active output.
	 *
	 * @param rampEnabled
	 * @throws DeviceException
	 * @see #enableRamping()
	 * @see #disableRamping()
	 */
	public void setRampEnabled(boolean rampEnabled) throws DeviceException {
		try {
			logger.info("Setting ramping to {} for output loop {}", rampEnabled, activeOutput);
			this.rampEnabled = rampEnabled;
		} catch (Exception e) {
			logger.error("Error trying to set ramping {} for output {}", rampEnabled, activeOutput, e);
			throw new DeviceException("Error setting value in Lakeshore 336 Temperature Controller device", e);
		}
	}

	/**
	 * Finds out if ramping is enabled on the current active output.
	 *
	 * @return The current ramping status
	 * @throws DeviceException
	 */
	public boolean isRampEnabled() throws DeviceException {
		try {
			return rampEnabled;
		} catch (Exception e) {
			logger.error("Error getting ramping enabled status for output {}", activeOutput, e);
			throw new DeviceException("Error reading from Lakeshore 336 Temperature Controller device", e);
		}

	}

	/**
	 * Switches ramping on for the currently active output.
	 *
	 * @throws DeviceException
	 * @see #disableRamping()
	 */
	public void enableRamping() throws DeviceException {
		setRampEnabled(true);
	}

	/**
	 * Switches ramping off for the currently active output.
	 *
	 * @throws DeviceException
	 * @see #enableRamping()
	 */
	public void disableRamping() throws DeviceException {
		setRampEnabled(false);
	}

	/**
	 * Switches blocking on. When the temperature is changed the device will be busy until the ramping finishes (if enabled) and the demand temperature is
	 * reached (within tolerance).
	 *
	 * @see #disableBlocking()
	 */
	public void enableBlocking() {
		logger.info("Enabled blocking");
		blocking = true;
	}

	/**
	 * Switches blocking off. <code>isBusy()</code> will always return <code>false</code>. This allows the temperature to be recorded at every point in the scan
	 * without waiting for a demand temperature to be reached, or to start a temperature ramp in a script and then perform other operations.
	 *
	 * @see #enableBlocking()
	 */
	public void disableBlocking() {
		logger.info("Disabled blocking");
		blocking = false;
	}

	/**
	 * Finds out if the Lakeshore is currently blocking.
	 *
	 * @return current blocking status
	 * @see #disableBlocking()
	 * @see #enableBlocking()
	 */
	public boolean isBlocking() {
		return this.blocking;
	}

	/**
	 * Gets the output which is currently being controlled by GDA
	 *
	 * @return The currently active output
	 */
	public int getActiveOutput() {
		return activeOutput;
	}

	/**
	 * Sets the output to be controlled by GDA between 1 and 4
	 * <p>
	 * <b>Switching active output does not switch off other outputs it just changes which output GDA is talking to.</b>
	 *
	 * @param activeOutput
	 *            The output to control
	 * @throws DeviceException
	 */
	public void setActiveOutput(int activeOutput) throws DeviceException {
		if (activeOutput < 1 || activeOutput > 4) {
			logger.error("The active output must be between 1 and 4");
			throw new DeviceException("The active output must be between 1 and 4");
		}

		logger.info("Changed active output to {}", activeOutput);
		this.activeOutput = activeOutput;
	}

	/**
	 * Switches off the currently active output
	 *
	 * @throws DeviceException
	 */
	public void outputOff() throws DeviceException {
		setHeaterRange(0);
	}

	/**
	 * Switches off all the outputs
	 *
	 * @throws DeviceException
	 */
	public void allOutputsOff() throws DeviceException {
		// Store the current active output
		int originalActiveOutput = getActiveOutput();

		for (int i = 1; i <= 4; i++) {
			setActiveOutput(i);
			setHeaterRange(0);
		}

		// Reset active output
		setActiveOutput(originalActiveOutput);
	}

}