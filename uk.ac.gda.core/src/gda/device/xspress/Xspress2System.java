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
import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.data.metadata.Metadata;
import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.device.Timer;
import gda.device.Xspress;
import gda.device.detector.DAServer;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.jython.Jython;
import gda.jython.JythonServerFacade;
import gda.util.Gaussian;
import gda.util.Lockable;
import gda.util.LoggingConstants;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a set of Xspress2 boards and detectors. Actually communicates with a DAServer object which is connected to
 * a real daserver. Makes up suitable data if dummy is set to true.
 */
public class Xspress2System extends DeviceBase implements Findable, Xspress,Lockable {
	/**
	 * Possible value of readoutMode - implies readout will return a filename.
	 */
	public static final int READOUT_FILE = 0;

	/**
	 * Possible value of readoutMode - implies readout will return windowed data.
	 */
	public static final int READOUT_WINDOWED = 1;

	/** * File extension to use for files created by DAserver/XSPRESS2. */
	public static final String fileExtension = "xsp";

	// These are the names of the things which can be set by setAttribute.
	private static final String USER = "User";

	private static final String PASSWORD = "Password";

	private static final String HOST = "Host";

	private static final String REMOTEENDIAN = "Endian";

	// These are used to calculate the size of the data
	private static final int mcaChannels = Xspress2Utilities.mcaChannels;

	private static final int mcaGrades = Xspress2Utilities.mcaGrades;

	// These are the objects this must know about.
	private String daServerName;

	private DAServer daServer = null;

	private String tfgName;

	private Timer tfg = null;

	private int readoutMode = READOUT_FILE;

	private ArrayList<Detector> detectorList;

	private String mcaOpenCommand = null;

	private String scalerOpenCommand = null;

	private String startupScript = null;

	private int numberOfDetectors;

	private int mcaHandle = -1;

	private int scalerHandle = -1;

	private boolean dummy = false;

	private String xspressSpoolDirectory = null;

	private String beamlineName = null;

	private NumTracker runNumber = null;
	private boolean readDetectorsUsingScript = false;

	protected static final Logger logger = LoggerFactory.getLogger(Xspress2System.class);
	private static final ReentrantLock deviceLock = new ReentrantLock();

	@Override
	public void configure() throws FactoryException {
		// A real system needs a connection to a real da.server via a DAServer
		// object.
		if (!isDummy()) {
			logger.debug("Xspress2System.configure(): finding: " + daServerName);
			if ((daServer = (DAServer) Finder.getInstance().find(daServerName)) == null) {
				logger.error("Xspress2System.configure(): Server " + daServerName + " not found");
			}
		}

		// Both dummy and real systems should have a tfg
		logger.debug("Xspress2System.configure(): finding " + tfgName);
		if ((tfg = (Timer) Finder.getInstance().find(tfgName)) == null) {
			logger.error("Xspress2System.configure(): TimeFrameGenerator " + tfgName + " not found");
		}

		// If everything has been found send the open commands.
		if (tfg != null && (daServer != null || isDummy())) {
			try {
				open();
			} catch (DeviceException e) {
				throw new FactoryException(e.getMessage(),e);
			}
		}

		detectorList = new ArrayList<Detector>();

		String configFileName = LocalProperties.get("gda.device.xspress.configFileName");
		loadAndInitializeDetectors(configFileName);

		// Setup where we are going to spool the data to.
		xspressSpoolDirectory = "/tmp";
		try {
			xspressSpoolDirectory = PathConstructor.createFromProperty("gda.device.xspress.spoolDir");
		} catch (Exception e) {
			logger.info("Spool directory path not available , using /tmp directory");
		}

		// Lets see if we can determine the name of the instrument/beamline we
		// are
		// using ?
		Metadata metadata = (Metadata) Finder.getInstance().find("GDAMetadata");
		if (metadata != null) {

			try {
				// Default to 'tmp' as this will be used by the NumTracker to
				// determine the scan number, therefore if we are using just SRS
				// format then it will look at the XX.tmp Numtracker file.
				beamlineName = metadata.getMetadataValue("instrument", "gda.instrument", "tmp");
			} catch (DeviceException e) {
				logger.error("Errors occurred when trying to determine instrument name.");
			}
		}

		// Create a NumTracker instance
		try {
			runNumber = new NumTracker(beamlineName);
		} catch (IOException e) {
			logger.error("Failed to create NumTracker.");
		}
		readDetectorsUsingScript = LocalProperties.check("gda.xspress.readusingscript", false);
	}

