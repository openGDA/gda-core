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

import static org.eclipse.dawnsci.nexus.NXdetector.NX_ANGULAR_CALIBRATION;
import static org.eclipse.scanning.device.AbstractMetadataField.ATTRIBUTE_NAME_LOCAL_NAME;
import static org.eclipse.scanning.device.AbstractMetadataField.ATTRIBUTE_NAME_UNITS;
import static org.eclipse.scanning.example.detector.MandelbrotDetector.FIELD_NAME_IMAGINARY_AXIS;
import static org.eclipse.scanning.example.detector.MandelbrotDetector.FIELD_NAME_REAL_AXIS;
import static org.eclipse.scanning.example.detector.MandelbrotDetector.FIELD_NAME_SPECTRUM;
import static org.eclipse.scanning.example.detector.MandelbrotDetector.FIELD_NAME_SPECTRUM_AXIS;
import static org.eclipse.scanning.example.detector.MandelbrotDetector.FIELD_NAME_VALUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.eclipse.dawnsci.analysis.api.tree.SymbolicNode;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXnote;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.device.INexusDeviceService;
import org.eclipse.dawnsci.nexus.device.impl.NexusDeviceService;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.device.GroupMetadataNode;
import org.eclipse.scanning.device.NexusMetadataAppender;
import org.eclipse.scanning.device.ScalarField;
import org.eclipse.scanning.device.ScalarMetadataAttribute;
import org.eclipse.scanning.device.ScannableField;
import org.eclipse.scanning.device.ScannableMetadataAttribute;
import org.eclipse.scanning.example.detector.MandelbrotDetector;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.sequencer.RunnableDeviceServiceImpl;
import org.eclipse.scanning.test.util.TestDetectorHelpers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import gda.TestHelpers;
import gda.device.Scannable;
import gda.device.ScannableMotionUnits;
import gda.factory.Factory;
import gda.factory.Finder;
import uk.ac.diamond.daq.scanning.ScannableDeviceConnectorService;
import uk.ac.diamond.osgi.services.ServiceProvider;

class NexusMetadataAppenderTest {

	private static final String[] NO_FIELDS = new String[0];

	private static final long DETECTOR_NUMBER = 4;
	private static final String DETECTOR_DESCRIPTION = "Mandelbrot Detector";
	private static final double DETECTOR_DISTANCE = 1.39;
	private static final double DETECTOR_DIAMETER = 234.56;
	private static final String SCANNABLE_NAME_DETECTOR_DISTANCE = "detDistance";
	private static final String SCANNABLE_NAME_TIME_OF_FLIGHT = "tof";
	private static final String SCANNABLE_NAME_RAW_TIME_OF_FLIGHT = "rawTof";
	private static final String SCANNABLE_NAME_RAW_TIME_OF_FLIGHT_FREQUENCY = "rawTofFreq";
	private static final String GROUP_NAME_CALIBRATION_METHOD = "calibration_method";
	private static final String CALIBRATION_DESCRIPTION = "This is the calibration description";
	private static final String INTERNAL_LINK_PATH = "/entry/calibration/angular";
	private static final String EXTERNAL_FILE_PATH = "detFlatField.nxs";
	private static final String EXTERNAL_LINK_PATH = "/entry/det/flatfield";
	private static final String UNITS_MILLIS = "mm";
	private static final double TIME_OF_FLIGHT = 123.45;
	private static final long RAW_TIME_OF_FLIGHT = 125L;
	private static final long RAW_TIME_OF_FLIGHT_FREQUENCY = 60L;

	private MandelbrotDetector detector;

	@BeforeAll
	public static void setUpServices() {
		ServiceProvider.setService(INexusDeviceService.class, new NexusDeviceService());
		ServiceProvider.setService(IScannableDeviceService.class, new ScannableDeviceConnectorService());
		ServiceProvider.setService(IRunnableDeviceService.class, new RunnableDeviceServiceImpl());
	}

	@AfterAll
	public static void tearDownServices() {
		ServiceProvider.reset();
	}

