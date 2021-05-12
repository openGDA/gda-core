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

package org.eclipse.scanning.test.device;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXnote;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.ServiceHolder;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.device.INexusDeviceService;
import org.eclipse.dawnsci.nexus.device.impl.NexusDeviceService;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.device.GroupMetadataNode;
import org.eclipse.scanning.device.NexusMetadataAppender;
import org.eclipse.scanning.device.ScalarField;
import org.eclipse.scanning.device.Services;
import org.eclipse.scanning.example.detector.MandelbrotDetector;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.example.scannable.MockScannableConnector;
import org.eclipse.scanning.test.util.TestDetectorHelpers;
import org.junit.Before;
import org.junit.Test;

public class NexusMetadataAppenderTest {

	private static final long DETECTOR_NUMBER = 4;
	private static final String DETECTOR_DESCRIPTION = "Mandelbrot Detector";
	private static final double DETECTOR_DISTANCE = 1.39;
	private static final String SCANNABLE_NAME_DETECTOR_DISTANCE = "detDistance";
	private static final String GROUP_NAME_CALIBRATION_METHOD = "calibration_method";
	private static final String CALIBRATION_DESCRIPTION = "This is the calibration description";

	private INexusDeviceService nexusDeviceService;

	private IScannableDeviceService scannableDeviceService;

	private MandelbrotDetector detector;

	@Before
	public void setUp() throws Exception {
		nexusDeviceService = new NexusDeviceService();
		new ServiceHolder().setNexusDeviceService(nexusDeviceService);

		scannableDeviceService = new MockScannableConnector(null);
		new Services().setScannableDeviceService(scannableDeviceService);

		final MandelbrotModel detModel = new MandelbrotModel();
		detModel.setName("mandelbrot");
		detModel.setRealAxisName("xNex");
		detModel.setImaginaryAxisName("yNex");
		detModel.setColumns(64);
		detModel.setRows(64);

		detector = (MandelbrotDetector) TestDetectorHelpers.createAndConfigureMandelbrotDetector(detModel);
	}

	@Test
	public void testAppendMetadata() throws Exception {
		// Arrange
		// A scannable for the detector distance, to use with a ScannableField
		@SuppressWarnings("unchecked")
		final IScannable<Double> detectorDistanceScannable = mock(IScannable.class);
		when(detectorDistanceScannable.getName()).thenReturn(SCANNABLE_NAME_DETECTOR_DISTANCE);
		when(detectorDistanceScannable.getPosition()).thenReturn(DETECTOR_DISTANCE);
		scannableDeviceService.register(detectorDistanceScannable);

		// create a metadata appender with the same name as the detector and register it with the nexus device service
		final NexusMetadataAppender<NXdetector> appender = new NexusMetadataAppender<>();
		appender.setName(detector.getName());
		appender.addScalarField(NXdetector.NX_DETECTOR_NUMBER, DETECTOR_NUMBER);
		appender.addScalarField(NXdetector.NX_DESCRIPTION, DETECTOR_DESCRIPTION);
		appender.addScannableField(NXdetector.NX_DISTANCE, SCANNABLE_NAME_DETECTOR_DISTANCE);

		final GroupMetadataNode<NXnote> calibrationNoteNode = new GroupMetadataNode<>(
				GROUP_NAME_CALIBRATION_METHOD, NexusBaseClass.NX_NOTE);
		calibrationNoteNode.addChildNode(new ScalarField(NXnote.NX_DESCRIPTION, CALIBRATION_DESCRIPTION));
		appender.addField(calibrationNoteNode);

		appender.register();

		// Act
		final INexusDevice<NXdetector> decoratedNexusDevice = nexusDeviceService.decorateNexusDevice(detector);

		final NexusScanInfo scanInfo = new NexusScanInfo();
		scanInfo.setRank(2);
		final NexusObjectProvider<NXdetector> nexusObjectProvider = decoratedNexusDevice.getNexusProvider(scanInfo);
		final NXdetector nxDetector = nexusObjectProvider.getNexusObject();

		// Assert
		assertThat(nxDetector, is(notNullValue()));
		assertThat(nxDetector.getDetector_numberScalar(), is(DETECTOR_NUMBER));
		assertThat(nxDetector.getDescriptionScalar(), is(equalTo(DETECTOR_DESCRIPTION)));
		assertThat(nxDetector.getDistanceScalar(), is(DETECTOR_DISTANCE));

		final NXnote calibrationMethodNote = nxDetector.getCalibration_method();
		assertThat(calibrationMethodNote, is(notNullValue()));
		assertThat(calibrationMethodNote.getDescriptionScalar(), is(equalTo(CALIBRATION_DESCRIPTION)));
	}

}
