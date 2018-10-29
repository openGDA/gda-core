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

package gda.device.enumpositioner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.EnumPositionerStatus;
import gda.epics.connection.EpicsController;
import gda.epics.util.JCAUtils;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.CAStatus;
import gov.aps.jca.Channel;
import gov.aps.jca.Monitor;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.dbr.DBR_STS_Short;
import gov.aps.jca.event.ConnectionEvent;
import gov.aps.jca.event.ConnectionListener;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import gov.aps.jca.event.PutEvent;
import gov.aps.jca.event.PutListener;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * This classes addresses a positioner that is controlled by a single PV, in contrast to (for example) {@link gda.device.enumpositioner.EpicsPositioner}
 * <p>
 * It additionally allows the user to use different names for the preset positions than those defined in Epics.
 * In this case, the object needs to be configured with a map of {@code <user-facing name>} to {@code <Epics name>}
 */
@ServiceInterface(EnumPositioner.class)
public class EpicsSimplePositioner extends EnumPositionerBase implements ConnectionListener, MonitorListener {

	private static final Logger logger = LoggerFactory.getLogger(EpicsSimplePositioner.class);

	protected String pvName;
	protected EpicsController controller;
	protected Channel currentPositionChnl;
	private PutCallbackListener pcbl;

	private Map<String, String> values;
	private Map<String, String> reverseValues;
	protected HashSet<Channel> monitorInstalledSet;

	public EpicsSimplePositioner(){
		controller = EpicsController.getInstance();
		pcbl = new PutCallbackListener();
	}
	@Override
	public void configure() throws FactoryException {

		// cf. ScannableMotor.configure
		this.inputNames = new String[] { getName() };

		this.outputFormat = new String[]{"%s"};

		try {
			if (!isConfigured()) {
				monitorInstalledSet = new HashSet<Channel>(3);

				currentPositionChnl = controller.createChannel(pvName, this);

				setConfigured(true);
			} // end of if(!configured)

		} catch (Exception e) {
			throw new FactoryException("failed to create channel " + pvName, e);
		}
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {

		String positionString = position.toString();

		// find in the positionNames array the index of the string
		if (values.containsKey(positionString)) {
			final String value = values.get(positionString);
			setPositionerStatus(EnumPositionerStatus.MOVING);
			try {
				controller.caput(currentPositionChnl, value, pcbl);
			} catch (Exception e) {
				setPositionerStatus(EnumPositionerStatus.ERROR);
				throw new DeviceException("failed to moveTo", e);
			}

		} else {
			// if get here then wrong position name supplied
			throw new DeviceException("Position called: " + positionString + " not found.");
		}
	}

	@Override
	public String getPosition() throws DeviceException {
		try {
			String value = controller.cagetString(currentPositionChnl);
			return reverseValues.get(value);
		} catch (Exception e) {
			throw new DeviceException("failed to get position", e);
		}
	}

	public String getPvName() {
		return pvName;
	}

	@Override
	public String checkPositionValid(Object position) {
		if (!values.containsKey(position.toString())){
			return position.toString() + "not in list of acceptable values";
		}
		return null;
	}

	public void setPvName(String pvName) {
		this.pvName = pvName;
	}

	/**
	 * Setup position map using enum values from Epics, with key = value.
	 */
	private void setMapFromEpics() throws TimeoutException, CAException, InterruptedException {
		String[] enumValues = controller.cagetLabels(currentPositionChnl);
		logger.debug("Setting map values from Epics : {} = {}", pvName, Arrays.asList(enumValues));
		Map<String,String> valuesMap = new LinkedHashMap<>();
		for(String enumValue : enumValues) {
			valuesMap.put(enumValue,  enumValue);
		}
		setValues(valuesMap);
	}

	@Override
	public void connectionChanged(ConnectionEvent ev) {
		Channel ch = (Channel) ev.getSource();
		boolean installMonitor = false;

		if (ev.isConnected()) {
			synchronized (monitorInstalledSet) {
				installMonitor = !monitorInstalledSet.contains(ch);
			}
			// If no map has been set by user, generate it using available values from Epics
			if (values == null || values.isEmpty()) {
				try {
					setMapFromEpics();
				} catch (TimeoutException | CAException | InterruptedException e) {
					logger.error("Problem generating map from Epics values", e);
				}
			}
		}

		// start a monitor on the first connection
		if (installMonitor) {
			try {
				// Print some information
				logger.info(JCAUtils.timeStamp() + " Search successful for: " + ch.getName());
				// ch.printInfo();
				// Add a monitor listener on every successful connection
				// The following is commented out to solve scan pyException
				// problem
				// - need to track down the real cause.
				controller.setMonitor(ch, DBRType.STS_SHORT, Monitor.VALUE | Monitor.ALARM, this);

				synchronized (monitorInstalledSet) {
					monitorInstalledSet.add(ch);
				}

			} catch (Exception ex) {
				logger.error("Add Monitor failed for Channel: " + ch.getName() + " : " + ex);
				return;
			}
		}

		// print connection state
		logger.info(JCAUtils.timeStamp() + " ");
		if (ch.getConnectionState() == Channel.CONNECTED) {
			logger.info(ch.getName() + " is connected");
		} else if (ch.getConnectionState() == Channel.DISCONNECTED) {
			logger.info(ch.getName() + " is disconnected");
		} else if (ch.getConnectionState() == Channel.CLOSED) {
			logger.info(ch.getName() + " is closed");
		}
	}

	@Override
	public void monitorChanged(MonitorEvent arg0) {
		// no matter what message is received, send observers latest status
		final DBR dbr = arg0.getDBR();
		short position = 0;
		if (dbr.isSTS()) {
			final DBR_STS_Short value = ((DBR_STS_Short) dbr);
			position = value.getShortValue()[0];
		}
		try {
			if (currentPositionChnl != null && getNumberOfPositions() > 0) {
				notifyIObservers(getPosition(position), getStatus());
			} else {
				notifyIObservers(null, getStatus());
			}
		} catch (DeviceException e) {
			logger.debug("Exception while updating EpicsPositioner {}", getName(), e);
		}

	}

	@Override
	public String toFormattedString() {
		try {
			return getName() + " : " + getPosition() + " " + createFormattedListAcceptablePositions();
		} catch (DeviceException e) {
			return valueUnavailableString();
		}
	}

	/**
	 * @return Returns the values.
	 */
	public Map<String, String> getValues() {
		return values;
	}

	/**
	 * Map<String, String> - means <GDA name, EPICS name>
	 * @param values
	 *            The values to set.
	 */
	public void setValues(Map<String, String> values) {
		this.values = values;
		addPositions(values.keySet());
		reverseValues = new HashMap<>(3);
		for (String key : values.keySet()) {
			final String value = values.get(key);
			reverseValues.put(value, key);
		}
	}
	private class PutCallbackListener implements PutListener {
		volatile PutEvent event = null;
		@Override
		public void putCompleted(PutEvent ev) {
			logger.debug("caputCallback complete for {}", getName());
			event = ev;

			if (event.getStatus() != CAStatus.NORMAL) {
				logger.error("Put failed. Channel {} : Status {}", ((Channel) event.getSource()).getName(), event
						.getStatus());
			} else {
				logger.info("{} move done", getName());
			}
			setPositionerStatus(EnumPositionerStatus.IDLE);
		}

	}

}
