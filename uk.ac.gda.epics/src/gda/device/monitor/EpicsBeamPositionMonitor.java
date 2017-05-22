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

package gda.device.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.epics.ConfigurationNotFoundException;
import gda.configuration.epics.Configurator;
import gda.configuration.epics.EpicsConfiguration;
import gda.device.DeviceException;
import gda.device.Monitor;
import gda.device.epicsdevice.EpicsInterfaceDevice;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.epics.interfaces.BpmType;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

/**
 * EpicsBpmController Class
 *
 * @since 9.3.3
 * @fy65 9.3.3 @Deprecated please use {@link gda.device.monitor.EpicsBpmController} instead
 */
@Deprecated
public class EpicsBeamPositionMonitor extends MonitorBase implements Monitor, InitializationListener, EpicsInterfaceDevice{

	private static final Logger logger = LoggerFactory.getLogger(EpicsBeamPositionMonitor.class);

	private DoubleValueMonitorListener intensityMonitorListener, xMonitorListener, yMonitorListener;

	private EpicsChannelManager channelManager;

	/**
	 * GDA device Name
	 */
	private String deviceName;

	protected EpicsConfiguration epicsConfiguration;

	private boolean poll=false;

	private gov.aps.jca.Monitor intensityMonitor, xMonitor, yMonitor;

	private Channel intensityCh;
	private Channel xCh;
	private Channel yCh;
	private EpicsController controller;

