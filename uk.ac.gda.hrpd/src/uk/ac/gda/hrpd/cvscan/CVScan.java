/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.hrpd.cvscan;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.multichannelscaler.EpicsMcsSis3820;
import gda.device.detector.multichannelscaler.EpicsMultiChannelScaler;
import gda.device.detector.multichannelscaler.Mca;
import gda.device.monitor.IonChamberBeamMonitor;
import gda.device.scannable.ScannableMotionBase;
import gda.factory.FactoryException;
import gda.hrpd.data.EpicsCVscanDataWriter;
import gda.hrpd.data.ProcessedMacData;
import gda.hrpd.data.RawMacData;
import gda.hrpd.pmac.SafePosition;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServerFacade;
import gda.jython.JythonStatus;
import gda.jython.scriptcontroller.ScriptControllerBase;
import gda.jython.scriptcontroller.Scriptcontroller;
import gda.observable.IObserver;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.gda.api.remoting.ServiceInterface;
import uk.ac.gda.hrpd.cvscan.event.FileNumberEvent;

/**
 * * <li>Specify {@link Scriptcontroller} instance to handle data file name changed event {@link FileNumberEvent} which
 * facilitate server to client communication</li>
 */
@ServiceInterface(Scannable.class)
public class CVScan extends ScannableMotionBase implements IObserver {

	private static final String PROCESSED_EXTENSION_PROPERTY = "gda.data.file.extension.rebinned";

	private static final String RAW_EXTENSION_PROPERTY = "gda.data.file.extension.raw";

	private static final Logger logger = LoggerFactory.getLogger(CVScan.class);

	// store for additional scannable to be added dynamically which produce metadata in data file header
	private ArrayList<Scannable> scannables = new ArrayList<Scannable>();

	// objects this class object depends on
	private ArrayList<Detector> mcsDetectors = new ArrayList<Detector>();
	private EpicsCVScan controller;
	private IonChamberBeamMonitor beamMonitor;
	private EpicsCVscanDataWriter dataWriter;

	// variables to cache the various states of objects
	private volatile boolean isBeamMonitorRunning = false;
	public volatile boolean paused;
	private volatile int pausedCounter = 0;
	private volatile boolean isGDAScanning = false;
	private volatile long collectionNumber = 1;
	private double totaltime;
	private int actualpulses;
	private String rawfilename;
	private String rebinfilename;
	private ArrayList<String> profiles;
	private File rawfile;
	private File rebinnedfile;
	private int retrycount = 0;

	private AtomicBoolean rawDataSaved = new AtomicBoolean(false);

	private static final int NTHREDS = 2;
	private ExecutorService executor;
	// collision prevention objects
	private Scannable psdScannableMotor;
	private SafePosition psdSafePosition;
	// server-to-client event passing object
	private Scriptcontroller scriptController;

	private ArrayList<Future<String>> list = new ArrayList<Future<String>>();

	private Callable<?> safePositionSetup;

	/** Whether raw data should be summed at the end of scans - only has effect when multiple files are collected */
	private boolean summing = true;

	@Override
	public void configure() throws FactoryException {
		if (!isConfigured()) {
			this.setInputNames(new String[] { "tth" });
			this.setOutputFormat(new String[] { "%s" });
			try {
				getAvailableCVScanProfiles();
			} catch (Exception e) {
				throw new FactoryException("getAvailableCVScanProfiles failed " + getName());
			} // initialise profiles variable.
			this.level = 9;
			setConfigured(true);
		}
	}

	/*
	 * send data filename to observers in client from the server.
	 */
	private void fireNewDataFile() {
		if (getScriptController() instanceof ScriptControllerBase) {
			getScriptController().update(this, new FileNumberEvent(getDataWriter().getCurrentFileName(), collectionNumber));
		}
	}

