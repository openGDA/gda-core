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

package gda.device.xspress;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.device.Xspress;
import gda.factory.Findable;
import gda.factory.Finder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a set of Xspress boards and detectors. Actually communicates with an ExafsServer object.
 */
public class XspressSystem extends DeviceBase implements Findable, Xspress {
	
	private static final Logger logger = LoggerFactory.getLogger(XspressSystem.class);
	
	private int numberOfDetectors;

	private String exafsServerName;

	private ExafsServer exafsServer;

	private DetectorList detectorList;

	private static final String SENDMCDATA = "226";

	private static final String SETWINDOW = "227";

	private static final String INITXPRESS = "228";

	private static final String READXSPRESSDETECTOR = "245";

	private static final String READALLXSPRESSDETECTORS = "246";

	private static final String READXSPRESSBYFRAME = "247";

	private class DetectorList extends ArrayList<Detector> {
		private DetectorList() {
		}

		private Detector getDetector(int index) {
			return super.get(index);
		}
	}

	/**
	 * Constructor
	 */
	public XspressSystem() {
	}

	/**
	 * Constructor
	 * 
	 * @param exafsServer
	 * @param numberOfDetectors
	 */
	public XspressSystem(ExafsServer exafsServer, int numberOfDetectors) {
		this.exafsServer = exafsServer;
		this.numberOfDetectors = numberOfDetectors;
		configure();
	}

	@Override
	public void configure() {
		detectorList = new DetectorList();
		int numberOfBoards = (numberOfDetectors + 1) / 2;
		exafsServer = (ExafsServer) Finder.getInstance().find(exafsServerName);

		ExafsServerReply reply = exafsServer.sendCommand(INITXPRESS + " " + numberOfBoards + " " + numberOfDetectors);

		logger.debug("XspressSystem init - reply to INITXPRESS was: " + reply);
		String configFileName = LocalProperties.get("gda.device.xspress.configFileName");
		loadAndInitializeDetectors(configFileName);
	}

	/**
	 * @param exafsServerName
	 */
	public void setExafsServerName(String exafsServerName) {
		this.exafsServerName = exafsServerName;
	}

	/**
	 * @param exafsServer
	 */
	public void setExafsServer(ExafsServer exafsServer) {
		this.exafsServer = exafsServer;
	}

	/**
	 * @return exafs server name
	 */
	public String getExafsServerName() {
		return exafsServerName;
	}

	/**
	 * @return exafs server object
	 */
	public ExafsServer getExafsServer() {
		return exafsServer;
	}

	/**
	 * @param numberOfDetectors
	 */
	public void setNumberOfDetectors(int numberOfDetectors) {
		this.numberOfDetectors = numberOfDetectors;
	}

	@Override
	public int getNumberOfDetectors() throws DeviceException {
		return numberOfDetectors;
	}

	@Override
	public Detector getDetector(int index) {
		return detectorList.getDetector(index);
	}

	/**
	 * Gets the multi-channel data for a detector element.
	 * 
	 * @param detector
	 *            the detector
	 * @param startChannel
	 *            the starting channel number
	 * @param endChannel
	 *            the ending channel number
	 * @param time
	 *            the time to count for (seconds)
	 * @return array[endChannel-startChannel+1] of double values representing the counts in each channel
	 * @throws DeviceException
	 */
	@Override
	public double[] getMCData(int detector, int startChannel, int endChannel, int time) throws DeviceException {
		ArrayList<ExafsServerReply> replyList;
		int valuesPerLine = 10;
		int numberOfValues = (endChannel - startChannel + 1);
		double[] values = new double[numberOfValues];
		ExafsServerReply reply;
		StringTokenizer strTok;

		// For the duration of the data collection the timeout time of the
		// AsynchronousReaderWriter must be set to be longer than the
		// collection time.

		long timeout = exafsServer.getTimeOut();
		if (timeout / 1000 <= time)
			exafsServer.setTimeOut(time * 1500);
		replyList = exafsServer.sendCommand(SENDMCDATA + " " + detector + " " + startChannel + " " + endChannel + " "
				+ time + " " + valuesPerLine, "mcdata complete");
		exafsServer.setTimeOut(timeout);

		Iterator<ExafsServerReply> i = replyList.iterator();

		reply = i.next(); // about to start (or error)

		if (reply.getStatus() != 0) {
			throw new DeviceException(" SENDMCDATA command returned: " + reply.getCommand() + " " + reply.getStatus()
					+ " " + reply.getRest());
		}

		// The next reply should be the first one containing data.
		reply = i.next();

		logger.debug("last reply was:" + reply);
		if (reply.getStatus() == 0) {
			int j = 0;
			logger.debug("numberOfValues, valuesPerLine " + numberOfValues + " " + valuesPerLine);
			int numberOfLines = numberOfValues / valuesPerLine;
			if (numberOfValues % valuesPerLine != 0)
				numberOfLines += 1;
			logger.debug("numberOfLines " + numberOfLines);

			for (int k = 0; k < numberOfLines; k++) {
				reply = i.next();
				strTok = new StringTokenizer(reply.getRest(), " \r\n");
				// FIXME: The values.length is not necessary for the real server
				// which sends exactly the right number of numbers but saves
				// tedious workings in the dummy
				while (strTok.hasMoreTokens() && j < values.length) {
					values[j] = Double.valueOf(strTok.nextToken()).doubleValue();
					j++;
				}
			}
			reply = i.next(); // ending
			reply = i.next(); // complete
		} else {
			throw new DeviceException("Error processing SENDMCDATA command");
		}
		return values;
	}

