/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package gda.device.detector.pvcam;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.epics.connection.EpicsController;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

/**
 * EPICS PVCAM driver specific processing variables support.
 *
 * @see "file:///dls_sw/prod/R3.14.12.3/support/areaDetector/1-9dls4/documentation/pvcamDoc.html"
 */
public class EpicsPvCam implements DetectorInitializer {
	// Setup the logging facilities
	static final Logger logger = LoggerFactory.getLogger(EpicsPvCam.class);
	private String name;

	protected final static EpicsController EPICS_CONTROLLER = EpicsController.getInstance();

	private String basePVName;
	// List of Fields supported below
	private final String INITIALISE_DETECTOR = "Initialize";
	/**
	 * Map that stores the channel against the PV name
	 */
	private Map<String, Channel> channelMap = new HashMap<String, Channel>();

	@Override
	public void initialiseDetector() {
		try {
			EPICS_CONTROLLER.caput(createChannel(getBasePVName() + INITIALISE_DETECTOR), 1);
		} catch (CAException | InterruptedException | TimeoutException e) {
			logger.error(getName() + ": detector initialize failed.", e);
		}
	}

	private Channel createChannel(String fullPvName) throws CAException, TimeoutException {
		Channel channel = channelMap.get(fullPvName);
		if (channel == null) {
			try {
				channel = EPICS_CONTROLLER.createChannel(fullPvName);
			} catch (CAException cae) {
				logger.warn("EpicsPvCam -> Problem creating channel", cae);
				throw cae;
			} catch (TimeoutException te) {
				logger.warn("EpicsPvCam -> Problem creating channel", te);
				throw te;
			}
			channelMap.put(fullPvName, channel);
		}
		return channel;
	}

	public void init() throws Exception {
		if (name == null || name.isEmpty()) {
			throw new IllegalStateException("Object name is required.");
		}
		if (basePVName == null || basePVName.isEmpty()) {
			throw new IllegalStateException("Base PV name is required.");
		}
	}

	public String getBasePVName() {
		return basePVName;
	}

	public void setBasePVName(String basePVName) {
		this.basePVName = basePVName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void destroy() throws Exception {
		if (!channelMap.isEmpty()) {
			for (Channel ch : channelMap.values()) {
				ch.destroy();
			}
		}
	}
}
