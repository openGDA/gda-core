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

package gda.data.nexus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeNotNull;

import java.io.File;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeBuilder;
import gda.data.nexus.tree.NexusTreeNode;
import gda.data.nexus.tree.NexusTreeNodeSelection;
import gda.util.TestUtils;
import uk.ac.gda.util.OSUtils;


public class NexusReadinTest {

	private String filename;


	@Before
	public void setUpClass() {
		var testFileFolder = TestUtils.getGDALargeTestFilesLocation();
		assumeNotNull(testFileFolder); // Skip test if property not set
		var path = Paths.get(testFileFolder).resolve(getClass().getSimpleName()).resolve("nexus-readin.nxs");
		assertTrue("Test Nexus file doesn't exist", Files.exists(path));
		filename = path.toString();
	}

	@Test
	public void readValue() throws Exception {
		assumeFalse(this.getClass().getCanonicalName() + ".readValue skipped, since this test fails in native code on a 32-bit machine", OSUtils.is32bitJVM());
		assert new File(filename).canRead();
		assertEquals(209.0, getValue(), 0);
	}

	private double getValue() throws Exception {
		INexusTree tree_out2 = NexusTreeBuilder.getNexusTree(filename, getSelection());
		NexusTreeNode node = (NexusTreeNode) tree_out2.getChildNode(0).getChildNode("Wax", NexusExtractor.NXDataClassName).getChildNode(0);
		double [] data = (double[]) node.groupData.getBuffer();
		return data[ 4* node.groupData.dimensions[2] + 5];
	}

	private NexusTreeNodeSelection getSelection() throws Exception {
		String xml = "<?xml version='1.0' encoding='UTF-8'?>" +
		"<nexusTreeNodeSelection>" +
		"<nexusTreeNodeSelection><nxClass>NXentry</nxClass><wanted>1</wanted><dataType>2</dataType>" +
		"<nexusTreeNodeSelection><nxClass>NXdata</nxClass><name>.*</name><wanted>1</wanted><dataType>2</dataType>" +
		"<nexusTreeNodeSelection><nxClass>SDS</nxClass><name>data</name><wanted>1</wanted><dataType>2</dataType>" +
		"</nexusTreeNodeSelection>" +
		"</nexusTreeNodeSelection>" +
		"</nexusTreeNodeSelection>" +
		"</nexusTreeNodeSelection>";
		return NexusTreeNodeSelection.createFromXML(new InputSource(new StringReader(xml)));
	}
}