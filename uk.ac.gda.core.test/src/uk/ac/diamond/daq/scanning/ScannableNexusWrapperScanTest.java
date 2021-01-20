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

import static gda.data.scan.nexus.device.ScannableNexusDevice.ATTR_NAME_GDA_FIELD_NAME;
import static gda.data.scan.nexus.device.ScannableNexusDevice.ATTR_NAME_GDA_SCANNABLE_NAME;
import static gda.data.scan.nexus.device.ScannableNexusDevice.ATTR_NAME_GDA_SCAN_ROLE;
import static gda.data.scan.nexus.device.ScannableNexusDevice.ATTR_NAME_LOCAL_NAME;
import static gda.data.scan.nexus.device.ScannableNexusDevice.COLLECTION_NAME_SCANNABLES;
import static gda.data.scan.nexus.device.ScannableNexusDevice.FIELD_NAME_VALUE_SET;
import static org.eclipse.dawnsci.nexus.NexusConstants.NXCLASS;
import static org.eclipse.scanning.test.utilities.scan.nexus.NexusAssert.assertAxes;
import static org.eclipse.scanning.test.utilities.scan.nexus.NexusAssert.assertIndices;
import static org.eclipse.scanning.test.utilities.scan.nexus.NexusAssert.assertSolsticeScanGroup;
import static org.eclipse.scanning.test.utilities.scan.nexus.NexusAssert.assertTarget;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.measure.quantity.Angle;
import javax.measure.quantity.Energy;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
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
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusScanInfo.ScanRole;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.nexus.builder.impl.DefaultNexusBuilderFactory;
import org.eclipse.dawnsci.nexus.device.INexusDeviceService;
import org.eclipse.dawnsci.nexus.device.impl.NexusDeviceService;
import org.eclipse.dawnsci.nexus.scan.impl.NexusScanFileServiceImpl;
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
import org.w3c.dom.NodeList;

