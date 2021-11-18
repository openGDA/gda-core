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

import static gda.data.scan.datawriter.AbstractNexusDataWriterScanTest.DummyNexusDetector.ARRAY_ATTR_NAME;
import static gda.data.scan.datawriter.AbstractNexusDataWriterScanTest.DummyNexusDetector.ARRAY_ATTR_VALUE;
import static gda.data.scan.datawriter.AbstractNexusDataWriterScanTest.DummyNexusDetector.COLLECTION_NAME;
import static gda.data.scan.datawriter.AbstractNexusDataWriterScanTest.DummyNexusDetector.DETECTOR_NUMBER;
import static gda.data.scan.datawriter.AbstractNexusDataWriterScanTest.DummyNexusDetector.FIELD_NAME_EXTERNAL;
import static gda.data.scan.datawriter.AbstractNexusDataWriterScanTest.DummyNexusDetector.FIELD_NAME_SPECTRUM;
import static gda.data.scan.datawriter.AbstractNexusDataWriterScanTest.DummyNexusDetector.FIELD_NAME_VALUE;
import static gda.data.scan.datawriter.AbstractNexusDataWriterScanTest.DummyNexusDetector.FLOAT_ATTR_NAME;
import static gda.data.scan.datawriter.AbstractNexusDataWriterScanTest.DummyNexusDetector.FLOAT_ATTR_VALUE;
import static gda.data.scan.datawriter.AbstractNexusDataWriterScanTest.DummyNexusDetector.INT_ATTR_NAME;
import static gda.data.scan.datawriter.AbstractNexusDataWriterScanTest.DummyNexusDetector.INT_ATTR_VALUE;
import static gda.data.scan.datawriter.AbstractNexusDataWriterScanTest.DummyNexusDetector.SERIAL_NUMBER;
import static gda.data.scan.datawriter.AbstractNexusDataWriterScanTest.DummyNexusDetector.STRING_ATTR_NAME;
import static gda.data.scan.datawriter.AbstractNexusDataWriterScanTest.DummyNexusDetector.STRING_ATTR_VALUE;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertDataNodesEqual;
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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import javax.measure.quantity.Length;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.nexus.IWritableNexusDevice;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXnote;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
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
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.IndexIterator;
import org.eclipse.january.dataset.PositionIterator;
import org.eclipse.january.dataset.Random;
import org.eclipse.january.dataset.SliceND;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;

import gda.TestHelpers;
import gda.data.ServiceHolder;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.metadata.StoredMetadataEntry;
import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.data.scan.datawriter.scannablewriter.ScannableWriter;
import gda.data.scan.datawriter.scannablewriter.SingleScannableWriter;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Monitor;
import gda.device.Scannable;
import gda.device.detector.DummyDetector;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NexusDetector;
import gda.device.detector.countertimer.DummyCounterTimer;
import gda.device.monitor.DummyMonitor;
import gda.device.scannable.DummyScannable;
import gda.device.scannable.DummyUnitsScannable;
import gda.device.scannable.ScannableBase;
import gda.jython.InterfaceProvider;
import gda.scan.ConcurrentScan;
import gda.scan.IScanDataPoint;

public abstract class AbstractNexusDataWriterScanTest {

	public enum DetectorType {
		NONE(),
		NEXUS_DEVICE(NXdetector.NX_DATA),
		COUNTER_TIMER,
		GENERIC(NXdetector.NX_DATA),
		FILE_CREATOR,
		/**
		 *  Explicitly non-alphabetical, non-order of attachment to test prioritising of NexusGroupData
		 */
		NEXUS_DETECTOR(FIELD_NAME_SPECTRUM, FIELD_NAME_VALUE, NXdetector.NX_DATA, FIELD_NAME_EXTERNAL),
		/**
		 *  Alternate order to test re-prioritising when set
		 */
		MODIFIED_NEXUS_DETECTOR(FIELD_NAME_EXTERNAL, FIELD_NAME_SPECTRUM, FIELD_NAME_VALUE, NXdetector.NX_DATA);

