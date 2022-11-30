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

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.factory.FactoryException;
import gda.jython.JythonServerFacade;
import gov.aps.jca.Channel;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.dbr.DBR_LABELS_Enum;
import gov.aps.jca.dbr.DBR_String;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

/**
 * This class is designed to support Oxford Cryostream 700 and Phenix Crystat .
 */
public class CryoController extends DeviceBase implements InitializationListener {
	private static final Logger logger = LoggerFactory.getLogger(CryoController.class);
	public final double MAX_RAMP_RATE = 360.0; // Kelvin/hour
	public final double MIN_RAMP_RATE = 1.0; // K/hour
	/** Start/restart the program, which is a control for the whole system */
	private Channel restart = null;

	/** Resume the program */
	private Channel resume = null;

	/** Pause the program */
	private Channel pause = null;

	/** Stop the program */
	private Channel stop = null;

	/** Cool temperature target */
	private Channel ctemp = null;

	/** Start a cool program */
	private Channel cool = null;

	/** Start a hold */
	private Channel hold = null;

	/** Start a purge or warm */
	private Channel purge = null;

	/** Start a ramp */
	private Channel ramp = null;

	/** Set ramp rate */
	private Channel rrate = null;

	/** Ramp target temperature */
	private Channel rtemp = null;

	/** Start a plateau */
	private Channel plat = null;

