/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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
import java.io.OutputStream;

import org.jline.builtins.telnet.Connection;
import org.jline.builtins.telnet.ConnectionData;
import org.jline.builtins.telnet.ConnectionEvent;
import org.jline.builtins.telnet.ConnectionListener;
import org.jline.builtins.telnet.TelnetIO;
import org.jline.reader.EndOfFileException;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.Terminal.Signal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Telnet connection to the JythonServer
 *
 * Manages the life of the connection and provides the shell for the user to
 * interact with
 */
class JythonTelnetConnection extends Connection {
	private static final Logger logger = LoggerFactory.getLogger(JythonTelnetConnection.class);

	private final TelnetIO io;
	private final Terminal terminal;

	JythonTelnetConnection(ThreadGroup tcg, ConnectionData cd) throws IOException {
		super(tcg, cd);
		io = new TelnetIO();
		io.setConnection(this);
		io.initIO();
		terminal = TerminalBuilder.builder()
				.type(getConnectionData().getNegotiatedTerminalType().toLowerCase())
				.streams(
						new JlineInput(),
						new JlineOutput())
				.system(false)
				.name("GDA")
				.size(new Size(
						getConnectionData().getTerminalColumns(),
						getConnectionData().getTerminalRows()
						))
				.build();
		addConnectionListener(new ConnectionListener() {
			@Override
			public void connectionTerminalGeometryChanged(ConnectionEvent ce) {
				terminal.setSize(new Size(getConnectionData().getTerminalColumns(), getConnectionData().getTerminalRows()));
				terminal.raise(Signal.WINCH);
			}
		});
	}

	/**
	 * Actually run the shell.
	 * <p>
	 * This is called after the connection has been established and telnet negotiation has finished.
	 */
	@Override
	protected void doRun() throws Exception {
		try (JythonShell shell = new JythonShell(terminal)) {
			shell.run();
		} catch (EndOfFileException eof) {
			logger.info("EOF in jython telnet connection");
		}
	}

	@Override
	protected void doClose() throws Exception {
		terminal.close();
		io.closeOutput();
		io.closeInput();
	}

	/**
	 * Wrapper around TelnetIO to provide InputStream for connection
	 */
	private class JlineInput extends InputStream {
		@Override
		public int read() throws IOException {
			return io.read();
		}
		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			int r = read();
			if (r >= 0) {
				b[off] = (byte) r;
				return 1;
			} else {
				return -1;
			}
		}
	}

	/**
	 * Wrapper around TelnetIO to provide OutputStream for connection
	 */
	private class JlineOutput extends OutputStream {
		@Override
		public void write(int b) throws IOException {
			io.write(b);
		}
		@Override
		public void flush() throws IOException {
			io.flush();
		}
	}
}
