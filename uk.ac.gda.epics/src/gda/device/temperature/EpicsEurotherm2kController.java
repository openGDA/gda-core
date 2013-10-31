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

package gda.device.temperature;

import gda.configuration.epics.ConfigurationNotFoundException;
import gda.configuration.epics.Configurator;
import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.epics.interfaces.Eurotherm2kType;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gov.aps.jca.Channel;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.dbr.DBR_LABELS_Enum;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is designed to support Cyberstar Blower, MRI Furnace, and Stoe Furnace.
 */
public class EpicsEurotherm2kController extends DeviceBase implements Configurable, Findable, InitializationListener {

	/**
	 * the logger instance
	 */
	private static final Logger logger = LoggerFactory.getLogger(EpicsEurotherm2kController.class);
	/**
	 * maximum temperature ramp rate allowed
	 */
	public double maxRampRate = 1.2; // in C/sec, doc:5C/minute
	/**
	 * minimum ramp rate
	 */
	public double minRampRate = 0.01; // in C/sec, doc:5C/minute
	/**
	 * maximum output power allowed
	 */
	public double maxOutput = 35.0; // % percent

	/**
	 * the target temperature
	 */
	private Channel setpoint = null;
	/**
	 * the ramp rate
	 */
	private Channel ramprate = null;
	/**
	 * the output power
	 */
	private Channel output = null;
	/**
	 * the set point read back value
	 */
	private Channel setpointrbv = null;
	/**
	 * the ramp rate read back value
	 */
	private Channel rampraterbv = null;
	/**
	 * the output read back value
	 */
	private Channel outputrbv = null;
	/**
	 * the current temperature
	 */
	private Channel temp = null;

	/**
	 * PID parameter - proportional
	 */
	private Channel p = null;
	/**
	 * PID parameter - integral
	 */
	private Channel i = null;
	/**
	 * PID parameter - differential
	 */
	private Channel d = null;
	/**
	 * proportional read back value
	 */
	private Channel prbv = null;
	/**
	 * integral read back value
	 */
	private Channel irbv = null;
	/**
	 * differential read back value
	 */
	private Channel drbv = null;
	/**
	 * error message
	 */
	private Channel error = null;

	private ErrorListener el;

	private TempListener ctl;
	private double currtemp;

	/**
	 * GDA device Name
	 */
	private String deviceName = null;
	/**
	 * EPICS controller
	 */
	private EpicsController controller;

	/**
	 * EPICS Channel Manager
	 */
	private EpicsChannelManager channelManager;

	private ConnectionListener cl;

	private Channel disable = null;
	private String connState = "Disabled";


	/**
	 * Constructor
	 */
	public EpicsEurotherm2kController() {
		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager(this);
		ctl = new TempListener();
		el = new ErrorListener();
		cl = new ConnectionListener();
	}

