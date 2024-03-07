/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package gda.data.scan.datawriter;

import static gda.data.scan.nexus.device.DummyNexusDetector.COLLECTION_NAME;
import static gda.data.scan.nexus.device.DummyNexusDetector.DETECTOR_NUMBER;
import static gda.data.scan.nexus.device.DummyNexusDetector.DIAMETER;
import static gda.data.scan.nexus.device.DummyNexusDetector.DIAMETER_UNITS;
import static gda.data.scan.nexus.device.DummyNexusDetector.FIELD_NAME_EXTERNAL;
import static gda.data.scan.nexus.device.DummyNexusDetector.FIELD_NAME_IMAGE_X;
import static gda.data.scan.nexus.device.DummyNexusDetector.FIELD_NAME_IMAGE_Y;
import static gda.data.scan.nexus.device.DummyNexusDetector.FIELD_NAME_SPECTRUM;
import static gda.data.scan.nexus.device.DummyNexusDetector.FIELD_NAME_VALUE;
import static gda.data.scan.nexus.device.DummyNexusDetector.SERIAL_NUMBER;
import static gda.data.scan.nexus.device.GDADeviceNexusConstants.ATTRIBUTE_NAME_LOCAL_NAME;
import static gda.data.scan.nexus.device.GDADeviceNexusConstants.ATTRIBUTE_NAME_TARGET;
import static gda.data.scan.nexus.device.GDADeviceNexusConstants.ATTRIBUTE_NAME_UNITS;
import static gda.device.scannable.ScannableUtils.convertToJava;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.eclipse.dawnsci.nexus.NexusConstants.DATA_INDICES_SUFFIX;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertAxes;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertDataNodesEqual;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertSignal;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

import java.io.File;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Length;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXnote;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.nexus.test.utilities.NexusTestUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DateDataset;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.IndexIterator;
import org.eclipse.january.dataset.PositionIterator;
import org.eclipse.january.dataset.Random;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.python.core.PyFloat;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;

import gda.TestHelpers;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.metadata.StoredMetadataEntry;
import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.data.scan.datawriter.scannablewriter.ScannableWriter;
import gda.data.scan.datawriter.scannablewriter.SingleScannableWriter;
import gda.data.scan.nexus.device.DummyNexusDetector;
import gda.data.scan.nexus.device.SimpleDummyNexusDetector;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.DummyDetector;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NexusDetector;
import gda.device.detector.countertimer.DummyCounterTimer;
import gda.device.monitor.DummyMonitor;
import gda.device.scannable.DummyMultiFieldUnitsScannable;
import gda.device.scannable.DummyScannable;
import gda.device.scannable.DummyUnitsScannable;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import gda.scan.ConcurrentScan;
import gda.scan.IScanDataPoint;
import uk.ac.diamond.daq.scanning.DummyStringScannable;
import uk.ac.diamond.osgi.services.ServiceProvider;

public abstract class AbstractNexusDataWriterScanTest {

	public enum PrimaryDeviceType {
		NONE(false),
		SINGLE_FIELD_MONITOR(false, SINGLE_FIELD_MONITOR_NAME),
		MULTI_FIELD_MONITOR(false, MULTI_FIELD_MONITOR_FIELD_NAMES[0]),
		NEXUS_DEVICE(true, NXdetector.NX_DATA),
		COUNTER_TIMER(true, COUNTER_TIMER_NAMES[COUNTER_TIMER_NAMES.length - 1]),
		GENERIC(true, NXdetector.NX_DATA),
		FILE_CREATOR(true),
		SIMPLE_NEXUS_DETECTOR(true, NXdetector.NX_DATA),
		NO_DATA_NEXUS_DETECTOR(true),

		/**
		 * Explicitly non-alphabetical, non-order of attachment to test prioritising of NexusGroupData
		 */
		NEXUS_DETECTOR(true) {
			@Override
			public List<String> getPrimaryFieldNames(Scannable primaryDevice) {
				return ((DummyNexusDetector) primaryDevice).getPrimaryFieldNames();
			}
		};

		/** Is this device a detector **/
		private final boolean isDetector;

		/** Names of primary fields defining datasets **/
		private final List<String> primaryFieldNames;

		PrimaryDeviceType(boolean isDetector, String... primaryFieldNames) {
			this.isDetector = isDetector;
			this.primaryFieldNames = List.of(primaryFieldNames);
		}

		public List<String> getPrimaryFieldNames(@SuppressWarnings("unused") Scannable primaryDevice) {
			return primaryFieldNames;
		}

		public boolean isDetector() {
			return isDetector;
		}
	}

	/**
	 * A generic detector that extends DummyDetector to return an fixed-length double array.
	 * Used by {@link AbstractNexusDataWriterScanTest#concurrentScanGenericDetector_scalarData(int)}
	 * and {@link AbstractNexusDataWriterScanTest#concurrentScanGenericDetector_arrayData(int)}
	 */
	protected static class DummyGenericDetector extends DummyDetector {

		private int dataSize;

		public DummyGenericDetector(int dataSize) {
			this.dataSize = dataSize;
		}

		@Override
		protected Object acquireData() {
			if (dataSize == 1) {
				return random.nextDouble(); // return as a scalar value
			}

			return random.doubles(dataSize, 0, getMaxDataValue()).toArray();
		}

		@Override
		public int[] getDataDimensions() throws DeviceException {
			return new int[] { dataSize };
		}

		@Override
		public String getDescription() throws DeviceException {
			return "An example generic detector";
		}

		@Override
		public String getDetectorID() throws DeviceException {
			return "gen1";
		}

		@Override
		public String getDetectorType() throws DeviceException {
			return "generic";
		}

	}

	/**
	 * A detector that write its own data (or rather claims to by {@link #createsOwnFiles()} returning true),
	 * meaning that its data doesn't need to be written by the data writer. Used by
	 * {@link AbstractNexusDataWriterScanTest#concurrentScanFileCreatorDetector(int)}
	 */
	protected static class DummyFileCreatorDetector extends DummyDetector {

		private int fileNum = 1;

		@Override
		public boolean createsOwnFiles() throws DeviceException {
			return true;
		}

		@Override
		protected Object acquireData() {
			return "file" + (fileNum++) + ".tif";
		}

		@Override
		public int[] getDataDimensions() throws DeviceException {
			return EMPTY_SHAPE;
		}

		@Override
		public String getDescription() throws DeviceException {
			return "File Creating Detector";
		}

		@Override
		public String getDetectorID() throws DeviceException {
			return "fileDet1";
		}

		@Override
		public String getDetectorType() throws DeviceException {
			return "file creator";
		}

	}

	/**
	 * A simple detector that extends DummyDetector to return an image as a two-dimensional DoubleDataset.
	 * Used by {@link NexusScanDataWriterScanTest#concurrentScanRegisteredNexusDevice(int)}
	 */
	protected static class DummyImageDetector extends DummyDetector {

		private static final int[] IMAGE_SIZE = new int[] { 8, 8 }; // small image size for tests

		@Override
		protected Object acquireData() {
			// override DummyDetector to return an image (a 2d DoubleDataset)
			return Random.rand(IMAGE_SIZE);
		}

		@Override
		public int[] getDataDimensions() throws DeviceException {
			return IMAGE_SIZE;
		}

	}

	protected static class NoDataNexusDetector extends DummyDetector implements NexusDetector {

		public static final String DETECTOR_NAME = "nonWritingNexusDet";
		public static final String FIELD_NAME_VALUE = "value";
		public static final long DETECTOR_NUMBER = 7L;
		public static final String DESCRIPTION = "A NexusDetector with no primary fields";

