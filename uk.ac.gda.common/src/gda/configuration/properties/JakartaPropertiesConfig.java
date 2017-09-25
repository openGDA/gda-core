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

package gda.configuration.properties;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationFactory;
import org.apache.commons.configuration.DataConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Properties Configuration class, implementing PropertiesConfig interface. Uses the Jakarta Commons Configuration
 * package for its implementation. It insulates client code from the details of using Jakarta.
 * <p>
 * <p>
 * The class creates a composite configuration object on startup. Clients call loadPropertyData() to add property data
 * sources. This method can be passed with the name of either a Jakarta descriptor file (file name must end with
 * "config.xml"), or a Jakarta XML property file (ending in ".xml") or a legacy java properties file (file name must end
 * with ".properties").
 * <p>
 * <p>
 * Each call to loadPropertyData adds a new group of properties to the composite configuration object.
 * <p>
 * <p>
 * N.B. The order of loading properties is important, as this affects the order of searching for keys when querying a
 * property value. If duplicate keys exist, then the first key found has its value returned.
 * <p>
 * <p>
 * So user overrides should be loaded first. Plugin/technique-specific properties should be loaded next. Default/core
 * property data should be loaded last.
 * <p>
 */
public class JakartaPropertiesConfig implements PropertiesConfig {

	private static final Logger logger = LoggerFactory.getLogger(JakartaPropertiesConfig.class);

	// all properties config data is combined into this object
	private CompositeConfiguration config = null;

	// README - this map may not be needed - was for debugging initially.
	private Map<String, Configuration> configMap = null;

	/**
	 * Constructor for JakartaPropertiesConfig objects. Creates a new composite configuration and adds a system
	 * configuration to it.
	 */
	public JakartaPropertiesConfig() {
		// create global composite to store all loaded property config data
		config = new CompositeConfiguration();

		// create a system properties configuration - grabs all system
		// properties.
		Configuration sysConfig = new SystemConfiguration();
		config.addConfiguration(sysConfig);

		// create map to store individual configs
		configMap = new HashMap<String, Configuration>();

		// put system properties in the map
		configMap.put("system", sysConfig);
	}

	@Override
	public Iterator<String> getKeys() {
		return config.getKeys();
	}

	/**
	 * Remove all cached property information and reload system properties. User must reload any required property data
	 * sources.
	 */
	public void ResetProperties() {
		// clear out composite config and add a fresh system config into it
		config.clear();
		Configuration sysConfig = new SystemConfiguration();
		config.addConfiguration(sysConfig);

		// clear out the config map and put system into it
		configMap.clear();
		configMap.put("system", sysConfig);
	}

	/**
	 * Workaround for an apparent bug with factory base path default of ".". Configuration descriptor file (config.xml)
	 * contains references to one or more property data sources. Files should work with relative pathnames. However just
	 * specifying "java.properties" doesnt find the file! So need to fixup the factory base path with the location of
	 * the config.xml file. This appears to fix the problem - eg "java.properties" is now found.
	 *
	 * @param factory
	 *            the current configuration factory in use
	 * @param listName
	 *            file path name of the property data source
	 */
	private void configFactoryBasePathBugWorkaround(ConfigurationFactory factory, String listName) {
		// String basePath = factory.getBasePath(); // retain for debugging

		// work out length of path excluding filename
		int pathEndIndex = 0;
		pathEndIndex = Math.max(pathEndIndex, listName.lastIndexOf('\\'));
		pathEndIndex = Math.max(pathEndIndex, listName.lastIndexOf('/'));

		// set the path into factory, since its default of "." doesnt
		// successfully fetch files from the same folder as config.xml!!
		String p = listName.substring(0, pathEndIndex);
		factory.setBasePath(p);

		// String setBasePath = factory.getBasePath(); // retain for debugging
	}

