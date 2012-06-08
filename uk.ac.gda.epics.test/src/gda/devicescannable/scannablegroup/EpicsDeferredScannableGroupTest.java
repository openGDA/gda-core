/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.devicescannable.scannablegroup;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import gda.MockFactory;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.controlpoint.EpicsControlPoint;
import gda.device.scannable.scannablegroup.EpicsDeferredScannableGroup;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

public class EpicsDeferredScannableGroupTest {

	EpicsDeferredScannableGroup group;
	EpicsControlPoint mockedControlPoint;
	Scannable rawa;
	Scannable rawb;
	Scannable rawc;

	@Before
	public void setUp() throws DeviceException {
		rawa = MockFactory.createMockScannable("a");
		rawb = MockFactory.createMockScannable("b");
		rawc = MockFactory.createMockScannable("c");
		mockedControlPoint = mock(EpicsControlPoint.class);
		when(mockedControlPoint.getPosition()).thenReturn(0);
		group = new EpicsDeferredScannableGroup();
		group.setGroupMembers(new Scannable[] { rawa, rawb, rawc });
		group.setDeferredControlPoint(mockedControlPoint);
	}

	@Test
	public void testAtCommandFailure() throws DeviceException {

		InOrder inOrder = inOrder(mockedControlPoint, rawa, rawb, rawc);
		group.atCommandFailure();
		inOrder.verify(rawa).stop();
		inOrder.verify(rawb).stop();
		inOrder.verify(rawc).stop();
		inOrder.verify(mockedControlPoint).setValue(0);
	}

	@Test
	public void testStop() throws DeviceException {
		InOrder inOrder = inOrder(mockedControlPoint, rawa, rawb, rawc);
		group.stop();
		inOrder.verify(rawa).stop();
		inOrder.verify(rawb).stop();
		inOrder.verify(rawc).stop();
		inOrder.verify(mockedControlPoint).setValue(0);
	}

	@Test
	public void testAsynchronousMoveTo() throws DeviceException {
		InOrder inOrder = inOrder(mockedControlPoint, rawa, rawb, rawc);
		group.asynchronousMoveTo(new Double[] { 1., 2., 3. });
		inOrder.verify(mockedControlPoint).setValue(1);
		inOrder.verify(rawa).asynchronousMoveTo(1.);
		inOrder.verify(rawb).asynchronousMoveTo(2.);
		inOrder.verify(rawc).asynchronousMoveTo(3.);
		inOrder.verify(mockedControlPoint).setValue(0);
	}

	@Test
	public void testGetPosition() throws DeviceException {
		when(rawa.getPosition()).thenReturn(1.);
		when(rawb.getPosition()).thenReturn(2.);
		when(rawc.getPosition()).thenReturn(3.);
		Object pos = group.getPosition();
		assertArrayEquals(new Object[] { 1., 2., 3. }, (Object[]) pos);
	}

	@Test
	public void testSetDefer() throws DeviceException {
		group.setDefer(false);
		verify(mockedControlPoint).setValue(0.);
		group.setDefer(true);
		verify(mockedControlPoint).setValue(1.);
	}

	@Test
	public void testAsynchronousMoveToViaElements() throws DeviceException {
		Scannable a =  (group.getGroupMembers().get(0));
		Scannable c =  (group.getGroupMembers().get(2));
		InOrder inOrder = inOrder(mockedControlPoint, rawa, rawb, rawc);
		
		a.atLevelMoveStart();
		c.atLevelMoveStart();
		a.asynchronousMoveTo(1.);
		c.asynchronousMoveTo(3.);
		
		inOrder.verify(mockedControlPoint).setValue(1.);
		inOrder.verify(rawa).asynchronousMoveTo(1.);
		inOrder.verify(rawc).asynchronousMoveTo(3.);
		inOrder.verify(mockedControlPoint).setValue(0.);
	}
	
	@Test
	public void testIsBusy() throws DeviceException {
		when(rawa.isBusy()).thenReturn(false);
		when(rawb.isBusy()).thenReturn(true);
		when(rawc.isBusy()).thenReturn(false);
		assertTrue(group.isBusy());
		
	}

}
