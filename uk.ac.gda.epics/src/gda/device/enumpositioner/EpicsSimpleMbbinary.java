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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.EnumPositionerStatus;
import gda.device.scannable.ScannablePositionChangeEvent;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.EpicsController.MonitorType;
import gda.epics.connection.InitializationListener;
import gda.factory.FactoryException;
import gda.jython.JythonServerFacade;
import gov.aps.jca.CAStatus;
import gov.aps.jca.Channel;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import gov.aps.jca.event.PutEvent;
import gov.aps.jca.event.PutListener;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * This class maps to EPICS SimpleMbbinary template.
 */
@ServiceInterface(EnumPositioner.class)
public class EpicsSimpleMbbinary extends EnumPositionerBase implements InitializationListener {

	private static final Logger logger = LoggerFactory.getLogger(EpicsSimpleMbbinary.class);

	private EpicsController controller;

	private EpicsChannelManager channelManager;

	private Channel pv;

	private PvMonitorListener pvMonitor;

	private PutCallbackListener pcbl;

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
		if (!isConfigured()) {
			if (recordName == null) {
				throw new IllegalStateException("No recordName set for EpicsSimpleMbbinary + '" + getName() + "'");
			}
			createChannelAccess(recordName);
			channelManager.tryInitialize(100);
			setConfigured(true);
		}
	}

	private void createChannelAccess(String recordName) throws FactoryException {
		try {
			pv = channelManager.createChannel(recordName, pvMonitor, MonitorType.NATIVE, false);
			// acknowledge that creation phase is completed
			channelManager.creationPhaseCompleted();

		} catch (Exception e) {
			// TODO take care of destruction
			throw new FactoryException("failed to connect to all channels", e);
		}
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		try {
			short test = controller.cagetEnum(pv);
			return getPosition(test);
		} catch (Exception e) {
			throw new DeviceException("failed to get position from " + pv.getName(), e);
		}
	}

	public void reset() {
		if (getPositionerStatus() == EnumPositionerStatus.ERROR) {
			logger.info("reset device {}", getName());
			setPositionerStatus(EnumPositionerStatus.IDLE);
		} else if (getPositionerStatus() == EnumPositionerStatus.MOVING) {
			logger.info("device {} is moving", getName());
			int i = 0;
			while (getPositionerStatus() == EnumPositionerStatus.MOVING && i < 30) {
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
		if (containsPosition(position.toString())) {
			int target = getPositionIndex(position.toString());
			try {
				setPositionerStatus(EnumPositionerStatus.MOVING);
				controller.caput(pv, target, pcbl);
			} catch (Exception e) {
				setPositionerStatus(EnumPositionerStatus.ERROR);
				throw new DeviceException(pv.getName() + " failed to moveTo " + position.toString(), e);
			}
			return;
		}

		// if get here then wrong position name supplied
		throw new DeviceException("Position called: " + position.toString() + " not found.");

	}

	public String[] getEpicsPositions() throws DeviceException {
		try {
			return controller.cagetLabels(pv);
		} catch (Exception e) {
			throw new DeviceException(getName() + " exception in getPositions",e);
		}
	}

	@Override
	public void initializationCompleted() throws DeviceException {
		setPositionsInternal(Arrays.asList(getEpicsPositions()));
		initialised = true;
		logger.info("{} is initialised. Number of positions: {} ", getName(), getNumberOfPositions());
	}

	/**
	 * InPos monitor listener
	 */
	private class PvMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			logger.info("Monitor event received from {}", arg0.getSource());
			if (initialised) {
				int value = -1;
				String position = "";
				DBR dbr = arg0.getDBR();
				if (dbr.isENUM()) {
					value = ((DBR_Enum) dbr).getEnumValue()[0];
					try {
						position = EpicsSimpleMbbinary.this.getPositions()[value];
					} catch (DeviceException e) {
						logger.error("Could not read Enum Positions. New value: {}", value, e);
						return;
					}
					setPositionerStatus(EnumPositionerStatus.IDLE);
					notifyIObservers(EpicsSimpleMbbinary.this, new ScannablePositionChangeEvent(position));
					logger.info("{} is at {}", getName(), position);
				} else {
					logger.error("Expect enum but got {}", dbr.getType().getName());
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
				setPositionerStatus(EnumPositionerStatus.ERROR);
			} else {
				logger.info("{} move done", getName());
				setPositionerStatus(EnumPositionerStatus.IDLE);
			}
		}

	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		if (recordName == null) {
			// readOnly is set by createChannelAccess
			throw new IllegalStateException("readOnly may only be set when recordName is used to configure device: " + getName());
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
