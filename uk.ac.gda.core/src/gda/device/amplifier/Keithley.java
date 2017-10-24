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

package gda.device.amplifier;

import gda.device.DeviceException;
import gda.device.Gpib;
import gda.factory.Finder;
import gda.observable.IObserver;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that implements the keithley Amplifier
 */
public class Keithley extends AmplifierBase {

	private static final Logger logger = LoggerFactory.getLogger(Keithley.class);

	private static final double VOLTAGEBIAS_MAX = 5.0;

	private static final double VOLTAGEBIAS_MIN = -5.0;

	private static final String VOLTAGEBIAS = "B";

	private static final String COMMANDEXECUTOR = "X";

	private static final String ZEROCHECK_OR_CORRECT = "C";

	private static final String SETGAIN = "R";

	private static final String FILTER_RISE_TIME = "T";

	private static final String VOLTAGEBIAS_VALUE = "V";

	private static final String ENLARGEGAIN = "W";

	private static final String FILTER = "P";

	private static final String AUTO_FILTER = "Z";

	private static final String OUTPUT = "U";

	private static final String MODEL_NO = "428";

	private static final String GAIN_UNIT = "V/A";

	private static final char READ_TERMINATOR = '\r';

	private static final String CURRENT_SUPPRESS = "S";

	private final String CURRENT_SUPPRESS_SWITCH = "N";

	private final int READY = 11;

	private String name = "";

	private final double[] CURRENT_RANGE = { 0.0, 0.000000005, 0.00000005, 0.0000005, 0.000005, 0.00005, 0.0005, 0.005 };

	private final double[] RISE_TIME_RANGE = { 0.00001, 0.00003, 0.0001, 0.0003, 0.001, 0.003, 0.010, 0.030, 0.100,
			0.300 };

	private String[] statusArray = { "Invalid device dependent command was " + "received",
			"Invalid device dependent command option", "Not in remote", "", " Suppress range or value conflict",
			"Input current too large to suppress", "Auto suppression requested with zero check on",
			"Zero correct failed", "Checksum error", "overload condition ." + " reduce gain or ip current or both",
			"Gain or Rise Time conflict", "Ready" };

	private Gpib gpib;

	private int timeout = 10000;

	private String gpibInterfaceName;

	private String deviceName;

	private boolean deviceConnected = false;

	private BigDecimal voltageBiasCheckConstant = new BigDecimal(10000);

	@Override
	public void autoCurrentSuppress() throws DeviceException {
		setZeroCheck(false);
		sendCommand(CURRENT_SUPPRESS_SWITCH + 2 + COMMANDEXECUTOR);
		notifyIObservers(this, "SUPPRESSING");
		Thread tr = uk.ac.gda.util.ThreadManager.getThread(new Runnable() {

			@Override
			public synchronized void run() {
				int count = 0;
				String stat = "";
				while (true) {
					try {

						wait(100);

						if ((stat = getStatus()).equals("READY") || count == 3)
							break;
						count++;
					} catch (InterruptedException e) {
						logger.error("Auto current suppress thread interrupted", e);
					} catch (DeviceException e) {
						logger.error("Error getting status in auto current suppress thread" ,e);
					}
				}
				notifyIObservers(Keithley.this, stat);
			}

		}, getClass().getName());
		tr.start();
	}

	@Override
	public void autoZeroCorrect() throws DeviceException {
		sendCommand(ZEROCHECK_OR_CORRECT + 2 + COMMANDEXECUTOR);
		notifyIObservers(this, "CORRECTING");
	}

	/**
	 * Finds the gpib from the xml file makes sure the device is connected to the Gpib
	 */
	@Override
	public void configure() {
		logger.debug("Finding: " + gpibInterfaceName);
		if ((gpib = (Gpib) Finder.getInstance().find(gpibInterfaceName)) == null) {
			logger.error("Gpib Board " + gpibInterfaceName + " not found");
		} else {
			try {
				logger.debug("Keithley configure");
				// finds the device to make sure it is
				// actually connected to gpib
				gpib.setTimeOut(name, timeout);
				int device = gpib.findDevice(deviceName);
				logger.debug("Gpib Device " + deviceName + " found" + device);
				deviceConnected = true;
				initialiseKeithley();

			} catch (DeviceException de) {
				logger.error("Exception occured in configuring {}", name, de);
			}
		}
	}

