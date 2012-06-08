/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.scannable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import gda.MockFactory;
import gda.device.ControlPoint;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.FactoryException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

public class DeferredScannableTest {
	private DeferredScannable dScannable;
	ControlPoint mockedControlPoint;
	Scannable rawa;

	@Before
	public void setUp() throws DeviceException, FactoryException {
		rawa = MockFactory.createMockScannable("a");
		mockedControlPoint = mock(ControlPoint.class);
		when(mockedControlPoint.getPosition()).thenReturn(0);

		createScannable();
		getScannable().configure();
	}
	
	void createScannable() {
		dScannable = new DeferredScannable();
		dScannable.setName("DeferTestScannable");
		getScannable().setControlPointScannable(rawa);
		getScannable().setDeferredControlPoint(mockedControlPoint);
	}

	DeferredScannable getScannable() {
		return dScannable;
	}
	
	@Test
	public void testAtCommandFailure() throws DeviceException {

		InOrder inOrder = inOrder(mockedControlPoint, rawa);
		getScannable().atCommandFailure();
		inOrder.verify(rawa).stop();
		inOrder.verify(mockedControlPoint).setValue(0);
	}

	@Test
	public void testStop() throws DeviceException {
		InOrder inOrder = inOrder(mockedControlPoint, rawa);
		getScannable().stop();
		inOrder.verify(rawa).stop();
		inOrder.verify(mockedControlPoint).setValue(0);
	}

	@Test
	public void testAsynchronousMoveTo() throws DeviceException {
		InOrder inOrder = inOrder(mockedControlPoint, rawa);
		getScannable().asynchronousMoveTo(1.);
		inOrder.verify(mockedControlPoint).setValue(1);
		inOrder.verify(rawa).asynchronousMoveTo(1.);
		inOrder.verify(mockedControlPoint).setValue(0);
	}

	@Test
	public void testGetPosition() throws DeviceException {
		when(rawa.getPosition()).thenReturn(1.);
		Object pos = getScannable().getPosition();
		assertEquals(pos, 1.);
	}

	@Test
	public void testSetDefer() throws DeviceException {
		getScannable().setDefer(false);
		verify(mockedControlPoint).setValue(0.);
		getScannable().setDefer(true);
		verify(mockedControlPoint).setValue(1.);
	}


	
	@Test
	public void testIsBusy() throws DeviceException {
		when(rawa.isBusy()).thenReturn(true);
		assertTrue(getScannable().isBusy());
		
	}


}
