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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.startsWith;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import gda.device.DeviceException;
import gda.device.detector.xmap.edxd.EDXDController.PRESET_TYPES;
import gda.device.epicsdevice.FindableEpicsDevice;
import gda.device.epicsdevice.IEpicsChannel;
import gda.device.epicsdevice.ReturnType;
import gda.observable.IObserver;

/**
 * The following functions are not tested, as they make calls that will fail in a unit test context:
 * <p>
 * <li>configure() with no XMAP set
 * <li>reconfigure()
 * <li>saveCurrentSettings()
 * <li>loadSettings()
 * <li>listSettings()
 */
public class EDXDControllerTest {

	private class EDXDControllerForTest extends EDXDController {
		public DeviceException getCollectDataException() {
			return collectData_Exception;
		}

		public void setCollectDataException(DeviceException exception) {
			collectData_Exception = exception;
		}
	}

	// Default number of sub-detectors created by controller
	private static final int NUM_ELEMENTS = 24;

	private FindableEpicsDevice xmapDevice;
	private IEpicsChannel statusChannel;
	private EDXDControllerForTest controller;

	@Before
	public void setUp() {
		statusChannel = mock(IEpicsChannel.class);

		xmapDevice = mock(FindableEpicsDevice.class);
		when(xmapDevice.createEpicsChannel(ReturnType.DBR_NATIVE, "ACQUIRING", "")).thenReturn(statusChannel);

		controller = new EDXDControllerForTest();
		controller.setXmap(xmapDevice);
	}

	@Test
	public void testConfigure() throws Exception {
		controller.configure();
		verify(xmapDevice).createEpicsChannel(ReturnType.DBR_NATIVE, "ACQUIRING", "");
		verify(statusChannel).addIObserver(any(IObserver.class));
	}

	@Test
	public void testCollectData() throws Exception {
		final double collectionTime = 0.5;
		controller.configure();
		controller.setCollectionTime(collectionTime);
		controller.collectData();

		verify(xmapDevice).setValue("SETPRESETVALUE", "", collectionTime);
		verify(xmapDevice).setValue("SETPRESETTYPE", "", 1);
		verify(xmapDevice).setValue(null, "ACQUIRE", "", 1, 6);
		assertNull(controller.getCollectDataException());
	}

	@Test
	public void testCollectDataAcquireError() throws Exception {
		final double collectionTime = 0.5;
		doThrow(new DeviceException("Error in acquire")).when(xmapDevice).setValue(any(Object.class), eq("ACQUIRE"), anyString(), any(Object.class),
				anyDouble());

		controller.configure();
		controller.setCollectionTime(collectionTime);
		controller.collectData();

		verify(xmapDevice).setValue("SETPRESETVALUE", "", collectionTime);
		verify(xmapDevice).setValue("SETPRESETTYPE", "", 1);
		verify(xmapDevice).setValue(null, "ACQUIRE", "", 1, 6);
		assertNotNull(controller.getCollectDataException());
	}

	@Test
	public void testGetDescription() throws Exception {
		assertEquals("The EDXD Detector controller", controller.getDescription());
	}

	@Test
	public void testGetDetectorID() throws Exception {
		assertEquals(" EDXD Detector", controller.getDetectorID());
	}

	@Test
	public void testGetDetectorType() throws Exception {
		assertEquals("Multi channel MCA", controller.getDetectorType());
	}

	@Test
	public void testGetAcquisitionTime() throws Exception {
		final double presetValue = 0.6;
		when(xmapDevice.getValue(ReturnType.DBR_NATIVE, "GETPRESETVALUE", "")).thenReturn(new Double(presetValue));

		final double acquitisionTime = controller.getAcquisitionTime();
		verify(xmapDevice).getValue(ReturnType.DBR_NATIVE, "GETPRESETVALUE", "");
		assertEquals(presetValue, acquitisionTime, 0.001);
	}

	@Test
	public void testGetPresetType() throws Exception {
		final int presetType = 5;
		when(xmapDevice.getValue(ReturnType.DBR_NATIVE, "GETPRESETTYPE", "")).thenReturn(new Integer(presetType));

		final int typeReturned = controller.getPresetType();
		verify(xmapDevice).getValue(ReturnType.DBR_NATIVE, "GETPRESETTYPE", "");
		assertEquals(presetType, typeReturned);
	}

	@Test
	public void testCreatesOwnFiles() throws Exception {
		assertFalse(controller.createsOwnFiles());
	}

	@Test
	public void testVerifyData() throws Exception {
		controller.verifyData();
	}

