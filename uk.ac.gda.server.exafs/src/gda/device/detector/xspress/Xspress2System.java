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

import gda.configuration.properties.LocalProperties;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.Timer;
import gda.device.detector.DAServer;
import gda.device.detector.DetectorBase;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NexusDetector;
import gda.device.detector.countertimer.TfgScaler;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Finder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;
import org.nexusformat.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.xspress.DetectorDeadTimeElement;
import uk.ac.gda.beans.xspress.DetectorElement;
import uk.ac.gda.beans.xspress.XspressDeadTimeParameters;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.beans.xspress.XspressROI;
import uk.ac.gda.util.CorrectionUtils;
import uk.ac.gda.util.beans.xml.XMLHelpers;

/**
 * Represents a set of Xspress2 boards and detectors. It actually communicates with a DAServer object which is connected
 * to a da.server process running on a MVME computer.
 * <p>
 * This returns data as a Nexus tree from its readout method.
 * <p>
 * As this acts differently from the TFGv1 classes, some of the Xpress interface methods may not be implemented. This
 * needs resolving at some point.
 * <p>
 * Deadtime correction methods: none thres all hw sca hw - - hw sca+mca hw - - roi hw new hw hw = apply deadtime factor
 * from hardware scalers only new = scale both types of ROI using total counts / counts in rois This needs refactoring
 * so that roi when all are selected are also corrected.
 */
public class Xspress2System extends DetectorBase implements NexusDetector, XspressDetector, Scannable, Configurable {

	private static final Logger logger = LoggerFactory.getLogger(Xspress2System.class);

	private static final int NO_RES_GRADE = 1;
	private static final int RES_THRES = 2;
	private static final int ALL_RES = 16;

	// These are used to calculate the size of the datas
	private int fullMCABits = 12;
	private int mcaGrades = 1; // reset during every open()
	private Integer maxNumberOfFrames = 0; // the number of frames which TFG has space for, based on the current config
	// in TFG

	// These are the objects this must know about.
	private String daServerName;
	protected DAServer daServer = null;
	private String tfgName;
	protected Timer tfg = null;

	// hold the parameters for this object
	private XspressParameters xspressParameters;

	// Values used in DAServer commands
	private String mcaOpenCommand = null;
	private String scalerOpenCommand = null;
	private String startupScript = null;
	protected int numberOfDetectors;
	// number of values from each hardware scaler (e.g. total, resets, originalWindowed, time)
	protected final int numberOfScalers = 4;
	private int mcaHandle = -1;
	private int scalerHandle = -1;
	private String xspressSystemName;
	private boolean sumAllElementData = false;

	// Full path to config file
	private String configFileName = null;

	private Double deadtimeEnergy = null;  // in keV NOT eV!

	protected int framesRead = 0;
	// mode override property, when set to true the xspress is always set in SCAlers and MCA Mode
	// does not change with the value in the parameters file, no rois are set
	private boolean modeOverride = LocalProperties.check("gda.xspress.mode.override");

	private Boolean onlyDisplayFF = false;

	public static final String ONLY_DISPLAY_FF_ATTR = "ff_only";

	private Boolean addDTScalerValuesToAscii = false;
	private Boolean saveRawSpectrum = false;

	public static final String ADD_DT_VALUES_ATTR = "add_dt_values";

	// this is only when using resgrades, when resgrades separated and 'extra' ascii columns are requested
	// TODO this class *really* needs refactoring!...
	private TfgScaler ionChambersCounterTimer = null;

	private XspressDeadTimeParameters xspressDeadTimeParameters;

	private String dtcConfigFileName;

	public String getDtcConfigFileName() {
		return dtcConfigFileName;
	}

	public void setDtcConfigFileName(String dtcConfigFileName) {
		this.dtcConfigFileName = dtcConfigFileName;
	}

	public Xspress2System() {
		this.inputNames = new String[] {};
	}

	@Override
	public void configure() throws FactoryException {
		// A real system needs a connection to a real da.server via a DAServer object.
		if (daServer == null) {
			logger.debug("Xspress2System.configure(): finding: " + daServerName);
			if ((daServer = (DAServer) Finder.getInstance().find(daServerName)) == null) {
				logger.error("Xspress2System.configure(): Server " + daServerName + " not found");
			}
		}
		// Both dummy and real systems should have a tfg
		if (tfg == null) {
			logger.debug("Xspress2System.configure(): finding " + tfgName);
			if ((tfg = (Timer) Finder.getInstance().find(tfgName)) == null) {
				logger.error("Xspress2System.configure(): TimeFrameGenerator " + tfgName + " not found");
			}
		}
		try {
			loadAndInitializeDetectors(configFileName, dtcConfigFileName);
		} catch (Exception e) {
			logger.error(
					"Error loading template XML. Will use a detector with a few default elements instead. "
							+ e.getMessage(), e);
			ArrayList<XspressROI> regions = new ArrayList<XspressROI>();
			xspressParameters = new XspressParameters();
			xspressDeadTimeParameters = new XspressDeadTimeParameters();
			if (modeOverride) {
				xspressParameters.setReadoutMode(READOUT_MCA);
			}
			else {
				xspressParameters.setReadoutMode(READOUT_SCALERONLY);
			}
			xspressParameters.setResGrade(ResGrades.NONE);
			xspressParameters.addDetectorElement(new DetectorElement("Element0", 0, 0, 4000, false, regions));
			xspressParameters.addDetectorElement(new DetectorElement("Element1", 1, 85, 2047, false, regions));
			xspressParameters.addDetectorElement(new DetectorElement("Element2", 2, 34, 2439, false, regions));
			xspressParameters.addDetectorElement(new DetectorElement("Element3", 3, 31, 2126, false, regions));
			xspressParameters.addDetectorElement(new DetectorElement("Element4", 4, 0, 4000, true, regions));
			xspressParameters.addDetectorElement(new DetectorElement("Element5", 5, 85, 2047, false, regions));
			xspressParameters.addDetectorElement(new DetectorElement("Element6", 6, 34, 2439, false, regions));
			xspressParameters.addDetectorElement(new DetectorElement("Element7", 7, 31, 2126, false, regions));
			xspressParameters.addDetectorElement(new DetectorElement("Element8", 8, 31, 2126, false, regions));
			xspressDeadTimeParameters.addDetectorDeadTimeElement(new DetectorDeadTimeElement("Elemen0", 0, 3.4E-7, 0.0,
					3.4E-7));
			xspressDeadTimeParameters.addDetectorDeadTimeElement(new DetectorDeadTimeElement("Elemen1", 1, 3.8E-7, 0.0,
					3.8E-7));
			xspressDeadTimeParameters.addDetectorDeadTimeElement(new DetectorDeadTimeElement("Elemen2", 2, 3.7E-7, 0.0,
					3.7E-7));
			xspressDeadTimeParameters.addDetectorDeadTimeElement(new DetectorDeadTimeElement("Elemen3", 3, 3.0E-7, 0.0,
					3.0E-7));
			xspressDeadTimeParameters.addDetectorDeadTimeElement(new DetectorDeadTimeElement("Elemen4", 4, 3.4E-7, 0.0,
					3.4E-7));
			xspressDeadTimeParameters.addDetectorDeadTimeElement(new DetectorDeadTimeElement("Elemen5", 5, 3.5E-7, 0.0,
					3.5E-7));
			xspressDeadTimeParameters.addDetectorDeadTimeElement(new DetectorDeadTimeElement("Elemen6", 6, 3.3E-7, 0.0,
					3.3E-7));
			xspressDeadTimeParameters.addDetectorDeadTimeElement(new DetectorDeadTimeElement("Elemen7", 7, 3.0E-7, 0.0,
					3.0E-7));
			xspressDeadTimeParameters.addDetectorDeadTimeElement(new DetectorDeadTimeElement("Elemen8", 8, 3.3E-7, 0.0,
					3.3E-7));
			numberOfDetectors = xspressParameters.getDetectorList().size();
		}

		if (!ResGrades.isResGrade(xspressParameters.getResGrade())) {
			throw new FactoryException("resGrade " + xspressParameters.getResGrade() + " is not an acceptable string");
		}

		// If everything has been found send the format, region of interest, windows & open commands.
		if (tfg != null && (daServer != null)) {
			try {
				close();
				doStartupScript();
				doFormatRunCommand(determineNumberOfBits());
				configureDetectorFromParameters();
				open();
			} catch (DeviceException e) {
				throw new FactoryException(e.getMessage(), e);
			}
		}

		configured = true;

	}

	private void configureDetectorFromParameters() throws DeviceException {
		// always remove all rois first
		if (modeOverride) {
			xspressParameters.setReadoutMode(READOUT_MCA);
		} else {
			doRemoveROIs();
		}
		for (DetectorElement detector : xspressParameters.getDetectorList()) {
			doSetWindowsCommand(detector);
			if (xspressParameters.getReadoutMode().equals(XspressDetector.READOUT_ROIS)) {
				doSetROICommand(detector);
			}
		}
	}

	@Override
	public Double getDeadtimeCalculationEnergy() throws DeviceException {
		return deadtimeEnergy;
	}

	@Override
	public void setDeadtimeCalculationEnergy(Double energy) throws DeviceException {
		deadtimeEnergy = energy;
	}

	public int getMcaHandle() {
		return mcaHandle;
	}

	public int getScalerHandle() {
		return scalerHandle;
	}

	public void setDaServerName(String daServerName) {
		this.daServerName = daServerName;
	}

	public String getDaServerName() {
		return daServerName;
	}

	public DAServer getDaServer() {
		return daServer;
	}

	public void setDaServer(DAServer daServer) {
		this.daServer = daServer;
	}

	public Timer getTfg() {
		return tfg;
	}

