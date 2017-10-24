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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to control a LinkamCI Those computer interface boxes control the Linkam range of heating/freezing stages. They
 * offer a serial connection.
 */
public class LinkamCI extends TemperatureBase {

	private static final Logger logger = LoggerFactory.getLogger(LinkamCI.class);

	private final static double MAXTEMP = 900.0;

	private final static double MINTEMP = -35.0;

	private String debugName;

	private int state;

	private AsynchronousReaderWriter arw = null;

	private int startingRamp = 1;

	private Serial serial;

	private String serialDeviceName;

	private double samplingTime; // DSC stage only

	// FIXME ?
	// Currently we either have or have not got a DSC stage. Dsc
	// should be replaced with a more general Stage class if necessary later
	private LinkamStage stage = null;

	// Possible values of the status byte
	private final int STOPPED = 1;

	private final int HEATING = 16;

	private final int COOLING = 32;

	private final int HOLDINGLIMIT = 48;

	private final int HOLDINGTIME = 64;

	private final int HOLDINGTEMP = 80;

	private String errorMessage = "";

	private final int COOLINGFAULT = 1;

	private final int OK = 128;

	// FIXME
	// This makes the xml configuration bogus
	private String parity = Serial.PARITY_NONE;

	private int baudRate = Serial.BAUDRATE_19200;

	private int stopBits = Serial.STOPBITS_1;

	private int byteSize = Serial.BYTESIZE_8;

	/**
	 * Constructor
	 */
	public LinkamCI() {
		// These will be overwritten by the values specified in the XML
		// but are given here as defaults.
		lowerTemp = MINTEMP;
		upperTemp = MAXTEMP;
	}

