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

package gda.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HumidityServer Class
 */
public class HumidityServer implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(HumidityServer.class);

	protected Socket socket;

	private int port = 0;

	private HumiditySocketExecutor executor;

	/**
	 * @param args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		if (args.length > 0) {
			try {
				new HumidityServer(new HumiditySocketExecutor(), Integer.parseInt(args[0]));
			} catch (NumberFormatException e) {
				logger.debug("Usage: java gda.util.HumidityServer portNumber");
				logger.debug("portNumber MUST be an integer");
			}
		} else {
			logger.debug("Usage: java gda.util.ServerSocket portNumber");
		}
	}

	/**
	 * @param executor
	 * @param port
	 */
	public HumidityServer(HumiditySocketExecutor executor, int port) {
		this.executor = executor;
		this.port = port;

		// Start the thread dealing with remote commands.
		uk.ac.gda.util.ThreadManager.getThread(this).start();
	}

	// Runnable interface.

	@Override
	public void run() {
		ServerSocket serverSocket = null;
		BufferedReader reader = null;
		try {
			String command;
			serverSocket = new ServerSocket(port);

			while (true) {
				try {
					socket = serverSocket.accept();
					executor.setSocket(socket);
					reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

					while (!socket.isClosed() && socket.isConnected()) {
						if ((command = reader.readLine()) != null) {
							executor.execute(command, writer);
						} else
							break;
					}

					socket.close();
				} catch (IOException ioex) {
					logger.error("Error while running server", ioex);
				}
			}
		} catch (NumberFormatException e) {
			logger.debug(e.getStackTrace().toString());
		} catch (IOException ex) {
			logger.error("Fatal error in SocketServer, failed to bind socket on port " + port);
		} finally {
			if (serverSocket != null) {
				try {
					serverSocket.close();
				} catch (IOException e) {
					// ignore
				}
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}
}
