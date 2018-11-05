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

import gda.device.DeviceException;
import gda.device.Monitor;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * A dummy implementation of the Monitor interface for testing / development.
 */
@ServiceInterface(Monitor.class)
public class DummyMonitor extends MonitorBase {

	private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor();

	private double latestValue = 0.0;
	private String unit = "";

	private Double constantValue;

	/** Interval between new values - in milliseconds */
	private int updateInterval = 5000;

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
		return unit;
	}

	public void setUnit(String units) {
		unit = units;
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