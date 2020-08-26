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

import static org.eclipse.scanning.test.utilities.scan.nexus.NexusAssert.assertAxes;
import static org.eclipse.scanning.test.utilities.scan.nexus.NexusAssert.assertIndices;
import static org.eclipse.scanning.test.utilities.scan.nexus.NexusAssert.assertSolsticeScanGroup;
import static org.eclipse.scanning.test.utilities.scan.nexus.NexusAssert.assertTarget;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static uk.ac.diamond.daq.scanning.ScannableNexusWrapper.ATTR_NAME_GDA_FIELD_NAME;
import static uk.ac.diamond.daq.scanning.ScannableNexusWrapper.ATTR_NAME_GDA_SCANNABLE_NAME;
import static uk.ac.diamond.daq.scanning.ScannableNexusWrapper.ATTR_NAME_GDA_SCAN_ROLE;
import static uk.ac.diamond.daq.scanning.ScannableNexusWrapper.ATTR_NAME_LOCAL_NAME;
import static uk.ac.diamond.daq.scanning.ScannableNexusWrapper.COLLECTION_NAME_SCANNABLES;
import static uk.ac.diamond.daq.scanning.ScannableNexusWrapper.FIELD_NAME_VALUE_SET;

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
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.InterfaceUtils;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
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
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.event.RunEvent;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.connector.activemq.ActivemqConnectorService;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.example.detector.ConstantVelocityDevice;
import org.eclipse.scanning.example.detector.ConstantVelocityModel;
import org.eclipse.scanning.example.detector.DarkImageDetector;
import org.eclipse.scanning.example.detector.DarkImageModel;
import org.eclipse.scanning.example.detector.MandelbrotDetector;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.example.file.MockFilePathService;
import org.eclipse.scanning.points.PointGeneratorService;
import org.eclipse.scanning.points.serialization.PointsModelMarshaller;
import org.eclipse.scanning.sequencer.RunnableDeviceServiceImpl;
import org.eclipse.scanning.server.servlet.Services;
import org.eclipse.scanning.test.utilities.scan.mock.MockDetectorModel;
import org.eclipse.scanning.test.utilities.scan.mock.MockOperationService;
import org.eclipse.scanning.test.utilities.scan.mock.MockWritableDetector;
import org.eclipse.scanning.test.utilities.scan.mock.MockWritingMandelbrotDetector;
import org.eclipse.scanning.test.utilities.scan.mock.MockWritingMandlebrotModel;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
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
import gda.device.scannable.DummyScannable;
import gda.device.scannable.ScannableBase;
import gda.factory.Factory;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;

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
@Ignore("DAQ-3089 Temporarily skip due to failures")
public class ScannableNexusWrapperScanTest {

	@BeforeClass
	public static void setServices() throws Exception {

		connector   = new ScannableDeviceConnectorService();
		dservice    = new RunnableDeviceServiceImpl(connector); // Not testing OSGi so using hard coded service.
		gservice    = new PointGeneratorService();
		fileFactory = new NexusFileFactoryHDF5();

		final ActivemqConnectorService activemqConnectorService = new ActivemqConnectorService();
		activemqConnectorService.setJsonMarshaller(new MarshallerService(new PointsModelMarshaller()));
		IEventService eservice  = new EventServiceImpl(activemqConnectorService);

		IRunnableDeviceService dservice  = new RunnableDeviceServiceImpl(connector);
		RunnableDeviceServiceImpl impl = (RunnableDeviceServiceImpl)dservice;
		impl._register(MockDetectorModel.class, MockWritableDetector.class);
		impl._register(MockWritingMandlebrotModel.class, MockWritingMandelbrotDetector.class);
		impl._register(MandelbrotModel.class, MandelbrotDetector.class);
		impl._register(ConstantVelocityModel.class, ConstantVelocityDevice.class);
		impl._register(DarkImageModel.class, DarkImageDetector.class);

		final Services services = new Services();
		services.setEventService(eservice);
		services.setRunnableDeviceService(dservice);
		services.setGeneratorService(gservice);
		services.setConnector(connector);

		final INexusDeviceService nexusDeviceService = new NexusDeviceService();
		new org.eclipse.dawnsci.nexus.ServiceHolder().setNexusFileFactory(fileFactory);
		org.eclipse.scanning.sequencer.ServiceHolder serviceHolder = new org.eclipse.scanning.sequencer.ServiceHolder();
		serviceHolder.setNexusDeviceService(nexusDeviceService);
		serviceHolder.setOperationService(new MockOperationService());
		serviceHolder.setFilePathService(new MockFilePathService());

		final org.eclipse.dawnsci.nexus.scan.ServiceHolder serviceHolder2 = new org.eclipse.dawnsci.nexus.scan.ServiceHolder();
		serviceHolder2.setNexusDeviceService(nexusDeviceService);
		serviceHolder2.setNexusBuilderFactory(new DefaultNexusBuilderFactory());
	}


