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
import gda.hrpd.pmac.EpicsCVScanController.CurrentState;
import gda.hrpd.pmac.SafePosition;
import gda.hrpd.pmac.UnsafeOperationException;
import gda.jython.InterfaceProvider;
import gda.jython.Jython;
import gda.jython.JythonServerFacade;
import gda.jython.scriptcontroller.ScriptControllerBase;
import gda.jython.scriptcontroller.Scriptcontroller;
import gda.observable.IObserver;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.hrpd.cvscan.event.FileNumberEvent;

import com.google.common.base.Joiner;

/**
 * * <li>Specify {@link Scriptcontroller} instance to handle data file name changed event {@link FileNumberEvent} which
 * facilitate server to client communication</li>
 */
public class CVScan extends ScannableMotionBase implements IObserver, InitializingBean {

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

	private static final int NTHREDS = 2;
	private ExecutorService executor = Executors.newFixedThreadPool(NTHREDS);
	// collision prevention objects
	private Scannable psdScannableMotor;
	private SafePosition psdSafePosition;
	// server-to-client event passing object
	private Scriptcontroller scriptController;

	private ArrayList<Future<String>> list = new ArrayList<Future<String>>();

	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			if (controller != null) {
				controller.addIObserver(this);
			} else {
				throw new FactoryException("EpicsCVScan object is not defined.");
			}

