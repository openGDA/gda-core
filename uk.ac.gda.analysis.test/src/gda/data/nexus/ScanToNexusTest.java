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
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeBuilder;
import gda.data.nexus.tree.NexusTreeNode;
import gda.data.nexus.tree.NexusTreeNodeSelection;
import gda.data.scan.datawriter.AsciiWriterExtender;
import gda.data.scan.datawriter.AsciiWriterExtenderConfig;
import gda.data.scan.datawriter.DataWriter;
import gda.data.scan.datawriter.DefaultDataWriterFactory;
import gda.data.scan.datawriter.IDataWriterExtender;
import gda.data.scan.datawriter.NXLinkCreator;
import gda.data.scan.datawriter.NXSubEntryWriter;
import gda.data.scan.datawriter.NXTomoEntryLinkCreator;
import gda.device.Detector;
import gda.device.Scannable;
import gda.scan.ConcurrentScan;
import gda.util.TestUtils;

import java.io.File;
import java.util.LinkedList;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.nexusformat.NexusFile;

import uk.ac.gda.analysis.hdf5.Hdf5Helper;
import uk.ac.gda.analysis.hdf5.Hdf5HelperData;
import uk.ac.gda.util.io.FileUtils;

/**
 * Class to test writing of nexus files during a scan
 */
public class ScanToNexusTest {

	final static String TestFileFolder = "testfiles/gda/data/nexus/";

	static void runScanToCreateFile(DataWriter dataWriter, IDataWriterExtender dataWriterExtender) throws InterruptedException, Exception {
		Scannable simpleScannable1 = TestHelpers.createTestScannable("SimpleScannable1", 0., new String[] {},
				new String[] { "simpleScannable1" }, 0, new String[] { "%5.2g" }, new String[] { "\u212B" }); // Angstrom

		Scannable simpleScannable2 = TestHelpers.createTestScannable("SimpleScannable2", 0., new String[] {},
				new String[] { "simpleScannable2" }, 0, new String[] { "%5.2g" }, new String[] { "eV" });

		int[] dims1 = new int[] { 10 };
		int totalLength = NexusFileHandle.calcTotalLength(dims1);
		double[] data1In = new double[totalLength];
		for (int index = 0; index < totalLength; index++) {
			data1In[index] = index;
		}

		Detector simpleDetector1 = TestHelpers.createTestDetector("SimpleDetector1", 0., new String[] {},
				new String[] {}, 0, new String[] { "%5.2g", "%5.2g", "%5.2g", "%5.2g", "%5.2g", "%5.2g", "%5.2g",
						"%5.2g", "%5.2g", "%5.2g" }, TestHelpers.createTestNexusGroupData(dims1, NexusFile.NX_FLOAT64,
						data1In, true), null, "description1", "detectorID1", "detectorType1");

		Object[] args = new Object[] { simpleScannable1, 0., 10., 1., simpleScannable2, simpleDetector1 };
		ConcurrentScan scan = new ConcurrentScan(args);
		scan.setDataWriter(dataWriter != null ? dataWriter : DefaultDataWriterFactory.createDataWriterFromFactory());
		if( dataWriterExtender != null)
			scan.getDataWriter().addDataWriterExtender(dataWriterExtender);
		scan.runScan();
//		scan.getDataWriter().completeCollection();
	}

	static void runNestedScanToCreateFile(IDataWriterExtender dataWriterExtender) throws InterruptedException,
			Exception {
		Scannable simpleScannable1 = TestHelpers.createTestScannable("SimpleScannable1", 0., new String[] {},
				new String[] { "simpleScannable1" }, 1, new String[] { "%5.2g" }, new String[] { "\u212B" }); // Angstrom

		Scannable simpleScannable2 = TestHelpers.createTestScannable("SimpleScannable2", 0., new String[] {},
				new String[] { "simpleScannable2" }, 2, new String[] { "%5.2g" }, new String[] { "eV" });

		Scannable simpleScannable3 = TestHelpers.createTestScannable("SimpleScannable3", 0., new String[] {},
				new String[] { "simpleScannable3" }, 3, new String[] { "%5.2g" }, new String[] { "eV" });

		int[] dims1 = new int[] { 10 };
		int totalLength = NexusFileHandle.calcTotalLength(dims1);
		double[] data1In = new double[totalLength];
		for (int index = 0; index < totalLength; index++) {
			data1In[index] = index;
		}

		Detector simpleDetector1 = TestHelpers.createTestDetector("SimpleDetector1", 0., new String[] {},
				new String[] {}, 0, new String[] { "%5.2g" }, TestHelpers.createTestNexusGroupData(dims1,
						NexusFile.NX_FLOAT64, data1In, true), null, "description1", "detectorID1", "detectorType1");

		Object[] args = new Object[] { simpleScannable1, 0., 1, 1., simpleScannable2, 0., 2, 1., simpleScannable3, 0.,
				3, 1., simpleDetector1 };
		ConcurrentScan scan = new ConcurrentScan(args);
		scan.setDataWriter(DefaultDataWriterFactory.createDataWriterFromFactory());
		scan.getDataWriter().addDataWriterExtender(dataWriterExtender);
		scan.runScan();
		scan.getDataWriter().completeCollection();
	}

