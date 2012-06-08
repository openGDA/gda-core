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

package gda.device.scannable.scannablegroup;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import gda.MockFactory;
import gda.device.DeviceException;
import gda.device.Scannable;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Integration type test of CoordinatedScannableGroup and CoordinatedScannableElements.
 */
public class ScannableGroupTest {

	ScannableGroup group;
	Scannable a;
	Scannable b;
	Scannable c;

	@Before
	public void setUp() throws DeviceException {
		a = MockFactory.createMockScannable("a");
		b = MockFactory.createMockScannable("b");
		c = MockFactory.createMockScannable("c");
		group = new ScannableGroup();
		group.setGroupMembers(new Scannable[] { a, b, c });
	}

	@Test
	public void testAsynchronousMoveTo() throws DeviceException {
		group.asynchronousMoveTo(new Double[] {1.,2.,3.});
		verify(a).asynchronousMoveTo(1.);
		verify(b).asynchronousMoveTo(2.);
		verify(c).asynchronousMoveTo(3.);
	}
	@Test
	public void testAsynchronousMoveToWithNulls() throws DeviceException {
		group.asynchronousMoveTo(new Double[] {1.,null,3.});
		verify(a).asynchronousMoveTo(1.);
		verify(b, times(0)).asynchronousMoveTo(any());
		verify(c).asynchronousMoveTo(3.);
	}
	
	@Test
	public void testCheckPositionOkay() throws DeviceException {
		assertEquals(null, group.checkPositionValid(new Double[] {1., 2., 3.}));
		verify(a).checkPositionValid(new Double[] {1.});
		verify(b).checkPositionValid(new Double[] {2.});
		verify(c).checkPositionValid(new Double[] {3.});
	}
	
	@Test
	public void testCheckPositionFails() throws DeviceException {
		when(c.checkPositionValid(any())).thenReturn("c position is bad");
		assertEquals("c position is bad", group.checkPositionValid(new Double[] {1., 2., 3.}));
		verify(a).checkPositionValid(new Double[] {1.});
		verify(b).checkPositionValid(new Double[] {2.});
		verify(c).checkPositionValid(new Double[] {3.});
	}
	
}
