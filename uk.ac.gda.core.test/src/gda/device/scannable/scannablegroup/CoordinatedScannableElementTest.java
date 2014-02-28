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
import gda.MockFactory;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.scannablegroup.CoordinatedScannableGroup;
import junit.framework.TestCase;

/**
 * 
 */
public class CoordinatedScannableElementTest extends TestCase{
	
	Scannable wrapped;
	CoordinatedScannableGroup mockedGroup;
	CoordinatedChildScannable element;

	@Override
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
	
	public void testPhysicalAsynchronousMoveTo() throws DeviceException {
		element.physicalAsynchronousMoveTo(1);
		verify(wrapped).asynchronousMoveTo(1);
	}
	
	public void testPhysicalIsBusy() throws DeviceException {
		element.physicalIsBusy();
		verify(wrapped).isBusy();
	}
	
	public void testAtLevelMoveStart() throws DeviceException {
		element.atLevelMoveStart();
		verify(mockedGroup).addChildToMove(element);
	}
	
	public void testAsynchronousMoveToWhenGroupTargeting() throws DeviceException {
		when(mockedGroup.isTargeting()).thenReturn(true);
		element.asynchronousMoveTo(1);
		verify(mockedGroup).setChildTarget(element, 1);
	}
	
	public void testAsynchronousMoveToWhenGroupNotTargeting() throws DeviceException {
		when(mockedGroup.isTargeting()).thenReturn(false);
		element.asynchronousMoveTo(1);
		verify(wrapped).asynchronousMoveTo(1);
	}
	

	public void testIsBusy() throws DeviceException {
		element.isBusy();
		verify(mockedGroup).isBusy();
	}
	
	public void testAtCommandFailure() throws DeviceException {
		element.atCommandFailure();
		verify(mockedGroup).atCommandFailure();
	}	

}