		private List<String> primaryFieldNames;

		DetectorType(String... primaryFieldNames) {
			this.primaryFieldNames = List.of(primaryFieldNames);
		}

		public List<String> getPrimaryFieldNames() {
			return primaryFieldNames;
		}
	}

	/**
	 * A generic detector that extends DummyDetector to return an fixed-length double array.
	 * Used by {@link AbstractNexusDataWriterScanTest#concurrentScanGenericDetector_scalarData()}
	 * and {@link AbstractNexusDataWriterScanTest#concurrentScanGenericDetector_arrayData()}
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
	 * {@link AbstractNexusDataWriterScanTest#concurrentScanFileCreatorDetector()}
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
	 * Used by {@link NexusScanDataWriterScanTest#concurrentScanRegisteredNexusDevice()}
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

	protected static class DummyNexusDetector extends DummyDetector implements NexusDetector {

		public static final String EXTERNAL_NEXUS_FILE_NAME = "external.nxs";

		// note 'value' causes a name conflict with the monitor's 'value' field when creating the NXdata group with NexusDataWriter
		public static final String FIELD_NAME_VALUE = "value_";
		public static final String FIELD_NAME_SPECTRUM = "spectrum";

		public static final int SPECTRUM_SIZE = 8;
		public static final int[] IMAGE_SIZE = { 8, 8 };

		public static final String NOTE_TEXT = "This is a note";
		public static final long DETECTOR_NUMBER = 1L;
		public static final String SERIAL_NUMBER = "ABC12345XYZ";
		public static final double DIAMETER = 52.2;
		public static final String DIAMETER_UNITS = "mm";

		public static final String STRING_ATTR_NAME = "stringAttr";
		public static final String INT_ATTR_NAME = "intAttr";
		public static final String FLOAT_ATTR_NAME = "floatAttr";
		public static final String ARRAY_ATTR_NAME = "arrayAttr";
		public static final String STRING_ATTR_VALUE = "stringVal";
		public static final int INT_ATTR_VALUE = 2;
		public static final double FLOAT_ATTR_VALUE = 5.432;
		public static final double[] ARRAY_ATTR_VALUE = { 1.23, 2.34, 3.45, 4.56, 5.67 };

		public static final String COLLECTION_NAME = "collection";
		public static final String COLLECTION_FIELD_NAME = "fieldName";
		public static final String COLLECTION_ATTR_NAME = "attrName";
		public static final String COLLECTION_FIELD_VALUE = "fieldValue";
		public static final String COLLECTION_ATTR_VALUE = "attrValue";

		public static final String FIELD_NAME_EXTERNAL = "external";

		private String outputDir = null;
		private int[] scanDimensions = null;
		private boolean firstData = true;
		private ILazyWriteableDataset externalDataset = null;
		private PositionIterator posIter = null;
		private String externalFilePath = null;

		public void setScanDimensions(int[] scanDimensions) {
			this.scanDimensions = scanDimensions;
			posIter = new PositionIterator(scanDimensions);
		}

		public void setOutputDir(String outputDir) {
			this.outputDir = outputDir;
		}

		@Override
		public NexusTreeProvider readout() throws DeviceException {
			return (NexusTreeProvider) super.readout();
		}

		@Override
		protected Object acquireData() {
			if (firstData) {
				createExternalNexusFile();
				firstData = false;
			}

			final NXDetectorData data = new NXDetectorData(this);

			/*
			 * Priorities set to be explictly non-alphabetical, non-order of insertion, with order of insertion being default when priority not set
			 */
			final NexusGroupData valueData = new NexusGroupData(Math.random() * Double.MAX_VALUE);
			data.addData(getName(), FIELD_NAME_VALUE, valueData);

			final NexusGroupData spectrumData = new NexusGroupData(Random.rand(SPECTRUM_SIZE));
			data.addData(getName(), FIELD_NAME_SPECTRUM, spectrumData);
			data.setPrioritisedData(getName(), FIELD_NAME_SPECTRUM, NexusExtractor.SDSClassName);

