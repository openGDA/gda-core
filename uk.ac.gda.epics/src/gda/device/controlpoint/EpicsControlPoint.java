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

package gda.device.controlpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.ControlPoint;
import gda.device.DeviceException;
import gda.device.scannable.ScannableMotionBase;
import gda.device.scannable.ScannableUtils;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * The Class EpicsControlPoint.
 */
@ServiceInterface(ControlPoint.class)
public class EpicsControlPoint extends ScannableMotionBase implements ControlPoint, InitializationListener {

	private static final Logger logger = LoggerFactory.getLogger(EpicsControlPoint.class);

	private double sensitivity = 0.0;

	private String pvName;

	private double latestValue;

	private Channel theChannelSet;

	private Channel theChannelGet;

	private String pvNameSetPoint;

	private String pvNameGetPoint;

	protected EpicsChannelManager channelManager;

	protected EpicsController controller;

	//if true the channels are monitored and observers notified of changes
	boolean monitorChannels=true;

	/**
	 * The Constructor.
	 */
	public EpicsControlPoint() {
		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager(this);
	}

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		this.inputNames = new String[]{getName()};

		if (!isConfigured()) {
			if (getPvName() != null) {
				try {
					pvNameSetPoint = getPvName();
					theChannelSet = channelManager.createChannel(pvNameSetPoint, getMonitorListener(), false);
					channelManager.creationPhaseCompleted();

					pvNameGetPoint = pvNameSetPoint;
					theChannelGet = theChannelSet;
				} catch (CAException e) {
					throw new FactoryException("failed to create Channel for Control Point Set", e);
				}
			} else if ((getPvNameSetPoint() != null) && (getPvNameGetPoint() != null)) {

				if (getPvNameSetPoint().equals(getPvNameGetPoint())) {
					logger.warn("The PV name set point and PV name get point may not be the same");
				}

				try {
					theChannelSet = channelManager.createChannel(pvNameSetPoint, getMonitorListener(), false);
				} catch (CAException e) {
					throw new FactoryException("failed to create Channel for Control Point Set", e);
				}
				try {
					theChannelGet = channelManager.createChannel(pvNameGetPoint, getMonitorListener(), false);
				} catch (CAException e) {
					throw new FactoryException("failed to create Channel for Control Point Get", e);
				}
				channelManager.creationPhaseCompleted();
			} else {
				logger.error("Control point not properly specified", getName());
				throw new FactoryException("Control point not properly specified for the control point " + getName());
			}
		}

		setConfigured(true);
	}

	MonitorListener getMonitorListener(){
		return monitorChannels ? new ValueMonitorListener() : null;
	}
	// Implementation of ControlPoint
	@Override
	public double getValue() throws DeviceException {
		try {
			latestValue = controller.cagetDouble(theChannelGet);
		} catch (Exception e) {
			throw new DeviceException("ControlPoint " + getName() + " exception in getValue", e);
		}
		return latestValue;
	}

	@Override
	public void setValue(double newValue) throws DeviceException {
		try {
			controller.caputWait(theChannelSet, newValue); // TODO: Should not block asynchronousMoveTo thread.
		} catch (Exception e) {
			throw new DeviceException("ControlPoint " + getName() + " exception in eetValue", e);
		}
	}

	/**
	 * Returns the name of the pv this object is monitoring
	 *
	 * @return the name of the pv
	 */
	public String getPvNameSetPoint() {
		return pvNameSetPoint;
	}

	/**
	 * Sets the name of the pv this object monitors. This must be called before the configure method makes the
	 * connections to the pv.
	 *
	 * @param pvNameSetPoint
	 */

	public void setPvNameSetPoint(String pvNameSetPoint) {
		this.pvNameSetPoint = pvNameSetPoint;
	}

	/**
	 * Sets the name of the pv this object monitors. This must be called before the configure method makes the
	 * connections to the pv.
	 *
	 * @param pvNameGetPoint
	 */
	public void setPvNameGetPoint(String pvNameGetPoint) {
		this.pvNameGetPoint = pvNameGetPoint;
	}

	/**
	 * Returns the name of the pv this object is monitoring
	 *
	 * @return the name of the pv
	 */
	public String getPvNameGetPoint() {
		return pvNameGetPoint;
	}

	public String getPvName() {
		return pvName;
	}

	public void setPvName(String pvName) {
		this.pvName = pvName;
	}

	/**
	 * Returns the sensitivity of updates from this monitor. The sensitivity is the percentage change which must occur
	 * in the pv value for the IObservers to be informed. This prevents unneccessary updating.
	 *
	 * @return the sensitivity of updates from this monitor
	 */
	public double getSensitivity() {
		return sensitivity;
	}

	/**
	 * Sets the sensitivity level of updates to IObservers from this object.
	 *
	 * @param sensitivity
	 */
	public void setSensitivity(double sensitivity) {
		this.sensitivity = sensitivity;
	}

	// Implementation of InitializationListener
	@Override
	public void initializationCompleted() {
		logger.debug("ControlPoint -  " + getName() + " is initialised.");
	}

	private class ValueMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			// extract the value and confirm its type
			DBR dbr = arg0.getDBR();
			if (dbr.isDOUBLE()) {
				// update the latest value
				double lastValue = latestValue;
				latestValue = ((DBR_Double) dbr).getDoubleValue()[0];

				// if the percentage change has been great enough, then inform
				// IObservers
				if (Math.abs((lastValue - latestValue) / lastValue) * 100.0 >= sensitivity) {
					notifyIObservers(this, latestValue);
				}
			}
		}
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		setValue(ScannableUtils.objectToArray(position)[0]);

	}

	@Override
	public Object getPosition() throws DeviceException {
		return getValue();
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return false;
	}

	public boolean isMonitorChannels() {
		return monitorChannels;
	}

	public void setMonitorChannels(boolean monitorChannels) {
		this.monitorChannels = monitorChannels;
	}



}
