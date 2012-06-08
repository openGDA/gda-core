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

package gda.device.digitalio;

import gda.device.DeviceException;
import gda.device.DigitalIO;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Specific to National Instrument's USB digital IO device model 9472, this class is responsible for sending (cannot
 * read) on/off bits to the device. Currently only operates on one channel.
 * 
 * @see JniDigitalIO
 */
public class NiUsb9472 extends DigitalIOBase implements DigitalIO {
	
	private static final Logger logger = LoggerFactory.getLogger(NiUsb9472.class);
	
	private JniDigitalIO jniDigitalIO = new JniDigitalIO();

	private int deviceNumber = 1;

	private int portNumber = 0;

	private int lineStart = 0;

	private int lineEnd = 7;

	private int state = -1;

	private String connectionChannel;

	private String channelName = "chan1";

	private final static int NUMBER_OF_LINES = 8;

	private byte[] lineStates = new byte[NUMBER_OF_LINES];

	/**
	 * Constructor.
	 */
	public NiUsb9472() {
	}

	@Override
	public void configure() {
		if (jniDigitalIO == null) {
			logger.error("NiUsb9472: Digitalio dll not found.");
		} else {
			try {
				connectionChannel = "Dev" + deviceNumber + "/port" + portNumber + "/line" + lineStart + ":" + lineEnd;
				logger.debug("NiUsb9472: connecting as " + connectionChannel);

				int returned = 0;

				returned = jniDigitalIO.configureDigitalIOWrite(connectionChannel);
				logger.debug("NiUsb9472: ConfigureDigitalIOWrite returned " + returned + " Error message is: "
						+ jniDigitalIO.getErrorMessage());

				returned = jniDigitalIO.start(0);
				logger.debug("NiUsb9472: start returned " + returned + " Error message is: "
						+ jniDigitalIO.getErrorMessage());

				returned = jniDigitalIO.writeChannel(0, lineStates, NUMBER_OF_LINES);
				logger.error("NiUsb9472: Write channel returned " + returned + " Error message is: "
						+ jniDigitalIO.getErrorMessage());

				returned = jniDigitalIO.stop(0);
				logger.debug("NiUsb9472: stop returned " + returned + " Error message is: "
						+ jniDigitalIO.getErrorMessage());

				// README - hardware needs to be in a known state so set here
				setState(channelName, LOW_STATE);
				state = LOW_STATE;
			} catch (Exception e) {
				logger.error("NiUsb9472: Exception caught setting state in configure");
			}
		}

	}

	/**
	 * @return device number
	 */
	public int getDeviceNumber() {
		return deviceNumber;
	}

	/**
	 * @param deviceNumber
	 */
	public void setDeviceNumber(int deviceNumber) {
		this.deviceNumber = deviceNumber;
	}

	/**
	 * @return line end
	 */
	public int getLineEnd() {
		return lineEnd;
	}

	/**
	 * @param lineEnd
	 */
	public void setLineEnd(int lineEnd) {
		this.lineEnd = lineEnd;
	}

	/**
	 * @return line start
	 */
	public int getLineStart() {
		return lineStart;
	}

	/**
	 * @param lineStart
	 */
	public void setLineStart(int lineStart) {
		this.lineStart = lineStart;
	}

	/**
	 * @return The port number.
	 */
	public int getPortNumber() {
		return portNumber;
	}

	/**
	 * Sets the port numnber.
	 * 
	 * @param portNumber
	 */
	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}

	/**
	 * Action method to return state of specified channel
	 * 
	 * @param channelName
	 *            name of channel
	 * @return current logic state of channel
	 * @throws DeviceException
	 */
	@Override
	public int getState(String channelName) throws DeviceException {
		return getState();
	}

	/**
	 * Getter method for state information - hardware cannot be queried, so software must do work.
	 * 
	 * @return current logic state of channel
	 */
	public int getState() {
		return state;
	}

	/**
	 * Action method to set state of specified channel
	 * 
	 * @param channelName
	 *            name of channel
	 * @param stateToSet
	 *            state of channel
	 * @throws DeviceException
	 */
	@Override
	public void setState(String channelName, int stateToSet) throws DeviceException {
		if (stateToSet == LOW_STATE || stateToSet == HIGH_STATE) {
			int reply = 0;
			lineStates[0] = (byte) stateToSet;

			reply = jniDigitalIO.configureDigitalIOWrite(connectionChannel);
			logger.debug("NiUsb9472: setState configure write returned " + reply + " Error message is: "
					+ jniDigitalIO.getErrorMessage());

			reply = jniDigitalIO.start(0);
			logger.debug("NiUsb9472: setState start returned  " + reply + " Error message is: "
					+ jniDigitalIO.getErrorMessage());

			reply = jniDigitalIO.writeChannel(0, lineStates, NUMBER_OF_LINES);
			logger.error("NiUsb9472: setState returned  " + reply + " Error message is: "
					+ jniDigitalIO.getErrorMessage());

			reply = jniDigitalIO.stop(0);
			logger.debug("NiUsb9472: setState stop returned  " + reply + " Error message is: "
					+ jniDigitalIO.getErrorMessage());

			state = stateToSet;
		} else {
			logger.error("NiUsb9472: Invalid state for setting - state still: " + state);
		}
	}

	/**
	 * Returns the list of channel definitions available
	 * 
	 * @return the ArrayList of channel setups
	 */
	public ArrayList<ChannelSetup> getChannelSetupList() {
		return null;// channelSetupList;
	}
}
