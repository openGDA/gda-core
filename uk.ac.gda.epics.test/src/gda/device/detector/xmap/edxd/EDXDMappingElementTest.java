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

package gda.device.detector.xmap.edxd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import gda.device.DeviceException;
import gda.device.detector.analyser.EpicsMCARegionOfInterest;
import gda.device.detector.analyser.EpicsMCASimple;
import gda.device.epicsdevice.XmapEpicsDevice;
import gda.factory.FactoryException;

public class EDXDMappingElementTest {

	private XmapEpicsDevice xmapDevice;
	private EpicsMCASimple simpleMca;

	// In this test, we work with 4 "real" ROIS and let the rest default to zero
	private static final int NUM_NONZERO_ROIS = 4;
	private static final int MAX_ROIS = 32;

	private EpicsMCARegionOfInterest[] initialRois;
	private double[][] newRois;

	private static final double[] NEW_LOW_ROIS = new double[] { 1.2, 1.76, 3.6, 5.3 };
	private static final double[] NEW_HIGH_ROIS = new double[] { 6.2, 8.1, 9.9, 10.2 };

	@Before
	public void setUp() throws Exception {
		initialRois = new EpicsMCARegionOfInterest[NUM_NONZERO_ROIS];
		for (int i = 0; i < NUM_NONZERO_ROIS; i++) {
			initialRois[i] = new EpicsMCARegionOfInterest(i, i + 2.5, i + 5.7, i, i + 0.6, "region-" + i);
		}

		newRois = new double[MAX_ROIS][2];
		for (int i = 0; i < NEW_LOW_ROIS.length; i++) {
			newRois[i][0] = NEW_LOW_ROIS[i];
			newRois[i][1] = NEW_HIGH_ROIS[i];
		}

		xmapDevice = mock(XmapEpicsDevice.class);
		when(xmapDevice.getRecordPV("MCA1")).thenReturn("BLXXI-EA-DET-01:MCA1");
		when(xmapDevice.getRecordPV("MCA2")).thenReturn("BLXXI-EA-DET-01:MCA2");

		simpleMca = mock(EpicsMCASimple.class);
		when(simpleMca.isConfigured()).thenReturn(true);
		when(simpleMca.getRegionsOfInterest()).thenReturn(initialRois);
		when(simpleMca.getNthRegionOfInterest(anyInt())).thenAnswer(invocation -> {
			final int index = invocation.getArgumentAt(0, Integer.class);
			if (index < initialRois.length) {
				return initialRois[index];
			}
			return new EpicsMCARegionOfInterest(0, 0.0, 0.0, 0, 0.0, "");
		});
	}

	@Test
	public void testConstructorElement1() throws Exception {
		@SuppressWarnings("unused")
		final EDXDMappingElement mappingElement1 = new EDXDMappingElement(xmapDevice, 1, simpleMca);
		verify(simpleMca).setName("MCA1");
		verify(simpleMca).setMcaPV("BLXXI-EA-DET-01:MCA1");
		verify(simpleMca).configure();
	}

	@Test
	public void testConstructorElement2() throws Exception {
		@SuppressWarnings("unused")
		final EDXDMappingElement mappingElement2 = new EDXDMappingElement(xmapDevice, 2, simpleMca);
		verify(simpleMca).setName("MCA2");
		verify(simpleMca).setMcaPV("BLXXI-EA-DET-01:MCA2");
		verify(simpleMca).configure();
	}

	@Test
	public void testConstructorInvalidElement() throws Exception {
		@SuppressWarnings("unused")
		final EDXDMappingElement mappingElement4 = new EDXDMappingElement(xmapDevice, 4, simpleMca);
		verify(simpleMca).setName("MCA4");
		verify(simpleMca).setMcaPV(null);
		verify(simpleMca).configure();
	}

