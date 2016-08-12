/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.mar;

import gda.device.DeviceException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements socket commands to marccd software. Documented in "Appendix 3: the Remote Mode of marccd", v 0.10.17.
 * Other changes have been made to the API, up to v 0.19.
 */
public class MarCCDController extends Observable {

	private static final Logger logger = LoggerFactory.getLogger(MarCCDController.class);

	/**
	 * name of the attribute to use when setting the binning mode. All attributes must be integer or else the
	 * get/setAttribute from the Detector will not work
	 */
	public static final String BINNING = "binning mode";
	public static final int BINTWO = 2;
	public static final int BINTHREE = 3;
	public static final int BINFOUR = 4;
	public static final int BINEIGHT = 8;
	public static final String RAWDATA = "raw mode";
	public static final int CORRECTED = 0;
	public static final int RAW = 3;
	// the actual mode that
	// corresponds to raw
	// mode output in
	// readout. Example:
	// readout,3,filename to
	// the detector will
	// write out a raw file

	/**
	 * the desired dark mode for data collection
	 */
	public static final String DARKDATA = "dark mode";

	/**
	 * dark to be recorded per time interval - 9
	 */
	public static final int DARKINTERVAL = 9;

	/**
	 * dark recorded per sample - 10
	 */
	public static final int DARKSAMPLE = 10;

	/**
	 * no darks to be recorded - 11
	 */
	public static final int DARKNO = 11;

	/**
	 * the number of milliseconds until a timeout.
	 */
	private static final int TIMEOUT = 15000;

	/**
	 * number of milliseconds between printing out a notice during waitUntilDone()
	 */
	private static final long TIME_PRINT_INTERVAL = 1000;

	private Status currentState = new Status();

	private Header currentHeader = new Header();

	private MarCCDCommand command = new MarCCDCommand();

	private int corrected;

	public MarCCDController(String hostname, int port) {
		command.setHostname(hostname);
		command.setPort(port);
	}

	public MarCCDController() {
	}

	public void initialize() throws UnknownHostException, IOException {
		command.initialize();
	}

	public void close() throws IOException {
		command.close();
	}

	public void setHostname(String host) {
		command.setHostname(host);
	}

	public void setPort(int port) {
		command.setPort(port);
	}

	// wait until methods

	/**
	 * This method will check the state until the detector is busy (state=0x8). This is useful for verifying that a
	 * command has been received by the detector, and that it has started acting on the command, usually a delay of 40
	 * ms (hence the 10 ms sleeps). Once the detector is busy, it will not go to ready (state=0x0) until the command is
	 * done, says Michael Blum.
	 * 
	 * @throws DeviceException
	 */
	public void waitUntilBusy() throws DeviceException {
		Date startTime = new Date();

		updateState();
		while (!currentState.isBusy()) {
			try {
				updateState();
				checkError();
				logger.info(Integer.toHexString(getState()));
				checkTimeout(startTime);
			} catch (DeviceException e) {
				logger.error("Exception thrown:" + e.getMessage(), e);
				throw new DeviceException("waitUntilBusy:" + e.getMessage(), e);
			} catch (TimeoutException e) {
				logger.error("Timeout: " + e.getMessage(), e);
				throw new DeviceException("waitUntilBusy timeout:" + e.getMessage(), e);
			}
		}
	}

	/**
	 * @throws DeviceException
	 */
	public void waitUntilNotBusy() throws DeviceException {
		Date startTime = new Date();
		while (currentState.isBusy()) {
			try {
				Thread.sleep(100);
				updateState();
				checkError();
				checkTimeout(startTime);
			} catch (InterruptedException e) {
				logger.error("Interrupted:" + e.getMessage(), e);
				throw new DeviceException("waitUntilNotBusy:" + e.getMessage(), e);
			} catch (DeviceException e) {
				logger.error("DeviceException thrown:" + e.getMessage(), e);
				throw new DeviceException("waitUntilNotBusy:" + e.getMessage(), e);
			} catch (TimeoutException e) {
				logger.error("Timeout: " + e.getMessage(), e);
				throw new DeviceException("waitUntilNotBusy timeout:" + e.getMessage(), e);
			}
		}
	}