	@Override
	public void rawAsynchronousMoveTo(Object time) throws DeviceException {
		checkForCollision();
		if (!isGDAScanning) {
			collectionNumber = 1;
			controller.setGDAScanning(false);
			controller.setCollectionNumber(collectionNumber);
			// must increment file number for manual collection, i.e. not in a scan, but in a pos
			controller.setFileNumber(getDataWriter().incrementFileNumber());
			createFilesToWriteTo();
			fireNewDataFile();
			executor = Executors.newFixedThreadPool(NTHREDS);
			if (controller != null) {
				controller.addIObserver(this);
			} else {
				throw new DeviceException("EpicsCVScan object is not defined.");
			}
		}
		pausedCounter = 0; // initialise counter for paused flag for this cvscan
		rawDataSaved.set(false);
		this.totaltime = Double.valueOf(time.toString()).doubleValue();
		try {
			controller.setTime(totaltime);
		} catch (Exception e1) {
			logger.error(getName() + ": Set total scan time failed.", e1);
			throw new DeviceException(getName() + ":Set total scan time failed. ", e1);
		}
		// ensure beam monitor start
		if (!isBeamMonitorRunning) {
			startBeamMonitor();
		}
		try {
			Thread.sleep(100); // delay to ensure monitor thread starts well before EPICS cvscan starts
		} catch (InterruptedException e1) {
			// No op
		}
		// ensure fast shutter open if not
		if (!((EpicsMultiChannelScaler) mcsDetectors.get(0)).isShutterOpened()) {
			((EpicsMultiChannelScaler) mcsDetectors.get(0)).openShutter();
		}
		if (!paused) {
			try {
				controller.start();
				logger.info("{}: start cvscan", getName());
			} catch (CAException e) {
				logger.error("{}: start cvscan failed.", getName(), e);
			} catch (InterruptedException e) {
				logger.error("cvscan start being interrupted.", e);
			}
		} else {
			// Ensure that controller is busy so that waitWhileBusy doesn't immediately return
			controller.setBusy(true);
			logger.info("{}  is in a PAUSED state", getName());
		}
	}

	private void checkForCollision() throws DeviceException {
		if (safePositionSetup != null) {
			try {
				safePositionSetup.call();
			} catch (Exception e) {
				logger.warn("Failed to run safe position setup - this scan may fail", e);
			}
		}
		// collision avoidance check delta motor position only proceed if delta motor is at PSD Safe Position defined in
		// Spring configuration
		if (psdScannableMotor != null) {
			double position = (double)psdScannableMotor.getPosition();
			psdSafePosition.checkPosition("PSD detector", position);
		}
	}

	public void restart() throws DeviceException {
		// ensure beam monitor start
		if (!isBeamMonitorRunning) {
			startBeamMonitor();
		}
		try {
			Thread.sleep(100); // delay to ensure monitor thread starts well before EPICS cvscan starts
		} catch (InterruptedException e1) {
			// No op
		}
		// ensure fast shutter open if not
		if (!((EpicsMultiChannelScaler) mcsDetectors.get(0)).isShutterOpened()) {
			((EpicsMultiChannelScaler) mcsDetectors.get(0)).openShutter();
		}
		if (!paused) {
			try {
				controller.start();
				logger.info("{}: restart cvscan", getName());
			} catch (CAException e) {
				logger.error("{}: restart scan failed.", getName(), e);
				throw new DeviceException(getName() + ":restart scan failed. ", e);
			} catch (InterruptedException e) {
				logger.error("cvscan restart being interrupted", e);
			}
		} else {
			logger.info("{}  is in a PAUSED state", getName());
		}
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		// TODO define inputName[getName()], extraName[getFilename()] to support scansReturnToOriginalPositions=1
		// so that at scan end an extra CVScan will be called.
		if(!isGDAScanning) {
			String datafile = null;
			for (Future<String> future : list) {
				try {
					datafile = future.get();
				} catch (InterruptedException e) {
					logger.error("Data saving to "+datafile+" is interrupted.", e);
					throw new DeviceException("Data saving to " +datafile+" is interrupted.", e);
				} catch (ExecutionException e) {
					logger.error("Data saving to "+ datafile +" throws ExecutionException.", e);
					throw new DeviceException("Data saving to "+datafile+ " throws ExecutionException.", e);
				}
				logger.info("Data {} saving completed.", datafile);
			}
			executor.shutdown();
			boolean terminated;
			try {
				terminated = executor.awaitTermination(1, TimeUnit.MINUTES);
				if (!terminated) {
					throw new java.util.concurrent.TimeoutException("data saving shutdown timeout.");
				}
			} catch (InterruptedException |java.util.concurrent.TimeoutException e) {
				logger.error("Data saving executor failed in shutdown.", e);
				throw new DeviceException("Data saving executor failed in shutdown.", e);
			}
			if (controller != null) {
				controller.deleteIObserver(this);
			} else {
				throw new DeviceException("EpicsCVScan object is not defined.");
			}
			// need to clear the list of future when finished
			if (!list.isEmpty()) {
				list.clear();
			}
			return new File(datafile).getName();
		}
		return getFilename();
	}

