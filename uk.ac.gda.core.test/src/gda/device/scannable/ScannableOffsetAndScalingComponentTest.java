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

package gda.device.scannable;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import gda.device.Scannable;
import gda.device.scannable.component.ScannableOffsetAndScalingComponent;

/**
 * Note: this is largely tested through ScannableMotionUnitsBaseTeast
 */
public class ScannableOffsetAndScalingComponentTest {


	ScannableOffsetAndScalingComponent oc;
	private Scannable mockHostScannable;


	@Before
	public void setUp() {
		mockHostScannable = mock(Scannable.class);
		when(mockHostScannable.getInputNames()).thenReturn(new String[]{"i1", "i2"});
		oc = new ScannableOffsetAndScalingComponent();
		oc.setHostScannable(mockHostScannable);
	}

	@Test
	public void testExternalTowardInternalNothingSet() {
		Object object = new Object();
		assertEquals(object, oc.externalTowardInternal(object));
	}

	@Test
	public void testInternalTowardExternal() {
		Object object = new Object();
		assertEquals(object, oc.internalTowardExternal(object));
	}

	@Test
	public void testExternalTowardInternalWithOffsetOnly() {
		Object object = new Object();
		oc.setOffset(new Double[]{1., null});
		assertArrayEquals(new Object[]{9., object}, (Object[]) oc.externalTowardInternal(new Object[]{10, object}));
	}
	@Test
	public void testInternalTowardExternalWithOffsetOnly() {
		Object object = new Object();
		oc.setOffset(new Double[]{1., null});
		assertArrayEquals(new Object[]{11., object}, (Object[]) oc.internalTowardExternal(new Object[]{10, object}));
	}
	@Test
	public void testExternalTowardInternalWithScaleOnly() {
		Object object = new Object();
		oc.setScalingFactor(new Double[]{2., null});
		assertArrayEquals(new Object[]{5., object}, (Object[]) oc.externalTowardInternal(new Object[]{10, object}));
	}
	@Test
	public void testInternalTowardExternalWithScaleOnly() {
		Object object = new Object();
		oc.setScalingFactor(new Double[]{2., null});
		assertArrayEquals(new Object[]{20., object}, (Object[]) oc.internalTowardExternal(new Object[]{10, object}));
	}

	@Test
	public void testExternalTowardInternalWithBoth() {
		when(mockHostScannable.getInputNames()).thenReturn(new String[]{"i1", "i2", "i3", "i4"});
		Object object = new Object();
		oc.setScalingFactor(new Double[]{2., 4., null, null});
		oc.setOffset(new Double[]{20., null, 50., null});
		assertArrayEquals(new Object[]{40., 25., 50., object}, (Object[]) oc.externalTowardInternal(new Object[]{100, 100, 100, object}));
	}

	@Test
	public void testInternaltowardExternalWithBoth() {
		when(mockHostScannable.getInputNames()).thenReturn(new String[]{"i1", "i2", "i3", "i4"});
		Object object = new Object();
		oc.setScalingFactor(new Double[]{2., 4., null, null});
		oc.setOffset(new Double[]{20., null, 50., null});
		assertArrayEquals(new Object[]{100., 100., 100., object}, (Object[]) oc.internalTowardExternal(new Object[]{40., 25., 50., object}));
	}



}
