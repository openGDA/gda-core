/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.data.metadata;

import java.io.IOException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.FileConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.FactoryException;
import uk.ac.diamond.daq.persistence.jythonshelf.LocalParameters;

public class PersistantMetadataEntry extends MetadataEntry {

	private static final Logger logger = LoggerFactory.getLogger(PersistantMetadataEntry.class);

	// private transient XMLConfiguration config = null;

	private String defaultValue = "0-0";

	@Override
	public void configure() throws FactoryException {
		super.configure();
		try {
			openConfig();
		} catch (ConfigurationException e) {
			throw new FactoryException("problem creating PersistantMetadataEntry " + getName() + " :", e);
		} catch (IOException e) {
			throw new FactoryException("problem creating PersistantMetadataEntry " + getName() + " :", e);
		}
	}

	synchronized private FileConfiguration openConfig() throws ConfigurationException, IOException {
		// if (config == null) {
		FileConfiguration config = LocalParameters.getXMLConfiguration("persistantMetadata");
		config.reload();
		if (config.getString(getName()) == null) {
			logger.warn("No saved entry found for PersistantMetadataEntry named: '" + getName() + "'. Setting it to: '"
					+ getDefaultValue() + "'");
			// setValue(getDefaultValue());
			config.setProperty(getName(), getDefaultValue());
			config.save();
			notifyIObservers(this, getDefaultValue());
		}
		// }
		return config;
	}

	public void reload() {
		// config.reload();
	}

	@Override
	public String readActualValue() throws ConfigurationException, IOException {
		return openConfig().getString(getName()); // null if missing
	}

	@Override
	public void setValue(String value) throws Exception {
		logger.info("Setting PersistantMetadataEntry " + getName() + " to " + value);
		FileConfiguration config = openConfig();
		config.setProperty(getName(), value);
		config.save();
		notifyIObservers(this, value);
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	public boolean canStoreValue() {
		return true;
	}
}