	private String getFilename() {
		return rebinnedfile.getName();
	}

	public String getRebinnedFilePath() {
		return Optional.ofNullable(rebinnedfile)
				.map(File::getAbsolutePath)
				.orElse(null);
	}

	public String getRawFilePath() {
		return Optional.ofNullable(rawfile)
				.map(File::getAbsolutePath)
				.orElse(null);
	}

	@Override
	public boolean isBusy() {
		return controller.isBusy();
	}

	@Override
	public void atScanStart() throws DeviceException {
		checkForCollision();
		// any preparation works here.
		paused = false;
		pausedCounter = 0;
		isGDAScanning = true;
		executor=Executors.newFixedThreadPool(NTHREDS);
		if (controller != null) {
			controller.addIObserver(this);
		} else {
			throw new DeviceException("EpicsCVScan object is not defined.");
		}
		collectionNumber = 1;
		controller.setCollectionNumber(collectionNumber);
		controller.setGDAScanning(true);
		controller.setFileNumber(getDataWriter().getFileNumber());
		// must create filename here as getPosition() is always called first before scan start
		// when scansReturnToOriginalPositions = 1 in localStation.py
		// It will be over-written by same call in atPointStart()
		filesToWriteTo();
	}

	@Override
	public void atScanEnd() throws DeviceException {
		paused = false;
		isGDAScanning = false;
		int finalCollectionNumber = (int)collectionNumber;
		collectionNumber = 1;
		controller.setCollectionNumber(collectionNumber);
		controller.setGDAScanning(false);

		executor.shutdown();
		boolean terminated;
		try {
			terminated = executor.awaitTermination(1, TimeUnit.MINUTES);
			if (!terminated) {
				throw new java.util.concurrent.TimeoutException("data saving shutdown timeout.");
			}
		} catch (InterruptedException |java.util.concurrent.TimeoutException e) {
			logger.error("Data saving executor failed in shutdown.", e);
			throw new DeviceException("Data saving executor failed in shutdown.", e);
		}
		if (!list.isEmpty()) {
			list.clear();
		}
		// Check is for > 2 as the collection number is the next file that would be collected
		if (finalCollectionNumber > 2 && summing) {
			var dir = getDataWriter().getDataDir();
			var name = getDataWriter().getCurrentFileName();
			var rawExt = LocalProperties.get(RAW_EXTENSION_PROPERTY, "raw");
			var procExt = LocalProperties.get(PROCESSED_EXTENSION_PROPERTY, "dat");
			var summedRawFilename = String.format("%s-summed.%s", name, rawExt);
			var summedProcessedFilename = String.format("%s-summed.%s", name, procExt);
			Async.submit(() -> {
				logger.debug("Writing summed raw data to {}", summedRawFilename);
				// collection number is incremented at the end of each point so is
				// exclusive range includes all files
				IntStream.range(1, finalCollectionNumber)
						.mapToObj(i -> buildFilename(dir, name, i, rawExt))
						.map(RawMacData::readFile)
						// If both are present, add them, else empty
						.reduce((l, r) -> l.flatMap(lv -> r.map(lv::add)))
						.ifPresent(d -> d.ifPresentOrElse(
								f -> f.tryWrite(Paths.get(dir, summedRawFilename).toString())
										.ifPresent(e -> logger.error("Error writing summed raw MAC data", e)),
								() -> logger.error("Failed to read all raw MAC data")
						));
			}).onFailure(t -> logger.error("Error in background raw data summing task", t));

			Async.submit(() -> {
				logger.debug("Writing summed processed data to {}", summedProcessedFilename);
				// collection number is incremented at the end of each point so is
				// exclusive range includes all files
				IntStream.range(1, finalCollectionNumber)
						.mapToObj(i -> buildFilename(dir, name, i, procExt))
						.map(ProcessedMacData::readFile)
						// If both are present, add them, else empty
						.reduce((l, r) -> l.flatMap(lv -> r.map(lv::add)))
						.ifPresent(d -> d.ifPresentOrElse(
								f -> f.tryWrite(Paths.get(dir, summedProcessedFilename).toString())
										.ifPresent(e -> logger.error("Error writing summed processed MAC data", e)),
								() -> logger.error("Failed to read all processed MAC data")
						));
			}).onFailure(t -> logger.error("Error in background processed data summing task", t));

		}
		if (controller != null) {
			controller.deleteIObserver(this);
		} else {
			throw new DeviceException("EpicsCVScan object is not defined.");
		}
	}

