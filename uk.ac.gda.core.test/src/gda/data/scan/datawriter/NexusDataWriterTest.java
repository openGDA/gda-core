/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

import static java.util.stream.Collectors.toList;
import static org.eclipse.scanning.test.utilities.scan.nexus.NexusAssert.assertGroupNodesEqual;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXcollection;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXmonitor;
import org.eclipse.dawnsci.nexus.NXnote;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NXuser;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.nexus.appender.NexusMetadataAppender;
import org.eclipse.dawnsci.nexus.appender.impl.NexusFileAppenderService;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.dawnsci.nexus.device.SimpleNexusDevice;
import org.eclipse.dawnsci.nexus.device.impl.NexusDeviceService;
import org.eclipse.dawnsci.nexus.template.impl.NexusTemplateServiceImpl;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import gda.TestHelpers;
import gda.configuration.properties.LocalProperties;
import gda.data.ServiceHolder;
import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.data.scan.datawriter.scannablewriter.ScannableWriter;
import gda.data.scan.datawriter.scannablewriter.SingleScannableWriter;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.NexusDetector;
import gda.jython.InterfaceProvider;
import gda.scan.ScanDataPoint;

/**
 * Unit test class for {@link NexusDataWriter}.
 * Note this test class is far from a full unit test for NexusDataWriter. It doesn't run a full scan,
 * see {@link NexusDataWriterScanTest} for that. Instead it calls {@link NexusDataWriter#addData(gda.scan.IScanDataPoint)}
 * with a scan point containing one of each type of detector (in terms of the methods that NexusDataWriter uses to
 * write them), and scannables configured with {@link SingleScannableWriter}s.
 * It also tests that metadata scannables are calculated correctly, and that any templates are applied.
 */
public class NexusDataWriterTest {

	private enum DetectorType {

		/**
		 * This detector is written to in makeGenericDetector (including if/then blocks for NexusDetetctor),
		 * it also has a monitor (NexusDetector can return multiple nexus objects, as some subclasses can control multiple devices).
		 */
		NEXUS_DETECTOR("nexusDetector") {

			private static final String MONITOR_NAME = "nexusDetectorMonitor";

			@Override
			public Detector getDetector() {
				final Detector nexusDetector = mock(NexusDetector.class);
				when(nexusDetector.getName()).thenReturn(DetectorType.NEXUS_DETECTOR.getName());
				return nexusDetector;
			}

			@Override
			public Object getDetectorData() {
				final NexusTreeProvider nexusTreeProvider = mock(NexusTreeProvider.class);
				final INexusTree nexusTreeRoot = new NexusTreeNode("", NexusExtractor.NXInstrumentClassName, null);
				nexusTreeRoot.addChildNode(new NexusTreeNode(DetectorType.NEXUS_DETECTOR.getName(), NexusExtractor.NXDetectorClassName, null));
				nexusTreeRoot.addChildNode(new NexusTreeNode(MONITOR_NAME, NexusExtractor.NXMonitorClassName, null));
				when(nexusTreeProvider.getNexusTree()).thenReturn(nexusTreeRoot);
				return nexusTreeProvider;
			}

			@Override
			public void addExpectedDataField(NXdetector expectedDetectorGroup, NXdetector actualDetectorGroup) {
				expectedDetectorGroup.setLocal_nameScalar(getName());
			}

			@Override
			protected Set<String> getMetadataFieldsWrittenByNexusDataWriter() {
				return new HashSet<>(Arrays.asList(NXdetector.NX_LOCAL_NAME));
			}

			@Override
			public void createAndRegisterAppender() {
				super.createAndRegisterAppender();

				// also create and register an appender for the monitor
				final NexusMetadataAppender<NXdetector> appender = new NexusMetadataAppender<>(MONITOR_NAME);
				final Map<String, Object> metadata = new HashMap<>();
				metadata.put("name", MONITOR_NAME);
				appender.setNexusMetadata(metadata);
				ServiceHolder.getNexusFileAppenderService().register(appender);
			}

			@Override
			public void checkNexusEntry(NXentry entry) throws Exception {
				super.checkNexusEntry(entry);

				// Also check the monitor group for the monitor subtree
				final String monitorGroupPath = "/entry1/" + MONITOR_NAME;
				final NXmonitor monitorGroup = entry.getMonitor(MONITOR_NAME);
				final NXmonitor expectedMonitorGroup = NexusNodeFactory.createNXmonitor();
				expectedMonitorGroup.setField("name", MONITOR_NAME);
				assertGroupNodesEqual(monitorGroupPath, expectedMonitorGroup, monitorGroup);
			}

		},

