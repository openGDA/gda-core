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
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.NDFile;
import gda.device.detector.areadetector.v17.NDFileHDF5;
import gda.device.detector.areadetector.v17.NDPluginBase;
import gda.device.detector.xmap.edxd.EDXDController.COLLECTION_MODES;
import gda.device.detector.xmap.edxd.EDXDController.NEXUS_FILE_MODE;
import gda.device.detector.xmap.edxd.EDXDController.PIXEL_ADVANCE_MODE;
import gda.device.epicsdevice.FindableEpicsDevice;
import gda.device.epicsdevice.IEpicsChannel;
import gda.device.epicsdevice.ReturnType;
import gda.factory.FactoryException;
import gda.observable.IObserver;

public class EDXDMappingControllerTest {
	private static final int NUM_ELEMENTS = 24;
	private static final int ELEMENT_OFFSET = 2;

	private FindableEpicsDevice xmapDevice;
	private IEpicsChannel statusChannel;
	private EDXDMappingController controller;
	private NDFileHDF5 ndFileHDF5;
	private NDFile ndFile;
	private NDPluginBase ndPluginBase;

	@Before
	public void setUp() {
		statusChannel = mock(IEpicsChannel.class);

		final Map<String, String> pvs = new HashMap<>();
		for (int i = 1; i < NUM_ELEMENTS + ELEMENT_OFFSET + 1; i++) {
			pvs.put("MCA" + i, "BLXXI-EA-DET-01:MCA" + i);
		}

		xmapDevice = mock(FindableEpicsDevice.class);
		when(xmapDevice.getRecordPVs()).thenReturn(pvs);
		when(xmapDevice.createEpicsChannel(ReturnType.DBR_NATIVE, "ACQUIRING", "")).thenReturn(statusChannel);
		when(xmapDevice.isConfigured()).thenReturn(true);

		ndPluginBase = mock(NDPluginBase.class);

		ndFile = mock(NDFile.class);
		when(ndFile.getPluginBase()).thenReturn(ndPluginBase);

		ndFileHDF5 = mock(NDFileHDF5.class);
		when(ndFileHDF5.getFile()).thenReturn(ndFile);

		controller = new EDXDMappingController();
		controller.setXmap(xmapDevice);
		controller.setElementOffset(ELEMENT_OFFSET);
		controller.setHdf5(ndFileHDF5);
	}

	@Test
	public void testConfigure() throws Exception {
		controller.configure();
		verify(xmapDevice).createEpicsChannel(ReturnType.DBR_NATIVE, "ACQUIRING", "");
		verify(statusChannel).addIObserver(any(IObserver.class));

		// Check that the sub-detectors are of the correct type
		for (int i = 0; i < NUM_ELEMENTS; i++) {
			final IEDXDElement subDetector = controller.getSubDetector(i);
			assertTrue(subDetector instanceof EDXDMappingElement);
		}
	}

	@Test(expected = FactoryException.class)
	public void testConfigureWithNoXmapDeviceFails() throws Exception {
		final EDXDMappingController controllerNoXmap = new EDXDMappingController();
		controllerNoXmap.setElementOffset(ELEMENT_OFFSET);
		controllerNoXmap.setHdf5(ndFileHDF5);
		controllerNoXmap.configure();
	}

	@Test
	public void testSetDynamicRange() throws Exception {
		final double dynamicRange = 3.6;
		final String dynamicRangeRecord = "GETDYNRANGE" + ELEMENT_OFFSET;
		when(xmapDevice.getValue(ReturnType.DBR_NATIVE, dynamicRangeRecord, "")).thenReturn(dynamicRange);

		final double rangeReturned = controller.setDynamicRange(dynamicRange);
		assertEquals(dynamicRange, rangeReturned, 0.001);
		verify(xmapDevice).setValue("SETDYNRANGE", "", dynamicRange);
		verify(xmapDevice).getValue(ReturnType.DBR_NATIVE, dynamicRangeRecord, "");
	}

	@Test
	public void testGetMaxAllowedROIs() throws Exception {
		assertEquals(32, controller.getMaxAllowedROIs());
	}

	@Test
	public void testActivateROI() throws Exception {
		controller.activateROI();
		verifyZeroInteractions(xmapDevice);
	}