	/**
	 * @throws DeviceException
	 */
	public void waitUntilAllDone() throws DeviceException {
		Date startTime = new Date();
		long timeCounter = 0;
		while (!currentState.isIdle()) {
			try {
				Thread.sleep(100);
				updateState();
				checkError();
				timeCounter = checkTimeout(startTime);
				if (timeCounter % TIME_PRINT_INTERVAL < 100) {
					logger.warn("Time has exceeded " + timeCounter + " ms");
				}
			} catch (InterruptedException e) {
				logger.error("Interrupted:" + e.getMessage(), e);
				throw new DeviceException("waitUntilAllDone interrupted:" + e.getMessage(), e);
			} catch (DeviceException e) {
				logger.error("DeviceException thrown:" + e.getMessage(), e);
				throw new DeviceException("waitUntilAllDone deviceException:" + e.getMessage(), e);
			} catch (TimeoutException e) {
				logger.error("Timeout: " + e.getMessage(), e);
				throw new DeviceException("waitUntilAllDone timeout:" + e.getMessage(), e);
			}
		}
	}

	/**
	 * @throws DeviceException
	 */
	public void waitUntilReadingDone() throws DeviceException {
		Date startTime = new Date();
		while (currentState.isReading()) {
			try {
				Thread.sleep(10);
				updateState();
				checkError();
				checkTimeout(startTime);
			} catch (InterruptedException e) {
				logger.error("Interrupted:" + e.getMessage(), e);
				throw new DeviceException("waitUntilReadingDone interrupted:" + e.getMessage(), e);
			} catch (DeviceException e) {
				logger.error("Exception thrown:" + e.getMessage(), e);
				throw new DeviceException("waitUntilReadingDone:" + e.getMessage(), e);
			} catch (TimeoutException e) {
				logger.error("Timeout: " + e.getMessage(), e);
				throw new DeviceException("waitUntilReadingDone timeout:" + e.getMessage(), e);
			}
		}
	}

	/**
	 * @throws DeviceException
	 */
	public void waitUntilAcquiringDone() throws DeviceException {
		Date startTime = new Date();
		while (currentState.isAcquiring()) {
			try {
				Thread.sleep(100);
				updateState();
				checkError();
				checkTimeout(startTime);
			} catch (InterruptedException e) {
				logger.error("Interrupted:" + e.getMessage(), e);
				throw new DeviceException("waitUntilAcquiringDone interrupted:" + e.getMessage(), e);
			} catch (DeviceException e) {
				logger.error("Exception thrown:" + e.getMessage(), e);
				throw new DeviceException("waitUntilAcquiringDone:" + e.getMessage(), e);
			} catch (TimeoutException e) {
				logger.error("Timeout: " + e.getMessage(), e);
				throw new DeviceException("waitUntilAcquiringDone timeout:" + e.getMessage(), e);
			}
		}
	}

	/**
	 * @throws DeviceException
	 */
	public void waitUntilCorrectingDone() throws DeviceException {
		Date startTime = new Date();
		while (currentState.isCorrecting()) {
			try {
				Thread.sleep(100);
				updateState();
				checkError();
				checkTimeout(startTime);
			} catch (InterruptedException e) {
				logger.error("Interrupted:" + e.getMessage(), e);
				throw new DeviceException("waitUntilCorrectingDone interrupted:" + e.getMessage(), e);
			} catch (DeviceException e) {
				logger.error("Exception thrown:" + e.getMessage(), e);
				throw new DeviceException("waitUntilCorrectingDone:" + e.getMessage(), e);
			} catch (TimeoutException e) {
				logger.error("Timeout: " + e.getMessage(), e);
				throw new DeviceException("waitUntilCorrectingDone timeout:" + e.getMessage(), e);
			}
		}
	}

	/**
	 * @throws DeviceException
	 */
	public void waitUntilWritingDone() throws DeviceException {
		Date startTime = new Date();
		while (currentState.isWriting()) {
			try {
				Thread.sleep(100);
				updateState();
				checkError();
				checkTimeout(startTime);
			} catch (InterruptedException e) {
				logger.error("Interrupted:" + e.getMessage(), e);
				throw new DeviceException("waitUntilWritingDone interrupted:" + e.getMessage(), e);
			} catch (DeviceException e) {
				logger.error("Exception thrown:" + e.getMessage(), e);
				throw new DeviceException("waitUntilWritingDone:" + e.getMessage(), e);
			} catch (TimeoutException e) {
				logger.error("Timeout: " + e.getMessage(), e);
				throw new DeviceException("waitUntilWritingDone timeout:" + e.getMessage(), e);
			}
		}
	}

