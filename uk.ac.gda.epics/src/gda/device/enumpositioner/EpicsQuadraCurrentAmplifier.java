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

package gda.device.enumpositioner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.epics.ConfigurationNotFoundException;
import gda.configuration.epics.Configurator;
import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.epicsdevice.EpicsInterfaceDevice;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.EpicsController.MonitorType;
import gda.epics.connection.InitializationListener;
import gda.epics.interfaces.CurrAmpQuadType;
import gda.factory.FactoryException;
import gov.aps.jca.Channel;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_CTRL_Double;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

/**
 * EpicsCurrAmpQuadController Class
 *
 * @since 9.3.3
 * @fy65 9.3.3 @Deprecated please use {@link gda.device.enumpositioner.EpicsCurrAmpQuadController} instead
 */
@Deprecated
public class EpicsQuadraCurrentAmplifier extends EnumPositionerBase implements EpicsInterfaceDevice, EnumPositioner,
		InitializationListener {
	private static final Logger logger = LoggerFactory.getLogger(EpicsQuadraCurrentAmplifier.class);

	private EpicsChannelManager channelManager;

	private EpicsController controller;

	private Channel chRange = null;
	private Channel chRangeRBV = null;

	private CurrentMonitorListener monitorCurrent1, monitorCurrent2, monitorCurrent3, monitorCurrent4;

	private String deviceName;

	/**
	 * Constructor
	 */
	public EpicsQuadraCurrentAmplifier() {
		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager(this);
		monitorCurrent1 = new CurrentMonitorListener();
		monitorCurrent2 = new CurrentMonitorListener();
		monitorCurrent3 = new CurrentMonitorListener();
		monitorCurrent4 = new CurrentMonitorListener();
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
				CurrAmpQuadType currAmpConfig;
				try {
					currAmpConfig = Configurator.getConfiguration(getDeviceName(), gda.epics.interfaces.CurrAmpQuadType.class);
					createChannelAccess(currAmpConfig.getI1().getPv(), currAmpConfig.getI2().getPv(), currAmpConfig
							.getI3().getPv(), currAmpConfig.getI4().getPv(), currAmpConfig.getRANGE().getPv(), currAmpConfig.getRANGE_READBACK().getPv());
//					channelManager.tryInitialize(100);
				} catch (ConfigurationNotFoundException e) {
					logger.error("Can NOT find EPICS configuration for current amplifier quad " + getDeviceName(), e);
				}
			} else {
				logger.error("Missing EPICS interface configuration for the current amplifier quad " + getName());
				throw new FactoryException("Missing EPICS interface configuration for the current amplifier quad "
						+ getName());
			}
			configured = true;
		}
	}

	private void createChannelAccess(String pvCurrent1, String pvCurrent2, String pvCurrent3, String pvCurrent4,
			String pvRange, String pvRangeRBV) throws FactoryException {
		try {
			channelManager.createChannel(pvCurrent1, monitorCurrent1, MonitorType.CTRL, false);
			channelManager.createChannel(pvCurrent2, monitorCurrent2, MonitorType.CTRL, false);
			channelManager.createChannel(pvCurrent3, monitorCurrent3, MonitorType.CTRL, false);
			channelManager.createChannel(pvCurrent4, monitorCurrent4, MonitorType.CTRL, false);

			// chRange = channelManager.createChannel(pvRange, rangeMonitor, MonitorType.CTRL, false);
			if (pvRange !=null && !pvRange.isEmpty()) {
				chRange = channelManager.createChannel(pvRange, false);
			}

			if (pvRangeRBV !=null && !pvRangeRBV.isEmpty()) {
				chRangeRBV = channelManager.createChannel(pvRangeRBV, false);
				}

			channelManager.creationPhaseCompleted();
			channelManager.tryInitialize(100);


		} catch (Throwable th) {
			throw new FactoryException("failed to connect to all channels", th);
		}
	}

	@Override
	public void initializationCompleted() throws DeviceException {
		// borrowed from EpicsPneumatic
		String[] position = getPositions();
		for (int i = 0; i < position.length; i++) {
			if (position[i] != null || position[i] != "") {
				super.positions.add(position[i]);
			}
		}
	}

	/**
	 * gets the current status position of this device.
	 *
	 * @return position in String
	 * @throws DeviceException
	 */
	@Override
	public String getPosition() throws DeviceException {
		return getRangeValue();
	}

	@Override
	public String[] getPositions() throws DeviceException {
		try {
			return controller.cagetLabels(chRange);
		} catch (Exception e) {
			throw new DeviceException(getName() + " exception in getPositions",e);
		}
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		// find in the positionNames array the index of the string
		if (positions.contains(position.toString())) {
			int target = positions.indexOf(position.toString());
			try {
//				controller.caput(chRange, target, channelManager);
//				Thread.sleep(1000);
				controller.caput(chRange, target, 10);
			} catch (Throwable th) {
				throw new DeviceException(chRange.getName() + " failed to moveTo " + position.toString(), th);
			}

			return;
		}
		// if get here then wrong position name supplied
		throw new DeviceException("Position called: " + position.toString() + " not found.");

	}

	// need independent monitors for each current value
	private class CurrentMonitorListener implements MonitorListener {
		/**
		 * @see gov.aps.jca.event.MonitorListener#monitorChanged(gov.aps.jca.event.MonitorEvent)
		 */
		double current;

		@Override
		public void monitorChanged(MonitorEvent mev) {
			DBR dbr = mev.getDBR();
			if (dbr.isDOUBLE()) {
				current = ((DBR_CTRL_Double) dbr).getDoubleValue()[0];
				notifyIObservers(this, current);
			} else {
				logger.error("Error: .RBV should return DOUBLE type value.");
			}
		}

		public double getCurrent() {
			return current;
		}

	}

	/**
	 * @return range values
	 * @throws DeviceException
	 */
	public String[] getRangeValues() throws DeviceException {
		return getPositions();
	}

	/**
	 * @return range values
	 * @throws DeviceException
	 */
	public String getRangeValue() throws DeviceException {
		try {
			short test = controller.cagetEnum(chRangeRBV);
			return positions.get(test);
		} catch (Throwable th) {
			throw new DeviceException("failed to get position from " + chRangeRBV.getName(), th);
		}

	}

	/**
	 * @return four currents
	 */
	public double[] getCurrents() {
		double[] currents = new double[4];

		currents[0] = monitorCurrent1.getCurrent();
		currents[1] = monitorCurrent2.getCurrent();
		currents[2] = monitorCurrent3.getCurrent();
		currents[3] = monitorCurrent4.getCurrent();
		return currents;
	}

	@Override
	public String getDeviceName() {
		return deviceName;
	}

	@Override
	public void setDeviceName(String name) {
		this.deviceName = name;
	}
}