		/**
		 * This detector is written to in makeFileCreatorDetector
		 */
		FILE_CREATOR_DETECTOR("fileCreatorDetector") {
			@Override
			public Detector getDetector() throws Exception {
				final Detector ownFilesDetector = mock(Detector.class);
				when(ownFilesDetector.getName()).thenReturn(DetectorType.FILE_CREATOR_DETECTOR.getName());
				when(ownFilesDetector.createsOwnFiles()).thenReturn(true);
				return ownFilesDetector;
			}

			@Override
			public Object getDetectorData() {
				return "det-file.nxs";
			}

			@Override
			public void addExpectedDataField(NXdetector expectedDetectorGroup, NXdetector actualDetectorGroup) {
				expectedDetectorGroup.setTypeScalar("Detector");
				expectedDetectorGroup.setDescriptionScalar("Generic GDA Detector - External Files");
				final NXnote dataFileNote = NexusNodeFactory.createNXnote();
				// Note: makeFileCreatorDetector calls nexusFile.addData for the dataset below, which gets written to disk, but doesn't change the GroupNode in memory
				// ILazyWriteableDataset dataFileDataset = NexusUtils.createLazyWriteableDataset("file_name", String.class, new int[] { 1 }, null, null);
				// dataFileNote.initializeLazyDataset("file_name", 1, String.class);
				dataFileNote.addAttribute(TreeFactory.createAttribute("data_filename", new int[] { 1 }));
				final IDataset fileNameDataset = DatasetFactory.createFromObject(getDetectorData()).reshape(1, 1);
				dataFileNote.setFile_name(fileNameDataset);
				expectedDetectorGroup.addGroupNode("data_file", dataFileNote);
			}

		},

		/**
		 * This detector is written to in makeCounterTimer
		 */
		COUNTER_TIMER("counterTimer") {

			private String[] extraNames = { "extra1", "extra2" };

			@Override
			public Detector getDetector() throws Exception {
				final Detector counterTimer = mock(Detector.class);
				when(counterTimer.getExtraNames()).thenReturn(extraNames);
				when(counterTimer.getName()).thenReturn(DetectorType.COUNTER_TIMER.getName());
				when(counterTimer.getDescription()).thenReturn("counter timer description");
				when(counterTimer.getDetectorType()).thenReturn("counter timer");
				when(counterTimer.getDetectorID()).thenReturn("counterTimer");
				return counterTimer;
			}

			@Override
			public Object getDetectorData() {
				return new double[] { 1.234, 5.678 };
			}

			@Override
			public void addExpectedDataField(NXdetector expectedDetectorGroup, NXdetector actualDetectorGroup) throws NexusException {
				expectedDetectorGroup.setDescriptionScalar("counter timer description");
				expectedDetectorGroup.setTypeScalar("counter timer");
				expectedDetectorGroup.setField("id", "counterTimer");
				for (String extraName : extraNames) {
					// for these fields, its easiest to just add the actual dataNodes added by makeCounterTimer
					expectedDetectorGroup.addDataNode(extraName, actualDetectorGroup.getDataNode(extraName));
				}
			}
		},

		/**
		 * This detector is written to in makeGenericDetector, including the else blocks for if instanceof NexusDetector / else tests
		 */
		GENERIC_DETECTOR("genericDetector") {
			@Override
			public Detector getDetector() throws Exception {
				final Detector genericDetector = mock(Detector.class);
				when(genericDetector.getName()).thenReturn(DetectorType.GENERIC_DETECTOR.getName());
				when(genericDetector.getExtraNames()).thenReturn(new String[0]);
				when(genericDetector.getDataDimensions()).thenReturn(new int[] { 3 });
				when(genericDetector.getDescription()).thenReturn("generic detector description");
				when(genericDetector.getDetectorType()).thenReturn("generic detector");
				when(genericDetector.getDetectorID()).thenReturn("genericDetector");
				return genericDetector;
			}

			@Override
			public Object getDetectorData() {
				return new double[] { 1.23, 4.56, 7.89 };
			}

			@Override
			public void addExpectedDataField(NXdetector expectedDetectorGroup, NXdetector actualDetectorGroup) throws NexusException {
				expectedDetectorGroup.addDataNode(NXdetector.NX_DATA, actualDetectorGroup.getDataNode(NXdetector.NX_DATA));
				expectedDetectorGroup.setDescriptionScalar("generic detector description");
				expectedDetectorGroup.setTypeScalar("generic detector");
				expectedDetectorGroup.setField("id", "genericDetector");
			}

		};

