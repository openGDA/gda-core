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

package gda.device.scannable;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.FileConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import uk.ac.diamond.daq.persistence.jythonshelf.LocalParameters;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * Dummy object that is able to save its position into a local parameter
 */
@ServiceInterface(Scannable.class)
public class DummyPersistentScannable extends DummyScannable {
	private static final Logger logger = LoggerFactory.getLogger(DummyPersistentScannable.class);

	protected FileConfiguration configuration;

	/**
	 * Constructor
	 */
	public DummyPersistentScannable() {
		super();
		try {
			configuration = LocalParameters.getThreadSafeXmlConfiguration("UserConfiguration");
		} catch (Exception e) {
			logger.error("Exception in constructor for DummyPersistentScannable {}", getName(), e);
		}
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {
		Double[] positionArray = ScannableUtils.objectToArray(position);
		configuration.setProperty(getPropertyName(), position.toString());
		try {
			configuration.save();
			final Double newPosition = positionArray[0];
			notifyIObservers(getName(), newPosition);
			notifyIObservers(getName(), new ScannablePositionChangeEvent(newPosition));
			notifyIObservers(getName(), ScannableStatus.IDLE);
		} catch (ConfigurationException e) {
			logger.error("Configuration exception in rawAsynchronousMoveTo for DummyPersistentScannable {}", getName(), e);
		}
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		String propertyName = getPropertyName();
		if (configuration.getProperty(propertyName)== null) {
			logger.warn("Value {} does not exist, initializing to 0.0", propertyName);
			configuration.setProperty(propertyName, "0.0");
			try {
				configuration.save();
			} catch (ConfigurationException e) {
				logger.error("Configuration exception when saving position for {}", getName(), e);
			}
		}
		Object position = configuration.getProperty(propertyName);
		return ScannableUtils.objectToArray(position);
	}

	private String getPropertyName() {
		return getName() + "PersistentPosition";
	}
}