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
import gda.device.detector.NexusDetector;
import gda.device.detector.xspress.xspress2data.Xspress2Controller;
import gda.device.detector.xspress.xspress2data.Xspress2CurrentSettings;
import gda.device.detector.xspress.xspress2data.Xspress2NexusTreeProvider;
import gda.factory.FactoryException;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.beans.vortex.DetectorDeadTimeElement;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.xspress.ResGrades;
import uk.ac.gda.beans.xspress.XspressDeadTimeParameters;
import uk.ac.gda.beans.xspress.XspressDetector;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.devices.detector.FluorescenceDetector;
import uk.ac.gda.devices.detector.FluorescenceDetectorParameters;
import uk.ac.gda.util.beans.xml.XMLHelpers;

/**
 * Represents a set of Xspress2 boards and detectors. It actually communicates
 * with a DAServer object which is connected to a da.server process running on a
 * MVME computer.
 * <p>
 * This returns data as a Nexus tree from its readout method.
 * <p>
 * This does not actually drive the TFG (Timer), but assumes that the TFG will
 * be driven by another object in the scan, which is normal for Spectroscopy
 * beamlines whose scans are driven by a TFG which in turn drives a number of
 * other detectors used in the experiment.
 * <p>
 * Deadtime correction methods: none thres all hw sca hw - - hw sca+mca hw - -
 * roi hw new hw hw = apply deadtime factor from hardware scalers only new =
 * scale both types of ROI using total counts / counts in rois This needs
 * refactoring so that roi when all are selected are also corrected.
 */
public class Xspress2Detector extends XspressSystem implements NexusDetector, XspressDetector, FluorescenceDetector {

	private static final Logger logger = LoggerFactory.getLogger(Xspress2Detector.class);

	public static final int NO_RES_GRADE = 1;
	public static final int RES_THRES = 2;
	public static final int ALL_RES = 16;
	public static final String ADD_DT_VALUES_ATTR = "add_dt_values";

	protected int maxNumberOfRois = 7; // TODO check this value against what the hardware can actually handle!

	protected int lastFrameCollected = 0;
	// mode override property, when set to true the xspress is always set in SCAlers and MCA Mode
	// does not change with the value in the parameters file, no rois are set
	private boolean modeOverride = LocalProperties.check("gda.xspress.mode.override");
	private Xspress2NexusTreeProvider xspress2SystemData;
	private Xspress2CurrentSettings settings;
	protected Xspress2Controller controller;

	public Xspress2Detector() {
		this.inputNames = new String[] {};
		settings = new Xspress2CurrentSettings();
	}

	@Override
	public void configure() throws FactoryException {
		xspress2SystemData = new Xspress2NexusTreeProvider(getName(),settings);
		// A real system needs a connection to a real da.server via a DAServer object.
		try {
			loadAndInitializeDetectors(configFileName, dtcConfigFileName);
		} catch (Exception e) {
			logger.error(
					"Error loading template XML. Will use a detector with a few default elements instead. "
							+ e.getMessage(), e);
			useDefaultXspressParameters();
		}

		if (!ResGrades.isResGrade(settings.getParameters().getResGrade())) {
			throw new FactoryException("resGrade " + settings.getParameters().getResGrade() + " is not an acceptable string");
		}

		controller.configure();

		configured = true;
	}

