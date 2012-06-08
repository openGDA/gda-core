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
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.epics.interfaces.SimpleBinaryType;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EnablerController Class
 */
@Deprecated
public class EnablerController extends DeviceBase implements Configurable, Findable, InitializationListener {

	private static final Logger logger = LoggerFactory.getLogger(EnablerController.class);

	// control fields
	/**
	 * sample position demanding channel
	 */
	private Channel enableChannel;

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

	private Vector<String> positions = new Vector<String>();
	
	private StateMonitorListener sml;

	/**
	 * Constructor
	 */
	public EnablerController() {
		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager(this);
		sml = new StateMonitorListener();
	}

	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			if (getDeviceName() != null) {
				// phase II beamlines interface using GDA's deviceName.
				SimpleBinaryType config;
				try {
					config = Configurator.getConfiguration(getDeviceName(), gda.epics.interfaces.SimpleBinaryType.class);

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
	private void createChannelAccess(SimpleBinaryType config) throws FactoryException {
		try {
			enableChannel = channelManager.createChannel(config.getRECORD().getPv(), sml, false);
			// acknowledge that creation phase is completed
			channelManager.creationPhaseCompleted();
		} catch (Throwable th) {
			throw new FactoryException("failed to create all channels", th);
		}
	}

	/**
	 * @throws DeviceException
	 */
	public void enable() throws DeviceException {
		try {
			controller.caput(enableChannel, 1, 5);
		} catch (Throwable e) {
			throw new DeviceException("enable failed. ", e);
		}
	}

	/**
	 * @throws DeviceException
	 */
	public void disable() throws DeviceException {
		try {
			controller.caput(enableChannel, 0, 5);
		} catch (Throwable e) {
			throw new DeviceException("disable failed. ", e);
		}
	}

	/**
	 * @return String status
	 * @throws DeviceException
	 */
	public String getStatus() throws DeviceException {
		try {
			return positions.get(controller.cagetEnum(enableChannel));
		} catch (Throwable e) {
			throw new DeviceException("failed to get spin status ", e);
		}
	}

	/**
	 * gets the available positions from this device.
	 * 
	 * @return available positions
	 * @throws InterruptedException 
	 * @throws CAException 
	 * @throws TimeoutException 
	 */
	public String[] getPositions() throws TimeoutException, CAException, InterruptedException {
		return controller.cagetLabels(enableChannel);
	}

	@Override
	public void initializationCompleted() throws TimeoutException, CAException, InterruptedException  {
		String[] position = getPositions();
		for (int i = 0; i < position.length; i++) {
			if (position[i] != null || position[i] != "") {
				positions.add(position[i]);
			}
		}
		logger.info("{} is initialised.", getName());
	}
	
	private class StateMonitorListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr.isENUM()) {
				short index = ((DBR_Enum) dbr).getEnumValue()[0];
				String position=positions.get(index);
				logger.warn("Position of {}: {}", getName(), position);
				notifyIObservers(position);
			} else {
				logger.error("{} : ProfileListener expect Enum type but got {} type.", getName(), dbr.getType());
			}
		}
		
	}

	public void notifyIObservers(String position) {
		logger.warn("{}: Notify observers of position changed to {}", getName(), position);
		this.notifyIObservers(this, position);
		logger.warn("{}: notify sent.", getName());
	}
	/**
	 * @return deviceName
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
