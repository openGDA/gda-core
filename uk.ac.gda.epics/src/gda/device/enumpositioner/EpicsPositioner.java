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

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.epics.ConfigurationNotFoundException;
import gda.configuration.epics.Configurator;
import gda.device.DeviceException;
import gda.device.EnumPositionerStatus;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.epics.interfaces.PositionerType;
import gda.epics.xml.EpicsRecord;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gov.aps.jca.CAException;
import gov.aps.jca.CAStatus;
import gov.aps.jca.Channel;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.dbr.Severity;
import gov.aps.jca.dbr.Status;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import gov.aps.jca.event.PutEvent;
import gov.aps.jca.event.PutListener;

/**
 * This class maps to EPICS positioner template.
 *
 * @see <a href="http://serv0002.cs.diamond.ac.uk/cgi-bin/wiki.cgi/positioner">positioner module documentation</a>
 */
public class EpicsPositioner extends EnumPositionerBase implements InitializationListener {

	private static final Logger logger = LoggerFactory.getLogger(EpicsPositioner.class);

	private static final String COULD_NOT_ESTABLISH_VALUE_CHANNEL_NAMES_WARNING = "could not establish channel names of position values";

	private Map<String,Channel> positionValueChannels = new LinkedHashMap<>(16);

	/**
	 * flag to set in Spring
	 */
	private boolean allowPositionValueReads = false;

	private boolean acceptNewMoveToPositionWhileMoving;

	private String epicsRecordName;

	private String deviceName;

	protected EpicsController controller;

	protected EpicsChannelManager channelManager;

	private String recordName;

	private String selectRecordName;

	private String inPosRecordName;

	private String dMovRecordName;

	private String stopRecordName;

	private String errorRecordName;

	protected Channel select;

	private Channel inPos;

	private Channel dmov;

	private Channel stop;

	private Channel error;

	private InposMonitorListener inposMonitor;

	protected DmovMonitorListener dmovMonitor;

	private ErrorMonitorListener errorMonitor;

	private PutCallbackListener putCallbackListener;
	private Status status = Status.NO_ALARM;
	private Severity severity = Severity.NO_ALARM;

	private Object lock = new Object();

	protected PositionerType configuration;

	/**
	 * Constructor
	 */
	public EpicsPositioner() {
		super();
		channelManager = new EpicsChannelManager(this);
		controller = EpicsController.getInstance();
		inposMonitor = new InposMonitorListener();
		dmovMonitor = new DmovMonitorListener();
		errorMonitor = new ErrorMonitorListener();
		putCallbackListener = new PutCallbackListener();
	}

	public boolean getAllowPositionValueReads() {
		return allowPositionValueReads;
	}

	public void setAllowPositionValueReads(boolean allowPositionValueReads) {
		this.allowPositionValueReads = allowPositionValueReads;
	}

	/**
	 * Sets the record name that this positioner will link to.
	 *
	 * @param recordName the record name
	 */
	public void setRecordName(String recordName) {
		this.recordName = recordName;
	}

	/**
	 * Sets the configuration for this positioner.
	 */
	public void setConfiguration(PositionerType configuration) {
		this.configuration = configuration;
	}

	@Override
	public void configure() throws FactoryException {
		this.setInputNames(new String[]{getName()});
		this.setExtraNames(new String[0]);
		if (!isConfigured()) {

			if (recordName != null) {
				setRecordNamesUsingBasePv(recordName);
			}

			else if (configuration != null) {
				setRecordNamesUsingEpicsConfiguration(configuration);
			}

			// EPICS interface version 2 for phase I beamlines + I22
			else if (getEpicsRecordName() != null) {
				EpicsRecord epicsRecord;

				if ((epicsRecord = Finder.getInstance().find(epicsRecordName)) != null) {
					final String fullRecordName = epicsRecord.getFullRecordName();
					setRecordNamesUsingBasePv(fullRecordName);
				} else {
					final String message = String.format("Epics Record %s not found", epicsRecordName);
					logger.error(message);
					throw new FactoryException(message);
				}
			}

			// EPICS interface version 3 for phase II beamlines (excluding I22).
			else if (getDeviceName() != null) {
				PositionerType pnrConfig;
				try {
					pnrConfig = Configurator.getConfiguration(getDeviceName(), PositionerType.class);
					setRecordNamesUsingEpicsConfiguration(pnrConfig);
				} catch (ConfigurationNotFoundException e) {
					logger.error("Can NOT find EPICS configuration for motor {}", getDeviceName(), e);
				}
			}

			// Nothing specified in Server XML file
			else {
				final String message = String.format("Missing EPICS interface configuration for the motor %s", getName());
				logger.error(message);
				throw new FactoryException(message);
			}

			createChannelAccess();
			channelManager.tryInitialize(100);

			// getPosition() returns a single String, so explain what it is
			inputNames = new String[]{getName()};

			setConfigured(true);
		}
	}