	@BeforeEach
	public void setUp() throws Exception {
		final MandelbrotModel detModel = new MandelbrotModel();
		detModel.setName("mandelbrot");
		detModel.setRealAxisName("xNex");
		detModel.setImaginaryAxisName("yNex");
		detModel.setColumns(64);
		detModel.setRows(64);

		detector = (MandelbrotDetector) TestDetectorHelpers.createAndConfigureMandelbrotDetector(detModel);

		// A scannable for the detector distance, to use with a ScannableField

		final Factory factory = TestHelpers.createTestFactory();
		factory.addFindable(createMockScannable(SCANNABLE_NAME_DETECTOR_DISTANCE, DETECTOR_DISTANCE, "mm"));
		factory.addFindable(createMockScannable(SCANNABLE_NAME_TIME_OF_FLIGHT, TIME_OF_FLIGHT, "ms"));
		factory.addFindable(createMockScannable(SCANNABLE_NAME_RAW_TIME_OF_FLIGHT, RAW_TIME_OF_FLIGHT, "ms"));
		factory.addFindable(createMockScannable(SCANNABLE_NAME_RAW_TIME_OF_FLIGHT_FREQUENCY, RAW_TIME_OF_FLIGHT_FREQUENCY, "Hz"));
		Finder.addFactory(factory);
	}

	@AfterEach
	public void tearDown() {
		Finder.removeAllFactories();
	}

	private Scannable createMockScannable(String name, Object position, String units) throws Exception {
		final ScannableMotionUnits mockScannable = Mockito.mock(ScannableMotionUnits.class);
		when(mockScannable.getName()).thenReturn(name);
		when(mockScannable.getPosition()).thenReturn(position);
		when(mockScannable.getUserUnits()).thenReturn(units);
		when(mockScannable.getInputNames()).thenReturn(new String[] { name });
		when(mockScannable.getExtraNames()).thenReturn(NO_FIELDS);
		return mockScannable;
	}

