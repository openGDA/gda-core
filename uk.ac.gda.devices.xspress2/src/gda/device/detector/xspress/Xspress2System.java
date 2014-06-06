/*-
 * Copyright © 2014 Diamond Light Source Ltd., Science and Technology
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
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.Timer;
import gda.device.detector.DAServer;
import gda.device.detector.DetectorBase;
import gda.device.detector.NexusDetector;
import gda.device.detector.countertimer.TfgScaler;
import gda.device.detector.xspress.xspress2data.ResGrades;
import gda.device.detector.xspress.xspress2data.Xspress2CurrentSettings;
import gda.device.detector.xspress.xspress2data.Xspress2SystemData;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Finder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.xspress.DetectorDeadTimeElement;
import uk.ac.gda.beans.xspress.DetectorElement;
import uk.ac.gda.beans.xspress.XspressDeadTimeParameters;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.beans.xspress.XspressROI;
import uk.ac.gda.util.beans.xml.XMLHelpers;

/**
 * Represents a set of Xspress2 boards and detectors. It actually communicates
 * with a DAServer object which is connected to a da.server process running on a
 * MVME computer.
 * <p>
 * This returns data as a Nexus tree from its readout method.
 * <p>
 * As this acts differently from the TFGv1 classes, some of the Xpress interface
 * methods may not be implemented. This needs resolving at some point.
 * <p>
 * Deadtime correction methods: none thres all hw sca hw - - hw sca+mca hw - -
 * roi hw new hw hw = apply deadtime factor from hardware scalers only new =
 * scale both types of ROI using total counts / counts in rois This needs
 * refactoring so that roi when all are selected are also corrected.
 */
public class Xspress2System extends DetectorBase implements NexusDetector, XspressDetector, Scannable, Configurable {

	private static final Logger logger = LoggerFactory.getLogger(Xspress2System.class);

	public static final int NO_RES_GRADE = 1;
	public static final int RES_THRES = 2;
	public static final int ALL_RES = 16;
	public static final String ONLY_DISPLAY_FF_ATTR = "ff_only";
	public static final String ADD_DT_VALUES_ATTR = "add_dt_values";

	// These are used to calculate the size of the data
//	private int fullMCABits = 12;
//	private int mcaGrades = 1; // reset during every open()
	private Integer maxNumberOfFrames = 0; // the number of frames which TFG has
											// space for, based on the current
											// config in TFG

	// These are the objects this must know about.
	private String daServerName;
	protected DAServer daServer = null;
	private String tfgName;
	protected Timer tfg = null;

	// Values used in DAServer commands
	private String mcaOpenCommand = null;
	private String scalerOpenCommand = null;
	private String startupScript = null;
//	protected int numberOfDetectors;
	protected final int numberOfScalers = 4; // number of values from each hardware scaler (e.g. total, resets, originalWindowed, time)
	private int mcaHandle = -1;
	private int scalerHandle = -1;
	private String xspressSystemName;

	// Full path to config file
	private String configFileName = null;
//	private Double deadtimeEnergy = null; // in keV NOT eV!
	protected int lastFrameCollected = 0;
	// mode override property, when set to true the xspress is always set in
	// SCAlers and MCA Mode
	// does not change with the value in the parameters file, no rois are set
	private boolean modeOverride = LocalProperties.check("gda.xspress.mode.override");
	// this is only when using resgrades, when resgrades separated and 'extra' ascii columns are requested
	private TfgScaler ionChambersCounterTimer = null;
	private String dtcConfigFileName;
	private Xspress2SystemData xspress2SystemData;
	private Xspress2CurrentSettings settings;


	public Xspress2System() {
		this.inputNames = new String[] {};
//		updateSettings();
		settings = new Xspress2CurrentSettings();
		xspress2SystemData = new Xspress2SystemData(getName(),settings);
	}
	
//	private void updateSettings() {
//		ArrayList<String> filteredChannels = getChannelLabels();
//		getChannelLabels(filteredChannels, true);
//		detectorSettings = new Xspress2CurrentSettings(getChannelLabels(), filteredChannels,
//				getExtraNames(), getOutputFormat(), getDetectorList(), mcaGrades, getCurrentMCASize(),
//				xspressParameters, deadtimeEnergy);
//	}

