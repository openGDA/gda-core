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
import java.util.Collection;

import org.python.core.PyString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.epics.ConfigurationNotFoundException;
import gda.configuration.epics.Configurator;
import gda.device.DeviceException;
import gda.device.EditableEnumPositioner;
import gda.device.EnumPositionerStatus;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.EpicsController.MonitorType;
import gda.epics.connection.InitializationListener;
import gda.epics.interfaces.SimpleBinaryType;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.CAStatus;
import gov.aps.jca.Channel;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.dbr.DBR_STS_Enum;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import gov.aps.jca.event.PutEvent;
import gov.aps.jca.event.PutListener;

/**
 * Similar to EpicsValve, except looks at a single pv which can only have positions 0 or 1, and uses the values "Out"
 * and "In" externally for those positions.
 * <p>
 * EpicsValve should be used if the device uses the proper Epics Valve/Shutter template
 */
public class EpicsSimpleBinary extends EnumPositionerBase implements EditableEnumPositioner, MonitorListener, InitializationListener {

	private static final Logger logger = LoggerFactory.getLogger(EpicsSimpleBinary.class);

	private String epicsRecordName;

	private String deviceName;

	private EpicsController controller;

	private EpicsChannelManager channelManager;

	// the channel to change between 0 and 1
	private Channel controlChnl;

	private boolean readonly = false;

	private String pvName;

	private PutCallbackListener pcbl;

	/**
	 * Constructor
	 */
	public EpicsSimpleBinary() {
		super();
		super.setPositionsInternal(Arrays.asList("Out", "In"));
		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager(this);
		pcbl = new PutCallbackListener();
	}

	public void setPvName(String pvName) {
		this.pvName = pvName;
	}

	private void checkConfigured() throws DeviceException {
		if (!isConfigured()) {
			throw new DeviceException(getName() + " is not yet configured");
		}
	}

	@Override
	public void configure() throws FactoryException {
		if (!isConfigured()) {
			if (pvName != null) {
				createChannelAccess(pvName);
			} else {
				if (epicsRecordName != null) {
					try {
						// remove any final :
						if (epicsRecordName.endsWith(":")) {
							epicsRecordName = epicsRecordName.substring(0, epicsRecordName.lastIndexOf(':'));
						}

						createChannelAccess(epicsRecordName);
					} catch (Exception e) {
						final String message = String.format("Error while trying to configure: %s", getName());
						logger.error(message, e);
						throw new FactoryException(message, e);
					}
				} else if (deviceName != null) {
					try {
						final SimpleBinaryType simpleBinaryConfig = Configurator.getConfiguration(deviceName, SimpleBinaryType.class);
						createChannelAccess(simpleBinaryConfig.getRECORD().getPv());
					} catch (ConfigurationNotFoundException e) {
						logger.error("Can NOT find EPICS configuration for motor {}", deviceName, e);
					}
				}
			}
			this.inputNames = new String[] { getName() };
			this.outputFormat = new String[] { "%s" };
			channelManager.tryInitialize(100);
			setConfigured(true);
		}
	}

	private void createChannelAccess(String pv) throws FactoryException {
		try {
			controlChnl = channelManager.createChannel(pv, this,MonitorType.STS, false);
		} catch (CAException e) {
			throw new FactoryException(getName() + ": can not create channel to " + pv);
		}
		// acknowledge that creation phase is completed
		channelManager.creationPhaseCompleted();
	}

	/**
	 * @see gov.aps.jca.event.MonitorListener#monitorChanged(gov.aps.jca.event.MonitorEvent)
	 */
	@Override
	public void monitorChanged(MonitorEvent arg0) {
		final DBR dbr = arg0.getDBR();
		if (dbr.isENUM()) {
			final int dmovValue = ((DBR_Enum) dbr).getEnumValue()[0];
			this.notifyIObservers(this, dmovValue);
		} else if (dbr.isSTS()) {
			final int dmovValue = ((DBR_STS_Enum) dbr).getEnumValue()[0];
			this.notifyIObservers(this, dmovValue);
		} else {
			logger.error("Error in MonitorEvent from {}: should return ENUM type value.", epicsRecordName);
		}
	}

	/**
	 * @see gda.device.EnumPositioner#getStatus()
	 */
	@Override
	public EnumPositionerStatus getStatus() throws DeviceException {
		return EnumPositionerStatus.IDLE;
	}

	/**
	 * The defaults are { "Out", "In" }. Use this method to override.
	 *
	 * @param newPositions
	 */
	@Override
	public void setPositions(String[] newPositions) {

		if (newPositions.length != 2) {
			throw new IllegalArgumentException("Positions array must have 2 elements");
		}

		if (newPositions[0] == null || newPositions[1] == null || newPositions[0].isEmpty() || newPositions[1].isEmpty()) {
			throw new IllegalArgumentException("Positions array cannot have empty elements");
		}

		super.setPositionsInternal(Arrays.asList(newPositions));
	}

	@Override
	public void setPositions(Collection<String> positions) {
		setPositions(positions.toArray(new String[positions.size()]));
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		final String positionString = position.toString();

		checkConfigured();
		if (readonly) {
			throw new DeviceException("trying to move readonly epics object" + getName());
		}

		throwExceptionIfInvalidTarget(positionString);

		try {
			if (containsPosition(positionString)) {
				controller.caput(controlChnl, positionString, pcbl);
			}
		} catch (Exception e) {
			throw new DeviceException(getName() + " exception in rawAsynchronousMoveTo", e);
		}
	}

	/**
	 * @see gda.device.Scannable#getPosition()
	 */
	@Override
	public Object getPosition() throws DeviceException {
		checkConfigured();
		try {
			return controller.cagetString(controlChnl);
		} catch (Exception e) {
			throw new DeviceException(e);
		}
	}

	/**
	 * @see gda.device.Scannable#isBusy()
	 */
	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

	@Override
	public String checkPositionValid(Object position) {
		if (!(position instanceof String) && !(position instanceof PyString)) {
			return "position not a string";
		}
		final String posString = position.toString();
		if (!containsPosition(posString)) {
			return posString + " not in array of acceptable strings";
		}
		return null;
	}

	/**
	 * @param epicsRecordName
	 *            the templateName to set
	 */
	public void setEpicsRecordName(String epicsRecordName) {
		this.epicsRecordName = epicsRecordName;
	}

	/**
	 * Set to true to prevent attempts to change the pv through this class
	 *
	 * @param readonly
	 */
	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	private class PutCallbackListener implements PutListener {
		volatile PutEvent event = null;

		@Override
		public void putCompleted(PutEvent ev) {
			logger.debug("caputCallback complete for {}", getName());
			event = ev;

			if (event.getStatus() != CAStatus.NORMAL) {
				logger.error("Put failed. Channel {} : Status {}", ((Channel) event.getSource()).getName(),
						event.getStatus());
			}
			logger.info("{}: put completed", getName());
		}
	}

	@Override
	public void initializationCompleted() {
		logger.info("{}: initialisation completed", getName());
	}

}
