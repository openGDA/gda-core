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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Monitor;
import gda.factory.FactoryException;
import uk.ac.gda.api.remoting.ServiceInterface;

@ServiceInterface(Monitor.class)
public class DummyEpicsMonitor extends EpicsMonitor {

	private static final Logger logger = LoggerFactory.getLogger(DummyEpicsMonitor.class);

	private Object latestValue = 0;

	/**
	 * Sets the value of this monitor.
	 *
	 * @param newVal the value
	 */
	public void setValue(Object newVal) {
		if (newVal instanceof List) {
			// Spring cannot pass an array, so convert List
			final List<?> newValList = (List<?>) newVal;
			final Object firstVal = newValList.get(0);
			if (firstVal instanceof Integer) {
				latestValue = newValList.stream().mapToInt(a -> (int) a).toArray();
			} else if (firstVal instanceof Double || firstVal instanceof Float) {
				latestValue = newValList.stream().mapToDouble(a -> (double) a).toArray();
			} else if (firstVal instanceof String) {
				latestValue = newValList.stream().map(a -> (String) a).toArray();
			} else {
				// Anything else (unlikely to happen) - leave as it is
				latestValue = newVal;
			}
		} else {
			// Not a list - just store the value
			latestValue = newVal;
		}

		notifyIObservers(this, latestValue);
	}
	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		// this only represents a single value which should be the same string as its name
		this.setInputNames(new String[0]);
		this.setExtraNames(new String[] { getName() });
		setConfigured(true);
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