	/**
	 * Constructor
	 */
	public EpicsBeamPositionMonitor() {
		setInputNames(new String[0]);
		controller=EpicsController.getInstance();
		channelManager = new EpicsChannelManager(this);
		intensityMonitorListener = new DoubleValueMonitorListener();
		xMonitorListener = new DoubleValueMonitorListener();
		yMonitorListener = new DoubleValueMonitorListener();
	}

	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			if (getDeviceName() != null) {
				BpmType bpmConfig;
				try {
					if (epicsConfiguration != null) {
						bpmConfig = epicsConfiguration.getConfiguration(getDeviceName(), gda.epics.interfaces.BpmType.class);
					} else {
						bpmConfig = Configurator.getConfiguration(getDeviceName(), gda.epics.interfaces.BpmType.class);
					}
					createChannelAccess(bpmConfig.getINTENSITY().getPv(), bpmConfig.getXPOS().getPv(), bpmConfig.getYPOS().getPv());
					channelManager.tryInitialize(100);
				} catch (ConfigurationNotFoundException e) {
					logger.error("Can NOT find EPICS configuration for BPM " + getDeviceName(), e);
				}
			} else {
				logger.error("Missing EPICS interface configuration for the BPM " + getName());
				throw new FactoryException("Missing EPICS interface configuration for the BPM " + getName());
			}
			this.modifyExtraNames();
			configured = true;
		}// end of if (!configured)
	}

	private void modifyExtraNames(){
		setInputNames(new String[0]);
		String preName=this.getName()+"_";
		setExtraNames(new String[]{ preName+"intensity", preName+"x", preName+"y"});

		outputFormat = new String[inputNames.length + extraNames.length];

		for (int i = 0; i < outputFormat.length; i++) {
			outputFormat[i] = "%4.10f";
		}
		setOutputFormat(outputFormat);
	}


	private void createChannelAccess(String intensityRec, String xPosRec, String yPosRec) throws FactoryException {
		try {
			intensityCh = channelManager.createChannel(intensityRec, false);
			xCh=channelManager.createChannel(xPosRec, false);
			yCh=channelManager.createChannel(yPosRec, false);
			channelManager.creationPhaseCompleted();
		} catch (Throwable th) {
			throw new FactoryException("failed to connect to all channels", th);
		}
	}

	@Override
	public Object getPosition() throws DeviceException {
		double[] value = new double[3];
		value[0] = getIntensity();
		value[1] = getXPosition();
		value[2] = getYPosition();

		return value;
	}


	private class DoubleValueMonitorListener implements MonitorListener {
		/**
		 *
		 * @see gov.aps.jca.event.MonitorListener#monitorChanged(gov.aps.jca.event.MonitorEvent)
		 */
		private double value;
		@Override
		public void monitorChanged(MonitorEvent mev) {
			DBR dbr = mev.getDBR();
			if (dbr.isDOUBLE()) {
				value = ((DBR_Double) dbr).getDoubleValue()[0];
				notifyIObservers(this, value);
			} else {
				logger.error("Error: .RBV should return DOUBLE type value.");
			}
		}

		public double getValue(){
			return value;
		}

	}

	/**
	 * @return intensity total
	 * @throws DeviceException
	 */
	public double getIntensity() throws DeviceException {
		if (isPoll()) {
			try {
				return controller.cagetDouble(intensityCh);
			} catch (Throwable e) {
				throw new DeviceException(getName() + ": Cannot get current value from " + intensityCh.getName(), e);
			}
		}
		return intensityMonitorListener.getValue();
	}

	/**
	 * @return x position
	 * @throws DeviceException
	 */
	public double getXPosition() throws DeviceException {
		if (isPoll()) {
			try {
				return controller.cagetDouble(xCh);
			} catch (Throwable e) {
				throw new DeviceException(getName() + ": Cannot get current value from " + xCh.getName(), e);
			}
		}
		return xMonitorListener.getValue();
	}

	/**
	 * @return y position
	 * @throws DeviceException
	 */
	public double getYPosition() throws DeviceException {
		if (isPoll()) {
			try {
				return controller.cagetDouble(yCh);
			} catch (Throwable e) {
				throw new DeviceException(getName() + ": Cannot get current value from " + yCh.getName(), e);
			}
		}
		return yMonitorListener.getValue();
	}

	@Override
	public void initializationCompleted() {
		if (isPoll()) {
			enablePoll();
		} else {
			disablePoll();
		}

	}

	public void disablePoll() {
		setPoll(false);
		if (intensityCh != null && intensityMonitorListener != null) {
			try {
				intensityMonitor = intensityCh.addMonitor(DBRType.DOUBLE, 0, gov.aps.jca.Monitor.VALUE, intensityMonitorListener);
			} catch (IllegalStateException e) {
				logger.error("Fail to add monitor to channel " + intensityCh.getName(), e);
			} catch (CAException e) {
				logger.error("Fail to add monitor to channel " + intensityCh.getName(), e);
			}
		}
		if (xCh != null && xMonitorListener != null) {
			try {
				xMonitor = xCh.addMonitor(DBRType.DOUBLE, 0, gov.aps.jca.Monitor.VALUE, xMonitorListener);
			} catch (IllegalStateException e) {
				logger.error("Fail to add monitor to channel " + xCh.getName(), e);
			} catch (CAException e) {
				logger.error("Fail to add monitor to channel " + xCh.getName(), e);
			}
		}
		if (yCh != null && yMonitorListener != null) {
			try {
				yMonitor = yCh.addMonitor(DBRType.DOUBLE, 0, gov.aps.jca.Monitor.VALUE, yMonitorListener);
			} catch (IllegalStateException e) {
				logger.error("Fail to add monitor to channel " + yCh.getName(), e);
			} catch (CAException e) {
				logger.error("Fail to add monitor to channel " + yCh.getName(), e);
			}
		}
	}

	public void enablePoll() {
		setPoll(true);
		if (intensityMonitor != null && intensityMonitorListener != null) {
			intensityMonitor.removeMonitorListener(intensityMonitorListener);
		}
		if (xMonitor != null && xMonitorListener != null) {
			xMonitor.removeMonitorListener(xMonitorListener);
		}
		if (yMonitor != null && yMonitorListener != null) {
			yMonitor.removeMonitorListener(yMonitorListener);
		}
	}

	@Override
	public String getDeviceName() {
		return deviceName;
	}

	@Override
	public void setDeviceName(String name) {
		this.deviceName = name;
	}

	/**
	 * Sets the EpicsConfiguration to use.
	 *
	 * @param epicsConfiguration the EpicsConfiguration
	 */
	public void setEpicsConfiguration(EpicsConfiguration epicsConfiguration) {
		this.epicsConfiguration = epicsConfiguration;
	}

	public boolean isPoll() {
		return poll;
	}

	public void setPoll(boolean poll) {
		this.poll = poll;
	}
}