	@Override
	public void configure() throws FactoryException {
		// A real system needs a connection to a real da.server via a DAServer
		// object.
		if (daServer == null) {
			logger.debug("Xspress2System.configure(): finding: " + daServerName);
			if ((daServer = (DAServer) Finder.getInstance().find(daServerName)) == null)
				logger.error("Xspress2System.configure(): Server " + daServerName + " not found");
		}
		// Both dummy and real systems should have a tfg
		if (tfg == null) {
			logger.debug("Xspress2System.configure(): finding " + tfgName);
			if ((tfg = (Timer) Finder.getInstance().find(tfgName)) == null)
				logger.error("Xspress2System.configure(): TimeFrameGenerator " + tfgName + " not found");
		}
		try {
			loadAndInitializeDetectors(configFileName, dtcConfigFileName);
		} catch (Exception e) {
			logger.error(
					"Error loading template XML. Will use a detector with a few default elements instead. "
							+ e.getMessage(), e);
			useDefaultXspressParameters();
		}

		if (!ResGrades.isResGrade(settings.getParameters().getResGrade()))
			throw new FactoryException("resGrade " + settings.getParameters().getResGrade() + " is not an acceptable string");

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
//		updateSettings();
		configured = true;
	}

	private void useDefaultXspressParameters() {
		ArrayList<XspressROI> regions = new ArrayList<XspressROI>();
		XspressParameters xspressParameters = new XspressParameters();
		XspressDeadTimeParameters xspressDeadTimeParameters = new XspressDeadTimeParameters();
		if (modeOverride)
			xspressParameters.setReadoutMode(READOUT_MCA);
		else
			xspressParameters.setReadoutMode(READOUT_SCALERONLY);
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
		settings.setXspressParameters(xspressParameters);
	}

	private void configureDetectorFromParameters() throws DeviceException {
		// always remove all rois first
		if (modeOverride)
			settings.getParameters().setReadoutMode(READOUT_MCA);
		else
			doRemoveROIs();
		
		for (DetectorElement detector : settings.getDetectorElements()) {
			doSetWindowsCommand(detector);
			if (settings.getParameters().getReadoutMode().equals(XspressDetector.READOUT_ROIS))
				doSetROICommand(detector);
		}
	}

	@Override
	public Double getDeadtimeCalculationEnergy() throws DeviceException {
		return settings.getDeadtimeEnergy();
	}

