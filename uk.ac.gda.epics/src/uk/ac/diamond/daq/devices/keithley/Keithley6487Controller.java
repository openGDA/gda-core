/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.keithley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import gda.device.BaseEpicsDeviceController;
import gda.device.DeviceException;
import gda.factory.FactoryException;

public class Keithley6487Controller extends BaseEpicsDeviceController {

	private static final String RANGE_PV = "Range";
	private static final String READING_PV = "Measure";
	private static final String READBACK_RATE_PV = "Measure.SCAN";
	private static final String ZERO_CHECK_RBV_PV = "ZeroCheckRBV";
	private static final String AUTO_RANGE_RBV_PV = "AutoRangeRBV";
	private static final String LOCAL_CONTROLS_RBV_PV = "KLOCKRBV";
	private static final String DAMPING_ENABLE_RBV_PV = "DampingRBV";
	private static final String FILTER_ENABLE_RBV_PV = "FilterEnableRBV";
	private static final String SOURCE_ENABLED_RBV_PV = "SourceEnableRBV";
	private static final String SOURCE_VOLTAGE_RBV_PV = "SourceVoltageRBV";
	private static final String SOURCE_VOLTAGE_SETPOINT_PV = "SourceVoltage";
	private static final String SOURCE_RANGE_PV = "SourceRange";
	private static final String SOURCE_I_LIMIT_PV = "SourceIlimit";
	private static final String SOURCE_INTERLOCK_PV = "SourceInterlock";
	private static final String SOURCE_INTERLOCK_STATUS_PV = "SourceInterlockRBV";
	private static final String VOLTAGE_SOURCE_READBACK_RATE_PV = "SourceReadSettings.SCAN";

	private final List<String> readbackRates = new ArrayList<>();
	private final HashMap<Double, String> readbackRatesMap = new HashMap<>();
	private final HashMap<String, Double> readbackRatesMapReverse = new HashMap<>();

	private boolean disabled = false;

	public Keithley6487Controller(String basePvName){
		this.setBasePvName(basePvName);
	}

	/* Get a list of labels from PV, parse double values from labels, finally pack all into hashmap */
	private void initializeReadbackRatesMap() {
		logger.debug("Initializing readback rates hashmap");
		readbackRatesMap.clear();
		try {
			initialiseEnumChannel(READBACK_RATE_PV, readbackRates);
			for (String rate: readbackRates) {
				Scanner sc = new Scanner(rate);
	            try {
	                double a = sc.nextDouble();
					readbackRatesMap.put(a, rate);
					readbackRatesMapReverse.put(rate, a);
	            } catch (Exception e) {
	            	//pass - don't populate map if there is no double in it
	            }
			}
			logger.debug("Hashmap keyset of readback rates: {}", readbackRatesMap.keySet());
			logger.debug("Hashmap values of readback rates: {}", readbackRatesMap.values());
		} catch (Exception e) {
			logger.error("Initializing readback rates from Keithley failed: ", e);
		}
	}

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			logger.debug("Detector already configured");
			return;
		}
		if (getBasePvName() == null) {
			logger.error("Configure called with no basePVName. Check spring configuration!");
			throw new FactoryException("Configure called with no basePVName. Check spring configuration!");
		}

		// Check the basePv ends with : if not add it
		if (!getBasePvName().endsWith(":")) {
			logger.trace("Keithley 6400 Series basePv didn't end with : adding one");
			setBasePvName(getBasePvName() + ":");
			}

		logger.info("Configuring keithley6487 with base PV: {}", getBasePvName());

		try {
			initializeReadbackRatesMap();
		} catch (Exception e) {
			throw new FactoryException("Configuring Keithley6487 failed", e);
		}

		/*
		 * try { getEpicsController().setMonitor(getChannel(ACQUIRE_RBV), this); } catch (Exception e) { throw new
		 * FactoryException("Error setting up EPICS monitors", e); }
		 */
	}

	public void setReadbackRate(double readbackrate) throws DeviceException {
		logger.info("Setting readback rate");
		if (readbackRatesMap.containsKey(readbackrate)) {
			setStringValue(READBACK_RATE_PV, readbackRatesMap.get(readbackrate), "readback rate");
			logger.info("Setting readback rate to {} --> done", readbackrate);
		} else {
			logger.debug("Allowed readback rates are " + readbackRatesMap.keySet());
			throw new DeviceException("The specified readback rate is not valid!");
		}
	}

	public String getReadbackRate() throws DeviceException {
		return getStringValue(READBACK_RATE_PV, "readback rate");
	}

	public List<String> getReadbackRates() {
		return new ArrayList<>(readbackRates);
	}

	public double getReading() throws DeviceException {
		return getDoubleValue(READING_PV, "current");
	}

	public boolean isDisabled() {
		return disabled ;
	}

	public void setDisabled(Boolean disabled) {
		this.disabled = disabled;
	}

	public String getZeroCheckRBV() throws DeviceException {
		return getStringValue(ZERO_CHECK_RBV_PV, "zero check");
	}

	public String getRangeAutoRBV() throws DeviceException {
		return getStringValue(AUTO_RANGE_RBV_PV, "auto range setting");
	}

	public String getRange() throws DeviceException {
		return getStringValue(RANGE_PV, "range value");
	}

	public String getFilter() throws DeviceException {
		return getStringValue(FILTER_ENABLE_RBV_PV, "filter enable");
	}

	public String getDamping() throws DeviceException {
		return getStringValue(DAMPING_ENABLE_RBV_PV, "damping enable");
	}

	public String getLocalControls() throws DeviceException {
		return getStringValue(LOCAL_CONTROLS_RBV_PV, "local controls");
	}

	public String getVoltageSourceEnabledRBV() throws DeviceException {
		return getStringValue(SOURCE_ENABLED_RBV_PV, "voltage source enable");
	}

	public double getVoltageSourceRBV() throws DeviceException {
		return getDoubleValue(SOURCE_VOLTAGE_RBV_PV, "source voltage value");
	}

	public double getVoltageSourceSetpoint() throws DeviceException {
		return getDoubleValue(SOURCE_VOLTAGE_SETPOINT_PV, "source voltage setpoint");
	}

	public String getVoltageSourceRange() throws DeviceException {
		return getStringValue(SOURCE_RANGE_PV, "voltage source range");
	}

	public String getVoltageSourceILimit() throws DeviceException {
		return getStringValue(SOURCE_I_LIMIT_PV, "voltage source current limit");
	}

	public String getVoltageSourceInterlock() throws DeviceException {
		return getStringValue(SOURCE_INTERLOCK_PV, "voltage source interlock");
	}

	public String getVoltageSourceInterlockStatus() throws DeviceException {
		return getStringValue(SOURCE_INTERLOCK_STATUS_PV, "voltage source interlock status");
	}

	public String getVoltageReadbackRate() throws DeviceException {
		return getStringValue(VOLTAGE_SOURCE_READBACK_RATE_PV, "zero check");
	}

	public double getCollectionTimeS() throws DeviceException {
		return readbackRatesMapReverse.get(getReadbackRate());
	}

}
