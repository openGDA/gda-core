/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Random;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import gda.device.DeviceException;
import gda.device.detector.xmap.edxd.EDXDMappingController;
import gda.device.detector.xmap.edxd.IEDXDElement;
import gda.factory.FactoryException;

public class EpicsXmapControllerTest {

	private static final int NUMBER_OF_BINS = 1024;
	private static final int NUMBER_OF_ELEMENTS = 2;
	private static final int MAX_ALLOWED_ROIS = 8;

	private static final double[][] ELEMENT0_ROIS = new double[][] { { 1.2, 1.76 }, { 3.6, 5.3 }, { 6.2, 8.1 }, { 9.9, 10.2 } };
	private static final double[] ELEMENT0_LOW_ROIS = new double[] { 1.2, 9.9 };
	private static final double[] ELEMENT0_HIGH_ROIS = new double[] { 1.76, 10.2 };
	private static final double[] ELEMENT0_ROI_COUNTS = new double[] { 173.87, 28.4, 237.3, 134.2 };
	private static final int[] ELEMENT0_DATA = new int[NUMBER_OF_BINS];
	private static final double[] ELEMENT0_ENERGY_BINS = new double[NUMBER_OF_BINS];

	private static final double[][] ELEMENT1_ROIS = new double[][] { { 2.4, 3.1 }, { 4.2, 4.7 }, { 5.4, 6.1 }, { 8.2, 8.7 } };
	private static final double[] ELEMENT1_LOW_ROIS = new double[] { 2.4, 5.4 };
	private static final double[] ELEMENT1_HIGH_ROIS = new double[] { 3.1, 6.1 };
	private static final double[] ELEMENT1_ROI_COUNTS = new double[] { 93.234, 155.2, 784.3, 12.3 };
	private static final int[] ELEMENT1_DATA = new int[NUMBER_OF_BINS];
	private static final double[] ELEMENT1_ENERGY_BINS = new double[NUMBER_OF_BINS];

	private EpicsXmapController xmapController;
	private EDXDMappingController edxdController;
	private IEDXDElement subDetector0;
	private IEDXDElement subDetector1;

	@BeforeClass
	public static void setUpClass() {
		// Set upper limit to make numbers more readable when debugging
		final double maxValue = Math.pow(10, 6);
		final Random rand = new Random();
		for (int i = 0; i < NUMBER_OF_BINS; i++) {
			ELEMENT0_DATA[i] = Math.abs(rand.nextInt((int) maxValue));
			ELEMENT1_DATA[i] = Math.abs(rand.nextInt((int) maxValue));
			ELEMENT0_ENERGY_BINS[i] = Math.max(Math.abs(rand.nextDouble()), maxValue);
			ELEMENT1_ENERGY_BINS[i] = Math.max(Math.abs(rand.nextDouble()), maxValue);
		}
	}

	@Before
	public void setUp() throws Exception {
		subDetector0 = mock(IEDXDElement.class);
		when(subDetector0.readoutInts()).thenReturn(ELEMENT0_DATA);
		when(subDetector0.getROICounts()).thenReturn(ELEMENT0_ROI_COUNTS);
		when(subDetector0.getLowROIs()).thenReturn(ELEMENT0_LOW_ROIS);
		when(subDetector0.getHighROIs()).thenReturn(ELEMENT0_HIGH_ROIS);
		when(subDetector0.getEnergyBins()).thenReturn(ELEMENT0_ENERGY_BINS);

		subDetector1 = mock(IEDXDElement.class);
		when(subDetector1.readoutInts()).thenReturn(ELEMENT1_DATA);
		when(subDetector1.getROICounts()).thenReturn(ELEMENT1_ROI_COUNTS);
		when(subDetector1.getLowROIs()).thenReturn(ELEMENT1_LOW_ROIS);
		when(subDetector1.getHighROIs()).thenReturn(ELEMENT1_HIGH_ROIS);
		when(subDetector1.getEnergyBins()).thenReturn(ELEMENT1_ENERGY_BINS);

		edxdController = mock(EDXDMappingController.class);
		when(edxdController.getBins()).thenReturn(NUMBER_OF_BINS);
		when(edxdController.getNumberOfElements()).thenReturn(NUMBER_OF_ELEMENTS);
		when(edxdController.getMaxAllowedROIs()).thenReturn(MAX_ALLOWED_ROIS);
		when(edxdController.getSubDetector(0)).thenReturn(subDetector0);
		when(edxdController.getSubDetector(1)).thenReturn(subDetector1);

		xmapController = new EpicsXmapController();
		xmapController.setEdxdController(edxdController);
		xmapController.configure();
		verify(edxdController).getNumberOfElements();
	}