	/**
	 * @throws DeviceException
	 */
	public void waitUntilDezingeringDone() throws DeviceException {
		Date startTime = new Date();
		while (currentState.isDezingering()) {
			try {
				Thread.sleep(100);
				updateState();
				checkError();
				checkTimeout(startTime);
			} catch (InterruptedException e) {
				logger.error("Interrupted:" + e.getMessage(), e);
				throw new DeviceException("waitUntilDezingeringDone interrupted:" + e.getMessage(), e);
			} catch (DeviceException e) {
				logger.error("Exception thrown:" + e.getMessage(), e);
				throw new DeviceException("waitUntilDezingeringDone:" + e.getMessage(), e);
			} catch (TimeoutException e) {
				logger.error("Timeout: " + e.getMessage(), e);
				throw new DeviceException("waitUntilDezingeringDone timeout:" + e.getMessage(), e);
			}
		}
	}

	/**
	 * @throws DeviceException
	 */
	public void waitUntilReadingStarted() throws DeviceException {
		Date startTime = new Date();
		while (!currentState.isReading()) {
			try {
				Thread.sleep(50);
				updateState();
				checkError();
				checkTimeout(startTime);
			} catch (InterruptedException e) {
				logger.error("Interrupted:" + e.getMessage(), e);
				throw new DeviceException("waitUntilReadingStarted interrupted:" + e.getMessage(), e);
			} catch (DeviceException e) {
				logger.error("Exception thrown:" + e.getMessage(), e);
				throw new DeviceException("waitUntilReadingStarted:" + e.getMessage(), e);
			} catch (TimeoutException e) {
				logger.error("Timeout: " + e.getMessage(), e);
				throw new DeviceException("waitUntilReadingStarted timeout:" + e.getMessage(), e);
			}
		}
	}

	/**
	 * @throws DeviceException
	 */
	public void waitUntilDezingerStarted() throws DeviceException {
		Date startTime = new Date();
		while (!currentState.isDezingering()) {
			try {
				Thread.sleep(50);
				updateState();
				checkError();
				checkTimeout(startTime);
			} catch (InterruptedException e) {
				logger.error("Interrupted:" + e.getMessage(), e);
				throw new DeviceException("waitUntilDezingerStarted interrupted:" + e.getMessage(), e);
			} catch (DeviceException e) {
				logger.error("Exception thrown:" + e.getMessage(), e);
				throw new DeviceException("waitUntilDezingerStarted:" + e.getMessage(), e);
			} catch (TimeoutException e) {
				logger.error("Timeout: " + e.getMessage(), e);
				throw new DeviceException("waitUntilDezingerStarted timeout:" + e.getMessage(), e);
			}
		}
	}

	/**
	 * @throws DeviceException
	 */
	public void waitUntilCorrectingStarted() throws DeviceException {
		Date startTime = new Date();
		while (!currentState.isCorrecting()) {
			try {
				Thread.sleep(50);
				updateState();
				checkError();
				checkTimeout(startTime);
			} catch (InterruptedException e) {
				logger.error("Interrupted:" + e.getMessage(), e);
				throw new DeviceException("waitUntilCorrectingStarted interrupted:" + e.getMessage(), e);
			} catch (DeviceException e) {
				logger.error("Exception thrown:" + e.getMessage());
				throw new DeviceException("waitUntilCorrectingStarted:" + e.getMessage(), e);
			} catch (TimeoutException e) {
				logger.error("Timeout: " + e.getMessage(), e);
				throw new DeviceException("waitUntilCorrectingStarted timeout:" + e.getMessage(), e);
			}
		}
	}

	/**
	 * @throws DeviceException
	 */
	public void waitUntilAcquiringStarted() throws DeviceException {
		Date startTime = new Date();
		while (!currentState.isAcquiring()) {
			try {
				Thread.sleep(10);
				updateState();
				checkError();
				checkTimeout(startTime);
			} catch (InterruptedException e) {
				logger.error("Interrupted:" + e.getMessage(), e);
				throw new DeviceException("waitUntilAcquiringStarted interrupted:" + e.getMessage(), e);
			} catch (DeviceException e) {
				logger.error("Exception thrown:" + e.getMessage(), e);
				throw new DeviceException("waitUntilAcquiringStarted:" + e.getMessage(), e);
			} catch (TimeoutException e) {
				logger.error("Timeout: " + e.getMessage(), e);
				throw new DeviceException("waitUntilAcquiringStarted timeout:" + e.getMessage(), e);
			}
		}
	}