		private String name;

		private DetectorType(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		/**
		 * Returns the detector to be written, a Mockito mock
		 * @return detector
		 * @throws Exception
		 */
		public abstract Detector getDetector() throws Exception;

		/**
		 * The detector data to be added to the single scan point.
		 * The type of this data varies for each detector type
		 * @return detector data
		 */
		public abstract Object getDetectorData();

		public void createAndRegisterAppender() {
			final Map<String, Object> detMetadata = createAppenderMetadata();
			NexusDataWriterTest.createAndRegisterAppender(getName(), detMetadata);
		}

		protected Map<String, Object> createAppenderMetadata() {
			final Map<String, Object> metadata = new HashMap<>();
			metadata.put(NXdetector.NX_DESCRIPTION, "description of detector " + (ordinal() + 1));
			metadata.put(NXdetector.NX_LAYOUT, "area");
			metadata.put(NXdetector.NX_LOCAL_NAME, getName());
			metadata.put(NXdetector.NX_DETECTOR_NUMBER, ordinal() + 1);
			metadata.put(NXdetector.NX_DETECTOR_READOUT_TIME, 0.012);

			// remove nodes created by NexusDataWriter from the metadata used by the appender
			metadata.keySet().removeIf(getMetadataFieldsWrittenByNexusDataWriter()::contains);

			return metadata;
		}

		/**
		 * Returns the names of metadata fields written to the nexus file by NexusDataWriter for this type
		 * of detector. createAppenderMetadata
		 * @return metdata
		 */
		protected Set<String> getMetadataFieldsWrittenByNexusDataWriter() {
			// NexusDataWriter writes 'description' for most types of detectors
			return new HashSet<>(Arrays.asList(NXdetector.NX_DESCRIPTION));
		}

		/**
		 * Add the expected data to the given group
		 * @param expectedDetectorGroup
		 * @throws NexusException
		 */
		public abstract void addExpectedDataField(NXdetector expectedDetectorGroup, NXdetector actualDetectorGroup) throws NexusException;

		private NXdetector createExpectedDetectorGroup(NXdetector actualDetectorGroup) throws NexusException {
			final NXdetector expectedDetectorGroup = NexusNodeFactory.createNXdetector();
			final Map<String, Object> expectedMetadata = createAppenderMetadata();
			// add the fields added by the the appender to the expected detector group
			for (Map.Entry<String, Object> metadataEntry : expectedMetadata.entrySet()) {
				expectedDetectorGroup.setField(metadataEntry.getKey(), metadataEntry.getValue());
			}

			// add fields written by NexusDataWriter rather than the appender
			addExpectedDataField(expectedDetectorGroup, actualDetectorGroup);
			return expectedDetectorGroup;
		}

		public void checkNexusEntry(NXentry entry) throws Exception {
			final NXdetector detectorGroup = entry.getInstrument().getDetector(getName());
			assertThat(detectorGroup, is(notNullValue()));
			final NXdetector expectedDetectorGroup = createExpectedDetectorGroup(detectorGroup);
			assertGroupNodesEqual("/entry1/instrument/" + getName(), expectedDetectorGroup, detectorGroup);
		}

	}

	private static final String TEMPLATE_FILE_PATH = "testfiles/gda/scan/datawriter/NexusDataWriterTest/simple-template.yaml";

	private static final String USER_DEVICE_NAME = "user";
	private static final String[] SCANNABLE_NAMES = { "stage_y", "stage_x" };
	private static final String[] METADATA_SCANNABLES_NAMES = // names start from 0, so they match the index
			IntStream.rangeClosed(0, 8).mapToObj(i -> "meta"+i).toArray(String[]::new);

	private String testScratchDirectoryName;

	private INexusDataWriter nexusDataWriter;

	private String nexusFilePath;

	private INexusDevice<NXuser> userDevice;