	@Override
	public void setDeadtimeCalculationEnergy(Double energy) throws DeviceException {
		settings.setDeadtimeEnergy(energy);
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

	/**
	 * This is all detectors (elements), both included and excluded.
	 */
	@Override
	public int getNumberOfDetectors() {
		return settings.getNumberOfDetectors();
	}

	/**
	 * The number of different versions of mca/roi data read out. This varies
	 * depending on the resolution mode in use: 1 if no res threshold, 2 if a
	 * threshold set and 16 if no threshold but all res grades to be read out
	 * separately.
	 * 
	 * @return The number of resolution grades
	 */
	@Override
	public int getNumberofGrades() {
		return settings.getMcaGrades();
	}

	/**
	 * This is the string used by DAServer to set the mode of the resolution
	 * grade.
	 * 
	 * @return Returns the resGrade.
	 */
	@Override
	public String getResGrade() {
		return settings.getParameters().getResGrade();
	}

	/**
	 * @param resGrade
	 *            The resGradeto set, if set to "res-thres" an additional float
	 *            value in the range 0.0 to 16.0 is required.
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
		settings.getParameters().setResGrade(resGrade);
		if (configured) {
			close();
			doFormatRunCommand(numberOfBits);
			open();
		}
	}

	/**
	 * Sets a region of interest. This is used when the readout mode is set to
	 * ROI. Each region of interest may be a partial mca or a virtual scaler
	 * (sum of counts in that region).
	 * 
	 * @param detector
	 * @param regionList
	 * @throws DeviceException
	 */
	public void setRegionOfInterest(int detector, ArrayList<XspressROI> regionList) throws DeviceException {
		DetectorElement detectorElement = settings.getParameters().getDetector(detector);
		detectorElement.setRegionList(regionList);
		if (configured)
			doSetROICommand(detectorElement);
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
		DetectorElement detectorElement = settings.getParameters().getDetector(detector);
		detectorElement.setWindow(windowStart, windowEnd);
		if (configured)
			doSetWindowsCommand(detectorElement);
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
		if (!daServer.isConnected())
			daServer.connect();
		// if this class was used to define framesets, then memeory is only
		// cleared at the start of the scan
		if (!tfg.getAttribute("TotalFrames").equals(0)) {
			stop();
			clear();
			start();
		}
		lastFrameCollected = -1;
	}

	@Override
	public void atScanStart() throws DeviceException {
		if (!daServer.isConnected())
			daServer.connect();
		stop();
		clear();
		start();
		lastFrameCollected = -1;
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		super.atCommandFailure();
		lastFrameCollected = -1;
	}

	@Override
	public String[] getOutputFormat() {
		return settings.getOutputFormat();
	}

	@Override
	public void setOutputFormat(String[] names) {
		settings.setDefaultOutputFormat(names);
	}

	@Override
	public String[] getExtraNames() {
		return settings.getExtraNames();
	}

	@Override
	public ArrayList<String> getChannelLabels() {
		return settings.getChannelLabels();
	}

	/**
	 * Sets the da.server command which should be used to open the mca
	 * connection.
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
	 * Sets the da.server command which should be used to open the scaler
	 * connection.
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
		return settings.isAddDTScalerValuesToAscii();
	}

	public void setAddDTScalerValuesToAscii(Boolean addDTScalerValuesToAscii) {
		settings.setAddDTScalerValuesToAscii(addDTScalerValuesToAscii);
	}

	/**
	 * @return Returns the bit size of a full mca (12 = 4096)
	 */
	public int getFullMCABits() {
		return settings.getFullMCABits();
	}

	public int getFullMCASize() {
		return settings.getFullMCASize();
	}

	/**
	 * @param fullMCABits
	 * @throws DeviceException
	 */
	public void setFullMCABits(int fullMCABits) throws DeviceException {
		settings.setFullMCABits(fullMCABits);
		if (configured) {
			close();
			doFormatRunCommand(determineNumberOfBits());
			open();
		}
	}

	/**
	 * Readout mode refers to the nature of the data this returns in its readout
	 * and readoutScalers methods. This is not a setting in DAServer.
	 */
	@Override
	public String getReadoutMode() throws DeviceException {
		return settings.getParameters().getReadoutMode();
	}

	@Override
	public void setReadoutMode(String readoutMode) throws DeviceException {
		if (modeOverride && !readoutMode.equals(settings.getParameters().getReadoutMode())) {
			settings.getParameters().setReadoutMode(XspressDetector.READOUT_MCA);
			configureDetectorFromParameters();
		} else if ((readoutMode.equals(XspressDetector.READOUT_SCALERONLY)
				|| readoutMode.equals(XspressDetector.READOUT_MCA) || readoutMode.equals(XspressDetector.READOUT_ROIS))
				&& !readoutMode.equals(settings.getParameters().getReadoutMode())) {
			settings.getParameters().setReadoutMode(readoutMode);
			configureDetectorFromParameters();
		}
	}

	public boolean isOnlyDisplayFF() {
		return settings.getParameters().isOnlyShowFF();
	}

	public void setOnlyDisplayFF(boolean onlyDisplayFF) {
		settings.getParameters().setOnlyShowFF(onlyDisplayFF);
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
			setOnlyDisplayFF(ffonly);
		} else
			super.setAttribute(attribute, value);
	}

	/**
	 * @param which
	 * @return true if detectorElement is excluded.
	 */
	public boolean isDetectorExcluded(int which) {
		return settings.getParameters().getDetector(which).isExcluded();
	}

	public void setDetectorExcluded(int which, boolean excluded) {
		settings.getParameters().getDetector(which).setExcluded(excluded);
	}

	public List<DetectorElement> getDetectorList() {
		return settings.getParameters().getDetectorList();
	}

