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

package gda.jython.server;

import java.io.IOException;
import java.io.InputStream;

import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.UserInterruptException;
import org.python.core.Py;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * InputStream to be used when a command needs to read from StdIn
 * <p>
 * Lets Jline handle the input to enable movement in the line etc
 */
class JlineInputStream extends InputStream {
	private static final Logger logger = LoggerFactory.getLogger(JlineInputStream.class);
	private final LineReader reader;

	JlineInputStream(LineReader reader) {
		this.reader = reader;
	}

	@Override
	public int read() throws IOException {
		// This should never be called but just pass through to socket input if it is
		logger.warn("Unexpected call to JlineInputStream.read()", new Exception("Non-error -> stack trace for unexpected call"));
		return reader.getTerminal().input().read();
	}

	/**
	 * Use LineReader to wrap input stream and provide readline-like input support
	 */
	@Override
	public int read(byte[] buf, int offset, int len) throws IOException {
		String line;
		try {
			// We don't want the raw_input line to be kept in the history
			reader.setVariable(LineReader.DISABLE_HISTORY, true);
			line = reader.readLine(new String(buf, 0, offset));
		} catch (EndOfFileException eof) { // ctrl-d while editing
			throw Py.EOFError("EOF when reading a line");
		} catch (UserInterruptException uie) { // ctrl-c while editing
			throw Py.KeyboardInterrupt("KeyboardInterrupt");
		} finally {
			reader.setVariable(LineReader.DISABLE_HISTORY, false);
		}
		byte[] bytes = line.getBytes();
		for (int i = 0; i < bytes.length; i++) {
			buf[i+offset] = bytes[i];
		}
		return bytes.length;
	}
}