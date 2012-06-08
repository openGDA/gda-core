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

package gda.io.socket;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An ethernet network class. This class was moved from experimental specific areas into <code>gda.io</code>. It is
 * used to write and read strings from a socket.
 */
public class SimpleSock {
	private static final Logger logger = LoggerFactory.getLogger(SimpleSock.class);

	private Socket socket;

	private OutputStreamWriter writer;

	private InputStreamReader reader;

	private int waitTime = 100;

	/**
	 * Initialise the connection
	 * 
	 * @param address
	 * @param port
	 * @throws IOException
	 * @throws UnknownHostException
	 * @throws ConnectException
	 */
	public void init(String address, int port) throws IOException, UnknownHostException, ConnectException {
		logger.info("Attempting connection to Host: " + address + " Port: " + port);

		socket = new Socket(InetAddress.getByName(address), port);
		writer = new OutputStreamWriter(socket.getOutputStream());
		reader = new InputStreamReader(socket.getInputStream());
	}

	/**
	 * Write a string into the socket
	 * 
	 * @param s
	 * @throws IOException
	 */
	public void write(String s) throws IOException {
		writer.write(s, 0, s.length());
		writer.write("\015\012", 0, 2);
		writer.flush();
	}

	/**
	 * Read a 64 byte block from the socket.
	 * 
	 * @return String
	 * @throws IOException
	 */
	public String read() throws IOException {
		StringBuffer sb = new StringBuffer(64);
		char c;

		do {
			while (!reader.ready()) {
				try {
					Thread.sleep(waitTime);
				} catch (InterruptedException e) {
					// deliberately do nothing
				}
			}

			c = (char) reader.read();
			sb.append(c);
		} while (c != '\n');

		return (sb.toString()).trim();
	}
}
