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

import gda.configuration.epics.ConfigurationNotFoundException;
import gda.configuration.epics.Configurator;
import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.EnumPositionerStatus;
import gda.device.scannable.ScannablePositionChangeEvent;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.epics.interfaceSpec.GDAEpicsInterfaceReader;
import gda.epics.interfaceSpec.Xml;
import gda.epics.interfaces.PneumaticCallbackType;
import gda.epics.xml.EpicsRecord;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.util.Sleep;
import gda.util.exceptionUtils;
import gov.aps.jca.CAException;
import gov.aps.jca.CAStatus;
import gov.aps.jca.Channel;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import gov.aps.jca.event.PutEvent;
import gov.aps.jca.event.PutListener;

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class maps onto the EPICS PneumaticCallback template.
 */
public class EpicsPneumaticCallback extends EnumPositionerBase implements EnumPositioner, InitializationListener {
	private static final Logger logger = LoggerFactory.getLogger(EpicsPneumaticCallback.class);

	private String epicsRecordName;

	private String deviceName;

	private EpicsController controller;

	private EpicsChannelManager channelManager;

	private String controlPv;
	private String statusPv;

	private Channel control = null;

	private Channel status;

	private boolean allPVsSet = false;

	private PutCallbackListener pcl;

	private StatusMonitorListener statusMonitor;

	private Object lock = new Object();

	protected Vector<String> statuspositions = new Vector<String>();

	private boolean readOnly = false;

	/**
	 * If statusPv is simply a list of positions and does not contains state such as IDLE, MOVING etc set this to true
	 */
	public boolean statusPvIndicatesPositionOnly = false;

	public boolean isStatusPvIndicatesPositionOnly() {
		return statusPvIndicatesPositionOnly;
	}

	public void setStatusPvIndicatesPositionOnly(boolean statusPvIndicatesPositionOnly) {
		this.statusPvIndicatesPositionOnly = statusPvIndicatesPositionOnly;
	}

	/**
	 * constructor
	 */
	public EpicsPneumaticCallback() {
		super();
		channelManager = new EpicsChannelManager(this);
		controller = EpicsController.getInstance();
		statusMonitor = new StatusMonitorListener();
		pcl = new PutCallbackListener();
	}

	// method to support Phase I beamlines EPICS interface
	private void setPvNames(String recordName) {
		controlPv = recordName + ":CON";
		statusPv = recordName + ":STA";
		setAllPVsSet(true);
	}

	public void setPvNames(PneumaticCallbackType conf) {
		controlPv = conf.getCONTROL().getPv();
		statusPv = conf.getSTA().getPv();
		setAllPVsSet(true);
	}

	// method supporting EpicsDevice interface
	private void setPvNames(gda.epics.interfaceSpec.Device device) {
		controlPv = device.getField("CONTROL").getPV();
		statusPv = device.getField("STA").getPV();
		setAllPVsSet(true);
	}

	private String basePV;

	public String getPvBase() {
		return basePV;
	}

	public void setPvBase(String basePV) {
		this.basePV = basePV;
		setPvNames(basePV);
	}

	/**
	 * Sets the control PV used by this object.
	 *
	 * @param controlPv
	 *            the control PV
	 */
	public void setControlPv(String controlPv) {
		this.controlPv = controlPv;
		setAllPVsSet(statusPv != null);
	}

	public String getControlPv() {
		return controlPv;
	}

	/**
	 * Sets the status PV used by this object.
	 *
	 * @param statusPv
	 *            the status PV
	 */
	public void setStatusPv(String statusPv) {
		this.statusPv = statusPv;
		setAllPVsSet(controlPv != null);
	}

