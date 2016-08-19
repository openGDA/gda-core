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

import org.apache.commons.lang.ArrayUtils;
import org.python.core.PyString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.epics.ConfigurationNotFoundException;
import gda.configuration.epics.Configurator;
import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.EnumPositionerStatus;
import gda.device.Scannable;
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
public class EpicsSimpleBinary extends EnumPositionerBase implements EnumPositioner, MonitorListener, Scannable,
		InitializationListener {

	private static final Logger logger = LoggerFactory.getLogger(EpicsSimpleBinary.class);

	private String epicsRecordName;

	private String deviceName;

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	private EpicsController controller;

	private EpicsChannelManager channelManager;

	private String[] positions = new String[] { "Out", "In" };

	// the channel to change between 0 and 1
	private Channel controlChnl;

	private boolean readonly = false;

	private PutCallbackListener pcbl;

	/**
	 * Constructor
	 */
	public EpicsSimpleBinary() {
		super();
		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager(this);
		pcbl = new PutCallbackListener();
	}

	private String pvName;

	public void setPvName(String pvName) {
		this.pvName = pvName;
	}

	void checkConfigured() throws DeviceException {
		if (!configured)
			throw new DeviceException(getName() + " is not yet configured");
	}

	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			if (pvName != null) {
				createChannelAccess(pvName);
			} else {
				if (epicsRecordName != null) {
					try {
						// remove any final :
						if (epicsRecordName.endsWith(":")) {
							epicsRecordName = epicsRecordName.substring(0, epicsRecordName.lastIndexOf(":"));
						}

						createChannelAccess(epicsRecordName);
					} catch (Exception e) {
						logger.error("Error while trying to configure: " + getName() + " : " + e.getMessage());
						throw new FactoryException("Error while trying to configure: " + getName() + " : " + e.getMessage());
					}
				} else if (getDeviceName() != null) {
					SimpleBinaryType simpleBinaryConfig;
					try {
						simpleBinaryConfig = Configurator.getConfiguration(getDeviceName(), SimpleBinaryType.class);
						createChannelAccess(simpleBinaryConfig.getRECORD().getPv());
					} catch (ConfigurationNotFoundException e) {
						logger.error("Can NOT find EPICS configuration for motor " + getDeviceName(), e);
					}

				}
			}
			this.inputNames = new String[] { getName() };
			this.outputFormat = new String[] { "%s" };
			channelManager.tryInitialize(100);
			configured = true;
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
		DBR dbr = arg0.getDBR();
		if (dbr.isENUM()) {
			int dmovValue = ((DBR_Enum) dbr).getEnumValue()[0];
			this.notifyIObservers(this, dmovValue);
		} else if (dbr.isSTS()) {
			int dmovValue = ((DBR_STS_Enum) dbr).getEnumValue()[0];
			this.notifyIObservers(this, dmovValue);
		} else {
			logger.error("errorwith MonitorEvent from" + epicsRecordName + "should return ENUM type value.");
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
	 * @see gda.device.EnumPositioner#getPositions()
	 */
	@Override
	public String[] getPositions() throws DeviceException {
		return positions;
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

		if (newPositions[0] == null || newPositions[1] == null || newPositions[0].isEmpty()
				|| newPositions[1].isEmpty()) {
			throw new IllegalArgumentException("Positions array cannot have empty elements");
		}

		positions = newPositions;
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {

		String positionString = position.toString();

		checkConfigured();
		if (readonly) {
			throw new DeviceException("trying to move readonly epics object" + getName());
		}

		throwExceptionIfInvalidTarget(positionString);

		try {
			if (ArrayUtils.contains(positions, positionString)) {
				controller.caput(controlChnl, positionString, pcbl);
			}
		} catch (Exception e) {
			throw new DeviceException(getName() +" exception in rawAsynchronousMoveTo",e);
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
			throw new DeviceException(e.getMessage(), e);
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
		if (!ArrayUtils.contains(positions, position)) {
			return position.toString() + "not in array of acceptable strings";
		}
		return null;
	}

	/**
	 * @return the epicsRecordName
	 */
	public String getEpicsRecordName() {
		return epicsRecordName;
	}

	/**
	 * @param epicsRecordName
	 *            the templateName to set
	 */
	public void setEpicsRecordName(String epicsRecordName) {
		this.epicsRecordName = epicsRecordName;
	}

	public boolean isReadonly() {
		return readonly;
	}

	/**
	 * Set to true to prevent attempts to change the pv through this class
	 *
	 * @param readonly
	 */
	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
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
