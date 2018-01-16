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

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileHDF5;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.detector.DetectorBase;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NexusDetector;
import gda.device.detector.TfgFFoverI0;
import gda.device.detector.xspress.Xspress2Detector;
import gda.device.detector.xspress.xspress2data.Xspress2CurrentSettings;
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.epics.ReadOnlyPV;
import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.xspress.ResGrades;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.devices.detector.FluorescenceDetector;
import uk.ac.gda.devices.detector.FluorescenceDetectorParameters;
import uk.ac.gda.devices.detector.xspress3.Xspress3Controller;
import uk.ac.gda.devices.detector.xspress3.Xspress3Detector;
import uk.ac.gda.devices.detector.xspress3.controllerimpl.EpicsXspress3Controller;
import uk.ac.gda.util.beans.xml.XMLHelpers;

/**
 * Class to setup and readout from Xspress4 detector.<p>
 * Setup and running of the detector is similar to Xspress3 (i.e. through Epics via PVs) but data produced and to be written
 * to the Nexus file is similar to Xspress2. All detector data will be contained in the hdf file produced by the detector,
 * and a small subset of will be added to the Nexus file {@link #readout()} method, depending on the readout mode setting
 * (i.e. resolution grade information, deadtime correction calculation information, in-window sum, total FF, etc). <p>
 *
 * The strategy is to store settings for the collection using the {@link XspressParameters} class (as used by {@link XSpress2}), and
 * to delegate driving of the detector to {@link Xspress3Detector} and {@link Xspress3Controller} where possible, and create additional
 * PVs to obtain data where necessary.
 */
@SuppressWarnings("serial")
public class Xspress4Detector extends DetectorBase implements FluorescenceDetector, NexusDetector {

	private static final Logger logger = LoggerFactory.getLogger(Xspress4Detector.class);

	private Xspress3Detector detector;
	private Xspress3Controller controller;
	private TfgFFoverI0 tfgFFI0;

	private Xspress2CurrentSettings currentSettings;
	private XspressParameters parameters;
	private String configFileName;

	private Map<String, Integer> nexusScalerNameIndexMap = new HashMap<>();  // <scaler label, scaler number>
	private Map<String, Integer> asciiScalerNameIndexMap = new LinkedHashMap<>(); // <ascii column label, scaler number> ; linked hashmap so that keyset order is same as order in which keys were added

	// Standard scalers for each detector element, separate PV for each scaler type
	private static final String SCA_TEMPLATE = ":C%d_SCA%d:Value_RBV"; // detectorElement, sca number
	private ReadOnlyPV<Double>[][] pvForScaler = null; // [detectorElement][scalerNumber].scalerNumber=0...7

	// Standard scalers for each detector element, provided as an array
	private static final String SCA_ARRAY_TEMPLATE = ":C%d_SCAS"; // detectorElement
	private ReadOnlyPV<Double[]>[] pvForScalerArray = null; // [detectorElement]scalerNumber=0...7

	// Resolution grade values for each detector element (array of 16 in-window counts, one value for each resolution grade)
	private static final String RES_GRADE_TEMPLATE = ":C%d_SCA%d_RESGRADES"; // detectorElement, SCA{5,6}
	private ReadOnlyPV<Double[]>[][] pvForResGradeArray = null; // [detector element][window number]

	// Array of MCA for each detector element (summed over all res grades)
	private static final String ARRAY_DATA_TEMPLATE = ":ARR%d:ArrayData"; //detectorElement
	private ReadOnlyPV<Double[]>[] pvForArrayData; // [detectorElement]

	// PV to cause all array data PVs above to be updated (e.g. caput “1” to this)
	private static final String UPDATE_ARRAYS_TEMPLATE = ":UPDATE_ARRAYS";
	private PV<Integer> pvUpdateArrays = null;

	// PV to set the exposure time (used for Software triggered collection)
	private static final String ACQUIRE_TIME_TEMPLATE = ":AcquireTime";
	private PV<Double> pvAcquireTime = null;

	/** Trigger modes (caget -d31 BL20I-EA-XSP4-01:TriggerMode). Use 'TTL veto' for hardware triggered scans, 'Software' for software triggered scans*/
	public enum TriggerMode {Software, Hardware, Burst, TtlVeto, IDC, SoftwareStartStop, TtlBoth, LvdsVetoOnly, LvdsBoth};
	private TriggerMode currentTriggerMode = TriggerMode.Software;

	private static final String TRIGGER_MODE_TEMPLATE = ":TriggerMode";
	private PV<TriggerMode> pvTriggerMode = null;

	private static final String ARRAY_COUNTER = ":ArrayCounter";
	private PV<Integer> pvArrayCounter = null;
	private int numFramesReadoutAtPointStart;

