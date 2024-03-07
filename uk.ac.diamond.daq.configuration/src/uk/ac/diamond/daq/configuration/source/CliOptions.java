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

package uk.ac.diamond.daq.configuration.source;

import static java.util.stream.Collectors.toMap;
import static uk.ac.diamond.daq.configuration.ConfigurationOptions.Action.APPEND;
import static uk.ac.diamond.daq.configuration.ConfigurationOptions.Action.OVERWRITE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.core.runtime.Platform;

import uk.ac.diamond.daq.configuration.ConfigurationOptions;
import uk.ac.diamond.daq.configuration.ConfigurationSource;
import uk.ac.diamond.daq.configuration.ConfigurationOptions.Action;

/**
 * Wrapper around options, flags and arguments passed via the command line <br>
 * By default, all options are appended to any that may be provided by ConfigurationSources with
 * lower precedence but this can be changed on a per-option basis by passing the relevant {@code
 * --no-default-<option>} flags
 */
public record CliOptions(CliOptions.CliArgs cli) implements ConfigurationSource {
	/** Prefix added to option name to act as override flag */
	private static final String DEFAULT_PREFIX = "default-";
	/** Repeatable option to add a properties file */
	private static final String PROPERTIES_OPT = "properties";
	/** Repeatable option to add a logging configuration file */
	private static final String LOGGING_OPT = "logging";
	/** Repeatable option to add a spring configuration xml file */
	private static final String SPRING_XML_OPT = "spring-xml";
	/** Repeatable option to add a spring profile */
	private static final String PROFILE_OPT = "profile";

	/**
	 * Map of single character flags or options that should be mapped to longer versions for
	 * convenience
	 */
	private static final Map<String, String> ALIASES =
			Map.of(
					"c", "gda-config",
					"p", PROFILE_OPT,
					"x", SPRING_XML_OPT,
					"l", LOGGING_OPT,
					"k", PROPERTIES_OPT, // p is taken by profiles and k => key/value isn't
					// too great a stretch
					"P", "no-" + DEFAULT_PREFIX + PROFILE_OPT); // used as a flag rather than option

	/** Default flags that should be applied when parsing the command line args */
	/*
	 * These are set as defaults so that flags are phrased in the terms of opting out
	 * of defaults (eg, --no-default-x) rather than opting in to overwriting behaviour
	 * and requiring --overwrite-x or similar.
	 */
	private static final Set<String> DEFAULT_FLAGS =
			Set.of(
					DEFAULT_PREFIX + PROFILE_OPT,
					DEFAULT_PREFIX + SPRING_XML_OPT,
					DEFAULT_PREFIX + PROPERTIES_OPT,
					DEFAULT_PREFIX + LOGGING_OPT);

	/** The options that should be repeatable to accept multiple options */
	private static final Set<String> MULTI_VALUE_OPTIONS =
			Set.of(
					PROFILE_OPT,
					SPRING_XML_OPT,
					LOGGING_OPT,
					PROPERTIES_OPT
					);

	/**
	 * Get a stream of values for the associated key where they are passed in a single,
	 * comma-separated string value
	 */
	private ConfigurationOptions getConfigOptions(String key) {
		var values = Optional.ofNullable(cli.varOptions.get(key))
				.map(List::stream)
				.orElseGet(Stream::empty);
		var action = actionFromFlag(key);
		return new ConfigurationOptions(values, action);
	}

	@Override
	public ConfigurationOptions getSpringXml() {
		return getConfigOptions(SPRING_XML_OPT);
	}

	@Override
	public ConfigurationOptions getProfiles() {
		return getConfigOptions(PROFILE_OPT);
	}

	@Override
	public ConfigurationOptions getPropertiesFiles() {
		return getConfigOptions(PROPERTIES_OPT);
	}

	@Override
	public ConfigurationOptions getLoggingConfiguration() {
		return getConfigOptions(LOGGING_OPT);
	}

	@Override
	public Map<String, String> getProperties() {
		return cli.options.entrySet().stream()
				.collect(toMap(e -> toGdaProperty(e.getKey()), Entry::getValue));
	}

	/** Get a single property passed in directly - does not read from property files */
	public Optional<String> getProperty(String key) {
		return Optional.ofNullable(cli.options.get(fromGdaProperty(key)));
	}

	/** Get the config directory if one was specified on the command line */
	public Optional<String> configDirectory() {
		return getProperty("gda-config");
	}

	/** Check whether a given option should append to, or overwrite, other configuration sources */
	private Action actionFromFlag(String flag) {
		return cli.flags.contains(DEFAULT_PREFIX + flag) ? APPEND : OVERWRITE;
	}

	/** Convert a key/flag passed via a --kebab-case-name into a dot.separated.name used by GDA */
	private static String toGdaProperty(String key) {
		return key.replace("-", ".");
	}

	/** Convert a name from a gda.dotted.name to a kebab-case-name used by CLI options/flags */
	private static String fromGdaProperty(String key) {
		return key.replace(".", "-");
	}

	/** Parse CLI options from default process argv values */
	public static CliOptions build() {
		return parse(Platform.getApplicationArgs());
	}