		protected NoDataNexusDetector() {
			setName(DETECTOR_NAME);
		}

		@Override
		public NexusTreeProvider readout() throws DeviceException {
			return (NexusTreeProvider) super.readout();
		}

		@Override
		protected Object acquireData() {
			final NXDetectorData data = new NXDetectorData(this);

			// add a couple of metadata fields
			final INexusTree detTree = data.getDetTree(getName());
			detTree.addChildNode(new NexusTreeNode(NXdetector.NX_DETECTOR_NUMBER, NexusExtractor.SDSClassName, detTree,
					new NexusGroupData(DETECTOR_NUMBER)));
			detTree.addChildNode(new NexusTreeNode(NXdetector.NX_DESCRIPTION, NexusExtractor.SDSClassName, detTree,
					new NexusGroupData(DESCRIPTION)));

			return data;
		}

	}

	private static final String TEMPLATE_FILE_PATH = "testfiles/gda/scan/datawriter/simple-template.yaml";
	protected static final String METADATA_KEY_FEDERAL_ID = "federalid";
	protected static final String METADATA_KEY_VISIT_ID = "visit";
	protected static final String METADATA_KEY_INSTRUMENT = "instrument";

	protected static final String[] METADATA_SCANNABLE_NAMES = // names start from 0, so they match the index
			IntStream.rangeClosed(0, 8).mapToObj(i -> "meta"+i).toArray(String[]::new);

	protected static final int MAX_SCAN_RANK = 3; // larger scans take too long

	protected static final int[] EMPTY_SHAPE = new int[0];
	protected static final int[] SINGLE_VALUE_SHAPE = new int[] { 1 };

	protected static final int[] GRID_SHAPE = { 8, 5 };
	private static final int DEFAULT_NUM_AXIS_POINTS = 2;

	protected static final String ENTRY_NAME = "entry1";

	protected static final String INSTRUMENT_NAME = "instrument";
	protected static final String SCANNABLE_NAME_PREFIX = "scannable";
	protected static final String BEFORE_SCAN_COLLECTION_NAME = "before_scan";
	protected static final String SINGLE_FIELD_MONITOR_NAME = "mon01";
	protected static final String MULTI_FIELD_MONITOR_NAME = "multiMon";
	protected static final String STRING_VALUED_METADATA_SCANNABLE_NAME = "strScn";
	protected static final String MULTI_FIELD_METADATA_SCANNABLE_NAME = "multiFieldScannable";

	protected static final int EXPECTED_SCAN_NUMBER = 1;
	protected static final String EXPECTED_ENTRY_IDENTIFER = "1";
	protected static final String EXPECTED_PROGRAM_NAME = "GDA 7.11.0";
	protected static final String EXPECTED_USER_GROUP_NAME = "user01";
	protected static final String EXPECTED_USER_ID = "abc12345";
	protected static final String EXPECTED_USER_NAME = "Ted Jones";
	protected static final String EXPECTED_VISIT_ID = "gda456-1";

	protected static final double START_VALUE = 0.0;
	protected static final double STEP_SIZE = 1.0;
	protected static final double SCANNABLE_LOWER_BOUND = -123.456;
	protected static final double SCANNABLE_UPPER_BOUND = 987.654;

	public static final String[] COUNTER_TIMER_NAMES = { "one", "two", "three", "four" };
	public static final String[] MULTI_FIELD_MONITOR_FIELD_NAMES = { "h", "k", "l" };

	protected static final String EXPECTED_MONOCHROMATOR_NAME = "myMonochromator";
	protected static final double EXPECTED_MONOCHROMATOR_ENERGY = 5.432;
	protected static final double EXPECTED_MONOCHROMATOR_WAVELENGTH = 543.34;
	protected static final double EXPECTED_INSERTION_DEVICE_GAP = 1.234;
	protected static final double EXPECTED_SOURCE_ENERGY = 3.0;
	protected static final double EXPECTED_SOURCE_CURRENT = 25.5;

	protected static final double SINGLE_FIELD_MONITOR_VALUE = 2.5;
	protected static final Object[] MULTI_FIELD_MONITOR_VALUES = new Object[] { 28.0, -24.5, new PyFloat(3.9) }; // test mixed java and python values
	protected static final String SCANNABLE_PV_NAME_PREFIX = "BL00P-MO-STAGE-01:S";
	protected static final String META_SCANNABLE_PV_NAME_PREFIX = "BL00P-MO-META-01:S";
	protected static final String STRING_VALUED_METADATA_SCANNABLE_VALUE = "string value";
	protected static final Object[] MULTI_FIELD_METADATA_SCANNABLE_INPUT_FIELD_VALUES =
			new Object[] { 25.61, null, new PyFloat(-76.23) };
	protected static final Object[] MULTI_FIELD_METADATA_SCANNABLE_EXTRA_FIELD_VALUES =
			new Object[] { "one", null, "three" };

	static Stream<Arguments> parameters() {
		return IntStream.rangeClosed(1, MAX_SCAN_RANK).mapToObj(Arguments::of);
	}

	static Stream<Arguments> nexusDetectorParameters() {
		return IntStream.rangeClosed(1, MAX_SCAN_RANK).mapToObj(Integer::valueOf)
				.mapMulti((scanRank, mapper) -> {
					mapper.accept(Arguments.of(scanRank, NXdetector.NX_DATA));
					mapper.accept(Arguments.of(scanRank, FIELD_NAME_EXTERNAL));
				});
	}

	static List<Object[]> argumentsToArray(List<Arguments> argumentsList) {
		return argumentsList.stream().map(args -> args.get()).toList();
	}

	protected boolean createMonitor = true;
	private String outputDir;

	protected int scanRank;
	protected int[] scanDimensions;
	protected Scannable[] scannables;
	protected Scannable monitor;
	protected Set<String> expectedMetadataScannableNames;

	protected Detector detector;

	protected PrimaryDeviceType primaryDeviceType; // the type of detector we're testing, in terms of nexus writing, e.g. counter timer

	private Object[] scanArguments;

	protected void setupFields(int scanRank) {
		this.scanRank = scanRank;
		scanDimensions = new int[scanRank];
		// dimensions 0 and 1 use GRID_SHAPE, any remaining use DEFAULT_NUM_AXIS_POINTS to keep the
		// scan size small for higher dimension scans
		for (int i = 0; i < scanRank; i++) {
			scanDimensions[i] = i < GRID_SHAPE.length ? GRID_SHAPE[i] : DEFAULT_NUM_AXIS_POINTS;
		}
	}

	@AfterAll
	public static void tearDownServices() {
		GDAMetadataProvider.setInstanceForTesting(null);
		Finder.removeAllFactories();
		ServiceProvider.reset();
	}

	protected void setUpMetadata() throws Exception {
		addMetadataEntry(METADATA_KEY_FEDERAL_ID, EXPECTED_USER_ID);
		addMetadataEntry(METADATA_KEY_VISIT_ID, EXPECTED_VISIT_ID);
	}

	protected void addMetadataEntry(String metadataKey, Object value) {
		GDAMetadataProvider.getInstance().addMetadataEntry(new StoredMetadataEntry(metadataKey, value.toString()));
	}

	@AfterEach
	public void tearDown() {
		scannables = null;
		monitor = null;
		NexusDataWriterConfiguration.getInstance().clear();
	}

	protected abstract void setUpTest(String testName) throws Exception;

