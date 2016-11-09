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

import java.io.IOException;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SocketServerThreadBase extends ServerThread implements SessionClosedCallback {

	private static final Logger logger = LoggerFactory.getLogger(SocketServerThreadBase.class);

	protected Socket socket;

	protected SocketServerThreadBase(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void sessionClosed() {
		// Remove ourself as a terminal also removes us as an observer
		command_server.deleteOutputTerminal(this);
		try {
			socket.close();
		} catch (IOException ioe) {
			logger.error("Unable to close socket", ioe);
		}
	}

}