	@Override
	public void atPointStart() throws DeviceException {
		// display the filename or file number that data will be saved to when completed
		createFilesToWriteTo();
		fireNewDataFile();

		paused = false;

		// start beam monitor thread
		if (!isBeamMonitorRunning) {
			startBeamMonitor();
		}
		if (!((EpicsMultiChannelScaler) mcsDetectors.get(0)).isShutterOpened()) {
			((EpicsMultiChannelScaler) mcsDetectors.get(0)).openShutter();
		}
		controller.setCollectionNumber(collectionNumber);
		retrycount = 0;
		fireNewDataFile();
	}

	// hack to satisfy scan framework requirement
	private void filesToWriteTo() {
		rebinnedfile = buildFile(getDataWriter().getDataDir(), getDataWriter().getCurrentFileName(),
				collectionNumber, LocalProperties.get(PROCESSED_EXTENSION_PROPERTY, "dat"));
	}

	/*
	 * create file handle for both RAW data and rebinned data.
	 */
	private void createFilesToWriteTo() {
		rawfile = buildFile(getDataWriter().getDataDir(), getDataWriter().getCurrentFileName(),
				collectionNumber, LocalProperties.get(RAW_EXTENSION_PROPERTY, "raw"));
		InterfaceProvider.getTerminalPrinter().print("Raw data will be saved to file: " + rawfile.getAbsolutePath());
		rebinnedfile = buildFile(getDataWriter().getDataDir(), getDataWriter().getCurrentFileName(),
				collectionNumber, LocalProperties.get(PROCESSED_EXTENSION_PROPERTY, "dat"));
		InterfaceProvider.getTerminalPrinter().print(
				"Rebinned data will be saved to file: " + rebinnedfile.getAbsolutePath());
		InterfaceProvider.getTerminalPrinter().print("When constant velocity scan completed successfully.");
	}

	@Override
	public void atPointEnd() throws DeviceException {
		// stop beam monitor thread
		if (isBeamMonitorRunning) {
			stopBeamMonitor();
		}
		// close fast shutter
		if (((EpicsMultiChannelScaler) mcsDetectors.get(0)).isShutterOpened()) {
			((EpicsMultiChannelScaler) mcsDetectors.get(0)).closeShutter();
		}
		// need to ensure paused flag cleared on emergency stop.
		paused = false;
		String datafile = null;
		for (Future<String> future : list) {
			try {
				datafile = future.get();
			} catch (InterruptedException e) {
				logger.error("Data saving to "+datafile+" is interrupted.", e);
				throw new DeviceException("Data saving to " +datafile+" is interrupted.", e);
			} catch (ExecutionException e) {
				logger.error("Data saving to "+ datafile +" throws ExecutionException.", e);
				throw new DeviceException("Data saving to "+datafile+ " throws ExecutionException.", e);
			}
			logger.info("Data {} saving completed.", datafile);
		}

		retrycount = 0;
		collectionNumber++;
		getDataWriter().completeCollection();
	}

	@Override
	public void stop() throws DeviceException {
		// need to ensure paused flag cleared on emergency stop.
		paused = false;
		isGDAScanning = false;
		collectionNumber = 1;
		controller.setCollectionNumber(collectionNumber);
		controller.setGDAScanning(false);
		try {
			controller.abort();
			if (isBeamMonitorRunning) {
				stopBeamMonitor();
			}
			if (((EpicsMultiChannelScaler) mcsDetectors.get(0)).isShutterOpened()) {
				((EpicsMultiChannelScaler) mcsDetectors.get(0)).closeShutter();
			}
		} catch (Exception e) {
			logger.error(getName() + " failed to stop CVScan", e);
			throw new DeviceException(getName() + ": failed to stop CVScan", e);
		}
	}

	/**
	 * set or change CVScan profile
	 */
	public void setCVScanProfile(String profile) throws DeviceException {
		try {
			controller.setProfile(profile);
		} catch (InterruptedException e) {
			logger.error("InterruptedException ", e);
			throw new DeviceException(e);
		}
	}

	/**
	 * gets current CVScan profile
	 *
	 * @return CVScan profile name
	 * @throws DeviceException
	 */
	public String getCVScanProfile() throws DeviceException {
		return controller.getProfile();
	}