	protected void setUpTest(String testName, Class<? extends DataWriter> dataWriterClass) throws Exception {
		final String testDir = TestHelpers.setUpTest(this.getClass(), testName + scanRank + "d", true, dataWriterClass);
		outputDir = testDir + "/Data/";

		setUpScannables();
		setupMonitor();
		setUpMetadata();
		setupMetadataScannables();
	}

	private void setUpScannables() throws Exception {
		this.scannables = new Scannable[scanRank];
		for (int i = 0; i < scanRank; i++) {
			final String name = SCANNABLE_NAME_PREFIX + i;
			final DummyUnitsScannable<Length> dummyScannable = new DummyUnitsScannable<>(name, 0.0, "mm", "mm");
			dummyScannable.setLowerGdaLimits(SCANNABLE_LOWER_BOUND);
			dummyScannable.setUpperGdaLimits(SCANNABLE_UPPER_BOUND);
			dummyScannable.setControllerRecordName(SCANNABLE_PV_NAME_PREFIX + i);
			dummyScannable.setOutputFormat(new String[] { "%5." + (i + 1) + (i % 2 == 0 ? "G" : "E") });
			InterfaceProvider.getJythonNamespace().placeInJythonNamespace(name, dummyScannable);
			this.scannables[i] = dummyScannable;
		}
	}

	private void setupMonitor() throws DeviceException {
		if (!createMonitor) return;

		switch (primaryDeviceType) {
			case NONE: break; // no monitor
			case MULTI_FIELD_MONITOR:
				final DummyMultiFieldUnitsScannable<Length> multiFieldMonitor = new DummyMultiFieldUnitsScannable<>(MULTI_FIELD_MONITOR_NAME);
				multiFieldMonitor.setHardwareUnitString("mm");
				multiFieldMonitor.setUserUnits("mm");
				multiFieldMonitor.setInputNames(MULTI_FIELD_MONITOR_FIELD_NAMES);
				multiFieldMonitor.setCurrentPosition(MULTI_FIELD_MONITOR_VALUES);
				InterfaceProvider.getJythonNamespace().placeInJythonNamespace(MULTI_FIELD_MONITOR_NAME, multiFieldMonitor);
				this.monitor = multiFieldMonitor;
				break;
			default: // create monitor with single extra name field
				final DummyMonitor dummyMonitor = new DummyMonitor();
				dummyMonitor.setConstantValue(SINGLE_FIELD_MONITOR_VALUE);
				dummyMonitor.setName(SINGLE_FIELD_MONITOR_NAME);
				dummyMonitor.configure();
				InterfaceProvider.getJythonNamespace().placeInJythonNamespace(SINGLE_FIELD_MONITOR_NAME, dummyMonitor);
				this.monitor = dummyMonitor;
		}
	}

	private void setupMetadataScannables() throws Exception {
		final Multimap<Integer, Integer> dependencies = ArrayListMultimap.create();
		dependencies.put(1, 5);
		dependencies.putAll(2, List.of(0, 7));
		dependencies.putAll(4, List.of(1, 6, 7));
		dependencies.put(5, 6);
		dependencies.put(7, 8);

		// add dependencies between scannables using the location map
		final Map<String, ScannableWriter> locationMap = new HashMap<>();
		for (int i = 0; i < METADATA_SCANNABLE_NAMES.length; i++) {
			final String name = METADATA_SCANNABLE_NAMES[i];
			final String outputFormat = i % 2 == 0? "%5.3f" : "%3.5g";
			createScannable(name, i, META_SCANNABLE_PV_NAME_PREFIX + i, outputFormat);

			final List<String> prerequisites = dependencies.get(i).stream().map(j -> METADATA_SCANNABLE_NAMES[j]).collect(toList());
			if (i == 0) prerequisites.add(scannables[0].getName());
			locationMap.put(name, createScannableWriter(name, prerequisites));
		}

		locationMap.put(scannables[0].getName(), createScannableWriter(scannables[0].getName(),
				List.of(METADATA_SCANNABLE_NAMES[5])));

		final DummyStringScannable strValuedScannable = new DummyStringScannable(
				STRING_VALUED_METADATA_SCANNABLE_NAME, "string value");
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace(STRING_VALUED_METADATA_SCANNABLE_NAME, strValuedScannable);

		createMultiFieldMetadataScannable(MULTI_FIELD_METADATA_SCANNABLE_NAME);

		final NexusDataWriterConfiguration config = NexusDataWriterConfiguration.getInstance();
		config.setMetadataScannables(Sets.newHashSet(METADATA_SCANNABLE_NAMES[0], METADATA_SCANNABLE_NAMES[1],
				STRING_VALUED_METADATA_SCANNABLE_NAME, MULTI_FIELD_METADATA_SCANNABLE_NAME));
		config.setLocationMap(locationMap);

		final Map<String, Collection<String>> metadataScannablesPerDetectorMap = new HashMap<>();
		if (detector != null) {
			metadataScannablesPerDetectorMap.put(detector.getName(),
					List.of(METADATA_SCANNABLE_NAMES[2], METADATA_SCANNABLE_NAMES[4]));
		}
		config.setMetadataScannablesPerDetectorMap(metadataScannablesPerDetectorMap);

		// set the location of the template file
		config.setNexusTemplateFiles(List.of(Paths.get(TEMPLATE_FILE_PATH).toAbsolutePath().toString()));

		// create the set of expected metadata scannable names
		createExpectedMetadataScannableNames();
	}

	protected boolean hasLocationMapEntry(String scannableName) {
		return NexusDataWriterConfiguration.getInstance().getLocationMap().containsKey(scannableName);
	}

	protected DummyScannable createScannable(final String name, double value) throws DeviceException {
		final DummyScannable scannable = new DummyScannable(name);
		scannable.moveTo(value);
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace(name, scannable);
		return scannable;
	}

	protected DummyScannable createScannable(final String name, double value, String pvName, String outputFormat) throws DeviceException {
		final DummyScannable scannable = createScannable(name, value);
		scannable.setControllerRecordName(pvName);
		scannable.setOutputFormat(new String[] { outputFormat });
		return scannable;
	}

	private Scannable createMultiFieldMetadataScannable(final String name) throws DeviceException {
		final DummyMultiFieldUnitsScannable<Dimensionless> scannable = new DummyMultiFieldUnitsScannable<>(name);
		scannable.setInputNames(new String[]{ "input1", "input2", "input3" });
		scannable.setExtraNames(new String[] { "extra1", "extra2", "extra3"});
		scannable.setCurrentPosition(MULTI_FIELD_METADATA_SCANNABLE_INPUT_FIELD_VALUES);
		scannable.setExtraFieldsPosition(MULTI_FIELD_METADATA_SCANNABLE_EXTRA_FIELD_VALUES);
		scannable.setOutputFormat(new String[] { "%5.5g", "%5.5g", "%5.5g", "%s", "%s", "%s" });
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace(name, scannable);

		return scannable;
	}

	private void createExpectedMetadataScannableNames() {
		final Set<Integer> includedMetadataScannableIndices = new HashSet<>();
		includedMetadataScannableIndices.addAll(Sets.newHashSet(0, 1, 5, 6)); // included in all scans
		if (detector != null) {
			includedMetadataScannableIndices.addAll(Sets.newHashSet(2, 4, 7, 8));
		}

		expectedMetadataScannableNames = IntStream.range(0, METADATA_SCANNABLE_NAMES.length)
				.filter(includedMetadataScannableIndices::contains)
				.mapToObj(i -> METADATA_SCANNABLE_NAMES[i])
				.collect(toCollection(HashSet::new));
		expectedMetadataScannableNames.add(STRING_VALUED_METADATA_SCANNABLE_NAME);
		expectedMetadataScannableNames.add(MULTI_FIELD_METADATA_SCANNABLE_NAME);
	}

