/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Monitor;
import gda.device.Scannable;

/**
 * A dummy implementation of the Monitor interface for testing / development.
 */
public class DummyMonitor extends MonitorBase implements Monitor, Scannable {

	private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor();
	private static final Logger logger = LoggerFactory.getLogger(DummyMonitor.class);

	double latestValue = 0.0;

	private Double constantValue;

	/** Interval between new values - in milliseconds */
	private int updateInterval = 5000;

	/**
	 * Constructor.
	 */
	public DummyMonitor() {
	}

	@Override
	public void configure() {
		this.inputNames = new String[]{};
		this.extraNames = new String[]{this.getName()};
		EXECUTOR.scheduleAtFixedRate(this::updateValue, 0, updateInterval, TimeUnit.MILLISECONDS);
	}

	private void updateValue() {
		latestValue = Math.random() * 100;
		notifyIObservers(this, latestValue);
	}

	@Override
	public Object getPosition() throws DeviceException {
		if (constantValue!=null) return constantValue;
		return latestValue;
	}

	/**
	 * Sets the starting value
	 *
	 * @param value
	 */
	public void setValue(double value) {
		this.latestValue = value;
	}

	@Override
	public int getElementCount() throws DeviceException {
		return 1;
	}

	@Override
	public String getUnit() throws DeviceException {
		// unknown
		return "";
	}

	/**
	 * @return Returns the constantValue.
	 */
	public Double getConstantValue() {
		return constantValue;
	}

	/**
	 * @param constantValue The constantValue to set.
	 */
	public void setConstantValue(Double constantValue) {
		this.constantValue = constantValue;
	}

	/**
	 * Set time between updates
	 * @param updateInterval in milliseconds
	 */
	public void setUpdateInterval(int updateInterval) {
		this.updateInterval = updateInterval;
	}
}