	@Test
	void testAppendMetadata() throws Exception {
		// Arrange
		// create a metadata appender with the same name as the detector and register it with the nexus device service
		final NexusMetadataAppender<NXdetector> appender = new NexusMetadataAppender<>();
		appender.setName(detector.getName());
		appender.addScalarField(NXdetector.NX_DETECTOR_NUMBER, DETECTOR_NUMBER);
		appender.addScalarField(NXdetector.NX_DESCRIPTION, DETECTOR_DESCRIPTION);
		appender.addScalarField(NXdetector.NX_DIAMETER, DETECTOR_DIAMETER, UNITS_MILLIS);
		appender.addScannableField(NXdetector.NX_DISTANCE, SCANNABLE_NAME_DETECTOR_DISTANCE);
		appender.addLinkedField(NXdetector.NX_ANGULAR_CALIBRATION, INTERNAL_LINK_PATH);
		appender.addExternalLinkedField(NXdetector.NX_FLATFIELD, EXTERNAL_FILE_PATH, EXTERNAL_LINK_PATH);

		final ScannableField timeOfFlightField = new ScannableField(NXdetector.NX_TIME_OF_FLIGHT, SCANNABLE_NAME_TIME_OF_FLIGHT);
		timeOfFlightField.addAttribute(new ScalarMetadataAttribute(NXdetector.NX_TIME_OF_FLIGHT_ATTRIBUTE_AXIS, 3));
		timeOfFlightField.addAttribute(new ScalarMetadataAttribute(NXdetector.NX_TIME_OF_FLIGHT_ATTRIBUTE_PRIMARY, 1));
		timeOfFlightField.addAttribute(new ScalarMetadataAttribute(NXdetector.NX_TIME_OF_FLIGHT_ATTRIBUTE_LONG_NAME, "time_of_flight"));
		appender.addField(timeOfFlightField);

		final ScannableField rawTimeOfFlightField = new ScannableField(NXdetector.NX_RAW_TIME_OF_FLIGHT, SCANNABLE_NAME_RAW_TIME_OF_FLIGHT);
		rawTimeOfFlightField.addAttribute(new ScannableMetadataAttribute(NXdetector.NX_RAW_TIME_OF_FLIGHT_ATTRIBUTE_FREQUENCY, SCANNABLE_NAME_RAW_TIME_OF_FLIGHT_FREQUENCY));
		appender.addField(rawTimeOfFlightField);

		final GroupMetadataNode<NXnote> calibrationNoteNode = new GroupMetadataNode<>(
				GROUP_NAME_CALIBRATION_METHOD, NexusBaseClass.NX_NOTE);
		calibrationNoteNode.addChildNode(new ScalarField(NXnote.NX_DESCRIPTION, CALIBRATION_DESCRIPTION));
		appender.addField(calibrationNoteNode);

		appender.register();

		// Act
		final INexusDevice<NXdetector> decoratedNexusDevice = ServiceProvider.getService(INexusDeviceService.class)
				.decorateNexusDevice(detector);

		final NexusScanInfo scanInfo = new NexusScanInfo();
		scanInfo.setShape(2, 2);
		final NexusObjectProvider<NXdetector> nexusObjectProvider = decoratedNexusDevice.getNexusProvider(scanInfo);
		final NXdetector nxDetector = nexusObjectProvider.getNexusObject();

		// Assert
		assertThat(nxDetector, is(notNullValue()));
		assertThat(nxDetector.getDataNodeNames(), containsInAnyOrder(NXdetector.NX_COUNT_TIME, NXdetector.NX_DATA,
				FIELD_NAME_SPECTRUM, FIELD_NAME_VALUE, "exposure_time", "escape_radius", "max_iterations",
				FIELD_NAME_REAL_AXIS, FIELD_NAME_IMAGINARY_AXIS, FIELD_NAME_SPECTRUM_AXIS, "name",
				NXdetector.NX_DESCRIPTION, NXdetector.NX_DETECTOR_NUMBER, NXdetector.NX_DISTANCE, NXdetector.NX_DIAMETER,
				NXdetector.NX_TIME_OF_FLIGHT, NXdetector.NX_RAW_TIME_OF_FLIGHT));

		assertThat(nxDetector.getDetector_numberScalar(), is(DETECTOR_NUMBER));
		assertThat(nxDetector.getDescriptionScalar(), is(equalTo(DETECTOR_DESCRIPTION)));

		assertThat(nxDetector.getDistanceScalar(), is(closeTo(DETECTOR_DISTANCE, 1e-15)));
		assertThat(nxDetector.getDataNode(NXdetector.NX_DISTANCE).getAttributeNames(),
				containsInAnyOrder(ATTRIBUTE_NAME_UNITS, ATTRIBUTE_NAME_LOCAL_NAME));
		assertThat(nxDetector.getAttrString(NXdetector.NX_DISTANCE, ATTRIBUTE_NAME_UNITS), is(equalTo("mm")));
		assertThat(nxDetector.getAttrString(NXdetector.NX_DISTANCE, ATTRIBUTE_NAME_LOCAL_NAME),
				is(equalTo(SCANNABLE_NAME_DETECTOR_DISTANCE + "." + SCANNABLE_NAME_DETECTOR_DISTANCE)));

		assertThat(nxDetector.getDiameterScalar(), is(DETECTOR_DIAMETER));
		assertThat(nxDetector.getDataNode(NXdetector.NX_DIAMETER).getAttributeNames(), contains(ATTRIBUTE_NAME_UNITS));
		assertThat(nxDetector.getAttrString(NXdetector.NX_DIAMETER, ATTRIBUTE_NAME_UNITS), is(equalTo(UNITS_MILLIS)));

		assertThat(nxDetector.getTime_of_flightScalar(), is(closeTo(TIME_OF_FLIGHT, 1e-15)));
		assertThat(nxDetector.getDataNode(NXdetector.NX_TIME_OF_FLIGHT).getAttributeNames(),
				containsInAnyOrder(NXdetector.NX_TIME_OF_FLIGHT_ATTRIBUTE_AXIS, NXdetector.NX_TIME_OF_FLIGHT_ATTRIBUTE_PRIMARY,
						NXdetector.NX_TIME_OF_FLIGHT_ATTRIBUTE_LONG_NAME, ATTRIBUTE_NAME_UNITS, ATTRIBUTE_NAME_LOCAL_NAME));
		assertThat(nxDetector.getTime_of_flightAttributeAxis(), is(equalTo(3L)));
		assertThat(nxDetector.getTime_of_flightAttributePrimary(), is(equalTo(1L)));
		assertThat(nxDetector.getTime_of_flightAttributeLong_name(), is(equalTo("time_of_flight")));
		assertThat(nxDetector.getAttrString(NXdetector.NX_TIME_OF_FLIGHT, ATTRIBUTE_NAME_UNITS), is(equalTo("ms")));
		assertThat(nxDetector.getAttrString(NXdetector.NX_TIME_OF_FLIGHT, ATTRIBUTE_NAME_LOCAL_NAME),
				is(equalTo(SCANNABLE_NAME_TIME_OF_FLIGHT + "." + SCANNABLE_NAME_TIME_OF_FLIGHT)));

		assertThat(nxDetector.getRaw_time_of_flightScalar(), is(equalTo(RAW_TIME_OF_FLIGHT)));
		assertThat(nxDetector.getDataNode(NXdetector.NX_RAW_TIME_OF_FLIGHT).getAttributeNames(),
				containsInAnyOrder(NXdetector.NX_RAW_TIME_OF_FLIGHT_ATTRIBUTE_FREQUENCY, ATTRIBUTE_NAME_UNITS, ATTRIBUTE_NAME_LOCAL_NAME));
		assertThat(nxDetector.getRaw_time_of_flightAttributeFrequency(), is(equalTo(RAW_TIME_OF_FLIGHT_FREQUENCY)));
		assertThat(nxDetector.getAttrString(NXdetector.NX_RAW_TIME_OF_FLIGHT, ATTRIBUTE_NAME_UNITS), is(equalTo("ms")));
		assertThat(nxDetector.getAttrString(NXdetector.NX_RAW_TIME_OF_FLIGHT, ATTRIBUTE_NAME_LOCAL_NAME),
				is(equalTo(SCANNABLE_NAME_RAW_TIME_OF_FLIGHT + "." + SCANNABLE_NAME_RAW_TIME_OF_FLIGHT)));

		assertThat(nxDetector.getSymbolicNodeNames(), containsInAnyOrder(NXdetector.NX_FLATFIELD, NX_ANGULAR_CALIBRATION));
		final SymbolicNode angularCalibrationLink = nxDetector.getSymbolicNode(NXdetector.NX_ANGULAR_CALIBRATION);
		assertThat(angularCalibrationLink, is(notNullValue()));
		assertThat(angularCalibrationLink.getSourceURI(), is(nullValue()));
		assertThat(angularCalibrationLink.getPath(), is(equalTo(INTERNAL_LINK_PATH)));
		final SymbolicNode flatfieldLink = nxDetector.getSymbolicNode(NXdetector.NX_FLATFIELD);
		assertThat(flatfieldLink, is(notNullValue()));
		assertThat(flatfieldLink.getSourceURI(), is(equalTo(new URI(EXTERNAL_FILE_PATH))));
		assertThat(flatfieldLink.getPath(), is(equalTo(EXTERNAL_LINK_PATH)));

		assertThat(nxDetector.getGroupNodeNames(), contains(GROUP_NAME_CALIBRATION_METHOD));
		final NXnote calibrationMethodNote = nxDetector.getCalibration_method();
		assertThat(calibrationMethodNote, is(notNullValue()));
		assertThat(calibrationMethodNote.getDescriptionScalar(), is(equalTo(CALIBRATION_DESCRIPTION)));
	}

}
