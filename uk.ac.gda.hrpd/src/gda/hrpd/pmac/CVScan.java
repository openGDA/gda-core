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

package gda.hrpd.pmac;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
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
import gda.hrpd.pmac.EpicsCVScanController.CurrentState;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServerFacade;
import gda.jython.JythonStatus;
import gda.observable.IObserver;
import gov.aps.jca.CAException;
import uk.ac.diamond.scisoft.analysis.SDAPlotter;

public class CVScan extends ScannableMotionBase implements IObserver {

	private static final Logger logger = LoggerFactory.getLogger(CVScan.class);
	// store for additional scannable to be added dynamically which produce metadata in data file header
	private ArrayList<Scannable> scannables = new ArrayList<Scannable>();
	// objects this class object depends on
	private ArrayList<Detector> mcsDetectors = new ArrayList<Detector>();
	private EpicsCVScanController controller;
	private IonChamberBeamMonitor beamMonitor;
	private EpicsCVscanDataWriter dataWriter;
	// varaibles to cache the various states of objects
	private volatile boolean isBeamMonitorRunning = false;
	public volatile boolean paused;
	// private volatile CurrentState state;
	private volatile int pausedCounter = 0;
	private volatile boolean firstTime = true;
	private volatile boolean isGDAScanning = false;
	private volatile long collectionNumber = 1;

	private double totaltime;
	private int actualpulses;
	private String rawfilename;
	private String rebinfilename;
	private String plotPanelName = null;
	private ArrayList<String> profiles;
	private File rawfile;
	private File rebinnedfile;
	private int retrycount = 0;

	private Thread dataSaverThread;
	private Scannable psdScannableMotor;
	private SafePosition psdSafePosition;

	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			if (controller != null) {
				controller.addIObserver(this);
			} else {
				throw new FactoryException("EpicsCVScanController object is not defined.");
			}
			if (mcsDetectors == null || mcsDetectors.isEmpty()) {
				throw new FactoryException("Detector objects are not defined.");
			}
			if (dataWriter == null) {
				throw new FactoryException("Data Writer object is not defined.");
			}
			if (beamMonitor == null) {
				throw new FactoryException("Beam monitor object is not defined.");
			}
			if (getPlotPanelName() == null) {
				throw new FactoryException("Missing Plot Panel Name configuration for " + getName());
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
			if (Math
					.abs(Double.parseDouble(psdScannableMotor.getPosition().toString()) - psdSafePosition.getPosition()) > psdSafePosition
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
	}

	private void filesToWriteTo() {
		rebinnedfile = new File(appendToFileName(getDataWriter().getDataDir(), getDataWriter().getCurrentFileName(),
				collectionNumber, LocalProperties.get("gda.data.file.extension.rebinned", "dat")));
	}

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
		paused = false;
		try {
			if (dataSaverThread != null) {
				dataSaverThread.join(); // wait until both raw data and rebinned data files are written.
			}
		} catch (InterruptedException e) {
			logger.warn("Save data thread is interrupted.", e);
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
			logger.error(getName() + " failed to stop CVScan",  e);
			throw new DeviceException(getName() + ": failed to stop CVScan", e);
		}
	}

	// / Extension functions - EPICS CVScan specific methods ///

	public void startUpdatePlot() {
		controller.setLive(true);
	}