	/**
	 * Sets the gpib interface name.
	 *
	 * @param gpibInterfaceName
	 *            the gpib interface name
	 */
	public void setGpibInterfaceName(String gpibInterfaceName) {
		this.gpibInterfaceName = gpibInterfaceName;
	}

	/**
	 * Gets the gpib interface name.
	 *
	 * @return the gpib interface name
	 */
	public String getGpibInterfaceName() {
		return gpibInterfaceName;
	}

	/**
	 * Sets the device name.
	 *
	 * @param deviceName
	 *            The device name
	 */
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	/**
	 * Gets the device name.
	 *
	 * @return The device name
	 */
	public String getDeviceName() {
		return deviceName;
	}

	/**
	 * Sets the device timeout.
	 *
	 * @param timeout
	 *            The device timeout
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	/**
	 * Gets the device timeout.
	 *
	 * @return The device timeout
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * gets the current suppress value
	 *
	 * @return the current suppress value in Amps
	 * @throws DeviceException
	 */
	@Override
	public double getCurrentSuppressValue() throws DeviceException {
		String reply = sendReplyCommand("");
		int suppressValueBeginIndex = 4;
		reply = reply.substring(suppressValueBeginIndex, (reply.indexOf(READ_TERMINATOR)));
		return Double.parseDouble(reply);

	}

	/**
	 * gets the Filter rise time
	 *
	 * @return filter rise time as a double in seconds
	 * @throws DeviceException
	 */
	@Override
	public double getFilterRiseTime() throws DeviceException {
		String reply = sendReplyCommand(OUTPUT + 0 + COMMANDEXECUTOR);
		int index = reply.indexOf(FILTER_RISE_TIME);
		int riseTimeIndex = Integer.parseInt(reply.substring((index + 1), (index + 2)));
		return RISE_TIME_RANGE[riseTimeIndex];
	}

	/**
	 * Gets the total gain of the amplifier in V/A
	 *
	 * @return gain as a string
	 * @throws DeviceException
	 */
	@Override
	public double getGain() throws DeviceException {
		String reply = sendReplyCommand(OUTPUT + 3 + COMMANDEXECUTOR);
		return Double.parseDouble(removeGainTerminator(reply));
	}

	/**
	 * Gets the status of the amplifier
	 *
	 * @return status ready if ready to accept a command or error status if error has occured
	 * @throws DeviceException
	 */
	@Override
	public String getStatus() throws DeviceException {
		String status = "";
		String reply = null;
		reply = sendReplyCommand(OUTPUT + 1 + COMMANDEXECUTOR);
		int errorIndex = reply.indexOf('1');
		if (errorIndex > 0)
			status = statusArray[errorIndex - MODEL_NO.length()];
		else {

			int pollbyte = gpib.getSerialPollByte(name);
			status = Integer.toBinaryString(pollbyte);
			String subString = "00000000";
			if (status.length() < 8) {
				status = subString.substring(status.length()) + status;
			}
			notifyIObservers(this, Integer.toString(pollbyte));

			if (status.charAt(3) == '1')
				status = statusArray[READY];
			else
				status = "ERROR";
		}
		return status;
	}

