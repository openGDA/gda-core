/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.detector.xspress4;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileHDF5;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.DetectorBase;
import gda.device.detector.NexusDetector;
import gda.device.detector.TfgFFoverI0;
import gda.device.detector.xspress.xspress2data.Xspress2CurrentSettings;
import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;
import uk.ac.gda.api.remoting.ServiceInterface;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.xspress.ResGrades;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.devices.detector.DetectorWithConfigurationFile;
import uk.ac.gda.devices.detector.FluorescenceDetector;
import uk.ac.gda.devices.detector.FluorescenceDetectorParameters;
import uk.ac.gda.devices.detector.xspress3.Xspress3Detector.XspressHelperMethods;
import uk.ac.gda.util.beans.xml.XMLHelpers;

/**
 * Class to setup and readout from Xspress4 detector.<p>
 * Setup and running of the detector is similar to Xspress3 (i.e. through Epics via PVs) but data produced and to be written
 * to the Nexus file is more similar to Xspress2. All detector data is contained in the hdf file written by the detector,
 * and a small subset of this will be processed and added to the GDA Nexus file via {@link #readout()} method. The exact
 * values written to Nexus file depend on the readout mode setting
 * (i.e. resolution grade information, deadtime correction calculation information, in-window sum, total FF, etc). <p>
 *
 * The strategy is to store settings for the collection using the {@link XspressParameters} class (as used by {@link XSpress2}).
 * Access to PVs on the  detector is via an implementation of {@link Xspress4Controller}.
 */
@ServiceInterface(FluorescenceDetector.class)
@SuppressWarnings("serial")
public class Xspress4Detector extends DetectorBase implements FluorescenceDetector, NexusDetector, DetectorWithConfigurationFile {
	private static final Logger logger = LoggerFactory.getLogger(Xspress4Detector.class);

	/** Trigger modes (caget -d31 BL20I-EA-XSP4-01:TriggerMode). Use 'TTL veto' for hardware triggered scans, 'Software' for software triggered scans*/
	public enum TriggerMode {Software, Hardware, Burst, TtlVeto, IDC, SoftwareStartStop, TtlBoth, LvdsVetoOnly, LvdsBoth};

	private Xspress4Controller xspress4Controller;
	private Xspress4NexusTree xspress4NexusTree;

	private TfgFFoverI0 tfgFFI0;

	private Xspress2CurrentSettings currentSettings;
	private XspressParameters parameters;
	private String configFileName;

	private Map<String, Integer> nexusScalerNameIndexMap = new HashMap<>();  // <scaler label, scaler number>
	private Map<String, Integer> asciiScalerNameIndexMap = new LinkedHashMap<>(); // <ascii column label, scaler number> ; linked hashmap so that keyset order is same as order in which keys were added

	private TriggerMode currentTriggerMode = TriggerMode.TtlVeto;
	private int numFramesReadoutAtPointStart = 0;
	private boolean writeHdfFiles = true;

	private String filePath = "";
	private String filePrefix = "";

	private String defaultSubDirectory = "";
	public static int MAX_ROI_PER_CHANNEL = 4;

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		super.configure();
		inputNames = new String[] {};
		filePrefix = getName();

		checkControllersAreSet();

		// setup default name-index map if not configured through spring
		if (nexusScalerNameIndexMap.isEmpty()) {
			setDefaultScalerIndexMap();
		}

