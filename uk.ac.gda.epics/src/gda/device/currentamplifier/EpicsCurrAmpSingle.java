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

package gda.device.currentamplifier;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.epics.ConfigurationNotFoundException;
import gda.configuration.epics.Configurator;
import gda.device.CurrentAmplifier;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.EpicsController.MonitorType;
import gda.epics.connection.InitializationListener;
import gda.epics.interfaces.CurrAmpSingleType;
import gda.factory.FactoryException;
import gda.jython.JythonServerFacade;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.Monitor;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.dbr.DBR_CTRL_Double;
import gov.aps.jca.dbr.DBR_CTRL_Enum;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

/**
 * EPICS template interface class for Single Channel Current Amplifier device.
 */
public class EpicsCurrAmpSingle extends CurrentAmplifierBase implements InitializationListener, Scannable,
		CurrentAmplifier {

	private static final Logger logger = LoggerFactory.getLogger(EpicsCurrAmpSingle.class);

	private String deviceName = null;
	private String pvName = null;
	private EpicsChannelManager channelManager;

	private EpicsController controller;

	// cached values
	private volatile double current = Double.NaN;
	private volatile String gain = "";
	private volatile Status status = Status.NORMAL;
	private volatile String mode = "";

	private Channel Ic = null;

	private Channel setGain = null;

	private Channel setAcDc = null;

	private Object lock = new Object();

	private CurrentMonitorListener iMonitor;
	private GainMonitorListener gainMonitor;
	private ModeMonitorListener modeMonitor;
	private boolean enableValueMonitoring = true;
	private boolean poll=false;
	private boolean acdcAvailable=true; // keep the original default behaviour if not set in Spring bean definition

	private Monitor monitor;

	/**
	 * Constructor
	 */
	public EpicsCurrAmpSingle() {

		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager(this);
		iMonitor = new CurrentMonitorListener();
		gainMonitor = new GainMonitorListener();
		modeMonitor = new ModeMonitorListener();

	}

	/**
	 * Configures the class with the PV information from the gda-interface.xml file. Vendor and model are available
	 * through EPICS but are currently not supported in GDA.
	 *
	 * @see gda.device.DeviceBase#configure()
	 */
	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			if (getDeviceName() != null) {
				CurrAmpSingleType currAmpConfig;
				try {
					currAmpConfig = Configurator.getConfiguration(getDeviceName(),
							gda.epics.interfaces.CurrAmpSingleType.class);
					createChannelAccess(currAmpConfig);
					channelManager.tryInitialize(100);
				} catch (ConfigurationNotFoundException e) {
					logger.error("Can NOT find EPICS configuration for current amplifier " + getDeviceName(), e);
				}
			} else if (getPvName() != null) {
				createChannelAccess(getPvName());
				channelManager.tryInitialize(100);
			} else {
				logger.error("Missing EPICS interface configuration for the current amplifier " + getName());
				throw new FactoryException("Missing EPICS interface configuration for the current amplifier "
						+ getName());
			}
			// to get scandatapoint haeder correctly named, for a single valued scannable, set input name to its
			// scannable name
			this.inputNames[0] = getName();
			this.outputFormat[0] = "%5.4f";
			configured = true;
		}// end of if (!configured)
	}

	@Override
	public String[] getGainPositions() throws DeviceException {
		String[] positionLabels = new String[gainPositions.size()];
		try {
			positionLabels = controller.cagetLabels(setGain);
		} catch (Exception e) {
			throw new DeviceException(getName() + " exception in getGainPositions", e);
		}
		return positionLabels;
	}

	@Override
	public String[] getModePositions() throws DeviceException {
		if (!acdcAvailable) throw new DeviceException("Mode channel is not available for device '"+getName()+"'");
		try {
			return controller.cagetLabels(setAcDc);
		} catch (Exception e) {
			throw new DeviceException(getName() + " exception in getGainPositions", e);
		}
	}
	/**
	 * returns a parsed list of gains available for this amplifier.
	 *
	 * @throws DeviceException
	 */
	@Override
	public void listGains() throws DeviceException {
		try {
			String[] gainsAvai = getGainPositions();
			for (String gain : gainsAvai) {
				JythonServerFacade f = JythonServerFacade.getInstance();
				f.print(gain);
			}
		} catch (DeviceException e) {
			throw new DeviceException(getName() + " : Cannot list all gain settings for this amplifer.");
		}
	}

	@Override
	public Status getStatus() throws DeviceException {
		return status;
	}

	@Override
	public void setGain(String position) throws DeviceException {
		if (gainPositions.contains(position)) {
			int target = gainPositions.indexOf(position);
			try {
				controller.caput(setGain, target, 2);
			} catch (Throwable th) {
				throw new DeviceException(setGain.getName() + " failed to moveTo " + position, th);
			}
			return;
		}
		// if get here then wrong position name supplied
		throw new DeviceException("Position called: " + position + " not found.");
	}

	@Override
	public String getGain() throws DeviceException {
		try {
			short test = controller.cagetEnum(setGain);
			return gainPositions.get(test);
		} catch (Throwable th) {
			throw new DeviceException("failed to get gain position from " + setGain.getName(), th);
		}
	}

	@Override
	public double getCurrent() throws DeviceException {
		try {
			return controller.cagetDouble(Ic);
		} catch (Throwable e) {
			throw new DeviceException(getName() + ": Cannot get current value from " + Ic.getName());
		}
	}

	@Override
	public String getMode() throws DeviceException {
		if (!acdcAvailable) throw new DeviceException("Mode channel is not available for device '"+getName()+"'");
		try {
			return controller.cagetLabels(setAcDc)[0];
		} catch (Throwable e) {
			throw new DeviceException(getName() + ": Cannot get amplifier mode from " + setAcDc.getName());
		}
	}

	@Override
	public void setMode(String mode) throws DeviceException {
		if (!acdcAvailable)
			throw new DeviceException("Mode channel is not available for device '" + getName() + "'");
		try {
			controller.caput(setAcDc, mode, 2);
		} catch (Throwable th) {
			throw new DeviceException(setAcDc.getName() + " failed to moveTo " + mode, th);
		}
	}

	@Override
	public void initializationCompleted() {
		// borrowed from EpicsPneumatic

		try {
			super.gainPositions.addAll(Arrays.asList(getGainPositions()));

			if (acdcAvailable) {
				super.modePositions.addAll(Arrays.asList(getModePositions()));
			}
			if (!isEnableValueMonitoring()) {
				disableValueMonitoring();
			} else {
				enableValueMonitoring();
			}
			logger.info(getName() + " - initialisation completed.");
		} catch (DeviceException e) {
			logger.warn(getName() + " - initialisation failed.");
		}
	}

	@Override
	public String toFormattedString() {
		try {

			// get the current position as an array of doubles
			Object position = getPosition();

			// if position is null then simply return the name
			if (position == null) {
				logger.warn("getPosition() from " + getName() + " returns NULL.");
				return getName() + " : NOT AVAILABLE";
			}

			// else build a string of formatted positions
			String output = getName() + " : " + String.format(outputFormat[0], position);
			// output += this.inputNames[0] + " : " + String.format(outputFormat[0], position) + " ";
			// output += this.inputNames[1] + " : " + String.format(outputFormat[1], getGain()) + " ";
			// output += this.inputNames[2] + " : " + String.format(outputFormat[2], getStatus()) + " ";
			// output += this.inputNames[3] + " : " + String.format(outputFormat[3], getMode()) + " ";

			return output.trim();

		} catch (Exception e) {
			logger.info(getName() + ": exception while getting position. " + e.getMessage() + "; " + e.getCause(), e);
			return getName();
		}
	}

	/**
	 * gets device name.
	 *
	 * @return device name
	 */
	public String getDeviceName() {
		return deviceName;
	}

	/**
	 * sets device name.
	 *
	 * @param name
	 */
	public void setDeviceName(String name) {
		this.deviceName = name;
	}

	/**
	 * creates channel access to amplifier
	 *
	 * @param currAmpConfig
	 * @throws FactoryException
	 */
	private void createChannelAccess(CurrAmpSingleType currAmpConfig) throws FactoryException {
		try {
			Ic = channelManager.createChannel(currAmpConfig.getI().getPv(), false);
			setGain = channelManager.createChannel(currAmpConfig.getSETGAIN().getPv(), gainMonitor, MonitorType.CTRL,
					false);
			if (acdcAvailable) {
				setAcDc = channelManager.createChannel(currAmpConfig.getSETACDC().getPv(), modeMonitor, MonitorType.CTRL,
					false);
			}
			channelManager.creationPhaseCompleted();

		} catch (Throwable th) {
			throw new FactoryException("failed to connect to all channels", th);
		}
	}

	private void createChannelAccess(String pvName2) throws FactoryException {
		try {
			Ic = channelManager.createChannel(pvName2 + ":I", false);
			setGain = channelManager.createChannel(pvName2 + ":GAIN", gainMonitor, MonitorType.CTRL, false);
			if (acdcAvailable) {
				setAcDc = channelManager.createChannel(pvName2 + ":ACDC", modeMonitor, MonitorType.CTRL, false);
			}
			channelManager.creationPhaseCompleted();

		} catch (Throwable th) {
			throw new FactoryException("failed to connect to all channels", th);
		}
	}

	/**
	 * Current monitor
	 */
	public class CurrentMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent mev) {
			DBR dbr = mev.getDBR();
			if (dbr.isDOUBLE()) {
				current = ((DBR_CTRL_Double) dbr).getDoubleValue()[0];
				notifyIObservers(this, new Double(current));
			} else {
				logger.error("Current should return DOUBLE type value.");
			}
		}
	}

	/**
	 * Overload status monitor.
	 *
	 * @author fy65
	 */
	public class OverloadMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent mev) {
			short value = -1;
			DBR dbr = mev.getDBR();
			if (dbr.isENUM()) {
				value = ((DBR_CTRL_Enum) dbr).getEnumValue()[0];
				if (value == 0) {
					synchronized (lock) {
						status = Status.NORMAL;
					}
				} else if (value == 1) {
					synchronized (lock) {
						status = Status.OVERLOAD;
					}
				}
				notifyIObservers(this, status);
			} else {
				logger.error("Overload status does not return Enum type.");
			}
		}
	}

	/**
	 * AC or DC mode monitor.
	 */
	public class ModeMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent mev) {
			DBR dbr = mev.getDBR();
			if (dbr.isENUM()) {
				int value = ((DBR_CTRL_Enum) dbr).getEnumValue()[0];
				notifyIObservers(this, mode=modePositions.get(value));
			} else {
				logger.error("Mode does not return Enum type.");
			}
		}
	}

	/**
	 * Gain monitor.
	 */
	public class GainMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent mev) {
			DBR dbr = mev.getDBR();
			if (dbr.isENUM()) {
				int value = ((DBR_CTRL_Enum) dbr).getEnumValue()[0];
				notifyIObservers(this, gain=gainPositions.get(value));
			} else {
				logger.error("Gain does not return Enum type.");
			}
		}
	}

	@Override
	public String getGainUnit() throws DeviceException {
		return null;
	}

	@Override
	public void setGainUnit(String unit) throws DeviceException {
	}

	public boolean isEnableValueMonitoring() {
		return enableValueMonitoring;
	}

	public void setEnableValueMonitoring(boolean enableValueMonitoring) {
		this.enableValueMonitoring = enableValueMonitoring;
		this.poll=!enableValueMonitoring;
	}

	public void enableValueMonitoring() {
		if (Ic != null && iMonitor != null) {
			try {
				monitor = Ic.addMonitor(DBRType.CTRL_DOUBLE, 0, Monitor.VALUE, iMonitor);
				setEnableValueMonitoring(true);
				setPoll(false);
			} catch (IllegalStateException e) {
				logger.error("Fail to add monitor to channel " + Ic.getName(), e);
			} catch (CAException e) {
				logger.error("Fail to add monitor to channel " + Ic.getName(), e);
			}
		}
	}

	public void disableValueMonitoring() {
		if (monitor != null && iMonitor != null) {
			monitor.removeMonitorListener(iMonitor);
			setEnableValueMonitoring(false);
			setPoll(true);
		}
	}

	public boolean isPoll() {
		return poll;
	}

	public void setPoll(boolean poll) {
		this.poll = poll;
		this.enableValueMonitoring=!poll;
	}

	public String getPvName() {
		return pvName;
	}

	public void setPvName(String pvName) {
		this.pvName = pvName;
	}

	public boolean isAcdcAvailable() {
		return acdcAvailable;
	}

	public void setAcdcAvailable(boolean acdcAvailable) {
		this.acdcAvailable = acdcAvailable;
	}

}