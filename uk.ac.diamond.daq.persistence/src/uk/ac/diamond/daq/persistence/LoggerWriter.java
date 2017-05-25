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

package uk.ac.diamond.daq.persistence;

import java.io.IOException;
import java.io.Writer;

import org.slf4j.Logger;

/**
 * A {@link Writer} subclass that passes messages to an SLF4J {@link Logger}.
 */
public class LoggerWriter extends Writer {

	private final Logger logger;

	/**
	 * Creates a {@link LoggerWriter} that will pass messages to the specified {@link Logger}.
	 */
	public LoggerWriter(Logger logger) {
		this.logger = logger;
	}

	private StringBuilder buffer = new StringBuilder();

	private State state = State.IN_MESSAGE;

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {

		// This is a really simple state machine that toggles between two states depending on whether or not the next
		// character is a newline. Repeated newline characters are treated as a single line break, so empty lines will
		// be discarded; but in practice they don't matter, and it saves having to work out what newline representation
		// is being used.

		synchronized (buffer) {
			for (int i=off; i<off+len; i++) {
				final char c = cbuf[i];

				switch (state) {

					case IN_MESSAGE:
						if (isNewlineCharacter(c)) {
							logger.info(buffer.toString());
							buffer.setLength(0);
							state = State.IN_LINEBREAK;
						} else {
							buffer.append(c);
						}
						break;

					case IN_LINEBREAK:
						if (!isNewlineCharacter(c)) {
							buffer.append(c);
							state = State.IN_MESSAGE;
						}
				}
			}
		}
	}

	private static boolean isNewlineCharacter(char c) {
		return (c == '\r') || (c == '\n');
	}

	@Override
	public void close() throws IOException {
		// ignore
	}

	@Override
	public void flush() throws IOException {
		// ignore
	}

	enum State {
		IN_MESSAGE,
		IN_LINEBREAK
	}

}
