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

import gda.analysis.Plotter;
import gda.configuration.properties.LocalProperties;
import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.jython.InterfaceProvider;
import gov.aps.jca.CAException;
import gov.aps.jca.CAStatus;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.dbr.DBR_Int;
import gov.aps.jca.dbr.DBR_String;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import gov.aps.jca.event.PutEvent;
import gov.aps.jca.event.PutListener;

import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.SDAPlotter;

public class EpicsCVScanController extends DeviceBase implements InitializationListener, Configurable, Findable {

	private static final Logger logger = LoggerFactory.getLogger(EpicsCVScanController.class);
	/* base class declared name private, so need to re-declare here */
	@SuppressWarnings("unused")
	private String name = null;
	private String pv_root = null;
	private String plotPanelName = null;

	/* cached data - volatile as they are updated potentially by multiple threads */
	private volatile CurrentState currentstate = CurrentState.Done;
	private volatile String message = null;
	private volatile int numberofpulsedone;
	private volatile int totalnumberofpulse;
	private volatile String profile;
	private volatile boolean busy = false;

	private StateListener statel;
	private MessageListener messagel;
	private PulseDoneListener pdl;
	private PulseTotalListener ptl;
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
	private Channel statusmessagechannel;
	private Channel pulsesnumberdonechannel;
	private Channel puslestotalnumberchannel;
	private volatile boolean GDAScanning = false;
	private String legend = null;

	public boolean isGDAScanning() {
		return GDAScanning;
	}

	public void setGDAScanning(boolean gDAScanning) {
		GDAScanning = gDAScanning;
	}

	public enum CurrentState {
		Done, Aborted, Executing, Flyback, Paused, Fault, Reduction, LVIO
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
	private Channel mac1x;
	private Channel mac2x;
	private Channel mac3x;
	private Channel mac4x;
	private Channel mac5x;
	private Channel mac1y;
	private Channel mac2y;
	private Channel mac3y;
	private Channel mac4y;
	private Channel mac5y;
	// private Channel mac1m;
	// private Channel mac2m;
	// private Channel mac3m;
	// private Channel mac4m;
	// private Channel mac5m;
	private Channel allx;
	private Channel ally;
	private Channel allye;
	private Channel mav;
	@SuppressWarnings("unused")
	private boolean local;
	private long collectionNumber;
	private boolean live = true;
	private long fileNumber;

	public long getFileNumber() {
		return fileNumber;
	}

	public void setFileNumber(long fileNumber) {
		this.fileNumber = fileNumber;

	}

	public boolean isLive() {
		return live;
	}

	public void setLive(boolean live) {
		this.live = live;
	}

	public long getCollectionNumber() {
		return collectionNumber;
	}

	public void setCollectionNumber(long collectionNumber) {
		this.collectionNumber = collectionNumber;
	}

