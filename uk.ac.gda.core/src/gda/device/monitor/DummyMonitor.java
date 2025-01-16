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

	private boolean throwExceptionOnAttemptedMove = false;

	/** Interval between new values - in milliseconds */
	private int updateInterval = 5000;

	public DummyMonitor() {
		// default constructor
	}

	public DummyMonitor(String name) {
		this();
		setName(name);
	}

	public DummyMonitor(String name, double constantValue) {
		this(name);
		setConstantValue(constantValue);
	}

	@Override
	public void configure() {
		if (isConfigured()) {
			return;
		}
		this.inputNames = new String[]{};
		this.extraNames = new String[]{this.getName()};

		//Only need to send updates if value changes randomly from updateValue()
		if (constantValue == null) {
			Async.scheduleAtFixedRate(this::updateValue, 0, updateInterval, MILLISECONDS, "%s (%s)", getClass().getName(), getName());
		}
		setConfigured(true);
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		if (isThrowExceptionOnAttemptedMove()) {
			throw new DeviceException("This scannable is fixed at constant position " + getConstantValue() + ". It cannot be moved to " + position + ".");
		}
		else {
			super.asynchronousMoveTo(position);
		}
	}

	private void updateValue() {
		latestValue = constantValue == null ? Math.random() * 100 : constantValue;
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

	public boolean isThrowExceptionOnAttemptedMove() {
		return throwExceptionOnAttemptedMove;
	}

	public void setThrowExceptionOnAttemptedMove(boolean throwExceptionOnAttemptedMove) {
		this.throwExceptionOnAttemptedMove = throwExceptionOnAttemptedMove;
	}
}