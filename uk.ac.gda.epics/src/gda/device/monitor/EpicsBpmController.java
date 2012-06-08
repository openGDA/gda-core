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
import gda.epics.connection.EpicsController.MonitorType;
import gda.epics.connection.InitializationListener;
import gda.epics.interfaces.BpmType;
import gda.factory.FactoryException;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_CTRL_Double;
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

	private IntensityMonitorListener intensityMonitor;

	private XPosMonitorListener xPosMonitor;

	private YPosMonitorListener yPosMonitor;

	/**
	 * GDA device Name
	 */
	private String deviceName = null;

	private EpicsChannelManager channelManager;

	/**
	 * Constructor
	 */
	public EpicsBpmController() {
		channelManager = new EpicsChannelManager(this);
		intensityMonitor = new IntensityMonitorListener();
		xPosMonitor = new XPosMonitorListener();
		yPosMonitor = new YPosMonitorListener();
		
		setInputNames(new String[0]);
		setExtraNames(new String[]{ "intensityTotal", "xPosition", "yPosition" });
//		this.setOutputFormat(names);

		outputFormat = new String[inputNames.length + extraNames.length];
		
		for (int i = 0; i < outputFormat.length; i++) {
			outputFormat[i] = "%4.10f";
		}
		setOutputFormat(outputFormat);
		//completeInstantiation();
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
			channelManager.createChannel(intensityRec, intensityMonitor, MonitorType.CTRL, false);
			channelManager.createChannel(xPosRec, xPosMonitor, MonitorType.CTRL, false);
			channelManager.createChannel(yPosRec, yPosMonitor, MonitorType.CTRL, false);
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
		// TODO Auto-generated method stub

	}

	private class IntensityMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent mev) {
			DBR dbr = mev.getDBR();
			if (dbr.isDOUBLE()) {
				intensityTotal = ((DBR_CTRL_Double) dbr).getDoubleValue()[0];
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
				xPosition = ((DBR_CTRL_Double) dbr).getDoubleValue()[0];
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
				yPosition = ((DBR_CTRL_Double) dbr).getDoubleValue()[0];
				notifyIObservers(this, yPosition);
			} else {
				logger.error("Error: .RBV should return DOUBLE type value.");
			}
		}
	}

	/**
	 * @return intensity total
	 */
	public double getIntensityTotal() {
		return this.intensityTotal;
	}

	/**
	 * @return x position
	 */
	public double getXPosition() {
		return this.xPosition;
	}

	/**
	 * @return y position
	 */
	public double getYPosition() {
		return this.yPosition;
	}

}