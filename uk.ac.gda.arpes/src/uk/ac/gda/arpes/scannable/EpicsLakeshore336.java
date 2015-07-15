/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.arpes.scannable;

import java.util.HashMap;
import java.util.Map;

import gda.device.DeviceException;
import gda.device.detector.areadetector.IPVProvider;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.ScannableUtils;
import gda.epics.connection.EpicsController;
import gda.factory.FactoryException;
import gov.aps.jca.Channel;

public class EpicsLakeshore336 extends ScannableBase {

	private final EpicsController EPICS_CONTROLLER = EpicsController.getInstance();
	private String basePVName = null;
	private IPVProvider pvProvider;

	public static final String CH_TEMP = "KRDG%d";
	public static final String LOOP_DEMAND = "SETP_S%d";
	public static final String LOOP_DEMAND_RBV = "SETP%d";
	public static final String LOOP_OUTPUT = "HTR%d";
	public static final String LOOP_HEATERRANGE = "RANGE_S%d";
	public static final String LOOP_HEATERRANGE_RBV = "RANGE%d";
	public static final String LOOP_RAMP = "RAMP_S%d";
	public static final String LOOP_RAMP_RBV = "RAMP%d";
	public static final String LOOP_RAMP_ENABLE = "RAMPST_S%d";
	public static final String LOOP_RAMP_ENABLE_RBV = "RAMPST%d";
	public static final String LOOP_INPUT = "OMINPUT_S%d";
	public static final String LOOP_INPUT_RBV = "OMINPUT%d";
	public static final String LOOP_MANUAL_OUT = "MOUT_S%d";
	public static final String LOOP_MANUAL_OUT_RBV = "MOUT%d";
	public static final String LOOP_P = "P_S%d";
	public static final String LOOP_P_RBV = "P%d";
	public static final String LOOP_I = "I_S%d";
	public static final String LOOP_I_RBV = "I%d";
	public static final String LOOP_D = "D_S%d";
	public static final String LOOP_D_RBV = "D%d";

	double tolerance = 0.05;

	public double getTolerance() {
		return tolerance;
	}

	public void setTolerance(double tolerance) {
		this.tolerance = tolerance;
	}

	public EpicsLakeshore336() {
		setInputNames(new String[] { "demand" });
		setExtraNames(new String[] { "cryostat", "sample", "shield", "heater", "heatersetting" });
		setOutputFormat(new String[] { "%5.5g", "%5.5g", "%5.5g", "%5.5g", "%5.5g", "%1.0f" });
	}

	/**
	 * Map that stores the channel against the PV name
	 */
	private Map<String, Channel> channelMap = new HashMap<String, Channel>();

	private Channel getChannel(String pvPostFix, int number) throws Exception {
		return getChannel(String.format(pvPostFix, number));
	}

	private Channel getChannel(String pvPostFix) throws Exception {
		String fullPvName;
		if (pvProvider != null) {
			fullPvName = pvProvider.getPV(pvPostFix);
		} else {
			fullPvName = basePVName + pvPostFix;
		}
		Channel channel = channelMap.get(fullPvName);
		if (channel == null) {
			channel = EPICS_CONTROLLER.createChannel(fullPvName);
			channelMap.put(fullPvName, channel);
		}
		return channel;
	}

	public IPVProvider getPvProvider() {
		return pvProvider;
	}

	public void setPvProvider(IPVProvider pvProvider) {
		this.pvProvider = pvProvider;
	}

	public String getBasePVName() {
		return basePVName;
	}

	public void setBasePVName(String basePVName) {
		this.basePVName = basePVName;
	}

	@Override
	public void configure() throws FactoryException {
		super.configure();
	}

	/**
	 * We assume the PID control is tuned reasonably well so we don't have to deal with overshooting or ringing
	 */
	@Override
	public boolean isBusy() throws DeviceException {
		int activeLoop = getActiveLoop();
		if (activeLoop == 0)
			return false;
		try {
			if (EPICS_CONTROLLER.cagetInt(getChannel(LOOP_HEATERRANGE_RBV, activeLoop)) == 0)
				return false;
			if (getDemandTemperature() == 0.0) // Special setpoint of 0 means never busy
				return false;
		} catch (Exception e) {
			throw new DeviceException("Error reading from Lakeshore 336 Temperature Controller device", e);
		}
		// If ramping is enabled need to check if the ramping currentSetpoint has reached the finalSetpoint yet
		if (getRampEnable()) {
			try {
				double finalSetpoint = EPICS_CONTROLLER.cagetDouble(getChannel(LOOP_DEMAND, getActiveLoop()));
				// currentSetpoint will be slowly ramping if ramp is enabled
				double currentSetpoint = EPICS_CONTROLLER.cagetDouble(getChannel(LOOP_DEMAND_RBV, getActiveLoop()));
				if (currentSetpoint != finalSetpoint) { // Should be exactly equal once ramp completes
					return true; // Still ramping so isBusy() returns true
				}
			} catch (Exception e) {
				throw new DeviceException("Error reading from Lakeshore 336 Temperature Controller device", e);
			}
		}
		return Math.abs(getDemandTemperature() - getControlledTemperature()) > tolerance;
	}

