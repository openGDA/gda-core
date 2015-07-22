/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;

import org.eclipse.dawnsci.analysis.api.dataset.ILazyWriteableDataset;
import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.LazyWriteableDataset;
import org.eclipse.dawnsci.hdf5.nexus.NexusException;
import org.eclipse.dawnsci.hdf5.nexus.NexusFile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NexusFileTest {

	private static final String FILE_NAME = "/tmp/test.nxs";
	private static final String FILE2_NAME = "/tmp/ext-test.nxs";

	private NexusFile nf;

	@Before
	public void setUp() throws Exception {
		nf = NexusUtils.createNexusFile(FILE_NAME);
	}

	@After
	public void tearDown() throws Exception {
		nf.close();
		nf = null;
	}

	@Test
	public void testOpenToRead() throws Exception {
		// create a group and close the file
		nf.getGroup("/a/b/c", true);
		nf.close();

		// open the file to read and test that the created group is present
		nf.openToRead();
		assertNotNull(nf.getGroup("/a/b/c", false));
	}

	@Test(expected = NexusException.class)
	public void testOpenToReadTryWrite() throws Exception {
		// create a group and close the file
		nf.getGroup("/a/b/c", true);
		nf.close();

		// open the file to read only, and try writing to it, exception should be thrown
		nf.openToRead();
		nf.getGroup("/e/f/g", true);
	}

	@Test
	public void testOpenToWrite() throws Exception {
		// create a group and close the file
		nf.getGroup("/a/b/c", true);
		nf.close();

		// open the file to write and check the previously written group is still there
		nf.openToWrite(false);
		assertNotNull(nf.getGroup("/a/b/c", false));

		// write a new group (would throw an exception if file wasn't writable)
		nf.getGroup("/e/f/g", true);
	}

	@Test
	public void testCreateAndOpenToWrite() throws Exception {
		// create a group and close the file
		nf.getGroup("/a/b/c", true);
		nf.close();

		// create and open the file to write - overwrites the old file
		nf.createAndOpenToWrite();
		try {
			// try getting the previously created group, should not exist
			// as createAndOpenToWrite overwrites the old file
			nf.getGroup("/a/b/c", false);
			fail("Group should not exist as old file should have been overwritten");
		} catch (NexusException e) {
			// fall through
		}

		// try writing a new group
		nf.getGroup("/e/f/g", true);
		assertNotNull(nf.getGroup("/e/f/g", false));
	}

	@Test
	public void testGetPath() throws Exception {
		final String path = "/a/b/c/";
		GroupNode groupNode = nf.getGroup(path, true);
		assertEquals(path, nf.getPath(groupNode));
	}

	@Test
	public void testGetGroup() throws Exception {
		// create a new group
		GroupNode group = nf.getGroup("/a/b/c", true);
		assertNotNull(group);

		// test that the group is a child of its expected parent group
		GroupNode parentGroup = nf.getGroup("/a/b", false);
		assertEquals(1, parentGroup.getNames().size());
		assertTrue(parentGroup.getNames().contains("c"));
	}

	@Test(expected = NexusException.class)
	public void testGetGroupNoCreate() throws Exception {
		nf.getGroup("/a/b/c/d", false);
	}

	@Test
	public void testGetGroupOfClass() throws Exception {
		// create a new group of class Nxtext
		final String className = "NXtext";
		GroupNode parentGroup = nf.getGroup("/a/b", true);
		GroupNode group = nf.getGroup(parentGroup, "c", className, true);

		assertEquals(className, group.getAttribute("NX_class").getFirstElement());
		assertEquals(1, parentGroup.getNames().size());
		assertTrue(parentGroup.getNames().contains("c"));
	}

	@Test
	public void testCreateDataPathLazyDataset() throws Exception {
		int[] shape = { 5, 5 };
		ILazyWriteableDataset dataset = new LazyWriteableDataset("data", Dataset.INT32, shape, shape, null, null);
		DataNode dataNode = nf.createData("/a/b/c", dataset, true);
		assertNotNull(dataNode);
		assertSame(dataset, dataNode.getDataset());

		GroupNode parentGroup = nf.getGroup("/a/b/c", false);
		assertSame(dataNode, parentGroup.getDataNode("data"));
	}

	@Test
	public void testCreateDataGroupNodeLazyDataset() throws Exception {
		GroupNode parentGroup = nf.getGroup("/a/b/c", true);

		int[] shape = { 5, 5 };
		ILazyWriteableDataset dataset = new LazyWriteableDataset("data", Dataset.INT32, shape, shape, null, null);
		DataNode dataNode = nf.createData(parentGroup, dataset);
		assertNotNull(dataNode);
		assertSame(dataset, dataNode.getDataset());

		assertSame(dataNode, parentGroup.getDataNode("data"));
	}

	@Test
	public void testCreateDataPathDataset() throws Exception {
		Dataset dataset = DatasetFactory.createRange(10.0, Dataset.FLOAT64).reshape(2, 5);
		dataset.setName("data");
		DataNode dataNode = nf.createData("/a/b/c", dataset, true);
		assertNotNull(dataNode);
		assertSame(dataset, dataNode.getDataset());

		GroupNode parentGroup = nf.getGroup("/a/b/c", false);
		assertSame(dataNode, parentGroup.getDataNode("data"));
	}

	@Test
	public void testCreateDataGroupNodeDataset() throws Exception {
		GroupNode parentGroup = nf.getGroup("/a/b/c", true);

		Dataset dataset = DatasetFactory.createRange(10.0, Dataset.FLOAT64).reshape(2, 5);
		dataset.setName("data");
		DataNode dataNode = nf.createData(parentGroup, dataset);
		assertNotNull(dataNode);
		assertSame(dataset, dataNode.getDataset());

		assertSame(dataNode, parentGroup.getDataNode("data"));
	}

	@Test
	public void testAddAttributeNode() throws Exception {
		Dataset attribDataset = DatasetFactory.createRange(10.0, Dataset.FLOAT64).reshape(2, 5);
		attribDataset.setName("testAttribute");

		GroupNode node = nf.getGroup("/a/b/c", true);
		Attribute attribute = nf.createAttribute(attribDataset);
		assertNotNull(attribute);
		nf.addAttribute(node, attribute);

		assertNotNull(node.getAttribute("testAttribute"));
		assertSame(attribute, node.getAttribute("testAttribute"));
	}

	@Test
	public void testAddAttributePath() throws Exception {
		Dataset attribDataset = DatasetFactory.createRange(10.0, Dataset.FLOAT64).reshape(2, 5);
		attribDataset.setName("testAttribute");

		GroupNode node = nf.getGroup("/a/b/c", true);
		Attribute attribute = nf.createAttribute(attribDataset);
		assertNotNull(attribute);
		nf.addAttribute(node, attribute);
		assertNotNull(node.getAttribute("testAttribute"));
		assertSame(attribute, node.getAttribute("testAttribute"));
	}

	@Test
	public void testLink() throws Exception {
		nf.getGroup("/a/b/c/d", true);
		nf.link("/a/b/c", "/f/g");

		GroupNode linkedGroup = nf.getGroup("/f/g", false);
		assertNotNull(linkedGroup);
		assertNotNull(linkedGroup.getGroupNode("d"));
	}

	@Test
	public void testLinkExternal() throws Exception {
		NexusFile extFile = null;
		try {
			extFile = NexusUtils.createNexusFile(FILE2_NAME);
			extFile.getGroup("/e/f/g", true);
			nf.linkExternal(new URI("nxfile:///./" + FILE2_NAME + "#e"), "/a/b/c", true);
			GroupNode groupB = nf.getGroup("/a/b", false);
			GroupNode groupE = groupB.getGroupNode("e");
			// TODO this test fails with NexusFileNAPI due to a bug
			// TODO confirm, should we see /a/b/c/e or /a/b/e ?
			assertNotNull(groupE);
		} finally {
			if (extFile != null) {
				extFile.close();
			}
		}
	}

	@Test
	public void testIsPathValid() throws Exception {
		nf.getGroup("/a/b/c", true);
		assertTrue(nf.isPathValid("/a/b/c"));
		assertFalse(nf.isPathValid("/a/b/c/d"));
	}

}
