/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.FileConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.data.PathConstructor;
import gda.factory.FactoryException;
import uk.ac.diamond.daq.persistence.jythonshelf.LocalParameters;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * Implements the server-side behaviour for the UserOptionsService
 * @see UserOptionsService
 */
@SuppressWarnings("rawtypes")
@ServiceInterface(UserOptionsService.class)
public class UserOptionsManager implements UserOptionsService {

	private static final Logger logger = LoggerFactory.getLogger(UserOptionsManager.class);

	private boolean isConfigured = false;
	private String name;
	private String templateConfigDir = null; // typically ${gda.config}/xml

	private String templateConfigName = null;

	private FileConfiguration clearConfiguration(FileConfiguration config, String title)
			throws ConfigurationException {
		config.clear(); // after clear, save and then reload to ensure all items are removed
		config.save();
		config.addProperty(UserOptionsMap.propTitle, title);
		return config;
	}

	private FileConfiguration clearConfiguration(String configDir, String configName, String title)
			throws ConfigurationException, IOException {
		FileConfiguration config = LocalParameters.getXMLConfiguration(configDir, configName, true, true);
		return clearConfiguration(config, title);
	}

	private FileConfiguration clearTemplate() throws ConfigurationException, IOException {
		FileConfiguration config = LocalParameters.getXMLConfiguration(templateConfigDir, templateConfigName, true, true);
		return clearConfiguration(config, config.getString(UserOptionsMap.propTitle));
	}

	@Override
	public void configure() throws FactoryException {
		templateConfigDir = LocalProperties.get(PROP_TEMPLATE_DIRECTORY);
		if (templateConfigDir == null) { // use default ${gda.config}/xml
			templateConfigDir = LocalProperties.get(LocalProperties.GDA_CONFIG) + File.separator + "xml";
		}
		if (templateConfigDir == null) {
			throw new FactoryException("gda.config not defined for user options template");
		}

		String templateName = LocalProperties.get(PROP_OPTIONS_FILENAME);
		if (templateName == null) {
			logger.warn("gda.util.userOptions.defaultConfigName not defined. Using \"GDAUserOptions\".");
			templateName = "GDAUserOptions";
		}
		templateConfigName = templateName + "Template";

		isConfigured = true;
	}

