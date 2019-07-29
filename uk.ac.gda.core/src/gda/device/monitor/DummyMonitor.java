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

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import gda.device.DeviceException;
import uk.ac.diamond.daq.concurrent.Async;

/**
 * A dummy implementation of the Monitor interface for testing / development.
 */
public class DummyMonitor extends MonitorBase {

	private double latestValue = 0.0;
	private String unit = "";

	private Double constantValue;

	/** Interval between new values - in milliseconds */
	private int updateInterval = 5000;

	@Override
	public void configure() {
		if (isConfigured()) {
			return;
		}
		this.inputNames = new String[]{};
		this.extraNames = new String[]{this.getName()};
		Async.scheduleAtFixedRate(this::updateValue, 0, updateInterval, MILLISECONDS, "%s (%s)", getClass().getName(), getName());
		setConfigured(true);
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

	@Override
	public String toFormattedString() {
		if (unit != null && !unit.isEmpty()) {
			return String.format("%s %s", super.toFormattedString(), unit);
		}
		return super.toFormattedString();
	}
}