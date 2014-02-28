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

package gda.device.detector.uview;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UViewTcpSocketConnection {
	
	private static final Logger logger = LoggerFactory.getLogger(UViewTcpSocketConnection.class);

	private String host;
	
	private int port;
	
	private Socket socket;
	
	private BufferedInputStream inStream;
	
	private PrintStream outStream;
	
	private boolean connected;
	
	public UViewTcpSocketConnection(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public synchronized void connect() throws IOException {
		if (connected) {
			return;
		}
		try { 
			socket = new Socket(host, port);
			outStream = new PrintStream(socket.getOutputStream());
			inStream = new BufferedInputStream(socket.getInputStream());
			initializeAscii();
			connected = true;
		} catch (IOException e) {
			closeOutputStream();
			closeInputStream(false);
			closeSocket(false);
			throw e;
		}
	}
	
	public synchronized void initializeAscii() throws IOException {
		byte[] response = new byte[2];
		outStream.print("asc\0");
		inStream.read(response);
		if (response[0] != 0x30 || response[1] != 0x00 ) { //"0\0"
			throw new IOException("Invalid response configuring UView TCP port to ASCII");
		}
	}
	
	public synchronized void disconnect() {
		closeSocket(true);
		closeOutputStream();
		closeInputStream(true);
		connected = false;
	}
	
	private void closeOutputStream() {
		if (outStream != null) {
			outStream.close();
			outStream = null;
		}
	}
	
	private void closeInputStream(boolean logErrors) {
		if (inStream != null) {
			try {
				inStream.close();
			} catch (IOException e) {
				if (logErrors) logger.warn("Unable to close input stream", e);
			}
			inStream = null;
		}
	}
	
	private void closeSocket(boolean logErrors) {
		if (socket != null) {
			try {
				outStream.print("clo\0");
				socket.close();
			} catch (IOException e) {
				if (logErrors) logger.warn("Unable to close input stream", e);
			}
			socket = null;
		}
	}
	
	public synchronized String sendCommand(String command) throws IOException {
		return new String( sendCommandBuffered(command, 1024) ).trim();
	}
	
	public synchronized byte[] sendCommandBuffered(String command, int len) throws IOException {
		checkConnected();
		byte[] response = new byte[len];
		outStream.print(command + "\0");
		inStream.read(response);
		return response;
	}
	
	private void checkConnected() throws IOException {
		if (!connected) throw new IOException("Not Connected");
	}
	
	@Override
	protected void finalize() {
		if (connected) {
			disconnect();
			logger.warn("Socket was not closed before finalization");
		}
	}
}
