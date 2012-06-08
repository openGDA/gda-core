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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Monitor;
import gda.device.Scannable;

/**
 * A dummy implementation of the Monitor interface for testing / development.
 */
public class DummyMonitor extends MonitorBase implements Monitor, Runnable, Scannable {
	
	private static final Logger logger = LoggerFactory.getLogger(DummyMonitor.class);
	
	double latestValue = 0.0;
	
	private Double constantValue;

	/**
	 * Constructor.
	 */
	public DummyMonitor() {
	}

	@Override
	public void configure() {
		this.inputNames = new String[]{};
		this.extraNames = new String[]{this.getName()};
		// TODO Do not need separate threads in all monitors since 
		// thread just used to assign value. Could use single thread
		// and process all DummyMonitors in it.
		uk.ac.gda.util.ThreadManager.getThread(this).start();
	}

	@Override
	public void run() {
		try {
			// generate a new value at regular intervals
			while (true) {
				Thread.sleep(5000);
				latestValue = Math.random() * 100;
				notifyIObservers(this, latestValue);
			}
		} catch (InterruptedException e) {
			logger.warn(getName() + " thread interrupted. Stopping number generation.");
			e.printStackTrace();
		}

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
}