	/**
	 * Retrieves the five PVs for this {@link EpicsPositioner} from the specified {@link PositionerType} object.
	 */
	private void setRecordNamesUsingEpicsConfiguration(PositionerType config) {
		selectRecordName = config.getSELECT().getPv();
		inPosRecordName = config.getINPOS().getPv();
		dMovRecordName = config.getDMOV().getPv();
		stopRecordName = config.getSTOP().getPv();
		errorRecordName = config.getERROR().getPv();
	}

	/**
	 * Builds the five PVs for this {@link EpicsPositioner} by appending suffixes to a base record name.
	 */
	protected void setRecordNamesUsingBasePv(String recordName) {
		selectRecordName = recordName + ":SELECT";
		inPosRecordName = recordName + ":INPOS";
		dMovRecordName = recordName + ":DMOV";
		stopRecordName = recordName + ":STOP.PROC";
		errorRecordName = recordName + ":ERROR.K";
	}

	protected void createChannelAccess() throws FactoryException {
		try {
			select = channelManager.createChannel(selectRecordName, false);
			inPos = channelManager.createChannel(inPosRecordName, inposMonitor, false);
			dmov = channelManager.createChannel(dMovRecordName, dmovMonitor, false);
			stop = channelManager.createChannel(stopRecordName, false);
			error = channelManager.createChannel(errorRecordName, errorMonitor, false);

			// acknowledge that creation phase is completed
			channelManager.creationPhaseCompleted();

		} catch (Exception e) {
			// TODO take care of destruction
			throw new FactoryException("failed to connect to all channels", e);
		}
	}

	@Override
	public String getPosition() throws DeviceException {
		try {
			final short test = controller.cagetEnum(select);
			return getPosition(test);
		} catch (Exception e) {
			throw new DeviceException("failed to get position from " + select.getName(), e);
		}
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		// find in the positionNames array the index of the string
		if (containsPosition(position.toString())) {
			final int target = getPositionIndex(position.toString());
			try {
				if (getStatus() == EnumPositionerStatus.MOVING) {
					if (acceptNewMoveToPositionWhileMoving) {
						// stop synchronously
						controller.caput(stop, 1, 0.0); // 0.0 means no timeout
						// flag idle
						synchronized (lock) {
							setPositionerStatus(EnumPositionerStatus.IDLE);
						}
					} else {
						// reject new moveTo position
						logger.warn("{} is busy", getName());
						return;
					}
				}
				// // ensure idle
				// Preconditions.checkArgument(getStatus() == EnumPositionerStatus.IDLE);
				// flag moving
				synchronized (lock) {
					setPositionerStatus(EnumPositionerStatus.MOVING);
				}
				// move
				controller.caput(select, target, putCallbackListener);
			} catch (Exception e) {
				setPositionerStatus(EnumPositionerStatus.ERROR);
				throw new DeviceException(select.getName() + " failed to moveTo " + position.toString(), e);
			}
			return;
		}

		// if get here then wrong position name supplied
		throw new DeviceException("Position called: " + position.toString()+ " not found.");

	}

	public boolean getAcceptNewMoveToPositionWhileMoving() {
		return this.acceptNewMoveToPositionWhileMoving;
	}

	public void setAcceptNewMoveToPositionWhileMoving(boolean acceptNewMoveToPositionWhileMoving) {
		this.acceptNewMoveToPositionWhileMoving = acceptNewMoveToPositionWhileMoving;
	}

	@Override
	public void stop() throws DeviceException {
		try {
			controller.caput(stop, 1, putCallbackListener);
		} catch (Exception e) {
			throw new DeviceException("failed to stop " + stop.getName(), e);
		}
	}

	@Override
	public String[] getPositions() throws DeviceException  {
		try {
			return controller.cagetLabels(select);
		} catch (Exception e) {
			throw new DeviceException("Error gettings labels for " + getName(),e);
		}
	}

	@Override
	public boolean isInPos() throws DeviceException {
		try {
			// EPICS data type for this PV is double: value is 1.0 if in position
			final double inPosVal = controller.cagetDouble(inPos);
			return Math.abs(inPosVal - 1.0) < 0.0001;
		} catch (Exception e) {
			throw new DeviceException("Exception getting INPOS for " + getName(), e);
		}
	}

