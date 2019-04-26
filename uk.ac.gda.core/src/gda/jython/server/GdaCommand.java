/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SessionAware;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.concurrent.Async;

/**
 * Base class for handling incoming SSH connections. Holds IO streams and the client session
 * as well as handling closing the connection.
 */
public abstract class GdaCommand implements Command, SessionAware {
	private static final Logger logger = LoggerFactory.getLogger(GdaCommand.class);

	/** Exit code for successful completion */
	protected static final int EXIT_SUCCESS = 0;
	/** Exit code for completion with error */
	protected static final int EXIT_ERROR = 1;

	/** Session information from the SSH connection */
	private ServerSession session;

	/** Callback to close connection */
	private ExitCallback exitCallback;

	/** Client's stdin */
	private InputStream stdin;

	/** Client's stdout */
	private OutputStream stdout;

	/** Client's stderr */
	private OutputStream stderr;

	/** Flag to prevent streams being closed multiple times */
	private AtomicBoolean closed = new AtomicBoolean(false);

	@Override
	public void setInputStream(InputStream stdin) {
		this.stdin = stdin;
	}

	public InputStream getStdin() {
		return stdin;
	}

	@Override
	public void setOutputStream(OutputStream stdout) {
		this.stdout = stdout;
	}

	public OutputStream getStdout() {
		return stdout;
	}

	@Override
	public void setErrorStream(OutputStream stderr) {
		this.stderr = stderr;
	}

	public OutputStream getStderr() {
		return stderr;
	}

	@Override
	public void setSession(ServerSession serverSession) {
		session = serverSession;
	}

	public ServerSession getSession() {
		return session;
	}

	/** Get the client address in the form user@host */
	protected String getClientAddress() {
		String user = session.getUsername();
		String address = resolveHost(session.getClientAddress());
		return user + "@" + address;
	}

	@Override
	public void setExitCallback(ExitCallback exit) {
		exitCallback = exit;
	}

	@Override
	public void destroy() throws Exception {
		exit(EXIT_ERROR); // If this hasn't been successfully closed already it must be an unexpected error somewhere
	}

	/** Log an error before exiting with error exit code ({@value #EXIT_ERROR}) */
	protected void exit(Throwable t) {
		if (t != null) {
			logger.error("Closing command after error", t);
		}
		exit(EXIT_ERROR);
	}

	/** Flush and close streams, then call the exit callback with the given exitCode */
	protected void exit(int exitCode) {
		if (closed.compareAndSet(false, true)) {
			flush(stdout, stderr);
			exitCallback.onExit(exitCode);
		}
	}

	/** Attempt to flush all output streams (ignoring any errors) */
	private static void flush(OutputStream... streams) {
		for (OutputStream s : streams) {
			try {
				s.flush();
			} catch (IOException e) {
				// Ignore
			}
		}
	}

	/** Convert a {@link SocketAddress} into a string host name */
	private static String resolveHost(SocketAddress socket) {
		String addr = socket.toString();
		try {
			InetSocketAddress sock = (InetSocketAddress)socket;
			addr = InetAddress.getByAddress(sock.getAddress().getAddress()).getCanonicalHostName();
		} catch (ClassCastException | UnknownHostException e) {
			logger.error("Couldn't resolve hostname", e);
		}
		return addr;
	}

	/** Start command running in a background thread - called by SSH server */
	@Override
	public void start(Environment env) {
		Async.submit(() -> run(env), getClass().getSimpleName())
				.onSuccess(this::exit)
				.onFailure(this::exit);
	}

	protected abstract int run(Environment env) throws IOException;
}
