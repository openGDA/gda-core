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

package gda.device.detector.analyser;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import gda.device.epicsdevice.EpicsDevice;
import gda.device.epicsdevice.FindableEpicsDevice;

public class EpicsMCASimpleTest {

	private static final HashMap<String, String> recordPVs = new HashMap<String, String>();

	private EpicsMCASimple mcaSimpleDevice;

	@BeforeClass
	public static void setUpClass() {
		recordPVs.put("RECORD", "BL0XI-DI-DET-01:aim_adc1");
	}

	@Before
	public void setUp() throws Exception {
		final EpicsDevice mcaDevice = new EpicsDevice("mca_ed", recordPVs, true);
		mcaDevice.configure();
		final FindableEpicsDevice epicsDevice = new FindableEpicsDevice("test", mcaDevice);

		mcaSimpleDevice = new EpicsMCASimple();
		mcaSimpleDevice.setName("mcaSimpleDevice");
		mcaSimpleDevice.setEpicsDevice(epicsDevice);
		mcaSimpleDevice.configure();
	}

	@Test
	public void testGetCalibrationParameters() throws Exception {
		final EpicsMCACalibration calibrationParams = (EpicsMCACalibration) mcaSimpleDevice.getCalibrationParameters();
		assertEquals("EGU", calibrationParams.getEngineeringUnits());
		assertEquals(1.0, calibrationParams.getCalibrationOffset(), 0.0001);
		assertEquals(0.0, calibrationParams.getCalibrationQuadratic(), 0.0001);
		assertEquals(1.0, calibrationParams.getCalibrationSlope(), 0.0001);
		assertEquals(0.0, calibrationParams.getTwoThetaAngle(), 0.0001);
	}

	@Test
	public void testGetData() throws Exception {
		final int[] data = (int[]) mcaSimpleDevice.getData();
		assertEquals(2048, data.length);
		for (int i = 0; i < data.length; i++) {
			assertEquals(i, data[i]);
		}
	}

	@Test
	public void testSetData() throws Exception {
		final int dataLen = ((int[]) mcaSimpleDevice.getData()).length;
		final int[] newData = new int[dataLen];
		for (int i = 0; i < dataLen; i++) {
			newData[i] = i;
		}
		mcaSimpleDevice.setData(newData);
		assertArrayEquals(newData, (int[]) mcaSimpleDevice.getData());
	}

	@Test
	public void testGetElapsedParameters() throws Exception {
		final float[] elapsedParams = (float[]) mcaSimpleDevice.getElapsedParameters();
		assertEquals(2, elapsedParams.length);
		assertEquals(1.1, elapsedParams[0], 0.001);
		assertEquals(1.0, elapsedParams[1], 0.001);
	}

	@Test
	public void testGetRegionsOfInterest() throws Exception {
		final EpicsMCARegionOfInterest[] mcaRois = (EpicsMCARegionOfInterest[]) mcaSimpleDevice.getRegionsOfInterest();
		assertEquals(32, mcaRois.length);
		for (int i = 0; i < mcaRois.length; i++) {
			final EpicsMCARegionOfInterest mcaRoi = mcaRois[i];
			assertEquals(i, mcaRoi.getRegionIndex());
			assertEquals(-1d, mcaRoi.getRegionLow(), 0.001);
			assertEquals(-1d, mcaRoi.getRegionHigh(), 0.001);
			assertEquals(0, mcaRoi.getRegionBackground());
			assertEquals(1d, mcaRoi.getRegionPreset(), 0.001);
			assertEquals(Integer.toString(i), mcaRoi.getRegionName());
		}
	}

	@Test
	public void testSetRegionsOfInterest() throws Exception {
		final EpicsMCARegionOfInterest[] newRegionsOfInterest = new EpicsMCARegionOfInterest[mcaSimpleDevice.getNumberOfRegions()];
		for (Integer i = 0; i < mcaSimpleDevice.getNumberOfRegions(); i++) {
			newRegionsOfInterest[i] = new EpicsMCARegionOfInterest(i, i, i * 2, i / 2, i, i.toString());
		}
		mcaSimpleDevice.setRegionsOfInterest(newRegionsOfInterest);
		assertArrayEquals(newRegionsOfInterest, (EpicsMCARegionOfInterest[]) mcaSimpleDevice.getRegionsOfInterest());
	}

	@Test
	public void testGetRegionsOfInterestCount() throws Exception {
		final double[][] roiCount = mcaSimpleDevice.getRegionsOfInterestCount();
		assertEquals(32, roiCount.length);
		for (int i = 0; i < roiCount.length; i++) {
			final double[] valuesForRegion = roiCount[i];
			assertEquals(2, valuesForRegion.length);
			assertEquals(i * 1000, valuesForRegion[0], 0.001);
			assertEquals(i * 1000, valuesForRegion[1], 0.001);
		}
	}

	@Test
	public void testDispose() throws Exception {
		mcaSimpleDevice.dispose();
		// Dispose does nothing in dummy mode, so functions should still return values
		assertNotNull(mcaSimpleDevice.getRegionsOfInterest());
	}

	@Test
	public void testGetDwellTme() throws Exception {
		assertEquals(1.0, mcaSimpleDevice.getDwellTime(), 0.001);
	}

	@Test
	public void testSetDwellTime() throws Exception {
		mcaSimpleDevice.setDwellTime(1.5);
		assertEquals(1.5, mcaSimpleDevice.getDwellTime(), 0.001);
	}

	@Test
	public void testGetNumberOfChannels() throws Exception {
		assertEquals(2048, mcaSimpleDevice.getNumberOfChannels());
	}

	@Test
	public void testSetNumberOfChannels() throws Exception {
		mcaSimpleDevice.setNumberOfChannels(100);
		assertEquals(100, mcaSimpleDevice.getNumberOfChannels());
	}

	@Test
	public void testGetPresets() throws Exception {
		final EpicsMCAPresets presets = (EpicsMCAPresets) mcaSimpleDevice.getPresets();
		assertEquals(1.0, presets.getPresetRealTime(), 0.001);
		assertEquals(1.0, presets.getPresetLiveTime(), 0.001);
		assertEquals(1, presets.getPresetCounts());
		assertEquals(1, presets.getPresetCountlow());
		assertEquals(1, presets.getPresetCountHigh());
		assertEquals(1, presets.getPresetSweeps());
	}

	@Test
	public void testSetPresets() throws Exception {
		final EpicsMCAPresets newPresets = new EpicsMCAPresets((float) 1.5, (float) 2.0, 1, 2, 3, 4);
		mcaSimpleDevice.setPresets(newPresets);
		assertEquals(newPresets, mcaSimpleDevice.getPresets());
	}
}