	@Test(expected = DeviceException.class)
	public void testVerifyDataWhenError() throws Exception {
		controller.setCollectDataException(new DeviceException("Error in data collection"));
		controller.verifyData();
	}

	@Test
	public void testDefaultNumberOfElements() {
		assertEquals(NUM_ELEMENTS, controller.getNumberOfElements());
	}

	@Test
	public void testSetBins() throws Exception {
		final int numberOfBins = 4;
		when(xmapDevice.getValue(ReturnType.DBR_NATIVE, "GETNBINS", "")).thenReturn(numberOfBins);

		final int binsReturned = controller.setBins(numberOfBins);
		assertEquals(numberOfBins, binsReturned);
		verify(xmapDevice).setValue("SETNBINS", "", numberOfBins);
		verify(xmapDevice).getValue(ReturnType.DBR_NATIVE, "GETNBINS", "");
	}

	@Test
	public void testGetBins() throws Exception {
		final int numberOfBins = 4;
		when(xmapDevice.getValue(ReturnType.DBR_NATIVE, "GETNBINS", "")).thenReturn(numberOfBins);

		final int binsReturned = controller.getBins();
		assertEquals(numberOfBins, binsReturned);
		verify(xmapDevice).getValue(ReturnType.DBR_NATIVE, "GETNBINS", "");
	}

	@Test
	public void testSetDynamicRange() throws Exception {
		final double dynamicRange = 3.6;
		when(xmapDevice.getValue(ReturnType.DBR_NATIVE, "GETDYNRANGE0", "")).thenReturn(dynamicRange);

		final double rangeReturned = controller.setDynamicRange(dynamicRange);
		assertEquals(dynamicRange, rangeReturned, 0.001);
		verify(xmapDevice).setValue("SETDYNRANGE", "", dynamicRange);
		verify(xmapDevice).getValue(ReturnType.DBR_NATIVE, "GETDYNRANGE0", "");
	}

	@Test
	public void testSetBinWidth() throws Exception {
		final double binWidth = 0.08;
		controller.setBinWidth(binWidth);
		verify(xmapDevice).setValue("SETBINWIDTH", "", binWidth);
	}

	@Test
	public void testSetup() throws Exception {
		final double maxEnergy = 0.75;
		final int numBins = 4;
		when(xmapDevice.getValue(ReturnType.DBR_NATIVE, "GETDYNRANGE0", "")).thenReturn(maxEnergy * 2.0);
		when(xmapDevice.getValue(ReturnType.DBR_NATIVE, "GETNBINS", "")).thenReturn(numBins);

		controller.setup(maxEnergy, numBins);
		verify(xmapDevice).setValue("SETDYNRANGE", "", maxEnergy * 2.0);
		verify(xmapDevice).setValue("SETNBINS", "", numBins);
		verify(xmapDevice).setValue("SETBINWIDTH", "", (maxEnergy * 1000.0 / numBins));
	}

	@Test
	public void testSetPreampGain() throws Exception {
		final double preampGain = 2.5;

		// EDXDElement.setPreampGain() also calls getPreampGain(), so the mock XMAP device needs to handle this.
		// The same applies to other set* functions
		when(xmapDevice.getValue(eq(ReturnType.DBR_NATIVE), startsWith("GETPREAMPGAIN"), eq(""))).thenReturn(preampGain);

		controller.configure();
		controller.setPreampGain(preampGain);
		for (int i = 0; i < NUM_ELEMENTS; i++) {
			verify(xmapDevice).setValue("SETPREAMPGAIN" + (i + 1), "", preampGain);
			verify(xmapDevice).getValue(ReturnType.DBR_NATIVE, "GETPREAMPGAIN" + (i + 1), "");
		}
	}

	@Test
	public void testSetPeakTime() throws Exception {
		final double peakTime = 0.1;
		when(xmapDevice.getValue(eq(ReturnType.DBR_NATIVE), startsWith("GETPEAKTIME"), eq(""))).thenReturn(peakTime);

		controller.configure();
		controller.setPeakTime(peakTime);
		for (int i = 0; i < NUM_ELEMENTS; i++) {
			verify(xmapDevice).setValue("SETPEAKTIME" + (i + 1), "", peakTime);
			verify(xmapDevice).getValue(ReturnType.DBR_NATIVE, "GETPEAKTIME" + (i + 1), "");
		}
	}