	private ScannableWriter createScannableWriter(String scannableName, List<String> prerequisiteNames) {
		final SingleScannableWriter writer = new SingleScannableWriter();
		writer.setPaths(String.format(
				"instrument:NXinstrument/%s:NXpositioner/%s", scannableName, scannableName ));
		writer.setUnits("mm");
		writer.setPrerequisiteScannableNames(prerequisiteNames);
		return writer;
	}

	@ParameterizedTest(name = "scanRank = {0}")
	@MethodSource("parameters")
	public void concurrentScanNoDetectorOrMonitor(int scanRank) throws Exception {
		setupFields(scanRank);
		concurrentScan(null, PrimaryDeviceType.NONE, "NoDetectorOrMonitor");
	}

	@ParameterizedTest(name = "scanRank = {0}")
	@MethodSource("parameters")
	public void concurrentScanNoDetectorSingleFieldMonitor(int scanRank) throws Exception {
		setupFields(scanRank);
		concurrentScan(null, PrimaryDeviceType.SINGLE_FIELD_MONITOR, "NoDetectorSingleFieldMonitor");
	}

	@ParameterizedTest(name = "scanRank = {0}")
	@MethodSource("parameters")
	public void concurrentScanNoDetectorMultiFieldMonitor(int scanRank) throws Exception {
		setupFields(scanRank);
		concurrentScan(null, PrimaryDeviceType.MULTI_FIELD_MONITOR, "NoDetectorMultiFieldMonitor");
	}

	@ParameterizedTest(name = "scanRank = {0}")
	@MethodSource("parameters")
	public void concurrentScanCounterTimer(int scanRank) throws Exception {
		setupFields(scanRank);
		final DummyCounterTimer detector = createCounterTimer();
		concurrentScan(detector, PrimaryDeviceType.COUNTER_TIMER, "CounterTimer");
	}

	@ParameterizedTest(name = "scanRank = {0}")
	@MethodSource("parameters")
	public void concurrentScanCounterTimerNoMonitor(int scanRank) throws Exception {
		setupFields(scanRank);
		createMonitor = false;
		final DummyCounterTimer detector = createCounterTimer();
		concurrentScan(detector, PrimaryDeviceType.COUNTER_TIMER, "CounterTimer");
	}

	private DummyCounterTimer createCounterTimer() throws FactoryException, DeviceException {
		final DummyCounterTimer detector = new DummyCounterTimer();
		detector.setName("counterTimer");
		detector.setDataDecimalPlaces(3); // this property doesn't seem to be used
		detector.setUseGaussian(true);
		detector.setInputNames(new String[0]);

		detector.setExtraNames(COUNTER_TIMER_NAMES);
		detector.setTotalChans(COUNTER_TIMER_NAMES.length);
		detector.setTimerName("timer");
		detector.configure();
		detector.setCollectionTime(10.0);
		detector.setOutputFormat(IntStream.range(0, COUNTER_TIMER_NAMES.length)
				.mapToObj(i -> "%5." + (i + 1) + "g").toArray(String[]::new));
		return detector;
	}

	@ParameterizedTest(name = "scanRank = {0}")
	@MethodSource("parameters")
	public void concurrentScanGenericDetector_scalarData(int scanRank) throws Exception {
		setupFields(scanRank);
		detector = new DummyGenericDetector(1);
		detector.setName("genericDetector");
		concurrentScan(detector, PrimaryDeviceType.GENERIC, "GenericDetector_scalarData");
	}

	@ParameterizedTest(name = "scanRank = {0}")
	@MethodSource("parameters")
	public void concurrentScanGenericDetector_arrayData(int scanRank) throws Exception {
		setupFields(scanRank);
		detector = new DummyGenericDetector(6);
		detector.setName("genericDetector");
		concurrentScan(detector, PrimaryDeviceType.GENERIC, "GenericDetector_arrayData");
	}

	@ParameterizedTest(name = "scanRank = {0}")
	@MethodSource("parameters")
	public void concurrentScanFileCreatorDetector(int scanRank) throws Exception {
		setupFields(scanRank);
		detector = new DummyFileCreatorDetector();
		detector.setName("fileCreatorDetector");
		concurrentScan(detector, PrimaryDeviceType.FILE_CREATOR, "FileCreatorDetector");
	}

	@ParameterizedTest(name = "scanRank = {0}, priorityFieldName = {1}")
	@MethodSource("nexusDetectorParameters") //nexusDetectorParameters")
	public void concurrentScanNexusDetector(int scanRank, String priorityFieldName) throws Exception {
		setupFields(scanRank);
		detector = createNexusDetector(priorityFieldName);
		concurrentScan(detector, PrimaryDeviceType.NEXUS_DETECTOR, "NexusDetector");
	}

	@ParameterizedTest(name = "scanRank = {0}")
	@MethodSource("parameters")
	public void concurrentScanSimpleNexusDetector(int scanRank) throws Exception {
		setupFields(scanRank);
		detector = new SimpleDummyNexusDetector();
		concurrentScan(detector, PrimaryDeviceType.SIMPLE_NEXUS_DETECTOR, "SimpleNexusDetector");
	}

	@ParameterizedTest(name = "scanRank = {0}")
	@MethodSource("parameters")
	public void concurrentScanNoDataNexusDetector(int scanRank) throws Exception {
		setupFields(scanRank);
		detector = new NoDataNexusDetector();
		concurrentScan(detector, PrimaryDeviceType.NO_DATA_NEXUS_DETECTOR, "NoPrimaryFieldNexusDetector");
	}

	private NexusDetector createNexusDetector(String prioritizedDataFieldName) {
		final DummyNexusDetector nexusDetector = new DummyNexusDetector();
		nexusDetector.setScanDimensions(scanDimensions);
		nexusDetector.setPrioritisedDataFieldName(prioritizedDataFieldName);
		return nexusDetector;
	}

	protected void concurrentScan(Detector detector, PrimaryDeviceType detectorType, String testSuffix) throws Exception {
		this.detector = detector;
		this.primaryDeviceType = detectorType;

		setUpTest("concurrentScan" + testSuffix); // create test dir and initialize properties
		if (detectorType == PrimaryDeviceType.NEXUS_DETECTOR) {
			((DummyNexusDetector) detector).setOutputDir(outputDir); // to write external file
		}

		// create the scan
		scanArguments = createScanArguments(detector);
		final ConcurrentScan scan = new ConcurrentScan(scanArguments);
		assertThat(scan.getScanNumber(), is(-1));

		// run the scan
		scan.runScan();

		assertThat(scan.getScanNumber(), is(EXPECTED_SCAN_NUMBER));
		final IScanDataPoint lastPoint = InterfaceProvider.getScanDataPointProvider().getLastScanDataPoint();
		assertThat(lastPoint.getScanIdentifier(), is(EXPECTED_SCAN_NUMBER));

		// check the nexus file was written with the expected name
		final File expectedNexusFile = new File(outputDir + "1.nxs");
		final String expectedNexusFilePath = expectedNexusFile.getAbsolutePath();
		assertThat(expectedNexusFile.exists(), is(true));
		assertThat(lastPoint.getCurrentFilename(), is(equalTo(expectedNexusFilePath)));
		assertThat(scan.getDataWriter().getCurrentFileName(), is(equalTo(expectedNexusFilePath)));

		// check the content of the nexus file
		try (final NexusFile nexusFile = NexusTestUtils.openNexusFile(expectedNexusFilePath)) {
			checkNexusFile(nexusFile);
		}

		// check an SRS file was created
		final File expectedSrsFile = new File(outputDir + "1.dat");
		assertThat(expectedSrsFile.exists(), is(true));
	}

