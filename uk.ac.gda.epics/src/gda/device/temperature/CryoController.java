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
import gda.epics.interfaces.OXCS700Type;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.jython.JythonServerFacade;
import gov.aps.jca.Channel;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.dbr.DBR_LABELS_Enum;
import gov.aps.jca.dbr.DBR_String;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is designed to support Oxford Cryostream 700 and Phenix Crystat .
 */
public class CryoController extends DeviceBase implements Configurable, Findable, InitializationListener {
	private static final Logger logger = LoggerFactory.getLogger(CryoController.class);
	public final double MAX_RAMP_RATE = 360.0; // Kevin/hour
	public final double MIN_RAMP_RATE = 1.0; // K/hour
	/**
	 * start/restart the program, which is a control for the whole system
	 */
	private Channel restart = null;
	/**
	 * resume the program
	 */
	private Channel resume = null;
	/**
	 * pause the program
	 */
	private Channel pause = null;
	/**
	 * stop the program
	 */
	private Channel stop = null;
	/**
	 * cool temperature target
	 */
	private Channel ctemp = null;
	/**
	 * start a cool program
	 */
	private Channel cool = null;
	/**
	 * start a hold
	 */
	private Channel hold = null;
	/**
	 * start a purge or warm
	 */
	private Channel purge = null;
	/**
	 * start a ramp
	 */
	private Channel ramp = null;
	/**
	 * set ramp rate
	 */
	private Channel rrate = null;
	/**
	 * ramp target temperature
	 */
	private Channel rtemp = null;
	/**
	 * start a plateau
	 */
	private Channel plat = null;
	/**
	 * set plateau time
	 */
	private Channel ptime = null;
	private Channel temp = null;
	private Channel alarm = null;
	private Channel phase = null;
	private Channel runmode = null;
	private Channel ramprate = null;
	private Channel targettemp = null;
	private Channel remaining = null;
	private Channel runtime = null;
	private Channel end = null;
	private Channel disable=null;
	private Vector<String> phases = new Vector<String>();
	private Vector<String> runmodes = new Vector<String>();
	private AlarmListener al;
	private CurrentTempListener ctl;
	private ConnectionListener connlist;
	private String alarmStatus ="No Alarm";
	private double currtemp;
	private String connState = "Disabled";
	private String deviceName = null;
	private EpicsController controller;
	private EpicsChannelManager channelManager;

	public CryoController() {
		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager(this);
		al = new AlarmListener();
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
		if (!configured) {
			// EPICS interface version 2 for phase II beamlines.
			if (getDeviceName() != null) {
				OXCS700Type Config;
				try {
					Config = Configurator.getConfiguration(getDeviceName(), gda.epics.interfaces.OXCS700Type.class);
					try {
						createChannelAccess(Config);
						channelManager.tryInitialize(100);
					} catch (FactoryException e) {
						logger.warn("{}: this device is not available on startup and need to be configured later before use.", e.getMessage());
						throw new FactoryException(e.getMessage() + ": This device is not available on startup and need to be configured later before use.");
					}
				} catch (ConfigurationNotFoundException e) {
					logger.error("Can NOT find EPICS configuration for CryoController " + getDeviceName(), e);
					throw new FactoryException("Missing EPICS XML configuration for CryoController "
							+ getDeviceName());
				}
			}
			// Nothing specified in Server XML file
			else {
				logger.error("Missing EPICS configuration for {}", getName());
				throw new FactoryException("Missing EPICS interface configuration for CryoController " + getName());
			}
			try {
				connState=getDisable();
			} catch (DeviceException e) {
				logger.warn("Failed to get Hardware connection state in {} configure().", getName());
			}
			configured = true;
		}// end of if (!configured)

	}

