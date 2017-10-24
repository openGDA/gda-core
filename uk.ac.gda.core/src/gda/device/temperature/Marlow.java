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

import java.text.NumberFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Serial;
import gda.device.TemperatureStatus;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.util.PollerEvent;

/**
 * Class to control the Marlow SE50100 series controller. The Marlow expects even parity, 1 stop bit and 7 data bits
 * with a baud rate selected between 300 & 9600 README This particular class has been reinstated for CD12. It uses
 * default values for serial as xml rewrite is in progress. Most new methods implemented in Eurotherm900 and
 * Eurotherm2000 have been removed and there is no facility for writing to a file.
 */
public class Marlow extends TemperatureBase implements ReplyChecker {

	private static final Logger logger = LoggerFactory.getLogger(Marlow.class);

	private final static double MINTEMP = -35.0;

	private final static String SETPOINT = "SL";

	private final static String MEASURED = "PV";

	private final static String WORKING = "SP";

	private final static String MINSET = "LS";

	private final static String MAXSET = "HS";

	private final static String XPBAND = "XP";

	private final static String IDENTITY = "II";

	private final static char STX = '\02';

	private final static char ETX = '\03';

	private final static char EOT = '\04';

	private final static char ENQ = '\05';

	private final static char ACK = '\06';

	private final static char NAK = 21;

	private int uid = 0;

	private int gid = 0;

	private String unit = "00";

	private String group = "00";

	private boolean stopFlag = false;

	private double startTime = 0;

	private String serialDeviceName;

	private Serial serial;

	private String debugName;

	private AsynchronousReaderWriter arw = null;

	private String parity = Serial.PARITY_EVEN;

	private int baudRate = Serial.BAUDRATE_9600;

	private int stopBits = Serial.STOPBITS_1;

	private int byteSize = Serial.BYTESIZE_7;

	/**
	 * Constructor
	 */
	public Marlow() {
		// These will be overwritten by the values specified in the XML
		// but are given here as defaults.
		lowerTemp = MINTEMP;
		upperTemp = MINTEMP;
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
				arw.setReplyChecker(this);
				arw.setCommandEndString("");
				getInstrumentIdentity();
				setHWLowerTemp(lowerTemp);
				setHWUpperTemp(upperTemp);
				targetTemp = getTargetTemperature();
				currentTemp = getCurrentTemperature();
				startPoller();
				configured = true;
			} catch (DeviceException de) {
				logger.error("Error in {}.configure()", getName(), de);
			}
			startPoller();
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

	/**
	 * @param uid
	 */
	public void setUid(int uid) {
		this.uid = uid;
		if (uid >= 0 && uid <= 9)
			unit = "" + uid + uid;
	}

	/**
	 * @return uid
	 */
	public int getUid() {
		return uid;
	}

	/**
	 * @param gid
	 */
	public void setGid(int gid) {
		this.gid = gid;
		if (gid >= 0 && gid <= 9)
			group = "" + gid + gid;
	}

	/**
	 * @return gid
	 */
	public int getGid() {
		return gid;
	}

	/**
	 * Check a valid termination character has been received in the response.
	 *
	 * @param reply
	 *            is the raw reply from the Marlow.
	 * @throws DeviceException
	 */
	private void checkReply(String reply) throws DeviceException {
		int irep = reply.charAt(0);
		logger.debug(debugName + " checkReply character: " + irep);

		if (reply.charAt(0) == NAK) {
			throw new DeviceException("Negative Acknowledgement received from Marlow");
		} else if (reply.charAt(0) != ACK) {
			throw new DeviceException("Spurious reply from Marlow");
		}
	}

	/**
	 * Decode the raw response from the Marlow, checking the checksum value and returns the decoded value in String
	 * form.
	 *
	 * @param buffer
	 *            reply from Marlow
	 * @return decoded reply
	 * @throws DeviceException
	 */
	private String decodeReply(String buffer) throws DeviceException {
		String response = null;
		int bcc = 0;
		int i;

		for (i = 1; i < buffer.length(); i++) {
			bcc ^= buffer.charAt(i);
			if (buffer.charAt(i) == ETX)
				break;
		}

		if (buffer.charAt(++i) == bcc) {
			response = buffer.substring(3, i - 1);
			logger.debug("Reply from Marlow: " + response);
		} else {
			throw new DeviceException("Marlow replied with checksum error");
		}
		return response;
	}

	/**
	 * Encode a mnemonic & parameter value for setting the value in the Marlow controller
	 *
	 * @param mnemonic
	 *            is the two letter code.
	 * @param value
	 *            of the parameter
	 * @return decoded reply
	 */
	private String encode(String mnemonic, double value) {
		// Calculate the verification digit BCC as explained in Marlow book
		// First calculate integer value
		String str = String.valueOf(value);
		// substring(0,5)
		String pack = mnemonic + str.substring(0, Math.min(7, str.length())) + ETX;
		char bcc = 0;

		for (int i = 0; i < pack.length(); i++)
			bcc ^= pack.charAt(i);
		// send the instruction to the Marlow
		// value should be 5 digits only!!!
		return EOT + group + unit + STX + pack + bcc;
	}

