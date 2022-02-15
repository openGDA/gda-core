package uk.ac.diamond.daq.server.configuration.properties;

import org.apache.commons.configuration.ConfigurationException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.JakartaPropertiesConfig;
import gda.configuration.properties.PropertiesConfig;
import uk.ac.diamond.daq.server.configuration.ConfigurationDefaults;
import uk.ac.diamond.daq.services.PropertyService;

/**
 * A service implementation to provide access to GDA properties. If a service
 * needs access to properties, depending on this service is the preferred way to
 * access them.
 * 
 * @since 9.6
 * @author James Mudd
 */
@Component(name="GdaPropertyService", immediate=true)
public class GdaPropertyService implements PropertyService {
	private static final Logger logger = LoggerFactory.getLogger(GdaPropertyService.class);

	private final PropertiesConfig propConfig = new JakartaPropertiesConfig();

	@Activate
	public void activate() throws ConfigurationException {
		ConfigurationDefaults.initialise();
		final String rootPropertyFile = ConfigurationDefaults.APP_PROPERTIES_FILE.toString();
		// Note logging is not setup here so will go to stdout
		logger.info("Loading properties from: {}", rootPropertyFile);
		propConfig.loadPropertyData(rootPropertyFile);
		logger.info("Loaded properties sucessfully from property files");
	}

	@Override
	public String getAsString(String property, String defaultValue) {
		return propConfig.getString(property, defaultValue);
	}

	@Override
	public int getAsInt(String property, int defaultValue) {
		return propConfig.getInteger(property, defaultValue);
	}

	@Override
	public double getAsDouble(String property, double defaultValue) {
		return propConfig.getDouble(property, defaultValue);
	}

	@Override
	public boolean getAsBoolean(String property, boolean defaultValue) {
		return propConfig.getBoolean(property, defaultValue);
	}

	@Override
	public boolean isSet(String property) {
		return propConfig.containsKey(property);
	}

	@Override
	@Deprecated
	public void set(String property, String value) {
		logger.warn("Setting the property '{}' to '{}'. This feature will be removed in the future", property, value);
		propConfig.setString(value, property);
	}

}