	private static class SampleAngleScannable extends DummyScannable {

		private static final double ANGLE = Math.toRadians(30);

		private final boolean perp;

		public SampleAngleScannable(String name, boolean perp) {
			super(name);
			setExtraNames(new String[] { "sax", "say" });
			this.perp = perp;
		}

		@Override
		public Object rawGetPosition() throws DeviceException {
			double pos = ((Double) super.rawGetPosition()).doubleValue();
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

	private static class MultiFieldMetadataScannable extends DummyScannable {

		private LinkedHashMap<String, Object> inputFieldValues = new LinkedHashMap<>();

		private LinkedHashMap<String, Object> extraFieldValues = new LinkedHashMap<>();

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
				String key = inputFieldValues.keySet().iterator().next();
				inputFieldValues.put(key, position);
			} else {
				Object[] positionArray = (Object[]) position;
				Iterator<String> inputFieldNameIterator = inputFieldValues.keySet().iterator();
				for (int i = 0; i < positionArray.length; i++) {
					String inputFieldName = inputFieldNameIterator.next();
					inputFieldValues.put(inputFieldName, positionArray[i]);
				}
			}
		}

	}

	public static final String TEST_CONFIG_FILE_PATH =
			"testfiles/gda/scanning/ScannableNexusWrapperScanTest/testdatawriter.xml";
	protected static IScannableDeviceService connector;
	protected static IRunnableDeviceService  dservice;
	protected static IPointGeneratorService  gservice;
	protected static INexusFileFactory       fileFactory;


	protected File output;

	private IWritableDetector<MandelbrotModel> detector;
	private Map<String, ScannableWriter> locationMap;
	private Set<String> legacyMetadataScannables;

	@Before
	public void before() throws Exception {
		MandelbrotModel model = new MandelbrotModel();
		model.setName("mandelbrot");
		model.setRealAxisName("salong");
		model.setImaginaryAxisName("saperp");

		detector = (IWritableDetector<MandelbrotModel>) dservice.createRunnableDevice(model);
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
		factory.addFindable(new DummyScannable("pgm_cff", 123.23432));
		factory.addFindable(new DummyScannable("energy", 9.357e8));
		factory.addFindable(new DummyScannable("pgm_linedensity", 28));
		factory.addFindable(new DummyScannable("ring_current", 15.2));
		factory.addFindable(new DummyScannable("ring_energy", 47.53));
		factory.addFindable(new DummyScannable("lc_pressure", 73.012));
		factory.addFindable(new DummyStringScannable("sample_name", "name", "test sample"));

		MultiFieldMetadataScannable cryostat = new MultiFieldMetadataScannable("cryostat");
		cryostat.addInputField("temperature_demand", 20.0);
		cryostat.addExtraField("cryostat_temperature", 19.9);
		cryostat.addExtraField("temperatue", 19.9);
		cryostat.addExtraField("shield_temperature", 26.5);
		cryostat.addExtraField("heater_percent", 64.3);
		cryostat.addExtraField("heater_setting", 15.3);
		factory.addFindable(cryostat);

		MultiFieldMetadataScannable id = new MultiFieldMetadataScannable("id");
		id.setScanMetadataAttribute(Scannable.ATTR_NX_CLASS, "NXinsertion_device");
		id.addExtraField("gap", 20);
		id.addExtraField("final_polarisation_label", "label");
		id.addExtraField("phase", 60);
		factory.addFindable(id);

		DummyScannable attr = new DummyScannable("attributes", 2.5);
		attr.setInputNames(new String[] { "value" });
		attr.setScanMetadataAttribute("stringAttr", "foo");
		attr.setScanMetadataAttribute("doubleAttr", 123.456);
		factory.addFindable(attr);

		DummyScannable beam = new DummyScannable("beam", 3.25);
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
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new File(path));
		Element rootElement = document.getDocumentElement();
		rootElement.normalize();