	/**
	 * Gets the status of the amplifier
	 *
	 * @param checkCommand
	 *            command whose status has to be checked
	 * @return status
	 * @throws DeviceException
	 */
	public boolean getStatus(String checkCommand) throws DeviceException {
		// FIXME
		if (checkCommand.equals(SETGAIN) || checkCommand.equals(FILTER_RISE_TIME) || checkCommand.equals("H")
				|| checkCommand.equals("J") || checkCommand.equals("K") || checkCommand.equals("M")
				|| checkCommand.equals("Y"))
			throw new DeviceException("Incorrect status check argument " + checkCommand);
		String reply = sendReplyCommand(OUTPUT + 0 + COMMANDEXECUTOR);
		int index = reply.indexOf(checkCommand);
		if (index == -1)
			throw new DeviceException("Incorrect status check argument " + checkCommand);
		char c = reply.charAt(index + 1);
		if (c == '1')
			return true;
		else if (c == '2'
				&& (checkCommand.equals(ZEROCHECK_OR_CORRECT) || checkCommand.equals(CURRENT_SUPPRESS_SWITCH)))
			return true;
		else
			return false;
	}

	/**
	 * gets the Voltage bias value in Volts
	 *
	 * @return voltage bias
	 * @throws DeviceException
	 */
	@Override
	public double getVoltageBias() throws DeviceException {
		return Double.parseDouble(removeVoltageBiasTerminator(sendReplyCommand(OUTPUT + 2 + COMMANDEXECUTOR)));
	}

	/**
	 * sets the autofilter on/off
	 *
	 * @param onOff
	 * @throws DeviceException
	 */
	@Override
	public void setAutoFilter(boolean onOff) throws DeviceException {
		int level = onOff ? 1 : 0;
		sendCommand(AUTO_FILTER + level + COMMANDEXECUTOR);
	}

	/**
	 * sets the current suppress on/off
	 *
	 * @param onOff
	 * @throws DeviceException
	 */
	@Override
	public void setCurrentSuppress(boolean onOff) throws DeviceException {
		int level = onOff ? 1 : 0;
		sendCommand(CURRENT_SUPPRESS_SWITCH + level + COMMANDEXECUTOR);
	}

	/**
	 * sets the current suppression params
	 *
	 * @param value
	 *            in Amps
	 * @throws DeviceException
	 */
	@Override
	public void setCurrentSuppressionParams(double value) throws DeviceException {
		if (value < -CURRENT_RANGE[7] || value > CURRENT_RANGE[7])
			throw new DeviceException("Current Suppress value out of range");
		sendCommand(CURRENT_SUPPRESS + "," + 0 + COMMANDEXECUTOR);
		sendCommand(CURRENT_SUPPRESS + value + "," + COMMANDEXECUTOR);
		sendCommand(CURRENT_SUPPRESS + "," + 10 + COMMANDEXECUTOR);
		// FIXME : should display the units by some other way
		notifyIObservers(this, Double.toString(getCurrentSuppressValue()) + " A");
	}

	/**
	 * sets the Current suppression params
	 *
	 * @param value
	 *            in amps
	 * @param range
	 * @throws DeviceException
	 */
	@Override
	public void setCurrentSuppressionParams(double value, int range) throws DeviceException {
		if (value < -CURRENT_RANGE[range] || value > CURRENT_RANGE[range])
			throw new DeviceException("Current Suppress value out of range" + value + " " + range);
		sendCommand(CURRENT_SUPPRESS + value + "," + range + COMMANDEXECUTOR);
		// FIXME : should display the units by some other way
		notifyIObservers(this, Double.toString(getCurrentSuppressValue()) + " A");
	}

	/**
	 * enable/disable X10 gain
	 *
	 * @param onOff
	 * @throws DeviceException
	 */
	@Override
	public void setEnlargeGain(boolean onOff) throws DeviceException {
		int level = (onOff) ? 1 : 0;
		sendCommand(ENLARGEGAIN + level + COMMANDEXECUTOR);
	}

	/**
	 * enable/disable filter
	 *
	 * @param onOff
	 * @throws DeviceException
	 */
	@Override
	public void setFilter(boolean onOff) throws DeviceException {
		int level = (onOff) ? 1 : 0;
		sendCommand(FILTER + level + COMMANDEXECUTOR);
	}

