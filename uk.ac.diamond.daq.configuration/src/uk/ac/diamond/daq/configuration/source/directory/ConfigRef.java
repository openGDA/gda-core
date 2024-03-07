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

package uk.ac.diamond.daq.configuration.source.directory;

import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;

import uk.ac.diamond.daq.configuration.source.directory.ConfigLayout.LayoutBuilder;

/**
 * A representation of a config directory included via the 'extras' section of a config.toml file.
 */
public record ConfigRef(Path root, Optional<ConfigFile> overrides) {
	private static final Logger logger = LoggerFactory.getLogger(ConfigRef.class);

	/** The constructor used when an included directory is specified by path only */
	@JsonCreator
	public ConfigRef(String root) {
		this(Path.of(root), Optional.empty());
	}

	/**
	 * The constructor used when an included directory is specified as a nested table within the
	 * 'extras' table, along with any optional overrides specified.
	 *
	 * @param overrides Any paths/options that should override the settings in the included
	 *	 directory's config.toml file
	 * @return a reference to an included config directory
	 */
	@JsonCreator
	public static ConfigRef parse(ConfigFile overrides) {
		var root = overrides.getRoot();
		if (root == null || root.isBlank()) {
			throw new IllegalStateException("Extra configurations need a root field");
		}
		return new ConfigRef(Path.of(root), Optional.of(overrides));
	}

	/**
	 * Recursively read config directories by following links to extra configs if present. <br>
	 * The order of the resulting stream is deterministic and follows a depth-first approach to
	 * traversing the extras of each directory. <br>
	 * No attempt is made to remove or merge duplicates if, for example, config A includes B and C,
	 * and B also includes C. This will most likely lead to errors unless care is taken to pass
	 * overrides correctly, where duplicate spring beans are defined.
	 *
	 * @param base The root path against which this included directory should be resolved. All
	 *	 included config directories are assumed to be relative to the directory that imported
	 *	 them, not to the top level 'gda.config' directory.
	 */
	public Stream<ConfigDirectory> resolveConfigs(String process, Path base) {
		var absRoot = base.resolve(root()).normalize();
		logger.info("Resolving configs from {} ({} -> {})", absRoot, base, root());
		if (!Files.exists(absRoot)) {
			logger.error("Config directory '{}' does not exist", absRoot);
			return Stream.empty();
		}
		var file = ConfigFile.read(absRoot.resolve("config.toml"));

		// Default to an empty layout
		var layout = LayoutBuilder.empty();

		// Merge in the changes from the config file
		file.map(ConfigFile::asLayout).ifPresent(layout::update);

		// Merge in the overrides from the importing config file
		overrides.map(ConfigFile::asLayout).ifPresent(layout::update);

		if (file.isEmpty() && overrides.isEmpty()) {
			logger.warn("Configuration directory {} has no config.toml and has no overrides where included", absRoot);
		}

		var local = new ConfigDirectory(absRoot, layout.build(process));
		var extras =
				file.stream()
						.flatMap(
								c -> c.extras().flatMap(cr -> cr.resolveConfigs(process, absRoot)));
		return concat(of(local), extras);
	}
}
