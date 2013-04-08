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

import gda.configuration.epics.ConfigurationNotFoundException;
import gda.configuration.epics.Configurator;
import gda.device.ControlPoint;
import gda.device.DeviceException;
import gda.device.scannable.ScannableMotionBase;
import gda.device.scannable.ScannableUtils;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.epics.interfaces.SimplePvType;
import gda.epics.xml.EpicsRecord;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class EpicsControlPoint.
 */
public class EpicsControlPoint extends ScannableMotionBase implements ControlPoint, InitializationListener {
	
	private static final Logger logger = LoggerFactory.getLogger(EpicsControlPoint.class);

	private double sensitivity = 0.0;

	private String epicsRecordNameSetPoint;

	private String epicsRecordNameGetPoint;

	private String pvNameSetPoint;

	private String pvNameGetPoint;

	private String pvName;

	private double latestValue;

	private Channel theChannelSet;

	private Channel theChannelGet;

	private EpicsRecord epicsRecordSetPoint;

	private EpicsRecord epicsRecordGetPoint;

	private String recordNameSetPoint;

	private String recordNameGetPoint;

	private String deviceNameSetPoint;
	private String deviceNameGetPoint;

	private String deviceName;

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
		
		this.inputNames = new String[]{getName()};

		// String recordName = null;
		if (!configured) {
			// Original implementation of EPICS interface
			if ((getEpicsRecordNameSetPoint() != null) & (getEpicsRecordNameGetPoint() != null)) {

				if (getEpicsRecordNameSetPoint().equals(getEpicsRecordNameGetPoint())) {
					logger.warn("The EPICS record name set/set points may not be the same");
				}
				
				// Set point
				epicsRecordSetPoint = (EpicsRecord) Finder.getInstance().find(epicsRecordNameSetPoint);
				if (epicsRecordSetPoint != null) {
					recordNameSetPoint = epicsRecordSetPoint.getFullRecordName();
					try {
						theChannelSet = channelManager.createChannel(recordNameSetPoint, getMonitorListener(),
								false);
						// acknowledge that creation phase is completed
						channelManager.creationPhaseCompleted();
					} catch (Throwable th) {
						throw new FactoryException("failed to create Channel for Control Point Set", th);
					}
				}

				// Get point
				epicsRecordGetPoint = (EpicsRecord) Finder.getInstance().find(epicsRecordNameGetPoint);
				if (epicsRecordGetPoint != null) {
					recordNameGetPoint = epicsRecordGetPoint.getFullRecordName();
					try {
						theChannelGet = channelManager.createChannel(recordNameGetPoint, getMonitorListener(),
								false);
						// acknowledge that creation phase is completed
						channelManager.creationPhaseCompleted();
					} catch (Throwable th) {
						throw new FactoryException("failed to create Channel for Control Point Get", th);
					}
				}
			}

			// EPICS interface version 2 for phase II beamlines.
			// Single deviceName is used for both get and set point
			else if (getDeviceName() != null) {

				// Set point
				try {
					SimplePvType simplePv = Configurator.getConfiguration(getDeviceName(), gda.epics.interfaces.SimplePvType.class);
					recordNameSetPoint = simplePv.getRECORD().getPv();
					theChannelSet = channelManager.createChannel(recordNameSetPoint, getMonitorListener(), false);
					
					recordNameGetPoint = recordNameSetPoint;
					theChannelGet = theChannelSet;
					
					channelManager.creationPhaseCompleted();
				} catch (CAException e) {
					throw new FactoryException("failed to create Channel for Control Point", e);
				} catch (ConfigurationNotFoundException e) {
					throw new FactoryException("No SimplePv for Control Point", e);
				}
			}			
			// Two different deviceNames are used for get and set point respectively
			else if ((getDeviceNameSetPoint() != null) & (getDeviceNameGetPoint() != null)) {

				if (getDeviceNameSetPoint().equals(getDeviceNameGetPoint())) {
					logger.warn("The device name set point and device name get point may not be the same");
				}
				
				// Set point
				try {
					SimplePvType simplePvSet = Configurator.getConfiguration(getDeviceNameSetPoint(),
							gda.epics.interfaces.SimplePvType.class);
					recordNameSetPoint = simplePvSet.getRECORD().getPv();
					theChannelSet = channelManager.createChannel(recordNameSetPoint, getMonitorListener(), false);
					channelManager.creationPhaseCompleted();
				} catch (CAException e) {
					throw new FactoryException("failed to create Channel for Control Point Get", e);
				} catch (ConfigurationNotFoundException e) {
					throw new FactoryException("No SimplePv for Control Point Get", e);
				}

				// Get point
				try {
					SimplePvType simplePvGet = Configurator.getConfiguration(getDeviceNameGetPoint(),
							gda.epics.interfaces.SimplePvType.class);
					recordNameGetPoint = simplePvGet.getRECORD().getPv();
					theChannelGet = channelManager.createChannel(recordNameGetPoint, getMonitorListener() , false);
					channelManager.creationPhaseCompleted();
				} catch (CAException e) {
					throw new FactoryException("failed to create Channel for Control Point Get", e);
				} catch (ConfigurationNotFoundException e) {
					throw new FactoryException("No SimplePv for Control Point Get", e);
				}
			} else if (getPvName() != null) {
				try {
					recordNameSetPoint = getPvName();
					theChannelSet = channelManager.createChannel(recordNameSetPoint, getMonitorListener(), false);
					channelManager.creationPhaseCompleted();
					
					recordNameGetPoint = recordNameSetPoint;
					theChannelGet = theChannelSet;
				} catch (CAException e) {
					throw new FactoryException("failed to create Channel for Control Point Set", e);
				}
			} else if ((getPvNameSetPoint() != null) & (getPvNameGetPoint() != null)) {

				if (getPvNameSetPoint().equals(getPvNameGetPoint())) {
					logger.warn("The PV name set point and PV name get point may not be the same");
				}
				
				try {
					recordNameSetPoint = getPvNameSetPoint();
					theChannelSet = channelManager.createChannel(recordNameSetPoint, getMonitorListener(), false);
					channelManager.creationPhaseCompleted();
				} catch (CAException e) {
					throw new FactoryException("failed to create Channel for Control Point Set", e);
				}
				try {
					recordNameGetPoint = getPvNameGetPoint();
					theChannelGet = channelManager.createChannel(recordNameGetPoint, getMonitorListener(), false);
					channelManager.creationPhaseCompleted();
				} catch (CAException e) {
					throw new FactoryException("failed to create Channel for Control Point Get", e);
				}
			} else {
				logger.error("Control point not properly specified", getName());
				throw new FactoryException("Control point not properly specified for the control point " + getName());
			}
		}