	public EpicsCVScanController() {
		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager(this);
		statel = new StateListener();
		messagel = new MessageListener();
		startcallbacklistener = new StartCallbackListener();
		pausecallbacklistener = new PauseCallbackListener();
		abortcallbacklistener = new AbortCallbackListener();
		pdl = new PulseDoneListener();
		ptl = new PulseTotalListener();
		pfl = new ProfileListener();
	}

	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			if (getPv_root() != null) {
				createChannelAccess(pv_root);
				channelManager.tryInitialize(100);
			} else {
				logger.error("Missing EPICS configuration for {}", getName());
				throw new FactoryException("Missing EPICS interface configuration for " + getName());
			}
			if (getPlotPanelName() == null) {
				throw new FactoryException("Missing Plot Panel Name configuration for " + getName());
			}
			configured = true;
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
			statusmessagechannel = channelManager.createChannel(pvRoot + ":MESSAGE", messagel, false);
			pulsesnumberdonechannel = channelManager.createChannel(pvRoot + ":GPULSES", pdl, false);
			puslestotalnumberchannel = channelManager.createChannel(pvRoot + ":NPULSES", ptl, false);
			// Raw 2theta positions where scaler was triggered
			rawx = channelManager.createChannel(pvRoot + ":RAWX", false);
			// X scale of each MAC arm,
			mac1x = channelManager.createChannel(pvRoot + ":MAC1X", false);
			mac2x = channelManager.createChannel(pvRoot + ":MAC2X", false);
			mac3x = channelManager.createChannel(pvRoot + ":MAC3X", false);
			mac4x = channelManager.createChannel(pvRoot + ":MAC4X", false);
			mac5x = channelManager.createChannel(pvRoot + ":MAC5X", false);
			// Y Counts on each MAC arm
			mac1y = channelManager.createChannel(pvRoot + ":MAC1Y", false);
			mac2y = channelManager.createChannel(pvRoot + ":MAC2Y", false);
			mac3y = channelManager.createChannel(pvRoot + ":MAC3Y", false);
			mac4y = channelManager.createChannel(pvRoot + ":MAC4Y", false);
			mac5y = channelManager.createChannel(pvRoot + ":MAC5Y", false);
			// Monitor sum Ie+Io on each MAC arm
			// mac1m = channelManager.createChannel(pvRoot + ":MAC1M", false);
			// mac2m = channelManager.createChannel(pvRoot + ":MAC2M", false);
			// mac3m = channelManager.createChannel(pvRoot + ":MAC3M", false);
			// mac4m = channelManager.createChannel(pvRoot + ":MAC4M", false);
			// mac5m = channelManager.createChannel(pvRoot + ":MAC5M", false);
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

