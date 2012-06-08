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

import java.io.BufferedReader;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listens to a port and prints out everything to System.out
 */
public class ClientListenThread extends Thread {
	private static final Logger logger = LoggerFactory.getLogger(ClientListenThread.class);

	BufferedReader in = null;

	/**
	 * @param in
	 */
	public ClientListenThread(BufferedReader in) {
		this.in = in;
	}

	@Override
	public synchronized void run() {
		String fromServer;
		try {
			while ((fromServer = in.readLine()) != null) {
				logger.debug(fromServer);
			}
		} catch (IOException ex) {
			logger.error("Error while communicating with CommandServer via socket: " + ex.getMessage());
		}

	}

}