	/**
	 * Sets daServerName, which is the name of the DAServer object used to communicate with the actual da.server.
	 * 
	 * @param daServerName
	 */
	public void setDaServerName(String daServerName) {
		this.daServerName = daServerName;
	}

	/**
	 * Gets daServerName.
	 * 
	 * @return the name
	 */
	public String getDaServerName() {
		return daServerName;
	}

	/**
	 * Sets numberOfDetectors, this is the number of detector elements in the xspress2 system.
	 * 
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
	 * @return The number of resolution grades
	 */
	public int getNumberofGrades() {
		return mcaGrades;
	}

	/**
	 * Sets the da.server command which should be used to open the mca connection.
	 * 
	 * @param mcaOpenCommand
	 *            the command
	 */
	public void setMcaOpenCommand(String mcaOpenCommand) {
		this.mcaOpenCommand = mcaOpenCommand;
	}

	/**
	 * Gets the da.server command which is used to open the mca connection.
	 * 
	 * @return the command
	 */
	public String getMcaOpenCommand() {
		return mcaOpenCommand;
	}

	/**
	 * Sets the da.server command which should be used to open the scaler connection.
	 * 
	 * @param scalerOpenCommand
	 *            the command
	 */
	public void setScalerOpenCommand(String scalerOpenCommand) {
		this.scalerOpenCommand = scalerOpenCommand;
	}

	/**
	 * Gets the da.server command which is used to open the scaler connection.
	 * 
	 * @return the command
	 */
	public String getScalerOpenCommand() {
		return scalerOpenCommand;
	}

	/**
	 * Sets startupScript which is a start up command to send to daserver.
	 * 
	 * @param startupScript
	 *            the startup command
	 */
	public void setStartupScript(String startupScript) {
		this.startupScript = startupScript;
	}

	/**
	 * Gets startupScript.
	 * 
	 * @return the startup command
	 */
	public String getStartupScript() {
		return startupScript;
	}

	/**
	 * Sends the daServer commands to clear the xspress system. Note that this is very time consuming and should only be
	 * done when necessary.
	 * @throws DeviceException 
	 */
	public void clear() throws DeviceException {
		Object obj;
		if (mcaHandle < 0)
			open();
		if (mcaHandle >= 0) {
			if (daServer != null && daServer.isConnected()) {
				obj = daServer.sendCommand("clear " + mcaHandle);
				if (((Integer) obj).intValue() == -1)
					logger.error(getName() + ": clear failed");
			}
		}
	}

	/**
	 * Sends the daServer commands to start the xspress system counting.
	 * @throws DeviceException 
	 */
	public void start() throws DeviceException {
		if (mcaHandle < 0)
			open();
		if (mcaHandle >= 0 && daServer != null && daServer.isConnected())
			daServer.sendCommand("enable " + mcaHandle);
	}

	/**
	 * Sends the daServer commands to stop the xspress system counting.
	 * @throws DeviceException 
	 */
	public void stop() throws DeviceException {
		if (mcaHandle < 0)
			open();
		if (mcaHandle >= 0 && daServer != null && daServer.isConnected())
			daServer.sendCommand("disable " + mcaHandle);
	}

	/**
	 * Creates dummy data for a detector. The data is a pair of overlapping Gaussians. The peak positions depend on
	 * which detector is specified so that window and gain setting code can be tested.
	 * 
	 * @param detector
	 *            the detector number
	 * @return suitable dummy data
	 */
	private long[] createDummyData(int detector) {
		long[] data = new long[mcaChannels * mcaGrades];
		Gaussian gaussianOne;
		Gaussian gaussianTwo;
		double noiseLevel = 0.2 + 0.01 * detector;

		for (int k = 0; k < mcaGrades; k++) {
			gaussianOne = new Gaussian(1600.0 + 100.0 * detector, (mcaGrades - k) * 20.0, 1000.0);
			gaussianTwo = new Gaussian(1000.0 + 100.0 * detector, (mcaGrades - k) * 20.0, 500.0);
			for (int i = 0; i < mcaChannels; i++) {
				data[i + k * mcaChannels] = 1;
				/*
				 * (long) (gaussianOne.yAtX(i) (1.0 - Math.random() * noiseLevel) + gaussianTwo.yAtX(i) (1.0 -
				 * Math.random() * noiseLevel));
				 */
				data[i + k * mcaChannels] = (long) (gaussianOne.yAtX(i) * (1.0 - Math.random() * noiseLevel) + gaussianTwo
						.yAtX(i)
						* (1.0 - Math.random() * noiseLevel));
			}
		}

		return data;
	}

