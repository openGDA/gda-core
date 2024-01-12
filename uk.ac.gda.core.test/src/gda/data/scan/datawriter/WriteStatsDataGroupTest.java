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

import static gda.configuration.properties.LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT;
import static gda.data.scan.datawriter.NexusScanDataWriter.PROPERTY_VALUE_DATA_FORMAT_NEXUS_SCAN;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertAuxiliarySignals;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertAxes;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertIndices;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertSignal;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
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
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.Random;
import org.eclipse.january.dataset.SliceND;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gda.TestHelpers;
import gda.configuration.properties.LocalProperties;
import gda.data.scan.nexus.NexusScanDataWriterTestSetup;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.DummyDetector;
import gda.device.scannable.DummyScannable;
import gda.scan.ConcurrentScan;

public class WriteStatsDataGroupTest {

	private static class DummyStatsDetector extends DummyDetector implements IWritableNexusDevice<NXdetector> {

		private static final int[] IMAGE_SIZE = new int[] { 8, 8 };

		private ILazyWriteableDataset imageDataset;
		private ILazyWriteableDataset minValueDataset;
		private ILazyWriteableDataset maxValueDataset;
		private ILazyWriteableDataset meanDataset;
		private ILazyWriteableDataset sumDataset;

		public DummyStatsDetector() {
			setName(DETECTOR_NAME);
		}

		@Override
		protected Object acquireData() {
			return Random.rand(IMAGE_SIZE);
		}

		@Override
		public int[] getDataDimensions() throws DeviceException {
			return IMAGE_SIZE;
		}

		@Override
		public NexusObjectProvider<NXdetector> getNexusProvider(NexusScanInfo info) throws NexusException {
			final NXdetector det = NexusNodeFactory.createNXdetector();

			final int scanRank = info.getOverallRank();
			imageDataset = det.initializeLazyDataset(NXdetector.NX_DATA, scanRank + 2, Double.class);
			minValueDataset = det.initializeLazyDataset(DATASET_NAME_MIN, scanRank, Double.class);
			maxValueDataset = det.initializeLazyDataset(DATASET_NAME_MAX, scanRank, Double.class);
			meanDataset = det.initializeLazyDataset(DATASET_NAME_MEAN, scanRank, Double.class);
			sumDataset = det.initializeLazyDataset(DATASET_NAME_SUM, scanRank, Double.class);

			final NexusObjectWrapper<NXdetector> nexusWrapper = new NexusObjectWrapper<>(getName(), det, NXdetector.NX_DATA);
			nexusWrapper.addAuxilaryDataGroup(DATA_GROUP_NAME_STATS,
					List.of(DATASET_NAME_SUM, DATASET_NAME_MIN, DATASET_NAME_MAX, DATASET_NAME_MEAN));
			return nexusWrapper;
		}

		@Override
		public void writePosition(Object data, SliceND scanSlice) throws NexusException {
			try {
				final Dataset dataset = (Dataset) data;
				IWritableNexusDevice.writeDataset(imageDataset, data, scanSlice);
				IWritableNexusDevice.writeDataset(minValueDataset, dataset.min(), scanSlice);
				IWritableNexusDevice.writeDataset(maxValueDataset, dataset.max(), scanSlice);
				IWritableNexusDevice.writeDataset(meanDataset, dataset.mean(), scanSlice);
				IWritableNexusDevice.writeDataset(sumDataset, dataset.sum(), scanSlice);
			} catch (DatasetException e) {
				throw new NexusException("Could not write data for detector: " + getName());
			}
		}

		@Override
		public void scanEnd() throws NexusException {
			imageDataset = null;
		}

	}

	private static final String DETECTOR_NAME = "det";
	private static final String SCANNABLE_NAME = "s1";

	private static final String DATA_GROUP_NAME_STATS = "stats";
	private static final String DATASET_NAME_MIN = "min";
	private static final String DATASET_NAME_MAX = "max";
	private static final String DATASET_NAME_MEAN = "mean";
	private static final String DATASET_NAME_SUM = "sum";

	private static final int NUM_POINTS = 10;

	private Path outputDir;
	private Scannable scannable;
	private Detector detector;

	@BeforeAll
	public static void setUpServices() {
		NexusScanDataWriterTestSetup.setUp();
	}

	@AfterAll
	public static void tearDownServices() {
		NexusScanDataWriterTestSetup.tearDown();
		LocalProperties.clearProperty(GDA_DATA_SCAN_DATAWRITER_DATAFORMAT);
	}

	@BeforeEach
	public void setUp() {
		scannable = new DummyScannable("s1", 0.0);
		detector = new DummyStatsDetector();
	}

