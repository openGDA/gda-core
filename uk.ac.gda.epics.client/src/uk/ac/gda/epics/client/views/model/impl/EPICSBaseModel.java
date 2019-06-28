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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import gda.device.DeviceException;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.factory.FindableBase;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.event.MonitorListener;

/**
 * Base model class - model represents the data that is retrieved from EPICS to be represented on the client side UI.
 * This class provides the basic functionality for the rest of the models to inherit.
 *
 * @author rsr31645
 */
public abstract class EPICSBaseModel extends FindableBase implements InitializingBean, InitializationListener {
	protected final static EpicsController EPICS_CONTROLLER = EpicsController.getInstance();
	protected Map<String, Channel> channelMap;

	protected String basePVName;

	public EPICSBaseModel() {
		channelMap = new HashMap<String, Channel>();
	}

	@Override
	public void initializationCompleted() throws InterruptedException, DeviceException, TimeoutException, CAException {
		getLogger().info("{} is initialised.", getName());
	}

	protected Channel getChannel(String pvElementName, MonitorListener ml, String... args) throws Exception {
		try {
			String pvPostFix = null;
			if (args.length > 0) {
				// PV element name is different from the pvPostFix
				pvPostFix = args[0];
			} else {
				pvPostFix = pvElementName;
			}

			return createChannel(getBasePVName() + pvPostFix, ml);
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
		if (getBasePVName() == null) {
			throw new IllegalArgumentException("'deviceName','basePVName' or 'pvProvider' needs to be declared");
		}

		doCheckAfterPropertiesSet();
	}

	protected abstract Logger getLogger();

	protected abstract void doCheckAfterPropertiesSet() throws Exception;

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