	private long[] createDummyScalerData(@SuppressWarnings("unused") int detector) {
		Random generator = new Random();

		long[] data = new long[Xspress2Utilities.scalerFields];
		// long[] fluData;

		for (int i = 0; i < numberOfDetectors; i++) {
			// Create to dummy data.
			// fluData = createDummyData(i);

			// Add up the elements for the total number of counts.
			// for (int j = 0; j < fluData.length; j++)
			// {
			// data[0] += fluData[j];
			// }

			data[0] = generator.nextInt(1000000);

			// Don't know what a suitable value would be for the number of
			// resets,
			// so just use a random value for now.
			data[1] = generator.nextInt();

			// For now just set the windowed counts to be the same as the total
			// counts.
			data[2] = generator.nextInt(500000);
		}

		return data;
	}

	/**
	 * Gets the multi-channel data for a detector element.
	 * 
	 * @param detector
	 *            the detector
	 * @param startGrade
	 *            the starting grade
	 * @param endGrade
	 *            the ending grade
	 * @param time
	 *            the time to count for (milliseconds)
	 * @return array[4096] of double values representing the counts in each channel summed between startgrade and
	 *         endGrade
	 * @throws DeviceException
	 */
	@Override
	public Object getMCData(int detector, int startGrade, int endGrade, int time) throws DeviceException {
		boolean locked = false;
		String filename = "notcreated";
		try
		{
			
			locked = this.tryLock(5, TimeUnit.SECONDS);
			if(!locked)
				throw new DeviceException("Error in detector getMCData , Xspress is already locked ");
		
		// long[] data = new long[mcaChannels * mcaGrades];
		// double[] values = new double[mcaChannels * mcaGrades];
		stop();
		clear();
		start();
		// Time in msec
		tfg.countAsync(time);
		do {
			synchronized (this) {
				try {
					wait(100);
				} catch (InterruptedException e) {
				}
			}
		} while (tfg.getStatus() == Timer.ACTIVE);

		stop();

		if (isDummy()) {
			filename = writeDummyFile();
		} else if (mcaHandle >= 0 && daServer != null && daServer.isConnected()) {
			try {

				File f = File.createTempFile("mca", "." + fileExtension, new File(xspressSpoolDirectory));

				filename = f.getCanonicalPath();

				// Send the command to DAserver to write a file.
				daServer.sendCommand("read 0 0 0 " + mcaGrades * mcaChannels + " " + numberOfDetectors + " 1 from "
						+ mcaHandle + " to-local-file \"" + filename + "\" raw");

			} catch (IOException e) {
				throw new DeviceException(e.getMessage());
			}
		}
		} catch (InterruptedException e) {
			logger.error("Error in locking the Xspress for data collection", e);
		}
		finally{
			if(locked)
			{
				this.unlock();
			}
			
		}

		// The dummy data file always contains all detectors so need to specify
		// which one we want, the real file has been written with only one
		// detector in it so we specify 0..
		/*
		 * data = Xspress2Utilities.interpretDataFile(filename, 0, isDummy() ? detector : 0); for (int j = 0; j < 65536;
		 * j++) { values[j] = data[j]; } return values;
		 */

		return filename;
	}

	/**
	 * Set attribute values for "User", "Password", "Host", "TotalFrames", "Endian".
	 * 
	 * @param attributeName
	 *            the attribute name
	 * @param value
	 *            the attribute value
	 */
	@Override
	public void setAttribute(String attributeName, Object value) {
		if (attributeName.equals(USER)) {
		} else if (attributeName.equals(PASSWORD)) {
		} else if (attributeName.equals(HOST)) {
		} else if (attributeName.equals(REMOTEENDIAN)) {
		}
	}

