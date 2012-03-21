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

package gda.device.detector.xmap;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import gda.data.nexus.tree.INexusTree;
import gda.device.Analyser;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.detector.analyser.EpicsMCA;
import gda.device.detector.analyser.EpicsMCAPresets;
import gda.factory.FactoryException;

import java.util.ArrayList;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

public class XmapDetectorFromEpicsMcaTest {

	private XmapDetectorFromEpicsMca xmap;
	private EpicsMCA mca0;
	private EpicsMCA mca1;

	@Before
	public void setUp() {
		xmap = new XmapDetectorFromEpicsMca();
		xmap.setName("xmap");
		mca0 = mock(EpicsMCA.class);
		mca1 = mock(EpicsMCA.class);
		ArrayList<Analyser> mcalist = new ArrayList<Analyser>();
		mcalist.add(mca0);
		mcalist.add(mca1);
		xmap.setAnalysers(mcalist);
		// Note: not configured here to aid testing
	}

	@Test
	public void testGetNumberOfMca() throws DeviceException {
		assertEquals(2, xmap.getNumberOfMca());
	}

	@Test
	public void testLoadConfigurationFromFile() throws Exception {
		xmap.setConfigFileName("testfiles/gda/device/detector/xmap/XmapDetectorFromEpicsMcaTest.xml");
		xmap.loadConfigurationFromFile();
		assertArrayEquals(new String[] { "Pb_K", "Fe_K" }, xmap.getChannelLabels().toArray(new String[0]));
		assertArrayEquals(new String[] { "Element0_realtime", "Element0_livetime", "Element0_Pb_K", "Element0_Fe_K",
				"Element1_realtime", "Element1_livetime", "Element1_Pb_K", "Element1_Fe_K" }, xmap.getExtraNames());
		assertArrayEquals(new String[] {}, xmap.getInputNames());
		// TODO: outputFormat??
		// assertArrayEquals(new String[]{}, xmap.getOutputFormat());
	}

	@Test
	public void testConfigure() throws FactoryException {
		assertFalse(xmap.isUseLiveTime());
		assertFalse(xmap.isReadNetCounts());

		xmap.setConfigFileName("testfiles/gda/device/detector/xmap/XmapDetectorFromEpicsMcaTest.xml");
		xmap.configure();
		// just to check loadConfigurationFromFile has been called:
		assertArrayEquals(new String[] { "Pb_K", "Fe_K" }, xmap.getChannelLabels().toArray(new String[0]));

	}

	@Test
	public void testClear() throws DeviceException {
		xmap.clear();
		verify(mca0).clear();
		verify(mca1).clear();
	}

	@Test
	public void testStart() throws DeviceException {
		xmap.start();
		verify(mca0).startAcquisition();
		verify(mca1).startAcquisition();
	}

	@Test
	public void testClearAndStart() throws DeviceException {
		xmap.clearAndStart();
		InOrder inOrder = inOrder(mca0, mca1);
		inOrder.verify(mca0).eraseStartAcquisition();
		inOrder.verify(mca1).eraseStartAcquisition();
	}

	@Test
	public void testCollectData() throws DeviceException {
		xmap.collectData();
		verify(mca0).eraseStartAcquisition();
		verify(mca1).eraseStartAcquisition();
	}

	@Test
	public void testStop() throws DeviceException {
		xmap.stop();
		verify(mca0).stop();
		verify(mca1).stop();
	}

	@Test
	public void testSetNumberOfBins() throws DeviceException {
		xmap.setNumberOfBins(2000);
		verify(mca0).setNumberOfChannels(2000);
		verify(mca1).setNumberOfChannels(2000);

	}

	@Test
	public void testGetNumberOfBins() throws DeviceException {
		when(mca0.getNumberOfChannels()).thenReturn((long) 2000);
		when(mca1.getNumberOfChannels()).thenReturn((long) 2000);
		assertEquals(2000, xmap.getNumberOfBins());
	}

	@Test(expected = IllegalStateException.class)
	public void testGetNumberOfBinsNoConsensus() throws DeviceException {
		when(mca0.getNumberOfChannels()).thenReturn((long) 1000);
		when(mca1.getNumberOfChannels()).thenReturn((long) 2000);
		xmap.getNumberOfBins();
	}

