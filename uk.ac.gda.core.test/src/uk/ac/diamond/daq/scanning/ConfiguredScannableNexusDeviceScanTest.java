/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.GROUP_NAME_DIAMOND_SCAN;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertAxes;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertSignal;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinsertion_device;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusScanInfo.ScanRole;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.nexus.test.utilities.NexusTestUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Random;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import gda.TestHelpers;
import gda.data.scan.datawriter.NexusScanDataWriter;
import gda.data.scan.nexus.NexusScanDataWriterTestSetup;
import gda.data.scan.nexus.device.ScannableNexusDeviceConfiguration;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.DummyDetector;
import gda.device.scannable.DummyScannable;
import gda.jython.InterfaceProvider;
import gda.scan.ConcurrentScan;

class ConfiguredScannableNexusDeviceScanTest {

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

	private static final String DETECTOR_NAME = "det";
	private static final String SCANNABLE_NAME = "s1";
	private static final String ID_GAP_SCANNABLE_NAME = "jgap";

	private Scannable scannable;
	private Scannable idGapScannable;
	private Detector detector;
	private Path outputDir;

	@BeforeAll
	static void setUpServices() {
		NexusScanDataWriterTestSetup.setUp();
	}

	@AfterAll
	static void tearDown() {
		NexusScanDataWriterTestSetup.tearDown();
	}

	@BeforeEach
	void setUp() {
		scannable = new DummyScannable(SCANNABLE_NAME, 4.73);
		idGapScannable = new DummyScannable(ID_GAP_SCANNABLE_NAME, 4.73);
		detector = new DummyDetector(DETECTOR_NAME);

		ScannableNexusDeviceConfiguration config = new ScannableNexusDeviceConfiguration();
		config.setScannableName(idGapScannable.getName());
		config.setFieldPaths(NXinsertion_device.NX_GAP);
		config.setNexusBaseClass(NexusBaseClass.NX_INSERTION_DEVICE);
		config.register();
	}

	private static Stream<Arguments> getTestArguments() {
		return Stream.of(
//				Arguments.of(ScanRole.SCANNABLE, true),
//				Arguments.of(ScanRole.MONITOR_PER_POINT, true),
				Arguments.of(ScanRole.SCANNABLE, false)
//				Arguments.of(ScanRole.MONITOR_PER_POINT, false)
		);
	}

	@ParameterizedTest(name = "scanRole={0}, withDetector={1}")
	@MethodSource("getTestArguments")
	void testConfiguredScannableNexusDevice(ScanRole scanRole, boolean withDetector) throws Exception {
		final String testDir = TestHelpers.setUpTest(getClass(),
				"testConfiguredScannableNexusDevice", true, NexusScanDataWriter.class);
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace(scannable.getName(), scannable);

		outputDir = Path.of(testDir, "Data");

		final List<Object> scanArgs = new ArrayList<>();
		scanArgs.add(scanRole == ScanRole.SCANNABLE ? idGapScannable : scannable);
		scanArgs.addAll(List.of(0, 3, 1));
		if (scanRole == ScanRole.MONITOR_PER_POINT) {
			scanArgs.add(idGapScannable);
		}
		if (withDetector) {
			scanArgs.add(detector);
		}

		final ConcurrentScan scan = new ConcurrentScan(scanArgs.toArray());
		scan.run();

		checkNexusFile(scanRole, withDetector);
	}

	private void checkNexusFile(ScanRole scanRole, boolean withDetector) throws NexusException {
		final Path filePath = outputDir.resolve("1.nxs");
		assertThat(Files.exists(filePath), is(true));
		try (final NexusFile nexusFile = NexusTestUtils.openNexusFile(filePath.toString())) {
			checkNexusFile(nexusFile, scanRole, withDetector);
		}
	}

	private void checkNexusFile(NexusFile nexusFile, ScanRole scanRole, boolean withDetector) throws NexusException {
		final TreeFile nexusTree = NexusUtils.loadNexusTree(nexusFile);
		final NXroot nexusRoot = (NXroot) nexusTree.getGroupNode();
		assertThat(nexusRoot, is(notNullValue()));
		final NXentry entry = nexusRoot.getEntry();
		assertThat(entry, is(notNullValue()));

		final String dataGroupName = withDetector ? DETECTOR_NAME : ID_GAP_SCANNABLE_NAME;
		assertThat(entry.getGroupNodeNames(), containsInAnyOrder(GROUP_NAME_DIAMOND_SCAN,
				"instrument", "sample", dataGroupName));

		final NXinstrument instrument = entry.getInstrument();
		assertThat(instrument, is(notNullValue()));

		final Set<String> deviceNames = new HashSet<>(Set.of(ID_GAP_SCANNABLE_NAME));
		if (scanRole == ScanRole.MONITOR_PER_POINT) deviceNames.add(SCANNABLE_NAME);
		if (withDetector) deviceNames.add(DETECTOR_NAME);

		assertThat(instrument.getGroupNodeNames(), containsInAnyOrder(deviceNames.toArray()));
		final NXinsertion_device insertionDevice = instrument.getInsertion_device(ID_GAP_SCANNABLE_NAME);
		assertThat(insertionDevice, is(notNullValue()));

		assertThat(insertionDevice.getDataNodeNames(), containsInAnyOrder(NXinsertion_device.NX_GAP, "name"));
		assertThat(insertionDevice.getDataNode(NXinsertion_device.NX_GAP), is(notNullValue()));
		IDataset nameDataset = insertionDevice.getDataset("name");
		// TODO add name field to ScannableNexusDeviceConfiguration to allow nexus object to have
		// different name that the scannable
		assertThat(nameDataset.getString(), is(equalTo(ID_GAP_SCANNABLE_NAME)));

		final Set<String> dataNodeNames = new HashSet<>(deviceNames);
		System.err.println("Device names = " + deviceNames); // TODO REMOVE
		final NXdata dataGroup = entry.getData(dataGroupName);
		System.err.println("Data node names = "+ dataGroup.getDataNodeNames());
		assertThat(dataGroup, is(notNullValue()));
		if (scanRole == ScanRole.SCANNABLE && !withDetector)
			dataNodeNames.add(NXinsertion_device.NX_GAP);
		assertThat(dataGroup.getDataNodeNames(), containsInAnyOrder(dataNodeNames.toArray()));
		final String signalName = withDetector ?  DETECTOR_NAME :
				(scanRole == ScanRole.SCANNABLE ? NXinsertion_device.NX_GAP : ID_GAP_SCANNABLE_NAME);
		assertSignal(dataGroup, signalName);
		assertAxes(dataGroup, scanRole == ScanRole.SCANNABLE ? ID_GAP_SCANNABLE_NAME : SCANNABLE_NAME);
		deviceNames.forEach(name -> assertThat(dataGroup.getDataNode(name), is(notNullValue())));
	}

}
