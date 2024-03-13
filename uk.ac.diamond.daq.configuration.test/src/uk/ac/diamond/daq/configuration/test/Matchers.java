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

package uk.ac.diamond.daq.configuration.test;

import static org.hamcrest.Matchers.contains;

import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;

import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import uk.ac.diamond.daq.configuration.ConfigurationOptions;
import uk.ac.diamond.daq.configuration.ConfigurationOptions.Action;

/** Functions for matching entities relating to GDA Configuration */
public final class Matchers {
	private Matchers() {}

	public static Matcher<Iterable<? extends URL>> containsURLs(String... paths) {
		var urls =
				Arrays.stream(paths)
						.map(
								u -> {
									try {
										return Path.of(u).toUri().toURL();
									} catch (MalformedURLException e) {
										throw new UncheckedIOException(e);
									}
								})
						.toArray(URL[]::new);
		return contains(urls);
	}

	/**
	 * Assert that the matched {@link ConfigurationOptions} has no options but has the required
	 * action
	 */
	public static Matcher<ConfigurationOptions> emptyOptions(Action override) {
		return new ConfigurationOptionsMatcher(override);
	}

	/**
	 * Assert that the match {@link ConfigurationOptions} has the given options and has the required
	 * action
	 */
	public static Matcher<ConfigurationOptions> containsOptions(
			Action override, String... options) {
		return new ConfigurationOptionsMatcher(override, options);
	}

	private static class ConfigurationOptionsMatcher
			extends CustomTypeSafeMatcher<ConfigurationOptions> {
		private String[] options;
		private String[] actual;
		private Action override;

		public ConfigurationOptionsMatcher(Action override, String... options) {
			super(
					"ConfigurationOptions (%s) containing: %s"
							.formatted(override, Arrays.toString(options)));
			this.override = override;
			this.options = options;
		}

		@Override
		protected boolean matchesSafely(ConfigurationOptions item) {
			actual = item.options().toArray(String[]::new);
			return Arrays.equals(actual, options) && item.action() == override;
		}

		@Override
		protected void describeMismatchSafely(
				ConfigurationOptions item, Description mismatchDescription) {
			mismatchDescription.appendText(
					"was ConfigurationOptions(%s, %s)"
							.formatted(item.action(), Arrays.toString(actual)));
		}
	}
}
