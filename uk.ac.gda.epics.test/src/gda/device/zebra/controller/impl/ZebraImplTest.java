/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package gda.device.zebra.controller.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import gda.device.zebra.controller.Zebra;
import gda.epics.CachedLazyPVFactory;
import gda.epics.ReadOnlyPV;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for ZebraImpl
 * <p>
 * Most of the functions that delegate to a PVFactory are not tested here.
 */
public class ZebraImplTest {

	private ZebraImpl zebraImpl;
	private CachedLazyPVFactory pvFactory;
	private ReadOnlyPV<Double[]> mockPV;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		mockPV = mock(ReadOnlyPV.class);
		pvFactory = mock(CachedLazyPVFactory.class);
		when(pvFactory.getReadOnlyPVDoubleArray(anyString())).thenReturn(mockPV);

		zebraImpl = new ZebraImpl();
		zebraImpl.setPvFactory(pvFactory);
		zebraImpl.setZebraPrefix("TESTZEBRA:ZEBRA:");
		zebraImpl.afterPropertiesSet();
	}

	@Test
	public void testInitialState() {
		assertFalse(zebraImpl.isUseAvalField());
		assertEquals("zebra", zebraImpl.getName());
		assertEquals("TESTZEBRA:ZEBRA:", zebraImpl.getZebraPrefix());
	}

	@Test
	public void testSetUseAvalField() {
		zebraImpl.setUseAvalField(true);
		assertTrue(zebraImpl.isUseAvalField());
	}

	@Test
	public void testSetZebraPrefix() {
		zebraImpl.setZebraPrefix("ZEBRA2");
		assertEquals("ZEBRA2", zebraImpl.getZebraPrefix());
	}

	@Test
	public void testAfterPropertiesSetMissingName() {
		final ZebraImpl zebraImpl2 = new ZebraImpl();
		zebraImpl2.setName("");
		zebraImpl2.setZebraPrefix("Zebra");
		try {
			zebraImpl2.afterPropertiesSet();
			fail("Calling afterPropertiesSet() with no name set should fail");
		} catch (Exception e) {
			assertEquals("name is not set", e.getMessage());
		}
	}

	@Test
	public void testAfterPropertiesSetMissingPrefix() {
		final ZebraImpl zebraImpl2 = new ZebraImpl();
		try {
			zebraImpl2.afterPropertiesSet();
			fail("Calling afterPropertiesSet() with no prefix set should fail");
		} catch (Exception e) {
			assertEquals("zebraPrefix is not set", e.getMessage());
		}
	}

	// getEnc1AvalPV() always returns the first encoder

	@Test
	public void testGetEnc1AvalPVEnc1() {
		ReadOnlyPV<Double[]> result = zebraImpl.getEnc1AvalPV();
		verify(pvFactory).getReadOnlyPVDoubleArray("PC_ENC1");
		assertEquals(mockPV, result);
	}

	@Test
	public void testGetEnc1AvalPVEnc1UseAval() {
		zebraImpl.setUseAvalField(true);
		ReadOnlyPV<Double[]> result = zebraImpl.getEnc1AvalPV();
		verify(pvFactory).getReadOnlyPVDoubleArray("PC_ENC1.AVAL");
		assertEquals(mockPV, result);
	}

	@Test
	public void testGetEnc1AvalPVEnc2() {
		ReadOnlyPV<Double[]> result = zebraImpl.getEnc1AvalPV();
		verify(pvFactory).getReadOnlyPVDoubleArray("PC_ENC1");
		assertEquals(mockPV, result);
	}

	@Test
	public void testGetEnc1AvalPVEnc3UseAval() {
		zebraImpl.setUseAvalField(true);
		ReadOnlyPV<Double[]> result = zebraImpl.getEnc1AvalPV();
		verify(pvFactory).getReadOnlyPVDoubleArray("PC_ENC1.AVAL");
		assertEquals(mockPV, result);
	}

	// getEncPV() returns the encoder corresponding to the parameter passed to it

	@Test
	public void testGetEncPVEnc1() {
		ReadOnlyPV<Double[]> result = zebraImpl.getEncPV(Zebra.PC_ENC_ENC1);
		verify(pvFactory).getReadOnlyPVDoubleArray("PC_ENC1");
		assertEquals(mockPV, result);
	}

	@Test
	public void testGetEncPVEnc2() {
		ReadOnlyPV<Double[]> result = zebraImpl.getEncPV(Zebra.PC_ENC_ENC2);
		verify(pvFactory).getReadOnlyPVDoubleArray("PC_ENC2");
		assertEquals(mockPV, result);
	}

	@Test
	public void testGetEncPVEnc3() {
		ReadOnlyPV<Double[]> result = zebraImpl.getEncPV(Zebra.PC_ENC_ENC3);
		verify(pvFactory).getReadOnlyPVDoubleArray("PC_ENC3");
		assertEquals(mockPV, result);
	}
}
