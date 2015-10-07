/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.device.detector;

import gda.TestHelpers;
import gda.configuration.properties.LocalProperties;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.metadata.GdaMetadata;
import gda.data.metadata.Metadata;
import gda.data.metadata.StoredMetadataEntry;
import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeBuilder;
import gda.data.nexus.tree.NexusTreeBuilder.TREE_CONTENTS;
import gda.data.nexus.tree.NexusTreeNode;
import gda.data.scan.datawriter.DataWriter;
import gda.data.scan.datawriter.DataWriterExtenderBase;
import gda.data.scan.datawriter.DefaultDataWriterFactory;
import gda.data.scan.datawriter.IDataWriterExtender;
import gda.device.Detector;
import gda.device.Scannable;
import gda.scan.ConcurrentScan;
import gda.scan.IScanDataPoint;

import org.junit.Assert;
import org.junit.Test;

/**
 * Class to test writing of nexus files during a scan
 */
public class NexusDetectorWritingTest {

	final static String TestFileFolder = "testfiles/gda/data/nexus/";

	static void runScanToCreateFile(int numElements) throws InterruptedException, Exception {
		Scannable simpleScannable1 = TestHelpers.createTestScannable("SimpleScannable1", 0., new String[] {},
				new String[] { "simpleScannable1" }, 0, new String[] { "%5.2g" }, new String[] {"\u212B"}); //Angstrom

		Scannable simpleScannable2 = TestHelpers.createTestScannable("SimpleScannable2", 0., new String[] {},
				new String[] { "simpleScannable2" }, 0, new String[] { "%5.2g" }, new String[] {"eV"});

		int[] dims1 = new int[] { 10 };
		int totalLength = NexusExtractor.calcTotalLength(dims1);
		double[] data1In = new double[totalLength];
		for (int index = 0; index < totalLength; index++) {
			data1In[index] = index;
		}

		Detector nxDetector = new DummyNXDetector("DummyNexusDetector",numElements);

		Object[] args = new Object[] { simpleScannable1, 0., 10., 1., simpleScannable2, nxDetector};
		ConcurrentScan scan = new ConcurrentScan(args);
		DataWriter dataWriter = DefaultDataWriterFactory.createDataWriterFromFactory();
		dataWriter.addDataWriterExtender(new DataWriterExtenderBase(){

			@Override
			public void addData(IDataWriterExtender parent, IScanDataPoint dataPoint) throws Exception {
				dataPoint.getAllValuesAsDoubles();
				super.addData(parent, dataPoint);
			}});
		scan.setDataWriter(dataWriter);
		scan.runScan();
	}

	/**
	 * Creates a scan file using a simple detector and scannable. Checks content is correct
	 *
	 * @throws Exception
	 *             if the test fails
	 */
	@Test
	public void testWriteNXDetector_OneElement() throws Exception {
		String testScratchDirectoryName = TestHelpers.setUpTest(NexusDetectorWritingTest.class, "testWriteNXDetector_OneElement", true);

		LocalProperties.set(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, "NexusDataWriter");
		LocalProperties.set("gda.nexus.createSRS","false");
		Metadata metadata = new GdaMetadata();
		GDAMetadataProvider.setInstanceForTesting(metadata);
		metadata.addMetadataEntry(new StoredMetadataEntry(GDAMetadataProvider.COLLECTION_DESCRIPTION,"Description of Collection"));
		metadata.addMetadataEntry(new StoredMetadataEntry(GDAMetadataProvider.EXPERIMENT_DESCRIPTION,"Description of Experiment"));
		metadata.addMetadataEntry(new StoredMetadataEntry(GDAMetadataProvider.SCAN_IDENTIFIER,"12345678"));

		runScanToCreateFile(1);
		String filename = testScratchDirectoryName + "/Data/" + "1.nxs";

		INexusTree tree = NexusTreeBuilder.getNexusTree(filename, TREE_CONTENTS.ALLNXENTRY);
		tree.sort(NexusTreeNode.getNameComparator());
		tree.sort(NexusTreeNode.getNameComparator());

		String filename_expected = TestFileFolder + "testWriteNXDetector_expected.nxs";
		INexusTree tree_expected = NexusTreeBuilder.getNexusTree(filename_expected, TREE_CONTENTS.ALLNXENTRY);
		tree_expected.sort(NexusTreeNode.getNameComparator());
		Assert.assertFalse("sorted trees are not the same: sort(" + filename_expected + ") NE (" + filename + ")", !tree_expected.equals(tree));

	}

	@Test
	public void testWriteNXDetector_MultiElement() throws Exception {
		String testScratchDirectoryName = TestHelpers.setUpTest(NexusDetectorWritingTest.class, "testWriteNXDetector_MultiElement", true);

		LocalProperties.set(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, "NexusDataWriter");
		LocalProperties.set("gda.nexus.createSRS","false");
		Metadata metadata = new GdaMetadata();
		GDAMetadataProvider.setInstanceForTesting(metadata);
		metadata.addMetadataEntry(new StoredMetadataEntry(GDAMetadataProvider.SCAN_IDENTIFIER,"12345678"));
		runScanToCreateFile(5);
		String filename = testScratchDirectoryName + "/Data/" + "1.nxs";

		INexusTree tree = NexusTreeBuilder.getNexusTree(filename, TREE_CONTENTS.ALLNXENTRY);
		tree.sort(NexusTreeNode.getNameComparator());
		tree.sort(NexusTreeNode.getNameComparator());
		String filename_expected = TestFileFolder + "testWriteNXDetector_MultiElement_expected.nxs";
		INexusTree tree_expected = NexusTreeBuilder.getNexusTree(filename_expected, TREE_CONTENTS.ALLNXENTRY);
		tree_expected.sort(NexusTreeNode.getNameComparator());
		Assert.assertFalse("sorted trees are not the same: sort(" + filename_expected + ") NE (" + filename + ")", !tree_expected.equals(tree));

	}

}