import gda.TestHelpers;
import gda.data.ServiceHolder;
import gda.data.scan.datawriter.NexusDataWriter;
import gda.data.scan.datawriter.scannablewriter.ScannableWriter;
import gda.data.scan.datawriter.scannablewriter.SingleScannableWriter;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.ScannableMotion;
import gda.device.ScannableMotionUnits;
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
 * setting {@link NexusDataWriter#setMetadatascannables(Set)} and
 * {@link NexusDataWriter#setLocationmap(Map)}.
 *
 * Additionally includes a test that {@link ScannableNexusWrapper}
 * adds attributes contributed using {@link Scannable#setScanMetadataAttribute(String, Object)}
 *
 * Note that we can't extends NexusTest as we use the real ScannableDeviceConnectorService
 * and LoaderServiceImpl instead of mocks.
 */
public class ScannableNexusWrapperScanTest {

	private static JythonServer jythonServer;

	@BeforeClass
	public static void setServices() throws Exception {
		jythonServer = mock(JythonServer.class);
		connector   = new ScannableDeviceConnectorService();
		sservice    = new RunnableDeviceServiceImpl(connector); // Not testing OSGi so using hard coded service.
		gservice    = new PointGeneratorService();
		fileFactory = new NexusFileFactoryHDF5();

		final ActivemqConnectorService activemqConnectorService = new ActivemqConnectorService();
		activemqConnectorService.setJsonMarshaller(new MarshallerService(new PointsModelMarshaller()));
		final IEventService eservice  = new EventServiceImpl(activemqConnectorService);

		final Services services = new Services();
		services.setEventService(eservice);
		services.setRunnableDeviceService(sservice);
		services.setGeneratorService(gservice);
		services.setConnector(connector);

		final INexusDeviceService nexusDeviceService = new NexusDeviceService();
		new org.eclipse.dawnsci.nexus.ServiceHolder().setNexusFileFactory(fileFactory);
		final org.eclipse.scanning.sequencer.ServiceHolder serviceHolder = new org.eclipse.scanning.sequencer.ServiceHolder();
		serviceHolder.setNexusDeviceService(nexusDeviceService);
		serviceHolder.setNexusScanFileService(new NexusScanFileServiceImpl());
		serviceHolder.setOperationService(new MockOperationService());
		serviceHolder.setFilePathService(new MockFilePathService());

		final org.eclipse.dawnsci.nexus.scan.ServiceHolder scanServiceHolder = new org.eclipse.dawnsci.nexus.scan.ServiceHolder();
		scanServiceHolder.setNexusDeviceService(nexusDeviceService);
		scanServiceHolder.setNexusBuilderFactory(new DefaultNexusBuilderFactory());
	}


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
			setInputNames(new String[0]);
			setExtraNames(new String[] { fieldName });
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

	private static class DummyEnergyScananble extends DummyUnitsScannable<Energy> {

		public DummyEnergyScananble(String name, double initialValue) throws Exception {
			super(name, initialValue, "GeV", "GeV");
			this.setLowerGdaLimits(0.1);
			this.setUpperGdaLimits(10000.0);
		}

	}

	private static class MultiFieldMetadataScannable extends DummyScannable {

		private final LinkedHashMap<String, Object> inputFieldValues = new LinkedHashMap<>();

		private final LinkedHashMap<String, Object> extraFieldValues = new LinkedHashMap<>();

		public MultiFieldMetadataScannable(String name) {
			setName(name);
			setInputNames(new String[0]);
			setExtraNames(new String[0]);
		}

		public void addInputField(String name, Object value) {
			inputFieldValues.put(name, value);
		}

		public void addExtraField(String name, Object value) {
			extraFieldValues.put(name, value);
		}

		@Override
		public String[] getInputNames() {
			if (inputFieldValues == null) return new String[0];
			return inputFieldValues.keySet().toArray(new String[inputFieldValues.size()]);
		}

		@Override
		public String[] getExtraNames() {
			if (extraFieldValues == null) return new String[0];
			return extraFieldValues.keySet().toArray(new String[extraFieldValues.size()]);
		}

		@Override
		public Object rawGetPosition() throws DeviceException {
			return Stream.concat(
					inputFieldValues.values().stream(),
					extraFieldValues.values().stream()).toArray();
		}

		@Override
		public void rawAsynchronousMoveTo(Object position) throws DeviceException {
			if (inputFieldValues.size() == 1) {
				final String key = inputFieldValues.keySet().iterator().next();
				inputFieldValues.put(key, position);
			} else {
				final Object[] positionArray = (Object[]) position;
				final Iterator<String> inputFieldNameIterator = inputFieldValues.keySet().iterator();
				for (int i = 0; i < positionArray.length; i++) {
					final String inputFieldName = inputFieldNameIterator.next();
					inputFieldValues.put(inputFieldName, positionArray[i]);
				}
			}
		}

	}

	public static final String TEST_CONFIG_FILE_PATH =
			"testfiles/gda/scanning/ScannableNexusWrapperScanTest/testdatawriter.xml";
	protected static IScannableDeviceService connector;
	protected static IScanService		     sservice;
	protected static IPointGeneratorService  gservice;
	protected static INexusFileFactory       fileFactory;


	protected File output;

	private IWritableDetector<MandelbrotModel> detector;
	private Map<String, ScannableWriter> locationMap;
	private Set<String> legacyMetadataScannables;

	@Before
	public void before() throws Exception {
		final MandelbrotModel model = new MandelbrotModel();
		model.setName("mandelbrot");
		model.setRealAxisName("salong");
		model.setImaginaryAxisName("saperp");

		detector = TestDetectorHelpers
				.createAndConfigureMandelbrotDetector(model);
		assertNotNull(detector);
		detector.addRunListener(new IRunListener() {

			@Override
			public void runPerformed(RunEvent event) throws ScanningException {
				System.out.println("Ran mandelbrot detector @ " + event.getPosition());
			}

		});

		readLegacySpringConfig(TEST_CONFIG_FILE_PATH);
		ServiceHolder.getNexusDataWriterConfiguration().setLocationMap(locationMap);
		ServiceHolder.getNexusDataWriterConfiguration().setMetadataScannables(legacyMetadataScannables);

		final Factory factory = TestHelpers.createTestFactory();
		factory.addFindable(jythonServer);
		factory.addFindable(new SampleAngleScannable("salong", false));
		factory.addFindable(new SampleAngleScannable("saperp", true));
		factory.addFindable(new DummyScannable("sax", 5.0));
		factory.addFindable(new DummyScannable("say", 23.4));
		factory.addFindable(new DummyScannable("satilt", 127.4));
		factory.addFindable(new DummyScannable("saazimuth", 24.32));
		factory.addFindable(new DummyScannable("s6_xsize", 2.92));
		factory.addFindable(new DummyScannable("s2_ysize", 8.34));
		factory.addFindable(new DummyScannable("s2_xsize", 1.66));
		factory.addFindable(new DummyScannable("exit_slit", 0.0683));
		factory.addFindable(new DummyEnergyScananble("pgm_cff", 123.45));
		factory.addFindable(new DummyScannable("energy", 9.357e8));
		factory.addFindable(new DummyScannable("pgm_linedensity", 28));
		factory.addFindable(new DummyScannable("ring_current", 15.2));
		factory.addFindable(new DummyScannable("ring_energy", 47.53));
		factory.addFindable(new DummyScannable("lc_pressure", 73.012));
		factory.addFindable(new DummyStringScannable("sample_name", "name", "test sample"));

		final MultiFieldMetadataScannable cryostat = new MultiFieldMetadataScannable("cryostat");
		cryostat.addInputField("temperature_demand", 20.0);
		cryostat.addExtraField("cryostat_temperature", 19.9);
		cryostat.addExtraField("temperatue", 19.9);
		cryostat.addExtraField("shield_temperature", 26.5);
		cryostat.addExtraField("heater_percent", 64.3);
		cryostat.addExtraField("heater_setting", 15.3);
		factory.addFindable(cryostat);

		final MultiFieldMetadataScannable id = new MultiFieldMetadataScannable("id");
		id.setScanMetadataAttribute(Scannable.ATTR_NX_CLASS, "NXinsertion_device");
		id.addExtraField("gap", 20);
		id.addExtraField("final_polarisation_label", "label");
		id.addExtraField("phase", 60);
		factory.addFindable(id);

		final DummyScannable attr = new DummyScannable("attributes", 2.5);
		attr.setInputNames(new String[] { "value" });
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

	private Map<String, ScannableWriter> readLegacyLocationMap(Element argumentsPropertyElement) {
		final Map<String, ScannableWriter> locationMap = new HashMap<>();
		final Element mapElement = getChildElements(argumentsPropertyElement, "map").get(0);
		for (final Element entryElement : getChildElements(mapElement, "entry")) {
			final String id = entryElement.getAttribute("key");
			final SingleScannableWriter scannableWriter = new SingleScannableWriter();

			final Element beanElement = getChildElements(entryElement, "bean").get(0);
			for (final Element propertyElement : getChildElements(beanElement, "property")) {
				final String propertyName = propertyElement.getAttribute("name");
				final NodeList valueElements = propertyElement.getElementsByTagName("value");
				List<String> values;
				final String valueStr = propertyElement.getAttribute("value");
				if (!StringUtils.isBlank(valueStr)) {
					values = Arrays.asList(valueStr);
				} else {
					values = new ArrayList<>(valueElements.getLength());
					for (int k = 0; k < valueElements.getLength(); k++) {
						final Element valueElement = (Element) valueElements.item(k);
						final String value = valueElement.getTextContent();
						values.add(value);
					}
				}

				if ("paths".equals(propertyName)) {
					scannableWriter.setPaths(values.toArray(new String[values.size()]));
				} else if ("units".equals(propertyName)) {
					scannableWriter.setUnits(values.toArray(new String[values.size()]));
				} else if ("prerequisiteScannableNames".equals(propertyName)) {
					scannableWriter.setPrerequisiteScannableNames(values);
				}
			}
			locationMap.put(id, scannableWriter);
		}

		return locationMap;
	}

	private Set<String> readLegacyMetadataScannables(Element argumentsPropertyElement) {
		final Set<String> metadataScannables = new HashSet<>();
		final String valueAttr = argumentsPropertyElement.getAttribute("value");
		if (!StringUtils.isBlank(valueAttr)) {
			metadataScannables.add(valueAttr);
		} else {
			final Element setElement = getChildElements(argumentsPropertyElement, "set").get(0);
			for (final Element valueElement : getChildElements(setElement, "value")) {
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
		assertEquals(DeviceState.ARMED, scanner.getDeviceState());
		final String filePath = ((AbstractRunnableDevice<ScanModel>) scanner).getModel().getFilePath();

		final NexusFile nf = org.eclipse.dawnsci.nexus.ServiceHolder.getNexusFileFactory().newNexusFile(filePath);
		nf.openToRead();

		final TreeFile nexusTree = NexusUtils.loadNexusTree(nf);
		final NXroot rootNode = (NXroot) nexusTree.getGroupNode();
		final NXentry entry = rootNode.getEntry();
		final NXinstrument instrument = entry.getInstrument();
		final NXsample sample = entry.getSample();

		// check the scan points have been written correctly
		assertSolsticeScanGroup(entry, false, false, sizes);

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
				scannableNames.stream().map(x -> x + "_" + FIELD_NAME_VALUE_SET),
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

			String nxDataFieldName = scannableName + "_" + FIELD_NAME_VALUE_SET;
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
						is(equalTo(scannableName + "." + valueFieldName)));
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
					nxDataFieldName = scannableName + "_" + valueFieldName;
					assertThat(nxData.getDataNode(nxDataFieldName), is(sameInstance(dataNode)));
					assertIndices(nxData, nxDataFieldName, defaultDimensionMappings);
					assertTarget(nxData, nxDataFieldName, rootNode,
							"/entry/instrument/" + (inLocationMap ? COLLECTION_NAME_SCANNABLES + "/" : "") +
							scannableName + "/" + valueFieldName);

					if (paths != null && paths.length > fieldIndex) {
						// check the same datanode can also be found at the path for this fieldname in the location map
						assertThat(valueFieldName, getDataNode(entry, paths[fieldIndex]), is(sameInstance(dataNode)));
						if (expectedUnits != null && expectedUnits.length > fieldIndex) {
							// check the units attribute has been written according to the location map
							final Attribute unitsAttribute = dataNode.getAttribute("units");
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

		fail("No scannable exists with name " + scannableName);
		return null; // never reached
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

			NXobject nexusObject = (NXobject) instrument.getGroupNode(metadataScannableName);
			if (locationMap.containsKey(metadataScannableName)) {
				// if there is an entry in the location map for this group, the nexus object
				// should have been added to a collection called 'scannables'
				assertThat(nexusObject, is(nullValue()));
				final NXcollection scannablesCollection = (NXcollection) instrument.getGroupNode(
						COLLECTION_NAME_SCANNABLES);
				assertThat(scannablesCollection, is(notNullValue()));
				nexusObject = (NXobject) scannablesCollection.getGroupNode(metadataScannableName);
			}

			// Check that the nexus object is of the expected base class
			assertThat(nexusObject, is(notNullValue()));
			final String expectedNexusBaseClass = (String) scannable.getScanMetadataAttribute(Scannable.ATTR_NX_CLASS);
			assertThat(nexusObject.getNexusBaseClass().toString(), is(equalTo(
					expectedNexusBaseClass == null ? NexusBaseClass.NX_POSITIONER.toString() : expectedNexusBaseClass)));

			assertThat(nexusObject.getGroupNodeNames(), is(empty()));
			assertThat(nexusObject.getNumberOfGroupNodes(), is(0));

			assertThat(nexusObject.getAttributeNames(),
					containsInAnyOrder(NXCLASS, ATTR_NAME_GDA_SCANNABLE_NAME, ATTR_NAME_GDA_SCAN_ROLE));
			assertThat(nexusObject.getNumberOfAttributes(), is(3));
			assertThat(nexusObject.getAttrString(null, ATTR_NAME_GDA_SCANNABLE_NAME),
					is(equalTo(metadataScannableName)));
			assertThat(nexusObject.getAttrString(null, ATTR_NAME_GDA_SCAN_ROLE),
					is(equalTo(ScanRole.MONITOR_PER_SCAN.toString().toLowerCase())));

			final String[] valueFieldNames = (String[]) ArrayUtils.addAll(
					scannable.getInputNames(), scannable.getExtraNames());
			if (nexusObject.getNexusBaseClass() == NexusBaseClass.NX_POSITIONER &&
					valueFieldNames[0].equals(scannable.getName())) {
				valueFieldNames[0] = NXpositioner.NX_VALUE;
			}

			final Set<String> expectedDataNodeNames = new HashSet<>();
			expectedDataNodeNames.addAll(scannable.getScanMetadataAttributeNames());
			expectedDataNodeNames.removeAll(Arrays.asList(Scannable.ATTR_NX_CLASS, Scannable.ATTR_NEXUS_CATEGORY));
			expectedDataNodeNames.addAll(Arrays.asList(NXpositioner.NX_NAME));
			expectedDataNodeNames.addAll(Arrays.asList(valueFieldNames));
			if (hasLimits(scannable)) {
				expectedDataNodeNames.addAll(Arrays.asList(NXpositioner.NX_SOFT_LIMIT_MIN, NXpositioner.NX_SOFT_LIMIT_MAX));
			}
			assertThat(nexusObject.getDataNodeNames(), containsInAnyOrder(expectedDataNodeNames.toArray()));
			assertThat(nexusObject.getNumberOfDataNodes(), is(expectedDataNodeNames.size()));

			final String expectedName = metadataScannableName.equals("sample_name") ? "test sample": metadataScannableName;
			assertThat(nexusObject.getString(NXpositioner.NX_NAME), is(equalTo(expectedName)));

			if (hasLimits(scannable) && nexusObject instanceof NXpositioner) {
				final NXpositioner positioner = (NXpositioner) nexusObject;
				assertThat(positioner.getSoft_limit_minScalar(),
						is(equalTo(((ScannableMotion) scannable).getLowerGdaLimits()[0])));
				assertThat(positioner.getSoft_limit_maxScalar(),
						is(equalTo(((ScannableMotion) scannable).getUpperGdaLimits()[0])));
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
					if (metadataScannableName.equals("sax") || metadataScannableName.equals("say")) {
						// special case, datasets in location map overwritten with
						// lazy writable dataset by salong and saperp
						assertThat(getDataNode(entry, paths[fieldIndex]), is(notNullValue()));
					} else {
						assertThat(getDataNode(entry, paths[fieldIndex]), is(sameInstance(dataNode)));
						if (expectedUnits != null && expectedUnits.length > fieldIndex && StringUtils.isNotBlank(expectedUnits[fieldIndex])) {
							final Attribute unitsAttribute = dataNode.getAttribute("units");
							assertThat(unitsAttribute, is(notNullValue()));
							assertThat(unitsAttribute.getFirstElement(), is(equalTo(expectedUnits[fieldIndex])));
						}
					}
				}
			}

			if (prerequisiteScannableNames != null) {
				for (final String prerequisiteScannableName : prerequisiteScannableNames) {
					assertThat(prerequisiteScannableName,
							metadataScannableNames.contains(prerequisiteScannableName), is(true));
				}
			}
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
		if(scannable instanceof ScannableMotion) {
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

		final IDataset extentDataset = beam.getDataset("extent"); // TODO use getExtent when Nexus base classes are next generated
		assertThat(extentDataset, is(notNullValue()));
		assertThat(extentDataset.getRank(), is(0));
		assertThat(extentDataset.getObject(), is(equalTo(3.25)));
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

	private DataNode getDataNode(NXentry entry, String augmentedPath) {
		GroupNode currentNode = entry;
		final Iterator<Pair<String, String>> parsedPathIter = parseAugmentedPath(augmentedPath).iterator();
		DataNode dataNode = null;
		while (parsedPathIter.hasNext()) {
			final Pair<String, String> parsedPathSegment = parsedPathIter.next();
			final String name = parsedPathSegment.getFirst();
			final String nxClass = parsedPathSegment.getSecond();

			if (parsedPathIter.hasNext()) {
				currentNode = currentNode.getGroupNode(name);
				assertThat(currentNode, is(notNullValue()));
				assertThat(currentNode, is(instanceOf(NXobject.class)));
				final NexusBaseClass expectedBaseClass = nxClass.equals("NXgoniometer") ?
						NexusBaseClass.NX_COLLECTION : NexusBaseClass.getBaseClassForName(nxClass);
				assertThat(expectedBaseClass, is(sameInstance(((NXobject) currentNode).getNexusBaseClass())));
			} else {
				// final segment
				assertThat(nxClass, is(nullValue()));
				dataNode = currentNode.getDataNode(name);
			}
		}

		assertThat(dataNode, is(notNullValue()));
		return dataNode;
	}

	private List<Pair<String, String>> parseAugmentedPath(String augmentedPath) {
		final String[] segments = augmentedPath.split(org.eclipse.dawnsci.analysis.api.tree.Node.SEPARATOR);
		final List<Pair<String, String>> parsedSegments = new ArrayList<Pair<String, String>>(segments.length);
		for (final String segment : segments) {
			final String[] pair = segment.split(NexusFile.NXCLASS_SEPARATOR, 2);
			parsedSegments.add(new Pair<>(pair[0], pair.length > 1 ? pair[1] : null));
		}

		return parsedSegments;
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
		final IPointGenerator<CompoundModel> pointGen = gservice.createCompoundGenerator(compoundModel);

		// Create the model for a scan
		final ScanModel scanModel = new ScanModel();
		scanModel.setPointGenerator(pointGen);
		scanModel.setScanPathModel(compoundModel);
		scanModel.setDetector(detector);

		final IScannable<?> attributeScannable = connector.getScannable("attributes");
		final IScannable<?> beamSizeScannable = connector.getScannable("beam");
		scanModel.setMonitorsPerScan(attributeScannable, beamSizeScannable);

		// Create a file to scan into
		final File output = File.createTempFile("test_legacy_nexus", ".nxs");
		output.deleteOnExit();
		scanModel.setFilePath(output.getAbsolutePath());
		System.out.println("File writing to " + scanModel.getFilePath());

		// Create a scan and run it without publishing events
		final IRunnableDevice<ScanModel> scanner = sservice.createScanDevice(scanModel);

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