	@Before
	public void setUp() throws Exception {
		testScratchDirectoryName = TestHelpers.setUpTest(NexusDataWriterTest.class, "", true);

		// create the services
		final ServiceHolder serviceHolder = new ServiceHolder();
		serviceHolder.setNexusTemplateService(new NexusTemplateServiceImpl());
		serviceHolder.setNexusDeviceService(new NexusDeviceService());
		serviceHolder.setNexusFileAppenderService(new NexusFileAppenderService());

		// create and configure the NexusDataWriter
		LocalProperties.set(NexusDataWriter.GDA_NEXUS_CREATE_SRS, "false");
		nexusDataWriter = new NexusDataWriter();
		nexusDataWriter.configureScanNumber(1);
		nexusDataWriter.setNexusFileNameTemplate("scan-%d.nxs");
		assertThat(nexusDataWriter.getCurrentScanIdentifier(), is(1));
		assertThat(nexusDataWriter.getNexusFileName(), is(equalTo("scan-1.nxs")));
		assertThat(nexusDataWriter.getCurrentFileName(), endsWith(testScratchDirectoryName.toString() + "Data/scan-1.nxs"));
		nexusFilePath = nexusDataWriter.getCurrentFileName();
	}

	@After
	public void tearDown() {
		new File(nexusDataWriter.getCurrentFileName()).delete();
		new File(testScratchDirectoryName).delete();
		NexusDataWriter.clearConfiguration();
	}

	@Test
	public void testNexusDataWriter() throws Exception {
		// Arrange: create a nexus device and register it with the nexus device service
		final NXuser user = createUserGroup();
		final NexusObjectProvider<NXuser> userProvider = new NexusObjectWrapper<>(USER_DEVICE_NAME, user);
		userDevice = new SimpleNexusDevice<>(userProvider);
		ServiceHolder.getNexusDeviceService().register(userDevice);

		// Set the location of the template file
		final String templateFileAbsolutePath = Paths.get(TEMPLATE_FILE_PATH).toAbsolutePath().toString();
		ServiceHolder.getNexusDataWriterConfiguration().setNexusTemplateFiles(Arrays.asList(templateFileAbsolutePath));

		// Create a nexus appender for each detector and register it with the nexus file appender service
		for (DetectorType detectorType : DetectorType.values()) {
			detectorType.createAndRegisterAppender();
		}

		// Create a nexus appender for the first scannable only (second has location map entry)
		createAndRegisterAppender(SCANNABLE_NAMES[0], createScannableMetadata(SCANNABLE_NAMES[0]));
		createAndConfigureMetadataScannables();

		// Act: write a point. Writing the first point causes the nexus file to be created.
		final ScanDataPoint firstPoint = createScanDataPoint();
		nexusDataWriter.addData(firstPoint);
		nexusDataWriter.completeCollection();

		// check that the file has been created as expected
		assertThat(new File(nexusFilePath).exists(), is(true));

		// We should add tests for the rest of NexusDataWriters behaviour
		try (NexusFile nexusFile = new NexusFileFactoryHDF5().newNexusFile(nexusFilePath)) {
			nexusFile.openToRead();
			checkNexusFile(nexusFile);
		}
	}

	private void checkNexusFile(NexusFile nexusFile) throws Exception {
		final TreeFile nexusTree = NexusUtils.loadNexusTree(nexusFile);
		final NXroot nexusRoot = (NXroot) nexusTree.getGroupNode();
		assertThat(nexusRoot, is(notNullValue()));
		final NXentry entry = nexusRoot.getEntry("entry1");
		assertThat(entry, is (notNullValue()));

		// check that the scan entry group created by the template has been added (note: we don't test the content here)
		assertThat(nexusRoot.getEntry("scan"), is(notNullValue()));

		final NXuser userGroup = entry.getUser(USER_DEVICE_NAME);
		assertThat(userGroup, is(notNullValue()));
		final NXcollection userCollectionGroup = (NXcollection) userGroup.getGroupNode("collection");
		assertThat(userCollectionGroup, is(notNullValue()));

		// check that the user group added by an INexusDevice has been added
		final NXuser expectedUserGroup = userDevice.getNexusProvider(null).getNexusObject();
		assertThat(userGroup, is(not(sameInstance(expectedUserGroup)))); // because its been read in from the file
		assertGroupNodesEqual("/entry1/" + USER_DEVICE_NAME, expectedUserGroup, userGroup);

		// check that the metadata has been added for each detector
		for (DetectorType detectorType : DetectorType.values()) {
			detectorType.checkNexusEntry(entry);
		}

		// check that the metadata for the first scannable has been written correctly
		// (note: the second scannables doesn't have metadata)
		for (String scannableName : SCANNABLE_NAMES) {
			checkScannable(entry, scannableName, false);
		}

		// check that the correct metadata scannables have been written
		for (String metadataScannableName : METADATA_SCANNABLES_NAMES) {
			checkScannable(entry, metadataScannableName, true);
		}
	}

