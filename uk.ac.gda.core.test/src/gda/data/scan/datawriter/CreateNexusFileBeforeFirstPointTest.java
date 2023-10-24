/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

import static gda.data.scan.datawriter.NexusScanDataWriter.PROPERTY_NAME_CREATE_FILE_AT_SCAN_START;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.nexus.IWritableNexusDevice;
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
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
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

class CreateNexusFileBeforeFirstPointTest {

	private final class DummyNexusDeviceDetector extends DummyDetector implements IWritableNexusDevice<NXdetector> {

		private static final int[] IMAGE_SIZE = { 8, 8 };

		private ILazyWriteableDataset imageDataset;

		private boolean firstPoint = true;

		private DummyNexusDeviceDetector() {
			super("det");
		}

		@Override
		protected Object acquireData() {
			try {
				if (firstPoint) {
					checkNexusFile(false);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			firstPoint = false;
			return Random.rand(IMAGE_SIZE);
		}

		@Override
		public int[] getDataDimensions() throws DeviceException {
			return IMAGE_SIZE;
		}

		@Override
		public NexusObjectProvider<NXdetector> getNexusProvider(NexusScanInfo info) throws NexusException {
			System.err.println("Getting nexus providers");
			final NXdetector det = NexusNodeFactory.createNXdetector();
			imageDataset = det.initializeLazyDataset(NXdetector.NX_DATA, info.getOverallRank() + 2, Double.class);
			return new NexusObjectWrapper<>(getName(), det, NXdetector.NX_DATA);
		}

		@Override
		public void writePosition(Object data, SliceND scanSlice) throws NexusException {
			try {
				IWritableNexusDevice.writeDataset(imageDataset, data, scanSlice);
			} catch (DatasetException e) {
				throw new NexusException("Could not write data for detector: " + getName());
			}
		}

		@Override
		public void scanEnd() throws NexusException {
			imageDataset = null;
		}
	}

	private static final int[] SCAN_SHAPE = { 5, 3 };

	private Scannable scannable1;
	private Scannable scannable2;
	private Detector detector;
	private Path outputDir;

	@BeforeAll
	public static void setUpServices() {
		NexusScanDataWriterTestSetup.setUp();
		LocalProperties.set(PROPERTY_NAME_CREATE_FILE_AT_SCAN_START, true);
	}

	@AfterAll
	public static void tearDownServices() {
		NexusScanDataWriterTestSetup.tearDown();
		LocalProperties.clearProperty(PROPERTY_NAME_CREATE_FILE_AT_SCAN_START);
	}

	@BeforeEach
	public void setUp() {
		scannable1 = new DummyScannable("s1", 0.0);
		scannable2 = new DummyScannable("s2", 0.0);
		detector = new DummyNexusDeviceDetector();
	}

	@Test
	void testNexusFileCreatedBeforeFirstPoint() throws Exception {
		final String testDir = TestHelpers.setUpTest(this.getClass(), "testNexusFileCreatedBeforeFirstPoint", true, NexusScanDataWriter.class);
		outputDir = Path.of(testDir, "Data");

		final Object[] scanArgs = new Object[] {
				scannable1, 0, SCAN_SHAPE[0] - 1, 1,
				scannable2, 0, SCAN_SHAPE[1] - 1, 1,
				detector
		};

		final ConcurrentScan scan = new ConcurrentScan(scanArgs);

		scan.runScan();

		checkNexusFile(true);
	}

	private void checkNexusFile(boolean scanFinished) throws NexusException {
		// this method is called with from the acquireData method of the detector during the
		// first scan point with scanFinished as true, then from the test method with
		// scanFinished as false
		final Path path = outputDir.resolve("1.nxs");
		assertThat(Files.exists(path), is(true));

		try (final NexusFile nexusFile = NexusTestUtils.openNexusFile(path.toString())) {
			checkNexusFile(nexusFile, scanFinished);
		}
	}

	private void checkNexusFile(NexusFile nexusFile, boolean scanFinished) throws NexusException {
		final TreeFile nexusTree = NexusUtils.loadNexusTree(nexusFile);
		final NXroot nexusRoot = (NXroot) nexusTree.getGroupNode();
		assertThat(nexusRoot, is(notNullValue()));
		final NXentry entry = nexusRoot.getEntry();
		assertThat(entry, is(notNullValue()));

		final NXinstrument instrument = entry.getInstrument();
		assertThat(instrument, is(notNullValue()));

		final NXpositioner positioner1 = instrument.getPositioner(scannable1.getName());
		assertThat(positioner1, is(notNullValue()));

		final int[] expectedShape = scanFinished ? SCAN_SHAPE : new int[] { 0, 0 };
		final IDataset dataset1 = positioner1.getDataset(NXpositioner.NX_VALUE);
		assertThat(dataset1, is(notNullValue()));
		assertThat(dataset1.getElementClass(), is(equalTo(Double.class)));
		assertThat(dataset1.getShape(), equalTo(scanFinished ? SCAN_SHAPE : expectedShape));

		final NXpositioner positioner2 = instrument.getPositioner(scannable2.getName());
		assertThat(positioner1, is(notNullValue()));

		final IDataset dataset2 = positioner2.getDataset(NXpositioner.NX_VALUE);
		assertThat(dataset2, is(notNullValue()));
		assertThat(dataset2.getElementClass(), is(equalTo(Double.class)));
		assertThat(dataset2.getShape(), equalTo(scanFinished ? SCAN_SHAPE : expectedShape));

		final NXdetector detGroup = instrument.getDetector(detector.getName());
		assertThat(detGroup, is(notNullValue()));

		final ILazyDataset detDataNode = detGroup.getLazyDataset(NXdetector.NX_DATA);
		assertThat(detDataNode, is(notNullValue()));
		assertThat(detDataNode.getElementClass(), is(equalTo(Double.class)));

		assertThat(detDataNode.getShape(), equalTo(
				ArrayUtils.addAll(expectedShape, scanFinished ? DummyNexusDeviceDetector.IMAGE_SIZE : new int[] { 0, 0 })));
	}

}
