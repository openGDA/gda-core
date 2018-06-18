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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
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

import org.jscience.physics.quantities.Dimensionless;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.NonSI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import gda.TestHelpers;
import gda.device.epicsdevice.FindableEpicsDevice;
import gda.device.epicsdevice.ReturnType;
import gda.factory.Factory;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.util.converters.IQuantityConverter;
import gda.util.converters.IReloadableQuantitiesConverter;

public class EpicsMCASimpleTest {

	private static final String CALIBRATION_NAME = "mca_roi_conversion";

	private static final Pattern REGION_EXTRACTION_PATTERN = Pattern.compile(".R([0-9]+)[A-Z]*");

	private static final int CHANNEL = 355;
	private static final double ENERGY = 4.56;

	private static final Pattern ENERGY_PATTERN = Pattern.compile("4.56[0]* eV");

	private static final Quantity CHANNEL_QUANTITY = Dimensionless.valueOf(CHANNEL);
	private static final Quantity ENERGY_QUANTITY = Quantity.valueOf(ENERGY, NonSI.ELECTRON_VOLT);

	private static final String CHANNEL_TO_ENERGY_STRING = String.format("channelToEnergy:%d", CHANNEL);
	private static final String ENERGY_TO_CHANNEL_STRING = String.format("energyToChannel%.2f eV", ENERGY);

	private EpicsMCASimple mcaSimpleDevice;
	private FindableEpicsDevice epicsDevice;

	/**
	 * Allow creation of mock quantities converters that can be added to Finder
	 */
	private interface FindableConverter extends IQuantityConverter, IReloadableQuantitiesConverter, Findable { }
	private FindableConverter channelToEnergyConverter;
	private Factory testFactory;

	@Before
	public void setUp() throws Exception {
		epicsDevice = createEpicsDevice();

		mcaSimpleDevice = new EpicsMCASimple();
		mcaSimpleDevice.setName("mcaSimpleDevice");
		mcaSimpleDevice.setEpicsDevice(epicsDevice);
		mcaSimpleDevice.configure();

		// Create mock converters but don't add to finder at this point
		channelToEnergyConverter = mock(FindableConverter.class);
		when(channelToEnergyConverter.getName()).thenReturn(CALIBRATION_NAME);
		when(channelToEnergyConverter.toSource(CHANNEL_QUANTITY)).thenReturn(ENERGY_QUANTITY);
		when(channelToEnergyConverter.toTarget(ENERGY_QUANTITY)).thenReturn(CHANNEL_QUANTITY);

		testFactory = TestHelpers.createTestFactory("EpicsMCASimpleTestFactory");
		testFactory.addFindable(channelToEnergyConverter);
		Finder.getInstance().addFactory(testFactory);
	}