	private void createAndConfigureMetadataScannables() throws DeviceException {
		for (String metadataScannableName : METADATA_SCANNABLES_NAMES) {
			final Scannable metadataScannable = createMockScannable(metadataScannableName);
			InterfaceProvider.getJythonNamespace().placeInJythonNamespace(metadataScannableName, metadataScannable);
		}
		// add an appender for the first metadata scannable
		createAndRegisterAppender(METADATA_SCANNABLES_NAMES[0], createScannableMetadata(METADATA_SCANNABLES_NAMES[0]));

		// beside the 'user' nexus device only the first two metadata scannables are added directly
		NexusDataWriter.setMetadatascannables(new HashSet<>(Arrays.asList(USER_DEVICE_NAME,
				METADATA_SCANNABLES_NAMES[0], METADATA_SCANNABLES_NAMES[1])));

		// Add dependencies from detectors to metadata scannables
		final Map<String, Collection<String>> metadataScannablesPerDetector = new HashMap<>();
		// nexusDetector depends on meta2
		metadataScannablesPerDetector.put(DetectorType.NEXUS_DETECTOR.getName(), Arrays.asList(METADATA_SCANNABLES_NAMES[2]));
		// counterTimer depends on meta1 and meta4
		metadataScannablesPerDetector.put(DetectorType.COUNTER_TIMER.getName(),
				Arrays.asList(METADATA_SCANNABLES_NAMES[1], METADATA_SCANNABLES_NAMES[4]));
		NexusDataWriter.setMetadataScannablesPerDetector(metadataScannablesPerDetector);

		final Multimap<Integer, Integer> dependencies = ArrayListMultimap.create();
		dependencies.put(1, 5);
		dependencies.putAll(2, Arrays.asList(0, 7));
		dependencies.putAll(4, Arrays.asList(1, 6, 7));
		dependencies.put(5, 6);
		dependencies.put(7, 8);

		// add dependencies between scannables using the location map
		final Map<String, ScannableWriter> locationMap = new HashMap<>();
		for (int i = 0; i < METADATA_SCANNABLES_NAMES.length; i++) {
			final String name = METADATA_SCANNABLES_NAMES[i];
			final List<String> prerequisites = dependencies.get(i).stream().map(j -> METADATA_SCANNABLES_NAMES[j]).collect(toList());
			if (i == 0) prerequisites.add("stage_x");
			locationMap.put(name, createScannableWriter(name, prerequisites));
		}

		// stage_x depends on meta5
		locationMap.put(SCANNABLE_NAMES[1], createScannableWriter(SCANNABLE_NAMES[1],
				Arrays.asList(METADATA_SCANNABLES_NAMES[5])));

		NexusDataWriter.setLocationmap(locationMap);
	}

	private NXuser createUserGroup() {
		final NXuser user = NexusNodeFactory.createNXuser();
		user.setNameScalar("John Smith");
		user.setRoleScalar("Beamline Scientist");
		user.setAddressScalar("Diamond Light Source, Didcot, Oxfordshire, OX11 0DE");
		user.setEmailScalar("john.smith@diamond.ac.uk");
		user.setFacility_user_idScalar("abc12345");
		final NXcollection collection = NexusNodeFactory.createNXcollection(); // to check that child groups are added
		collection.setField("foo", "bar");
		user.addGroupNode("collection", collection);
		return user;
	}

	private SingleScannableWriter createScannableWriter(String scannableName, List<String> prerequisiteNames) {
		final SingleScannableWriter writer = new SingleScannableWriter();
		writer.setPaths(new String[] { String.format(
				"instrument:NXinstrument/%s:NXpositioner/%s", scannableName, scannableName ) });
		writer.setUnits(new String[] { "mm" });
		writer.setPrerequisiteScannableNames(prerequisiteNames);
		return writer;
	}

	private void checkScannable(NXentry entry, String scannableName, boolean isMetadata) throws Exception {
		final GroupNode parentNode = entry.getInstrument();
		final GroupNode scannableGroup = parentNode.getGroupNode(scannableName);
		if (scannableName.equals(METADATA_SCANNABLES_NAMES[3])) { // no dependency on meta3
			assertThat(scannableGroup, is(nullValue()));
			return;
		}

		assertThat(scannableGroup, is(notNullValue()));
		final GroupNode expectedScannableGroup = createExpectedScannableGroup(scannableName, isMetadata);
		assertGroupNodesEqual("/entry1/instrument/" + scannableName, expectedScannableGroup, scannableGroup);
	}