	@Test
	void testWriteStatsDataGroup() throws Exception {
		final String testDir = TestHelpers.setUpTest(WriteStatsDataGroupTest.class, "testWriteStatsDataGroup", true);
		LocalProperties.set(GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, PROPERTY_VALUE_DATA_FORMAT_NEXUS_SCAN);

		outputDir = Path.of(testDir, "Data");

		final Object[] scanArgs = new Object[] { scannable, 0, NUM_POINTS, 1, detector };
		final ConcurrentScan scan = new ConcurrentScan(scanArgs);
		scan.runScan();

		final Path filePath = outputDir.resolve("1.nxs");
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

		checkInstrument(entry);
		checkDataGroups(entry);
	}

	private void checkInstrument(final NXentry entry) {
		final NXinstrument instrument = entry.getInstrument();
		assertThat(instrument, is(notNullValue()));

		assertThat(instrument.getGroupNodeNames(), containsInAnyOrder(SCANNABLE_NAME, DETECTOR_NAME));

		final NXpositioner pos = instrument.getPositioner(SCANNABLE_NAME);
		assertThat(pos, is(notNullValue()));
		assertThat(pos.getDataNodeNames(), is(notNullValue()));
		final DataNode posDataset = pos.getDataNode(NXpositioner.NX_VALUE);
		assertThat(posDataset, is(notNullValue()));

		final NXdetector detGroup = instrument.getDetector(DETECTOR_NAME);
		assertThat(detGroup, is(notNullValue()));
		assertThat(detGroup.getDataNodeNames(), containsInAnyOrder(NXdetector.NX_DATA,
				DATASET_NAME_MIN, DATASET_NAME_MAX, DATASET_NAME_MEAN, DATASET_NAME_SUM));
		final DataNode detDataset = detGroup.getDataNode(NXdetector.NX_DATA);
		assertThat(detDataset, is(notNullValue()));
	}

	private void checkDataGroups(NXentry entry) {
		assertThat(entry.getAllData().keySet(), contains(DETECTOR_NAME, DATA_GROUP_NAME_STATS));

		checkMainDataGroup(entry);
		checkStatsDataGroup(entry);
	}

	private void checkMainDataGroup(NXentry entry) {
		final NXdata dataGroup = entry.getData(DETECTOR_NAME);
		assertThat(dataGroup, is(notNullValue()));
		assertThat(dataGroup.getDataNodeNames(), contains(NXdata.NX_DATA, SCANNABLE_NAME));
		assertSignal(dataGroup, NXdata.NX_DATA);
		assertAxes(dataGroup, SCANNABLE_NAME, ".", ".");
		assertIndices(dataGroup, SCANNABLE_NAME, 0);

		final NXinstrument instr = entry.getInstrument();
		assertThat(dataGroup.getDataNode(NXdata.NX_DATA),
				is(sameInstance(instr.getDetector(DETECTOR_NAME).getDataNode(NXdetector.NX_DATA))));
		assertThat(dataGroup.getDataNode(SCANNABLE_NAME),
				is(sameInstance(instr.getPositioner(SCANNABLE_NAME).getDataNode(NXpositioner.NX_VALUE))));
	}

	private void checkStatsDataGroup(NXentry entry) {
		final NXdata statsGroup = entry.getData(DATA_GROUP_NAME_STATS);
		assertThat(statsGroup, is(notNullValue()));
		assertThat(statsGroup.getDataNodeNames(), containsInAnyOrder(SCANNABLE_NAME,
				DATASET_NAME_MIN, DATASET_NAME_MAX, DATASET_NAME_MEAN, DATASET_NAME_SUM));
		assertSignal(statsGroup, DATASET_NAME_SUM);
		assertAuxiliarySignals(statsGroup, DATASET_NAME_MIN, DATASET_NAME_MAX, DATASET_NAME_MEAN);
		assertAxes(statsGroup, SCANNABLE_NAME);

		final NXinstrument instr = entry.getInstrument();
		assertIndices(statsGroup, SCANNABLE_NAME, 0);
		assertThat(statsGroup.getDataNode(DATASET_NAME_MIN),
				is(sameInstance(instr.getDetector(DETECTOR_NAME).getDataNode(DATASET_NAME_MIN))));
		assertThat(statsGroup.getDataNode(DATASET_NAME_MAX),
				is(sameInstance(instr.getDetector(DETECTOR_NAME).getDataNode(DATASET_NAME_MAX))));
		assertThat(statsGroup.getDataNode(DATASET_NAME_MEAN),
				is(sameInstance(instr.getDetector(DETECTOR_NAME).getDataNode(DATASET_NAME_MEAN))));
		assertThat(statsGroup.getDataNode(DATASET_NAME_SUM),
				is(sameInstance(instr.getDetector(DETECTOR_NAME).getDataNode(DATASET_NAME_SUM))));
	}

}