	/**
	 * Reads the detector windows, gains etc from file and sets the real detector windows.
	 * 
	 * @param filename
	 *            detector window setup filename
	 * @return result string
	 */
	@Override
	public String loadAndInitializeDetectors(String filename) {
		BufferedReader bufferedReader;
		Detector detector;
		String result = "Not done yet";

		try {
			bufferedReader = new BufferedReader(new FileReader(filename));
			for (int i = 0; i < numberOfDetectors; i++) {
				// Create a Detector object from a line in the file
				// add it to the list of Detectors and set the window of
				// the real detector to its values
				detector = new Detector(bufferedReader.readLine());
				detectorList.add(detector);
				setDetectorWindow(i, detector.getWindowStart(), detector.getWindowEnd());
				result = "Successfully read file " + filename;
			}
			bufferedReader.close();
		} catch (IOException ioe) {
			logger.error("IOException in loadAndInitializeDetectors: " + ioe.getMessage());
			result = "Unable to read file " + filename + "\nError was " + ioe.getMessage();
		}

		return result;
	}

	/**
	 * 
	 */
	@Override
	public void quit() {
		exafsServer.quit();
		saveDetectors(LocalProperties.get("gda.device.xspress.configFileName"));
	}

	/**
	 * Reads a specified detector element.
	 * 
	 * @param which
	 *            the detector to read
	 * @return a DetectorReading for the given detector
	 */
	@Override
	public DetectorReading readDetector(int which) {
		logger.debug("readDetector called");
		DetectorReading detectorReading = null;

		ExafsServerReply reply = exafsServer.sendCommand(READXSPRESSDETECTOR + " " + which);
		logger.debug("readDetector reply was " + reply);
		logger.debug("readDetector reply.getStatus() was " + reply.getStatus());
		if (reply.getStatus() == 0) {
			StringTokenizer strTok = new StringTokenizer(reply.getRest(), " \n\r");

			int windowed = (int) Double.valueOf(strTok.nextToken()).doubleValue();
			int total = (int) Double.valueOf(strTok.nextToken()).doubleValue();
			int acc = (int) Double.valueOf(strTok.nextToken()).doubleValue();
			int resets = (int) Double.valueOf(strTok.nextToken()).doubleValue();

			detectorReading = new DetectorReading(detectorList.getDetector(which), total, resets, acc, windowed);
			logger.debug("readDetector returning: " + detectorReading);
		}

		return detectorReading;
	}

	/**
	 * Reads all the detectors.
	 * 
	 * @return an array of DetectorReadings one for each detector
	 */
	@Override
	public DetectorReading[] readDetectors() {
		ArrayList<ExafsServerReply> replyList;
		StringTokenizer strTok;
		ExafsServerReply reply;
		int windowed;
		int resets;
		int acc;
		int total;

		DetectorReading[] detectorReadings = new DetectorReading[numberOfDetectors];

		replyList = exafsServer.sendCommand(READALLXSPRESSDETECTORS + " dummy", "read done");

		Iterator<ExafsServerReply> i = replyList.iterator();
		int j = 0;
		while (i.hasNext()) {
			reply = i.next();
			if (reply.getRest().indexOf("read done") < 0) {
				strTok = new StringTokenizer(reply.getRest(), " \n\r");
				do {
					windowed = (int) Double.valueOf(strTok.nextToken()).doubleValue();
					total = (int) Double.valueOf(strTok.nextToken()).doubleValue();
					acc = (int) Double.valueOf(strTok.nextToken()).doubleValue();
					resets = (int) Double.valueOf(strTok.nextToken()).doubleValue();
					detectorReadings[j] = new DetectorReading(detectorList.getDetector(j), total, resets, acc, windowed);
					j = j + 1;
				} while (strTok.hasMoreTokens());
			}
		}

		return detectorReadings;
	}