	public double getMonitorAvaerage() throws TimeoutException, CAException, InterruptedException {
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
	 * the following modes: Done, Aborted, or Paused. This is done to ensure the EPICS "Start" is never being called
	 * more than once during a constant velocity scan.
	 * 
	 * @throws CAException
	 * @throws InterruptedException 
	 */
	public void start() throws CAException, InterruptedException {
		if (currentstate == CurrentState.Done || currentstate == CurrentState.Aborted
				|| currentstate == CurrentState.Paused || currentstate == CurrentState.Fault) {
			busy = true;
			controller.caput(start, 1, startcallbacklistener);
			logger.info("{}: Start CV scan", getName());
		} else {
			InterfaceProvider.getTerminalPrinter().print(
					"EPICS CVScan is busy, its current state is " + currentstate.toString());
			logger.warn("EPICS CVScan is busy. its current state is {}", currentstate.toString());
			legend = null;
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
	public CurrentState getCurrentState() throws TimeoutException, CAException, InterruptedException {
		short value = controller.cagetEnum(currentstatechannel);
		if (value == 0) {
			currentstate = CurrentState.Done;
		} else if (value == 1) {
			currentstate = CurrentState.Aborted;
		} else if (value == 2) {
			currentstate = CurrentState.Executing;
		} else if (value == 3) {
			currentstate = CurrentState.Flyback;
		} else if (value == 4) {
			currentstate = CurrentState.Paused;
		} else if (value == 5) {
			currentstate = CurrentState.Fault;
		} else if (value == 6) {
			currentstate = CurrentState.Reduction;
		} else if (value == 7) {
			currentstate = CurrentState.LVIO;
		} else {
			logger.error("{} reports UNKNOWN state value: {}", getName(), value);
			throw new IllegalStateException(getName() + " in a unknown state.");
		}

		return currentstate;
	}

	/**
	 * pulls the status message from EPICS and update cached {@link #message} value
	 * 
	 * @return the status message as String
	 * @throws TimeoutException
	 * @throws CAException
	 * @throws InterruptedException 
	 */
	public String getStatusMessage() throws TimeoutException, CAException, InterruptedException {
		return message = controller.cagetString(statusmessagechannel);
	}

	/**
	 * pulls the number of pulse done from EPICS and update cached {@link #numberofpulsedone} value
	 * 
	 * @return the status message as String
	 * @throws TimeoutException
	 * @throws CAException
	 * @throws InterruptedException 
	 */
	public int getNumberOfPulsesDone() throws TimeoutException, CAException, InterruptedException {
		return numberofpulsedone = controller.cagetInt(pulsesnumberdonechannel);
	}

	/**
	 * pulls the total number of pulse from EPICS and update cached {@link #totalnumberofpulse} value
	 * 
	 * @return the status message as String
	 * @throws TimeoutException
	 * @throws CAException
	 * @throws InterruptedException 
	 */
	public int getTotalNumberPulses() throws TimeoutException, CAException, InterruptedException {
		return totalnumberofpulse = controller.cagetInt(puslestotalnumberchannel);
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

	public double[] getMAC1X() throws TimeoutException, CAException, InterruptedException {
		return controller.cagetDoubleArray(mac1x);
	}

	public int getMAC1XSize() {
		return mac1x.getElementCount();
	}

	public double[] getMAC2X() throws TimeoutException, CAException, InterruptedException {
		return controller.cagetDoubleArray(mac2x);
	}

	public int getMAC2XSize() {
		return mac3x.getElementCount();
	}

	public double[] getMAC3X() throws TimeoutException, CAException, InterruptedException {
		return controller.cagetDoubleArray(mac3x);
	}

	public int getMAC3XSize() {
		return mac3x.getElementCount();
	}

	public double[] getMAC4X() throws TimeoutException, CAException, InterruptedException {
		return controller.cagetDoubleArray(mac4x);
	}

	public int getMAC4XSize() {
		return mac4x.getElementCount();
	}

	public double[] getMAC5X() throws TimeoutException, CAException, InterruptedException {
		return controller.cagetDoubleArray(mac5x);
	}

	public int getMAC5XSize() {
		return mac5x.getElementCount();
	}

	public double[] getMAC1Y() throws TimeoutException, CAException, InterruptedException {
		return controller.cagetDoubleArray(mac1y);
	}

	public int getMAC1YSize() {
		return mac1y.getElementCount();
	}

	public double[] getMAC2Y() throws TimeoutException, CAException, InterruptedException {
		return controller.cagetDoubleArray(mac2y);
	}

	public int getMAC2YSize() {
		return mac3y.getElementCount();
	}

	public double[] getMAC3Y() throws TimeoutException, CAException, InterruptedException {
		return controller.cagetDoubleArray(mac3y);
	}

	public int getMAC3YSize() {
		return mac3y.getElementCount();
	}

	public double[] getMAC4Y() throws TimeoutException, CAException, InterruptedException {
		return controller.cagetDoubleArray(mac4y);
	}

	public int getMAC4YSize() {
		return mac4y.getElementCount();
	}

	public double[] getMAC5Y() throws TimeoutException, CAException, InterruptedException {
		return controller.cagetDoubleArray(mac5y);
	}

	public int getMAC5YSize() {
		return mac5y.getElementCount();
	}

	// public double[] getMAC1M() throws TimeoutException, CAException {
	// return controller.cagetDoubleArray(mac1m);
	// }
	//
	// public int getMAC1MSize() {
	// return mac1m.getElementCount();
	// }
	//
	// public double[] getMAC2M() throws TimeoutException, CAException {
	// return controller.cagetDoubleArray(mac2m);
	// }
	//
	// public int getMAC2MSize() {
	// return mac3m.getElementCount();
	// }
	//
	// public double[] getMAC3M() throws TimeoutException, CAException {
	// return controller.cagetDoubleArray(mac3m);
	// }
	//
	// public int getMAC3MSize() {
	// return mac3m.getElementCount();
	// }
	//
	// public double[] getMAC4M() throws TimeoutException, CAException {
	// return controller.cagetDoubleArray(mac4m);
	// }
	//
	// public int getMAC4MSize() {
	// return mac4m.getElementCount();
	// }
	//
	// public double[] getMAC5M() throws TimeoutException, CAException {
	// return controller.cagetDoubleArray(mac5m);
	// }
	//
	// public int getMAC5MSize() {
	// return mac5m.getElementCount();
	// }

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
	public CurrentState getState() {
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

	public String getPlotPanelName() {
		return plotPanelName;
	}

	public void setPlotPanelName(String plotPanelName) {
		this.plotPanelName = plotPanelName;
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
		// initialse Profile choices
		String[] profiles = getProfiles();
		for (int i = 0; i < profiles.length; i++) {
			if (profiles[i] != null || profiles[i] != "") {
				this.profiles.add(profiles[i]);
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
		try {
			this.message = getStatusMessage();
		} catch (TimeoutException e) {
			logger.error("Timeout on initialising Status Message of " + getName(), e);
		} catch (CAException e) {
			logger.error("CAException on initialising Status Message of " + getName(), e);
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
				currentstate = CurrentState.Done;
				busy = false;
			} else if (value == 1) {
				currentstate = CurrentState.Aborted;
				busy = false;
			} else if (value == 2) {
				currentstate = CurrentState.Executing;
				busy = true;
			} else if (value == 3) {
				currentstate = CurrentState.Flyback;
				busy = true;
			} else if (value == 4) {
				currentstate = CurrentState.Paused;
				busy = true;
			} else if (value == 5) {
				currentstate = CurrentState.Fault;
				legend = null;
				busy = true; // GDA must be busy as auto retry kicked in on fault, otherwise for-loop carrys on
			} else if (value == 6) {
				currentstate = CurrentState.Reduction;
				busy = true;
			} else if (value == 7) {
				currentstate = CurrentState.LVIO;
				busy = true;
			} else {
				logger.error("{} reports UNKNOWN state value: {}", getName(), value);
				throw new IllegalStateException(getName() + " in a unknown state.");
			}
			logger.info("Monitor update from EPICS {}: Current State: {}", ((Channel) arg0.getSource()).getName(),
					currentstate);

			notifyIObservers(currentstate);
		}
	}

	private void notifyIObservers(CurrentState currentstate) {
		notifyIObservers(this, currentstate);
	}

	/**
	 * Monitor Status Message in EPICS, and update cached message variable.
	 */
	private class MessageListener implements MonitorListener {
		boolean first=true;
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			if (first) {
				first=false;
				return;
			}
			DBR dbr = arg0.getDBR();
			if (dbr.isSTRING()) {
				message = ((DBR_String) dbr).getStringValue()[0];
				if (InterfaceProvider.getTerminalPrinter() != null) {
					InterfaceProvider.getTerminalPrinter().print(getName() +": " + message);
				}
			} else {
				logger.error("{} : MessageListener expect String type but got {} type.", getName(), dbr.getType());
			}
		}
	}

	/**
	 * Monitor Number of pulses done in EPICS, and update cached numberofpulsedone variable. This state variable can be
	 * used to display progress bar on Client. It also updates plot of stage-based rebinned data by pulling them from
	 * EPICS CVScan.
	 */
	private class PulseDoneListener implements MonitorListener {
		double[] x = null;
		double[] y = null;
		String filePrefix = null;
		String fileSuffix = null;
		boolean first=true;
		
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			if (first) {
				first=false;
				return;
			}
			DBR dbr = arg0.getDBR();
			boolean live=false;
			if (InterfaceProvider.getJythonNamespace() != null) {
				live = (Boolean) InterfaceProvider.getJythonNamespace().getFromJythonNamespace("LIVE");
			}
			if (!live) {
				return;
			}
			if (dbr.isINT()) {
				numberofpulsedone = ((DBR_Int) dbr).getIntValue()[0];
				if (numberofpulsedone == 0) { // if current state=Fault, restart do not send Pulse processed = 0 event
					// logger.info("get 2theta data");
					x = get2Theta();
					filePrefix = LocalProperties.get("gda.data.file.prefix", "");
					fileSuffix = LocalProperties.get("gda.data.file.suffix", "-mac");
					legend = filePrefix + getFileNumber() + fileSuffix + "-" + String.format("%03d", collectionNumber);
					// logger.info("2theta size {}", x.length);
				} else {
					// logger.info("get counts data");
					y = getCount();
					// logger.info("Count size {}", y.length);
				}
				if (x != null && y != null) {
					DoubleDataset yds = new DoubleDataset(y);
					if (legend == null) {
						filePrefix = LocalProperties.get("gda.data.file.prefix", "");
						fileSuffix = LocalProperties.get("gda.data.file.suffix", "-mac");
						legend = filePrefix + getFileNumber() + fileSuffix + "-"
								+ String.format("%03d", collectionNumber);
					}
					yds.setName(legend);
					try {
						SDAPlotter.plot(getPlotPanelName(), new DoubleDataset(x), yds);
					} catch (Exception e) {
						logger.error("MAC detector data live plot failed.", e);
					}
					Plotter.plot(getPlotPanelName(), new DoubleDataset(x), yds);
				}
			} else {
				logger.error("{} : PulseDoneListener expect Integer type but got {} type.", getName(), dbr.getType());
			}
		}
	}

	private double[] get2Theta() {
		double[] x1 = ArrayUtils.EMPTY_DOUBLE_ARRAY;
		try {
			// logger.info("gets MAC1X");
			x1 = getMAC1X();
			// logger.info("gets MAC1X DONE");
		} catch (TimeoutException e) {
			logger.error("Timeout while gets MAC1X", e);
			e.printStackTrace();
		} catch (CAException e) {
			logger.error("CAException while gets MAC1X", e);
			e.printStackTrace();
		} catch (InterruptedException e) {
			logger.error("InterruptedException while gets MAC1X", e);
		}
		double[] x2 = ArrayUtils.EMPTY_DOUBLE_ARRAY;
		try {
			// logger.info("gets MAC2X");
			x2 = getMAC2X();
			// logger.info("gets MAC2X DONE");
		} catch (TimeoutException e) {
			logger.error("Timeout while gets MAC2X", e);
			e.printStackTrace();
		} catch (CAException e) {
			logger.error("CAException while gets MAC2X", e);
			e.printStackTrace();
		} catch (InterruptedException e) {
			logger.error("InterruptedException while gets MAC2X", e);
		}
		double[] x3 = ArrayUtils.EMPTY_DOUBLE_ARRAY;
		try {
			// logger.info("gets MAC3X");
			x3 = getMAC3X();
			// logger.info("gets MAC3X DONE");
		} catch (TimeoutException e) {
			logger.error("Timeout while gets MAC3X", e);
			e.printStackTrace();
		} catch (CAException e) {
			logger.error("CAException while gets MAC3X", e);
			e.printStackTrace();
		} catch (InterruptedException e) {
			logger.error("InterruptedException while gets MAC3X", e);
		}
		double[] x4 = ArrayUtils.EMPTY_DOUBLE_ARRAY;
		try {
			// logger.info("gets MAC4X");
			x4 = getMAC4X();
			// logger.info("gets MAC4X DONE");
		} catch (TimeoutException e) {
			logger.error("Timeout while gets MAC4X", e);
			e.printStackTrace();
		} catch (CAException e) {
			logger.error("CAException while gets MAC4X", e);
			e.printStackTrace();
		} catch (InterruptedException e) {
			logger.error("InterruptedException while gets MAC4X", e);
		}
		double[] x5 = ArrayUtils.EMPTY_DOUBLE_ARRAY;
		try {
			// logger.info("gets MAC5X");
			x5 = getMAC5X();
			// logger.info("gets MAC5X DONE");
		} catch (TimeoutException e) {
			logger.error("Timeout while gets MAC5X", e);
			e.printStackTrace();
		} catch (CAException e) {
			logger.error("CAException while gets MAC5X", e);
			e.printStackTrace();
		} catch (InterruptedException e) {
			logger.error("InterruptedException while gets MAC5X", e);
		}
		return ArrayUtils.subarray(ArrayUtils.addAll(ArrayUtils.addAll(
				ArrayUtils.addAll(ArrayUtils.addAll(x1, x2), x3), x4), x5), 16500, 305000);
	}

	// Y1,Y2,Y3,Y4,Y5 only temperarily available in EPICS so must be get first before processing them.
	private double[] getCount() {
		double[] y1 = ArrayUtils.EMPTY_DOUBLE_ARRAY;
		try {
			// logger.info("gets MAC1Y");
			y1 = getMAC1Y();
			// logger.info("gets MAC1Y DONE");
		} catch (TimeoutException e) {
			logger.error("Timeout while gets MAC1Y", e);
			e.printStackTrace();
		} catch (CAException e) {
			logger.error("CAException while gets MAC1Y", e);
			e.printStackTrace();
		} catch (InterruptedException e) {
			logger.error("InterruptedException while gets MAC1Y", e);
		}
		double[] y2 = ArrayUtils.EMPTY_DOUBLE_ARRAY;
		try {
			// logger.info("gets MAC2Y");
			y2 = getMAC2Y();
			// logger.info("gets MAC2Y DONE");
		} catch (TimeoutException e) {
			logger.error("Timeout while gets MAC2Y", e);
			e.printStackTrace();
		} catch (CAException e) {
			logger.error("CAException while gets MAC2Y", e);
			e.printStackTrace();
		} catch (InterruptedException e) {
			logger.error("InterruptedException while gets MAC2Y", e);
		}
		double[] y3 = ArrayUtils.EMPTY_DOUBLE_ARRAY;
		try {
			// logger.info("gets MAC3Y");
			y3 = getMAC3Y();
			// logger.info("gets MAC3Y DONE");
		} catch (TimeoutException e) {
			logger.error("Timeout while gets MAC3Y", e);
			e.printStackTrace();
		} catch (CAException e) {
			logger.error("CAException while gets MAC3Y", e);
			e.printStackTrace();
		} catch (InterruptedException e) {
			logger.error("InterruptedException while gets MAC3Y", e);
		}
		double[] y4 = ArrayUtils.EMPTY_DOUBLE_ARRAY;
		try {
			// logger.info("gets MAC4Y");
			y4 = getMAC4Y();
			// logger.info("gets MAC4Y DONE");
		} catch (TimeoutException e) {
			logger.error("Timeout while gets MAC4Y", e);
			e.printStackTrace();
		} catch (CAException e) {
			logger.error("CAException while gets MAC4Y", e);
			e.printStackTrace();
		} catch (InterruptedException e) {
			logger.error("InterruptedException while gets MAC4Y", e);
		}
		double[] y5 = ArrayUtils.EMPTY_DOUBLE_ARRAY;
		try {
			// logger.info("gets MAC5Y");
			y5 = getMAC5Y();
			// logger.info("gets MAC5Y DONE");
		} catch (TimeoutException e) {
			logger.error("Timeout while gets MAC5Y", e);
			e.printStackTrace();
		} catch (CAException e) {
			logger.error("CAException while gets MAC5Y", e);
			e.printStackTrace();
		} catch (InterruptedException e) {
			logger.error("InterruptedException while gets MAC5Y", e);
		}
		return ArrayUtils.subarray(ArrayUtils.addAll(ArrayUtils.addAll(
				ArrayUtils.addAll(ArrayUtils.addAll(y1, y2), y3), y4), y5), 16500, 305000);
	}

	/**
	 * Monitor the total number of pulses in EPICS Constant Velocity scan, and update cached total number of pulse
	 * variable. This state variable can be used to display progress bar on Client.
	 */
	private class PulseTotalListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr.isINT()) {
				totalnumberofpulse = ((DBR_Int) dbr).getIntValue()[0];
			} else {
				logger.error("{} : PulseTotalListener expect Integer type but got {} type.", getName(), dbr.getType());
			}
		}
	}

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
				logger
						.error("Put failed. Channel {} : Status {}", ((Channel) ev.getSource()).getName(), ev
								.getStatus());
			}
			// always update current state
			try {
				if ((currentstate = getCurrentState()) == CurrentState.Done) {
					busy = false;
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
				logger
						.error("Put failed. Channel {} : Status {}", ((Channel) ev.getSource()).getName(), ev
								.getStatus());
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
				logger
						.error("Put failed. Channel {} : Status {}", ((Channel) ev.getSource()).getName(), ev
								.getStatus());
			}
			try {
				busy = false;
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