	@Override
	public Double[] getPosition() throws DeviceException {
		Double[] pos = new Double[] {
				getDemandTemperature(),
				getTemperature(0),
				getTemperature(1),
				getTemperature(2),
				getHeaterPercent(),
				getHeaterRange().doubleValue()
				};
		return pos;
	}

	@Override
	public void asynchronousMoveTo(Object externalPosition) throws DeviceException {
		int activeLoop = getActiveLoop();
		if (activeLoop == 0)
			throw new DeviceException(
					"no control loop currently active on this device and I am not allowed to enable one yet.");
		Double[] doubles = ScannableUtils.objectToArray(externalPosition);
		setDemandTemperature(doubles[0]);
	}

	private Double getHeaterPercent() throws DeviceException {
		try {
			if (getActiveLoop() == 0)
				return 0.0;
			return EPICS_CONTROLLER.cagetDouble(getChannel(LOOP_OUTPUT, getActiveLoop()));
		} catch (Exception e) {
			throw new DeviceException("Error reading from Lakeshore 336 Temperature Controller device", e);
		}
	}

	private Double getTemperature(int i) throws DeviceException {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(CH_TEMP, i));
		} catch (Exception e) {
			throw new DeviceException("Error reading from Lakeshore 336 Temperature Controller device", e);
		}
	}

	public Double getControlledTemperature() throws DeviceException {
		try {
			if (getActiveLoop() == 0)
				return null;
			int sensor = EPICS_CONTROLLER.cagetInt(getChannel(LOOP_INPUT_RBV, getActiveLoop())) - 1;
			return EPICS_CONTROLLER.cagetDouble(getChannel(CH_TEMP, sensor));
		} catch (Exception e) {
			throw new DeviceException("Error reading from Lakeshore 336 Temperature Controller device", e);
		}
	}

	public void setDemandTemperature(double demand) throws DeviceException {
		try {
			if (getActiveLoop() == 0)
				return;
			EPICS_CONTROLLER.caputWait(getChannel(LOOP_DEMAND, getActiveLoop()), demand);
		} catch (Exception e) {
			throw new DeviceException("Error setting value in Lakeshore 336 Temperature Controller device", e);
		}
	}

	public Double getDemandTemperature() throws DeviceException {
		try {
			if (getActiveLoop() == 0)
				return null;
			return EPICS_CONTROLLER.cagetDouble(getChannel(LOOP_DEMAND_RBV, getActiveLoop()));
		} catch (Exception e) {
			throw new DeviceException("Error reading from Lakeshore 336 Temperature Controller device", e);
		}
	}

	public int getActiveLoop() {
		return 1;
		// try {
		// for(int i: new int[] {1, 2}) {
		// if (EPICS_CONTROLLER.cagetInt(getChannel(LOOP_HEATERRANGE_RBV,i)) != 0)
		// return i;
		// }
		// } catch (Exception e) {
		// throw new DeviceException("Error reading from Lakeshore 336 Temperature Controller device", e);
		// }
		// return 0;
	}

	public void setManualOutput(double demand) throws DeviceException {
		try {
			if (getActiveLoop() == 0)
				return;
			EPICS_CONTROLLER.caputWait(getChannel(LOOP_MANUAL_OUT, getActiveLoop()), demand);
		} catch (Exception e) {
			throw new DeviceException("Error setting value in Lakeshore 336 Temperature Controller device", e);
		}
	}

	public Double getManualOutput() throws DeviceException {
		try {
			if (getActiveLoop() == 0)
				return null;
			return EPICS_CONTROLLER.cagetDouble(getChannel(LOOP_MANUAL_OUT_RBV, getActiveLoop()));
		} catch (Exception e) {
			throw new DeviceException("Error reading from Lakeshore 336 Temperature Controller device", e);
		}
	}

	public void setP(double demand) throws DeviceException {
		try {
			if (getActiveLoop() == 0)
				return;
			EPICS_CONTROLLER.caputWait(getChannel(LOOP_P, getActiveLoop()), demand);
		} catch (Exception e) {
			throw new DeviceException("Error setting value in Lakeshore 336 Temperature Controller device", e);
		}
	}

	public Double getP() throws DeviceException {
		try {
			if (getActiveLoop() == 0)
				return null;
			return EPICS_CONTROLLER.cagetDouble(getChannel(LOOP_P_RBV, getActiveLoop()));
		} catch (Exception e) {
			throw new DeviceException("Error reading from Lakeshore 336 Temperature Controller device", e);
		}
	}

	public void setI(double demand) throws DeviceException {
		try {
			if (getActiveLoop() == 0)
				return;
			EPICS_CONTROLLER.caputWait(getChannel(LOOP_I, getActiveLoop()), demand);
		} catch (Exception e) {
			throw new DeviceException("Error setting value in Lakeshore 336 Temperature Controller device", e);
		}
	}

	public Double getI() throws DeviceException {
		try {
			if (getActiveLoop() == 0)
				return null;
			return EPICS_CONTROLLER.cagetDouble(getChannel(LOOP_I_RBV, getActiveLoop()));
		} catch (Exception e) {
			throw new DeviceException("Error reading from Lakeshore 336 Temperature Controller device", e);
		}
	}

	public void setD(double demand) throws DeviceException {
		try {
			if (getActiveLoop() == 0)
				return;
			EPICS_CONTROLLER.caputWait(getChannel(LOOP_D, getActiveLoop()), demand);
		} catch (Exception e) {
			throw new DeviceException("Error setting value in Lakeshore 336 Temperature Controller device", e);
		}
	}

	public Double getD() throws DeviceException {
		try {
			if (getActiveLoop() == 0)
				return null;
			return EPICS_CONTROLLER.cagetDouble(getChannel(LOOP_D_RBV, getActiveLoop()));
		} catch (Exception e) {
			throw new DeviceException("Error reading from Lakeshore 336 Temperature Controller device", e);
		}
	}

	public void setHeaterRange(int demand) throws DeviceException {
		try {
			if (getActiveLoop() == 0)
				return;
			EPICS_CONTROLLER.caputWait(getChannel(LOOP_HEATERRANGE, getActiveLoop()), demand);
		} catch (Exception e) {
			throw new DeviceException("Error setting value in Lakeshore 336 Temperature Controller device", e);
		}
	}

	public Integer getHeaterRange() throws DeviceException {
		try {
			if (getActiveLoop() == 0)
				return null;
			return EPICS_CONTROLLER.cagetInt(getChannel(LOOP_HEATERRANGE_RBV, getActiveLoop()));
		} catch (Exception e) {
			throw new DeviceException("Error reading from Lakeshore 336 Temperature Controller device", e);
		}
	}

	public void setRampRate(double rampRate) throws DeviceException {
		try {
			if (getActiveLoop() == 0)
				return;
			EPICS_CONTROLLER.caputWait(getChannel(LOOP_RAMP, getActiveLoop()), rampRate);
		} catch (Exception e) {
			throw new DeviceException("Error setting value in Lakeshore 336 Temperature Controller device", e);
		}
	}

	public Double getRampRate() throws DeviceException {
		try {
			if (getActiveLoop() == 0)
				return null;
			return EPICS_CONTROLLER.cagetDouble(getChannel(LOOP_RAMP_RBV, getActiveLoop()));
		} catch (Exception e) {
			throw new DeviceException("Error reading from Lakeshore 336 Temperature Controller device", e);
		}
	}

	public void setRampEnable(boolean rampEnabled) throws DeviceException {
		try {
			if (getActiveLoop() == 0)
				return;
			EPICS_CONTROLLER.caputWait(getChannel(LOOP_RAMP_ENABLE, getActiveLoop()), rampEnabled ? 1 : 0);
		} catch (Exception e) {
			throw new DeviceException("Error setting value in Lakeshore 336 Temperature Controller device", e);
		}
	}

	public Boolean getRampEnable() throws DeviceException {
		try {
			if (getActiveLoop() == 0)
				return null;
			int rampEnabled = EPICS_CONTROLLER.cagetInt(getChannel(LOOP_RAMP_ENABLE_RBV, getActiveLoop()));
			if (rampEnabled == 1) {
				return true;
			}
			return false;
		} catch (Exception e) {
			throw new DeviceException("Error reading from Lakeshore 336 Temperature Controller device", e);
		}
	}
}