	private static final String DTC_FACTORS = ":DTC_FACTORS";
	private ReadOnlyPV<Double[]> pvDtcFactors = null;

	private static final String ROI_RES_GRADE_BIN = ":ROI:BinY";
	private PV<Integer> pvRoiResGradeBin = null;

	private double caputTimeout = 10.0; // Timeout for CACLient put operations (seconds)

	private boolean dummyMode;

	@Override
	public void configure() throws FactoryException {
		super.configure();
		inputNames = new String[] {};
		if (controller == null) {
			logger.warn("Controller has not been set");
		} else {
			//Create xspress3 detector object
			detector = new Xspress3Detector();
			detector.setName("detectorFor"+getName());
			detector.setController(controller);
			detector.setFilePrefix(getName());
			detector.setWriteHDF5Files(true);
			detector.setFilePath("/dls/i20/data/2017/cm16762-4/spool/");
			detector.configure();
		}

		// setup PVs for talking to the detector
		setupPvs();

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
	}

	public Xspress3Controller getController() {
		return controller;
	}

	public void setController(Xspress3Controller controller) {
		this.controller = controller;
	}

	@Override
	public void atScanStart() throws DeviceException {
		try {
			setTriggerMode(currentTriggerMode);
			if (currentTriggerMode==TriggerMode.Software) {
				setAcquireTime(getCollectionTime());
			}

			if (!dummyMode) {
				// reset counter for total number of frames read out
				pvArrayCounter.putWait(0, caputTimeout);

				// Setup ROI binning to integrate over resolution grade bins if using Scalers/Scalers+MCA mode
				int newResGradeBin = 16;
				// Set ROI Y bin for resolution grade readout
				if (parameters.getReadoutMode().equals(XspressParameters.READOUT_MODE_REGIONSOFINTEREST)) {
					newResGradeBin = 1;
				}
				if (pvRoiResGradeBin.get() != newResGradeBin) {
					pvRoiResGradeBin.putWait(newResGradeBin);
					getMCAData(500); // collect frame of data (so that new bin setting is picked up by hdf writer)
				}
			}
			detector.atScanStart();

		} catch (IOException e) {
			logger.error("Problem setting 'Total Frames Readout' to 0 at scan start", e);
			throw new DeviceException(e);
		}
	}

	public void setAcquireTime(double time) {
		if (dummyMode) {
			return;
		}
		try {
			pvAcquireTime.putWait(time, caputTimeout);
		} catch (IOException e) {
			logger.error("Problem setting collection time to {} seconds", time, e);
		}
	}

	@Override
	public void atScanEnd() throws DeviceException {
		detector.atScanEnd();
		controller.setSavingFiles(false); //stop the filewriter

		// create link to hdf file...
		if (detector.isWriteHDF5Files()) {
			String path = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation().getFilename();
			try (NexusFile nexusFile = NexusFileHDF5.openNexusFile(path)) {
				URI hdfFile = new URI(controller.getFullFileName() + "#entry/data/data");
				String nexusLinkName = "/entry1/" + getName() + "/MCAs";
				nexusFile.linkExternal(hdfFile, nexusLinkName, false);
				nexusFile.close();
			} catch (Exception e) {
				logger.error("Problem creating link to hdf file in nexus", e);
			}
		}
	}

	@Override
	public void atPointEnd() throws DeviceException {
		detector.atPointEnd();
	}

	public void acquireFrameAndWait() throws DeviceException {
		acquireFrameAndWait(getCollectionTime()*1000, getCollectionTime()*1000);
	}

	private void waitForCounterToIncrement(int numFramesBeforeAcquire, double timeoutMillis) throws DeviceException, InterruptedException {
		final long pollIntervalMillis = 50;
		for (long i = 0; i < timeoutMillis
				&& controller.getTotalFramesAvailable() == numFramesBeforeAcquire; i += pollIntervalMillis) {
			Thread.sleep(pollIntervalMillis);
		}
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
		int numFramesBeforeAcquire = controller.getTotalFramesAvailable();
		logger.info(":Acquire called");
		controller.doStart();
		try {
			Thread.sleep((long)collectionTimeMillis);
			waitForCounterToIncrement(numFramesBeforeAcquire, timeoutMillis);
		} catch (InterruptedException e) {
			logger.warn("Interrupted while waiting for acquire");
		}
		logger.info("Wait for acquire finished");
		if (controller.getTotalFramesAvailable()==numFramesBeforeAcquire) {
			logger.warn("Acquire not finished after waiting for {} secs", timeoutMillis*0.001);
		}
	}

	@Override
	public void atScanLineStart() throws DeviceException {
		detector.setFramesRead(0);
		controller.setSavingFiles(detector.isWriteHDF5Files());
		controller.doErase();

		// Start Acquire if using hardware triggering (i.e. detector waits for external trigger for each frame)
		if (!dummyMode && currentTriggerMode!=TriggerMode.Software) {
			controller.doStart();
		}
	}