	@Before
	public void beforeEachTest() {
		// clear metadata
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
		String testScratchDirectoryName = TestHelpers.setUpTest(ScanToNexusTest.class, "testCreateScanFile", true);

		LocalProperties.set(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, "NexusDataWriter");
		LocalProperties.set("gda.nexus.createSRS", "true");
		Metadata metadata = new GdaMetadata();
		GDAMetadataProvider.setInstanceForTesting(metadata);
		metadata.addMetadataEntry(new StoredMetadataEntry(GDAMetadataProvider.COLLECTION_DESCRIPTION,
				"Description of Collection"));
		metadata.addMetadataEntry(new StoredMetadataEntry(GDAMetadataProvider.EXPERIMENT_DESCRIPTION,
				"Description of Experiment"));
		metadata.addMetadataEntry(new StoredMetadataEntry(GDAMetadataProvider.SCAN_IDENTIFIER, "12345678"));

		runScanToCreateFile(null, null);
		// read nexus file
		String filename = testScratchDirectoryName + "/Data/" + "1.nxs";

		// now generate an SRS file with the SimpleScannable1 and SimpleScannable2 and meta data
		LinkedList<String> datasetNames = new LinkedList<String>();
		datasetNames.add("simpleScannable1");
		datasetNames.add("simpleScannable2");
		datasetNames.add("SimpleDetector1.SimpleDetector1_0");
		datasetNames.add("SimpleDetector1.SimpleDetector1_1");
		datasetNames.add("SimpleDetector1.SimpleDetector1_2");
		datasetNames.add("SimpleDetector1.SimpleDetector1_3");
		datasetNames.add("SimpleDetector1.SimpleDetector1_4");
		datasetNames.add("SimpleDetector1.SimpleDetector1_5");
		datasetNames.add("SimpleDetector1.SimpleDetector1_6");
		datasetNames.add("SimpleDetector1.SimpleDetector1_7");
		datasetNames.add("SimpleDetector1.SimpleDetector1_8");
		datasetNames.add("SimpleDetector1.SimpleDetector1_9");
		datasetNames.add("SimpleDetector2.data");
		datasetNames.add("SimpleDetector3.data");

		/* now save to ASCII file that caters for multi dimensional data */
		String asciiOutputFile = testScratchDirectoryName + "/ascii.txt";
		uk.ac.diamond.scisoft.analysis.io.NexusLoader.convertToAscii(filename, null, null, asciiOutputFile, datasetNames);
		junitx.framework.FileAssert.assertEquals(new File(TestFileFolder + "testCreateScanFile_ascii_expected.txt"),
				new File(asciiOutputFile));

		String srsDataFile = testScratchDirectoryName + "/Data/" + "1.dat";
		junitx.framework.FileAssert.assertEquals(new File(TestFileFolder + "testCreateScanFile_NexusWriterAscii_expected.dat"),
				new File(srsDataFile));
	}

	/**
	 * Creates a scan file using a simple detector and scannable. Checks content is correct
	 * 
	 * @throws Exception
	 *             if the test fails
	 */
	@Test
	public void testCreateScanToSRSFile() throws Exception {
		String testScratchDirectoryName = TestHelpers
				.setUpTest(ScanToNexusTest.class, "testCreateScanToSRSFile", true);

		LocalProperties.set(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, "SrsDataFile");
		runScanToCreateFile(null,null);
		junitx.framework.FileAssert.assertEquals(new File(TestFileFolder + "testCreateScanToSRSFile_expected.dat"),
				new File(testScratchDirectoryName + "/Data/1.dat"));
	}
	
