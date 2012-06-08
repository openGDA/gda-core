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
import gda.configuration.epics.EpicsConfiguration;
import gda.device.DeviceException;
import gda.device.Monitor;
import gda.device.epicsdevice.EpicsInterfaceDevice;
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
public class EpicsBeamPositionMonitor extends MonitorBase implements Monitor, InitializationListener, EpicsInterfaceDevice{

	private static final Logger logger = LoggerFactory.getLogger(EpicsBeamPositionMonitor.class);

	private DoubleValueMonitorListener intensityMonitor, xMonitor, yMonitor;

	/**
	 * GDA device Name
	 */

	private EpicsChannelManager channelManager;

	private String deviceName;
	
	protected EpicsConfiguration epicsConfiguration;

	/**
	 * Constructor
	 */
	public EpicsBeamPositionMonitor() {
		setInputNames(new String[0]);
		channelManager = new EpicsChannelManager(this);
		intensityMonitor = new DoubleValueMonitorListener();
		xMonitor = new DoubleValueMonitorListener();
		yMonitor = new DoubleValueMonitorListener();
		
		
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
		setExtraNames(new String[]{ preName+"intensity", preName+"x", preName+"y", preName+"current1", preName+"current2", preName+"current3", preName+"current4"});

		outputFormat = new String[inputNames.length + extraNames.length];
		
		for (int i = 0; i < outputFormat.length; i++) {
			outputFormat[i] = "%4.10f";
		}
		setOutputFormat(outputFormat);
	}
	

	private void createChannelAccess(String intensityRec, String xPosRec, String yPosRec) throws FactoryException {
		try {
			channelManager.createChannel(intensityRec, intensityMonitor, MonitorType.CTRL, false);
			channelManager.createChannel(xPosRec, xMonitor, MonitorType.CTRL, false);
			channelManager.createChannel(yPosRec, yMonitor, MonitorType.CTRL, false);
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
				value = ((DBR_CTRL_Double) dbr).getDoubleValue()[0];
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
	 */
	public double getIntensity() {
		return intensityMonitor.getValue();
	}
	
	/**
	 * @return x position
	 */
	public double getXPosition() {
		return xMonitor.getValue();
	}
	
	/**
	 * @return y position
	 */
	public double getYPosition() {
		return yMonitor.getValue();
	}
	
	@Override
	public void initializationCompleted() {
		// TODO Auto-generated method stub
		
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
}