	/**
	 * sets the rise time of the filter
	 *
	 * @param level
	 * @throws DeviceException
	 */
	@Override
	public void setFilterRiseTime(int level) throws DeviceException {
		if (level >= 0 && level <= 9) {
			sendCommand(FILTER_RISE_TIME + level + COMMANDEXECUTOR);
			// FIXME : should display the units by some other way
			notifyIObservers(this, Double.toString(getFilterRiseTime()) + " secs");
		} else
			throw new DeviceException("Filter rise time out of range");
	}

	/**
	 * sets instrument gain
	 *
	 * @param level
	 * @throws DeviceException
	 */
	@Override
	public void setGain(int level) throws DeviceException {
		if (level >= 0 && level <= 10) {
			sendCommand(SETGAIN + level + COMMANDEXECUTOR);
			// FIXME : should display the units by some other way
			notifyIObservers(this, Double.toString(getGain()) + " " + GAIN_UNIT);
		} else
			throw new DeviceException("Gain out of range");
	}

	/**
	 * Enable/disable voltage bias output
	 *
	 * @param voltageBias
	 * @throws DeviceException
	 */
	@Override
	public void setVoltageBias(boolean voltageBias) throws DeviceException {
		int enable = (voltageBias) ? 1 : 0;
		sendCommand(VOLTAGEBIAS + enable + COMMANDEXECUTOR);
	}

	/**
	 * sets the voltage bias in Volts
	 *
	 * @param value
	 *            is the voltagebias
	 * @throws DeviceException
	 */
	@Override
	public void setVoltageBias(double value) throws DeviceException {
		if (value < VOLTAGEBIAS_MIN || value > VOLTAGEBIAS_MAX)
			throw new DeviceException("Keithey Amplifier :voltage bias value out of range");
		BigDecimal bd = new BigDecimal(value);
		bd.setScale(4, BigDecimal.ROUND_HALF_UP);
		if (((int) (bd.multiply(voltageBiasCheckConstant).floatValue())) % 25 == 0) {
			NumberFormat de = new DecimalFormat("####E0");
			sendCommand(VOLTAGEBIAS_VALUE + de.format(bd.floatValue()) + COMMANDEXECUTOR);
			// FIXME : should display the units by some other way
			notifyIObservers(this, Double.toString(getVoltageBias()) + " V");
		} else {
			throw new DeviceException("Keithey Amplifier :voltage bias resolution error");
		}
	}

	/**
	 * enable/disable zero check
	 *
	 * @param onOff
	 * @throws DeviceException
	 */
	@Override
	public void setZeroCheck(boolean onOff) throws DeviceException {
		int enable = (onOff) ? 1 : 0;
		sendCommand(ZEROCHECK_OR_CORRECT + enable + COMMANDEXECUTOR);
	}

	private void initialiseKeithley() throws DeviceException {
		gpib.sendDeviceClear(name);
		sendCommand("Y2X");
		gpib.setTerminator(name, READ_TERMINATOR);
		gpib.setReadTermination(name, true);
	}

	private String removeVoltageBiasTerminator(String string) {
		return (string.substring(string.indexOf('V') + 1, string.indexOf(READ_TERMINATOR)));
	}

	private String removeGainTerminator(String string) {
		return (string.substring(0, string.indexOf(GAIN_UNIT)));
	}

	/**
	 * @param command
	 *            the command to send
	 * @throws DeviceException
	 */
	private void sendCommand(String command) throws DeviceException {
		if (!deviceConnected)
			throw new DeviceException("Keithley not connected");
		gpib.write(name, command);

	}

	/**
	 * @param command
	 *            the command to send
	 * @return reply the reply string
	 * @throws DeviceException
	 */
	private String sendReplyCommand(String command) throws DeviceException {
		if (!deviceConnected)
			throw new DeviceException("Keithley not connected");
		String reply = "";
		gpib.write(name, command);
		reply = gpib.read(name, 80);
		return reply;
	}

	// FIXME : should decide if the get methods need to have notifyObserver
	// calls
	@Override
	public void addIObserver(IObserver anIObserver) {
		super.addIObserver(anIObserver);
		notifyIObservers(this, "");
	}

}
