/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.data.metadata;

import static org.mockito.Mockito.when;
import gda.MockFactory;
import gda.TestHelpers;
import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.ScannableMotionUnits;
import gda.device.detector.NXDetector;
import gda.device.detector.nxdetector.plugin.NullNXCollectionStrategy;
import gda.device.scannable.DummyScannable;
import gda.device.scannable.DummyUnitsScannable;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.factory.Factory;
import gda.factory.Finder;
import gda.scan.ConcurrentScan;

import java.util.Random;

import org.apache.commons.math3.util.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class NXMetaDataProviderTest {
	Random rand;
	private ScannableMotionUnits bsx;
	private Scannable testScn;
	
	@Before
	public void setUp() {
		this.rand = new Random();
		try {
			bsx = MockFactory.createMockScannableMotionUnits("bsx");
			when(bsx.getUserUnits()).thenReturn("mm");
			when(bsx.getAttribute(ScannableMotionUnits.USERUNITS)).thenReturn("eV");
			when(bsx.getPosition()).thenReturn(6.0);
		} catch (DeviceException e) {
			// TODO Auto-generated catch block
			//logger.error("TODO put description of error here", e);
		}
		
		try {
			testScn = MockFactory.createMockScannable(
					"testScn" // String name, 
					,new String[] { "testScn_I1" }	// String[] inputNames, 
					,new String[] {}				// String[] extraNames,
					,new String[] { "%f" }			// String[] outputFormat, 
					,0								// int level, 
					,1331.013						// Object position
					);
			
			//when(testScn.getUserUnits()).thenReturn("mm");
			when(testScn.getAttribute(ScannableMotionUnits.USERUNITS)).thenReturn("eV");
			//when(testScn.getPosition()).thenReturn(6.0);
		} catch (DeviceException e) {
			// TODO Auto-generated catch block
			//logger.error("TODO put description of error here", e);
		}
	}
	
	private void populateNXMetaDataProvider(NXMetaDataProvider metaDataProvider, int numEntries, String entryKeyRoot, String entryValueRoot){
		
		if (numEntries > 1){
			for(int i=0; i<numEntries; i++){
				String currIdx = Integer.toString(i);
				String currEntryKey = entryKeyRoot+"_"+currIdx;
				String currEntryValue = entryValueRoot+"_"+currIdx;
				metaDataProvider.add(currEntryKey, currEntryValue);
			}
		}else{
			metaDataProvider.add(entryKeyRoot, entryValueRoot);
		}
	}
	
//	@Test
//	public void testInScan() throws InterruptedException, Exception {
//		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testInScan", true);
//		
//		
//		NXDetector d = new NXDetector();
//		d.setName("det");
//		d.setCollectionStrategy(new NullNXCollectionStrategy("test"));
////		d.setAppendToNXDetectorData(false);
//		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();
//		populateNXMetaDataProvider(metaDataProvider, 4, "key", "value");
//		metaDataProvider.add("pi", 3.14159);
//		metaDataProvider.setName("metashop");
////		
////		String elementKey = "key";
////		String elementValue = "value";
////		int numElements = 3;
////		if (numElements > 1){
////			for(int i=0; i<numElements; i++){
////				String elmIdx = Integer.toString(i);
////				String elmKey = elementKey+"_"+elmIdx;
////				String elmValue = elementValue+"_"+elmIdx;
////				metaDataProvider.add(elmKey,elmValue);
////			}
////		}else{
////			metaDataProvider.add(elementKey,elementValue);
////		}
//		//metaDataProvider.add("key","value");
//		
//		Scannable splScn1 = TestHelpers.createTestScannable("SimpleScannable1", 13., new String[] {},
//				new String[] { "simpleScannable1" }, 0, new String[] { "%f" }, new String[] { "\u212B" }); // Angstrom
//		
//		metaDataProvider.add(splScn1);
//
//		d.setAdditionalPluginList(Arrays.asList(new NXPlugin[]{ new  NXDetectorMetadataPlugin2(metaDataProvider)}));
//		d.configure();
//		
//		DummyScannable s = new DummyScannable("s");
//		s.configure();
//		
//		ConcurrentScan conScan = new ConcurrentScan(new Object[]{s ,0, 10, 1, d }); 
//		conScan.runScan();
//		
//	}

	@Test
	public void testInScan1() throws InterruptedException, Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testInScan1", true);
		
		NXDetector d = new NXDetector();
		d.setName("det");
		d.setCollectionStrategy(new NullNXCollectionStrategy("test"));
		d.configure();

		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();
		populateNXMetaDataProvider(metaDataProvider, 4, "key", "value");
		metaDataProvider.add("pi", 3.14159);
		metaDataProvider.add("catch", 22);
		
		double [] dblArray = new double[] {3, 1, 4, 159};
		metaDataProvider.add("myDoubleArray", dblArray);
		
		int [] intArray = new int[] {3, 1, 4, 159};
		metaDataProvider.add("myIntArray", intArray);
		
		metaDataProvider.setName("metashop");

		DummyScannable s = new DummyScannable("s");
		s.configure();
		
		
		Factory factory = TestHelpers.createTestFactory("test");
		factory.addFindable(metaDataProvider);
		//Finder.getInstance().addFactory(factory);
		
		Scannable smplScn1 = TestHelpers.createTestScannable("mySimpleScannableI1E0", 13., new String[] {},
				new String[] { "mySimpleScannableI1E0_input1" }, 0, new String[] { "%f" }, new String[] { "\u212B" }); // Angstrom
		
		factory.addFindable(smplScn1);
		metaDataProvider.add(smplScn1);

		Scannable scnI2E1 = TestHelpers.createTestScannable("mySimpleScannableI2E1", 
				new double[] { 1.11, 1.21, 3.13 }, new String[] { "mySimpleScannableI2E1_extra1" }, new String[] { "mySimpleScannableI2E1_input1", "mySimpleScannableI2E1_input2" },
				1, new String[] { "%f", "%f", "%f" }, null);
		
		factory.addFindable(scnI2E1);
		metaDataProvider.add(scnI2E1);
		
		Scannable scnI1E2 = TestHelpers.createTestScannable("mySimpleScannableI1E2", 
				new double[] { 1.11, 3.13, 3.23 }, new String[] { "mySimpleScannableI1E2_extra1", "mySimpleScannableIn1Ex2_extra2" }, new String[] { "mySimpleScannableI1E2_input1" },
				1, new String[] { "%f", "%f", "%f" }, null);
		
		factory.addFindable(scnI1E2);
		metaDataProvider.add(scnI1E2);
		
		Scannable scnI1E1U = TestHelpers.createTestScannable("mySimpleScannableI1E1U", 
				new int[] { 101, 303 }, new String[] { "mySimpleScannableI1E1U_extra1" }, new String[] { "mySimpleScannableI1E1U_input1" },
				1, new String[] { "%i", "%i" }, new String[] {"eV", "keV"});
		
		factory.addFindable(scnI1E1U);
		metaDataProvider.add(scnI1E1U);
		
//		Scannable scnI1E0 = TestHelpers.createTestScannable("mySimpleScannableI1E0", 
//				new String [] { "string_is_returned" }, new String[] {}, new String[] { "mySimpleScannableI1E0_input1" },
//				1, new String[] { "%s" }, null);
//		metaDataProvider.add(scnI1E0);
		
		String angstrom = "\u212B";
		Scannable ss101 = TestHelpers.createTestScannable(
				"SimpleScannable101" 						//name
				,101.2										//position
				,new String[] {}							//extraNames
				,new String[] { "simpleScannable101" }		//inputNames
				,0											//level
				,new String[] { "%f" }						//outputFormat
				,new String[] { angstrom }					//units
				);
		Scannable ss102 = TestHelpers.createTestScannable("SimpleScannable102", 102.1, new String[] {},
				new String[] { "simpleScannable102" }, 0, new String[] { "%f" }, new String[] { "eV" });
		Scannable ss103 = TestHelpers.createTestScannable("SimpleScannable103", 103.3, new String[] {},
				new String[] { "simpleScannable103" }, 0, new String[] { "%f" }, new String[] { "mA" });
		ScannableGroup group = new ScannableGroup("myScannableGroup", new Scannable[] {ss101,ss102,ss103,bsx, testScn});
		metaDataProvider.add(group);
		
		//Scannable scnMotnU = new DummyUnitsScannable("bsx", 0, "mm", "micron");
		metaDataProvider.add(bsx);
		factory.addFindable(testScn);
		metaDataProvider.add(testScn);
		
		Finder.getInstance().addFactory(factory);
		
		//add_default(metaDataProvider);
		//setup property  to be read by NexusDataWriter
		LocalProperties.set("gda.nexus.metadata.provider.name",metaDataProvider.getName());
		
		ConcurrentScan conScan = new ConcurrentScan(new Object[]{s ,0, 10, 1, d }); 
		conScan.runScan();
		String filename = conScan.getDataWriter().getCurrentFileName();
		
//		NexusTreeNodeSelection top = new NexusTreeNodeSelection();
//		top.setName(NexusExtractor.topName);
//		top.setNxClass(NexusExtractor.topClass);
//		top.setWanted(NexusTreeNodeSelection.GET_THIS_ITEM);
//		NexusTreeNodeSelection nxEntries = new NexusTreeNodeSelection();
//		nxEntries.setName("");
//		nxEntries.setNxClass(NexusExtractor.NXEntryClassName);
//		nxEntries.setWanted(NexusTreeNodeSelection.GET_THIS_ITEM);
//		nxEntries.setDataType(NexusTreeNodeSelection.NAME_DIMS_AND_DATA);
//		top.addChildNode(nxEntries);
//		NexusTreeNodeSelection nxCollections = new NexusTreeNodeSelection();
//		nxCollections.setName("at_scan_start");
//		nxCollections.setNxClass(NexusExtractor.NXCollectionClassName);
//		nxCollections.setWanted(NexusTreeNodeSelection.GET_THIS_AND_BELOW);
//		nxCollections.setDataType(NexusTreeNodeSelection.NAME_DIMS_AND_DATA);
//		nxEntries.addChildNode(nxCollections);
//		
//		//NXLinkCreator
//		INexusTree tree = NexusTreeBuilder.getNexusTree(filename, top);
//		INexusTree beforeScanMetaData = metaDataProvider.getBeforeScanMetaData();
//		INexusTree childNode = tree.getChildNode(0).getChildNode(0);
//		Assert.assertEquals(beforeScanMetaData, childNode);
		
	}

	@Test
	public void testList() throws InterruptedException, Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testList", true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();
		metaDataProvider.add("key","value");
		metaDataProvider.add("key","value1");
		metaDataProvider.add("key1","value");
		metaDataProvider.add("key1","value1");
		
		Scannable splScn1 = TestHelpers.createTestScannable("SimpleScannable1", 13., new String[] {},
				new String[] { "simpleScannable1" }, 0, new String[] { "%5.2g" }, new String[] { "\u212B" }); // Angstrom
		
		metaDataProvider.add(splScn1);
		
		
		//Assert.assertEquals(",key:value", metaDataProvider.listAsString());
		String fmt = "%s:%s";
		String delimiter = ",";
		String expected = metaDataProvider.concatenateKeyAndValueForListAsString(fmt, delimiter);
		Assert.assertEquals(expected, metaDataProvider.listAsString(fmt, delimiter));
	}
	
	@Test
	public void testAdd() throws InterruptedException, Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testAdd", true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();
		populateNXMetaDataProvider(metaDataProvider, 4, "key", "value");
		
		int randomIntForTestKey = rand.nextInt(181081);
		String randomPostfixForTestKey = Integer.toString(randomIntForTestKey);
		String addedKey = "last_added_key" + "_" + randomPostfixForTestKey;
		String addedValue = "value_mapped_to_" + addedKey;
		System.out.println("***addedKey = " +addedKey);
		System.out.println("***addedValue = " +addedValue);
		
		// test before adding
		boolean doesContainAddedKey_before = metaDataProvider.containsKey(addedKey);
		Assert.assertEquals(false, doesContainAddedKey_before);
		
		// add
		metaDataProvider.add(addedKey, addedValue); 
		
		// test after adding
		boolean doesContainAddedKey_after = metaDataProvider.containsKey(addedKey);
		Assert.assertEquals(true, doesContainAddedKey_after);
		
		//String valueMappedToAddedKey = metaDataProvider.get(addedKey).toString();
		Pair<?, ?> valueWithUnitsMappedToAddedKey = (Pair<?, ?>) metaDataProvider.get(addedKey);
		String valueMappedToAddedKey = valueWithUnitsMappedToAddedKey.getFirst().toString();
		Assert.assertEquals(addedValue, valueMappedToAddedKey);
	}
	
	@Test
	public void testRemove() throws InterruptedException, Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testRemove", true);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();
		populateNXMetaDataProvider(metaDataProvider, 4, "key", "value");
		
		int randomIntForTestKey = rand.nextInt(181081);
		String randomPostfixForTestKey = Integer.toString(randomIntForTestKey);
		String removedKey = "removed_key" + "_" + randomPostfixForTestKey;
		String removedValue = "value_mapped_to_" + removedKey;
		System.out.println("***removedKey = " +removedKey);
		System.out.println("***removedValue = " +removedValue);
		
		// first add 
		metaDataProvider.add(removedKey, removedValue);
		
		// then remove 
		metaDataProvider.remove(removedKey);
		
		// test
		boolean doesContainRemovedKey = metaDataProvider.containsKey(removedKey);
		Assert.assertEquals(false, doesContainRemovedKey);
	}
}