	@Test
	public void testErrorInConfigure() throws Exception {
		doThrow(new FactoryException("Error configuring simpleMca")).when(simpleMca).configure();
		@SuppressWarnings("unused")
		final EDXDMappingElement mappingElement = new EDXDMappingElement(xmapDevice, 1, simpleMca);
		// Constructor currently does not fail: just logs an error.
	}

	@Test
	public void testGetLowROIs() throws Exception {
		final EDXDMappingElement mappingElement = new EDXDMappingElement(xmapDevice, 1, simpleMca);
		final double[] lowROIs = mappingElement.getLowROIs();
		assertEquals(MAX_ROIS, lowROIs.length);
		for (int i = 0; i < NUM_NONZERO_ROIS; i++) {
			assertEquals(i + 2.5, lowROIs[i], 0.0001);
		}
		for (int i = NUM_NONZERO_ROIS; i < MAX_ROIS; i++) {
			assertEquals(0.0, lowROIs[i], 0.0001);
		}
	}

	@Test
	public void testGetLowROIsMcaNotConfigured() throws Exception {
		when(simpleMca.isConfigured()).thenReturn(false);
		final EDXDMappingElement mappingElement = new EDXDMappingElement(xmapDevice, 1, simpleMca);
		final double[] lowROIs = mappingElement.getLowROIs();
		assertNull(lowROIs);
	}

	@Test
	public void testGetHighROIs() throws Exception {
		final EDXDMappingElement mappingElement = new EDXDMappingElement(xmapDevice, 1, simpleMca);
		final double[] highROIs = mappingElement.getHighROIs();
		assertEquals(MAX_ROIS, highROIs.length);
		for (int i = 0; i < NUM_NONZERO_ROIS; i++) {
			assertEquals(i + 5.7, highROIs[i], 0.0001);
		}
		for (int i = NUM_NONZERO_ROIS; i < MAX_ROIS; i++) {
			assertEquals(0.0, highROIs[i], 0.0001);
		}
	}

	@Test
	public void testGetHighROIsMcaNotConfigured() throws Exception {
		when(simpleMca.isConfigured()).thenReturn(false);
		final EDXDMappingElement mappingElement = new EDXDMappingElement(xmapDevice, 1, simpleMca);
		final double[] highROIs = mappingElement.getHighROIs();
		assertNull(highROIs);
	}

	@Test
	public void testSetLowRois() throws Exception {
		final EDXDMappingElement mappingElement = new EDXDMappingElement(xmapDevice, 1, simpleMca);
		mappingElement.setLowROIs(NEW_LOW_ROIS);

		final ArgumentCaptor<EpicsMCARegionOfInterest[]> roisCapture = ArgumentCaptor.forClass(EpicsMCARegionOfInterest[].class);
		verify(simpleMca).setRegionsOfInterest(roisCapture.capture());
		final EpicsMCARegionOfInterest[] roisParameter = roisCapture.getValue();

		for (int i = 0; i < NEW_LOW_ROIS.length; i++) {
			// Check that the ROI low limit has been changed
			assertEquals(NEW_LOW_ROIS[i], roisParameter[i].getRegionLow(), 0.001);
			// but that the high limit has not
			assertEquals(initialRois[i].getRegionHigh(), roisParameter[i].getRegionHigh(), 0.001);
		}
	}

	@Test
	public void testSetHighRois() throws Exception {
		final EDXDMappingElement mappingElement = new EDXDMappingElement(xmapDevice, 1, simpleMca);
		mappingElement.setHighROIs(NEW_HIGH_ROIS);

		final ArgumentCaptor<EpicsMCARegionOfInterest[]> roisCapture = ArgumentCaptor.forClass(EpicsMCARegionOfInterest[].class);
		verify(simpleMca).setRegionsOfInterest(roisCapture.capture());
		final EpicsMCARegionOfInterest[] roisParameter = roisCapture.getValue();

		for (int i = 0; i < NEW_HIGH_ROIS.length; i++) {
			// Check that the ROI high limit has been changed
			assertEquals(NEW_HIGH_ROIS[i], roisParameter[i].getRegionHigh(), 0.001);
			// but that the low limit has not
			assertEquals(initialRois[i].getRegionLow(), roisParameter[i].getRegionLow(), 0.001);
		}
	}