	/**
	 * Encode a mnemonic & parameter value for setting the value in the Marlow controller
	 *
	 * @param mnemonic
	 *            is the two letter code.
	 * @param status
	 *            is the status word
	 * @return encoded reply
	 */
	/*
	 * never used method private String encode(String mnemonic, String status) { // Calculate the verification digit BCC
	 * as explained in Marlow book // First calculate integer value String pack = mnemonic + status + ETX; char bcc = 0;
	 * for (int i = 0; i < pack.length(); i++) bcc ^= pack.charAt(i); // send the instruction to the Marlow // value
	 * should be 5 digits only!!! return EOT + group + unit + STX + pack + bcc; }
	 */

	/**
	 * Encode a mnemonic when requesting values from the Marlow controller
	 *
	 * @param mnemonic
	 *            is the two letter code.
	 * @return encoded reply
	 */
	private String encode(String mnemonic) {
		return EOT + group + unit + mnemonic + ENQ;
	}

	/**
	 * Gets the current temperature by asking the actual Marlow
	 *
	 * @return currentTemp
	 * @throws DeviceException
	 */
	@Override
	public double getCurrentTemperature() throws DeviceException {
		String str = encode(MEASURED);
		String reply = arw.sendCommandAndGetReply(str);
		currentTemp = java.lang.Double.valueOf(decodeReply(reply)).doubleValue();
		return currentTemp;
	}

	/**
	 * Get the identification number of the controller
	 *
	 * @throws DeviceException
	 */
	private void getInstrumentIdentity() throws DeviceException {
		String str = encode(IDENTITY);
		String reply = arw.sendCommandAndGetReply(str);
		String identity = decodeReply(reply);
		logger.debug("Marlow " + identity.substring(1) + " controller");
	}

	/**
	 * Get the curent target temperature of the Marlow controller
	 *
	 * @return the target temperature
	 * @throws DeviceException
	 */
	@Override
	public double getTargetTemperature() throws DeviceException {
		String str = encode(WORKING);
		String reply = arw.sendCommandAndGetReply(str);
		return Double.valueOf(decodeReply(reply)).doubleValue();
	}

	/**
	 * Get to the Xp parameter
	 *
	 * @return the Xp paramter
	 * @throws DeviceException
	 */
	private double getXp() throws DeviceException {
		String str = encode(XPBAND);
		String reply = arw.sendCommandAndGetReply(str);
		return Double.valueOf(decodeReply(reply)).doubleValue();
	}

	/**
	 * Hold at temperature
	 *
	 * @throws DeviceException
	 */
	@Override
	public void hold() throws DeviceException {
		// deliberately do nothing
	}

	/**
	 * Executes when poll timer fires
	 *
	 * @param pe
	 *            the polling event
	 */
	@Override
	public void pollDone(PollerEvent pe) {
		String stateString = null;
		String dataString = null;

		NumberFormat n = NumberFormat.getInstance();
		n.setMaximumFractionDigits(2);
		n.setGroupingUsed(false);

		logger.debug("Marlow pollDone called");

		try {
			logger.debug("Marlow pollDone: stopFlag is " + stopFlag);
			if (isAtTargetTemperature() || stopFlag) {
				busy = false;
				stopFlag = false;
				logger.debug("pollDone: stop flag should be false, is:  " + stopFlag
						+ "::: busy flag should be false, is: " + busy);
			}
			logger.debug("busy is " + busy);
			if (busy)
				stateString = (targetTemp > currentTemp) ? "Heating" : "Cooling";
			else if (currentRamp > -1)
				stateString = "At temperature";
			else
				stateString = "Idle";
		} catch (DeviceException de) {
			logger.error("Error in pollDone", de);
		}
		if (timeSinceStart >= 0.0) {
			Date d = new Date();
			timeSinceStart = d.getTime() - startTime;
		}

		dataString = "" + n.format(timeSinceStart / 1000.0) + " " + currentTemp;

		TemperatureStatus ts = new TemperatureStatus(currentTemp, currentRamp, stateString, dataString);

		logger.debug("Marlow notifying IObservers with " + ts);
		notifyIObservers(this, ts);

		try {
			getSetPoint();
		} catch (DeviceException de) {
			logger.error("Error getting set point", de);
		}

	}

	/**
	 * Get the SetPoint
	 *
	 * @return the set point
	 * @throws DeviceException
	 */
	private double getSetPoint() throws DeviceException {
		String str = encode(SETPOINT);
		String reply = arw.sendCommandAndGetReply(str);
		return java.lang.Double.valueOf(decodeReply(reply)).doubleValue();
	}

	/**
	 * Get to the OutputPower This method needs re implementing, if necessary
	 *
	 * @return the output power
	 * @throws DeviceException
	 */
	/*
	 * never used private double getOutputPower() throws DeviceException { // README: OUTPOWER is not available in 800s
	 * return Double.NaN; }
	 */

