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
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to control the Eurotherm controller. The EuroTherm expects even parity, 1 stop bit and 7 data bits with a baud
 * rate selected between 300 & 9600
 */
public class Eurotherm2000 extends TemperatureBase implements ReplyChecker {

	private static final Logger logger = LoggerFactory.getLogger(Eurotherm2000.class);

	private final static double MAXTEMP = 900.0;

	private final static double MINTEMP = -35.0;

	private final static String SETPOINT = "SL";

	private final static String MEASURED = "PV";

	private final static String WORKING = "SP";

	private final static String XPBAND = "XP";

	private final static String IDENTITY = "II";

	private final static String OUTPOWER = "OP";

	private final static char STX = '\02';

	private final static char ETX = '\03';

	private final static char EOT = '\04';

	private final static char ENQ = '\05';

	private final static char ACK = '\06';

	private final static char NAK = 21;

	private String unit = "00";

	private String group = "00";

	private double startTime = 0;

	private Serial serial = null;

	private String serialDeviceName;

	private int uid = 0;

	private int gid = 0;

	private String debugName;

	private AsynchronousReaderWriter arw = null;

	private String parity = Serial.PARITY_EVEN;

	private int baudRate = Serial.BAUDRATE_9600;

	private int stopBits = Serial.STOPBITS_1;

	private int byteSize = Serial.BYTESIZE_7;

	/**
	 * Constructor
	 */
	public Eurotherm2000() {
		// These will be overwritten by the values specified in the XML
		// but are given here as defaults.
		lowerTemp = MINTEMP;
		upperTemp = MAXTEMP;
	}

