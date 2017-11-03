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

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.epics.ConfigurationNotFoundException;
import gda.configuration.epics.Configurator;
import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.EnumPositionerStatus;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.epics.interfaceSpec.GDAEpicsInterfaceReader;
import gda.epics.interfaceSpec.Xml;
import gda.epics.interfaces.PneumaticType;
import gda.epics.xml.EpicsRecord;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

/**
 * This class maps onto the EPICS Pneumatic template.
 *
 * @deprecated Replace with {@link EpicsPneumaticCallback}
 */
@Deprecated
public class EpicsPneumatic extends EnumPositionerBase implements EnumPositioner, InitializationListener {

	private static final Logger logger = LoggerFactory.getLogger(EpicsPneumatic.class);

	private String epicsRecordName;

	private String deviceName;

	private EpicsController controller;

	private EpicsChannelManager channelManager;

	private String controlPv;

	private String statusPv;

	private Channel control = null;

	private Channel status;

	private StatusMonitorListener statusMonitor;

	private Object lock = new Object();

	private Vector<String> statuspositions = new Vector<String>();

	/**
	 * constructor
	 */
	public EpicsPneumatic() {
		super();
		channelManager = new EpicsChannelManager(this);
		controller = EpicsController.getInstance();
		statusMonitor = new StatusMonitorListener();
		logger.warn("EpicsPneumatic is deprecated, it should be replaced with EpicsPneumaticCallback");
	}

	private void setPvNames(String recordName) {
		controlPv = recordName + ":CON";
		statusPv = recordName + ":STA";
	}

	private void setPvNames(PneumaticType config) {
		controlPv = config.getCONTROL().getPv();
		statusPv = config.getSTA().getPv();
	}

	// method supporting EpicsDevice interface
	private void setPvNames(gda.epics.interfaceSpec.Device device) {
		controlPv = device.getField("CONTROL").getPV();
		statusPv = device.getField("STA").getPV();
	}

	/**
	 * Sets the PV name that this object will link to. The control and status PVs will be formed by appending ":CON" and
	 * ":STA" to the specified PV name. If these names are not correct the control/status PVs can be set separately.
	 *
	 * @param pvName
	 *            the record name
	 */
	public void setPvName(String pvName) {
		setPvNames(pvName);
	}

	/**
	 * Sets the control PV used by this object.
	 *
	 * @param controlPv
	 *            the control PV
	 */
	public void setControlPv(String controlPv) {
		this.controlPv = controlPv;
	}

	/**
	 * Sets the status PV used by this object.
	 *
	 * @param statusPv
	 *            the status PV
	 */
	public void setStatusPv(String statusPv) {
		this.statusPv = statusPv;
	}

	@Override
	public void configure() throws FactoryException {
		if (!configured) {

			if (controlPv == null || statusPv == null) {

				// EPICS interface version 2 for phase I beamlines + I22
				if (getEpicsRecordName() != null) {
					EpicsRecord epicsRecord;

					if ((epicsRecord = (EpicsRecord) Finder.getInstance().findNoWarn(epicsRecordName)) != null) {
						String recordName = epicsRecord.getEpicsDeviceName();
						setPvNames(recordName);
					} else {
						setPvNames(epicsRecordName);
					}
				}
				// EPICS interface version 3 for phase II beamlines (excluding I22).
				else if (getDeviceName() != null) {
					PneumaticType pneuConfig;
					try {
						pneuConfig = Configurator.getConfiguration(getDeviceName(),
								gda.epics.interfaces.PneumaticType.class);
						setPvNames(pneuConfig);
					} catch (ConfigurationNotFoundException e) {
						/* Try to read from unchecked xml */
						try {
							gda.epics.interfaceSpec.Device device = GDAEpicsInterfaceReader.getDeviceFromType(
									Xml.pneumatic_type_name, deviceName);
							setPvNames(device);
						} catch (Exception ex) {
							logger.error("Can NOT find EPICS configuration for scaler " + getDeviceName() + "."
									+ e.getMessage(), ex);
						}
					}

				}
				// Nothing specified in Server XML file
				else {
					logger.error("Missing EPICS interface configuration for the motor {} ", getName());
					throw new FactoryException("Missing EPICS interface configuration for the motor " + getName());
				}
			}

			try {
				createChannelAccess();
			} catch (Throwable th) {
				throw new FactoryException("failed to connect to all channels", th);
			}

			configured = true;
		}
	}

	private void createChannelAccess() throws CAException {
		control = channelManager.createChannel(controlPv, false);
		status = channelManager.createChannel(statusPv, statusMonitor, false);
		channelManager.creationPhaseCompleted();
		channelManager.tryInitialize(100);
	}