	@Override
	public void atPointStart() throws DeviceException {
		// collect new frame of data (software trigger only)
		if (!dummyMode) {
			if (currentTriggerMode==TriggerMode.Software) {
				acquireFrameAndWait();
			} else {
				// Get number of frames available from array counter
				numFramesReadoutAtPointStart = controller.getTotalFramesAvailable();
			}
		}
	}

	@Override
	public void stop() throws DeviceException {
		detector.stop();
	}

	@Override
	public int getStatus() throws DeviceException {
		return detector.getStatus();
	}

	@Override
	public void collectData() throws DeviceException {
		// do nothing...
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return detector.createsOwnFiles();
	}

	/**
	 * Return array with current values of MCA data
	 * @return array of data [num detector elements, number of MCA channels]
	 * @throws IOException
	 * @throws DeviceException
	 */
	public double[][] getMcaData() throws IOException, DeviceException {
		if (dummyMode) {
			return controller.readoutDTCorrectedLatestMCA(0, detector.getNumberOfElements() - 1);
		} else {
			// Update the arrays
			pvUpdateArrays.putWait(1, caputTimeout);
			double[][] mcaData = new double[detector.getNumberOfElements()][];
			for(int i=0; i<detector.getNumberOfElements(); i++) {
				mcaData[i] = ArrayUtils.toPrimitive(pvForArrayData[i].get());
			}
			return mcaData;
		}
	}

	@Override
	public double[][] getMCAData(double timeMillis) throws DeviceException {
		double mcaData[][] = null;
		try {
			controller.doStop();

			// Store the currently set trigger mode
			TriggerMode trigMode = getTriggerMode();

			//Set software trigger mode, collection for 1 frame of data
			setTriggerMode(TriggerMode.Software);
			controller.setNumFramesToAcquire(1);
			setAcquireTime(timeMillis*0.001);

			// Record frame of data on detector
			controller.doErase();
			acquireFrameAndWait(timeMillis, timeMillis);
			controller.doStop();

			// Reset trigger mode to original value
			setTriggerMode(trigMode);

			mcaData = getMcaData();
		} catch (IOException e) {
			logger.error("Problem getting MCA data", e);
		}
		return mcaData;
	}

	@Override
	public int getNumberOfElements() {
		return detector.getNumberOfElements();
	}

	@Override
	public int getMCASize() {
		return detector.getMCASize();
	}

	@Override
	public int getMaxNumberOfRois() {
		return detector.getMaxNumberOfRois();
	}

	@Override
	public FluorescenceDetectorParameters getConfigurationParameters() {
		return parameters;
	}

	public String getConfigFileName() {
		return configFileName;
	}

	public void setConfigFileName(String configFileName) {
		this.configFileName = configFileName;
	}

	public Xspress3Detector getDetector() {
		return detector;
	}

	@Override
	public String[] getExtraNames() {
		return currentSettings.getExtraNames();
	}

	@Override
	public String[] getOutputFormat() {
		return currentSettings.getOutputFormat();
	}

	public void setScalerNameIndexMap(Map<String,Integer> scalerIndexMap) {
		this.nexusScalerNameIndexMap = scalerIndexMap;
	}

	public Map<String,Integer> getScalerNameIndexMap() {
		return nexusScalerNameIndexMap;
	}

	public void loadConfigurationFromFile(String configFilename) {
		if (StringUtils.isEmpty(configFilename)) {
			return;
		}
		try {
			parameters = (XspressParameters) XMLHelpers.createFromXML(XspressParameters.mappingURL, XspressParameters.class, XspressParameters.schemaURL, configFilename);
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
		switch(resGrade[0]) {
			case ResGrades.NONE 	 : mcaGrades = 1; break;
			case ResGrades.THRESHOLD : mcaGrades = 2;
			threshold = Double.parseDouble(resGrade[1]);
			break;
			case ResGrades.ALLGRADES : mcaGrades = 16; break;
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
			controller.setWindows(elementNumber, windowNumber, new int[] { element.getWindowStart(), element.getWindowEnd() });
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
				controller.setWindows(elementNumber, i, new int[] {start, end});
			}
		}
	}

