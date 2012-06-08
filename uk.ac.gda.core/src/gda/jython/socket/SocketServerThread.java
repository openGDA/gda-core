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

import gda.jython.JythonServerFacade;

import java.io.IOException;
import java.net.Socket;

/**
 * Thread for dealing with a client connected to the Jython server through a
 * socket.
 */
public class SocketServerThread extends ServerThread {
	Socket socket = null;

	/**
	 * Creates a server thread.
	 * 
	 * @param socket client socket
	 */
	public SocketServerThread(Socket socket) {
		try {
			setOutputStream(socket.getOutputStream());
			setInputStream(socket.getInputStream());
		} catch (IOException ioe) {
			throw new RuntimeException("Unable to accept client", ioe);
		}
		JythonServerFacade.getInstance().print("New command connection accepted from " + socket.getRemoteSocketAddress());
	}

}