	private Object[] createScanArguments(Detector detector) {
		final List<Object> scanArgs = new ArrayList<>();

		// add scannables
		for (int i = 0; i < scanRank; i++) {
			scanArgs.add(scannables[i]);
			scanArgs.add(START_VALUE);
			scanArgs.add(getStopValue(i));
			scanArgs.add(STEP_SIZE);
		}

		// add monitor
		if (monitor != null) {
			scanArgs.add(monitor);
		}

		// add detector if present
		if (detector != null) {
			scanArgs.add(detector);
		}

		return scanArgs.toArray();
	}

	private double getStopValue(int i) {
		return START_VALUE + STEP_SIZE * (scanDimensions[i] - 1);
	}

	protected String getExpectedScanCommand() {
		return "scan " + String.join(" ", Arrays.stream(scanArguments)
			.map(arg -> arg instanceof Scannable ? ((Scannable) arg).getName() : arg.toString())
			.toArray(String[]::new));
	}

	protected String[] getScannableNames() {
		return Arrays.stream(scannables).map(Scannable::getName).toArray(String[]::new);
	}

	protected String[] getScannableAndMonitorNames() {
		return Streams.concat(Arrays.stream(scannables), Optional.ofNullable(monitor).stream())
			.map(Scannable::getName)
			.toArray(String[]::new);
	}

	protected abstract Set<String> getExpectedPositionerNames();

	protected int getNumScannedDevices() { // scannables, per-point monitors and detectors
		return scanRank + (monitor != null ? 1 : 0) + (detector != null ? 1 : 0);
	}

	protected int getNumDevices() { // includes metadata scannables
		return getNumScannedDevices() + getNumMetadataScannables();
	}

	protected int getNumMetadataScannables() {
		return getExpectedMetadataScannableNames().size();
	}

	protected Set<String> getExpectedMetadataScannableNames() {
		return expectedMetadataScannableNames;
	}

	protected DataNode getDataNode(NXentry entry, String nodePath) {
		final NodeLink nodeLink = entry.findNodeLink(nodePath);
		assertThat(nodePath, nodeLink, is(notNullValue()));
		assertThat(nodePath, nodeLink.isDestinationData(), is(true));
		return (DataNode) nodeLink.getDestination();
	}

	private void checkNexusFile(NexusFile nexusFile) throws Exception {
		final TreeFile nexusTree = NexusUtils.loadNexusTree(nexusFile);
		final NXroot nexusRoot = (NXroot) nexusTree.getGroupNode();
		assertThat(nexusRoot, is(notNullValue()));
		final NXentry entry = nexusRoot.getEntry(ENTRY_NAME);
		assertThat(entry, is (notNullValue()));

		checkNexusMetadata(entry);
		checkSampleGroup(entry);
		checkUsers(entry);
		checkInstrument(entry.getInstrument());
		checkDataGroups(entry);
		checkTemplateEntry(nexusRoot);
	}

	protected void checkInstrument(NXinstrument instrument) throws Exception {
		assertThat(instrument, is(notNullValue()));
		checkInstrumentGroupMetadata(instrument); // check no of nodes, etc
		checkScannablesAndMonitors(instrument);
		checkDetector(instrument);
		checkMetadataDeviceGroups(instrument);
	}

	protected void checkScannablesAndMonitors(final NXinstrument instrument) throws Exception {
		final Map<String, NXpositioner> positioners = instrument.getAllPositioner();
		final String[] expectedPositionerNames = getExpectedPositionerNames().toArray(String[]::new);
		assertThat(positioners.keySet(), containsInAnyOrder(expectedPositionerNames));
		assertThat(positioners.size(), is(expectedPositionerNames.length));

		checkScannables(positioners);
		checkMonitor(instrument);
		checkMetadataScannables(positioners);
	}

	private void checkScannables(final Map<String, NXpositioner> positioners) throws Exception {
		// the NXpositioner for the first scannable is written differently as it has a
		// ScannableWriter configured in the location map
		final String firstScannableName = scannables[0].getName();
		checkConfiguredScannablePositioner(firstScannableName, positioners.get(firstScannableName));

		// check the remaining scannables have been written correctly
		for (int i = 1; i < scanRank; i++) {
			final String scannableName = scannables[i].getName();
			final NXpositioner scannablePos = positioners.get(scannableName);
			assertThat(scannablePos, is(notNullValue()));
			checkDefaultScannablePositioner(scannablePos, i);
		}
	}

	private void checkMetadataScannables(final Map<String, NXpositioner> positioners) throws Exception {
		final Set<String> expectedMetadataScannableNames = getExpectedMetadataScannableNames();
		for (int i = 0; i < METADATA_SCANNABLE_NAMES.length; i++) {
			final String metadataScannableName = METADATA_SCANNABLE_NAMES[i];
			final NXpositioner positioner = positioners.get(metadataScannableName);
			if (expectedMetadataScannableNames.contains(metadataScannableName)) {
				assertThat(positioner, is(notNullValue()));
				checkMetadataScannablePositioner(positioner, i);
			} else {
				assertThat(positioner, is(nullValue()));
			}
		}
	}

	protected Map<String, Object> getMultiFieldScannableExpectedValuesMap() {
		final Scannable multiFieldScannable = (Scannable) InterfaceProvider.getJythonNamespace()
				.getFromJythonNamespace(MULTI_FIELD_METADATA_SCANNABLE_NAME);
		final String[] multiFieldScannableFieldNames = ArrayUtils.addAll(
				multiFieldScannable.getInputNames(), multiFieldScannable.getExtraNames());
		final Object[] expectedMultiFieldScannableFieldValues = ArrayUtils.addAll(
				MULTI_FIELD_METADATA_SCANNABLE_INPUT_FIELD_VALUES, MULTI_FIELD_METADATA_SCANNABLE_EXTRA_FIELD_VALUES);

		return IntStream.range(0, multiFieldScannableFieldNames.length).mapToObj(Integer::valueOf)
						.filter(i -> expectedMultiFieldScannableFieldValues[i] != null)
						.collect(toMap(i -> multiFieldScannableFieldNames[i],
								i -> convertToJava(expectedMultiFieldScannableFieldValues[i])));
	}

	private void checkDetector(NXinstrument instrument) throws Exception {
		final Set<String> detectorGroupNames = instrument.getAllDetector().keySet();

		if (detector == null) {
			assertThat(detectorGroupNames, is(empty()));
			return; // no detector in this scan, so no NXdetector group to check
		}

		assertThat(detectorGroupNames, is(not(empty())));
		assertThat(detectorGroupNames, contains(detector.getName()));

		// check that the NXdetector group for the detector is as expected
		final NXdetector detectorGroup = instrument.getDetector(detector.getName());
		assertThat(detectorGroup, is(notNullValue()));
		checkAttributes(detectorGroup, getExpectedDetectorAttributes());

		switch (primaryDeviceType) {
			case NONE:
				break; // detector == null, so this case is not reached
			case NEXUS_DEVICE:
				checkNexusDeviceDetector(detectorGroup);
				break;
			case COUNTER_TIMER:
				checkCounterTimerDetector(detectorGroup);
				break;
			case GENERIC:
				checkGenericDetector(detectorGroup);
				break;
			case FILE_CREATOR:
				checkFileCreatorDetector(detectorGroup);
				break;
			case SIMPLE_NEXUS_DETECTOR:
				checkSimpleNexusDetector(detectorGroup);
				break;
			case NO_DATA_NEXUS_DETECTOR:
				checkNoDataNexusDetector(detectorGroup);
				break;
			case NEXUS_DETECTOR:
				checkNexusDetector(detectorGroup);
				break;
			default:
				throw new IllegalArgumentException("Unknown detector type: " + primaryDeviceType);
		}
	}