	/**
	 * @throws DeviceException
	 */
	public void waitUntilDezingeringQueued() throws DeviceException {
		Date startTime = new Date();
		while (!currentState.isDezingeringQueued()) {
			try {
				Thread.sleep(10);
				updateState();
				checkError();
				checkTimeout(startTime);
			} catch (InterruptedException e) {
				logger.error("Interrupted:" + e.getMessage(), e);
				throw new DeviceException("waitUntilDezingeringQueued interrupted:" + e.getMessage(), e);
			} catch (DeviceException e) {
				logger.error("Exception thrown:" + e.getMessage(), e);
				throw new DeviceException("waitUntilDezingeringQueued:" + e.getMessage(), e);
			} catch (TimeoutException e) {
				logger.error("Timeout: " + e.getMessage(), e);
				throw new DeviceException("waitUntilDezingeringQueued timeout:" + e.getMessage(), e);
			}
		}
	}

	/**
	 * @return boolean
	 * @throws DeviceException
	 */
	public boolean isAcquiring() throws DeviceException {
		updateState();
		return currentState.isAcquiring();
	}

	/**
	 * @return boolean
	 * @throws DeviceException
	 */
	public boolean isReading() throws DeviceException {
		updateState();
		return currentState.isReading();
	}

	/**
	 * @return boolean
	 * @throws DeviceException
	 */
	public boolean isCorrecting() throws DeviceException {
		updateState();
		return currentState.isCorrecting();
	}

	/**
	 * @return boolean
	 * @throws DeviceException
	 */
	public boolean isWriting() throws DeviceException {
		updateState();
		return currentState.isWriting();
	}

	/**
	 * @return boolean
	 * @throws DeviceException
	 */
	public boolean isDezingering() throws DeviceException {
		updateState();
		return currentState.isDezingering();
	}

	/**
	 * @return boolean
	 * @throws DeviceException
	 */
	public boolean isBusy() throws DeviceException {
		updateState();
		return currentState.isBusy();
	}

	/**
	 * @return boolean
	 * @throws DeviceException
	 */
	public boolean isError() throws DeviceException {
		updateState();
		return currentState.isError();
	}

	/**
	 * @throws DeviceException
	 */
	public void checkError() throws DeviceException {
		updateState();
		currentState.checkError();
	}

	/**
	 * Method checks whether the we are over the timeout time (MarCCDController.TIMEOUT). If so, throw a
	 * TimeoutException
	 * 
	 * @param startTime
	 *            - time when the calling method was first called
	 * @return timeout
	 * @throws TimeoutException
	 */
	public long checkTimeout(Date startTime) throws TimeoutException {
		Date currentTime = new Date();
		long difference = currentTime.getTime() - startTime.getTime();
		if (difference > TIMEOUT) {
			throw new TimeoutException("Function has timed out beyond " + TIMEOUT + " ms");
		}
		return difference;
	}

	// controller methods here
	public int[] getSize() throws IOException {
		String sizeString = command.getSize();
		String[] splitString = sizeString.split(",");
		int[] returnValue = new int[2];
		returnValue[0] = Integer.parseInt(splitString[0].trim());
		returnValue[1] = Integer.parseInt(splitString[1].trim());
		return returnValue;
	}

	/**
	 * @throws DeviceException
	 */
	public void updateState() throws DeviceException {
		try {
			String stateString = command.getState();
			if (stateString.length() > 2) {
				currentState.setState(Integer.parseInt(stateString.substring(2), 16));
			}
		} catch (Exception e) {
			throw new DeviceException("Error updating state information " + e.getMessage());
		}
	}

	/**
	 * 
	 */
	public void printState() {
		currentState.printState();
	}

	/**
	 * @return int[] background size
	 * @throws DeviceException
	 */
	public int[] getSizeBkg() throws DeviceException {
		try {
			String bkgString = command.getSizeBkg();
			String[] splitString = bkgString.split(",");
			int[] returnValue = new int[2];
			returnValue[0] = Integer.parseInt(splitString[0].trim());
			returnValue[1] = Integer.parseInt(splitString[1].trim());
			return returnValue;
		} catch (Exception e) {
			throw new DeviceException("Error getting background size " + e.getMessage());
		}
	}