			this.setInputNames(new String[] { "tth" });
			this.setOutputFormat(new String[] { "%s" });
			try {
				getAvailableCVScanProfiles();
			} catch (Exception e) {
				throw new FactoryException("getAvailableCVScanProfiles failed " + getName());
			} // initialise profiles variable.
			this.level = 9;
			configured = true;
		}
	}

	/*
	 * send data filename to observers in client from the server.
	 */
	private void fireNewDataFile() {
		if (getScriptController() != null && getScriptController() instanceof ScriptControllerBase) {
			((ScriptControllerBase) getScriptController()).update(this, new FileNumberEvent(getDataWriter()
					.getCurrentFileName(), collectionNumber));
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
		}
		pausedCounter = 0; // initialise counter for paused flag for this cvscan
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
			logger.info("{}  is in a PAUSED state", getName());
		}
	}

	private void checkForCollision() throws DeviceException {
		// collision avoidance check delta motor position only proceed if delta motor is at PSD Safe Position defined in
		// Spring configuration
		if (psdScannableMotor != null) {
			if (Math.abs(Double.parseDouble(psdScannableMotor.getPosition().toString()) - psdSafePosition.getPosition()) > psdSafePosition
					.getTolerance()) {
				throw new UnsafeOperationException(psdScannableMotor.getPosition(), psdSafePosition.getPosition(),
						"Cannot proceed as PSD detector is not at safe position.");
			}
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
		return getFilename();
	}

	private String getFilename() {
		int end = rebinnedfile.getName().indexOf(".");
		return rebinnedfile.getName().substring(0, end);
	}

	@Override
	public boolean rawIsBusy() {
		return controller.isBusy();
	}

	@Override
	public void atScanStart() throws DeviceException {
		checkForCollision();
		// any preparation works here.
		paused = false;
		pausedCounter=0;
		isGDAScanning = true;
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
	public void atScanEnd() {
		paused = false;
		isGDAScanning = false;
		collectionNumber = 1;
		controller.setCollectionNumber(collectionNumber);
		controller.setGDAScanning(false);
		// kick off other post processes
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
		rebinnedfile = new File(appendToFileName(getDataWriter().getDataDir(), getDataWriter().getCurrentFileName(),
				collectionNumber, LocalProperties.get("gda.data.file.extension.rebinned", "dat")));
	}

	/*
	 * create file handle for both RAW data and rebinned data.
	 */
	private void createFilesToWriteTo() {
		rawfile = new File(appendToFileName(getDataWriter().getDataDir(), getDataWriter().getCurrentFileName(),
				collectionNumber, LocalProperties.get("gda.data.file.extension.raw", "raw")));
		InterfaceProvider.getTerminalPrinter().print("Raw data will be saved to file: " + rawfile.getAbsolutePath());
		rebinnedfile = new File(appendToFileName(getDataWriter().getDataDir(), getDataWriter().getCurrentFileName(),
				collectionNumber, LocalProperties.get("gda.data.file.extension.rebinned", "dat")));
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
		String datafile;
		paused = false;
		for (Future<String> future : list) {
			try {
				datafile=future.get();
			} catch (InterruptedException e) {
				logger.error("Data saving is interrupted.", e);
				throw new DeviceException("Data saving is interrupted.",e);
			} catch (ExecutionException e) {
				logger.error("Data saving throwsExecutionException.", e);
				throw new DeviceException("Data saving throwsExecutionException.",e);
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

	private String appendToFileName(String dir, String filename, long collectionNumber, String ext) {
		Joiner stringJioner = Joiner.on("-").skipNulls();
		String fname = stringJioner.join(dir, filename, String.format("%03d", collectionNumber), ext);
		return fname;
	}

	private void startBeamMonitor() {
		if (beamMonitor.isMonitorOn()) {
			// if beam monitor is not on, cvscan control thread will not be started.
			isBeamMonitorRunning = true;
			Thread bmt = uk.ac.gda.util.ThreadManager.getThread(new ScanControl(), "cvscanBeamMonitor");
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
			return saveRawData();
		}
	}

	private String saveRawData() throws Exception  {
		double[] raw2theta = null;
		double monitorAverage = 0;
		try {
			monitorAverage = controller.getMonitorAverage();
		} catch (TimeoutException | CAException | InterruptedException e1) {
			logger.error(getName()+" cannot get monitor avarge data from "+controller.getName(),e1);
			throw new Exception(getName()+" cannot get monitor avarge data from "+controller.getName(),e1);
		}
		actualpulses = controller.getRaw2ThetaSize();
		try {
			raw2theta = controller.getRaw2ThetaPositions();
		} catch (TimeoutException | CAException | InterruptedException e1) {
			logger.error(getName()+" cannot get raw two-theta positions data from "+controller.getName(),e1);
			throw new Exception(getName()+" cannot get raw two-theta positions data from "+controller.getName(),e1);
		}

		int[][] data = new int[2 * EpicsMcsSis3820.MAX_NUMBER_MCA][controller.getTotalNumberOfPulses()];
		// retrieve the count data from detectors
		InterfaceProvider.getTerminalPrinter().print("reading raw data from detectors");
		for (int i = 0; i < mcsDetectors.size(); i++) {
			for (Map.Entry<Integer, Mca> e : ((EpicsMultiChannelScaler) mcsDetectors.get(i)).getMcaList().entrySet()) {
				try {
					data[e.getKey().intValue() - 1] = ((EpicsMultiChannelScaler) mcsDetectors.get(i)).getData(e
							.getValue().getScalerChannel());
				} catch (DeviceException e1) {
					logger.error(getName()+" cannot get detector data from " + mcsDetectors.get(i).getName() + " "
							+ e.getValue().getName(), e1);
					throw new Exception(getName()+" cannot get detector data from " + mcsDetectors.get(i).getName() + " "
							+ e.getValue().getName(), e1);
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
			logger.error(getName()+" cannot get monitor average data from "+controller.getName(),e);
			throw new Exception(getName()+" cannot get monitor average data from "+controller.getName(),e);
		}
		try {
			rebinned2theta = controller.getRebinned2ThetaPositions();
		} catch (TimeoutException | CAException | InterruptedException e) {
			logger.error(getName()+" cannot get rebinned two-theta positions data from "+controller.getName(),e);
			throw new Exception(getName()+" cannot get rebinned two-theta positions data from "+controller.getName(),e);
		}
		try {
			rebinnedCounts = controller.getRebinnedCounts();
		} catch (TimeoutException | CAException | InterruptedException e) {
			logger.error(getName()+" cannot get rebinned counts data from "+controller.getName(),e);
			throw new Exception(getName()+" cannot get rebinned counts data from "+controller.getName(),e);
		}
		try {
			rebinnedCountErrors = controller.getRebinnedCountErrors();
		} catch (TimeoutException | CAException | InterruptedException e) {
			logger.error(getName()+" cannot get rebinned count errors data from "+controller.getName(),e);
			throw new Exception(getName()+" cannot get rebinned count errors data from "+controller.getName(),e);
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
		if (source==controller && arg instanceof CurrentState) {
			if ((CurrentState) arg == CurrentState.Reduction) {
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
			} else if ((CurrentState) arg == CurrentState.Flyback) {
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
				// save reduced data
				Callable<String> worker = new SaveRebinnedData();
				Future<String> submit = executor.submit(worker);
				list.add(submit);
			} else if ((CurrentState) arg == CurrentState.Paused) {
					InterfaceProvider.getTerminalPrinter().print(getName() + ": Paused");
					logger.info("{}: paused", getName());
			} else if ((CurrentState) arg == CurrentState.Fault) {
					String message = controller.getMessage();
					InterfaceProvider.getTerminalPrinter().print(getName() + ": Status=Fault, " + "message=" + message);
					logger.error("Current State at {}, message = {}", ((CurrentState) arg).toString(), message);
					try {
						InterfaceProvider.getTerminalPrinter().print("try to restart scan...");
						if (retrycount > 3) {
							InterfaceProvider.getTerminalPrinter().print(
									"Abort current CV scan/script: maximum number of retry exceeded.");
							if (JythonServerFacade.getInstance().getScanStatus() == Jython.RUNNING) {
								JythonServerFacade.getInstance().abortCommands();
							}
							if (JythonServerFacade.getInstance().getScriptStatus() == Jython.RUNNING) {
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
			} else if ((CurrentState) arg == CurrentState.Done) {
					InterfaceProvider.getTerminalPrinter().print(getName() + ": Completed");
					logger.info("{}: completed", getName());
				if (isBeamMonitorRunning) {
					stopBeamMonitor();
				}
				retrycount = 0;
			} else if ((CurrentState) arg == CurrentState.Aborted) {
					InterfaceProvider.getTerminalPrinter().print(getName() + ": Aborted");
					logger.info("{}: aborted", getName());
				if (isBeamMonitorRunning) {
					stopBeamMonitor();
				}
				retrycount = 0;
			} else if ((CurrentState) arg == CurrentState.Executing) {
					InterfaceProvider.getTerminalPrinter().print(getName() + ": Executing");
					logger.info("{}: executing", getName());
			} else if ((CurrentState) arg == CurrentState.LVIO) {
					InterfaceProvider.getTerminalPrinter().print(getName() + ": Limit Violation");
					InterfaceProvider
							.getTerminalPrinter()
							.print("Please click 'STOP ALL' button to abort current scan and solve limit problem before restart scan.");
					logger.info("{}: Limit Violation", getName());
			}
		}
		if (source==controller && arg instanceof String) {
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

	@Override
	public void afterPropertiesSet() throws Exception {
		if (controller != null) {
			throw new IllegalStateException("EpicsCVScan object is not defined.");
		}
		if (mcsDetectors == null || mcsDetectors.isEmpty()) {
			throw new IllegalStateException("Detector objects are not defined.");
		}
		if (dataWriter == null) {
			throw new IllegalStateException("Data Writer object is not defined.");
		}
		if (beamMonitor == null) {
			throw new IllegalStateException("Beam monitor object is not defined.");
		}
	}
}
