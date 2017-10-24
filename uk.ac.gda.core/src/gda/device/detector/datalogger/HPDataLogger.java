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

package gda.device.detector.datalogger;

import gda.device.DataLogger;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.Serial;
import gda.device.detector.DetectorBase;
import gda.device.serial.StringReader;
import gda.device.serial.StringWriter;
import gda.factory.Findable;
import gda.factory.Finder;

import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// import gda.device.datalogger.DataLoggerPanel;

/**
 * An implementation for Agilent HP34970A Data Acquisition unit (Data Logger). It has a Poller, which has a default
 * pollTime of 1 second and the implemented pollDone() method changes the dataValues array and notifies the DataLogger's
 * observers.
 *
 * @see gda.device.serial.SerialComm
 */
public class HPDataLogger extends DetectorBase implements DataLogger, Detector, Findable, Runnable, Scannable {

	private static final Logger logger = LoggerFactory.getLogger(HPDataLogger.class);

	// Commands and settings for the Data Logger
	private final String GET_NO_OF_CHANNELS = "ROUTe:SCAN:SIZE?";

	private final String READ = "Read?";

	private final String ERROR = "SYST:ERR?";

	private static final String CLEAR_ERROR_QUEUE = "*CLS";

	// private static final String RESET_AND_CLEAR = "*RST;*CLS";
	private final String WRITE_TERMINATOR = "\n";

	private final String READ_TERMINATOR = "\r\n";

	// Timeout of 8000 is sufficient for using up to 8 channels.
	// May need to increase if using more channels.
	private final int DEFAULT_TIMEOUT = 8000;

	private final int DEFAULT_POLLTIME = 1000;

	private final int DEFAULT_CHANNELS = 8;

	// RS232 communications protocol defaults:
	private Serial serial = null;

	private String parity = Serial.PARITY_EVEN;

	private int baudRate = Serial.BAUDRATE_9600;

	private int stopBits = Serial.STOPBITS_1;

	private int byteSize = Serial.BYTESIZE_7;

	// private String flowControlIn = Serial.FLOWCONTROL_NONE;
	// private String flowControlOut = Serial.FLOWCONTROL_NONE;

	private String serialDeviceName;

	// The timeout may need to be altered to allow changes from outside
	private int timeout = DEFAULT_TIMEOUT;

	private long pollTime = DEFAULT_POLLTIME;

	private int noOfChannels = DEFAULT_CHANNELS;

	// data for updating DataLoggerMonitor
	private String[] dataStringValues;

	// data for updating Scan
	private double[] dataDoubleValues;

	private boolean connected = false;

	// Max number of bytes read in per channel
	private final int numBytesReadPerChannel = 16;

	// Max number of bytes to read during sendCommand,
	// to cope with long lines caused by lots of channels.
	private int maxNumBytesToRead = 4 * numBytesReadPerChannel;

	private StringReader reader;

	private StringWriter writer;

	private Thread runner;

	// **** Default constructor, getter and setter methods for CASTOR ****
	// //
	/**
	 * Default constructor for the HP Data Logger
	 */
	public HPDataLogger() {
	}

