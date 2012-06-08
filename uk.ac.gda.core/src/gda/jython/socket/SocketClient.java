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
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client object to talk to the GDA using sockets.
 * <p>
 * The GDA must be running a socket connection. This will automatically occur id the machine running the object server
 * has the java property: gda.jython.socket in its java.properties file.
 * <p>
 * This class also has a "main" method so that a connection maybe run from a system terminal
 */
public class SocketClient {
	private static final Logger logger = LoggerFactory.getLogger(SocketClient.class);

	/**
	 * 
	 */
	public Socket socket = null;

	/**
	 * 
	 */
	public PrintWriter out = null;

	/**
	 * 
	 */
	public BufferedReader in = null;

	String host = null;

	int port = 0;

	/**
	 * 
	 */
	public SocketClient() {
	}

	/**
	 * @param args
	 */
	public synchronized static void main(String[] args) {
		try {
			SocketClient client = new SocketClient();
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

			// get the name and port of the machine hosting the JythonServer
			// object
			logger.debug("Enter the name of the GDA ObjectServer host");
			String host = in.readLine();
			client.setHost(host);

			logger.debug("Enter the GDA ObjectServer host port");
			String port = in.readLine();
			client.setPort(Integer.parseInt(port));

			// connect and open two threads for reading and writing
			client.connect();
			new ClientListenThread(client.in).start();
			new ClientSendThread(client.out).start();
		} catch (IOException ex) {
		}
	}

	/**
	 * Connect to the defined socket.
	 */
	public void connect() {

		try {
			socket = new Socket(host, port);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host: " + host + ":" + port);
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to: " + host + ":" + port);
			System.exit(1);
		}
	}

	/**
	 * Close all conections.
	 */
	public void disconnect() {
		try {
			out.close();
			in.close();
			socket.close();
		} catch (IOException ex) {
		}
	}

	/**
	 * @param command
	 *            the command to send to the GDA Comand Server
	 */
	public void send(String command) {
		out.println(command);
	}

	/**
	 * @return the port number in use
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param host
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @param port
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return the name of the GDA server machine.
	 */
	public String getHost() {
		return host;
	}
}
