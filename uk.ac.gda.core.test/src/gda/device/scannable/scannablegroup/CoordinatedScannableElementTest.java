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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gda.MockFactory;
import gda.device.DeviceException;
import gda.device.Scannable;

/**
 *
 */
public class CoordinatedScannableElementTest {

	private Scannable wrapped;
	private CoordinatedScannableGroup mockedGroup;
	private CoordinatedChildScannable element;

	@BeforeEach
	public void setUp() throws Exception {
		wrapped = MockFactory.createMockScannable("wrapped");
		mockedGroup = mock(CoordinatedScannableGroup.class, "mockGroup");
		element = new CoordinatedChildScannable(wrapped, mockedGroup);
	}

//	@Test (expected=java.lang.Exception.class)
//	public void testInitWithMultifieldScannablesFails() throws DeviceException {
//		Scannable multi = MockFactory.createMockScannable("multi", new String [] {"i1","i2"}, new String [] {}, new String [] {"%f","%f"}, 5, new int []{0,0});
//		new CoordinatedElementScannable(multi, mockedGroup);
//	}

	@Test
	public void testPhysicalAsynchronousMoveTo() throws DeviceException {
		element.physicalAsynchronousMoveTo(1);
		verify(wrapped).asynchronousMoveTo(1);
	}

	@Test
	public void testPhysicalIsBusy() throws DeviceException {
		element.physicalIsBusy();
		verify(wrapped).isBusy();
	}

	@Test
	public void testAtLevelMoveStart() throws DeviceException {
		element.atLevelMoveStart();
		verify(mockedGroup).addChildToMove(element);
	}

	@Test
	public void testAsynchronousMoveToWhenGroupTargeting() throws DeviceException {
		when(mockedGroup.isTargeting()).thenReturn(true);
		element.asynchronousMoveTo(1);
		verify(mockedGroup).setChildTarget(element, 1);
	}

	@Test
	public void testAsynchronousMoveToWhenGroupNotTargeting() throws DeviceException {
		when(mockedGroup.isTargeting()).thenReturn(false);
		element.asynchronousMoveTo(1);
		verify(wrapped).asynchronousMoveTo(1);
	}

	@Test
	public void testIsBusy() throws DeviceException {
		element.isBusy();
		verify(mockedGroup).isBusy();
	}

	@Test
	public void testAtCommandFailure() throws DeviceException {
		element.atCommandFailure();
		verify(mockedGroup).atCommandFailure();
	}

}