	@Test
	public void testDeactivateROI() throws Exception {
		controller.deactivateROI();
		verifyZeroInteractions(xmapDevice);
	}

	@Test
	public void testStart() throws Exception {
		controller.start();
		verify(xmapDevice).setValueNoWait("ERASESTART", "", 1);
	}

	@Test
	public void testStop() throws Exception {
		controller.stop();
		verify(xmapDevice).setValueNoWait("STOPALL", "", 1);
	}

	@Test
	public void testStopXmapNull() throws Exception {
		controller.setXmap(null);
		controller.stop();
		verifyZeroInteractions(xmapDevice);
	}

	@Test
	public void testStopXmapNotConfigured() throws Exception {
		when(xmapDevice.isConfigured()).thenReturn(false);
		controller.stop();
		verify(xmapDevice).isConfigured();
		verifyNoMoreInteractions(xmapDevice);
	}

	@Test
	public void testSetResumeTrue() throws Exception {
		controller.setResume(true);
		verifyZeroInteractions(xmapDevice);
	}

	@Test
	public void testSetResumeFalse() throws Exception {
		controller.setResume(false);
		verify(xmapDevice).setValueNoWait("ERASEALL", "", 1);
	}

	@Test
	public void testClear() throws Exception {
		controller.clear();
		verify(xmapDevice).setValueNoWait("ERASEALL", "", 1);
	}

	@Test
	public void testClearAndStart() throws Exception {
		controller.clearAndStart();
		verify(xmapDevice).setValueNoWait("ERASESTART", "", 1);
	}

	@Test
	public void testSetCollectionMode() throws Exception {
		controller.setCollectionMode(COLLECTION_MODES.LIST_MAPPING);
		verify(xmapDevice).setValueNoWait("COLLECTMODE", "", 3); // ordinal of LIST_MAPPING in enum
	}

	@Test
	public void testSetAcquisitionTimeOn() throws Exception {
		final double acquisitionTime = 0.03;
		controller.setAquisitionTimeOn(true);
		controller.setAquisitionTime(acquisitionTime);
		verify(xmapDevice).setValue("SETPRESETVALUE", "", acquisitionTime);
	}

	@Test
	public void testSetAcquisitionTimeOff() throws Exception {
		final double acquisitionTime = 0.03;
		controller.setAquisitionTimeOn(false);
		controller.setAquisitionTime(acquisitionTime);
		verifyZeroInteractions(xmapDevice);
	}

	@Test
	public void testSetPixelAdvanceMode() throws Exception {
		controller.setPixelAdvanceMode(PIXEL_ADVANCE_MODE.SYNC);
		verify(xmapDevice).setValueNoWait("PIXELADVANCEMODE", "", 1);
	}

	@Test
	public void testSetIgnoreGateTrue() throws Exception {
		controller.setIgnoreGate(true);
		verify(xmapDevice).setValueNoWait("IGNOREGATE", "", 1);
	}

	@Test
	public void testSetIgnoreGateFalse() throws Exception {
		controller.setIgnoreGate(false);
		verify(xmapDevice).setValueNoWait("IGNOREGATE", "", 0);
	}

	@Test
	public void testSetAutoPixelsPerBufferTrue() throws Exception {
		controller.setAutoPixelsPerBuffer(true);
		verify(xmapDevice).setValueNoWait("AUTOPIXELSPERBUFFER", "", 1);
	}

	@Test
	public void testSetAutoPixelsPerBufferFalse() throws Exception {
		controller.setAutoPixelsPerBuffer(false);
		verify(xmapDevice).setValueNoWait("AUTOPIXELSPERBUFFER", "", 0);
	}

	@Test
	public void testSetPixelsPerBuffer() throws Exception {
		final int pixelsPerBuffer = 1024;
		controller.setPixelsPerBuffer(pixelsPerBuffer);
		verify(xmapDevice).setValueNoWait("PIXELSPERBUFFER", "", pixelsPerBuffer);
	}

	@Test
	public void testSetPixelsPerRun() throws Exception {
		final int pixelsPerRun = 4096;
		controller.setPixelsPerRun(pixelsPerRun);
		verify(xmapDevice).setValue("PIXELSPERRUN", "", pixelsPerRun);
	}

