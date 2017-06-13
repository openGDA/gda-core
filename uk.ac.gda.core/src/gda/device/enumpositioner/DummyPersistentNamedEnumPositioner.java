/*-
 * Copyright © 2011 Diamond Light Source Ltd., Science and Technology
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

package gda.device.enumpositioner;

import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.FileConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import uk.ac.diamond.daq.persistence.jythonshelf.LocalParameters;

public class DummyPersistentNamedEnumPositioner extends DummyNamedEnumPositioner {
	private static final Logger mylogger = LoggerFactory.getLogger(DummyPersistentNamedEnumPositioner.class);

	private FileConfiguration configuration;

	private String configurationName;
	private String propertyName;

	public DummyPersistentNamedEnumPositioner() {
		super();
	}

	@Override
	public void configure() {
			super.configure();
		try {
			configuration = LocalParameters.getThreadSafeXmlConfiguration(getConfigurationName());
		} catch (ConfigurationException e) {
			mylogger.error("Configuration exception in constructor for DummyPersistentNamedEnumPositioner",e);
		} catch (IOException e) {
			mylogger.error("IO exception for DummyPersistentNamedEnumPositioner", e);
		}
	}

	@Override
	public void moveTo(Object position) throws DeviceException {
		configuration.setProperty(propertyName,position);
		try {
			configuration.save();
		} catch (ConfigurationException e) {
			mylogger.error("Configuration exception in rawAsynchronousMoveTo for DummyPersistentNamedEnumPositioner",e);
		}
		super.moveTo(position);
	}

	@Override
	public String getPosition() throws DeviceException {
		return super.getPosition();
	}

	public String getConfigurationName() {
		return configurationName;
	}

	public void setConfigurationName(String configurationName) {
		this.configurationName = configurationName;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}
}