	/**
	 * Reads all the detectors.
	 * 
	 * @param frame
	 * @return an array of DetectorReadings one for each detector
	 */
	private DetectorReading[] readDetectors(int frame) {
		ArrayList<ExafsServerReply> replyList;
		StringTokenizer strTok;
		ExafsServerReply reply;
		int windowed;
		int resets;
		int acc;
		int total;

		DetectorReading[] detectorReadings = new DetectorReading[numberOfDetectors];

		replyList = exafsServer.sendCommand(READXSPRESSBYFRAME + " " + frame, "read done");

		Iterator<ExafsServerReply> i = replyList.iterator();
		int j = 0;
		while (i.hasNext()) {
			reply = i.next();
			if (reply.getRest().indexOf("read done") < 0) {
				strTok = new StringTokenizer(reply.getRest(), " \n\r");
				do {
					windowed = (int) Double.valueOf(strTok.nextToken()).doubleValue();
					total = (int) Double.valueOf(strTok.nextToken()).doubleValue();
					acc = (int) Double.valueOf(strTok.nextToken()).doubleValue();
					resets = (int) Double.valueOf(strTok.nextToken()).doubleValue();
					detectorReadings[j] = new DetectorReading(detectorList.getDetector(j), total, resets, acc, windowed);
					j = j + 1;
				} while (strTok.hasMoreTokens());
			}
		}

		return detectorReadings;
	}

	/**
	 * Saves the detector windows, gains etc to file
	 * 
	 * @param filename
	 *            the filename to write detector setup in.
	 */
	@Override
	public void saveDetectors(String filename) {
		FileWriter fileWriter;

		try {
			fileWriter = new FileWriter(filename);
			for (Detector detector : detectorList)
				fileWriter.write(detector + "\n");
			fileWriter.close();
		} catch (IOException ioe) {
			logger.error("IOException in saveDetectors: " + ioe.getMessage());
		}
	}

	/**
	 * Sends a command to the ExafsServer.
	 * 
	 * @param command
	 *            the command
	 */
	public void sendCommand(String command) {
		ExafsServerReply reply = exafsServer.sendCommand(command);
		logger.debug("XspressSystem.sendCommand got reply " + reply);
	}

	/**
	 * Sets the window of a detector
	 * 
	 * @param detector
	 *            the detector
	 * @param lower
	 *            the start of the window
	 * @param upper
	 *            the end of the window
	 */
	@Override
	public void setDetectorWindow(int detector, int lower, int upper) {

		// set the window of the real detector to the required values
		ExafsServerReply reply = exafsServer.sendCommand(SETWINDOW + " " + detector + " " + lower + " " + upper);

		// if the command succeeds set the values in the detector object
		if (reply.getStatus() == 0) {
			detectorList.getDetector(detector).setWindow(lower, upper);
		}
	}

	/**
	 * Sets the gain
	 * 
	 * @param detector
	 *            the detector
	 * @param gain
	 *            the new gain
	 */
	@Override
	public void setDetectorGain(int detector, double gain) {
		detectorList.getDetector(detector).setGain(gain);
	}

	/**
	 * Sets the offset
	 * 
	 * @param detector
	 *            the detector
	 * @param offset
	 *            the new offset
	 */
	@Override
	public void setDetectorOffset(int detector, double offset) {
		detectorList.getDetector(detector).setOffset(offset);
	}

	@Override
	public Object readout() {
		DetectorReading[] dr = readDetectors(0);
		double[] values = new double[numberOfDetectors];

		for (int i = 0; i < values.length; i++)
			values[i] = dr[i].getWindowed();

		return values;
	}

	@Override
	public double[] readFrame(int startChannel, int channelCount, int frame) {
		double[] values = null;
		DetectorReading[] dr = readDetectors(frame);
		values = new double[channelCount];

		for (int i = startChannel; i < startChannel + channelCount; i++)
			values[i - startChannel] = dr[i].getWindowed();

		return values;
	}

	@Override
	public void setReadoutMode(int newMode) throws DeviceException {
		// Deliberately does nothing
	}
}