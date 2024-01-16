/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

import static java.util.stream.StreamSupport.stream;

import java.util.List;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import junit.framework.TestCase;

public class LogbackUtilsTest extends TestCase {

	private static final String ROOT_LOGGER_NAME = "ROOT";

	public void testReset() {
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

		// This creates a couple of extra loggers besides the root logger...
		LoggerFactory.getLogger("xxx.yyy");
		// ...so there should be at least 3
		assertTrue(loggerContext.getLoggerList().size() >= 3);

		// Resetting the logging system shouldn't remove existing loggers...
		LogbackUtils.resetLogging();
		// ...so there should still be at least 3
		assertTrue(loggerContext.getLoggerList().size() >= 3);

		// None of the loggers should have any appenders, or a level (except for the root logger)
		for (Logger logger : loggerContext.getLoggerList()) {
			if (logger.getName().equals(ROOT_LOGGER_NAME)) {
				assertEquals(Level.DEBUG, logger.getLevel());
			} else {
				assertNull(logger.getLevel());
			}
			assertEquals(0, getAppendersForLogger(logger).size());
		}
	}

	/**
	 * Returns a list of all appenders for the specified logger.
	 *
	 * @param logger a Logback {@link Logger}
	 *
	 * @return a list of the logger's appenders
	 */
	static List<Appender<ILoggingEvent>> getAppendersForLogger(Logger logger) {
		Iterable<Appender<ILoggingEvent>> appenders = logger::iteratorForAppenders;
		return stream(appenders.spliterator(), false).toList();
	}
}