	@Override
	public UserOptionsMap createOptionsMapFromTemplate() throws ConfigurationException, IOException {
		FileConfiguration template = LocalParameters.getXMLConfiguration(templateConfigDir, templateConfigName, false, true);
		UserOptionsMap templateOptions = new UserOptionsMap();
		templateOptions.setTitle(template.getString(UserOptionsMap.propTitle));
		templateOptions.setIsDefault(true);
		return this.setOptionsMapFromConfig(templateOptions, template, template);
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public UserOptionsMap getOptions(String configDir, String configName) throws ConfigurationException, IOException {
		return getOptionsMapFromConfig(configDir, configName);
	}

	@Override
	public UserOptionsMap getOptionsCurrent() throws ConfigurationException, IOException {
		String optionsDirectory = PathConstructor.getClientVisitDirectory();
		String optionsFilename = LocalProperties.get(PROP_OPTIONS_FILENAME);
		return getOptionsMapFromConfig(optionsDirectory, optionsFilename);
	}

	public UserOptionsMap getOptionsMapFromConfig(String configDir, String configName) throws ConfigurationException, IOException {
		FileConfiguration template = LocalParameters.getXMLConfiguration(templateConfigDir, templateConfigName, false, true);
		FileConfiguration config = LocalParameters.getXMLConfiguration(configDir, configName, true, true);
		UserOptionsMap options = createOptionsMapFromTemplate();
		options = this.setOptionsMapFromConfig(options, template, config);
		options.setIsDefault(false);
		return options;
	}

	public String getTemplateConfigDir() {
		return templateConfigDir;
	}

	public String getTemplateConfigName() {
		return templateConfigName;
	}

	@Override
	public Boolean hasTemplate() {
		boolean isOk = isConfigured();
		if (isOk) {
			File template = new File(templateConfigDir + File.separator + templateConfigName + ".xml");
			isOk = template.exists();
		}
		return isOk;
	}

	@Override
	public boolean isConfigured() {
		return this.isConfigured;
	}

	@Override
	public UserOptionsMap resetOptions(String configDir, String configName)
			throws ConfigurationException, IOException {
		UserOptionsMap options = createOptionsMapFromTemplate();
		options.setIsDefault(true);
		return saveOptionsMapValuesToConfig(configDir, configName, options);
	}

	@Override
	public UserOptionsMap saveOptions(String configDir, String configName, UserOptionsMap options) throws ConfigurationException, IOException {
		return this.saveOptionsMapValuesToConfig(configDir, configName, options);
	}

	@Override
	public UserOptionsMap saveOptionsCurrent(UserOptionsMap options) throws ConfigurationException, IOException {
		String optionsDirectory = PathConstructor.getClientVisitDirectory();
		String optionsFilename = LocalProperties.get(PROP_OPTIONS_FILENAME);
		return saveOptionsMapValuesToConfig(optionsDirectory, optionsFilename, options);
	}

	public void saveOptionsMapToTemplate(UserOptionsMap options) throws ConfigurationException, IOException {
		this.clearTemplate();
		FileConfiguration config = LocalParameters.getXMLConfiguration(templateConfigDir, templateConfigName, true, true);
		config.addProperty(UserOptionsMap.propTitle, options.getTitle());
		Integer index = 0;
		Iterator<Map.Entry<String, UserOption>> iter = options.entrySet().iterator();
		while (iter.hasNext()) {
			try {
				Map.Entry<String, UserOption> entry = iter.next();
				@SuppressWarnings("unchecked")
				UserOption<String, Object> option = entry.getValue();
				String tag = "options.option(" + index + ").";
				config.addProperty(tag + UserOptionsMap.propKeyName, entry.getKey());
				if (option.defaultValue instanceof Boolean) {
					config.addProperty(tag + UserOptionsMap.propType, UserOptionsMap.typeBoolean);
				} else if (option.defaultValue instanceof String) {
					config.addProperty(tag + UserOptionsMap.propType, UserOptionsMap.typeString);
				} else if (option.defaultValue instanceof Double) {
					config.addProperty(tag + UserOptionsMap.propType, UserOptionsMap.typeDouble);
				} else if (option.defaultValue instanceof Integer) {
					config.addProperty(tag + UserOptionsMap.propType, UserOptionsMap.typeInteger);
				}
				config.addProperty(tag + UserOptionsMap.propDesc, option.description);
				config.addProperty(tag + UserOptionsMap.propDefValue, option.defaultValue);
				index++;
			} catch (Exception ex) {
				throw new ConfigurationException("Error reading saving " + index + ".", ex);
			}
		}
		config.save();
	}

	public UserOptionsMap saveOptionsMapValuesToConfig(String configDir, String configName, UserOptionsMap options)
			throws ConfigurationException, IOException {
		FileConfiguration config = this.clearConfiguration(configDir, configName, options.getTitle());
		Integer index = 0;
		Iterator<Map.Entry<String, UserOption>> iter = options.entrySet().iterator();
		while (iter.hasNext()) {
			try {
				String tag = "options.option(" + index + ").";
				Map.Entry<String, UserOption> entry = iter.next();
				config.addProperty(tag + UserOptionsMap.propKeyName, entry.getKey());
				config.addProperty(tag + UserOptionsMap.propValue, entry.getValue().value);
				index++;
			} catch (Exception ex) {
				throw new ConfigurationException("Error saving " + index, ex);
			}
		}
		config.save();
		return getOptionsMapFromConfig(configDir, configName);
	}

	public void setConfigured(boolean flag) {
		isConfigured = flag;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	private UserOptionsMap setOptionsMapFromConfig(UserOptionsMap options, FileConfiguration template, FileConfiguration config)
			throws ConfigurationException {
		Integer index = 0;
		while (true) {
			String tag = "options.option(" + index + ").";
			try {
				String keyName = template.getString(tag + UserOptionsMap.propKeyName);
				if (keyName == null) break; // stop after last key

				String description = template.getString(tag + UserOptionsMap.propDesc);
				String valtype = template.getString(tag + UserOptionsMap.propType);
				UserOption<String, ? extends Object> option = null;

				if (valtype.contentEquals(UserOptionsMap.typeBoolean)) {
					Boolean defValue = template.getBoolean(tag + UserOptionsMap.propDefValue);
					Boolean value = config.getBoolean(tag + UserOptionsMap.propValue, defValue);
					option = new UserOption<String, Boolean>(description, defValue, value);
				} else if (valtype.contentEquals(UserOptionsMap.typeString)) {
					String defValue = template.getString(tag + UserOptionsMap.propDefValue);
					String value = config.getString(tag + UserOptionsMap.propValue, defValue);
					option = new UserOption<String, String>(description, defValue, value);
				} else if (valtype.contentEquals(UserOptionsMap.typeDouble)) {
					Double defValue = template.getDouble(tag + UserOptionsMap.propDefValue);
					Double value = config.getDouble(tag + UserOptionsMap.propValue, defValue);
					option = new UserOption<String, Double>(description, defValue, value);
				} else if (valtype.contentEquals(UserOptionsMap.typeInteger)) {
					Integer defValue = template.getInt(tag + UserOptionsMap.propDefValue);
					Integer value = config.getInt(tag + UserOptionsMap.propValue, defValue);
					option = new UserOption<String, Integer>(description, defValue, value);
				}

				if (null != option) {
					options.put(keyName, option);
				}
				index++;
			} catch (Exception ex) {
				throw new ConfigurationException("Error reading option " + index, ex);
			}
		}
		return options;
	}

	public void setTemplateConfigDir(String templateConfigDir) {
		this.templateConfigDir = templateConfigDir;
	}

	public void setTemplateConfigName(String templateConfigName) {
		this.templateConfigName = templateConfigName;
	}

}
