/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.scanning;

import static gda.data.scan.nexus.device.AbstractScannableNexusDevice.ATTR_NAME_GDA_FIELD_NAME;
import static gda.data.scan.nexus.device.AbstractScannableNexusDevice.ATTR_NAME_GDA_SCANNABLE_NAME;
import static gda.data.scan.nexus.device.AbstractScannableNexusDevice.ATTR_NAME_GDA_SCAN_ROLE;
import static gda.data.scan.nexus.device.AbstractScannableNexusDevice.ATTR_NAME_LOCAL_NAME;
import static gda.data.scan.nexus.device.AbstractScannableNexusDevice.ATTR_NAME_UNITS;
import static gda.data.scan.nexus.device.AbstractScannableNexusDevice.COLLECTION_NAME_SCANNABLES;
import static gda.data.scan.nexus.device.AbstractScannableNexusDevice.FIELD_NAME_VALUE_SET;
import static org.eclipse.dawnsci.nexus.NexusConstants.NXCLASS;
import static org.eclipse.dawnsci.nexus.builder.data.NexusDataBuilder.ATTR_NAME_TARGET;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertAxes;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertDiamondScanGroup;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertIndices;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertTarget;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;
import javax.measure.quantity.Temperature;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NXbeam;
import org.eclipse.dawnsci.nexus.NXcollection;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NXsample;
import org.eclipse.dawnsci.nexus.NXtransformations;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusScanInfo.ScanRole;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.nexus.builder.impl.DefaultNexusBuilderFactory;
import org.eclipse.dawnsci.nexus.device.INexusDeviceService;
import org.eclipse.dawnsci.nexus.device.impl.NexusDeviceService;
import org.eclipse.dawnsci.nexus.scan.impl.NexusScanFileServiceImpl;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.InterfaceUtils;
import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableEventDevice;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.device.IWritableDetector;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.AbstractPosition;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.scan.IScanService;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.event.RunEvent;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.connector.activemq.ActivemqConnectorService;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.example.file.MockFilePathService;
import org.eclipse.scanning.points.PointGeneratorService;
import org.eclipse.scanning.points.serialization.PointsModelMarshaller;
import org.eclipse.scanning.points.validation.ValidatorService;
import org.eclipse.scanning.sequencer.RunnableDeviceServiceImpl;
import org.eclipse.scanning.server.servlet.Services;
import org.eclipse.scanning.test.util.TestDetectorHelpers;
import org.eclipse.scanning.test.utilities.scan.mock.MockOperationService;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import gda.TestHelpers;
import gda.data.ServiceHolder;
import gda.data.scan.datawriter.NexusDataWriterConfiguration;
import gda.data.scan.datawriter.scannablewriter.ScannableWriter;
import gda.data.scan.datawriter.scannablewriter.SingleScannableWriter;
import gda.data.scan.datawriter.scannablewriter.TransformationWriter;
import gda.device.ControllerRecord;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.ScannableMotion;
import gda.device.ScannableMotionUnits;
import gda.device.scannable.DummyMultiFieldUnitsScannable;
import gda.device.scannable.DummyScannable;
import gda.device.scannable.DummyUnitsScannable;
import gda.device.scannable.ScannableBase;
import gda.factory.Factory;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServer;

/**
 * Test {@link ScannableNexusWrapper}. In particular tests that it
 * correctly uses the legacy spring configuration. This is done by
 * setting {@link NexusDataWriterConfiguration#setMetadataScannables(Set)}
 * {@link NexusDataWriterConfiguration#setLocationMap(Map)}.
 *
 * Additionally includes a test that {@link ScannableNexusWrapper}
 * adds attributes contributed using {@link Scannable#setScanMetadataAttribute(String, Object)}
 *
 * Note that we can't extends NexusTest as we use the real ScannableDeviceConnectorService
 * and LoaderServiceImpl instead of mocks.
 */
public class ScannableNexusWrapperScanTest {

	private static class SampleAngleScannable extends DummyUnitsScannable<Angle> {

		private static final double ANGLE = Math.toRadians(30);

		private final boolean perp;

		public SampleAngleScannable(String name, boolean perp) throws Exception {
			setName(name);
			setExtraNames(new String[] { "sax", "say" });
			setUserUnits("rad");
			setUpperGdaLimits(45.0);
			setLowerGdaLimits(-45.0);
			this.perp = perp;
		}

		@Override
		public Object getPosition() throws DeviceException {
			final double pos = (Double) super.getPosition();
			double x, y;
			if (perp) { // saperp - perpendicular to the beam line
				x = -pos * Math.sin(ANGLE);
				y = pos * Math.cos(ANGLE);
			} else { // salong - along the beam line
				x = pos * Math.cos(ANGLE);
				y = pos * Math.sin(ANGLE);
			}

			return new double[] { pos, x, y };
		}

	}

	private static class DummyStringScannable extends ScannableBase {

		private final String value;

		public DummyStringScannable(String name, String fieldName, String value) {
			super();
			setName(name);
			setInputNames(new String[] { fieldName });
			setExtraNames(new String[0]);
			this.value = value;
		}

		@Override
		public boolean isBusy() throws DeviceException {
			return false;
		}

		@Override
		public Object getPosition() {
			return value;
		}

	}

	private static class DummyEnergyScannable extends DummyUnitsScannable<Energy> {

		public DummyEnergyScannable(String name, double initialValue) throws Exception {
			super(name, initialValue, "GeV", "GeV");
			this.setLowerGdaLimits(0.1);
			this.setUpperGdaLimits(10000.0);
		}

	}

	// constants for reading the location map XML with DOM
	private static final String ELEMENT_NAME_MAP = "map";
	private static final String ELEMENT_NAME_ENTRY = "entry";
	private static final String ELEMENT_NAME_PROPERTY = "property";
	private static final String ELEMENT_NAME_BEAN = "bean";
	private static final String ELEMENT_NAME_LIST = "list";
	private static final String ELEMENT_NAME_VALUE = "value";
	private static final String ELEMENT_NAME_SET = "set";

	private static final String ATTRIBUTE_NAME_CLASS = "class";
	private static final String ATTRIBUTE_NAME_NAME = "name";
	private static final String ATTRIBUTE_NAME_KEY = "key";
	private static final String ATTRIBUTE_NAME_VALUE = "value";

