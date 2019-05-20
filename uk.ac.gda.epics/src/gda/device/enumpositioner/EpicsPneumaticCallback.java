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

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.EnumPositionerStatus;
import gda.device.scannable.ScannablePositionChangeEvent;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
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
 * This class maps onto the EPICS PneumaticCallback template.
 */
@ServiceInterface(EnumPositioner.class)
public class EpicsPneumaticCallback extends EnumPositionerBase implements InitializationListener {

	private static final Logger logger = LoggerFactory.getLogger(EpicsPneumaticCallback.class);

	private EpicsController controller;

	private EpicsChannelManager channelManager;

	private String basePV;

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

	private void setPvNames(String recordName) {
		controlPv = recordName + ":CON";
		statusPv = recordName + ":STA";
		setAllPVsSet(true);
	}

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
		if (!isConfigured()) {
			if (!isAllPVsSet()) {
				logger.error("Missing PV configuration for the motor {} ", getName());
				throw new FactoryException("Missing PV configuration for the motor " + getName());
			}

			try {
				createChannelAccess();
			} catch (CAException e) {
				// TODO take care of destruction
				throw new FactoryException("failed to connect to all channels", e);
			}
			setConfigured(true);
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

		} catch (Exception e) {
			throw new DeviceException("failed to get status position from " + status.getName(), e);
		}
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		if (isReadOnly()) {
			throw new DeviceException("Cannot move " + getName()
					+ " as it is configured (within the gda software) to be read only");
		}

		// find in the positionNames array the index of the string
		if (containsPosition(position.toString())) {
			int target = getPositionIndex(position.toString());
			try {
				if (getStatus() == EnumPositionerStatus.MOVING) {
					logger.warn("{} is busy", getName());
					return;
				}
				setPositionerStatus(EnumPositionerStatus.MOVING);
				controller.caput(control, target, pcl);
			} catch (CAException e) {
				setPositionerStatus(EnumPositionerStatus.ERROR);
				throw new DeviceException(control.getName() + " failed to moveTo " + position.toString()
						+ "\n!!! Epics Channel Access problem", e);
			} catch (Exception e) {
				setPositionerStatus(EnumPositionerStatus.ERROR);
				throw new DeviceException(control.getName() + " failed to moveTo " + position.toString() + "\n!!! ", e);
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
	public String[] getStatusPositions() throws DeviceException {
		try {
			return controller.cagetLabels(status);
		} catch (Exception e) {
			throw new DeviceException(getName() + " exception in getStatusPositions", e);
		}
	}

	@Override
	public void initializationCompleted() throws DeviceException {
		clearPositions();
		for (String position : getPositions()) {
			if (position != null && !position.isEmpty()) {
				addPosition(position);
			}
		}
		this.statuspositions.clear();
		for (String statusPosition : getStatusPositions()) {
			if (statusPosition != null && !statusPosition.isEmpty()) {
				this.statuspositions.add(statusPosition);
			}
		}
		logger.info("{} is initialised. Control positions available: {}, Status positions available: {}", getName(), getPositionsList(), statuspositions);
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
				try {
					while (EpicsPneumaticCallback.this.statuspositions.size() < value + 1) {
						Thread.sleep(100);
					}
				} catch (InterruptedException e) {
					logger.error("Thread interrupted while waiting for status positions to be populated", e);
					Thread.currentThread().interrupt();
					// don't continue to notify observers
					return;
				}
				notifyIObservers(EpicsPneumaticCallback.this,
						new ScannablePositionChangeEvent(EpicsPneumaticCallback.this.statuspositions.get(value)));

			} else {
				if (value == 0) {
					synchronized (lock) {
						setPositionerStatus(EnumPositionerStatus.ERROR);
					}
				} else if (value == 1 || value == 3) {
					synchronized (lock) {
						setPositionerStatus(EnumPositionerStatus.IDLE);
					}
				} else if (value == 2 || value == 4) {
					synchronized (lock) {
						setPositionerStatus(EnumPositionerStatus.MOVING);
					}
				}
				notifyIObservers(this, getPositionerStatus());
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
					setPositionerStatus(EnumPositionerStatus.ERROR);
				} else {
					logger.info("{} move done", getName());
					setPositionerStatus(EnumPositionerStatus.IDLE);
				}
			} catch (Exception ex) {
				logger.error("Error in putCompleted for {}", getName(), ex);
			}
		}
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
