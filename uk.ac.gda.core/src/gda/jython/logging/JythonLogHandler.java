/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package gda.jython.logging;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.python.core.PyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Submit log messages to SLF4J loggers.
 * <p>
 * This is intended to be used from Jython to write to the main logs. This intermediate layer is needed
 * so that the stack trace can be manipulated to include both the Jython and Java stack traces.
 *
 * @since 9.8
 */
public class JythonLogHandler {

	public enum LogLevel {
		TRACE(log -> log::trace),
		DEBUG(log -> log::debug),
		INFO(log -> log::info),
		WARN(log -> log::warn),
		ERROR(log -> log::error);

		/** Cache for Jython to java log level mapping */
		private static final Map<Integer, LogLevel> jythonLevels = new HashMap<>();
		/** Function to get correct log function from a given logger */
		private final Function<Logger, BiConsumer<String, Object[]>> logFunction;

		/** Convert a Jython logging level (integer) into a {@link LogLevel} that can be used for
		 * logging to a SLF4J logger
		 *
		 * @param level Jython integer logging level (0-100)
		 * @return LogLevel
		 */
		public static LogLevel fromJython(int level) {
			// Jython log levels are just integers so map to Java levels (and cache)
			return jythonLevels.computeIfAbsent(level, lvl -> {
				if (lvl >= 40) return ERROR;
				else if (lvl >= 30) return WARN;
				else if (lvl >= 20) return INFO;
				else if (lvl >= 10) return DEBUG;
				else return TRACE;
			});
		}

		/**
		 * Create a LogLevel that maps a logger to one of its logging levels
		 *
		 * @param logFunction A function that takes a {@link Logger} and returns one of its
		 *         logging methods (trace, debug etc)
		 */
		private LogLevel(Function<Logger, BiConsumer<String, Object[]>> logFunction) {
			this.logFunction = logFunction;
		}

		/** Log the given message and args to the logger at this LogLevel */
		public void log(Logger logger, String message, Object...args) {
			logFunction.apply(logger).accept(message, args);
		}
	}

	/**
	 * Send log message to the specified logger
	 *
	 * @param level the level to log at - this is mapped from the jython logging library levels
	 * @param loggerName The name of the logger to use
	 * @param message The message to log. All formatting is done of the Jython side
	 * @param exc A PyException wrapping the underlying Jython/Java exceptions
	 */
	public void submitLog(LogLevel level, String loggerName, String message, PyException exc) {
		Throwable error = PythonException.from(exc);
		Object[] args = error == null ? new Object[] {} : new Object[] {error};
		level.log(LoggerFactory.getLogger(loggerName), message, args);
	}
}
