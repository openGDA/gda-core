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

package gda.device.xspress;

import gda.device.DeviceBase;
import gda.factory.Findable;
import gda.util.ConsoleReader;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides Ethernet communications with OS9 exafsdc program.
 */

public class ExafsServer extends DeviceBase implements Findable {

	private static final Logger logger = LoggerFactory.getLogger(ExafsServer.class);

	private String debugName = getClass().getName();

	private String host = "none";

	private int port = -1;

	private boolean connected = false;

	private Socket socket = null;

	// private boolean stopThread = false;
	private AsynchronousReaderWriter arw;

	private String commandPrefix = "system command ";

	private String replyEndString = "\r\n";

	/**
	 * Constructor.
	 */
	public ExafsServer() {
	}

	/**
	 * @param host
	 * @param port
	 */
	public ExafsServer(String host, int port) {
		this.host = host;
		this.port = port;
		configure();
	}

	@Override
	public void configure() {
		openSocket();

		if (connected) {
			debugName = debugName + "(" + host + "," + port + ")";
			arw = new AsynchronousReaderWriter(socket);
			arw.setReplyEndString(replyEndString);
			String startupMessage = arw.getReply();
			logger.debug(debugName + " startup message received: " + startupMessage);
		}
	}

	/**
	 * @param host
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return The host name
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param port
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return The port number
	 */
	public int getPort() {
		return port;
	}

	/**
	 * 
	 * 
	 */
	public void quit() {
		arw.sendCommandAndGetReply(commandPrefix + "23 QUIT");
		arw.stop();
		closeSocket();
	}

	/**
	 * Closes the socket
	 */
	private void closeSocket() {
		if (socket != null) {
			try {
				connected = false;
				socket.close();
				socket = null;
			} catch (IOException ioe) {
				logger.error(debugName + " caught IOException while closing socket: " + ioe.getMessage());
			}
		}
	}

	/**
	 * @return true if connected
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * Creates the socket and its associated input and output streams
	 */
	protected void openSocket() {
		try {
			logger.debug(debugName + " opening socket to " + host + " " + port);
			socket = new Socket(host, port);
			connected = true;
		} catch (IOException ioe) {
			logger.error(debugName + " caught IOException opening socket: " + ioe.getMessage());
		}
	}

	/**
	 * Sends a command and gets a single line reply
	 * 
	 * @param command
	 *            the command to send
	 * @return the reply
	 */
	public ExafsServerReply sendCommand(String command) {
		return new ExafsServerReply(arw.sendCommandAndGetReply(commandPrefix + command));
	}

	/**
	 * Sends a command and gets list of replies. Covers up some of the difficulties caused by the exafs server.
	 * 
	 * @param command
	 *            the command to send
	 * @param lookFor
	 *            replies are added to the list until one contains this
	 * @return an ArrayList<ExafsServerReply> of ExafsServerReply objects
	 */
	public ArrayList<ExafsServerReply> sendCommand(String command, String lookFor) {
		ArrayList<ExafsServerReply> replyList = new ArrayList<ExafsServerReply>();

		ArrayList<String> replies = arw.sendCommandAndGetReply(commandPrefix + command, lookFor);
		for (String reply : replies)
			replyList.add(new ExafsServerReply(reply));

		return replyList;
	}

	/**
	 * Main program for testing purposes.
	 * 
	 * @param args
	 *            command line input arguments
	 */
	public static void main(String args[]) {
		ExafsServer exafsServer;
		String command;
		ExafsServerReply reply;
		String host = "xrsdev2";
		int port = 7000;

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-host"))
				host = args[i + 1];
			if (args[i].equals("-port"))
				port = Integer.valueOf(args[i + 1]).intValue();
		}

		logger.debug("ExafsServer connecting to host " + host + " on port " + port);

		exafsServer = new ExafsServer(host, port);

		while (exafsServer.isConnected()) {
			command = ConsoleReader.readString("Enter command: ");
			logger.debug("command is: " + command);

			if (command.startsWith("q")) {
				exafsServer.quit();
				System.exit(0);
			} else {
				reply = exafsServer.sendCommand(command);
				logger.debug("remote server replied: " + reply);
			}
		}
	}

	/**
	 * @return timeout
	 */
	public long getTimeOut() {
		return arw.getTimeOut();
	}

	/**
	 * @param i
	 *            timeout
	 */
	public void setTimeOut(long i) {
		arw.setTimeOut(i);
	}
}
