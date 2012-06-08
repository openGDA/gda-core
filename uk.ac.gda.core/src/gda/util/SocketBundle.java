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
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a Buffered reader and writer socket pair for a given host/port.
 */
public class SocketBundle {
	private static final Logger logger = LoggerFactory.getLogger(SocketBundle.class);

	private Socket s = null;

	private BufferedWriter os = null;

	private BufferedReader is = null;

	private int id;

	/**
	 * Opens the socket and creates the buffered reader and writer
	 * 
	 * @param host
	 * @param port
	 * @throws IOException
	 */
	public void openSocket(String host, int port) throws IOException {
		try {
			s = new Socket(host, port);
			id = s.getLocalPort();
			os = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
			is = new BufferedReader(new InputStreamReader(s.getInputStream()));

			logger.debug("Connected OK");

		} catch (IOException ex) {
			logger.error("SocketBundle failed to create socket: " + ex);
			throw ex;
		}

	}

	/**
	 * Closes the streams and the socket.
	 */
	public void closeSocket() {
		try {
			if (os != null) {
				os.close();
				os = null;
			}
			if (is != null) {
				is.close();
				is = null;
			}
			if (s != null) {
				s.close();
				s = null;
			}
		} catch (IOException ex) {
			logger.error("SocketBundle failed to close: " + ex);
		}
	}

	/**
	 * @return the BufferedReader object
	 */
	public BufferedReader getReader() {
		return is;
	}

	/**
	 * @return the BufferedWriter object
	 */
	public BufferedWriter getWriter() {
		return os;
	}

	/**
	 * @return the local port number of which the socket is bound
	 */
	public int getId() {
		return id;
	}

}
