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

package uk.ac.diamond.daq.persistence.jythonshelf;

import static java.util.Arrays.stream;

import java.io.Writer;
import java.util.regex.Pattern;

import org.slf4j.Logger;

/**
 * A {@link Writer} subclass that passes messages to an SLF4J {@link Logger}.
 */
public class LoggerWriter extends Writer {
	/**
	 * Regex matching (possibly repeated) newlines of any type (\r\n etc).
	 * Matches multiple new lines to prevent empty lines being logged.
	 */
	private static final Pattern NEWLINES = Pattern.compile("\\R+");

	private final Logger logger;

	/**
	 * Creates a {@link LoggerWriter} that will pass messages to the specified {@link Logger}.
	 */
	public LoggerWriter(Logger logger) {
		this.logger = logger;
	}

	@Override
	public void write(char[] cbuf, int off, int len) {
		String[] lines = NEWLINES.split(new String(cbuf, off, len), 0);
		synchronized (this) {
			// Synchronised to prevent multiple multi-line messages being interleaved
			stream(lines).forEach(this::logLine);
		}
	}

	/**
	 * Subclass and overwrite to log at a level other than INFO.
	 */
	protected void logLine(String line) {
		logger.info(line);
	}

	@Override
	public void close() {}

	@Override
	public void flush() {}
}
