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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import gda.device.epicsdevice.FindableEpicsDevice;
import gda.device.epicsdevice.ReturnType;

public class EpicsMCASimpleTest {

	private static final Pattern REGION_EXTRACTION_PATTERN = Pattern.compile(".R([0-9]+)[A-Z]*");

	private EpicsMCASimple mcaSimpleDevice;
	private FindableEpicsDevice epicsDevice;

	@Before
	public void setUp() throws Exception {
		epicsDevice = createEpicsDevice();

		mcaSimpleDevice = new EpicsMCASimple();
		mcaSimpleDevice.setName("mcaSimpleDevice");
		mcaSimpleDevice.setEpicsDevice(epicsDevice);
		mcaSimpleDevice.configure();
	}

	private FindableEpicsDevice createEpicsDevice() throws Exception {
		final int DATA_LEN = 2048;
		final int[] MCA_DATA = new int[DATA_LEN];
		for (int i = 0; i < DATA_LEN; i++) {
			MCA_DATA[i] = i;
		}

		final Map<String, Object> deviceData = new HashMap<>();
		deviceData.put(".CALO", 1.0);
		deviceData.put(".CALQ", 0.0);
		deviceData.put(".CALS", 1.0);
		deviceData.put(".DWEL", 1.0);
		deviceData.put(".EGU", "EGU");
		deviceData.put(".ELTM", 1.0);
		deviceData.put(".ERTM", 1.1);
		deviceData.put(".NMAX", 2048);
		deviceData.put(".NUSE", 2048);
		deviceData.put(".PCT", 1);
		deviceData.put(".PCTH", 1);
		deviceData.put(".PCTL", 1);
		deviceData.put(".PLTM", 1.0);
		deviceData.put(".PRTM", 1.0);
		deviceData.put(".PSWP", 1);
		deviceData.put(".TTH", 0.0);
		deviceData.put(".VAL", MCA_DATA);

		final FindableEpicsDevice device = mock(FindableEpicsDevice.class);
		when(device.getDummy()).thenReturn(true);

		when(device.getValue(any(ReturnType.class), anyString(), anyString())).thenAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) {
				final String field = invocation.getArgumentAt(2, String.class);

				// Handle specifiers that include region number
				if (Pattern.matches(".R[0-9]+N?", field)) { // Rn or RnN
					return Integer.parseInt(extractRegionNumber(field)) * 1000.0;
				}
				if (Pattern.matches(".R[0-9]+(LO|HI)", field)) { // RnLO or RnHI
					return -1;
				}
				if (Pattern.matches(".R[0-9]+BG", field)) {
					return (short) 0;
				}
				if (Pattern.matches(".R[0-9]+P", field)) {
					return 1.0;
				}
				if (Pattern.matches(".R[0-9]+NM", field)) {
					return extractRegionNumber(field);
				}

				// Look everything else up in the map
				return deviceData.get(field);
			}
		});
		return device;
	}

	private static String extractRegionNumber(String field) {
		final Matcher matcher = REGION_EXTRACTION_PATTERN.matcher(field);
		if (matcher.matches()) {
			return matcher.group(1);
		}
		throw new IllegalArgumentException(field + " does not contain a region number");
	}

	//----------------------------------------------------------------------------------------------

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
		final int numRegions = mcaSimpleDevice.getNumberOfRegions();
		final EpicsMCARegionOfInterest[] newRegionsOfInterest = new EpicsMCARegionOfInterest[mcaSimpleDevice.getNumberOfRegions()];
		for (int i = 0; i < numRegions; i++) {
			final int index = i;
			final double regionLow = i;
			final double regionHigh = i * 2;
			final int background = i / 2;
			final int preset = i;
			final String name = Integer.toString(i);
			newRegionsOfInterest[i] = new EpicsMCARegionOfInterest(index, regionLow, regionHigh, background, preset, name);
		}

		mcaSimpleDevice.setRegionsOfInterest(newRegionsOfInterest);
		for (int i = 0; i < numRegions; i++) {
			verify(epicsDevice).setValue("", ".R" + i + "LO", (int) newRegionsOfInterest[i].getRegionLow());
			verify(epicsDevice).setValue("", ".R" + i + "HI", (int) newRegionsOfInterest[i].getRegionHigh());
			verify(epicsDevice, atLeastOnce()).setValue("", ".R" + i + "BG", (short) newRegionsOfInterest[i].getRegionBackground());
			final double regionPreset = newRegionsOfInterest[i].getRegionPreset();
			verify(epicsDevice, atLeastOnce()).setValue("", ".R" + i + "IP", (regionPreset <= 0) ? 0 : 1);
			verify(epicsDevice, atLeastOnce()).setValue("", ".R" + i + "P", (i == 0) ? 0d : 1d);
			verify(epicsDevice, atLeastOnce()).setValue("", ".R" + i + "NM", Integer.toString(i));
		}
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
		verify(epicsDevice).dispose();
		assertNotNull(mcaSimpleDevice.getRegionsOfInterest());
	}

	@Test
	public void testGetDwellTme() throws Exception {
		assertEquals(1.0, mcaSimpleDevice.getDwellTime(), 0.001);
	}

	@Test
	public void testSetDwellTime() throws Exception {
		mcaSimpleDevice.setDwellTime(1.5);
		verify(epicsDevice).setValue("", ".DWEL", 1.5);
	}

	@Test
	public void testGetNumberOfChannels() throws Exception {
		assertEquals(2048, mcaSimpleDevice.getNumberOfChannels());
	}

	@Test
	public void testSetNumberOfChannels() throws Exception {
		mcaSimpleDevice.setNumberOfChannels(100);
		verify(epicsDevice).setValue("", ".NUSE", 100);
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
		verify(epicsDevice).setValue("", ".PRTM", (double) newPresets.getPresetRealTime());
		verify(epicsDevice).setValue("", ".PLTM", (double) newPresets.getPresetLiveTime());
		verify(epicsDevice, times(2)).setValue("", ".PCT", (int) newPresets.getPresetCounts());
		verify(epicsDevice).setValue("", ".PCTL", (int) newPresets.getPresetCountlow());
		verify(epicsDevice).setValue("", ".PCTH", (int) newPresets.getPresetCountHigh());
		verify(epicsDevice).setValue("", ".PSWP", (int) newPresets.getPresetSweeps());
	}
}