	private static final String PROPERTY_NAME_PATHS = "paths";
	private static final String PROPERTY_NAME_UNITS = "units";
	private static final String PROPERTY_NAME_PREREQUISITE_SCANNABLE_NAMES = "prerequisiteScannableNames";
	private static final String PROPERTY_NAME_OFFSET = "offset";
	private static final String PROPERTY_NAME_OFFSET_UNITS = "offset_units";
	private static final String PROPERTY_NAME_TRANSFORMATION = "transformation";
	private static final String PROPERTY_NAME_VECTOR = "vector";
	private static final String PROPERTY_NAME_DEPENDS_ON = "depends_on";

	// names of the scannables to be scanned over
	private static final String SCANNABLE_NAME_SAPERP = "saperp";
	private static final String SCANNABLE_NAME_SALONG = "salong";

	public static final String TEST_CONFIG_FILE_PATH =
			"testfiles/gda/scanning/ScannableNexusWrapperScanTest/testdatawriter.xml";

	private static IScannableDeviceService scannableDeviceService;
	private static IScanService scanService;
	private static IPointGeneratorService pointGenService;
	private static INexusFileFactory nexusFileFactory;
	private static JythonServer jythonServer;


	private File output;

	private IWritableDetector<MandelbrotModel> detector;
	private Map<String, ScannableWriter> locationMap;
	private Set<String> legacyMetadataScannables;

	@BeforeClass
	public static void setServices() throws Exception {
		jythonServer = mock(JythonServer.class);
		scannableDeviceService = new ScannableDeviceConnectorService();
		scanService = new RunnableDeviceServiceImpl(scannableDeviceService); // Not testing OSGi so using hard coded service.
		pointGenService = new PointGeneratorService();
		nexusFileFactory = new NexusFileFactoryHDF5();

		final ActivemqConnectorService activemqConnectorService = new ActivemqConnectorService();
		activemqConnectorService.setJsonMarshaller(new MarshallerService(new PointsModelMarshaller()));
		final IEventService eservice  = new EventServiceImpl(activemqConnectorService);

		final Services services = new Services();
		services.setEventService(eservice);
		services.setRunnableDeviceService(scanService);
		services.setGeneratorService(pointGenService);
		services.setConnector(scannableDeviceService);

		final INexusDeviceService nexusDeviceService = new NexusDeviceService();
		new org.eclipse.dawnsci.nexus.ServiceHolder().setNexusFileFactory(nexusFileFactory);
		final org.eclipse.scanning.sequencer.ServiceHolder serviceHolder = new org.eclipse.scanning.sequencer.ServiceHolder();
		serviceHolder.setNexusDeviceService(nexusDeviceService);
		serviceHolder.setNexusScanFileService(new NexusScanFileServiceImpl());
		serviceHolder.setOperationService(new MockOperationService());
		serviceHolder.setFilePathService(new MockFilePathService());

		final org.eclipse.dawnsci.nexus.scan.ServiceHolder scanServiceHolder = new org.eclipse.dawnsci.nexus.scan.ServiceHolder();
		scanServiceHolder.setNexusDeviceService(nexusDeviceService);
		scanServiceHolder.setNexusBuilderFactory(new DefaultNexusBuilderFactory());

		final org.eclipse.scanning.points.ServiceHolder pointsServiceHolder = new org.eclipse.scanning.points.ServiceHolder();
		pointsServiceHolder.setValidatorService(new ValidatorService());
		pointsServiceHolder.setPointGeneratorService(new PointGeneratorService());
	}

	@BeforeClass
	public static void createJythonServer() throws Exception {
		jythonServer = mock(JythonServer.class);
	}

	@Before
	public void before() throws Exception {
		final MandelbrotModel model = new MandelbrotModel();
		model.setName("mandelbrot");
		model.setRealAxisName(SCANNABLE_NAME_SALONG);
		model.setImaginaryAxisName(SCANNABLE_NAME_SAPERP);

		detector = TestDetectorHelpers.createAndConfigureMandelbrotDetector(model);
		assertThat(detector, is(notNullValue()));
		detector.addRunListener(IRunListener.createRunPerformedListener(event -> System.out.println("Ran mandelbrot detector @ " + event.getPosition())));

		readLegacySpringConfig(TEST_CONFIG_FILE_PATH);
		ServiceHolder.getNexusDataWriterConfiguration().setLocationMap(locationMap);
		ServiceHolder.getNexusDataWriterConfiguration().setMetadataScannables(legacyMetadataScannables);

		final Factory factory = TestHelpers.createTestFactory();
		factory.addFindable(jythonServer);
		factory.addFindable(new SampleAngleScannable(SCANNABLE_NAME_SALONG, false));
		factory.addFindable(new SampleAngleScannable(SCANNABLE_NAME_SAPERP, true));
		factory.addFindable(new DummyScannable("sax", 5.0, "BL00P-MO-SAMPLE-01:SAX"));
		factory.addFindable(new DummyScannable("say", 23.4, "BL00P-MO-SAMPLE-01:SAY"));
		factory.addFindable(new DummyScannable("satilt", 127.4));
		factory.addFindable(new DummyScannable("saazimuth", 24.32));
		factory.addFindable(new DummyScannable("s6_xsize", 2.92));
		factory.addFindable(new DummyScannable("s2_ysize", 8.34));
		factory.addFindable(new DummyScannable("s2_xsize", 1.66));
		factory.addFindable(new DummyScannable("exit_slit", 0.0683, "BL00P-MO-SLIT-01:EXIT"));
		factory.addFindable(new DummyEnergyScannable("pgm_cff", 123.45));
		factory.addFindable(new DummyScannable("energy", 9.357e8));
		factory.addFindable(new DummyScannable("pgm_linedensity", 28));
		factory.addFindable(new DummyScannable("ring_current", 15.2, "SR-DI-DCCT-01:SIGNAL"));
		factory.addFindable(new DummyScannable("ring_energy", 47.53, "CS-CS-MSTAT-01:BEAMENERGY"));
		factory.addFindable(new DummyScannable("lc_pressure", 73.012));
		factory.addFindable(new DummyStringScannable("sample_name", "name", "test sample"));

		final DummyMultiFieldUnitsScannable<Temperature> cryostat = new DummyMultiFieldUnitsScannable<>("cryostat", "K");
		cryostat.setInputNames(new String[] { "temperature_demand" });
		cryostat.setExtraNames(new String[] { "cryostat_temperature", "temperature", "shield_temperature", "heater_percent", "heater_setting" });
		cryostat.setCurrentPosition(20.0);
		cryostat.setExtraFieldsPosition(19.9, 19.9, 26.5, 64.3, 15.3);
		factory.addFindable(cryostat);

		final DummyMultiFieldUnitsScannable<Length> id = new DummyMultiFieldUnitsScannable<>("id");
		id.setScanMetadataAttribute(Scannable.ATTR_NX_CLASS, "NXinsertion_device");
		id.setInputNames(new String[0]); // no input names
		id.setExtraNames(new String[] { "gap", "final_polarisation_label", "phase" });
		id.setExtraFieldsPosition(20, "label", 60);
		factory.addFindable(id);

		final DummyMultiFieldUnitsScannable<Angle> sapolar = new DummyMultiFieldUnitsScannable<>("sapolar");
		sapolar.setInputNames(new String[] { "alpha", "delta", "omicron" });
		sapolar.setExtraNames(new String[0]);
		sapolar.setCurrentPosition(0.5, 1.25, -0.75);
		factory.addFindable(sapolar);

		final DummyScannable attr = new DummyScannable("attributes", 2.5);
		attr.setInputNames(new String[] { NXpositioner.NX_VALUE });
		attr.setScanMetadataAttribute("stringAttr", "foo");
		attr.setScanMetadataAttribute("doubleAttr", 123.456);
		factory.addFindable(attr);

		final DummyScannable beam = new DummyScannable("beam", 3.25);
		beam.setInputNames(new String[] { "extent" });
		beam.setScanMetadataAttribute(Scannable.ATTR_NX_CLASS, "NXbeam");
		beam.setScanMetadataAttribute(Scannable.ATTR_NEXUS_CATEGORY, "NXsample");
		factory.addFindable(beam);

		Finder.addFactory(factory);
	}