	public void stopUpdatePlot() {
		controller.setLive(false);
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
				if( e instanceof RuntimeException)
					throw (RuntimeException)e;
				if( e instanceof DeviceException)
					throw (DeviceException)e;
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
				if( e instanceof RuntimeException)
					throw (RuntimeException)e;
				if( e instanceof DeviceException)
					throw (DeviceException)e;
				throw new DeviceException(getName() + ": set 2nd motor scan range failed.", e);
			}
		} else {
			InterfaceProvider.getTerminalPrinter().print(
					getName() + ": 2nd motor is not available in the Current CVScan profile.");
		}
	}

	private String appendToFileName(String dir, String filename, long collectionNumber, String ext) {
		return dir.concat(filename).concat("-").concat(String.format("%03d", collectionNumber)).concat(".").concat(ext);
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

	private class SaveData implements Runnable {

		@Override
		public void run() {
			if (pausedCounter == 0) {
				saveRawData();
			} else {
				InterfaceProvider.getTerminalPrinter().print(
						"CVScan had been paused " + pausedCounter + " times. No raw data file created");
				logger.info("CVScan had been paused {} times. No raw data file created.", pausedCounter);
			}

			saveRebinnedData();
		}
	}

	@SuppressWarnings("unused")
	private class SaveRawData implements Runnable {
		@Override
		public void run() {
			saveRawData();
		}
	}

	private String saveRawData() {
		double[] raw2theta = null;
		double monitorAverage = 0;
		try {
			monitorAverage = controller.getMonitorAvaerage();
		} catch (Throwable e) {
			logger.error("can not get raw data from {}", controller.getName(), e);
		}
		actualpulses = controller.getRaw2ThetaSize();
		try {
			raw2theta = controller.getRaw2ThetaPositions();
		} catch (Exception e) {
			logger.error("Timeout: can not get raw two-theta positions from {}", controller.getName(), e);
		}

		int[][] data = new int[2 * EpicsMcsSis3820.MAX_NUMBER_MCA][controller.getTotalNumberOfPulses()];
		// retrieve the count data from detectors
		InterfaceProvider.getTerminalPrinter().print("reading raw data from detectors");
		for (int i = 0; i < mcsDetectors.size(); i++) {
			for (Map.Entry<Integer, Mca> e : ((EpicsMultiChannelScaler) mcsDetectors.get(i)).getMcaList().entrySet()) {
				try {
					data[e.getKey().intValue() - 1] = ((EpicsMultiChannelScaler) mcsDetectors.get(i)).getData(e
							.getValue().getScalerChannel());
				} catch (Throwable ex) {
					logger.error("can not get detector data from " + mcsDetectors.get(i).getName() + " "
							+ e.getValue().getName(), ex);
				}
			}
		}
		rawfilename = getDataWriter().addRawData(rawfile, actualpulses, scannables, inputNames[0].trim(), mcsDetectors,
				raw2theta, data, totaltime, monitorAverage);

		return rawfilename;
	}

	@SuppressWarnings("unused")
	private class SaveRebinnedData implements Runnable {
		@Override
		public void run() {
			saveRebinnedData();
		}
	}

	private String saveRebinnedData() {
		double[] rebinned2theta = null;
		double[] rebinnedCounts = null;
		double[] rebinnedCountErrors = null;
		double monitorAverage = 0;
		int numberOfElements = 0;
		try {
			monitorAverage = controller.getMonitorAvaerage();
			rebinned2theta = controller.getRebinned2ThetaPositions();
			rebinnedCounts = controller.getRebinnedCounts();
			rebinnedCountErrors = controller.getRebinnedCountErrors();
			numberOfElements = controller.getRebinned2ThetaSize();
		} catch (Throwable e) {
			logger.error("can not get rebinned data from {}", controller.getName(), e);
		}
		rebinfilename = getDataWriter().addRebinnedData(rebinnedfile, numberOfElements, scannables, rebinned2theta,
				rebinnedCounts, rebinnedCountErrors, totaltime, monitorAverage);
		Dataset counts = DatasetFactory.createFromObject(rebinnedCounts);
		counts.setName(getFilename());
		try {
			SDAPlotter.plot(getPlotPanelName(), DatasetFactory.createFromObject(rebinned2theta), counts);
		} catch (Exception e) {
			logger.error("MAC detector rebinned data plot failed.", e);
		}

		return rebinfilename;
	}

	/**
	 * Observer of Current State of {@link EpicsCVScanController} object. On state updates to {@code Reduction}, kicks
	 * off a new threads to save raw data; on state updates to @{code Flyback}, kicks off a new thread to save rebinned
	 * data. Any other states just print message to the Jython terminal.
	 */
	@Override
	public void update(Object source, Object arg) {
		if (source instanceof EpicsCVScanController && arg instanceof CurrentState) {
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
			} else if ((CurrentState) arg == CurrentState.Flyback) {
				if (!firstTime) {
					// exclude object creation update as jython server not avaibale
					logger.info("{}: flyback", getName());
					dataSaverThread = uk.ac.gda.util.ThreadManager.getThread(new SaveData(), "DataSaver");
					dataSaverThread.start();
				} else {
					firstTime = false;
				}
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
			} else if ((CurrentState) arg == CurrentState.Paused) {
				if (!firstTime) {
					// exclude object creation update as jython server not avaibale
					InterfaceProvider.getTerminalPrinter().print(getName() + ": Paused");
					logger.info("{}: paused", getName());
				} else {
					firstTime = false;
				}
			} else if ((CurrentState) arg == CurrentState.Fault) {
				if (!firstTime) {
					// exclude object creation update as jython server not avaibale
					String message = controller.getMessage();
					InterfaceProvider.getTerminalPrinter().print(getName() + ": Status=Fault, " + "message=" + message);
					logger.error("Current State at {}, message = {}", ((CurrentState) arg).toString(), message);
					try {
						InterfaceProvider.getTerminalPrinter().print("try to restart scan...");
						if (retrycount > 3) {
							InterfaceProvider.getTerminalPrinter().print(
									"Abort current CV scan/script: maximum number of retry exceeded.");
							if (JythonServerFacade.getInstance().getScanStatus() == JythonStatus.RUNNING) {
								InterfaceProvider.getCommandAborter().abortCommands();
							}
							if (JythonServerFacade.getInstance().getScriptStatus() == JythonStatus.RUNNING) {
								InterfaceProvider.getCommandAborter().abortCommands();
							}
							stop();
							InterfaceProvider
									.getTerminalPrinter()
									.print(
											"Please click 'STOP ALL' button to abort current scan and solve the EPICS FAULT problem before restart scan.");
							retrycount = 0;
						}
						retrycount++;
						restart();
					} catch (DeviceException e) {
						logger.error(getName() + ": retry cvscan on Fault state failed.", e);
					}
				} else {
					firstTime = false;
				}
				if (isBeamMonitorRunning) {
					stopBeamMonitor();
				}
			} else if ((CurrentState) arg == CurrentState.Done) {
				if (!firstTime) {
					// exclude object creation update as jython server not avaibale
					InterfaceProvider.getTerminalPrinter().print(getName() + ": Completed");
					logger.info("{}: completed", getName());
				} else {
					firstTime = false;
				}
				if (isBeamMonitorRunning) {
					stopBeamMonitor();
				}
				retrycount=0;
			} else if ((CurrentState) arg == CurrentState.Aborted) {
				if (!firstTime) {
					// exclude object creation update as jython server not avaibale
					InterfaceProvider.getTerminalPrinter().print(getName() + ": Aborted");
					logger.info("{}: aborted", getName());
				} else {
					firstTime = false;
				}
				if (isBeamMonitorRunning) {
					stopBeamMonitor();
				}
				retrycount=0;
			} else if ((CurrentState) arg == CurrentState.Executing) {
				if (!firstTime) {
					// exclude object creation update as jython server not avaibale
					InterfaceProvider.getTerminalPrinter().print(getName() + ": Executing");
					logger.info("{}: executing", getName());
				} else {
					firstTime = false;
				}
			} else if ((CurrentState) arg == CurrentState.LVIO) {
				if (!firstTime) {
					// exclude object creation update as jython server not avaibale
					InterfaceProvider.getTerminalPrinter().print(getName() + ": Limit Violation");
					InterfaceProvider
							.getTerminalPrinter()
							.print(
									"Please click 'STOP ALL' button to abort current scan and solve limit problem before restart scan.");
					logger.info("{}: Limit Violation", getName());
				} else {
					firstTime = false;
				}
			}
		}
		if (source instanceof EpicsCVScanController && arg instanceof String) {
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

	public EpicsCVScanController getController() {
		return controller;
	}

	public void setController(EpicsCVScanController controller) {
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

	public String getPlotPanelName() {
		return plotPanelName;
	}

	public void setPlotPanelName(String plotPanelName) {
		this.plotPanelName = plotPanelName;
	}
}
