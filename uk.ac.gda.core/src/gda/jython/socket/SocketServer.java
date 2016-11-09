/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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
import java.net.ServerSocket;

import org.apache.sshd.SshServer;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;

/**
 * Provides a socket for the GDA command server to accept commands from outside the GDA
 */
public class SocketServer implements Runnable {

	/**
	 * Represents a server type.
	 */
	public enum ServerType {

		/** Telnet server. */
		TELNET,

		/** SSH server. */
		SSH
	}

	private static final Logger logger = LoggerFactory.getLogger(SocketServer.class);

	/** set to false to close socket */
	public volatile boolean listening = true;

	// the default port
	int port = 4444;

	String name = "command_socket";

	private ServerType serverType = ServerType.TELNET;

	/**
	 *
	 */
	public SocketServer() {
	}

	/**
	 * Sets the server type.
	 *
	 * @param serverType the server type
	 */
	public void setServerType(ServerType serverType) {
		this.serverType = serverType;
	}

	@Override
	public void run() {
		if (serverType == ServerType.TELNET) {
			runTelnetServer();
		} else {
			runSshServer();
		}
	}

	private void runTelnetServer() {
		ServerSocket serverSocket = null;

		try {
			serverSocket = new ServerSocket(port);
			logger.info("Listening on port " + serverSocket.getLocalPort());
		} catch (IOException e) {
			logger.error("Could not listen on port: " + port + " because of exception \"" + e.getMessage() + "\".");
			return;
		}

		try {
			while (listening) {
				// start a new thread for every connection to this socket
				new SocketServerWithTelnetNegotiationThread(serverSocket.accept()).start();
			}
			serverSocket.close();
		} catch (IOException e) {
			logger.error("Error in Telnet server thread", e);
		}
	}

	private void runSshServer() {
		logger.debug("Starting SSH server on port " + port);

		if (authenticator == null) {
			throw new IllegalArgumentException("Cannot start SSH server because a PasswordAuthenticator has not been provided");
		}

		SshServer sshd = SshServer.setUpDefaultServer();
		sshd.setPort(port);

		String gdaConfig = LocalProperties.get(LocalProperties.GDA_CONFIG);
		File hostKey = new File(new File(gdaConfig, "etc"), "hostkey.ser");
		sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(hostKey.getAbsolutePath()));

		sshd.setShellFactory(new SshShellFactory());

		sshd.setPasswordAuthenticator(authenticator);

		try {
			sshd.start();
		} catch (Exception e) {
			logger.error("Could not start SSH server", e);
		}
		logger.info("SSH server listening on port " + port);
	}

	/**
	 * Returns the port this object will open
	 *
	 * @return int
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Sets the port this object will use.
	 *
	 * @param portNumber
	 */
	public void setPort(int portNumber) {
		port = portNumber;
	}

	private PasswordAuthenticator authenticator;

	public void setAuthenticator(PasswordAuthenticator authenticator) {
		this.authenticator = authenticator;
	}

}