	/**
	 * Opens the connections to daServer.
	 * @throws DeviceException 
	 */
	private void open() throws DeviceException {
		Object obj;
		Integer reply;

		if (daServer != null && daServer.isConnected()) {
			// with the new firmware upgrade new command is avialable to set the
			// resgrade and format which has to be run before opening the mca and scalers
			// paths. USing the startupScript variable in this version for getting this command from the
			// xml, in later versions this will be changed to a proper format command available from the
			// xml - 5-11-08

			if (startupScript != null) {
				reply = (Integer) daServer.sendCommand(startupScript);
				logger.debug("Xspress2System format-run - reply  was: " + reply);
			}

			if (mcaOpenCommand != null) {
				if ((obj = daServer.sendCommand(mcaOpenCommand)) != null) {
					mcaHandle = ((Integer) obj).intValue();
					logger.info("Xspress2System: open() using mcaHandle " + mcaHandle);
					//System.out.println("Xspress2System: open() using mcaHandle " + mcaHandle);
				}
			}

			if (scalerOpenCommand != null) {
				if ((obj = daServer.sendCommand(scalerOpenCommand)) != null) {
					scalerHandle = ((Integer) obj).intValue();
					logger.info("Xspress2System: open() using scalerHandle " + scalerHandle);
					// System.out.println("Xspress2System: open() using scalerHandle "
					// + scalerHandle);
				}
			}
			// With the firmware upgrade there is no need to have a startup script
			// as all the setup is done in the config script stored in the daserver
			// 5-11-08
			/*
			 * if (startupScript != null) { reply = (Integer) daServer.sendCommand(startupScript);
			 * logger.debug("Xspress2System setup - reply was: " + reply); }
			 */
		}
	}

	/**
	 * Reads the detector windows, gains etc from file.
	 * 
	 * @param filename
	 *            detector window setup filename
	 * @return result string
	 */
	@Override
	public String loadAndInitializeDetectors(String filename) {
		String result = "Not done yet";

		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
			for (int i = 0; i < numberOfDetectors; i++) {
				// Create a Detector object from a line in the file and
				// add it to the list of Detectors. Note that unlike in
				// XspressSystem it is not necessary to set the window of the
				// real
				// detector - all windowing and manipulation of gains and
				// offsets
				// is done within this class.
				detectorList.add(new Detector(bufferedReader.readLine()));
			}
			result = "Successfully read file " + filename;
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
		// It is not entirely clear yet how this should work
		return null;
	}

