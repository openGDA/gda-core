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

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;
import gda.scan.Scan.ScanStatus;
import gov.aps.jca.CAException;
import gov.aps.jca.CAStatus;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import gov.aps.jca.event.PutEvent;
import gov.aps.jca.event.PutListener;

public class EpicsCVScan extends DeviceBase implements InitializationListener {

	private static final Logger logger = LoggerFactory.getLogger(EpicsCVScan.class);

	/* base class declared name private, so need to re-declare here */
	@SuppressWarnings("unused")
	private String name = null;
	private String pv_root = null;

	/* cached data - volatile as they are updated potentially by multiple threads */
	private volatile EpicsCVScanState currentstate = EpicsCVScanState.Done;
	private volatile String message = null;
	private volatile int numberofpulsedone;
	private volatile int totalnumberofpulse;
	private volatile String profile;
	private volatile boolean busy = false;

	private StateListener statel;
//	private MessageListener messagel;
	private ProfileListener pfl;
	// control channels
	private Channel timechannel;
	private Channel profilechannel;
	private Channel keepseparate;
	private Channel mstart;
	private Channel mrange;
	private Channel start;
	private Channel pause;
	private Channel abort;
	private Channel currentstatechannel;
//	private Channel statusmessagechannel;
	// private Channel pulsesnumberdonechannel;
	// private Channel puslestotalnumberchannel;
	private volatile boolean GDAScanning = false;

	public boolean isGDAScanning() {
		return GDAScanning;
	}

	public void setGDAScanning(boolean gDAScanning) {
		GDAScanning = gDAScanning;
	}

	/**
	 * EPICS controller
	 */
	private EpicsController controller;

	/**
	 * EPICS Channel Manager
	 */
	private EpicsChannelManager channelManager;
	private Vector<String> profiles = new Vector<String>();
	private StartCallbackListener startcallbacklistener;
	private PauseCallbackListener pausecallbacklistener;
	private AbortCallbackListener abortcallbacklistener;
	// data channels
	private Channel rawx;
	private Channel allx;
	private Channel ally;
	private Channel allye;
	private Channel mav;
	@SuppressWarnings("unused")
	private boolean local;
	private long collectionNumber;
	private long fileNumber;

	private IScanStatusProvider jythonScanStatus;

	public long getFileNumber() {
		return fileNumber;
	}

	public void setFileNumber(long fileNumber) {
		this.fileNumber = fileNumber;

	}

	public long getCollectionNumber() {
		return collectionNumber;
	}

	public void setCollectionNumber(long collectionNumber) {
		this.collectionNumber = collectionNumber;
	}

	public EpicsCVScan() {
		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager(this);
		statel = new StateListener();
//		messagel = new MessageListener();
		startcallbacklistener = new StartCallbackListener();
		pausecallbacklistener = new PauseCallbackListener();
		abortcallbacklistener = new AbortCallbackListener();
		pfl = new ProfileListener();
	}

	@Override
	public void configure() throws FactoryException {
		if (!isConfigured()) {
			if (getPv_root() != null) {
				createChannelAccess(pv_root);
				channelManager.tryInitialize(100);
			} else {
				logger.error("Missing EPICS configuration for {}", getName());
				throw new FactoryException("Missing EPICS interface configuration for " + getName());
			}
			jythonScanStatus = new ScanStatusProvider();
			setConfigured(true);
		}
	}