	/**
	 * Check a valid termination character has been received in the response.
	 *
	 * @param reply
	 *            is the raw reply from the Eurotherm.
	 * @throws DeviceException
	 */
	private void checkReply(String reply) throws DeviceException {
		int irep = reply.charAt(0);
		logger.debug(debugName + " checkReply character: " + irep);

		if (reply.charAt(0) == NAK) {
			throw new DeviceException("Negative Acknowledgement received from Eurotherm");
		} else if (reply.charAt(0) != ACK) {
			throw new DeviceException("Spurious reply from Eurotherm");
		}
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
			logger.debug("Eurotherm configure called");
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
				logger.error("Error configuring {}", getName(), de);
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
	 * Decode the raw response from the Eurotherm, checking the checksum value and returns the decoded value in String
	 * form.
	 *
	 * @param buffer
	 *            the raw repsonse from the Eurotherm
	 * @return decoded value
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
			logger.debug("Reply from Eurotherm: " + response);
		} else {
			throw new DeviceException("Eurotherm replied with checksum error");
		}
		return response;
	}

	/**
	 * Encode a mnemonic & parameter value for setting the value in the eurotherm controller
	 *
	 * @param mnemonic
	 *            is the two letter code.
	 * @param value
	 *            of the parameter
	 * @return the encoded string
	 */
	private String encode(String mnemonic, double value) {
		// Calculate the verification digit BCC as explained in EUROTHERM book
		// First calculate integer value
		String str = String.valueOf(value);
		String pack = mnemonic + str.substring(0, Math.min(7, str.length())) + ETX;
		char bcc = 0;

		for (int i = 0; i < pack.length(); i++)
			bcc ^= pack.charAt(i);

		// send the instruction to the EUROTHERM
		// value should be 5 digits only!!!

		return EOT + group + unit + STX + pack + bcc;
	}

	/**
	 * Encode a mnemonic when requesting values from the Eurotherm controller
	 *
	 * @param mnemonic
	 *            is the two letter code.
	 * @return the encoded string
	 */
	private String encode(String mnemonic) {
		return EOT + group + unit + mnemonic + ENQ;
	}

	/**
	 * Gets the current temperature by asking the actual Eurotherm
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
	 * @return the current status
	 * @throws DeviceException
	 */
	public String getStatus() throws DeviceException {
		String str = encode("SO");
		String reply = arw.sendCommandAndGetReply(str);
		logger.debug("getStatus status is " + decodeReply(reply));
		return decodeReply(reply);
	}

	/**
	 * Get to the lower operating temperature
	 *
	 * @throws DeviceException
	 */
	private void getInstrumentIdentity() throws DeviceException {
		String str = encode(IDENTITY);
		String reply = arw.sendCommandAndGetReply(str);
		String identity = decodeReply(reply);
		logger.debug("Eurotherm " + identity.substring(1) + " controller");

		if (!identity.equals(">2440")) {
			throw new DeviceException("Wrong type of controller");
		}
	}

	/**
	 * Get the curent target temperature of the EuroTherm controller
	 *
	 * @return the target temperature
	 * @throws DeviceException
	 */
	@Override
	public double getTargetTemperature() throws DeviceException {
		String str = encode(WORKING);
		String reply = arw.sendCommandAndGetReply(str);
		return java.lang.Double.valueOf(decodeReply(reply)).doubleValue();
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
		return java.lang.Double.valueOf(decodeReply(reply)).doubleValue();
	}

	/**
	 * Hold at temperature
	 *
	 * @throws DeviceException
	 */
	@Override
	public void hold() throws DeviceException {
		logger.debug("Eurotherm setting hold state");
		String str = encode("PC", 3);
		String reply = arw.sendCommandAndGetReply(str);
		checkReply(reply);
		logger.debug("Eurotherm is in hold state");
	}

	/**
	 * Is at the set point. This overrides the method in TemperatureBase.
	 *
	 * @return true if it is
	 * @throws DeviceException
	 */
	@Override
	public boolean isAtTargetTemperature() throws DeviceException {
		String status = getStatus();
		int hexStatus = Integer.valueOf(status.substring(1), 16).intValue();

		if ((hexStatus & 0x1000) == 0x1000) {
			logger.debug("Assume heating or cooling");
			return false;
		} else if (hexStatus == 0) {
			logger.debug("Assume at temperature");
			return true;
		} else
			return false;
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

		logger.debug("Eurotherm pollDone called");

		try {
			currentTemp = getCurrentTemperature();
			if (busy && isAtTargetTemperature()) {
				busy = false;
				startHoldTimer();
			}

			logger.debug("busy is " + busy);
			if (busy)
				stateString = (targetTemp > currentTemp) ? "Heating" : "Cooling";
			else if (currentRamp > -1)
				stateString = "At temperature";
			else
				stateString = "Idle";
		} catch (DeviceException de) {
			logger.error("Error checking current temperature in pollDone", de);
		}
		if (timeSinceStart >= 0.0) {
			Date d = new Date();
			timeSinceStart = d.getTime() - startTime;
		}

		dataString = "" + n.format(timeSinceStart / 1000.0) + " " + currentTemp;

		TemperatureStatus ts = new TemperatureStatus(currentTemp, currentRamp, stateString, dataString);

		logger.debug("Eurotherm notifying IObservers with " + ts);
		notifyIObservers(this, ts);

		double outputPower = 0.0;
		double setPoint = 0.0;
		try {
			outputPower = getOutputPower();
			setPoint = getSetPoint();
		} catch (DeviceException de) {
			logger.error("Error getting output power or setpoint", de);
		}
		if (dataFileWriter != null) {
			dataFileWriter.write("" + n.format(timeSinceStart / 1000.0) + " " + setPoint + " " + currentTemp + " "
					+ outputPower);
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
	 * Get to the OutputPower
	 *
	 * @return the output power
	 * @throws DeviceException
	 */
	private double getOutputPower() throws DeviceException {
		String str = encode(OUTPOWER);
		String reply = arw.sendCommandAndGetReply(str);
		return java.lang.Double.valueOf(decodeReply(reply)).doubleValue();
	}

	/**
	 * Tells the hardware to start heating or cooling
	 *
	 * @throws DeviceException
	 */
	private void sendStart() throws DeviceException {
		String str;
		String reply;

		if (busy)
			throw new DeviceException("Eurotherm is already ramping to temperature");

		str = encode("PC", 2);
		reply = arw.sendCommandAndGetReply(str);
		checkReply(reply);

		// Send the setPoint2 stuff which will be used when the
		// heating or cooling stops
		logger.debug("Setting setPoint 2 to " + targetTemp);
		str = encode("S2", targetTemp);
		reply = arw.sendCommandAndGetReply(str);
		checkReply(reply);

		logger.debug("Setting current setPoint to setPoint 2");
		str = encode("SS", 1);
		reply = arw.sendCommandAndGetReply(str);
		checkReply(reply);

		busy = true;
	}

	/**
	 * Hold at temperature
	 *
	 * @throws DeviceException
	 */
	public void sendStop() throws DeviceException {
		String str = encode("SS", 0);
		String reply = arw.sendCommandAndGetReply(str);
		checkReply(reply);

		str = encode("PC", 1);
		reply = arw.sendCommandAndGetReply(str);
		checkReply(reply);
		busy = false;
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
		if (name.equalsIgnoreCase("datafilename"))
			return dataFileWriter.getDataFileName();
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

	@Override
	public void doStart() throws DeviceException {
		int i;
		TemperatureRamp ramp;
		String header = null;
		Date d = new Date();
		startTime = d.getTime();
		dataFileWriter.open();
		header = "#";
		dataFileWriter.write(header);
		for (i = 0; i < rampList.size(); i++) {
			ramp = rampList.get(i);
			header = "# start " + ramp.getStartTemperature() + " rate " + ramp.getRate() + " end "
					+ ramp.getEndTemperature() + " dwell " + ramp.getDwellTime();
			dataFileWriter.write(header);
		}
		header = "#Actual start time: " + d;
		dataFileWriter.write(header);

		logger.debug("Eurotherm2000 about to sendStart");
		sendStart();
		logger.debug("Eurotherm2000 returned from sendStart");
	}

	@Override
	public void doStop() throws DeviceException {
		dataFileWriter.write("#Actual end time: " + (new Date()));
		dataFileWriter.close();
		logger.debug("Eurotherm2000 about to sendStop");
		sendStop();
		logger.debug("Eurotherm2000 returned from sendStop");
	}

	/**
	 * Sends the ramps to the hardware.
	 *
	 * @param which
	 *            the temperature ramp
	 * @throws DeviceException
	 */
	@Override
	protected void sendRamp(int which) throws DeviceException {
		String str;
		String reply;

		logger.debug("Eurotherm2000 about to sendRamp");
		if (!rampList.isEmpty()) {

			// Specify editing program 1
			str = encode("EP", 1);
			reply = arw.sendCommandAndGetReply(str);
			checkReply(reply);

			// Specify segement 1 is a rate ramp
			str = encode("$1", 1);
			reply = arw.sendCommandAndGetReply(str);
			checkReply(reply);

			// Specify target temperature for the ramp
			str = encode("s1", rampList.get(which).getEndTemperature());
			reply = arw.sendCommandAndGetReply(str);
			checkReply(reply);
			targetTemp = rampList.get(which).getEndTemperature();

			// Specify rate for ramp
			str = encode("d1", rampList.get(which).getRate());
			reply = arw.sendCommandAndGetReply(str);
			checkReply(reply);

			// Specify segement 2 is an end segment
			str = encode("$2", 0);
			reply = arw.sendCommandAndGetReply(str);
			checkReply(reply);

			// Specify end segment is a reset
			str = encode("p2", 1);
			reply = arw.sendCommandAndGetReply(str);
			checkReply(reply);
		}
		logger.debug("Eurotherm2000 returned from sendRamp");
	}

	/**
	 * Starts the next ramp in the rampList.
	 *
	 * @throws DeviceException
	 * @see #sendRamp(int)
	 */
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

	/**
	 * Starts a temperature change towards previously set target temperature
	 *
	 * @throws DeviceException
	 */
	@Override
	public void startTowardsTarget() throws DeviceException {
		String str;
		String reply;

		// targetTemp will have been set by the TemperatureBase method
		// setTargetTemperature

		// Specify editing program 1
		str = encode("EP", 1);
		reply = arw.sendCommandAndGetReply(str);
		checkReply(reply);

		// Specify segement 1 is a step
		str = encode("$1", 4);
		reply = arw.sendCommandAndGetReply(str);
		checkReply(reply);

		// Specify target temperature for the ramp
		str = encode("s1", targetTemp);
		reply = arw.sendCommandAndGetReply(str);
		checkReply(reply);
		count = 0;

		// Send the command to actually start moving
		sendStart();
	}

	@Override
	protected void setHWLowerTemp(double lowerTemp) throws DeviceException {
		// FIXME should this method be implemented
	}

	@Override
	protected void setHWUpperTemp(double upperTemp) throws DeviceException {
		// FIXME should this method be implemented
	}

	@Override
	public void runRamp() throws DeviceException {
		// TODO Auto-generated method stub

	}
}