	@Test
	public void testSetTriggerThreshold() throws Exception {
		final double triggerThreshold = 5.38;
		when(xmapDevice.getValue(eq(ReturnType.DBR_NATIVE), startsWith("GETTRIGTHRESH"), eq(""))).thenReturn(triggerThreshold);

		controller.configure();
		controller.setTriggerThreshold(triggerThreshold);
		for (int i = 0; i < NUM_ELEMENTS; i++) {
			verify(xmapDevice).setValue("SETTRIGTHRESH" + (i + 1), "", triggerThreshold);
			verify(xmapDevice).getValue(ReturnType.DBR_NATIVE, "GETTRIGTHRESH" + (i + 1), "");
		}
	}

	@Test
	public void testSetBaseThreshold() throws Exception {
		final double baseThreshold = 3.21;
		when(xmapDevice.getValue(eq(ReturnType.DBR_NATIVE), startsWith("GETBASETHRESH"), eq(""))).thenReturn(baseThreshold);

		controller.configure();
		controller.setBaseThreshold(baseThreshold);
		for (int i = 0; i < NUM_ELEMENTS; i++) {
			verify(xmapDevice).setValue("SETBASETHRESH" + (i + 1), "", baseThreshold);
			verify(xmapDevice).getValue(ReturnType.DBR_NATIVE, "GETBASETHRESH" + (i + 1), "");
		}
	}

	@Test
	public void testSetBaseLength() throws Exception {
		final int baseLength = 8;

		// EDXDElement.getBaseLength() actually uses SETBASELENGTH<n> to return the length
		// See code for an explanation
		when(xmapDevice.getValue(eq(ReturnType.DBR_NATIVE), startsWith("SETBASELENGTH"), eq(""))).thenReturn((short)baseLength);

		controller.configure();
		controller.setBaseLength(baseLength);
		for (int i = 0; i < NUM_ELEMENTS; i++) {
			verify(xmapDevice).setValue("SETBASELENGTH" + (i + 1), "", baseLength);
			verify(xmapDevice).getValue(ReturnType.DBR_NATIVE, "SETBASELENGTH" + (i + 1), "");
		}
	}

	@Test
	public void testSetEnergyThreshold() throws Exception {
		final double energyThreshold = 9.2;
		when(xmapDevice.getValue(eq(ReturnType.DBR_NATIVE), startsWith("GETENERGYTHRESH"), eq(""))).thenReturn(energyThreshold);

		controller.configure();
		controller.setEnergyThreshold(energyThreshold);
		for (int i = 0; i < NUM_ELEMENTS; i++) {
			verify(xmapDevice).setValue("SETENERGYTHRESH" + (i + 1), "", energyThreshold);
			verify(xmapDevice).getValue(ReturnType.DBR_NATIVE, "GETENERGYTHRESH" + (i + 1), "");
		}
	}

	@Test
	public void testSetResetDelay() throws Exception {
		final double resetDelay = 3.65;
		when(xmapDevice.getValue(eq(ReturnType.DBR_NATIVE), startsWith("GETRESETDELAY"), eq(""))).thenReturn(resetDelay);

		controller.configure();
		controller.setResetDelay(resetDelay);
		for (int i = 0; i < NUM_ELEMENTS; i++) {
			verify(xmapDevice).setValue("SETRESETDELAY" + (i + 1), "", resetDelay);
			verify(xmapDevice).getValue(ReturnType.DBR_NATIVE, "GETRESETDELAY" + (i + 1), "");
		}
	}

	@Test
	public void testSetGapTime() throws Exception {
		final double gapTime = 0.7;
		when(xmapDevice.getValue(eq(ReturnType.DBR_NATIVE), startsWith("GETGAPTIME"), eq(""))).thenReturn(gapTime);

		controller.configure();
		controller.setGapTime(gapTime);
		for (int i = 0; i < NUM_ELEMENTS; i++) {
			verify(xmapDevice).setValue("SETGAPTIME" + (i + 1), "", gapTime);
			verify(xmapDevice).getValue(ReturnType.DBR_NATIVE, "GETGAPTIME" + (i + 1), "");
		}
	}

	@Test
	public void testSetTriggerPeakTime() throws Exception {
		final double triggerPeakTime = 1.2;
		when(xmapDevice.getValue(eq(ReturnType.DBR_NATIVE), startsWith("GETTRIGPEAKTIME"), eq(""))).thenReturn(triggerPeakTime);

		controller.configure();
		controller.setTriggerPeakTime(triggerPeakTime);
		for (int i = 0; i < NUM_ELEMENTS; i++) {
			verify(xmapDevice).setValue("SETTRIGPEAKTIME" + (i + 1), "", triggerPeakTime);
			verify(xmapDevice).getValue(ReturnType.DBR_NATIVE, "GETTRIGPEAKTIME" + (i + 1), "");
		}
	}

