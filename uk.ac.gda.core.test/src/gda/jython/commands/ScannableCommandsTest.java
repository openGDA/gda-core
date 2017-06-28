/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.jython.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import gda.MockFactory;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.ScannableMotion;
import gda.jython.InterfaceProvider;
import gda.jython.MockJythonServerFacade;

public class ScannableCommandsTest {
	private ScannableMotion lev4;
	private ScannableMotion lev5a;
	private Scannable lev5b;
	private ScannableMotion lev6;

	/**
	 * Setups of environment for the tests
	 *
	 * @throws Exception
	 *             if setup fails
	 */
	@Before
	public void setUp() throws Exception {
		InterfaceProvider.setTerminalPrinterForTesting(new MockJythonServerFacade());

		lev4 = MockFactory.createMockScannableMotion("lev4", 4);
		lev5a = MockFactory.createMockScannableMotion("lev5a", 5);
		lev5b = MockFactory.createMockScannable("lev5b", 5);
		lev6 = MockFactory.createMockScannableMotion("lev6", 6);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testPosWithSingleMove() throws Exception {
		ScannableCommands.pos(lev4, 1.2);
		InOrder inOrder = inOrder(lev4);
		inOrder.verify(lev4).checkPositionValid(1.2);
		inOrder.verify(lev4).atLevelStart();
		inOrder.verify(lev4).atLevelMoveStart();
		inOrder.verify(lev4).asynchronousMoveTo(1.2);
		inOrder.verify(lev4).waitWhileBusy();
		inOrder.verify(lev4).atLevelEnd();
		inOrder.verify(lev4).getPosition();
	}

	/**
	 * TODO: This test is getting far too big
	 * @throws Exception
	 */
	@Test
	public void testPosWithTripleConcurrentMove() throws Exception {
		ScannableCommands.pos(lev5a, 1.3, lev6, 1.4, lev5b, 1.35, lev4, 1.2);
		InOrder inOrder = inOrder(lev4, lev5a, lev5b, lev6);

		// NOTE: The order of this four is unimportant
		inOrder.verify(lev5a).checkPositionValid(1.3);
		inOrder.verify(lev6).checkPositionValid(1.4);
		//inOrder.verify(lev5b).checkPositionValid(1.35); Not ScannableMotion so not checked
		inOrder.verify(lev4).checkPositionValid(1.2);

		inOrder.verify(lev4).atLevelStart();
		inOrder.verify(lev4).atLevelMoveStart();
		inOrder.verify(lev4).asynchronousMoveTo(1.2);
		inOrder.verify(lev4).waitWhileBusy();
		inOrder.verify(lev4).atLevelEnd();

		// NOTE: Order of any pair here is unimportant
		inOrder.verify(lev5a).atLevelStart();
		inOrder.verify(lev5b).atLevelStart();
		inOrder.verify(lev5a).atLevelMoveStart();
		inOrder.verify(lev5b).atLevelMoveStart();
		inOrder.verify(lev5a).asynchronousMoveTo(1.3);
		inOrder.verify(lev5b).asynchronousMoveTo(1.35);
		inOrder.verify(lev5a).waitWhileBusy();
		inOrder.verify(lev5b).waitWhileBusy();
		inOrder.verify(lev5a).atLevelEnd();
		inOrder.verify(lev5b).atLevelEnd();

		inOrder.verify(lev6).atLevelStart();
		inOrder.verify(lev6).atLevelMoveStart();
		inOrder.verify(lev6).asynchronousMoveTo(1.4);
		inOrder.verify(lev6).waitWhileBusy();
		inOrder.verify(lev6).atLevelEnd();

		// NOTE: getPosition() order unimportant
		inOrder.verify(lev5a).getPosition();
		inOrder.verify(lev6).getPosition();
		inOrder.verify(lev5b).getPosition();
		inOrder.verify(lev4).getPosition();

		for (Scannable scn : Arrays.asList(lev4, lev5a, lev5b, lev6)) {
			verify(scn, times(1)).atLevelStart();
			verify(scn, times(1)).atLevelMoveStart();
			verify(scn, times(1)).asynchronousMoveTo(anyObject());
			verify(scn, times(1)).waitWhileBusy();
			verify(scn, times(1)).atLevelEnd();
		}
	}

	/**
	 * TODO: This test is getting far too big
	 * @throws Exception
	 */
	@Test
	public void testPosWithTripleConcurrentMoveAndFailingScannable() throws Exception {
		doThrow(new DeviceException("lev5a move failed")).when(lev5a).waitWhileBusy();

		try {
			ScannableCommands.pos(lev5a, 1.3, lev6, 1.4, lev5b, 1.35, lev4, 1.2);
			assertFalse(false); // DeviceException expected
		} catch (DeviceException e) {
			assertEquals(e.getMessage(), "lev5a move failed");
		}
		InOrder inOrder = inOrder(lev4, lev5a, lev5b, lev6);
		// NOTE: The order of this four is unimportant
		inOrder.verify(lev5a).checkPositionValid(1.3);
		inOrder.verify(lev6).checkPositionValid(1.4);
		//inOrder.verify(lev5b).checkPositionValid(1.35); Not ScannableMotion so not checked
		inOrder.verify(lev4).checkPositionValid(1.2);

		inOrder.verify(lev4).atLevelStart();
		inOrder.verify(lev4).atLevelMoveStart();
		inOrder.verify(lev4).asynchronousMoveTo(1.2);
		inOrder.verify(lev4).waitWhileBusy();
		inOrder.verify(lev4).atLevelEnd();

		// NOTE: Order of any pair here is unimportant
		inOrder.verify(lev5a).atLevelStart();
		inOrder.verify(lev5b).atLevelStart();
		inOrder.verify(lev5a).atLevelMoveStart();
		inOrder.verify(lev5b).atLevelMoveStart();
		inOrder.verify(lev5a).asynchronousMoveTo(1.3);
		inOrder.verify(lev5b).asynchronousMoveTo(1.35);
		inOrder.verify(lev5a).waitWhileBusy(); // threw the DeviceException
		inOrder.verify(lev5b).stop();

		inOrder.verify(lev6, never()).atLevelMoveStart();
		inOrder.verify(lev6, never()).atLevelStart();
		inOrder.verify(lev6, never()).asynchronousMoveTo(anyObject());
		inOrder.verify(lev6, never()).waitWhileBusy();
		inOrder.verify(lev6, never()).atLevelEnd();

		// NOTE: getPosition() order unimportant
		inOrder.verify(lev5a, never()).getPosition();
		inOrder.verify(lev6, never()).getPosition();
		inOrder.verify(lev5b, never()).getPosition();
		inOrder.verify(lev4, never()).getPosition();
	}

	/**
	 * @throws InterruptedException
	 * @throws Exception
	 */
	@Test
	public void testAtCommandFailureForOkayPos() throws InterruptedException, Exception {
		ScannableCommands.pos(lev5a, 1.3, lev6, 1.4, lev5b, 1.35, lev4, 1.2);
		for (Scannable scn : Arrays.asList(lev4, lev5a, lev5b, lev6)) {
			verify(scn, times(0)).atCommandFailure();
		}
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testAtCommandFailureForPosException() throws Exception {
		Scannable failer = MockFactory.createMockScannable("failer");
		doThrow(new DeviceException("Planned failure for test")).when(failer).asynchronousMoveTo(anyObject());


		try{
			ScannableCommands.pos(lev5a, 1.3, lev6, 1.4, failer, 1.35, lev4, 1.2);
			Assert.fail("InterruptedException expected");
		}catch (Exception e) {
			// We expect one of these!
		}

		for (Scannable scn : Arrays.asList(lev4, lev5a, failer, lev6)) {
			verify(scn, times(1)).atCommandFailure();
		}
	}
}