	/**
	 * create channel access implementing phase II beamline EPICS interfaces.
	 * 
	 * @param config
	 * @throws FactoryException
	 */
	private void createChannelAccess(OXCS700Type config) throws FactoryException {
		try {
			restart = channelManager.createChannel(config.getRESTART().getPv(), false);
			resume = channelManager.createChannel(config.getRESUME().getPv(), false);
			pause = channelManager.createChannel(config.getPAUSE().getPv(), false);
			stop = channelManager.createChannel(config.getSTOP().getPv(), false);
			ctemp = channelManager.createChannel(config.getCTEMP().getPv(), false);
			cool = channelManager.createChannel(config.getCOOL().getPv(), false);
			hold = channelManager.createChannel(config.getHOLD().getPv(), false);
			purge = channelManager.createChannel(config.getPURGE().getPv(), false);
			ramp = channelManager.createChannel(config.getRAMP().getPv(), false);
			rrate = channelManager.createChannel(config.getRRATE().getPv(), false);
			rtemp = channelManager.createChannel(config.getRTEMP().getPv(), false);
			plat = channelManager.createChannel(config.getPLAT().getPv(), false);
			ptime = channelManager.createChannel(config.getPTIME().getPv(), false);
			phase = channelManager.createChannel(config.getPHASE().getPv(), false);
			runmode = channelManager.createChannel(config.getRUNMODE().getPv(), false);
			ramprate = channelManager.createChannel(config.getRAMPRATE().getPv(), false);
			targettemp = channelManager.createChannel(config.getTARGETTEMP().getPv(), false);
			remaining = channelManager.createChannel(config.getREMAINING().getPv(), false);
			runtime = channelManager.createChannel(config.getRUNTIME().getPv(), false);
			temp = channelManager.createChannel(config.getTEMP().getPv(), ctl, false);
			alarm = channelManager.createChannel(config.getALARM().getPv(), al, false);
			try {
				if (config.getEND().getPv() != null) {
					end = channelManager.createChannel(config.getEND().getPv(), false);
				}
			} catch (RuntimeException e) {
				if (e instanceof NullPointerException) {
					// do nothing - phenix stat do not have this channel.
				} else {
					throw e;
				}
			}
			disable=channelManager.createChannel(config.getDISABLE().getPv(), connlist, false);
			// acknowledge that creation phase is completed
			channelManager.creationPhaseCompleted();
		} catch (Throwable th) {
			throw new FactoryException("failed to create reuqired channels for " + getName(), th);
		}
	}

	/**
	 * switches the Cryostream on, executing the start-up phase or current Phase Table. It is also used to re-start the
	 * control program after it has been halted.
	 * 
	 * @throws DeviceException
	 */
	public void start() throws DeviceException {
		try {
			controller.caput(restart, 1, 2);
		} catch (Throwable e) {
			throw new DeviceException("failed to start/restart the program.", e);
		}
	}

	/**
	 * halt the Cryostream Plus, turn off the pump and all the heaters. The controller may then be safely switches off,
	 * or re-started by start().
	 * 
	 * @throws DeviceException
	 */
	public void stop() throws DeviceException {
		try {
			controller.caput(stop, 1, 2);
		} catch (Throwable e) {
			throw new DeviceException("failed to stop the program.", e);
		}
	}

	/**
	 * @throws DeviceException
	 */
	public void resume() throws DeviceException {
		try {
			controller.caput(resume, 1, 2);
		} catch (Throwable e) {
			throw new DeviceException("failed to resume the program.", e);
		}
	}

	/**
	 * @throws DeviceException
	 */
	public void pause() throws DeviceException {
		try {
			controller.caput(pause, 1, 2);
		} catch (Throwable e) {
			throw new DeviceException("failed to pause the program.", e);
		}
	}

	/**
	 * @return phase
	 * @throws DeviceException
	 */
	public String getPhase() throws DeviceException {
		try {
			return phases.get(controller.cagetEnum(phase));
		} catch (Throwable e) {
			throw new DeviceException("failed to get current PHASE ID.", e);
		}
	}

