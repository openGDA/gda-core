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

import static gda.jython.server.auth.Authenticator.State.ACCEPT;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.sshd.common.FactoryManager;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.jython.server.auth.AuthorizedKeysDirectory;
import gda.jython.server.auth.GdaAuthenticator;

public class GdaSshServer {
	private static final Logger logger = LoggerFactory.getLogger(GdaSshServer.class);

	/** Property holding the directory in which to look for SSH public keys */
	public static final String GDA_JYTHON_SERVER_KEY_DIR = "gda.remote.ssh.keys";
	/** The current beamline name from system properties */
	private static final String BEAMLINE = LocalProperties.get(LocalProperties.GDA_BEAMLINE_NAME);


	/**
	 * Create and run an SSH server listening on the given port.
	 * This method returns leaving the server running in the background.
	 * @param port to listen on
	 * @return a runnable to shutdown the server cleanly
	 */
	public static Runnable runServer(int port) {
		GdaSshServer server = new GdaSshServer(port);
		server.run();
		return server::close;
	}

	private SshServer server;

	private GdaSshServer(int port) {
		server = SshServer.setUpDefaultServer();
		logger.info("Running SSH server on port {}", port);
		server.setPort(port);
		server.getProperties().put(FactoryManager.IDLE_TIMEOUT, 0); // 0 -> no timeout
		// Input is being read by Jline. Server read process timing out causes it to read null
		// and close the connection.
		server.getProperties().put(FactoryManager.NIO2_READ_TIMEOUT, 0); // 0 -> no timeout
		server.setShellFactory(SshShellCommand::new);
		server.setCommandFactory(SshExecCommand::new);
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
			return new GdaAuthenticator((u, k, s) -> ACCEPT);
		} else {
			Path keys = Paths.get(keyDirectory);
			return new GdaAuthenticator(
					new AuthorizedKeysDirectory(keys),
					new AuthorizedKeysDirectory(keys.resolve(BEAMLINE))
			);
		}
	}

	public void close() {
		try {
			logger.debug("Shutting down SSH server");
			server.close();
		} catch (IOException e) {
			logger.error("Error closing SSH server", e);
		}
	}
}