			final NexusGroupData imageData = new NexusGroupData(Random.rand(IMAGE_SIZE));
			data.addData(getName(), NXdetector.NX_DATA, imageData);

			// add an NXnote child group - NXDetectorData has a convenience method for this
			data.addNote(getName(), NOTE_TEXT);

			final INexusTree detTree = data.getDetTree(getName());

			// add some metadata dataset (i.e. per-scan or non point-dependent)
			detTree.addChildNode(new NexusTreeNode(NXdetector.NX_DETECTOR_NUMBER, NexusExtractor.SDSClassName, detTree,
					new NexusGroupData(DETECTOR_NUMBER)));
			detTree.addChildNode(new NexusTreeNode(NXdetector.NX_SERIAL_NUMBER, NexusExtractor.SDSClassName, detTree,
					new NexusGroupData(SERIAL_NUMBER)));
			final INexusTree diameterNode = new NexusTreeNode(NXdetector.NX_DIAMETER, NexusExtractor.SDSClassName, detTree,
					new NexusGroupData(DIAMETER));
			diameterNode.addChildNode(new NexusTreeNode(ATTRIBUTE_NAME_UNITS, NexusExtractor.AttrClassName, diameterNode,
					new NexusGroupData(DIAMETER_UNITS)));
			detTree.addChildNode(diameterNode);

			// add some attributes
			detTree.addChildNode(new NexusTreeNode(STRING_ATTR_NAME, NexusExtractor.AttrClassName, detTree,
					new NexusGroupData(STRING_ATTR_VALUE)));
			detTree.addChildNode(new NexusTreeNode(INT_ATTR_NAME, NexusExtractor.AttrClassName, detTree,
					new NexusGroupData(INT_ATTR_VALUE)));
			detTree.addChildNode(new NexusTreeNode(FLOAT_ATTR_NAME, NexusExtractor.AttrClassName, detTree,
					new NexusGroupData(FLOAT_ATTR_VALUE)));
			detTree.addChildNode(new NexusTreeNode(ARRAY_ATTR_NAME, NexusExtractor.AttrClassName, detTree,
					new NexusGroupData(ARRAY_ATTR_VALUE)));

			// add an NXcollection child group with a data node and an attribute
			final INexusTree collectionNode = new NexusTreeNode(COLLECTION_NAME, NexusExtractor.NXCollectionClassName, detTree);
			collectionNode.addChildNode(new NexusTreeNode(COLLECTION_FIELD_NAME, NexusExtractor.SDSClassName, collectionNode,
					new NexusGroupData(COLLECTION_FIELD_VALUE)));
			collectionNode.addChildNode(new NexusTreeNode(COLLECTION_ATTR_NAME, NexusExtractor.AttrClassName, collectionNode,
					new NexusGroupData(COLLECTION_ATTR_VALUE)));
			detTree.addChildNode(collectionNode);

			writeToExternalFile(); // write to dataset in external file

			final String externalTargetPath = "nxfile://" + externalFilePath + "#entry/data/data";
			data.addExternalFileLink(getName(), FIELD_NAME_EXTERNAL, externalTargetPath, false, true, 2);