	/**
	 * @return run mode
	 * @throws DeviceException
	 */
	public String getRunmode() throws DeviceException {
		try {
			return runmodes.get(controller.cagetEnum(runmode));
		} catch (Throwable e) {
			throw new DeviceException("failed to get current run mode.", e);
		}
	}

	/**
	 * @return ramp rate
	 * @throws DeviceException
	 */
	public double getRampRate() throws DeviceException {
		try {
			return controller.cagetDouble(ramprate);
		} catch (Throwable e) {
			throw new DeviceException("failed to get current ramp rate.", e);
		}
	}

	/**
	 * @return target temp
	 * @throws DeviceException
	 */
	public double getTargetTemp() throws DeviceException {
		try {
			return controller.cagetDouble(targettemp);
		} catch (Throwable e) {
			throw new DeviceException("failed to get current target temperature.", e);
		}
	}

	/**
	 * @return remaining
	 * @throws DeviceException
	 */
	public double getRemaining() throws DeviceException {
		try {
			return controller.cagetDouble(remaining);
		} catch (Throwable e) {
			throw new DeviceException("failed to get remaining time in current phase.", e);
		}
	}

	/**
	 * @return run time
	 * @throws DeviceException
	 */
	public double getRunTime() throws DeviceException {
		try {
			return controller.cagetDouble(runtime);
		} catch (Throwable e) {
			throw new DeviceException("failed to get run time since pump starts.", e);
		}
	}

	/**
	 * @return temp
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
	 * @return alarm
	 * @throws DeviceException
	 */
	public String getAlarm() throws DeviceException {
		try {
			return controller.caget(alarm);
		} catch (Throwable e) {
			throw new DeviceException("failed to get alarm status.", e);
		}
	}

	/**
	 * @throws DeviceException
	 */
	public void cool() throws DeviceException {
		try {
			controller.caput(cool, 1, 2);
		} catch (Throwable e) {
			throw new DeviceException("failed to start a fast cool phase.", e);
		}
	}

	/**
	 * @param t
	 * @throws DeviceException
	 */
	public void setCoolTemp(double t) throws DeviceException {
		try {
			controller.caput(ctemp, t, 2);
		} catch (Throwable e) {
			throw new DeviceException("failed to set cool temperature.", e);
		}
	}

	/**
	 * @return cool temp
	 * @throws DeviceException
	 */
	public double getCoolTemp() throws DeviceException {
		try {
			return controller.cagetDouble(ctemp);
		} catch (Throwable e) {
			throw new DeviceException("failed to get cool temperature.", e);
		}
	}

	/**
	 * @throws DeviceException
	 */
	public void hold() throws DeviceException {
		try {
			controller.caput(hold, 1, 2);
		} catch (Throwable e) {
			throw new DeviceException("failed to start hold phase.", e);
		}
	}

	/**
	 * @throws DeviceException
	 */
	public void purge() throws DeviceException {
		try {
			controller.caput(purge, 1, 2);
		} catch (Throwable e) {
			throw new DeviceException("failed to start purge/warm phase.", e);
		}
	}

	/**
	 * @throws DeviceException
	 */
	public void ramp() throws DeviceException {
		try {
			controller.caput(ramp, 1, 2);
		} catch (Throwable e) {
			throw new DeviceException("failed to start a ramp phase.", e);
		}
	}

	/**
	 * @param K_hr
	 * @throws DeviceException
	 */
	public void setRampRate(double K_hr) throws DeviceException {
		try {
			controller.caput(rrate, K_hr, 2);
		} catch (Throwable e) {
			throw new DeviceException("failed to set the ramp rate in K/hr.", e);
		}
	}

	/**
	 * @param k
	 * @throws DeviceException
	 */
	public void setTargetTemp(double k) throws DeviceException {
		try {
			controller.caput(rtemp, k, 2);
		} catch (Throwable e) {
			throw new DeviceException("failed to set ramp Target Temperature.", e);
		}
	}

