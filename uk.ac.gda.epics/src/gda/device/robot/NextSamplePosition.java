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

/**
 * NextSamplePosition Class
 */
public class NextSamplePosition extends DeviceBase implements InitializationListener {

	private static final Logger logger = LoggerFactory.getLogger(NextSamplePosition.class);

	// control fields
	/**
	 * sample position demanding channel
	 */
	private Channel sampleNumberChannel;
	/**
	 * EPICS controller for CA methods
	 */
	private EpicsController controller;
	/**
	 * EPICS Channel Manager
	 */
	private EpicsChannelManager channelManager;

	private String pvName;

	/**
	 * Constructor
	 */
	public NextSamplePosition() {
		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager(this);
	}

	@Override
	public void configure() throws FactoryException {
		if (!isConfigured()) {
			if (getPvName() == null) { // Nothing specified in Server XML file
				logger.error("Missing PV for Carousel sample position {}", getName());
				throw new FactoryException("Missing PV for Carousel sample position " + getName());
			}
			createChannelAccess(getPvName());
			channelManager.tryInitialize(100);
			setConfigured(true);
		}
	}

	/**
	 * creates all required channels
	 *
	 * @param config
	 * @throws FactoryException
	 */
	private void createChannelAccess(String pv) throws FactoryException {
		try {
			sampleNumberChannel = channelManager.createChannel(pv, false);
			// acknowledge that creation phase is completed
			channelManager.creationPhaseCompleted();
		} catch (Throwable th) {
			throw new FactoryException("failed to create all channels", th);
		}
	}

	/**
	 * gets the demanding sample position number
	 *
	 * @return sample position
	 * @throws DeviceException
	 */
	public double getSamplePosition() throws DeviceException {
		try {
			return controller.cagetDouble(sampleNumberChannel);
		} catch (Throwable e) {
			throw new DeviceException("Failed to get robot's sample position number. ", e);
		}
	}

	/**
	 * set the demanding sample position number
	 *
	 * @param pos
	 * @throws DeviceException
	 */
	public void setSamplePosition(double pos) throws DeviceException {
		try {
			controller.caput(sampleNumberChannel, pos, 60.0);
		} catch (Throwable e) {
			throw new DeviceException("failed to put sample position number", e);
		}
	}

	@Override
	public void initializationCompleted() {

		logger.info("Carousel Sample position number is initialised.");

	}

	public String getPvName() {
		return pvName;
	}

	public void setPvName(String pvName) {
		this.pvName = pvName;
	}

}