			return data;
		}

		private void writeToExternalFile() {
			final IDataset dataToWrite = Random.rand(IMAGE_SIZE);

			if (!posIter.hasNext()) {
				// hasNext() actually moves the posIter to the next position(!);
				throw new NoSuchElementException("posIter ran out of positions, shape = " + Arrays.toString(scanDimensions));
			}

			final int[] start = posIter.getPos();
			final int[] stop = Arrays.stream(start).map(pos -> pos + 1).toArray();
			final SliceND scanSlice = new SliceND(scanDimensions, start, stop, null);

			try {
				IWritableNexusDevice.writeDataset(externalDataset, dataToWrite, scanSlice);
			} catch (DatasetException e) {
				throw new RuntimeException("Error writing to external file", e);
			}
		}

		private void createExternalNexusFile() {
			externalFilePath = outputDir + EXTERNAL_NEXUS_FILE_NAME;
			final TreeFile treeFile = NexusNodeFactory.createTreeFile(externalFilePath);
			final NXroot root = NexusNodeFactory.createNXroot();
			treeFile.setGroupNode(root); // the structure of the external file doesn't matter
			final NXentry entry = NexusNodeFactory.createNXentry();
			root.setEntry(entry);
			final NXdata data = NexusNodeFactory.createNXdata();
			entry.setData(data);

			final int datasetRank = scanDimensions.length + IMAGE_SIZE.length;
			externalDataset = data.initializeLazyDataset(NXdata.NX_DATA, datasetRank, Double.class);

			try {
				NexusTestUtils.saveNexusFile(treeFile);
			} catch (NexusException e) {
				throw new RuntimeException("Error creating external nexus file", e);
			}
		}

	}

	private static final String TEMPLATE_FILE_PATH = "testfiles/gda/scan/datawriter/simple-template.yaml";
	protected static final String METADATA_KEY_FEDERAL_ID = "federalid";
	protected static final String METADATA_KEY_INSTRUMENT = "instrument";

	protected static final String[] METADATA_SCANNABLE_NAMES = // names start from 0, so they match the index
			IntStream.rangeClosed(0, 8).mapToObj(i -> "meta"+i).toArray(String[]::new);

	protected static final int MAX_SCAN_RANK = 3; // larger scans take too long

	protected static final int[] EMPTY_SHAPE = new int[0];
	protected static final int[] SINGLE_VALUE_SHAPE = new int[] { 1 };

	protected static final int[] GRID_SHAPE = { 8, 5 };
	private static final int DEFAULT_NUM_AXIS_POINTS = 2;

	protected static final String ENTRY_NAME = "entry1";

	protected static final String ATTRIBUTE_NAME_LOCAL_NAME = "local_name";
	protected static final String ATTRIBUTE_NAME_TARGET = "target";
	protected static final String ATTRIBUTE_NAME_UNITS = "units";

	protected static final String INSTRUMENT_NAME = "instrument";
	protected static final String SCANNABLE_NAME_PREFIX = "scannable";
	protected static final String MONITOR_NAME = "mon01";

	protected static final int EXPECTED_SCAN_NUMBER = 1;
	protected static final String EXPECTED_ENTRY_IDENTIFER = "1";
	protected static final String EXPECTED_PROGRAM_NAME = "GDA 7.11.0";
	protected static final String EXPECTED_USER_GROUP_NAME = "user01";
	protected static final String EXPECTED_USER_ID = "abc12345";
	protected static final String EXPECTED_USER_NAME = "Ted Jones";

	protected static final double START_VALUE = 0.0;
	protected static final double STEP_SIZE = 1.0;
	protected static final double SCANNABLE_LOWER_BOUND = -123.456;
	protected static final double SCANNABLE_UPPER_BOUND = 987.654;

	public static final String[] COUNTER_TIMER_NAMES = { "one", "two", "three", "four" };

	protected static final String EXPECTED_MONOCHROMATOR_NAME = "myMonochromator";
	protected static final double EXPECTED_MONOCHROMATOR_ENERGY = 5.432;
	protected static final double EXPECTED_MONOCHROMATOR_WAVELENGTH = 543.34;
	protected static final double EXPECTED_INSERTION_DEVICE_GAP = 1.234;
	protected static final double EXPECTED_SOURCE_ENERGY = 3.0;
	protected static final double EXPECTED_SOURCE_CURRENT = 25.5;

	protected static final double MONITOR_VALUE = 2.5;

	private String outputDir;

	protected final int scanRank;
	protected final int[] scanDimensions;
	protected Scannable[] scannables;
	protected Monitor monitor;
	protected Set<String> expectedMetadataScannableNames;

	protected Detector detector;

	protected DetectorType detectorType; // the type of detector we're testing, in terms of nexus writing, e.g. counter timer

	private Object[] scanArguments;

	protected AbstractNexusDataWriterScanTest(int scanRank) {
		this.scanRank = scanRank;
		scanDimensions = new int[scanRank];
		// dimensions 0 and 1 use GRID_SHAPE, any remaining use DEFAULT_NUM_AXIS_POINTS to keep the
		// scan size small for higher dimension scans
		for (int i = 0; i < scanRank; i++) {
			scanDimensions[i] = i < GRID_SHAPE.length ? GRID_SHAPE[i] : DEFAULT_NUM_AXIS_POINTS;
		}
	}

	@AfterClass
	public static void tearDownServices() {
		GDAMetadataProvider.setInstanceForTesting(null);
	}

	@Before
	public void setUp() throws Exception {
		// setup devices
		this.scannables = new Scannable[scanRank];
		for (int i = 0; i < scanRank; i++) {
			final DummyUnitsScannable<Length> dummyScannable = new DummyUnitsScannable<>(
					SCANNABLE_NAME_PREFIX + i, 0.0, "mm", "mm");
			dummyScannable.setLowerGdaLimits(SCANNABLE_LOWER_BOUND);
			dummyScannable.setUpperGdaLimits(SCANNABLE_UPPER_BOUND);
			this.scannables[i] = dummyScannable;
		}

		final DummyMonitor dummyMonitor = new DummyMonitor();
		dummyMonitor.setConstantValue(MONITOR_VALUE);
		dummyMonitor.setName(MONITOR_NAME);
		dummyMonitor.configure();
		this.monitor = dummyMonitor;
	}

	protected void setUpMetadata() throws Exception {
		addMetadataEntry(METADATA_KEY_FEDERAL_ID, EXPECTED_USER_ID);
	}

	protected void addMetadataEntry(String metadataKey, Object value) {
		GDAMetadataProvider.getInstance().addMetadataEntry(new StoredMetadataEntry(metadataKey, value.toString()));
	}

	@After
	public void tearDown() {
		scannables = null;
		monitor = null;
		new ServiceHolder().setNexusWriterConfiguration(null);
	}

	protected void setUpTest(String testName) throws Exception {
		final String testDir = TestHelpers.setUpTest(this.getClass(), testName + scanRank + "d", true);
		outputDir = testDir + "/Data/";

		setUpMetadata();
		setupMetadataScannables();
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
			createScannable(name, i);

			final List<String> prerequisites = dependencies.get(i).stream().map(j -> METADATA_SCANNABLE_NAMES[j]).collect(toList());
			if (i == 0) prerequisites.add(scannables[0].getName());
			locationMap.put(name, createScannableWriter(name, prerequisites));
		}

		locationMap.put(scannables[0].getName(), createScannableWriter(scannables[0].getName(),
				List.of(METADATA_SCANNABLE_NAMES[5])));

		final NexusDataWriterConfiguration config = ServiceHolder.getNexusDataWriterConfiguration();
		config.setMetadataScannables(Sets.newHashSet(METADATA_SCANNABLE_NAMES[0], METADATA_SCANNABLE_NAMES[1]));
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

	protected void createScannable(final String name, double value) throws DeviceException {
		final DummyScannable scannable = new DummyScannable(name);
		scannable.moveTo(value);
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace(name, scannable);
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
				.collect(toSet());
	}

	private ScannableWriter createScannableWriter(String scannableName, List<String> prerequisiteNames) {
		final SingleScannableWriter writer = new SingleScannableWriter();
		writer.setPaths(new String[] { String.format(
				"instrument:NXinstrument/%s:NXpositioner/%s", scannableName, scannableName ) });
		writer.setUnits(new String[] { "mm" });
		writer.setPrerequisiteScannableNames(prerequisiteNames);
		return writer;
	}

	@Test
	public void concurrentScanNoDetector() throws Exception {
		concurrentScan(null, DetectorType.NONE, "NoDetector");
	}

	@Test
	public void concurrentScanNoDetectorOrMonitor() throws Exception {
		monitor = null;
		concurrentScan(null, DetectorType.NONE, "NoDetectorOrMonitor");
	}

	@Test
	public void concurrentScanCounterTimer() throws Exception {
		final DummyCounterTimer detector = new DummyCounterTimer();
		detector.setName("counterTimer");
		detector.setDataDecimalPlaces(3);
		detector.setUseGaussian(true);
		detector.setInputNames(new String[0]);

		detector.setExtraNames(COUNTER_TIMER_NAMES);
		detector.setTotalChans(COUNTER_TIMER_NAMES.length);
		detector.setTimerName("timer");
		detector.configure();
		detector.setCollectionTime(10.0);

		detector.setOutputFormat(Collections.nCopies(
				COUNTER_TIMER_NAMES.length, ScannableBase.DEFAULT_OUTPUT_FORMAT).toArray(String[]::new));

		concurrentScan(detector, DetectorType.COUNTER_TIMER, "CounterTimer");
	}

	@Test
	public void concurrentScanGenericDetector_scalarData() throws Exception {
		detector = new DummyGenericDetector(1);
		detector.setName("Generic Detector");
		concurrentScan(detector, DetectorType.GENERIC, "GenericDetector_scalarData");
	}

	@Test
	public void concurrentScanGenericDetector_arrayData() throws Exception {
		detector = new DummyGenericDetector(6);
		detector.setName("Generic Detector");
		concurrentScan(detector, DetectorType.GENERIC, "GenericDetector_arrayData");
	}

	@Test
	public void concurrentScanFileCreatorDetector() throws Exception {
		detector = new DummyFileCreatorDetector();
		detector.setName("fileCreatorDetector");
		concurrentScan(detector, DetectorType.FILE_CREATOR, "FileCreatorDetector");
	}

	@Test
	public void concurrentScanNexusDetectorWithPrimaryFieldSet() throws Exception {
		detector = new DummyNexusDetector() {
			@Override
			public Object acquireData() {
				final NXDetectorData data = (NXDetectorData) super.acquireData();
				data.setPrioritisedData(getName(), FIELD_NAME_EXTERNAL, NexusExtractor.ExternalSDSLink);
				return data;
			}

		};
		final DummyNexusDetector castDetector = (DummyNexusDetector) detector;
		castDetector.setScanDimensions(scanDimensions);

		detector.setName("nexusDetector");
		concurrentScan(detector, DetectorType.MODIFIED_NEXUS_DETECTOR, "NexusDetector");
	}

	@Test
	public void concurrentScanNexusDetector() throws Exception {
		detector = new DummyNexusDetector();
		((DummyNexusDetector) detector).setScanDimensions(scanDimensions);

		detector.setName("nexusDetector");
		concurrentScan(detector, DetectorType.NEXUS_DETECTOR, "NexusDetector");
	}

	protected void concurrentScan(Detector detector, DetectorType detectorType, String testSuffix) throws Exception {
		this.detector = detector;
		this.detectorType = detectorType;

		setUpTest("concurrentScan" + testSuffix); // create test dir and initialize properties
		if (detectorType == DetectorType.NEXUS_DETECTOR) {
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

	protected abstract String[] getExpectedPositionerNames();

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
		final String[] expectedPositionerNames = getExpectedPositionerNames();
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

		switch (detectorType) {
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
			case NEXUS_DETECTOR:
			case MODIFIED_NEXUS_DETECTOR:
				checkNexusDetector(detectorGroup);
				break;
			default:
				throw new IllegalArgumentException("Unknown detector type: " + detectorType);
		}
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

		for (String name : detector.getExtraNames()) {
			final DataNode dataNode = detGroup.getDataNode(name);
			assertThat(dataNode, is(notNullValue()));
			checkDatasetWritten(dataNode.getDataset(), EMPTY_SHAPE);
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

	private void checkNexusDetector(NXdetector detGroup) throws Exception {
		assertThat(detGroup.getGroupNodeNames(), containsInAnyOrder("note", COLLECTION_NAME));
		assertThat(detGroup.getDataNodeNames(), containsInAnyOrder(NXdetector.NX_DATA, // primary fields, written at each point
				FIELD_NAME_SPECTRUM, FIELD_NAME_VALUE, FIELD_NAME_EXTERNAL,
				NXdetector.NX_LOCAL_NAME, // added for all detectors
				NXdetector.NX_DETECTOR_NUMBER, NXdetector.NX_DIAMETER, NXdetector.NX_SERIAL_NUMBER));

		assertThat(detGroup.getLocal_nameScalar(), is(equalTo(detector.getName())));
		checkDatasetWritten(detGroup.getDataNode(FIELD_NAME_VALUE).getDataset(), EMPTY_SHAPE);
		checkDatasetWritten(detGroup.getDataNode(FIELD_NAME_SPECTRUM).getDataset(),
				new int[] { DummyNexusDetector.SPECTRUM_SIZE });
		checkDatasetWritten(detGroup.getDataNode(NXdetector.NX_DATA).getDataset(), DummyNexusDetector.IMAGE_SIZE);
		checkDatasetWritten(detGroup.getDataNode(FIELD_NAME_EXTERNAL).getDataset(), DummyNexusDetector.IMAGE_SIZE);

		assertThat(detGroup.getDetector_numberScalar(), is(DETECTOR_NUMBER));
		assertThat(detGroup.getSerial_numberScalar(), is(equalTo(SERIAL_NUMBER)));

		assertThat(detGroup.getAttributeNames(), containsInAnyOrder(NexusConstants.NXCLASS,
				STRING_ATTR_NAME, INT_ATTR_NAME, FLOAT_ATTR_NAME, ARRAY_ATTR_NAME));
		assertThat(detGroup.getAttr(null, STRING_ATTR_NAME).getSlice(),
				is(equalTo(DatasetFactory.createFromObject(STRING_ATTR_VALUE))));
		assertThat(detGroup.getAttr(null, INT_ATTR_NAME).getSlice(),
				is(equalTo(DatasetFactory.createFromObject(new int[] { INT_ATTR_VALUE })))); // written as 1d dataset of size 1 rather than scalar dataset
		assertThat(detGroup.getAttr(null, FLOAT_ATTR_NAME).getSlice(),
				is(equalTo(DatasetFactory.createFromObject(new double[] { FLOAT_ATTR_VALUE })))); // written as 1d dataset of size 1 rather than scalar dataset
		assertThat(detGroup.getAttr(null, ARRAY_ATTR_NAME).getSlice(),
				is(equalTo(DatasetFactory.createFromObject(ARRAY_ATTR_VALUE))));

		final NXnote note = (NXnote) detGroup.getGroupNode("note");
		assertThat(note, is(notNullValue()));
		assertThat(note.getDataNodeNames(), containsInAnyOrder(NXnote.NX_TYPE, NXnote.NX_DESCRIPTION));
		assertThat(note.getTypeScalar(), is(equalTo("text/plain")));
		assertThat(note.getDescriptionScalar(), is(equalTo("This is a note")));
	}

	protected abstract void checkMonitor(NXinstrument instrument) throws Exception;

	protected abstract void checkConfiguredScannablePositioner(String scannableName, NXpositioner positioner) throws Exception;

	protected abstract void checkDefaultScannablePositioner(NXpositioner positioner, int scanIndex) throws Exception;

	protected abstract void checkInstrumentGroupMetadata(NXinstrument instrument);

	protected abstract void checkDataGroups(NXentry entry) throws Exception;

	protected abstract void checkMetadataScannablePositioner(NXpositioner positioner, int index) throws Exception;

	protected void checkNexusMetadata(NXentry entry) {
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
		assertThat(data.getDataNodeNames(), containsInAnyOrder(
				expectedDataNodeLinks.keySet().toArray(new String[expectedDataNodeLinks.size()])));

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

}