	private void createChannelAccess(String pvRoot) throws FactoryException {

		try {
			timechannel = channelManager.createChannel(pvRoot + ":TIME", false);
			profilechannel = channelManager.createChannel(pvRoot + ":PROFILE", pfl, false);
			// flyback = channelManager.createChannel(pvRoot + ":FLY", false);
			keepseparate = channelManager.createChannel(pvRoot + ":KEEP", false);
			// mflyback = channelManager.createChannel(pvRoot + ":MFLY", false);
			mstart = channelManager.createChannel(pvRoot + ":MSTART", false);
			mrange = channelManager.createChannel(pvRoot + ":MRANGE", false);
			start = channelManager.createChannel(pvRoot + ":START", false);
			pause = channelManager.createChannel(pvRoot + ":PAUSE", false);
			abort = channelManager.createChannel(pvRoot + ":ABORT", false);
			currentstatechannel = channelManager.createChannel(pvRoot + ":STATE", statel, false);
//			statusmessagechannel = channelManager.createChannel(pvRoot + ":MESSAGE", messagel, false);
			// Raw 2theta positions where scaler was triggered
			rawx = channelManager.createChannel(pvRoot + ":RAWX", false);
			// X scale of the combined, rebinned data
			allx = channelManager.createChannel(pvRoot + ":ALLX", false);
			// Y counts on the combined, rebinned data
			ally = channelManager.createChannel(pvRoot + ":ALLY", false);
			// Count errors on the combined rebinned data
			allye = channelManager.createChannel(pvRoot + ":ALLYE", false);
			mav = channelManager.createChannel(pvRoot + ":MAV", false);
			// acknowledge that creation phase is completed
			channelManager.creationPhaseCompleted();
		} catch (Throwable th) {
			throw new FactoryException("failed to create reuqired channels", th);
		}
	}

	public double getMonitorAverage() throws TimeoutException, CAException, InterruptedException {
		return controller.cagetDouble(mav);
	}

	/**
	 * gets the total time of the constant velocity scan.
	 *
	 * @return total time in seconds
	 * @throws TimeoutException
	 * @throws CAException
	 * @throws InterruptedException
	 */
	public double getTime() throws TimeoutException, CAException, InterruptedException {
		return controller.cagetDouble(timechannel);
	}

	/**
	 * sets the total time for the constant velocity scan.
	 *
	 * @param time
	 *            in seconds
	 * @throws CAException
	 * @throws InterruptedException
	 */
	public void setTime(double time) throws CAException, InterruptedException {
		controller.caput(timechannel, time);
	}

	/**
	 * gets the motor components that participate the subsequent constant velocity scan.
	 *
	 * @return String motor names
	 * @throws DeviceException
	 */
	public String getProfile() throws DeviceException {
		short test;
		try {
			test = controller.cagetEnum(profilechannel);
			return profiles.get(test);
		} catch (Throwable th) {
			throw new DeviceException("failed to get profile from " + profilechannel.getName(), th);
		}
	}

	/**
	 * sets the motor components that participate the subsequent constant velocity scan.
	 *
	 * @param profile
	 * @throws DeviceException
	 * @throws InterruptedException
	 */
	public void setProfile(String profile) throws DeviceException, InterruptedException {
		if (profiles.contains(profile)) {
			int target = profiles.indexOf(profile);
			try {
				controller.caput(profilechannel, target);
			} catch (CAException e) {
				throw new DeviceException(profilechannel.getName() + " failed to move to " + profile, e);
			}
		} else {
			throw new DeviceException("Profile called " + profile + " is not found.");
		}
	}

	public boolean isKeepSeparate() throws TimeoutException, CAException, InterruptedException {
		String isKeepSeparate = controller.cagetLabels(keepseparate)[0];
		if (isKeepSeparate.equalsIgnoreCase("Yes")) {
			return true;
		}
		return false;
	}

	public void setKeepSeparate(boolean b) throws CAException, InterruptedException {
		if (b) {
			controller.caput(keepseparate, 1);
		} else {
			controller.caput(keepseparate, 0);
		}
	}

	/**
	 * gets the scan start position of 2nd motor that participating the constant velocity scan of two-theta.
	 *
	 * @return the 2nd motor scan range.
	 * @throws TimeoutException
	 * @throws CAException
	 * @throws InterruptedException
	 */
	public double get2ndMotorStartPosition() throws TimeoutException, CAException, InterruptedException {
		return controller.cagetDouble(mstart);
	}

	/**
	 * sets the scan start position of 2nd motor that participating the constant velocity scan of two-theta.
	 *
	 * @param position
	 * @throws CAException
	 * @throws InterruptedException
	 */
	public void set2ndMotorStartPosition(double position) throws CAException, InterruptedException {
		controller.caput(mstart, position);
	}

	/**
	 * gets the scan range of 2nd motor that participating the constant velocity scan of two-theta.
	 *
	 * @return the 2nd motor scan range.
	 * @throws TimeoutException
	 * @throws CAException
	 * @throws InterruptedException
	 */
	public double get2ndMotorScanRange() throws TimeoutException, CAException, InterruptedException {
		return controller.cagetDouble(mrange);
	}

