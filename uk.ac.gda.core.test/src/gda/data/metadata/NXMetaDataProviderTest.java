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

import gda.TestHelpers;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.NXDetector;
import gda.device.detector.NXDetectorData;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataNullAppender;
import gda.device.detector.nxdetector.NXPlugin;
import gda.device.detector.nxdetector.plugin.NXDetectorMetadataPlugin;
import gda.device.detector.nxdetector.plugin.NullNXCollectionStrategy;
import gda.device.scannable.DummyScannable;
import gda.scan.ConcurrentScan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class NXMetaDataProviderTest {
	Random rand;
	
	@Before
	public void setUp() {
		this.rand = new Random();
	}
	
	private NXMetaDataProvider createNXMetaDataProvider(int numEntries, String entryKeyRoot, String entryValueRoot){
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();
		
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
		return metaDataProvider;
	}
	
	@Test
	public void testInScan() throws InterruptedException, Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testInScan", true);
		
		
		NXDetector d = new NXDetector();
		d.setName("det");
		d.setCollectionStrategy(new NullNXCollectionStrategy("test"));
//		d.setAppendToNXDetectorData(false);
		NXMetaDataProvider metaDataProvider = createNXMetaDataProvider(4, "key", "value");
		metaDataProvider.setName("metashop");
//		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();
//		
//		String elementKey = "key";
//		String elementValue = "value";
//		int numElements = 3;
//		if (numElements > 1){
//			for(int i=0; i<numElements; i++){
//				String elmIdx = Integer.toString(i);
//				String elmKey = elementKey+"_"+elmIdx;
//				String elmValue = elementValue+"_"+elmIdx;
//				metaDataProvider.add(elmKey,elmValue);
//			}
//		}else{
//			metaDataProvider.add(elementKey,elementValue);
//		}
		//metaDataProvider.add("key","value");
		
		Scannable splScn1 = TestHelpers.createTestScannable("SimpleScannable1", 13., new String[] {},
				new String[] { "simpleScannable1" }, 0, new String[] { "%5.2g" }, new String[] { "\u212B" }); // Angstrom
		
		metaDataProvider.add(splScn1);

		d.setAdditionalPluginList(Arrays.asList(new NXPlugin[]{ new  NXDetectorMetadataPlugin2(metaDataProvider)}));
		d.configure();
		
		DummyScannable s = new DummyScannable("s");
		s.configure();
		
		ConcurrentScan conScan = new ConcurrentScan(new Object[]{s ,0, 10, 1, d }); 
		conScan.runScan();
		
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
		NXMetaDataProvider metaDataProvider = createNXMetaDataProvider(4, "key", "value");
		//Random rand = new Random();
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
		
		String valueMappedToAddedKey = metaDataProvider.get(addedKey);
		Assert.assertEquals(addedValue, valueMappedToAddedKey);
	}
	
	@Test
	public void testRemove() throws InterruptedException, Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testRemove", true);
		NXMetaDataProvider metaDataProvider = createNXMetaDataProvider(4, "key", "value");
		//Random rand = new Random();
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


