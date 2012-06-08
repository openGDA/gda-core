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

import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * A test of DummyExafsServer should really test whether it fulfills its purpose of behaving like a real ExafsServer. In
 * order to test this properly we would have to send the same commands to dummy and real and check that we got the same
 * replies.Unfortunately this is a bit tricky to arrange.
 */
public class DummyExafsServerTest {
	private DummyExafsServer des;

	/**
	 */
	@BeforeClass
	public static void setUpBeforeClass() {
	}

	/**
	 */
	@AfterClass
	public static void tearDownAfterClass() {
	}

	/**
	 */
	@Before
	public void setUp() {
		des = new DummyExafsServer();
	}

	/**
	 */
	@After
	public void tearDown() {
	}

	/**
	 * 
	 */
	@Test
	public final void testSetHost() {
		des.setHost("ethelred");
		assertTrue(des.getHost().equals("ethelred"));
	}

	/**
	 * 
	 */
	@Test
	public final void testSetPort() {
		des.setPort(1234);
		assertEquals(new Integer(1234), new Integer(des.getPort()));
	}

	/**
	 * openSocket does nothing so testing it is easy
	 */
	@Test
	public final void testOpenSocket() {
		// openSocket does nothing so testing it is easy
	}

	/**
	 * 
	 */
	@Test
	public final void testSendCommandString() {
		ExafsServerReply esr;

		esr = des.sendCommand("nonsense");
		assertEquals(null, esr);

		esr = des.sendCommand("23 QUIT");
		assertEquals(null, esr);

		esr = des.sendCommand("228 5 9");
		assertTrue(esr.getReply().equals("dummy exafsserver 228 0 5 xspress boards initialized"));

		esr = des.sendCommand("227 0 1 1234");
		assertTrue(esr.getReply().equals("dummy exafsserver 227 0 window set"));
	}

	/**
	 * 
	 */
	@Test
	public final void testSendCommandStringString() {
		// Before testing the mcdata command we have to do an initialize
		// detectors
		// command
		des.sendCommand("228 5 9");
		int startChannel;
		int endChannel;
		int valuesPerLine;
		int expectedNumberOfValues;
		int expectedNumberOfLines;

		startChannel = 1;
		endChannel = 4000;
		valuesPerLine = 10;
		ArrayList<ExafsServerReply> replyList = des.sendCommand("226 0 " + startChannel + " " + endChannel + " 1 "
				+ valuesPerLine, "mcdata complete");
		expectedNumberOfValues = (endChannel - startChannel) + 1;
		expectedNumberOfLines = 4 + expectedNumberOfValues / valuesPerLine;
		if (expectedNumberOfValues % valuesPerLine != 0) {
			expectedNumberOfLines += 1;
		}
		assertEquals(expectedNumberOfLines, replyList.size());

		startChannel = 100;
		endChannel = 700;
		valuesPerLine = 5;
		replyList = des.sendCommand("226 0 " + startChannel + " " + endChannel + " 1 " + valuesPerLine,
				"mcdata complete");
		expectedNumberOfValues = (endChannel - startChannel) + 1;
		expectedNumberOfLines = 4 + expectedNumberOfValues / valuesPerLine;
		if (expectedNumberOfValues % valuesPerLine != 0) {
			expectedNumberOfLines += 1;
		}

		assertEquals(expectedNumberOfLines, replyList.size());

		replyList = des.sendCommand("246 dummy ", "readDone");
	}

	/**
	 * 
	 */
	@Test
	public final void testGetTimeOut() {
		assertEquals(10000, des.getTimeOut());
	}

	/**
	 * 
	 */
	@Test
	public final void testSetTimeOut() {
		des.setTimeOut(12345);
		assertEquals(12345, des.getTimeOut());
	}

}
