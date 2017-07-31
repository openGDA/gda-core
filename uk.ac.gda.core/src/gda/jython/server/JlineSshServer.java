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
import java.nio.file.Paths;

import org.apache.sshd.common.FactoryManager;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.pubkey.AcceptAllPublickeyAuthenticator;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.jline.builtins.ssh.ShellCommand;
import org.jline.builtins.ssh.ShellFactoryImpl;
import org.jline.builtins.ssh.Ssh.ExecuteParams;
import org.jline.terminal.Terminal;
import org.python.core.PyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.jython.JythonServerFacade;

public class JlineSshServer {

	private static final Logger logger = LoggerFactory.getLogger(JlineSshServer.class);

	/** Property holding the directory in which to look for SSH public keys */
	public static final String GDA_JYTHON_SERVER_KEY_DIR = "gda.remote.ssh.keys";

	/**
	 * Create and run an SSH server listening on the given port.
	 * This method returns leaving the server running in the background.
	 * @param port to listen on
	 * @return a runnable to shutdown the server cleanly
	 */
	public static Runnable runServer(int port) {
		JlineSshServer server = new JlineSshServer(port);
		server.run();
		return server::close;
	}

	private SshServer server;

	private JlineSshServer(int port) {
		server = SshServer.setUpDefaultServer();
		logger.info("Running SSH server on port {}", port);
		server.setPort(port);
		server.getProperties().put(FactoryManager.IDLE_TIMEOUT, 0); // 0 -> no timeout
		server.setShellFactory(new ShellFactoryImpl(params -> {
			logger.info("Running SSH shell as {}", params.getEnv().get("USER"));
			try {
				runShell(params.getTerminal());
			} finally {
				params.getCloser().run();
			}
		}));
		server.setCommandFactory(command -> new ShellCommand(this::exec, command));
		server.setPublickeyAuthenticator(getAuth());
		server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(
				Paths.get(LocalProperties.getVarDir(), ".gda.server.key")
		));
	}

	/**
	 * Start the server without reraising exceptions
	 */
	protected void run() {
		try {
			server.start();
		} catch (IOException e1) {
			logger.error("Could not start SSH server", e1);
		}
	}

	/**
	 * Get the Authenticator to use. If the {@link #GDA_JYTHON_SERVER_KEY_DIR} property is set,
	 * use it to look up keys, if not, allow all users to connect.
	 * @return The PublickeyAuthenticator to use to authenticate users
	 */
	private PublickeyAuthenticator getAuth() {
		String keyDirectory = LocalProperties.get(GDA_JYTHON_SERVER_KEY_DIR);
		if (keyDirectory == null) {
			logger.warn("No key directory is set, SSH connections will not be authenticated");
			return AcceptAllPublickeyAuthenticator.INSTANCE;
		} else {
			return new GdaAuthorizedKeys(keyDirectory);
		}
	}

	private void runShell(Terminal term) {
		try (JythonShell shell = new JythonShell(term)) {
			shell.run();
		} catch (Exception e) {
			term.writer().format("Error connecting to GDA: '%s'", e.getMessage());
			logger.error("Jython shell failed", e);
		}
	}

	/**
	 * Run a command given in non-interactive mode, eg:<br>
	 * {@code ssh bl-control pos abc 1}<br>
	 * will result in this being called with 'pos abc 1' as the command.
	 * <p>
	 * This does not run an interactive shell and anything read from stdin is read
	 * directly rather than through a JLine reader.
	 * <p>
	 * All output from the command (including errors) will be printed to stdout of the calling client.
	 * @param params Container for command, stdin, stdout
	 */
	private void exec(ExecuteParams params) {
		logger.debug("exec command '{}'", params.getCommand());
		JythonServerFacade jsf = JythonServerFacade.getInstance();
		try {
			jsf.exec(params.getCommand());
		} catch (PyException pe) {
			try {
				params.getErr().write(("Error running command: " + pe.getMessage()).getBytes());
			} catch (IOException e) {
				logger.error("Couldn't write error to SSH client: {}", pe, e);
			}
		}
		logger.debug("Exec complete");
	}

	public void close() {
		try {
			server.close();
		} catch (IOException e) {
			logger.error("Error closing SSH server", e);
		}
	}
}
