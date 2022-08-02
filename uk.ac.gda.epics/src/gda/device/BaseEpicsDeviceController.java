/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package gda.device;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.epics.connection.EpicsController;
import gda.factory.ConfigurableBase;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.event.MonitorListener;
import gov.aps.jca.event.PutListener;

public class BaseEpicsDeviceController extends ConfigurableBase {

	// Non-static logger and getClass() allows log messages to come from the correct subclass,
	// avoiding confusion in logs if multiple subclasses of this are used on the same beamline
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private String basePvName;
	private EpicsController epicsController = EpicsController.getInstance();
	private final Map<String, Channel> channels = new HashMap<>();

	private static final String EPICS_GET_ERROR_MESSAGE_TEMPLATE = "Unable to get %s from EPICS";
	private static final String EPICS_SET_ERROR_MESSAGE_TEMPLATE = "Unable to set %s to %s via EPICS";
	private static final String EPICS_GET_INTERRUPTED_ERROR_MESSAGE_TEMPLATE = "Thread interrupted while getting %s from EPICS.";
	private static final String EPICS_SET_INTERRUPTED_ERROR_MESSAGE_TEMPLATE = "Thread interrutped while setting %s to %s via EPICS";

	public String getBasePvName() {
		return basePvName;
	}

	public void setBasePvName(String basePvName) {
		this.basePvName = basePvName;
	}

	protected Channel getChannel(String pvSuffix) throws TimeoutException, CAException {
		String fullPvName = getBasePvName() + pvSuffix;
		Channel channel = channels.get(fullPvName);

		if (channel == null) {
			channel = epicsController.createChannel(fullPvName);
			channels.put(fullPvName, channel);
		}

		return channel;
	}

	protected EpicsController getEpicsController() {
		return epicsController;
	}

	protected void initialiseEnumChannel(String channel, List<String> list) throws Exception {
		String[] positionLabels = null;
		positionLabels = epicsController.cagetLabels(getChannel(channel));
		if (positionLabels == null || positionLabels.length == 0) {
			throw new DeviceException("Error getting labels from enum channel: " + getBasePvName() + channel);
		}
		// Clear the list here this allows for rerunning configure
		list.clear();
		// Add the positions to the list
		for (String position : positionLabels) {
			if (position == null || position.isEmpty()) {
				logger.warn("Enum channel {} contains empty entries", getBasePvName() + channel);
			} else {
				list.add(position);
			}
		}
	}

	protected int getIntegerValue(String channelName, String fieldNameForErrorMessage) throws DeviceException {
		try {
			return epicsController.cagetInt(getChannel(channelName));
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new DeviceException(String.format(EPICS_GET_INTERRUPTED_ERROR_MESSAGE_TEMPLATE, fieldNameForErrorMessage), exception);
		} catch (Exception exception) {
			throw new DeviceException(String.format(EPICS_GET_ERROR_MESSAGE_TEMPLATE, fieldNameForErrorMessage), exception);
		}
	}

	protected void setIntegerValue(String channelName, int value, String fieldNameForErrorMessage) throws DeviceException {
		try {
			epicsController.caputWait(getChannel(channelName), value);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new DeviceException(String.format(EPICS_SET_INTERRUPTED_ERROR_MESSAGE_TEMPLATE, fieldNameForErrorMessage, value), exception);
		} catch (Exception exception) {
			throw new DeviceException(String.format(EPICS_SET_ERROR_MESSAGE_TEMPLATE, fieldNameForErrorMessage, value), exception);
		}
	}

	protected double getDoubleValue(String channelName, String fieldNameForErrorMessage) throws DeviceException {
		try {
			return epicsController.cagetDouble(getChannel(channelName));
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new DeviceException(String.format(EPICS_GET_INTERRUPTED_ERROR_MESSAGE_TEMPLATE, fieldNameForErrorMessage), exception);
		} catch (Exception exception) {
			throw new DeviceException(String.format(EPICS_GET_ERROR_MESSAGE_TEMPLATE, fieldNameForErrorMessage), exception);
		}
	}

	protected void setDoubleValue(String channelName, double value, String fieldNameForErrorMessage) throws DeviceException {
		try {
			epicsController.caputWait(getChannel(channelName), value);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new DeviceException(String.format(EPICS_SET_INTERRUPTED_ERROR_MESSAGE_TEMPLATE, fieldNameForErrorMessage, value), exception);
		} catch (Exception exception) {
			throw new DeviceException(String.format(EPICS_SET_ERROR_MESSAGE_TEMPLATE, fieldNameForErrorMessage, value), exception);
		}
	}

