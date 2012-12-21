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

package gda.device.detector.xspress;

import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.Timer;
import gda.device.detector.DAServer;
import gda.device.detector.DetectorBase;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NexusDetector;
import gda.factory.Configurable;
import gda.factory.FactoryException;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.nexusformat.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.xspress.DetectorDeadTimeElement;
import uk.ac.gda.beans.xspress.DetectorElement;
import uk.ac.gda.beans.xspress.XspressDeadTimeParameters;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.beans.xspress.XspressROI;
import uk.ac.gda.util.beans.xml.XMLHelpers;

/**
 * Represents a set of Xspress1 boards and detectors. Actually communicates with an DAServer object.
 */
public class Xspress1System extends DetectorBase implements NexusDetector, XspressDetector, Scannable, Configurable {
	
	private static final Logger logger = LoggerFactory.getLogger(Xspress1System.class);
	public static final int READOUT_FILE = 0;
	public static final int READOUT_WINDOWED = 1;

	private Boolean onlyDisplayFF = false;
	public static final String ONLY_DISPLAY_FF_ATTR = "ff_only";

//	private static final int mcaChannels = 4096;
//	private DetectorList detectorList;

	private DAServer daServer;
	private Timer timer = null;
	private String startupScript = null;
	private String mcaOpenCommand = null;
	private String scalerOpenCommand = null;
	private String xspressSystemName = null;
	private int numberOfDetectors = -1;
	protected final int numberOfScalers = 4;
	private int mcaHandle = -1;
	private int scalerHandle = -1;

	// Full path to config file
	private String configFileName = null;
	private String dtcConfigFileName;

	
	private Double deadtimeEnergy = null;
	private int framesRead = 0;
	// hold the parameters for this object
	private XspressParameters xspressParameters;
	private XspressDeadTimeParameters xspressDeadTimeParameters;
	private Integer maxNumberOfFrames = 0; // the number of frames which TFG has space for, based on the current config in TFG
	private Boolean addDTScalerValuesToAscii = false;


	public Xspress1System() {
		this.inputNames = new String[] {};
	}

	@Override
	public void configure() throws FactoryException {
		if (daServer == null) {
			logger.error("Xspress1System.configure(): DaServer not found");
			throw new FactoryException("Xspress1System.configure(): DaServer not found");
		}
		if (timer == null) {
			logger.error("Xspress1System.configure(): Time Frame Generator not found");
			throw new FactoryException("Xspress1System.configure(): Time Frame Generator not found");
		}

		try {
			loadAndInitializeDetectors(configFileName,dtcConfigFileName);
		} catch (Exception e) {
			logger.error(
					"Error loading template XML. Will use a detector with a few default elements instead. "
							+ e.getMessage(), e);
			ArrayList<XspressROI> regions = new ArrayList<XspressROI>();
			xspressParameters = new XspressParameters();
			xspressParameters.setReadoutMode(READOUT_SCALERONLY);
			xspressParameters.setResGrade(ResGrades.NONE);
			xspressDeadTimeParameters = new XspressDeadTimeParameters();
			xspressParameters.addDetectorElement(new DetectorElement("Element0", 0, 0, 4000, 
					false, regions));
			xspressParameters.addDetectorElement(new DetectorElement("Element1", 1, 85, 2047, 
					false, regions));
			xspressParameters.addDetectorElement(new DetectorElement("Element2", 2, 34, 2439, 
					false, regions));
			xspressParameters.addDetectorElement(new DetectorElement("Element3", 3, 31, 2126, 
					false, regions));
			xspressParameters.addDetectorElement(new DetectorElement("Element4", 4, 0, 4000, true,
					regions));
			xspressParameters.addDetectorElement(new DetectorElement("Element5", 5, 85, 2047,
					false, regions));
			xspressParameters.addDetectorElement(new DetectorElement("Element6", 6, 34, 2439, 
					false, regions));
			xspressParameters.addDetectorElement(new DetectorElement("Element7", 7, 31, 2126, 
					false, regions));
			xspressParameters.addDetectorElement(new DetectorElement("Element8", 8, 31, 2126, 
					false, regions));
			xspressDeadTimeParameters.addDetectorDeadTimeElement(new DetectorDeadTimeElement("Elemen0", 0,3.4E-7, 0.0, 3.4E-7));
			xspressDeadTimeParameters.addDetectorDeadTimeElement(new DetectorDeadTimeElement("Elemen1", 1,3.8E-7, 0.0, 3.8E-7));
			xspressDeadTimeParameters.addDetectorDeadTimeElement(new DetectorDeadTimeElement("Elemen2", 2,3.7E-7, 0.0, 3.7E-7));
			xspressDeadTimeParameters.addDetectorDeadTimeElement(new DetectorDeadTimeElement("Elemen3", 3,3.0E-7, 0.0, 3.0E-7));
			xspressDeadTimeParameters.addDetectorDeadTimeElement(new DetectorDeadTimeElement("Elemen4", 4,3.4E-7, 0.0, 3.4E-7));
			xspressDeadTimeParameters.addDetectorDeadTimeElement(new DetectorDeadTimeElement("Elemen5", 5,3.5E-7, 0.0, 3.5E-7));
			xspressDeadTimeParameters.addDetectorDeadTimeElement(new DetectorDeadTimeElement("Elemen6", 6,3.3E-7, 0.0, 3.3E-7));
			xspressDeadTimeParameters.addDetectorDeadTimeElement(new DetectorDeadTimeElement("Elemen7", 7,3.0E-7, 0.0, 3.0E-7));
			xspressDeadTimeParameters.addDetectorDeadTimeElement(new DetectorDeadTimeElement("Elemen8", 8,3.3E-7, 0.0, 3.3E-7));
			numberOfDetectors = xspressParameters.getDetectorList().size();
		}
		// If everything has been found send the format, region of interest, windows & open commands.
		if (timer != null && (daServer != null)) {
			try {
				close();
				doStartupScript();
				configureDetectorFromParameters();
				open();
			} catch (DeviceException e) {
				throw new FactoryException(e.getMessage(), e);
			}
		}
		configured = true;
	}