	/**
	 * list CVScan profile available
	 *
	 * @return list of Profiles
	 * @throws InterruptedException
	 */
	public ArrayList<String> getAvailableCVScanProfiles() throws InterruptedException {
		profiles = new ArrayList<String>();
		String[] profileNames = controller.getProfiles();
		for (String each : profileNames) {
			profiles.add(each);
		}
		return profiles;
	}

	/**
	 * reset the controller busy status to false if and only if locked to true.
	 *
	 * @throws CAException
	 * @throws InterruptedException
	 */
	public void reset() throws CAException, InterruptedException {
		controller.setBusy(false);
		paused = false;
		collectionNumber = 1;
		isGDAScanning = false;
		controller.setCollectionNumber(collectionNumber);
		controller.setGDAScanning(false);
		controller.abort();
	}

	/**
	 * set the start position of the 2nd motor in current CVScan Profile.
	 *
	 * @param position
	 * @throws DeviceException
	 */
	public void set2ndMotorStartPosition(double position) throws DeviceException {
		if (!getCVScanProfile().equalsIgnoreCase(profiles.get(0))) {
			try {
				controller.set2ndMotorStartPosition(position);
			} catch (Exception e) {
				logger.error(getName() + ": set 2nd motor start position failed.", e);
				if (e instanceof RuntimeException)
					throw (RuntimeException) e;
				if (e instanceof DeviceException)
					throw (DeviceException) e;
				throw new DeviceException(getName() + ": set 2nd motor start position failed.", e);
			}
		} else {
			InterfaceProvider.getTerminalPrinter().print(
					getName() + ": 2nd motor is not available in the Current CVScan profile.");
		}
	}

	/**
	 * get the start position of the 2nd motor in the current CVScan Profile
	 *
	 * @return start position of 2nd motor
	 * @throws DeviceException
	 */
	public double get2ndMotorStartPosition() throws DeviceException {
		if (!getCVScanProfile().equalsIgnoreCase(profiles.get(0))) {
			try {
				return controller.get2ndMotorStartPosition();
			} catch (Throwable e) {
				throw new DeviceException(getName() + ": get 2nd motor start position failed.", e);
			}
		}
		InterfaceProvider.getTerminalPrinter().print(
				getName() + ": 2nd motor is not available in the Current CVScan profile.");
		return 0.0;
	}

	/**
	 * get the scan range of the 2nd motor in the current CVScan Profile.
	 *
	 * @return scan range of 2nf motor
	 * @throws DeviceException
	 */
	public double get2ndMotorScanRange() throws DeviceException {
		if (!getCVScanProfile().equalsIgnoreCase(profiles.get(0))) {
			try {
				return controller.get2ndMotorScanRange();
			} catch (Throwable e) {
				throw new DeviceException(getName() + ": get 2nd motor scan range failed.", e);
			}
		}
		InterfaceProvider.getTerminalPrinter().print(
				getName() + ": 2nd motor is not available in the Current CVScan profile.");
		return 0.0;
	}

	/**
	 * set the scan range of 2nd motor in the current CVScan Profile.
	 *
	 * @param position
	 * @throws DeviceException
	 */
	public void set2ndMotorScanRange(double position) throws DeviceException {
		if (!getCVScanProfile().equalsIgnoreCase(profiles.get(0))) {
			try {
				controller.set2ndMotorScanRange(position);
			} catch (Exception e) {
				logger.error(getName() + ": set 2nd motor scan range failed.", e);
				if (e instanceof RuntimeException)
					throw (RuntimeException) e;
				if (e instanceof DeviceException)
					throw (DeviceException) e;
				throw new DeviceException(getName() + ": set 2nd motor scan range failed.", e);
			}
		} else {
			InterfaceProvider.getTerminalPrinter().print(
					getName() + ": 2nd motor is not available in the Current CVScan profile.");
		}
	}

	private String buildFilename(String dir, String filename, long collectionNumber, String ext) {
		return buildFile(dir, filename, collectionNumber, ext).getPath();
	}

	private File buildFile(String dir, String filename, long collectionNumber, String ext) {
		String fullFileName = String.format("%s-%03d.%s", filename, collectionNumber, ext);
		return new File(dir, fullFileName);
	}

	private void startBeamMonitor() {
		if (beamMonitor.isMonitorOn()) {
			// if beam monitor is not on, cvscan control thread will not be started.
			isBeamMonitorRunning = true;
			Thread bmt = new Thread(new ScanControl(), "cvscanBeamMonitor");
			bmt.setDaemon(true);
			bmt.start();
		} else {
			isBeamMonitorRunning = false;
			paused = false;
		}
	}