	/**
	 * Load in a single property data source, or a "config.xml" descriptor file (specifying multiple data sources).
	 * Properties are added to the global composite property data set.
	 * <p>
	 * <p>
	 * N.B. Due to underlying Jakarta (commons configuration) implementation, any previously loaded properties (with
	 * duplicate keys) will override subsequently loaded properties. ie clients should load in override (eg user) data
	 * before loading default (eg core) data.
	 * <p>
	 * <p>
	 * N.B. Also system properties are currently loaded before any calls to loadPropertyData, so they take precedence
	 * over everything.
	 *
	 * @param listName
	 *            the path name of the property data source. If the name ends in "config.xml" then the file is treated
	 *            as a Jakarta commons configuration descriptor file. If the file ends in ".xml" it is loaded as a
	 *            Jakarta XML property configuration file. Otherwise it is loaded as a legacy flat-file key-value pair
	 *            plain text property file. N.B. Although Jakarta supports other data sources, eg JDBC, these are not
	 *            yet supported via this method.
	 * @throws ConfigurationException
	 * @see gda.configuration.properties.PropertiesConfig#loadPropertyData(java.lang.String)
	 */
	@Override
	public void loadPropertyData(String listName) throws ConfigurationException {
		Configuration userConfig = null;

		if (listName.contains(".xml")) {
			if (listName.endsWith("config.xml")) {

				// *****
				// FIXME 'ConfigurationFactory' should be replaced with the new and improved 'DefaultConfigurationBuilder'
				// *****

				// create a JCC configuration factory from a JCC config descriptor
				// file and make a JCC configuration interface/object from it
				ConfigurationFactory factory = new ConfigurationFactory();

				// README - fix to get relative paths in config.xml working.
				// See comment for this method for explanation.
				configFactoryBasePathBugWorkaround(factory, listName);

				// now try to load in config.xml - relative paths should now work
				factory.setConfigurationFileName(listName);
				userConfig = factory.getConfiguration();
			} else {
				// load a JCC XML-format property file
				userConfig = new XMLConfiguration(listName);
			}
		} else {
			if (listName.contains(".properties")) {
				// load a classic java properties flat-textfile,
				// containing just name-value pairs - with extended JCC syntax
				userConfig = new PropertiesConfiguration(listName);
			}
		}

		if (userConfig != null) {
			config.addConfiguration(userConfig);
			configMap.put(listName, userConfig);
		}
	}

	/**
	 * Dump out all existing properties to message logging info channel.
	 */
	@Override
	public void dumpProperties() {

		Iterator<String> keyIterator = config.getKeys();

		while (keyIterator.hasNext()) {
			String key = keyIterator.next();

			Object o = config.getProperty(key);
			if (o != null) {
				// Check for multiple setting of properties
				if (o instanceof List<?>) {
					logger.debug("{} is set multiple times the value used will be the first! This maybe ok if deliberately overridden", key);
					// Check for unnecessary overriding of properties
					List<?> list = (List<?>) o;
					// If all the values are the same it's definitely unnecessarily overridden
					if (list.size() > 1 && list.stream().distinct().count() == 1){
						logger.warn("{} is unnecessarily overridden", key);
					}
				}
				if (o instanceof String) {
					// Calling getString method ensures value has any
					// processing
					// done by commons config applied - ie string
					// interpolation, etc.
					logger.debug("{} = {}", key, LocalProperties.get(key));
				} else {
					// Handle non-string objects, eg ArrayList's
					logger.debug("{} = {}", key, o);
				}
			}
		}
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

	/**
	 * Get a file pathname. Any backslashes found are converted to forward slashes. N.B. We know this is a file
	 * pathname. Backslashes can cause problems with Java code expecting forward slashes. So it *should* be safe to
	 * convert any backslashes to forward slashes. If any client code needs to fetch properties with backslashes (eg to
	 * pass to native JNI code), then either convert the result back to forward slashes, or just call getString instead.
	 *
	 * @see gda.configuration.properties.PropertiesConfig#getPath(java.lang.String, java.lang.String)
	 */
	@Override
	public String getPath(String name, String defaultValue) {
		String value = config.getString(name, defaultValue);

		if (value != null) {
			value = value.replace('\\', '/');
		}

		return value;
	}

	@Override
	public URL getURL(String name, URL defaultValue) {
		// Use decorator, which can convert to URLs, Locales, Dates, Colours,
		// etc.
		DataConfiguration dataConfig = new DataConfiguration(config);

		return dataConfig.getURL(name, defaultValue);
	}

	@Override
	public void setString(String value, String name) {
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