	/**
	 * @return int[] bin
	 */
	public int[] getBin() throws DeviceException {
		try {
			String bkgString = command.getBin();
			String[] splitString = bkgString.split(",");
			int[] returnValue = new int[2];
			returnValue[0] = Integer.parseInt(splitString[0].trim());
			returnValue[1] = Integer.parseInt(splitString[1].trim());
			return returnValue;
		} catch (Exception e) {
			throw new DeviceException("Error getting binning information" + e.getMessage());
		}
	}

	/**
	 * @param bin
	 */
	public void setBin(int bin) {
		if (bin == MarCCDController.BINTWO) {
			doBinTwo();
		} else if (bin == MarCCDController.BINTHREE) {
			doBinThree();
		} else if (bin == MarCCDController.BINFOUR) {
			doBinFour();
		} else if (bin == MarCCDController.BINEIGHT) {
			doBinEight();
		}
	}

	private void doBinTwo() {
		command.setBin(2, 2);
	}

	private void doBinThree() {
		command.setBin(3, 3);
	}

	private void doBinFour() {
		command.setBin(4, 4);
	}

	/**
	 * This works for v 0.19.0 on i04-1-mar01
	 */
	private void doBinEight() {
		command.setBin(8, 8);
	}

	/**
	 * @return corrected
	 */
	public int getCorrected() {
		return corrected;
	}

	/**
	 * @param corr
	 */
	public void setCorrected(int corr) {
		if (corr == MarCCDController.CORRECTED) {
			corrected = corr;
		} else if (corr == MarCCDController.RAW) {
			corrected = corr;
		}
	}

	/**
	 * @return frameshift
	 */
	public int getFrameshift() throws DeviceException {
		try {
			String frameshift = command.getFrameshift();
			int shift = Integer.parseInt(frameshift);
			return shift;
		} catch (Exception e) {
			throw new DeviceException("Error getting frameshift" + e.getMessage());
		}
	}

	/**
	 * @param frameshift
	 */
	public void setFrameshift(int frameshift) {
		command.setFrameshift(frameshift);
	}

	/**
	 * @param type
	 * @param xSize
	 * @param ySize
	 */
	public void setThumbnail1(String type, int xSize, int ySize) {
		command.setThumbnail1(type, xSize, ySize);
	}

	/**
	 * @param type
	 * @param xSize
	 * @param ySize
	 */
	public void setThumbnail2(String type, int xSize, int ySize) {
		command.setThumbnail2(type, xSize, ySize);
	}

	public void start() throws DeviceException {
		header();
		command.start();
		waitUntilAcquiringStarted();
		logger.info("Acquiring started");
		checkError(); // should this be done here?
	}

	/**
	 * @param flag
	 * @throws DeviceException
	 */
	public void readout(int flag) throws DeviceException {
		String none = "";
		waitUntilReadingDone();
		command.readout(flag, none, none, none);
		waitUntilReadingStarted();
		waitUntilReadingDone();
	}

	/**
	 * @param flag
	 * @param filename
	 * @throws DeviceException
	 */
	public void readout(int flag, String filename) throws DeviceException {
		String none = "";
		waitUntilReadingDone();
		command.readout(flag, filename, none, none);
		waitUntilReadingStarted();
		waitUntilReadingDone();
	}

	/**
	 * @param flag
	 * @param filename
	 * @param thumbnail1
	 * @param thumbnail2
	 * @throws DeviceException
	 */
	public void readout(int flag, String filename, String thumbnail1, String thumbnail2) throws DeviceException {
		waitUntilReadingDone();
		command.readout(flag, filename, thumbnail1, thumbnail2);
		waitUntilReadingStarted();
		waitUntilReadingDone();
	}

	/**
	 * @param flag
	 * @throws DeviceException
	 */
	public void dezinger(int flag) throws DeviceException {
		command.dezinger(flag);
		waitUntilDezingeringQueued();
		waitUntilAllDone();
	}

	/**
	 * @throws DeviceException
	 */
	public void correct() throws DeviceException {
		command.correct();
		waitUntilCorrectingStarted();
		waitUntilAllDone();
	}

	/**
	 * @param filename
	 * @param flag
	 */
	public void writefile(@SuppressWarnings("unused") String filename, int flag) {
		// tag used as this method may be called from jython
		command.writefile(flag);
	}

	/**
	 * @param filename
	 * @param flag
	 */
	public void writeThumbnail1(String filename, int flag) {
		command.writeThumbnail1(filename, flag);
	}

	/**
	 * @param filename
	 * @param flag
	 */
	public void writeThumbnail2(String filename, int flag) {
		command.writeThumbnail1(filename, flag);
	}