	private void useDefaultXspressParameters() {
		ArrayList<DetectorROI> regions = new ArrayList<DetectorROI>();
		XspressParameters xspressParameters = new XspressParameters();
		XspressDeadTimeParameters xspressDeadTimeParameters = new XspressDeadTimeParameters();
		if (modeOverride) {
			xspressParameters.setReadoutMode(READOUT_MCA);
		} else {
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
		settings.setXspressParameters(xspressParameters);
	}

	@Override
	public Double getDeadtimeCalculationEnergy() throws DeviceException {
		return settings.getDeadtimeEnergy();
	}

	@Override
	public void setDeadtimeCalculationEnergy(Double energy) throws DeviceException {
		settings.setDeadtimeEnergy(energy);
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

		controller.setResolutionGrade(resGrade);
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
	public void setRegionOfInterest(int detector, ArrayList<DetectorROI> regionList) throws DeviceException {
		DetectorElement detectorElement = settings.getParameters().getDetector(detector);
		detectorElement.setRegionList(regionList);
		if (configured) {
			controller.doSetROICommand(detectorElement);
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
		DetectorElement detectorElement = settings.getParameters().getDetector(detector);
		detectorElement.setWindow(windowStart, windowEnd);
		if (configured) {
			controller.doSetWindowsCommand(detectorElement);
		}
	}

	@Override
	public void atScanLineStart() throws DeviceException {
		controller.checkIsConnected();
		// if this class was used to define framesets, then memory is only cleared at the start of the scan
		if (controller.getTotalFrames() != 0) {
			stop();
			clear();
			start();
		}
		lastFrameCollected = -1;
	}

	@Override
	public void atScanStart() throws DeviceException {
		controller.checkIsConnected();
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

	@Override
	public Boolean getAddDTScalerValuesToAscii() {
		return settings.isAddDTScalerValuesToAscii();
	}

	@Override
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
			controller.setFullMCABits(fullMCABits);
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
			controller.configureDetectorFromParameters();
		} else if ((readoutMode.equals(XspressDetector.READOUT_SCALERONLY)
				|| readoutMode.equals(XspressDetector.READOUT_MCA) || readoutMode.equals(XspressDetector.READOUT_ROIS))
				&& !readoutMode.equals(settings.getParameters().getReadoutMode())) {
			settings.getParameters().setReadoutMode(readoutMode);
			controller.configureDetectorFromParameters();
		}
	}

	@Override
	public boolean isOnlyDisplayFF() {
		return settings.getParameters().isOnlyShowFF();
	}

	@Override
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
			controller.setResolutionGrade(resGrade, 12);
		} else if (attribute.equals(ONLY_DISPLAY_FF_ATTR)) {
			Boolean ffonly = Boolean.parseBoolean(value.toString());
			setOnlyDisplayFF(ffonly);
		} else {
			super.setAttribute(attribute, value);
		}
	}

	/**
	 * @param which
	 * @return true if detectorElement is excluded.
	 */
	@Override
	public boolean isDetectorExcluded(int which) {
		return settings.getParameters().getDetector(which).isExcluded();
	}

	@Override
	public void setDetectorExcluded(int which, boolean excluded) {
		settings.getParameters().getDetector(which).setExcluded(excluded);
	}

	@Override
	public List<DetectorElement> getDetectorList() {
		return settings.getParameters().getDetectorList();
	}

	public boolean isAlwaysRecordRawMCAs() {
		return settings.isAlwaysRecordRawMCAs();
	}

	public void setAlwaysRecordRawMCAs(boolean alwaysRecordRawMCAs) {
		settings.setAlwaysRecordRawMCAs(alwaysRecordRawMCAs);
	}

	/**
	 * Sends the daServer commands to clear the xspress system. Note that this
	 * is very time consuming and should only be done when necessary.
	 *
	 * @throws DeviceException
	 */
	@Override
	public void clear() throws DeviceException {
		controller.clear();
	}

	/**
	 * Sends the daServer commands to enable the xspress system counting. This
	 * does not start the TFG counting.
	 *
	 * @see uk.ac.gda.beans.xspress.XspressDetector#start()
	 */
	@Override
	public void start() throws DeviceException {
		controller.start();
	}

	@Override
	public void atScanEnd() throws DeviceException {
		// if this class was used to define framesets, then ensure they are cleared at the end of the scan
		if (controller.getTotalFrames() == 0) {
			stop(); // stops the TFG - useful if scan aborted and so TFG still in a PAUSE state rather than an IDLE state
		}
	}

	@Override
	public void stop() throws DeviceException {
		controller.stop();
		lastFrameCollected = -1;
	}

