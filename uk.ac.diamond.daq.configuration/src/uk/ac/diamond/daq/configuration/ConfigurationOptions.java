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

import static java.util.stream.Stream.concat;
import static uk.ac.diamond.daq.configuration.ConfigurationOptions.Action.APPEND;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Wrapper around a stream of Strings that also indicates whether subsequent options from other
 * sources should be included.
 */
public record ConfigurationOptions(Stream<String> options, ConfigurationOptions.Action action) {
	public enum Action {
		/** Replace all other configurations with the given options */
		OVERWRITE,
		/** Add the given options to the options from other configurations */
		APPEND;
	}

	/**
	 * Merge another stream of options into this one
	 *
	 * <p>The resultant stream will either be this stream if it is set to overwrite (and should
	 * therefore ignore any subsequent options) or a new stream combining the options from both
	 * streams and the overwrite behaviour of the merged options.
	 *
	 * @param other The other {@link ConfigurationOptions} to be merged into this one
	 * @return A new {@link ConfigurationOptions} that reflects the combination of the two streams
	 */
	public ConfigurationOptions merge(ConfigurationOptions other) {
		return switch (action) {
			case APPEND -> new ConfigurationOptions(concat(options, other.options), other.action);
			case OVERWRITE -> this;
		};
	}

	public static Stream<String> effectiveOptions(
			Collection<ConfigurationSource> sources,
			Function<ConfigurationSource, ConfigurationOptions> extract) {
		return sources.stream().map(extract).reduce(empty(), ConfigurationOptions::merge).options();
	}

	public static ConfigurationOptions empty() {
		return new ConfigurationOptions(Stream.empty(), APPEND);
	}
}
