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
 * Class to control the Lauda Compact Low-Temperature Thermostat.
 */
public class Lauda extends TemperatureBase {

	private static final Logger logger = LoggerFactory.getLogger(Lauda.class);

	private double startTime = 0;

	private final static double MAXTEMP = 200.0;

	private final static double MINTEMP = -35.0;

	private final static String OKREPLY = "OK\r\n";

	private int probe = 0;

	private Serial serial;

	private String serialDeviceName;

	private AsynchronousReaderWriter arw = null;

	private String parity = Serial.PARITY_NONE;

	private int baudRate = Serial.BAUDRATE_9600;

	private int stopBits = Serial.STOPBITS_2;

	private int byteSize = Serial.BYTESIZE_8;

	/**
	 * Constructs a Lauda bath, starting a new thread to monitor the current temperature.
	 */
	public Lauda() {
		// These will be overwritten by the values specified in the XML
		// but are given here as defaults.
		lowerTemp = MINTEMP;
		upperTemp = MAXTEMP;
	}

	@Override
	public void configure() throws FactoryException {
		super.configure();
		logger.debug("Finding: " + serialDeviceName);
		if ((serial = (Serial) Finder.getInstance().find(serialDeviceName)) == null) {
			logger.error("Serial Device " + serialDeviceName + " not found");
		} else {
			try {
				serial.setBaudRate(baudRate);
				serial.setStopBits(stopBits);
				serial.setByteSize(byteSize);
				serial.setParity(parity);
				serial.setReadTimeout(0);
				serial.flush();
				arw = new AsynchronousReaderWriter(serial);
				arw.setReplyEndString("\r\n");
				arw.setCommandEndString("\r");
				setPoint = getSetPoint();
				setHWLowerTemp(lowerTemp);
				setHWUpperTemp(upperTemp);
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
	protected void doStart() throws DeviceException {
		Date d = new Date();
		startTime = d.getTime();
		sendStart();
	}

	@Override
	protected void doStop() throws DeviceException {
		sendStop();
	}

	/**
	 * Read the temperature of the Lauda bath.
	 *
	 * @return the Lauda water bath temperature
	 * @throws DeviceException
	 * @throws NumberFormatException
	 */
	@Override
	public double getCurrentTemperature() throws DeviceException, NumberFormatException {
		double temp;

		switch (probe) {
		case 1:
			temp = getExtProbe1();
			break;
		case 2:
			temp = getExtProbe2();
			break;
		default:
			temp = getBathTemp();
		}
		return temp;
	}

	/**
	 * Set the temperature of the Lauda bath in the range -20C to 150C.
	 *
	 * @param temp
	 *            required temparature of the Lauda water bath.
	 * @throws DeviceException
	 */
	@Override
	public synchronized void setTargetTemperature(double temp) throws DeviceException {
		if (temp > upperTemp || temp < lowerTemp)
			throw new DeviceException("Trying to set temperature outside of limits");
		if (busy)
			throw new DeviceException("Water bath is already ramping to temperature");

		String str = "OUT " + temp;
		if (!arw.sendCommandAndGetReply(str).equals(OKREPLY))
			throw new DeviceException("Lauda command failure");

		busy = true;
		setPoint = temp;
		targetTemp = setPoint;
	}

	/**
	 * Get the current setpoint temperature.
	 *
	 * @return the setpoint temperature
	 * @throws DeviceException
	 */
	public double getSetPoint() throws DeviceException {
		String reply = arw.sendCommandAndGetReply("IN 3");
		return java.lang.Double.valueOf(reply).doubleValue();
	}

	/**
	 * Set the switching point for undertemperature (usually set to the lower operating temperature of the Lauda bath
	 * thermostat, it must be in the range -35C and 200C.
	 *
	 * @param lowLimit
	 *            the lower temperature limit in degrees C
	 * @throws DeviceException
	 */
	@Override
	public void setHWLowerTemp(double lowLimit) throws DeviceException {
		if (lowLimit < MINTEMP || lowLimit > upperTemp)
			throw new DeviceException("Invalid lower temperature limit");

		String str = "OUT L" + lowLimit;
		if (!arw.sendCommandAndGetReply(str).equals(OKREPLY))
			throw new DeviceException("Lauda command failure");
		if (getHWLowerTemp() != lowLimit)
			throw new DeviceException("Failed to set lower temperature limit");
	}

	/**
	 * Set the upper operating temperature of the Lauda bath, it must be in the range -35C and 200C.
	 *
	 * @param upperLimit
	 *            the upper temperature limit in degrees C
	 * @throws DeviceException
	 */
	@Override
	public void setHWUpperTemp(double upperLimit) throws DeviceException {
		if (upperLimit < lowerTemp || upperLimit > MAXTEMP || upperLimit < getSetPoint())
			throw new DeviceException("Invalid upper temperature limit");

		String str = "OUT H" + upperLimit;
		if (!arw.sendCommandAndGetReply(str).equals(OKREPLY))
			throw new DeviceException("Lauda command failure");
		if (getHWUpperTemp() != upperLimit)
			throw new DeviceException("Failed to set upper temperature limit");
	}

	private double getHWUpperTemp() throws DeviceException {
		String reply = arw.sendCommandAndGetReply("IN 9");
		return java.lang.Double.valueOf(reply).doubleValue();
	}

	private double getHWLowerTemp() throws DeviceException {
		String reply = arw.sendCommandAndGetReply("IN 8");
		logger.debug("getLowerTemp got reply " + reply);
		return java.lang.Double.valueOf(reply).doubleValue();
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
				case 1:
					setExtProbe1();
					break;
				case 2:
					setExtProbe2();
					break;
				default:
					setIntProbe();
				}
				logger.debug("Setting probe source to " + name);
				break;
			}
		}
	}