	private void stopBeamMonitor() {
		isBeamMonitorRunning = false;
	}

	private class ScanControl implements Runnable {

		@Override
		public void run() {
			while (beamMonitor.isMonitorOn() && isBeamMonitorRunning) {
				if (!beamMonitor.isBeamOn() && !paused) {
					try {
						InterfaceProvider.getTerminalPrinter().print("BEAM OFF - Pause current CVScan.");
						controller.pause();
						paused = true;
						// remember paused action as raw data will be invalid.
						pausedCounter++;
						// ScanBase.paused = true;
						logger.warn("BEAM OFF - Current CVScan is paused");
					} catch (Exception e) {
						logger.error("Failed to pause current constant velocity scan", e);
					}
				} else if (beamMonitor.isBeamOn() && paused) {
					try {
						InterfaceProvider.getTerminalPrinter().print("BEAM ON - Resume current CVScan.");
						// controller.resume();
						controller.start();
						paused = false;
						logger.info("BEAN ON - Resume current CVScan.");
					} catch (Exception e) {
						logger.error("Failed to resume current constant velocity scan", e);
					}
				}
			}
		}
	}

	private class SaveRawData implements Callable<String> {
		@Override
		public String call() throws Exception {
			if (!rawDataSaved.compareAndSet(false, true)) {
				logger.info("Raw data already saved");
				return null;
			}
			if (pausedCounter == 0) {
				return saveRawData();
			}
			InterfaceProvider.getTerminalPrinter().print(
					"CVScan had been paused " + pausedCounter + " times. No raw data file created");
			logger.info("CVScan had been paused {} times. No raw data file created.", pausedCounter);

			return null;
		}
	}

	private String saveRawData() throws Exception {
		double[] raw2theta = null;
		double monitorAverage = 0;
		try {
			monitorAverage = controller.getMonitorAverage();
		} catch (TimeoutException | CAException | InterruptedException e1) {
			logger.error(getName() + " cannot get monitor avarge data from " + controller.getName(), e1);
			throw new Exception(getName() + " cannot get monitor avarge data from " + controller.getName(), e1);
		}
		actualpulses = controller.getRaw2ThetaSize();
		try {
			raw2theta = controller.getRaw2ThetaPositions();
		} catch (TimeoutException | CAException | InterruptedException e1) {
			logger.error(getName() + " cannot get raw two-theta positions data from " + controller.getName(), e1);
			throw new Exception(getName() + " cannot get raw two-theta positions data from " + controller.getName(), e1);
		}
		//TODO where the 2nd parameter set???
		int[][] data = new int[2 * EpicsMcsSis3820.MAX_NUMBER_MCA][actualpulses];
		// retrieve the count data from detectors
		InterfaceProvider.getTerminalPrinter().print("reading raw data from detectors");
		for (int i = 0; i < mcsDetectors.size(); i++) {
			for (Map.Entry<Integer, Mca> e : ((EpicsMultiChannelScaler) mcsDetectors.get(i)).getMcaList().entrySet()) {
				try {
					data[e.getKey().intValue() - 1] = ((EpicsMultiChannelScaler) mcsDetectors.get(i)).getData(e
							.getValue().getScalerChannel());
				} catch (DeviceException e1) {
					logger.error(getName() + " cannot get detector data from " + mcsDetectors.get(i).getName() + " "
							+ e.getValue().getName(), e1);
					throw new Exception(getName() + " cannot get detector data from " + mcsDetectors.get(i).getName()
							+ " " + e.getValue().getName(), e1);
				}
			}
		}
		rawfilename = getDataWriter().addRawData(rawfile, actualpulses, scannables, inputNames[0].trim(), mcsDetectors,
				raw2theta, data, totaltime, monitorAverage);

		return rawfilename;
	}

	private class SaveRebinnedData implements Callable<String> {
		@Override
		public String call() throws Exception {
			return saveRebinnedData();
		}
	}

