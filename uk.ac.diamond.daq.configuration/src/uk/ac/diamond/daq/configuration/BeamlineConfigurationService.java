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

package uk.ac.diamond.daq.configuration;

import static java.lang.Boolean.getBoolean;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.configuration2.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.configuration.properties.ConfigurationServicePropertyConfig;
import uk.ac.diamond.daq.configuration.source.CliOptions;
import uk.ac.diamond.daq.configuration.source.GdaEnvironment;
import uk.ac.diamond.daq.configuration.source.directory.ConfigRef;
import uk.ac.diamond.daq.services.PropertyService;

public class BeamlineConfigurationService implements BeamlineConfiguration, PropertyService {
	private static final String GDA_CONFIG_DEBUG = "gda.config.debug";
	private static final Logger logger =
			LoggerFactory.getLogger(BeamlineConfigurationService.class);


	private List<CoreConfigurationSource> coreConfig = new ArrayList<>();

	private BeamlineConfiguration config;

	public void activate(Map<String, String> properties) {
		var process = properties.get("process");
		if (process == null || process.isBlank()) {
			throw new IllegalArgumentException("A process name is required as a service property");
		}

		var workingDirectory = System.getProperty("user.dir");
		logger.info("Building configuration with working directory: {}", workingDirectory);
		var configCli = CliOptions.build();
		logger.info("Command line args parsed to: {}", configCli);
		var configEnv = GdaEnvironment.build();
		logger.info("GDA environment: {}", configEnv);
		// --gda-config overrides $GDA_CONFIG overrides -Dgda.config overrides $PWD
		String configDirectory =
				configCli
						.configDirectory()
						.or(configEnv::configDirectory)
						.or(() -> Optional.ofNullable(System.getProperty("gda.config")))
						.orElseGet(
								() -> {
									// If gda.config hasn't been set some other way, ensure that
									// it is still available as a property
									logger.info(
											"No config directory specified - using pwd: {}",
											workingDirectory);
									System.setProperty("gda.config", workingDirectory);
									return workingDirectory;
								});
		logger.info("Resolving all configurations from directory '{}'", workingDirectory);
		var directories =
				new ConfigRef(configDirectory).resolveConfigs(process, Path.of(workingDirectory));

		var root = Path.of(configDirectory);

		var beamlineConfig =
				new CompositeBeamlineConfiguration(
						root, concat(of(configCli, configEnv), directories), coreConfig);

		logger.debug("Setting all system properties from config");
		beamlineConfig.systemProperties().forEach(System::setProperty);
		logger.debug("Setting configuration service as source of LocalProperties");
		LocalProperties.setProperties(new ConfigurationServicePropertyConfig(beamlineConfig.properties()));

		if (getBoolean(GDA_CONFIG_DEBUG)) {
			beamlineConfig.printFullDebugDetails();
		} else {
			logger.info("Set 'gda.config.debug' system property to true to enable debug logging of loaded config");
		}

		this.config = beamlineConfig;
	}

	@Override
	public Stream<URL> getSpringXml() {
		return config.getSpringXml();
	}

	@Override
	public Stream<URL> getPropertiesFiles() {
		return config.getPropertiesFiles();
	}

	@Override
	public Stream<URL> getLoggingConfiguration() {
		return config.getLoggingConfiguration();
	}

	@Override
	public Stream<String> getProfiles() {
		return config.getProfiles();
	}

	@Override
	public Configuration properties() {
		return config.properties();
	}

	@Override
	public Map<String, String> properties(Predicate<String> keyFilter) {
		return config.properties(keyFilter);
	}

	@Override
	public Map<String, String> directProperties() {
		return config.directProperties();
	}

	@Override
	public String getAsString(String property, String defaultValue) {
		return config.properties().getString(property, defaultValue);
	}

	@Override
	public int getAsInt(String property, int defaultValue) {
		return config.properties().getInt(property, defaultValue);
	}

	@Override
	public double getAsDouble(String property, double defaultValue) {
		return config.properties().getDouble(property, defaultValue);
	}

	@Override
	public boolean getAsBoolean(String property, boolean defaultValue) {
		return config.properties().getBoolean(property, defaultValue);
	}

	@Override
	public boolean isSet(String property) {
		return config.properties().containsKey(property);
	}

	@Override
	public void set(String property, String value) {
		// should be deprecated?
		config.properties().setProperty(property, value);
	}
}
