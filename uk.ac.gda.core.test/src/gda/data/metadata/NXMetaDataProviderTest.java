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
import gda.device.detector.NXDetector;
import gda.device.detector.NXDetectorData;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataChildNodeAppender;
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

import org.junit.Assert;
import org.junit.Test;

public class NXMetaDataProviderTest {

	@Test
	public void testInScan() throws InterruptedException, Exception {
		TestHelpers.setUpTest(NXMetaDataProviderTest.class, "testInScan", true);
		
		
		NXDetector d = new NXDetector();
		d.setName("det");
		d.setCollectionStrategy(new NullNXCollectionStrategy("test"));
//		d.setAppendToNXDetectorData(false);
		NXMetaDataProvider metaDataProvider = new NXMetaDataProvider();
		metaDataProvider.add("key","value");

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
		
		Assert.assertEquals(",key:value", metaDataProvider.listAsString());
	}
	
}
class NXDetectorMetadataPlugin2 extends NXDetectorMetadataPlugin{

	public NXDetectorMetadataPlugin2(NexusTreeProvider metaDataProvider) {
		super(metaDataProvider);
		// TODO Auto-generated constructor stub
	}
	@Override
	public List<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
			DeviceException {
		List<NXDetectorDataAppender> appenders = new ArrayList<NXDetectorDataAppender>();
		if (firstReadoutInScan) {
			INexusTree treeToAppend = getMetaDataProvider().getNexusTree();
			appenders.add(new NXDetectorDataChildNodeAppender1(treeToAppend));
		} else {
			appenders.add(new NXDetectorDataNullAppender());
		}
		firstReadoutInScan = false;
		return appenders;
	}	
}

class NXDetectorDataChildNodeAppender1 implements NXDetectorDataAppender {

	private final INexusTree treeToAppend;
	
	public NXDetectorDataChildNodeAppender1(INexusTree treeToAppend) {
		this.treeToAppend = treeToAppend;
		
	}
	@Override
	public void appendTo(NXDetectorData data, String detectorName) {
		data.getNexusTree().addChildNode(treeToAppend);
	}

}