	public void setTfg(Timer tfg) {
		this.tfg = tfg;
	}

	@Override
	public int getNumberOfDetectors() {
		return numberOfDetectors;
	}

	/**
	 * Sets the number of elements (detectors) in the Xspress
	 * 
	 * @param numberOfDetectors
	 */
	public void setNumberOfDetectors(int numberOfDetectors) {
		this.numberOfDetectors = numberOfDetectors;
	}

	/**
	 * The number of different versions of mca/roi data read out. This varies depending on the resolution mode in use: 1
	 * if no res threshold, 2 if a threshold set and 16 if no threshold but all res grades to be read out separately.
	 * 
	 * @return The number of resolution grades
	 */
	@Override
	public int getNumberofGrades() {
		return mcaGrades;
	}

	/**
	 * This is the string used by DAServer to set the mode of the resolution grade.
	 * 
	 * @return Returns the resGrade.
	 */
	@Override
	public String getResGrade() {
		return xspressParameters.getResGrade();
	}

	/**
	 * @param resGrade
	 *            The resGradeto set, if set to "res-thres" an additional float value in the range 0.0 to 16.0 is
	 *            required.
	 * @throws DeviceException
	 */
	@Override
	public void setResGrade(String resGrade) throws DeviceException {

		if (!ResGrades.isResGrade(resGrade)) {
			throw new DeviceException("resGrade " + resGrade + " is not an acceptable string");
		}

		setResolutionGrade(resGrade, determineNumberOfBits());
	}

	private void setResolutionGrade(String resGrade, int numberOfBits) throws DeviceException {
		xspressParameters.setResGrade(resGrade);
		if (configured) {
			close();
			doFormatRunCommand(numberOfBits);
			open();
		}
	}

	/**
	 * Sets a region of interest. This is used when the readout mode is set to ROI. Each region of interest may be a
	 * partial mca or a virtual scaler (sum of counts in that region).
	 * 
	 * @param detector
	 * @param regionList
	 * @throws DeviceException
	 */
	public void setRegionOfInterest(int detector, ArrayList<XspressROI> regionList) throws DeviceException {
		DetectorElement detectorElement = xspressParameters.getDetector(detector);
		detectorElement.setRegionList(regionList);
		if (configured) {
			doSetROICommand(detectorElement);
		}
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

	public String getTfgName() {
		return tfgName;
	}

	public void setTfgName(String tfgName) {
		this.tfgName = tfgName;
	}

	public String getConfigFileName() {
		return configFileName;
	}

	public void setConfigFileName(String configFileName) {
		this.configFileName = configFileName;
	}

	@Override
	public void atScanLineStart() throws DeviceException {
		if (!daServer.isConnected()) {
			daServer.connect();
		}
		// if this class was used to define framesets, then memeory is only cleared at the start of the scan
		if (!tfg.getAttribute("TotalFrames").equals(0)) {
			stop();
			clear();
			start();
		}
		framesRead = 0;
	}

	@Override
	public void atScanStart() throws DeviceException {
		if (!daServer.isConnected()) {
			daServer.connect();
		}
		stop();
		clear();
		start();
		framesRead = 0;
	}

	@Override
	public String[] getOutputFormat() {
		if (onlyDisplayFF
				|| !(xspressParameters.getReadoutMode().equals(XspressDetector.READOUT_ROIS) && mcaGrades == ALL_RES)) {
			return super.getOutputFormat();
		}

		return getAllResGradesInAsciiOutputFormat();
	}

	/*
	 * for only when all res grades being displayed separately in ascii output
	 */
	private String[] getAllResGradesInAsciiOutputFormat() {
		ArrayList<String> format = new ArrayList<String>();
		for (int i = 0; i < 16; i++) {
			format.add("%.4g");
		}
		// for best8 grades for each element
		for (int i = 0; i < xspressParameters.getDetectorList().size(); i++) {
			format.add(super.getOutputFormat()[0]);
		}
		// for FF
		format.add(super.getOutputFormat()[0]);

		if (addDTScalerValuesToAscii) {
			for (int i = 0; i < xspressParameters.getDetectorList().size(); i++) {
				for (int j = 0; j < 4; j++) {
					format.add(super.getOutputFormat()[0]);
				}
			}
		}
		return format.toArray(new String[0]);
	}

	@Override
	public String[] getExtraNames() {
		return getChannelLabels().toArray(new String[0]);
	}

	@Override
	public ArrayList<String> getChannelLabels() {
		ArrayList<String> channelLabels = new ArrayList<String>();
		if (onlyDisplayFF) {
			channelLabels.add("FF");
		} else {
			getUnFilteredChannelLabels(channelLabels);
		}

		if (addDTScalerValuesToAscii) {
			for (DetectorElement detector : xspressParameters.getDetectorList()) {
				channelLabels.add(detector.getName() + "_allEvents");
				channelLabels.add(detector.getName() + "_numResets");
				channelLabels.add(detector.getName() + "_inWinEvents");
				channelLabels.add(detector.getName() + "_tfgClock");
			}

		}
		return channelLabels;
	}

	private void getUnFilteredChannelLabels(ArrayList<String> channelLabels) {
		if (xspressParameters.getReadoutMode().equals(XspressDetector.READOUT_ROIS)) {
			// loop through all elements and find all the virtual scalers
			if (mcaGrades != ALL_RES) {
				for (DetectorElement element : xspressParameters.getDetectorList()) {
					String channelName = element.getName() + "_";
					for (XspressROI roi : element.getRegionList()) {
						channelLabels.add(channelName + roi.getRoiName());
					}
				}
			}
			// when all 16 grades separate, then a 'full' list of channels means 16 resGrade bins: 15, 15+14,
			// 15+14+13....
			else {
				for (int i = 0; i < 16; i++) {
					channelLabels.add("res_bin_" + i + "_norm");
				}
				for (DetectorElement element : xspressParameters.getDetectorList()) {
					channelLabels.add(element.getName() + "_best8");
				}
			}
			channelLabels.add("FF");

			if (mcaGrades == RES_THRES) {
				channelLabels.add("FF_bad");
			}

		} else {
			for (DetectorElement detector : xspressParameters.getDetectorList()) {
				channelLabels.add(detector.getName());
			}
			channelLabels.add("FF");
		}
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

	public String getStartupScript() {
		return startupScript;
	}

	public String getXspressSystemName() {
		return xspressSystemName;
	}

	public void setXspressSystemName(String xspressSystemName) {
		this.xspressSystemName = "'" + xspressSystemName + "'";
	}

	public Boolean getAddDTScalerValuesToAscii() {
		return addDTScalerValuesToAscii;
	}

	public void setAddDTScalerValuesToAscii(Boolean addDTScalerValuesToAscii) {
		this.addDTScalerValuesToAscii = addDTScalerValuesToAscii;
	}

	/**
	 * @return Returns the bit size of a full mca (12 = 4096)
	 */
	public int getFullMCABits() {
		return fullMCABits;
	}

	public int getFullMCASize() {
		return 1 << fullMCABits;
	}

	/**
	 * @param fullMCABits
	 * @throws DeviceException
	 */
	public void setFullMCABits(int fullMCABits) throws DeviceException {
		this.fullMCABits = fullMCABits;
		if (configured) {
			close();
			doFormatRunCommand(determineNumberOfBits());
			open();
		}

	}

	/**
	 * Readout mode refers to the nature of the data this returns in its readout and readoutScalers methods. This is not
	 * a setting in DAServer.
	 */
	@Override
	public String getReadoutMode() throws DeviceException {
		return xspressParameters.getReadoutMode();
	}

	@Override
	public void setReadoutMode(String readoutMode) throws DeviceException {
		if (modeOverride && !readoutMode.equals(xspressParameters.getReadoutMode())) {
			xspressParameters.setReadoutMode(XspressDetector.READOUT_MCA);
			configureDetectorFromParameters();
		} else if ((readoutMode.equals(XspressDetector.READOUT_SCALERONLY)
				|| readoutMode.equals(XspressDetector.READOUT_MCA) || readoutMode.equals(XspressDetector.READOUT_ROIS))
				&& !readoutMode.equals(xspressParameters.getReadoutMode())) {
			xspressParameters.setReadoutMode(readoutMode);
			configureDetectorFromParameters();
		}
	}

	public boolean isOnlyDisplayFF() {
		return onlyDisplayFF;
	}

	public void setOnlyDisplayFF(boolean onlyDisplayFF) {
		this.onlyDisplayFF = onlyDisplayFF;
	}

	@Override
	public void setAttribute(String attribute, Object value) throws DeviceException {

		if (attribute.equals("readoutModeForCalibration") && value instanceof String[]) {

			String readoutMode = ((String[]) value)[0];
			String resGrade = ((String[]) value)[1];

			// as before
			setReadoutMode(readoutMode);

			// like setResGrade but the numberOfBits MUST be 12
			setResolutionGrade(resGrade, 12);
		} else if (attribute.equals(ONLY_DISPLAY_FF_ATTR)) {
			Boolean ffonly = Boolean.parseBoolean(value.toString());
			this.onlyDisplayFF = ffonly;
		} else {
			super.setAttribute(attribute, value);
		}
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

	public TfgScaler getIonChambersCounterTimer() {
		return ionChambersCounterTimer;
	}

	public void setIonChambersCounterTimer(TfgScaler ionChambersCounterTimer) {
		this.ionChambersCounterTimer = ionChambersCounterTimer;
	}

	/**
	 * Sends the daServer commands to clear the xspress system. Note that this is very time consuming and should only be
	 * done when necessary.
	 * 
	 * @throws DeviceException
	 */
	@Override
	public void clear() throws DeviceException {
		if (mcaHandle < 0 || scalerHandle < 0) {
			open();
		}
		if (mcaHandle >= 0 && daServer != null && daServer.isConnected()) {
			sendCommand("clear ", mcaHandle);
		}
		if (scalerHandle >= 0 && daServer != null && daServer.isConnected()) {
			sendCommand("clear ", scalerHandle);
		}
	}

	/**
	 * Sends the daServer commands to enable the xspress system counting. This does not start the TFG counting.
	 * 
	 * @see gda.device.detector.xspress.XspressDetector#start()
	 */
	@Override
	public void start() throws DeviceException {
		if (mcaHandle < 0 || scalerHandle < 0) {
			open();
		}
		if (mcaHandle >= 0 && daServer != null && daServer.isConnected()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				logger.error("TODO put description of error here", e);
			}
			sendCommand("enable ", mcaHandle);
		}
		if (scalerHandle >= 0 && daServer != null && daServer.isConnected()) {
			sendCommand("enable ", scalerHandle);
		}
	}

	@Override
	public void atScanEnd() throws DeviceException {
		// if this class was used to define framesets, then ensure they are cleared at the end of the scan
		if (tfg.getAttribute("TotalFrames").equals(0)) {
			stop(); // stops the TFG - useful if scan aborted and so TFG still in a PAUSE state rather than an IDLE
					// state
		}
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

		close();
	}

	@Override
	public void close() throws DeviceException {
		if (mcaHandle >= 0 && daServer != null && daServer.isConnected()) {
			daServer.sendCommand("close " + mcaHandle);
			mcaHandle = -1;
		}
		if (scalerHandle >= 0 && daServer != null && daServer.isConnected()) {
			daServer.sendCommand("close " + scalerHandle);
			scalerHandle = -1;
		}
	}

	/**
	 * @return int - the size in bits of the MCA array based on the readout mode and region of interest options.
	 */
	private int determineNumberOfBits() {

		if (!xspressParameters.getReadoutMode().equals(XspressDetector.READOUT_ROIS)) {
			return fullMCABits;
		}

		int channels = findLargestChannelReadout();

		int order = 0;

		do {
			order++;
		} while (Math.pow(2, order) <= channels);

		return order;
	}

	private int findLargestChannelReadout() {
		int maxSize = 0;
		for (DetectorElement element : xspressParameters.getDetectorList()) {
			int thisMcasize = 1; // always get an extra values for the out of window counts
			for (XspressROI roi : element.getRegionList()) {
				if (xspressParameters.getRegionType().equals(XspressParameters.VIRTUALSCALER))
					thisMcasize++;
				else
					thisMcasize += roi.getRoiEnd() - roi.getRoiStart() + 1;
			}
			if (maxSize < thisMcasize)
				maxSize = thisMcasize;
		}
		return maxSize;
	}

	/**
	 * @return the current size of the mca's based on the readout mode and region of interest options.
	 */
	public int getCurrentMCASize() {
		return 1 << determineNumberOfBits();
	}

	public int getCurrentMCABits() {
		return determineNumberOfBits();
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

				String newResGrade = xspressParameters.getResGrade();
				// override the res-grade if the readout mode is scalers only or saclers + mca
				// This might not be the best place to do this
				if (!xspressParameters.getReadoutMode().equals(XspressDetector.READOUT_ROIS)) {
					newResGrade = ResGrades.NONE;
				}

				startupScript = "xspress2 format-run 'xsp1' " + newResGrade;

				if ((obj = daServer.sendCommand(startupScript)) == null) {
					throw new DeviceException("Null reply received from daserver during " + startupScript);
				} else if (((Integer) obj).intValue() == -1) {
					throw new DeviceException(getName() + ": " + startupScript + " failed");
				} else {
					maxNumberOfFrames = ((Integer) obj).intValue();
					logger.info("Xspress2System startup script - reply  was: " + maxNumberOfFrames);
				}
			}
		}
	}

