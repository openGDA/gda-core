/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.device.robot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.factory.FactoryException;
import gov.aps.jca.Channel;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

/**
 * Class for the experimental hutch door latch state, used by robot to decide when safety needs to be checked.
 */
public class DoorLatchState extends DeviceBase implements InitializationListener {

	private static final Logger logger = LoggerFactory.getLogger(DoorLatchState.class);

	// control fields
	/**
	 * Door latch state channel
	 */
	private Channel needRecoverChannel;

	/**
	 * EPICS controller for CA methods
	 */
	private EpicsController controller;

	/**
	 * EPICS Channel Manager
	 */
	private EpicsChannelManager channelManager;

	private DoorLatchStateListener dlsl;

	private volatile boolean hasOpened=false;

	private String pvName;

	/**
	 * Constructor
	 */
	public DoorLatchState() {
		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager(this);
		dlsl = new DoorLatchStateListener();
	}

	@Override
	public void configure() throws FactoryException {
		if (!isConfigured()) {
			if (getPvName() == null) { // Nothing specified in Server XML file
				logger.error("Missing PV for Experiment Door Latch State {}", getName());
				throw new FactoryException("Missing PV for Experiment Door Latch State " + getName());
			}
			createChannelAccess(getPvName());
			channelManager.tryInitialize(100);
			setConfigured(true);
		}
	}

	/**
	 * creates all required channels
	 *
	 * @param pv
	 * @throws FactoryException if channel cannot be created
	 */
	private void createChannelAccess(String pv) throws FactoryException {
		try {
			needRecoverChannel = channelManager.createChannel(pv, dlsl, false);
			// acknowledge that creation phase is completed
			channelManager.creationPhaseCompleted();
		} catch (Throwable th) {
			throw new FactoryException("failed to create channel "+ pv, th);
		}
	}

	/**
	 * gets the raw value from sample state from Robot.
	 *
	 * @return the raw value from sample state from Robot
	 * @throws DeviceException
	 */
	public int getState() throws DeviceException {
		try {
			return controller.cagetInt(needRecoverChannel);
		} catch (Throwable e) {
			throw new DeviceException("Failed to get experiment hutch door state. ", e);
		}
	}

	public void resetDoorLatch() throws DeviceException {
		try {
			controller.caput(needRecoverChannel,0.0);
		} catch (Exception e) {
			throw new DeviceException("Failed to reset experiment hutch door state ", e);
		}
	}
	@Override
	public void initializationCompleted() {
		if (this.hasOpened) {
			logger.warn("Experiment hatch door has been opened more than once since last robot run.");
		}
		logger.info("Door latch state is initialised.");
	}

	/**
	 *
	 */
	public class DoorLatchStateListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR state = arg0.getDBR();
			double s = -1.0;
			if (state.isDOUBLE()) {
				s = ((DBR_Double) state).getDoubleValue()[0];
			} else {
				logger.error("Expecting double from EPICS but got {} ", state.getType());
				throw new IllegalStateException("Sample State returns wrong value type" + state.getType());
			}
			if (s == 0.0) {
				hasOpened=false;
			} else if (s == 1.0) {
				hasOpened = true;
			} else {
				logger.error("Expecting 0.0, 1.0, from EPICS but got {} ", s);
				throw new IllegalStateException("Door Latch State returns wrong value : " + s);
			}
			notifyIObservers(this, hasOpened);

		}

	}

	public String getPvName() {
		return pvName;
	}

	public void setPvName(String pvName) {
		this.pvName = pvName;
	}

}
