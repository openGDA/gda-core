/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.mythen.epics;

import gda.data.fileregistrar.FileRegistrarHelper;
import gda.device.DeviceException;
import gda.device.detector.mythen.MythenDetectorImpl;
import gda.device.detector.mythen.data.MythenDataFileUtils.FileType;
import gda.device.detector.mythen.data.MythenProcessedDataset;
import gda.device.detector.mythen.data.MythenRawDataset;
import gda.device.detector.mythen.tasks.DataProcessingTask;
import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;
import gda.util.Sleep;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.devices.mythen.epics.MythenEpicsClient.Setting;

/**
 * Mythen detector (version 3) that uses EPICS client to acquire data from the detector. 
 * Note that Mythen 3 API is different from Mythen 2 API so this class is not backward compatible.
 */
public class MythenDetector extends MythenDetectorImpl implements IMythenDetector {
	
	private static final Logger logger = LoggerFactory.getLogger(MythenDetector.class);
	
	@SuppressWarnings("hiding")
	private MythenEpicsClient mythenClient;

	private ArrayList<File> processedDataFilesForScan=new ArrayList<File>();
	public MythenDetector() {
		
	}
	@Override
	public void configure() throws FactoryException {
		if (!isConfigured()) {
			super.configure();
			try { // default configuration for software triggered acquisition
//				configureDetectorForAutoModeAcquisition();
				setConfigured(true);
			} catch (Exception e) {
				logger.error("Failed to configure detector parameters", e);
				throw new FactoryException("Failed to configure detector parameters", e);
			}
		}
	}

	/**
	 * @throws IOException
	 * @throws Exception
	 */
	private void configureDetectorForAutoModeAcquisition() throws IOException, Exception {
		// fixed/shared file writer parameters
		setFileTemplate("%s_%d"); // name convention 
		enableAutoIncrement();
		enableAutoSave();
		setNumCycles(1);
		setNumFrames(1);
		setNumGates(1);
		autoMode();
		resetArrayCounter();
		// disable all data correction - just collect RAW data and use GDA to processing raw data
		disableDataCorrection();
	}

	/**
	 * @throws IOException
	 */
	private void disableDataCorrection() throws IOException {
		disableFlatFieldCorrection();
		disableCountRateCorrection();
		disableBadChannelCorrection();
		disableAngularConversion();
	}	
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if (detectorID == null) {
			throw new IllegalStateException("You have not set the detector ID");
		}

		if (getMythenClient() == null) {
			throw new IllegalStateException("You have not set a Mythen EPICS client to be used when collecting data");
		}