	/**
	 * sets the scan range of 2nd motor that participating the constant velocity scan of two-theta.
	 *
	 * @param position
	 * @throws CAException
	 * @throws InterruptedException
	 */
	public void set2ndMotorScanRange(double position) throws CAException, InterruptedException {
		controller.caput(mrange, position);
	}

	/**
	 * starts, restarts, or resume the constant velocity scan. The scan only starts when its current state is in one of
	 * the following modes: Done, Aborted, Paused or Fault. This is done to ensure the EPICS "Start" is never being
	 * called more than once during a constant velocity scan.
	 *
	 * @throws CAException
	 * @throws InterruptedException
	 */
	public void start() throws CAException, InterruptedException {
		if (currentstate == EpicsCVScanState.Done || currentstate == EpicsCVScanState.Aborted
				|| currentstate == EpicsCVScanState.Paused || currentstate == EpicsCVScanState.Fault) {
			busy = true;
			controller.caput(start, 1, startcallbacklistener);
//			jythonScanStatus.setStatus(ScanStatus.RUNNING);
			logger.info("{}: Start CV scan", getName());
		} else {
			InterfaceProvider.getTerminalPrinter().print(
					"EPICS CVScan is busy, its current state is " + currentstate.toString());
			logger.warn("EPICS CVScan is busy. its current state is {}", currentstate.toString());
		}
	}

	/**
	 * pauses current constant velocity scan.
	 *
	 * @throws CAException
	 * @throws InterruptedException
	 */
	public void pause() throws CAException, InterruptedException {
		controller.caput(pause, 1, pausecallbacklistener);
		logger.info("{}: Pause CV scan", getName());
	}

	/**
	 * aborts current constant velocity scan.
	 *
	 * @throws CAException
	 * @throws InterruptedException
	 */
	public void abort() throws CAException, InterruptedException {
		controller.caput(abort, 1, abortcallbacklistener);
		logger.info("{}: Abort CV scan", getName());
	}

	/**
	 * pulls the current state of the CVScan from EPICS and update cached {@link #currentstate}.
	 *
	 * @return the current state
	 * @throws TimeoutException
	 * @throws CAException
	 * @throws InterruptedException
	 */
	public EpicsCVScanState getCurrentState() throws TimeoutException, CAException, InterruptedException {
		short value = controller.cagetEnum(currentstatechannel);
		if (value == 0) {
			currentstate = EpicsCVScanState.Done;
		} else if (value == 1) {
			currentstate = EpicsCVScanState.Aborted;
		} else if (value == 2) {
			currentstate = EpicsCVScanState.Executing;
		} else if (value == 3) {
			currentstate = EpicsCVScanState.Flyback;
		} else if (value == 4) {
			currentstate = EpicsCVScanState.Paused;
		} else if (value == 5) {
			currentstate = EpicsCVScanState.Fault;
		} else if (value == 6) {
			currentstate = EpicsCVScanState.Reduction;
		} else if (value == 7) {
			currentstate = EpicsCVScanState.LVIO;
		} else {
			logger.error("{} reports UNKNOWN state value: {}", getName(), value);
			throw new IllegalStateException(getName() + " in a unknown state.");
		}

		return currentstate;
	}

	/****************** Access CVScan data *****************************/
	/**
	 * gets the raw two-theta position where scalers/detectors are triggered.
	 *
	 * @return two-theta positions
	 * @throws TimeoutException
	 * @throws CAException
	 * @throws InterruptedException
	 */
	public double[] getRaw2ThetaPositions() throws TimeoutException, CAException, InterruptedException {
		return controller.cagetDoubleArray(rawx);
	}

	public int getRaw2ThetaSize() {
		return rawx.getElementCount();
	}

	public double[] getRebinned2ThetaPositions() throws TimeoutException, CAException, InterruptedException {
		return controller.cagetDoubleArray(allx);
	}

	public int getRebinned2ThetaSize() {
		return allx.getElementCount();
	}

	public double[] getRebinnedCounts() throws TimeoutException, CAException, InterruptedException {
		return controller.cagetDoubleArray(ally);
	}