	@Override
	public void close() throws DeviceException {
		controller.close();
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
		// if mode override is set as a property ignore all the parameter file settings
		if (modeOverride) {
			settings.getParameters().setReadoutMode(READOUT_MCA);
		}
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

		int[] data = controller.runOneFrame(time);

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
	 * @return int - the number of versions of data you get depending on
	 *         resolution setting
	 */
	private int numResGrades() {
		// if not in ROI mode then only one res grade
		if (settings.getParameters().getReadoutMode().compareTo(READOUT_ROIS) != 0) {
			return 1;
		}
		// if none then only one set of 4096 numbers per mca
		if (settings.getParameters().getResGrade().compareTo(ResGrades.NONE) == 0) {
			return NO_RES_GRADE;
		} else if (settings.getParameters().getResGrade().compareTo(ResGrades.ALLGRADES) == 0) {
			return ALL_RES;
		// otherwise you get 2 arrays (bad, good)
		} else {
			return RES_THRES;
		}
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
		return readout(getName(), startFrame, finalFrame);
	}

	/**
	 * Version of readout method in which the given detectorName is used in the NexusTree. Used where this object is a component of a different detector class.
	 */
	public NexusTreeProvider[] readout(String detectorName, int startFrame, int finalFrame) throws DeviceException {
		int numberOfFrames = finalFrame - startFrame + 1;

		int[] rawHardwareScalerData = controller.readoutHardwareScalers(startFrame, numberOfFrames);

		if (settings.getParameters().getReadoutMode().equals(XspressDetector.READOUT_SCALERONLY)) {
			return xspress2SystemData.unpackScalerData(detectorName, numberOfFrames, rawHardwareScalerData);
		}

		int[] mcaData = controller.readoutMca(startFrame, numberOfFrames, getCurrentMCASize());
		double[][] scalerDataUsingMCAMemory = xspress2SystemData.readoutScalerDataUsingMCAMemory(detectorName, numberOfFrames,
				rawHardwareScalerData, mcaData, true, controller.getI0());

		if (settings.getParameters().getReadoutMode().equals(XspressDetector.READOUT_ROIS)) {
			return xspress2SystemData.readoutROIData(detectorName, numberOfFrames, rawHardwareScalerData, mcaData,
					scalerDataUsingMCAMemory);
		}

		// else read out full mca, which is deadtime corrected using the hardware scalers
		return xspress2SystemData.readoutFullMCA(detectorName, numberOfFrames, rawHardwareScalerData, mcaData, scalerDataUsingMCAMemory);
	}

	@Override
	public int[] getRawScalerData() throws DeviceException {
		return controller.readoutHardwareScalers(0, 1);
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
		if (controller.getTotalFrames() == 0) {
			return readoutScalerData(0, 0, true, getRawScalerData(), getCurrentMCASize())[0];
		}
		return readoutScalerData(lastFrameCollected, lastFrameCollected, true, getRawScalerData(), getCurrentMCASize())[0];
	}

	public double[] readoutScalerDataNoCorrection() throws DeviceException {
		if (controller.getTotalFrames() == 0) {
			return readoutScalerData(0, 0, false, getRawScalerData(), getCurrentMCASize())[0];
		}
		return readoutScalerData(lastFrameCollected, lastFrameCollected, false, getRawScalerData(), getCurrentMCASize())[0];
	}

	public double[][] readoutScalerData(String detectorName, int startFrame, int finalFrame, boolean performCorrections,
			int[] rawscalerData, int currentMcaSize) throws DeviceException {
		int numberOfFrames = finalFrame - startFrame + 1;
		int[] mcaData = controller.readoutMca(startFrame, numberOfFrames, currentMcaSize);
		return xspress2SystemData.readoutScalerDataUsingMCAMemory(detectorName, numberOfFrames, rawscalerData, mcaData,
				performCorrections, controller.getI0());

	}

	public double[][] readoutScalerData(int startFrame, int finalFrame, boolean performCorrections,
			int[] rawscalerData, int currentMcaSize) throws DeviceException {
		return readoutScalerData(getName(), startFrame, finalFrame, performCorrections,
			 rawscalerData,  currentMcaSize);
	}

	@Override
	public void reconfigure() throws FactoryException {
		controller.reconfigure();
	}

	@Override
	public void collectData() throws DeviceException {
		controller.collectData();
		if (controller.getTotalFrames() == 0) {
			lastFrameCollected = 0;
		} else {
			lastFrameCollected++;
		}
	}

	@Override
	public int getStatus() throws DeviceException {
		return controller.getStatus();
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

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		if (attributeName.equals("liveStats")) {
			return calculateLiveStats();
		} else if (attributeName.equals(ONLY_DISPLAY_FF_ATTR)) {
			return settings.getParameters().isOnlyShowFF();
		}
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
					new ArrayList<DetectorROI>()));
			xspressDeadTimeParameters.addDetectorDeadTimeElement(new DetectorDeadTimeElement("Element " + i, i,
					2.5304E-9, 2.2534E-7, 2.5454E-7));
		}
		XMLHelpers.writeToXML(XspressParameters.mappingURL, xspressParameters, fileName);
		System.out.println("Created file " + fileName);
	}

	public void setSumAllElementData(boolean sumAllElementData) {
		settings.setSumAllElementData(sumAllElementData);
	}

	public boolean isSumAllElementData() {
		return settings.isSumAllElementData();
	}

	@Override
	public Boolean getSaveRawSpectrum() {
		return settings.getParameters().isSaveRawSpectrum();
	}

	@Override
	public void setSaveRawSpectrum(Boolean saveRawSpectrum) {
		settings.getParameters().setSaveRawSpectrum(saveRawSpectrum);
	}

	public Xspress2CurrentSettings getCurrentSettings() {
		return settings;
	}

	public void setCurrentSettings(Xspress2CurrentSettings settings) {
		this.settings = settings;
	}

	public Xspress2Controller getController() {
		return controller;
	}

	public void setController(Xspress2Controller controller) {
		this.controller = controller;
		controller.setCurrentSettings(settings);
	}

	public Xspress2NexusTreeProvider getSystemData() {
		return xspress2SystemData;
	}

	@Override
	public double[][] getMCAData(double time) throws DeviceException {

		int[] data = controller.runOneFrame((int) Math.round(time));

		if (data != null) {
			try {
				// [numFrames][numberOfDetectors][numResGrades][mcaSize]
				int[][][][] fourD = xspress2SystemData.unpackRawDataTo4D(data, 1, 1, 4096,
						settings.getNumberOfDetectors());

				// remove frame and res-grade settings - not used by I18 or B18
				int numDetectors = fourD[0].length;
				int mcaSize = fourD[0][0][0].length;
				double[][] twoD = new double[numDetectors][mcaSize];

				for (int det = 0; det < numDetectors; det++){
					for (int mcaChan = 0; mcaChan < mcaSize; mcaChan++){
						twoD[det][mcaChan] = fourD[0][det][0][mcaChan];
					}
				}

				return twoD;
			} catch (Exception e) {
				throw new DeviceException("Error while unpacking MCA Data. Data length was " + data.length, e);
			}
		}

		return null;
	}

	public void loadConfigurationFromFile() throws Exception {
		loadAndInitializeDetectors(getConfigFileName());
	}

	public DetectorROI[] getRegionsOfInterest() throws DeviceException {
		List<DetectorROI> rois = settings.getParameters().getDetectorList().get(0).getRegionList();
		return (DetectorROI[]) rois.toArray();
	}

	public void setRegionsOfInterest(DetectorROI[] regionList)
			throws DeviceException {

		// convert to list
		List<DetectorROI> rois = new ArrayList<DetectorROI>();
		for(DetectorROI roi : regionList){
			rois.add(roi);
		}

		// replace rois in each detector channel(element) in the settings object and push to hardware
		for(DetectorElement element : settings.getParameters().getDetectorList()){
			element.setRegionList(rois);
			controller.doSetROICommand(element);
		}
	}

	@Override
	public int getNumberOfElements() {
		return settings.getNumberOfDetectors();
	}

	@Override
	public int getMCASize() {
		return settings.getFullMCASize();
	}

	@Override
	public int getMaxNumberOfRois() {
		return maxNumberOfRois;
	}

	@Override
	public void applyConfigurationParameters(
			FluorescenceDetectorParameters parameters) throws Exception {
		settings.setXspressParameters((XspressParameters) parameters);
		// if mode override is set as a property ignore all the parameter file settings
		if (modeOverride) {
			settings.getParameters().setReadoutMode(READOUT_MCA);
		}
	}

	@Override
	public FluorescenceDetectorParameters getConfigurationParameters() {
		return settings.getParameters();
	}
}