	/**
	 * Creates a scan file using a simple detector and scannable. Checks content is correct
	 * 
	 * @throws Exception
	 *             if the test fails
	 */
	@Test
	public void testCreateScanToAlignedSRSFile() throws Exception {
		String testScratchDirectoryName = TestHelpers
				.setUpTest(ScanToNexusTest.class, "testCreateScanToAlignedSRSFile", true);

		LocalProperties.set("gda.data.scan.datawriter.dataFormat.SrsDataFile.aligncolumns", "True");
		LocalProperties.set(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, "SrsDataFile");
		runScanToCreateFile(null,null);
		junitx.framework.FileAssert.assertEquals(new File(TestFileFolder + "testCreateScanToAlignedSRSFile_expected.dat"),
				new File(testScratchDirectoryName + "/Data/1.dat"));
	}

	/**
	 * Test the DataWriterExtender
	 * 
	 * @throws Exception
	 *             if the test fails
	 */
	@Test
	public void testDataWriterExtender() throws Exception {
		String testScratchDirectoryName = TestHelpers.setUpTest(ScanToNexusTest.class, "testDataWriterExtender", true);
		Metadata metadata = new GdaMetadata();
		GDAMetadataProvider.setInstanceForTesting(metadata);
		metadata.addMetadataEntry(new StoredMetadataEntry(GDAMetadataProvider.COLLECTION_DESCRIPTION,
				"Description of Collection"));
		metadata.addMetadataEntry(new StoredMetadataEntry(GDAMetadataProvider.EXPERIMENT_DESCRIPTION,
				"Description of Experiment"));
		metadata.addMetadataEntry(new StoredMetadataEntry(GDAMetadataProvider.SCAN_IDENTIFIER, "12345678"));

		String output = testScratchDirectoryName + "/1.txt";
		LinkedList<AsciiWriterExtenderConfig> config = new LinkedList<AsciiWriterExtenderConfig>();
		config.add(new AsciiWriterExtenderConfig("I0", "%10.5f", "I0"));
		config.add(new AsciiWriterExtenderConfig("ln(I0/it)", "%10.5f", "ln(I0/(2+it))"));

		final AsciiWriterExtender writer = new AsciiWriterExtender(output, config, "\t", null, true);
		writer.addVariable("I0", "SimpleScannable1", 0);
		writer.addVariable("it", "SimpleScannable2", 0);
		runScanToCreateFile(null, writer);
		junitx.framework.FileAssert.assertEquals(new File(TestFileFolder + "testDataWriterExtender_expected.txt"),
				new File(output));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testReadNXS_createTreeForAllData() throws Exception {
		TestHelpers.setUpTest(ScanToNexusTest.class, "testReadNXS_createTreeForAllData", true);

		String filename = TestFileFolder + File.separator + "1.nxs";
		NexusTreeNodeSelection metaDataSel = NexusTreeNodeSelection.createTreeForAllData();// createTreeForAllNXData();
		INexusTree tree = NexusTreeBuilder.getNexusTree(filename, metaDataSel);
		tree.sort(NexusTreeNode.getNameComparator());
		INexusTree branch = tree.getChildNode(2).getChildNode(2).getChildNode(0).getChildNode(0);
		String val = branch.toText("", ":", "=", "|");
		org.junit.Assert.assertEquals(val,
				"|Attr:target=dimensions:39=type:NX_CHAR=data:/entry1/instrument/SimpleDetector3/data\n");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testReadNXS_createTreeForAllNXData() throws Exception {
		TestHelpers.setUpTest(ScanToNexusTest.class, "testReadNXS_createTreeForAllNXData", true);

		String filename = TestFileFolder + File.separator + "1.nxs";
		NexusTreeNodeSelection metaDataSel = NexusTreeNodeSelection.createTreeForAllNXData();// ();
		INexusTree tree = NexusTreeBuilder.getNexusTree(filename, metaDataSel);
		tree.sort(NexusTreeNode.getNameComparator());
		INexusTree branch = tree.getChildNode(0).getChildNode(2).getChildNode(0).getChildNode(0);
		String val = branch.toText("", ":", "=", "|");
		org.junit.Assert.assertEquals(val,
				"|Attr:target=dimensions:39=type:NX_CHAR=data:/entry1/instrument/SimpleDetector3/data\n");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testReadNXS_createTreeForAllNXEntries() throws Exception {
		TestHelpers.setUpTest(ScanToNexusTest.class, "testReadNXS_createTreeForAllNXEntries", true);

		String filename = TestFileFolder + File.separator + "1.nxs";
		NexusTreeNodeSelection metaDataSel = NexusTreeNodeSelection.createTreeForAllNXEntries();// ();
		INexusTree tree = NexusTreeBuilder.getNexusTree(filename, metaDataSel);
		tree.sort(NexusTreeNode.getNameComparator());
		INexusTree branch = tree.getChildNode(0).getChildNode(2).getChildNode(0).getChildNode(0);
		String val = branch.toText("", ":", "=", "|");
		org.junit.Assert.assertEquals(val,
				"|Attr:target=dimensions:39=type:NX_CHAR=data:/entry1/instrument/SimpleDetector3/data\n");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testReadNXS_createTreeForAllMetaData() throws Exception {
		TestHelpers.setUpTest(ScanToNexusTest.class, "testReadNXS_createTreeForAllMetaData", true);

		String filename = TestFileFolder + File.separator + "1.nxs";
		NexusTreeNodeSelection metaDataSel = NexusTreeNodeSelection.createTreeForAllMetaData();// ();
		INexusTree tree = NexusTreeBuilder.getNexusTree(filename, metaDataSel);
		tree.sort(NexusTreeNode.getNameComparator());
		INexusTree branch = tree.getChildNode(0).getChildNode(3).getChildNode(2).getChildNode(0).getChildNode(0);
		String val = branch.toText("", ":", "=", "|");
		org.junit.Assert.assertEquals(val,
				"|Attr:target=dimensions:39=type:NX_CHAR=data:/entry1/instrument/SimpleDetector3/data\n");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testSimpleExtractor_AllNXentry() throws Exception {
		TestHelpers.setUpTest(ScanToNexusTest.class, "testSimpleExtractor_AllNXentry", true);
		String filename = TestFileFolder + File.separator + "1.nxs";
		INexusTree tree = NexusTreeBuilder.getNexusTree(filename, NexusTreeBuilder.TREE_CONTENTS.ALLNXENTRY);
		tree.sort(NexusTreeNode.getNameComparator());
		INexusTree branch = tree.getChildNode(0).getChildNode(2).getChildNode(0).getChildNode(0);
		String val = branch.toText("", ":", "=", "|");
		org.junit.Assert.assertEquals(val,
				"|Attr:target=dimensions:39=type:NX_CHAR=data:/entry1/instrument/SimpleDetector3/data\n");
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testSimpleExtractor_All() throws Exception {
		TestHelpers.setUpTest(ScanToNexusTest.class, "testSimpleExtractor_All", true);
		String filename = TestFileFolder + File.separator + "1.nxs";
		INexusTree tree = NexusTreeBuilder.getNexusTree(filename, NexusTreeBuilder.TREE_CONTENTS.ALL);
		tree.sort(NexusTreeNode.getNameComparator());
		INexusTree branch = tree.getChildNode(2).getChildNode(2).getChildNode(0).getChildNode(0);
		String val = branch.toText("", ":", "=", "|");
		org.junit.Assert.assertEquals(val,
				"|Attr:target=dimensions:39=type:NX_CHAR=data:/entry1/instrument/SimpleDetector3/data\n");
	}

	/**
	 * Creates a scan file using a simple detector and scannable. Checks content is correct
	 * 
	 * @throws Exception
	 *             if the test fails
	 */
	@Test
	public void testCreateNestedScanFile() throws Exception {
		LocalProperties.set(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, "NexusDataWriter");
		LocalProperties.set("gda.nexus.createSRS", "false");
		runNestedScanToCreateFile(null);
	}
	
	
	
	@Test
	public void testNexusSubEntryCreator() throws Exception {
		String testScratchDirectoryName = TestHelpers.setUpTest(ScanToNexusTest.class, "testNexusSubEntryCreator", true);
		LocalProperties.set(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, "NexusDataWriter");
		LocalProperties.set("gda.nexus.createSRS", "false");

		runScanToCreateFile(null, null);
		
		NXLinkCreator nxLinkCreator = new NXLinkCreator();
		//create 3 links
		//add link to simpleScannable1 in current file
		nxLinkCreator.addLink("/entry2:NXentry/test", null, "entry1/default/simpleScannable1");
		//add link to simpleScannable1 in 1.nxs via nxfile 
		nxLinkCreator.addLink("/entry2:NXentry/test2", (new File(testScratchDirectoryName + "/Data/1.nxs")).getAbsolutePath(), "entry1/SimpleDetector1/simpleScannable1");
		//add link to entry2/test2 in this file - which itself points to simpleScannable1 in 1.nxs using currentfile  
		nxLinkCreator.addLink("/entry2:NXentry/test3", "", "entry2/test2");
		

		Scannable simpleScannable1 = TestHelpers.createTestScannable("SimpleScannable1", 0., new String[] {},
				new String[] { "simpleScannable1" }, 0, new String[] { "%5.2g" }, new String[] { "\u212B" }); // Angstrom

		Object[] args = new Object[] { simpleScannable1, 0., 10., 2. };
		ConcurrentScan scan = new ConcurrentScan(args);
		
		DataWriter dw = DefaultDataWriterFactory.createDataWriterFromFactory();
		IDataWriterExtender writer = new NXSubEntryWriter(nxLinkCreator);
		dw.addDataWriterExtender(writer);
		scan.setDataWriter(dw);
		scan.runScan();


		//1. test points to 2.nxs 
		Hdf5HelperData helperData = Hdf5Helper.getInstance().readDataSetAll(testScratchDirectoryName + "/Data/2.nxs", "/entry2", "test", true);
		double[] data = (double[]) helperData.data;
		Assert.assertEquals(10.0, data[5], 1e-6);

		
		//2. test2 points to 1.nxs 
		helperData = Hdf5Helper.getInstance().readDataSetAll(testScratchDirectoryName + "/Data/2.nxs", "/entry2", "test2", true);
		data = (double[]) helperData.data;
		Assert.assertEquals(5.0, data[5], 1e-6);

		//3. test3 points to test2 so should be the same assertion 2
		helperData = Hdf5Helper.getInstance().readDataSetAll(testScratchDirectoryName + "/Data/2.nxs", "/entry2", "test3", true);
		data = (double[]) helperData.data;
		Assert.assertEquals(5.0, data[5], 1e-6);
		
	}
	
	@Test
	public void testNXTomoEntryLinkCreator() throws Exception {
		String testScratchDirectoryName = TestHelpers.setUpTest(ScanToNexusTest.class, "testNXTomoEntryLinkCreator", true);
		
		String testFilesLocation = TestUtils.getGDALargeTestFilesLocation();
		if( testFilesLocation == null){
			Assert.fail("TestUtils.getGDALargeTestFilesLocation() returned null - test aborted");
		}
		
		NXTomoEntryLinkCreator nxLinkCreator = new NXTomoEntryLinkCreator();
		nxLinkCreator.setInstrument_detector_distance_target("entry1/scan_identifier");
		nxLinkCreator.setInstrument_detector_x_pixel_size_target("entry1/scan_identifier");
		nxLinkCreator.setInstrument_detector_y_pixel_size_target("entry1/scan_identifier");
		nxLinkCreator.afterPropertiesSet();
		
		String targetFilename = testScratchDirectoryName + File.separator + "tomoScan.nxs";
		FileUtils.copy( new File(testFilesLocation + File.separator + "tomoScan.nxs"), new File(targetFilename));

		String fileAbsolutePath = new File(targetFilename).getAbsolutePath();
		nxLinkCreator.makelinks(fileAbsolutePath);
		
		// test 1: rotation angle data
		Hdf5HelperData helperData = Hdf5Helper.getInstance().readDataSetAll(fileAbsolutePath, "/entry1/tomo_entry/sample", "rotation_angle", true);
		double[] data = (double[]) helperData.data;
		
		Hdf5HelperData helperData_ref = Hdf5Helper.getInstance().readDataSetAll(fileAbsolutePath, "/entry1/tomo_entry_ref/sample", "rotation_angle", true);
		double[] data_ref = (double[]) helperData_ref.data;
		
		// test equality of entries for a given index
		int zidx = 7;
		Assert.assertEquals(data[zidx], data_ref[zidx], 1e-16);
		
		// test 2: detector data 
		helperData = Hdf5Helper.getInstance().readDataSetAll(fileAbsolutePath, "/entry1/tomo_entry/instrument/detector", "data", false);
		helperData_ref = Hdf5Helper.getInstance().readDataSetAll(fileAbsolutePath, "/entry1/tomo_entry_ref/instrument/detector", "data", false);
		
		// test equality of dims of detector data
		Assert.assertEquals(helperData.dims.length, helperData_ref.dims.length);
		Assert.assertEquals(helperData.dims[0], helperData_ref.dims[0]);
		Assert.assertEquals(helperData.dims[1], helperData_ref.dims[1]);
		Assert.assertEquals(helperData.dims[2], helperData_ref.dims[2]);
	}

}