	/** Set plateau time */
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
	private Vector<String> phases = new Vector<>();
	private Vector<String> runmodes = new Vector<>();
	private AlarmListener al;
	private CurrentTempListener ctl;
	private ConnectionListener connlist;
	private String alarmStatus ="No Alarm";
	private double currtemp;
	private String connState = "Disabled";
	private String pvRoot = null;
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
		if (!isConfigured()) {
			if (getPvRoot() == null) {
				logger.error("Missing PV for {}", getName());
				throw new FactoryException("Missing PV for CryoController " + getName());
			}
			createChannelAccess(getPvRoot());
			channelManager.tryInitialize(100);
			try {
				connState = getDisable();
			} catch (DeviceException e) {
				logger.warn("Failed to get Hardware connection state in {} configure().", getName());
			}
			setConfigured(true);
		}
	}

	private void createChannelAccess(String pvRoot) throws FactoryException {
		try {
			restart		= channelManager.createChannel(pvRoot + ":RESTART", false);
			resume		= channelManager.createChannel(pvRoot + ":RESUME", false);
			pause		= channelManager.createChannel(pvRoot + ":PAUSE", false);
			stop		= channelManager.createChannel(pvRoot + ":STOP", false);
			ctemp		= channelManager.createChannel(pvRoot + ":CTEMP", false);
			cool		= channelManager.createChannel(pvRoot + ":COOL", false);
			hold		= channelManager.createChannel(pvRoot + ":HOLD", false);
			purge		= channelManager.createChannel(pvRoot + ":PURGE", false);
			ramp		= channelManager.createChannel(pvRoot + ":RAMP", false);
			rrate		= channelManager.createChannel(pvRoot + ":RRATE", false);
			rtemp		= channelManager.createChannel(pvRoot + ":RTEMP", false);
			plat		= channelManager.createChannel(pvRoot + ":PLAT", false);
			ptime		= channelManager.createChannel(pvRoot + ":PTIME", false);
			phase		= channelManager.createChannel(pvRoot + ":PHASE", false);
			runmode		= channelManager.createChannel(pvRoot + ":RUNMODE", false);
			ramprate	= channelManager.createChannel(pvRoot + ":RAMPRATE", false);
			targettemp	= channelManager.createChannel(pvRoot + ":TARGETTEMP",false);
			remaining	= channelManager.createChannel(pvRoot + ":REMAINING", false);
			runtime		= channelManager.createChannel(pvRoot + ":RUNTIME", false);
			temp		= channelManager.createChannel(pvRoot + ":TEMP", false);
			alarm		= channelManager.createChannel(pvRoot + ":ALARM.VALA", false);
			end			= channelManager.createChannel(pvRoot + ":END", false);
			disable		= channelManager.createChannel(pvRoot + ":DISABLE", connlist, false);
			// acknowledge that creation phase is completed
			channelManager.creationPhaseCompleted();
		} catch (Exception ex) {
			throw new FactoryException("failed to create required channels for " + getName() + " using " + pvRoot, ex);
		}
	}

	/**
	 * Switches the Cryostream on, executing the start-up phase or current Phase Table. It is also used to re-start the
	 * control program after it has been halted.
	 *
	 * @throws DeviceException
	 */
	public void start() throws DeviceException {
		try {
			controller.caput(restart, 1, 2);
		} catch (Exception e) {
			throw new DeviceException("Failed to start/restart the program.", e);
		}
	}

	/**
	 * Halt the Cryostream Plus, turn off the pump and all the heaters. The controller may then be safely switches off,
	 * or re-started by start().
	 *
	 * @throws DeviceException
	 */
	public void stop() throws DeviceException {
		try {
			controller.caput(stop, 1, 2);
		} catch (Exception e) {
			throw new DeviceException("Failed to stop the program.", e);
		}
	}

	/**
	 * @throws DeviceException
	 */
	public void resume() throws DeviceException {
		try {
			controller.caput(resume, 1, 2);
		} catch (Exception e) {
			throw new DeviceException("Failed to resume the program.", e);
		}
	}

	/**
	 * @throws DeviceException
	 */
	public void pause() throws DeviceException {
		try {
			controller.caput(pause, 1, 2);
		} catch (Exception e) {
			throw new DeviceException("Failed to pause the program.", e);
		}
	}

	/**
	 * @return phase
	 * @throws DeviceException
	 */
	public String getPhase() throws DeviceException {
		try {
			return phases.get(controller.cagetEnum(phase));
		} catch (Exception e) {
			throw new DeviceException("Failed to get current PHASE ID.", e);
		}
	}

	/**
	 * @return run mode
	 * @throws DeviceException
	 */
	public String getRunmode() throws DeviceException {
		try {
			return runmodes.get(controller.cagetEnum(runmode));
		} catch (Exception e) {
			throw new DeviceException("Failed to get current run mode.", e);
		}
	}

	/**
	 * @return ramp rate
	 * @throws DeviceException
	 */
	public double getRampRate() throws DeviceException {
		try {
			return controller.cagetDouble(ramprate);
		} catch (Exception e) {
			throw new DeviceException("Failed to get current ramp rate.", e);
		}
	}

	/**
	 * @return target temp
	 * @throws DeviceException
	 */
	public double getTargetTemp() throws DeviceException {
		try {
			return controller.cagetDouble(targettemp);
		} catch (Exception e) {
			throw new DeviceException("Failed to get current target temperature.", e);
		}
	}

	/**
	 * @return remaining
	 * @throws DeviceException
	 */
	public double getRemaining() throws DeviceException {
		try {
			return controller.cagetDouble(remaining);
		} catch (Exception e) {
			throw new DeviceException("Failed to get remaining time in current phase.", e);
		}
	}

	/**
	 * @return run time
	 * @throws DeviceException
	 */
	public double getRunTime() throws DeviceException {
		try {
			return controller.cagetDouble(runtime);
		} catch (Exception e) {
			throw new DeviceException("Failed to get run time since pump starts.", e);
		}
	}

	/**
	 * @return temp
	 * @throws DeviceException
	 */
	public double getTemp() throws DeviceException {
		try {
			return controller.cagetDouble(temp);
		} catch (Exception e) {
			throw new DeviceException("Failed to get current temperature.", e);
		}
	}

	/**
	 * @return alarm
	 * @throws DeviceException
	 */
	public String getAlarm() throws DeviceException {
		try {
			return controller.caget(alarm);
		} catch (Exception e) {
			throw new DeviceException("Failed to get alarm status.", e);
		}
	}

	/**
	 * @throws DeviceException
	 */
	public void cool() throws DeviceException {
		try {
			controller.caput(cool, 1, 2);
		} catch (Exception e) {
			throw new DeviceException("Failed to start a fast cool phase.", e);
		}
	}

	/**
	 * @param t
	 * @throws DeviceException
	 */
	public void setCoolTemp(double t) throws DeviceException {
		try {
			controller.caput(ctemp, t, 2);
		} catch (Exception e) {
			throw new DeviceException("Failed to set cool temperature.", e);
		}
	}

	/**
	 * @return cool temp
	 * @throws DeviceException
	 */
	public double getCoolTemp() throws DeviceException {
		try {
			return controller.cagetDouble(ctemp);
		} catch (Exception e) {
			throw new DeviceException("Failed to get cool temperature.", e);
		}
	}

	/**
	 * @throws DeviceException
	 */
	public void hold() throws DeviceException {
		try {
			controller.caput(hold, 1, 2);
		} catch (Exception e) {
			throw new DeviceException("Failed to start hold phase.", e);
		}
	}

	/**
	 * @throws DeviceException
	 */
	public void purge() throws DeviceException {
		try {
			controller.caput(purge, 1, 2);
		} catch (Exception e) {
			throw new DeviceException("Failed to start purge/warm phase.", e);
		}
	}

	/**
	 * @throws DeviceException
	 */
	public void ramp() throws DeviceException {
		try {
			controller.caput(ramp, 1, 2);
		} catch (Exception e) {
			throw new DeviceException("Failed to start a ramp phase.", e);
		}
	}

	/**
	 * @param K_hr
	 * @throws DeviceException
	 */
	public void setRampRate(double K_hr) throws DeviceException {
		try {
			controller.caput(rrate, K_hr, 2);
		} catch (Exception e) {
			throw new DeviceException("Failed to set the ramp rate in K/hr.", e);
		}
	}

	/**
	 * @param k
	 * @throws DeviceException
	 */
	public void setTargetTemp(double k) throws DeviceException {
		try {
			controller.caput(rtemp, k, 2);
		} catch (Exception e) {
			throw new DeviceException("Failed to set ramp Target Temperature.", e);
		}
	}

	/**
	 * @throws DeviceException
	 */
	public void plateau() throws DeviceException {
		try {
			controller.caput(plat, 1, 2);
		} catch (Exception e) {
			throw new DeviceException("Failed to start a plateau phase.", e);
		}
	}

	/**
	 * @param t
	 * @throws DeviceException
	 */
	public void setPlateauTime(double t) throws DeviceException {
		try {
			controller.caput(ptime, t, 2);
		} catch (Exception e) {
			throw new DeviceException("Failed to set time for the plateau.", e);
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
		} catch (Exception e) {
			throw new DeviceException("Failed to end.", e);
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
		} catch (Exception e) {
			throw new DeviceException("Failed to get phase ids.", e);
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
		} catch (Exception e) {
			throw new DeviceException("Failed to get run mode labels.", e);
		}
		return runmodeLabels;
	}

	@Override
	public void initializationCompleted() {
		try {
			for (String phaseName : getPhases()) {
				if (phaseName != null && !phaseName.isEmpty()) {
					phases.add(phaseName);
				}
			}
		} catch (DeviceException e) {
			logger.error("Failed to initialise phase IDs", e);
		}
		try {
			for (String runMode : getRunmodes()) {
				if (runMode != null && !runMode.isEmpty()) {
					runmodes.add(runMode);
				}
			}
		} catch (DeviceException e) {
			logger.error("Failed to initialise run mode labels.", e);
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

	public String getPvRoot() {
		return pvRoot;
	}

	public void setPvRoot(String pvRoot) {
		this.pvRoot = pvRoot;
	}

	/**
	 * Check if the hardware is connected in EPICS
	 * @return True or False
	 */
	public boolean isConnected() {
		return connState.equals("Enabled");
	}

	/**
	 * Sets EPICS hardware connection state
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
	 * Sets the EPICS hardware connection state: true - Disabled; false - Enabled
	 * @param bool
	 * @throws DeviceException
	 */
	public void setDisbale(boolean bool) throws DeviceException {
		try {
			if (bool)
				controller.caput(disable, 1, 2);
			else
				controller.caput(disable, 0, 2);
		} catch (Exception e) {
			throw new DeviceException("Failed to set DISABLE PV.", e);
		}
	}

	/**
	 * Gets the EPICS hardware connection state.
	 * @return Disabled or Enabled
	 * @throws DeviceException
	 */
	public String getDisable() throws DeviceException{
		try {
			return connState = controller.cagetEnum(disable)==0 ? "Enabled" : "Disabled";
		} catch (Exception e) {
			throw new DeviceException("Failed to get from DISABLE PV.", e);
		}
	}
}