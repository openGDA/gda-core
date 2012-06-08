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
import gda.device.detector.DAServer;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.util.Gaussian;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a set of Xspress boards and detectors. Actually communicates with an DAServer object.
 */
public class Xspress1System extends DeviceBase implements Findable, Xspress {
	
	private static final Logger logger = LoggerFactory.getLogger(Xspress1System.class);
	
	/**
	 * 
	 */
	public static final int READOUT_FILE = 0;

	/**
	 * 
	 */
	public static final int READOUT_WINDOWED = 1;

	// The local endian is set to big endian as tests show that the
	// stream.readFloat() seems to imply this. Attempts to use little endian
	// gave incorrect values on a PC.
	private static final String localEndian = "motorola";

	private static final int mcaChannels = 4096;

	private DetectorList detectorList;

	private DAServer daServer;

	private String daServerName;

	private String mcaOpenCommand = null;

	private String scalerOpenCommand = null;

	private String xspressSystemName = null;

	private int numberOfDetectors = -1;

	private int mcaHandle = -1;

	private int scalerHandle = -1;

	private boolean dummy = false;

	private class DetectorList extends ArrayList<Detector> {
		private DetectorList() {
		}

		private Detector getDetector(int index) {
			return super.get(index);
		}
	}

	@Override
	public void configure() {
		detectorList = new DetectorList();
		logger.debug("Finding: " + daServerName);
		if (!isDummy()) {
			if ((daServer = (DAServer) Finder.getInstance().find(daServerName)) == null) {
				logger.error("Server " + daServerName + " not found");
			} else {
				open();
			}
		}

		String configFileName = LocalProperties.get("gda.device.xspress.configFileName");
		loadAndInitializeDetectors(configFileName);
	}

	/**
	 * @param daServerName
	 */
	public void setDaServerName(String daServerName) {
		this.daServerName = daServerName;
	}

	/**
	 * @return da.server name
	 */
	public String getDaServerName() {
		return daServerName;
	}

	/**
	 * @return Returns the xspressSystemName.
	 */
	public String getXspressSystemName() {
		return xspressSystemName;
	}

	/**
	 * @param xspressSystemName
	 *            The xspressSystemName to set.
	 */
	public void setXspressSystemName(String xspressSystemName) {
		this.xspressSystemName = xspressSystemName;
	}

	/**
	 * @param mcaOpenCommand
	 */
	public void setMcaOpenCommand(String mcaOpenCommand) {
		this.mcaOpenCommand = mcaOpenCommand;
	}

	/**
	 * @return the mca open command
	 */
	public String getMcaOpenCommand() {
		return mcaOpenCommand;
	}

	/**
	 * @param scalerOpenCommand
	 */
	public void setScalerOpenCommand(String scalerOpenCommand) {
		this.scalerOpenCommand = scalerOpenCommand;
	}

	/**
	 * @return the scaler open command
	 */
	public String getScalerOpenCommand() {
		return scalerOpenCommand;
	}

	/**
	 * @param numberOfDetectors
	 */
	public void setNumberOfDetectors(int numberOfDetectors) {
		this.numberOfDetectors = numberOfDetectors;
	}

	@Override
	public int getNumberOfDetectors() {
		return numberOfDetectors;
	}

	/**
	 * Gets the multi-channel data for a detector element.
	 * 
	 * @param detector
	 *            the detector
	 * @param startChannel
	 *            the starting Channel
	 * @param endChannel
	 *            the ending Channel
	 * @param time
	 *            the time to count for (seconds)
	 * @return array[4096] of double values representing the counts in each channel summed between startChannel and
	 *         endChannel
	 * @throws DeviceException
	 */
	@Override
	public double[] getMCData(int detector, int startChannel, int endChannel, int time) throws DeviceException {
		int npts = endChannel - startChannel + 1;
		double[] values = new double[npts];
		if (isDummy()) {
			long[] data = createDummyData(detector);
			for (int i = 0; i < npts; i++) {
				values[i] = data[startChannel + i];
			}
		} else {
			String cmd = "xspress set-collect-time '" + xspressSystemName + "' " + time;
			if (time != 0.0 && daServer != null && daServer.isConnected()) {
				daServer.sendCommand(cmd);
			}

			logger.debug("start Channel and end Channel " + startChannel + " " + endChannel);
			values = daServer.getBinaryData("read " + startChannel + " " + detector + " 0 " + npts + " 1 1 "
					+ localEndian + " float from " + mcaHandle, npts);

		}
		return values;
	}

	private void open() {
		Object obj;
		String cmd;

		cmd = mcaOpenCommand;
		if (cmd != null && daServer != null && daServer.isConnected()) {
			if ((obj = daServer.sendCommand(cmd)) != null) {
				mcaHandle = ((Integer) obj).intValue();
				logger.debug("Xspress1System: open() using mcaHandle " + mcaHandle);
			}
		}

		cmd = scalerOpenCommand;
		if (cmd != null && daServer != null && daServer.isConnected()) {
			if ((obj = daServer.sendCommand(cmd)) != null) {
				scalerHandle = ((Integer) obj).intValue();
				logger.debug("Xspress1System: open() using scalerHandle " + scalerHandle);
			}
		}
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
		// Deliberately does nothing.
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

		long[] values = new long[4];
		values = daServer.getLongBinaryData("read 0 " + which + " 0 " + " 4 " + " 1 1 " + localEndian + " long from "
				+ scalerHandle, 4);

		detectorReading = new DetectorReading(detectorList.getDetector(which), (int) values[2], (int) values[3],
				(int) values[1], (int) values[0]);
		logger.debug("readDetector returning: " + detectorReading);

		return detectorReading;
	}