	@Test
	public void testSetTriggerGapTime() throws Exception {
		final double triggerGapTime = 0.3;
		when(xmapDevice.getValue(eq(ReturnType.DBR_NATIVE), startsWith("GETTRIGGAPTIME"), eq(""))).thenReturn(triggerGapTime);

		controller.configure();
		controller.setTriggerGapTime(triggerGapTime);
		for (int i = 0; i < NUM_ELEMENTS; i++) {
			verify(xmapDevice).setValue("SETTRIGGAPTIME" + (i + 1), "", triggerGapTime);
			verify(xmapDevice).getValue(ReturnType.DBR_NATIVE, "GETTRIGGAPTIME" + (i + 1), "");
		}
	}

	@Test
	public void testSetMaxWidth() throws Exception {
		final double maxWidth = 0.04;
		when(xmapDevice.getValue(eq(ReturnType.DBR_NATIVE), startsWith("GETMAXWIDTH"), eq(""))).thenReturn(maxWidth);

		controller.configure();
		controller.setMaxWidth(maxWidth);
		for (int i = 0; i < NUM_ELEMENTS; i++) {
			verify(xmapDevice).setValue("SETMAXWIDTH" + (i + 1), "", maxWidth);
			verify(xmapDevice).getValue(ReturnType.DBR_NATIVE, "GETMAXWIDTH" + (i + 1), "");
		}
	}

	@Test
	public void testGetSubDetector() throws Exception {
		controller.configure();

		// There should be 24 sub-detectors by default
		for (int i = 0; i < NUM_ELEMENTS; i++) {
			final EDXDElement subDetector = controller.getSubDetector(i);
			assertNotNull(subDetector);
			assertEquals(String.format("EDXD_Element_%02d", i + 1), subDetector.getName());
		}

		// Note that, if there are some detectors, attempting to access a detector out of range
		// will throw an exception...(see next test)
		try {
			controller.getSubDetector(NUM_ELEMENTS);
			fail("Attempt to access non-existent sub-detector should fail");
		} catch (IndexOutOfBoundsException e) {
			// Expected to throw an exception
		}
	}

	@Test
	public void testGetSubDetectorNotConfigured() {
		// ...whereas, if there are no detectors, the function returns null
		for (int i = 0; i < 25; i++) {
			assertNull(controller.getSubDetector(i));
		}
	}

	@Test
	public void testSetResumeFalse() throws Exception {
		controller.configure();
		controller.setResume(false);
		verify(xmapDevice).setValue("SETRESUME", "", 0);
	}

	@Test
	public void testSetResumeTrue() throws Exception {
		controller.configure();
		controller.setResume(true);
		verify(xmapDevice).setValue("SETRESUME", "", 1);
	}

	@Test
	public void testGetData() throws Exception {
		final double[] data = new double[] { 1.2, 3.4 };
		when(xmapDevice.getValue(eq(ReturnType.DBR_NATIVE), startsWith("DATA"), eq(""))).thenReturn(data);

		controller.configure();
		final double[] dataReturned = controller.getData(3);
		verify(xmapDevice).getValue(ReturnType.DBR_NATIVE, "DATA4", "");
		assertEquals(data.length, dataReturned.length);
		for (int i = 0; i < data.length; i++) {
			assertEquals(data[i], dataReturned[i], 0.001);
		}
	}

	@Test
	public void testSetAquisitionTime() throws Exception {
		final double collectionTime = 0.35;
		controller.configure();
		controller.setAquisitionTime(collectionTime);
		verify(xmapDevice).setValue("SETPRESETVALUE", "", collectionTime);
	}

	@Test
	public void testSetPresetType() throws Exception {
		controller.configure();
		controller.setPresetType(PRESET_TYPES.LIVE_TIME);
		verify(xmapDevice).setValueNoWait("SETPRESETTYPE", "", 2); // 2 is ordinal value of PRESET_TYPES.LIVE_TIME
	}

	@Test
	public void testStart() throws Exception {
		controller.configure();
		controller.start();
		verify(xmapDevice).setValueNoWait("ACQUIRE", "", 1);
	}

	@Test
	public void testStop() throws Exception {
		controller.configure();
		controller.stop();
		verify(xmapDevice).setValueNoWait("ACQUIRE", "", 0);
	}

	@Test
	public void testActivateROI() throws Exception {
		controller.configure();
		controller.activateROI();
		verify(xmapDevice).setValue("SCAACTIVATE", "", 1);
	}