	protected Map<String, Object> getExpectedDetectorAttributes() {
		if (primaryDeviceType == PrimaryDeviceType.NEXUS_DETECTOR) {
			return Map.ofEntries(
					Map.entry(NexusConstants.NXCLASS, NexusBaseClass.NX_DETECTOR.toString()),
					Map.entry(DummyNexusDetector.STRING_ATTR_NAME, DummyNexusDetector.STRING_ATTR_VALUE),
					Map.entry(DummyNexusDetector.INT_ATTR_NAME, new int[] { DummyNexusDetector.INT_ATTR_VALUE }),
					Map.entry(DummyNexusDetector.FLOAT_ATTR_NAME, new double[] { DummyNexusDetector.FLOAT_ATTR_VALUE }),
					Map.entry(DummyNexusDetector.ARRAY_ATTR_NAME, DummyNexusDetector.ARRAY_ATTR_VALUE)
				);
		}

		return Map.of(NexusConstants.NXCLASS, NexusBaseClass.NX_DETECTOR.toString());
	}

	private void checkNexusDeviceDetector(final NXdetector detGroup) throws DatasetException {
		final DataNode dataNode = detGroup.getDataNode(NXdata.NX_DATA);
		assertThat(dataNode, is(notNullValue()));

		final ILazyDataset dataset = dataNode.getDataset();
		final int[] shape = dataset.getShape();
		assertThat(shape.length, is(scanRank + 2));
		assertThat(Arrays.copyOfRange(shape, 0, scanRank), is(equalTo(scanDimensions)));
		checkDatasetWritten(dataset, DummyImageDetector.IMAGE_SIZE);
	}

	private void checkCounterTimerDetector(NXdetector detGroup) throws DatasetException {
		final String[] extraNames = detector.getExtraNames();
		final String[] expectedDataNodeNames = ArrayUtils.addAll(extraNames,
				NXdetector.NX_DESCRIPTION, NXdetector.NX_TYPE, "id");
		assertThat(detGroup.getDataNodeNames(), containsInAnyOrder(expectedDataNodeNames));

		assertThat(detGroup.getDescriptionScalar(), is(equalTo("Dummy Counter Timer")));
		assertThat(detGroup.getTypeScalar(), is(equalTo("DUMMY")));
		assertThat(detGroup.getDataNode("id").getString(), is(equalTo("dumdum-2")));

		for (int fieldIndex = 0; fieldIndex < extraNames.length; fieldIndex++) {
			final String fieldName = extraNames[fieldIndex];
			final DataNode dataNode = detGroup.getDataNode(fieldName);
			assertThat(dataNode, is(notNullValue()));

			checkDatasetWritten(dataNode.getDataset(), EMPTY_SHAPE);

			final Map<String, Object> expectedAttrs = getExpectedCounterTimerFieldAttributes(fieldName, fieldIndex);
			checkAttributes(dataNode, expectedAttrs);
		}
	}

	protected Map<String, Object> getExpectedCounterTimerFieldAttributes(String fieldName,
			@SuppressWarnings("unused") int fieldIndex) throws DatasetException {
		return Map.ofEntries(
				Map.entry(ATTRIBUTE_NAME_LOCAL_NAME, detector.getName() + "." + fieldName),
				Map.entry(ATTRIBUTE_NAME_TARGET, "/entry1/instrument/counterTimer/" + fieldName));
	}

	protected void checkAttributes(Node node, Map<String, Object> expectedAttributes) {
		assertThat(node.getAttributeNames(), containsInAnyOrder(expectedAttributes.keySet().toArray()));
		for (Map.Entry<String, Object> attrEntry : expectedAttributes.entrySet()) {
			final Attribute attr = node.getAttribute(attrEntry.getKey());
			if (attrEntry.getValue() instanceof String) {
				assertThat("attribute " + attrEntry.getKey(), attr.getFirstElement(), is(equalTo(attrEntry.getValue())));
			} else {
				assertThat("attribute " + attrEntry.getKey(), attr.getValue(),
						is(equalTo(DatasetFactory.createFromObject(attrEntry.getValue()))));
			}
		}
	}

	private void checkGenericDetector(NXdetector detGroup) throws Exception {
		assertThat(detGroup.getDataNodeNames(), containsInAnyOrder(
				NXdetector.NX_DATA, NXdetector.NX_DESCRIPTION, NXdetector.NX_TYPE, "id"));
		assertThat(detGroup.getDescriptionScalar(), is(equalTo("An example generic detector")));
		assertThat(detGroup.getTypeScalar(), is(equalTo("generic")));
		assertThat(detGroup.getDataNode("id").getString(), is(equalTo("gen1")));

		final DataNode dataNode = detGroup.getDataNode(NXdetector.NX_DATA);
		assertThat(dataNode, is(notNullValue()));
		checkDatasetWritten(dataNode.getDataset(), getDataDimensionsToWrite(detector));
		final Map<String, Object> expectedAttributes = Map.ofEntries(
				Map.entry(ATTRIBUTE_NAME_TARGET, "/entry1/instrument/genericDetector/data"),
				Map.entry(ATTRIBUTE_NAME_LOCAL_NAME, "genericDetector.genericDetector"));
		checkAttributes(dataNode, expectedAttributes);
	}

	private int[] getDataDimensionsToWrite(Detector detector) throws DeviceException {
		// a 1-dimensional array of size 1 is written as if it was scalar
		final int[] dataDims = detector.getDataDimensions();
		return dataDims.length == 1 && dataDims[0] == 1 ? EMPTY_SHAPE : dataDims;
	}

	private void checkFileCreatorDetector(NXdetector detGroup) throws DatasetException {
		assertThat(detGroup.getDataNodeNames(), containsInAnyOrder(NXdetector.NX_DESCRIPTION, NXdetector.NX_TYPE));
		// TODO: DAQ 3180 - NexusDataWriter writes these hard coded values instead of calling
		// detector.getDescription() and detector.getType(), also it doesn't write ID. Why not? should the new writer change this
		assertThat(detGroup.getDescriptionScalar(), is(equalTo("Generic GDA Detector - External Files")));
		assertThat(detGroup.getTypeScalar(), is(equalTo("Detector")));
//		assertThat(detGroup.getDataNode("id").getString(), is(equalTo("fileDet1")));

		assertThat(detGroup.getGroupNodeNames(), contains("data_file"));
		final NXnote dataFileGroup = detGroup.getData_file();
		assertThat(dataFileGroup, is(notNullValue()));

		assertThat(dataFileGroup.getDataNodeNames(), contains(NXnote.NX_FILE_NAME));
		final DataNode fileNameDataNode = dataFileGroup.getDataNode(NXnote.NX_FILE_NAME);
		assertThat(fileNameDataNode, is(notNullValue()));

		final IDataset dataset = fileNameDataNode.getDataset().getSlice();
		assertThat(dataset.getShape(), is(equalTo(scanDimensions)));

		int expectedFileNum = 1;
		final PositionIterator posIter = new PositionIterator(dataset.getShape());
		while (posIter.hasNext()) {
			int[] pos = posIter.getPos();
			assertThat(dataset.getString(pos), is(equalTo("file" + (expectedFileNum++) + ".tif")));
		}
	}