	@Test
	public void testSetAcquisitionTimeRealTimeDefault() throws DeviceException {
		assertFalse(xmap.isUseLiveTime()); // i.e. use real time by default
		xmap.setAcquisitionTime(1000.);
		verify(mca0).setPresets((new EpicsMCAPresets((float) 1., 0, 0, 0, 0, 0)));
		verify(mca1).setPresets((new EpicsMCAPresets((float) 1., 0, 0, 0, 0, 0)));
		verify(mca0).setCollectionTime(1.);
		verify(mca1).setCollectionTime(1.);
	}

	@Test
	public void testSetAcquisitionTimeLiveTime() throws DeviceException {
		xmap.setUseLiveTime(true);
		assertTrue(xmap.isUseLiveTime()); // i.e. use real time by default
		xmap.setAcquisitionTime(1000.);
		verify(mca0).setPresets((new EpicsMCAPresets(0, (float) 1., 0, 0, 0, 0)));
		verify(mca1).setPresets((new EpicsMCAPresets(0, (float) 1., 0, 0, 0, 0)));
		verify(mca0).setCollectionTime(1.);
		verify(mca1).setCollectionTime(1.);
	}

	@Test
	public void testGetAcquisitionTimeNearlyEqual() throws DeviceException {
		when(mca0.getCollectionTime()).thenReturn(1.);
		when(mca1.getCollectionTime()).thenReturn(1.0001);
		assertEquals(1000., xmap.getAcquisitionTime(), .0001);
	}

	@Test
	public void testGetAcquisitionTime() throws DeviceException {
		when(mca0.getCollectionTime()).thenReturn(1.00001);
		when(mca1.getCollectionTime()).thenReturn(1.00001);
		assertEquals(1000., xmap.getAcquisitionTime(), .1);
	}

	@Test
	public void testGetAcquisitionTimeWithZero() throws DeviceException {
		when(mca0.getCollectionTime()).thenReturn(0.);
		when(mca1.getCollectionTime()).thenReturn(0.);
		assertEquals(0., xmap.getAcquisitionTime(), 0.0001);
	}

	@Test(expected = IllegalStateException.class)
	public void testGetAcquisitionTimeNoConsensus() throws DeviceException {
		when(mca0.getCollectionTime()).thenReturn(1.);
		when(mca1.getCollectionTime()).thenReturn(1.01);
		xmap.getAcquisitionTime();
	}

	@Test
	public void testGetRealTime() throws DeviceException {
		testGetAcquisitionTime();
		assertEquals(1000, xmap.getRealTime(), .1);
	}

	@Test(expected = IllegalStateException.class)
	public void testGetRealTimeNoConsensus() throws DeviceException {
		when(mca0.getCollectionTime()).thenReturn(1.);
		when(mca1.getCollectionTime()).thenReturn(1.01);
		xmap.getRealTime();
	}

	@Test
	public void testUpdate() {
		// nothing
	}

	@Test
	public void testCreatesOwnFiles() throws DeviceException {
		assertFalse(xmap.createsOwnFiles());
	}

	@Test
	public void testGetStatusIDLE() throws DeviceException {
		when(mca0.getStatus()).thenReturn(Detector.IDLE);
		when(mca1.getStatus()).thenReturn(Detector.IDLE);
		assertEquals(Detector.IDLE, xmap.getStatus());
	}

	@Test
	public void testGetStatusBUSY() throws DeviceException {
		when(mca0.getStatus()).thenReturn(Detector.BUSY);
		when(mca1.getStatus()).thenReturn(Detector.BUSY);
		assertEquals(Detector.BUSY, xmap.getStatus());
		when(mca0.getStatus()).thenReturn(Detector.IDLE);
		when(mca1.getStatus()).thenReturn(Detector.BUSY);
		assertEquals(Detector.BUSY, xmap.getStatus());
		when(mca0.getStatus()).thenReturn(Detector.BUSY);
		when(mca1.getStatus()).thenReturn(Detector.IDLE);
		assertEquals(Detector.BUSY, xmap.getStatus());
	}