	@After
	public void tearDown() {
		// Remove factories from Finder so they do not affect other tests
		Finder.getInstance().removeAllFactories();
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
		deviceData.put(".SEQ", 8);
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

	@Test
	public void testClear() throws Exception {
		mcaSimpleDevice.clear();
		verify(epicsDevice).setValue("", ".ERAS", 1);
	}

	@Test
	public void testClearWaitForCompletion() throws Exception {
		mcaSimpleDevice.clearWaitForCompletion();
		verify(epicsDevice).setValue("", ".ERAS", 1);
	}

	@Test
	public void testCollectDataColletionTimeNotSet() throws Exception {
		mcaSimpleDevice.collectData();
		verify(epicsDevice).setValue("", ".ERAS", 1);
		verify(epicsDevice).setValue("", ".STOP", 1);
	}

	@Test
	public void testCollectDataColletionTimeSet() throws Exception {
		mcaSimpleDevice.setCollectionTime(0.5);
		mcaSimpleDevice.collectData();
		verify(epicsDevice).setValue("", ".ERAS", 1);
		verify(epicsDevice).setValueNoWait("", ".STRT", 1);
	}

	@Test
	public void testCreatesOwnFiles() throws Exception {
		assertFalse(mcaSimpleDevice.createsOwnFiles());
	}

	@Test
	public void testDeleteRegionOfInterest() throws Exception {
		mcaSimpleDevice.deleteRegionOfInterest(1);
		verify(epicsDevice, atLeastOnce()).setValue("", ".R1LO", -1);
		verify(epicsDevice, atLeastOnce()).setValue("", ".R1HI", -1);
		verify(epicsDevice, atLeastOnce()).setValue("", ".R1BG", (short) -1);
		verify(epicsDevice, atLeastOnce()).setValue("", ".R1IP", 0);
		verify(epicsDevice, atLeastOnce()).setValue("", ".R1P", 0d);
		verify(epicsDevice, atLeastOnce()).setValue("", ".R1NM", "");
	}

	@Test
	public void testEraseStartAcquisition() throws Exception {
		mcaSimpleDevice.eraseStartAcquisition();
		verify(epicsDevice).setValueNoWait("", ".ERST", 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetAttributeChannelToEnergyNoConverter() throws Exception {
		Finder.getInstance().removeAllFactories();
		mcaSimpleDevice.getAttribute(CHANNEL_TO_ENERGY_STRING);
	}

	@Test
	public void testGetAttributeChannelToEnergyConversion() throws Exception {
		Finder.getInstance().addFactory(testFactory);

		final String energyAttr = (String) (mcaSimpleDevice.getAttribute(CHANNEL_TO_ENERGY_STRING));
		// Compare energy, ignoring excess digits after decimal point
		assertTrue(ENERGY_PATTERN.matcher(energyAttr).matches());

		// Verify that it uses the input channel number
		final ArgumentCaptor<Quantity> quantityCaptor = ArgumentCaptor.forClass(Quantity.class);
		verify(channelToEnergyConverter).toSource(quantityCaptor.capture());
		assertEquals(CHANNEL, quantityCaptor.getValue().intValue());

		Finder.getInstance().removeAllFactories();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetAttributeEnergyToChannelNoConverter() throws Exception {
		Finder.getInstance().removeAllFactories();
		mcaSimpleDevice.getAttribute(ENERGY_TO_CHANNEL_STRING);
	}

	@Test
	public void testGetAttributeEnergyToChannelConversion() throws Exception {
		Finder.getInstance().addFactory(testFactory);

		final String channelAttr = (String) (mcaSimpleDevice.getAttribute(ENERGY_TO_CHANNEL_STRING));
		assertEquals(CHANNEL, Integer.parseInt(channelAttr));

		// Verify that it used the input energy
		final ArgumentCaptor<Quantity> quantityCaptor = ArgumentCaptor.forClass(Quantity.class);
		verify(channelToEnergyConverter).toTarget(quantityCaptor.capture());
		assertEquals(ENERGY, quantityCaptor.getValue().getAmount(), 0.001);
		assertEquals(NonSI.ELECTRON_VOLT, quantityCaptor.getValue().getUnit());

		Finder.getInstance().removeAllFactories();
	}

	@Test
	public void testGetAttributeNumberOfChannels() throws Exception {
		assertEquals(2048L, mcaSimpleDevice.getAttribute("NumberOfChannels"));
	}

	@Test
	public void testGetCalibrationName() {
		assertEquals(CALIBRATION_NAME, mcaSimpleDevice.getCalibrationName());
	}

	@Test
	public void testGetChannelForEnergy() throws Exception {
		Finder.getInstance().addFactory(testFactory);
		assertEquals(CHANNEL, mcaSimpleDevice.getChannelForEnergy(ENERGY));
	}

	@Test
	public void testGetEnergyForChannel() throws Exception {
		Finder.getInstance().addFactory(testFactory);
		assertEquals(ENERGY, mcaSimpleDevice.getEnergyForChannel(CHANNEL), 0.001);
	}

	@Test
	public void testGetDataDimensions() throws Exception {
		final int[] dataDimensions = mcaSimpleDevice.getDataDimensions();
		assertEquals(1, dataDimensions.length);
		assertEquals(2048, dataDimensions[0]);
	}

	@Test
	public void testGetDescription() throws Exception {
		assertEquals("EPICS Mca", mcaSimpleDevice.getDescription());
	}

	@Test
	public void testGetDetectorID() throws Exception {
		assertEquals("unknown", mcaSimpleDevice.getDetectorID());
	}

	@Test
	public void testGetDetectorType() throws Exception {
		assertEquals("EPICS", mcaSimpleDevice.getDetectorType());
	}

	@Test
	public void testGetIndexForRawROI() {
		assertEquals(0, mcaSimpleDevice.getIndexForRawROI());
	}

	@Test
	public void testGetNthRegionOfInterest() throws Exception {
		final EpicsMCARegionOfInterest roi = mcaSimpleDevice.getNthRegionOfInterest(4);
		assertEquals(4, roi.getRegionIndex());
		assertEquals(-1.0, roi.getRegionLow(), 0.001);
		assertEquals(-1.0, roi.getRegionHigh(), 0.001);
		assertEquals(0, roi.getRegionBackground());
		assertEquals(1.0, roi.getRegionPreset(), 0.001);
		assertEquals("4", roi.getRegionName());
	}

	@Test
	public void testGetNumberOfRegions() throws Exception {
		assertEquals(32, mcaSimpleDevice.getNumberOfRegions());
	}

	@Test
	public void testGetNumberOfValsPerRegionOfInterest() {
		mcaSimpleDevice.setReadNetCounts(false);
		assertEquals(1, mcaSimpleDevice.getNumberOfValsPerRegionOfInterest());
		mcaSimpleDevice.setReadNetCounts(true);
		assertEquals(2, mcaSimpleDevice.getNumberOfValsPerRegionOfInterest());
	}

	@Test
	public void testGetRoiCount() throws Exception {
		for (int i = 0; i < 32; i++) {
			assertEquals(i * 1000.0, mcaSimpleDevice.getRoiCount(i), 0.001);
		}
	}

	@Test
	public void testGetRoiNetCount() throws Exception {
		for (int i = 0; i < 32; i++) {
			assertEquals(i * 1000.0, mcaSimpleDevice.getRoiNetCount(i), 0.001);
		}
	}

	@Test
	public void testGetSequence() throws Exception {
		assertEquals(8, mcaSimpleDevice.getSequence());
	}

	@Test
	public void testReadout() throws Exception {
		final int[] data = (int[]) mcaSimpleDevice.readout();
		assertEquals(2048, data.length);
		for (int i = 0; i < data.length; i++) {
			assertEquals(i, data[i]);
		}
	}

	@Test
	public void testSetCalibration() throws Exception {
		final String engineeringUnits = "EGU";
		final float calibrationOffset = (float) 1.25;
		final float calibrationSlope = (float) 0.08;
		final float calibrationQuadratic = (float) 1.5;
		final float twoThetaAngle = (float) 0.4;
		final EpicsMCACalibration calibration = new EpicsMCACalibration(engineeringUnits, calibrationOffset, calibrationSlope, calibrationQuadratic, twoThetaAngle);

		mcaSimpleDevice.setCalibration(calibration);
		verify(epicsDevice, atLeastOnce()).setValue("", ".EGU", engineeringUnits);
		verify(epicsDevice, atLeastOnce()).setValue("", ".CALO", (double) calibrationOffset);
		verify(epicsDevice, atLeastOnce()).setValue("", ".CALS", (double) calibrationSlope);
		verify(epicsDevice, atLeastOnce()).setValue("", ".CALQ", (double) calibrationQuadratic);
		verify(epicsDevice, atLeastOnce()).setValue("", ".TTH", (double) twoThetaAngle);
	}

	@Test
	public void testSetSequence() throws Exception {
		final int sequence = 4;
		mcaSimpleDevice.setSequence(sequence);
		verify(epicsDevice).setValue("", ".SEQ", sequence);
	}

	@Test
	public void testStartAcquisition() throws Exception {
		mcaSimpleDevice.startAcquisition();
		verify(epicsDevice).setValueNoWait("", ".STRT", 1);
	}

	@Test
	public void testStopAcquisition() throws Exception {
		mcaSimpleDevice.stopAcquisition();
		verify(epicsDevice).setValue("", ".STOP", 1);
	}
}
