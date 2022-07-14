/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import gda.device.zebra.LogicGateConfiguration;
import gda.device.zebra.controller.Zebra;
import gda.epics.CachedLazyPVFactory;
import gda.epics.PV;
import gda.epics.ReadOnlyPV;

/**
 * Tests for ZebraImpl
 * <p>
 * Most of the functions that delegate to a PVFactory are not tested here.
 */
public class ZebraImplTest {

	private ZebraImpl zebraImpl;
	private CachedLazyPVFactory pvFactory;
	private ReadOnlyPV<Double[]> mockReadOnlyDoubleArrayPV;
	private PV<Integer> mockIntegerPV;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		mockReadOnlyDoubleArrayPV = mock(ReadOnlyPV.class);
		mockIntegerPV = mock(PV.class);
		pvFactory = mock(CachedLazyPVFactory.class);
		when(pvFactory.getReadOnlyPVDoubleArray(anyString())).thenReturn(mockReadOnlyDoubleArrayPV);
		when(pvFactory.getPVInteger(anyString())).thenReturn(mockIntegerPV);

		zebraImpl = new ZebraImpl();
		zebraImpl.setName("zebra");
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
		ZebraImpl zebraImpl2 = new ZebraImpl();
		zebraImpl2.setZebraPrefix("Zebra");
		try {
			zebraImpl2.afterPropertiesSet();
			fail("Calling afterPropertiesSet() with no name set should fail");
		} catch (Exception e) {
			assertEquals("name is not set", e.getMessage());
		}
	}

	@Test
	public void testAfterPropertiesSetEmptyName() {
		ZebraImpl zebraImpl2 = new ZebraImpl();
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
		ZebraImpl zebraImpl2 = new ZebraImpl();
		zebraImpl2.setName("zebra_two");
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
		assertEquals(mockReadOnlyDoubleArrayPV, result);
	}

	@Test
	public void testGetEnc1AvalPVEnc1UseAval() {
		zebraImpl.setUseAvalField(true);
		ReadOnlyPV<Double[]> result = zebraImpl.getEnc1AvalPV();
		verify(pvFactory).getReadOnlyPVDoubleArray("PC_ENC1.AVAL");
		assertEquals(mockReadOnlyDoubleArrayPV, result);
	}

	@Test
	public void testGetEnc1AvalPVEnc2() {
		ReadOnlyPV<Double[]> result = zebraImpl.getEnc1AvalPV();
		verify(pvFactory).getReadOnlyPVDoubleArray("PC_ENC1");
		assertEquals(mockReadOnlyDoubleArrayPV, result);
	}

	@Test
	public void testGetEnc1AvalPVEnc3UseAval() {
		zebraImpl.setUseAvalField(true);
		ReadOnlyPV<Double[]> result = zebraImpl.getEnc1AvalPV();
		verify(pvFactory).getReadOnlyPVDoubleArray("PC_ENC1.AVAL");
		assertEquals(mockReadOnlyDoubleArrayPV, result);
	}

	// getEncPV() returns the encoder corresponding to the parameter passed to it

	@Test
	public void testGetEncPVEnc1() {
		ReadOnlyPV<Double[]> result = zebraImpl.getEncPV(Zebra.PC_ENC_ENC1);
		verify(pvFactory).getReadOnlyPVDoubleArray("PC_ENC1");
		assertEquals(mockReadOnlyDoubleArrayPV, result);
	}

	@Test
	public void testGetEncPVEnc2() {
		ReadOnlyPV<Double[]> result = zebraImpl.getEncPV(Zebra.PC_ENC_ENC2);
		verify(pvFactory).getReadOnlyPVDoubleArray("PC_ENC2");
		assertEquals(mockReadOnlyDoubleArrayPV, result);
	}

	@Test
	public void testGetEncPVEnc3() {
		ReadOnlyPV<Double[]> result = zebraImpl.getEncPV(Zebra.PC_ENC_ENC3);
		verify(pvFactory).getReadOnlyPVDoubleArray("PC_ENC3");
		assertEquals(mockReadOnlyDoubleArrayPV, result);
	}

	private void applyAndGateCheckPVsAndCheckValues(LogicGateConfiguration config, List<Integer> expectedPVValues) throws IOException {
		zebraImpl.applyAndGateConfig(0, config);

		ArgumentCaptor<String> pvNames = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Integer> putValues = ArgumentCaptor.forClass(Integer.class);
		verify(pvFactory, times(6)).getPVInteger(pvNames.capture());
		verify(mockIntegerPV, times(6)).putWait(putValues.capture());

		assertEquals("AND0_ENA", pvNames.getAllValues().get(0));
		assertEquals("AND0_INP1", pvNames.getAllValues().get(1));
		assertEquals("AND0_INP2", pvNames.getAllValues().get(2));
		assertEquals("AND0_INP3", pvNames.getAllValues().get(3));
		assertEquals("AND0_INP4", pvNames.getAllValues().get(4));
		assertEquals("AND0_INV", pvNames.getAllValues().get(5));

		for(int i = 0; i < expectedPVValues.size(); i++) {
			assertEquals(expectedPVValues.get(i), putValues.getAllValues().get(i));
		}
	}

	@Test
	public void testGivenSimpleLogicGateConfigWhenApplyAndGateConfigCalledThenPVsWrittenAsExpected() throws IOException {
		LogicGateConfiguration config = new LogicGateConfiguration.Builder().input(1).source(3).build();

		applyAndGateCheckPVsAndCheckValues(config, Arrays.asList(1, 3, 0, 0, 0, 0));
	}

	@Test
	public void testGivenComplexLogicGateConfigWhenApplyAndGateConfigCalledThenPVsWrittenAsExpected() throws IOException {
		LogicGateConfiguration config = new LogicGateConfiguration.Builder()
				.input(1).source(17).invert()
				.input(4).source(53)
				.build();

		applyAndGateCheckPVsAndCheckValues(config, Arrays.asList(9, 17,0, 0, 53, 1));
	}

	@Test
	public void testGivenAnotherComplexLogicGateConfigWhenApplyAndGateConfigCalledThenPVsWrittenAsExpected() throws IOException {
		LogicGateConfiguration config = new LogicGateConfiguration.Builder()
				.input(3).source(23).invert()
				.input(1).source(61)
				.input(2).source(8).invert()
				.build();

		applyAndGateCheckPVsAndCheckValues(config, Arrays.asList(7, 61, 8, 23, 0, 6));
	}

	@Test
	public void testGivenAllLogicGatesAppliedWhenApplyAndGateConfigCalledThenPVsWrittenAsExpected() throws IOException {
		LogicGateConfiguration config = new LogicGateConfiguration.Builder()
				.input(1).source(4).invert()
				.input(2).source(15).invert()
				.input(3).source(27).invert()
				.input(4).source(38).invert()
				.build();

		applyAndGateCheckPVsAndCheckValues(config, Arrays.asList(15, 4, 15, 27, 38, 15));
	}
}
