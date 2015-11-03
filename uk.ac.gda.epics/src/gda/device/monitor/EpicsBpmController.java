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

import gda.configuration.epics.ConfigurationNotFoundException;
import gda.configuration.epics.Configurator;
import gda.device.DeviceException;
import gda.device.Monitor;
import gda.device.scannable.ScannableBase;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.epics.interfaces.BpmType;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EpicsBpmController Class
 */
public class EpicsBpmController extends ScannableBase implements Monitor, InitializationListener {

	private static final Logger logger = LoggerFactory.getLogger(EpicsBpmController.class);

	private volatile double intensityTotal = Double.NaN;

	private volatile double xPosition = Double.NaN;

	private volatile double yPosition = Double.NaN;

//	private String[] inputNames = { "intensityTotal", "xPosition", "yPosition" };

	private IntensityMonitorListener intensityMonitorListener;

	private XPosMonitorListener xPosMonitorListener;

	private YPosMonitorListener yPosMonitorListener;
	private boolean poll=false;
	private gov.aps.jca.Monitor iMonitor;
	private gov.aps.jca.Monitor xMonitor;
	private gov.aps.jca.Monitor yMonitor;

	/**
	 * GDA device Name
	 */
	private String deviceName = null;

	private EpicsChannelManager channelManager;
	private EpicsController controller;

	private Channel intensityCh;

	private Channel xPosCh;

	private Channel yPosCh;

	/**
	 * Constructor
	 */
	public EpicsBpmController() {
		channelManager = new EpicsChannelManager(this);
		controller=EpicsController.getInstance();
		intensityMonitorListener = new IntensityMonitorListener();
		xPosMonitorListener = new XPosMonitorListener();
		yPosMonitorListener = new YPosMonitorListener();
		
		setInputNames(new String[0]);
		setExtraNames(new String[]{ "intensityTotal", "xPosition", "yPosition" });

		outputFormat = new String[inputNames.length + extraNames.length];
		
		for (int i = 0; i < outputFormat.length; i++) {
			outputFormat[i] = "%4.10f";
		}
		setOutputFormat(outputFormat);
	}

	@Override
	public void configure() throws FactoryException {
		String intensityRec = null, xPosRec = null, yPosRec = null;
		if (!configured) {
			if (getDeviceName() != null) {
				BpmType bpmConfig;
				try {
					bpmConfig = Configurator.getConfiguration(getDeviceName(), gda.epics.interfaces.BpmType.class);
					intensityRec = bpmConfig.getINTENSITY().getPv();
					xPosRec = bpmConfig.getXPOS().getPv();
					yPosRec = bpmConfig.getYPOS().getPv();
					createChannelAccess(intensityRec, xPosRec, yPosRec);
					channelManager.tryInitialize(100);
				} catch (ConfigurationNotFoundException e) {
					logger.error("Can NOT find EPICS configuration for BPM " + getDeviceName(), e);
				}
			} else {
				logger.error("Missing EPICS interface configuration for the BPM " + getName());
				throw new FactoryException("Missing EPICS interface configuration for the BPM " + getName());
			}
			configured = true;
		}// end of if (!configured)
	}

	/**
	 * @return device name
	 */
	public String getDeviceName() {
		return deviceName;
	}

	/**
	 * @param name
	 */
	public void setDeviceName(String name) {
		this.deviceName = name;
	}

	private void createChannelAccess(String intensityRec, String xPosRec, String yPosRec) throws FactoryException {
		try {
			intensityCh = channelManager.createChannel(intensityRec, false);
			xPosCh = channelManager.createChannel(xPosRec, false);
			yPosCh = channelManager.createChannel(yPosRec, false);
			channelManager.creationPhaseCompleted();
		} catch (Throwable th) {
			// TODO take care of destruction
			throw new FactoryException("failed to connect to all channels", th);
		}
	}

	@Override
	public int getElementCount() throws DeviceException {
		// TODO Auto-generated method stub
		return 3;
	}

