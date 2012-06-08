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

package gda.device.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.factory.FactoryException;

/**
 * 
 */
public class DummyEpicsMonitor extends EpicsMonitor {
	
	private static final Logger logger = LoggerFactory.getLogger(DummyEpicsMonitor.class);

	private Object latestValue = 0;
	
	/**
	 * Constructor
	 */
	public DummyEpicsMonitor() {
		//controller = EpicsController.getInstance();
		//channelManager = new EpicsChannelManager(this);
	}
	
	/**
	 * Sets the value of this monitor.
	 * 
	 * @param newVal the value
	 */
	public void setValue(Object newVal) {
		this.latestValue = newVal;
		notifyIObservers(this, latestValue);
	}
	@Override
	public void configure() throws FactoryException {
		// this only represents a single value which should be the same string as its name
		this.setInputNames(new String[] { getName() });
	}

	@Override
	public Object getPosition() throws DeviceException {

		// single or array value?
		return this.latestValue;
	}



	@Override
	public void initializationCompleted() {
		logger.info("DummyMonitor -  " + getName() + " is initialised.");
	}

}