	/**
	 * Initialise the cryo controller object.
	 * 
	 * @throws FactoryException
	 */
	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			// EPICS interface version 2 for phase II beamlines.
			if (getDeviceName() != null) {
				Eurotherm2kType config;
				try {
					config = Configurator.getConfiguration(getDeviceName(), gda.epics.interfaces.Eurotherm2kType.class);
					try {
						createChannelAccess(config);
						channelManager.tryInitialize(100);
					} catch (FactoryException e) {
						logger.warn("{}: this device is not available on startup and need to be configured later before use.", e.getMessage());
						throw new FactoryException(e.getMessage() + ": This device is not available on startup and need to be configured later before use.");
					}
				} catch (ConfigurationNotFoundException e) {
					logger.error("Can NOT find EPICS XML configuration for EpicsEurotherm2kController " + getDeviceName(), e);
					throw new FactoryException("Missing EPICS XML configuration for EpicsEurotherm2kController :  "
							+ getDeviceName());
				}
			}
			// Nothing specified in Server XML file
			else {
				logger.error("Missing EPICS configuration for {}", getName());
				throw new FactoryException("Missing EPICS configuration for EpicsEurotherm2kController " + getName());
			}
			try {
				connState=getDisable();
			} catch (DeviceException e) {
				logger.warn("Failed to get Hardware connection state in {} configure().", getName());
			}
			configured = true;
		}// end of if (!configured)
	}

	@Override
	public void reconfigure() throws FactoryException {
		if (!configured)
			configure();
	}
	/**
	 * create channel access implementing phase II beamline EPICS interfaces.
	 * 
	 * @param config
	 * @throws FactoryException
	 */
	private void createChannelAccess(Eurotherm2kType config) throws FactoryException {
		try {
			setpoint = channelManager.createChannel(config.getSP().getPv(), false);
			setpointrbv = channelManager.createChannel(config.getSPRBV().getPv(), false);
			ramprate = channelManager.createChannel(config.getRR().getPv(), null, false);
			rampraterbv = channelManager.createChannel(config.getRRRBV().getPv(), false);
			output = channelManager.createChannel(config.getO().getPv(), false);
			outputrbv = channelManager.createChannel(config.getORBV().getPv(), false);
			temp = channelManager.createChannel(config.getPVRBV().getPv(), ctl, false);
			p = channelManager.createChannel(config.getP().getPv(), null, false);
			prbv = channelManager.createChannel(config.getPRBV().getPv(), false);
			i = channelManager.createChannel(config.getI().getPv(), null, false);
			irbv = channelManager.createChannel(config.getIRBV().getPv(), false);
			d = channelManager.createChannel(config.getD().getPv(), null, false);
			drbv = channelManager.createChannel(config.getDRBV().getPv(), false);
			error = channelManager.createChannel(config.getERR().getPv(), el, true);
			disable=channelManager.createChannel(config.getDISABLE().getPv(), cl, true);

			// acknowledge that creation phase is completed
			channelManager.creationPhaseCompleted();
		} catch (Throwable th) {
			configured = false;
			throw new FactoryException("failed to create reuqired connections for " + getName(), th);
		}
	}

	/**
	 * sets the target temperature and start ramp to it, ramp rate must be set first.
	 * 
	 * @param temp
	 * @throws DeviceException
	 */
	public void setTargetTemperature(double temp) throws DeviceException {
		// if (temp > MAX_TEMPERATURE) {
		// throw new IllegalArgumentException("Target temperature can not exceed " + MAX_TEMPERATURE + " degree");
		// }
		try {
			controller.caput(setpoint, temp, 2);
		} catch (Throwable e) {
			throw new DeviceException("failed to set target temperature.", e);
		}
	}

	/**
	 * gets the target temperature
	 * 
	 * @return the set point
	 * @throws DeviceException
	 */
	public double getTargetTemperature() throws DeviceException {
		try {
			return controller.cagetDouble(setpointrbv);
		} catch (Throwable e) {
			throw new DeviceException("failed to get tergate temperature.", e);
		}
	}

	/**
	 * sets the temperature ramp rate
	 * 
	 * @param rate
	 * @throws DeviceException
	 */
	public void setRampRate(double rate) throws DeviceException {
		if (rate > maxRampRate) {
			throw new IllegalArgumentException("Temperature ramp rate can not exceed " + maxRampRate + " degree");
		}
		try {
			controller.caput(ramprate, rate, 2);
		} catch (Throwable e) {
			throw new DeviceException("failed to set the temperature ramp rate.", e);
		}
	}

	/**
	 * gets the temperature ramp rate
	 * 
	 * @return ramp rate
	 * @throws DeviceException
	 */
	public double getRampRate() throws DeviceException {
		try {
			return controller.cagetDouble(rampraterbv);
		} catch (Throwable e) {
			throw new DeviceException("failed to get the temperature ramp rate.", e);
		}
	}

	/**
	 * sets the power output in percentage (max 35%)
	 * 
	 * @param output
	 * @throws DeviceException
	 */
	public void setOutput(double output) throws DeviceException {
		if (output > maxOutput) {
			throw new IllegalArgumentException("Power output can not exceed " + maxOutput + "%");
		}
		try {
			controller.caput(this.output, output, 2);
		} catch (Throwable e) {
			throw new DeviceException("failed to set the power output.", e);
		}
	}

	/**
	 * gets the power output in percentage
	 * 
	 * @return power output
	 * @throws DeviceException
	 */
	public double getOutput() throws DeviceException {
		try {
			return controller.cagetDouble(outputrbv);
		} catch (Throwable e) {
			throw new DeviceException("failed to get power output.", e);
		}
	}

	/**
	 * gets the current temperature
	 * 
	 * @return run mode
	 * @throws DeviceException
	 */
	public double getTemp() throws DeviceException {
		try {
			return controller.cagetDouble(temp);
		} catch (Throwable e) {
			throw new DeviceException("failed to get current temperature.", e);
		}
	}

	/**
	 * sets the PID proportional parameter P
	 * 
	 * @param p
	 * @throws DeviceException
	 */
	public void setProportional(double p) throws DeviceException {
		try {
			controller.caput(this.p, p, 2);
		} catch (Throwable e) {
			throw new DeviceException("failed to set PID proportional parameter.", e);
		}
	}

	/**
	 * gets the PID proportional parameter P
	 * 
	 * @return proportional
	 * @throws DeviceException
	 */
	public double getProportional() throws DeviceException {
		try {
			return controller.cagetDouble(prbv);
		} catch (Throwable e) {
			throw new DeviceException("failed to get PID proportional parameter.", e);
		}
	}

	/**
	 * sets the PID integral parameter P
	 * 
	 * @param integral
	 * @throws DeviceException
	 */
	public void setIntegral(double integral) throws DeviceException {
		try {
			controller.caput(this.i, integral, 2);
		} catch (Throwable e) {
			throw new DeviceException("failed to set PID integral parameter.", e);
		}
	}

	/**
	 * gets the PID integral parameter I public double getMaxRampRate() { return maxRampRate; } public void
	 * setMaxRampRate(double maxRampRate) { this.maxRampRate = maxRampRate; } public double getMinRampRate() { return
	 * minRampRate; } public void setMinRampRate(double minRampRate) { this.minRampRate = minRampRate; } public double
	 * getMaxOutput() { return maxOutput; } public void setMaxOutput(double maxOutput) { this.maxOutput = maxOutput; }
	 * 
	 * @return integral
	 * @throws DeviceException
	 */
	public double getIntegral() throws DeviceException {
		try {
			return controller.cagetDouble(irbv);
		} catch (Throwable e) {
			throw new DeviceException("failed to get PID integral parameter.", e);
		}
	}

	/**
	 * sets the PID differential parameter P
	 * 
	 * @param differential
	 * @throws DeviceException
	 */
	public void setDifferential(double differential) throws DeviceException {
		try {
			controller.caput(this.d, differential, 2);
		} catch (Throwable e) {
			throw new DeviceException("failed to set PID differential parameter.", e);
		}
	}

	/**
	 * gets the PID differential parameter D
	 * 
	 * @return remaining
	 * @throws DeviceException
	 */
	public double getDifferential() throws DeviceException {
		try {
			return controller.cagetDouble(drbv);
		} catch (Throwable e) {
			throw new DeviceException("failed to get PID differential parameter.", e);
		}
	}

	/**
	 * gets the error string
	 * 
	 * @return error string
	 * @throws DeviceException
	 */
	public String getError() throws DeviceException {
		try {
			return controller.caget(error);
		} catch (Throwable e) {
			throw new DeviceException("failed to get the error string.", e);
		}
	}

	/**
	 * clear the error string
	 * 
	 * @throws DeviceException
	 */
	public void clear() throws DeviceException {
		try {
			controller.caput(error, "", 2);
		} catch (Throwable e) {
			throw new DeviceException("failed to clear the error string.", e);
		}
	}

	@Override
	public void initializationCompleted() {

		if (connState.equals("Enabled")) {
			logger.info("{} is initialised.", getName());
		} else if (connState.equals("Disabled")) {
			logger.warn("{} is NOT connected to hardware.", getName());
		}

	}

	/**
	 * @author fy65
	 */
	public class ErrorListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			String errorString = "";
			if (dbr.isLABELS()) {
				errorString = ((DBR_LABELS_Enum) dbr).getLabels()[0];
				notifyIObservers(this, errorString);
			}
		}
	}


	/**
	 * @author fy65
	 */
	public class TempListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr.isDOUBLE()) {
				currtemp = ((DBR_Double) dbr).getDoubleValue()[0];
				notifyIObservers(this, currtemp);
			}
		}
	}
	/**
	 * @author fy65
	 */
	public class ConnectionListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr.isENUM()) {
				connState = ((DBR_Enum) dbr).getEnumValue()[0]==0 ? "Enabled" : "Disabled";
				if (connState.equals("Enabled")) {
					logger.info("{} - underlying EPICS is connected to hardware.", getName());
				} else if (connState.equals("Disabled")) {
					logger.warn("{} - underlying EPICS is not connected to hardware.", getName());
				} else {
					logger.error("{} error, report to Engineers.", getName());
				}
				notifyIObservers(this, connState);
			}
		}
	}
	/**
	 * @return deviceName
	 */
	public String getDeviceName() {
		return deviceName;
	}

	/**
	 * @param deviceName
	 */
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	/**
	 * returns the maximum ramp rate of this device.
	 * 
	 * @return maximum ramp rate
	 */
	public double getMaxRampRate() {
		return maxRampRate;
	}

	/**
	 * sets the maximum ramp rate of this device - used only by Castor
	 * 
	 * @param maxRampRate
	 */
	public void setMaxRampRate(double maxRampRate) {
		this.maxRampRate = maxRampRate;
	}

	/**
	 * returns the minimum ramp rate of the device
	 * 
	 * @return minimum ramp rate
	 */
	public double getMinRampRate() {
		return minRampRate;
	}

	/**
	 * @param minRampRate
	 */
	public void setMinRampRate(double minRampRate) {
		this.minRampRate = minRampRate;
	}

	/**
	 * returns the maximum output
	 * 
	 * @return maximum output
	 */
	public double getMaxOutput() {
		return maxOutput;
	}

	/**
	 * sets the maximum output
	 * 
	 * @param maxOutput
	 */
	public void setMaxOutput(double maxOutput) {
		this.maxOutput = maxOutput;
	}
	/**
	 * check if the hardware is connected in EPICS
	 * @return True or False
	 */
	public boolean isConnected() {
		return connState.equals("Enabled");
	}

	/**
	 * sets EPICS hardware connection state 
	 * @param connected
	 */
	public void setConnected(boolean connected) {
		try {
			setDisbale(connected);
		} catch (DeviceException e) {
			logger.error("{} - {}", getName(),e.getMessage());
		}
	}

	/**
	 * sets the EPICS hardware connection state: true - Disabled; false - Enabled
	 * @param bool
	 * @throws DeviceException
	 */
	public void setDisbale(boolean bool) throws DeviceException {
		try {
			if (bool) {
				controller.caput(disable, 1, 2);
			} else {
				controller.caput(disable, 0, 2);
			}
		} catch (Throwable e) {
		throw new DeviceException("failed to set DISABLE PV.", e);
		}
	}
	
	/**
	 * gets the EPICS hardware connection state.
	 * @return Disabled or Enabled
	 * @throws DeviceException
	 */
	public String getDisable() throws DeviceException{
		try {
			return connState = controller.cagetEnum(disable)==0 ? "Enabled" : "Disabled";
		} catch (Throwable e) {
			throw new DeviceException("failed to get from DISABLE PV.", e);
		}
	}
}