	public int getRebinnedCountsSize() {
		return ally.getElementCount();
	}

	public double[] getRebinnedCountErrors() throws TimeoutException, CAException, InterruptedException {
		return controller.cagetDoubleArray(allye);
	}

	public int getRebinnedCountErrorsSize() {
		return allye.getElementCount();
	}

	/**
	 * return the latest status message that updated from EPICS
	 *
	 * @return the cached {@link #message}
	 */
	public String getMessage() {
		return this.message;
	}

	/**
	 * returns the latest current state that updated from EPICS
	 *
	 * @return the cached {@link #currentstate}
	 */
	public EpicsCVScanState getState() {
		return currentstate;
	}

	/**
	 * returns the latest number of pulses completed that updated from EPICS
	 *
	 * @return the cached {@link #numberofpulsedone}
	 */
	public int getNumberOfPulseDone() {
		return numberofpulsedone;
	}

	/**
	 * returns the latest total number of pulses in this CVScan that updated from EPICS
	 *
	 * @return the cached {@link #totalnumberofpulse}
	 */
	public int getTotalNumberOfPulses() {
		return totalnumberofpulse;
	}

	public String getPv_root() {
		return pv_root;
	}

	public void setPv_root(String pvRoot) {
		pv_root = pvRoot;
	}

	public String[] getProfiles() throws InterruptedException {
		String[] profileLabels = new String[profiles.size()];
		try {
			profileLabels = controller.cagetLabels(profilechannel);
		} catch (TimeoutException e) {
			logger.error("Timeout on initialising Profiles of " + getName(), e);
		} catch (CAException e) {
			logger.error("CAException on initialising Profiles of " + getName(), e);
		}
		return profileLabels;
	}

	@Override
	public void initializationCompleted() throws InterruptedException {
		// initialise Profile choices
		for (String profileName : getProfiles()) {
			if (profileName != null && !profileName.isEmpty()) {
				this.profiles.add(profileName);
			}
		}
		// initialise state variables
		try {
			this.currentstate = getCurrentState();
		} catch (TimeoutException e) {
			logger.error("Timeout on initialising Current State of " + getName(), e);
		} catch (CAException e) {
			logger.error("CAException on initialising Current State of " + getName(), e);
		}
		logger.info("{} is initialised", this.getName());
	}

	public boolean isBusy() {
		return busy;
	}

	public void setBusy(boolean b) {
		busy = b;
	}


	/**
	 * Monitor Current State in EPICS, and update cached state variable.
	 */
	private class StateListener implements MonitorListener {
		private boolean first = true;

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			short value = -1;
			DBR dbr = arg0.getDBR();
			if (dbr.isENUM()) {
				value = ((DBR_Enum) dbr).getEnumValue()[0];
			} else {
				logger.error("{} : StateListener expect Enum type but got {} type.", getName(), dbr.getType());
			}
			if (value == 0) {
				currentstate = EpicsCVScanState.Done;
				busy = false;
			} else if (value == 1) {
				currentstate = EpicsCVScanState.Aborted;
				busy = false;
			} else if (value == 2) {
				currentstate = EpicsCVScanState.Executing;
				busy = true;
			} else if (value == 3) {
				currentstate = EpicsCVScanState.Flyback;
				busy = true;
			} else if (value == 4) {
				currentstate = EpicsCVScanState.Paused;
				busy = true;
			} else if (value == 5) {
				currentstate = EpicsCVScanState.Fault;
				busy = true; // GDA must be busy as auto retry kicked in on fault, otherwise for-loop carries on
			} else if (value == 6) {
				currentstate = EpicsCVScanState.Reduction;
				busy = true;
			} else if (value == 7) {
				currentstate = EpicsCVScanState.LVIO;
				busy = true;
			} else {
				logger.error("{} reports UNKNOWN state value: {}", getName(), value);
				throw new IllegalStateException(getName() + " in a unknown state.");
			}
			logger.info("Current State from EPICS {} update to {}", ((Channel) arg0.getSource()).getName(),
					currentstate);

			if (first) { // do not propagate 1st connection event to the observer
				first = false;
				return;
			}
			notifyIObservers(currentstate);
		}
	}

	private void notifyIObservers(EpicsCVScanState currentstate) {
		notifyIObservers(this, currentstate);
	}

