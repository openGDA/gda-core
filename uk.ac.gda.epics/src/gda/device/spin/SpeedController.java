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

package gda.device.spin;

import gda.configuration.epics.ConfigurationNotFoundException;
import gda.configuration.epics.Configurator;
import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.device.robot.NextSamplePosition;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.epics.interfaces.SimplePvType;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gov.aps.jca.Channel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SpeedController Class
 */
@Deprecated
public class SpeedController extends DeviceBase implements Configurable, Findable, InitializationListener {
	
	private static final Logger logger = LoggerFactory.getLogger(NextSamplePosition.class);

	// control fields
	/**
	 * sample position demanding channel
	 */
	private Channel speedChannel;

	/**
	 * EPICS controller for CA methods
	 */
	private EpicsController controller;

	/**
	 * EPICS Channel Manager
	 */
	private EpicsChannelManager channelManager;

	/**
	 * phase II interface GDA-EPICS link parameter
	 */
	private String deviceName;

	/**
	 * Constructor
	 */
	public SpeedController() {
		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager(this);
	}

	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			if (getDeviceName() != null) {
				// phase II beamlines interface using GDA's deviceName.
				SimplePvType config;
				try {
					config = Configurator.getConfiguration(getDeviceName(), gda.epics.interfaces.SimplePvType.class);

					createChannelAccess(config);
					channelManager.tryInitialize(100);
				} catch (ConfigurationNotFoundException e) {
					logger.error("Can NOT find EPICS configuration for sample spin " + getDeviceName(), e);
					throw new FactoryException("Epics sample spin " + getDeviceName() + " not found");
				}
			} // Nothing specified in Server XML file
			else {
				logger.error("Missing EPICS configuration for sample spin {}", getName());
				throw new FactoryException("Missing EPICS configuration for sample spin " + getName());
			}
			configured = true;
		}
	}

	/**
	 * creates all required channels
	 * 
	 * @param config
	 * @throws FactoryException
	 */
	private void createChannelAccess(SimplePvType config) throws FactoryException {
		try {
			speedChannel = channelManager.createChannel(config.getRECORD().getPv(), false);
			// acknowledge that creation phase is completed
			channelManager.creationPhaseCompleted();
		} catch (Throwable th) {
			throw new FactoryException("failed to create all channels", th);
		}
	}

	/**
	 * @param value
	 * @throws DeviceException
	 */
	public void setSpeed(double value) throws DeviceException {
		try {
			controller.caput(speedChannel, value, 5);
		} catch (Throwable e) {
			throw new DeviceException("set speed failed. ", e);
		}
	}

	/**
	 * @return speed
	 * @throws DeviceException
	 */
	public double getSpeed() throws DeviceException {
		try {
			return controller.cagetDouble(speedChannel);
		} catch (Throwable e) {
			throw new DeviceException("failed to get spin status ", e);
		}
	}

	@Override
	public void initializationCompleted() {
		logger.info("{} is initialised.", getName());
	}

	/**
	 * @return device name
	 */
	public String getDeviceName() {
		return deviceName;
	}

	/**
	 * @param deviceName
	 */
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

}