	private String saveRebinnedData() throws Exception {
		double[] rebinned2theta = null;
		double[] rebinnedCounts = null;
		double[] rebinnedCountErrors = null;
		double monitorAverage = 0;
		int numberOfElements = 0;
		try {
			monitorAverage = controller.getMonitorAverage();
		} catch (TimeoutException | CAException | InterruptedException e) {
			logger.error(getName() + " cannot get monitor average data from " + controller.getName(), e);
			throw new Exception(getName() + " cannot get monitor average data from " + controller.getName(), e);
		}
		try {
			rebinned2theta = controller.getRebinned2ThetaPositions();
		} catch (TimeoutException | CAException | InterruptedException e) {
			logger.error(getName() + " cannot get rebinned two-theta positions data from " + controller.getName(), e);
			throw new Exception(getName() + " cannot get rebinned two-theta positions data from "
					+ controller.getName(), e);
		}
		try {
			rebinnedCounts = controller.getRebinnedCounts();
		} catch (TimeoutException | CAException | InterruptedException e) {
			logger.error(getName() + " cannot get rebinned counts data from " + controller.getName(), e);
			throw new Exception(getName() + " cannot get rebinned counts data from " + controller.getName(), e);
		}
		try {
			rebinnedCountErrors = controller.getRebinnedCountErrors();
		} catch (TimeoutException | CAException | InterruptedException e) {
			logger.error(getName() + " cannot get rebinned count errors data from " + controller.getName(), e);
			throw new Exception(getName() + " cannot get rebinned count errors data from " + controller.getName(), e);
		}
		numberOfElements = controller.getRebinned2ThetaSize();
		rebinfilename = getDataWriter().addRebinnedData(rebinnedfile, numberOfElements, scannables, rebinned2theta,
				rebinnedCounts, rebinnedCountErrors, totaltime, monitorAverage);

		return rebinfilename;
	}

	/**
	 * Observer of Current State of {@link EpicsCVScan} object. On state updates to {@code Reduction}, kicks off a new
	 * threads to save raw data; on state updates to @{code Flyback}, kicks off a new thread to save rebinned data. Any
	 * other states just print message to the Jython terminal.
	 */
	@Override
	public void update(Object source, Object arg) {
		logger.trace("CVScan update: source {}, controller {}, Objects.equals(controller,source) = {}",
				source, controller, Objects.equals(controller, source));

		if (arg instanceof EpicsCVScanState state) {
			if (state == EpicsCVScanState.Reduction) {
				// sometime can not receive this from EPICS, so move raw data writer to FLYBACK
				logger.info("{}: data reduction", getName());
				if (isBeamMonitorRunning) {
					stopBeamMonitor();
				}
				if (((EpicsMultiChannelScaler) mcsDetectors.get(0)).isShutterOpened()) {
					try {
						((EpicsMultiChannelScaler) mcsDetectors.get(0)).closeShutter();
					} catch (DeviceException e) {
						logger.error("{}: Failed to close fast shutter", getName());
					}
				}
				// save raw data
				Callable<String> worker = new SaveRawData();
				Future<String> submit = executor.submit(worker);
				list.add(submit);
			} else if (state == EpicsCVScanState.Flyback) {
				logger.info("{}: flyback", getName());
				if (isBeamMonitorRunning) {
					stopBeamMonitor();
				}
				if (((EpicsMultiChannelScaler) mcsDetectors.get(0)).isShutterOpened()) {
					try {
						((EpicsMultiChannelScaler) mcsDetectors.get(0)).closeShutter();
					} catch (DeviceException e) {
						logger.error("{}: Failed to close fast shutter", getName());
					}
				}
				// save data
				list.add(executor.submit(new SaveRawData()));
				list.add(executor.submit(new SaveRebinnedData()));
			} else if (state == EpicsCVScanState.Paused) {
				InterfaceProvider.getTerminalPrinter().print(getName() + ": Paused");
				logger.info("{}: paused", getName());
			} else if (state == EpicsCVScanState.Fault) {
				String message = controller.getMessage();
				InterfaceProvider.getTerminalPrinter().print(getName() + ": Status=Fault, " + "message=" + message);
				logger.error("Current State at {}, message = {}", arg, message);
				try {
					InterfaceProvider.getTerminalPrinter().print("try to restart scan...");
					if (retrycount > 3) {
						InterfaceProvider.getTerminalPrinter().print(
								"Abort current CV scan/script: maximum number of retry exceeded.");
						if (JythonServerFacade.getInstance().getScanStatus() == JythonStatus.RUNNING) {
							JythonServerFacade.getInstance().abortCommands();
						}
						if (JythonServerFacade.getInstance().getScriptStatus() == JythonStatus.RUNNING) {
							JythonServerFacade.getInstance().abortCommands();
						}
						stop();
						InterfaceProvider
								.getTerminalPrinter()
								.print("Please click 'STOP ALL' button to abort current scan and solve the EPICS FAULT problem before restart scan.");
						retrycount = 0;
					}
					retrycount++;
					restart();
				} catch (DeviceException e) {
					logger.error(getName() + ": retry cvscan on Fault state failed.", e);
				}
				if (isBeamMonitorRunning) {
					stopBeamMonitor();
				}
			} else if (state == EpicsCVScanState.Done) {
				InterfaceProvider.getTerminalPrinter().print(getName() + ": Completed");
				logger.info("{}: completed", getName());
				if (isBeamMonitorRunning) {
					stopBeamMonitor();
				}
				retrycount = 0;
			} else if (state == EpicsCVScanState.Aborted) {
				InterfaceProvider.getTerminalPrinter().print(getName() + ": Aborted");
				logger.info("{}: aborted", getName());
				if (isBeamMonitorRunning) {
					stopBeamMonitor();
				}
				retrycount = 0;
			} else if (state == EpicsCVScanState.Executing) {
				InterfaceProvider.getTerminalPrinter().print(getName() + ": Executing");
				logger.info("{}: executing", getName());
			} else if (state == EpicsCVScanState.LVIO) {
				InterfaceProvider.getTerminalPrinter().print(getName() + ": Limit Violation");
				InterfaceProvider
						.getTerminalPrinter()
						.print("Please click 'STOP ALL' button to abort current scan and solve limit problem before restart scan.");
				logger.info("{}: Limit Violation", getName());
			}
		}
		if (arg instanceof String) {
			try {
				if (getAvailableCVScanProfiles().contains(arg)) {
					logger.info("{}: Profile updated to {}", getName(), arg);
				}
			} catch (Exception e) {
				logger.error(getName() + ": Error handle profile updated ", e);
			}
		}
	}

