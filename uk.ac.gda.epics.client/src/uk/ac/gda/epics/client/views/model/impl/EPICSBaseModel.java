/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.epics.client.views.model.impl;

import gda.configuration.epics.ConfigurationNotFoundException;
import gda.configuration.epics.Configurator;
import gda.device.DeviceException;
import gda.device.detector.areadetector.IPVProvider;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.epics.interfaces.NDPluginBaseType;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.event.MonitorListener;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;

/**
 * Base model class - model represents the data that is retrieved from EPICS to be represented on the client side UI.
 * This class provides the basic functionality for the rest of the models to inherit.
 *
 * @author rsr31645
 */
public abstract class EPICSBaseModel<T> implements InitializingBean, InitializationListener, Findable {
	protected final static EpicsController EPICS_CONTROLLER = EpicsController.getInstance();
	protected Map<String, Channel> channelMap;

	protected String deviceName;
	protected String pluginBase;
	protected String basePVName;
	protected IPVProvider pvProvider;
	protected String name;

	/**
	 * @return Returns the name.
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            The name to set.
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}

	protected T config;

	public EPICSBaseModel() {
		channelMap = new HashMap<String, Channel>();
	}

	@Override
	public void initializationCompleted() throws InterruptedException, DeviceException, TimeoutException, CAException {
		getLogger().info("{} is initialised.", getName());

	}

	/**
	 * @return Returns the deviceName.
	 */
	public String getDeviceName() {
		return deviceName;
	}

	/**
	 * @param deviceName
	 *            The deviceName to set.
	 * @throws FactoryException
	 */
	public void setDeviceName(String deviceName) throws FactoryException {
		this.deviceName = deviceName;
		initializeConfig();
	}

	/**
	 * The plugins composites the NDPluginBaseType, the device name is the same
	 *
	 * @return {@link NDPluginBaseType}
	 * @throws FactoryException
	 */
	protected NDPluginBaseType getPluginBaseTypeConfig() throws FactoryException {

		NDPluginBaseType pluginBaseTypeConfiguration = null;
		if (pluginBase != null) {
			try {
				pluginBaseTypeConfiguration = Configurator.getConfiguration(getPluginBase(), NDPluginBaseType.class);
			} catch (ConfigurationNotFoundException e) {
				getLogger().error("EPICS configuration for device {} not found", getPluginBase());
				throw new FactoryException("EPICS configuration for device " + getPluginBase() + " not found.", e);
			}
		}
		return pluginBaseTypeConfiguration;
	}

	private void initializeConfig() throws FactoryException {
		if (deviceName != null) {
			try {
				config = Configurator.getConfiguration(getDeviceName(), getConfigClassType());
			} catch (ConfigurationNotFoundException e) {
				getLogger().error("EPICS configuration for device {} not found", getDeviceName());
				throw new FactoryException("EPICS configuration for device " + getDeviceName() + " not found.", e);
			}
		}
	}

	public T getConfig() {
		return config;
	}

	protected abstract Class<T> getConfigClassType();

	protected Channel getChannel(String pvElementName, MonitorListener ml, String... args) throws Exception {
		try {
			String pvPostFix = null;
			if (args.length > 0) {
				// PV element name is different from the pvPostFix
				pvPostFix = args[0];
			} else {
				pvPostFix = pvElementName;
			}

			String fullPvName;
			if (pvProvider != null) {
				fullPvName = pvProvider.getPV(pvElementName);
			} else {
				fullPvName = getBasePVName() + pvPostFix;
			}
			return createChannel(fullPvName, ml);
		} catch (Exception exception) {
			getLogger().warn("Problem getting channel", exception);
			throw exception;
		}
	}

	public Channel createChannel(String fullPvName, MonitorListener ml) throws CAException, TimeoutException,
			InterruptedException {
		Channel channel;
		synchronized (channelMap) {
			channel = channelMap.get(fullPvName);
			if (channel == null) {
				try {
					channel = EPICS_CONTROLLER.createChannel(fullPvName);
					if (ml != null) {
						EPICS_CONTROLLER.setMonitor(channel, ml);
					}
					int i = 0;
					while (Channel.CONNECTED != channel.getConnectionState()) {
						Thread.sleep(50);
						if (i > 10) {
							break;
						}
						i++;
					}
				} catch (CAException cae) {
					getLogger().warn("Problem creating channel", cae);
					throw cae;
				}
				if (Channel.CONNECTED == channel.getConnectionState()) {
					channelMap.put(fullPvName, channel);
				}
			}
		}
		return channel;
	}

	public void setBasePVName(String basePVName) {
		this.basePVName = basePVName;
	}

	public String getBasePVName() {
		return basePVName;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (deviceName == null && getBasePVName() == null && pvProvider == null) {
			throw new IllegalArgumentException("'deviceName','basePVName' or 'pvProvider' needs to be declared");
		}

		doCheckAfterPropertiesSet();
	}

	protected abstract Logger getLogger();

	protected abstract void doCheckAfterPropertiesSet() throws Exception;

	public void setPluginBase(String pluginBase) {
		this.pluginBase = pluginBase;
	}

	public String getPluginBase() {
		return pluginBase;
	}

	public void dispose() {
		getLogger().info("Disposing:");
		synchronized (channelMap) {
			for (Channel ch : channelMap.values()) {
				EPICS_CONTROLLER.destroy(ch);

				getLogger().info("Channel being destroyed:" + ch.getName());
			}
			channelMap.clear();
		}
	}
}