	/**
	 * Execute the format-run command on da.server. This sets the resgrade.
	 */
	private void doFormatRunCommand(int numberOfBits) throws DeviceException {

		String newResGrade = xspressParameters.getResGrade();
		// override the res-grade if the readout mode is scalers only or saclers + mca
		// This might not be the best place to do this
		if (!xspressParameters.getReadoutMode().equals(XspressDetector.READOUT_ROIS)) {
			newResGrade = ResGrades.NONE;
		}

		String formatCommand = "xspress2 format-run " + xspressSystemName + " " + numberOfBits + " " + newResGrade;
		if (daServer != null && daServer.isConnected()) {
			Integer numFrames = ((Integer) daServer.sendCommand(formatCommand)).intValue();
			if (numFrames == null) {
				throw new DeviceException("Null reply received from daserver during " + formatCommand);
			} else if (numFrames == -1) {
				throw new DeviceException(getName() + ": " + formatCommand + " failed");
			} else if (numFrames < maxNumberOfFrames) {
				maxNumberOfFrames = numFrames;
				logger.info("Xspress2System formatCommand - maximum time frames achievable: " + maxNumberOfFrames);
			} else {
				logger.info("Xspress2System formatCommand - maximum time frames achievable: " + numFrames
						+ " but limited to " + maxNumberOfFrames + " by startupscript");
			}
		}
	}

	private void doRemoveROIs() throws DeviceException {

		Object obj;
		int rc;
		String roiCommand = "xspress2 set-roi " + xspressSystemName + " -1";
		if ((obj = daServer.sendCommand(roiCommand)) != null) {
			if ((rc = ((Integer) obj).intValue()) < 0) {
				throw new DeviceException("Xspress2System error removing regions of interest: " + rc);
			}
		}

	}

	private void doSetROICommand(DetectorElement detector) throws DeviceException {

		Object obj;
		int rc;

		String roiCommand = "xspress2 set-roi " + xspressSystemName + " " + detector.getNumber();
		List<XspressROI> regionList = detector.getRegionList();
		if (regionList.isEmpty()) {
			return; // No regions for detector element.
		}

		for (XspressROI region : regionList) {
			roiCommand += " " + region.getRoiStart() + " " + region.getRoiEnd() + " "
					+ calculateRegionBins(region);
		}

		if ((obj = daServer.sendCommand(roiCommand)) != null) {
			if ((rc = ((Integer) obj).intValue()) < 0) {
				throw new DeviceException("Xspress2System error setting regions of interest: " + rc);
			}
		}
	}

	private int calculateRegionBins(XspressROI region) {
		int regionBins = 1; // 1 means a virtual scaler
		if (xspressParameters.getRegionType() != null && xspressParameters.getRegionType().equals(XspressROI.MCA)) {
			// else regionBins should be the size of the MCA. (DAserver will not accept any other values).
			regionBins = region.getRoiEnd() - region.getRoiStart() + 1;
		}
		return regionBins;
	}

	private void doSetWindowsCommand(DetectorElement detector) throws DeviceException {
		Object obj;
		int rc;
		String windowCommand = "xspress2 set-window " + xspressSystemName + " " + detector.getNumber() + " "
				+ detector.getWindowStart() + " " + detector.getWindowEnd();
		if ((obj = daServer.sendCommand(windowCommand)) != null) {
			if ((rc = ((Integer) obj).intValue()) < 0) {
				throw new DeviceException("Xspress2System error setting windows: " + rc);
			}
		}
	}