	@Test
	public void testGetStatusFAULT() throws DeviceException {
		when(mca0.getStatus()).thenReturn(Detector.FAULT);
		when(mca1.getStatus()).thenReturn(Detector.FAULT);
		assertEquals(Detector.FAULT, xmap.getStatus());
		when(mca0.getStatus()).thenReturn(Detector.FAULT);
		when(mca1.getStatus()).thenReturn(Detector.BUSY);
		assertEquals(Detector.FAULT, xmap.getStatus());
		when(mca0.getStatus()).thenReturn(Detector.IDLE);
		when(mca1.getStatus()).thenReturn(Detector.FAULT);
		assertEquals(Detector.FAULT, xmap.getStatus());
	}

	@Test
	public void testGetDataInt() throws DeviceException {
		when(mca0.getNumberOfChannels()).thenReturn((long) 5);
		when(mca1.getNumberOfChannels()).thenReturn((long) 5);
		when(mca0.getData()).thenReturn(new int[] { 1, 2, 3, 4, 5 });
		when(mca1.getData()).thenReturn(new int[] { 11, 12, 13, 14, 15 });

	}

	@Test
	public void testGetData() throws DeviceException {
		when(mca0.getNumberOfChannels()).thenReturn((long) 5);
		when(mca1.getNumberOfChannels()).thenReturn((long) 5);
		when(mca0.getData()).thenReturn(new int[] { 1, 2, 3, 4, 5 });
		when(mca1.getData()).thenReturn(new int[] { 11, 12, 13, 14, 15 });
		assertArrayEquals(new int[][] { { 1, 2, 3, 4, 5 }, { 11, 12, 13, 14, 15 } }, xmap.getData());
	}

	@Test
	public void testGetDataIntWithMcasThatReturnZeroPaddedSpecta() throws DeviceException {
		when(mca0.getNumberOfChannels()).thenReturn((long) 5);
		when(mca1.getNumberOfChannels()).thenReturn((long) 5);
		when(mca0.getData()).thenReturn(new int[] { 1, 2, 3, 4, 5, 90, 91 });
		when(mca1.getData()).thenReturn(new int[] { 11, 12, 13, 14, 15, 92, 93, 94 });
		assertArrayEquals(new int[] { 1, 2, 3, 4, 5 }, xmap.getData(0));
		assertArrayEquals(new int[] { 11, 12, 13, 14, 15 }, xmap.getData(1));
	}

	// TODO: extra names and so on from CopyOfNexusXmap

	@Test
	public void testSetROINonPublic() throws DeviceException {
		when(mca0.getNumberOfRegions()).thenReturn(5);
		xmap.setROI(new double[][] { { 10., 20. }, { 30., 40. }, { 50., 60. } }, 0);
		verify(mca0).addRegionOfInterest(0, 10., 20., -1, -1, "roi0");
		verify(mca0).addRegionOfInterest(1, 30., 40., -1, -1, "roi1");
		verify(mca0).addRegionOfInterest(2, 50., 60., -1, -1, "roi2");
		verify(mca0).deleteRegionOfInterest(3);
		verify(mca0).deleteRegionOfInterest(4);
	}

	@Test
	public void testSetROIs() throws DeviceException {
		when(mca0.getNumberOfRegions()).thenReturn(5);
		when(mca1.getNumberOfRegions()).thenReturn(5);
		xmap.setROIs(new double[][] { { 10., 20. }, { 30., 40. }, { 50., 60. } });
		verify(mca0).addRegionOfInterest(0, 10., 20., -1, -1, "roi0");
		verify(mca0).addRegionOfInterest(1, 30., 40., -1, -1, "roi1");
		verify(mca0).addRegionOfInterest(2, 50., 60., -1, -1, "roi2");
		verify(mca0).deleteRegionOfInterest(3);
		verify(mca0).deleteRegionOfInterest(4);
		verify(mca1).addRegionOfInterest(0, 10., 20., -1, -1, "roi0");
		verify(mca1).addRegionOfInterest(1, 30., 40., -1, -1, "roi1");
		verify(mca1).addRegionOfInterest(2, 50., 60., -1, -1, "roi2");
		verify(mca1).deleteRegionOfInterest(3);
		verify(mca1).deleteRegionOfInterest(4);
	}