	protected String getStringValue(String channelName, String fieldNameForErrorMessage) throws DeviceException {
		try {
			return epicsController.cagetString(getChannel(channelName));
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new DeviceException(String.format(EPICS_GET_INTERRUPTED_ERROR_MESSAGE_TEMPLATE, fieldNameForErrorMessage), exception);
		} catch (Exception exception) {
			throw new DeviceException(String.format(EPICS_GET_ERROR_MESSAGE_TEMPLATE, fieldNameForErrorMessage), exception);
		}
	}

	protected void setStringValue(String channelName, String value, String fieldNameForErrorMessage) throws DeviceException {
		try {
			epicsController.caputWait(getChannel(channelName), value);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new DeviceException(String.format(EPICS_SET_INTERRUPTED_ERROR_MESSAGE_TEMPLATE, fieldNameForErrorMessage, value), exception);
		} catch (Exception exception) {
			throw new DeviceException(String.format(EPICS_SET_ERROR_MESSAGE_TEMPLATE, fieldNameForErrorMessage, value), exception);
		}
	}

	protected void setStringValue(String channelName, String value, double timeout, String fieldNameForErrorMessage) throws DeviceException{
		try {
			epicsController.caput(getChannel(channelName), value, timeout);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new DeviceException(String.format(EPICS_SET_INTERRUPTED_ERROR_MESSAGE_TEMPLATE, fieldNameForErrorMessage, value), exception);
		} catch (Exception exception) {
			throw new DeviceException(String.format(EPICS_SET_ERROR_MESSAGE_TEMPLATE, fieldNameForErrorMessage, value), exception);
		}
	}

	protected void setStringValueAsynchronously(String channelName, String value, String fieldNameForErrorMessage) throws DeviceException {
		try {
			epicsController.caput(getChannel(channelName), value);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new DeviceException(String.format(EPICS_SET_INTERRUPTED_ERROR_MESSAGE_TEMPLATE, fieldNameForErrorMessage, value), exception);
		} catch (Exception exception) {
			throw new DeviceException(String.format(EPICS_SET_ERROR_MESSAGE_TEMPLATE, fieldNameForErrorMessage, value), exception);
		}
	}

	protected void setStringValueAsynchronously(String channelName, String value, PutListener pl, String fieldNameForErrorMessage) throws DeviceException {
		try {
			epicsController.caput(getChannel(channelName), value, pl);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new DeviceException(String.format(EPICS_SET_INTERRUPTED_ERROR_MESSAGE_TEMPLATE, fieldNameForErrorMessage, value), exception);
		} catch (Exception exception) {
			throw new DeviceException(String.format(EPICS_SET_ERROR_MESSAGE_TEMPLATE, fieldNameForErrorMessage, value), exception);
		}
	}

	protected double[] getDoubleArray(String channelName, String fieldNameForErrorMessage) throws DeviceException {
		try {
			return epicsController.cagetDoubleArray(getChannel(channelName));
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new DeviceException(String.format(EPICS_GET_INTERRUPTED_ERROR_MESSAGE_TEMPLATE, fieldNameForErrorMessage), exception);
		} catch (Exception exception) {
			throw new DeviceException(String.format(EPICS_GET_ERROR_MESSAGE_TEMPLATE, fieldNameForErrorMessage), exception);
		}
	}

	protected double[] getDoubleArray(String channelName, int numberOfElements, String fieldNameForErrorMessage) throws DeviceException {
		try {
			return epicsController.cagetDoubleArray(getChannel(channelName), numberOfElements);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new DeviceException(String.format(EPICS_GET_INTERRUPTED_ERROR_MESSAGE_TEMPLATE, fieldNameForErrorMessage), exception);
		} catch (Exception exception) {
			throw new DeviceException(String.format(EPICS_GET_ERROR_MESSAGE_TEMPLATE, fieldNameForErrorMessage), exception);
		}
	}

	protected gov.aps.jca.Monitor setMonitor(String channelName, MonitorListener listener) throws DeviceException {
		try {
			return epicsController.setMonitor(getChannel(channelName), listener);
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new DeviceException("Interrupted while setting a monitor on " + channelName);
		} catch (TimeoutException | CAException exception) {
			throw new DeviceException ("An error occured while setting a monitor on " + channelName, exception);
		}
	}

	protected gov.aps.jca.Monitor setMonitor(String channelName, DBRType type, int mask, MonitorListener listener) throws DeviceException {
		try {
			return epicsController.setMonitor(getChannel(channelName), type, mask, listener);
		} catch (TimeoutException | CAException exception) {
			throw new DeviceException ("An error occured while setting a monitor on " + channelName, exception);
		}
	}
}
