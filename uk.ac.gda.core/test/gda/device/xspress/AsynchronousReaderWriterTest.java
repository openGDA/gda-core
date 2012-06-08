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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gda.device.xspress.AsynchronousReaderWriter;
import gda.util.SocketExecutor;
import gda.util.SocketServer;
import java.net.Socket;
import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * AsynchronousReaderWriterTest Class
 */
public class AsynchronousReaderWriterTest {

	@SuppressWarnings("unused")
	private static SocketServer ss;

	private static SocketExecutor dse = new TestSocketExecutor();

	private static AsynchronousReaderWriter arw;

	/**
	 * @throws Exception
	 */
	@BeforeClass
	public static void setUpBefore() throws Exception {
		ss = new SocketServer(dse, 14711);
		
		// Wait a few seconds for the port to be opened
		Thread.sleep(3000);
		
		Socket s = new Socket("localhost", 14711);
		arw = new AsynchronousReaderWriter(s);
		arw.setReplyEndString(System.getProperty("line.separator"));
	}

	/**
	 * 
	 */
	@Test
	public void testSetTimeout() {
		arw.setTimeOut(5000);
		assertEquals(5000l, arw.getTimeOut());
		// FIXME - should test at this point that timeOut actually works
	}

	/**
	 * 
	 */
	@Test
	public void testSendCommandAndGetReplyStringString() {
		ArrayList<String> reply = arw.sendCommandAndGetReply("goodbye", "Ok" + System.getProperty("line.separator"));
		assertEquals(2, reply.size());
		assertTrue(reply.get(1).equals("Ok" + System.getProperty("line.separator")));
	}

	/**
	 * 
	 */
	@Test
	public void testSendCommandAndGetReplyString() {
		String reply = arw.sendCommandAndGetReply("hello");
		assertTrue(reply.equals("Ok" + System.getProperty("line.separator")));
	}

	/**
	 */
	@AfterClass
	public static void tearDownAfter() {
		if (arw != null) {
			arw.stop();
			arw.sendCommandAndGetReply("quit");
		}
	}
}
