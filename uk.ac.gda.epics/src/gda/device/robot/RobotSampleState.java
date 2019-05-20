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
 * RobotSampleState Class
 */
public class RobotSampleState extends DeviceBase implements InitializationListener {

	private static final Logger logger = LoggerFactory.getLogger(RobotSampleState.class);

	// control fields
	/**
	 * robot's sample state 0 - on carousel, 1 - on gripper, 2 - on diffractometer
	 */
	private Channel sampleStateChannel;

	/**
	 * EPICS controller for CA methods
	 */
	private EpicsController controller;

	/**
	 * EPICS Channel Manager
	 */
	private EpicsChannelManager channelManager;

	private SampleStateListener sstatels;

	private SampleState sstate;

	private String pvName;

	/**
	 * Constructor
	 */
	public RobotSampleState() {
		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager(this);
		sstatels = new SampleStateListener();
	}

	@Override
	public void configure() throws FactoryException {
		if (!isConfigured()) {
			if (getPvName() == null) { // Nothing specified in Server XML file
				logger.error("Missing PV for Robot State {}", getName());
				throw new FactoryException("Missing PV for Robot State " + getName());
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
	 * @throws FactoryException
	 */
	private void createChannelAccess(String pv) throws FactoryException {
		try {
			sampleStateChannel = channelManager.createChannel(pv, sstatels, false);
			// acknowledge that creation phase is completed
			channelManager.creationPhaseCompleted();
		} catch (Throwable th) {
			throw new FactoryException("failed to create all channels", th);
		}
	}

	/**
	 * gets the raw value from sample state from Robot.
	 *
	 * @return the raw value from sample state from Robot
	 * @throws DeviceException
	 */
	public int getRobotSampleState() throws DeviceException {
		try {
			return controller.cagetInt(sampleStateChannel);
		} catch (Throwable e) {
			throw new DeviceException("Failed to get robot's sample state. ", e);
		}
	}

	/**
	 * gets the state of sample for the robot - where is the sample?
	 *
	 * @return the state of sample for the robot - where is the sample
	 * @throws DeviceException
	 */
	public SampleState getSampleState() throws DeviceException {
		int state = getRobotSampleState();

		if (state == 0) {
			return SampleState.CAROUSEL;
		} else if (state == 1) {
			return SampleState.INJAWS;
		} else if (state == 2) {
			return SampleState.DIFF;
		} else {
			logger.error("Robot sample state is UNKNOWN. Please visually inspect and report this to Engineer.");
			return SampleState.UNKNOWN;
		}

	}

	@Override
	public void initializationCompleted() {

		logger.info("Robot Sample state is initialised.");

	}

	/**
	 *
	 */
	public class SampleStateListener implements MonitorListener {

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
				sstate = SampleState.CAROUSEL;
			} else if (s == 1.0) {
				sstate = SampleState.INJAWS;
			} else if (s == 2.0) {
				sstate = SampleState.DIFF;
			} else {
				logger.error("Expecting 0.0, 1.0, or 2.0 from EPICS but got {} ", s);
				throw new IllegalStateException("Sample State returns wrong value : " + s);
			}
			notifyIObservers(this, sstate);

		}

	}

	public String getPvName() {
		return pvName;
	}

	public void setPvName(String pvName) {
		this.pvName = pvName;
	}

}
