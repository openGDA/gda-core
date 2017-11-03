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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.epics.ConfigurationNotFoundException;
import gda.configuration.epics.Configurator;
import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.EnumPositionerStatus;
import gda.device.scannable.ScannablePositionChangeEvent;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.EpicsController.MonitorType;
import gda.epics.connection.InitializationListener;
import gda.epics.interfaceSpec.GDAEpicsInterfaceReader;
import gda.epics.interfaceSpec.InterfaceException;
import gda.epics.interfaceSpec.Xml;
import gda.epics.interfaces.SimpleMbbinaryType;
import gda.epics.xml.EpicsRecord;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.jython.JythonServerFacade;
import gov.aps.jca.CAStatus;
import gov.aps.jca.Channel;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import gov.aps.jca.event.PutEvent;
import gov.aps.jca.event.PutListener;

/**
 * This class maps to EPICS SimpleMbbinary template.
 */
public class EpicsSimpleMbbinary extends EnumPositionerBase implements EnumPositioner, InitializationListener {

	private static final Logger logger = LoggerFactory.getLogger(EpicsSimpleMbbinary.class);

	private String deviceName;

	private EpicsController controller;

	private EpicsChannelManager channelManager;

	private Channel pv;

	private PvMonitorListener pvMonitor;

	private PutCallbackListener pcbl;

	private String epicsRecordName;

	private boolean readOnly = true;

	private String recordName;

	private boolean initialised = false;

	/**
	 * Constructor
	 */
	public EpicsSimpleMbbinary() {
		super();
		channelManager = new EpicsChannelManager(this);
		controller = EpicsController.getInstance();
		pvMonitor = new PvMonitorListener();
		pcbl = new PutCallbackListener();
	}

	/**
	 * Sets the record name that this object will connect to.
	 *
	 * @param recordName
	 *            the record name
	 */
	public void setRecordName(String recordName) {
		this.recordName = recordName;
	}

	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			if (recordName != null) {
				createChannelAccess(recordName);
				channelManager.tryInitialize(100);
			}

