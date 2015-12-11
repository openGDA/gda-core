/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.data.nexus.extractor;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.dawnsci.nexus.NexusException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import gda.data.nexus.tree.INexusSourceProvider;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeBuilder;
import gda.data.nexus.tree.NexusTreeNodeSelection;
import gda.util.TestUtils;

public class NexusExtractorTest {
	String scratchFolder;
	URL testdataFile;
	static String TestFileFolder;
	@BeforeClass
	static public void setUpClass() {
		TestFileFolder = TestUtils.getGDALargeTestFilesLocation();
		if( TestFileFolder == null){
			Assert.fail("TestUtils.getGDALargeTestFilesLocation() returned null - test aborted");
		}
	}

	@Before
	public void setUp() throws Exception {
		scratchFolder = TestUtils.setUpTest(NexusExtractorTest.class, "setUp", true);
	}

	/**
	 */
	@After
	public void tearDown() {
	}

	/**
	 * test of NexusExtractor.getNexusGroupData for an SDS and then an attribute The path of the item to get is obtained
	 * by reading in the tree from file TestFileFolder + "327.nxs" THe value returned should equal the value of the node
	 * from the tree used to get the path
	 *
	 * @throws NexusException
	 */
	@Test
	public void testGetNexusGroupDataURLStringBoolean() throws NexusException, NexusExtractorException {
		INexusTree tree = NexusTreeBuilder.getNexusTree(TestFileFolder + "327.nxs", NexusTreeNodeSelection
				.createTreeForAllNXData());
		INexusTree node = tree.getChildNode(0).getChildNode(22).getChildNode(0);
		NexusGroupData data = NexusExtractor.getNexusGroupData(((INexusSourceProvider) tree).getSource(), node
				.getNodePathWithClasses(), null, null, true);
		Assert.assertEquals(node.getData(), data);
		node = tree.getChildNode(0).getChildNode(22).getChildNode(0).getChildNode(1);
		data = NexusExtractor.getNexusGroupData(((INexusSourceProvider) tree).getSource(), node
				.getNodePathWithClasses(), null, null, true);
		Assert.assertEquals(node.getData(), data);

		node = tree.getChildNode(0).getChildNode(22).getChildNode(0);
		int[] dims = node.getData().dimensions;
		int[] startPos = new int[dims.length];
		dims[dims.length - 1] = 1;
		startPos[startPos.length - 1] = 1;
		data = NexusExtractor.getNexusGroupData(((INexusSourceProvider) tree).getSource(), node
				.getNodePathWithClasses(), startPos, dims, true);
		Assert.assertEquals(1, data.dimensions[dims.length - 1]);
	}

	/**
	 * test of NexusExtractor.getNexusGroupData for an SDS and then an attribute The path of the item to get is given
	 * explicitly
	 *
	 * @throws NexusException
	 * @throws MalformedURLException
	 */
	@Test
	public void testGetNexusGroupDataURLStringBoolean2() throws NexusException,
			MalformedURLException {
		String pathWithClass = "//" + "entry1" + "/" + NexusExtractor.NXEntryClassName + "/" + "EDXD_Element_22" + "/"
				+ NexusExtractor.NXDataClassName + "/" + "a" + "/" + NexusExtractor.SDSClassName + "/";
		URL url = new URL("file:" + new File(TestFileFolder + "327.nxs").getAbsolutePath());
		NexusGroupData data = NexusExtractor.getNexusGroupData(url, pathWithClass, null, null, true);
		Assert.assertTrue(data.isDouble() );
		Assert.assertEquals( 1, data.dimensions.length);
		Assert.assertEquals( 11, data.dimensions[0]);
		Assert.assertEquals( (Double)1.0, (Double)((double [])data.getBuffer())[10]);

		int[] dims = new int []{2};
		int[] startPos = new int[] { 2 };
		data = NexusExtractor.getNexusGroupData(url, pathWithClass, startPos, dims, true);
		Assert.assertTrue(data.isDouble());
		Assert.assertEquals( 1, data.dimensions.length);
		Assert.assertEquals( 2, data.dimensions[0]);
		Assert.assertEquals( (Double)0.2, (Double)((double [])data.getBuffer())[0]);
		Assert.assertEquals( (Double)0.3, (Double)((double [])data.getBuffer())[1]);
	}
}
