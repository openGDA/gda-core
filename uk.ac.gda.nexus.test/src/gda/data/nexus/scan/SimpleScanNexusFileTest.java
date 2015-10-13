/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package gda.data.nexus.scan;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import gda.data.nexus.NexusUtils;
import gda.util.TestUtils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyWriteableDataset;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.StringDataset;
import org.eclipse.dawnsci.nexus.NXbeam;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.impl.NXbeamImpl;
import org.eclipse.dawnsci.nexus.impl.NXdataImpl;
import org.eclipse.dawnsci.nexus.impl.NXdetectorImpl;
import org.eclipse.dawnsci.nexus.impl.NXinstrumentImpl;
import org.eclipse.dawnsci.nexus.impl.NXobjectImpl;
import org.eclipse.dawnsci.nexus.impl.NXsampleImpl;
import org.eclipse.dawnsci.nexus.impl.NexusNodeFactory;
import org.junit.BeforeClass;
import org.junit.Test;

public class SimpleScanNexusFileTest {

	public class TestDetector implements NexusDevice<NXdetector> {

		private ILazyWriteableDataset defaultDataset;

		@Override
		public Class<NXdetector> getNexusBaseClass() {
			return NXdetector.class;
		}

		@Override
		public NXdetector createBaseClassInstance(NexusNodeFactory nxObjectFactory) {
			final NXdetectorImpl nxDetector = nxObjectFactory.createNXdetector();

			nxDetector.setDescription(StringDataset.createFromObject("Test Detector"));
			defaultDataset = nxDetector.initializeLazyDataset(NXdetectorImpl.NX_DATA, 2, Dataset.FLOAT64);
			// could add more fields

			return nxDetector;
		}

		@Override
		public DeviceType getDeviceType() {
			return DeviceType.INSTRUMENT;
		}

		@Override
		public String getName() {
			return "analyser";
		}

		@Override
		public ILazyWriteableDataset getDefaultWriteableDataset() {
			return defaultDataset;
		}

		@Override
		public ILazyWriteableDataset getDataset(String path) {
			return null;
		}

	}

	public class TestBeam implements NexusDevice<NXbeam> {

		@Override
		public Class<NXbeam> getNexusBaseClass() {
			return NXbeam.class;
		}

		@Override
		public NXbeam createBaseClassInstance(NexusNodeFactory nxObjectFactory) {
			final NXbeamImpl beam = nxObjectFactory.createNXbeam();
			beam.setIncident_wavelength(DatasetFactory.createFromObject(123.456));
			beam.setFlux(DatasetFactory.createFromObject(12.34f));

			return beam;
		}

		@Override
		public gda.data.nexus.scan.NexusDevice.DeviceType getDeviceType() {
			return DeviceType.SAMPLE;
		}

		@Override
		public String getName() {
			return "beam";
		}

		@Override
		public ILazyWriteableDataset getDefaultWriteableDataset() {
			return null;
		}

		@Override
		public ILazyWriteableDataset getDataset(String path) {
			return null;
		}

	}

	private static final String FILE_NAME = "nexusTestFile.nx5";

	private static String testScratchDirectoryName;

	private static String filePath;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testScratchDirectoryName = TestUtils.generateDirectorynameFromClassname(SimpleScanNexusFileTest.class.getCanonicalName());
		TestUtils.makeScratchDirectory(testScratchDirectoryName);
		filePath = testScratchDirectoryName + FILE_NAME;
	}

	@Test
	public void testNexusScan() throws Exception {
		// setup the scan
		final List<NexusDevice<?>> devices = new ArrayList<>();
		devices.add(new TestDetector());
		devices.add(new TestBeam());

		// setup and create the nexus file builder
		final DefaultNexusFileBuilder nexusFileBuilder = new DefaultNexusFileBuilder();
		nexusFileBuilder.setNexusDevices(devices);
		nexusFileBuilder.setFilePath(filePath);
		nexusFileBuilder.addDataLink(NXdataImpl.NX_DATA, "instrument/analyser/data");

		nexusFileBuilder.buildNexusFile();

		// check the constructed tree is as expected
		final TreeFile nexusTree = nexusFileBuilder.getNexusTree();
		checkNexusTree(nexusTree, true);

		// save the nexus file
		NexusUtils.saveNexusFile(nexusTree);

		// load the saved file and check the loaded tree is as expected
		TreeFile reloadedNexusTree = NexusUtils.loadNexusFile(filePath, true);
		checkNexusTree(reloadedNexusTree, false);
	}

	public void checkNexusTree(TreeFile nexusTree, boolean beforeSave) {
		final NXroot rootNode = (NXroot) nexusTree.getGroupNode();
		assertNotNull(rootNode);
		assertEquals(1, rootNode.getNumberOfGroupNodes());
		assertEquals(0, rootNode.getNumberOfDataNodes());

		NXentry entry = rootNode.getEntry();
		assertNotNull(entry);
		assertEquals(3, entry.getNumberOfGroupNodes());
		assertEquals(0, rootNode.getNumberOfDataNodes());

		NXinstrumentImpl instrument = (NXinstrumentImpl) entry.getInstrument();
		checkInstrument(instrument, beforeSave);

		NXsampleImpl sample = (NXsampleImpl) entry.getSample();
		checkSample(sample, beforeSave);

		NXdataImpl data = (NXdataImpl) entry.getData();
		checkData(data, beforeSave);
		assertSame(data.getDataNode(NXdataImpl.NX_DATA), instrument.getDetector("analyser").getDataNode("data"));
	}

	private void checkWriteableDataset(NXobjectImpl impl, String name, int expectedRank, Class<?> expectedElementClass, boolean beforeSave) {
		ILazyDataset dataset;
		if (beforeSave) {
			dataset = impl.getLazyWritableDataset(name);
		} else {
			dataset = impl.getDataset(name);
		}

		assertNotNull(dataset);
		assertEquals(expectedRank, dataset.getRank());
		assertEquals(expectedElementClass, dataset.elementClass());
	}

	private void checkInstrument(NXinstrumentImpl instrument, boolean beforeSave) {
		assertNotNull(instrument);
		assertEquals(1, instrument.getNumberOfGroupNodes());
		assertEquals(0, instrument.getNumberOfDataNodes());

		NXdetectorImpl detector = (NXdetectorImpl) instrument.getDetector("analyser");
		assertNotNull(detector);
		assertEquals("Test Detector", detector.getDescription().getString());

		checkWriteableDataset(detector, NXdetectorImpl.NX_DATA, 2, Double.class, beforeSave);
	}

	private void checkSample(NXsampleImpl sample, boolean beforeSave) {
		assertNotNull(sample);
		assertEquals(1, sample.getNumberOfGroupNodes());
		assertEquals(0, sample.getNumberOfDataNodes());

		NXbeam beam = sample.getBeam();
		assertNotNull(beam);
		assertEquals(123.456, beam.getIncident_wavelength().getDouble(), 1e-15);
		assertEquals(12.34f, beam.getFlux().getFloat(), 1e-7);
	}

	private void checkData(NXdataImpl data, boolean beforeSave) {
		assertNotNull(data);
		assertEquals(0, data.getNumberOfGroupNodes());
		assertEquals(1, data.getNumberOfDataNodes());
		checkWriteableDataset(data, NXdataImpl.NX_DATA, 2, Double.class, beforeSave);
	}

}
