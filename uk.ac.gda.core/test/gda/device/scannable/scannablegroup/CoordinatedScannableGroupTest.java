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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import gda.MockFactory;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.component.PositionValidator;

import org.junit.Before;
import org.junit.Test;


/**
 * Integration type test of CoordinatedScannableGroup and CoordinatedScannableElements.
 */
public class CoordinatedScannableGroupTest {

	CoordinatedScannableGroup group;
	Scannable rawa;
	Scannable rawb;
	Scannable rawc;
	ICoordinatedChildScannable a;
	ICoordinatedChildScannable b;
	ICoordinatedChildScannable c;
	private PositionValidator validator1;
	private PositionValidator validator2;

	@Before
	public void setUp() throws DeviceException {
		rawa = MockFactory.createMockScannable("rawa");
		rawb = MockFactory.createMockScannableMotion("rawb");
		rawc = MockFactory.createMockScannableMotion("rawc");
		group = new CoordinatedScannableGroup();
		group = spy(group);
		group.coordinatedScannableComponent = new CoordinatedParentScannableComponent(group); // Hack for test only
		group.setGroupMembers(new Scannable[] { rawa, rawb, rawc });
		a = (ICoordinatedChildScannable) (group.getGroupMembers().get(0));
		b = (ICoordinatedChildScannable) (group.getGroupMembers().get(1));
		c = (ICoordinatedChildScannable) (group.getGroupMembers().get(2));
		validator1 = mock(PositionValidator.class);
		validator2 = mock(PositionValidator.class);
		
	}

	@Test
	public void testWrapScannable() {
		ICoordinatedChildScannable wrapped;
		wrapped = group.wrapScannable(rawa);
		assertEquals(rawa, ((ICoordinatedScannableGroupChildScannable) wrapped).getPhysicalScannable());
	}
	

	@Test
	public void testSetGroupMembers() {
		assertEquals(rawa, ((ICoordinatedScannableGroupChildScannable) a).getPhysicalScannable());
		assertEquals(rawb, ((ICoordinatedScannableGroupChildScannable) b).getPhysicalScannable());
		assertEquals(rawc, ((ICoordinatedScannableGroupChildScannable) c).getPhysicalScannable());
	}

	@Test
	public void testAddElementToMove() {
		assertFalse(group.isTargeting());
		group.addChildToMove(a);
		assertTrue(group.isTargeting());
	}

	@Test
	public void testSetElementTarget() throws DeviceException {
		group.addChildToMove(a);
		group.addChildToMove(b);
		group.setChildTarget(a, 1);
	}

	@Test
	public void testStartMove() throws DeviceException {
		
		doNothing().when(group).asynchronousMoveTo(anyObject());
		group.addChildToMove(a);
		group.addChildToMove(b);
		group.addChildToMove(c);
		group.setChildTarget(a, 1.);
		group.setChildTarget(b, 2.);
		group.setChildTarget(c, 3.);

//		verify(group).asynchronousMoveTo(anyObject());
		verify(group).asynchronousMoveTo(new Double[] {1.,2.,3.});
		verify(rawa, never()).asynchronousMoveTo(anyObject());
		verify(rawb, never()).asynchronousMoveTo(anyObject());
		verify(rawc, never()).asynchronousMoveTo(anyObject());
	}

	@Test
	public void testStartMovePartial() throws DeviceException {

		doNothing().when(group).asynchronousMoveTo(anyObject());
		group.addChildToMove(a);
		group.addChildToMove(c);
		group.setChildTarget(a, 1.);
		group.setChildTarget(c, 3.);
		verify(group).asynchronousMoveTo(new Double[] {1.,null,3.});
	}

	@Test
	public void testAtCommandFailure() throws DeviceException {
		group.atCommandFailure();
		verify(rawa).stop();
		verify(rawb).stop();
		verify(rawc).stop();
	}

	@Test
	public void testIsBusy() throws DeviceException {
		when(rawa.isBusy()).thenReturn(false);
		when(rawb.isBusy()).thenReturn(true);
		when(rawc.isBusy()).thenReturn(false);
		assertTrue(group.isBusy());
		
	}
	
	@Test
	public void testCheckPositionOkay() throws DeviceException {
		assertEquals(null, group.checkPositionValid(new Double[] {1., 2., 3.}));
		verify(rawa).checkPositionValid(new Double[] {1.});
		verify(rawb).checkPositionValid(new Double[] {2.});
		verify(rawc).checkPositionValid(new Double[] {3.});
	}
	
	@Test
	public void testCheckPositionFailForFailingMember() throws DeviceException {
		when(rawc.checkPositionValid(any())).thenReturn("c position is bad");
		assertEquals("c position is bad", group.checkPositionValid(new Double[] {1., 2., 3.}));
		verify(rawa).checkPositionValid(new Double[] {1.});
		verify(rawb).checkPositionValid(new Double[] {2.});
		verify(rawc).checkPositionValid(new Double[] {3.});
	}
	@Test
	public void testCheckPositionWithAdditionalValidatorOkay() throws DeviceException {
		configureWithmocksOfScannableBase();
		group.addPositionValidator("val1", validator1);
		group.addPositionValidator("val2", validator2);
		when(rawb.getPosition()).thenReturn(12.);
		when(((ScannableBase)rawa).externalToInternal(1.)).thenReturn(1.);
		when(((ScannableBase)rawb).externalToInternal(12.)).thenReturn(12.5);
		when(((ScannableBase)rawc).externalToInternal(3.)).thenReturn(3.);
		assertEquals(null, group.checkPositionValid(new Double[] {1., null, 3.}));
		verify(rawa).checkPositionValid(new Double[] {1.});
		verify(rawc).checkPositionValid(new Double[] {3.});
		verify(validator1).checkInternalPosition(new Double[] {1., 12.5, 3.});
		verify(validator2).checkInternalPosition(new Double[] {1., 12.5, 3.});
	}
	
	@Test
	public void testCheckPositionWithAdditionalValidatorsFailForFailingValidator() throws DeviceException {
		testCheckPositionWithAdditionalValidatorOkay(); //for setup
		when(validator2.checkInternalPosition(new Double[] {1., 12.5, 3.})).thenReturn("validator2 problem");
		assertEquals("validator2 problem", group.checkPositionValid(new Double[] {1., null, 3.}));
	
	}

	private void configureWithmocksOfScannableBase() throws DeviceException {
		rawa = MockFactory.createMockScannable(ScannableBase.class, "rawa", new String[] {"rawa"}, new String[] {}, new String[] {"%f"}, 5, 11.);
		rawb = MockFactory.createMockScannable(ScannableBase.class, "rawb", new String[] {"rawb"}, new String[] {}, new String[] {"%f"}, 5, 12.);
		rawc = MockFactory.createMockScannable(ScannableBase.class, "rawc", new String[] {"rawc"}, new String[] {}, new String[] {"%f"}, 5, 13.);
		group = new CoordinatedScannableGroup();
		group.setGroupMembers(new Scannable[] { rawa, rawb, rawc });
	}
	
	
	
	

}
