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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import gda.device.BaseEpicsDeviceController;
import gda.device.DeviceException;
import gda.factory.FactoryException;

public class DummyKeithley6487Controller extends BaseEpicsDeviceController {

	private final List<String> readbackRates = new ArrayList<>(Arrays.asList(
			"Passive", "Event", "I/O Intr", "10 second", "5 second", "2 second", "1 second", ".5 second", ".2 second", ".1 second"));
	private final HashMap<Double, String> readbackRatesMap = new HashMap<>();
	private final HashMap<String, Double> readbackRatesMapReverse = new HashMap<>();
	private String readbackrate= "10.0 second";

	Random random = new Random();
	private boolean disabled = false;

	/** Get a list of labels from PV, parse double values from labels, finally pack all into hashmap */
	private void initializeReadbackRatesMap() {
		logger.debug("Initializing readback rates hashmap");
		readbackRatesMap.clear();
		try {
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
			logger.error("Initializing readback rates from Keithley 6487 failed: ", e);
		}
	}

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			logger.debug("Detector already configured");
			return;
		}
		logger.info("Configuring dummy Keithley 6487");
		try {
			initializeReadbackRatesMap();
		} catch (Exception e) {
			throw new FactoryException("Configuring Keithley 6487 failed", e);
		}
	}

	public void setReadbackRate(double readbackrate) throws DeviceException {
		logger.info("Setting readback rate");
		if (readbackRatesMap.containsKey(readbackrate)) {
			this.readbackrate = readbackRatesMap.get(readbackrate);
			logger.info("Setting readback rate to {} --> done", readbackrate);
		} else {
			logger.debug("Allowed readback rates are " + readbackRatesMap.keySet());
			throw new DeviceException("The specified readback rate is not valid! Allowed values: "+readbackRatesMap.keySet());
		}
	}

	public String getReadbackRate() {
		return readbackrate;
	}

	public List<String> getReadbackRates() {
		return new ArrayList<>(readbackRates);
	}

	public double getReading() {
		return random.nextDouble();
	}

	public boolean isDisabled() {
		return disabled ;
	}

	public void setDisabled(Boolean disabled) {
		this.disabled = disabled;
	}

	public double getCollectionTimeS() {
		return readbackRatesMapReverse.get(getReadbackRate());
	}

}