	/**
	 * @throws DeviceException
	 */
	public void plateau() throws DeviceException {
		try {
			controller.caput(plat, 1, 2);
		} catch (Throwable e) {
			throw new DeviceException("failed to start a plateau phase.", e);
		}
	}

	/**
	 * @param t
	 * @throws DeviceException
	 */
	public void setPlateauTime(double t) throws DeviceException {
		try {
			controller.caput(ptime, t, 2);
		} catch (Throwable e) {
			throw new DeviceException("failed to set time for the plateau.", e);
		}
	}

	/**
	 * @throws DeviceException
	 */
	public void end() throws DeviceException {
		try {
			if (end != null)
				controller.caput(end, 1, 2);
			else {
				JythonServerFacade.getInstance().print("end() is not available on this device: " + getName());
				logger.info("end() is not available on this device: {}.", getName());
			}
		} catch (Throwable e) {
			throw new DeviceException("failed to end.", e);
		}
	}

	/**
	 * @return String[] phases
	 * @throws DeviceException
	 */
	public String[] getPhases() throws DeviceException {
		String[] phaseLabels = new String[phases.size()];
		try {
			phaseLabels = controller.cagetLabels(phase);
		} catch (Throwable e) {
			throw new DeviceException("failed to get phase ids.", e);
		}
		return phaseLabels;
	}

	/**
	 * @return run modes
	 * @throws DeviceException
	 */
	public String[] getRunmodes() throws DeviceException {
		String[] runmodeLabels = new String[runmodes.size()];
		try {
			runmodeLabels = controller.cagetLabels(runmode);
		} catch (Throwable e) {
			throw new DeviceException("failed to get run mode labels.", e);
		}
		return runmodeLabels;
	}

	@Override
	public void initializationCompleted() {
		String[] position;
		try {
			position = getPhases();
			for (int i = 0; i < position.length; i++)
				if (position[i] != null || position[i] != "")
					phases.add(position[i]);
		} catch (DeviceException e) {
			logger.error("failed to initialise phase IDs", e);
			e.printStackTrace();
		}
		try {
			position = getRunmodes();
			for (int i = 0; i < position.length; i++)
				if (position[i] != null || position[i] != "")
					runmodes.add(position[i]);
		} catch (DeviceException e) {
			logger.error("failed to initialise run mode labels.", e);
			e.printStackTrace();
		}
			
		if (connState.equals("Enabled"))
			logger.info("{} is initialised.", getName());
		else if (connState.equals("Disabled"))
			logger.warn("{} is NOT connected to hardware.", getName());
	}

	/**
	 * @author fy65
	 */
	public class AlarmListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr.isSTRING())
				alarmStatus = ((DBR_String) dbr).getStringValue()[0];
			else if (dbr.isLABELS()) {
				alarmStatus = ((DBR_LABELS_Enum) dbr).getLabels()[0];
				if (alarmStatus != null)
					notifyIObservers(this, alarmStatus);
			}
		}
	}

	/**
	 * @author fy65
	 */
	public class CurrentTempListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr.isDOUBLE())
				currtemp = ((DBR_Double) dbr).getDoubleValue()[0];
			notifyIObservers(this, currtemp);
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
				if (connState.equals("Enabled"))
					logger.info("{} - underlying EPICS is connected to hardware.", getName());
				else if (connState.equals("Disabled"))
					logger.warn("{} - underlying EPICS is not connected to hardware.", getName());
				else
					logger.error("{} error, report to Engineers.", getName());
				notifyIObservers(this, connState);
			}
		}
	}
	
	/**
	 * @return Disabled or Enabled
	 */
	public String getConnectionState(){
		return connState;
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
			if (bool)
				controller.caput(disable, 1, 2);
			else
				controller.caput(disable, 0, 2);
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