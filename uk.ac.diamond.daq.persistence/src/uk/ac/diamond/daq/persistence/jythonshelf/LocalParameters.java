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

package uk.ac.diamond.daq.persistence.jythonshelf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;

/**
 * A singleton class used to access the GDA's local parameter stores in ${gda.var}. This system supplements the
 * java.properties system for startup configuration. It is used to store changing parameters in a standard way.
 * <p>
 * The class uses Apache Common XMLConfiguration objects which support not only simple key/value pairs, but also
 * arbitrarily complex hierarchical configurations of nodes, including arrays of nodes.
 * <p>
 * There is a default configuration stored in the file ${gda.var}/localParameters.xml. All keys added to this
 * should be prefaced with the package and class name of the writing class. Additional configuration files may also be
 * created and used in the /var directory, but these should only be used if there is a real benefit to splitting the
 * configuration nodes across more than one file.
 * <p>
 * The default behaviour is to create a requested configuration file if it does not exist. NOTE: make sure to use the
 * save() method after setting a property!
 *
 * @see "http://commons.apache.org/configuration/userguide/user_guide.html"
 */
public class LocalParameters {
	private static final Logger logger = LoggerFactory.getLogger(LocalParameters.class);

	static final String DEFAULT_CONFIG_NAME = "localParameters";

	/** Holds non-thread-safe configurations. */
	static HashMap<String, XMLConfiguration> configList = new HashMap<>();

	/**
	 * This is a singleton class. Constructor is private.
	 */
	private LocalParameters() {

	}

	/**
	 * Gets the single instance of the default XML configuration. Will create one if it does not exist.
	 *
	 * @return XMLConfiguration
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	public static XMLConfiguration getXMLConfiguration() throws ConfigurationException, IOException {
		return getXMLConfiguration(DEFAULT_CONFIG_NAME);
	}

	/**
	 * Gets the single instance of the named XML configuration. Will create one if it does not exist.
	 *
	 * @param configName
	 *            The name of the configuration to load (with no trailing .xml)
	 * @return An XMLConfiguration
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	public static XMLConfiguration getXMLConfiguration(String configName) throws ConfigurationException, IOException {
		return getXMLConfiguration(configName, true);
	}

	/**
	 * Gets the single instance of the named XML configuration. If createIfMissing is true then will create one if it
	 * does not exist.
	 *
	 * @param configName
	 *            The name of the configuration to load (with no trailing .xml)
	 * @param createIfMissing
	 * @return XMLConfiguration
	 * @throws ConfigurationException
	 * @throws IOException
	 */
	public static XMLConfiguration getXMLConfiguration(String configName, Boolean createIfMissing)
			throws ConfigurationException, IOException {
		return getXMLConfiguration(getDefaultConfigDir(), configName, createIfMissing);
	}

	private static String getDefaultConfigDir() {
		return LocalProperties.getVarDir();
	}

	/**
	 * Rereads the xml file come what may.
	 *
	 * @param configName
	 * @return XMLConfiguration
	 * @throws ConfigurationException
	 * @throws IOException
	 */
	public static XMLConfiguration getNewXMLConfiguration(String configName) throws ConfigurationException, IOException{
		if (configList.containsKey(configName)){
			configList.remove(configName);
		}
		return getXMLConfiguration(configName);
	}

	private static XMLConfiguration loadConfigurationFromFile(String filename) throws ConfigurationException {
		XMLConfiguration config = new XMLConfiguration();
		config.setDelimiterParsingDisabled(false); // This needs to change if GDA-2492 is fixed
		config.setFileName(filename);
		config.load();
		return config;
	}