	private NXobject createExpectedScannableGroup(String scannableName, boolean isMetadata) {
		final NXpositioner expectedGroup = NexusNodeFactory.createNXpositioner();
		final Map<String, Object> expectedMetadata = createScannableMetadata(scannableName);
		// add the fields added by the the appender to the expected detector group
		if (!isMetadata || scannableName.equals(METADATA_SCANNABLES_NAMES[0])) { // only the first metadata scannable has an appender
			for (Map.Entry<String, Object> metadataEntry : expectedMetadata.entrySet()) {
				expectedGroup.setField(metadataEntry.getKey(), metadataEntry.getValue());
			}
		}

		final DataNode valueDataNode = NexusNodeFactory.createDataNode();
		final Dataset value = DatasetFactory.createFromObject(0.01); // all scannables have this value in this test
		final IDataset valueDataset = isMetadata ? value.reshape(1) : value.reshape(1, 1);
		valueDataNode.setDataset(valueDataset);

		setAttribute(valueDataNode, "axis", isMetadata ? "1" : "1,2");
		setAttribute(valueDataNode, "local_name", scannableName + "." + scannableName);

		if (scannableName.equals(SCANNABLE_NAMES[0])) { // 'stage_y' is written by a SingleScannableWriter
			setAttribute(valueDataNode, "target", "/entry1/instrument" + scannableName + "/" + scannableName);
			setAttribute(valueDataNode, "label", "1");
			setAttribute(valueDataNode, "primary", "1");
		} else if (scannableName.equals(SCANNABLE_NAMES[1])) { // 'stage_x' is written by makeScannablesAndMonitors
			setAttribute(valueDataNode, "target", "/entry1/instrument" + scannableName + "/" + scannableName);
			setAttribute(valueDataNode, "units", "mm");
		} else {
			setAttribute(valueDataNode, "units", "mm");
		}

		expectedGroup.addDataNode(scannableName, valueDataNode);
		return expectedGroup;
	}

	private void setAttribute(DataNode dataNode, String attrName, Object attrValue) {
		dataNode.addAttribute(TreeFactory.createAttribute(attrName, attrValue));
	}

	private Map<String, Object> createScannableMetadata(String name) {
		if (name.equals(SCANNABLE_NAMES[0])) {
			// The first scannable has an appender registered which adds these fields
			final Map<String, Object> metadata = new HashMap<>();
			metadata.put(NXpositioner.NX_NAME, name);
			metadata.put(NXpositioner.NX_DESCRIPTION, "description of " + name);
			return metadata;
		}
		return Collections.emptyMap();
	}

	public static void createAndRegisterAppender(String name, Map<String, Object> metadata) {
		final NexusMetadataAppender<NXdetector> appender = new NexusMetadataAppender<>(name);
		appender.setNexusMetadata(metadata);
		ServiceHolder.getNexusFileAppenderService().register(appender);
	}

	private ScanDataPoint createScanDataPoint() throws Exception {
		// NexusDataWriter writes nexus structure the first data point is added, according to the devices in that point
		final ScanDataPoint firstPoint = new ScanDataPoint();
		firstPoint.setCurrentPointNumber(0);
		firstPoint.setNumberOfPoints(40);
		firstPoint.setScanDimensions(new int[] { 5, 3 });

		// add detectors to the the point
		for (DetectorType detectorType : DetectorType.values()) {
			firstPoint.addDetector(detectorType.getDetector());
			firstPoint.addDetectorData(detectorType.getDetectorData(), null);
		}

		for (String scannableName : SCANNABLE_NAMES) {
			final Scannable scannable = createMockScannable(scannableName);
			firstPoint.addScannable(scannable);
			firstPoint.addScannablePosition(scannable.getPosition(), null);
		}

		return firstPoint;
	}

	private Scannable createMockScannable(String scannableName) throws DeviceException {
		final Scannable scannable = mock(Scannable.class);
		when(scannable.getName()).thenReturn(scannableName);
		// is GDA8 the input name has to be the scannable name, as that is used in the NXdata group. If the correct name
		// 'value' was used (according to the nexus standard for NXpositioner) that was cause a name clash in the NXdata groups
		when(scannable.getInputNames()).thenReturn(new String[] { scannableName });
		when(scannable.getExtraNames()).thenReturn(new String[0]);
		when(scannable.getPosition()).thenReturn(0.01);

		return scannable;
	}

}