	@Test(expected = FactoryException.class)
	public void testConfigureNoControllerSet() throws Exception {
		final EpicsXmapController xmapControllerUnconfigured = new EpicsXmapController();
		xmapControllerUnconfigured.configure();
	}

	@Test
	public void testNumberOfElementsSet() throws Exception {
		assertEquals(NUMBER_OF_ELEMENTS, xmapController.getNumberOfElements());
	}

	@Test
	public void testClearAndStart() throws Exception {
		xmapController.clearAndStart();
		verify(edxdController).setResume(false);
		verify(edxdController).start();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testDeleteROIs() throws Exception {
		xmapController.deleteROIs(0);
	}

	@Test
	public void testGetAcquisitionTime() throws Exception {
		final double acquisitionTime = 0.55;
		when(edxdController.getAcquisitionTime()).thenReturn(acquisitionTime);
		assertEquals(acquisitionTime, xmapController.getAcquisitionTime(), 0.001);
	}

	@Test
	public void testGetDataFromSubdetector() throws Exception {
		final int[] data0 = xmapController.getData(0);
		final int[] data1 = xmapController.getData(1);

		assertArrayEquals(data0, ELEMENT0_DATA);
		assertArrayEquals(data1, ELEMENT1_DATA);
	}

	@Test
	public void testGetAllData() throws Exception {
		final int[][] data = xmapController.getData();

		assertEquals(2, data.length);
		assertTrue(Arrays.equals(data[0], ELEMENT0_DATA));
		assertTrue(Arrays.equals(data[1], ELEMENT1_DATA));
	}

	@Test
	public void testGetNumberOfBins() throws Exception {
		assertEquals(NUMBER_OF_BINS, xmapController.getNumberOfBins());
	}

	@Test
	public void testSetNumberOrBins() throws Exception {
		xmapController.setNumberOfBins(5);
		verify(edxdController).setBins(5);
	}

	@Test
	public void testGetNumberOfROIsFromController() {
		assertEquals(MAX_ALLOWED_ROIS, xmapController.getNumberOfROIs());
	}

	@Test
	public void testGetNumberOfROIsFails() throws Exception {
		when(edxdController.getMaxAllowedROIs()).thenThrow(new DeviceException("Exception in getMaxAllowedROIs()"));
		assertEquals(0, xmapController.getNumberOfROIs());
	}

	@Test
	public void testGetROICountsByIndex() throws Exception {
		final double[] roiCounts0 = xmapController.getROICounts(0);
		assertEquals(ELEMENT0_ROI_COUNTS[0], roiCounts0[0], 0.001);
		assertEquals(ELEMENT1_ROI_COUNTS[0], roiCounts0[1], 0.001);

		final double[] roiCounts3 = xmapController.getROICounts(3);
		assertEquals(ELEMENT0_ROI_COUNTS[3], roiCounts3[0], 0.001);
		assertEquals(ELEMENT1_ROI_COUNTS[3], roiCounts3[1], 0.001);
	}

	@Test
	public void testGetRoisByElement() throws Exception {
		assertArrayEquals(ELEMENT0_ROI_COUNTS, xmapController.getROIs(0), 0.001);
		assertArrayEquals(ELEMENT1_ROI_COUNTS, xmapController.getROIs(1), 0.001);
	}

	@Test
	public void testGetRoisByElementPassingDataArray() throws Exception {
		// The data array is in fact ignored
		final int[][] data = new int[][] { { 1, 2 }, { 3, 4 } };
		assertArrayEquals(ELEMENT0_ROI_COUNTS, xmapController.getROIs(0, data), 0.001);
		assertArrayEquals(ELEMENT1_ROI_COUNTS, xmapController.getROIs(1, data), 0.001);
	}

	@Test
	public void testGetROIsSum() throws Exception {
		final double[] expectedSum = new double[4];
		for (int i = 0; i < 4; i++) {
			expectedSum[i] = ELEMENT0_ROI_COUNTS[i] + ELEMENT1_ROI_COUNTS[i];
		}

		xmapController.setROI(ELEMENT0_ROIS, 0);
		xmapController.setROI(ELEMENT1_ROIS, 1);
		final double[] roiSum = xmapController.getROIsSum();
		assertArrayEquals(expectedSum, roiSum, 0.001);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetReadRate() throws Exception {
		assertEquals(0, xmapController.getReadRate(), 0.001);
	}

	@Test
	public void testGetRealTime() throws Exception {
		final double realTime = 98708.93;
		when(subDetector0.getRealTime()).thenReturn(realTime);
		assertEquals(realTime, xmapController.getRealTime(), 0.001);
	}

	@Test
	public void testGetStatus() throws Exception {
		final int status = 42;
		when(edxdController.getStatus()).thenReturn(status);
		assertEquals(status, xmapController.getStatus());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testGetStatusRate() throws Exception {
		assertEquals(0, xmapController.getStatusRate(), 0.001);
	}

	@Test
	public void testSetAcquisitionTime() throws Exception {
		final double collectionTime = 0.75;
		xmapController.setAcquisitionTime(collectionTime);
		verify(edxdController).setAquisitionTime(collectionTime);
	}

	@Test
	public void testSetNthROI() throws Exception {
		final double[][] rois = new double[][] { ELEMENT0_ROIS[1], ELEMENT1_ROIS[1] };
		final ArgumentCaptor<double[]> element0LowRoisCaptor = ArgumentCaptor.forClass(double[].class);
		final ArgumentCaptor<double[]> element0HighRoisCaptor = ArgumentCaptor.forClass(double[].class);
		final ArgumentCaptor<double[]> element1LowRoisCaptor = ArgumentCaptor.forClass(double[].class);
		final ArgumentCaptor<double[]> element1HighRoisCaptor = ArgumentCaptor.forClass(double[].class);

		xmapController.setNthROI(rois, 1);

		// The call should have set low & high ROI #1 respectively in each element
		verify(subDetector0).setLowROIs(element0LowRoisCaptor.capture());
		verify(subDetector0).setHighROIs(element0HighRoisCaptor.capture());
		verify(subDetector1).setLowROIs(element1LowRoisCaptor.capture());
		verify(subDetector1).setHighROIs(element1HighRoisCaptor.capture());

		assertEquals(ELEMENT0_ROIS[1][0], element0LowRoisCaptor.getValue()[1], 0.001);
		assertEquals(ELEMENT0_ROIS[1][1], element0HighRoisCaptor.getValue()[1], 0.001);
		assertEquals(ELEMENT1_ROIS[1][0], element1LowRoisCaptor.getValue()[1], 0.001);
		assertEquals(ELEMENT1_ROIS[1][1], element1HighRoisCaptor.getValue()[1], 0.001);

		verify(edxdController, times(2)).activateROI();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSetNumberOfElements() throws Exception {
		xmapController.setNumberOfElements(3);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSetNumberOfROIs() {
		xmapController.setNumberOfROIs(6);
	}

	@Test
	public void testSetRoisForElement() throws Exception {
		xmapController.setROI(ELEMENT0_ROIS, 0);

		final ArgumentCaptor<double[][]> roisCaptor = ArgumentCaptor.forClass(double[][].class);
		verify(subDetector0).setROIs(roisCaptor.capture());
		verifyZeroInteractions(subDetector1);

		verifyRoisSet(ELEMENT0_ROIS, roisCaptor.getValue());
		verify(edxdController).activateROI();
		assertEquals(ELEMENT0_ROIS.length, xmapController.getNumberOfROIs());
	}

	@Test
	public void testSetSameRoisForAllElements() throws Exception {
		xmapController.setROIs(ELEMENT0_ROIS);

		final ArgumentCaptor<double[][]> roisCaptor0 = ArgumentCaptor.forClass(double[][].class);
		final ArgumentCaptor<double[][]> roisCaptor1 = ArgumentCaptor.forClass(double[][].class);

		verify(subDetector0).setROIs(roisCaptor0.capture());
		verify(subDetector1).setROIs(roisCaptor1.capture());

		verifyRoisSet(ELEMENT0_ROIS, roisCaptor0.getValue());
		verifyRoisSet(ELEMENT0_ROIS, roisCaptor1.getValue());

		verify(edxdController, times(2)).activateROI();
		assertEquals(ELEMENT0_ROIS.length, xmapController.getNumberOfROIs());
	}

	/**
	 * When setting ROIs, if fewer ROIs are passed to the XMAP controller than the maximum allowed, the controller will pad them out with zeros
	 *
	 * @param roisPassedByCaller
	 *            The ROIs passed to the controller
	 * @param roisSetByController
	 *            The ROIs that the controller sets on the element
	 */
	private static void verifyRoisSet(double[][] roisPassedByCaller, double[][] roisSetByController) {
		// Verify the ROIs set explicitly
		for (int i = 0; i < roisPassedByCaller.length; i++) {
			assertArrayEquals(roisPassedByCaller[i], roisSetByController[i], 0.001);
		}
		// Verify the "padding"
		final double[] zeroRoi = new double[] { 0.0, 0.0 };
		for (int i = roisPassedByCaller.length; i < NUMBER_OF_ELEMENTS; i++) {
			assertArrayEquals(zeroRoi, roisSetByController[i], 0.001);
		}
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSetReadRateDouble() throws Exception {
		xmapController.setReadRate(1.1);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSetReadRateString() throws Exception {
		xmapController.setReadRate("1.1");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSetStatusRateDouble() throws Exception {
		xmapController.setStatusRate(4.2);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testSetStatusRateString() throws Exception {
		xmapController.setStatusRate("4.2");
	}

	@Test
	public void testStart() throws Exception {
		xmapController.start();
		verify(edxdController).start();
	}

	@Test
	public void testStop() throws Exception {
		xmapController.stop();
		verify(edxdController).stop();
	}

	@Test
	public void testGetEvents() throws Exception {
		final int events = 79328;
		when(edxdController.getEvents(0)).thenReturn(events);
		assertEquals(events, xmapController.getEvents(0));
	}

	@Test
	public void testGetICR() throws Exception {
		final double inputCountRate = 200.75;
		when(edxdController.getICR(1)).thenReturn(inputCountRate);
		assertEquals(inputCountRate, xmapController.getICR(1), 0.001);
	}

	@Test
	public void testGetOCR() throws Exception {
		final double outputCountRate = 345.67;
		when(edxdController.getOCR(1)).thenReturn(outputCountRate);
		assertEquals(outputCountRate, xmapController.getOCR(1), 0.001);
	}

	@Test
	public void testGetRoiParameters() throws Exception {
		final double[][] element0Expected = new double[][] { { 1.2, 1.76 }, { 9.9, 10.2 } };
		final double[][] element1Expected = new double[][] { { 2.4, 3.1 }, { 5.4, 6.1 } };

		assertArrayEquals(element0Expected, xmapController.getROIParameters(0));
		assertArrayEquals(element1Expected, xmapController.getROIParameters(1));
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void testGetRoiParametersInvalidIndex() throws Exception {
		xmapController.getROIParameters(2);
	}

	@Test
	public void testGetEnergyBinsForElement() throws Exception {
		assertArrayEquals(ELEMENT0_ENERGY_BINS, xmapController.getEnergyBins(0), 0.001);
		assertArrayEquals(ELEMENT1_ENERGY_BINS, xmapController.getEnergyBins(1), 0.001);
	}

	@Test
	public void testGetEnergyBinsForAllElements() throws Exception {
		final double[][] energyBins = xmapController.getEnergyBins();
		assertArrayEquals(ELEMENT0_ENERGY_BINS, energyBins[0], 0.001);
		assertArrayEquals(ELEMENT1_ENERGY_BINS, energyBins[1], 0.001);
	}
}