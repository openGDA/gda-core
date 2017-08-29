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
import java.util.List;

import org.python.core.Py;
import org.python.jline.TerminalFactory;
import org.python.jline.UnixTerminal;
import org.python.jline.console.ConsoleReader;
import org.python.jline.console.UserInterruptException;
import org.python.jline.console.completer.CandidateListCompletionHandler;
import org.python.jline.console.completer.Completer;
import org.python.jline.console.history.FileHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.jython.JythonServerFacade;
import gda.jython.completion.AutoCompletion;

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
	private File historyFile;
	private FileHistory hist;
	private final InputStream rawInputStream;

	public JlineServerListenThread(InputStream in, OutputStream out, SessionClosedCallback sessionClosedCallback) throws IOException {

		super(sessionClosedCallback);
		this.cr = new ConsoleReader(in, out);

		final String gdaVar = LocalProperties.getVarDir();
		historyFile = new File(gdaVar, "server.history");
		hist = new FileHistory(historyFile);
		cr.setHistory(hist);
		cr.setHistoryEnabled(true);
		logger.debug("Writing telnet history to {}", historyFile);

		cr.addCompleter(new GdaJythonCompleter());
		CandidateListCompletionHandler clch = new CandidateListCompletionHandler();
		clch.setPrintSpaceAfterFullCompletion(false);
		cr.setCompletionHandler(clch);

		cr.setHandleUserInterrupt(true);
		rawInputStream = new JlineInputStream();
	}

	@Override
	public void close() {
		try {
			hist.flush();
			logger.debug("Wrote telnet history to {}", historyFile);
		} catch (IOException e) {
			//Not much we can do here - worst case is we lose the history
			logger.warn("Error writing telnet terminal history", e);
		}
		cr.close();

	}

	@Override
	protected String readLine(String prompt) throws IOException {
		try {
			return cr.readLine(prompt);
		} catch (UserInterruptException uie) {
			cr.println("KeyboardInterrupt");
			throw new InterruptedInputException(uie);
		}
	}

	private static final class GdaJythonCompleter implements Completer {
		@Override
		public int complete(String buffer, int cursor, List<CharSequence> candidates) {
			AutoCompletion ac = JythonServerFacade.getInstance().getCompletionsFor(buffer, cursor);
			candidates.addAll(ac.getStrings());
			return ac.getPosition();
		}
	}

	public static class UnixXtermTerminal extends UnixTerminal {
		public UnixXtermTerminal() throws Exception {
			//TODO: Check for $TERM==dumb first?
			super("/dev/tty", "xterm");
		}
	}


	@Override
	protected InputStream getInput() {
		return rawInputStream;
	}

	/**
	 * A JLine enabled input stream to pass to the jython interpreter as stdin<p>
	 * Used mainly for <code>input</code> and <code>raw_input</code>
	 */
	class JlineInputStream extends InputStream {
		@Override
		public int read() throws IOException {
			// This should never be called but just pass through to socket input if it is
			logger.warn("Unexpected call to RawJline.read()", new Exception("Non-error -> stack trace for unexpected call"));
			return cr.getInput().read();
		}

		/**
		 * Use the ConsoleReader for reading user input to provide readline support
		 */
		@Override
		public int read(byte[] buf, int offset, int len) throws IOException {
			// don't add raw input to history
			cr.setHistoryEnabled(false);
			try {
				// Use the contents of the buffer as the prompt
				String line = cr.readLine(new String(buf, 0, offset));
				if (line == null) {
					throw Py.EOFError("EOF when reading a line");
				}
				byte[] bline = line.getBytes();
				for (int i=0; i < bline.length; i++) {
					buf[i+offset] = bline[i];
				}
				return bline.length;
			} finally {
				cr.setHistoryEnabled(true);
			}
		}
	}
}