	/**
	 * Abort the data acquisition (started with start command). Does not work with readout. Does not clear writing
	 * errors.
	 */
	public void abort() {
		command.abort();
	}

	/**
	 * 
	 */
	public void header() {
		String headerInfo = currentHeader.getCommandString();
		command.header(headerInfo);
	}

	/**
	 * @param state
	 */
	public void setState(int state) {
		command.setState(state);
	}

	/**
	 * @return integer value of detector state - more useful in hex
	 */
	public int getState() throws DeviceException {
		try {
			int state = Integer.parseInt(command.getState().trim().substring(2), 16);
			return state;
		} catch (Exception e) {
			throw new DeviceException("Error reading state " + e.getMessage());
		}
	}

	private class Header {

		Map<String, String> map = new HashMap<String, String>();

		/**
		 * @return command string
		 */
		String getCommandString() {
			String delimiter = ",", newline = "\n";

			StringBuilder output = new StringBuilder("header");

			for (String label : map.keySet()) {
				output.append(delimiter);
				output.append(label);
				output.append("=");
				output.append(map.get(label));
			}
			output.append(newline);
			return output.toString();
		}
	}

	/**
	 * 
	 */
	class Status {
		int state;

		// the following are the same values as used in marccd_client_socket.c,
		// version 1

		// task status masks
		static final int TASK_STATUS_QUEUED = 0x1;

		static final int TASK_STATUS_EXECUTING = 0x2;

		static final int TASK_STATUS_ERROR = 0x4;

		static final int TASK_STATUS_RESERVED = 0x8;

		static final int TASK_STATUS_BUSY = 0x8; // a holdover from v0

		// API

		// task numbers
		static final int TASK_ACQUIRE = 0;

		static final int TASK_READ = 1;

		static final int TASK_CORRECT = 2;

		static final int TASK_WRITE = 3;

		static final int TASK_DEZINGER = 4;

		// some masks
		static final int STATUS_MASK = 0xf;

		int TASK_STATUS_MASK;

		/**
		 * @param value
		 */
		public Status(int value) {
			state = value;
		}

		/**
		 * 
		 */
		public Status() {
			state = 0;
		}

		void printState() {
			System.out.println(Integer.toHexString(state));
		}

		/**
		 * @param task
		 * @return state
		 */
		int getState(int task) {
			TASK_STATUS_MASK = (STATUS_MASK << (4 * (task + 1)));
			int TASK_STATUS = (((state) & TASK_STATUS_MASK)) >> (4 * (task + 1));
			return TASK_STATUS;
		}

		/**
		 * @return acquire state
		 */
		int getAcquireState() {
			int acquireStatus = getState(TASK_ACQUIRE);
			return acquireStatus;
		}

		/**
		 * @return read state
		 */
		int getReadState() {
			int readStatus = getState(TASK_READ);
			return readStatus;
		}

		/**
		 * @return correct state
		 */
		int getCorrectState() {
			int correctStatus = getState(TASK_CORRECT);
			return correctStatus;
		}

		int getWriteState() {
			int writeStatus = getState(TASK_WRITE);
			return writeStatus;
		}

		int getDezingerState() {
			int dezingerStatus = getState(TASK_DEZINGER);
			return dezingerStatus;
		}

		int getGeneralState() {
			int generalStatus = (state & STATUS_MASK);
			return generalStatus;
		}

		/**
		 * Should be removed, as this is only useful during initial testing. Rayonix recommend not using this
		 * 
		 * @param currentState
		 */
		void setState(int currentState) {
			state = currentState;
		}

		// seems like knowing what is being executed is more important than
		// whether other jobs are queued, so only implement is(executing)
		// methods
		boolean isReading() {
			int readStatus = getState(TASK_READ);
			return ((readStatus & TASK_STATUS_EXECUTING) != 0);
		}

		boolean isAcquiring() {
			int acquireStatus = getState(TASK_ACQUIRE);
			return ((acquireStatus & TASK_STATUS_EXECUTING) != 0);
		}

		boolean isAcquiringQueued() {
			int acquireStatus = getState(TASK_ACQUIRE);
			return ((acquireStatus & TASK_STATUS_QUEUED) != 0);
		}

		boolean isCorrecting() {
			int correctStatus = getState(TASK_CORRECT);
			return ((correctStatus & TASK_STATUS_EXECUTING) != 0);
		}