	@Override
	public void initializationCompleted() throws DeviceException, InterruptedException  {
		for (String position : getPositions()) {
			if (position != null && !position.isEmpty()) {
				addPosition(position);
			}
		}

		if (allowPositionValueReads) {
			// establish position value channels
			String basePV = recordName;
			if (recordName == null && epicsRecordName != null) {
				final EpicsRecord epicsRecord = Finder.getInstance().find(epicsRecordName);
				basePV = epicsRecord.getFullRecordName();
			}
			if (basePV != null) {
				final String positionValueChannelNamePrefix = basePV.substring(0, basePV.lastIndexOf(':')) + ":P:VAL";
				final CharSequence positionValueChannelNameSuffixes = "ABCDEFGHIJKLMNOP";
				for (int i = 0; i < getNumberOfPositions(); i++) {
					final char letter = positionValueChannelNameSuffixes.charAt(i);
					final String positionValueChannelName = positionValueChannelNamePrefix + letter;
					try {
						final Channel positionValueChannel = channelManager.createChannel(positionValueChannelName, false);
						Thread.sleep(100);
						positionValueChannels.put(getPosition(i), positionValueChannel);
					}
					catch (CAException e) {
						logger.error("could not create channel {}", positionValueChannelName);

						logger.info("destroying any position value channels that were created");
						for (Channel positionValueChannel : positionValueChannels.values()) {
							controller.destroy(positionValueChannel);
						}

						throw new DeviceException(e);
					}
				}
			}
			else {
				logger.warn(COULD_NOT_ESTABLISH_VALUE_CHANNEL_NAMES_WARNING);
			}
		}

		logger.info("{} is initialised", getName());
	}

	/**
	 * @return the physical motor value for the supplied position string.
	 * @throws DeviceException
	 */
	public Double getPositionValue(String position) throws DeviceException {
		if (!allowPositionValueReads) {
			throw new DeviceException("object not configured to allow reading of position values");
		}
		if (positionValueChannels.isEmpty()) {
			throw new DeviceException(COULD_NOT_ESTABLISH_VALUE_CHANNEL_NAMES_WARNING);
		}
		try {
			final Channel positionValueChannel = positionValueChannels.get(position);
			return controller.cagetDouble(positionValueChannel);
		} catch (Exception e) {
			throw new DeviceException("could not get value of position " + position, e);
		}
	}

	/**
	 * @return positions mapped to values in position order
	 */
	private Map<String,Double> getPositionsMap() throws DeviceException {
		final Map<String,Double> positionsMap = new LinkedHashMap<>();
		for (String position : positionValueChannels.keySet()) {
			positionsMap.put(position, getPositionValue(position));
		}
		return positionsMap;
	}

	/**
	 * Reverse lookup - potentially flawed because several positions may have the same value.
	 */
	public String getPositionFromValue(double value) throws DeviceException {
		for (Map.Entry<String, Double> entry : getPositionsMap().entrySet()) {
			if (entry.getValue() == value) {
				return entry.getKey();
			}
		}
		return null;
	}

	/**
	 * InPos monitor listener
	 */
	private class InposMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			double value = -1.0;
			final DBR dbr = arg0.getDBR();
			if (dbr.isDOUBLE()) {
				value = ((DBR_Double) dbr).getDoubleValue()[0];
			}
			if (value == 1.0) {
				synchronized (lock) {
					setPositionerStatus(EnumPositionerStatus.IDLE);
				}
			}

			sendUpdate();
		}

	}

	/**
	 * DMOV monitor
	 */
	private class DmovMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			double value = -1.0;
			final DBR dbr = arg0.getDBR();
			if (dbr.isDOUBLE()) {
				value = ((DBR_Double) dbr).getDoubleValue()[0];
			}
			if (value == 0.0) {
				synchronized (lock) {
					setPositionerStatus(EnumPositionerStatus.MOVING);
				}
			} else if (value == 1.0) {
				synchronized (lock) {
					if (getPositionerStatus() != EnumPositionerStatus.ERROR) {
						setPositionerStatus(EnumPositionerStatus.IDLE);
					}
				}
			}

			sendUpdate();
		}
	}

	/**
	 * Error Monitor
	 */
	private class ErrorMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			double value = -1.0;
			final DBR dbr = arg0.getDBR();
			if (dbr.isDOUBLE()) {
				value = ((DBR_Double) dbr).getDoubleValue()[0];
			}
			if (value != 0.0) {
				synchronized (lock) {
					setPositionerStatus(EnumPositionerStatus.ERROR);
				}
			}

			sendUpdate();
		}

	}

	private void sendUpdate() {
		notifyIObservers(this, getPositionerStatus());
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

	private class PutCallbackListener implements PutListener {
		@Override
		public void putCompleted(PutEvent ev) {
			try {
				logger.debug("caputCallback complete for {}", getName());

				if (ev.getStatus() != CAStatus.NORMAL) {
					logger.error("Put failed. Channel {} : Status {}", ((Channel) ev.getSource()).getName(), ev.getStatus());
					setPositionerStatus(EnumPositionerStatus.ERROR);
				} else {
					logger.info("{} move done", getName());
					setPositionerStatus(EnumPositionerStatus.IDLE);
				}

				if (status == Status.NO_ALARM && severity == Severity.NO_ALARM) {
					logger.info("{} moves OK", getName());
					setPositionerStatus(EnumPositionerStatus.IDLE);
				} else {
					// if Alarmed, check and report MSTA status
					logger.error("{} reports Alarm: {}", getName(), status);
					setPositionerStatus(EnumPositionerStatus.ERROR);
				}

			} catch (Exception ex) {
				logger.error("Error in putCompleted for {}", getName(), ex);
			}
		}
	}
}