	/**
	 * Reads all the detectors.
	 * 
	 * @return an array of DetectorReadings one for each detector
	 * @throws DeviceException 
	 */
	@Override
	public DetectorReading[] readDetectors() throws DeviceException {
		int windowed;
		int resets;
		int acc;
		int total;
		boolean lock =false;
		
		
		DetectorReading[] detectorReadings = new DetectorReading[numberOfDetectors];
		try {
			lock = this.tryLock(5, TimeUnit.SECONDS);
		
		// Get the scaler data from XSPRESS 2
		// long[] data =
		// daServer.getLongBinaryData("read 0 0 0 " + numberOfDetectors + " "
		// + Xspress2Utilities.scalerFields + " 1 from " + scalerHandle
		// + "raw intel", numberOfDetectors);
		//      
		// long[][] data = (long[][]) readoutScalers();
		if(!lock)
		{
			throw new DeviceException("Error reading detectors, Xspress is already locked");
		}
		if (readDetectorsUsingScript) {
			try {
				JythonServerFacade jsf = JythonServerFacade.getInstance();
				String totalsStr = "";
				String resetsStr = "";
				String windowedStr = "";
				// logger.info("Sending command " +
				// "getDetectorMonitorCounts()");
				String result = jsf.evaluateCommand("getDetectorMonitorCounts()");
				logger.info("RESULT IS " + result);
				String tmp = result.replace("[", "\t");
				tmp = tmp.replace("]", "\t");
				StringTokenizer stk = new StringTokenizer(tmp, "\t");
				int tokens = stk.countTokens();

				for (int i = 0; i < tokens; i++) {
					String s = stk.nextToken();
					if (s.trim().equals(",") || s.equals(""))
						continue;

					if (totalsStr.equals(""))
						totalsStr = s;
					else if (resetsStr.equals(""))
						resetsStr = s;
					else
						windowedStr = s;

				}
				// System.out.println("The RESULT is " + result);
				StringTokenizer totalsStk = new StringTokenizer(totalsStr, ",");
				StringTokenizer resetsStk = new StringTokenizer(resetsStr, ",");
				StringTokenizer windowedStk = new StringTokenizer(windowedStr, ",");

				// Now lets create a DetectorReading object for each element.
				for (int i = 0; i < numberOfDetectors; i++) {
					// total = (int) data[i][0];
					total = Integer.valueOf(totalsStk.nextToken().trim());

					// logger.info("totals["+i+"]="+total);

					// resets = (int) data[i][1];
					resets = Integer.valueOf(resetsStk.nextToken().trim());
					acc = 0; // dunno what this is!
					// windowed = (int) data[i][2];
					windowed = Integer.valueOf(windowedStk.nextToken().trim());
					 //System.out.println("The readings from the detector are "
					// +this.getDetector(i) + " " + total +" " + resets + " " +
					// windowed );
					detectorReadings[i] = new DetectorReading(this.getDetector(i), total, resets, acc, windowed);
				}
			} catch (DeviceException e) {
				logger.error("Xspress2System.readDetectors(): Cannot read from XSPRESS2.");
			
			}
		} else {
			try {
				long[] totalsData = readoutTotalCountsScaler();
				long[] resetsData = readoutResetsScaler();
				long[] windowsData = readoutWindowsCountsScaler();

				// Now lets create a DetectorReading object for each element.
				for (int i = 0; i < numberOfDetectors; i++) {
					// total = (int) data[i][0];
					total = (int) totalsData[i];

					// logger.info("totals["+i+"]="+total);

					// resets = (int) data[i][1];
					resets = (int) resetsData[i];
					acc = 0; // dunno what this is!
					// windowed = (int) data[i][2];
					windowed = (int) windowsData[i];

					detectorReadings[i] = new DetectorReading(this.getDetector(i), total, resets, acc, windowed);
				}
			} catch (Exception e) {
				throw new DeviceException(e.getMessage(), e);
			}
		}
		} catch (InterruptedException e1) {
			logger.error("Unable to lock detector for reading " , e1);
			return null;
		}
		finally{
			if(lock)
			{
				this.unlock();
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
	@SuppressWarnings("unused")
	private DetectorReading[] readDetectors(int frame) {
		// It is not entirely clear yet how this should work
		return new DetectorReading[numberOfDetectors];
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
		detectorList.get(detector).setWindow(lower, upper);
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
		detectorList.get(detector).setGain(gain);
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
		detectorList.get(detector).setOffset(offset);
	}

	@Override
	public Detector getDetector(int which) throws DeviceException {
		return detectorList.get(which);
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

	/**
	 * Writes a dummy data file - the data is written in the same format as is used by the real daserver.
	 * 
	 * @return the name of the file
	 */
	public String writeDummyFile() {
		long[] data;
		String filename = "notcreated";
		File f;
		byte[] bytes = new byte[mcaGrades * mcaChannels * 4];
		try {
			f = File
					.createTempFile(constructFilePrefix("mca"), "." + fileExtension, new File(constructDataDirectory()));
			FileOutputStream fos = new FileOutputStream(f);
			DataOutputStream dos = new DataOutputStream(fos);

			for (int j = 0; j < numberOfDetectors; j++) {
				data = createDummyData(j);
				for (int l = 0; l < (mcaGrades * mcaChannels * 4); l += 4) {
					bytes[l + 0] = (byte) (data[l / 4] >>> 24);
					bytes[l + 1] = (byte) (data[l / 4] >>> 16);
					bytes[l + 2] = (byte) (data[l / 4] >>> 8);
					bytes[l + 3] = (byte) (data[l / 4]);
				}
				dos.write(bytes);
			}
			dos.close();
			filename = f.getCanonicalPath();
		} catch (IOException e) {
			logger.error("writeDummyData: IOException while writing file " + filename);
		}

		return filename;
	}

	/**
	 * Writes a dummy scaler data file - the data is written in the same format as is used by the real daserver.
	 * 
	 * @return the name of the file
	 */
	public String writeDummyScalerFile() {
		long[] data;
		String filename = "notcreated";
		File f;
		byte[] bytes = new byte[Xspress2Utilities.scalerFields * 4];
		try {
			f = File
					.createTempFile(constructFilePrefix("sca"), "." + fileExtension, new File(constructDataDirectory()));
			FileOutputStream fos = new FileOutputStream(f);
			DataOutputStream dos = new DataOutputStream(fos);

			for (int j = 0; j < numberOfDetectors; j++) {
				data = createDummyScalerData(j);

				for (int l = 0; l < (Xspress2Utilities.scalerFields * 4); l += 4) {
					bytes[l + 0] = (byte) (data[l / 4] >>> 24);
					bytes[l + 1] = (byte) (data[l / 4] >>> 16);
					bytes[l + 2] = (byte) (data[l / 4] >>> 8);
					bytes[l + 3] = (byte) (data[l / 4]);
				}
				dos.write(bytes);
			}
			dos.close();
			filename = f.getCanonicalPath();
		} catch (IOException e) {
			logger.error("writeDummyData: IOException while writing file " + filename);
		}

		return filename;
	}

	private String constructDataDirectory() throws IOException {
		String xspressDir = null;

		// Is there a scan running ?
		if (JythonServerFacade.getInstance().getScanStatus() == Jython.RUNNING) {
			String dataDir = PathConstructor.createFromDefaultProperty();
			File directory = new File(dataDir + "/" + runNumber.getCurrentFileNumber());
			if (directory.exists()) {
				// Now check that it is a directory
				if (!directory.isDirectory()) {
					logger.error("Cannot create scan directory " + runNumber.getCurrentFileNumber()
							+ " because a file of the same name already exists.");
				}

			} else {
				// Create it.
				logger.debug("Trying to create directory : " + directory.getCanonicalPath());
				directory.mkdir();
			}

			xspressDir = directory.getCanonicalPath();

		} else {
			// No scan running, then use the spool directory.
			logger.debug(LoggingConstants.FINEST, "No Scan Running - writing xspress files to spool directory");
			xspressDir = xspressSpoolDirectory;
		}

		return xspressDir;
	}

	private String constructFilePrefix(String customPrefix) {
		String prefix = "";

		if (customPrefix == null) {
			customPrefix = "mca";
		}

		// If the detector is a dummy then add a prefix to filename.
		if (isDummy()) {
			prefix = "dummy-" + prefix;
		}

		// Is there a scan running ?
		if (JythonServerFacade.getInstance().getScanStatus() == Jython.RUNNING) {
			// Use prefix <beamline>-<scannumber>
			prefix = prefix + beamlineName + "-" + runNumber.getCurrentFileNumber() + "-";
		} else {
			prefix = prefix + customPrefix + "-";
		}

		return prefix;
	}

	/**
	 * @return total counts
	 * @throws Exception 
	 */
	public long[] readoutTotalCountsScaler() throws Exception {
		long[] data = new long[numberOfDetectors];
		if (isDummy()) {
			for (int i = 0; i < numberOfDetectors; i++) {
				data[i] = createDummyScalerData(i)[0];
			}
		} else {

			/*
			 * data = daServer.getLongBinaryData("read 0 0 0 " + " " + numberOfDetectors + " 1 1 from " + scalerHandle + "
			 * raw", numberOfDetectors);
			 */
			/*data = daServer.getLongBinaryData("read 0 0 0 " + " 1" + numberOfDetectors + " 1 from " + scalerHandle
					+ " raw", numberOfDetectors);*/
			data = daServer.getLongBinaryData("read 0 0 0 " + " 1 " + numberOfDetectors + " 1 from " + scalerHandle
					, numberOfDetectors);
			// for (int i=0; i<numberOfDetectors;i++)
			// {
			// logger.info("READOUT:Total("+i+")="+data[i]);
			// }
		}

		return data;
	}

	/**
	 * @return number of resets
	 * @throws Exception 
	 */
	public long[] readoutResetsScaler() throws Exception {
		long[] data = new long[numberOfDetectors];
		if (isDummy()) {
			for (int i = 0; i < numberOfDetectors; i++) {
				data[i] = createDummyScalerData(i)[1];
			}
		} else {

			/*
			 * data = daServer.getLongBinaryData("read 0 1 0 " + " " + numberOfDetectors + " 1 1 from " + scalerHandle + "
			 * raw", numberOfDetectors);
			 */

			data = daServer.getLongBinaryData("read 1 0 0 " + " 1 " + numberOfDetectors + " 1 from " + scalerHandle
					, numberOfDetectors);
			/*data = (long[])daServer.getData("read 1 0 0 " + " 1 " + numberOfDetectors + " 1 from " + scalerHandle
					, numberOfDetectors);*/
		}

		return data;
	}

	/**
	 * @return windowed counts from scaler
	 * @throws Exception 
	 */
	public long[] readoutWindowsCountsScaler() throws Exception {
		long[] data = new long[numberOfDetectors];
		if (isDummy()) {
			for (int i = 0; i < numberOfDetectors; i++) {
				data[i] = createDummyScalerData(i)[2];
			}
		} else {

			/*
			 * data = daServer.getLongBinaryData("read 0 2 0 " + " " + numberOfDetectors + " 1 1 from " + scalerHandle + "
			 * raw", numberOfDetectors);
			 */
			data = daServer.getLongBinaryData("read 2 0 0 " + " 1 " + numberOfDetectors + " 1 from " + scalerHandle
					, numberOfDetectors);
		}

		return data;
	}

	/**
	 * @return scaler data
	 * @throws Exception 
	 */
	public Object readoutScalers() throws Exception {
		Object value = null;
		String filename = "notcreated";
		// int numberOfFrames = 1;
		int numberOfScalers = Xspress2Utilities.scalerFields;

		// try
		// {
		if (isDummy()) {
			filename = writeDummyScalerFile();
			value = Xspress2Utilities.interpretScalerFile(filename, 0);
		} else if (scalerHandle >= 0 && daServer != null && daServer.isConnected()) {
			// File f =
			// File.createTempFile(this.constructFilePrefix("sca"), "."
			// + fileExtension, new File(this.constructDataDirectory()));
			// filename = f.getCanonicalPath();
			//
			// daServer.sendCommand("read 0 0 0 " + numberOfDetectors + " "
			// + numberOfScalers + " " + numberOfFrames + " from "
			// + scalerHandle + " to-local-file \"" + filename + "\" raw");
			//
			// logger.info("Written Scalers file (" + numberOfDetectors + ","
			// + numberOfScalers + "," + numberOfFrames + ") : " + filename);

			/*
			 * value = daServer.getLongBinaryData("read 0 0 0 " + " " + numberOfDetectors + " " + numberOfScalers + " 1
			 * from " + scalerHandle + " raw intel", numberOfDetectors numberOfScalers);
			 */
			value = daServer.getLongBinaryData("read 0 0 0 " + " " + numberOfScalers + " " + numberOfDetectors
					+ " 1 from " + scalerHandle + " raw intel", numberOfDetectors * numberOfScalers);

			// data = daServer.getLongBinaryData("read 0 0 0", ndata);

		}
		// }
		// catch (IOException e)
		// {
		// throw new DeviceException(e.getMessage());
		// }

		return value;
	}

	/**
	 * Sends the commands to daServer to write the data to a file. If readoutMode is set to READOUT_FILE returns the
	 * file name. If readoutMode is set to READOUT_WINDOWED windows the data in the file and returns the windowed
	 * values.
	 * 
	 * @return the name of the data file OR the windowed data
	 * @throws DeviceException
	 */
	@Override
	public Object readout() throws DeviceException {
		Object value = null;

		String filename = "notcreated";

		int numberOfFrames = 1;
		try {
			// Write the data file - either dummy data or a request to daServer.
			// Note that we are using numberOfFrames = 1 here which implies that
			// this readout method will only work for step scans.
			if (isDummy()) {
				filename = writeDummyFile();
			} else if (mcaHandle >= 0 && daServer != null && daServer.isConnected()) {
				stop();

				File f = File.createTempFile(this.constructFilePrefix("mca"), "." + fileExtension, new File(this
						.constructDataDirectory()));
				filename = f.getCanonicalPath();
				daServer.sendCommand("read 0 0 0 " + mcaGrades * mcaChannels + " " + numberOfDetectors + " "
						+ numberOfFrames + " from " + mcaHandle + " to-local-file \"" + filename + "\" raw");
			}
		} catch (IOException e) {
			throw new DeviceException(e.getMessage());
		}

		// In READOUT_FILE mode we just return the filename, in READOUT_WINDOWED
		// mode the data has to be extracted and windowed for each detector.
		if (readoutMode == READOUT_FILE) {
			value = filename;
		} else {
			value = sumGradesAndDoWindowing(filename, 0);
		}
		return value;
	}

	/**
	 * @param filename
	 * @param frameNumber
	 * @return the summed and windowed data
	 * @throws DeviceException
	 */
	private double[] sumGradesAndDoWindowing(String filename, int frameNumber) throws DeviceException {
		long[] data;
		double[] windowedTotals = new double[numberOfDetectors];
		for (int k = 0; k < numberOfDetectors; k++) {
			data = Xspress2Utilities.interpretDataFile(filename, frameNumber, k);
			windowedTotals[k] = getDetector(k).windowData(
					Xspress2Utilities.sumGrades(data, 0, Xspress2Utilities.mcaGrades - 1));
		}

		return windowedTotals;
	}

	/**
	 * @param startChannel
	 * @param channelCount
	 * @param frame
	 * @return filename
	 * @throws DeviceException 
	 */
	public String readFrameFile(@SuppressWarnings("unused") int startChannel, @SuppressWarnings("unused") int channelCount, int frame) throws DeviceException {
		// Object value = null;

		String filename = "notcreated";

		int numberOfFrames = 1;
		try {
			// Write the data file - either dummy data or a request to daServer.
			// Note that we are using numberOfFrames = 1 here which implies that
			// this readout method will only work for step scans.
			if (isDummy()) {
				filename = writeDummyFile();
			} else if (mcaHandle >= 0 && daServer != null && daServer.isConnected()) {
				stop();
				File f = File.createTempFile(constructFilePrefix("mca"), "." + fileExtension, new File(
						xspressSpoolDirectory));
				filename = f.getCanonicalPath();
				daServer.sendCommand("read 0 0 " + frame + " " + mcaGrades * mcaChannels + " " + numberOfDetectors
						+ " " + numberOfFrames + " from " + mcaHandle + " to-local-file \"" + filename + "\" raw");
			}
		} catch (IOException e) {
			// DO SOMETHING HERE
		}

		return filename;
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
		Object value = null;

		String filename = "notcreated";
		String command = "";

		int numberOfFrames = 1;
		try {
			// Write the data file - either dummy data or a request to daServer.
			// Note that we are using numberOfFrames = 1 here which implies that
			// this readout method will only work for step scans.
			if (isDummy()) {
				filename = writeDummyFile();
			} else if (mcaHandle >= 0 && daServer != null && daServer.isConnected()) {
				stop();
				File f = File.createTempFile("mca", "dat", new File(xspressSpoolDirectory));
				filename = f.getCanonicalPath();
				command = "read 0 0 " + frame + " " + mcaGrades * mcaChannels + " " + numberOfDetectors + " "
						+ numberOfFrames + " from " + mcaHandle + " to-local-file \"" + filename + "\" raw";
				logger.debug("XSP2-readFrame sent: " + command);
				daServer.sendCommand(command);
			}
			value = sumGradesAndDoWindowing(filename, 0);
		} catch (IOException e) {
			// DO SOMETHING HERE
		} catch (DeviceException e) {
			// AND HERE
		}

		return (double[]) value;
	}

	/**
	 * Gets the readoutMode.
	 * 
	 * @return READOUT_FILE or READOUT_WINDOWED
	 */
	public int getReadoutMode() {
		return readoutMode;
	}

	/**
	 * Sets the readoutMode to READOUT_FILE or READOUT_WINDOWED.
	 * 
	 * @param readoutMode
	 *            the new mode to set
	 */
	@Override
	public void setReadoutMode(int readoutMode) {
		this.readoutMode = readoutMode;
	}

	/**
	 * Gets tfgName.
	 * 
	 * @return tfgName
	 */
	public String getTfgName() {
		return tfgName;
	}

	/**
	 * Sets tfgName which is the name of the time frame generator object.
	 * 
	 * @param tfgName
	 */
	public void setTfgName(String tfgName) {
		this.tfgName = tfgName;
	}

	@Override
	public void reconfigure() throws FactoryException {
		try {
			// A real system needs a connection to a real da.server via a DAServer
			// object.
			if (!isDummy()) {
				logger.debug("Xspress2System.reconfigure(): reconnecting to: " + daServerName);
				daServer.reconnect();
			}
			// does not reconfigure the tfg -- need to check if it is needed

			// If everything has been found send the open commands.
			if (tfg != null && (daServer != null || isDummy())) {
				open();
			}
		} catch (DeviceException e) {
			throw new FactoryException(e.getMessage(),e);
		}

	}
	
	/**
	 * @param timeout
	 * @param unit 
	 * @return boolean true if locked
	 * @throws InterruptedException 
	 */
	@Override
	public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException
	{
		boolean lock = false;
		try{
			lock = deviceLock.tryLock(timeout, unit);
		
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return lock;
	}
	/**
	 * 
	 */
	@Override
	public void unlock()
	{
		try{
			deviceLock.unlock();			
		} catch (IllegalMonitorStateException e) {
			
		}
	}
}