	/**
	 * used to add scannables to cvscan which are passed to the MAC data writer to capture their values as metadata in
	 * the header.
	 *
	 * @param scannableList
	 */
	public void addScannables(ArrayList<Scannable> scannableList) {
		for (Scannable s : scannableList) {
			scannables.add(s);
		}
	}

	public void addScannable(Scannable s) {
		if (!scannables.contains(s)) {
			scannables.add(s);
		}
	}

	public void removeScannable(Scannable s) {
		if (scannables.contains(s)) {
			scannables.remove(s);
		}
	}

	public void removeAllScannables() {
		if (!scannables.isEmpty()) {
			for (Scannable s : scannables) {
				scannables.remove(s);
			}
		}
	}

	public ArrayList<Detector> getDetectors() {
		return mcsDetectors;
	}

	public void addDetector(Detector mcsDetector) {
		this.mcsDetectors.add(mcsDetector);
	}

	public void setDetectors(ArrayList<Detector> mcsDetectors) {
		this.mcsDetectors = mcsDetectors;
	}

	public EpicsCVScan getController() {
		return controller;
	}

	public void setController(EpicsCVScan controller) {
		this.controller = controller;
	}

	public IonChamberBeamMonitor getBeamMonitor() {
		return beamMonitor;
	}

	public void setBeamMonitor(IonChamberBeamMonitor beamMonitor) {
		this.beamMonitor = beamMonitor;
	}

	public Scannable getPsdScannableMotor() {
		return psdScannableMotor;
	}

	public void setPsdScannableMotor(Scannable psdScannableMotor) {
		this.psdScannableMotor = psdScannableMotor;
	}

	public SafePosition getPsdSafePosition() {
		return psdSafePosition;
	}

	public void setPsdSafePosition(SafePosition psdSafePosition) {
		this.psdSafePosition = psdSafePosition;
	}

	public EpicsCVscanDataWriter getDataWriter() {
		return dataWriter;
	}

	public void setDataWriter(EpicsCVscanDataWriter dataWriter) {
		this.dataWriter = dataWriter;
	}

	public Scriptcontroller getScriptController() {
		return scriptController;
	}

	public void setScriptController(Scriptcontroller scriptController) {
		this.scriptController = scriptController;
	}

	public Callable<?> getSafePositionSetup() {
		return safePositionSetup;
	}

	public void setSafePositionSetup(Callable<?> safePositionSetup) {
		this.safePositionSetup = safePositionSetup;
	}

	public boolean isSumming() {
		return summing;
	}

	public void setSumming(boolean summing) {
		this.summing = summing;
	}

}
