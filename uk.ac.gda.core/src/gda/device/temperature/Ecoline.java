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

package gda.device.temperature;

import gda.device.DeviceException;
import gda.device.Serial;
import gda.device.TemperatureRamp;
import gda.device.TemperatureStatus;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.util.PollerEvent;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to control the Integra Compact Low-Temperature Thermostat.
 */
public class Ecoline extends TemperatureBase {

	private static final Logger logger = LoggerFactory.getLogger(Ecoline.class);

	private double startTime = 0;

	private final static double MAXTEMP = 200.0;

	private final static double MINTEMP = -35.0;

	private final static int READ_TIMEOUT = 1000;

	private final static String OKREPLY = "OK\r\n";

	private Serial serial;

	private String serialDeviceName;

	private int probe = 0;

	private NumberFormat nf;

	private static final int dec_places = 2;

	private static final int int_places = 3;

	private AsynchronousReaderWriter arw = null;

	private String parity = Serial.PARITY_NONE;

	private int baudRate = Serial.BAUDRATE_9600;

	private int stopBits = Serial.STOPBITS_1;

	private int byteSize = Serial.BYTESIZE_8;

	/**
	 * Constructs a bath, starting a new thread to monitor the current temperature.
	 */
	public Ecoline() {
		// These will be overwritten by the values specified in the XML
		// but are given here as defaults.
		lowerTemp = MINTEMP;
		upperTemp = MAXTEMP;
	}

	@Override
	public void configure() throws FactoryException {
		super.configure();
		nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(dec_places);
		nf.setMaximumIntegerDigits(int_places);
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
				arw = new AsynchronousReaderWriter(serial);
				arw.setReplyEndString("\r\n");
				arw.setCommandEndString("\r");
				setHWLowerTemp(lowerTemp);
				setHWUpperTemp(upperTemp);
				setPoint = getSetPoint();
				currentTemp = getCurrentTemperature();
				startPoller();
				configured = true;
			} catch (DeviceException de) {
				logger.error("Error configuring {}", serialDeviceName, de);
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
		arw = null;
		configured = false;
	}

	/**
	 * Set the name of the serial communication port on the host computer.
	 *
	 * @param serialDeviceName
	 *            the name of the serial port
	 */
	public void setSerialDeviceName(String serialDeviceName) {
		this.serialDeviceName = serialDeviceName;
	}

	/**
	 * Get the name of the serial communication port on the host computer.
	 *
	 * @return the name of the serial port
	 */
	public String getSerialDeviceName() {
		return serialDeviceName;
	}

	@Override
	public double getCurrentTemperature() throws DeviceException, NumberFormatException {
		double temp;

		switch (probe) {
		case 1:
			temp = getExtTemp();
			break;
		case 0:
		default:
			temp = getBathTemp();
		}
		return temp;
	}

	/**
	 * Get the current setpoint temperature.
	 *
	 * @return the setpoint temperature
	 * @throws DeviceException
	 */
	public double getSetPoint() throws DeviceException {
		String reply = arw.sendCommandAndGetReply("IN_SP_00");
		return java.lang.Double.valueOf(reply).doubleValue();
	}

	/**
	 * {@inheritDoc} Set the switching point for undertemperature (usually set to the lower operating temperature of the
	 * bath thermostat, it must be in the range -35C and 200C.
	 *
	 * @see gda.device.temperature.TemperatureBase#setHWLowerTemp(double)
	 */
	@Override
	public void setHWLowerTemp(double lowLimit) throws DeviceException {
		// deliberately do nothing
	}

	/**
	 * Set the upper operating temperature of the water bath, it must be in the range -35C and 200C. {@inheritDoc}
	 *
	 * @see gda.device.temperature.TemperatureBase#setHWUpperTemp(double)
	 */
	@Override
	public void setHWUpperTemp(double upperLimit) throws DeviceException {
		// deliberately do nothing
	}

	/**
	 * Switch control variable to source external Pt 100 T1
	 *
	 * @param name
	 *            the probe name
	 * @throws DeviceException
	 */
	@Override
	public void setProbe(String name) throws DeviceException {
		ArrayList<String> names = getProbeNames();
		for (int i = 0; i < names.size(); i++) {
			if (names.get(i).equals(name)) {
				switch (i) {
				case 0:
				default:
					setIntProbe();
				}
				logger.debug("Setting probe source to " + name);
				break;
			}
		}
	}

	/**
	 * Program a timed temperature ramp into the water bath. This does not initiate the program. Unrealistically short
	 * times will generate an error and the parameters will not be set.
	 *
	 * @param which
	 *            is the desired ramp to load
	 * @throws DeviceException
	 */
	@Override
	protected void sendRamp(int which) throws DeviceException {
		double finalTemp;
		double rate;

		TemperatureRamp ramp = rampList.get(which);

		finalTemp = ramp.getEndTemperature();
		rate = ramp.getRate();

		logger.debug("finalTemp is " + finalTemp);
		if (finalTemp > upperTemp || finalTemp < lowerTemp)
			throw new DeviceException("Invalid ramp final temperature");

		double dtime = Math.abs((finalTemp - currentTemp) / rate);
		int minutes = (int) dtime;

		logger.debug("minutes is " + minutes);
		if (minutes < 0 || minutes > 999)
			throw new DeviceException("Invalid ramp time");

		String str = "RMP_OUT_00_" + nf.format(finalTemp) + "_" + nf.format(minutes);
		setPoint = finalTemp;
		if (!arw.sendCommandAndGetReply(str).equals(OKREPLY)) {
			logger.debug("sendRamp throwing");
			throw new DeviceException(getName() + " command failure");
		}
	}