	@Before
	public void createFile() throws IOException {
		output = File.createTempFile("test_legacy_nexus", ".nxs");
		output.deleteOnExit();
	}

	@After
	public void deleteFile() {
		output.delete();
		// Remove factories from Finder so they do not affect other tests
		Finder.removeAllFactories();
	}

	public void readLegacySpringConfig(String path) throws Exception {
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder builder = factory.newDocumentBuilder();
		final Document document = builder.parse(new File(path));
		final Element rootElement = document.getDocumentElement();
		rootElement.normalize();

		for (final Element beanElement : getChildElements(rootElement, "bean")) {
			final String classAttr = beanElement.getAttribute("class");
			// only process beans with class MethodInvokingFactoryBean
			if ("org.springframework.beans.factory.config.MethodInvokingFactoryBean".equals(classAttr)) {
				String staticMethod = null;
				Element argumentsPropertyElement = null;
				for (final Element propertyElement : getChildElements(beanElement, "property")) {
					final String propertyName = propertyElement.getAttribute("name");
					if ("staticMethod".equals(propertyName)) {
						staticMethod = propertyElement.getAttribute("value");
					} else if ("arguments".equals(propertyName)) {
						argumentsPropertyElement = propertyElement;
					}
				}

				if ("gda.data.scan.datawriter.NexusDataWriter.setLocationmap".equals(staticMethod)) {
					this.locationMap = readLegacyLocationMap(argumentsPropertyElement);
//					printLocationMap(locationMap);
				} else if ("gda.data.scan.datawriter.NexusDataWriter.setMetadatascannables".equals(staticMethod)) {
					this.legacyMetadataScannables = readLegacyMetadataScannables(argumentsPropertyElement);
//					printMetadataScannables(legacyMetadataScannables);
				}
			}
		}
	}

	@Test
	public void testNexusScannableWrapperScan() throws Exception {
		testGridScan(null);
	}

	@Test
	public void testNexusScannableWrapperScan3D() throws Exception {
		testGridScan("energy");
	}

	@Test
	public void testNexusScannableWrapperScan3DWithJythonOuterScannable() throws Exception {
		TestHelpers.setUpTest(ScannableNexusWrapperScanTest.class, "testNexusScannableWrapper3DWithJythonOuterScannable", true);
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace("jy_energy", new DummyScannable("jy_energy", 9.357e8));

		testGridScan("jy_energy");
	}

	private void testGridScan(String outerScannableName) throws Exception {
		final int[] size = (outerScannableName == null) ? new int[] { 8, 5 } : new int[] { 2, 5, 3 };
		final IRunnableDevice<ScanModel> scanner = createGridScan(detector, outerScannableName, size);
		scanner.run(null);
		checkNexusFile(scanner, size);
	}

	@SuppressWarnings("unused")
	private void printLocationMap(Map<String, ScannableWriter> locationMap) {
		for (final Map.Entry<String, ScannableWriter> entry : locationMap.entrySet()) {
			System.err.println("scannable name: " + entry.getKey());
			final SingleScannableWriter scannableWriter = (SingleScannableWriter) entry.getValue();
			final String[] paths = scannableWriter.getPaths();
			for (int i = 0; i < paths.length; i++) {
				System.err.println("paths[" + i + "] = " + paths[i]);
			}
			final String[] units = scannableWriter.getUnits();
			if (units == null) {
				System.err.println("no units");
			} else {
				for (int i = 0; i < units.length; i++) {
					System.err.println("units[" + i + "] = " + units[i]);
				}
			}
			final Collection<String> preqrequisites = scannableWriter.getPrerequisiteScannableNames();
			if (preqrequisites == null || preqrequisites.isEmpty()) {
				System.err.println("No prerequisites");
			} else {
				System.err.println("prerequisites: " +
						StringUtils.join(scannableWriter.getPrerequisiteScannableNames(), ", "));
			}
		}
	}

	@SuppressWarnings("unused")
	private void printMetadataScannables(Set<String> metadataScannables) {
		if (metadataScannables == null || metadataScannables.isEmpty()) {
			System.err.println("No metadata scannables");
		} else {
			System.err.println("Metadata scannables: " + StringUtils.join(metadataScannables, ", "));
		}
	}