	/**
	 * Program a timed temperature ramp into the Lauda bath. This does not initiate the program. Unrealistically short
	 * times will generate an error and the parameters will not be set.
	 *
	 * @param which
	 *            is the required ramp to load
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
		int time = (int) dtime;
		int hours = time / 60;
		int minutes = time - (hours * 60);

		logger.debug("hours is " + hours);
		logger.debug("minutes is " + minutes);
		if ((hours < 0 || hours > 99) || (minutes < 0 || minutes > 59))
			throw new DeviceException("Invalid ramp time");

		String str = "SEG " + finalTemp + " " + hours + ":" + minutes;
		setPoint = finalTemp;
		if (!arw.sendCommandAndGetReply(str).equals(OKREPLY)) {
			logger.debug("sendRamp throwing");
			throw new DeviceException("Lauda command failure");
		}
	}

	/**
	 * Initiate the programmed temperature ramp in the Lauda bath.
	 *
	 * @throws DeviceException
	 */
	public void sendStart() throws DeviceException {
		if (busy)
			throw new DeviceException("Water bath is already ramping to temperature");
		if (!arw.sendCommandAndGetReply("START").equals(OKREPLY))
			throw new DeviceException("Lauda command failure");
		busy = true;
	}

	/**
	 * Switch control variable to source external Pt 100 T1
	 *
	 * @throws DeviceException
	 */
	public void setExtProbe1() throws DeviceException {
		if (busy)
			throw new DeviceException("Water bath is already ramping to temperature");

		if (getStatusSignal().charAt(5) == '1') {
			if (!arw.sendCommandAndGetReply("OUT RT1").equals(OKREPLY))
				throw new DeviceException("Lauda command failure");
			probe = 1;
		} else
			throw new DeviceException("External Pt 100 T1 is not connected");
	}

	/**
	 * Switch control variable to source external Pt 100 T2
	 *
	 * @throws DeviceException
	 */
	public void setExtProbe2() throws DeviceException {
		if (busy)
			throw new DeviceException("Water bath is already ramping to temperature");

		if (getStatusSignal().charAt(6) == '1') {
			if (!arw.sendCommandAndGetReply("OUT RT2").equals(OKREPLY))
				throw new DeviceException("Lauda command failure");
			probe = 2;
		} else
			throw new DeviceException("External Pt 100 T1 is not connected");

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

		if (!arw.sendCommandAndGetReply("OUT RTi").equals(OKREPLY))
			throw new DeviceException("Lauda command failure");

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
		String reply = arw.sendCommandAndGetReply("IN 1");
		return java.lang.Double.valueOf(reply).doubleValue();
	}

	/**
	 * Read the temperature of the external probe 1.
	 *
	 * @return the external probe 1 temperature
	 * @throws DeviceException
	 */
	public double getExtProbe1() throws DeviceException {
		if (getStatusSignal().charAt(5) == '1') {
			String reply = arw.sendCommandAndGetReply("IN 2");
			return java.lang.Double.valueOf(reply).doubleValue();
		}
		throw new DeviceException("External Pt 100 T1 is not connected");
	}

