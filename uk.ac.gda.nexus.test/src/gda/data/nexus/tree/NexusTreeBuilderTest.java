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

package gda.data.nexus.tree;

import java.io.File;
import java.io.StringReader;
import java.net.URL;

import org.eclipse.dawnsci.hdf5.nexus.NexusException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.InputSource;

import gda.data.nexus.extractor.NexusExtractorException;
import gda.util.TestUtils;

public class NexusTreeBuilderTest {
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
		scratchFolder=TestUtils.setUpTest(NexusTreeBuilderTest.class, "setUp", true);
	}


	@After
	public void tearDown() {
	}


	private NexusTreeNodeSelection getSelForAllButData() throws Exception{
		String xml = "<?xml version='1.0' encoding='UTF-8'?>" +
		"<nexusTreeNodeSelection>" +
		"<nexusTreeNodeSelection><nxClass>NXentry</nxClass><wanted>2</wanted><dataType>2</dataType>" +
		"<nexusTreeNodeSelection><nxClass>NXdata</nxClass><wanted>2</wanted><dataType>2</dataType>" +
		"<nexusTreeNodeSelection><nxClass>SDS</nxClass><wanted>2</wanted><dataType>1</dataType>" +
		"</nexusTreeNodeSelection>" +
		"</nexusTreeNodeSelection>" +
		"<nexusTreeNodeSelection><nxClass>NXinstrument</nxClass><wanted>2</wanted><dataType>2</dataType>" +
		"<nexusTreeNodeSelection><nxClass>NXdetector</nxClass><wanted>2</wanted><dataType>2</dataType>" +
		"<nexusTreeNodeSelection><nxClass>SDS</nxClass><wanted>2</wanted><dataType>1</dataType>" +
		"</nexusTreeNodeSelection>" +
		"</nexusTreeNodeSelection>" +
		"</nexusTreeNodeSelection>" +
		"</nexusTreeNodeSelection>" +
		"</nexusTreeNodeSelection>";
		return NexusTreeNodeSelection.createFromXML(new InputSource(new StringReader(xml)));
	}

	@Test
	public void testNexusTreeBuilderToGetStructureButNotData() throws NexusException, NexusExtractorException, Exception {
		INexusTree tree = NexusTreeBuilder.getNexusTree(TestFileFolder + File.separator + "327.nxs", getSelForAllButData());
		Assert.assertEquals("test",tree.getChildNode(0).getChildNode(26).getChildNode(22).getChildNode(5).getData().dimensions[0],11);
		Assert.assertEquals("test",tree.getChildNode(0).getChildNode(26).getChildNode(22).getChildNode(5).getData().getBuffer(),null);
	}
}
