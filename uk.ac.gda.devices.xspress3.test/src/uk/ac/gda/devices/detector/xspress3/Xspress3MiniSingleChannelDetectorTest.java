/*-
 * Copyright © 2026 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.detector.xspress3;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IntegerDataset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;

import gda.data.NumTracker;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.scannable.ScannableBase;
import gda.factory.FactoryException;

class Xspress3MiniSingleChannelDetectorTest {
	private static final String[] NEW_EXTRA_NAMES = {"test1","test2"};
	private Xspress3MiniSingleChannelDetector xspress3mini;
	private Xspress3MiniController controller;
	private double[][] fakeData;

	@BeforeEach
	void setupXspress3mini() throws FactoryException, DeviceException {
		// Mock the EPICS layer
		controller = mock(Xspress3MiniController.class);
		fakeData = new double[2][4096];
		Random random = new Random();
		for (int i = 0; i < 2; i++) {
			fakeData[i] = random.ints(4096, 0, 100).asDoubleStream().toArray();
		}
		when(controller.readoutDTCorrectedLatestSummedMCA(0, 0)).thenReturn(fakeData);

		xspress3mini = new Xspress3MiniSingleChannelDetector();
		xspress3mini.setName("xspress3mini");
		xspress3mini.setController(controller);
		xspress3mini.setDetectorDataEntryMap();

		try (MockedConstruction<NumTracker> mocked = mockConstruction(NumTracker.class)){
			xspress3mini.configure();
		}
	}

	@Test
	void testConfigure() {
		assertEquals(0,xspress3mini.getInputNames().length);
		assertArrayEquals(new String[] {xspress3mini.getName()}, xspress3mini.getExtraNames());
		assertArrayEquals(new String[] {"%5.5g"}, xspress3mini.getOutputFormat());
	}

	@Test
	void testSetExtraNames() {
		Xspress3MiniSingleChannelDetector xspress3miniMock = spy(xspress3mini);
		xspress3miniMock.setExtraNames(NEW_EXTRA_NAMES);
		assertEquals(NEW_EXTRA_NAMES, xspress3miniMock.getExtraNames());
		verify(xspress3miniMock).cacheFormats();
	}

	@Test
	void testAtScanStartNoSuperCall() throws DeviceException, InterruptedException {
		Xspress3MiniSingleChannelDetector xspress3miniMock = spy(xspress3mini);
		xspress3miniMock.setUseParentClassMethods(false);
		xspress3miniMock.atScanStart();
		assertTrue(xspress3miniMock.getIsFirstPoint());
		verify(xspress3miniMock, never()).waitWhileBusy();
		verify(xspress3miniMock, never()).stop();
	}

	@Test
	void testAtScanLineStartNoSuperCall() throws DeviceException {
		Xspress3MiniSingleChannelDetector xspress3miniMock = spy(xspress3mini);
		xspress3miniMock.setUseParentClassMethods(false);
		xspress3miniMock.atScanLineStart();
		verify(xspress3miniMock, never()).startRunningXspress3FrameSet();
	}

	@Test
	void testSetEmptyDetectorDataEntryMap() {
		xspress3mini.setDetectorDataEntryMap();
		assertNotNull(xspress3mini.detectorDataEntryMap);
		assertEquals(2,xspress3mini.detectorDataEntryMap.size());

		assertEquals(xspress3mini.detectorDataEntryMap.keySet(), Set.of(Xspress3MiniSingleChannelDetector.SUMMED_ARRAY_RECORD_NAME,	xspress3mini.getName()));

		assertNotNull(xspress3mini.detectorDataEntryMap.get(Xspress3MiniSingleChannelDetector.SUMMED_ARRAY_RECORD_NAME));
		assertNotNull(xspress3mini.detectorDataEntryMap.get(xspress3mini.getName()));

		assertEquals(xspress3mini.detectorDataEntryMap.get(Xspress3MiniSingleChannelDetector.SUMMED_ARRAY_RECORD_NAME).getName(),Xspress3MiniSingleChannelDetector.SUMMED_ARRAY_RECORD_NAME);
		assertTrue(xspress3mini.detectorDataEntryMap.get(Xspress3MiniSingleChannelDetector.SUMMED_ARRAY_RECORD_NAME).isEnabled());
		assertTrue(xspress3mini.detectorDataEntryMap.get(Xspress3MiniSingleChannelDetector.SUMMED_ARRAY_RECORD_NAME).getIsDetectorEntry());
		assertTrue(xspress3mini.detectorDataEntryMap.get(Xspress3MiniSingleChannelDetector.SUMMED_ARRAY_RECORD_NAME).getValue() instanceof IntegerDataset);
		assertEquals(xspress3mini.getMCASize(),xspress3mini.detectorDataEntryMap.get(Xspress3MiniSingleChannelDetector.SUMMED_ARRAY_RECORD_NAME).getValue().getSize());

		assertEquals(xspress3mini.detectorDataEntryMap.get(xspress3mini.getName()).getName(),xspress3mini.getName());
		assertTrue(xspress3mini.detectorDataEntryMap.get(xspress3mini.getName()).isEnabled());
		assertTrue(xspress3mini.detectorDataEntryMap.get(xspress3mini.getName()).getIsDetectorEntry());
		assertTrue(xspress3mini.detectorDataEntryMap.get(xspress3mini.getName()).getValue() instanceof DoubleDataset);
		assertEquals(1,xspress3mini.detectorDataEntryMap.get(xspress3mini.getName()).getValue().getSize());
	}

	@Test
	void testGetEmptyDetectorData() throws DeviceException {
		xspress3mini.setDetectorDataEntryMap();
		final NexusTreeProvider detectorData = xspress3mini.getFileStructure();
		assertNotNull(detectorData);
		assertTrue(detectorData instanceof NXDetectorData);
		NXDetectorData dd = (NXDetectorData) detectorData;
		assertNotNull(dd.getDoubleVals());
		assertArrayEquals(new Double[] {0.0}, dd.getDoubleVals());
		assertArrayEquals(new String[] {xspress3mini.getName()}, dd.getExtraNames());
		assertArrayEquals(new String[] {"%5.5g"}, dd.getOutputFormat());
		assertNotNull(dd.getNexusTree());
		assertEquals(String.format(dd.getOutputFormat()[0], 0.0), dd.toString());
	}

	@Test
	void testReadoutSimDetectorData() throws DeviceException {
		xspress3mini.collectData();
		final NexusTreeProvider detectorData = xspress3mini.readout();
		assertTrue(detectorData instanceof NXDetectorData);
		assertNotNull(detectorData);
		NXDetectorData dd = (NXDetectorData) detectorData;
		assertNotNull(dd.getDoubleVals());
		assertArrayEquals(new Double[] {Arrays.stream(fakeData[0]).sum()}, dd.getDoubleVals());
		assertArrayEquals(new String[] {xspress3mini.getName()}, dd.getExtraNames());
		assertArrayEquals(new String[] {"%5.5g"}, dd.getOutputFormat());
		assertNotNull(dd.getNexusTree());
		assertEquals(String.format(dd.getOutputFormat()[0], Arrays.stream(fakeData[0]).sum()), dd.toString());
	}

	static Stream<int[]> inputArrays() {
		return Stream.of(
			new int[] {1, 6, 2, 4},
			new int[] {4, 5},
			new int[] {2},
			new int[] {}
		);
	}

	@ParameterizedTest
	@MethodSource("inputArrays")
	void testSetRecordRoisOutputFormats(int[] recordRois) {
		xspress3mini.setRecordRois(recordRois);
		assertEquals(recordRois.length+1, xspress3mini.getExtraNames().length);
		assertEquals(recordRois.length+1, xspress3mini.getOutputFormat().length);
		assertEquals(xspress3mini.getName(), xspress3mini.getExtraNames()[0]);
		assertEquals(ScannableBase.DEFAULT_OUTPUT_FORMAT, xspress3mini.getOutputFormat()[0]);
		assertEquals(0,xspress3mini.getInputNames().length);
		for (int i=0;i<recordRois.length-1;i++) {
			assertEquals(ScannableBase.DEFAULT_OUTPUT_FORMAT, xspress3mini.getOutputFormat()[i+1]);
			assertEquals(String.format("roi%1d", recordRois[i])+Xspress3MiniSingleChannelDetector.TOTAL, xspress3mini.getExtraNames()[i+1]);
		}
	}
}
