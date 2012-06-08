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

package gda.device.detector.areadetector.v17.impl;

import gda.device.detector.areadetector.IPVProvider;
import gda.epics.connection.EpicsController;
import gov.aps.jca.Channel;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleChannelProvider {

	static final Logger logger = LoggerFactory.getLogger(SimpleChannelProvider.class);
	private final EpicsController epicsController;
	private IPVProvider pvProvider;


	SimpleChannelProvider( EpicsController epicsController, IPVProvider pvProvider){
		this.epicsController = epicsController;
		this.pvProvider = pvProvider;
		
	}
	
	/**
	 * Map that stores the channel against the PV name
	 */
	private Map<String, Channel> channelMap = new HashMap<String, Channel>();


	public Channel createChannel(String pvKey) throws Exception {
		Channel channel = channelMap.get(pvKey);
		if (channel == null) {
			channel = epicsController.createChannel(pvProvider.getPV(pvKey));
			channelMap.put(pvKey, channel);
		}
		return channel;
	}

}
