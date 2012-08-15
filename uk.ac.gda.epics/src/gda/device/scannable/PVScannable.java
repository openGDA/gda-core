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

package gda.device.scannable;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.epics.ConfigurationNotFoundException;
import gda.configuration.epics.Configurator;
import gda.device.DeviceException;
import gda.epics.CAClient;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.epics.interfaces.SimpleMotorType;
import gda.epics.interfaces.SimplePvType;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import gov.aps.jca.event.PutEvent;
import gov.aps.jca.event.PutListener;

/**
 * Represents and controls a PV. Unlike gda.device.controlpoint classes, this operates a single pv.
 * <p>
 * The isBusy value is based on whether the CAPutCallBack has returned after a call to asynchronousMoveTo.
 * <p>
 * There is an optional deadband value to restrict the amount of messages distributed by this class.
 * <p>
 * There is also a canMove flag to restrict changes to the PV.
 */
public class PVScannable extends ScannableBase implements MonitorListener, InitializationListener, PutListener {
	
	private static final Logger logger = LoggerFactory.getLogger(PVScannable.class);

	/**
	 * The attribute to get to retrieve the units string from Epics.
	 */
	public static final String UNITSATTRIBUTE = "unitName";
	private String name;
	private double deadband = 0.0;
	private double lastKnownValue = 0.0;
	private boolean isBusy = false;
	private boolean canMove = true;
	private String pvName = "";
	private String unitsPvName = "";
	private String units = "";
	private String deviceName;

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	private Channel theChannel;
	private EpicsController controller;
	private EpicsChannelManager channelManager;
	
	public PVScannable() {
		
	}

	public PVScannable(String name, String pv) {
		this.name=name;
		this.pvName=pv;
	}
	/**
	 * @see gda.device.DeviceBase#configure()
	 */
	@Override
	public void configure() throws FactoryException {
		this.setInputNames(new String[] { getName() });

		// connect to PV
		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager(this);
		if (pvName==""){
			SimplePvType config;
			try {
				config = Configurator.getConfiguration(getDeviceName(), SimplePvType.class);
			} catch (ConfigurationNotFoundException e) {
				logger.error(
						"Can NOT find EPICS configuration for PV scannable " + getDeviceName() + "."
								+ e.getMessage(), e);
				throw new FactoryException("Can NOT find EPICS configuration for PV scannable " + getDeviceName()
						+ "." + e.getMessage(), e);
			}	
			pvName=config.getRECORD().getPv();
		}
		try {
			theChannel = channelManager.createChannel(pvName, this);
			channelManager.creationPhaseCompleted();
		} catch (CAException e) {
			logger.warn("CAException while configuring " + getName() + ": " + e.getMessage());
		}
	}

	/**
	 * @return the pvName
	 */
	public String getPvName() {
		return pvName;
	}

	/**
	 * @param pvName
	 *            the pvName to set
	 */
	public void setPvName(String pvName) {
		this.pvName = pvName;
	}

	/**
	 * @return the deadband used when notifying IObservers of changes
	 */
	public double getDeadband() {
		return deadband;
	}

	/**
	 * @param deadband
	 *            the deadband to set
	 */
	public void setDeadband(double deadband) {
		this.deadband = deadband;
	}

	/**
	 * @return the canMove
	 */
	public boolean isCanMove() {
		return canMove;
	}

	/**
	 * @param canMove
	 *            the canMove to set
	 */
	public void setCanMove(boolean canMove) {
		this.canMove = canMove;
	}

	/**
	 * @return the unitsPvName
	 */
	public String getUnitsPvName() {
		return unitsPvName;
	}

	/**
	 * @param unitsPvName
	 *            the unitsPvName to set
	 */
	public void setUnitsPvName(String unitsPvName) {
		this.unitsPvName = unitsPvName;
	}

	/**
	 * @return the units
	 */
	public String getUnits() {
		return units;
	}

	/**
	 * @param units
	 *            the units to set
	 */
	public void setUnits(String units) {
		this.units = units;
	}

	/**
	 * @see gda.device.Scannable#asynchronousMoveTo(java.lang.Object)
	 */
	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		if (canMove) {
			double target = ScannableUtils.objectToArray(position)[0];
			try {
				controller.caput(theChannel, target, this);
				isBusy = true;
			} catch (Exception e) {
				isBusy = false;
				throw new DeviceException(e.getMessage(),e);
			}
		}
	}

	/**
	 * @see gda.device.Scannable#getPosition()
	 */
	@Override
	public Object getPosition() throws DeviceException {
		try {
			return controller.cagetDouble(theChannel);
		} catch (InterruptedException e) {
			throw new DeviceException(getName() + " exception in getPosition", e);
		} catch (CAException e) {
			throw new DeviceException(getName() + " exception in getPosition", e);
		} catch (TimeoutException e) {
			throw new DeviceException(getName() + " exception in getPosition", e);
		}

	}

	/**
	 * @see gda.device.Scannable#isBusy()
	 */
	@Override
	public boolean isBusy() throws DeviceException {
		return isBusy;
	}

	/**
	 * @see gov.aps.jca.event.MonitorListener#monitorChanged(gov.aps.jca.event.MonitorEvent)
	 */
	@Override
	public void monitorChanged(MonitorEvent arg0) {

		try {
			double newPosition = (Double) getPosition();
			// if there is a deadband then test if change is great enough
			if (this.deadband > 0) {
				if (Math.abs(newPosition - this.lastKnownValue) > this.deadband) {
					notifyObserversOfNewPosition(newPosition);
				}
			} else {
				notifyObserversOfNewPosition(newPosition);
			}
		} catch (DeviceException e) {
			logger.error("DeviceException during monitorChanged in " + getName() + ": " + e.getMessage());
		}

	}
	
	private void notifyObserversOfNewPosition(Serializable newPosition) {
		this.notifyIObservers(this, newPosition);
		this.notifyIObservers(this, new ScannablePositionChangeEvent(newPosition));
	}

	/**
	 * @see gda.epics.connection.InitializationListener#initializationCompleted()
	 */
	@Override
	public void initializationCompleted() {
		// do nothing
	}

	/**
	 * @see gov.aps.jca.event.PutListener#putCompleted(gov.aps.jca.event.PutEvent)
	 */
	@Override
	public void putCompleted(PutEvent arg0) {
		// get here the callbacks from caputs made in asynchronousMoveTo
		isBusy = false;
	}

	/**
	 * @see gda.device.DeviceBase#getAttribute(java.lang.String)
	 */
	@Override
	public Object getAttribute(String attributename) throws DeviceException {
		if (attributename == UNITSATTRIBUTE && unitsPvName != "") {
			CAClient ca = new CAClient();
			try {
				return ca.caget(unitsPvName);
			} catch (InterruptedException e) {
				throw new DeviceException(getName() + " exception in getAttribute for " + attributename, e);
			} catch (CAException e) {
				throw new DeviceException(getName() + " exception in getAttribute for " + attributename, e);
			} catch (TimeoutException e) {
				throw new DeviceException(getName() + " exception in getAttribute for " + attributename, e);
			}
		} else if (attributename == UNITSATTRIBUTE && units != "") {
			return units;
		}
		return null;
	}
	
	@Override
	public String toString(){
		try {
			String tostring = ScannableUtils.getFormattedCurrentPosition(this);
			String units = (String) getAttribute(UNITSATTRIBUTE);
			
			if (units == null || units.isEmpty()){
				return tostring;
			}
			return tostring + units;
		} catch (DeviceException e) {
			return getName();
		}
	}

}