	/**
	 * Reads all the detectors.
	 * 
	 * @return an array of DetectorReadings one for each detector
	 */
	@Override
	public DetectorReading[] readDetectors() {
		return readDetectors(0);
	}

	/**
	 * Reads all the detectors.
	 * 
	 * @param frame
	 * @return an array of DetectorReadings one for each detector
	 */
	private DetectorReading[] readDetectors(int frame) {
		DetectorReading[] detectorReadings = new DetectorReading[numberOfDetectors];

		int npts = 4 * numberOfDetectors;
		long[] values;
		if (isDummy()) {
			for (int k = 0; k < numberOfDetectors; k++) {
				values = createDummyData(k);
				detectorReadings[k] = windowDummyData(values, k);
			}
		} else {
			values = daServer.getLongBinaryData("read 0 0 " + frame + " 4 " + numberOfDetectors + " 1 " + localEndian
					+ " long from " + scalerHandle, npts);

			for (int j = 0; j < numberOfDetectors; j++) {
				int i = j * 4;
				detectorReadings[j] = new DetectorReading(detectorList.getDetector(j), (int) values[i + 2],
						(int) values[i + 3], (int) values[i + 1], (int) values[i + 0]);
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
	 * Sets the window of the given detector.
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
		if (isDummy()) {
			detectorList.getDetector(detector).setWindow(lower, upper);
		} else {
			Object obj = null;
			String cmd = "xspress set-windows '" + xspressSystemName + "' " + detector + " " + lower + " " + upper;
			if (daServer != null && daServer.isConnected()) {
				obj = daServer.sendCommand(cmd);
				if (((Integer) obj).intValue() == 0) {
					// if the command succeeds set the values in the
					// detector object
					detectorList.getDetector(detector).setWindow(lower, upper);
				}
			}
		}
	}

	/**
	 * Sets the gain for the given detector.
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
	 * Sets the offset for the given detector.
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
	public Detector getDetector(int which) throws DeviceException {
		return detectorList.getDetector(which);
	}

	/**
	 * Reads the values from the detectors and returns them.
	 * 
	 * @return the windowed data
	 * @throws DeviceException
	 */
	@Override
	public Object readout() throws DeviceException {
		DetectorReading[] dr = readDetectors(0);
		double[] values = new double[numberOfDetectors];

		for (int i = 0; i < values.length; i++)
			values[i] = dr[i].getWindowed();

		return values;
	}

	/**
	 * Returns the data for a single time frame
	 * 
	 * @param startChannel
	 *            starting channel number
	 * @param channelCount
	 *            how many channels
	 * @param frame
	 *            which time frame
	 * @return an array of data (one value for each detector element)
	 */
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
	public void setReadoutMode(int readoutMode) {
		// Deliberately does nothing
	}

	/**
	 * Returns whether or not this behaves as a dummy.
	 * 
	 * @return the value of dummy
	 */
	public boolean isDummy() {
		return dummy;
	}

	/**
	 * Sets whether or not this behaves as a dummy.
	 * 
	 * @param dummy
	 */
	public void setDummy(boolean dummy) {
		this.dummy = dummy;
	}

	/*
	 * Creates some dummy data @return an array of dummy values
	 */
	private long[] createDummyData(int detector) {
		long[] data = new long[mcaChannels];
		Gaussian gaussianOne;
		Gaussian gaussianTwo;
		double noiseLevel = 0.2 + 0.01 * detector;

		gaussianOne = new Gaussian(1600.0 + 100.0 * detector, 500.0, 1000.0);
		gaussianTwo = new Gaussian(1000.0 + 10.0 * detector, 200.0, 500.0);
		for (int i = 0; i < mcaChannels; i++) {
			data[i] = (long) (gaussianOne.yAtX(i) * (1.0 - Math.random() * noiseLevel) + gaussianTwo.yAtX(i)
					* (1.0 - Math.random() * noiseLevel));
		}

		return data;
	}

	private DetectorReading windowDummyData(long[] data, int detector) {
		int total = 0;
		int windowed = 0;
		int acc = 0;
		int resets = 0;
		Detector d = null;
		DetectorReading dr = null;
		try {
			d = getDetector(detector);
			System.out.println("WWWWWWWINDOW " + d.getWindowStart() + " " + d.getWindowEnd());
			for (int i = 0; i < data.length; i++) {
				total += data[i];
				if (i > d.getWindowStart() && i < d.getWindowEnd()) {
					windowed += data[i];
				}
			}
			dr = new DetectorReading(d, total, resets, acc, windowed);
		} catch (DeviceException e) {
			logger.error("DeviceException creating dummy data");
		}

		return dr;
	}
}