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

import java.util.Iterator;

import org.apache.commons.configuration.ConfigurationException;

/**
 * PropertiesConfig Interface to abstract the user of properties data source from the underlying implementation.
 */
public interface PropertiesConfig {

	/**
	 *
	 * @return all the property keys
	 */
	public Iterator<String> getKeys();

	/**
	 * Load in property information from a data source.
	 *
	 * @param listName
	 *            the path name of the property file.
	 * @throws ConfigurationException
	 */
	public void loadPropertyData(String listName) throws ConfigurationException;

	/**
	 * Dump out all existing properties to info channel.
	 */
	public void dumpProperties();

	/**
	 * Get an integer property value using a specified key string.
	 *
	 * @param name
	 *            the key specified to fetch the integer value
	 * @param defaultValue
	 *            the default value to return if the key is not found
	 * @return the property value to return to the caller
	 */
	public int getInteger(String name, int defaultValue);

	/**
	 * Get a string property value using a specified key string.
	 *
	 * @param name
	 *            the key specified to fetch the string value
	 * @param defaultValue
	 *            the default value to return if the key is not found
	 * @return the property value to return to the caller
	 */
	public String getString(String name, String defaultValue);

	/**
	 * Get a float property value using a specified key string.
	 *
	 * @param name
	 *            the key specified to fetch the float value
	 * @param defaultValue
	 *            the default value to return if the key is not found
	 * @return the property value to return to the caller
	 */
	public float getFloat(String name, float defaultValue);

	/**
	 * Get a double property value using a specified key string.
	 *
	 * @param name
	 *            the key specified to fetch the double value
	 * @param defaultValue
	 *            the default value to return if the key is not found
	 * @return the property value to return to the caller
	 */
	public double getDouble(String name, double defaultValue);

	/**
	 * Get a boolean property value using a specified key string.
	 *
	 * @param name
	 *            the key specified to fetch the boolean value
	 * @param defaultValue
	 *            the default value to return if the key is not found
	 * @return the property value to return to the caller
	 */
	public boolean getBoolean(String name, boolean defaultValue);

	/**
	 * Get a file path property value using a specified key string.
	 *
	 * @param name
	 *            the key specified to fetch the file path value
	 * @param defaultValue
	 *            the default value to return if the key is not found
	 * @return the property value to return to the caller
	 */
	public String getPath(String name, String defaultValue);

	/**
	 * Assign a string property value to a specified key string.
	 *
	 * @param value
	 *            the string value to assign to the specified key
	 * @param name
	 *            the key specified to assign to the value
	 */
	public void setString(String value, String name);

	/**
	 * Remove a property from the configuration
	 * @param key
	 */
	public void clearProperty(String key);

	/**
	 * @param key
	 * @return True if the configuration contains the specified key
	 */
	public boolean containsKey(String key);

	public String[] getStringArray(String propertyName);
}