		if (dataConverter == null) {
			throw new IllegalStateException(
					"You have not set a data converter; this is needed for converting raw data to processed data");
		}
	}

	@Override
	public void atScanStart() throws DeviceException {
		super.atScanStart();
		if (!processedDataFilesForScan.isEmpty()) {
			processedDataFilesForScan.clear();
		}
		try {
			configureDetectorForAutoModeAcquisition();
		} catch (Exception e) {
			logger.error("Failed to configure the detector at scan start.", e);
			throw new DeviceException("Failed to configure the detector at scan start.", e);
		}
		try {
			setFilePath(getDataDirectory().getAbsolutePath());
			setFileName(getBaseFilename());
			setNextFileNumber((int)collectionNumber+1);	//set starting index number
		} catch (IOException e) {
			logger.error("Failed to set data output parameters", e);
			throw new DeviceException("Failed to set data output parameters.",e);
		}		
	}
	
	@Override
	public void setCollectionTime(double collectionTime) throws DeviceException {
		super.setCollectionTime(collectionTime);
		try {
			setExposureTime(collectionTime);
		} catch (Exception e) {
			logger.error("failed to set exposure time", e);
			throw new DeviceException("failed to set exposure time",e);
		}
	}
	
	/**
	 * override GDA format to match SLS detector data file name format.
	 */
	@Override
	protected String buildFilenameWithoutSuffix(int number) {
		return buildFilenameWithoutSuffix(String.format("%d", number));
	}
	/**
	 * override GDA format to match SLS detector data file name format.
	 */
	@Override
	protected String buildFilenameWithoutSuffix(String s) {
		return String.format("%d-mythen_%s", this.scanNumber, s);
	}
	
	protected String getBaseFilename() {
		return String.format("%d-mythen", this.scanNumber);
	}
	
	@Override
	public String buildFilename(String s, FileType type) {
		final String suffix = (type == FileType.PROCESSED) ? "dat" : "raw";
		return String.format("%d-mythen_%s.%s", this.scanNumber, s, suffix);
	}
	/**
	 * rebuild Raw data file name added for Jython use as Jython does not support enum Java type yet.
	 * @param number
	 * @return filename
	 */
	@Override
	public String buildRawFilename(int number) {
		return buildFilename(String.format("%d", number), FileType.RAW);
	}
	/**
	 * collect data from detector using EPICS client.
	 * This method is non-blocking.
	 */
	@Override
	public void collectData() throws DeviceException {
		beforeCollectData();
		Thread mythenAcquire = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					startWait();
					afterCollectData();
				} catch (DeviceException e) {
					logger.error("{}: acquisition failed." + getName(), e);
					throw new RuntimeException("Unable to collect data", e);
				} finally {
					status = IDLE;
				}
			}
		}, collectionFilename);
