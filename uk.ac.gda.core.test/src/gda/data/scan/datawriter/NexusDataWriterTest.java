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

import static org.eclipse.scanning.test.utilities.scan.nexus.NexusAssert.assertGroupNodesEqual;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.nexus.NXcollection;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXmonitor;
import org.eclipse.dawnsci.nexus.NXnote;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXuser;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.appender.NexusMetadataAppender;
import org.eclipse.dawnsci.nexus.appender.impl.NexusFileAppenderService;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.dawnsci.nexus.device.SimpleNexusDevice;
import org.eclipse.dawnsci.nexus.device.impl.NexusDeviceService;
import org.eclipse.dawnsci.nexus.template.impl.NexusTemplateServiceImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import gda.TestHelpers;
import gda.configuration.properties.LocalProperties;
import gda.data.ServiceHolder;
import gda.data.nexus.NexusFileFactory;
import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeNode;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.NexusDetector;
import gda.jython.InterfaceProvider;
import gda.scan.ScanDataPoint;

/**
 * Test class for {@link NexusDataWriter}.
 * Note: at of writing (2019-08-22) this only tests that templates are applied.
 * If we ever get around to testing the rest of NexusDataWriter's behaviour, those tests should be added to this class.
 */
public class NexusDataWriterTest {