	/**
	 * Create PVs used for reading SCA (scaler), MCA and resolution grade data,
	 * setting trigger mode, collection time, updating MCA data arrays.
	 */
	public void setupPvs() {
		dummyMode = true;

		if (controller != null && controller instanceof EpicsXspress3Controller) {
			dummyMode = false;
			int numScalers = 8;
			int numElements = detector.getNumberOfElements();

			String pvBase = ((EpicsXspress3Controller) controller).getEpicsTemplate();

			pvForScaler = new ReadOnlyPV[numElements][numScalers];
			pvForScalerArray = new ReadOnlyPV[numElements];
			pvForResGradeArray = new ReadOnlyPV[numElements][2];
			pvForArrayData  = new ReadOnlyPV[numElements];

			for (int element = 0; element < numElements; element++) {

				String pvnameScalerArray = pvBase + String.format(SCA_ARRAY_TEMPLATE, element+1);
				pvForScalerArray[element] = LazyPVFactory.newReadOnlyDoubleArrayPV(pvnameScalerArray);

				String pvnameResGrade = pvBase + String.format(RES_GRADE_TEMPLATE, element+1, 5);
				pvForResGradeArray[element][0] = LazyPVFactory.newReadOnlyDoubleArrayPV(pvnameResGrade);

				pvnameResGrade = pvBase + String.format(RES_GRADE_TEMPLATE, element+1, 6);
				pvForResGradeArray[element][1] = LazyPVFactory.newReadOnlyDoubleArrayPV(pvnameResGrade);

				String pvnameArrayData = pvBase + String.format(ARRAY_DATA_TEMPLATE, element+1);
				pvForArrayData[element] = LazyPVFactory.newReadOnlyDoubleArrayPV(pvnameArrayData);

				for (int sca = 0; sca < numScalers; sca++) {
					String pvnameScaler = pvBase + String.format(SCA_TEMPLATE, element+1, sca);
					pvForScaler[element][sca] = LazyPVFactory.newReadOnlyDoublePV(pvnameScaler);
				}
			}

			pvTriggerMode = LazyPVFactory.newEnumPV(pvBase + TRIGGER_MODE_TEMPLATE, TriggerMode.class);
			pvAcquireTime = LazyPVFactory.newDoublePV(pvBase + ACQUIRE_TIME_TEMPLATE);
			pvUpdateArrays = LazyPVFactory.newIntegerPV(pvBase + UPDATE_ARRAYS_TEMPLATE);
			pvArrayCounter = LazyPVFactory.newIntegerPV(pvBase + ARRAY_COUNTER);
			pvDtcFactors = LazyPVFactory.newReadOnlyDoubleArrayPV(pvBase + DTC_FACTORS);
			pvRoiResGradeBin = LazyPVFactory.newIntegerPV(pvBase + ROI_RES_GRADE_BIN);
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
	 * Readout scaler values for each detector element
	 *
	 * @param scalerNumber
	 *  index of scaler to get values from (0...7), corresponding to following quantities :
	 *            <li>0 = time
	 *            <li>1 = reset ticks
	 *            <li>2 = reset counts
	 *            <li>3 = all events
	 *            <li>4 = all good events
	 *            <li>5 = counts in window 1
	 *            <li>6 = counts in window 2
	 *            <li>7 = pileup events
	 * @return 1-d array containing scaler the value for each detector element.
	 * @throws DeviceException
	 * @throws IOException
	 */
	public double[] readoutScaler(int scalerNumber) throws DeviceException, IOException {
		if (scalerNumber < 0 || scalerNumber > 7) {
			logger.warn("Scaler number {} is outside of expected range 0...7", scalerNumber);
			return null;
		}

		int numElements = detector.getNumberOfElements();
		double[] results = new double[numElements];
		if (!dummyMode) {
			for (int i = 0; i < numElements; i++) {
				results[i] = pvForScaler[i][scalerNumber].get();
			}
		} else {
			if (scalerNumber == 5) {
				results = ArrayUtils.toPrimitive(controller.readoutDTCorrectedSCA1(0, 0, 0, numElements - 1)[0]);
			} else if (scalerNumber == 6) {
				results = ArrayUtils.toPrimitive(controller.readoutDTCorrectedSCA2(0, 0, 0, numElements - 1)[0]);
				for(int i=0;i<results.length; i++) {
					results[i]+=1.0;
				}
			} else {
				// Get 'non window count' scaler data from controller
				// Data order : int[frame][channel][time,reset ticks, reset counts,all events, all goodEvents, pileup counts]
				Integer[][][] data = controller.readoutScalerValues(0, 0, 0, numElements - 1);
				int indexInData = scalerNumber;
				if (scalerNumber == 7) {
					indexInData = 5;
				}
				// Extract numbers for selected scaler :
				for (int i = 0; i < numElements; i++) {
					results[i] = data[0][i][indexInData].doubleValue();
				}
			}
		}
		return results;
	}

	/**
	 * Return deadtime correction data. Num scalers is usually 8;
	 * @return Dataset [num detector elements, num scalers]
	 * @throws IOException
	 */
	private Dataset getDeadtimeScalerData() throws IOException {
		int numElements = detector.getNumberOfElements();
		Double[][] allScalerData = new Double[numElements][8];  // [num elements][num scalers]

		// Get array of scaler values for each detector element
		for(int i=0; i<detector.getNumberOfElements(); i++) {
			if (dummyMode) {
				Double[] dataForElement = new Double[8];
				for(int j=0; j<dataForElement.length; j++) {
					dataForElement[j] = i*100.0 + j;
				}
				allScalerData[i] = dataForElement;
			} else {
				allScalerData[i] = pvForScalerArray[i].get();
			}
		}
		return DatasetFactory.createFromObject(allScalerData, Dataset.FLOAT64);
	}

	/**
	 * Add dead time correction data to Nexus tree and plottable data (i.e. values of SCA0 ... SCA7 for each channel)
	 * @param detectorTree
	 * @throws DeviceException
	 * @throws IOException
	 */
	private void addDeadtimeScalerData(INexusTree detectorTree, NXDetectorData frame) throws DeviceException, IOException {
		Dataset deadtimeScalerData = getDeadtimeScalerData();
		int numElements = deadtimeScalerData.getShape()[0];

		// Copy array of scaler values and add to Nexus tree
		// (each entry is scaler values of particular type for all det. elements)
		for(String scalerName : nexusScalerNameIndexMap.keySet() ) {
			int scalerIndex = nexusScalerNameIndexMap.get(scalerName);
			double[] scalerData = new double[numElements];
			for(int i=0; i<numElements; i++) {
				scalerData[i] = deadtimeScalerData.getDouble(i, scalerIndex);
			}
			NXDetectorData.addData(detectorTree, scalerName, new NexusGroupData(scalerData), "counts", 1);
		}

		// Add the deadtime factors to Nexus tree
		double[] dtcFactors = getDtcFactors();
		NXDetectorData.addData(detectorTree, "dtc factors", new NexusGroupData(dtcFactors), "value", 1);

		// Add dead-time correction values for each detector element to to ascii output :
		if (parameters.isShowDTRawValues()) {
			// allEvents, reset ticks, inWindow events, tfgClock
			String[] extraNames = getExtraNames();
//			int nameIndex = ArrayUtils.indexOf(extraNames, "FF")+1;

			//Find index of first column with deadtime correction data (i.e. Element 0_allEvents)
			int nameIndex = 0;
			String firstColumnName = asciiScalerNameIndexMap.keySet().iterator().next();
			for(nameIndex=0; nameIndex<extraNames.length; nameIndex++) {
				if (extraNames[nameIndex].endsWith(firstColumnName)) {
					break;
				}
			}

			// Index start for deadtime correction data is immediately after FF column
			for(int i=0; i<numElements; i++) {
				for(int scalerIndex : asciiScalerNameIndexMap.values()) {
					frame.setPlottableValue(extraNames[nameIndex++], deadtimeScalerData.getDouble(i, scalerIndex));
				}
			}
		}
	}

	/**
	 * Return non deadtime corrected resolution grade data (in window counts for each detector element for all resolution grades)
	 * @return Dataset [num detector elements, num res grades]
	 * @throws IOException
	 */
	private Dataset getResGradeData(int windowNumber) throws IOException {
		int numDetectorElements = detector.getNumberOfElements();
		Dataset thresholdData;

		if (dummyMode) {
			int numGrades = 16;
			thresholdData = DatasetFactory.zeros(new int[] { numDetectorElements, numGrades }, Dataset.FLOAT64);
			// set some values in dataset to assist debugging
			for (int i = 0; i < numDetectorElements; i++) {
				double val = i * (100+windowNumber);
				for (int j = 0; j < numGrades; j++) {
					thresholdData.set(val + j, i, j);
				}
			}
		} else {

			// Read resolution grade data from PVs, add to nexus tree
			Double[][] resgradeForDetectorElement = new Double[numDetectorElements][];
			for(int i=0; i<numDetectorElements; i++) {
				resgradeForDetectorElement[i] = pvForResGradeArray[i][windowNumber].get();
			}
			// Make Dataset to add to nexustree (NexusGroup can't be created for Double[][])
			thresholdData = DatasetFactory.createFromObject(resgradeForDetectorElement, Dataset.FLOAT64);
		}
		return thresholdData;
	}

	/**
	 *
	 * @return Return deadtime correction factor for each detector element. This is a multiplicative factor
	 * that can be applied to in-window scaler counts to correct for missed photon counts.
	 * @throws IOException
	 */
	private double[] getDtcFactors() throws IOException {
		if (dummyMode) {
			double[] vals = new double[detector.getNumberOfElements()];
			for(int i=0; i<vals.length; i++) {
				vals[i] = 1 + 0.001*i;
			}
			return vals;
		} else {
			return ArrayUtils.toPrimitive(pvDtcFactors.get());
		}
	}

	/**
	 * Apply deadtime correction factors to 'raw' (i.e. non deadtime corrected) scaler values.
	 * @param rawScalerCounts
	 * @return
	 * @throws IOException
	 */
	private double[] getDtcScalerValues(double[] rawScalerCounts, double[] dtcFactors) {
		double[] dtcScalerCounts = new double[rawScalerCounts.length];
		for(int i=0; i<rawScalerCounts.length; i++) {
			dtcScalerCounts[i] = dtcFactors[i] * rawScalerCounts[i];
		}
		return dtcScalerCounts;
	}

	/**
	 * Compute sum over specified resolution grades for each detector element :
	 * @param thresholdData resolution grade data [num detector elements, num resolution grades]
	 * @param dtcFactors deadtime correction factors [num detector elements]; set to null to not apply deadtime correction
	 * @param start start grade for sum
	 * @param end end grade for sum (exclusive)
	 * @return Dataset [num detector elements]
	 */
	private Dataset getSumOverResgrades(Dataset thresholdData, double[] dtcFactors, int start, int end) {
		int numDetectorElements = thresholdData.getShape()[0];
		double[] sum = new double[numDetectorElements];
		for (int element = 0; element < numDetectorElements; element++) {
			// sum the resolution grade values
			sum[element] = (double)thresholdData.getSlice(new int[]{element, start}, new int[]{element+1, end}, null).sum();

			// correct for deadtime
			if (dtcFactors != null) {
				sum[element] *= dtcFactors[element];
			}
		}
		return DatasetFactory.createFromObject(sum, Dataset.FLOAT64);
	}

	/**
	 * Add in-window resolution grade data (for all 16 resolution grades) to Nexus tree and plottable data
	 * @param detectorTree
	 * @throws IOException
	 */
	private void addResolutionGradeData(INexusTree detectorTree, NXDetectorData frame, int windowNumber) throws IOException {
		Dataset thresholdData = getResGradeData(windowNumber);
		double[] dtcFactors = getDtcFactors();

		String roiName = parameters.getDetector(0).getRegionList().get(windowNumber).getRoiName();

		NXDetectorData.addData(detectorTree, roiName+"_resgrade", NexusGroupData.createFromDataset(thresholdData), "counts", 1);

		// Following data are plotted and put in Ascii file, but *not* added to Nexus (same as Xspress2)
		int numDetectorElements = thresholdData.getShape()[0];
		int numResgrades = thresholdData.getShape()[1];
		boolean showOnlyFF = parameters.isOnlyShowFF();

		for(int i=0; i<numDetectorElements; i++) {
			logger.debug("Sum over threshold for {} : {}", i, (double) thresholdData.getSlice(new int[]{i, 0}, new int[]{i+1, numResgrades}, null).sum());
		}

		// Compute 'res_bin_norm' values (value0 = 15, value1 = 15+14, value2 = 15+14+13 etc.)
		// (Deadtime corrected)
		String[] extraNames = getExtraNames();
		double[] resgradeSumArray = new double[numResgrades];
		double i0Counts = getI0();
		if (!showOnlyFF) {
			int nameIndex = 0;
			// sum resgrades from n...15, n=15,14,13...1
			for(int numGradesInSum = 1; numGradesInSum<=numResgrades; numGradesInSum++) {
				Dataset gradeSum = getSumOverResgrades(thresholdData, dtcFactors, numResgrades-numGradesInSum, numResgrades);
				resgradeSumArray[nameIndex] = (double)gradeSum.sum();
				frame.setPlottableValue(extraNames[nameIndex], resgradeSumArray[nameIndex]/i0Counts);
				nameIndex++;
			}
		}

		// best 8 resolution grades per detector element (sum taken from Xspress2NexusTreeProvider.extractPartialMCA)
		// (*not* deadtime corrected)
		for(int i=0; i<numDetectorElements; i++) {
			double best8Sum = (double) thresholdData.getSlice(new int[]{i, numResgrades-8}, new int[]{i+1, numResgrades}, null).sum();
			if (!showOnlyFF) {
				frame.setPlottableValue(extraNames[i+numResgrades], best8Sum);
			}
		}

		// FFsum (sum over all resolution grades, over all elements)
		double FFsum = resgradeSumArray[numResgrades-1];
		frame.setPlottableValue("FF", FFsum);
		NXDetectorData.addData(detectorTree, "FF", new NexusGroupData(FFsum), "counts", 1);
	}

	/**
	 * Add 'good' and 'bad' resolution grade data to Nexus tree and plottable data.
	 * These are the deadtime corrected 'good' and 'bad' in-window counts summed over resolution
	 * grades above and below a user specified threshold :
	 * <li>'Bad' count = sum of grades over range (0, threshold-1).
	 * <li>'Good' count = sum of grades over range (threshold, numResgrades)
	 * @param detectorTree
	 * @param frame
	 * @param windowNumber
	 * @throws IOException
	 */
	public void addResolutionThresholdData(INexusTree detectorTree, NXDetectorData frame, int windowNumber) throws IOException {
		Dataset thresholdData = getResGradeData(windowNumber);
		double[] dtcFactors = getDtcFactors();
		int numDetectorElements = thresholdData.getShape()[0];
		int numResgrades = thresholdData.getShape()[1];
		int threshold = getResolutionThreshold();

		// Get bad and good grade counts for each detector element (deadtime corrected)

		Dataset badGradeCounts = getSumOverResgrades(thresholdData, dtcFactors, 0, threshold);
		Dataset goodGradeCounts = getSumOverResgrades(thresholdData, dtcFactors, threshold, numResgrades);
		// Sum of bad and good grade counts across all detector elements
		double badGradeCountsAllElements = (double)badGradeCounts.sum();
		double goodGradeCountsAllElements = (double)goodGradeCounts.sum();

		// Add bad and good total counts
		frame.setPlottableValue("FF_bad", badGradeCountsAllElements);
		frame.setPlottableValue("FF", goodGradeCountsAllElements);

		// Good counts for each element
		if (!parameters.isOnlyShowFF()) {
			String[] extraNames = getExtraNames();
			for (int i = 0; i < numDetectorElements; i++) {
				frame.setPlottableValue(extraNames[i], goodGradeCounts.getDouble(i));
			}
		}

		NXDetectorData.addData(detectorTree, "good_counts", NexusGroupData.createFromDataset(goodGradeCounts), "counts", 1);
		NXDetectorData.addData(detectorTree, "bad_counts", NexusGroupData.createFromDataset(badGradeCounts), "counts", 1);
	}

	/**
	 * Add sum over all resolution grade data to Nexus tree and plottable data
	 *
	 * @param detectorTree
	 * @param frame
	 * @param windowNumber
	 * @throws IOException
	 */
	private void addResolutionGradeSumData(INexusTree detectorTree, NXDetectorData frame, int windowNumber) throws IOException {
		Dataset thresholdData = getResGradeData(windowNumber);
		boolean showOnlyFF = parameters.isOnlyShowFF();
		int numDetectorElements = thresholdData.getShape()[0];
		int numResGrades = thresholdData.getShape()[1];
		double[] dtcFactors = getDtcFactors();

		// Compute FF sum for each detector element
		double[] roiData = new double[numDetectorElements];
		double ffForAllDetElements = 0;
		String roiName = parameters.getDetector(0).getRegionList().get(windowNumber).getRoiName();
		for (int i = 0; i < numDetectorElements; i++) {
			double ffForWindow = 0.0;
			if (!parameters.getDetector(i).isExcluded()) {
				ffForWindow = (double) thresholdData.getSlice(new int[] { i, 0 }, new int[] { i + 1, numResGrades }, null).sum();
				ffForWindow *= dtcFactors[i]; // apply deadtime correction factor
				if (!showOnlyFF) {
					String ffName = parameters.getDetector(i).getName() + "_" + roiName;
					frame.setPlottableValue(ffName, ffForWindow);
				}
			}
			roiData[i] = ffForWindow;
			ffForAllDetElements += ffForWindow;
		}
		// Add FF sum for each element to Nexus
		NXDetectorData.addData(detectorTree, "FF_" + roiName, new NexusGroupData(roiData), "counts", 1);

		// Add the sum over all detector elements
		// (overwrites previously 'plottable' FF value when using multiple ROIs...)
		frame.setPlottableValue("FF", ffForAllDetElements);
		NXDetectorData.addData(detectorTree, "FF", new NexusGroupData(ffForAllDetElements), "counts", 1);
	}

	/**
	 * Add deadtime corrected scaler counts to plottable data; FF to Nexus tree and plottable data
	 * @param detectorTree
	 * @param frame
	 * @param window1CountData
	 * @param window2CountData
	 */
	private void addScalerData(INexusTree detectorTree, NXDetectorData frame, double[] window1CountData, double[] window2CountData, double[] dtcFactor) {
		String[] extraNames = getExtraNames();
		boolean showOnlyFF = parameters.isOnlyShowFF();

		double FFsum = 0.0;
		int nameIndex = 0;
		for (int i = 0; i < window1CountData.length; i++) {
			if (!parameters.getDetector(i).isExcluded()) {
				// deadtime corrected scaler counts
				double inWindowCount = window1CountData[i]*dtcFactor[i];

				FFsum += inWindowCount;
				if (!showOnlyFF) {
					frame.setPlottableValue(extraNames[nameIndex++], inWindowCount);
				}
				// Add values for window2 if needed
				if (window2CountData!=null && !showOnlyFF) {
					frame.setPlottableValue(extraNames[nameIndex++], window2CountData[i]*dtcFactor[i]);
				}
			}
		}
		frame.setPlottableValue("FF", FFsum);
		NXDetectorData.addData(detectorTree, "FF", new NexusGroupData(FFsum), "counts", 1);

		// Deadtime corrected in-window scaler counts
		double[] dtcWindowCounts = getDtcScalerValues(window1CountData, dtcFactor);
		NXDetectorData.addData(detectorTree, "scalers", new NexusGroupData(dtcWindowCounts), "counts", 1);
	}

	public NXDetectorData readFrame() throws DeviceException, IOException {
		// Construct nexus tree, add the detector data to it
		NXDetectorData frame = new NXDetectorData(currentSettings.getExtraNames(), currentSettings.getOutputFormat(), getName());
		INexusTree detTree = frame.getDetTree(getName());

		boolean regionOfInterest = parameters.getReadoutMode().equals(XspressParameters.READOUT_MODE_REGIONSOFINTEREST);
		int numRois = parameters.getDetector(0).getRegionList().size();
		int mcaGrades = currentSettings.getMcaGrades();

		// Get in window scaler counts for each element and calculate the FF sum (for 'included' detector elements only.
		double[] window1CountData = readoutScaler(5);
		double[] window2CountData = null;
		if (regionOfInterest && numRois==2) {
			window2CountData = readoutScaler(6);
		}

		// NB, when using multiple ROIs there seems to be only 1 'plottable' FF value...
		if (regionOfInterest) {
			if (mcaGrades == Xspress2Detector.ALL_RES) {
				// Resolution grades for multiple ROIs is not supported, (wasn't previously supported by Xspress2,
				// so getExtraNames() doesn't have necessary columns)
				addResolutionGradeData(detTree, frame, 0);
			} else if (mcaGrades == Xspress2Detector.NO_RES_GRADE) {
				for(int i=0; i<Math.min(numRois, 2); i++) {
					addResolutionGradeSumData(detTree, frame, i);
				}
			} else if (mcaGrades == Xspress2Detector.RES_THRES) {
				addResolutionThresholdData(detTree, frame, 0);
			}
		} else {
			double[] dtcFactors = getDtcFactors();
			addScalerData(detTree, frame, window1CountData, window2CountData, dtcFactors);
		}

		// Add deadtime related scaler data to Ascii and Nexus
		addDeadtimeScalerData(detTree, frame);

		// add link to the MCA data - need to do this at scan end or at least after hdf file has been created if using 'lazy open' in hdf writer)
//		if (detector.isWriteHDF5Files()) {
//			frame.addExternalFileLink(getName(), "MCAs", "nxfile://" + controller.getFullFileName() + "#entry/data/data", false, true);
//		}
		return frame;
	}

	private void waiterForCounter() throws Exception {
		try {
			waitForCounterToIncrement(numFramesReadoutAtPointStart, caputTimeout * 1000);
		} catch (DeviceException | InterruptedException e) {
			throw new Exception("Problem waiting for Array Counter to increment "+e);
		}
	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		NexusTreeProvider tree = null;
		try {
			logger.info("Readout from {} started", getName());
			waiterForCounter();
			tree = readFrame();
			logger.info("Readout from {} finished", getName());
		} catch (Exception e) {
			logger.warn("Problem encountered during readout()", e);
		}
		return tree;
	}

	public void setTriggerMode(TriggerMode mode) throws IOException {
		currentTriggerMode = mode;
		if (!dummyMode) {
			pvTriggerMode.putWait(mode, caputTimeout);
		}
	}

	/**
	 * Convenience method to set the trigger mode by using integer rather than {@link TriggerMode} enum.
	 * {@link TriggerMode} enum value set is TriggerMode.values()[intMode];
	 * @param intMode
	 * @throws IOException
	 */
	public void setTriggerMode(int intMode) throws IOException {
		int maxIntTriggerMode = TriggerMode.values().length-1;
		if (intMode < 0 || intMode > maxIntTriggerMode) {
			logger.warn("Cannot set trigger mode to {}, Value should be between 0 and {}", intMode, maxIntTriggerMode);
			return;
		}
		TriggerMode mode = TriggerMode.values()[intMode];
		setTriggerMode(mode);
	}

	/**
	 * Return the current trigger mode setting by reading from PV.
	 * @return
	 * @throws IOException
	 */
	public TriggerMode getTriggerMode() throws IOException {
		if (dummyMode) {
			return currentTriggerMode;
		} else {
			return pvTriggerMode.get();
		}
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
}
