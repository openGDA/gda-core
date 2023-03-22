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

package uk.ac.diamond.daq.server.configuration.properties;

import java.util.Iterator;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration2.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.PropertiesConfig;

/**
 * Adapter to let a {@link Configuration} instance be used as the source of LocalProperties
 */
public class ConfigurationServicePropertyConfig implements PropertiesConfig {
	private static final Logger logger = LoggerFactory.getLogger(ConfigurationServicePropertyConfig.class);

	private final Configuration config;

	public ConfigurationServicePropertyConfig(Configuration config) {
		this.config = config;
	}

	@Override
	public Iterator<String> getKeys() {
		return config.getKeys();
	}

	@Override
	public void loadPropertyData(String listName) throws ConfigurationException {
		// no-op
	}

	@Override
	public void dumpProperties() {
		config.getKeys().forEachRemaining(k -> logger.debug("{}={}", k, config.get(Object.class, k)));
	}

	@Override
	public int getInteger(String name, int defaultValue) {
		return config.getInt(name, defaultValue);
	}

	@Override
	public String getString(String name, String defaultValue) {
		return config.getString(name, defaultValue);
	}

	@Override
	public float getFloat(String name, float defaultValue) {
		return config.getFloat(name, defaultValue);
	}

	@Override
	public double getDouble(String name, double defaultValue) {
		return config.getDouble(name, defaultValue);
	}

	@Override
	public boolean getBoolean(String name, boolean defaultValue) {
		return config.getBoolean(name, defaultValue);
	}

	@Override
	public String getPath(String name, String defaultValue) {
		String value = config.getString(name, defaultValue);
		if (value != null) {
			value = value.replace('\\', '/');
		}
		return value;
	}

	@Override
	public void setString(String value, String name) {
		config.setProperty(name, value);
	}

	@Override
	public void setBoolean(boolean value, String name) {
		config.setProperty(name, value);
	}

	@Override
	public void clearProperty(String key) {
		config.clearProperty(key);
	}

	@Override
	public boolean containsKey(String key) {
		return config.containsKey(key);
	}

	@Override
	public String[] getStringArray(String propertyName) {
		return config.getStringArray(propertyName);
	}
}
