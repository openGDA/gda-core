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

package uk.ac.diamond.daq.server.configuration;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.concat;
import static uk.ac.diamond.daq.classloading.GDAClassLoaderService.temporaryClassLoader;
import static uk.ac.diamond.daq.server.configuration.ConfigurationOptions.effectiveOptions;

import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.EnvironmentConfiguration;
import org.apache.commons.configuration2.MapConfiguration;
import org.apache.commons.configuration2.SystemConfiguration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompositeBeamlineConfiguration implements BeamlineConfiguration {
	private static final String MODE_PROPERTY = "gda.mode";

	private static final Logger logger =
			LoggerFactory.getLogger(CompositeBeamlineConfiguration.class);

	/** Path used to resolve all relative paths in configuration */
	private final Path root;

	/** List of sources, in order of precedence, providing configuration to this GDA instance */
	private final List<ConfigurationSource> sources;

	/** List of sources, in order of precedence, provided by built plugins */
	private final List<CoreConfigurationSource> coreSources;

	/** Cached properties read from properties files returned by ConfigurationSource */
	private final Configuration properties;

	public CompositeBeamlineConfiguration(
			Path root,
			Stream<ConfigurationSource> sources,
			List<CoreConfigurationSource> coreSources) {
		this.root = root;
		this.coreSources = coreSources;
		var initialSourceList = sources.toList();
		var mode = initialSourceList.stream()
			.map(ConfigurationSource::getProperties)
			.map(p -> p.get(MODE_PROPERTY))
			.filter(Objects::nonNull)
			.findFirst()
			.orElseGet(() -> System.getProperty(MODE_PROPERTY));

		// If there is a mode set at this point, use it to load mode specific fields
		var sourceList = mode != null
				? initialSourceList.stream()
						.map(cs -> cs instanceof ModeAwareConfigSource macs ? macs.withMode(mode) : cs)
						.toList()
				: initialSourceList;

		properties = buildProperties(root, sourceList, coreSources);

		// If the mode present after loading properties is different, something odd has happened
		// eg, `gda.mode = dummy` in a live properties file.
		var newMode = properties.getString(MODE_PROPERTY);
		if (!Objects.equals(mode, newMode)) {
			throw new IllegalStateException("gda.mode changed while loading properties - GDA now in unknwon state");
		}
		this.sources = sourceList;
	}

	private static Configuration buildProperties(
			Path root,
			List<ConfigurationSource> sources,
			List<CoreConfigurationSource> coreSources) {
		logger.debug("Building config from user sources: {}", sources);
		logger.debug("Building config from core sources: {}", coreSources);
		// Without the classloader, commons configurations classes can't see each other
		try (var ccl = temporaryClassLoader()) {
			var combined = new CombinedConfiguration();
			var directProperties =
					sources.stream()
							.flatMap(s -> s.getProperties().entrySet().stream())
							.collect(
									toMap(
											Entry::getKey,
											Entry::getValue,
											(a, b) -> a)); // first value wins
			combined.addConfiguration(new MapConfiguration(directProperties));
			combined.addConfiguration(new EnvironmentConfiguration());
			combined.addConfiguration(new SystemConfiguration());

			var subs = translator(sources);
			var interp = combined.getInterpolator();
			var runtimeFiles =
					effectiveOptions(sources, ConfigurationSource::getPropertiesFiles)
							.map(subs::replace)
							.map(root::resolve)
							.map(CompositeBeamlineConfiguration::uncheckedUrl);
			var coreFiles =
					coreSources.stream().flatMap(CoreConfigurationSource::getPropertiesFiles);
			concat(runtimeFiles, coreFiles)
					.forEach(
							f -> {
								try {
									var params = new Parameters()
											.properties()
											.setParentInterpolator(interp)
											.setURL(f);
									var builder = new Configurations().propertiesBuilder().configure(params);
									logger.debug("Adding properties from {}", f);
									combined.addConfiguration(builder.getConfiguration());
								} catch (ConfigurationException e) {
									throw new IllegalStateException(
											"Couldn't load properties from '%s'".formatted(f), e);
								}
							});
			// I don't know why this needs to be called twice but without it there are
			// no properties available
			combined.interpolatedConfiguration();
			return combined.interpolatedConfiguration();
		}
	}

	/**
	 * Build a StringSubstitutor that replaces place-holders in text with all properties available
	 * to this configuration, including those loaded from the properties files.
	 *
	 * @return A StringSubstutor using all properties known to this configuration
	 */
	private StringSubstitutor translator() {
		return new StringSubstitutor(properties::getString);
	}

	/**
	 * Build a StringSubstitutor that replaces place-holders in text with the direct properties
	 * available in {@link #sources ConfigurationSources}. This does not include properties loaded
	 * from files listed in ConfigurationSources.
	 *
	 * @param sources The list of sources providing properties as a source of replacements
	 * @return A StringSubstitutor using the properties available directly from sources
	 */
	private static StringSubstitutor translator(List<ConfigurationSource> sources) {
		var properties =
				sources.stream()
						.flatMap(s -> s.getProperties().entrySet().stream())
						.collect(toMap(Entry::getKey, Entry::getValue, (l, r) -> l));
		return new StringSubstitutor(properties::get);
	}

	private static URL uncheckedUrl(Path path) {
		try {
			return path.toUri().toURL();
		} catch (MalformedURLException e) {
			throw new UncheckedIOException(e);
		}
	}

	/** Get all the xml files that should be used to build the server SpringContext */
	@Override
	public Stream<URL> getSpringXml() {
		return concat(
				resolvedPaths(ConfigurationSource::getSpringXml),
				coreSources.stream().flatMap(CoreConfigurationSource::getSpringXml));
	}

	/** Get all the properties files from which LocalProperties should be loaded */
	// Hopefully this will not be required if LocalProperties can be backed by this configuration
	@Override
	public Stream<URL> getPropertiesFiles() {
		return concat(
				effectiveOptions(sources, ConfigurationSource::getPropertiesFiles)
						.map(translator(sources)::replace)
						.map(root::resolve)
						.map(CompositeBeamlineConfiguration::uncheckedUrl),
				coreSources.stream().flatMap(CoreConfigurationSource::getPropertiesFiles));
	}

	/** Get all files that should be loaded to configure the logging framework */
	@Override
	public Stream<URL> getLoggingConfiguration() {
		return concat(
				resolvedPaths(ConfigurationSource::getLoggingConfiguration),
				coreSources.stream().flatMap(CoreConfigurationSource::getLoggingConfiguration));
	}

	/** Get all the profiles that should be used when loading the SpringContext */
	@Override
	public Stream<String> getProfiles() {
		return effectiveOptions(sources, ConfigurationSource::getProfiles);
	}

	@Override
	public Configuration properties() {
		return properties;
	}

	@Override
	public Map<String, String> properties(Predicate<String> keyFilter) {
		Iterable<String> keys = properties::getKeys;
		return StreamSupport.stream(keys.spliterator(), false)
				.filter(keyFilter)
				.collect(toMap(k -> k, properties::getString));
	}

	/**
	 * Build a stream of file paths (as strings) from all configurations options returned from the
	 * configuration {@link #sources}, extracted by the given function. <br>
	 * The overwrite/append preferences are taken into account and any <code>${place-holders}</code>
	 * are replaced with the properties they refer to. <br>
	 * Relative paths are resolved against the root of this configuration.
	 *
	 * @param extractor A function to extract a {@link ConfigurationOptions} from a
	 *	 {@link ConfigurationSource}
	 * @return A Stream of file paths relative to {@link #root} with place-holders resolved
	 */
	private Stream<URL> resolvedPaths(
			Function<ConfigurationSource, ConfigurationOptions> extractor) {
		return effectiveOptions(sources, extractor)
				.map(translator()::replace)
				.map(root::resolve)
				.map(CompositeBeamlineConfiguration::uncheckedUrl);
	}

	@Override
	public String toString() {
		return "CompositeBeamlineConfiguration [root=" + root + ", sources=" + sources + "]";
	}

	@Override
	public Map<String, String> directProperties() {
		return sources.stream()
				.flatMap(s -> s.getProperties().entrySet().stream())
				.collect(toMap(Entry::getKey, Entry::getValue, (a, b) -> a));
	}

	public Map<String, String> systemProperties() {
		var translator = translator();
		return sources.stream()
				.map(ConfigurationSource::systemProperties)
				.map(Map::entrySet) // arbitrary order within each source's properties
				.flatMap(Set::stream) // but stream still sorted by source
				.collect(
						toMap(
								Entry::getKey,
								entry -> translator.replace(entry.getValue()),
								(l, r) -> l)); // first sources override later ones
	}

	/**
	 * Debugging utility to print out full config including what each source contributes
	 * and the combined + interpolated results. Individual properties loaded from files
	 * are not included as they are more likely to include sensitive values.
	 */
	public void printFullDebugDetails() {
		try (var debug = new Formatter(new StringBuilder())) {
			debug.format("Root config directory: %s%n", root);
			debug.format("Using %d user sources, %d core sources%n", sources.size(), coreSources.size());
			BiConsumer<ConfigurationOptions, String> showOptions = (opts, name) -> {
				debug.format("        %s: %s%n", name, opts.action());
				opts.options().forEach(f -> debug.format("            %s%n", f));
			};
			BiConsumer<Stream<?>, String> showValues = (values, name) -> {
				debug.format("    %s%n", name);
				values.forEach(f -> debug.format("        %s%n", f));
			};

			// List user provided sources (config files + CLI + env)
			for (var src: sources) {
				debug.format("    User: %s%n", src);
				showOptions.accept(src.getSpringXml(), "Spring XML");
				showOptions.accept(src.getPropertiesFiles(), "Properties");
				showOptions.accept(src.getLoggingConfiguration(), "Logging");
				showOptions.accept(src.getProfiles(), "Profiles");

				debug.format("        System: %s%n", src.systemProperties());
				debug.format("        Defaults: %s%n", src.getProperties());
			}

			// List sources from compiled plugins
			for (var src: coreSources) {
				debug.format("    Core: %s%n", src);
				debug.format("        Spring XML: %s%n", src.getSpringXml().toList());
				debug.format("        Properties: %s%n", src.getPropertiesFiles().toList());
				debug.format("        Logging: %s%n", src.getLoggingConfiguration().toList());
			}

			// List combined configuration from all sources
			debug.format("Effective combined configuration%n");
			showValues.accept(getSpringXml(), "Spring XML");
			showValues.accept(getPropertiesFiles(), "Properties files");
			showValues.accept(getLoggingConfiguration(), "Logging");
			showValues.accept(getProfiles(), "Profiles");
			showValues.accept(directProperties().entrySet().stream(), "Direct Properties");
			showValues.accept(systemProperties().entrySet().stream(), "System Properties");

			System.out.println(debug); // called before logging is configured so sent to stdout anyway
		}
	}
}