		boolean isWriting() {
			int writeStatus = getState(TASK_WRITE);
			return ((writeStatus & TASK_STATUS_EXECUTING) != 0);
		}

		boolean isDezingering() {
			int dezingerStatus = getState(TASK_DEZINGER);
			return ((dezingerStatus & TASK_STATUS_EXECUTING) != 0);
		}

		boolean isDezingeringQueued() {
			int dezingerStatus = getState(TASK_DEZINGER);
			return ((dezingerStatus & TASK_STATUS_QUEUED) != 0);
		}

		boolean isBusy() {
			return (state == 8);
		}

		boolean isIdle() {
			boolean check = (state == 0);
			return check;
		}

		boolean isError() {
			int allErrorMask = (0x111111 * TASK_STATUS_ERROR) & state;
			boolean errorState = (allErrorMask != 0);
			return errorState;
		}

		// check whether there is an error, if so, throw an exception
		/**
		 * @throws DeviceException
		 */
		void checkError() throws DeviceException {
			int allErrorMask = (0x111111 * TASK_STATUS_ERROR) & state;
			// do any of the tasks have errors?
			if (allErrorMask != 0) {
				// figure out which part the error is in
				String badStatus = "";
				String otherMessage = "";
				int ACQUIRE_ERROR_MASK = TASK_STATUS_ERROR << (4 * (TASK_ACQUIRE + 1));
				int READ_ERROR_MASK = TASK_STATUS_ERROR << (4 * (TASK_READ + 1));
				int CORRECT_ERROR_MASK = TASK_STATUS_ERROR << (4 * (TASK_CORRECT + 1));
				int WRITE_ERROR_MASK = TASK_STATUS_ERROR << (4 * (TASK_WRITE + 1));
				int DEZINGER_ERROR_MASK = TASK_STATUS_ERROR << (4 * (TASK_DEZINGER + 1));
				int GENERAL_ERROR_MASK = TASK_STATUS_ERROR << 0;
				if ((ACQUIRE_ERROR_MASK & state) != 0) {
					badStatus = "acquire ";
				}
				if ((READ_ERROR_MASK & state) != 0) {
					badStatus += "read ";
				}
				if ((CORRECT_ERROR_MASK & state) != 0) {
					badStatus += "correct ";
				}
				if ((WRITE_ERROR_MASK & state) != 0) {
					badStatus += "write ";
					otherMessage = " Check that the file is being written to a valid directory";
				}
				if ((DEZINGER_ERROR_MASK & state) != 0) {
					badStatus += "dezinging ";
				}
				if ((GENERAL_ERROR_MASK & state) != 0) {
					badStatus += "general ";
				}
				if (badStatus == "") {
					badStatus = "unknown component";
				}
				throw new DeviceException(badStatus + "error. error=0x" + Integer.toHexString(allErrorMask)
						+ otherMessage);
			}
		}
	}

	/**
	 * Use socket commands to talk directly to the MARCCD Mosaic box. Most of the API commands are implemented here, and
	 * return values are all strings. MARCCDController or Status should interpret the Strings into usable information.
	 */
	public class MarCCDCommand {
		// defaults
		private String MARCCD_NAME = "localhost";

		private int COMMAND_PORT = 2222;

		// required for socket communication
		private Socket command_socket = null;

		private PrintStream command_out = null;

		private BufferedReader command_response = null;

		/**
		 * 
		 */
		public String response;

		String delimiter = ",", newline = "\n";

		/**
		 * Initialize the MARCCD command class. This should be done in an initialize or configure method so that we can
		 * change the hostname and port
		 * 
		 * @throws IOException
		 * @throws UnknownHostException
		 */
		void initialize() throws UnknownHostException, IOException {
			command_socket = new Socket(MARCCD_NAME, COMMAND_PORT);
			if (command_socket == null) {
				throw new IOException("MarCCDController - unable to create socket, stopping initialization");
			}
			command_out = new PrintStream(command_socket.getOutputStream());
			command_response = new BufferedReader(new InputStreamReader(command_socket.getInputStream()));
		}

		public void close() throws IOException {
			if (command_socket == null) {
				command_socket.close();
			}
		}

		/**
		 * @param host
		 */
		void setHostname(String host) {
			MARCCD_NAME = host;
		}

		/**
		 * @param port
		 */
		void setPort(int port) {
			COMMAND_PORT = port;
		}

