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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.factory.FactoryException;
import gov.aps.jca.Channel;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

/**
 * This class is designed to support Oxford Cryostream 700 and Phenix Crystat .
 * <p>
 * Example bean definition in Spring
 * <pre>
 * {@code
 <bean id="lakeshore_controller" class="gda.device.temperature.EpicsLakeshore340Controller">
	<property name="pvName" value="ME01D-EA-TCTRL-01:"/>
	<property name="local" value="true"/>
	<property name="configureAtStartup" value="true"/>
</bean>
 * }
 */
public class EpicsLakeshore340Controller extends DeviceBase implements ILakeshoreController, InitializationListener {
	/**
	 *
	 */
	@SuppressWarnings("unused")
	private static final long serialVersionUID = 6189569607134958641L;

	/**
	 * the logger instance
	 */
	private static final Logger logger = LoggerFactory.getLogger(EpicsLakeshore340Controller.class);

	/**
	 * Channel 0 temp
	 */
	private Channel krdg0 = null;
	/**
	 * Channel 1 temp
	 */
	private Channel krdg1 = null;
	/**
	 * Channel 2 temp
	 */
	private Channel krdg2 = null;
	/**
	 * Channel 3 temp
	 */
	private Channel krdg3 = null;

	/**
	 * Channel to use for readback
	 */
	private int readbackChannel = 0;

	/**
	 * target temperature
	 */
	private Channel targettemp = null;
	/**
	 * hardware connection only used by monitor
	 */
	private Channel disable = null;

	private CurrentTempListener ctl;
	private ConnectionListener connlist;
	private double currtemp;
	private String connState = "Disabled";
	private String pvName;
	/**
	 * EPICS controller
	 */
	private EpicsController controller;

	/**
	 * EPICS Channel Manager
	 */
	private EpicsChannelManager channelManager;

	/**
	 * Constructor
	 */
	public EpicsLakeshore340Controller() {
		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager(this);
		ctl = new CurrentTempListener();
		connlist = new ConnectionListener();
	}

	/**
	 * Initialise the cryo controller object.Monitor
	 *
	 * @throws FactoryException
	 */
	@Override
	public void configure() throws FactoryException {
		if (!isConfigured()) {
			if (readbackChannel < 0 || readbackChannel > 3) {
				throw new FactoryException("Readback channel must be between 0 and 3 inclusive.");
			}
			if (getPvName() == null) {
				logger.error("Missing PV for {}", getName());
				throw new FactoryException("Missing PV for CryoController " + getName());
			}
			createChannelAccess(getPvName());
			channelManager.tryInitialize(100);
			try {
				connState=getDisable();
			} catch (DeviceException e) {
				logger.warn("Failed to get Hardware connection state in {} configure().", getName());
			}
			setConfigured(true);
		}// end of if (!configured)

	}

	@Override
	public void reconfigure() throws FactoryException {
		setConfigured(false);
		configure();
		super.reconfigure();
	}

	private void createChannelAccess(String pvName) throws FactoryException {
		try {
		targettemp = channelManager.createChannel(pvName+"SETP_S", false);
		krdg0 = channelManager.createChannel(pvName+"KRDG0", readbackChannel == 0 ? ctl : null, false);
		krdg1 = channelManager.createChannel(pvName+"KRDG1", readbackChannel == 1 ? ctl : null, false);
		krdg2 = channelManager.createChannel(pvName+"KRDG2", readbackChannel == 2 ? ctl : null, false);
		krdg3 = channelManager.createChannel(pvName+"KRDG3", readbackChannel == 3 ? ctl : null, false);
		disable = channelManager.createChannel(pvName+"DISABLE", connlist, false);
		// acknowledge that creation phase is completed
		channelManager.creationPhaseCompleted();
		} catch (Throwable th) {
			throw new FactoryException("failed to create reuqired channels for " + getName(), th);
		}
	}

	/**
	 * gets temperature ramp rate
	 *
	 * @return ramp rate
	 * @throws DeviceException
	 */
	// public double getRampRate() throws DeviceException {
	// try {
	// return controller.cagetDouble(ramprate);
	// } catch (Throwable e) {
	// throw new DeviceException("failed to get current ramp rate.", e);
	// }
	// }
	/**
	 * gets the target temperature
	 *
	 * @return target temp
	 * @throws DeviceException
	 */
	@Override
	public double getTargetTemp() throws DeviceException {
		if (connState.equals("Disabled")) {
			throw new IllegalStateException("The device " + getName() + " is not connected.");
		}
		try {
			return controller.cagetDouble(targettemp);
		} catch (Throwable e) {
			throw new DeviceException("failed to get current target temperature.", e);
		}
	}

