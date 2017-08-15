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

package gda.util.userOptions;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.FileConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.data.PathConstructor;
import gda.device.DeviceException;
import gda.util.VisitPath;
import gda.util.exceptionUtils;
import gda.util.simpleServlet.corba.impl.SimpleServletAdapter;
import uk.ac.diamond.daq.persistence.jythonshelf.LocalParameters;

/**
 * UserOptions Class Use TreeMap as it is sorted by the key as opposed to HashMaps which are not.
 */
@SuppressWarnings( { "unchecked", "rawtypes" })
public class UserOptions extends java.util.TreeMap<String, UserOption> implements Serializable {

	private static final Logger logger = LoggerFactory.getLogger(UserOptions.class);

	/**
	 *
	 */
	private static final long serialVersionUID = 8365539022259775645L;
	/**
	 *
	 */
	public String title;
	/**
	 *
	 */
	public Boolean containsDefault;
	static String propTitle = "title", propDefValue = "defaultValue", propKeyName = "keyName", propValue = "value",
			propType = "type", typeBoolean = "typeBoolean", typeString = "typeString", typeDouble = "typeDouble",
			typeInteger = "typeInteger",propDesc = "description";

	public static UserOptions createFromTemplate(String configDir, String configName) throws ConfigurationException,
			IOException, Exception {
		FileConfiguration config = LocalParameters.getXMLConfiguration(configDir, configName, false, true);
		UserOptions options = new UserOptions();
		options.title = config.getString(propTitle);
		options.containsDefault = true;
		Integer index = 0;
		while (true) {
			String tag = "options.option(" + index + ").";
			Object description = null;
			try {
				String keyName = config.getString(tag + propKeyName);
				/*
				 * stop on last key
				 */
				if (keyName == null)
					break;
				description = config.getProperty(tag + propDesc);
				Object type = config.getProperty(tag + propType);
				UserOption<? extends Object, ? extends Object> option;
				if (type != null && type.equals(typeBoolean)) {
					option = new UserOption<Object, Boolean>(description, config.getBoolean(tag + propDefValue));
				} else if (type != null && type.equals(typeString)) {
					option = new UserOption<Object, String>(description, config.getString(tag + propDefValue));
				} else if (type != null && type.equals(typeDouble)) {
					option = new UserOption<Object, Double>(description, config.getDouble(tag + propDefValue));
				} else if (type != null && type.equals(typeInteger)) {
					option = new UserOption<Object, Integer>(description, config.getInt(tag + propDefValue));
				} else {
					option = new UserOption<Object, Object>(description, config.getProperty(tag + propDefValue));
				}
				options.put(keyName, option);
				index++;
			} catch (Exception ex) {
				throw new Exception("Error reading option " + index, ex);
			}
		}
		return options;
	}

	/**
	 * @param configDir
	 * @param configName
	 * @throws ConfigurationException
	 * @throws IOException
	 * @throws Exception
	 */
	public void saveToTemplate(String configDir, String configName) throws ConfigurationException, IOException,
			Exception {
		FileConfiguration config = LocalParameters.getXMLConfiguration(configDir, configName, true, true);
		//after clear we need to save and then reload to ensure all items are removed
		config.clear();
		config.save();
		config = LocalParameters.getXMLConfiguration(configDir, configName, true, true);
		config.addProperty(propTitle, title);
		Integer index = 0;
		Iterator<Map.Entry<String, UserOption>> iter = entrySet().iterator();
		while (iter.hasNext()) {
			UserOption option = null;
			try {
				String tag = "options.option(" + index + ").";
				Map.Entry<String, UserOption> entry = iter.next();
				option = entry.getValue();
				config.addProperty(tag + propKeyName, entry.getKey());
				if (option.defaultValue instanceof Boolean) {
					config.addProperty(tag + propType, typeBoolean);
				} else if (option.defaultValue instanceof String) {
					config.addProperty(tag + propType, typeString);
				} else if (option.defaultValue instanceof Double) {
					config.addProperty(tag + propType, typeDouble);
				} else if (option.defaultValue instanceof Integer) {
					config.addProperty(tag + propType, typeInteger);
				}
				config.addProperty(tag + propDesc, option.description);
				config.addProperty(tag + propDefValue, option.defaultValue);
				index++;
			} catch (Exception ex) {
				throw new Exception("Error reading saving " + index + ".", ex);
			}
		}
		config.save();
	}

