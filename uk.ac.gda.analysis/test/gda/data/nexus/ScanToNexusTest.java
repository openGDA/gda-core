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
import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.nxclassio.NexusFileHandle;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeBuilder;
import gda.data.nexus.tree.NexusTreeNode;
import gda.data.nexus.tree.NexusTreeNodeSelection;
import gda.data.nexus.tree.NexusTreeWriter;
import gda.data.scan.datawriter.AsciiWriterExtender;
import gda.data.scan.datawriter.AsciiWriterExtenderConfig;
import gda.data.scan.datawriter.DataWriter;
import gda.data.scan.datawriter.DataWriterExtenderBase;
import gda.data.scan.datawriter.DefaultDataWriterFactory;
import gda.data.scan.datawriter.IDataWriterExtender;
import gda.data.scan.datawriter.NexusDataWriter;
import gda.device.Detector;
import gda.device.Scannable;
import gda.scan.ConcurrentScan;
import gda.scan.IScanDataPoint;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.nexusformat.NXlink;
import org.nexusformat.NexusException;
import org.nexusformat.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.HDF5Loader;

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

		NexusDataWriter dataWriter = new NexusDataWriter();
		runScanToCreateFile(dataWriter, null);
		
		
		
		
		List<SubEntryLink> links = new Vector<SubEntryLink>();
		
		//create 3 links
		//add link to simpleScannable1 in current file
		links.add(  new SubEntryLink("/entry2:NXentry/test", "/entry1:NXentry/default:NXdata/simpleScannable1:NXdata"));

		//add link to simpleScannable1 in 1.nxs via nxfile 
		links.add(  new SubEntryLink("/entry2:NXentry/test2", 
				"nxfile://" + (new File(testScratchDirectoryName + "/Data/1.nxs")).getAbsolutePath() + "#entry1/SimpleDetector1/simpleScannable1"));

		//add link to entry2/test2 in this file - which itself points to simpleScannable1 in 1.nxs using currentfile  
		links.add(  new SubEntryLink("/entry2:NXentry/test3", "#entry2/test2"));
		
		dataWriter = new NexusDataWriter();
		IDataWriterExtender writer = new NXSubEntryWriter(links);
		dataWriter.addDataWriterExtender(writer);

		Scannable simpleScannable1 = TestHelpers.createTestScannable("SimpleScannable1", 0., new String[] {},
				new String[] { "simpleScannable1" }, 0, new String[] { "%5.2g" }, new String[] { "\u212B" }); // Angstrom

		Object[] args = new Object[] { simpleScannable1, 0., 10., 2. };
		ConcurrentScan scan = new ConcurrentScan(args);
		scan.setDataWriter(dataWriter);
		scan.runScan();

		HDF5Loader loader = new HDF5Loader(testScratchDirectoryName + "/Data/2.nxs");
		DataHolder holder = loader.loadFile();

		//entry2/test points to simpleScannable1 in current file
		ILazyDataset testds = holder.getLazyDataset("/entry2/test");
		IDataset testslice = testds.getSlice(null, null, null);
		Assert.assertEquals(testslice.getDouble(5), 10.0, 1e-6);

		//entry2/test2 points to simpleScannable1 in 1.nxs
		testds = holder.getLazyDataset("/entry2/test2");
		testslice = testds.getSlice(null, null, null);
		Assert.assertEquals(testslice.getDouble(5), 5.0, 1e-6);

		//entry2/test3 points to simpleScannable1 in 1.nxs 
		testds = holder.getLazyDataset("/entry2/test3");
		testslice = testds.getSlice(null, null, null);
		Assert.assertEquals(testslice.getDouble(5), 5.0, 1e-6);
	}
	
	
}

class SubEntryLink{
	public String key;
	public String value;
	public SubEntryLink(String key, String value) {
		super();
		this.key = key;
		this.value = value;
	}
	
}

class NXSubEntryWriter extends DataWriterExtenderBase{
	private static final Logger logger = LoggerFactory.getLogger(NXSubEntryWriter.class);

	String filename;
	List<SubEntryLink> links;
	
	
	public NXSubEntryWriter(List<SubEntryLink> links2) {
		super();
		this.links = links2;
	}

	@Override
	public void addData(IDataWriterExtender parent, IScanDataPoint dataPoint) throws Exception {
		filename = new File(dataPoint.getCurrentFilename()).getAbsolutePath();
	}

	@Override
	public void completeCollection(IDataWriterExtender parent) {
		/**
		 * add sub-entry section with links to other
		 */
		NexusFile file;
		try {
			file = new NexusFile(filename,NexusFile.NXACC_RDWR);
			for( SubEntryLink link : links){
				String value = link.value;
				String key = link.key;
				if( value.startsWith("nxfile")){
					makelink(file, key, null, value);
					
				} else if( value.startsWith("#")){
					makelink(file, key, null, "nxfile://"+filename+value);
				} else {
					NXlink nxlink = getLink(file,value);
					makelink(file, key, nxlink, null);
				}
				
			}
			file.flush();
			file.finalize();
			file.close();
			
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			logger.error("TODO put description of error here", e);
		}

	}

	private void makelink(NexusFile file, String path, NXlink link, String url) throws Exception {
		String [] parts = path.split("/",2);
		if( parts[0].isEmpty()){
			if( parts.length>1){
				makelink(file,parts[1], link, url);
			}
			return;
		}
		if( parts.length > 1){
			String []subParts = parts[0].split(":");
			String name = subParts[0];
			String nxClass = subParts[1];
			if( !(file.groupdir().containsKey(name) && file.groupdir().get(name).equals(nxClass))){
				file.makegroup(name, nxClass);
			}
			file.opengroup(name, nxClass);
			try{
				makelink(file, parts[1], link, url);
			} finally {
				file.closegroup();
			}
			return;
		}
		if( link != null)
			file.makenamedlink(parts[0], link);
		if( url != null)
			file.linkexternaldataset(parts[0],url);
	}


	private NXlink getLink(NexusFile file, String path) throws NexusException, Exception {
		String [] parts = path.split("/",2);
		if( parts[0].isEmpty()){
			if( parts.length>1){
				return getLink(file,parts[1]);
			}
			return null;
		}
		String []subParts = parts[0].split(":");
		String name = subParts[0];
		String nxClass = subParts[1];
		if( parts.length > 1){
			if( !(file.groupdir().containsKey(name) && file.groupdir().get(name).equals(nxClass))){
				throw new Exception("Item not found " + name + ":" + nxClass);
			}
			file.opengroup(name, nxClass);
			try{
				return getLink(file, parts[1]);
			} finally {
				file.closegroup();
			}
		}
		NXlink link=null;
		if( nxClass.equals(NexusExtractor.NXDataClassName))
		{
			try{
				file.opendata(name);
			} catch( NexusException ne){
				throw new Exception("Error calling opendata for "+name, ne);
			}
			link = file.getdataID();
			file.closedata();
		} 
		return link;
	}
};

