/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package gda.util.logging;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import gda.configuration.properties.LocalProperties;

public final class LoggingUtils {
	private LoggingUtils() { /* Prevent Instances */ }

	/**
	 * If the {@code gda.logs.dir} system property is not already set, it is set to the value of
	 * {@code gda.logs.dir} from {@code java.properties}.
	 */
	public static void setLogDirectory() {
		// The logging config files can only do parameter interpolation from system properties,
		// so check that the standard logs directory property is defined and set from java properties if possible
		String existingProperty = System.getProperty(LocalProperties.GDA_LOGS_DIR);
		if (existingProperty == null || existingProperty.isEmpty()) {
			String fromJavaProperties = LocalProperties.get(LocalProperties.GDA_LOGS_DIR);
			if (fromJavaProperties != null && !fromJavaProperties.isEmpty()) {
				System.setProperty(LocalProperties.GDA_LOGS_DIR, fromJavaProperties);
			}
		}
	}

	/** Simple function to log an abridged stack trace, as a TRACE message, to the logger provided
	 * <BR><BR>
	 * Various useless stack trace elements which don't add anything to establishing the context of a call are skipped.
	 * <BR><BR>
	 * For example:
	 * <PRE><BR>{@code
	 * logStackTrace(logger, "myFunction()");
	 * }</PRE>
	 * <BR>generates "myFunction() called from (abridged): ...".<BR><BR>
	 */
	public static void logStackTrace(Logger logger, String description) {
		final String[] prefixesToSkip = {"sun.reflect", "java.lang.reflect",
				"org.python.core", "org.python.proxies", "org.python.pycode", "org.python.util"};

		if (logger.isTraceEnabled()) {
			logger.trace("{} called from (abridged): {}", description, Arrays.stream(
				Thread.currentThread().getStackTrace())
					.skip(2) // Skip this function and the getStackTrace function itself
					.map(ste -> StringUtils.startsWithAny(ste.getClassName(), prefixesToSkip)? "..." : ste.toString())
					.distinct()
					.collect(Collectors.joining("\n\t", "\n\t", ""))
				);
		}
	}

	/**
	 * The default minimum logSince duration is {@value} ms
	 */
	public static long DEFAULT_MINIMUM_LOGSINCE_DURATION = 100;

	/** Simple function to log a duration, as a TRACE message, to the logger provided
	 * <BR><BR>
	 * To protect against excessive logging, log messages with durations below {@link #DEFAULT_MINIMUM_LOGSINCE_DURATION} are suppressed.
	 * <BR><BR>
	 * For example:
	 * <PRE><BR>{@code
	 * final Instant startTime = Instant.now();
	 * ...
	 * logSince(logger, startTime, "myFunction() connected within");
	 * }</PRE>
	 * <BR>generates "myFunction() connected within 230 ms".<BR><BR>
	 */
	public static void logSince(Logger logger, String description, Instant startTime) {
		logSince(logger, description, startTime, DEFAULT_MINIMUM_LOGSINCE_DURATION);
	}

	/** Simple function to log a duration, as a TRACE message, to the logger provided
	 * <BR><BR>
	 * To protect against excessive logging, log messages with durations below {@code minimumDuration} are suppressed, so a
	 * {@code minimumDuration} of {@code 0} will allow all durations to be logged.
	 * See {@link #logSince(Logger, String, Instant)} for example
	 */
	public static void logSince(Logger logger, String description, Instant startTime, long minimumDuration) {
		if (logger.isTraceEnabled()) {
			final Duration duration = Duration.between(startTime, Instant.now());
			if (duration.toMillis() > minimumDuration) {
				logger.trace("{} {} ms", description, duration.toMillis());
			}
		}
	}
}