	/**
	 * @return the maximum number of time frames possible based on the result of
	 *         the last format-run command. This will be 0 when using
	 *         DummyDaServer.
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

	public boolean isAlwaysRecordRawMCAs() {
		return xspress2SystemData.isAlwaysRecordRawMCAs();
	}

	public void setAlwaysRecordRawMCAs(boolean alwaysRecordRawMCAs) {
		xspress2SystemData.setAlwaysRecordRawMCAs(alwaysRecordRawMCAs);
	}

	/**
	 * Sends the daServer commands to clear the xspress system. Note that this
	 * is very time consuming and should only be done when necessary.
	 * 
	 * @throws DeviceException
	 */
	@Override
	public void clear() throws DeviceException {
		if (mcaHandle < 0 || scalerHandle < 0)
			open();
		if (mcaHandle >= 0 && daServer != null && daServer.isConnected())
			sendCommand("clear ", mcaHandle);
		if (scalerHandle >= 0 && daServer != null && daServer.isConnected())
			sendCommand("clear ", scalerHandle);
	}

	/**
	 * Sends the daServer commands to enable the xspress system counting. This
	 * does not start the TFG counting.
	 * 
	 * @see gda.device.detector.xspress.XspressDetector#start()
	 */
	@Override
	public void start() throws DeviceException {
		if (mcaHandle < 0 || scalerHandle < 0)
			open();
		if (mcaHandle >= 0 && daServer != null && daServer.isConnected()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				logger.error("Error sleeping for 100ms", e);
			}
			sendCommand("enable ", mcaHandle);
		}
		if (scalerHandle >= 0 && daServer != null && daServer.isConnected())
			sendCommand("enable ", scalerHandle);
	}

	@Override
	public void atScanEnd() throws DeviceException {
		// if this class was used to define framesets, then ensure they are
		// cleared at the end of the scan
		if (tfg.getAttribute("TotalFrames").equals(0))
			stop(); // stops the TFG - useful if scan aborted and so TFG still
					// in a PAUSE state rather than an IDLE state
	}

	@Override
	public void stop() throws DeviceException {
		if (mcaHandle < 0 || scalerHandle < 0)
			open();
		if (mcaHandle >= 0 && daServer != null && daServer.isConnected())
			sendCommand("disable ", mcaHandle);
		if (scalerHandle >= 0 && daServer != null && daServer.isConnected())
			sendCommand("disable ", scalerHandle);
		lastFrameCollected = -1;
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
	 * @return int - the size in bits of the MCA array based on the readout mode
	 *         and region of interest options.
	 */
	private int determineNumberOfBits() {

		if (!settings.getParameters().getReadoutMode().equals(XspressDetector.READOUT_ROIS))
			return settings.getFullMCABits();

		int channels = findLargestChannelReadout();
		int order = 0;
		do
			order++;
		while (Math.pow(2, order) <= channels);

		return order;
	}

	private int findLargestChannelReadout() {
		int maxSize = 0;
		for (DetectorElement element : settings.getParameters().getDetectorList()) {
			int thisMcasize = 1; // always get an extra values for the out of
									// window counts
			for (XspressROI roi : element.getRegionList()) {
				if (settings.getParameters().getRegionType().equals(XspressParameters.VIRTUALSCALER))
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
	 * @return the current size of the mca's based on the readout mode and
	 *         region of interest options.
	 */
	public int getCurrentMCASize() {
		return settings.getMcaSize();
	}

	public int getCurrentMCABits() {
		return settings.getFullMCABits();
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
				String newResGrade = settings.getParameters().getResGrade();
				// override the res-grade if the readout mode is scalers only or
				// saclers + mca
				// This might not be the best place to do this
				if (!settings.getParameters().getReadoutMode().equals(XspressDetector.READOUT_ROIS))
					newResGrade = ResGrades.NONE;
				startupScript = "xspress2 format-run 'xsp1' " + newResGrade;
				if ((obj = daServer.sendCommand(startupScript)) == null)
					throw new DeviceException("Null reply received from daserver during " + startupScript);
				else if (((Integer) obj).intValue() == -1)
					throw new DeviceException(getName() + ": " + startupScript + " failed");
				else {
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
		String newResGrade = settings.getParameters().getResGrade();
		// override the res-grade if the readout mode is scalers only or saclers
		// + mca
		// This might not be the best place to do this
		if (!settings.getParameters().getReadoutMode().equals(XspressDetector.READOUT_ROIS))
			newResGrade = ResGrades.NONE;
		String formatCommand = "xspress2 format-run " + xspressSystemName + " " + numberOfBits + " " + newResGrade;
		if (daServer != null && daServer.isConnected()) {
			Integer numFrames = ((Integer) daServer.sendCommand(formatCommand)).intValue();
			if (numFrames == null)
				throw new DeviceException("Null reply received from daserver during " + formatCommand);
			else if (numFrames == -1)
				throw new DeviceException(getName() + ": " + formatCommand + " failed");
			else if (numFrames < maxNumberOfFrames) {
				maxNumberOfFrames = numFrames;
				logger.info("Xspress2System formatCommand - maximum time frames achievable: " + maxNumberOfFrames);
			} else
				logger.info("Xspress2System formatCommand - maximum time frames achievable: " + numFrames
						+ " but limited to " + maxNumberOfFrames + " by startupscript");
		}
	}

	private void doRemoveROIs() throws DeviceException {
		Object obj;
		int rc;
		String roiCommand = "xspress2 set-roi " + xspressSystemName + " -1";
		if ((obj = daServer.sendCommand(roiCommand)) != null)
			if ((rc = ((Integer) obj).intValue()) < 0)
				throw new DeviceException("Xspress2System error removing regions of interest: " + rc);

	}

	private void doSetROICommand(DetectorElement detector) throws DeviceException {
		Object obj;
		int rc;
		String roiCommand = "xspress2 set-roi " + xspressSystemName + " " + detector.getNumber();
		List<XspressROI> regionList = detector.getRegionList();
		if (regionList.isEmpty())
			return; // No regions for detector element.
		for (XspressROI region : regionList)
			roiCommand += " " + region.getRoiStart() + " " + region.getRoiEnd() + " " + calculateRegionBins(region);
		if ((obj = daServer.sendCommand(roiCommand)) != null)
			if ((rc = ((Integer) obj).intValue()) < 0)
				throw new DeviceException("Xspress2System error setting regions of interest: " + rc);
	}

	private int calculateRegionBins(XspressROI region) {
		int regionBins = 1; // 1 means a virtual scaler
		if (settings.getParameters().getRegionType() != null && settings.getParameters().getRegionType().equals(XspressROI.MCA))
			// else regionBins should be the size of the MCA. (DAserver will not
			// accept any other values).
			regionBins = region.getRoiEnd() - region.getRoiStart() + 1;
		return regionBins;
	}

	private void doSetWindowsCommand(DetectorElement detector) throws DeviceException {
		Object obj;
		int rc;
		String windowCommand = "xspress2 set-window " + xspressSystemName + " " + detector.getNumber() + " "
				+ detector.getWindowStart() + " " + detector.getWindowEnd();
		if ((obj = daServer.sendCommand(windowCommand)) != null)
			if ((rc = ((Integer) obj).intValue()) < 0)
				throw new DeviceException("Xspress2System error setting windows: " + rc);
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
					if (mcaHandle < 0)
						throw new DeviceException("Failed to create the mca handle");
					logger.info("Xspress2System: open() using mcaHandle " + mcaHandle);
				}
			}

			if (scalerOpenCommand != null) {
				if ((obj = daServer.sendCommand(scalerOpenCommand)) != null) {
					scalerHandle = ((Integer) obj).intValue();
					if (scalerHandle < 0)
						throw new DeviceException("Failed to create the scaler handle");
					logger.info("Xspress2System: open() using scalerHandle " + scalerHandle);
				}
			}

			if ((obj = daServer.sendCommand("xspress2 get-res-bins " + xspressSystemName)) != null) {
				settings.setMcaGrades(((Integer) obj).intValue());
				logger.info("Xspress2System: mcaGrades " + settings.getMcaGrades());
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
		settings.setXspressParameters((XspressParameters) XMLHelpers.createFromXML(XspressParameters.mappingURL,
				XspressParameters.class, XspressParameters.schemaURL, filename));
//		if (detectorSettings.getXspressParameters() != null)
//			numberOfDetectors = xspressParameters.getDetectorList().size();
		// if mode override is set as a property ignore all the parameter file
		// settings
		if (modeOverride)
			settings.getParameters().setReadoutMode(READOUT_MCA);
	}

	public void loadAndInitializeDetectors(String filename, String dtcConfigFileName) throws Exception {
		loadAndInitializeDetectors(filename);
		XspressDeadTimeParameters xspressDeadTimeParameters = (XspressDeadTimeParameters) XMLHelpers.createFromXML(
				XspressDeadTimeParameters.mappingURL, XspressDeadTimeParameters.class,
				XspressDeadTimeParameters.schemaURL, dtcConfigFileName);
		xspress2SystemData.setDeadTimeParameters(xspressDeadTimeParameters);
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
			XMLHelpers.writeToXML(XspressParameters.mappingURL, settings.getParameters(), filename);
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
		settings.getParameters().getDetector(number).setWindow(lower, upper);
	}

	@Override
	public DetectorElement getDetector(int which) throws DeviceException {
		return settings.getParameters().getDetector(which);
	}

	public XspressDeadTimeParameters getDeadTimeParameters() {
		return xspress2SystemData.getDeadTimeParameters();
	}

	/**
	 * Gets the multi-channel data for all elements. Includes setup of the
	 * detector etc. Reads from one frame and tfg counts for time passed in
	 * (suggest 1000ms). Reads 1 time frame and assumes desired resGrade has
	 * already been set.
	 * 
	 * @param time
	 *            the time to count for (milliseconds)
	 * @return array[numberOfDetectors][mcaChannels] of int values representing
	 *         the counts in each channel.
	 * @throws DeviceException
	 */
	@Override
	public int[][][] getMCData(int time) throws DeviceException {
		if (!daServer.isConnected())
			daServer.connect();
		clear();
		start();
		tfg.clearFrameSets(); // we only want to collect a frame at a time
		tfg.countAsync(time); // run tfg for time
		do {
			synchronized (this) {
				try {
					wait(100);
				} catch (InterruptedException e) {
				}
			}
		} while (tfg.getStatus() == Timer.ACTIVE);

		// stop();

		int[] data = null;
		if (mcaHandle >= 0 && daServer != null && daServer.isConnected())
			data = readoutMca(0, 1, 4096); // NOTE 1 time frame

		if (data != null) {
			try {
				int[][][][] fourD = xspress2SystemData.unpackRawDataTo4D(data, 1, numResGrades(), 4096,
						settings.getNumberOfDetectors());
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
	 * @return int - the number of copies of data you get depending on
	 *         resolution setting
	 */
	private int numResGrades() {
		// if not in ROI mode then only one res grade
		if (settings.getParameters().getReadoutMode().compareTo(READOUT_ROIS) != 0)
			return 1;
		// if none then only one set of 4096 numbers per mca
		if (settings.getParameters().getResGrade().compareTo(ResGrades.NONE) == 0)
			return NO_RES_GRADE;
		// if all re-grades then 16 arrays per mca
		else if (settings.getParameters().getResGrade().compareTo(ResGrades.ALLGRADES) == 0)
			return ALL_RES;
		// otherwise you get 2 arrays (bad, good)
		else
			return RES_THRES;
	}

	/**
	 * Returns a NexusTreeProvider object which the NexusDataWriter can unpack
	 * properly
	 */
	@Override
	public NexusTreeProvider readout() throws DeviceException {
		return readout(lastFrameCollected, lastFrameCollected)[0];
	}

	/**
	 * Returns a NexusTreeProvider object which the NexusDataWriter can unpack
	 * properly
	 */
	public NexusTreeProvider[] readout(int startFrame, int finalFrame) throws DeviceException {
		int numberOfFrames = finalFrame - startFrame + 1;

		int[] rawHardwareScalerData = readoutHardwareScalers(startFrame, numberOfFrames);

		if (settings.getParameters().getReadoutMode().equals(XspressDetector.READOUT_SCALERONLY)) {
			return xspress2SystemData.unpackScalerData(numberOfFrames, rawHardwareScalerData);
		}

		int[] mcaData = readoutMca(startFrame, numberOfFrames, getCurrentMCASize());
		double[][] scalerDataUsingMCAMemory = xspress2SystemData.readoutScalerDataUsingMCAMemory(numberOfFrames, rawHardwareScalerData, mcaData, true,
				getI0());

		if (settings.getParameters().getReadoutMode().equals(XspressDetector.READOUT_ROIS))
			return xspress2SystemData.readoutROIData(numberOfFrames, rawHardwareScalerData, mcaData,
					scalerDataUsingMCAMemory);

		// else read out full mca, which is deadtime corrected using the hardware scalers
		return xspress2SystemData.readoutFullMCA(numberOfFrames, rawHardwareScalerData, mcaData, scalerDataUsingMCAMemory);
	}

	@Override
	public int[] getRawScalerData() throws DeviceException {
		return readoutHardwareScalers(0, 1);
	}

	/**
	 * Dead time corrected scaler data. Used by TfgXspress2 class which acts as
	 * an adapter for this class so that it can act like a counterTimer.
	 * 
	 * @return an array of doubles of dead time corrected 'in window' counts and
	 *         the sum of all the dead time corrected data.
	 * @throws DeviceException
	 * @Deprecated since 8.26 do not use this as the array of doubles does not
	 *             necessarily match to the outputNames
	 */
	@Override
	@Deprecated
	public double[] readoutScalerData() throws DeviceException {
		if (tfg.getAttribute("TotalFrames").equals(0))
			return readoutScalerData(0, 0, true, getRawScalerData(), getCurrentMCASize())[0];
		return readoutScalerData(lastFrameCollected, lastFrameCollected, true, getRawScalerData(), getCurrentMCASize())[0];
	}

	public double[] readoutScalerDataNoCorrection() throws DeviceException {
		if (tfg.getAttribute("TotalFrames").equals(0))
			return readoutScalerData(0, 0, false, getRawScalerData(), getCurrentMCASize())[0];
		return readoutScalerData(lastFrameCollected, lastFrameCollected, false, getRawScalerData(), getCurrentMCASize())[0];
	}

	public double[][] readoutScalerData(int startFrame, int finalFrame, boolean performCorrections,
			int[] rawscalerData, int currentMcaSize) throws DeviceException {
		int numberOfFrames = finalFrame - startFrame + 1;
		int[] mcaData = readoutMca(startFrame, numberOfFrames, currentMcaSize);
		return xspress2SystemData.readoutScalerDataUsingMCAMemory(numberOfFrames, rawscalerData, mcaData, performCorrections, getI0());
	}

	private Double getI0() {
		Double I0 = 1.0;
		if (settings.getMcaGrades() == Xspress2System.ALL_RES && ionChambersCounterTimer != null) {
			try {
				I0 = ionChambersCounterTimer.readout()[0];
			} catch (DeviceException e) {
				logger.error("Exception while trying to fetch I0 to normalise scalers for each res grade", e);
			}
		}
		return I0;
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
		if (mcaHandle < 0)
			open();
		if (mcaHandle >= 0 && daServer != null && daServer.isConnected()) {
			try {
				value = daServer.getIntBinaryData("read 0 0 " + startFrame + " " + mcaSize + " " + settings.getNumberOfDetectors()
						* settings.getMcaGrades() + " " + numberOfFrames + " from " + mcaHandle + " raw motorola", settings.getNumberOfDetectors()
						* settings.getMcaGrades() * mcaSize * numberOfFrames);
			} catch (Exception e) {
				throw new DeviceException(e.getMessage(), e);
			}
		}
		return value;
	}

	private synchronized int[] readoutHardwareScalers(int startFrame, int numberOfFrames) throws DeviceException {
		int[] value = null;
		if (scalerHandle < 0)
			open();
		if (scalerHandle >= 0 && daServer != null && daServer.isConnected()) {
			try {
				value = daServer.getIntBinaryData("read 0 0 " + startFrame + " " + numberOfScalers + " "
						+ settings.getNumberOfDetectors() + " " + numberOfFrames + " from " + scalerHandle + " raw motorola",
						settings.getNumberOfDetectors() * numberOfScalers * numberOfFrames);
			} catch (Exception e) {
				throw new DeviceException(e.getMessage(), e);
			}
		}
		return value;
	}

	@Override
	public void reconfigure() throws FactoryException {
		// A real system needs a connection to a real da.server via a DAServer
		// object.
		logger.debug("Xspress2System.reconfigure(): reconnecting to: " + daServerName);
		try {
			daServer.reconnect();
			// does not reconfigure the tfg -- need to check if it is needed
			// If everything has been found send the open commands.
			if (tfg != null && (daServer != null))
				open();
		} catch (DeviceException e) {
			throw new FactoryException(e.getMessage(), e);
		}
	}

	@Override
	public void collectData() throws DeviceException {
		if (!daServer.isConnected())
			daServer.connect();
		// if tfg not running with frames then clear and start the xspress
		// memory
		if (tfg.getAttribute("TotalFrames").equals(0)) {
			clear();
			start();
			lastFrameCollected = 0;
		} else
			lastFrameCollected++;// so all readout methods will read from the
									// same frame
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
	 * 
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

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		if (attributeName.equals("liveStats"))
			return calculateLiveStats();
		else if (attributeName.equals(ONLY_DISPLAY_FF_ATTR))
			return settings.getParameters().isOnlyShowFF();
		return null;
	}

	/**
	 * @return double[] - for every element, return the total count rate,
	 *         deadtime correction factor and in-window count rate
	 * @throws DeviceException
	 */
	private Object calculateLiveStats() throws DeviceException {
		int[] rawData = getRawScalerData();
		long[] rawDataLong = xspress2SystemData.convertUnsignedIntToLong(rawData);
		// TODO should saveRawSpectrum flag be checked here???
		double[] dtcs = xspress2SystemData.getDeadtimeCorrectionFactors(rawDataLong);
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
	 * Can be used to create an XspressParameters xml file. Useful when new 64
	 * element files are required!
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(final String[] args) throws Exception {
		final String fileName = args[0];
		int numEle = Integer.parseInt(args[1]);
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
		xspress2SystemData.setSumAllElementData(sumAllElementData);
	}

	public boolean isSumAllElementData() {
		return xspress2SystemData.isSumAllElementData();
	}

	public Boolean getSaveRawSpectrum() {
		return settings.getParameters().isSaveRawSpectrum();
	}

	public void setSaveRawSpectrum(Boolean saveRawSpectrum) {
		settings.getParameters().setSaveRawSpectrum(saveRawSpectrum);
	}

	public int getNumberFrames() throws DeviceException {
		// this value will be non-zero if collecting from a series of time
		// frames outside of the continuous scan mechanism
		if (tfg.getAttribute("TotalFrames").equals(0))
			return 0;
		return getNumberFramesFromTFGStatus();
	}

	public int getNumberFramesFromTFGStatus() throws DeviceException {
		String[] cmds = new String[] { "status show-armed", "progress", "status", "full", "lap", "frame" };
		HashMap<String, String> currentVals = new HashMap<String, String>();
		for (String cmd : cmds) {
			currentVals.put(cmd, runDAServerCommand("tfg read " + cmd).toString());
			logger.info("tfg read " + cmd + ": " + currentVals.get(cmd));
		}

		if (currentVals.isEmpty())
			return 0;

		// else either scan not started (return -1) or has finished (return
		// continuousParameters.getNumberDataPoints())

		// if started but nothing collected yet
		if (currentVals.get("status show-armed").equals("EXT-ARMED"))
			return 0;

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
			if ((obj = getDaServer().sendCommand(command)) == null)
				throw new DeviceException("Null reply received from daserver during " + command);
			return obj;
		}
		return null;
	}

	public String getDtcConfigFileName() {
		return dtcConfigFileName;
	}

	public void setDtcConfigFileName(String dtcConfigFileName) {
		this.dtcConfigFileName = dtcConfigFileName;
	}

	
}