	/**
	 * Tells the hardware to start heating or cooling
	 *
	 * @throws DeviceException
	 */
	private void sendStart() throws DeviceException {
		if (busy) {
			throw new DeviceException("Marlow is already moving to temperature");
		}
		logger.debug("sendStart: busy should be false, is: " + busy + ". Setting busy to true.");
		busy = true;
	}

	/**
	 * Set the proportional band control parameter Xp for the controller
	 *
	 * @param value
	 *            of parameter Xp
	 * @throws DeviceException
	 */
	private void setXpControl(double value) throws DeviceException {
		String str = encode(XPBAND, value);
		String reply = arw.sendCommandAndGetReply(str);
		checkReply(reply);
	}

	/**
	 * Implements methods from ReplyChecker class
	 *
	 * @param buffer
	 *            the reply from the Eurotherm
	 * @return true if reply is corretly formatted
	 */
	@Override
	public boolean bufferContainsReply(StringBuffer buffer) {
		boolean reply = false;
		int len = buffer.length();

		if (buffer.charAt(0) == ACK || buffer.charAt(0) == NAK || (len > 1 && buffer.charAt(len - 2) == ETX)) {
			reply = true;
		}
		return reply;
	}

	/**
	 * Overrides the DeviceBase method to getAttribute.
	 *
	 * @param name
	 *            the name of the attribute to obtain
	 * @return the attribute
	 * @throws DeviceException
	 */
	@Override
	public Object getAttribute(String name) throws DeviceException {
		if (name.equalsIgnoreCase("Xp"))
			return new Double(getXp());

		return null;
	}

	/**
	 * Overrides the DeviceBase method to setAttributes.
	 *
	 * @param name
	 *            the name of the attribute to obtain
	 * @param value
	 *            the attribute to set
	 * @throws DeviceException
	 */
	@Override
	public void setAttribute(String name, Object value) throws DeviceException {
		if (name.equalsIgnoreCase("Xp")) {
			setXpControl(((Double) value).doubleValue());
		} else if (name.equalsIgnoreCase("Accuracy")) {
			setAccuracy(((Double) value).doubleValue());
		}
	}

	// Implementations of abstract methods from TemperatureBase

	/**
	 * This method needs re implementing, if necessary
	 *
	 * @throws DeviceException
	 */
	@Override
	public void doStart() throws DeviceException {
		// deliberately do nothing
	}

	/**
	 * Sets the stop flag to true. Should it set busy to false at this point?
	 *
	 * @throws DeviceException
	 */
	@Override
	public void doStop() throws DeviceException {
		// dataFileWriter.close();// must implement this
		// sendStop();
		logger
				.debug("doStop: Send stop has not been called. "
						+ "If it had the stop flag would have been set to true.");
		logger.debug("Stop called" + " - NO COMMAND TO STOP ACTUAL HARDWARE. \n"
				+ "Therefore it will reach target temperature.");
		// busy = false;
		logger.debug("doStop: Stop flag should be false? Is: " + stopFlag + ". Setting it to true");
		logger.debug("doStop: busy flag should be false? Is: " + busy);
		stopFlag = true;
	}

	@Override
	protected void sendRamp(int which) throws DeviceException {
		// deliberately do nothing
	}

	@Override
	protected void startNextRamp() throws DeviceException {
		// deliberately do nothing
	}

	/**
	 * Starts a temperature change towards previously set target temperature. TargetTemp will have been set by the
	 * TemperatureBase method: setTargetTemperature(). This method sends the command to the hardware.
	 *
	 * @throws DeviceException
	 */
	@Override
	public void startTowardsTarget() throws DeviceException {

		if (!stopFlag) {
			logger.debug("startTowardsTarget: the Stop flag is: " + stopFlag + ". Sending start command.");
			String str = encode(SETPOINT, targetTemp);
			String reply = arw.sendCommandAndGetReply(str);
			checkReply(reply);
			// send start only deals with the busy flag
			sendStart();
		}
		if (stopFlag) {
			stopFlag = false;
			logger.debug("startTowardsTarget: the stop flag was true, has been set to false");
		}
	}

	/**
	 * Is this method used now. If not - deprecate
	 *
	 * @throws DeviceException
	 */
	@Override
	public synchronized void waitForTemp() throws DeviceException {
		while (busy) {
			logger.debug("waitForTemp: the busy flag is: " + busy);
			synchronized (this) {
				try {
					wait(POLLTIME);
				} catch (InterruptedException e) {
					throw new DeviceException("Interrupted waiting in waitForTemp()");
				}
			}
		}
		if (stopFlag) {
			logger.debug("waitForTemp: the stop flag was true, is being set to false.");
			stopFlag = false;
		}
	}

	@Override
	protected void setHWLowerTemp(double lowerTemp) throws DeviceException {
		String str = encode(MINSET, lowerTemp);
		String reply = arw.sendCommandAndGetReply(str);
		checkReply(reply);
	}

	@Override
	protected void setHWUpperTemp(double upperTemp) throws DeviceException {
		String str = encode(MAXSET, upperTemp);
		String reply = arw.sendCommandAndGetReply(str);
		checkReply(reply);
	}

	@Override
	public void runRamp() throws DeviceException {
		// TODO Auto-generated method stub

	}

}