	private Map<String, Object> getOptionValuesFromConfig(String configDir, String configName)
			throws ConfigurationException, IOException, Exception {
		FileConfiguration config = LocalParameters.getXMLConfiguration(configDir, configName, true, true);
		Map<String, Object> options = new HashMap<String, Object>();
		Integer index = 0;
		while (true) {
			String tag = "options.option(" + index + ").";
			try {
				String keyName = config.getString(tag + propKeyName);
				/*
				 * stop on last key
				 */
				if (keyName == null)
					break;
				Object option = config.getProperty(tag + propValue);
				options.put(keyName, option);
				index++;
			} catch (Exception ex) {
				throw new Exception("Error reading option.", ex);
			}
		}
		return options;
	}

	/**
	 * @param configDir
	 * @param configName
	 * @throws ConfigurationException
	 * @throws IOException
	 * @throws Exception
	 */
	public void setValuesFromConfig(String configDir, String configName) throws ConfigurationException, IOException,
			Exception {
		Map<String, Object> optionValues = getOptionValuesFromConfig(configDir, configName);
		Iterator<Map.Entry<String, UserOption>> iter = entrySet().iterator();
		Object val = null;
		containsDefault = false;
		while (iter.hasNext()) {
			Map.Entry<String, UserOption> entry = iter.next();
			UserOption option = entry.getValue();
			String key = entry.getKey();
			if ((val = optionValues.get(key)) != null) {
				if (option.value instanceof Boolean)
					option.value = Boolean.valueOf(val.toString());
				else if (option.value instanceof String)
					option.value = val.toString();
				else if (option.value instanceof Double)
					option.value = Double.valueOf(val.toString());
				else if (option.value instanceof Integer)
					option.value = Integer.valueOf(val.toString());
				else
					option.value = val;
			} else
				containsDefault = true;
		}
	}