	/**
	 * Initiate the programmed temperature ramp in the water bath.
	 *
	 * @throws DeviceException
	 */
	public void sendStart() throws DeviceException {
		if (busy)
			throw new DeviceException("Water bath is already ramping to temerature");
		if (!arw.sendCommandAndGetReply("RMP_START").equals(OKREPLY))
			throw new DeviceException(getName() + "command failure");
		busy = true;
	}

	/**
	 * Switch control variable to source external Pt 100 T1
	 */
	public void setExtProbe() {
		// deliberately do nothing
	}

	/**
	 * Switch control variable to source Ti (probe in the bath); control according to bath temperature (Default setting
	 * at power on)
	 *
	 * @throws DeviceException
	 */
	public void setIntProbe() throws DeviceException {
		if (busy)
			throw new DeviceException("Water bath is already ramping to temperature");

		if (!arw.sendCommandAndGetReply("OUT_MODE_01_0").equals(OKREPLY))
			throw new DeviceException(getName() + " command failure");

		probe = 0;
	}

	/**
	 * Read the temperature of the controlling device either internal bath external probe1 or external probe2.
	 *
	 * @return the control temperature
	 * @throws DeviceException
	 * @throws NumberFormatException
	 */
	public double getBathTemp() throws DeviceException, NumberFormatException {
		String reply = arw.sendCommandAndGetReply("IN_PV_00");
		return java.lang.Double.valueOf(reply).doubleValue();
	}

	/**
	 * Read the temperature of the external temperature.
	 *
	 * @return the external temperature
	 */
	public double getExtTemp() {
		return 0.0;
	}

	/**
	 * @return the current value of Xp
	 * @throws DeviceException
	 */
	public double getXp() throws DeviceException {
		String reply = arw.sendCommandAndGetReply("IN_PAR_00");
		return java.lang.Double.valueOf(reply).doubleValue();
	}

	/**
	 * @return the current value of Tn
	 * @throws DeviceException
	 */
	public double getTn() throws DeviceException {
		String reply = arw.sendCommandAndGetReply("IN_PAR_01");
		return java.lang.Double.valueOf(reply).doubleValue();
	}

	/**
	 * Get the status signal
	 *
	 * @return the status
	 * @throws DeviceException
	 */
	public String getStatusSignal() throws DeviceException {
		return arw.sendCommandAndGetReply("STAT");
	}

	@Override
	protected void startNextRamp() throws DeviceException {
		currentRamp++;
		logger.debug("startNextRamp called currentRamp now " + currentRamp);
		if (currentRamp < rampList.size()) {
			sendRamp(currentRamp);
			sendStart();
		} else {
			stop();
		}
	}

	@Override
	public void pollDone(PollerEvent pe) {
		String stateString = null;
		String dataString = null;

		NumberFormat n = NumberFormat.getInstance();
		n.setMaximumFractionDigits(2);
		n.setGroupingUsed(false);

		logger.debug(getName() + " pollDone called");

		try {
			if (isAtTargetTemperature()) {
				busy = false;
				startHoldTimer();
			}

			if (lowLevel())
				stateString = "Low Water Level";
			else if (overTemp())
				stateString = "Over temperature error";
			else if (busy)
				stateString = (targetTemp > currentTemp) ? "Heating" : "Cooling";
			else if (currentRamp > -1)
				stateString = "At temperature";
			else
				stateString = "Idle";
		} catch (DeviceException de) {
		}
		if (timeSinceStart >= 0.0) {
			Date d = new Date();
			timeSinceStart = d.getTime() - startTime;
		}

		dataString = "" + n.format(timeSinceStart / 1000.0) + " " + currentTemp;

		TemperatureStatus ts = new TemperatureStatus(currentTemp, currentRamp, stateString, dataString);

		logger.debug(getName() + " notifying IObservers with " + ts);
		notifyIObservers(this, ts);

		// change poll time ??

	}

	private boolean lowLevel() throws DeviceException {
		return (getStatusSignal().charAt(2) == '1'); // change and merge all
		// error
		// handling
	}

	private boolean overTemp() throws DeviceException {
		return (getStatusSignal().charAt(3) == '1');
	}

	@Override
	public void doStart() throws DeviceException {
		Date d = new Date();
		startTime = d.getTime();
		sendStart();
	}

	@Override
	public void hold() {
	}

	private void sendStop() throws DeviceException {
		if (!arw.sendCommandAndGetReply("RMP_STOP").equals(OKREPLY))
			throw new DeviceException(getName() + " command failure");
	}

	@Override
	public void doStop() throws DeviceException {
		sendStop();
	}

	/**
	 * Set the temperature of the water bath in the range -20C to 150C.
	 *
	 * @param temp
	 *            required temparature of the water bath.
	 * @throws DeviceException
	 */
	@Override
	public synchronized void setTargetTemperature(double temp) throws DeviceException {
		if (temp > upperTemp || temp < lowerTemp)
			throw new DeviceException("Trying to set temperature outside of limits");
		if (busy)
			throw new DeviceException("Water bath is already ramping to temerature");

		String str = "OUT_SP_00_" + nf.format(temp);
		if (!arw.sendCommandAndGetReply(str).equals(OKREPLY)) {
			logger.debug("setTemp " + temp);
			throw new DeviceException(getName() + " command failure");
		}

		busy = true;
		setPoint = getSetPoint();
		notify();
	}

	@Override
	public void startTowardsTarget() throws DeviceException {
		logger.error("Warning: startTowardsTarget not implemented in Integra");
	}

	@Override
	public void runRamp() throws DeviceException {
		// TODO Auto-generated method stub

	}
}