	private void checkSimpleNexusDetector(NXdetector detGroup) throws Exception {
		assertThat(detGroup.getGroupNodeNames(), is(empty()));
		assertThat(detGroup.getDataNodeNames(), contains(NXdetector.NX_DATA,
				NXdetector.NX_DATA + SimpleDummyNexusDetector.AXIS_NAME_SUFFIX + "1",
				NXdetector.NX_LOCAL_NAME));
	}

	private void checkNoDataNexusDetector(NXdetector detGroup) throws Exception {
		assertThat(detGroup.getGroupNodeNames(), is(empty()));
		assertThat(detGroup.getDataNodeNames(), containsInAnyOrder(NXdetector.NX_DESCRIPTION,
				NXdetector.NX_DETECTOR_NUMBER, NXdetector.NX_LOCAL_NAME));
		assertThat(detGroup.getLocal_nameScalar(), is(equalTo(NoDataNexusDetector.DETECTOR_NAME)));
		assertThat(detGroup.getDetector_numberScalar(), is(equalTo(NoDataNexusDetector.DETECTOR_NUMBER)));
		assertThat(detGroup.getDescriptionScalar(), is(equalTo(NoDataNexusDetector.DESCRIPTION)));
	}

	private void checkNexusDetector(NXdetector detGroup) throws Exception {
		assertThat(detGroup.getGroupNodeNames(), containsInAnyOrder("note", COLLECTION_NAME));
		assertThat(detGroup.getDataNodeNames(), containsInAnyOrder(getExpectedNexusDetectorDataNodeNames()));

		assertThat(detGroup.getLocal_nameScalar(), is(equalTo(detector.getName())));
		checkDatasetWritten(detGroup.getDataNode(FIELD_NAME_VALUE).getDataset(), EMPTY_SHAPE);
		checkDatasetWritten(detGroup.getDataNode(FIELD_NAME_SPECTRUM).getDataset(),
				new int[] { DummyNexusDetector.SPECTRUM_SIZE });
		checkDatasetWritten(detGroup.getDataNode(NXdetector.NX_DATA).getDataset(), DummyNexusDetector.IMAGE_SIZE);
		checkDatasetWritten(detGroup.getDataNode(FIELD_NAME_EXTERNAL).getDataset(), DummyNexusDetector.IMAGE_SIZE);

		assertThat(detGroup.getDetector_numberScalar(), is(DETECTOR_NUMBER));
		assertThat(detGroup.getSerial_numberScalar(), is(equalTo(SERIAL_NUMBER)));
		assertThat(detGroup.getDiameterScalar(), is(equalTo(DIAMETER)));
		assertThat(detGroup.getAttrString(NXdetector.NX_DIAMETER, ATTRIBUTE_NAME_UNITS), is(DIAMETER_UNITS));
		assertThat(detGroup.getGain_settingScalar(), is(equalTo(DummyNexusDetector.GAIN_SETTING)));
		assertThat(detGroup.getDataNode(NXdetector.NX_GAIN_SETTING).getAttributeNames(), is(empty()));

		// note that expected attributes on the detector group have already been tested

		final NXnote note = (NXnote) detGroup.getGroupNode("note");
		assertThat(note, is(notNullValue()));
		assertThat(note.getDataNodeNames(), containsInAnyOrder(NXnote.NX_TYPE, NXnote.NX_DESCRIPTION));
		assertThat(note.getTypeScalar(), is(equalTo("text/plain")));
		assertThat(note.getDescriptionScalar(), is(equalTo("This is a note")));
	}

	private String[] getExpectedNexusDetectorDataNodeNames() {
		return new String[] {
				// primary fields, written at each point
				NXdetector.NX_DATA, FIELD_NAME_SPECTRUM, FIELD_NAME_VALUE, FIELD_NAME_EXTERNAL,
				// metadata
				NXdetector.NX_LOCAL_NAME, NXdetector.NX_DETECTOR_NUMBER, NXdetector.NX_DIAMETER,
				NXdetector.NX_SERIAL_NUMBER, NXdetector.NX_GAIN_SETTING,
				FIELD_NAME_IMAGE_X, FIELD_NAME_IMAGE_Y // image axes
			};
	}

	private void checkMonitor(NXinstrument instrument) throws Exception {
		// check the monitor has been written correctly
		final GroupNode singleFieldMonitorPos = instrument.getGroupNode(SINGLE_FIELD_MONITOR_NAME);
		final GroupNode multiFieldMonitorPos = instrument.getGroupNode(MULTI_FIELD_MONITOR_NAME);

		if (!createMonitor || primaryDeviceType == PrimaryDeviceType.NONE) {
			assertThat(singleFieldMonitorPos, is(nullValue()));
			assertThat(multiFieldMonitorPos, is(nullValue()));
		} else if (primaryDeviceType == PrimaryDeviceType.MULTI_FIELD_MONITOR) {
			assertThat(singleFieldMonitorPos, is(nullValue()));
			assertThat(multiFieldMonitorPos, is(notNullValue()));
			checkMultiFieldMonitor(multiFieldMonitorPos);
		} else { // all other cases include a single field monitor
			assertThat(singleFieldMonitorPos, is(notNullValue()));
			checkSingleFieldMonitor(singleFieldMonitorPos);
			assertThat(multiFieldMonitorPos, is(nullValue()));
		}
	}

	protected abstract void checkSingleFieldMonitor(GroupNode monitorGroup) throws Exception;

	protected abstract void checkMultiFieldMonitor(GroupNode monitorGroup) throws Exception;

	protected abstract void checkConfiguredScannablePositioner(String scannableName, NXpositioner positioner) throws Exception;

	protected abstract void checkDefaultScannablePositioner(NXpositioner positioner, int scanIndex) throws Exception;

	protected abstract void checkInstrumentGroupMetadata(NXinstrument instrument) throws Exception;

	protected abstract void checkDataGroups(NXentry entry) throws Exception;

	protected abstract void checkMetadataScannablePositioner(NXpositioner positioner, int index) throws Exception;

	protected void checkNexusMetadata(NXentry entry) throws Exception {
		// start_time
		checkDateTime(entry.getStart_time());
		// end_time
		checkDateTime(entry.getEnd_time());
	}

	protected abstract void checkUsers(NXentry entry);

	protected void checkMetadataDeviceGroups(NXinstrument instrument) throws Exception {
		checkMonochromatorGroup(instrument);
		checkInsertionDeviceGroup(instrument);
		checkSourceGroup(instrument);
	}

	protected abstract void checkSourceGroup(NXinstrument instrument);

	protected abstract void checkSampleGroup(NXentry entry);

	protected abstract void checkInsertionDeviceGroup(NXinstrument instrument);

	protected abstract void checkMonochromatorGroup(NXinstrument instrument);

	private void checkDateTime(IDataset dataset) {
		assertThat(dataset, is(notNullValue()));
		assertThat(dataset.getRank(), either(is(0)).or(is(1)));
		assertThat(dataset.getElementClass(), is(equalTo(String.class)));
		DateDataset dateTime = DatasetUtils.cast(DateDataset.class, dataset);
		Date inDataset;
		if (dataset.getRank() == 0) {
			assertThat(dataset.getShape(), is(equalTo(EMPTY_SHAPE)));
			inDataset = dateTime.getDate();
		} else {
			assertThat(dataset.getShape(), is(equalTo(SINGLE_VALUE_SHAPE)));
			inDataset = dateTime.getDate(0);
		}

		assertThat(inDataset.before(Date.from(Instant.now())), is(true));
		assertThat(inDataset.after(Date.from((Instant.now().minus(5, ChronoUnit.MINUTES)))), is(true));
	}