//		while (mythenAcquire.isAlive()) {
//			try {
//				Thread.sleep(50);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		mythenAcquire.start();
	}
	@Override
	protected void afterCollectData() {
//		super.afterCollectData();
		processedFile = new File(getDataDirectory(), collectionFilename + ".dat");
		// check if Mythen created raw data file successfully or not
		double timer=System.currentTimeMillis();
		double timerElapsed=0.0;
		while (!rawFile.exists() && timerElapsed < collectionTime*1000) { //checking for maximum of the collection time
			Sleep.sleep(100);
			timerElapsed=System.currentTimeMillis()-timer;
		}
		if (timerElapsed>= collectionTime*1000) {
			// failed to create raw data after exposure time, then return no further process of raw data.
			print("Detector "+ getName()+" failed to create RAW data file "+rawFile.getAbsolutePath());
			return;
		}
		// read data and process it
		rawData = new MythenRawDataset(rawFile);
		processedData = dataConverter.process(rawData, delta);
		processedData.save(processedFile, isHasChannelInfo());
		if (InterfaceProvider.getTerminalPrinter() != null) {
			InterfaceProvider.getTerminalPrinter().print("Save to file " + processedFile.getAbsolutePath());
		}
		FileRegistrarHelper.registerFiles(new String[] { rawFile.getAbsolutePath(), processedFile.getAbsolutePath() });

		status = IDLE;
		processedDataFilesForScan.add(processedFile);
	}
	@Override
	public void stop() throws DeviceException {
		try {
			getMythenClient().stop();
		} catch (Exception e) {
			logger.error("Fail to stop detector acquisition.", e);
			throw new DeviceException("Fail to stop detector acquisition.", e);
		}
		super.stop();
	}
	
	@Override
	public Object readout() throws DeviceException {
		String filename = processedFile.getName();
		return filename;
	}
	private List<DataProcessingTask> processingTasks = new Vector<DataProcessingTask>();
	
	@Override
	public void atScanEnd() throws DeviceException {
		super.atScanEnd();
		for (DataProcessingTask task : getProcessingTasks()) {
			task.run(this);
		}
	}	
	//############### special methods for multiple frames, triggered, gated collections
	/**
	 * Captures multiple frames using a software trigger.
	 * Deprecated, please use {@link #multi(int, double, double)}.
	 * @param numFrames
	 * @param delayTime
	 * @param exposureTime
	 * @param delayAfterFrames - redundant input kept only for backward compatibility.
	 * @throws DeviceException
	 */
	@Override
	@Deprecated 
	public void multi(int numFrames, double delayTime, double exposureTime, double delayAfterFrames)
			throws DeviceException {
		_multi(TriggerMode.AUTO, 1, numFrames, delayTime, 1, exposureTime);
	}

	/**
	 * Captures multiple frames using a software trigger.
	 * @param numFrames
	 * @param delayTime
	 * @param exposureTime
	 * @throws DeviceException
	 */
	@Override
	public void multi(int numFrames, double delayTime, double exposureTime) throws DeviceException {
		_multi(TriggerMode.AUTO, 1, numFrames, delayTime, 1, exposureTime);
	}
	/**
	 * Captures multiple frames using a single trigger to start acquisition of all frames.
	 * @param numFrames
	 * @param delayTime
	 * @param exposureTime
	 * @throws DeviceException
	 */
	@Override
	public void smulti(int numFrames, double delayTime, double exposureTime) throws DeviceException {
		_multi(TriggerMode.TRIGGER, 1, numFrames, delayTime, 1, exposureTime);
	}

	/**
	 * Captures multiple frames using one trigger per frame.
	 * @param numCycles
	 * @param numFrames
	 * @param delayTime
	 * @param exposureTime
	 * @throws DeviceException
	 */
	@Override
	public void cmulti(int numCycles, int numFrames, double delayTime, double exposureTime) throws DeviceException {
		_multi(TriggerMode.TRIGGER, numCycles, numFrames, delayTime, 1, exposureTime);
	}
	/**
	 * MYTHEN 2 API no longer supported. Please use alternative {@link #cmulti(int, int, double, double)}.
	 * @param numFrames
	 * @param delayTime
	 * @param exposureTime
	 * @throws DeviceException
	 */
	@Deprecated
	@Override
	public void cmulti(int numFrames, double delayTime, double exposureTime) throws DeviceException {
		throw new RuntimeException("Mythen 2 API is no longer supported. Please use Mythen 3 API with same methd name and an extra numCycle parameter as first input parameter.");
	}
	
	private void _multi(TriggerMode trigger, int numCycles, int numFrames, double delayTime, int numGates, double exposureTime) throws DeviceException {
		long scanNumber = scanNumTracker.incrementNumber();
		final File dataDirectory = getDataDirectory();
		
		_acquire(trigger, numCycles, numFrames, delayTime, numGates, scanNumber, dataDirectory, -1, exposureTime, true);
	}
	/**
	 * method to print message to the Jython Terminal console.
	 * 
	 * @param msg
	 */
	private void print(String msg) {
		if (InterfaceProvider.getTerminalPrinter() != null) {
			InterfaceProvider.getTerminalPrinter().print(msg);
		}
	}
	/**
	 * gated multiple frames collection - one file per frame, numGates per frames
	 * @param numFrames - the number of frames to collect
	 * @param numGates - the number of gates per frame to expose
	 */
	@Override
	public void gated(int numFrames, int numGates) throws DeviceException {
		long scanNumber = scanNumTracker.incrementNumber();
		final File dataDirectory = getDataDirectory();
		gated(numFrames, numGates, scanNumber, dataDirectory, -1);
	}
	/**
	 * gated single frame collection.
	 * @param numGates the number of gates to expose.
	 */
	@Override
	public void gated(int numGates) throws DeviceException {
		gated(1, numGates);
	}

	/**
	 * gated multiple frames collection - one frame per file, numGates per frame, single cycle only
	 * Mythen detector controls the frame number increment starting from 0.
	 * exposure time is controlled by gate signal length
	 * Delay time = 0, 
	 * this acquisition waits for data correction and angular conversion to complete.
	 * 
	 * @param numFrames Number of frames to collects, i.e. number of data files to create
	 * @param numGates Number of gates for each frame
	 * @param scanNumber this acquisition number
	 * @param dataDirectory the data directory to save data to
	 * @param collectionNumber the index number for this acquisition - if <0 Mythen detector controls the frame number increment starting from 1.
	 * @throws DeviceException
	 */	
	@Override
	public void gated(int numFrames, int numGates, long scanNumber, File dataDirectory, int collectionNumber) throws DeviceException {
		_acquire(TriggerMode.GATING, 1, numFrames, 0, numGates, scanNumber, dataDirectory, collectionNumber, 0.0, true);
	}

	/**
	 * gated multiple frames collection - one frame per file, numGates per frame, only one cycle - 
	 * Mythen detector controls the frame number increment starting from 0.
	 * exposure time is controlled by gate signal length
	 * Delay time = 0, 
	 * does not wait for data correction and angular conversion, returns immediately after RAW data are collected. 
	 * 
	 * @param numFrames Number of frames to collects, i.e. number of data files to create
	 * @param numGates Number of gates for each frame
	 * @param scanNumber this acquisition number
	 * @throws DeviceException
	 */
	@Override
	public void gated(final int numFrames, int numGates, long scanNumber) throws DeviceException {
		_acquire(TriggerMode.GATING, 1, numFrames, 0, numGates, scanNumber, getDataDirectory(), -1, 0.0, false); //
	}

	/**
	 * gated multiple or single frame collection - one frame per file, numGates per frame - where GDA controls the collection
	 * number increment. This acquisition does not wait for data correction and angular conversion to complete before return.
	 * 
	 * @param numFrames
	 * @param numGates
	 * @param scanNumber
	 * @param collectionNumber
	 * @throws DeviceException
	 */
	@Override
	public void gated(final int numFrames, int numGates, long scanNumber, int collectionNumber) throws DeviceException {
		// must set scanNumber when operate outside GDA Scan command, e.g. in a for loop. This is normally set
		// atScanStart() in a scan
		this.scanNumber = scanNumber;
		// must set collection number when operate outside GDA Scan command, e.g. in a for loop. This is normally
		// initialised atScanStart() in a scan
		this.collectionNumber = collectionNumber;
		beforeCollectData(collectionNumber);
		_acquire(TriggerMode.GATING, 1, numFrames, 0.0, numGates, scanNumber, getDataDirectory(), collectionNumber, 0.0, false);
	}
	
	private void _acquire(TriggerMode trigger, final int numCycles, final int numFrames, double delayTime,
			int numGates, long scanNumber, final File dataDirectory, int collectionNumber, double exposureTime, boolean waitForDataCorrection)
			throws DeviceException {

		// Client uses the template "%s_%d.raw" when writing raw data files; this template includes a
		// placeholder the "raw" or "dat" suffix
		final String filenameTemplate;
		if (numFrames > 1) {
			filenameTemplate = "%s_f%d_%d.%s";
		} else {
			filenameTemplate = "%s_%d.%s";
		}

		// This is the prefix for all the raw/processed files
		final String prefix = String.format("%d-mythen", scanNumber);

		updateDeltaPosition();

		// configure detector
		logger.info("Configure detector");
		try {
			// file saving options
			setFileTemplate("%s_%d"); // name convention
			if (collectionNumber < 0) {
				// using EPICS file numbering system
				enableAutoIncrement();
				enableAutoSave();
				setNextFileNumber(1); //starting index number
			} else {
				// set our own file number
				disableAutoIncrement();
				disableAutoSave();
				setNextFileNumber(collectionNumber);
			}
			resetArrayCounter();
			// disable data correction - so we only want .raw data from the detector
			disableDataCorrection();

			setFilePath(dataDirectory.getAbsolutePath());
			setFileName(prefix);
			setTriggerMode(trigger.ordinal());
			setNumCycles(numCycles);
			setNumFrames(numFrames);
			setDelayTime(delayTime);
			setNumGates(numGates);
			setExposureTime(exposureTime);
		} catch (Exception e) {
			throw new DeviceException("failed to configure mythen detector before multi frames collection", e);
		}
		logger.info("Acquiring data");
		if (trigger == TriggerMode.TRIGGER) {
			print("Waiting for trigger_in signals ...");
		} else if (trigger == TriggerMode.GATING) {
			print("Waiting for gate_in signals ...");
		} else if (trigger == TriggerMode.RO_TRIGGER) {
			print("Waiting for ro_trigger_in signals ...");
		} else if (trigger == TriggerMode.TRIGGERRED_GATING) {
			print("Waiting for both trigger_in and gate_in signals ...");
		}
		startWait();

		// process data
		logger.info("Processing data");
		Thread processing = new Thread(new Runnable() { // post process raw data so it does not block acquisition

					@Override
					public void run() {
						logger.info("Processing data");
						afterCollectData(numCycles, numFrames, dataDirectory, filenameTemplate, prefix);
					}
				}, "DataProcessing");
		processing.start();
		if (waitForDataCorrection) {
			// wait for data processing thread to return
			while (processing.isAlive()) {
				Sleep.sleep(100);
			}
		}
		print("Acquisition completed.");
		logger.info("Finished");
	}

	private void afterCollectData(int numCycles, int numFrames, File dataDirectory, final String filenameTemplate,final String prefix) {
		print("Performe data corrections in 'DataProcessing' thread ...");
		for (int cycle = 1; cycle <= numCycles; cycle++) {
			File rawFile;
			if (numFrames > 1) {
				for (int frame = 1; frame <= numFrames; frame++) {
					//filename convention 'prefix_f%d_index.raw' for multiple frames acquisition
					rawFile = new File(dataDirectory, String.format(filenameTemplate, prefix, frame, cycle, "raw"));
					MythenRawDataset rawData = new MythenRawDataset(rawFile);
					MythenProcessedDataset processedData = dataConverter.process(rawData, delta);
					File processedFile = new File(dataDirectory, String.format(filenameTemplate, prefix, frame, cycle, "dat"));
					processedDataArchievalAndPlot(cycle, rawFile, processedData, processedFile);
				}
			} else {
				//filename convention 'prefix_index.raw' for single frame acquisition
				rawFile = new File(dataDirectory, String.format(filenameTemplate, prefix, cycle, "raw"));
				MythenRawDataset rawData = new MythenRawDataset(rawFile);
				MythenProcessedDataset processedData = dataConverter.process(rawData, delta);
				File processedFile = new File(dataDirectory, String.format(filenameTemplate, prefix, cycle, "dat"));
				processedDataArchievalAndPlot(cycle, rawFile, processedData, processedFile);
			}
		}
		print("Data correction and angular conversion completed.");
	}

	/**
	 * @param cycle
	 * @param rawFile
	 * @param processedData
	 * @param processedFile
	 */
	private void processedDataArchievalAndPlot(int cycle, File rawFile, MythenProcessedDataset processedData,
			File processedFile) {
		processedData.save(processedFile, isHasChannelInfo());
		print("Save to file " + processedFile.getAbsolutePath());
		FileRegistrarHelper.registerFiles(new String[] { rawFile.getAbsolutePath(), processedFile.getAbsolutePath() });
		if (cycle ==1) {
			atPointEnd(processedFile.getAbsolutePath(), processedData, true);
		} else {
			atPointEnd(processedFile.getAbsolutePath(), processedData, false);
		}
	}
	@Deprecated
	@Override
	protected void afterCollectData(File rawFile, int num) {
		throw new RuntimeException("Mythen 2 method - not supported in Mythen 3 any more");
	}
	@Deprecated
	@Override
	protected void afterCollectData(String collectionFilenameRoot, int numFiles) {
		throw new RuntimeException("Mythen 2 method - not supported in Mythen 3 any more");
	}

	@Override
	public MythenEpicsClient getMythenClient() {
		return mythenClient;
	}

	@Override
	public void setMythenClient(MythenEpicsClient mythenClient) {
		this.mythenClient = mythenClient;
	}
	@Override
	public void autoMode() throws Exception {
		getMythenClient().autoMode();
	}
	@Override
	public void triggerMode() throws Exception {
		getMythenClient().triggerMode();
	}
	@Override
	public void gatingMode() throws Exception {
		getMythenClient().gatingMode();
	}
	@Override
	public void ro_TriggerMode() throws Exception {
		getMythenClient().ro_TriggerMode();
	}
	@Override
	public void triggerredGatingMode() throws Exception {
		getMythenClient().triggerredGatingMode();
	}
	@Override
	public void setTriggerMode(int value) throws Exception {
		getMythenClient().setTriggerMode(value);
	}
	@Override
	public void setThreshold(double energy) throws Exception {
		getMythenClient().setThresholdEnergy(energy);
	}
	@Override
	public double getThreshold() throws Exception {
		return getMythenClient().getThresholdEnergy();
	}
	@Override
	public void setBeamEnergy(double energy) throws Exception {
		getMythenClient().setBeamEnergy(energy);
	}
	@Override
	public double getBeamEnergy() throws Exception {
		return getMythenClient().getsetBeamEnergy();
	}
	@Override
	public void standard() throws Exception {
		getMythenClient().setSetting(Setting.standard);
	}
	@Override
	public void fast() throws Exception {
		getMythenClient().setSetting(Setting.fast);
	}
	@Override
	public void highgain() throws Exception {
		getMythenClient().setSetting(Setting.highgain);
	}
	@Override
	public void setBitDepth(int bitDepth) throws Exception {
		getMythenClient().setBitDepth(bitDepth);
	}
	@Override
	public int getBitDepth() throws Exception {
		return getMythenClient().getBitDepth();
	}
	@Override
	public void setNumCycles(int value) throws Exception {
		getMythenClient().setNumCycles(value);
	}
	@Override
	public int getNumCycles() throws Exception {
		return getMythenClient().getNumCycles();
	}
	@Override
	public void setNumFrames(int value) throws Exception {
		getMythenClient().setNumFrames(value);
	}
	@Override
	public int getNumFrames() throws Exception {
		return getMythenClient().getNumFrames();
	}
	@Override
	public void setNumGates(int value) throws Exception {
		getMythenClient().setNumGates(value);
	}
	@Override
	public int getNumGates() throws Exception {
		return getMythenClient().getNumGates();
	}
	@Override
	public void setDelayTime(double value) throws Exception {
		getMythenClient().setDelayTime(value);
	}
	@Override
	public double getDelayTime() throws Exception {
		return getMythenClient().getDelayTime();
	}
	@Override
	public void setFilePath(String value) throws IOException {
		getMythenClient().setFilePath(value);
	}
	@Override
	public String getFilePath() throws Exception {
		return getMythenClient().getFilePath();
	}
	@Override
	public void setFileName(String value) throws IOException {
		getMythenClient().setFileName(value);
	}
	@Override
	public String getFileName() throws Exception {
		return getMythenClient().getFileName();
	}
	@Override
	public void setNextFileNumber(int value) throws IOException {
		getMythenClient().setNextFileNumber(value);
	}
	@Override
	public int getNextFileNumber() throws Exception {
		return getMythenClient().getNextFileNumber();
	}
	@Override
	public void enableAutoIncrement() throws IOException {
		getMythenClient().enableAutoIncrement();
	}
	@Override
	public void disableAutoIncrement() throws IOException {
		getMythenClient().disableAutoIncrement();
	}
	@Override
	public boolean isAutoIncrement() throws IOException {
		return getMythenClient().isAutoIncrement();
	}
	@Override
	public void enableAutoSave() throws IOException {
		getMythenClient().enableAutoSave();
	}
	@Override
	public void disableAutoSave() throws IOException {
		getMythenClient().disableAutoSave();
	}
	@Override
	public boolean isAutoSave() throws IOException {
		return getMythenClient().isAutoSave();
	}
	@Override
	public void setFileTemplate(String value) throws IOException {
		getMythenClient().setFileTemplate(value);
	}
	@Override
	public String getFileTemplate() throws Exception {
		return getMythenClient().getFileTemplate();
	}
	@Override
	public String getFullFileName() throws Exception {
		return getMythenClient().getFullFilename();
	}
	@Override
	public void setFlatFieldPath(String value) throws IOException {
		getMythenClient().setFlatFieldPath(value);
	}
	@Override
	public String getFlatFieldPath() throws Exception {
		return getMythenClient().getFlatFieldPath();
	}
	@Override
	public void setFlatFieldFile(String value) throws IOException {
		getMythenClient().setFlatFieldFile(value);
	}
	@Override
	public String getFlatFieldFile() throws Exception {
		return getMythenClient().getFlatFieldFile();
	}
	@Override
	public void enableFlatFieldCorrection() throws IOException {
		getMythenClient().enableFlatFieldCorrection();
	}
	@Override
	public void disableFlatFieldCorrection() throws IOException {
		getMythenClient().disableFlatFieldCorrection();
	}
	@Override
	public boolean isFlatFieldCorrected() throws IOException {
		return getMythenClient().isFlatFieldCorrected();
	}
	@Override
	public void enableCountRateCorrection() throws IOException {
		getMythenClient().enableCountRateCorrection();
	}
	@Override
	public void disableCountRateCorrection() throws IOException {
		getMythenClient().disableCountRateCorrection();
	}
	@Override
	public boolean isCountRateCorrected() throws IOException {
		return getMythenClient().isCountRateCorrected();
	}
	@Override
	public void enableBadChannelCorrection() throws IOException {
		getMythenClient().enableBadChannelCorrection();
	}
	@Override
	public void disableBadChannelCorrection() throws IOException {
		getMythenClient().disableBadChannelCorrection();
	}
	@Override
	public boolean isBadChannelCorrected() throws IOException {
		return getMythenClient().isBadChannelCorrected();
	}
	@Override
	public void enableAngularConversion() throws IOException {
		getMythenClient().enableAngularConversion();
	}
	@Override
	public void disableAngularConversion() throws IOException {
		getMythenClient().disableAngularConversion();
	}
	@Override
	public boolean isAngularConversionEnabled() throws IOException {
		return getMythenClient().isAngularConversionEnabled();
	}
	@Override
	public void setConfigFile(String value) throws IOException {
		getMythenClient().setConfigFile(value);
	}
	@Override
	public void loadConfigFile() throws IOException {
		getMythenClient().loadConfigFile();
	}
	@Override
	public void saveConfigFile() throws IOException {
		getMythenClient().saveConfigFile();
	}
	@Override
	public void setExposureTime(double exposureTime) throws Exception {
		getMythenClient().setExposure(exposureTime);
	}
	@Override
	public double getExposureTime() throws Exception {
		return getMythenClient().getExposure();
	}
	@Override
	public void setAcquirePeriod(double acquireperiod) throws Exception {
		getMythenClient().setAcquirePeriod(acquireperiod);
	}
	@Override
	public double getAcquirePeriod() throws Exception {
		return getMythenClient().getAcquirePeriod();
	}
	@Override
	public void startWait() throws DeviceException {
		getMythenClient().startWait();
	}
	private void resetArrayCounter() throws Exception {
		getMythenClient().resetArrayCounter();
	}

	@Override
	public ArrayList<File> getProcessedDataFilesForThisScan() {
		return processedDataFilesForScan;
	}

	@Override
	public int getNumberOfModules() {
		return numberOfModules;
	}

	@Override
	public List<DataProcessingTask> getProcessingTasks() {
		return processingTasks;
	}

	@Override
	public void setProcessingTasks(List<DataProcessingTask> processingTasks) {
		this.processingTasks = processingTasks;
	}
}