			// EPICS interface verion 2 for phase I beamlines + I22
			else if (getEpicsRecordName() != null) {
				EpicsRecord epicsRecord;

				if ((epicsRecord = (EpicsRecord) Finder.getInstance().find(epicsRecordName)) != null) {
					recordName = epicsRecord.getFullRecordName();
					createChannelAccess(recordName);
					channelManager.tryInitialize(100);
				} else {
					logger.error("Epics Record " + epicsRecordName + " not found");
					throw new FactoryException("Epics Record " + epicsRecordName + " not found");
				}
			}
			// EPICS interface version 3 for phase II beamlines (excluding I22).
			else if (getDeviceName() != null) {
				SimpleMbbinaryType pnrConfig;
				try {
					pnrConfig = Configurator.getConfiguration(getDeviceName(),
							gda.epics.interfaces.SimpleMbbinaryType.class);
					createChannelAccess(pnrConfig);
					channelManager.tryInitialize(100);
				} catch (ConfigurationNotFoundException e) {
					try {
						gda.epics.interfaceSpec.Device device = GDAEpicsInterfaceReader.getDeviceFromType(
								Xml.simpleMbbinary_type_name, getDeviceName());
						createChannelAccess(device);
						channelManager.tryInitialize(100);
					} catch (InterfaceException ex) {
						throw new FactoryException("Error initialising device " + getDeviceName());
					}
				}
			}
			configured = true;
		}
	}

	private void createChannelAccess(gda.epics.interfaceSpec.Device device) throws FactoryException {
		try {
			pv = channelManager.createChannel(device.getField("RECORD").getPV(), pvMonitor, MonitorType.NATIVE, false);
			readOnly = device.getField("RECORD").isReadOnly();
			// acknowledge that creation phase is completed
			channelManager.creationPhaseCompleted();

		} catch (Throwable th) {
			// TODO take care of destruction
			throw new FactoryException("failed to connect to all channels", th);
		}
	}

	private void createChannelAccess(SimpleMbbinaryType config) throws FactoryException {
		try {
			pv = channelManager.createChannel(config.getRECORD().getPv(), pvMonitor, MonitorType.NATIVE, false);
			readOnly = config.getRECORD().getRo();
			// acknowledge that creation phase is completed
			channelManager.creationPhaseCompleted();

		} catch (Throwable th) {
			// TODO take care of destruction
			throw new FactoryException("failed to connect to all channels", th);
		}
	}

	private void createChannelAccess(String recordName) throws FactoryException {
		try {
			pv = channelManager.createChannel(recordName, pvMonitor, MonitorType.NATIVE, false);
			// acknowledge that creation phase is completed
			channelManager.creationPhaseCompleted();

		} catch (Throwable th) {
			// TODO take care of destruction
			throw new FactoryException("failed to connect to all channels", th);
		}
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		try {
			short test = controller.cagetEnum(pv);
			return positions.get(test);
		} catch (Throwable th) {
			throw new DeviceException("failed to get position from " + pv.getName(), th);
		}
	}

	@Override
	public EnumPositionerStatus getStatus() {
		return positionerStatus;
	}

	/**
	 *
	 */
	public void reset() {
		if (positionerStatus == EnumPositionerStatus.ERROR) {
			logger.info("reset device {}", getName());
			positionerStatus = EnumPositionerStatus.IDLE;
		} else if (positionerStatus == EnumPositionerStatus.MOVING) {
			logger.info("device {} is moving", getName());
			int i = 0;
			while (positionerStatus == EnumPositionerStatus.MOVING && i < 30) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// noop
				}
				i++;
			}
			if (i == 30) {
				logger.error("device {} is locked in moving state, please report this.", getName());
			}
		}
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		if (readOnly) {
			JythonServerFacade.getInstance().print("You can not write to a READ-ONLY field in EPICS");
			logger.warn("Failed to write to Read-Only field in {}.", getName());
			return;
		}
		// find in the positionNames array the index of the string
		if (positions.contains(position.toString())) {
			int target = positions.indexOf(position.toString());
			try {
				positionerStatus = EnumPositionerStatus.MOVING;
				controller.caput(pv, target, pcbl);
			} catch (Throwable th) {
				positionerStatus = EnumPositionerStatus.ERROR;
				throw new DeviceException(pv.getName() + " failed to moveTo " + position.toString(), th);
			}
			return;
		}

		// if get here then wrong position name supplied
		throw new DeviceException("Position called: " + position.toString() + " not found.");

	}

	@Override
	public String[] getPositions() throws DeviceException {
		try {
			return controller.cagetLabels(pv);
		} catch (Exception e) {
			throw new DeviceException(getName() + " exception in getPositions",e);
		}
	}

	@Override
	public void initializationCompleted() throws DeviceException {
		String[] position = getPositions();
		for (int i = 0; i < position.length; i++) {
			if (position[i] != null || position[i] != "") {
				super.positions.add(position[i]);
				logger.info("{} has available position: {}", getName(), position[i]);
			}
		}
		initialised = true;
		logger.info("{} is initialised. Number of positions: {} ", getName(), positions.size());

	}

	/**
	 * InPos monitor listener
	 */
	private class PvMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			logger.debug("{} monitor changed: {}", getName(), arg0);
			if (initialised) {
				String[] posn;
				try {
					posn = getPositions();
				} catch (DeviceException de) {
					return;
				}
				if (posn.length != 0) {
					int value = -1;
					String position = "";
					DBR dbr = arg0.getDBR();
					if (dbr.isENUM()) {
						value = ((DBR_Enum) dbr).getEnumValue()[0];
						position = posn[value];
						if (!position.isEmpty()) {
							positionerStatus = EnumPositionerStatus.IDLE;
							notifyIObservers(EpicsSimpleMbbinary.this, new ScannablePositionChangeEvent(position));
						}
						logger.info("{} is at {}", getName(), position);
					}
				}
			}
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
				positionerStatus = EnumPositionerStatus.ERROR;
			} else {
				logger.info("{} move done", getName());
				positionerStatus = EnumPositionerStatus.IDLE;
			}
		}

	}

	/**
	 * @return epicsRecordName
	 */
	public String getEpicsRecordName() {
		return epicsRecordName;
	}

	/**
	 * @param epicsRecordName
	 */
	public void setEpicsRecordName(String epicsRecordName) {
		this.epicsRecordName = epicsRecordName;
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

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) throws DeviceException {
		if (recordName == null) {
			// readOnly is set by createChannelAccess
			throw new DeviceException("readOnly may only be set when recordName is used to configure device: " + deviceName);
		}
		this.readOnly = readOnly;
	}

	@Override
	public String toFormattedString() {
		try {
			// get the current position as an array of doubles
			Object position = getPosition();

			// if position is null then simply return the name
			if (position == null) {
				logger.warn("getPosition() from {} returns NULL.", getName());
				return valueUnavailableString();
			}
			return getName() + " : " + position.toString();
		} catch (Exception e) {
			logger.warn("{}: exception while getting position. ", getName(), e);
			return getName();
		}
	}
}