		// basic sendCommand method
		/**
		 * This command should wait for the machine to get busy before returning, as suggested by Michael Blum
		 * 
		 * @param command
		 * @return String
		 * @throws IOException
		 */
		private String sendCommand(String command) throws IOException {
			String toPrint = "";
			command_out.print(command + newline);
			do {
				toPrint = command_response.readLine();
			} while (toPrint.length() <= 1);

			return toPrint.trim();
		}

		/**
		 * Send command but with no response coming back
		 * 
		 * @param command
		 */
		private void sendCommandNoResponse(String command) {
			command_out.print(command + newline);
		}

		// API commands
		String getSize() throws IOException {
			String command = "get_size";
			response = sendCommand(command);
			return response;
		}

		String getSizeBkg() throws IOException {
			String command = "get_size_bkg";
			response = sendCommand(command);
			return response;
		}

		String getBin() throws IOException {
			String command = "get_bin";
			response = sendCommand(command);
			return response;
		}

		/**
		 * @param x
		 * @param y
		 */
		void setBin(int x, int y) {
			String command = "set_bin" + delimiter + x + delimiter + y;
			sendCommandNoResponse(command);
		}

		String getFrameshift() throws IOException {
			String command = "get_frameshift";
			response = sendCommand(command);
			return response;
		}

		/**
		 * @param nlines
		 */
		void setFrameshift(int nlines) {
			String command = "set_frameshift" + delimiter + nlines;
			sendCommandNoResponse(command);
		}

		/**
		 * @param type
		 * @param xsize
		 * @param ysize
		 */
		void setThumbnail1(String type, int xsize, int ysize) {
			String command = "set_thumbnail1" + delimiter + type + delimiter + xsize + delimiter + ysize;
			sendCommandNoResponse(command);
		}

		/**
		 * @param type
		 * @param xsize
		 * @param ysize
		 */
		void setThumbnail2(String type, int xsize, int ysize) {
			String command = "set_thumbnail2" + delimiter + type + delimiter + xsize + delimiter + ysize;
			sendCommandNoResponse(command);
		}

		/**
		 * 
		 */
		void start() {
			String command = "start";
			sendCommandNoResponse(command);
		}

		/**
		 * @param flag
		 * @param filename
		 * @param thumbnail1
		 * @param thumbnail2
		 */
		void readout(int flag, String filename, String thumbnail1, String thumbnail2) {
			String command = "readout" + delimiter + flag;
			if (filename != "") {
				command += delimiter + filename;
				if ((thumbnail1 != "") & (thumbnail2 != "")) {
					command += delimiter + thumbnail1 + delimiter + thumbnail2;
				}
			}
			sendCommandNoResponse(command);
		}

		/**
		 * @param flag
		 *            0 - use and store into the latest data frame, 1 - use and store into the current background frame
		 *            2 - use and store into system scratch storage (not useful; frame dezingered with itself)
		 */
		void dezinger(int flag) {
			String command = "dezinger" + delimiter + flag;
			sendCommandNoResponse(command);
		}

		/**
		 * 
		 */
		void correct() {
			String command = "correct";
			sendCommandNoResponse(command);
		}

		/**
		 * @param flag
		 *            0 - write raw file, 1 - write corrected file
		 */
		void writefile(int flag) {
			String command = "writefile" + delimiter + flag;
			sendCommandNoResponse(command);
		}

		/**
		 * @param filename
		 * @param flag
		 */
		void writeThumbnail1(String filename, int flag) {
			String command = "writethumbnail1" + delimiter + filename + delimiter + flag;
			sendCommandNoResponse(command);
		}

		/**
		 * @param filename
		 * @param flag
		 */
		void writeThumbnail2(String filename, int flag) {
			String command = "writethumbnail2" + delimiter + filename + delimiter + flag;
			sendCommandNoResponse(command);
		}

		/**
		 * 
		 */
		void abort() {
			String command = "abort";
			sendCommandNoResponse(command);
		}

		/**
		 * @param headerInfo
		 */
		void header(String headerInfo) {
			String command = "header" + delimiter + headerInfo;
			sendCommandNoResponse(command);
		}

		/**
		 * @return state
		 * @throws IOException
		 */
		String getState() throws IOException {
			String command = "get_state";
			response = sendCommand(command);
			return response;
		}

		void setState(int state) {
			String command = "set_state," + state;
			sendCommandNoResponse(command);
		}

		/**
		 * 
		 */
		void endAutomation() {
			String command = "end_automation";
			sendCommandNoResponse(command);
		}
	}
}