	private static final String TEMPLATE_FILE_PATH = "testfiles/gda/scan/datawriter/NexusDataWriterTest/simple-template.yaml";

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
			public void addExpectedDataField(NXdetector expectedDetectorGroup, NexusFile nexusFile) {
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
			public void checkNexusFile(NexusFile nexusFile) throws Exception {
				super.checkNexusFile(nexusFile);

				// Also check the monitor group for the monitor subtree
				final String monitorGroupPath = "/entry1/" + MONITOR_NAME;
				final GroupNode monitorGroup = nexusFile.getGroup(monitorGroupPath, false);
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
			public void addExpectedDataField(NXdetector expectedDetectorGroup, NexusFile nexusFile) {
				expectedDetectorGroup.setTypeScalar("Detector");
				expectedDetectorGroup.setDescriptionScalar("Generic GDA Detector - External Files");
				final NXnote dataFileNote = NexusNodeFactory.createNXnote();
				// Note: makeFileCreatorDetector calls nexusFile.addData for the dataset below, which gets written to disk, but doesn't change the GroupNode in memory
				// ILazyWriteableDataset dataFileDataset = NexusUtils.createLazyWriteableDataset("file_name", String.class, new int[] { 1 }, null, null);
				// dataFileNote.initializeLazyDataset("file_name", 1, String.class);
				dataFileNote.addAttribute(TreeFactory.createAttribute("data_filename", new int[] { 1 }));
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
			public void addExpectedDataField(NXdetector expectedDetectorGroup, NexusFile nexusFile) throws NexusException {
				expectedDetectorGroup.setDescriptionScalar("counter timer description");
				expectedDetectorGroup.setTypeScalar("counter timer");
				expectedDetectorGroup.setField("id", "counterTimer");
				for (String extraName : extraNames) {
					// for these fields, its easiest to just add the actual dataNodes added by makeCounterTimer
					expectedDetectorGroup.addDataNode(extraName, nexusFile.getData("/entry1/instrument/counterTimer/" + extraName));
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
			public void addExpectedDataField(NXdetector expectedDetectorGroup, NexusFile nexusFile) throws NexusException {
				expectedDetectorGroup.addDataNode(NXdetector.NX_DATA, nexusFile.getData("/entry1/instrument/genericDetector/data"));
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
		 * @param nexusFile
		 * @throws NexusException
		 */
		public abstract void addExpectedDataField(NXdetector expectedDetectorGroup, NexusFile nexusFile) throws NexusException;

		private NXdetector createExpectedDetectorGroup(NexusFile nexusFile) throws NexusException {
			final NXdetector expectedDetectorGroup = NexusNodeFactory.createNXdetector();
			final Map<String, Object> expectedMetadata = createAppenderMetadata();
			// add the fields added by the the appender to the expected detector group
			for (Map.Entry<String, Object> metadataEntry : expectedMetadata.entrySet()) {
				expectedDetectorGroup.setField(metadataEntry.getKey(), metadataEntry.getValue());
			}

			// add fields written by NexusDataWriter rather than the appender
			addExpectedDataField(expectedDetectorGroup, nexusFile);
			return expectedDetectorGroup;
		}

		public void checkNexusFile(NexusFile nexusFile) throws Exception {
			final String detectorGroupPath = "/entry1/instrument/" + getName();
			final GroupNode detectorGroup = nexusFile.getGroup(detectorGroupPath, false);
			final NXdetector expectedDetectorGroup = createExpectedDetectorGroup(nexusFile);
			assertGroupNodesEqual(detectorGroupPath, expectedDetectorGroup, detectorGroup);
		}

	}

	private static final String USER_DEVICE_NAME = "user";
	private static final String SCANNABLE_NAME = "scannable";
	private static final String METADATA_SCANNABLE_NAME = "metadataScannable";

	private String testScratchDirectoryName;

	private INexusDataWriter nexusDataWriter;

	private String nexusFilePath;

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
	}

	@Test
	public void testNexusDataWriter() throws Exception {
		// Arrange: create a nexus device and register it with the nexus device service
		final NXuser user = createUserGroup();
		final NexusObjectProvider<NXuser> userProvider = new NexusObjectWrapper<>(USER_DEVICE_NAME, user);
		final SimpleNexusDevice<NXuser> userNexusDevice = new SimpleNexusDevice<NXuser>(userProvider);
		ServiceHolder.getNexusDeviceService().register(userNexusDevice);

		// Set the location of the template file
		final String templateFileAbsolutePath = Paths.get(TEMPLATE_FILE_PATH).toAbsolutePath().toString();
		NexusDataWriter.setNexusTemplateFiles(Arrays.asList(templateFileAbsolutePath));

		// Create a nexus appender for each detector and register it with the nexus file appender service
		for (DetectorType detectorType : DetectorType.values()) {
			detectorType.createAndRegisterAppender();
		}

		// Create a nexus appender for the scannable
		createAndRegisterAppender(SCANNABLE_NAME, createScannableMetadata(SCANNABLE_NAME));

		final Scannable metadataScannable = createMockScannable(METADATA_SCANNABLE_NAME);
		createAndRegisterAppender(METADATA_SCANNABLE_NAME, createScannableMetadata(METADATA_SCANNABLE_NAME));
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace(METADATA_SCANNABLE_NAME, metadataScannable);

		NexusDataWriter.setMetadatascannables(new HashSet<>(Arrays.asList(USER_DEVICE_NAME, METADATA_SCANNABLE_NAME)));

		// Act: write a point. Writing the first point causes the nexus file to be created.
		final ScanDataPoint firstPoint = createScanDataPoint();
		nexusDataWriter.addData(firstPoint);
		nexusDataWriter.completeCollection();

		// check that the file has been created as expected
		assertThat(new File(nexusFilePath).exists(), is(true));

		// We should add tests for the rest of NexusDataWriters behaviour
		final String entryPath = "/entry1/";
		try (NexusFile nexusFile = NexusFileFactory.openFileToRead(nexusFilePath)) {
			assertThat(nexusFile.getGroup(entryPath, false), is(notNullValue()));

			// check that the scan entry group created by the template has been added (note: we don't test the content here)
			assertThat(nexusFile.getGroup("/scan", false), is(notNullValue()));
			final String userGroupPath = entryPath + USER_DEVICE_NAME + "/";
			assertThat(nexusFile.getGroup(userGroupPath, false), is(notNullValue()));
			final String userCollectionPath = userGroupPath + "collection";
			assertThat(nexusFile.getGroup(userCollectionPath, false), is(notNullValue()));

			// check that the user group added by an INexusDevice has been added
			final GroupNode userGroup = nexusFile.getGroup(userGroupPath, false);
			assertThat(user, is(not(sameInstance(userGroup))));
			assertGroupNodesEqual(userGroupPath, user, userGroup);

			// check that the metadata has been added for each detector
			for (DetectorType detectorType : DetectorType.values()) {
				detectorType.checkNexusFile(nexusFile);
			}

			checkScannableInNexusFile(nexusFile, SCANNABLE_NAME, false);
			checkScannableInNexusFile(nexusFile, METADATA_SCANNABLE_NAME, true);
		}
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

	private void checkScannableInNexusFile(NexusFile nexusFile, String scannableName, boolean isMetadata) throws NexusException, Exception {
		final String scannableGroupPath = getScannableGroupPath(scannableName, isMetadata);
		final GroupNode scannableGroup = nexusFile.getGroup(scannableGroupPath, false);
		final NXobject expectedScannableGroup = createExpectedScannableGroup(nexusFile, scannableName, isMetadata);
		assertGroupNodesEqual(scannableGroupPath, expectedScannableGroup, scannableGroup);
	}

	private NXobject createExpectedScannableGroup(NexusFile nexusFile, String scannableName, boolean isMetadata) throws NexusException {
		final NXobject expectedScannableGroup = isMetadata ? NexusNodeFactory.createNXcollection() : NexusNodeFactory.createNXpositioner();
		final Map<String, Object> expectedMetadata = createScannableMetadata(scannableName);
		// add the fields added by the the appender to the expected detector group
		for (Map.Entry<String, Object> metadataEntry : expectedMetadata.entrySet()) {
			expectedScannableGroup.setField(metadataEntry.getKey(), metadataEntry.getValue());
		}

		// add the value fields written the NexusDataWriter
		final String scannableGroupPath = getScannableGroupPath(scannableName, isMetadata);
		final DataNode valueDataNode = nexusFile.getData(scannableGroupPath + "/" + NXpositioner.NX_VALUE);
		expectedScannableGroup.addDataNode(NXpositioner.NX_VALUE, valueDataNode);
		return expectedScannableGroup;
	}

	private String getScannableGroupPath(String scannableName, boolean isMetadata) {
		final String parentGroupPath = "/entry1/" + (isMetadata ? "before_scan/" : "instrument/");
		return parentGroupPath + scannableName;
	}

	private Map<String, Object> createScannableMetadata(String name) {
		final Map<String, Object> metadata = new HashMap<>();
		metadata.put(NXpositioner.NX_NAME, name);
		metadata.put(NXpositioner.NX_DESCRIPTION, "description of " + name);
		return metadata;
	}

	public static void createAndRegisterAppender(String name, Map<String, Object> metadata) {
		final NexusMetadataAppender<NXdetector> appender = new NexusMetadataAppender<>(name);
		appender.setNexusMetadata(metadata);
		ServiceHolder.getNexusFileAppenderService().register(appender);
	}

	private ScanDataPoint createScanDataPoint() throws Exception {
		// NexusDataWriter writes nexus structure the first data point is added, according to the devices in that point
		final ScanDataPoint firstPoint = new ScanDataPoint();
		firstPoint.setScanDimensions(new int[] { 5 });

		// add detectors to the the point
		for (DetectorType detectorType : DetectorType.values()) {
			firstPoint.addDetector(detectorType.getDetector());
			firstPoint.addDetectorData(detectorType.getDetectorData(), null);
		}

		final Scannable scannable = createMockScannable(SCANNABLE_NAME);
		firstPoint.addScannable(scannable);
		firstPoint.addScannablePosition(scannable.getPosition(), null);
		return firstPoint;
	}

	private Scannable createMockScannable(String scannableName) throws DeviceException {
		final Scannable scannable = mock(Scannable.class);
		when(scannable.getName()).thenReturn(scannableName);
		when(scannable.getInputNames()).thenReturn(new String[] { NXpositioner.NX_VALUE });
		when(scannable.getExtraNames()).thenReturn(new String[0]);
		when(scannable.getPosition()).thenReturn(0.01);

		return scannable;
	}

}