	@Test
	public void testGetPixelsPerRun() throws Exception {
		final int pixelsPerRun = 4096;
		when(xmapDevice.getValue(ReturnType.DBR_NATIVE, "PIXELSPERRUN", "")).thenReturn(pixelsPerRun);

		final int valueReturned = controller.getPixelsPerRun();
		verify(xmapDevice).getValue(ReturnType.DBR_NATIVE, "PIXELSPERRUN", "");
		assertEquals(pixelsPerRun, valueReturned);
	}

	@Test
	public void testResetCounters() throws Exception {
		controller.resetCounters();
		verify(ndPluginBase).setDroppedArrays(0);
		verify(ndPluginBase).setArrayCounter(0);
	}

	@Test
	public void testStartRecording() throws Exception {
		when(ndFileHDF5.getCapture()).thenReturn((short) 0, (short) 0, (short) 0, (short) 1);
		controller.startRecording();
		verify(ndFileHDF5).startCapture();
		verify(ndFileHDF5, times(4)).getCapture();
	}

	@Test(expected = DeviceException.class)
	public void testStartRecordingAlreadyCapturing() throws Exception {
		when(ndFileHDF5.getCapture()).thenReturn((short) 1);
		controller.startRecording();
	}

	// It would be good to test the timeout on startRecording() but it is hard-coded to 60 secs
	// Maybe consider making it configurable.

	@Test
	public void testEndRecording() throws Exception {
		when(ndFile.getCapture_RBV()).thenReturn((short) 1, (short) 1, (short) 0);
		controller.endRecording();
		verify(ndFile, times(3)).getCapture_RBV();
	}

	@Test
	public void testEndRecordingForceStop() throws Exception {
		when(ndFile.getCapture_RBV()).thenReturn((short) 1);
		controller.endRecording();
		verify(ndFile, times(40)).getCapture_RBV();
		verify(ndFileHDF5).stopCapture();
		verify(ndPluginBase).getDroppedArrays_RBV();
	}

	@Test(expected = DeviceException.class)
	public void testEndRecordingDroppedFrames() throws Exception {
		when(ndFile.getCapture_RBV()).thenReturn((short) 1);
		when(ndPluginBase.getDroppedArrays_RBV()).thenReturn(22);
		controller.endRecording();
	}

	@Test
	public void testGetHDFFileName() throws Exception {
		final String fileName = "output.hdf";
		when(ndFileHDF5.getFullFileName_RBV()).thenReturn(fileName);

		final String fileNameReturned = controller.getHDFFileName();
		verify(ndFileHDF5).getFullFileName_RBV();
		assertEquals(fileName, fileNameReturned);
	}

	@Test
	public void testSetDirectory() throws Exception {
		final String dataDirectory = "/scratch/data";
		when(ndFile.filePathExists()).thenReturn(true);
		controller.setDirectory(dataDirectory);
		verify(ndFileHDF5).setFilePath(dataDirectory);
		verify(ndFile).filePathExists();
	}

	@Test(expected = Exception.class)
	public void testSetDirectoryDoesNotExist() throws Exception {
		final String dataDirectory = "/scratch/data";
		when(ndFile.filePathExists()).thenReturn(false);
		controller.setDirectory(dataDirectory);
	}

	@Test
	public void testSetFileNumber() throws Exception {
		final int fileNum = 567;
		controller.setFileNumber(fileNum);
		verify(ndFileHDF5).setFileNumber(fileNum);
	}

	@Test
	public void testSetFilenamePrefix() throws Exception {
		final String beamline = "iXX";
		controller.setFilenamePrefix(beamline);
		verify(ndFileHDF5).setFileName(beamline);
	}

	@Test
	public void testSetFilenamePostfix() throws Exception {
		controller.setFilenamePostfix("panda");
		verify(ndFileHDF5).setFileTemplate("%s%s-%d-panda.h5");
	}

	@Test
	public void testSetNexusCapture() throws Exception {
		final int number = 42;
		controller.setNexusCapture(number);
		verify(xmapDevice).setValueNoWait("NEXUS:Capture", "", number);
	}

	@Test
	public void testSetHdfNumCapture() throws Exception {
		final int number = 43;
		controller.setHdfNumCapture(number);
		verify(ndFileHDF5).setNumCapture(number);
	}

	@Test(expected = DeviceException.class)
	public void testSetHdfNumCaptureFails() throws Exception {
		final int number = 43;
		doThrow(new Exception("Failure in setNumCapture()")).when(ndFileHDF5).setNumCapture(anyInt());
		controller.setHdfNumCapture(number);
	}