	@Override
	public String getUnit() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		// TODO Auto-generated method stub
	}

	@Override
	public Object getPosition() throws DeviceException {
		double[] value = new double[3];
		value[0] = getIntensityTotal();
		value[1] = getXPosition();
		value[2] = getYPosition();

		return value;
	}
	
	@Override
	public boolean isBusy() throws DeviceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void initializationCompleted() {
		if (isPoll()) {
			disablePoll();
		} else {
			enablePoll();
		}
		logger.info("{} initialisation completed", getName());
	}
	public void disablePoll() {
		setPoll(false);
		if (intensityCh != null && intensityMonitorListener != null) {
			try {
				iMonitor = intensityCh.addMonitor(DBRType.DOUBLE, 0, gov.aps.jca.Monitor.VALUE, intensityMonitorListener);
			} catch (IllegalStateException e) {
				logger.error("Fail to add monitor to channel " + intensityCh.getName(), e);
			} catch (CAException e) {
				logger.error("Fail to add monitor to channel " + intensityCh.getName(), e);
			}
		}
		if (xPosCh != null && xPosMonitorListener != null) {
			try {
				xMonitor = xPosCh.addMonitor(DBRType.DOUBLE, 0, gov.aps.jca.Monitor.VALUE, xPosMonitorListener);
			} catch (IllegalStateException e) {
				logger.error("Fail to add monitor to channel " + xPosCh.getName(), e);
			} catch (CAException e) {
				logger.error("Fail to add monitor to channel " + xPosCh.getName(), e);
			}
		}
		if (yPosCh != null && yPosMonitorListener != null) {
			try {
				yMonitor = yPosCh.addMonitor(DBRType.DOUBLE, 0, gov.aps.jca.Monitor.VALUE, yPosMonitorListener);
			} catch (IllegalStateException e) {
				logger.error("Fail to add monitor to channel " + yPosCh.getName(), e);
			} catch (CAException e) {
				logger.error("Fail to add monitor to channel " + yPosCh.getName(), e);
			}
		}
	}

	public void enablePoll() {
		setPoll(true);
		if (iMonitor != null && intensityMonitorListener != null) {
			iMonitor.removeMonitorListener(intensityMonitorListener);
		}
		if (xMonitor != null && xPosMonitorListener != null) {
			xMonitor.removeMonitorListener(xPosMonitorListener);
		}
		if (yMonitor != null && yPosMonitorListener != null) {
			yMonitor.removeMonitorListener(yPosMonitorListener);
		}
	}

	private class IntensityMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent mev) {
			DBR dbr = mev.getDBR();
			if (dbr.isDOUBLE()) {
				intensityTotal = ((DBR_Double) dbr).getDoubleValue()[0];
				notifyIObservers(this, intensityTotal);
			} else {
				logger.error("Error: .RBV should return DOUBLE type value.");
			}
		}
	}

	private class XPosMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent mev) {
			DBR dbr = mev.getDBR();
			if (dbr.isDOUBLE()) {
				xPosition = ((DBR_Double) dbr).getDoubleValue()[0];
				notifyIObservers(this, xPosition);
			} else {
				logger.error("Error: .RBV should return DOUBLE type value.");
			}
		}
	}

	private class YPosMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent mev) {
			DBR dbr = mev.getDBR();
			if (dbr.isDOUBLE()) {
				yPosition = ((DBR_Double) dbr).getDoubleValue()[0];
				notifyIObservers(this, yPosition);
			} else {
				logger.error("Error: .RBV should return DOUBLE type value.");
			}
		}
	}

	/**
	 * @return intensity total
	 * @throws DeviceException 
	 */
	public double getIntensityTotal() throws DeviceException {
		if (isPoll()) {
			try {
				return controller.cagetDouble(intensityCh);
			} catch (TimeoutException e) {
				logger.error("Timeout Exception on get intensity from" + intensityCh.getName(), e);
				throw new DeviceException("Timeout Exception on get intensity from" + intensityCh.getName(), e);
			} catch (CAException e) {
				logger.error("CAException on get intensity from" + intensityCh.getName(), e);
				throw new DeviceException("CAException on get intensity from" + intensityCh.getName(), e);
			} catch (InterruptedException e) {
				logger.error("Interrupted Exception on get intensity from" + intensityCh.getName(), e);
				throw new DeviceException("Interrupted Exception on get intensity from" + intensityCh.getName(), e);
			}
		}
		return this.intensityTotal;
	}

	/**
	 * @return x position
	 * @throws DeviceException 
	 */
	public double getXPosition() throws DeviceException {
		if (isPoll()) {
			try {
				return controller.cagetDouble(xPosCh);
			} catch (TimeoutException e) {
				logger.error("Timeout Exception on get intensity from" + xPosCh.getName(), e);
				throw new DeviceException("Timeout Exception on get intensity from" + xPosCh.getName(), e);
			} catch (CAException e) {
				logger.error("CAException on get intensity from" + xPosCh.getName(), e);
				throw new DeviceException("CAException on get intensity from" + xPosCh.getName(), e);
			} catch (InterruptedException e) {
				logger.error("Interrupted Exception on get intensity from" + xPosCh.getName(), e);
				throw new DeviceException("Interrupted Exception on get intensity from" + xPosCh.getName(), e);
			}
		}
		return this.xPosition;
	}

	/**
	 * @return y position
	 * @throws DeviceException 
	 */
	public double getYPosition() throws  DeviceException {
		if (isPoll()) {
			try {
				return controller.cagetDouble(yPosCh);
			} catch (TimeoutException e) {
				logger.error("Timeout Exception on get intensity from" + yPosCh.getName(), e);
				throw new DeviceException("Timeout Exception on get intensity from" + yPosCh.getName(), e);
			} catch (CAException e) {
				logger.error("CAException on get intensity from" + yPosCh.getName(), e);
				throw new DeviceException("CAException on get intensity from" + yPosCh.getName(), e);
			} catch (InterruptedException e) {
				logger.error("Interrupted Exception on get intensity from" + yPosCh.getName(), e);
				throw new DeviceException("Interrupted Exception on get intensity from" + yPosCh.getName(), e);
			}
		}
		return this.yPosition;
	}

	public boolean isPoll() {
		return poll;
	}

	public void setPoll(boolean poll) {
		this.poll = poll;
	}

}