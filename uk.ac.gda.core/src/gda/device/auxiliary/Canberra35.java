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

package gda.device.auxiliary;

import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.device.MCAException;
import gda.device.MCAStatus;
import gda.device.Serial;
import gda.device.serial.StringReader;
import gda.device.serial.StringWriter;
import gda.factory.Finder;

import java.text.DecimalFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to control the Canberra35 MCA
 */
// FIXME this class needs refactoring possibly to implement the Memmory
// interface and remove unnecessary system.out's and then comment it.
public class Canberra35 extends DeviceBase {

	private static final Logger logger = LoggerFactory.getLogger(Canberra35.class);

	private final static int READ_TIMEOUT = 1000;

	private final static int WORDSIZE = 6;

	private int memorySpace;

	private int memories;

	private int liveTimes[];

	private boolean memoryChanged[];

	private StringReader reader;

	private StringWriter writer;

	private String serialDeviceName;

	private Serial serial;

	private int status;

	private StringBuffer prompt;

	private String response = "";

	private String parity = Serial.PARITY_NONE;

	private int baudRate = Serial.BAUDRATE_9600;

	private int stopBits = Serial.STOPBITS_2;

	private int byteSize = Serial.BYTESIZE_8;

	/**
	 * The Constructor.
	 */
	public Canberra35() {
		liveTimes = new int[4]; // not sure about the size
		memoryChanged = new boolean[4];
		liveTimes[0] = liveTimes[1] = liveTimes[2] = liveTimes[3] = 0;
		memoryChanged[0] = memoryChanged[1] = memoryChanged[2] = memoryChanged[3] = false;
		memorySpace = 4096; // can be obtained from xml file
		status = MCAStatus._READY;
		memories = 4;
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
				reader = new StringReader(serial);
				writer = new StringWriter(serial);
				reader.setStripTerminators(false);
				reader.stringProps.setTerminator("!");
				reader.stringProps.addTerminator("?");
				reader.stringProps.addTerminator("T");
				reader.stringProps.addTerminator("\n");

				prompt = new StringBuffer("#A00FB");
				logger.debug("inside init...");
				logger.debug("Initialising Canberra35...");

				writer.write(prompt.toString());
				response = reader.read();
				logger.debug("MCA response is" + response);
			} catch (DeviceException de) {
				logger.error("Error Initialising Canberra35 : " + de);
				// throw new MCAException(MCAStatus.FAULT,"Error initialising
				// Canberra35");
			} catch (Exception e) {
				logger.error("Unknown error initialising Canberra35");
				// throw new MCAException(MCAStatus.UNKNOWN, "Unknown error
				// initialising Canberra35");
			}
		}
	}

	/**
	 * Sets the serial device name.
	 *
	 * @param serialDeviceName
	 *            The serial device name
	 */
	public void setSerialDeviceName(String serialDeviceName) {
		this.serialDeviceName = serialDeviceName;
	}

	/**
	 * Gets the serial device name.
	 *
	 * @return The serial device name
	 */
	public String getSerialDeviceName() {
		return serialDeviceName;
	}

	/**
	 * @param length
	 * @throws MCAException
	 */
	public void setLength(int length) throws MCAException {
		if (length > 0) {
			logger.error("memory size is set to " + memorySpace / length);
			setMemories(memorySpace / length);
		}
	}

	private int getLength(int memory) {
		return (memorySpace / memory);
	}

	/**
	 * @return length
	 */
	public int getLength() {
		return (memorySpace / memories);
	}

	/**
	 * @return memory configuration
	 */
	public int[] getMemoryConfigurations() {
		int memoryConfig[] = new int[3];
		memoryConfig[0] = 4;
		memoryConfig[1] = 2;
		memoryConfig[2] = 1;

		return memoryConfig;
	}

	/**
	 * @param memory
	 * @throws MCAException
	 */
	public void setMemories(int memory) throws MCAException {
		logger.debug("Setting number Canberra35 memories to " + memory);

		status = getStatus();
		if (status != MCAStatus._READY)
			throw new MCAException(MCAStatus.from_int(status), "Cannot set the number of Canberra35 memories");

		switch (memory) {
		case 1:
		case 2:
		case 4:
			memories = memory;
			break;
		default:
			throw (new MCAException(MCAStatus.from_int(status), "Illegal number for Canberra35 memories"));
			// break;
		}
	}

	/**
	 * @return memories
	 */
	public int getMemories() {
		return memories;
	}

	private char getOffset(int memoriesNo, int memoryNo) {
		char c = (char) (memoriesNo + memoryNo + '?');
		return c;
	}

	private int readLiveTime(int memory) throws MCAException {
		prompt = new StringBuffer("# 00I@%");
		logger.debug("the offset is " + getOffset(memories, memory));
		prompt.setCharAt(1, getOffset(memories, memory));

		try {
			logger.debug("writing the read command" + prompt);
			writer.write(prompt.toString());
			logger.debug("finished writing the read command");
			response = reader.read();
			logger.debug("the response is" + response);
			writer.write("%");
			logger.debug("waiting for reply after writing %");
			response = reader.read(8);
			logger.debug("the second reponse is" + response + response.length());
			writer.write("%");
			response = reader.read(8);
			logger.debug("the live time as read from canberra is " + response + response.length());

			if (response.length() < 8) {
				logger.debug("inside length checking" + response.length());
				return -1;
			}
			logger.debug("outside length checking");
			return Integer.parseInt(response.substring(0, WORDSIZE));
		}

		catch (DeviceException de) {
			throw new MCAException(MCAStatus.FAULT, "Error reading live time" + de);
		}

		catch (Exception e) {
			throw new MCAException(MCAStatus.UNKNOWN, " Unknown error reading live time" + e);
		}
	}

	private void clearLiveTime(int memory) throws MCAException {
		prompt = new StringBuffer("# 00J@");
		logger.debug("the offset is " + getOffset(memories, memory));
		prompt.setCharAt(1, getOffset(memories, memory));

		try {
			logger.debug(prompt.toString());
			writer.write(prompt.toString());
			response = reader.read();

			prompt = new StringBuffer("#000000  ");
			writer.write(prompt.toString());

			response = reader.read(8);
		}

		catch (DeviceException de) {
			throw new MCAException(MCAStatus.FAULT, "Error clearing  live time");
		} catch (Exception e) {
			throw new MCAException(MCAStatus.UNKNOWN, " Unknown error clearing live time");
		}
	}

	/**
	 * @param memory
	 * @return live time
	 * @throws MCAException
	 */
	public int getLiveTime(int memory) throws MCAException {
		if (memoryChanged[memory - 1]) {
			status = getStatus();

			if (status != MCAStatus._READY)
				throw new MCAException(MCAStatus.from_int(status), " Cannot read data - mca busy");

			int liveTime = readLiveTime(memory);
			if (liveTime < 0) {
				logger.debug("live time get" + liveTime);
				throw new MCAException(MCAStatus.from_int(status), " Error reading live time");
			}

			liveTimes[memory - 1] += liveTime * 1000; // convert to
			// milliseconds
			memoryChanged[memory - 1] = false;
		}

		logger.debug("Live time for memory" + memory + "is" + liveTimes[memory - 1]);

		return liveTimes[memory - 1];
	}

	/**
	 * @return status
	 */
	public int getStatus() {

		switch (status) {
		case MCAStatus._BUSY:
			try {
				response = reader.read();
				logger.debug("MCA response " + response);

				if (response.indexOf("!") != -1)
					status = MCAStatus._READY;
				else
					status = MCAStatus._UNKNOWN; // unexpected characters
				// in
				// reply
			}

			catch (DeviceException de) {
				logger.error("Error getting status", de);
				status = MCAStatus._FAULT;
			}

			catch (Exception e) {
				status = MCAStatus._UNKNOWN;
			}

			if (status != MCAStatus._BUSY) {
				// Try to recover

				if (_reset())
					status = MCAStatus._READY;
			}
			break;

		case MCAStatus._READY:
		case MCAStatus._FAULT:
		case MCAStatus._UNKNOWN:
			// Try to recover

			if (_reset())
				status = MCAStatus._READY;
			break;

		default:
			break;
		}

		return status;
	}

	private boolean _reset() {
		prompt = new StringBuffer("#A00F@");

		try {
			writer.write(prompt.toString());
			response = reader.read();

			return (response.indexOf("!") != -1);
		}

		catch (DeviceException de) {
			// do nothing
		}

		return false;
	}

	/**
	 * @throws MCAException
	 */
	public void reset() throws MCAException {
		status = getStatus();

		if (status != MCAStatus._READY)
			throw new MCAException(MCAStatus.from_int(status), " Cannot reset Canberra35");

	}

	/**
	 * @throws MCAException
	 */
	public void stop() throws MCAException {
		throw new MCAException(MCAStatus.FAULT, "Canberra35 : Stop not implemented");
	}

	/**
	 * @param memory
	 * @param dwellTime
	 * @throws MCAException
	 */
	public void collect(int memory, int dwellTime) throws MCAException {
		status = getStatus();

		if (status != MCAStatus._READY)
			throw new MCAException(MCAStatus.from_int(status), "cannot start data collection");

		// read current live time if necessary
		if (memoryChanged[memory - 1]) {
			int liveTime = readLiveTime(memory);

			if (liveTime < 0)
				throw new MCAException(MCAStatus.from_int(status), " Error reading live time");

			liveTimes[memory - 1] += liveTime * 1000;

			memoryChanged[memory - 1] = false;

			status = getStatus();
			if (status != MCAStatus._READY)
				throw new MCAException(MCAStatus.from_int(status), "Error resetting Canberra");
		}

		// clear live time and reset

		clearLiveTime(memory);
		status = getStatus();
		if (status != MCAStatus._READY)
			throw new MCAException(MCAStatus.from_int(status), "Error resetting Canberra");

		// Calculate dwell time value and significant figures

		double ld = Math.log(dwellTime) / Math.log(10.0) - 3.0;
		int msd = (int) ld;
		int lsd = (int) (Math.pow(10.0, ld - msd) + 0.5);
		char ch = getOffset(memories, memory);

		logger.debug("Starting data collection in memory " + memory + " for " + dwellTime + " milliseconds");

		logger.debug("Setting dwellTime ...");

		prompt = new StringBuffer("#");
		prompt.append(ch);
		prompt.append(lsd);
		prompt.append(msd);
		prompt.append("A@");

		logger.debug(prompt.toString());

		try {
			writer.write(prompt.toString());
		}

		catch (DeviceException de) {
			throw new MCAException(MCAStatus.FAULT, "Error collecting data");
		}

		catch (Exception e) {
			throw new MCAException(MCAStatus.UNKNOWN, "Unknown error collecting data");
		}

		memoryChanged[memory - 1] = true;
		status = MCAStatus._BUSY;
	}

	/**
	 * @param data
	 * @param memory
	 * @throws MCAException
	 */
	public void write(final int data[], int memory) throws MCAException {
		status = getStatus();
		if (status != MCAStatus._READY)
			throw new MCAException(MCAStatus.from_int(status), "Cannot write data");

		int count = 0;
		// write
		prompt = new StringBuffer("# 00J@");
		logger.debug("the offset is " + getOffset(memories, memory));
		prompt.setCharAt(1, getOffset(memories, memory));

		try {
			writer.write(prompt.toString());

			response = reader.read();

			while (true) {

				DecimalFormat df = new DecimalFormat();
				df.applyPattern("000000");
				String s = df.format(data[count]);
				if (s.length() > WORDSIZE)
					s = s.substring(0, 6);

				prompt = new StringBuffer(s);
				prompt.append(" ");
				writer.write(prompt.toString());

				response = reader.read(8);
				count++;

				if (response.indexOf("T") == -1) {
					if ((response.indexOf("!")) == -1)
						throw new MCAException(MCAStatus.FAULT, "Error writing to memory(no terminator)");
					break;

				}
			}
		}

		catch (DeviceException de) {
			throw new MCAException(MCAStatus.FAULT, "Error writing to memory");
		} catch (Exception e) {
			throw new MCAException(MCAStatus.UNKNOWN, "Error writing to memory(Unknown)");

		}
	}

	// not sure what type of array is the return value
	/**
	 * @param memory
	 * @return int[]
	 * @throws MCAException
	 */
	public int[] read(int memory) throws MCAException {
		status = getStatus();

		if (status != MCAStatus._READY)
			throw new MCAException(MCAStatus.from_int(status), "Cannot read data");

		prompt = new StringBuffer("# 00I@%");
		int values = getLength(memories);

		int memoryData[] = new int[values];
		int count = 0;
		logger.debug("the offset is " + getOffset(memories, memory));
		prompt.setCharAt(1, getOffset(memories, memory));

		try {

			logger.debug("sending read command" + prompt);
			writer.write(prompt.toString());
			logger.debug("finished writing the read command");

			response = reader.read();
			logger.debug("the response is" + response.length() + response);
			writer.write("%");
			response = reader.read(8);
			logger.debug("the response after sending % is" + response.length() + response);
			prompt = new StringBuffer("%");

			while (true) {
				writer.write(prompt.toString());
				logger.debug("waiting for reply afre % " + prompt);

				response = reader.read(8);
				logger.debug("the response is" + response);

				if (response.length() < 8) {
					logger.debug("inside length checking" + response.length());
					if (response.indexOf("!") == -1)
						throw new MCAException(MCAStatus.FAULT, "Error reading memory (no terminator)");
					break;
				}
				logger.debug("the string length is" + response.length());
				memoryData[count] = Integer.parseInt(response.substring(0, 6));
				// Message.out(count);
				count++;
				logger.debug("the count is" + count);
			}

			if (memoryChanged[memory - 1]) {
				liveTimes[memory - 1] = +memoryData[0] * 1000;
				memoryChanged[memory - 1] = false;
			}

			return memoryData;
		}

		catch (DeviceException de) {
			throw new MCAException(MCAStatus.FAULT, "Error reading memory" + de);
		} catch (Exception e) {
			throw new MCAException(MCAStatus.UNKNOWN, "Error reading memory (unknown)" + e);
		}
	}

	/**
	 * @param memory
	 * @throws MCAException
	 */
	public void clear(int memory) throws MCAException {
		status = getStatus();
		if (status != MCAStatus._READY)
			throw new MCAException(MCAStatus.from_int(status), " Cannot clear canberra");

		prompt = new StringBuffer("# 00K@%");
		logger.debug("the offset is " + getOffset(memories, memory));
		prompt.setCharAt(1, getOffset(memories, memory));

		try {
			logger.debug(prompt.toString());
			writer.write(prompt.toString());

			response = reader.read();

			// get 000000__ after clear
			response = reader.read(8);

			liveTimes[memory - 1] = 0;
			memoryChanged[memory - 1] = false;
		}

		catch (DeviceException de) {
			throw new MCAException(MCAStatus.FAULT, " Error clearing memory");
		} catch (Exception e) {
			throw new MCAException(MCAStatus.UNKNOWN, "Error clearing memory(unknown)");
		}

	}
}
