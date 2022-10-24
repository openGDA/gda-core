/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package gda.device.controlpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.epics.connection.EpicsController;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.Monitor;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.event.MonitorListener;

/**
 * This is a reusable class that can be utilised for getting numerical
 * limits depending on the value of pv that returns a string. It requires
 * a bunch of {@link Limits} objects added to a {@link LimitsMap} object
 * which is defined in Spring xml configuration file.
 */
public class DynamicScannableLimits {

	private static final Logger logger = LoggerFactory.getLogger(DynamicScannableLimits.class);
	private LimitsMap limitsMap;
	private String pvName;
	private Channel channel;
	private EpicsController controller;
	// using monitor
	private boolean useMonitor;
	private Monitor monitor;
	private String currentPvValue;
	private String[] allPvLabels;

	private MonitorListener ml = event -> {
		DBR_Enum dbr = (DBR_Enum) event.getDBR();
		int index = dbr.getEnumValue()[0];
		currentPvValue = allPvLabels[index];
	};


	public void init() {
		controller = EpicsController.getInstance();
		try {
			channel = controller.createChannel(pvName);
		} catch (CAException | TimeoutException e) {
			logger.error("Error occured when trying to create channel from PV name", e);
		}
		if (useMonitor) {
			try {
				allPvLabels = controller.cagetLabels(channel);
				monitor = controller.setMonitor(channel, ml);
			} catch (CAException | InterruptedException | TimeoutException e) {
				logger.error("Error with getting labels or setting monitor", e);
			}
		}
	}

	public void destroy() {
		if (useMonitor) {
			monitor.removeMonitorListener(ml);
			monitor = null;
			try {
				channel.destroy();
				channel = null;
			} catch (IllegalStateException | CAException e) {
				logger.error("exception on destroy channel {}", channel.getName());
			}
		}
	}

	public Limits getLimits() {
		String key = "";
		try {
			if (useMonitor) {
				key = currentPvValue;
			} else {
				key = controller.cagetLabel(channel);
			}
		} catch (TimeoutException | CAException | InterruptedException e) {
			logger.error("Error occured while trying to get value from channel", e);
		}
		return limitsMap.getLimitsMap().get(key);
	}


	public void setLimitsMap(LimitsMap limitsMap) {
		this.limitsMap = limitsMap;
	}

	public void setPvName(String pvName) {
		this.pvName = pvName;
	}

	public void setUseMonitor(boolean useMonitor) {
		this.useMonitor = useMonitor;
	}
}