	/**
	 * Read the temperature of the external probe 2.
	 *
	 * @return the external probe 2 temperature
	 * @throws DeviceException
	 */
	public double getExtProbe2() throws DeviceException {
		if (getStatusSignal().charAt(6) == '1') {
			String reply = arw.sendCommandAndGetReply("IN 7");
			return java.lang.Double.valueOf(reply).doubleValue();
		}
		throw new DeviceException("External Pt 100 T2 is not connected");
	}

	/**
	 * Set the proportional band control paramter Xp for the controller
	 *
	 * @param temp
	 *            the Xp (degrees)
	 * @throws DeviceException
	 */
	public void setXpControl(double temp) throws DeviceException {
		String str = "OUT XP" + temp;
		if (!arw.sendCommandAndGetReply(str).equals(OKREPLY))
			throw new DeviceException("Lauda command failure");
	}

	/**
	 * Set the control paramter Tn for the controller
	 *
	 * @param time
	 *            Control parameter Tn (secs)
	 * @throws DeviceException
	 */
	public void setTnControl(double time) throws DeviceException {
		String str = "OUT TN" + time;
		if (!arw.sendCommandAndGetReply(str).equals(OKREPLY))
			throw new DeviceException("Lauda command failure");
	}

	/**
	 * Set the control paramter Tv for the controller
	 *
	 * @param time
	 *            the Tv (secs)
	 * @throws DeviceException
	 */
	public void setTvControl(double time) throws DeviceException {
		String str = "OUT TV" + time;
		if (!arw.sendCommandAndGetReply(str).equals(OKREPLY))
			throw new DeviceException("Lauda command failure");
	}

	/**
	 * Get the control parameter Xp for the controller.
	 *
	 * @return the control parameter Xp
	 * @throws DeviceException
	 */
	public double getXp() throws DeviceException {
		String reply = arw.sendCommandAndGetReply("IN A");
		return java.lang.Double.valueOf(reply).doubleValue();
	}

	/**
	 * Get the control parameter Tn for the controller.
	 *
	 * @return the control parameter Tn
	 * @throws DeviceException
	 */
	public double getTn() throws DeviceException {
		String reply = arw.sendCommandAndGetReply("IN B");
		return java.lang.Double.valueOf(reply).doubleValue();
	}

	/**
	 * Get the control parameter Tv for the controller.
	 *
	 * @return the control parameter Tv
	 * @throws DeviceException
	 */
	public double getTv() throws DeviceException {
		String reply = arw.sendCommandAndGetReply("IN C");
		return java.lang.Double.valueOf(reply).doubleValue();
	}

	/**
	 * Get the status signal
	 *
	 * @return the status
	 * @throws DeviceException
	 */
	public String getStatusSignal() throws DeviceException {
		return arw.sendCommandAndGetReply("IN 4");
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

		logger.debug("Lauda pollDone called");

		try {
			if (isAtTargetTemperature()) {
				busy = false;
				startHoldTimer();
			}

			if (lowLevel())
				stateString = "Low Water Level";
			else if (busy)
				stateString = (targetTemp > currentTemp) ? "Heating" : "Cooling";
			else if (currentRamp > -1)
				stateString = "At temperature";
			else
				stateString = "Idle";
		} catch (DeviceException de) {
			logger.debug(de.getStackTrace().toString());
		}
		if (timeSinceStart >= 0.0) {
			Date d = new Date();
			timeSinceStart = d.getTime() - startTime;
		}

		dataString = "" + n.format(timeSinceStart / 1000.0) + " " + currentTemp;

		TemperatureStatus ts = new TemperatureStatus(currentTemp, currentRamp, stateString, dataString);

		logger.debug("Lauda notifying IObservers with " + ts);
		notifyIObservers(this, ts);

		// change poll time ??
	}

	private boolean lowLevel() throws DeviceException {
		return (getStatusSignal().charAt(1) == '1');
	}

	private void sendStop() throws DeviceException {
		if (!arw.sendCommandAndGetReply("STOP").equals(OKREPLY))
			throw new DeviceException("Lauda command failure");
	}

	@Override
	public void startTowardsTarget() throws DeviceException {
		logger.error("Warning: startTowardsTarget not implemented in Lauda");
	}

	@Override
	public void hold() throws DeviceException {
		// TODO Auto-generated method stub

	}

	@Override
	public void runRamp() throws DeviceException {
		// TODO Auto-generated method stub

	}

}