	/**
	 * Warning - this may return a cached version of the object and does not re-read the underlying xml file.
	 *
	 * @param configDir
	 * @param configName
	 * @param createIfMissing
	 * @return XMLConfiguration
	 * @throws ConfigurationException
	 * @throws IOException
	 */
	public static XMLConfiguration getXMLConfiguration(String configDir, String configName, Boolean createIfMissing)
	throws ConfigurationException, IOException
	{
		return getXMLConfiguration(configDir, configName, createIfMissing, false);
	}
	/**
	 * @param configDir
	 * @param configName
	 * @param createIfMissing
	 * @param createAlways true if existing config in the cache is to be thrown away - re-reads the underlying file
	 * @return XMLConfiguration
	 * @throws ConfigurationException
	 * @throws IOException
	 */
	public synchronized static XMLConfiguration getXMLConfiguration(String configDir, String configName, Boolean createIfMissing, boolean createAlways)
			throws ConfigurationException, IOException {
		// Instantiate the Configuration if it has not been instantiated
		if (configDir == null || configDir.isEmpty())
			throw new IllegalArgumentException("configDir is null or empty");
		if (!configDir.endsWith(File.separator))
			configDir += File.separator;
		final String fullName = getFullName(configDir, configName);
		if (createAlways && configList.containsKey(fullName)){
			configList.remove(fullName);
		}
		if (!configList.containsKey(fullName)) {
			XMLConfiguration config;

			// Try to open the file
			try {
				config = loadConfigurationFromFile(fullName);
			} catch (ConfigurationException e)
			// catch (NoClassDefFoundError e)
			{
				// Assume the error occured because the file does not exist

				// Throw exception if createIfMissing is false
				if (!createIfMissing) {
					logger.error("Could not load " + configDir + configName + ".xml which will not be created");
					throw new ConfigurationException(e);
				}

				// else try to make it...
				try {
					File dir = new File(configDir);
					if (!dir.exists())
						if (!dir.mkdirs()) {
							throw new FileNotFoundException("Couldn't create directory: " + dir);
						}
					File file = new File(fullName);
					PrintWriter out = new PrintWriter(new FileWriter(file));
					out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\"?>");
					out.println("<" + configName + ">");
					out.println("</" + configName + ">");
					out.close();
				} catch (IOException ee) {
					logger.error("Failed trying to create non-existent file " + fullName);
					throw new IOException(ee);
				}

				// ... and read it again
				try {
					config = loadConfigurationFromFile(fullName);
				} catch (ConfigurationException ee) {
					logger.error("Failed trying to read newly-created file " + fullName);
					throw new ConfigurationException(e);
				}

				logger.debug("Created configuration file: " + fullName);

			}// endif - create a missing file
			config.setReloadingStrategy(new FileChangedReloadingStrategy());
			configList.put(fullName, config);
			logger.debug("Loaded the configuration file: {}", fullName);

		}// endif - instantiate a new configuration

		// return the configuration object
		return configList.get(fullName);
	}

	private static String getFullName(String configDir, String configName) {
		String fullName = configDir + configName + ".xml";
		return fullName;
	}

	public static FileConfiguration getThreadSafeXmlConfiguration(String configName) throws ConfigurationException, IOException {
		return getThreadSafeXmlConfiguration(configName, true);
	}

	public static FileConfiguration getThreadSafeXmlConfiguration(String configName, boolean createIfMissing) throws ConfigurationException, IOException {
		return getThreadSafeXmlConfiguration(getDefaultConfigDir(), configName, createIfMissing);
	}

	public static FileConfiguration getThreadSafeXmlConfiguration(String configDir, String configName, boolean createIfMissing) throws ConfigurationException, IOException {
		final String fullName = getFullName(configDir, configName);

		final FileConfiguration fileConfig = getXMLConfiguration(configDir, configName, createIfMissing);

		// Get the lock for this configuration. Create it if it doesn't exist.
		Object lockForThisConfig = locks.get(fullName);
		if (lockForThisConfig == null) {
			final Object value = new Object();
			lockForThisConfig = locks.putIfAbsent(fullName, value);
			if (lockForThisConfig == null) {
				lockForThisConfig = value;
			}
		}

		ThreadSafeFileConfiguration threadSafeConfig = new ThreadSafeFileConfiguration(fileConfig, lockForThisConfig);
		return threadSafeConfig;
	}

	/** Lock objects used to enforce thread safety. */
	private static ConcurrentHashMap<String, Object> locks = new ConcurrentHashMap<>();

}