	@Test
	public void testSetRois() throws Exception {
		final EDXDMappingElement mappingElement = new EDXDMappingElement(xmapDevice, 1, simpleMca);

		mappingElement.setROIs(newRois);

		// We expect setRegionsOfInterest() to be called twice, once to set the new low ROIs,
		// then again to set the new high ROIS
		final ArgumentCaptor<EpicsMCARegionOfInterest[]> roisCapture = ArgumentCaptor.forClass(EpicsMCARegionOfInterest[].class);
		verify(simpleMca, times(2)).setRegionsOfInterest(roisCapture.capture());
		final List<EpicsMCARegionOfInterest[]> roisParameters = roisCapture.getAllValues();
		assertEquals(2, roisParameters.size());

		// First invocation should change low limits only
		final EpicsMCARegionOfInterest[] param1 = roisParameters.get(0);
		for (int i = 0; i < NUM_NONZERO_ROIS; i++) {
			assertEquals(newRois[i][0], param1[i].getRegionLow(), 0.001);
			assertEquals(initialRois[i].getRegionHigh(), param1[i].getRegionHigh(), 0.001);
		}
		for (int i = NUM_NONZERO_ROIS; i < MAX_ROIS; i++) {
			assertEquals(0.0, param1[i].getRegionLow(), 0.001);
			assertEquals(0.0, param1[i].getRegionHigh(), 0.001);
		}

		// Second invocation should change high limits
		final EpicsMCARegionOfInterest[] param2 = roisParameters.get(1);
		for (int i = 0; i < NUM_NONZERO_ROIS; i++) {
			assertEquals(newRois[i][0], param2[i].getRegionLow(), 0.001);
			assertEquals(newRois[i][1], param2[i].getRegionHigh(), 0.001);
		}
		for (int i = NUM_NONZERO_ROIS; i < MAX_ROIS; i++) {
			assertEquals(0.0, param2[i].getRegionLow(), 0.001);
			assertEquals(0.0, param2[i].getRegionHigh(), 0.001);
		}
	}


	@Test(expected = DeviceException.class)
	public void testSetRoisFails() throws Exception {
		final EDXDMappingElement mappingElement = new EDXDMappingElement(xmapDevice, 1, simpleMca);

		// Simulate failure to set ROIs by always returning the same ROI object
		when(simpleMca.getNthRegionOfInterest(anyInt())).thenReturn(new EpicsMCARegionOfInterest(0, 0.0, 0.0, 0, 0.0, ""));
		mappingElement.setROIs(newRois);
	}

	@Test
	public void testGetROICounts() throws Exception {
		final EDXDMappingElement mappingElement = new EDXDMappingElement(xmapDevice, 1, simpleMca);
		final double[][] roiCounts = new double[][] { { 1.4, 2.6 }, { 3.7, 8.9 }, { 10.3, 11.2 } };
		when(simpleMca.getRegionsOfInterestCount()).thenReturn(roiCounts);

		final double[] roiCountsReturned = mappingElement.getROICounts();
		assertEquals(3, roiCountsReturned.length);

		// Should return the first element in every tuple
		for (int i = 0; i < roiCountsReturned.length; i++) {
			assertEquals(roiCounts[i][0], roiCountsReturned[i], 0.001);
		}
	}

	@Test
	public void testGetROICountsMcaNotConfigured() throws Exception {
		final EDXDMappingElement mappingElement = new EDXDMappingElement(xmapDevice, 1, simpleMca);
		when(simpleMca.isConfigured()).thenReturn(false);
		assertNull(mappingElement.getROICounts());
	}
}
