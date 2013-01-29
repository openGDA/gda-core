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

package gda.data.nexus;

import gda.TestHelpers;
import gda.configuration.properties.LocalProperties;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.metadata.GdaMetadata;
import gda.data.metadata.Metadata;
import gda.data.metadata.StoredMetadataEntry;
import gda.data.nexus.nxclassio.NexusFileHandle;
import gda.data.nexus.tree.NexusTreeBuilder;
import gda.data.nexus.tree.NexusTreeNodeSelection;
import gda.data.scan.datawriter.DefaultDataWriterFactory;
import gda.data.scan.datawriter.IDataWriterExtender;
import gda.device.Detector;
import gda.device.Scannable;
import gda.scan.ConcurrentScan;

import org.junit.Before;
import org.junit.Test;

/**
 * Class to test writing of nexus files during a scan
 */
public class NexusScanWithFilenameTest {

	final static String TestFileFolder = "testfiles/gda/data/nexus/";

	static void runScanToCreateFile(IDataWriterExtender dataWriterExtender) throws InterruptedException, Exception {

		Scannable simpleScannable2 = TestHelpers.createTestScannable("SimpleScannable2", 0., new String[] {},
				new String[] { "simpleScannable2" }, 0, new String[] { "%5.2g" }, new String[] { "eV" });

		int[] dims1 = new int[] { 10 };
		int totalLength = NexusFileHandle.calcTotalLength(dims1);
		double[] data1In = new double[totalLength];
		for (int index = 0; index < totalLength; index++) {
			data1In[index] = index;
		}

		Detector simpleDetector1 = TestHelpers.createTestFileDetector("fileWriterDet", 1, "randomname%05d.ext", new int[] { 2, 2 },
					"test detector", "123", "test detector");


		Object[] args = new Object[] { simpleScannable2, 0., 10., 1., simpleDetector1};
		ConcurrentScan scan = new ConcurrentScan(args);
		scan.setDataWriter(DefaultDataWriterFactory.createDataWriterFromFactory());
		scan.getDataWriter().addDataWriterExtender(dataWriterExtender);
		scan.runScan();
		scan.getDataWriter().completeCollection();
	}
	
	@Before
	public void beforeEachTest() {
		Metadata metadata = new GdaMetadata();
		GDAMetadataProvider.setInstanceForTesting(metadata);
	}

	/**
	 * Creates a scan file using a simple detector and scannable. Checks content is correct
	 * 
	 * @throws Exception
	 *             if the test fails
	 */
	@Test
	public void testCreateScanFile() throws Exception {
		String testScratchDirectoryName = TestHelpers.setUpTest(NexusScanWithFilenameTest.class, "testCreateScanFile", true);

		LocalProperties.set(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, "NexusDataWriter");
		LocalProperties.set("gda.nexus.createSRS", "false");
		Metadata metadata = new GdaMetadata();
		GDAMetadataProvider.setInstanceForTesting(metadata);
		metadata.addMetadataEntry(new StoredMetadataEntry(GDAMetadataProvider.COLLECTION_DESCRIPTION,
				"Description of Collection"));
		metadata.addMetadataEntry(new StoredMetadataEntry(GDAMetadataProvider.EXPERIMENT_DESCRIPTION,
				"Description of Experiment"));
		metadata.addMetadataEntry(new StoredMetadataEntry(GDAMetadataProvider.SCAN_IDENTIFIER, "12345678"));

		runScanToCreateFile(null);

		String filename = testScratchDirectoryName + "/Data/" + "1.nxs";

		NexusTreeNodeSelection metaDataSel = NexusTreeNodeSelection.createTreeForAllMetaData();
		NexusTreeBuilder.getNexusTree(filename, metaDataSel);
	}
}