	@Test
	public void testReadout() throws DeviceException, FactoryException {
		xmap.setConfigFileName("testfiles/gda/device/detector/xmap/XmapDetectorFromEpicsMcaTest.xml");
		xmap.configure();
		testGetData();
		testSetROIs();
		when(mca0.getRegionsOfInterestCount()).thenReturn(new double[][] { { 100, 80 }, { 200, 180 }, { 300, 280 } });
		when(mca1.getRegionsOfInterestCount()).thenReturn(new double[][] { { 101, 81 }, { 201, 181 }, { 301, 281 } });
		when(mca0.getElapsedParameters()).thenReturn(new float[] { 1, (float) 1.1 });
		when(mca1.getElapsedParameters()).thenReturn(new float[] { 2, (float) 2.1 });

		// [Element0_realtime, Element0_livetime, Element0_Pb_K, Element0_Fe_K, Element1_realtime, Element1_livetime,
		// Element1_Pb_K, Element1_Fe_K]
		NXDetectorData readout = (NXDetectorData) xmap.readout();
		assertArrayEquals(new double[] { 1, 1.1, 100, 200, 2, 2.1, 101, 201 },
				ArrayUtils.toPrimitive(readout.getDoubleVals()), .001);
		INexusTree rootNode = readout.getNexusTree().getChildNode(0);
		assertEquals("xmap", rootNode.getName());
		assertArrayEquals(new double[] { 1. }, (double[]) rootNode.getChildNode("Element0_realtime", "SDS").getData()
				.getBuffer(), .001);
		assertArrayEquals(new double[] { 1.1 }, (double[]) rootNode.getChildNode("Element0_livetime", "SDS").getData()
				.getBuffer(), .001);
		assertArrayEquals(new double[] { 100 }, (double[]) rootNode.getChildNode("Element0_Pb_K", "SDS").getData()
				.getBuffer(), .001);
		assertArrayEquals(new double[] { 200 }, (double[]) rootNode.getChildNode("Element0_Fe_K", "SDS").getData()
				.getBuffer(), .001);
		assertArrayEquals(new double[] { 2 }, (double[]) rootNode.getChildNode("Element1_realtime", "SDS").getData()
				.getBuffer(), .001);
		assertArrayEquals(new double[] { 2.1 }, (double[]) rootNode.getChildNode("Element1_livetime", "SDS").getData()
				.getBuffer(), .001);
		assertArrayEquals(new double[] { 101 }, (double[]) rootNode.getChildNode("Element1_Pb_K", "SDS").getData()
				.getBuffer(), .001);
		assertArrayEquals(new double[] { 201 }, (double[]) rootNode.getChildNode("Element1_Fe_K", "SDS").getData()
				.getBuffer(), .001);
		assertArrayEquals(new int[] { 1, 2, 3, 4, 5 }, ((int[][]) rootNode.getChildNode("fullSpectrum", "SDS")
				.getData().getBuffer())[0]);
		assertArrayEquals(new int[] { 11, 12, 13, 14, 15 },
				((int[][]) rootNode.getChildNode("fullSpectrum", "SDS").getData().getBuffer())[1]);
	}

	@Test
	public void testGetNumberOfROIs() throws DeviceException {
		when(mca0.getNumberOfRegions()).thenReturn(5);
		when(mca1.getNumberOfRegions()).thenReturn(5);
		xmap.setROIs(new double[][] { { 10., 20. }, { 30., 40. }, { 50., 60. } });
		assertEquals(3, xmap.getNumberOfROIs());
	}

	@Test
	public void testGetROICounts() throws DeviceException {
		when(mca0.getRegionsOfInterestCount()).thenReturn(new double[][] { { 100, 80 }, { 200, 180 }, { 300, 280 } });
		when(mca1.getRegionsOfInterestCount()).thenReturn(new double[][] { { 101, 81 }, { 201, 181 }, { 301, 281 } });
		assertFalse(xmap.isReadNetCounts());
		assertEquals(300., xmap.getROICounts(2)[0], .0001);
		assertEquals(301., xmap.getROICounts(2)[1], .0001);
	}

	@Test
	public void testGetROIsSum() throws DeviceException {
		testSetROIs();
		when(mca0.getRegionsOfInterestCount()).thenReturn(new double[][] { { 100, 80 }, { 200, 180 }, { 300, 280 } });
		when(mca1.getRegionsOfInterestCount()).thenReturn(new double[][] { { 101, 81 }, { 201, 181 }, { 301, 281 } });
		assertFalse(xmap.isReadNetCounts());
		assertArrayEquals(new double[] { 201, 401, 601 }, xmap.getROIsSum(), .001);
	}
}