	/**
	 * gets the current status position of this device.
	 *
	 * @return position in String
	 * @throws DeviceException
	 */
	@Override
	public String getPosition() throws DeviceException {
		try {
			if (status != null) {
				short test = controller.cagetEnum(status);
				return statuspositions.get(test);
			}

			return getName() + " : NOT Available.";

		} catch (Throwable th) {
			throw new DeviceException("failed to get status position from " + status.getName(), th);
		}
	}

	/**
	 * gets the current status of this device from EPICS, i.e poll it.
	 *
	 * @return EnumPositionerStatus
	 * @throws DeviceException
	 */
	public EnumPositionerStatus getPositionerStatus() throws DeviceException {
		try {
			short statusValue = controller.cagetEnum(status);
			String statusString = statuspositions.get(statusValue);
			// first check if its moving
			if (statusString.equals("Opening") || statusString.equals("Closing")) {
				return EnumPositionerStatus.MOVING;
			} else if (statusString.equals("Open") || statusString.equals("Closed")) {
				return EnumPositionerStatus.IDLE;
			} else if (statusString.equals("Fault")) {
				return EnumPositionerStatus.ERROR;
			} else {
				logger.error("{} returned an unknown status. It is set to ERROR now.", getName());
				return EnumPositionerStatus.ERROR;
			}
		} catch (Throwable e) {
			throw new DeviceException("while polling EpicsPneumatic " + getName() + " : " + e.getMessage(), e);
		}

	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		// find in the positionNames array the index of the string
		if (positions.contains(position.toString())) {
			int target = positions.indexOf(position.toString());
			try {
				controller.caput(control, target,channelManager);
			} catch (Throwable th) {
				throw new DeviceException(control.getName() + " failed to moveTo " + position.toString(), th);
			}
			return;
		}

		// if get here then wrong position name supplied
		throw new DeviceException("Position called \'" + position.toString() + "\' not found.");

	}

	@Override
	public void stop() throws DeviceException {
		throw new DeviceException("stop() operation is not available for " + getName());
	}

	/**
	 * gets the available positions from this device.
	 *
	 * @return the available positions from this device.
	 */
	@Override
	public String[] getPositions() throws DeviceException {
		try {
			return controller.cagetLabels(control);
		} catch (Exception e) {
			throw new DeviceException(getName() + " exception in getPositions", e);
		}
	}

	/**
	 * gets the available status positions from this device.
	 *
	 * @return the available status positions from this device.
	 */
	public String[] getStatusPositions() {
		String[] positionLabels = new String[statuspositions.size()];
		try {
			positionLabels = controller.cagetLabels(status);
		} catch (Exception e) {
			// ignore
		}
		return positionLabels;
	}

	@Override
	public void initializationCompleted() throws DeviceException {
		String[] position = getPositions();
		String[] statusposition = getStatusPositions();
		for (int i = 0; i < position.length; i++) {
			if (position[i] != null || position[i] != "") {
				super.positions.add(position[i]);
			}
		}
		for (int i = 0; i < statusposition.length; i++) {
			if (statusposition[i] != null || statusposition[i] != "") {
				this.statuspositions.add(statusposition[i]);
			}
		}
		logger.info("{} is initialised.", getName());
	}

	/**
	 * update pneumatic status from EPICS.
	 */
	private class StatusMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			short value = -1;
			DBR dbr = arg0.getDBR();
			if (dbr.isENUM()) {
				value = ((DBR_Enum) dbr).getEnumValue()[0];
			}
			if (value == 0) {
				synchronized (lock) {
					positionerStatus = EnumPositionerStatus.ERROR;
				}
			} else if (value == 1 || value == 3) {
				synchronized (lock) {
					positionerStatus = EnumPositionerStatus.IDLE;
				}
			} else if (value == 2 || value == 4) {
				synchronized (lock) {
					positionerStatus = EnumPositionerStatus.MOVING;
				}
			}
			notifyIObservers(this, positionerStatus);
		}
	}

	/**
	 * gets Epics record name
	 *
	 * @return String
	 */
	public String getEpicsRecordName() {
		return epicsRecordName;
	}

	/**
	 * sets EPICS record name
	 *
	 * @param epicsRecordName
	 */
	public void setEpicsRecordName(String epicsRecordName) {
		this.epicsRecordName = epicsRecordName;
	}

	/**
	 * gets the short device name
	 *
	 * @return String
	 */
	public String getDeviceName() {
		return deviceName;
	}

	/**
	 * sets the short device name
	 *
	 * @param deviceName
	 */
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	@Override
	public String toFormattedString() {
		try {
			// get the current position as an array of doubles
			Object position = getPosition();

			// if position is null then simply return the name
			if (position == null) {
				logger.warn("getPosition() from " + getName() + " returns NULL.");
				return valueUnavailableString();
			}

			return getName() + " : " + position.toString();
		} catch (Exception e) {
			logger.warn("{}: exception while getting position. ", getName(), e);
			return getName();
		}
	}
}