	private Map<String, ScannableWriter> readLegacyLocationMap(Element argumentsPropertyElement) throws Exception {
		final Map<String, ScannableWriter> locationMap = new HashMap<>();
		final Element mapElement = getChildElements(argumentsPropertyElement, ELEMENT_NAME_MAP).get(0);
		for (final Element entryElement : getChildElements(mapElement, ELEMENT_NAME_ENTRY)) {
			final String scannableName = entryElement.getAttribute(ATTRIBUTE_NAME_KEY);

			final Element beanElement = getChildElements(entryElement, ELEMENT_NAME_BEAN).get(0);
			final String beanClass = beanElement.getAttribute(ATTRIBUTE_NAME_CLASS);
			final Class<?> klass = Class.forName(beanClass);
			if (!(klass.equals(SingleScannableWriter.class) || klass.equals(TransformationWriter.class))) {
				throw new IllegalArgumentException("Unsupported ScannableWriter class: " + klass);
			}

			final SingleScannableWriter scannableWriter = (SingleScannableWriter) Class.forName(beanClass).
					getDeclaredConstructor().newInstance();
			for (final Element propertyElement : getChildElements(beanElement, ELEMENT_NAME_PROPERTY)) {
				final String propertyName = propertyElement.getAttribute(ATTRIBUTE_NAME_NAME);
				final Object[] values = getValues(propertyName, propertyElement);

				switch (propertyName) {
					case PROPERTY_NAME_PATHS:
						scannableWriter.setPaths((String[]) values);
						break;
					case PROPERTY_NAME_UNITS:
						scannableWriter.setUnits((String[]) values);
						break;
					case PROPERTY_NAME_PREREQUISITE_SCANNABLE_NAMES:
						scannableWriter.setPrerequisiteScannableNames(Arrays.asList((String[]) values));
						break;
					case PROPERTY_NAME_OFFSET:
						((TransformationWriter) scannableWriter).setOffset((Double[][]) values);
						break;
					case PROPERTY_NAME_OFFSET_UNITS:
						((TransformationWriter) scannableWriter).setOffsetUnits((String[]) values);
						break;
					case PROPERTY_NAME_TRANSFORMATION:
						((TransformationWriter) scannableWriter).setTransformation((String[]) values);
						break;
					case PROPERTY_NAME_VECTOR:
						((TransformationWriter) scannableWriter).setVector((Double[][]) values);
						break;
					case PROPERTY_NAME_DEPENDS_ON:
						((TransformationWriter) scannableWriter).setDependsOn((String[]) values);
						break;
					default:
						throw new IllegalArgumentException("Unknown property: " + propertyName);
				}
			}
			locationMap.put(scannableName, scannableWriter);
		}

		return locationMap;
	}

	private Object[] getValues(final String propertyName, final Element valuePropertyElement) {
		final Class<?> expectedType = getExpectedType(propertyName);
		assertThat(expectedType.isArray(), is(true)); // sanity check, all property are of array type
		final Class<?> elementType = expectedType.getComponentType();

		final String valueStr = valuePropertyElement.getAttribute(ATTRIBUTE_NAME_VALUE);
		if (!StringUtils.isBlank(valueStr)) {
			assertThat(elementType, is(equalTo(String.class))); // single values are always String so far
			return new String[] { valueStr };
		}

		// if 'value' not listed, should be a single child 'list' element
		final List<Element> listElements = getChildElements(valuePropertyElement, ELEMENT_NAME_LIST);
		assertThat(listElements, hasSize(1));

		return getListContents(listElements.get(0), elementType);
	}

	private Class<?> getExpectedType(String propertyName) {
		if (propertyName.equals(PROPERTY_NAME_VECTOR) || propertyName.equals(PROPERTY_NAME_OFFSET)) {
			return Double[][].class;
		}
		return String[].class;
	}

	private Object[] getListContents(Element listElement, Class<?> elementType) {
		if (elementType.isArray()) {
			final List<Element> childListElements = getChildElements(listElement, ELEMENT_NAME_LIST);
			assertThat(childListElements, is(not(empty())));
			final Class<?> componentType = elementType.getComponentType();
			return childListElements.stream()
					.map(childListElement -> getListContents(childListElement, componentType))
					.toArray(size -> (Object[]) Array.newInstance(elementType, size));
		}

		final List<Element> valueElements = getChildElements(listElement, ELEMENT_NAME_VALUE);
		final String[] stringArray = valueElements.stream().map(Element::getTextContent).toArray(String[]::new);
		if (elementType.equals(String.class)) {
			return stringArray;
		} else if (elementType.equals(Double.class)) {
			return Arrays.stream(stringArray).map(Double::valueOf).toArray(Double[]::new);
		}
		throw new IllegalArgumentException("Unexpected component type: " + elementType);
	}

	private Set<String> readLegacyMetadataScannables(Element argumentsPropertyElement) {
		final Set<String> metadataScannables = new HashSet<>();
		final String valueAttr = argumentsPropertyElement.getAttribute(ATTRIBUTE_NAME_VALUE);
		if (!StringUtils.isBlank(valueAttr)) {
			metadataScannables.add(valueAttr);
		} else {
			final Element setElement = getChildElements(argumentsPropertyElement, ELEMENT_NAME_SET).get(0);
			for (final Element valueElement : getChildElements(setElement, ELEMENT_NAME_VALUE)) {
				metadataScannables.add(valueElement.getTextContent());
			}
		}

		return metadataScannables;
	}

	private List<Element> getChildElements(Element parentElement, String tagName) {
		final List<Element> childElements = new ArrayList<>();
		for (Node child = parentElement.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getNodeType() == Node.ELEMENT_NODE && tagName.equals(child.getNodeName())) {
				childElements.add((Element) child);
			}
		}

