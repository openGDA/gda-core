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

package gda.data.scan.datawriter;

import static org.eclipse.dawnsci.nexus.scan.NexusScanConstants.GROUP_NAME_DIAMOND_SCAN;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertAxes;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertIndices;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertSignal;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.nexus.IWritableNexusDevice;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.dawnsci.nexus.test.utilities.NexusTestUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.SliceND;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gda.TestHelpers;
import gda.data.scan.datawriter.AbstractNexusDataWriterScanTest.DummyImageDetector;
import gda.data.scan.nexus.NexusScanDataWriterTestSetup;
import gda.device.Detector;
import gda.device.Scannable;
import gda.device.scannable.DummyScannable;
import gda.scan.ConcurrentScan;

class DetectorWithAxesFieldsScanTest {

	public class DetectorWithAxesFields extends DummyImageDetector implements IWritableNexusDevice<NXdetector> {

		private ILazyWriteableDataset imageDataset;
		private ILazyWriteableDataset anglesDataset;
		private ILazyWriteableDataset energiesDataset;
		private ILazyWriteableDataset spectrumDataset;
		private ILazyWriteableDataset externalIoDataset;

		public DetectorWithAxesFields() {
			setName(DETECTOR_NAME);
		}

		@Override
		public NexusObjectProvider<NXdetector> getNexusProvider(NexusScanInfo info) throws NexusException {
			final NXdetector det = NexusNodeFactory.createNXdetector();
			imageDataset = det.initializeLazyDataset(NXdetector.NX_DATA, info.getOverallRank() + 2, Double.class);
			anglesDataset = det.initializeLazyDataset(FIELD_NAME_ANGLES, info.getOverallRank() + 1, Double.class);
			energiesDataset = det.initializeLazyDataset(FIELD_NAME_ENERGIES, info.getOverallRank() + 1, Double.class);
			spectrumDataset = det.initializeLazyDataset(FIELD_NAME_SPECTRUM, info.getOverallRank() + 1, Double.class);
			externalIoDataset = det.initializeLazyDataset(FIELD_NAME_EXTERNAL_IO, info.getOverallRank() + 1, Double.class);

			final NexusObjectWrapper<NXdetector> nexusWrapper = new NexusObjectWrapper<>(getName(), det, NXdetector.NX_DATA);
			nexusWrapper.addAxisDataFieldForPrimaryDataField(FIELD_NAME_ANGLES, NXdetector.NX_DATA, 1);
			nexusWrapper.addAxisDataFieldForPrimaryDataField(FIELD_NAME_ENERGIES, NXdetector.NX_DATA, 2, 0, 2);
			nexusWrapper.addAxisDataFieldForPrimaryDataField(FIELD_NAME_SPECTRUM, NXdetector.NX_DATA, null, 0, 2);
			nexusWrapper.addAxisDataFieldForPrimaryDataField(FIELD_NAME_EXTERNAL_IO, NXdetector.NX_DATA, null, 0, 2);
			return nexusWrapper;
		}

		@Override
		public void writePosition(Object data, SliceND scanSlice) throws NexusException {
			try {
				IWritableNexusDevice.writeDataset(imageDataset, data, scanSlice);
				IWritableNexusDevice.writeDataset(anglesDataset, DatasetFactory.createRange(8), scanSlice);
				IWritableNexusDevice.writeDataset(energiesDataset, DatasetFactory.createRange(8), scanSlice);
				IWritableNexusDevice.writeDataset(spectrumDataset, DatasetFactory.createRange(8), scanSlice);
				IWritableNexusDevice.writeDataset(externalIoDataset, DatasetFactory.createRange(8), scanSlice);
			} catch (DatasetException e) {
				throw new NexusException("Could not write data for detector: " + getName());
			}
		}

		@Override
		public void scanEnd() throws NexusException {
			imageDataset = null;
			anglesDataset = null;
			energiesDataset = null;
			spectrumDataset = null;
			externalIoDataset = null;
		}

	}

	private static final String SCANNABLE_NAME = "s1";
	private static final String DETECTOR_NAME = "detector";
	private static final String FIELD_NAME_ANGLES = "angles";
	private static final String FIELD_NAME_ENERGIES = "energies";
	private static final String FIELD_NAME_SPECTRUM = "spectrum";
	private static final String FIELD_NAME_EXTERNAL_IO = "external_io";

	private Scannable scannable;
	private Detector detector;

