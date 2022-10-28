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
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.dbr.DBR_Int;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import uk.ac.diamond.daq.concurrent.Async;
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

	private Double upperLimit = Double.POSITIVE_INFINITY;

	private Double lowerLimit = Double.NEGATIVE_INFINITY;

	private boolean busy = false;

	/**
	 * Facility for position validation based on an other PV
	 */
	private DynamicScannableLimits dynamicScannableLimits;

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
			logger.error("Control point {} not properly specified", getName());
			throw new FactoryException("Control point not properly specified for the control point " + getName());
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
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DeviceException("ControlPoint " + getName() + " exception in getValue", e);
		} catch (CAException | TimeoutException e) {
			throw new DeviceException("ControlPoint " + getName() + " exception in getValue", e);
		}
		return latestValue;
	}

	@Override
	public void setValue(double newValue) throws DeviceException {
		if (dynamicScannableLimits != null) {
			Limits limits = dynamicScannableLimits.getLimits();
			lowerLimit = limits.getLow();
			upperLimit = limits.getHigh();
		}
		if (newValue > upperLimit.doubleValue()) {
			throw new DeviceException("ControlPoint " + getName() + " is greater than upper limit " + upperLimit.doubleValue());
		}
		if (newValue < lowerLimit.doubleValue()) {
			throw new DeviceException("ControlPoint " + getName() + " is less than lower limit " + lowerLimit.doubleValue());
		}
		Async.execute(() -> {
			try {
				busy = true;
				controller.caputWait(theChannelSet, newValue);
			} catch (TimeoutException | CAException e) {
				logger.error("ControlPoint " + getName() + " exception in setValue", e);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				logger.error("ControlPoint " + getName() + " exception in setValue", e);
			} finally {
				busy = false;
			}
		});
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
		logger.debug("ControlPoint -  {} is initialised.", getName());
	}

	private class ValueMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			// extract the value and confirm its type
			DBR dbr = arg0.getDBR();
			double lastValue = latestValue;
			if (dbr.isDOUBLE()) {
				// update the latest value
				latestValue = ((DBR_Double) dbr).getDoubleValue()[0];
			} else if (dbr.isINT()) {
				latestValue = ((DBR_Int) dbr).getIntValue()[0];
			}

				// if the percentage change has been great enough, then inform
				// IObservers
				if (Math.abs((lastValue - latestValue) / lastValue) * 100.0 >= sensitivity) {
					notifyIObservers(this, latestValue);
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
		return busy;
	}

	public boolean isMonitorChannels() {
		return monitorChannels;
	}

	public void setMonitorChannels(boolean monitorChannels) {
		this.monitorChannels = monitorChannels;
	}

	public Double getUpperLimit() {
		return upperLimit;
	}

	public void setUpperLimit(Double upperLimit) {
		this.upperLimit = upperLimit;
	}

	public Double getLowerLimit() {
		return lowerLimit;
	}

	public void setLowerLimit(Double lowerLimit) {
		this.lowerLimit = lowerLimit;
	}



}