		return childElements;
	}

	private void checkNexusFile(final IRunnableDevice<ScanModel> scanner, int... sizes) throws Exception{
		final ScanModel scanModel = ((AbstractRunnableDevice<ScanModel>) scanner).getModel();
		assertThat(scanner.getDeviceState(), is(DeviceState.ARMED));
		final String filePath = ((AbstractRunnableDevice<ScanModel>) scanner).getModel().getFilePath();

		final NexusFile nf = org.eclipse.dawnsci.nexus.ServiceHolder.getNexusFileFactory().newNexusFile(filePath);
		nf.openToRead();

		final TreeFile nexusTree = NexusUtils.loadNexusTree(nf);
		final NXroot rootNode = (NXroot) nexusTree.getGroupNode();
		final NXentry entry = rootNode.getEntry();
		final NXinstrument instrument = entry.getInstrument();
		final NXsample sample = entry.getSample();

		// check the scan points have been written correctly
		assertDiamondScanGroup(entry, false, false, sizes);

		DataNode dataNode = null;
		IDataset dataset = null;
		int[] shape = null;

		// check metadata scannables
		checkMetadataScannables(scanModel, entry);
		checkAttributeScannable(instrument);
		checkBeamSizeScannable(sample);

		final IPosition pos = scanModel.getPointGenerator().iterator().next();
		final Collection<String> scannableNames = pos.getNames();

		final String dataGroupName = scanModel.getDetectors().get(0).getName();
		final NXdata nxData = entry.getData(dataGroupName);
		assertThat(nxData, is(notNullValue()));

		// Check axes
		final List<String> expectedAxesNames = Stream.concat(
				scannableNames.stream().map(x -> x + NexusConstants.FIELD_SEPERATOR + FIELD_NAME_VALUE_SET),
				Arrays.asList("real", "imaginary").stream()).collect(Collectors.toList());
		assertAxes(nxData, expectedAxesNames.toArray(new String[expectedAxesNames.size()]));

		final int[] defaultDimensionMappings = IntStream.range(0, sizes.length).toArray();
		int scannableIndex = -1;
		for (final String scannableName : scannableNames) {
			scannableIndex++;
			NXpositioner positioner = instrument.getPositioner(scannableName);
			final boolean inLocationMap = locationMap.containsKey(scannableName);
			if (inLocationMap) {
				final NXcollection scannablesCollection = (NXcollection) instrument.getGroupNode(
						COLLECTION_NAME_SCANNABLES);
				assertThat(scannablesCollection, is(notNullValue()));
				positioner = (NXpositioner) scannablesCollection.getGroupNode(scannableName);
			}

			assertThat(positioner, is(notNullValue()));

			assertThat(positioner.getGroupNodeNames(), is(empty()));
			assertThat(positioner.getNumberOfGroupNodes(), is(0));

			assertThat(positioner.getAttributeNames(),
					containsInAnyOrder(NXCLASS, ATTR_NAME_GDA_SCANNABLE_NAME, ATTR_NAME_GDA_SCAN_ROLE));
			assertThat(positioner.getNumberOfAttributes(), is(3));

			assertThat(positioner.getAttrString(null, ATTR_NAME_GDA_SCANNABLE_NAME), is(equalTo(scannableName)));
			assertThat(positioner.getAttrString(null, ATTR_NAME_GDA_SCAN_ROLE),
					is(equalTo(ScanRole.SCANNABLE.toString().toLowerCase())));

			final Scannable legacyScannable = getScannable(scannableName);
			final List<String> inputFieldNames = new ArrayList<>();
			inputFieldNames.addAll(Arrays.asList(legacyScannable.getInputNames()));
			inputFieldNames.addAll(Arrays.asList(legacyScannable.getExtraNames()));
			final List<String> outputFieldNames = new ArrayList<>(inputFieldNames);
			if (outputFieldNames.contains(scannableName)) {
				outputFieldNames.set(outputFieldNames.indexOf(scannableName), NXpositioner.NX_VALUE);
			}

			// check the number of data nodes, num fields of legacy scannable + name + demand_value
			final List<String> additionalDataNodeNames = Arrays.asList(NXpositioner.NX_NAME, FIELD_NAME_VALUE_SET,
					NXpositioner.NX_SOFT_LIMIT_MIN, NXpositioner.NX_SOFT_LIMIT_MAX);
			final String[] expectedDataNodeNames = Stream.of(outputFieldNames, additionalDataNodeNames)
					.flatMap(Collection::stream).toArray(String[]::new);
			assertThat(positioner.getDataNodeNames(), containsInAnyOrder(expectedDataNodeNames));
			assertThat(positioner.getNumberOfDataNodes(), is(expectedDataNodeNames.length));

			assertThat(positioner.getNameScalar(), is(equalTo(scannableName)));

			// Demand values should be 1D
			dataNode = positioner.getDataNode(FIELD_NAME_VALUE_SET);
			dataset = dataNode.getDataset().getSlice();
			shape = dataset.getShape();
			assertThat(shape.length, is(1));
			assertThat(shape[0], is(sizes[scannableIndex]));

			String nxDataFieldName = scannableName + NexusConstants.FIELD_SEPERATOR + FIELD_NAME_VALUE_SET;
			assertThat(nxData.getDataNode(nxDataFieldName), is(sameInstance(dataNode)));
			assertIndices(nxData, nxDataFieldName, scannableIndex);
			assertTarget(nxData, nxDataFieldName, rootNode,
					"/entry/instrument/" + (inLocationMap ? COLLECTION_NAME_SCANNABLES + "/" : "") +
					scannableName + "/" + FIELD_NAME_VALUE_SET);

			final String[] paths = locationMap.containsKey(scannableName) ?
					((SingleScannableWriter) locationMap.get(scannableName)).getPaths() : null;
			final String[] expectedUnits = getExpectedUnits(legacyScannable);

			assertThat(positioner.getNameScalar(), is(equalTo(scannableName)));

			final DataNode setValueDataNode = positioner.getDataNode(NXpositioner.NX_VALUE + "_set");
			assertThat(setValueDataNode, is(notNullValue()));
			dataset = setValueDataNode.getDataset().getSlice();
			shape = dataset.getShape();
			assertThat(shape, is(equalTo(new int[] { sizes[scannableIndex] })));

			assertThat(positioner.getSoft_limit_minScalar(),
					is(equalTo(((ScannableMotion) legacyScannable).getLowerGdaLimits()[0])));
			assertThat(positioner.getSoft_limit_maxScalar(),
					is(equalTo(((ScannableMotion) legacyScannable).getUpperGdaLimits()[0])));

			for (int fieldIndex = 0; fieldIndex < outputFieldNames.size(); fieldIndex++) {
				final String valueFieldName = outputFieldNames.get(fieldIndex);
				dataNode = positioner.getDataNode(valueFieldName);
				assertThat(valueFieldName, dataNode, is(notNullValue()));

				assertThat(dataNode.getAttribute(ATTR_NAME_LOCAL_NAME), is(notNullValue()));
				assertThat(valueFieldName, positioner.getAttrString(valueFieldName, ATTR_NAME_LOCAL_NAME),
						is(equalTo(scannableName + NexusConstants.FIELD_SEPERATOR + inputFieldNames.get(fieldIndex))));
				assertThat(dataNode.getAttribute(ATTR_NAME_GDA_FIELD_NAME), is(notNullValue()));
				assertThat(valueFieldName, positioner.getAttrString(valueFieldName, ATTR_NAME_GDA_FIELD_NAME),
						is(equalTo(inputFieldNames.get(fieldIndex))));

				// Actual values should be scanD
				dataset = dataNode.getDataset().getSlice();
				shape = dataset.getShape();
				assertThat(valueFieldName, shape, is(equalTo(sizes)));

				if (fieldIndex == 0) {
					// currently only the first field of a Scannable is linked to from an NXdata group,
					// this is probably incorrect, see JIRA DAQ-311
					nxDataFieldName = scannableName + NexusConstants.FIELD_SEPERATOR + valueFieldName;
					assertThat(nxData.getDataNode(nxDataFieldName), is(sameInstance(dataNode)));
					assertIndices(nxData, nxDataFieldName, defaultDimensionMappings);
					assertTarget(nxData, nxDataFieldName, rootNode,
							"/entry/instrument/" + (inLocationMap ? COLLECTION_NAME_SCANNABLES + "/" : "") +
							scannableName + "/" + valueFieldName);

					if (paths != null && paths.length > fieldIndex) {
						// check the same datanode can also be found at the path for this fieldname in the location map
						final DataNode expectedLinkedDataNode = NexusUtils.getDataNode(entry, paths[fieldIndex]);
						assertThat(valueFieldName, expectedLinkedDataNode, is(sameInstance(dataNode)));
						if (expectedUnits != null && expectedUnits.length > fieldIndex) {
							// check the units attribute has been written according to the location map
							final Attribute unitsAttribute = dataNode.getAttribute(ATTR_NAME_UNITS);
							assertThat(unitsAttribute, is(notNullValue()));
							assertThat(unitsAttribute.getFirstElement(), is(equalTo(expectedUnits[fieldIndex])));
						}
					}
				}
			}
		}
	}

	private Scannable getScannable(String scannableName) {
		final Findable found = Finder.find(scannableName);
		if (found instanceof Scannable && !(found instanceof Detector)) {
			return (Scannable) found;
		}

		final Object jythonObj = InterfaceProvider.getJythonNamespace().getFromJythonNamespace(scannableName);
		if (jythonObj instanceof Scannable && !(jythonObj instanceof Detector)) {
			return (Scannable) jythonObj;
		}

		throw new IllegalArgumentException("No scannable exists with name " + scannableName);
	}

	private void checkMetadataScannables(final ScanModel scanModel, NXentry entry) throws Exception {
		DataNode dataNode;
		IDataset dataset;
		final NXinstrument instrument = entry.getInstrument();

		final Collection<IScannable<?>> perScan  = scanModel.getMonitorsPerScan();
		final Set<String> metadataScannableNames = perScan.stream().map(INameable::getName).collect(Collectors.toSet());

		final Set<String> expectedMetadataScannableNames = new HashSet<>(legacyMetadataScannables);
		Set<String> scannableNamesToCheck = new HashSet<>(expectedMetadataScannableNames);
		do {
			final Set<String> addedScannableNames = new HashSet<>();
			for (final String metadataScannableName : scannableNamesToCheck) {
				final Collection<String> reqdScannableNames = locationMap.get(metadataScannableName).getPrerequisiteScannableNames();
				if (reqdScannableNames != null && !reqdScannableNames.isEmpty()) {
					for (final String reqdScannableName : reqdScannableNames) {
						if (!expectedMetadataScannableNames.contains(reqdScannableName)) {
							expectedMetadataScannableNames.add(reqdScannableName);
							addedScannableNames.add(reqdScannableName);
						}
					}
				}
			}
			scannableNamesToCheck = addedScannableNames;
		} while (!scannableNamesToCheck.isEmpty());

		// check the metadata scannables specified in the legacy spring config are present
		final List<String> scannableNames = ((AbstractPosition) scanModel.getPointGenerator().iterator().next()).getNames();
		for (final String legacyMetadataScannableName : expectedMetadataScannableNames) {
			assertThat(legacyMetadataScannableName, metadataScannableNames.contains(legacyMetadataScannableName)
					|| scannableNames.contains(legacyMetadataScannableName), is(true));
		}

		// check each metadata scannable has been written correctly
		for (final String metadataScannableName : metadataScannableNames) {
			final Scannable scannable = Finder.find(metadataScannableName);
			if (scannable.getScanMetadataAttribute(Scannable.ATTR_NEXUS_CATEGORY) != null) {
				// the nexus object for a scannable with a nexus category won't be under NXinstrument
				continue;
			}

			final NXobject nexusObject;
			if (locationMap.containsKey(metadataScannableName)) {
				// if there is an entry in the location map for this group, the nexus object
				// should have been added to a collection called 'scannables'
				assertThat(instrument.getGroupNode(metadataScannableName), is(nullValue()));
				final NXcollection scannablesCollection = (NXcollection) instrument.getGroupNode(COLLECTION_NAME_SCANNABLES);
				assertThat(scannablesCollection, is(notNullValue()));
				nexusObject = (NXobject) scannablesCollection.getGroupNode(metadataScannableName);
			} else {
				nexusObject = (NXobject) instrument.getGroupNode(metadataScannableName);
			}

			// Check that the nexus object is of the expected base class
			assertThat(nexusObject, is(notNullValue()));
			assertThat(nexusObject.getNexusBaseClass(), is(getExpectedNexusBaseClass(scannable)));

			assertThat(nexusObject.getGroupNodeNames(), is(empty()));
			assertThat(nexusObject.getNumberOfGroupNodes(), is(0));

			assertThat(nexusObject.getAttributeNames(),
					containsInAnyOrder(NXCLASS, ATTR_NAME_GDA_SCANNABLE_NAME, ATTR_NAME_GDA_SCAN_ROLE));
			assertThat(nexusObject.getNumberOfAttributes(), is(3));
			assertThat(nexusObject.getAttrString(null, ATTR_NAME_GDA_SCANNABLE_NAME),
					is(equalTo(metadataScannableName)));
			assertThat(nexusObject.getAttrString(null, ATTR_NAME_GDA_SCAN_ROLE),
					is(equalTo(ScanRole.MONITOR_PER_SCAN.toString().toLowerCase())));

			final String[] valueFieldNames = ArrayUtils.addAll(scannable.getInputNames(), scannable.getExtraNames());
			if (nexusObject.getNexusBaseClass() == NexusBaseClass.NX_POSITIONER && scannable.getInputNames().length > 0) {
				valueFieldNames[0] = NXpositioner.NX_VALUE;
			}

			final Set<String> expectedDataNodeNames = new HashSet<>();
			expectedDataNodeNames.addAll(scannable.getScanMetadataAttributeNames());
			expectedDataNodeNames.removeAll(Arrays.asList(Scannable.ATTR_NX_CLASS, Scannable.ATTR_NEXUS_CATEGORY));
			expectedDataNodeNames.addAll(Arrays.asList(NXpositioner.NX_NAME));
			expectedDataNodeNames.addAll(Arrays.asList(valueFieldNames));
			if (hasLimits(scannable)) expectedDataNodeNames.addAll(Arrays.asList(NXpositioner.NX_SOFT_LIMIT_MIN, NXpositioner.NX_SOFT_LIMIT_MAX));
			final Optional<String> expectedPvName = Optional.of(scannable).filter(ControllerRecord.class::isInstance)
					.map(ControllerRecord.class::cast).map(ControllerRecord::getControllerRecordName);
			expectedPvName.ifPresent(pvName -> expectedDataNodeNames.add(NXpositioner.NX_CONTROLLER_RECORD));
			assertThat(nexusObject.getDataNodeNames(), containsInAnyOrder(expectedDataNodeNames.toArray()));
			assertThat(nexusObject.getNumberOfDataNodes(), is(expectedDataNodeNames.size()));
			assertThat(nexusObject.getString(NXpositioner.NX_NAME), is(equalTo(metadataScannableName)));

			if (nexusObject instanceof NXpositioner) {
				final NXpositioner positioner = (NXpositioner) nexusObject;
				if (hasLimits(scannable)) {
					assertThat(positioner.getSoft_limit_minScalar(), is(equalTo(((ScannableMotion) scannable).getLowerGdaLimits()[0])));
					assertThat(positioner.getSoft_limit_maxScalar(), is(equalTo(((ScannableMotion) scannable).getUpperGdaLimits()[0])));
				}
				if (expectedPvName.isPresent()) {
					assertThat(positioner.getController_recordScalar(), is(equalTo(expectedPvName.get())));
				}
			}

			final Object[] positionArray = getPositionArray(scannable);
			final String[] paths = locationMap.containsKey(metadataScannableName) ?
					((SingleScannableWriter) locationMap.get(metadataScannableName)).getPaths() : null;

			final String[] expectedUnits = getExpectedUnits(scannable);
			final Collection<String> prerequisiteScannableNames = Collections.emptyList();

			// check each field is written both inside the nexus object for the metadata scannable
			for (int fieldIndex = 0; fieldIndex < valueFieldNames.length; fieldIndex++) {
				final String valueFieldName = valueFieldNames[fieldIndex];
				dataNode = nexusObject.getDataNode(valueFieldName);
				assertThat(dataNode, is(notNullValue()));
				dataset = dataNode.getDataset().getSlice();
				assertThat(dataset.getRank(), is(0));
				assertThat(dataset.getObject(), is(equalTo(positionArray[fieldIndex])));

				if (paths != null && paths.length > fieldIndex) {
					// and to the location referred to by the location map
					final DataNode expectedLinkedDataNode = NexusUtils.getDataNode(entry, paths[fieldIndex]);
					if (metadataScannableName.equals("sax") || metadataScannableName.equals("say")) {
						// special case, datasets in location map overwritten with
						// lazy writable dataset by salong and saperp
						assertThat(expectedLinkedDataNode, is(notNullValue()));
					} else {
						assertThat(expectedLinkedDataNode, is(sameInstance(dataNode)));
						if (expectedUnits != null && expectedUnits.length > fieldIndex && StringUtils.isNotBlank(expectedUnits[fieldIndex])) {
							final Attribute unitsAttribute = dataNode.getAttribute(ATTR_NAME_UNITS);
							assertThat(unitsAttribute, is(notNullValue()));
							assertThat(unitsAttribute.getFirstElement(), is(equalTo(expectedUnits[fieldIndex])));
						}
					}
				}
			}

			if (locationMap.get(metadataScannableName) instanceof TransformationWriter) {
				checkTransformationsAtributes(nexusObject, valueFieldNames, (TransformationWriter) locationMap.get(metadataScannableName));
			}

			if (prerequisiteScannableNames != null) {
				for (final String prerequisiteScannableName : prerequisiteScannableNames) {
					assertThat(metadataScannableNames, contains(prerequisiteScannableName));
				}
			}
		}
	}

	private void checkTransformationsAtributes(NXobject nexusObject, String[] valueFieldNames, TransformationWriter transformationWriter) {
		for (int fieldIndex = 0; fieldIndex < valueFieldNames.length; fieldIndex++) {
			final DataNode dataNode = nexusObject.getDataNode(valueFieldNames[fieldIndex]);
			assertThat(dataNode.getAttributeNames(), containsInAnyOrder(
					ATTR_NAME_LOCAL_NAME, ATTR_NAME_GDA_FIELD_NAME, ATTR_NAME_UNITS, ATTR_NAME_TARGET,
					NXtransformations.NX_AXISNAME_ATTRIBUTE_DEPENDS_ON,
					NXtransformations.NX_AXISNAME_ATTRIBUTE_OFFSET, NXtransformations.NX_AXISNAME_ATTRIBUTE_OFFSET_UNITS,
					NXtransformations.NX_AXISNAME_ATTRIBUTE_TRANSFORMATION_TYPE,
					NXtransformations.NX_AXISNAME_ATTRIBUTE_VECTOR));

			assertThat(dataNode.getAttribute(ATTR_NAME_UNITS).getFirstElement(), is(equalTo(transformationWriter.getUnits()[fieldIndex])));
			assertThat(dataNode.getAttribute(NXtransformations.NX_AXISNAME_ATTRIBUTE_DEPENDS_ON).getFirstElement(),
					is(equalTo(transformationWriter.getDependsOn()[fieldIndex])));
			assertThat(dataNode.getAttribute(NXtransformations.NX_AXISNAME_ATTRIBUTE_TRANSFORMATION_TYPE).getFirstElement(),
					is(equalTo(transformationWriter.getTransformation()[fieldIndex])));

			final Attribute vectorAttr = dataNode.getAttribute(NXtransformations.NX_AXISNAME_ATTRIBUTE_VECTOR);
			assertThat(vectorAttr, is(notNullValue()));
			assertThat(vectorAttr.getValue(), is(equalTo(DatasetFactory.createFromObject(transformationWriter.getVector()[fieldIndex]))));

			final Attribute offsetAttr = dataNode.getAttribute(NXtransformations.NX_AXISNAME_ATTRIBUTE_OFFSET);
			assertThat(offsetAttr, is(notNullValue()));
			assertThat(offsetAttr.getValue(), is(equalTo(DatasetFactory.createFromObject(transformationWriter.getOffset()[fieldIndex]))));

			assertThat(dataNode.getAttribute(NXtransformations.NX_AXISNAME_ATTRIBUTE_OFFSET_UNITS).getFirstElement(),
					is(equalTo(transformationWriter.getUnits()[fieldIndex])));
		}
	}

	private String[] getExpectedUnits(Scannable scannable) {
		final int numFields = scannable.getInputNames().length + scannable.getExtraNames().length;
		final String[] expectedUnits = new String[numFields];

		final String scannableUnits = scannable instanceof ScannableMotionUnits ?
				((ScannableMotionUnits) scannable).getUserUnits() : null;
		Arrays.fill(expectedUnits, scannableUnits);

		if (locationMap.containsKey(scannable.getName())) {
			final String[] writerUnits = ((SingleScannableWriter) locationMap.get(scannable.getName())).getUnits();
			for (int i = 0; i < expectedUnits.length; i++) {
				if (writerUnits != null && i < writerUnits.length && writerUnits[i] != null) {
					expectedUnits[i] = writerUnits[i];
				}
			}
		}

		return expectedUnits;
	}

	private boolean hasLimits(Scannable scannable) {
		if (scannable instanceof ScannableMotion) {
			// assume that if upperLimits is set then lowerLimits is set also
			final Double[] upperLimits = ((ScannableMotion) scannable).getUpperGdaLimits();
			return upperLimits != null && upperLimits.length > 0;
		}
		return false;
	}

	private void checkAttributeScannable(NXinstrument instrument) throws Exception {
		final Scannable attributeScannable = Finder.find("attributes");
		final NXpositioner positioner = instrument.getPositioner(attributeScannable.getName());
		assertThat(positioner, is(notNullValue()));

		for (final String attrName : attributeScannable.getScanMetadataAttributeNames()) {
			final DataNode dataNode = positioner.getDataNode(attrName);
			assertThat(dataNode, is(notNullValue()));
			final IDataset dataset = dataNode.getDataset().getSlice();
			assertThat(dataset.getRank(), is(0));
			final Object expectedValue = attributeScannable.getScanMetadataAttribute(attrName);
			assertThat(dataset, is(instanceOf(InterfaceUtils.getInterface(expectedValue))));
			assertThat(dataset.getObject(), is(equalTo(expectedValue)));
		}
	}

	private void checkBeamSizeScannable(NXsample sample) throws Exception {
		final NXbeam beam = sample.getBeam();
		assertThat(beam, is(notNullValue()));

		assertThat(beam.getNumberOfGroupNodes(), is(0));
		assertThat(beam.getNumberOfAttributes(), is(3));
		assertThat(beam.getAttrString(null, ATTR_NAME_GDA_SCANNABLE_NAME), is(equalTo("beam")));
		assertThat(beam.getAttrString(null, ATTR_NAME_GDA_SCAN_ROLE),
				is(equalTo(ScanRole.MONITOR_PER_SCAN.toString().toLowerCase())));
		assertThat(beam.getNexusBaseClass(), is(NexusBaseClass.NX_BEAM));

		assertThat(beam.getDataNodeNames(), containsInAnyOrder("name", NXbeam.NX_EXTENT));
		assertThat(beam.getDataNode("name").getString(), is(equalTo("beam")));
		assertThat(beam.getExtentScalar(), is(closeTo(3.25, 1e-15)));
	}

	private NexusBaseClass getExpectedNexusBaseClass(Scannable metadataScannable) throws DeviceException {
		final String nxClassAttr = (String) metadataScannable.getScanMetadataAttribute(Scannable.ATTR_NX_CLASS);
		if (nxClassAttr != null) {
			return NexusBaseClass.getBaseClassForName(nxClassAttr);
		}

		return metadataScannable.getInputNames().length == 1 ? NexusBaseClass.NX_POSITIONER : NexusBaseClass.NX_COLLECTION;
	}

	private Object[] getPositionArray(Scannable legacyScannable) throws DeviceException {
		final Object position = legacyScannable.getPosition();
		if (!position.getClass().isArray()) {
			return new Object[] { position };
		}

		if (position.getClass().getComponentType().isPrimitive()) {
			final int size = Array.getLength(position);
			final Object[] outputArray = new Object[size];
			for (int i = 0; i < size; i++) {
				outputArray[i] = Array.get(position, i);
			}
			return outputArray;
		}

		return (Object[]) position;
	}

	private IRunnableDevice<ScanModel> createGridScan(final IRunnableDevice<? extends IDetectorModel> detector,
			String outerScannableName, int... size) throws Exception {
		// Create scan points for a grid and make a generator
		final TwoAxisGridPointsModel gridModel = new TwoAxisGridPointsModel();
		gridModel.setxAxisName("salong");
		gridModel.setxAxisPoints(size[size.length-1]);
		gridModel.setyAxisName("saperp");
		gridModel.setyAxisPoints(size[size.length-2]);
		gridModel.setBoundingBox(new BoundingBox(0,0,3,3));

		final CompoundModel compoundModel = new CompoundModel();

		// We add the outer scans, if any
		if (outerScannableName != null) {
			for (int dim = 0; dim < size.length - 2; dim++) {
				if (size[dim] > 1) { // TODO outer scannable name(s)? could use cryostat temperature as an outer scan
				    compoundModel.addModel(new AxialStepModel(outerScannableName, 10000,20000,9999.99d/(size[dim]-1)));
				} else {
					compoundModel.addModel(new AxialStepModel(outerScannableName + (dim+1), 10, 20, 30)); // Will generate one value at 10
				}
			}
		}
		compoundModel.addModel(gridModel);
		final IPointGenerator<CompoundModel> pointGen = pointGenService.createCompoundGenerator(compoundModel);

		// Create the model for a scan
		final ScanModel scanModel = new ScanModel();
		scanModel.setPointGenerator(pointGen);
		scanModel.setScanPathModel(compoundModel);
		scanModel.setDetector(detector);

		final IScannable<?> attributeScannable = scannableDeviceService.getScannable("attributes");
		final IScannable<?> beamSizeScannable = scannableDeviceService.getScannable("beam");
		scanModel.setMonitorsPerScan(attributeScannable, beamSizeScannable);

		// Create a file to scan into
		final File output = File.createTempFile("test_legacy_nexus", ".nxs");
		output.deleteOnExit();
		scanModel.setFilePath(output.getAbsolutePath());
		System.out.println("File writing to " + scanModel.getFilePath());

		// Create a scan and run it without publishing events
		final IRunnableDevice<ScanModel> scanner = scanService.createScanDevice(scanModel);

		final IPointGenerator<?> fgen = pointGen;
		((IRunnableEventDevice<ScanModel>)scanner).addRunListener(new IRunListener() {
			@Override
			public void runWillPerform(RunEvent evt) throws ScanningException {
				System.out.println("Running acquisition scan of size "+fgen.size());
			}
		});

		return scanner;
	}

}
