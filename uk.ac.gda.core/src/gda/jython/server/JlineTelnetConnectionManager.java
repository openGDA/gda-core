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

import org.jline.builtins.telnet.Connection;
import org.jline.builtins.telnet.ConnectionData;
import org.jline.builtins.telnet.ConnectionManager;
import org.jline.builtins.telnet.PortListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The telnet server. This class runs the telnet server and is responsible for
 * creating {@link JythonTelnetConnection}s when clients connect.
 * <br><br>
 * To run the server, call the static {@link #runServer(int)} method with the
 * port to listen for connections on.
 */
public class JlineTelnetConnectionManager extends ConnectionManager {
	private static final Logger logger = LoggerFactory.getLogger(JlineTelnetConnectionManager.class);

	/**
	 * Start listening for telnet connections on the given port.
	 * @param port the port to listen for connections on. Should be >1024
	 */
	public static void runServer(int port) {
		ConnectionManager conMan = new JlineTelnetConnectionManager();
		PortListener portListen = new PortListener("GDA", port, 10);
		portListen.setConnectionManager(conMan);
		portListen.start();
		logger.info("Listening for telnet connections on port {}", port);
	}

	private JlineTelnetConnectionManager() {
		super(
				30, // max connections
				5*60*1000, // warning timeout (5min)
				5*60*1000, // disconnect timeout (5min)
				60*1000, // housekeeping interval (1min)
				null, // connection filter
				null, // login shell
				false // line mode
				);
	}

	@Override
	protected Connection createConnection(ThreadGroup threadGroup, ConnectionData newCD) {
		try {
			return new JythonTelnetConnection(threadGroup, newCD);
		} catch (IOException e) {
			// This method can't throw checked exceptions, runtime exceptions will cause the
			// server to close, returning null will cause NPEs when the server tries to handle
			// closed connections.
			// Return Null Connection that immediately closes.
			logger.error("Could not create Jython connection", e);
			return new Connection(threadGroup, newCD) {
				@Override
				protected void doRun() {
					logger.debug("Running NoOp telnet connection");
				}
				@Override
				protected void doClose() {
					logger.debug("Closing NoOp telnet connection");
				}
			};
		}
	}

}