	/**
	 * gets current temperature
	 *
	 * @return temp
	 * @throws DeviceException
	 */
	@Override
	public double getTemp() throws DeviceException {
		switch (readbackChannel) {
		case 0:
			return getChannel0Temp();
		case 1:
			return getChannel1Temp();
		case 2:
			return getChannel2Temp();
		case 3:
			return getChannel3Temp();
		default:
			throw new IllegalStateException("Unknown channel specified");
		}
	}

	/**
	 * gets channel 0 temperature
	 *
	 * @return channel 0 temperature
	 * @throws DeviceException
	 */
	@Override
	public double getChannel0Temp() throws DeviceException {
		if (connState.equals("Disabled")) {
			throw new IllegalStateException("The device " + getName() + " is not connected.");
		}
		try {
			return controller.cagetDouble(krdg0);
		} catch (Throwable e) {
			throw new DeviceException("failed to get channel 0 temperature.", e);
		}
	}

	/**
	 * gets channel 1 temperature
	 *
	 * @return channel 1 temperature
	 * @throws DeviceException
	 */
	@Override
	public double getChannel1Temp() throws DeviceException {
		if (connState.equals("Disabled")) {
			throw new IllegalStateException("The device " + getName() + " is not connected.");
		}
		try {
			return controller.cagetDouble(krdg1);
		} catch (Throwable e) {
			throw new DeviceException("failed to get channel 1 temperature.", e);
		}
	}

	/**
	 * gets channel 2 temperature
	 *
	 * @return channel 2 temperature
	 * @throws DeviceException
	 */
	@Override
	public double getChannel2Temp() throws DeviceException {
		if (connState.equals("Disabled")) {
			throw new IllegalStateException("The device " + getName() + " is not connected.");
		}
		try {
			return controller.cagetDouble(krdg2);
		} catch (Throwable e) {
			throw new DeviceException("failed to get channel 2 temperature.", e);
		}
	}

	/**
	 * gets channel 3 temperature
	 *
	 * @return channel 3 temperature
	 * @throws DeviceException
	 */
	@Override
	public double getChannel3Temp() throws DeviceException {
		if (connState.equals("Disabled")) {
			throw new IllegalStateException("The device " + getName() + " is not connected.");
		}
		try {
			return controller.cagetDouble(krdg3);
		} catch (Throwable e) {
			throw new DeviceException("failed to get channel 3 temperature.", e);
		}
	}

	/**
	 * set ramp rate
	 *
	 * @param K_hr
	 * @throws DeviceException
	 */
	// public void setRampRate(double K_min) throws DeviceException {
	// try {
	// controller.caput(rrate, K_min, 2);
	// } catch (Throwable e) {
	// throw new DeviceException("failed to set the ramp rate in K/hr.", e);
	// }
	// }
	/**
	 * @param k
	 * @throws DeviceException
	 */
	@Override
	public void setTargetTemp(double k) throws DeviceException {
		if (connState.equals("Disabled")) {
			throw new IllegalStateException("The device " + getName() + " is not connected.");
		}
		try {
			controller.caput(targettemp, k, 20);
		} catch (Throwable e) {
			throw new DeviceException("failed to set Target Temperature.", e);
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
	 * monitors the current temperature
	 *
	 * @author fy65
	 */
	public class CurrentTempListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr.isDOUBLE()) {
				currtemp = ((DBR_Double) dbr).getDoubleValue()[0];
			}
			notifyIObservers(this, currtemp);
		}
	}

	/**
	 * Monitors the Hardware connection via EPICS
	 *
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
	 * returns the Hardware connection status in EPICS interface
	 *
	 * @return Disabled or Enabled
	 */
	@Override
	public String getConnectionState() {
		return connState;
	}

	/**
	 * check if the hardware is connected in EPICS
	 * @return True or False
	 */
	@Override
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

	@Override
	public int getReadbackChannel() {
		return readbackChannel;
	}

	@Override
	public void setReadbackChannel(int readbackChannel) {
		this.readbackChannel = readbackChannel;
		try {
			if (isConfigured()) {
				reconfigure();
			}
		} catch (FactoryException e) {
			logger.error("Re-configure {} failed after changing readback channel", getName(), e);
		}
	}

	public String getPvName() {
		return pvName;
	}

	public void setPvName(String pvName) {
		this.pvName = pvName;
	}
}