	/**
	 * @param configDir
	 * @param configName
	 * @throws ConfigurationException
	 * @throws IOException
	 * @throws Exception
	 */
	public void saveValuesToConfig(String configDir, String configName) throws ConfigurationException, IOException,
			Exception {
		FileConfiguration config = LocalParameters.getXMLConfiguration(configDir, configName, true, true);
		//after clear we need to save and then reload to ensure all items are removed
		config.clear();
		config.save();
		config = LocalParameters.getXMLConfiguration(configDir, configName, true, true);
		config.addProperty(propTitle, title);
		Integer index = 0;
		Iterator<Map.Entry<String, UserOption>> iter = entrySet().iterator();
		while (iter.hasNext()) {
			try {
				String tag = "options.option(" + index + ").";
				Map.Entry<String, UserOption> entry = iter.next();
				config.addProperty(tag + propKeyName, entry.getKey());
				config.addProperty(tag + propValue, entry.getValue().value);
				index++;
			} catch (Exception ex) {
				throw new Exception("Error saving " + index, ex);
			}
		}
		config.save();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof UserOptions))
			return false;
		UserOptions options = (UserOptions) o;
		if (!options.title.equals(title))
			return false;
		if (options.size() != size())
			return false;
		Iterator<Map.Entry<String, UserOption>> iter = entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, UserOption> entry = iter.next();
			if (!options.get(entry.getKey()).equals(entry.getValue()))
				return false;
		}
		return true;
	}

	/**
	 * @param configDirTemplate
	 * @param configNameTemplate
	 * @param configDirValues
	 * @param configNameValues
	 * @return UserOptions
	 * @throws ConfigurationException
	 * @throws IOException
	 * @throws Exception
	 */
	public static UserOptions getUserOptionsFromConfig(String configDirTemplate, String configNameTemplate,
			String configDirValues, String configNameValues) throws ConfigurationException, IOException, Exception {
		UserOptions newOptions = UserOptions.createFromTemplate(configDirTemplate, configNameTemplate);
		newOptions.setValuesFromConfig(configDirValues, configNameValues);
		return newOptions;
	}

	/*
	 * functions to allow the options to be got from the gui via the SimpleServlet device on the ObjectServer
	 */
	/**
	 * @param args
	 * @return UserOptions
	 * @throws ConfigurationException
	 * @throws IOException
	 * @throws Exception
	 */
	public static UserOptions __getUserOptionsFromConfig(String args) throws ConfigurationException, IOException,
			Exception {
		String[] s = args.split("&");
		return getUserOptionsFromConfig(s[0], s[1], s[2], s[3]);
	}

	/**
	 * @param configDirTemplate
	 * @param configNameTemplate
	 * @param configDirValues
	 * @param configNameValues
	 * @return UserOptions
	 * @throws DeviceException
	 */
	public static UserOptions getUserOptionsFromConfigFromGUI(String configDirTemplate, String configNameTemplate,
			String configDirValues, String configNameValues) throws DeviceException {
		return (UserOptions) SimpleServletAdapter.runServlet(UserOptions.class.getName(), "__getUserOptionsFromConfig",
				configDirTemplate + "&" + configNameTemplate + "&" + configDirValues + "&" + configNameValues);
	}

	/*
	 * functions to allow the options to be saved from the gui via the SimpleServlet device on the ObjectServer
	 */
	/**
	 * this function runs on the ObjectServer
	 *
	 * @param args
	 * @param options
	 * @throws ConfigurationException
	 * @throws IOException
	 * @throws Exception
	 */
	public static void __saveValuesToConfig(String args, UserOptions options) throws ConfigurationException,
			IOException, Exception {
		String[] s = args.split("&");
		options.saveValuesToConfig(s[0], s[1]);
	}

	/**
	 * this function runs on the gui
	 *
	 * @param configDirValues
	 * @param configNameValues
	 * @throws DeviceException
	 */
	public void saveValuesToConfigFromGUI(String configDirValues, String configNameValues) throws DeviceException {
		SimpleServletAdapter.runServlet(UserOptions.class.getName(), "__saveValuesToConfig", configDirValues + "&"
				+ configNameValues, this);
	}

	/*
	 * The following uses the above to provide access to a default set of properties defined by
	 * gda.util.userOptions.defaultConfigName
	 */
	private static String configDirTemplate = null, configNameTemplate = null,
			configNameValues = null;

	private static String configDirValues(){
		return VisitPath.getVisitPath();
	}
	static {
		configDirTemplate = LocalProperties.get("gda.util.userOptions.configDirTemplate");
		if (configDirTemplate == null) {
			//go back to previous behaviour
			configDirTemplate = LocalProperties.get(LocalProperties.GDA_CONFIG) + File.separator + "xml";
		}
		if (configDirTemplate == null) {
			throw new IllegalArgumentException("gda.config not defined");
		}
		configNameValues = LocalProperties.get("gda.util.userOptions.defaultConfigName");
		if (configNameValues == null) {
			logger.warn("gda.util.userOptions.defaultConfigName not defined. Using \"GDAUserOptions\".");
			configNameValues = "GDAUserOptions";
		}
		configNameTemplate = configNameValues + "Template";

	}

	/**
	 * @return UserOptions
	 * @throws DeviceException
	 */
	public static UserOptions getUserOptionsFromGUI() throws DeviceException {
		return UserOptions.getUserOptionsFromConfigFromGUI(configDirTemplate, configNameTemplate, configDirValues(),
				configNameValues);
	}

	public static UserOptions getUserOptionsFromRcpGui() throws DeviceException {
		final String visitDirectory = PathConstructor.getClientVisitDirectory();
		final UserOptions options = getUserOptionsFromConfigFromGUI(configDirTemplate, configNameTemplate, visitDirectory, configNameValues);
		return options;
	}

	/**
	 * @param options
	 * @throws DeviceException
	 */
	public static void saveUserOptionsFromGUI(UserOptions options) throws DeviceException {
		options.saveValuesToConfigFromGUI(configDirValues(), configNameValues);
	}

	public static void saveUserOptionsFromRcpGui(UserOptions options) throws DeviceException {
		final String visitDirectory = PathConstructor.getClientVisitDirectory();
		options.saveValuesToConfigFromGUI(visitDirectory, configNameValues);
	}

	/**
	 * @return UserOptions
	 * @throws ConfigurationException
	 * @throws IOException
	 * @throws Exception
	 */
	public static UserOptions getUserOptions() throws ConfigurationException, IOException, Exception {
		return UserOptions.getUserOptionsFromConfig(configDirTemplate, configNameTemplate, configDirValues(),
				configNameValues);
	}

	/**
	 * @param options
	 * @throws DeviceException
	 */
	public static void saveUserOptions(UserOptions options) throws DeviceException {
		options.saveValuesToConfigFromGUI(configDirValues(), configNameValues);
	}

}