		for (Element beanElement : getChildElements(rootElement, "bean")) {
			String classAttr = beanElement.getAttribute("class");
			// only process beans with class MethodInvokingFactoryBean
			if ("org.springframework.beans.factory.config.MethodInvokingFactoryBean".equals(classAttr)) {
				String staticMethod = null;
				Element argumentsPropertyElement = null;
				for (Element propertyElement : getChildElements(beanElement, "property")) {
					String propertyName = propertyElement.getAttribute("name");
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
		int[] size = (outerScannableName == null) ? new int[] { 8, 5 } : new int[] { 2, 5, 3 };
		IRunnableDevice<ScanModel> scanner = createGridScan(detector, outerScannableName, size);
		scanner.run(null);
		checkNexusFile(scanner, size);
	}

	@SuppressWarnings("unused")
	private void printLocationMap(Map<String, ScannableWriter> locationMap) {
		for (Map.Entry<String, ScannableWriter> entry : locationMap.entrySet()) {
			System.err.println("scannable name: " + entry.getKey());
			SingleScannableWriter scannableWriter = (SingleScannableWriter) entry.getValue();
			String[] paths = scannableWriter.getPaths();
			for (int i = 0; i < paths.length; i++) {
				System.err.println("paths[" + i + "] = " + paths[i]);
			}
			String[] units = scannableWriter.getUnits();
			if (units == null) {
				System.err.println("no units");
			} else {
				for (int i = 0; i < units.length; i++) {
					System.err.println("units[" + i + "] = " + units[i]);
				}
			}
			Collection<String> preqrequisites = scannableWriter.getPrerequisiteScannableNames();
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
		Map<String, ScannableWriter> locationMap = new HashMap<>();
		Element mapElement = getChildElements(argumentsPropertyElement, "map").get(0);
		for (Element entryElement : getChildElements(mapElement, "entry")) {
			final String id = entryElement.getAttribute("key");
			final SingleScannableWriter scannableWriter = new SingleScannableWriter();

			Element beanElement = getChildElements(entryElement, "bean").get(0);
			for (Element propertyElement : getChildElements(beanElement, "property")) {
				String propertyName = propertyElement.getAttribute("name");
				NodeList valueElements = propertyElement.getElementsByTagName("value");
				List<String> values;
				String valueStr = propertyElement.getAttribute("value");
				if (!StringUtils.isBlank(valueStr)) {
					values = Arrays.asList(valueStr);
				} else {
					values = new ArrayList<>(valueElements.getLength());
					for (int k = 0; k < valueElements.getLength(); k++) {
						Element valueElement = (Element) valueElements.item(k);
						String value = valueElement.getTextContent();
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
		Set<String> metadataScannables = new HashSet<>();
		String valueAttr = argumentsPropertyElement.getAttribute("value");
		if (!StringUtils.isBlank(valueAttr)) {
			metadataScannables.add(valueAttr);
		} else {
			Element setElement = getChildElements(argumentsPropertyElement, "set").get(0);
			for (Element valueElement : getChildElements(setElement, "value")) {
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
		String filePath = ((AbstractRunnableDevice<ScanModel>) scanner).getModel().getFilePath();

		NexusFile nf = org.eclipse.dawnsci.nexus.ServiceHolder.getNexusFileFactory().newNexusFile(filePath);
		nf.openToRead();

		TreeFile nexusTree = NexusUtils.loadNexusTree(nf);
		NXroot rootNode = (NXroot) nexusTree.getGroupNode();
		NXentry entry = rootNode.getEntry();
		NXinstrument instrument = entry.getInstrument();
		NXsample sample = entry.getSample();

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

		String dataGroupName = scanModel.getDetectors().get(0).getName();
		NXdata nxData = entry.getData(dataGroupName);
		assertNotNull(nxData);

		// Check axes
		List<String> expectedAxesNames = Stream.concat(
				scannableNames.stream().map(x -> x + "_" + FIELD_NAME_VALUE_SET),
				Arrays.asList("real", "imaginary").stream()).collect(Collectors.toList());
		assertAxes(nxData, expectedAxesNames.toArray(new String[expectedAxesNames.size()]));

		int[] defaultDimensionMappings = IntStream.range(0, sizes.length).toArray();
		int scannableIndex = -1;
		for (String scannableName : scannableNames) {
			scannableIndex++;
			NXpositioner positioner = instrument.getPositioner(scannableName);
			boolean inLocationMap = locationMap.containsKey(scannableName);
			if (inLocationMap) {
				NXcollection scannablesCollection = (NXcollection) instrument.getGroupNode(
						COLLECTION_NAME_SCANNABLES);
				assertNotNull(scannablesCollection);
				positioner = (NXpositioner) scannablesCollection.getGroupNode(scannableName);
			}

			assertNotNull(positioner);

			assertEquals(0, positioner.getNumberOfGroupNodes());
			assertEquals(3, positioner.getNumberOfAttributes()); // NXclass, gda_scannable_name, gda_scan_role

			assertEquals(scannableName, positioner.getNameScalar());
			assertEquals(scannableName, positioner.getAttrString(null, ATTR_NAME_GDA_SCANNABLE_NAME));
			assertEquals(ScanRole.SCANNABLE.toString().toLowerCase(),
					positioner.getAttrString(null, ATTR_NAME_GDA_SCAN_ROLE));

			// Demand values should be 1D
			dataNode = positioner.getDataNode(FIELD_NAME_VALUE_SET);
			dataset = dataNode.getDataset().getSlice();
			shape = dataset.getShape();
			assertEquals(1, shape.length);
			assertEquals(sizes[scannableIndex], shape[0]);

			String nxDataFieldName = scannableName + "_" + FIELD_NAME_VALUE_SET;
			assertSame(dataNode, nxData.getDataNode(nxDataFieldName));
			assertIndices(nxData, nxDataFieldName, scannableIndex);
			assertTarget(nxData, nxDataFieldName, rootNode,
					"/entry/instrument/" + (inLocationMap ? COLLECTION_NAME_SCANNABLES + "/" : "") +
					scannableName + "/" + FIELD_NAME_VALUE_SET);

			String[] paths = null;
			String[] units = null;
			if (locationMap.containsKey(scannableName)) {
				SingleScannableWriter writer = (SingleScannableWriter) locationMap.get(scannableName);
				paths = writer.getPaths();
				units = writer.getUnits();
			}

			// Actual values should be scanD
			Scannable legacyScannable = getScannable(scannableName);
			List<String> inputFieldNames = new ArrayList<>();
			inputFieldNames.addAll(Arrays.asList(legacyScannable.getInputNames()));
			inputFieldNames.addAll(Arrays.asList(legacyScannable.getExtraNames()));
			List<String> outputFieldNames = new ArrayList<>(inputFieldNames);
			if (outputFieldNames.contains(scannableName)) {
				outputFieldNames.set(outputFieldNames.indexOf(scannableName), NXpositioner.NX_VALUE);
			}

			// check the number of data nodes, num fields of legacy scannable + name + demand_value
			assertEquals(outputFieldNames.size() + 2, positioner.getNumberOfDataNodes());

			for (int fieldIndex = 0; fieldIndex < outputFieldNames.size(); fieldIndex++) {
				final String valueFieldName = outputFieldNames.get(fieldIndex);
				dataNode = positioner.getDataNode(valueFieldName);

				assertEquals(valueFieldName, positioner.getAttrString(valueFieldName, ATTR_NAME_LOCAL_NAME),
						scannableName + "." + valueFieldName);
				assertEquals(valueFieldName, positioner.getAttrString(valueFieldName, ATTR_NAME_GDA_FIELD_NAME),
						inputFieldNames.get(fieldIndex));

				dataset = dataNode.getDataset().getSlice();
				shape = dataset.getShape();
				assertArrayEquals(sizes, shape);

				if (fieldIndex == 0) {
					// currently only the first field of a Scannable is linked to from an NXdata group,
					// this is probably incorrect, see JIRA DAQ-311
					nxDataFieldName = scannableName + "_" + valueFieldName;
					assertSame(dataNode, nxData.getDataNode(nxDataFieldName));
					assertIndices(nxData, nxDataFieldName, defaultDimensionMappings);
					assertTarget(nxData, nxDataFieldName, rootNode,
							"/entry/instrument/" + (inLocationMap ? COLLECTION_NAME_SCANNABLES + "/" : "") +
							scannableName + "/" + valueFieldName);

					if (paths != null && paths.length > fieldIndex) {
						// check the same datanode can also be found at the path for this fieldname in the location map
						assertSame(dataNode, getDataNode(entry, paths[fieldIndex]));
						if (units != null && units.length > fieldIndex) {
							// check the units attribute has been written according to the location map
							Attribute unitsAttribute = dataNode.getAttribute("units");
							assertNotNull(unitsAttribute);
							assertEquals(units[fieldIndex], unitsAttribute.getFirstElement());
						}
					}
				}
			}
		}
	}

	private Scannable getScannable(String scannableName) {
		Findable found = Finder.find(scannableName);
		if (found instanceof Scannable && !(found instanceof Detector)) {
			return (Scannable) found;
		}

		Object jythonObj = InterfaceProvider.getJythonNamespace().getFromJythonNamespace(scannableName);
		if (jythonObj instanceof Scannable && !(jythonObj instanceof Detector)) {
			return (Scannable) jythonObj;
		}

		fail("No scannable exists with name " + scannableName);
		return null; // never reached
	}

	private void checkMetadataScannables(final ScanModel scanModel, NXentry entry) throws Exception {
		DataNode dataNode;
		IDataset dataset;
		NXinstrument instrument = entry.getInstrument();

		Collection<IScannable<?>> perScan  = scanModel.getMonitorsPerScan();
		Set<String> metadataScannableNames = perScan.stream().map(ms -> ms.getName()).collect(Collectors.toSet());

		Set<String> expectedMetadataScannableNames = new HashSet<>(legacyMetadataScannables);
		Set<String> scannableNamesToCheck = new HashSet<>(expectedMetadataScannableNames);
		do {
			Set<String> addedScannableNames = new HashSet<>();
			for (String metadataScannableName : scannableNamesToCheck) {
				Collection<String> reqdScannableNames = locationMap.get(metadataScannableName).getPrerequisiteScannableNames();
				if (reqdScannableNames != null && !reqdScannableNames.isEmpty()) {
					for (String reqdScannableName : reqdScannableNames) {
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
		List<String> scannableNames = ((AbstractPosition) scanModel.getPointGenerator().iterator().next()).getNames();
		for (String legacyMetadataScannableName : expectedMetadataScannableNames) {
			assertTrue(legacyMetadataScannableName, metadataScannableNames.contains(legacyMetadataScannableName)
					|| scannableNames.contains(legacyMetadataScannableName));
		}

		// check each metadata scannable has been written correctly
		for (String metadataScannableName : metadataScannableNames) {
			Scannable scannable = Finder.find(metadataScannableName);
			if (scannable.getScanMetadataAttribute(Scannable.ATTR_NEXUS_CATEGORY) != null) {
				// the nexus object for a scannable with a nexus category won't be under NXinstrument
				continue;
			}

			NXobject nexusObject = (NXobject) instrument.getGroupNode(metadataScannableName);

			if (locationMap.containsKey(metadataScannableName)) {
				// if there is an entry in the location map for this group, the nexus object
				// should have been added to a collection called 'scannables'
				assertNull(nexusObject);
				NXcollection scannablesCollection = (NXcollection) instrument.getGroupNode(
						COLLECTION_NAME_SCANNABLES);
				assertNotNull(scannablesCollection);
				nexusObject = (NXobject) scannablesCollection.getGroupNode(metadataScannableName);
			}

			// Check that the nexus object is of the expected base class
			assertNotNull(nexusObject);
			String expectedNexusBaseClass = (String) scannable.getScanMetadataAttribute(Scannable.ATTR_NX_CLASS);
			assertEquals(expectedNexusBaseClass == null ? "NXpositioner" : expectedNexusBaseClass,
					nexusObject.getNexusBaseClass().toString());

			if (metadataScannableName.equals("sample_name")) {
				assertEquals("test sample", nexusObject.getString("name"));
			} else {
				assertEquals(metadataScannableName, nexusObject.getString("name"));
			}

			assertEquals(0, nexusObject.getNumberOfGroupNodes());
			assertEquals(3, nexusObject.getNumberOfAttributes());

			assertEquals(metadataScannableName, nexusObject.getAttrString(null, ATTR_NAME_GDA_SCANNABLE_NAME));
			assertEquals(ScanRole.MONITOR_PER_SCAN.toString().toLowerCase(),
					nexusObject.getAttrString(null, ATTR_NAME_GDA_SCAN_ROLE));

			final String[] valueFieldNames = (String[]) ArrayUtils.addAll(
					scannable.getInputNames(), scannable.getExtraNames());
			if (nexusObject.getNexusBaseClass() == NexusBaseClass.NX_POSITIONER &&
					valueFieldNames[0] == scannable.getName()) {
				valueFieldNames[0] = NXpositioner.NX_VALUE;
			}

			final Object[] positionArray = getPositionArray(scannable);
			String[] paths = null;
			String[] units = null;
			Collection<String> prerequisiteScannableNames = Collections.emptyList();
			if (locationMap.containsKey(metadataScannableName)) {
				SingleScannableWriter writer = (SingleScannableWriter) locationMap.get(metadataScannableName);
				paths = writer.getPaths();
				units = writer.getUnits();
				writer.getPrerequisiteScannableNames();
			}

			// check each field is written both inside the nexus object for the metadata scannable
			for (int fieldIndex = 0; fieldIndex < valueFieldNames.length; fieldIndex++) {
				final String valueFieldName = valueFieldNames[fieldIndex];
				dataNode = nexusObject.getDataNode(valueFieldName);
				assertNotNull(dataNode);
				dataset = dataNode.getDataset().getSlice();
				assertEquals(0, dataset.getRank());
				assertEquals(positionArray[fieldIndex], dataset.getObject());

				if (paths != null && paths.length > fieldIndex) {
					// and to the location referred to by the location map
					if (metadataScannableName.equals("sax") || metadataScannableName.equals("say")) {
						// special case, datasets in location map overwritten with
						// lazy writable dataset by salong and saperp
						assertNotNull(getDataNode(entry, paths[fieldIndex]));
					} else {
						assertSame(dataNode, getDataNode(entry, paths[fieldIndex]));
						if (units != null && units.length > fieldIndex && StringUtils.isNotBlank(units[fieldIndex])) {
							Attribute unitsAttribute = dataNode.getAttribute("units");
							assertNotNull(unitsAttribute);
							assertEquals(units[fieldIndex], unitsAttribute.getFirstElement());
						}
					}
				}
			}

			if (prerequisiteScannableNames != null) {
				for (String prerequisiteScannableName : prerequisiteScannableNames) {
					assertTrue(prerequisiteScannableName, metadataScannableNames.contains(prerequisiteScannableName));
				}
			}
		}
	}

	private void checkAttributeScannable(NXinstrument instrument) throws Exception {
		Scannable attributeScannable = Finder.find("attributes");
		NXpositioner positioner = instrument.getPositioner(attributeScannable.getName());
		assertNotNull(positioner);

		for (String attrName : attributeScannable.getScanMetadataAttributeNames()) {
			DataNode dataNode = positioner.getDataNode(attrName);
			assertNotNull(dataNode);
			IDataset dataset = dataNode.getDataset().getSlice();
			assertEquals(0, dataset.getRank());
			Object expectedValue = attributeScannable.getScanMetadataAttribute(attrName);
			assertTrue(InterfaceUtils.getInterface(expectedValue).isInstance(dataset));
			assertEquals(expectedValue, dataset.getObject());
		}
	}

	private void checkBeamSizeScannable(NXsample sample) throws Exception {
		NXbeam beam = sample.getBeam();
		assertNotNull(beam);

		assertEquals(0, beam.getNumberOfGroupNodes());
		assertEquals(3, beam.getNumberOfAttributes());
		assertEquals("beam", beam.getAttrString(null, ATTR_NAME_GDA_SCANNABLE_NAME));
		assertEquals(ScanRole.MONITOR_PER_SCAN.toString().toLowerCase(),
				beam.getAttrString(null, ATTR_NAME_GDA_SCAN_ROLE));
		assertEquals(NexusBaseClass.NX_BEAM, beam.getNexusBaseClass());

		IDataset extentDataset = beam.getDataset("extent"); // TODO use getExtent when Nexus base classes are next generated
		assertNotNull(extentDataset);
		assertEquals(0, extentDataset.getRank());
		assertEquals(3.25, extentDataset.getObject());
	}

	private Object[] getPositionArray(Scannable legacyScannable) throws DeviceException {
		final Object position = legacyScannable.getPosition();
		if (!position.getClass().isArray()) {
			return new Object[] { position };
		}

		if (position.getClass().getComponentType().isPrimitive()) {
			final int size = Array.getLength(position);
			Object[] outputArray = new Object[size];
			for (int i = 0; i < size; i++) {
				outputArray[i] = Array.get(position, i);
			}
			return outputArray;
		}

		return (Object[]) position;
	}

	private DataNode getDataNode(NXentry entry, String augmentedPath) {
		GroupNode currentNode = entry;
		Iterator<Pair<String, String>> parsedPathIter = parseAugmentedPath(augmentedPath).iterator();
		DataNode dataNode = null;
		while (parsedPathIter.hasNext()) {
			Pair<String, String> parsedPathSegment = parsedPathIter.next();
			final String name = parsedPathSegment.getFirst();
			final String nxClass = parsedPathSegment.getSecond();

			if (parsedPathIter.hasNext()) {
				currentNode = currentNode.getGroupNode(name);
				assertNotNull(currentNode);
				assertTrue(currentNode instanceof NXobject);
				NexusBaseClass expectedBaseClass = nxClass.equals("NXgoniometer") ?
						NexusBaseClass.NX_COLLECTION : NexusBaseClass.getBaseClassForName(nxClass);
				assertSame(expectedBaseClass, ((NXobject) currentNode).getNexusBaseClass());
			} else {
				// final segment
				assertNull(nxClass);
				dataNode = currentNode.getDataNode(name);
			}
		}

		assertNotNull(dataNode);
		return dataNode;
	}

	private List<Pair<String, String>> parseAugmentedPath(String augmentedPath) {
		String[] segments = augmentedPath.split(org.eclipse.dawnsci.analysis.api.tree.Node.SEPARATOR);
		List<Pair<String, String>> parsedSegments = new ArrayList<Pair<String, String>>(segments.length);
		for (String segment : segments) {
			String[] pair = segment.split(NexusFile.NXCLASS_SEPARATOR, 2);
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
		final IRunnableDevice<ScanModel> scanner = dservice.createRunnableDevice(scanModel, null);

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