	@Test
	public void testDeactivateROI() throws Exception {
		controller.configure();
		controller.deactivateROI();
		verify(xmapDevice).setValue("SCAACTIVATE", "", 0);
	}

	@Test
	public void testGetMaxAllowedROIs() throws Exception {
		final int maxAllowedRois = 32;
		when(xmapDevice.getValue(ReturnType.DBR_NATIVE, "SCAELEMENTS", "")).thenReturn((double) maxAllowedRois);

		controller.configure();
		final int valueReturned = controller.getMaxAllowedROIs();
		verify(xmapDevice).getValue(ReturnType.DBR_NATIVE, "SCAELEMENTS", "");
		assertEquals(maxAllowedRois, valueReturned);
	}

	@Test
	public void testReadout() throws Exception {
		final int valuesPerElement = 3;
		final double[][] data = new double[NUM_ELEMENTS][valuesPerElement];
		for (int i = 0; i < NUM_ELEMENTS; i++) {
			for (int j = 0; j < valuesPerElement; j++) {
				data[i][j] = Math.random() * 100.0;
			}
		}

		final String dataRecordName = "DATA";
		when(xmapDevice.getValue(eq(ReturnType.DBR_NATIVE), startsWith(dataRecordName), eq(""))).thenAnswer(invocation -> {
			final String record = invocation.getArgumentAt(1, String.class);
			final int elementIndex = Integer.parseInt(record.substring(dataRecordName.length())) - 1;
			return data[elementIndex];
		});

		controller.configure();
		final double[][] dataRead = (double[][]) controller.readout();
		assertEquals(NUM_ELEMENTS, dataRead.length);

		for (int i = 0; i < NUM_ELEMENTS; i++) {
			verify(xmapDevice).getValue(ReturnType.DBR_NATIVE, dataRecordName + (i + 1), "");
			assertEquals(valuesPerElement, dataRead[i].length);
			for (int j = 0; j < valuesPerElement; j++) {
				assertEquals(data[i][j], dataRead[i][j], 0.001);
			}
		}
	}

	@Test
	public void testGetEvents() throws Exception {
		final String eventsRecordName = "EVENTS";
		when(xmapDevice.getValue(eq(ReturnType.DBR_NATIVE), startsWith(eventsRecordName), eq(""))).thenAnswer(invocation -> {
			final String record = invocation.getArgumentAt(1, String.class);
			return 100 * Integer.parseInt(record.substring(eventsRecordName.length()));
		});

		controller.configure();
		for (int i = 0; i < NUM_ELEMENTS; i++) {
			final int events = controller.getEvents(i);
			verify(xmapDevice).getValue(ReturnType.DBR_NATIVE, eventsRecordName + (i + 1), "");
			assertEquals(100 * (i + 1), events);
		}
	}

	@Test
	public void testGetICR() throws Exception {
		final String inputCountRateRecordName = "INPUTCOUNTRATE";
		when(xmapDevice.getValue(eq(ReturnType.DBR_NATIVE), startsWith(inputCountRateRecordName), eq(""))).thenAnswer(invocation -> {
			final String record = invocation.getArgumentAt(1, String.class);
			return 2.5 * Double.parseDouble(record.substring(inputCountRateRecordName.length()));
		});

		controller.configure();
		for (int i = 0; i < NUM_ELEMENTS; i++) {
			final double inputCountRate = controller.getICR(i);
			verify(xmapDevice).getValue(ReturnType.DBR_NATIVE, inputCountRateRecordName + (i + 1), "");
			assertEquals(2.5 * (i + 1.0), inputCountRate, 0.0001);
		}
	}

	@Test
	public void testGetOCR() throws Exception {
		final String outputCountRateRecordName = "OUTPUTCOUNTRATE";
		when(xmapDevice.getValue(eq(ReturnType.DBR_NATIVE), startsWith(outputCountRateRecordName), eq(""))).thenAnswer(invocation -> {
			final String record = invocation.getArgumentAt(1, String.class);
			return 4.7 * Double.parseDouble(record.substring(outputCountRateRecordName.length()));
		});

		controller.configure();
		for (int i = 0; i < NUM_ELEMENTS; i++) {
			final double inputCountRate = controller.getOCR(i);
			verify(xmapDevice).getValue(ReturnType.DBR_NATIVE, outputCountRateRecordName + (i + 1), "");
			assertEquals(4.7 * (i + 1.0), inputCountRate, 0.0001);
		}
	}
}