	protected void checkLinkedDatasets(NXdata data, NXentry entry, Map<String, String> expectedDataNodeLinks) {
		assertThat(data.getDataNodeNames(), containsInAnyOrder(expectedDataNodeLinks.keySet().toArray()));

		for (Map.Entry<String, String> dataNodeLinkEntry : expectedDataNodeLinks.entrySet()) {
			final String dataNodeName = dataNodeLinkEntry.getKey();
			final DataNode dataNode = data.getDataNode(dataNodeName);
			final String targetPath = dataNodeLinkEntry.getValue();
			final DataNode targetNode = getDataNode(entry, dataNodeLinkEntry.getValue());
			assertThat(targetNode, is(notNullValue()));

			if (dataNodeName.equals(FIELD_NAME_EXTERNAL)) {
				assertDataNodesEqual(targetPath, dataNode, targetNode);
			} else {
				assertThat(dataNode, is(sameInstance(targetNode)));
			}
		}
	}

	protected void checkDatasetWritten(ILazyDataset dataset, int[] dataShape) throws DatasetException {
		checkDatasetWritten(dataset.getSlice(), dataShape);
	}

	protected void checkDatasetWritten(IDataset dataset, int[] dataShape) {
		// Make sure none of the numbers are NaNs. The detector
		// is expected to fill this scan with non-nulls.
		final int[] expectedShape = Streams.concat(Arrays.stream(scanDimensions),
				Arrays.stream(dataShape)).toArray();
		assertThat(dataset.getShape(), is(equalTo(expectedShape)));

		final PositionIterator posIter = new PositionIterator(dataset.getShape());
		while (posIter.hasNext()) {
			int[] pos = posIter.getPos();
			assertThat(Double.isNaN(dataset.getDouble(pos)), is(false));
		}
	}

	protected Dataset getExpectedScannableDataset(int i) {
		final DoubleDataset dataset = DatasetFactory.zeros(scanDimensions);
		final Dataset scannableValues = DatasetFactory.createRange(START_VALUE, getStopValue(i) + 1.0, STEP_SIZE);
		final IndexIterator iter = dataset.getIterator(true);
		while (iter.hasNext()) {
			final int[] scanPosition = iter.getPos();
			final int scannableIndex = scanPosition[i]; // the index of the scannable value in its range
			final double expectedPos = scannableValues.getDouble(scannableIndex);
			dataset.setItem(expectedPos, scanPosition);
		}

		return dataset;
	}

	private void checkTemplateEntry(NXroot root) {
		// assert that the entry created by the template is present
		final NXentry mainEntry = root.getEntry(ENTRY_NAME);

		final NXentry scanEntry = root.getEntry("scan");
		assertThat(scanEntry, is(notNullValue()));
		assertThat(scanEntry.getDataNodeNames(), containsInAnyOrder(
				NXentry.NX_START_TIME, NXentry.NX_END_TIME, NXentry.NX_PROGRAM_NAME, NXentry.NX_DEFINITION));
		assertThat(scanEntry.getGroupNodeNames(), is(empty()));
		assertThat(scanEntry.getStart_timeScalar(), is(sameInstance(mainEntry.getStart_timeScalar())));
		assertThat(scanEntry.getEnd_timeScalar(), is(sameInstance(mainEntry.getEnd_timeScalar())));
		assertThat(scanEntry.getDefinitionScalar(), is(equalTo("NXscan")));
		assertThat(scanEntry.getProgram_nameScalar(), is(equalTo("GDA")));
		assertThat(scanEntry.getProgram_nameAttributeVersion(), is(equalTo("9.13")));
		assertThat(scanEntry.getProgram_nameAttributeConfiguration(), is(equalTo("dummy")));
	}

	protected void checkMeasurementDataGroup(NXdata dataGroup) throws Exception {
		assertThat(dataGroup, is(notNullValue()));

		final List<String> expectedFieldNames = getExpectedMeasurementGroupFieldNames();

		assertSignal(dataGroup, expectedFieldNames.get(expectedFieldNames.size() - 1));
		assertAxes(dataGroup, getScannableNames());
		assertThat(dataGroup.getDataNodeNames(), containsInAnyOrder(expectedFieldNames.toArray()));

		assertThat(dataGroup.getAttributeNames(), containsInAnyOrder(Stream.concat(
				Stream.of(NexusConstants.NXCLASS, NexusConstants.DATA_AXES, NexusConstants.DATA_SIGNAL),
				expectedFieldNames.stream().map(name -> name + NexusConstants.DATA_INDICES_SUFFIX)).toArray()));

		final IDataset expectedIndicesAttrValue = DatasetFactory.createFromObject(
				IntStream.range(0, scanDimensions.length).toArray());
		for (String scannableName : expectedFieldNames) {
			final DataNode dataNode = dataGroup.getDataNode(scannableName);
			assertThat(dataNode, is(notNullValue()));
			assertThat(dataNode.getDataset().getElementClass(), is(equalTo(Double.class)));
			assertThat(dataNode.getDataset().getShape(), is(equalTo(scanDimensions)));

			final Attribute indicesAttr = dataGroup.getAttribute(scannableName + DATA_INDICES_SUFFIX);
			assertThat(indicesAttr, is(notNullValue()));
			assertThat(indicesAttr.getValue(), is(equalTo(expectedIndicesAttrValue)));
		}
	}

	protected List<String> getExpectedMeasurementGroupFieldNames() throws Exception {
		return getExpectedScanFieldNames(false, true);
	}

	protected List<String> getExpectedScanFieldNames(boolean prefixWithDeviceName,
			boolean includeNexusDetectorFields) throws Exception {
		return Stream.concat(Arrays.stream(scannables), Stream.of(monitor, detector))
					.filter(Objects::nonNull)
					.map(device -> getDeviceFieldNames(device, prefixWithDeviceName, includeNexusDetectorFields))
					.flatMap(Function.identity())
					.map(fieldName -> prefixWithDeviceName ? fieldName : fieldName)
					.toList();
	}

	private Stream<String> getDeviceFieldNames(Scannable device, boolean prefixWithDeviceName, boolean includeNexusDetectorFields) {
		Stream<String> deviceFieldNames = getDeviceFieldNames(device, includeNexusDetectorFields);
		if (prefixWithDeviceName) {
			deviceFieldNames = deviceFieldNames.map(fieldName -> device.getName() + "." + fieldName);
		}
		return deviceFieldNames;
	}

	private Stream<String> getDeviceFieldNames(Scannable device, boolean includeNexusDetectorFields) {
		if (device instanceof Detector det) {
			if (ArrayUtils.isNotEmpty(det.getExtraNames()) &&
						(!(det instanceof NexusDetector) || includeNexusDetectorFields)) {
				// counter timer case, or nexus detector case if includeNexusDetectorFields is true
				return Arrays.stream(det.getExtraNames());
			}

			try {
				if (primaryDeviceType == PrimaryDeviceType.GENERIC &&
						Arrays.equals(det.getDataDimensions(), SINGLE_VALUE_SHAPE)) {
					return Stream.of(det.getName());
				}
			} catch (DeviceException e) {
				throw new IllegalArgumentException("Could not get detector data dimensions: " + det.getName(), e);
			}

			return Stream.empty(); // no fields for NexusDetector and det.writesOwnFiles() == true cases
		}

		 // Scannable: just join inputNames and extraNames
		return Stream.concat(Arrays.stream(device.getInputNames()), Arrays.stream(device.getExtraNames()));
	}

}