	@BeforeAll
	static void setUpServices() {
		NexusScanDataWriterTestSetup.setUp();
	}

	@AfterAll
	static void tearDownServices() {
		NexusScanDataWriterTestSetup.tearDown();
	}

	@BeforeEach
	void setUp() {
		scannable = new DummyScannable(SCANNABLE_NAME);
		detector = new DetectorWithAxesFields();
	}

	@Test
	void testDetectorWithAxesScan() throws Exception {
		final String testDir = TestHelpers.setUpTest(DetectorWithAxesFieldsScanTest.class,
				"testDetectorWithAxesScan", true, NexusScanDataWriter.class);

		final Object[] scanArgs = new Object[] { scannable, 0, 5, 1, detector };
		final ConcurrentScan scan = new ConcurrentScan(scanArgs);
		scan.runScan();

		final Path filePath = Path.of(testDir, "Data", "1.nxs");
		assertThat(Files.exists(filePath), is(true));

		try (final NexusFile nexusFile = NexusTestUtils.openNexusFile(filePath.toString())) {
			checkNexusFile(nexusFile);
		}
	}

	private void checkNexusFile(NexusFile nexusFile) throws NexusException {
		final TreeFile nexusTree = NexusUtils.loadNexusTree(nexusFile);
		final NXroot nexusRoot = (NXroot) nexusTree.getGroupNode();
		assertThat(nexusRoot, is(notNullValue()));
		final NXentry entry = nexusRoot.getEntry();
		assertThat(entry, is(notNullValue()));

		assertThat(entry.getGroupNodeNames(), containsInAnyOrder("instrument", "sample",
				GROUP_NAME_DIAMOND_SCAN, DETECTOR_NAME));

		final NXinstrument instrument = entry.getInstrument();
		assertThat(instrument, is(notNullValue()));

		final NXpositioner positioner = instrument.getPositioner(SCANNABLE_NAME);
		assertThat(positioner, is(notNullValue()));
		assertThat(positioner.getDataNodeNames(), containsInAnyOrder(NXpositioner.NX_VALUE, NXpositioner.NX_NAME,
				NXpositioner.NX_SOFT_LIMIT_MIN, NXpositioner.NX_SOFT_LIMIT_MAX));
		final DataNode positionerValueDataNode = positioner.getDataNode(NXpositioner.NX_VALUE);
		assertThat(positionerValueDataNode, is(notNullValue()));

		final NXdetector detector = instrument.getDetector(DETECTOR_NAME);
		assertThat(detector, is(notNullValue()));
		assertThat(detector.getDataNodeNames(), containsInAnyOrder(NXdetector.NX_DATA,
				FIELD_NAME_ANGLES, FIELD_NAME_ENERGIES, FIELD_NAME_SPECTRUM, FIELD_NAME_EXTERNAL_IO));

		final NXdata dataGroup = entry.getData(DETECTOR_NAME);
		assertThat(dataGroup, is(notNullValue()));
		assertThat(dataGroup.getDataNodeNames(), containsInAnyOrder(SCANNABLE_NAME, NXdata.NX_DATA,
				FIELD_NAME_ANGLES, FIELD_NAME_ENERGIES, FIELD_NAME_SPECTRUM, FIELD_NAME_EXTERNAL_IO));
		assertSignal(dataGroup, NXdata.NX_DATA);
		assertAxes(dataGroup, SCANNABLE_NAME, FIELD_NAME_ANGLES, FIELD_NAME_ENERGIES);
		assertIndices(dataGroup, SCANNABLE_NAME, 0);
		assertIndices(dataGroup, FIELD_NAME_ANGLES, 0, 1);
		assertIndices(dataGroup, FIELD_NAME_ENERGIES, 0, 2);
		assertThat(dataGroup.getDataNode(NXdata.NX_DATA),
				is(sameInstance(detector.getDataNode(NXdetector.NX_DATA))));
		assertThat(dataGroup.getDataNode(SCANNABLE_NAME),
				is(sameInstance(positioner.getDataNode(NXpositioner.NX_VALUE))));
		for (String axisName : List.of(FIELD_NAME_ANGLES, FIELD_NAME_ENERGIES, FIELD_NAME_SPECTRUM, FIELD_NAME_EXTERNAL_IO)) {
			assertThat(dataGroup.getDataNode(axisName), is(sameInstance(detector.getDataNode(axisName))));
		}
	}

}
