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

import gda.jython.socket.SocketClient;
import gda.jython.socket.SocketServer;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test harness for the gda.jython.socket package
 */
public class CommandSocketTest extends TestCase {
	/**
	 * Constructor.
	 */
	public CommandSocketTest() {
	}

	/**
	 * @return Test Suite
	 */
	public static Test suite() {
		return new TestSuite(CommandSocketTest.class);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	SocketServer commandSocket = null;

	SocketClient client = null;

	SocketClient client2 = null;

	static final int port = 4444;

	@Override
	protected void setUp() {
		// create a CommandSocket
		// (this test will fail if the port is firewall blocked!)
		commandSocket = new SocketServer();
		commandSocket.setPort(4444);
		uk.ac.gda.util.ThreadManager.getThread(commandSocket).start();
	}

	@Override
	protected void tearDown() {
		// close the socket
		commandSocket.listening = false;
	}

	/**
	 * 
	 */
	public void testServerAndClient() {
		client2 = new SocketClient();
		SocketClient.main(null);
	}

	/**
	 * 
	 */
	public void testSendBasicMessage() {
		// create a CommandClient instance
		client = new SocketClient();
		SocketClient.main(null);
		// send a simple Jython command using the CommandClient
	}

}
