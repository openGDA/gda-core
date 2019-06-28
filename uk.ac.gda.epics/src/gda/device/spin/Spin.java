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

package gda.device.spin;

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.ISpin;
import gda.device.scannable.ScannableBase;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.factory.FactoryException;
import gov.aps.jca.Channel;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * Spin class
 */
@ServiceInterface(ISpin.class)
public class Spin extends ScannableBase implements ISpin, InitializationListener {

	private static final Logger logger = LoggerFactory.getLogger(Spin.class);

	// control fields
	/**
	 * spin enable/disable channel
	 */
	private Channel enableChannel;

	/**
	 * spin speed channel
	 */
	private Channel speedChannel;
	/**
	 * EPICS controller for CA methods
	 */
	private EpicsController controller;

	/**
	 * EPICS Channel Manager
	 */
	private EpicsChannelManager channelManager;

	private String enablePV;

	private String speedPV;

	private Vector<String> positions = new Vector<String>();

	private StateMonitorListener sml;

	public volatile String currentposition = null;

	private boolean initialised = false;

	/**
	 * Constructor
	 */
	public Spin() {
		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager(this);
		sml = new StateMonitorListener();
	}

	@Override
	public void configure() throws FactoryException {
		if (!isConfigured()) {
			if (getSpeedPV() == null || getEnablePV() == null) {
				logger.error("Missing PV for sample spin {}", getName());
				throw new FactoryException("Missing PV for sample spin " + getName());
			}
			createChannelAccess(getSpeedPV(), getEnablePV());
			setConfigured(true);
		}
	}

	private void createChannelAccess(String speedPV2, String enablePV2) throws FactoryException {
		try {
			enableChannel = channelManager.createChannel(enablePV2, sml, false);
			speedChannel = channelManager.createChannel(speedPV2, false);
			// acknowledge that creation phase is completed
			channelManager.tryInitialize(100);
			channelManager.creationPhaseCompleted();
		} catch (Throwable th) {
			throw new FactoryException("failed to create all channels", th);
		}
	}

	@Override
	public void setSpeed(double value) throws DeviceException {
		try {
			controller.caput(speedChannel, value, 5);
		} catch (Throwable e) {
			throw new DeviceException("set speed failed. ", e);
		}
	}

	@Override
	public double getSpeed() throws DeviceException {
		try {
			return controller.cagetDouble(speedChannel);
		} catch (Throwable e) {
			throw new DeviceException("failed to get spin status ", e);
		}
	}

	/**
	 * enable spinning
	 *
	 * @throws DeviceException
	 */
	private void enable() throws DeviceException {
		try {
			controller.caput(enableChannel, 1, 5);
		} catch (Throwable e) {
			throw new DeviceException("enable failed. ", e);
		}
	}

	/**
	 * disable spinning
	 *
	 * @throws DeviceException
	 */
	private void disable() throws DeviceException {
		try {
			controller.caput(enableChannel, 0, 5);
		} catch (Throwable e) {
			throw new DeviceException("disable failed. ", e);
		}
	}

	@Override
	public String getState() throws DeviceException {
		try {
			return positions.get(controller.cagetEnum(enableChannel));
		} catch (Throwable e) {
			throw new DeviceException("failed to get spin status ", e);
		}
	}

	/**
	 * gets the available positions from this device.
	 *
	 * @return available positions
	 * @throws DeviceException
	 */
	public String[] getPositions() throws DeviceException {
		String[] positionLabels = new String[positions.size()];
		try {
			positionLabels = controller.cagetLabels(enableChannel);
		} catch (Exception e) {
			if( e instanceof RuntimeException)
				throw (RuntimeException)e;
			throw new DeviceException(getName() + " exception in getPositions",e);
		}
		return positionLabels;
	}

	@Override
	public void initializationCompleted() throws DeviceException {
		for (String position : getPositions()) {
			if (position != null && !position.isEmpty()) {
				positions.add(position);
			}
		}

		try {
			currentposition = getState();
		} catch (DeviceException e) {
			logger.error("{}: initialising current position failed. ", getName());
		}
		initialised = true;
		logger.info("{} is initialised.", getName());
	}

	@Override
	public void atScanStart() throws DeviceException {
		on();
	}

	@Override
	public void atScanEnd() throws DeviceException {
		off();
	}

	@Override
	public void on() throws DeviceException {
		enable();
	}

	@Override
	public void off() throws DeviceException {
		disable();
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		int pos = Integer.parseInt(position.toString());
		if (pos == 1) {
			on();
		} else if (pos == 0) {
			off();
		} else {
			throw new IllegalArgumentException("Only takes value 1 for on or 0 for off.");
		}
	}

	@Override
	public Object getPosition() throws DeviceException {
		return currentposition;
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

	private class StateMonitorListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			if (initialised) {
				DBR dbr = arg0.getDBR();
				if (dbr.isENUM()) {
					short index = ((DBR_Enum) dbr).getEnumValue()[0];
					currentposition = positions.get(index);
					logger.debug("Position of {}: {}", getName(), currentposition);
					notifyIObservers(currentposition);
				} else {
					logger.error("{} : ProfileListener expect Enum type but got {} type.", getName(), dbr.getType());
				}
			}
		}

		private void notifyIObservers(String position) {
			logger.info("{}: Notify observers of position changed to {}", getName(), position);
			Spin.this.notifyIObservers(this, position);
			logger.info("{}: notify sent.", getName());
		}
	}

	public String getEnablePV() {
		return enablePV;
	}

	public void setEnablePV(String enablePV) {
		this.enablePV = enablePV;
	}

	public String getSpeedPV() {
		return speedPV;
	}

	public void setSpeedPV(String speedPV) {
		this.speedPV = speedPV;
	}
}