//	/**
//	 * Monitor Status Message in EPICS, and update cached message variable.
//	 */
//	private class MessageListener implements MonitorListener {
//		boolean first = true;
//
//		@Override
//		public void monitorChanged(MonitorEvent arg0) {
//			if (first) {
//				first = false;
//				return;
//			}
//			DBR dbr = arg0.getDBR();
//			if (dbr.isSTRING()) {
//				message = ((DBR_String) dbr).getStringValue()[0];
//				if (InterfaceProvider.getTerminalPrinter() != null) {
//					InterfaceProvider.getTerminalPrinter().print(getName() + ": " + message);
//				}
//			} else {
//				logger.error("{} : MessageListener expect String type but got {} type.", getName(), dbr.getType());
//			}
//		}
//	}

	/**
	 * Monitor the total number of pulses in EPICS Constant Velocity scan, and update cached total number of pulse
	 * variable. This state variable can be used to display progress bar on Client.
	 */
	private class ProfileListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr.isENUM()) {
				short profileindex = ((DBR_Enum) dbr).getEnumValue()[0];
				if (!profiles.isEmpty())
					profile = profiles.get(profileindex);
				notifyIObservers(profile);
			} else {
				logger.error("{} : ProfileListener expect Enum type but got {} type.", getName(), dbr.getType());
			}
		}
	}

	private void notifyIObservers(String profile) {
		notifyIObservers(this, profile);
	}

	/**
	 * The start call back handler
	 */
	public class StartCallbackListener implements PutListener {

		@Override
		public synchronized void putCompleted(PutEvent ev) {
			logger.debug("{}: Start caputCallback complete ", getName());
			if (ev.getStatus() != CAStatus.NORMAL) {
				logger.error("Put failed. Channel {} : Status {}", ((Channel) ev.getSource()).getName(), ev.getStatus());
			}
			// always update current state
			try {
				if ((currentstate = getCurrentState()) == EpicsCVScanState.Done) {
					busy = false;
					jythonScanStatus.setStatus(ScanStatus.COMPLETED_OKAY);
				}
			} catch (TimeoutException e) {
				logger.error("Timeout on getting current state from " + currentstatechannel.getName(), e);
			} catch (CAException e) {
				logger.error("CAException on getting current state from " + currentstatechannel.getName(), e);
			} catch (InterruptedException e) {
				logger.error("InterruptedException  on getting current state from " + currentstatechannel.getName(), e);
			}
		}
	}

	public class PauseCallbackListener implements PutListener {

		@Override
		public synchronized void putCompleted(PutEvent ev) {
			logger.debug("{}: Pause caputCallback complete ", getName());
			if (ev.getStatus() != CAStatus.NORMAL) {
				logger.error("Put failed. Channel {} : Status {}", ((Channel) ev.getSource()).getName(), ev.getStatus());
			}
			try {
				busy = true;
				currentstate = getCurrentState();
			} catch (TimeoutException e) {
				logger.error("Timeout on getting current state from " + currentstatechannel.getName(), e);
			} catch (CAException e) {
				logger.error("CAException on getting current state from " + currentstatechannel.getName(), e);
			} catch (InterruptedException e) {
				logger.error("InterruptedException on getting current state from " + currentstatechannel.getName(), e);
			}
		}
	}

	public class AbortCallbackListener implements PutListener {

		@Override
		public synchronized void putCompleted(PutEvent ev) {
			logger.debug("{}: Abort caputCallback complete ", getName());
			if (ev.getStatus() != CAStatus.NORMAL) {
				logger.error("Put failed. Channel {} : Status {}", ((Channel) ev.getSource()).getName(), ev.getStatus());
			}
			try {
				busy = false;
				jythonScanStatus.setStatus(ScanStatus.COMPLETED_OKAY);
				currentstate = getCurrentState();
			} catch (TimeoutException e) {
				logger.error("Timeout on getting current state from " + currentstatechannel.getName(), e);
			} catch (CAException e) {
				logger.error("CAException on getting current state from " + currentstatechannel.getName(), e);
			} catch (InterruptedException e) {
				logger.error("InterruptedException on getting current state from " + currentstatechannel.getName(), e);
			}
		}

	}

}
