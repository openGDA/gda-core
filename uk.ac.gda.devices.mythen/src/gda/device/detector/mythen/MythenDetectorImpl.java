/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.device.detector.mythen;

import static gda.device.detector.mythen.client.Trigger.NONE;
import static java.math.BigDecimal.ZERO;
import gda.configuration.properties.LocalProperties;
import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.data.fileregistrar.FileRegistrarHelper;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.DetectorBase;
import gda.device.detector.Mythen;
import gda.device.detector.mythen.client.AcquisitionParameters;
import gda.device.detector.mythen.client.MythenClient;
import gda.device.detector.mythen.client.Trigger;
import gda.device.detector.mythen.data.DataConverter;
import gda.device.detector.mythen.data.MythenDataFileUtils.FileType;
import gda.device.detector.mythen.data.MythenProcessedDataset;
import gda.device.detector.mythen.data.MythenRawDataset;
import gda.device.detector.mythen.tasks.AtPointEndTask;
import gda.device.detector.mythen.tasks.ScanTask;
import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Implementation of the GDA Mythen interface.
 */
public class MythenDetectorImpl extends DetectorBase implements Mythen, InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(MythenDetectorImpl.class);

	@Override
	public boolean isLocal() {
		return true;
	}

	/**
	 * Exposure time in seconds.
	 */
	protected BigDecimal exposureTime;
	
	protected volatile int status = IDLE;

	protected String detectorID = "unknown";

	protected NumTracker scanNumTracker;

	protected long collectionNumber;

	/**
	 * The Mythen client that actually interacts with the Mythen controller hardware.
	 */
	protected MythenClient mythenClient;

	protected Scannable deltaScannable;

	/**
	 * The converter that converts raw Mythen data (channel and count) to processed data (angle and count).
	 */
	protected DataConverter dataConverter;

	/**
	 * The directory below the main data collection directory in which Mythen data files are saved.
	 */
	protected String subDirectory;

	/**
	 * Creates a new Mythen detector with a default collection time of 1s.
	 */
	public MythenDetectorImpl() {
		try {
			setCollectionTime(1);
		} catch (DeviceException e) {
			logger.error("MythenDetectorImpl caught DeviceException during instantiation: ", e);
		}
	}

	@Override
	public void configure() throws FactoryException {
		super.configure();
		try {
			if (LocalProperties.isScanSetsScanNumber()) {
				this.scanNumTracker = new NumTracker("scanbase_numtracker");
			} else {
				this.scanNumTracker = new NumTracker("tmp");
			}
		} catch (IOException e) {
			throw new FactoryException("Couldn't create NumTracker for Mythen detector", e);
		}
	}

	
	/**
	 * Sets the detector ID.
	 * 
	 * @param detectorID
	 *            the detector ID
	 */
	public void setDetectorID(String detectorID) {
		this.detectorID = detectorID;
	}

	/**
	 * Sets the Mythen client used by this Mythen detector object.
	 * 
	 * @param mythenClient
	 *            the Mythen client
	 */
	public void setMythenClient(MythenClient mythenClient) {
		this.mythenClient = mythenClient;
	}

	/**
	 * Sets the {@link Scannable} representing the delta circle.
	 * 
	 * @param deltaScannable
	 *            the delta scannable
	 */
	public void setDeltaScannable(Scannable deltaScannable) {
		this.deltaScannable = deltaScannable;
	}

	/**
	 * Sets the Mythen data converter used by this Mythen detector object.
	 * 
	 * @param dataConverter
	 *            the data converter
	 */
	public void setDataConverter(DataConverter dataConverter) {
		this.dataConverter = dataConverter;
	}

	public DataConverter getDataConverter() {
		return dataConverter;
	}

	public void setSubDirectory(String subDirectory) {
		this.subDirectory = subDirectory;
	}

	public synchronized String getSubDirectory() {
		return this.subDirectory;
	}

	/**
	 * Returns the data directory into which Mythen data files will be written.
	 * 
	 * @return the data directory
	 */
	public synchronized File getDataDirectory() {
		return new File(PathConstructor.createFromDefaultProperty() + this.subDirectory);
	}

	protected int numberOfModules;

	/**
	 * Sets the number of modules in the detector.
	 */
	public void setNumberOfModules(int numberOfModules) {
		this.numberOfModules = numberOfModules;
	}

	protected List<ScanTask> atScanStartTasks = new Vector<ScanTask>();

	/**
	 * Sets the list of tasks that will be executed at the start of the scan.
	 */
	public void setAtScanStartTasks(List<ScanTask> tasks) {
		this.atScanStartTasks = tasks;
	}

	protected List<AtPointEndTask> atPointEndTasks = new Vector<AtPointEndTask>();

	/**
	 * Sets the list of tasks that will be executed after each point in the scan.
	 */
	public void setAtPointEndTasks(List<AtPointEndTask> tasks) {
		this.atPointEndTasks = tasks;
	}

	protected List<ScanTask> atScanEndTasks = new Vector<ScanTask>();

	/**
	 * Sets the list of tasks that will be executed at the end of the scan.
	 */
	public void setAtScanEndTasks(List<ScanTask> tasks) {
		this.atScanEndTasks = tasks;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (detectorID == null) {
			throw new IllegalStateException("You have not set the detector ID");
		}

		if (mythenClient == null) {
			throw new IllegalStateException("You have not set a Mythen client to be used when collecting data");
		}

		if (dataConverter == null) {
			throw new IllegalStateException(
					"You have not set a data converter; this is needed for converting raw data to processed data");
		}
	}

	/** The most recently collected raw dataset. */
	protected MythenRawDataset rawData;

	/** File containing the most recently collected raw dataset. */
	protected File rawFile;

	/** The most recently collected processed dataset. */
	protected MythenProcessedDataset processedData;

	/** File containing the most recently collected processed dataset. */
	protected File processedFile;

	private long scanNumber;

	@Override
	public int getStatus() throws DeviceException {
		return status;
	}

	@Override
	public void atScanStart() throws DeviceException {
		collectionNumber = 0;
		this.scanNumber = scanNumTracker.getCurrentFileNumber();

		for (ScanTask task : atScanStartTasks) {
			task.run();
		}
	}

	public String getCurrentFilename() {
		// long scanNumber = scanNumTracker.getCurrentFileNumber() + 1;
		return buildFilenameWithoutSuffix((int) collectionNumber);
	}

	protected String buildFilenameWithoutSuffix(int number) {
		return buildFilenameWithoutSuffix(String.format("%04d", number));
	}

	protected String buildFilenameWithoutSuffix(String s) {
		return String.format("%d-mythen-%s", this.scanNumber, s);
	}

	protected String buildFilename(int number, FileType type) {
		return buildFilename(String.format("%04d", number), type);
	}

	/**
	 * rebuild Raw data file name added for Jython use as Jython does not support enum Java type yet.
	 * @param number
	 * @return filename
	 */
	public String buildRawFilename(int number) {
		return buildFilename(String.format("%04d", number), FileType.RAW);
	}

	protected String buildFilename(String s, FileType type) {
		final String suffix = (type == FileType.PROCESSED) ? "dat" : "raw";
		return String.format("%d-mythen-%s.%s", this.scanNumber, s, suffix);
	}

	protected String collectionFilename;
	protected double delta;

	protected void beforeCollectData() throws DeviceException {
		beforeCollectData((int) collectionNumber++);
	}
	
	/**
	 * this method is developed for external scripting where the script control collection number.
	 * 
	 * @param collectionNumber
	 * @throws DeviceException
	 */
	protected void beforeCollectData(int collectionNumber) throws DeviceException {
		status = BUSY;

		if (!getDataDirectory().exists()) {
			if (!getDataDirectory().mkdirs()) {
				throw new DeviceException("Unable to create data directory: " + getDataDirectory());
			}
		}

		this.collectionNumber = collectionNumber;
		collectionFilename = getCurrentFilename();
		rawFile = new File(getDataDirectory(), collectionFilename + ".raw");
		logger.info("Mythen data will be written to " + rawFile);

		updateDeltaPosition();
	}


	protected void updateDeltaPosition() throws DeviceException {
		if (deltaScannable != null) {
			delta = (Double) deltaScannable.getPosition();
		}
	}

	@Override
	public void collectData() throws DeviceException {
		beforeCollectData();
		Thread mythenAcquire = new Thread(new Runnable() {

			@Override
			public void run() {
				// do data acquisition into the file
				AcquisitionParameters params = new AcquisitionParameters.Builder()
				.filename(rawFile.getAbsolutePath())
				.frames(1)
				.exposureTime(exposureTime)
				.trigger(NONE)
				.build();
				try {
					mythenClient.acquire(params);
					afterCollectData();
				} catch (DeviceException e) {
					logger.error("{}: error text client acquisition failed.", e);
					throw new RuntimeException("Unable to collect data", e);
				} finally {
					status = IDLE;
				}
			}
		});
		while (mythenAcquire.isAlive()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		mythenAcquire.start();
	}

	protected void afterCollectData() {
		// read data and process it
		rawData = new MythenRawDataset(rawFile);
		processedData = dataConverter.process(rawData, delta);
		processedFile = new File(getDataDirectory(), collectionFilename + ".dat");
		processedData.save(processedFile);
		if (InterfaceProvider.getTerminalPrinter() != null) {
			InterfaceProvider.getTerminalPrinter().print("Save to file " + processedFile.getAbsolutePath());
		}
		FileRegistrarHelper.registerFiles(new String[] { rawFile.getAbsolutePath(), processedFile.getAbsolutePath() });

		status = IDLE;
	}

	@Override
	public Object readout() throws DeviceException {
		String filename = processedFile.getName();
		return filename.substring(0, filename.lastIndexOf('.'));
	}

	@Override
	public MythenProcessedDataset readoutProcessedData() {
		return processedData;
	}

	@Override
	public void atPointEnd() throws DeviceException {
		for (AtPointEndTask task : atPointEndTasks) {
			task.run(getCurrentFilename(), processedData);
		}
	}

	@Override
	public void atScanEnd() throws DeviceException {
		for (ScanTask task : atScanEndTasks) {
			task.run();
		}
	}

	@Override
	public void stop() throws DeviceException {
		for (ScanTask task : atScanEndTasks) {
			task.run();
		}
		status = IDLE;
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return true;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "Mythen";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return detectorID;
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "Mythen";
	}

	@Override
	public void setCollectionTime(double collectionTime) throws DeviceException {
		super.setCollectionTime(collectionTime);
		exposureTime = BigDecimal.valueOf(collectionTime);
	}

	/**
	 * Captures multiple frames using a software trigger.
	 */
	public void multi(int numFrames, double delayBeforeFrames, double exposureTime, double delayAfterFrames)
			throws DeviceException {
		_multi(Trigger.NONE, numFrames, delayBeforeFrames, exposureTime, delayAfterFrames);
	}

	/**
	 * Captures multiple frames using a single trigger to start acquisition of all frames.
	 */
	public void smulti(int numFrames, double exposureTime, double delayAfterFrames) throws DeviceException {
		_multi(Trigger.SINGLE, numFrames, 0, exposureTime, delayAfterFrames);
	}

	/**
	 * Captures multiple frames using one trigger per frame.
	 */
	public void cmulti(int numFrames, double delayBeforeFrames, double exposureTime) throws DeviceException {
		_multi(Trigger.CONTINUOUS, numFrames, delayBeforeFrames, exposureTime, 0);
	}

	private void _multi(Trigger trigger, int numFrames, double delayBeforeFrames, double exposureTime,
			double delayAfterFrames) throws DeviceException {
		long scanNumber = scanNumTracker.incrementNumber();

		final File dataDirectory = getDataDirectory();

		// Client uses the template "%s_%d.raw" when writing raw data files; this template includes a
		// placeholder the "raw" or "dat" suffix
		final String filenameTemplate = "%s_%d.%s";

		// This is the prefix for all the raw/processed files
		final String prefix = String.format("%d_mythen", scanNumber);

		final File baseFilename = new File(dataDirectory, prefix);

		updateDeltaPosition();

		// do acquisition
		AcquisitionParameters params = new AcquisitionParameters.Builder().frames(numFrames)
				.delayBeforeFrames(new BigDecimal(delayBeforeFrames)).exposureTime(new BigDecimal(exposureTime))
				.delayAfterFrames(new BigDecimal(delayAfterFrames)).filename(baseFilename.getAbsolutePath())
				.startIndex(1).trigger(trigger).build();
		logger.info("Acquiring data");
		mythenClient.acquire(params);

		// process data
		logger.info("Processing data");
		for (int frame = 1; frame <= numFrames; frame++) {
			File rawFile = new File(dataDirectory, String.format(filenameTemplate, prefix, frame, "raw"));
			MythenRawDataset rawData = new MythenRawDataset(rawFile);
			MythenProcessedDataset processedData = dataConverter.process(rawData, delta);
			File processedFile = new File(dataDirectory, String.format(filenameTemplate, prefix, frame, "dat"));
			processedData.save(processedFile);
		}

		logger.info("Finished");
	}

	public void gated(int numFrames, int numGates) throws DeviceException {
		long scanNumber = scanNumTracker.incrementNumber();
		final File dataDirectory = getDataDirectory();
		final String rawFilename = String.format("%d_mythen.raw", scanNumber);
		final File rawFile = new File(dataDirectory, rawFilename);
		final String processedFilename = String.format("%d_mythen.dat", scanNumber);
		final File processedFile = new File(dataDirectory, processedFilename);

		updateDeltaPosition();

		AcquisitionParameters params = new AcquisitionParameters.Builder().delayBeforeFrames(ZERO)
				.delayAfterFrames(ZERO).exposureTime(ZERO).frames(numFrames).gating(true).gates(numGates)
				.filename(rawFile.getAbsolutePath()).trigger(Trigger.NONE).build();

		logger.info("Acquiring data");
		mythenClient.acquire(params);

		logger.info("Processing data");
		MythenRawDataset rawData = new MythenRawDataset(rawFile);
		MythenProcessedDataset processedData = dataConverter.process(rawData, delta);
		processedData.save(processedFile);
	}

	public void gated(int numGates) throws DeviceException {
		gated(1, numGates);
	}

	public void gated(int numFrames, int numGates, long scanNumber, File dataDirectory, int collectionNumber)
			throws DeviceException {
		final String rawFilename = String.format("%d_mythen_%d.raw", scanNumber, collectionNumber);
		final File rawFile = new File(dataDirectory, rawFilename);
		final String processedFilename = String.format("%d_mythen_%d.dat", scanNumber, collectionNumber);
		final File processedFile = new File(dataDirectory, processedFilename);

		updateDeltaPosition();

		AcquisitionParameters params = new AcquisitionParameters.Builder().delayBeforeFrames(ZERO)
				.delayAfterFrames(ZERO).exposureTime(ZERO).frames(numFrames).gating(true).gates(numGates)
				.filename(rawFile.getAbsolutePath()).trigger(Trigger.NONE).build();

		logger.info("Acquiring data");
		mythenClient.acquire(params);

		logger.info("Processing data");
		MythenRawDataset rawData = new MythenRawDataset(rawFile);
		MythenProcessedDataset processedData = dataConverter.process(rawData, delta);
		processedData.save(processedFile);
	}

	/**
	 * gated multiple frames collection - one frame per file - where Mythen detector controls the frame number increment
	 * starting from 0. It launches only single textclient to collect multiple frames
	 * 
	 * @param numFrames
	 * @param numGates
	 * @param scanNumber
	 * @throws DeviceException
	 */
	public void gated(final int numFrames, int numGates, long scanNumber) throws DeviceException {
		final String collectionFilenameRoot = String.format("%d_mythen", scanNumber);
		File rawFile = new File(getDataDirectory(), collectionFilenameRoot);
		logger.info("Mythen data will be written to files with root name " + rawFile);
		updateDeltaPosition();

		AcquisitionParameters params = new AcquisitionParameters.Builder().delayBeforeFrames(ZERO)
				.delayAfterFrames(ZERO).exposureTime(ZERO).frames(numFrames).gating(true).gates(numGates)
				.filename(rawFile.getAbsolutePath()).trigger(Trigger.NONE).startIndex(0) // comment out to save to
																							// single file, remove
																							// comment to save to
																							// multiple files
				.build();

		logger.info("Acquiring data");
		mythenClient.acquire(params);

		Thread processing = new Thread(new Runnable() { // post process raw data so it does not block acquisition

					@Override
					public void run() {
						logger.info("Processing all frames");
						afterCollectData(collectionFilenameRoot, numFrames);
					}
				}, "DataProcessing");
		processing.start();
	}

	/**
	 * post processes of multiple frames collection and plotting them. this method is developed for external scripting
	 * use in which the collection file name is settable
	 * 
	 * @param collectionFilenameRoot
	 */
	protected void afterCollectData(String collectionFilenameRoot, int numFiles) {
		boolean clearFirst;
		for (int i = 0; i < numFiles; i++) {
			collectionFilename = collectionFilenameRoot + "_" + i;
			rawFile = new File(getDataDirectory(), collectionFilename + ".raw");
			// read data and process it
			rawData = new MythenRawDataset(rawFile);
			processedData = dataConverter.process(rawData, delta);
			processedFile = new File(getDataDirectory(), collectionFilename + ".dat");
			processedData.save(processedFile);
			if (InterfaceProvider.getTerminalPrinter() != null) {
				InterfaceProvider.getTerminalPrinter().print("Save to file " + processedFile.getAbsolutePath());
			}
			FileRegistrarHelper
					.registerFiles(new String[] { rawFile.getAbsolutePath(), processedFile.getAbsolutePath() });

			status = IDLE;
			if (i == 0) {
				clearFirst = true;
			} else {
				clearFirst = false;
			}
			atPointEnd(collectionFilename, processedData, clearFirst);
		}
	}

	/**
	 * plotting processed data.
	 * 
	 * @param filename
	 * @param processedData
	 * @param clearFirst
	 */
	public void atPointEnd(String filename, MythenProcessedDataset processedData, boolean clearFirst) {
		for (AtPointEndTask task : atPointEndTasks) {
			task.run(filename, processedData, clearFirst);
		}
	}

	/**
	 * gated multiple or single frame collection - one or more frames per file - where GDA controls the collection
	 * number increment.
	 * 
	 * @param numFrames
	 * @param numGates
	 * @param scanNumber
	 * @param collectionNumber
	 * @throws DeviceException
	 */
	public void gated(final int numFrames, int numGates, long scanNumber, int collectionNumber) throws DeviceException {
		// must set scanNumber when operate outside GDA Scan command, e.g. in a for loop. This is normally set
		// atScanStart() in a scan
		this.scanNumber = scanNumber;
		// must set collection number when operate outside GDA Scan command, e.g. in a for loop. This is normally
		// initialised atScanStart() in a scan
		this.collectionNumber = collectionNumber;
		beforeCollectData(collectionNumber);
		final String rawfilenameroot = rawFile.getAbsolutePath().replace(".raw", "");
		AcquisitionParameters params = new AcquisitionParameters.Builder().delayBeforeFrames(ZERO)
				.delayAfterFrames(ZERO).exposureTime(ZERO).frames(numFrames).gating(true).gates(numGates)
				.filename(rawfilenameroot).trigger(Trigger.NONE).startIndex(0).build();

		logger.info("Acquiring data");
		mythenClient.acquire(params);

		Thread processing = new Thread(new Runnable() { // post process raw data so it does not block acquisition

					@Override
					public void run() {
						logger.info("Processing data");
						for (int i = 1; i < numFrames + 1; i++) {
							File newrawFile = new File(rawfilenameroot + "_" + i + ".raw");
							afterCollectData(newrawFile, i);
						}
					}
				}, "DataProcessing");
		processing.start();
	}

	protected void afterCollectData(File rawFile, int num) {
		// read data and process it
		rawData = new MythenRawDataset(rawFile);
		processedData = dataConverter.process(rawData, delta);
		processedFile = new File(rawFile.getAbsolutePath().replace(".raw", ".dat"));
		processedData.save(processedFile);
		if (InterfaceProvider.getTerminalPrinter() != null) {
			InterfaceProvider.getTerminalPrinter().print("Save to file " + processedFile.getAbsolutePath());
		}
		FileRegistrarHelper.registerFiles(new String[] { rawFile.getAbsolutePath(), processedFile.getAbsolutePath() });

		status = IDLE;
		plot(processedFile.getName(), processedData, num);
	}

	public void plot(String filename, MythenProcessedDataset processedData, int num) {

		try {
			if (num == 1) {
				atPointEnd(filename, processedData, true);
			} else {
				atPointEnd(filename, processedData);
			}
		} catch (DeviceException e) {
			logger.error("Plot collected data failed at point " + num, e);
		}
	}

	/**
	 * this method is developed for external scripting use in which the collection file name is settable
	 * 
	 * @param collectionFilename
	 */
	protected void afterCollectData(File rawFile, String collectionFilename) {
		// read data and process it
		rawData = new MythenRawDataset(rawFile);
		processedData = dataConverter.process(rawData, delta);
		processedFile = new File(getDataDirectory(), collectionFilename + ".dat");
		processedData.save(processedFile);
		if (InterfaceProvider.getTerminalPrinter() != null) {
			InterfaceProvider.getTerminalPrinter().print("Save to file " + processedFile.getAbsolutePath());
		}
		FileRegistrarHelper.registerFiles(new String[] { rawFile.getAbsolutePath(), processedFile.getAbsolutePath() });

		status = IDLE;
		try {
			atPointEnd(collectionFilename, processedData);
		} catch (DeviceException e) {
			logger.error("failed to perform atPointEnd tasks", e);
		}
	}

	public void atPointEnd(String filename, MythenProcessedDataset processedData) throws DeviceException {
		for (AtPointEndTask task : atPointEndTasks) {
			task.run(filename, processedData);
		}
	}

}