	@Test
	public void testSetNexusFileFormat() throws Exception {
		final String format = "HDF5";
		controller.setNexusFileFormat(format);
		verify(xmapDevice).setValueNoWait("NEXUS:FileTemplate", "", format);
	}

	@Test
	public void testSetFileWriteMode() throws Exception {
		controller.setFileWriteMode(NEXUS_FILE_MODE.STREAM);
		verify(xmapDevice).setValueNoWait("NEXUS:FileWriteMode", "", 2);
	}

	@Test
	public void testSetCallbackTrue() throws Exception {
		controller.setCallback(true);
		verify(xmapDevice).setValueNoWait("NEXUS:EnableCallbacks", "", 1);
	}

	@Test
	public void testSetCallbackFalse() throws Exception {
		controller.setCallback(false);
		verify(xmapDevice).setValueNoWait("NEXUS:EnableCallbacks", "", 0);
	}

	@Test
	public void testSetNexusFileName() throws Exception {
		final String filename = "scan-1234.nxs";
		controller.setNexusFileName(filename);
		verify(xmapDevice).setValueNoWait("NEXUS:FileName", "", filename);
	}

	@Test
	public void testGetNexusFileName() throws Exception {
		final String filename = "scan-6789.nxs";
		when(xmapDevice.getValueAsString("NEXUS:FileName", "")).thenReturn(filename);

		final String filenameReturned = controller.getNexusFileName();
		verify(xmapDevice).getValueAsString("NEXUS:FileName", "");
		assertEquals(filename, filenameReturned);
	}

	@Test
	public void testSetNexusFilePath() throws Exception {
		final String filePath = "/scratch/data/nexus";
		controller.setNexusFilePath(filePath);
		verify(xmapDevice).setValueNoWait("NEXUS:FilePath", "", filePath);
	}

	@Test
	public void testGetNexusFilePath() throws Exception {
		final String filePath = "/scratch/data/nexus2";
		when(xmapDevice.getValueAsString("NEXUS:FilePath", "")).thenReturn(filePath);

		final String filePathReturned = controller.getNexusFilePath();
		verify(xmapDevice).getValueAsString("NEXUS:FilePath", "");
		assertEquals(filePath, filePathReturned);
	}

	@Test
	public void testGetFileNumber() throws Exception {
		final int fileNumber = 4657;
		when(xmapDevice.getValue(ReturnType.DBR_NATIVE, "NEXUS:FileNumber", "")).thenReturn(fileNumber);
		final int fileNumberReturned = controller.getFileNumber();
		verify(xmapDevice).getValue(ReturnType.DBR_NATIVE, "NEXUS:FileNumber", "");
		assertEquals(fileNumber, fileNumberReturned);
	}

	@Test
	public void testSetTemplateFileName() throws Exception {
		final String templateFileName = "EdeScan_Parameters.xml";
		controller.setTemplateFileName(templateFileName);
		verify(xmapDevice).setValueNoWait("TemplateFileName", "", templateFileName);
	}

	@Test
	public void testSetTemplateFilePath() throws Exception {
		final String templateFilePath = "/scratch/iXX-config/templates/";
		controller.setTemplateFilePath(templateFilePath);
		verify(xmapDevice).setValueNoWait("TemplateFilePath", "", templateFilePath);
	}

	@Test
	public void testGetCaptureStatusTrue() {
		when(ndFileHDF5.getStatus()).thenReturn(1);
		assertTrue(controller.getCaptureStatus());
	}

	@Test
	public void testGetCaptureStatusFalse() {
		when(ndFileHDF5.getStatus()).thenReturn(0);
		assertFalse(controller.getCaptureStatus());
	}

	@Test
	public void testIsBufferedArrayPortTrue() throws Exception {
		when(ndFileHDF5.getArrayPort()).thenReturn("xbuf");
		assertTrue(controller.isBufferedArrayPort());
	}

	@Test
	public void testIsBufferedArrayPortFalse() throws Exception {
		when(ndFileHDF5.getArrayPort()).thenReturn("something else");
		assertFalse(controller.isBufferedArrayPort());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testReconfigureNotSupported() throws Exception {
		controller.reconfigure();
	}
}