		configured = true;
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

	/**
	 * Gets the epics record name set point.
	 * 
	 * @return The epics record name set point
	 */
	public String getEpicsRecordNameSetPoint() {
		return epicsRecordNameSetPoint;
	}

	/**
	 * Sets the epics record name set point.
	 * 
	 * @param epicsRecordNameSetPoint
	 *            The epics record name set point
	 */
	public void setEpicsRecordNameSetPoint(String epicsRecordNameSetPoint) {
		this.epicsRecordNameSetPoint = epicsRecordNameSetPoint;
	}

	/**
	 * gets the short or EPICS-GDA shared name of the device
	 * 
	 * @return device name
	 */
	public String getDeviceName(){
		return deviceName;
	}
	
	/**
	 * sets the short or EPICS-GDA shared name for this device
	 * 
	 * @param deviceName
	 */
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	
	/**
	 * gets the short or EPICS-GDA shared name of the device
	 * 
	 * @return device name
	 */
	public String getDeviceNameSetPoint() {
		return deviceNameSetPoint;
	}

	/**
	 * sets the short or EPICS-GDA shared name for this device
	 * 
	 * @param deviceName
	 */
	public void setDeviceNameSetPoint(String deviceName) {
		this.deviceNameSetPoint = deviceName;
	}

	/**
	 * Gets the epics record name get point.
	 * 
	 * @return The epics record name get point
	 */
	public String getEpicsRecordNameGetPoint() {
		return epicsRecordNameGetPoint;
	}

	/**
	 * Sets the epics record name get point.
	 * 
	 * @param epicsRecordNameGetPoint
	 *            The epics record name get point
	 */
	public void setEpicsRecordNameGetPoint(String epicsRecordNameGetPoint) {
		this.epicsRecordNameGetPoint = epicsRecordNameGetPoint;
	}

	/**
	 * gets the short or EPICS-GDA shared name of the device
	 * 
	 * @return device name
	 */
	public String getDeviceNameGetPoint() {
		return deviceNameGetPoint;
	}

	/**
	 * sets the short or EPICS-GDA shared name for this device
	 * 
	 * @param deviceName
	 */
	public void setDeviceNameGetPoint(String deviceName) {
		this.deviceNameGetPoint = deviceName;
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