	@Override
	public void configure() throws FactoryException {
		super.configure();
		if (serial == null) {
			logger.debug("Finding: " + serialDeviceName);
			if ((serial = (Serial) Finder.getInstance().find(serialDeviceName)) == null) {
				logger.error("Serial Device " + serialDeviceName + " not found");
			}
		}
		if (serial != null) {
			logger.debug("LinkamCI configure called");
			// debugName is used in error output
			debugName = getClass().getName() + " " + getName();

			try {
				serial.setBaudRate(baudRate);
				serial.setStopBits(stopBits);
				serial.setByteSize(byteSize);
				serial.setParity(parity);
				serial.setReadTimeout(0);
				serial.flush();
				arw = new AsynchronousReaderWriter(serial);

				if ((stage = createStage()) != null) {
					stage.sendStartupCommands();
				}

				setPumpAuto(true);
				startPoller();
				configured = true;
			} catch (DeviceException de) {
				logger.error("Error in {}.configure()", getName(), de);
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

	public Serial getSerial() {
		return serial;
	}

	public void setSerial(Serial serial) {
		this.serial = serial;
	}

	/**
	 * @param samplingTime
	 */
	public void setSamplingTime(double samplingTime) {
		this.samplingTime = samplingTime;
	}

	/**
	 * @return samplingTime
	 */
	public double getSamplingTime() {
		return samplingTime;
	}

	/**
	 * @param newState
	 *            the new state
	 */
	private void changeState(int newState) {
		logger.debug("state is " + state + " " + stateToString(state));
		logger.debug("newState is " + newState + " " + stateToString(newState));

		if (newState != state) {
			switch (state) {
			case HOLDINGLIMIT:
				break;
			case HEATING:
			case COOLING:
				if (newState == HOLDINGLIMIT) {
					startHoldTimer();
				}
				break;
			default:
				break;
			}
			state = newState;
		}
	}

	/**
	 * Checks the value of the error byte and takes appropriate action.
	 *
	 * @param errorByte
	 *            the error byte
	 */
	private void checkError(int errorByte) {
		if ((errorByte & COOLINGFAULT) == COOLINGFAULT) {
			// The cooling too fast fault is not fatal so just change
			// message
			errorMessage = " TOO FAST";
			logger.error("Error detected in LinkamCI: errorMessage is " + errorMessage);
		} else if (errorByte != OK) {
			// Other errors are treated as fatal until we know otherwise
			errorMessage = " ERROR " + errorByte;
			logger.error("Error detected in LinkamCI: errorMessage is " + errorMessage);
			stop();
		} else {
			errorMessage = "";
		}
	}

	/**
	 * Extracts the current temperature from a four byted string (from either a T or D reply)
	 *
	 * @param string
	 *            the four byte substring
	 * @return the current temperature
	 */
	public static double extractTemperature(String string) {
		// Positive values have range 0 to 3A98 representing 0 to 15000
		// Negative values from -1960 to -1 are represented as F858
		// (63576) to FFFF (65535).
		double temperature = Double.NaN;

		try {
			int value = java.lang.Integer.parseInt(string, 16);

			if (value > 63575) {
				value = -((65535 - value) + 1);
			}
			temperature = value / 10.0;
		}
		// Sometimes the temperature part of a reply string seems to be invalid
		// even though sensible error and status values have been returned so we
		// catch the normally uncaught NumberFormatException to deal with this
		catch (NumberFormatException nfe) {
			logger.error("extractTemperature caught NumberFormatException " + nfe);
			logger.error("                   string was " + string);
		}
		return temperature;
	}

	/**
	 * Gets the current temperature by asking the actual LinkamCI
	 *
	 * @return currentTemp
	 * @throws DeviceException
	 */
	@Override
	public double getCurrentTemperature() throws DeviceException {
		String statusString = getStatusString();

		// The temperature is in bytes 6 to 9 of the status string
		currentTemp = extractTemperature(statusString.substring(6, 10));

		return currentTemp;
	}

	/**
	 * Gets the current temperature without asking the actual LinkamCI
	 *
	 * @return temperature (in degreesC)
	 */
	public double getTemperature() {
		return currentTemp;
	}

	/**
	 * Sends the ramps to the hardware.
	 *
	 * @param which
	 *            the current ramp
	 */
	@Override
	protected void sendRamp(int which) {
		TemperatureRamp temperatureRamp = rampList.get(which);
		stage.sendRamp(temperatureRamp);
		sendRate(1, temperatureRamp.getRate());
		sendLimit(1, temperatureRamp.getEndTemperature());
	}

	/**
	 * @return true if at the desired temperature
	 * @throws DeviceException
	 */
	@Override
	public boolean isAtTargetTemperature() throws DeviceException {
		return (state == HOLDINGLIMIT);
	}

	/**
	 * Get the Linkam status
	 *
	 * @return the status string
	 * @throws DeviceException
	 */
	private String getStatusString() throws DeviceException {
		String cmd = "T";
		String status = arw.sendCommandAndGetReply(cmd);

		logger.debug("getStatusString status is " + status);
		return status;
	}

	@Override
	public void hold() {
		sendHold();
	}

	/**
	 * Called each time the poller goes round. Implements PollerListener interface.
	 *
	 * @param pe
	 *            a PollerEvent constructed by the Poller which calls this
	 */
	@Override
	public void pollDone(PollerEvent pe) {
		try {
			logger.debug("pollDone called");
			String statusString = getStatusString();
			checkError(statusString.charAt(1));
			changeState(statusString.charAt(0));
			currentTemp = extractTemperature(statusString.substring(6, 10));
			stage.pollDone(pe);
		} catch (DeviceException de) {
			logger.error("Error in {}.pollDone()", getName(), de);
		}
	}

	/**
	 * Sends a notifyIObservers with a TemperatureStatus. Used by stages to notify IObservers of the Controller.
	 *
	 * @param additionalData
	 *            set as the additionalData field of the TemperatureStatus which is sent in the notify.
	 */
	public void sendNotify(String additionalData) {
		notifyIObservers(this, new TemperatureStatus(currentTemp, currentRamp, stateToString(state) + errorMessage,
				additionalData));
	}

	/**
	 * Sends a "B" command
	 */
	private void sendB() {
		logger.debug("Linkam sendB() called");

		arw.handleCommand("B");
	}

	/**
	 * Sends a hold command
	 */
	public void sendHold() {
		logger.debug("Linkam sendHold() called");

		arw.handleCommand("O");
	}

	/**
	 * Set limit temperature for given ramp.
	 *
	 * @param rampNumber
	 *            the current ramp number
	 * @param limit
	 *            is the temperature limit to a resolution of 0.1degC, max value 99.9
	 */
	private void sendLimit(int rampNumber, double limit) {
		String cmd = "L" + rampNumber;

		logger.debug("Linkam.sendLimit called " + limit);

		// if (limit < 0.1 || limit > 99.9)
		// throw new DeviceException ("Limit is outide Linkam specification");

		String send = cmd + (int) (limit * 10.0);

		arw.handleCommand(send);
	}

	/**
	 * Set rate (in degrees/minute) for current ramp.
	 *
	 * @param rampNumber
	 *            the current ramp number
	 * @param rate
	 *            is the heating/cooling rate. The rate is 0.01degC/min. The maximum is 99.99degC/min.
	 */
	private void sendRate(int rampNumber, double rate) {
		String cmd = "R" + rampNumber;

		logger.debug("Linkam.sendRate called " + rate);

		// if (rate < 0.01 || rate > 99.99)
		// throw new DeviceException ("Rate is outide Linkam specification");

		String send = cmd + (int) (rate * 100);

		arw.handleCommand(send);
	}

	/**
	 * Sends a start command
	 */
	private void sendStart() {
		logger.debug("Linkam sendStart called");

		arw.handleCommand("S");
	}

	/**
	 * Sends a stop commmand
	 */
	private void sendStop() {
		logger.debug("Linkam sendStop() called");

		arw.handleCommand("E");
	}

	/**
	 * Creates a dsc if the reply to the ? command demands it. Eventually Dsc should be replaced by a more general Stage
	 * class.
	 *
	 * @return an intance of the created stage
	 */
	private LinkamStage createStage() {
		String reply;
		LinkamStage stage = null;

		try {
			reply = arw.sendCommandAndGetReply("\u00efS");
			logger.debug("createStage S reply was " + reply);
			if (reply.indexOf("DSC") != -1) {
				stage = new DscStage(this, arw, samplingTime);
			} else {
				stage = new DefaultStage(this);
			}
		} catch (DeviceException de) {
			logger.error("Error in {}.createStage()", getName(), de);
		}
		return stage;
	}

	@Override
	public void doStart() throws DeviceException {
		stage.startRamping();
		sendStart();
	}

	@Override
	protected void startNextRamp() throws DeviceException {
		// At the end of ramp 0 send the B command which as well as
		// what is specified in instructions also causes external
		// signals to be sent e.g. to start a TFG
		currentRamp++;

		logger.debug("startNextRamp called currentRamp now " + currentRamp);
		if (currentRamp < rampList.size()) {
			if (currentRamp == startingRamp) {
				sendB();
				stage.startExperiment();
			}

			sendRamp(currentRamp);
		} else {
			stop();
		}
	}

	/**
	 * @param state
	 *            the status of the Linkam
	 * @return the state represented as a string
	 */
	private String stateToString(int state) {
		String string = null;

		switch (state) {
		case STOPPED:
			string = "STOPPED";
			break;
		case HEATING:
			string = "HEATING";
			break;
		case COOLING:
			string = "COOLING";
			break;
		case HOLDINGLIMIT:
			string = "HOLDING AT LIMIT";
			break;
		case HOLDINGTIME:
			string = "HOLDING TIME";
			break;
		case HOLDINGTEMP:
			string = "HOLDING CURRENT TEMPERATURE";
			break;
		default:
			string = "SOMETHING ELSE";
			break;
		}

		return string;
	}

	@Override
	public void doStop() {
		if (state != STOPPED) {
			sendStop();
			stage.stop();
		}
	}

	/**
	 * Set the value of liquid nitrogen pump automation.
	 *
	 * @param value
	 *            the new value
	 */
	private void setPumpAuto(boolean value) {
		if (value == true) {
			arw.handleCommand("Pa");
			poller.setPollTime(LONGPOLLTIME);
		} else {
			arw.handleCommand("Pm");
			poller.setPollTime(polltime);
		}
	}

	/**
	 * Sets the liquid nitrogen pump speed.
	 *
	 * @param speed
	 *            the new speed
	 */
	private void setPumpSpeed(int speed) {
		// The speed can be 0 to 30 (decimal)
		arw.handleCommand("P" + (char) (0x30 + speed));
	}

	/**
	 * @param startingRamp
	 *            the starting ramp number
	 */
	private void setStartingRamp(int startingRamp) {
		this.startingRamp = startingRamp;
	}

	/**
	 * Overrides the DeviceBase method to provide commands for a liquid nitrogen pump
	 *
	 * @param name
	 *            the name of the attribute to set
	 * @param value
	 *            the attribute value
	 */
	@Override
	public void setAttribute(String name, Object value) {
		if (name.equalsIgnoreCase("LNPumpAuto")) {
			setPumpAuto(((Boolean) value).booleanValue());
		}

		if (name.equalsIgnoreCase("LNPumpSpeed")) {
			setPumpSpeed(((Integer) value).intValue());
		}

		if (name.equalsIgnoreCase("StartingRamp")) {
			setStartingRamp(((Integer) value).intValue());
		}
	}

	/**
	 * Overrides the DeviceBase method to get current DSC dataset name.
	 *
	 * @param name
	 *            the attribute name to get
	 * @return the attribute value
	 */
	@Override
	public Object getAttribute(String name) {
		if (name.equalsIgnoreCase("DataFilename")) {
			return stage.getDataFileName();
		} else if (name.equalsIgnoreCase("NeedsCooler")) {
			return Boolean.TRUE;
		} else if (name.equalsIgnoreCase("NeedsCoolerSpeedSetting") && stage instanceof DscStage) {
			return Boolean.TRUE;
		} else if (name.equalsIgnoreCase("isDSC")) {
			return (stage instanceof DscStage);
		} else {
			return null;
		}
	}

	@Override
	public void startTowardsTarget() throws DeviceException {
		logger.error("Warning: startTowardsTarget not implemented in LinkamCI");
	}

	@Override
	protected void setHWLowerTemp(double lowerTemp) throws DeviceException {
	}

	@Override
	protected void setHWUpperTemp(double upperTemp) throws DeviceException {
	}

	@Override
	public void runRamp() throws DeviceException {
		// TODO Auto-generated method stub

	}

}