	public String getConfigFileName() {
		return configFileName;
	}

	public void setConfigFileName(String configFileName) {
		this.configFileName = configFileName;
	}

	public String getDtcConfigFileName() {
		return dtcConfigFileName;
	}

	public void setDtcConfigFileName(String dtcConfigFileName) {
		this.dtcConfigFileName = dtcConfigFileName;
	}

	public DAServer getDaServer() {
		return daServer;
	}

	public void setDaServer(DAServer daServer) {
		this.daServer = daServer;
	}

	public Timer getTimer() {
		return timer;
	}

	public void setTimer(Timer timer) {
		this.timer = timer;
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

	public String getStartupScript() {
		return startupScript;
	}

	public void setStartupScript(String startupScript) {
		this.startupScript = startupScript;
	}

	public void setMcaOpenCommand(String mcaOpenCommand) {
		this.mcaOpenCommand = mcaOpenCommand;
	}

	public String getMcaOpenCommand() {
		return mcaOpenCommand;
	}

	public void setScalerOpenCommand(String scalerOpenCommand) {
		this.scalerOpenCommand = scalerOpenCommand;
	}

	public String getScalerOpenCommand() {
		return scalerOpenCommand;
	}

	public void setNumberOfDetectors(int numberOfDetectors) {
		this.numberOfDetectors = numberOfDetectors;
	}

	@Override
	public int getNumberOfDetectors() {
		return numberOfDetectors;
	}

	/**
	 * @return the maximum number of time frames possible based on the result of the last format-run command. This will
	 *         be 0 when using DummyDaServer.
	 */
	public int getMaxNumberOfFrames() {
		return maxNumberOfFrames;
	}

	public void setMaxNumberOfFrames(int maxNumberOfFrames) {
		this.maxNumberOfFrames = maxNumberOfFrames;
	}

	public Boolean getAddDTScalerValuesToAscii() {
		return addDTScalerValuesToAscii;
	}

	public void setAddDTScalerValuesToAscii(Boolean addDTScalerValuesToAscii) {
		this.addDTScalerValuesToAscii = addDTScalerValuesToAscii;
	}

	/**
	 * @param which
	 * @return true if detectorElement is excluded.
	 */
	public boolean isDetectorExcluded(int which) {
		return xspressParameters.getDetector(which).isExcluded();
	}

	public void setDetectorExcluded(int which, boolean excluded) {
		xspressParameters.getDetector(which).setExcluded(excluded);
	}

	public List<DetectorElement> getDetectorList() {
		return xspressParameters.getDetectorList();
	}

	private void configureDetectorFromParameters() throws DeviceException {
		for (DetectorElement detector : xspressParameters.getDetectorList()) {
			doSetWindowsCommand(detector);
		}
	}

	private void doSetWindowsCommand(DetectorElement detector) throws DeviceException {
		Object obj = null;
		String cmd = "xspress set-windows '" + xspressSystemName + "' " + detector.getNumber() + " " + detector.getWindowStart() + " " + detector.getWindowEnd();
		if (daServer != null && daServer.isConnected()) {
			obj = daServer.sendCommand(cmd);
			if (((Integer) obj).intValue() < 0) {
				throw new DeviceException("Xspress1System error setting windows: ");
			}
		}
	}

	private void open() throws DeviceException {
		Object obj;
		if (daServer != null && daServer.isConnected()) {
			if (mcaOpenCommand != null) {
				if ((obj = daServer.sendCommand(mcaOpenCommand)) != null) {
					mcaHandle = ((Integer) obj).intValue();
					if (mcaHandle < 0) {
						throw new DeviceException("Failed to create the mca handle");
					}
					logger.info("Xspress1System: open() using mcaHandle " + mcaHandle);
				}
			}

			if (scalerOpenCommand != null) {
				if ((obj = daServer.sendCommand(scalerOpenCommand)) != null) {
					scalerHandle = ((Integer) obj).intValue();
					if (scalerHandle < 0) {
						throw new DeviceException("Failed to create the scaler handle");
					}
					logger.info("Xspress1System: open() using scalerHandle " + scalerHandle);
				}
			}
		}
	}

	/**
	 * Reads the detector windows, gains etc from file.
	 * 
	 * @param filename
	 *            detector window setup filename
	 * @throws Exception
	 */
	@Override
	public void loadAndInitializeDetectors(String filename) throws Exception {
		xspressParameters = (XspressParameters) XMLHelpers.createFromXML(XspressParameters.mappingURL,
				XspressParameters.class, XspressParameters.schemaURL, filename);
		if (xspressParameters != null) {
			logger.debug("loaded parameters for " + xspressParameters.getDetectorName());
			numberOfDetectors = xspressParameters.getDetectorList().size();
		}
	}

	public void loadAndInitializeDetectors(String filename, String dtcConfigFileName) throws Exception {
		loadAndInitializeDetectors(filename);
		xspressDeadTimeParameters = (XspressDeadTimeParameters)XMLHelpers.createFromXML(DetectorDeadTimeElement.mappingURL,
				DetectorDeadTimeElement.class, DetectorDeadTimeElement.schemaURL, dtcConfigFileName);
	}
	/**
	 * Set the hardware scaler window
	 * 
	 * @param detector
	 * @param windowStart
	 * @param windowEnd
	 * @throws DeviceException
	 */
	public void setWindows(int detector, int windowStart, int windowEnd) throws DeviceException {
		DetectorElement detectorElement = xspressParameters.getDetector(detector);
		detectorElement.setWindow(windowStart, windowEnd);
		if (configured) {
			doSetWindowsCommand(detectorElement);
		}
	}

	/**
	 * Saves the detector windows, gains etc to file
	 * 
	 * @param filename
	 *            the filename to write detector setup in.
	 */
	@Override
	public void saveDetectors(String filename) {
		try {
			XMLHelpers.writeToXML(XspressParameters.mappingURL, xspressParameters, filename);
		} catch (Exception e) {
			logger.error("Exception in saveDetectors: " + e.getMessage());
		}
	}

	@Override
	public DetectorElement getDetector(int which) throws DeviceException {
		return xspressParameters.getDetector(which);
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
		xspressParameters.getDetector(detector).setWindow(lower, upper);
	}

	@Override
	public void collectData() throws DeviceException {
		// if timer not running with frames then clear and start the xspress memory
		if (timer.getAttribute("TotalFrames").equals(0)) {
			clear();
			start();
		}
	}

	@Override
	public int getStatus() throws DeviceException {
		return timer.getStatus();
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		// this returns a Nexus Tree as it implements NexusDetector
		return null;
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "Xspress system version 1";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "Version 1";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "Xspress1System";
	}

	@Override
	public void stop() throws DeviceException {
		if (mcaHandle < 0 || scalerHandle < 0) {
			open();
		}
		if (mcaHandle >= 0 && daServer != null && daServer.isConnected()) {
			sendCommand("disable ", mcaHandle);
		}
		if (scalerHandle >= 0 && daServer != null && daServer.isConnected()) {
			sendCommand("disable ", scalerHandle);
		}
	}

	@Override
	public String[] getExtraNames() {
		return getChannelLabels().toArray(new String[0]);
	}

	@Override
	public void atScanStart() throws DeviceException {
		stop();
		clear();
		start();
		framesRead = 0;
	}

	@Override
	public void atScanLineStart() throws DeviceException {
		// if this class was used to define framesets, then memeory is only cleared at the start of the scan
		if (!timer.getAttribute("TotalFrames").equals(0)) {
			stop();
			clear();
			start();
		}
		framesRead = 0;
	}

	public boolean isOnlyDisplayFF() {
		return onlyDisplayFF;
	}

	public void setOnlyDisplayFF(boolean onlyDisplayFF) {
		this.onlyDisplayFF = onlyDisplayFF;
	}

	@Override
	public void setAttribute(String attributeName, Object value) throws DeviceException {
//		if (attribute.equals("readoutModeForCalibration") && value instanceof String[]) {
//
//			String readoutMode = ((String[]) value)[0];
//			String resGrade = ((String[]) value)[1];
//
//			// as before
//			setReadoutMode(readoutMode);
//
//			// like setResGrade but the numberOfBits MUST be 12
//			setResolutionGrade(resGrade, 12);
//		} else 
		if (attributeName.equals(ONLY_DISPLAY_FF_ATTR)) {
			Boolean ffonly = Boolean.parseBoolean(value.toString());
			this.onlyDisplayFF = ffonly;
		} else {
			super.setAttribute(attributeName, value);
		}
	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		if (attributeName.equals("liveStats")) {
//			return calculateLiveStats();
			return "Not implemented yet";
		} else if (attributeName.equals(ONLY_DISPLAY_FF_ATTR)) {
			return onlyDisplayFF;
		}
		return null;
	}

	@Override
	public void close() throws DeviceException {
		if (mcaHandle >= 0) {
			daServer.sendCommand("close " + mcaHandle);
			mcaHandle = -1;
		}
		if (scalerHandle >= 0) {
			daServer.sendCommand("close " + scalerHandle);
			scalerHandle = -1;
		}
	}

	@Override
	public void reconfigure() throws FactoryException {
		// A real system needs a connection to a real da.server via a DAServer object.
		logger.debug("Xspress1System.reconfigure(): reconnecting to: " + daServer.getName());
		try {
			daServer.reconnect();
		} catch (DeviceException e1) {
			logger.error("Could not reconnect to xspress", e1);
		}

		// does not reconfigure the timer -- need to check if it is needed
		// If everything has been found send the open commands.
		if (timer != null && (daServer != null)) {
			try {
				open();
			} catch (DeviceException e) {
				throw new FactoryException(e.getMessage(), e);
			}
		}
	}

	/**
	 * execute the startup script on da.server
	 * 
	 * @throws DeviceException
	 */
	private void doStartupScript() throws DeviceException {
		Object obj = null;
		if (daServer != null && daServer.isConnected()) {
			if (startupScript != null) {
				if ((obj = daServer.sendCommand(startupScript)) == null) {
					throw new DeviceException("Null reply received from daserver during " + startupScript);
				} else if (((Integer) obj).intValue() == -1) {
					throw new DeviceException(getName() + ": " + startupScript + " failed");
				}
			}
		}
	}

	@Override
	public void start() throws DeviceException {
		if (mcaHandle < 0 || scalerHandle < 0) {
			open();
		}
		if (mcaHandle >= 0 && daServer != null && daServer.isConnected()) {
			sendCommand("enable ", mcaHandle);
		}
		if (scalerHandle >= 0 && daServer != null && daServer.isConnected()) {
			sendCommand("enable ", scalerHandle);
		}
	}

	@Override
	public void clear() throws DeviceException {
		if (mcaHandle < 0 || scalerHandle < 0) {
			open();
		}
		if (mcaHandle >= 0) {
			sendCommand("clear ", mcaHandle);
		}
		if (scalerHandle >= 0) {
			sendCommand("clear ", scalerHandle);
		}
	}

	/**
	 * Returns a NexusTreeProvider object which the NexusDataWriter can unpack properly
	 */
	@Override
	public NexusTreeProvider readout() throws DeviceException {
		NexusTreeProvider out = readout(framesRead, framesRead)[0];
		if (!timer.getAttribute("TotalFrames").equals(0)) {
			framesRead++;
		}
		return out;
	}

	/**
	 * Returns a NexusTreeProvider object which the NexusDataWriter can unpack properly
	 */
	public NexusTreeProvider[] readout(int startFrame, int finalFrame) throws DeviceException {

		int numberOfFrames = finalFrame - startFrame + 1;
		NexusTreeProvider[] results = new NexusTreeProvider[numberOfFrames];

		int[] rawscalerData = readoutHardwareScalers(startFrame, numberOfFrames);
		int[][] unpackedScalerData = unpackRawScalerDataToFrames(rawscalerData, numberOfFrames);


		// scaler only mode
		if (xspressParameters.getReadoutMode().equals(XspressDetector.READOUT_SCALERONLY)) {
			double[][] scalerData = readoutScalerData(numberOfFrames, rawscalerData, true);
			for (int frame = 0; frame < numberOfFrames; frame++) {
				NXDetectorData thisFrame = new NXDetectorData(this);
				INexusTree detTree = thisFrame.getDetTree(getName());
				// do not use numberOfDetectors here so all information in the array is added to Nexus (i.e. FF)
				thisFrame.addData(detTree, "scalers", new int[] { numberOfDetectors }, NexusFile.NX_FLOAT64,
						ArrayUtils.subarray(scalerData[frame], 0, numberOfDetectors), "counts", 1);
				thisFrame = addExtraInformationToNexusTree(unpackedScalerData, scalerData, frame, thisFrame, detTree);
				results[frame] = thisFrame;

			}
		}
		return results;
	}

	protected NXDetectorData addExtraInformationToNexusTree(int[][] unpackedScalerData, double[][] scalerData,
			int frame, NXDetectorData thisFrame, INexusTree detTree) {
		thisFrame = addFFIfPossible(detTree, thisFrame, scalerData[frame]);
		thisFrame = fillNXDetectorDataWithScalerData(thisFrame, scalerData[frame], unpackedScalerData[frame]);
		thisFrame = addDTValuesToNXDetectorData(thisFrame, unpackedScalerData[frame]);
		return thisFrame;
	}

	private NXDetectorData addDTValuesToNXDetectorData(NXDetectorData thisFrame, int[] unpackedScalerData) {
		// always add raw scaler values to nexus data
		thisFrame.addData(thisFrame.getDetTree(getName()), "raw scaler values",
				new int[] { unpackedScalerData.length }, NexusFile.NX_INT32, unpackedScalerData, "counts", 1);

		return thisFrame;
	}

	/*
	 * Adds FF to the Nexus data where defined in the ascii data
	 */
	private NXDetectorData addFFIfPossible(INexusTree detTree, NXDetectorData thisFrame, double[] ds) {
		ArrayList<String> elementNames = new ArrayList<String>();
		getUnFilteredChannelLabels(elementNames);
		int ffColumn = elementNames.indexOf("FF");

		if (elementNames.size() == ds.length && ffColumn > -1) {
			thisFrame.addData(detTree, "FF", new int[] { 1 }, NexusFile.NX_FLOAT64, new double[] { ds[ffColumn] },
					"counts", 1);
		}
		return thisFrame;
	}

	/*
	 * Adds to the output the 'ascii' data which is the values which will be displayed in the Jython Terminal, plotting
	 * and ascii file.
	 */
	private NXDetectorData fillNXDetectorDataWithScalerData(NXDetectorData thisFrame, double[] scalerData,
			int[] rawScalervalues) {
		// only add FF
		if (onlyDisplayFF) {
			ArrayList<String> elementNames = new ArrayList<String>();
			getUnFilteredChannelLabels(elementNames);
			int ffColumn = elementNames.indexOf("FF");
			scalerData = new double[] { scalerData[ffColumn] };
			// names = new String[] { "FF" };
		}

		// add the raw scaler values used to calculate the deadtime to ascii
		if (addDTScalerValuesToAscii) {
			double[] dblRawScalerValues = new double[rawScalervalues.length];
			for (int i = 0; i < rawScalervalues.length; i++) {
				dblRawScalerValues[i] = rawScalervalues[i];
			}
			scalerData = ArrayUtils.addAll(scalerData, dblRawScalerValues);
		}

		String[] names = getExtraNames();
		for (int i = 0; i < names.length; i++) {
			thisFrame.setPlottableValue(names[i], scalerData[i]);
		}

		return thisFrame;
	}

	/**
	 * Gets the multi-channel data for all elements. Includes setup of the detector etc. Reads from one frame and tfg
	 * counts for time passed in (suggest 1000ms). Reads 1 time frame.
	 * 
	 * @param time
	 *            the time to count for (milliseconds)
	 * @return array[numberOfDetectors][mcaChannels] of int values representing the counts in each channel.
	 * @throws DeviceException
	 */
	@Override
	public int[][][] getMCData(int time) throws DeviceException {

		stop();
		clear();
		start();
		timer.countAsync(time);
		do {
			synchronized (this) {
				try {
					wait(100);
				} catch (InterruptedException e) {
					// do nothing for now
				}
			}
		} while (timer.getStatus() == Timer.ACTIVE);

		stop();

		int[] data = null;
		if (mcaHandle >= 0 && daServer != null && daServer.isConnected()) {
			data = readoutMca(0, 1, 4096); // NOTE 1 time frame
		}

		if (data != null) {
			try {
				// Note We dont have resgrades so this is set to 1
				// for compatibility with xspressDetector interface
				int[][][][] fourD = unpackRawDataTo4D(data, 1, 1, 4096);
				return fourD[0];
			} catch (Exception e) {
				throw new DeviceException("Error while unpacking MCA Data. Data length was " + data.length, e);
			}
		}

		return null;
	}

	/**
	 * Readout full mca for every detector element and specified time frame
	 * 
	 * @param startFrame
	 *            time frame to read
	 * @param numberOfFrames
	 * @return mca data
	 * @throws DeviceException
	 */
	private synchronized int[] readoutMca(int startFrame, int numberOfFrames, int mcaSize) throws DeviceException {
		int[] value = null;
		if (mcaHandle < 0) {
			open();
		}
		if (mcaHandle >= 0 && daServer != null && daServer.isConnected()) {
			try {
				value = daServer.getIntBinaryData("read 0 0 " + startFrame + " " + mcaSize + " " + numberOfDetectors
						+ " " + numberOfFrames + " from " + mcaHandle + " raw motorola", numberOfDetectors
						* mcaSize * numberOfFrames);
			} catch (Exception e) {
				throw new DeviceException(e.getMessage(),e);
			}
		}
		return value;
	}

	@Override
	public int[] getRawScalerData() throws DeviceException {
		return readoutHardwareScalers(0, 1);
	}

	public double[] readoutScalerDataNoCorrection() throws DeviceException {
		if (timer.getAttribute("TotalFrames").equals(0)) {
			return readoutScalerData(0, 0, false)[0];
		}
		return readoutScalerData(framesRead, framesRead, false)[0];
	}

	@Override
	public double[] readoutScalerData() throws DeviceException {
		if (timer.getAttribute("TotalFrames").equals(0)) {
			return readoutScalerData(0, 0, true)[0];
		}
		return readoutScalerData(framesRead, framesRead, true)[0];
	}

	private double[][] readoutScalerData(int startFrame, int finalFrame, boolean performCorrections)	throws DeviceException {
		int numberOfFrames = finalFrame - startFrame + 1;
		int[] rawscalerData = readoutHardwareScalers(startFrame, numberOfFrames);
		return readoutScalerData(numberOfFrames, rawscalerData, performCorrections);
	}

	/*
	 * Basically what goes into the Ascii file. Columns should match the values from getExtraNames() or
	 * getUnFilteredChannelLabels()
	 */
	private double[][] readoutScalerData(int numFrames, int[] rawScalerData, boolean performCorrections) {

		double[][] scalerData = new double[numFrames][];
		
		// else return hardware scalers using the win values from the hardwareData array
		int[][] unpackedScalerData = unpackRawScalerDataToFrames(rawScalerData, numFrames);

		for (int frame = 0; frame < numFrames; frame++) {

			scalerData[frame] = new double[numberOfDetectors];
			int counter = 0;
			for (int element = 0; element < numberOfDetectors; element++) {
				if (performCorrections) {
					int total = unpackedScalerData[frame][counter];
					int resets = unpackedScalerData[frame][counter + 1];
					int acc = unpackedScalerData[frame][counter + 2];
					int windowed = unpackedScalerData[frame][counter + 3];
					double deadtime = xspressDeadTimeParameters.getDetectorDT(element).getProcessDeadTimeInWindow();
					scalerData[frame][element] = relinearize(total, resets, acc, windowed, deadtime);
				} else {
					scalerData[frame][element] = unpackedScalerData[frame][counter + 3];
				}
				counter += 4;
			}

			double ff = 0;
			for (double value : scalerData[frame]) {
				ff += value;
			}
			scalerData[frame] = ArrayUtils.add(scalerData[frame], ff);

		}	
		return scalerData;
	}

	private synchronized int[] readoutHardwareScalers(int startFrame, int numberOfFrames) throws DeviceException {
		int[] value = null;
		if (scalerHandle < 0) {
			open();
		}
		if (scalerHandle >= 0 && daServer != null && daServer.isConnected()) {
			try {
				value = daServer.getIntBinaryData("read 0 0 " + startFrame + " " + numberOfScalers + " "
						+ numberOfDetectors + " " + numberOfFrames + " from " + scalerHandle + " raw motorola",
						numberOfDetectors * numberOfScalers * numberOfFrames);
			} catch (Exception e) {
				throw new DeviceException(e.getMessage(),e);
			}
		}
		return value;
	}

	private int[][] unpackRawScalerDataToFrames(int[] scalerData, int numFrames) {

		int numberDataPerFrame = 4 * getNumberOfDetectors();
		int[][] unpacked = new int[numFrames][numberDataPerFrame];
		int iterator = 0;

		for (int frame = 0; frame < numFrames; frame++) {
			for (int datum = 0; datum < numberDataPerFrame; datum++) {
				unpacked[frame][datum] = scalerData[iterator];
				iterator++;
			}
		}
		return unpacked;
	}

	/**
	 * Rescales the given counts to take into account dead time etc and creates a DetectorReading with the values - not
	 * used by Xspress2Systems.
	 * 
	 * @param total
	 *            the original total counts read from the detector
	 * @param resets
	 *            the number of resets counted
	 * @param acc
	 *            the number of some mysterious electronic things
	 * @param windowed
	 *            the original number of counts in the window
	 * @return the relinearized windowed counts
	 */
	public int relinearize(int total, int resets, int acc, int windowed, double deadTime) {
		// A, B, C, D have no real meaning they are just used to split
		// the rather unwieldy expression into manageable parts.Contact
		// the Detector Group for information about the details of the
		// expression.
		double A;
		double B;
		double C;
		double D;
		double factor;
		double working;
		double deadTimeSquared;
		double deadTimeCubed;
		double bigfactor;

		if (windowed <= 0)
			return (0);

		A = total;
		B = resets;
		C = acc;
		D = windowed;

		factor = (1.0 / (1.0 - B * 1.0e-07));

		A = factor * A;
		C = factor * C;
		D = factor * D;

		deadTimeSquared = deadTime * deadTime;
		deadTimeCubed = deadTime * deadTimeSquared;

		bigfactor = Math.sqrt(4.0 - 20.0 * deadTime * A + 27.0 * deadTimeSquared * A * A);
		bigfactor = bigfactor * Math.sqrt(3.0) / (9.0 * deadTimeCubed);
		bigfactor = bigfactor - 10.0 / (27.0 * deadTimeCubed) + A / deadTimeSquared;
		bigfactor = Math.pow(bigfactor, 1.0 / 3.0);

		working = (bigfactor - 2.0 / (9.0 * deadTimeSquared * bigfactor) + 2.0 / (3.0 * deadTime)) / A;

		working = working * D;

		return (int) working;
	}

	/**
	 * Convert the raw 1D array from DAServer into the 4D data it represents.
	 * <p>
	 * Assumes the packing order: frame,channel,res-grade,energy
	 * 
	 * @param rawData
	 * @param numFrames
	 * @return the data as int[][][]
	 */
	private int[][][][] unpackRawDataTo4D(int[] rawData, int numFrames, int numResGrades, int mcaSize) {

		int[][][][] output = new int[numFrames][numberOfDetectors][numResGrades][mcaSize];
		int i = 0;

		for (int frame = 0; frame < numFrames; frame++) {
			for (int channel = 0; channel < numberOfDetectors; channel++) {
				for (int res_grade = 0; res_grade < numResGrades; res_grade++) {
					for (int energy = 0; energy < mcaSize; energy++) {
						output[frame][channel][res_grade][energy] = rawData[i];
						i++;
					}
				}

			}
		}

		return output;
	}

	/**
	 * For use by the TFGXspress class. This defines what is returned by the readoutScalers method.
	 */
	@Override
	public ArrayList<String> getChannelLabels() {
		ArrayList<String> channelLabels = new ArrayList<String>();
		if (onlyDisplayFF) {
			channelLabels.add("FF");
		} else {
			getUnFilteredChannelLabels(channelLabels);
		}
		return channelLabels;
	}

	private void getUnFilteredChannelLabels(ArrayList<String> channelLabels) {
		for (DetectorElement detector : xspressParameters.getDetectorList()) {
			channelLabels.add(detector.getName());
		}
		channelLabels.add("FF");
	}

	@Override
	public int getNumberofGrades() {
		return 1;
	}

	@Override
	public String getResGrade() throws DeviceException {
		return xspressParameters.getResGrade();
	}

	@Override
	public void setResGrade(String grade) throws DeviceException {
		// not used
		xspressParameters.setResGrade("res-none");
	}

	@Override
	public String getReadoutMode() throws DeviceException {
		return xspressParameters.getReadoutMode();
	}

	@Override
	public void setReadoutMode(String readoutMode) throws DeviceException {
		xspressParameters.setReadoutMode(XspressDetector.READOUT_SCALERONLY);
	}

	@Override
	public void setDeadtimeCalculationEnergy(Double energy) throws DeviceException {
		deadtimeEnergy = energy;
	}

	@Override
	public Double getDeadtimeCalculationEnergy() throws DeviceException {
		return deadtimeEnergy;
	}

	private synchronized void sendCommand(String command, int handle) throws DeviceException {
		Object obj;
		if ((obj = daServer.sendCommand(command + handle)) == null) {
			throw new DeviceException("Null reply received from daserver during " + command);
		} else if (((Integer) obj).intValue() == -1) {
			logger.error(getName() + ": " + command + " failed");
			close();
			throw new DeviceException("Xspress1System " + getName() + " " + command + " failed");
		}
	}

	// this method is only for Junit testing
	/**
	 * for use by junit tests
	 * @throws DeviceException 
	 */
	protected void setFail() throws DeviceException {
		if (daServer != null && daServer.isConnected()) {
			daServer.sendCommand("Fail");
		}
	}
}