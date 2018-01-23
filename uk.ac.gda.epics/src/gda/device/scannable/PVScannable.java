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
import gda.epics.interfaces.SimplePvType;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DOUBLE;
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
	private double deadband = 0.0;
	private double lastKnownValue = 0.0;
	private boolean isBusy = false;
	private boolean canMove = true;
	protected String pvName = "";
	private String unitsPvName = "";
	private String units = "";
	private String deviceName;

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	protected Channel theChannel;
	protected EpicsController controller = EpicsController.getInstance();
	private EpicsChannelManager channelManager;

	public PVScannable() {
		channelManager = new EpicsChannelManager(this);
	}

	public PVScannable(String name, String pv) {
		setName(name);
		this.pvName=pv;
		channelManager = new EpicsChannelManager(this);
	}


	@Override
	public void configure() throws FactoryException {
		if (!isConfigured()) {
			this.setInputNames(new String[] { getName() });

			// connect to PV
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
			setConfigured(true);
		}
	}

	public String getPvName() {
		return pvName;
	}

	public void setPvName(String pvName) {
		this.pvName = pvName;
	}

	/**
	 * @return the deadband used when notifying IObservers of changes
	 */
	public double getDeadband() {
		return deadband;
	}

	public void setDeadband(double deadband) {
		this.deadband = deadband;
	}

	public boolean isCanMove() {
		return canMove;
	}

	public void setCanMove(boolean canMove) {
		this.canMove = canMove;
	}

	public String getUnitsPvName() {
		return unitsPvName;
	}

	public void setUnitsPvName(String unitsPvName) {
		this.unitsPvName = unitsPvName;
	}

	public String getUnits() {
		return units;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		if (canMove) {
			double target = ScannableUtils.objectToArray(position)[0];
			try {
				isBusy = true;
				controller.caput(theChannel, target, this);
			} catch (Exception e) {
				isBusy = false;
				throw new DeviceException(e.getMessage(),e);
			}
		}
	}

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

	@Override
	public boolean isBusy() throws DeviceException {
		return isBusy;
	}

	@Override
	public void monitorChanged(MonitorEvent event) {

		final DBR dbr = event.getDBR();

		if (!dbr.isDOUBLE()) {
			return;
		}

		final DOUBLE doubleDbr = (DOUBLE) dbr;
		final double newPosition = doubleDbr.getDoubleValue()[0];

		// if there is a deadband then test if change is great enough
		if (this.deadband > 0) {
			if (Math.abs(newPosition - this.lastKnownValue) > this.deadband) {
				notifyObserversOfNewPosition(newPosition);
			}
		} else {
			notifyObserversOfNewPosition(newPosition);
		}
	}

	private void notifyObserversOfNewPosition(Serializable newPosition) {
		this.notifyIObservers(this, newPosition);
		this.notifyIObservers(this, new ScannablePositionChangeEvent(newPosition));
	}

	@Override
	public void initializationCompleted() {
		// do nothing
	}

	@Override
	public void putCompleted(PutEvent arg0) {
		// get here the callbacks from caputs made in asynchronousMoveTo
		isBusy = false;
	}

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
			String unitString = (String) getAttribute(UNITSATTRIBUTE);

			if (unitString == null || unitString.isEmpty()){
				return tostring;
			}
			return tostring + unitString;
		} catch (Exception e) {
			logger.warn("{}: exception while getting value", getName(), e);
			return valueUnavailableString();
		}
	}

}