	/*
	 * Opens the connections to daServer.
	 */
	private void open() throws DeviceException {
		Object obj;
		if (daServer != null && daServer.isConnected()) {
			if (mcaOpenCommand != null) {
				if ((obj = daServer.sendCommand(mcaOpenCommand)) != null) {
					mcaHandle = ((Integer) obj).intValue();
					if (mcaHandle < 0) {
						throw new DeviceException("Failed to create the mca handle");
					}
					logger.info("Xspress2System: open() using mcaHandle " + mcaHandle);
				}
			}

			if (scalerOpenCommand != null) {
				if ((obj = daServer.sendCommand(scalerOpenCommand)) != null) {
					scalerHandle = ((Integer) obj).intValue();
					if (scalerHandle < 0) {
						throw new DeviceException("Failed to create the scaler handle");
					}
					logger.info("Xspress2System: open() using scalerHandle " + scalerHandle);
				}
			}

			if ((obj = daServer.sendCommand("xspress2 get-res-bins " + xspressSystemName)) != null) {
				mcaGrades = ((Integer) obj).intValue();
				logger.info("Xspress2System: mcaGrades " + mcaGrades);
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
			numberOfDetectors = xspressParameters.getDetectorList().size();
		}
		// if mode override is set as a proeprty ignore all the parameter file settings
		if (modeOverride)
			xspressParameters.setReadoutMode(READOUT_MCA);
	}

	public void loadAndInitializeDetectors(String filename, String dtcConfigFileName) throws Exception {
		loadAndInitializeDetectors(filename);
		xspressDeadTimeParameters = (XspressDeadTimeParameters) XMLHelpers.createFromXML(
				XspressDeadTimeParameters.mappingURL, XspressDeadTimeParameters.class,
				XspressDeadTimeParameters.schemaURL, dtcConfigFileName);
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

	/**
	 * Sets the window of the given detector.
	 * 
	 * @param number
	 *            the detector element number
	 * @param lower
	 *            the start of the window
	 * @param upper
	 *            the end of the window
	 */
	@Override
	public void setDetectorWindow(int number, int lower, int upper) {
		xspressParameters.getDetector(number).setWindow(lower, upper);
	}

	@Override
	public DetectorElement getDetector(int which) throws DeviceException {
		return xspressParameters.getDetector(which);
	}

	public XspressDeadTimeParameters getDeadTimeParameters() {
		return xspressDeadTimeParameters;
	}

	/**
	 * Gets the multi-channel data for all elements. Includes setup of the detector etc. Reads from one frame and tfg
	 * counts for time passed in (suggest 1000ms). Reads 1 time frame and assumes desired resGrade has already been set.
	 * 
	 * @param time
	 *            the time to count for (milliseconds)
	 * @return array[numberOfDetectors][mcaChannels] of int values representing the counts in each channel.
	 * @throws DeviceException
	 */
	@Override
	public int[][][] getMCData(int time) throws DeviceException {
		if (!daServer.isConnected()) {
			daServer.connect();
		}
		// stop();
		clear();
		start();
		tfg.countAsync(time);
		do {
			synchronized (this) {
				try {
					wait(100);
				} catch (InterruptedException e) {
					// do nothing for now
				}
			}
		} while (tfg.getStatus() == Timer.ACTIVE);

		// stop();

		int[] data = null;
		if (mcaHandle >= 0 && daServer != null && daServer.isConnected()) {
			data = readoutMca(0, 1, 4096); // NOTE 1 time frame
		}

		if (data != null) {
			try {
				int[][][][] fourD = unpackRawDataTo4D(data, 1, numResGrades(), 4096);
				return fourD[0];
			} catch (Exception e) {
				throw new DeviceException("Error while unpacking MCA Data. Data length was " + data.length, e);
			}
		}

		return null;
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		// this returns a Nexus Tree as it implements NexusDetector
		return null;
	}

	/**
	 * @return int - the number of copies of data you get depending on resolution setting
	 */
	private int numResGrades() {

		// if not in ROI mode then only one res grade
		if (xspressParameters.getReadoutMode().compareTo(READOUT_ROIS) != 0) {
			return 1;
		}

		// if none then only one set of 4096 numbers per mca
		if (xspressParameters.getResGrade().compareTo(ResGrades.NONE) == 0) {
			return NO_RES_GRADE;
		}
		// if all re-grades then 16 arrays per mca
		else if (xspressParameters.getResGrade().compareTo(ResGrades.ALLGRADES) == 0) {
			return ALL_RES;
		}
		// otherwise you get 2 arrays (bad, good)
		else {
			return RES_THRES;
		}
	}

	private int[][] getResGradesSlice(int[][] elementData, int startSlice, int endSlice, int startMCAPosition,
			int endMCAPosition) {
		int[][] out = new int[elementData.length][];

		for (int i = 0; i < elementData.length; i++) {
			int[] data = elementData[i];
			int[] slice = Arrays.copyOfRange(data, startSlice, endSlice + 1);

			int[] paddedArray = new int[getFullMCASize()];
			Arrays.fill(paddedArray, 0);

			int j = 0;
			for (int cell = startMCAPosition; cell < endMCAPosition; cell++) {
				paddedArray[cell] = slice[j];
				j++;
			}

			out[i] = paddedArray;
		}

		return out;
	}

	private int[] getResGradesValues(int[][] elementData, int startSlice) {
		int[] out = new int[elementData.length];

		for (int i = 0; i < elementData.length; i++) {
			out[i] = elementData[i][startSlice];
		}

		return out;
	}

	private interface Reading {
		int getElementNumber();

		String getRoiName();
	}

	private class VSReading implements Reading {
		protected String roiName; // name of the roi, so Readings from different elements can be asscoiated with each
		// other
		protected int elementNumber;
		protected double[] counts;
		@SuppressWarnings("unused")
		// maybe used in a note in the future
		protected String name; // unique name for this reading within this frame
		protected boolean contributesToFF = false; // if OUT of window, then should not be used to calculate FF

		protected VSReading(String roiName, int elementNumber, double[] counts, String name, boolean contributesToFF) {
			this.elementNumber = elementNumber;
			this.counts = counts;
			this.name = name;
			this.roiName = roiName;
			this.contributesToFF = contributesToFF;
		}

		@Override
		public int getElementNumber() {
			return elementNumber;
		}

		@Override
		public String getRoiName() {
			return roiName;
		}
	}

	private class MCAReading implements Reading {
		protected String roiName; // name of the roi, so Readings from different elements can be associated with each
		// other
		protected int elementNumber;
		@SuppressWarnings("unused")
		// maybe used in a note in the future
		protected String name; // unique name for this reading within this frame
		protected double[][] mcacounts;
		@SuppressWarnings("unused")
		protected int roiStart; // maybe used in a note in the future
		@SuppressWarnings("unused")
		protected int roiEnd; // maybe used in a note in the future
		protected double peakArea; // the contribution to FF from this partial MCA, when all res grades split, this
		protected double peakArea_bad;

		// should only be the top 8

		protected MCAReading(String roiName, int elementNumber, double[][] counts, String name, int roiStart,
				int roiEnd, double peakArea, double peakArea_bad) {
			this.elementNumber = elementNumber;
			this.mcacounts = counts;
			this.name = name;
			this.roiStart = roiStart;
			this.roiEnd = roiEnd;
			this.peakArea = peakArea;
			this.peakArea_bad = peakArea_bad;
			this.roiName = roiName;
		}

		@Override
		public int getElementNumber() {
			return elementNumber;
		}

		@Override
		public String getRoiName() {
			return roiName;
		}
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
	 * @param numberOfFrames
	 * @param scalerData
	 *            - the raw data from the scalers or null if no corrections are to be performed
	 * @param addOutOfWindowROI
	 *            - if true then add a virtual scaler at the end of the array or readings for each element of the OUT
	 *            counts
	 * @param mcaData
	 *            - the raw mca data in the 1D array that da.server supplies
	 * @return Reading[frame][element]
	 */
	private Reading[][] getROIs(int numberOfFrames, int[] scalerData, boolean addOutOfWindowROI, int[] mcaData) {

		// create structure to fill
		Reading[][] results = new Reading[numberOfFrames][];

		int[][] unpackedScalerData = new int[numberOfFrames][getNumberOfDetectors() * 4];
		if (scalerData != null) {
			// get the raw hardware scaler data
			unpackedScalerData = unpackRawScalerDataToFrames(scalerData, numberOfFrames);
		}

		// readout the mca memory and unpack into a 3D form
		int[][][][] unpackedMCAData = unpackRawDataTo4D(mcaData, numberOfFrames, numResGrades(), getCurrentMCASize());

		for (int frame = 0; frame < numberOfFrames; frame++) {
			Reading[] value = new Reading[0];
			double[] deadtimeCorrectionFactor = new double[getNumberOfDetectors()];
			Arrays.fill(deadtimeCorrectionFactor, 1.0);
			if (scalerData != null && !saveRawSpectrum) {
				deadtimeCorrectionFactor = calculateDeadtimeCorrectionFactors(convertUnsignedIntToLong(unpackedScalerData[frame]));
			}

			// loop over all the elements
			for (int element = 0; element < xspressParameters.getDetectorList().size(); element++) {

				// calculate the windowingCorrectionFactor based on good and bad
				DetectorElement thisElement = xspressParameters.getDetectorList().get(element);

				// loop over all the ROIs for this element
				int mcaPosition = 0;
				for (int roi = 0; roi < thisElement.getRegionList().size(); roi++) {
					XspressROI thisRoi = thisElement.getRegionList().get(roi);

					// if a virtual scaler return 1,2 or 16 numbers
					if (xspressParameters.getRegionType().equals(XspressParameters.VIRTUALSCALER)) {

						double[] out = extractVirtualScaler(unpackedMCAData, frame, deadtimeCorrectionFactor, element,
								mcaPosition);
						String elementName = getName() + "_element" + element + "_" + thisRoi.getRoiName();

						if (out.length == 2) {
							// if threshold, then split the good and the bad
							if (addOutOfWindowROI) {
								value = (Reading[]) ArrayUtils.add(value, new VSReading(thisRoi.getRoiName() + "_bad",
										element, new double[] { out[0] }, elementName + "_bad", false));
							}
							value = (Reading[]) ArrayUtils.add(value, new VSReading(thisRoi.getRoiName(), element,
									new double[] { out[1] }, elementName, true));
						} else {
							value = (Reading[]) ArrayUtils.add(value, new VSReading(thisRoi.getRoiName(), element, out,
									elementName, true));
						}

						// increment the position in the raw data array
						mcaPosition++;

					} else {
						int mcaEndPosition = mcaPosition + thisRoi.getRoiEnd() - thisRoi.getRoiStart();

						MCAReading reading = extractPartialMCA(unpackedMCAData, frame, deadtimeCorrectionFactor,
								element, mcaPosition, thisRoi, mcaEndPosition);
						value = (Reading[]) ArrayUtils.add(value, reading);

						// increment the position in the raw data array
						mcaPosition = mcaEndPosition + 1;
					}
				}

				if (getNumberofGrades() != ALL_RES && mcaPosition < getCurrentMCASize() - 1) {
					double[] out = extractVirtualScaler(unpackedMCAData, frame, deadtimeCorrectionFactor, element,
							unpackedMCAData[0][0][0].length - 1);
					String elementName = getName() + "_element" + element + "_OUT";
					String outName = "OUT";

					if (out.length == 2) {
						// if threshold, then split the good and the bad
						if (addOutOfWindowROI) {
							value = (Reading[]) ArrayUtils.add(value, new VSReading(outName + "_bad", element,
									new double[] { out[0] }, elementName + "_bad", false));
						}
						value = (Reading[]) ArrayUtils.add(value, new VSReading(outName, element,
								new double[] { out[1] }, elementName, false));

					} else {
						value = (Reading[]) ArrayUtils.add(value, new VSReading(outName, element, out, elementName,
								false));
					}
				}
			}

			results[frame] = value;
		}
		return results;
	}

	/*
	 * returns an MCA object for a single ROI in a single element. Applees to appropriate corrections.
	 */
	private MCAReading extractPartialMCA(int[][][][] unpackedMCAData, int frame, double[] deadtimeCorrectionFactor,
			int element, int mcaPosition, XspressROI thisRoi, int mcaEndPosition) {
		// get the raw data. Only interested in good counts if threshold set
		int[][] mcas;
		int[][] mcaDataForThisElement = unpackedMCAData[frame][element];
		switch (getNumberofGrades()) {
		case NO_RES_GRADE:
			mcas = new int[][] { getResGradesSlice(mcaDataForThisElement, mcaPosition, mcaEndPosition,
					thisRoi.getRoiStart(), thisRoi.getRoiEnd())[0] };
			break;
		case RES_THRES:
			// all res grades
		default:
			mcas = getResGradesSlice(mcaDataForThisElement, mcaPosition, mcaEndPosition, thisRoi.getRoiStart(),
					thisRoi.getRoiEnd());
			break;
		}

		// add to the array of readings
		double dctFactor = 1.0;
		if (deadtimeCorrectionFactor != null) {
			dctFactor = deadtimeCorrectionFactor[element];
		}
		double[][] out = correctMCAArray(mcas, dctFactor);

		double peakArea = 0;
		double peakArea_bad = 0;

		switch (getNumberofGrades()) {
		case NO_RES_GRADE:
			peakArea = sumPartialMCACounts(out[0]);
			break;
		case RES_THRES:
			// correct for good events thrown away in badIn
			double goodIn = sumPartialMCACounts(mcas[1]);
			double badIn = sumPartialMCACounts(mcas[0]);
			double goodOut = mcas[1][mcas[1].length - 1];
			// account for other ROIs by summing everything in the arrays
			double allEvents = sumArrayContents(mcaDataForThisElement[0]) + sumArrayContents(mcaDataForThisElement[1]);
			// so corrected value = goodIn * dct * (all/good)
			peakArea = goodIn * (allEvents / (goodIn + goodOut)) * dctFactor;
			peakArea_bad = badIn * dctFactor;
			break;
		// all res grades
		default:
			for (int grade = 15; grade >= 8; grade--) {
				peakArea += sumPartialMCACounts(out[grade]);
			}
			break;
		}
		String elementName = "element_" + element + "_" + thisRoi.getRoiName();

		// so MCAs are the deadtime corrected partial MCAs, but the scaler values are the sums: 15, 15+14 etc.
		MCAReading reading = new MCAReading(thisRoi.getRoiName(), element, out, elementName, thisRoi.getRoiStart(),
				thisRoi.getRoiEnd(), peakArea, peakArea_bad);
		return reading;
	}

	/*
	 * returns the VS values for a single ROI in a single element. Applees to appropriate corrections.
	 */
	private double[] extractVirtualScaler(int[][][][] unpackedMCAData, int frame, double[] deadtimeCorrectionFactor,
			int element, int mcaPosition) {
		// get the raw data. Only interested in good counts (second element) if threshold set
		int[] vsCounts;
		int[][] mcaDataForThisElement = unpackedMCAData[frame][element];
		double dctFactor = deadtimeCorrectionFactor[element];
		switch (getNumberofGrades()) {
		case NO_RES_GRADE:
			vsCounts = new int[] { getResGradesValues(mcaDataForThisElement, mcaPosition)[0] };
			break;
		case RES_THRES: // BAD, GOOD
			vsCounts = new int[] { getResGradesValues(mcaDataForThisElement, mcaPosition)[0],
					getResGradesValues(mcaDataForThisElement, mcaPosition)[1] };
			break;
		// all res grades
		default:
			vsCounts = getResGradesValues(mcaDataForThisElement, mcaPosition);
			break;
		}

		// then perform corrections based on values thrown away
		switch (getNumberofGrades()) {
		case NO_RES_GRADE:
			return correctScalerArray(vsCounts, deadtimeCorrectionFactor[element]);
		case RES_THRES: // BAD, GOOD
			// correct for good events thrown away in badIn
			double goodIn = vsCounts[1];
			double badIn = vsCounts[0];
			double goodOut = mcaDataForThisElement[1][mcaDataForThisElement[1].length - 1];
			// account for other ROIs by summing everything in array
//			double badOut = mcaDataForThisElement[0][mcaDataForThisElement[0].length - 1];
			double allEvents = sumArrayContents(mcaDataForThisElement[0]) + sumArrayContents(mcaDataForThisElement[1]);

			// so corrected value = goodIn * dct * (all/good)
			double goodIn_corrected = goodIn * (allEvents / (goodIn + goodOut)) * dctFactor;
			double badIn_corrected = badIn; // return the raw value as this is just for interests sake...

			return new double[] { badIn_corrected, goodIn_corrected };
			// all res grades
		default:
			int[] sumsInWindow = new int[16];
			for (int resGrade = 0; resGrade < 16; resGrade++) {
				for (int outBin = 0; outBin < 16; outBin++) {
					if (15 - outBin <= resGrade) {
						sumsInWindow[outBin] += mcaDataForThisElement[resGrade][0];
					}
				}
			}

			int[] sumsOutWindow = new int[16];
			for (int resGrade = 0; resGrade < 16; resGrade++) {
				for (int outBin = 0; outBin < 16; outBin++) {
					if (15 - outBin <= resGrade) {
						sumsOutWindow[outBin] += mcaDataForThisElement[resGrade][mcaDataForThisElement[resGrade].length - 1];
					}
				}
			}

			int totalCounts = sumsInWindow[15] + sumsOutWindow[15]; // so this is sum of all OUT and IN counts over all
			// res grades

			double[] correctedResGrades = new double[16];
			for (int i = 0; i < 16; i++) {
				correctedResGrades[i] = sumsInWindow[i] * (totalCounts / (sumsInWindow[i] + sumsOutWindow[i]))
						* dctFactor;
			}

			return correctedResGrades;

		}
	}

	private int sumArrayContents(int[] is) {
		int total = 0;
		for (int element: is){
			total += element;
		}
		return total;
	}

	private double sumPartialMCACounts(double[] ds) {

		double total = 0;
		for (double channel : ds) {
			total += channel;
		}

		return total;
	}

	private int sumPartialMCACounts(int[] ds) {

		int total = 0;
		for (int channel : ds) {
			total += channel;
		}

		return total;
	}

	/**
	 * Returns a NexusTreeProvider object which the NexusDataWriter can unpack properly
	 */
	@Override
	public NexusTreeProvider readout() throws DeviceException {
		NexusTreeProvider out = readout(framesRead, framesRead)[0];
		if (!tfg.getAttribute("TotalFrames").equals(0)) {
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
			double[][] scalerData = readoutScalerData(numberOfFrames, rawscalerData, null, true);
			for (int frame = 0; frame < numberOfFrames; frame++) {
				NXDetectorData thisFrame = new NXDetectorData(this);
				INexusTree detTree = thisFrame.getDetTree(getName());
				// do not use numberOfDetectors here so all information in the array is added to Nexus (i.e. FF)
				thisFrame.addData(detTree, "scalers", new int[] { numberOfDetectors }, NexusFile.NX_FLOAT64,
						ArrayUtils.subarray(scalerData[frame], 0, numberOfDetectors - 1), "counts", 1);
				thisFrame = addExtraInformationToNexusTree(unpackedScalerData, scalerData, frame, thisFrame, detTree);
				results[frame] = thisFrame;

			}
			return results;
		}

		int[] mcaData = readoutMca(startFrame, numberOfFrames, getCurrentMCASize());
		double[][] scalerData = readoutScalerData(numberOfFrames, rawscalerData, mcaData, true);

		// if ROI readout mode then
		if (xspressParameters.getReadoutMode().equals(XspressDetector.READOUT_ROIS)) {
			Reading[][] readings = getROIs(numberOfFrames, rawscalerData, true, mcaData);

			// loop over frames
			for (int frame = 0; frame < numberOfFrames; frame++) {

				NXDetectorData thisFrame = new NXDetectorData(this);
				INexusTree detTree = thisFrame.getDetTree(getName());

				// for each frame, group MCA rois by name and add to Nexus by each ROI
				HashMap<String, Vector<MCAReading>> mcaROIs = groupMCAROIs(readings[frame]);
				for (String roiName : mcaROIs.keySet()) {
					Vector<MCAReading> readingsInThisROI = mcaROIs.get(roiName);

					if (getNumberofGrades() == 1) {
						double[][] mcaDataInThisROI = new double[readingsInThisROI.size()][];
						int i = 0;
						for (MCAReading thisReading : readingsInThisROI) {
							mcaDataInThisROI[i] = thisReading.mcacounts[0];
							i++;
						}
						thisFrame.addData(detTree, roiName, new int[] { readingsInThisROI.size(), 4096 },
								NexusFile.NX_FLOAT64, mcaDataInThisROI, "counts", 1);
					} else if (getNumberofGrades() == 2) {
						double[][][] mcaDataInThisROI = new double[readingsInThisROI.size()][getNumberofGrades()][];
						int i = 0;
						for (MCAReading thisReading : readingsInThisROI) {
							// data from detector comes out BAD,GOOD, but more intuitive for users to have GOOD,BAD
							mcaDataInThisROI[i][0] = thisReading.mcacounts[1];
							mcaDataInThisROI[i][1] = thisReading.mcacounts[0];
							i++;
						}
						thisFrame.addData(detTree, roiName, new int[] { readingsInThisROI.size(), getNumberofGrades(),
								4096 }, NexusFile.NX_FLOAT64, mcaDataInThisROI, "counts", 1);
					} else {
						double[][][] mcaDataInThisROI = new double[readingsInThisROI.size()][getNumberofGrades()][];
						int i = 0;
						for (MCAReading thisReading : readingsInThisROI) {
							mcaDataInThisROI[i] = flip2DArray(thisReading.mcacounts);
							i++;
						}
						thisFrame.addData(detTree, roiName, new int[] { readingsInThisROI.size(), getNumberofGrades(),
								4096 }, NexusFile.NX_FLOAT64, mcaDataInThisROI, "counts", 1);
					}
				}
				mcaROIs = null;

				// for each frame, group VS rois by name and add to Nexus by each ROI
				HashMap<String, Vector<VSReading>> vsROIs = groupVSROIs(readings[frame]);
				for (String roiName : vsROIs.keySet()) {
					Vector<VSReading> readingsInThisROI = vsROIs.get(roiName);

					if (readingsInThisROI.get(0).counts.length == 1) {
						double[] vsDataInThisROI = new double[readingsInThisROI.size()];
						int i = 0;
						for (VSReading thisReading : readingsInThisROI) {
							vsDataInThisROI[i] = thisReading.counts[0];
							i++;
						}
						thisFrame.addData(detTree, roiName, new int[] { vsDataInThisROI.length }, NexusFile.NX_FLOAT64,
								vsDataInThisROI, "counts", 1);
					} else if (readingsInThisROI.get(0).counts.length == 2) {
						double[][] vsDataInThisROI = new double[readingsInThisROI.size()][2];
						int i = 0;
						for (VSReading thisReading : readingsInThisROI) {
							vsDataInThisROI[i] = thisReading.counts;
							i++;
						}
						thisFrame.addData(detTree, roiName, new int[] { readingsInThisROI.size(), 2 },
								NexusFile.NX_FLOAT64, vsDataInThisROI, "counts", 1);
					} else {
						// all 16 grades
						double[][] vsDataInThisROI = new double[readingsInThisROI.size()][getNumberofGrades()];
						int i = 0;
						for (VSReading thisReading : readingsInThisROI) {
							vsDataInThisROI[i] = thisReading.counts;
							i++;
						}
						thisFrame.addData(detTree, roiName,
								new int[] { readingsInThisROI.size(), getNumberofGrades() }, NexusFile.NX_FLOAT64,
								vsDataInThisROI, "counts", 1);
					}
				}

				thisFrame = addExtraInformationToNexusTree(unpackedScalerData, scalerData, frame, thisFrame, detTree);
				results[frame] = thisFrame;
			}

			return results;
		}

		// else read out full mca deadtime corrected using the hardware scalers
		int[][][][] data = unpackRawDataTo4D(mcaData, numberOfFrames, 1, getCurrentMCASize());

		for (int frame = 0; frame < numberOfFrames; frame++) {
			NXDetectorData thisFrame = new NXDetectorData(this);
			INexusTree detTree = thisFrame.getDetTree(getName());
			double[] deadtimeCorrectionFactors = new double[getNumberOfDetectors()];
			if (!saveRawSpectrum) {
				deadtimeCorrectionFactors = calculateDeadtimeCorrectionFactors(convertUnsignedIntToLong(unpackedScalerData[frame]));
			} else {
				Arrays.fill(deadtimeCorrectionFactors, 1.0);
			}

			double[][][] correctedMCAArrays = correctMCAArrays(data[frame], deadtimeCorrectionFactors);

			double[][] mcasSingleResGrade = null;
			mcasSingleResGrade = removeSingleDimensionFromArray(correctedMCAArrays);
			// add all MCA data in bulk
			thisFrame.addData(detTree, "MCAs", new int[] { numberOfDetectors, 4096 }, NexusFile.NX_FLOAT64,
					mcasSingleResGrade, "counts", 1);
			// add all in-window scaler counts in bulk
			thisFrame.addData(detTree, "scalers", new int[] { numberOfDetectors }, NexusFile.NX_FLOAT64,
					scalerData[frame], "counts", 1);

			// optionally create a sum of all MCAs together
			if (sumAllElementData) {
				double[] summation = new double[correctedMCAArrays[0][0].length];
				for (int element = 0; element < numberOfDetectors; element++) {
					double[][] out = correctedMCAArrays[element];
					for (int i = 0; i < out[0].length; i++) {
						summation[i] += out[0][i];
					}
				}
				thisFrame.addData(detTree, "allElementSum", new int[] { 4096 }, NexusFile.NX_FLOAT64, summation,
						"counts", 1);
			}

			thisFrame = addExtraInformationToNexusTree(unpackedScalerData, scalerData, frame, thisFrame, detTree);
			results[frame] = thisFrame;
		}
		return results;
	}

	private HashMap<String, Vector<MCAReading>> groupMCAROIs(Reading[] readings) {
		HashMap<String, Vector<MCAReading>> groupedROIs = new HashMap<String, Vector<MCAReading>>();

		for (int reading = 0; reading < readings.length; reading++) {
			if (readings[reading] instanceof MCAReading) {
				String thisROIName = readings[reading].getRoiName();
				MCAReading thisReading = (MCAReading) readings[reading];
				if (!groupedROIs.containsKey(thisROIName)) {
					Vector<MCAReading> vectorOfReadings = new Vector<MCAReading>();
					vectorOfReadings.add(thisReading.elementNumber, thisReading);
					groupedROIs.put(thisROIName, vectorOfReadings);
				} else {
					Vector<MCAReading> vectorOfReadings = groupedROIs.get(thisROIName);
					vectorOfReadings.add(thisReading.elementNumber, thisReading);
				}
			}
		}
		return groupedROIs;
	}

	private HashMap<String, Vector<VSReading>> groupVSROIs(Reading[] readings) {
		HashMap<String, Vector<VSReading>> groupedROIs = new HashMap<String, Vector<VSReading>>();

		for (int reading = 0; reading < readings.length; reading++) {
			if (readings[reading] instanceof VSReading) {
				String thisROIName = readings[reading].getRoiName();
				VSReading thisReading = (VSReading) readings[reading];
				if (!groupedROIs.containsKey(thisROIName)) {
					Vector<VSReading> vectorOfReadings = new Vector<VSReading>();
					vectorOfReadings.add(thisReading.elementNumber, thisReading);
					groupedROIs.put(thisROIName, vectorOfReadings);
				} else {
					Vector<VSReading> vectorOfReadings = groupedROIs.get(thisROIName);
					vectorOfReadings.add(thisReading.elementNumber, thisReading);
				}
			}
		}
		return groupedROIs;
	}

	protected NXDetectorData addExtraInformationToNexusTree(int[][] unpackedScalerData, double[][] scalerData,
			int frame, NXDetectorData thisFrame, INexusTree detTree) {
		thisFrame = addFFIfPossible(detTree, thisFrame, scalerData[frame]);
		thisFrame = fillNXDetectorDataWithScalerData(thisFrame, scalerData[frame], unpackedScalerData[frame]);
		thisFrame = addDTValuesToNXDetectorData(thisFrame, unpackedScalerData[frame]);
		return thisFrame;
	}

	/**
	 * When only one res garde, removes the single dimension to convert [element][resGrade][mca] into a true 2D array of
	 * [element][mca]
	 * 
	 * @param correctedMCAArrays
	 * @return double[][]
	 */
	private double[][] removeSingleDimensionFromArray(double[][][] correctedMCAArrays) {
		double[][] out = new double[correctedMCAArrays.length][correctedMCAArrays[0][0].length];

		for (int element = 0; element < correctedMCAArrays.length; element++) {
			for (int mcaChannel = 0; mcaChannel < correctedMCAArrays[0][0].length; mcaChannel++) {
				out[element][mcaChannel] = correctedMCAArrays[element][0][mcaChannel];
			}
		}
		return out;
	}

//	/**
//	 * When only one res garde, removes the single dimension to convert [element][resGrade][mca] into a true 2D array of
//	 * [element][mca]
//	 * 
//	 * @param correctedMCAArrays
//	 * @return double[][]
//	 */
//	private double[][] removeSingleDimensionFromArray(int[][][] correctedMCAArrays) {
//		double[][] out = new double[correctedMCAArrays.length][correctedMCAArrays[0][0].length];
//
//		for (int element = 0; element < correctedMCAArrays.length; element++) {
//			for (int mcaChannel = 0; mcaChannel < correctedMCAArrays[0][0].length; mcaChannel++) {
//				out[element][mcaChannel] = correctedMCAArrays[element][0][mcaChannel];
//			}
//		}
//		return out;
//	}

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

		if (mcaGrades == RES_THRES) {
			int ffBadColumn = elementNames.indexOf("FF_bad");
			if (ffBadColumn > -1) {
				thisFrame.addData(detTree, "FF_bad", new int[] { 1 }, NexusFile.NX_FLOAT64,
						new double[] { ds[ffBadColumn] }, "counts", 1);
			}
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
	 * Flips the dimensions of an array, so if it was 2 by 4096 it is now 4096 by 2
	 * 
	 * @param array
	 * @return double[][]
	 */
	private double[][] flip2DArray(double[][] array) {

		int firstDim = array.length; // 2
		int secondDim = array[0].length; // 4096

		double[][] newArray = new double[secondDim][firstDim];

		for (int i = 0; i < secondDim; i++) {
			for (int j = 0; j < firstDim; j++) {
				newArray[i][j] = array[j][i];
			}
		}

		return newArray;
	}

	/**
	 * Correct an array of [element][resGrade][mca] for deadtime where there is a single deadtime for each element which
	 * is used on every resGrade.
	 * 
	 * @param arrays
	 * @param deadtimeFactors
	 * @return double[][][]
	 */
	private double[][][] correctMCAArrays(int[][][] arrays, double[] deadtimeFactors) {
		double[][][] correctedValues = new double[arrays.length][][];
		for (int element = 0; element < arrays.length; element++) {
			correctedValues[element] = correctMCAArray(arrays[element], deadtimeFactors[element]);
		}
		return correctedValues;
	}

	private double[][] correctMCAArray(int[][] array, double deadtimeFactor) {
		double[][] out = new double[array.length][];
		for (int i = 0; i < array.length; i++) {
			out[i] = correctScalerArray(array[i], deadtimeFactor);
		}
		return out;
	}

	private double[] correctScalerArray(int[] array, double deadtimeFactor) {
		double[] out = new double[array.length];
		for (int i = 0; i < array.length; i++) {
			out[i] = array[i] * deadtimeFactor;
		}
		return out;
	}

	@Override
	public int[] getRawScalerData() throws DeviceException {
		return readoutHardwareScalers(0, 1);
	}

	/**
	 * Dead time corrected scaler data. Used by TfgXspress2 class which acts as an adapter for this class so that it can
	 * act like a counterTimer.
	 * 
	 * @return an array of doubles of dead time corrected 'in window' counts and the sum of all the dead time corrected
	 *         data.
	 * @throws DeviceException
	 */
	@Override
	public double[] readoutScalerData() throws DeviceException {
		if (tfg.getAttribute("TotalFrames").equals(0)) {
			return readoutScalerData(0, 0, true)[0];
		}
		return readoutScalerData(framesRead, framesRead, true)[0];
	}

	public double[] readoutScalerDataNoCorrection() throws DeviceException {
		if (tfg.getAttribute("TotalFrames").equals(0)) {
			return readoutScalerData(0, 0, false)[0];
		}
		return readoutScalerData(framesRead, framesRead, false)[0];
	}

	private double[][] readoutScalerData(int startFrame, int finalFrame, boolean performCorrections)
			throws DeviceException {
		int numberOfFrames = finalFrame - startFrame + 1;
		int[] mcaData = readoutMca(startFrame, numberOfFrames, getCurrentMCASize());
		int[] rawscalerData = readoutHardwareScalers(startFrame, numberOfFrames);
		return readoutScalerData(numberOfFrames, rawscalerData, mcaData, performCorrections);
	}

	/*
	 * Basically what goes into the Ascii file. Columns should match the values from getExtraNames() or
	 * getUnFilteredChannelLabels()
	 */
	private double[][] readoutScalerData(int numFrames, int[] rawScalerData, int[] mcaData, boolean performCorrections) {

		double[][] scalerData = new double[numFrames][];

		Double I0 = null;
		if (mcaGrades == ALL_RES && ionChambersCounterTimer != null) {
			try {
				I0 = ionChambersCounterTimer.readout()[0];
			} catch (DeviceException e) {
				logger.error("Exception while trying to fetch I0 to normalise scalers for each res grade", e);
			}
		}

		// if ROI readout mode then get the virtual scalers
		if (xspressParameters.getReadoutMode().equals(XspressDetector.READOUT_ROIS)) {

			Reading[][] readings = getROIs(numFrames, rawScalerData, true, mcaData);
			Reading[][] readingsUncorrected = null;
			if (mcaGrades == ALL_RES) {
				readingsUncorrected = getROIs(numFrames, null, false, mcaData);
			}

			for (int frame = 0; frame < numFrames; frame++) {
				scalerData[frame] = new double[0];
				// when ALL_RES then make a sum of each resGrade over all the elements/ROIs in each resGrade bin i.e.
				// 15, 15+14...; add to this array the sum of best 8 res grades, not corrected for DT
				if (mcaGrades == ALL_RES) {
					scalerData[frame] = new double[16 + getNumberOfDetectors()];
				}
				double ff = 0;
				double ff_bad = 0;

				for (int vs = 0; vs < readings[frame].length; vs++) {
					if (readings[frame][vs] instanceof VSReading) {
						VSReading vsreading = (VSReading) readings[frame][vs];
						if (mcaGrades != ALL_RES && vsreading.contributesToFF) {
							scalerData[frame] = ArrayUtils.addAll(scalerData[frame], vsreading.counts);
						}
						switch (mcaGrades) {
						case NO_RES_GRADE:
							if (vsreading.contributesToFF) {
								ff += vsreading.counts[0];
							}
							break;
						case RES_THRES:
							if (vsreading.contributesToFF) {
								ff += vsreading.counts[0];
							} else if (vsreading.getRoiName().contains("bad")
									&& !vsreading.getRoiName().contains("OUT")) {
								ff_bad += vsreading.counts[0];
							}
							break;
						case ALL_RES:
							// sum of resGrade bins over all ROIs
							for (int resGrade = 0; resGrade < 16; resGrade++) {
								for (int outBin = 0; outBin < 16; outBin++) {
									if (15 - outBin <= resGrade) {
										scalerData[frame][outBin] += vsreading.counts[resGrade];
									}
								}
							}

							// if a normalisation is possible, then normalise all to I0
							if (I0 != null && !I0.isNaN() && !I0.isInfinite() && I0 > 0) {
								for (int outBin = 0; outBin < 16; outBin++) {
									scalerData[frame][outBin] /= I0;
								}
							}

							// best 8 resGrades for each element, but not corrected for deadtime
							if (readingsUncorrected != null) {
								scalerData[frame][vsreading.getElementNumber() + 16] = ((VSReading) readingsUncorrected[frame][vs]).counts[7];
							}
							if (vsreading.contributesToFF) {
								ff += vsreading.counts[7];
							}
							break;
						}
					} else {
						MCAReading mcareading = (MCAReading) readings[frame][vs];
						if (mcaGrades != ALL_RES) {
							scalerData[frame] = ArrayUtils.add(scalerData[frame], mcareading.peakArea);
							ff += mcareading.peakArea;
							ff_bad += mcareading.peakArea_bad; // only really relevant when mcaGrades == RES_THRES
						} else {
							// sum of resGrade bins over all ROIs
							for (int resGrade = 0; resGrade < 16; resGrade++) {
								double sumCounts = sumPartialMCACounts(mcareading.mcacounts[resGrade]);
								for (int outBin = 0; outBin < 16; outBin++) {
									if (15 - outBin <= resGrade) {
										scalerData[frame][outBin] += sumCounts;
									}
								}
							}

							// if a normalisation is possible, then normalise all to I0
							if (I0 != null && !I0.isNaN() && !I0.isInfinite() && I0 > 0) {
								for (int outBin = 0; outBin < 16; outBin++) {
									scalerData[frame][outBin] /= I0;
								}
							}

							// best 8 resGrades for each element, but not corrected for deadtime
							if (readingsUncorrected != null) {
								scalerData[frame][mcareading.getElementNumber() + 16] = ((MCAReading) readingsUncorrected[frame][vs]).peakArea;
							}
							ff += mcareading.peakArea;

						}
					}
				}

				// append the sum (FF) to the array
				scalerData[frame] = ArrayUtils.add(scalerData[frame], ff);

				if (mcaGrades == RES_THRES) {
					scalerData[frame] = ArrayUtils.add(scalerData[frame], ff_bad);
				}
			}

		} else {
			// else return hardware scalers using the win values from the hardwareData array
			int[][] unpackedScalerData = unpackRawScalerDataToFrames(rawScalerData, numFrames);

			for (int frame = 0; frame < numFrames; frame++) {

				double[] deadtimeCorrectionFactors = new double[getNumberOfDetectors()];
				if (performCorrections && !saveRawSpectrum) {
					deadtimeCorrectionFactors = calculateDeadtimeCorrectionFactors(convertUnsignedIntToLong(unpackedScalerData[frame]));
				} else {
					Arrays.fill(deadtimeCorrectionFactors, 1.0);
				}

				scalerData[frame] = new double[numberOfDetectors];
				int counter = 2;
				for (int element = 0; element < numberOfDetectors; element++) {
					scalerData[frame][element] = unpackedScalerData[frame][counter]
							* deadtimeCorrectionFactors[element];
					counter += 4;
				}

				double ff = 0;
				for (double value : scalerData[frame]) {
					ff += value;
				}
				scalerData[frame] = ArrayUtils.add(scalerData[frame], ff);

			}
		}

		return scalerData;
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
						* mcaGrades + " " + numberOfFrames + " from " + mcaHandle + " raw motorola", numberOfDetectors
						* mcaGrades * mcaSize * numberOfFrames);
			} catch (Exception e) {
				throw new DeviceException(e.getMessage(),e);
			}
		}
		return value;
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

	@Override
	public void reconfigure() throws FactoryException {
		// A real system needs a connection to a real da.server via a DAServer object.
		logger.debug("Xspress2System.reconfigure(): reconnecting to: " + daServerName);
		try {
			daServer.reconnect();

			// does not reconfigure the tfg -- need to check if it is needed
			// If everything has been found send the open commands.
			if (tfg != null && (daServer != null)) {
				open();
			}
		} catch (DeviceException e) {
			throw new FactoryException(e.getMessage(), e);
		}
	}

	@Override
	public void collectData() throws DeviceException {

		if (!daServer.isConnected()) {
			daServer.connect();
		}

		// if tfg not running with frames then clear and start the xspress memory
		if (tfg.getAttribute("TotalFrames").equals(0)) {
			clear();
			start();
		}
	}

	@Override
	public int getStatus() throws DeviceException {
		return tfg.getStatus();
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "Xspress system version 2";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "Version 2";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "Xspress2System";
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

	private synchronized void sendCommand(String command, int handle) throws DeviceException {
		Object obj;
		if ((obj = daServer.sendCommand(command + handle)) == null) {
			throw new DeviceException("Null reply received from daserver during " + command);
		} else if (((Integer) obj).intValue() == -1) {
			logger.error(getName() + ": " + command + " failed");
			close();
			throw new DeviceException("Xspress2System " + getName() + " " + command + " failed");
		}
	}

	public long[] convertUnsignedIntToLong(int[] data) {
		long[] convertedData = new long[data.length];
		for (int i = 0; i < data.length; i++) {
			long value = data[i];
			// Now remove the sign extension caused by casting from int to long as the data
			// from the detector is really 32bit unsigned, but Java's int is 32bit signed!!
			if (value < 0)
				value = (value << 32) >>> 32;
			convertedData[i] = value;
		}
		return convertedData;
	}

	/**
	 * Given an array of the hardwareScalerReadings (4 values per element) for a given frame, this calculates the
	 * deadtime correction factor for each element.
	 * 
	 * @param hardwareScalerReadings
	 * @return double[]
	 */
	public double[] calculateDeadtimeCorrectionFactors(long[] hardwareScalerReadings) {
		// assumes in order element0all, element0reset, element0counts, element0time, element1all etc.

		double dataout[] = new double[(numberOfDetectors)];
		int k = 0;
		int l = 0;
		long all;
		long reset;
		// long win; // not used
		long time;
		time = 0;
		final List<DetectorElement> detectors = xspressParameters != null ? xspressParameters.getDetectorList() : null;
		final List<DetectorDeadTimeElement> detectorsDte = xspressDeadTimeParameters != null ? xspressDeadTimeParameters
				.getDetectorDeadTimeElementList() : null;
		DetectorDeadTimeElement detectorDte = null;
		int index = 0;
		if (detectors != null && detectorsDte != null)
			for (DetectorElement detector : detectors) {
				detectorDte = detectorsDte.get(index++);
				if (detector.isExcluded()) {
					k += 4;
					dataout[l] = 0.0;
				} else {
					all = hardwareScalerReadings[k++]; // total number of events
					reset = hardwareScalerReadings[k++]; // TFG reset counts
					// win = hardwareScalerReadings[k++]; // in window events. Not used in this calculation
					k++; // ignore win but move on k anyway
					time = hardwareScalerReadings[k++]; // TFG clock counts
					Double processDeadTimeAllEvent = calculateDetectorProcessDeadTimeAllEvent(detectorDte);
					Double processDeadTimeInWindowEvent = calculateDetectorProcessDeadTimeInWindowEvent(detectorDte);
					Double factor = dtc(all, reset, time, processDeadTimeAllEvent,processDeadTimeInWindowEvent);
					// need a sensible number if there were zeroes in the reading
					if (factor.isNaN() || factor.isInfinite()) {
						factor = 1.0;
					}
					dataout[l] = factor;
				}
				l++;
			}
		return dataout;
	}

	private double calculateDetectorProcessDeadTimeAllEvent(DetectorDeadTimeElement detectorDte) {
		Double grad = detectorDte.getProcessDeadTimeAllEventGradient();

		if (grad == null || grad == 0.0 || this.deadtimeEnergy == null || this.deadtimeEnergy == 0.0) {
			return detectorDte.getProcessDeadTimeAllEventOffset();
		}

		return detectorDte.getProcessDeadTimeAllEventOffset() + grad * this.deadtimeEnergy;
	}
	
	private double calculateDetectorProcessDeadTimeInWindowEvent(DetectorDeadTimeElement detectorDte) {
		Double grad = detectorDte.getProcessDeadTimeInWindowGradient();

		if (grad == null || grad == 0.0 || this.deadtimeEnergy == null || this.deadtimeEnergy == 0.0) {
			return detectorDte.getProcessDeadTimeInWindow();
		}

		return detectorDte.getProcessDeadTimeInWindow() + grad * this.deadtimeEnergy;
	}

	/*
	 * Documentation from William Helsby is available to explain the maths in this method
	 * @param all
	 * @param reset
	 * @param time
	 * @param processDeadTimeAllEvent
	 * @param processDeadTimeInWindow
	 * @return dead time correction factor for non piled up events
	 */
	private double dtc(long all, long reset, long time, double processDeadTimeAllEvent, double processDeadTimeInWindow) {
		final double clockRate = 12.5e-09;
		// Calculate the reset tick corrected measured count rate
		double dt = (time - reset) * clockRate;
		double measuredRate = all / dt;
		// calculate the input corrected count rate
		double corrected = CorrectionUtils.correct(measuredRate, processDeadTimeAllEvent);
		// calculate dead time correction factor to be applied to the in-window scaler
		double factor = time * clockRate / dt;
		factor *= 1 / Math.exp(-corrected * 2 * processDeadTimeInWindow);
		return factor;
	}

	/**
	 * Convert the raw 1D array from DAServer into the 4D data it represents.
	 * <p>
	 * Assumes the packing order: frame,channel,res-grade,energy
	 * 
	 * @param rawData
	 * @param numFrames
	 * @param numResGrades
	 * @return int[][][][]
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

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		if (attributeName.equals("liveStats")) {
			return calculateLiveStats();
		} else if (attributeName.equals(ONLY_DISPLAY_FF_ATTR)) {
			return onlyDisplayFF;
		}
		return null;
	}

	/**
	 * @return double[] - for every element, return the total count rate, deadtime correction factor and in-window count
	 *         rate
	 * @throws DeviceException
	 */
	private Object calculateLiveStats() throws DeviceException {
		int[] rawData = getRawScalerData();
		long[] rawDataLong = convertUnsignedIntToLong(rawData);
		// TODO should saveRawSpectrum flag be checked here???
		double[] dtcs = calculateDeadtimeCorrectionFactors(rawDataLong);
		Double[] results = new Double[3 * this.getNumberOfDetectors()];

		for (int element = 0; element < this.getNumberOfDetectors(); element++) {

			// count rate
			int allCounts = rawData[element * 4];
			int reset = rawData[element * 4 + 1];
			int counts = rawData[element * 4 + 2];
			int time = rawData[element * 4 + 3];
			final double clockRate = 12.5e-09;
			Double dt = (time - reset) * clockRate;
			Double measuredRate = allCounts / dt;

			if (measuredRate.isNaN() || measuredRate.isInfinite()) {
				results[element * 3] = 0.0;
				results[element * 3 + 1] = 0.0;
				results[element * 3 + 2] = 0.0;
			} else {
				results[element * 3] = measuredRate;
				results[element * 3 + 1] = dtcs[element];
				results[element * 3 + 2] = counts / dt;
			}
		}

		return results;

	}

	/**
	 * Can be used to create an XspressParameters xml file. Useful when new 64 element files are required!
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(final String[] args) throws Exception {

		final String fileName = args[0];
		final int numEle = Integer.parseInt(args[1]);

		XspressParameters xspressParameters = new XspressParameters();
		xspressParameters.setResGrade("res-thres 1.0");
		xspressParameters.setDetectorName("xspress2system");
		xspressParameters.setReadoutMode("Scalers only");
		XspressDeadTimeParameters xspressDeadTimeParameters = new XspressDeadTimeParameters();
		for (int i = 1; i <= numEle; i++) {
			xspressParameters.addDetectorElement(new DetectorElement("Element " + i, i, 0, 4095, false,
					new ArrayList<XspressROI>()));
			xspressDeadTimeParameters.addDetectorDeadTimeElement(new DetectorDeadTimeElement("Element " + i, i,
					2.5304E-9, 2.2534E-7, 2.5454E-7));
		}

		XMLHelpers.writeToXML(XspressParameters.mappingURL, xspressParameters, fileName);

		System.out.println("Created file " + fileName);
	}

	public void setSumAllElementData(boolean sumAllElementData) {
		this.sumAllElementData = sumAllElementData;
	}

	public boolean isSumAllElementData() {
		return sumAllElementData;
	}

	public Boolean getSaveRawSpectrum() {
		return saveRawSpectrum;
	}

	public void setSaveRawSpectrum(Boolean saveRawSpectrum) {
		this.saveRawSpectrum = saveRawSpectrum;
	}
	
	public int getNumberFrames() throws DeviceException {

		if (tfg.getAttribute("TotalFrames").equals(0)) {
			return 0;
		}

		String[] cmds = new String[] { "status show-armed", "progress", "status", "full", "lap", "frame" };
		HashMap<String, String> currentVals = new HashMap<String, String>();
		for (String cmd : cmds) {
			currentVals.put(cmd, runDAServerCommand("tfg read " + cmd).toString());
			logger.info("tfg read " + cmd + ": " + currentVals.get(cmd));
		}

		if (currentVals.isEmpty()) {
			return 0;
		}

		// else either scan not started (return -1) or has finished (return continuousParameters.getNumberDataPoints())

		// if started but nothing collected yet
		if (currentVals.get("status show-armed").equals("EXT-ARMED") /* && currentVals.get("status").equals("IDLE") */) {
			return 0;
		}

		// if frame is non-0 then work out the current frame
		if (!currentVals.get("frame").equals("0")) {
			String numFrames = currentVals.get("frame");
			return extractCurrentFrame(Integer.parseInt(numFrames));
		}

		return Integer.parseInt(tfg.getAttribute("TotalFrames").toString());
	}

	private int extractCurrentFrame(int frameValue) {
		if (isEven(frameValue)) {
			Integer numFrames = frameValue / 2;
			return numFrames;
		}
		Integer numFrames = (frameValue - 1) / 2;
		return numFrames;
	}

	private boolean isEven(int x) {
		return (x % 2) == 0;
	}

	private Object runDAServerCommand(String command) throws DeviceException {
		Object obj = null;
		if (getDaServer() != null && getDaServer().isConnected()) {
			if ((obj = getDaServer().sendCommand(command)) == null) {
				throw new DeviceException("Null reply received from daserver during " + command);
			}
			return obj;
		}
		return null;
	}


}