	/**
	 * Finds the serial port, configures and starts a new Poller. Sets the port parameters, clears the error queue and
	 * reads the number of Channels in use.
	 */
	@Override
	public void configure() {
		connected = false;

		logger.debug("Finding: " + serialDeviceName);
		if ((serial = (Serial) Finder.getInstance().find(serialDeviceName)) == null) {
			logger.error("Serial Device " + serialDeviceName + " not found");
		} else {
			try {
				serial.setBaudRate(baudRate);
				serial.setStopBits(stopBits);
				serial.setByteSize(byteSize);
				serial.setParity(parity);
				serial.setReadTimeout(timeout);
				serial.flush();
				reader = new StringReader(serial);
				writer = new StringWriter(serial);
				// set up communicators
				reader.stringProps.setTerminator(READ_TERMINATOR);
				writer.stringProps.setTerminator(WRITE_TERMINATOR);
				connected = true;

				// README
				// It is vital to clear any old error messages left in the
				// buffer
				writer.write(CLEAR_ERROR_QUEUE);

				noOfChannels = readNoOfChannels();

				// Allocate arrays to store data read from DataLogger
				dataDoubleValues = new double[noOfChannels];
				dataStringValues = new String[noOfChannels];

				// reading many channels means we have to increase maxsize of
				// characters read in
				if (maxNumBytesToRead < (noOfChannels * numBytesReadPerChannel + 2)) {
					maxNumBytesToRead = noOfChannels * numBytesReadPerChannel + 2;
				}

				runner = uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName() + " " + getName());
				runner.start();
			} catch (DeviceException de) {
				logger.error("Exception while connecting the Serial Port" + de);
			}
		}
	}

	/**
	 * Returns the name of this logger device.
	 *
	 * @return Returns the serialDeviceName.
	 */
	public String getSerialDeviceName() {
		return serialDeviceName;
	}

	/**
	 * Sets the name of this logger device.
	 *
	 * @param serialDeviceName
	 *            The serialDeviceName to set.
	 */
	public void setSerialDeviceName(String serialDeviceName) {
		this.serialDeviceName = serialDeviceName;
	}

	/**
	 * Returns the value of the class pollTime variable.
	 *
	 * @return pollTime.
	 */
	public long getPolltime() {
		return pollTime;
	}

	/**
	 * Sets the class pollTime variable.
	 *
	 * @param pollTime
	 *            The pollTime to set.
	 */
	public void setPolltime(long pollTime) {
		this.pollTime = pollTime;
	}

	// **** Methods for the DataLogger itself **** //

	/**
	 * Sends the command to see if an error has occurred. The error messages from the logger device consist of a string
	 * and a code. If no error has occurred, the code value is 0.
	 *
	 * @throws DeviceException
	 */
	private void checkForError() throws DeviceException {
		String message = null;

		logger.debug("HPDataLogger: Checking for errors");
		writer.write(ERROR);
		message = reader.read();
		logger.debug("HPDataLogger: The error check message is: " + message);

		StringTokenizer strTokens = new StringTokenizer(message, ",");
		if (strTokens.countTokens() != 0) {
			String s = strTokens.nextToken();
			int errorCode = 0;
			if (s.charAt(0) == '+') {
				errorCode = Integer.parseInt(s.substring(1));
			} else {
				errorCode = Integer.parseInt(s.substring(0));
			}

			if (errorCode != 0)
				throw new DeviceException("HPDataLogger: Error found: " + message);
		}
	}

	/**
	 * Method to fufil the gda.device.detector.Detector interface
	 */
	@Override
	public void collectData() {
		try {
			// if (connected)
			// {
			// Read from Datalogger and convert to doubles array
			// which is required format for scan data.
			readValuesToDoubles();
			// notifyIObservers(this, dataDoubleValues);

			logger.debug("****************" + System.currentTimeMillis());
			// }
		} catch (DeviceException de) {
			logger.error("Error during collectData()", de);
		}
	}

	/**
	 * Method to fufil the gda.device.detector.Detector interface
	 *
	 * @return the status as either STANDBY or IDLE
	 * @throws DeviceException
	 */
	@Override
	public int getStatus() throws DeviceException {
		if (connected) {
			return Detector.STANDBY;
		}
		return Detector.IDLE;
	}

	/**
	 * @see gda.device.Detector#readout()
	 */
	@Override
	public Object readout() throws DeviceException {
		return dataDoubleValues;
	}

	/**
	 * Clears the error log queue and sets the connected flag to true. This will enable the poller allowing
	 * notifications to observers.
	 *
	 * @exception DeviceException
	 *                if the device cannot be found
	 */
	@Override
	public void connect() throws DeviceException {
		// README: Original intent was to reset the logger, but this cannot be
		// done as it trashes any channels set up on data logger.

		// Clear alarm and error queue - preserves channels setup
		writer.write(CLEAR_ERROR_QUEUE);

		connected = true;
	}

	/**
	 * Sets the "connected" flag to false, thus disabling the poller notifications to the observers.
	 *
	 * @exception DeviceException
	 */
	@Override
	public void disconnect() throws DeviceException {
		connected = false;
	}

	/**
	 * Returns the value of the class variable noOfChannels.
	 *
	 * @return number of channels
	 */
	@Override
	public int getNoOfChannels() {
		return noOfChannels;
	}

	/**
	 * Sends a command to get the number of channels included in the scan. The reply from the logger device is preceeded
	 * with a +/- sign. This must be removed for positive numbers to avoid a NumberFormatException when using the
	 * parseInt() method. There will never be a negative number.
	 *
	 * @return number of channels being used
	 * @exception DeviceException
	 */
	private int readNoOfChannels() throws DeviceException {
		String reply = sendCommand(GET_NO_OF_CHANNELS);

		return Integer.parseInt(reply.substring(1));
	}

	/**
	 * This method is currently disabled
	 *
	 * @return deviceTimeout
	 */
	public int readTimeout() {
		return 0;
	}

	/**
	 * Sends a command to read the scan values from the logger device before filling a double array with the values from
	 * the Read Buffer. Used for generating data to pass back in response to a Scan collectData call. N.B. Fills out
	 * class dataDoubleValues array.
	 *
	 * @exception DeviceException
	 */
	private void readValuesToDoubles() throws DeviceException {
		String reply = sendCommand(READ);

		StringTokenizer strTokens = new StringTokenizer(reply, ",");
		if (strTokens.countTokens() != 0) {
			for (int i = 0; ((i < noOfChannels) && strTokens.hasMoreTokens()); i++) {
				String temp = strTokens.nextToken();
				dataDoubleValues[i] = Double.parseDouble(temp.trim());
			}
		}
	}

	/**
	 * Sends a command to read the scan values from the logger device before filling a String array with the values from
	 * the Read Buffer. Used for generating data to update the DataLoggerMonitor with. N.B. Fills out class
	 * dataStringValues array.
	 *
	 * @exception DeviceException
	 */
	private void readValuesToStrings() throws DeviceException {
		String reply = sendCommand(READ);

		StringTokenizer strTokens = new StringTokenizer(reply, ",");
		if (strTokens.countTokens() != 0) {
			for (int i = 0; ((i < noOfChannels) && strTokens.hasMoreTokens()); i++) {
				String temp = strTokens.nextToken();
				dataStringValues[i] = temp.trim();
			}
		}
	}

	/**
	 * Sends a command to the logger device and reads the reply. This method always sends a subsequent command by
	 * calling the method checkForError(), as error replies are not returned via the same route. The datalogger returns
	 * a null reply when CLEAR_ERROR_QUEUE * commands are sent, which manifests as a Device Exception that is thrown to
	 * the calling method.
	 *
	 * @param command
	 *            to be sent to the logger
	 * @return data logger reply
	 * @throws DeviceException
	 */
	private synchronized String sendCommand(String command) throws DeviceException {
		String reply = null;

		try {
			logger.debug("HPDataLogger: The command sent is: " + command);
			writer.write(command);
			reply = reader.read(maxNumBytesToRead);
			logger.debug("HPDataLogger: The reply from the logger is: " + reply);
		} catch (DeviceException de) {
			logger.error("HPDataLogger: Device Exception occurred" + de);
		} finally {
			checkForError();
		}
		return reply;
	}

	/**
	 * Sends the timeout value to set the timeout of the logger device, then checks for errors. This method is currently
	 * disabled.
	 *
	 * @param timeout
	 *            the timeout to be set
	 */
	public void sendTimeout(@SuppressWarnings("unused") int timeout) {
		// deliberately left empty at present
	}

	// implements the Runnable interface

	/**
	 * Calls readValuesToStrings() and fills the dataStringValues array, then notifies its observers. DataLoggerMonitor
	 * should display the data and scans should ignore the data. Called each time the pollTime elapses, implementing
	 * Runnable interface.
	 */
	@Override
	public void run() {
		if (runner == null) {
			logger.warn("No thread to read data logger values for " + this.getName());
		}

		while (runner != null) {
			try {
				if (connected) {
					// Read from Datalogger and convert to string array
					// which is desired format for DataLoggerMonitor
					// display.
					readValuesToStrings();
					notifyIObservers(this, dataStringValues);
					logger.debug("DataLogger run Notified observers");
				}

				Thread.sleep(pollTime);
			} catch (DeviceException de) {
				logger.error("Error running DataLogger", de);
			} catch (InterruptedException ie) {
				logger.error("DataLogger interrupted", ie);
				return;
			}
		}
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		// readout() doesn't return a filename.
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "HP DataLogger";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "unknown";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "DataLogger";
	}

}
