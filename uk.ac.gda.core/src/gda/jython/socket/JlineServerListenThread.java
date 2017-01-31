/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.jython.socket;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.python.jline.TerminalFactory;
import org.python.jline.UnixTerminal;
import org.python.jline.console.ConsoleReader;
import org.python.jline.console.history.FileHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;

public class JlineServerListenThread extends ServerListenThreadBase {

	static {
		// DAQ-392 - if TERM environment variable is 'dumb' (eg if servers have been started
		// from IDE launched without user environment), jline defaults to UnsupportedTerminal
		// and telnet connection is not usable (input not echoed, CompileError when running
		// commands).
		TerminalFactory.registerFlavor(TerminalFactory.Flavor.UNIX, UnixXtermTerminal.class);
		TerminalFactory.configure(TerminalFactory.Type.AUTO);
	}

	private static final Logger logger = LoggerFactory.getLogger(JlineServerListenThread.class);
	private final ConsoleReader cr;

	public JlineServerListenThread(InputStream in, OutputStream out, SessionClosedCallback sessionClosedCallback) throws IOException {

		super(sessionClosedCallback);
		this.cr = new ConsoleReader(in, out);

		final String gdaVar = LocalProperties.getVarDir();
		final File historyFile = new File(gdaVar, "server.history");
		cr.setHistory(new FileHistory(historyFile));
	}

	@Override
	public void close() {
		try {
			cr.close();
		} catch (IOException e) {
			//Not much we can do here - log warning and carry on
			logger.warn("Failed to close ConsoleReader", e);
		}
	}

	@Override
	protected String readLine(String prompt) throws IOException {
		return cr.readLine(prompt);
	}

	public static class UnixXtermTerminal extends UnixTerminal {
		public UnixXtermTerminal() throws Exception {
			//TODO: Check for $TERM==dumb first?
			super("/dev/tty", "xterm");
		}
	}
}