		currentSettings = new Xspress2CurrentSettings();
		if (configFileName==null) {
			logger.warn("Configuration file has not been set");
		} else {
			loadConfigurationFromFile(configFileName);
		}
		setConfigured(true);
		xspress4NexusTree = new Xspress4NexusTree(this);
	}

	protected void checkControllersAreSet() {
		Objects.requireNonNull(xspress4Controller, "Controller has not been set for Xspress4 detector - it will not function correctly or work in scans");
	}

	public void setController(Xspress4Controller controller) {
		xspress4Controller = controller;
	}

	public Xspress4Controller getController() {
		return xspress4Controller;
	}

	@Override
	public void atScanStart() throws DeviceException {
		waitForMcaCollection();

		setTriggerMode(currentTriggerMode);
		if (currentTriggerMode == TriggerMode.Software) {
			setAcquireTime(getCollectionTime());
		}

		// Make sure detector is stopped first
		xspress4Controller.stopAcquire();

		// reset counter for total number of frames read out
		xspress4Controller.resetFramesReadOut();

		int numberOfFramesToCollect = 0;
		if (InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation() == null) {
			// If there is no scan running, atScanStart was probably called by detector rate collection
			// and need to collect a single frame.
			numberOfFramesToCollect = 1;
		} else {
			// Set the number of frames to be collected from scan information
			numberOfFramesToCollect = XspressHelperMethods.getLengthOfEachScanLine();
		}
		setupNumFramesToCollect(numberOfFramesToCollect);
	}

	public void setupNumFramesToCollect(int numberOfFramesToCollect) throws DeviceException {

		xspress4Controller.setNumImages(numberOfFramesToCollect);

		if (writeHdfFiles) {

			// Setup ROI binning to integrate over resolution grade bins if using
			// Scalers/Scalers+MCA mode
			boolean saveResGrades = parameters.getReadoutMode().equals(XspressParameters.READOUT_MODE_REGIONSOFINTEREST);
			if (xspress4Controller.setSaveResolutionGradeData(saveResGrades)) {
				getMCAData(500); // collect frame of data (so that new bin setting is picked up by hdf writer)
			}

			// Set hdf output directory, name :
			String hdfDir = XspressHelperMethods.getFilePath(filePath, defaultSubDirectory);
			// make any parent directories
			File file = new File(hdfDir);
			if (!file.exists()) {
				file.mkdirs();
			}
			xspress4Controller.setHdfFilePath(hdfDir);
			xspress4Controller.setHdfNumFrames(numberOfFramesToCollect);
			xspress4Controller.setHdfFileName(XspressHelperMethods.getFilePrefix(filePrefix));

			// Not needed if auto-increment is off (should be off by default)
//			xspress3Controller.setNextFileNumber(0);
		}
	}


	/**
	 * Wait for Hdf file writing to flush the final frame of data.
	 * If number of captured frames is less than the number of demand frames, then stop immediately.
	 *
	 * @throws DeviceException
	 */
	protected void waitForFileWriter() throws DeviceException {
		if (xspress4Controller.getHdfNumCapturedFrames() < xspress4Controller.getHdfNumFramesRbv()) {
			xspress4Controller.stopHdfWriter();
		} else {
			// wait for captures to finish, then stop the writer.
			xspress4Controller.waitForCaptureState(false);
			xspress4Controller.stopHdfWriter();
		}
	}

	@Override
	public void atScanEnd() throws DeviceException {
		if (writeHdfFiles) {
			waitForFileWriter();
		}

		// create link to hdf file...
		if (writeHdfFiles) {
			String path = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation().getFilename();
			try (NexusFile nexusFile = NexusFileHDF5.openNexusFile(path)) {
				Path nexusFilePath = Paths.get(path).getParent();
				Path hdfFilePath = Paths.get(xspress4Controller.getHdfFullFileName());
				// Try to get relative path to hdf file from Nexus file
				Path hdfFileRelativePath = hdfFilePath;
				try{
					hdfFileRelativePath = nexusFilePath.relativize(hdfFilePath);
				}catch(IllegalArgumentException e) {
					logger.warn("Cannot set relative path to hdf file {} from Nexus file {}. "+
								"Using absolute path to hdf file instead.", hdfFilePath, nexusFilePath);
				}

				String relativeLink = hdfFileRelativePath + "#entry/data/data";
				String nexusLinkName = "/entry1/" + getName() + "/MCAs";
				nexusFile.linkExternal(new URI(relativeLink), nexusLinkName, false);
			} catch (Exception e) {
				logger.error("Problem creating link to hdf file in nexus", e);
			}
		}
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		xspress4Controller.stopHdfWriter();
		atScanEnd();
	}

	@Override
	public void atPointEnd() throws DeviceException {
		// Increments internal (non Epics) counter for number of frames read. Is this needed?
//		detector.atPointEnd();
	}

	public void acquireFrameAndWait() throws DeviceException {
		acquireFrameAndWait(getCollectionTime()*1000, getCollectionTime()*1000);
	}

	/**
	 * Acquire new frame of data on detector and wait until it's finished.
	 * i.e. counter for 'total frames readout' (:ArrayCounter_RBV) has been incremented, or timeout has been reached
	 *
	 * @param collectionTimeMillis Frame time (milliseconds).
	 * @param timeoutMillis Timeout - how long to wait for detector after expected collection time (milliseconds)
	 * @throws DeviceException
	 */
	public void acquireFrameAndWait(double collectionTimeMillis, double timeoutMillis) throws DeviceException {
		int numFramesBeforeAcquire = xspress4Controller.getTotalFramesAvailable();
		logger.info(":Acquire called");
		xspress4Controller.startAcquire();
		try {
			Thread.sleep((long)collectionTimeMillis);
			xspress4Controller.waitForCounterToIncrement(numFramesBeforeAcquire, (long)timeoutMillis);
		} catch (InterruptedException e) {
			// Reset interrupt status
			Thread.currentThread().interrupt();

			logger.warn("Interrupted while waiting for acquire");
		}
		logger.info("Wait for acquire finished");
		if (xspress4Controller.getTotalFramesAvailable()==numFramesBeforeAcquire) {
			logger.warn("Acquire not finished after waiting for {} secs", timeoutMillis*0.001);
		}
	}

	@Override
	public void atScanLineStart() throws DeviceException {
		xspress4Controller.startHdfWriter();
		xspress4Controller.waitForCaptureState(true);

		// Start Acquire if using hardware triggering (i.e. detector waits for external trigger for each frame)
		if (currentTriggerMode != TriggerMode.Software) {
			xspress4Controller.startAcquire();
		}
	}

	@Override
	public void atPointStart() throws DeviceException {
		// collect new frame of data (software trigger only)
		if (currentTriggerMode == TriggerMode.Software) {
			acquireFrameAndWait();
		} else {
			// Get number of frames available from array counter
			numFramesReadoutAtPointStart = xspress4Controller.getTotalFramesAvailable();
		}
	}

	@Override
	public void stop() throws DeviceException {
		xspress4Controller.stopAcquire();
		atScanEnd();
	}

	private volatile boolean mcaCollectionInProgress = false;

	public void waitForMcaCollection() throws DeviceException {
		try {
			logger.debug("Waiting for MCA collection to finish");
			waitWhileBusy();
			logger.debug("MCA collection finished");

		} catch (InterruptedException e) {
			// Reset interrupt status
			Thread.currentThread().interrupt();

			logger.warn("Thread interrupted waiting for MCA collection to finish", e);
		}
	}

	@Override
	public int getStatus() throws DeviceException {
		if (mcaCollectionInProgress) {
			return Detector.BUSY;
		}
		int status = xspress4Controller.getDetectorState().toGdaDetectorState();
		if (status == Detector.FAULT || status == Detector.STANDBY) {
			return status;
		}
		return Detector.IDLE;
	}

	@Override
	public void collectData() throws DeviceException {
		logger.debug("Skip collectData");
		// Don't need to do anything here, as detector is usually hardware triggered by Tfg during step scans and
		// making calls here to controller.doErase(), controller.doStart() can cause triggers to be missed by the detector.
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public double[][] getMCAData(double timeMillis) throws DeviceException {
		double mcaData[][] = null;
		try {
			xspress4Controller.stopAcquire();

			// Store the currently set trigger mode
			TriggerMode trigMode = getTriggerMode();

			//Set software trigger mode, collection for 1 frame of data
			setTriggerMode(TriggerMode.Software);
			xspress4Controller.setNumImages(1);
			setAcquireTime(timeMillis*0.001);

			// Record frame of data on detector
			acquireFrameAndWait(timeMillis, timeMillis);
			xspress4Controller.stopAcquire();

			// Reset trigger mode to original value
			setTriggerMode(trigMode);

			mcaData = xspress4Controller.getMcaData();
		} catch (DeviceException e) {
			logger.error("Problem getting MCA data", e);
			throw e;
		}
		return mcaData;
	}

	@Override
	public int getNumberOfElements() {
		return xspress4Controller.getNumElements();
	}

	@Override
	public int getMCASize() {
		return xspress4Controller.getNumMcaChannels();
	}

	@Override
	public int getMaxNumberOfRois() {
		return MAX_ROI_PER_CHANNEL;
	}

	@Override
	public FluorescenceDetectorParameters getConfigurationParameters() {
		return parameters;
	}

	@Override
	public String getConfigFileName() {
		return configFileName;
	}

	@Override
	public void setConfigFileName(String configFileName) {
		this.configFileName = configFileName;
	}

	@Override
	public String[] getExtraNames() {
		return currentSettings.getExtraNames();
	}

	@Override
	public String[] getOutputFormat() {
		return currentSettings.getOutputFormat();
	}

	@Override
	public void setOutputFormat(String[] outputFormat) {
		currentSettings.setDefaultOutputFormat(outputFormat);
	}

	public int getMcaGrades() {
		return currentSettings.getMcaGrades();
	}

	public void setScalerNameIndexMap(Map<String,Integer> scalerIndexMap) {
		this.nexusScalerNameIndexMap = scalerIndexMap;
	}

	public Map<String,Integer> getScalerNameIndexMap() {
		return nexusScalerNameIndexMap;
	}

	public Map<String, Integer> getAsciiScalerNameIndexMap() {
		return asciiScalerNameIndexMap;
	}

	public void setAsciiScalerNameIndexMap(Map<String, Integer> asciiScalerNameIndexMap) {
		this.asciiScalerNameIndexMap = asciiScalerNameIndexMap;
	}

	public void loadConfigurationFromFile(String configFilename) {
		if (StringUtils.isEmpty(configFilename)) {
			return;
		}
		try {
			parameters = XMLHelpers.createFromXML(XspressParameters.mappingURL, XspressParameters.class, XspressParameters.schemaURL, configFilename);
			this.configFileName = configFilename;
			setupCurrentSettings();
		} catch (Exception e) {
			logger.warn("Problem loading configuration from file {}", configFilename, e);
		}
	}

	public int getResolutionThreshold() {
		String[] resGrade = parameters.getResGrade().split(" ");
		double threshold = Double.parseDouble(resGrade[1]);
		return (int) threshold;
	}

	public void setupCurrentSettings() {
		currentSettings.setXspressParameters(parameters);
		int mcaGrades = 0;
		String[] resGrade = parameters.getResGrade().split(" ");
		double threshold = 0;
		switch (resGrade[0]) {
			case ResGrades.NONE:
				mcaGrades = 1;
				break;
			case ResGrades.THRESHOLD:
				mcaGrades = 2;
				threshold = Double.parseDouble(resGrade[1]);
				break;
			case ResGrades.ALLGRADES:
				mcaGrades = 16;
				break;
		}
		currentSettings.setMcaGrades(mcaGrades);
		currentSettings.setAddDTScalerValuesToAscii(parameters.isShowDTRawValues());
	}

	@Override
	public void applyConfigurationParameters(FluorescenceDetectorParameters parameters) throws Exception {
		this.parameters = (XspressParameters) parameters;
		setupCurrentSettings();
		applyConfigurationParameters();
	}

	/**
	 * Configure detector using current set of parameters
	 * @throws Exception
	 */
	public void applyConfigurationParameters() throws Exception {
		if (parameters.getReadoutMode().equals(XspressParameters.READOUT_MODE_REGIONSOFINTEREST)) {
			logger.info("Setting scaler windows using ROI values");
			setScalerWindowsFromRois();
		} else {
			logger.info("Setting scaler window1");
			setScalerWindow(0); // apply to window1 only
		}
		setDtcEnergyKev(parameters.getDeadtimeCorrectionEnergy());
	}

	/**
	 * Apply current set of scaler window parameters to detector (window start, end)
	 *
	 * @param windowNumber scaler window to apply settings to (0 = window1, 1 = window2)
	 * @throws DeviceException
	 */
	public void setScalerWindow(int windowNumber) throws DeviceException {
		for (DetectorElement element : parameters.getDetectorList()) {
			int elementNumber = element.getNumber();
			xspress4Controller.setScalerWindow(elementNumber, windowNumber, element.getWindowStart(), element.getWindowEnd());
		}
	}

	/**
	 * Set scaler windows using values in ROI settings
	 * @throws DeviceException
	 */
	public void setScalerWindowsFromRois() throws DeviceException {
		int numRois = parameters.getDetector(0).getRegionList().size();
		int numWindowsToSet = Math.min(numRois,  2);
		for (DetectorElement element : parameters.getDetectorList()) {
			int elementNumber = element.getNumber();
			for(int i=0; i<numWindowsToSet; i++) {
				int start = element.getRegionList().get(i).getRoiStart();
				int end = element.getRegionList().get(i).getRoiEnd();
				xspress4Controller.setScalerWindow(elementNumber, i, start, end);
			}
		}
	}

	public void setDefaultScalerIndexMap() {
		// Same labels as in Edm screen for 'SCA Settings and Data'
//		nexusScalerNameIndexMap = new HashMap<>();
//		scalerNameIndexMap.put("time", 0);
//		scalerNameIndexMap.put("reset ticks", 1);
//		scalerNameIndexMap.put("reset count", 2);
//		scalerNameIndexMap.put("all event", 3);
//		scalerNameIndexMap.put("all good", 4);
//		scalerNameIndexMap.put("pileup", 7);

		// Same as used for XSpress2
		nexusScalerNameIndexMap = new HashMap<>();
		nexusScalerNameIndexMap.put("raw scaler total", 3);
		nexusScalerNameIndexMap.put("tfg resets", 1);
		nexusScalerNameIndexMap.put("raw scaler in-window", 5);
		nexusScalerNameIndexMap.put("tfg clock cycles", 0);

		// Names of deadtime correction data columns in ascii file (need to be same order as the columns in extraNames!)
		asciiScalerNameIndexMap = new LinkedHashMap<>();
		asciiScalerNameIndexMap.put("allEvents", 3);
		asciiScalerNameIndexMap.put("numResets", 1);
		asciiScalerNameIndexMap.put("inWinEvents", 5);
		asciiScalerNameIndexMap.put("tfgClock", 0);
	}

	/**
	 * Return deadtime correction data. Num scalers is usually 8;
	 * @return Dataset [num detector elements, num scalers]
	 * @throws IOException
	 */
	private Dataset getDeadtimeScalerData() throws DeviceException {
		int numberDetectorElements = xspress4Controller.getNumElements();

		double[][] allScalerData = new double[numberDetectorElements][8];  // [num elements][num scalers]

		// Get array of scaler values for each detector element
		for(int i=0; i<numberDetectorElements; i++) {
			allScalerData[i] = xspress4Controller.getScalerArray(i);
		}
		return DatasetFactory.createFromObject(allScalerData);
	}

	/**	Return array used by {@link MonitorViewBase} to update view with latest data.
	 * Need to return 2d array with 3 columns (total counts, deadtime correction factor, unused column)
	 */
	public Double[] getLiveStats() throws DeviceException {
		double[] dtcFactors = xspress4Controller.getDeadtimeCorrectionFactors();
		Dataset scalerData = getDeadtimeScalerData();
		int allEventTotalIndex = 3;

		Double[] results = new Double[3 * dtcFactors.length];
		for (int i = 0; i < dtcFactors.length; i++) {
			// Total counts (value from 'all events' scaler)
			results[i * 3] = scalerData.getDouble(i, allEventTotalIndex);
			// dead time correction factor
			results[i * 3 + 1] = dtcFactors[i];
			results[i * 3 + 2] = 0.0;
		}
		return results;
	}

	@Override
	public double[] getDeadtimeCorrectionFactors() throws DeviceException {
		return xspress4Controller.getDeadtimeCorrectionFactors();
	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		NexusTreeProvider tree = null;
		try {
			logger.info("Readout from {} started", getName());
			xspress4Controller.waitForCounterToIncrement(numFramesReadoutAtPointStart, 10*1000);
			tree = xspress4NexusTree.getDetectorData();
			logger.info("Readout from {} finished", getName());
		} catch (Exception e) {
			logger.warn("Problem encountered during readout()", e);
		}
		return tree;
	}

	public void setTriggerMode(TriggerMode mode) throws DeviceException {
		if (mode != null) {
			currentTriggerMode = mode;
			xspress4Controller.setTriggerMode(mode.ordinal());
		}
	}

	/**
	 * Convenience method to set the trigger mode by using integer rather than {@link TriggerMode} enum.
	 * {@link TriggerMode} enum value set is TriggerMode.values()[intMode];
	 * @param intMode
	 * @throws IOException
	 */
	public void setTriggerMode(int intMode) throws DeviceException {
		int maxIntTriggerMode = TriggerMode.values().length-1;
		if (intMode < 0 || intMode > maxIntTriggerMode) {
			logger.warn("Cannot set trigger mode to {}, Value should be between 0 and {}", intMode, maxIntTriggerMode);
			return;
		}
		currentTriggerMode = TriggerMode.values()[intMode];
		xspress4Controller.setTriggerMode(intMode);;
	}

	/**
	 * Return the current trigger mode setting by reading from PV.
	 * @return
	 * @throws IOException
	 */
	public TriggerMode getTriggerMode() throws DeviceException {
		int triggerMode = xspress4Controller.getTriggerMode();
		return TriggerMode.values()[triggerMode];
	}

	public TfgFFoverI0 getTfgFFI0() {
		return tfgFFI0;
	}

	public void setTfgFFI0(TfgFFoverI0 tfgFFI0) {
		this.tfgFFI0 = tfgFFI0;
	}

	/**
	 * Get I0 value from TfgScaler of FFI0 device. This is used form nomalising the res_bin_norm values in {@link #addResolutionGradeData}.
	 * Use value of 1 if FFI0 is not available or it throws an exception.
	 * @return I0 value
	 */
	public double getI0() {
		double result = 1.0;
		try {
			if (tfgFFI0 != null && tfgFFI0.getCounterTimer() != null) {
				result = (double) tfgFFI0.getI0();
			}
		} catch(DeviceException e) {
			logger.warn("Problem getting I0 value from tfg scalers. Using value I0 = {}", result, e);
		}
		// to avoid division by zero
		if (Math.abs(result)<1e-6) {
			result = 1.0;
		}
		return result;
	}

	public void setAcquireTime(double time) throws DeviceException {
		xspress4Controller.setAcquireTime(time);
	}

	public void setDtcEnergyKev(double dtcEnergy) throws DeviceException {
		xspress4Controller.setDeadtimeCorrectionEnergy(dtcEnergy);
	}

	public double getDtcEnergyKev() throws DeviceException {
		return xspress4Controller.getDeadtimeCorrectionEnergy();
	}

	@Override
	public void setWriteHDF5Files(boolean writeHdfFiles) {
		this.writeHdfFiles = writeHdfFiles;
	}

	@Override
	public boolean isWriteHDF5Files() {
		return writeHdfFiles;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFilePrefix() {
		return filePrefix;
	}

	public void setFilePrefix(String filePrefix) {
		this.filePrefix = filePrefix;
	}

	public String getDefaultSubdirectory() {
		return defaultSubDirectory;
	}

	/**
	 * Set a subdirectory to use when writing the hdf files. This is a subdirectory at the default datadirectory location
	 * (gda.data.scan.datawriter.datadir) and is used if no explicit filePath has been set (using {@link #setFilePath(String)}).
	 * @param defaultSubdirectory
	 */
	public void setDefaultSubdirectory(String defaultSubDirectory) {
		this.defaultSubDirectory = defaultSubDirectory;
	}
}