	/** Parse CLI options from a pre-split array of strings */
	// This parser has no handling of things like strings or variable escaping
	public static CliOptions parse(String... argv) {
		return new CliOptions(new CliArgs(DEFAULT_FLAGS, MULTI_VALUE_OPTIONS, ALIASES).parse(argv));
	}

	/**
	 * Intentionally very basic argument parser. Supports
	 *
	 * <dl>
	 *   <dt>--key value
	 *   <dd>Long form options
	 *   <dt>-k value
	 *   <dd>Short form options
	 *   <dt>--flag
	 *   <dd>Long form flags
	 *   <dt>-f
	 *   <dd>Short form flags
	 *   <dt>-abc
	 *   <dd>Combined short flags, equivalent to -a -b -c
	 * </dl>
	 */
	public static class CliArgs {
		/** Map of aliases for options to allow short keys to be used in place of long keys */
		private final Map<String, String> aliases;

		private final Map<String, String> options;
		private final Map<String, List<String>> varOptions;
		private final Set<String> flags;
		private final List<String> args;

		/** The last option read - may be either a flag or an option depending on what follows */
		private String key;

		/** We have parsed a -- to mark the end of flags and options */
		private boolean complete;

		/**
		 * @param defaultFlags Flags that are assumed to be set by default - allows {@code
		 *	 --no-default-flag} options to remove defaults.
		 * @param varOpts Options that can be repeated to take a variable number of values
		 * @param aliases Mapping of alias to name - allows short flags to behave like long
		 *	 equivalents.
		 */
		public CliArgs(Set<String> defaultFlags, Set<String> varOpts, Map<String, String> aliases) {
			this.flags = new HashSet<>(defaultFlags);
			this.aliases = aliases;
			this.options = new HashMap<>();
			this.varOptions = varOpts.stream().collect(toMap(k -> k, k -> new ArrayList<>()));
			this.args = new ArrayList<>();
		}

		/**
		 * Parse the given args and process args, flags and options.
		 *
		 * @param argv unprocessed array of arguments passed to the process
		 */
		/*
		 * Basic premise is to parse args in a single pass, only handling a key
		 * when the next arg is read to determine how it should be interpreted.
		 * Any argument following a --key/-k is assumed to be an associated
		 * value, if two consecutive --keys are encountered, the first is
		 * assumed to be a boolean flag that takes no value. If a flag matches
		 * --no-xyz, it is taken to negate the equivalent --xyz flag.
		 */
		public CliArgs parse(String[] argv) {
			key = null;
			complete = false;
			for (var arg : argv) {
				if (complete) {
					// We've passed a -- so anything else should be interpreted literally
					args.add(arg);
				} else if (arg.startsWith("--")) { // start of long option/flag
					processLongOption(arg);
				} else if (arg.startsWith("-")) { // start of short option/flag/compact-flags
					processShortOption(arg);
				} else if (key == null) { // we're not expecting a value so add to arguments
					args.add(arg);
				} else { // We're expecting a value to set as value of pending option
					var option = aliases.getOrDefault(key, key);
					if (varOptions.containsKey(option)) {
						varOptions.get(option).add(arg);
					} else {
						options.put(option, arg);
					}
					key = null;
				}
			}
			if (key != null) {
				setFlag(key);
			}
			return this;
		}

		/**
		 * Handle an argument that starts with --. Mark any pending argument as a flag and set this
		 * argument as pending (unless it is only '--' in which case mark flag parsing as complete).
		 *
		 * @param arg starting with '--' to indicate a long flag/option
		 */
		private void processLongOption(String arg) {
			if (key != null) {
				setFlag(key);
			}
			if (arg.length() == 2) {
				complete = true;
			} else {
				key = arg.substring(2);
			}
		}

		/**
		 * Handle an argument that starts with - (but not --). Mark any pending argument as a flag
		 * and set this argument as pending.
		 *
		 * @param arg starting with '-' to indicate a short flag/option
		 */
		private void processShortOption(String arg) {
			if (key != null) {
				setFlag(key);
			}
			if (arg.length() > 2) {
				for (var c : arg.substring(1).toCharArray()) {
					String flag = String.valueOf(c);
					setFlag(flag);
				}
				key = null;
			} else {
				key = arg.substring(1);
			}
		}

		/**
		 * Set or unset a flag using the {@code --no-flag-name} convention to unset {@code
		 * --flag-name}
		 */
		private void setFlag(String flag) {
			flag = aliases.getOrDefault(flag, flag);
			if (flag.startsWith("no-")) {
				flags.remove(flag.substring(3));
			} else {
				flags.add(flag);
			}
		}

		public Map<String, String> options() {
			return options;
		}

		public Map<String, List<String>> varOptions() {
			return varOptions;
		}

		public Set<String> flags() {
			return flags;
		}

		public List<String> args() {
			return args;
		}

		@Override
		public String toString() {
			return "CliArgs [options=" + options + ", varOptions=" + varOptions + ", flags=" + flags + ", args=" + args
					+ "]";
		}

	}
}
