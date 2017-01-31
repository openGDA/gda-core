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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.JythonServerFacade;

/**
 * Listens to a port and passes input from that to the Command Server
 */
public abstract class ServerListenThreadBase extends Thread {
	private static final Logger logger = LoggerFactory.getLogger(ServerListenThreadBase.class);

	private final JythonServerFacade command_server = JythonServerFacade.getInstance();

	private final SessionClosedCallback sessionClosedCallback;

	protected ServerListenThreadBase(SessionClosedCallback sessionClosedCallback) {
		this.sessionClosedCallback = sessionClosedCallback;
	}

	@Override
	public void run() {
		boolean needMore = false;
		String fromServer = "";
		String previousCommand = "";
		String command = "";
		try {
			while ((fromServer = readLine(needMore ? "... " : ">>> ")) != null) {
				command = previousCommand + fromServer;
				needMore = command_server.runsource(command, "CommandThread");
				// needMore will be true if the command was incomplete e.g. the
				// first line of a for loop. It is false otherwise (including
				// mercifully if there is a syntax error).
				if (needMore) {
					previousCommand = command + "\n";
				} else {
					previousCommand = "";
				}
			}
		} catch (IOException ex) {
			logger.error("Error while communicating with CommandServer via socket: " + ex.getMessage());
		}
		close();
		sessionClosedCallback.sessionClosed();
	}

	/**
	 * Close the terminal
	 * gives a chance for any history to be saved etc
	 */
	protected void close() {}

	protected abstract String readLine(String prompt) throws IOException;

}