	public String getStatusPv() {
		return statusPv;
	}

	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			if (!isAllPVsSet()) {
				String recordName;
				// EPICS interface version 2 for phase I beamlines + I22
				if (getEpicsRecordName() != null) {
					EpicsRecord epicsRecord;

					if ((epicsRecord = (EpicsRecord) Finder.getInstance().find(epicsRecordName)) != null) {
						recordName = epicsRecord.getEpicsDeviceName();
						setPvNames(recordName);
					} else {
						logger.error("Epics Record " + epicsRecordName + " not found");
						throw new FactoryException("Epics Record " + epicsRecordName + " not found");
					}
				}
				// EPICS interface version 3 for phase II beamlines (excluding I22).
				else if (getDeviceName() != null) {
					PneumaticCallbackType pneuConfig;
					try {
						pneuConfig = Configurator.getConfiguration(getDeviceName(),
								gda.epics.interfaces.PneumaticCallbackType.class);
						setPvNames(pneuConfig);
					} catch (ConfigurationNotFoundException e) {
						/* Try to read from unchecked xml */
						try {
							gda.epics.interfaceSpec.Device device = GDAEpicsInterfaceReader.getDeviceFromType(
									Xml.pneumaticCallback_type_name, deviceName);
							setPvNames(device);
						} catch (Exception ex) {
							logger.error(
									"Can NOT find EPICS configuration for scaler " + getDeviceName() + "."
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
			} catch (CAException e) {
				// TODO take care of destruction
				throw new FactoryException("failed to connect to all channels", e);
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
			throw new DeviceException("while polling EpicsPneumaticCallback " + getName() + " : " + e.getMessage(), e);
		}

	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		if (isReadOnly()) {
			throw new DeviceException("Cannot move " + getName()
					+ " as it is configured (within the gda software) to be read only");
		}

		// find in the positionNames array the index of the string
		if (positions.contains(position.toString())) {
			int target = positions.indexOf(position.toString());
			try {
				if (getStatus() == EnumPositionerStatus.MOVING) {
					logger.warn("{} is busy", getName());
					return;
				}
				positionerStatus = EnumPositionerStatus.MOVING;
				controller.caput(control, target, pcl);
			} catch (CAException e) {
				positionerStatus = EnumPositionerStatus.ERROR;
				throw new DeviceException(control.getName() + " failed to moveTo " + position.toString()
						+ "\n!!! Epics Channel Access problem: " + e.getMessage(), e);
			} catch (Throwable th) {
				positionerStatus = EnumPositionerStatus.ERROR;
				throw new DeviceException(control.getName() + " failed to moveTo " + position.toString() + "\n!!! "
						+ th.getMessage(), th);
			}
			return;
		}

		// if get here then wrong position name supplied
		throw new DeviceException("Position called \'" + position.toString() + "\' not found.");

	}

	@Override
	public void stop() throws DeviceException {
		// throw new DeviceException("stop() operation is not available for " + getName());
	}

	/**
	 * gets the available positions from this device.
	 *
	 * @return the available positions from this device.
	 * @throws DeviceException
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
	 * @throws DeviceException
	 */
	public String[] getStatusPositions() throws DeviceException {
		try {
			return controller.cagetLabels(status);
		} catch (Exception e) {
			throw new DeviceException(getName() + " exception in getStatusPositions", e);
		}
	}

	@Override
	public void initializationCompleted() throws DeviceException {
		String[] position = getPositions();
		String[] statusposition = getStatusPositions();
		super.positions.clear();
		for (int i = 0; i < position.length; i++) {
			if (position[i] != null || position[i] != "") {
				super.positions.add(position[i]);
			}
		}
		this.statuspositions.clear();
		for (int i = 0; i < statusposition.length; i++) {
			if (statusposition[i] != null || statusposition[i] != "") {
				this.statuspositions.add(statusposition[i]);
			}
		}
		logger.info("{} is initialised.", getName());
	}

	/**
	 * update PneumaticCallback status from EPICS.
	 */
	private class StatusMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			short value = -1;
			DBR dbr = arg0.getDBR();
			if (dbr.isENUM()) {
				value = ((DBR_Enum) dbr).getEnumValue()[0];
			}
			if (statusPvIndicatesPositionOnly) {
				// See GDA-5822 - wait for status positions field being initialised before calling getPosition().
				while (statuspositions.size() < value + 1) {
					Sleep.sleep(100);
				}
				notifyIObservers(EpicsPneumaticCallback.this,
						new ScannablePositionChangeEvent(statuspositions.get(value)));

			} else {
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
	}

	private class PutCallbackListener implements PutListener {
		volatile PutEvent event = null;

		@Override
		public void putCompleted(PutEvent ev) {
			try {
				logger.debug("caputCallback complete for {}", getName());
				event = ev;

				if (event.getStatus() != CAStatus.NORMAL) {
					logger.error("Put failed. Channel {} : Status {}", ((Channel) event.getSource()).getName(),
							event.getStatus());
					positionerStatus = EnumPositionerStatus.ERROR;
				} else {
					logger.info("{} move done", getName());
					positionerStatus = EnumPositionerStatus.IDLE;
				}
			} catch (Exception ex) {
				exceptionUtils.logException(logger, "Error in putCompleted for " + getName(), ex);
			}
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

	public boolean isAllPVsSet() {
		return allPVsSet;
	}

	public void setAllPVsSet(boolean allPVsSet) {
		this.allPVsSet = allPVsSet;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public boolean isReadOnly() {
		return readOnly;
	}
}
