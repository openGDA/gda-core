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
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

import java.net.URI;
import java.util.Arrays;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyWriteableDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
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
		GroupNode childGroup = nf.getGroup("/a/b/c", false);
		GroupNode parentGroup = nf.getGroup("/a/b", false);
		assertEquals(1, parentGroup.getNames().size());
		assertTrue(parentGroup.getNames().contains("c"));
		assertSame(childGroup, parentGroup.getGroupNode("c"));
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
	public void testGroupProperties() throws Exception {
		//Test that the properties on a retrieved node match expectations without having traversed to children
		nf.getGroup("/a/b/c", true);
		nf.getGroup("/a/b/x", true);
		IDataset dataset = DatasetFactory.createRange(10.0, Dataset.FLOAT64).reshape(2, 5);
		dataset.setName("d");
		nf.createData("/a/b", dataset, false);
		IDataset attr = DatasetFactory.createFromObject(new int[] {1, 2, 3});
		IDataset attr2 = DatasetFactory.createFromObject(new int[] {4, 5});
		attr.setName("SomeAttribute");
		attr2.setName("Another Attribute");
		nf.addAttribute("/a/b", nf.createAttribute(attr));
		nf.addAttribute("/a/b/c", nf.createAttribute(attr2));
		nf.close();

		nf = NexusUtils.openNexusFileReadOnly(FILE_NAME);
		GroupNode group = nf.getGroup("/a/b", false);
		assertTrue(group.containsGroupNode("c"));
		assertTrue(group.containsGroupNode("x"));
		assertTrue(group.containsDataNode("d"));
		assertEquals(group.getDataNode("d").getDataset().getSlice(), dataset);
		assertTrue(group.containsAttribute("SomeAttribute"));
		assertEquals(attr, group.getAttribute("SomeAttribute").getValue());
		assertTrue(group.getGroupNode("c").containsAttribute("Another Attribute"));
		assertEquals(attr2, group.getGroupNode("c").getAttribute("Another Attribute").getValue());

		//Make sure group has no other children
		String[] childNames = group.getNames().toArray(new String[] {});
		//we could use assertTrue(childNames.contains(...)) but this gives a much better diagnostic on failure
		Arrays.sort(childNames);
		assertArrayEquals(childNames, new String[] {"c", "d", "x"});
	}

	@Test
	public void testDataNodeProperties() throws NexusException {
		IDataset dataset = DatasetFactory.createRange(10.0, Dataset.FLOAT64).reshape(2, 5);
		dataset.setName("d");
		nf.createData("/a/b", dataset, true);
		IDataset attr = DatasetFactory.createFromObject(new int[] {12, 3});
		attr.setName("SomeAttribute");
		nf.addAttribute("/a/b/d", nf.createAttribute(attr));
		nf.close();

		nf = NexusUtils.openNexusFileReadOnly(FILE_NAME);
		DataNode dataNode = nf.getData("/a/b/d");
		assertNotNull(dataNode);
		assertEquals(dataset, dataNode.getDataset().getSlice());
		Attribute foundAttr = dataNode.getAttribute("SomeAttribute");
		assertNotNull(foundAttr);
		assertEquals(attr, foundAttr.getValue());
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
	public void testGetDataWithAugmentedPath() throws Exception {
		GroupNode parentNode = nf.getGroup("/entry1:NXentry/instrument:NXinstrument/detector:NXdetector", true);
		IDataset dataset = DatasetFactory.createRange(10.0, Dataset.FLOAT64).reshape(2, 5);
		dataset.setName("data");
		nf.createData(parentNode, dataset);
		nf.close();

		nf = NexusUtils.openNexusFileReadOnly(FILE_NAME);
		GroupNode readNode = nf.getGroup("/entry1/instrument/detector", false);
		assertTrue(readNode.containsDataNode("data"));
		DataNode readDataNode = nf.getData("/entry1:NXentry/instrument:NXinstrument/detector:NXdetector/data");
		assertSame(readNode.getDataNode("data"), readDataNode);
		IDataset readData = readDataNode.getDataset().getSlice();
		assertEquals(dataset, readData);
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
		Dataset attrib2Dataset = DatasetFactory.createRange(6, Dataset.INT64).reshape(3, 2);
		attrib2Dataset.setName("testAttribute2");

		GroupNode node = nf.getGroup("/a/b/c", true);
		Attribute attribute = nf.createAttribute(attribDataset);
		assertNotNull(attribute);
		nf.addAttribute(node, attribute);
		Attribute attribute2 = nf.createAttribute(attrib2Dataset);
		assertNotNull(attribute2);
		nf.addAttribute(node, attribute2);
		assertNotNull(node.getAttribute("testAttribute"));
		assertSame(attribute, node.getAttribute("testAttribute"));
		assertNotNull(node.getAttribute("testAttribute2"));
		assertSame(attribute2, node.getAttribute("testAttribute2"));
	}

	@Test
	public void testReadbackDoubleArrayAttribute() throws Exception {
		GroupNode group = nf.getGroup("/a/b", true);
		IDataset attrData = DatasetFactory.createRange(12.0, Dataset.FLOAT64).reshape(3, 4);
		attrData.setName("attribute");
		nf.addAttribute(group, nf.createAttribute(attrData));
		nf.close();

		nf = NexusUtils.openNexusFileReadOnly(FILE_NAME);
		group = nf.getGroup("/a/b", false);
		Attribute attr = group.getAttribute("attribute");
		assertNotNull(attr);
		IDataset readData = attr.getValue();
		assertNotNull(readData);
		assertEquals(attrData, readData);
	}

	@Test
	public void testReadbackStringAttribute() throws Exception {
		GroupNode group = nf.getGroup("/a/b", true);
		IDataset attrString = DatasetFactory.createFromObject("SomeÅttributeString");
		attrString.setName("test");
		nf.addAttribute(group, nf.createAttribute(attrString));
		nf.close();
		nf = NexusUtils.openNexusFileReadOnly(FILE_NAME);
		group = nf.getGroup("/a/b", false);
		Attribute attr = group.getAttribute("test");
		assertNotNull(attr);
		if (attrString.getRank() == 0) {
			//Hack around the current ambiguity between scalar datasets (rank 0) and single element arrays
			attrString.resize(new int[] {1});
		}
		assertEquals(attrString, attr.getValue());
	}

	@Test
	public void testReadbackStringArrayAttribute() throws Exception {
		GroupNode group = nf.getGroup("/a/b", true);
		IDataset attrString = DatasetFactory.createFromObject(new String[] {"A String", "String1", "SomeÅttributeString", "String2"}).reshape(2,2);
		attrString.setName("test");
		nf.addAttribute(group, nf.createAttribute(attrString));
		nf.close();
		nf = NexusUtils.openNexusFileReadOnly(FILE_NAME);
		group = nf.getGroup("/a/b", false);
		Attribute attr = group.getAttribute("test");
		assertNotNull(attr);
		assertEquals(attrString, attr.getValue());
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
	public void testHardLinkDataset() throws Exception {
		IDataset dataset = DatasetFactory.createRange(10.0, Dataset.FLOAT64).reshape(2, 5);
		dataset.setName("data");
		nf.createData("/a/", dataset, true);
		nf.link("/a/data", "/x/data");
		DataNode dataNode = nf.getData("/a/data");
		DataNode linkedNode = nf.getData("/x/data");
		assertSame(linkedNode, nf.getData("/a/data"));
		nf.close();
		nf = NexusUtils.openNexusFileReadOnly(FILE_NAME);
		dataNode = nf.getData("/a/data");
		linkedNode = nf.getData("/x/data");
		assertNotNull(linkedNode);
		assertNotNull(linkedNode.getDataset().getSlice());
		assertSame(dataNode, linkedNode);
	}


	@Test
	public void testLinkExternal() throws Exception {
		try (NexusFile extFile = NexusUtils.createNexusFile(FILE2_NAME)) {
			extFile.getGroup("/d/e/f/g", true);
		}
		nf.linkExternal(new URI("nxfile://" + FILE2_NAME + "#/d/e"), "/a/b/c", true);
		GroupNode groupC = nf.getGroup("/a/b/c", false);
		GroupNode groupF = nf.getGroup(groupC, "f", null, false);
		GroupNode groupG = nf.getGroup(groupF, "g", null, false);
		assertNotNull(groupG);
		assertSame(groupG, nf.getGroup("/a/b/c/f/g", false));
	}

	@Test
	public void testLinkExternalUnderRoot() throws Exception {
		try (NexusFile extFile = NexusUtils.createNexusFile(FILE2_NAME)) {
			extFile.getGroup("/d", true);
		}
		nf.linkExternal(new URI("nxfile://" + FILE2_NAME + "#/d"), "/a", true);
		assertNotNull(nf.getGroup("/a", false));
	}

	@Test
	public void testLinkExternalUseSourceName() throws Exception {
		try (NexusFile extFile = NexusUtils.createNexusFile(FILE2_NAME)) {
			extFile.getGroup("/e/f/g", true);
		}
		nf.linkExternal(new URI("nxfile://" + FILE2_NAME + "#e"), "/a/b/c/d/", true);
		GroupNode groupD = nf.getGroup("/a/b/c/d", false);
		GroupNode groupE = nf.getGroup(groupD, "e", null, false);
		assertNotNull(groupE);
	}

	@Test
	public void testLinkExternalDatasetUseGivenName() throws Exception {
		IDataset externalData = DatasetFactory.createRange(10.0, Dataset.FLOAT64).reshape(2, 5);
		externalData.setName("data");
		try (NexusFile ef = NexusUtils.createNexusFile(FILE2_NAME)) {
			ef.createData("/a/b/c", externalData, true);
		}
		nf.linkExternal(new URI("nxfile://" + FILE2_NAME + "#a/b/c/data"), "/x/y/linkedData", false);
		DataNode dataNode = nf.getData("/x/y/linkedData");
		IDataset linkedData = dataNode.getDataset().getSlice();
		assertNotNull(linkedData);
		assertEquals(externalData, linkedData);
	}

	@Test
	public void testLinkExternalDatasetUseSourceName() throws Exception {
		IDataset externalData = DatasetFactory.createRange(10.0, Dataset.FLOAT64).reshape(2, 5);
		externalData.setName("data");
		try (NexusFile ef = NexusUtils.createNexusFile(FILE2_NAME)) {
			ef.createData("/a/b/c", externalData, true);
		}
		nf.linkExternal(new URI("nxfile://" + FILE2_NAME + "#a/b/c/data"), "/x/y/", false);
		DataNode dataNode = nf.getData("/x/y/data");
		IDataset linkedData = dataNode.getDataset().getSlice();
		assertNotNull(linkedData);
		assertEquals(externalData, linkedData);
	}

	@Test
	public void testLinkExternalSameFile() throws Exception {
		//"soft link" implementation
		nf.getGroup("/a/b/c", true);
		nf.linkExternal(new URI("#/a/b"), "/x", true);
		GroupNode groupXC = nf.getGroup("/x/c", false);
		GroupNode groupABC = nf.getGroup("/a/b/c", false);
		assertNotNull(groupXC);
		assertSame(groupXC, groupABC);
	}

	@Test
	public void testSoftLinkDataset() throws Exception {
		IDataset dataset = DatasetFactory.createRange(10.0, Dataset.FLOAT64).reshape(2, 5);
		dataset.setName("data");
		nf.createData("/a/", dataset, true);
		nf.linkExternal(new URI("#/a/data"), "/x/", false);
		DataNode dataNode = nf.getData("/a/data");
		DataNode linkedDataNode = nf.getData("/x/data");
		assertSame(dataNode, linkedDataNode);
	}

	@Test
	public void testRelativeExternalLink() throws Exception {
		IDataset externalData = DatasetFactory.createRange(10.0, Dataset.FLOAT64).reshape(2, 5);
		externalData.setName("data");
		try (NexusFile ef = NexusUtils.createNexusFile(FILE2_NAME)) {
			ef.createData("/a/b/c", externalData, true);
		}
		nf.linkExternal(new URI("nxfile://" + FILE2_NAME.replaceFirst("/tmp/", "") + "#a/b/c/data"), "/x/y/", false);
		DataNode dataNode = nf.getData("/x/y/data");
		IDataset linkedData = dataNode.getDataset().getSlice();
		assertNotNull(linkedData);
		assertEquals(externalData, linkedData);
	}

	@Test
	public void testIsPathValid() throws Exception {
		nf.getGroup("/a/b/c", true);
		assertTrue(nf.isPathValid("/a/b/c"));
		assertFalse(nf.isPathValid("/a/b/c/d"));
	}

	@Test
	public void testWritingSimpleStringArray() throws Exception {
		GroupNode g = nf.getGroup("/entry1:NXentry", true);
		NexusUtils.write(nf, g, "stringarray", new String[] {"String", "String Å"});
		nf.close();

		nf = NexusUtils.openNexusFileReadOnly(FILE_NAME);
		DataNode d = nf.getData("/entry1/stringarray");
		IDataset ds = d.getDataset().getSlice();
		int[] shape = ds.getShape();
		assertArrayEquals(new int[] {2}, shape);
	}

	@Test
	public void testWriteSimpleString() throws Exception {
		GroupNode g = nf.getGroup("/note:NXnote", true);
		NexusUtils.write(nf, g, "somestring", "MyString");
		nf.close();

		nf = NexusUtils.openNexusFileReadOnly(FILE_NAME);
		DataNode d = nf.getData("/note/somestring");
		IDataset ds = d.getDataset().getSlice();
		int[] shape = ds.getShape();
		assertArrayEquals(new int[] {1}, shape);
		assertEquals("MyString", ds.getString(0));
	}

	@Test
	public void testLazyWriteStringArray() throws Exception {
		int nPoints = 10;

		GroupNode g = nf.getGroup("/test:NXnote", true);
		ILazyWriteableDataset lazy = NexusUtils.createLazyWriteableDataset("stringarray", Dataset.STRING,
				new int[] {ILazyWriteableDataset.UNLIMITED}, null, null);
		nf.createData(g, lazy);
		nf.close();

		for (int i = 0; i < nPoints; i++) {
			lazy.setSlice(null, DatasetFactory.createFromObject("file" + i),
					SliceND.createSlice(lazy, new int[] {i}, new int[] {i+1}));
		}

		nf = NexusUtils.openNexusFileReadOnly(FILE_NAME);
		DataNode d = nf.getData("/test/stringarray");
		IDataset ds = d.getDataset().getSlice();
		int[] shape = ds.getShape();
		assertArrayEquals(new int[] {nPoints}, shape);
		for (int i = 0; i < nPoints; i++) {
			assertEquals("file" + i, ds.getString(i));
		}
	}

	@Test
	public void testLazyWrite2DInt32Array() throws Exception {
		GroupNode g = nf.getGroup("/test:NXnote", true);
		ILazyWriteableDataset lazy = NexusUtils.createLazyWriteableDataset("intarray", Dataset.INT32,
				new int[] {ILazyWriteableDataset.UNLIMITED, 10}, null, null);
		nf.createData(g, lazy);
		lazy.setSlice(null, DatasetFactory.createFromObject(new int[] {-1, -1, -1, -1}).reshape(2, 2), new int[] {0, 0}, new int[] {2, 2}, null);
		nf.close();
		nf = NexusUtils.openNexusFileReadOnly(FILE_NAME);
		DataNode node = nf.getData("/test/intarray");
		IDataset data = node.getDataset().getSlice(new int[] {0, 0}, new int[] {2, 2}, new int[] {1, 1});
		assertArrayEquals(new int[] {-1, -1, -1, -1}, (int[])((Dataset) data).getBuffer());
	}

	@Test
	public void testLazyWrite2DDoubleArray() throws Exception {
		GroupNode g = nf.getGroup("/test:NXnote", true);
		ILazyWriteableDataset lazy = NexusUtils.createLazyWriteableDataset("doublearray", Dataset.FLOAT64,
				new int[] {ILazyWriteableDataset.UNLIMITED, 10}, null, null);
		nf.createData(g, lazy);
		lazy.setSlice(null, DatasetFactory.createFromObject(new double[] {1, 2, 3, 4}).reshape(2, 2), new int[] {0, 0}, new int[] {2, 2}, null);
		nf.close();
		nf = NexusUtils.openNexusFileReadOnly(FILE_NAME);
		DataNode node = nf.getData("/test/doublearray");
		IDataset data = node.getDataset().getSlice(new int[] {0, 0}, new int[] {2, 2}, new int[] {1, 1});
		assertArrayEquals(new double[] {1, 2, 3, 4}, (double[])((Dataset) data).getBuffer(), 1e-12);
	}

	@Test
	public void testLazyWrite2DStringArray() throws Exception {
		GroupNode g = nf.getGroup("/test:NXnote", true);
		ILazyWriteableDataset lazy = NexusUtils.createLazyWriteableDataset("stringarray", Dataset.STRING,
				new int[] {2, 10}, new int[] {ILazyWriteableDataset.UNLIMITED, 10}, null);
		nf.createData(g, lazy);
		lazy.setSlice(null, DatasetFactory.createFromObject(new String[] {"Value1", "Value2"}).reshape(2, 1), new int[] {2, 0}, new int[] {4, 1}, null);
		nf.close();
		nf = NexusUtils.openNexusFileReadOnly(FILE_NAME);
		DataNode node = nf.getData("/test/stringarray");
		IDataset data = node.getDataset().getSlice();
		assertEquals("Value1", data.getString(2, 0));
	}

	@Test
	public void testWriteAfterHardLink() throws Exception {
		//Have had bugs where writing created a "new" dataset, leaving previously
		//hard linked nodes un-updated
		ILazyWriteableDataset lazyData = NexusUtils.createLazyWriteableDataset("c", Dataset.INT32,
				new int[] {ILazyWriteableDataset.UNLIMITED}, null, null);
		GroupNode g = nf.getGroup("/a/b", true);
		nf.createData(g, lazyData);
		nf.link("/a/b/c", "/x/");
		lazyData.setSlice(null, DatasetFactory.createFromObject(1), new int[] {0}, new int[] {1}, null);
		assertSame(nf.getData("/a/b/c").getDataset(), nf.getData("/x/c").getDataset());
		nf.flush();
		nf.close();
		nf = NexusUtils.openNexusFileReadOnly(FILE_NAME);
		assertEquals(nf.getData("/a/b/c").getDataset(), nf.getData("/x/c").getDataset());
	}

	@Test
	public void testNxClassGroup() throws Exception {
		GroupNode g = nf.getGroup("/entry1:NXentry/note:NXnote", true);
		GroupNode e = nf.getGroup("/entry1", false);
		Attribute gAttr = g.getAttribute("NX_class");
		Attribute eAttr = e.getAttribute("NX_class");
		assertNotNull(gAttr);
		assertArrayEquals(gAttr.getValue().getShape(), new int[] {1});
		assertEquals(gAttr.getValue().getString(0), "NXnote");
		assertNotNull(eAttr);
		assertArrayEquals(eAttr.getValue().getShape(), new int[] {1});
		assertEquals(eAttr.getValue().getString(0), "NXentry");

		nf.close();
		nf = NexusUtils.openNexusFileReadOnly(FILE_NAME);
		g = nf.getGroup("/entry1:NXentry/note:NXnote", true);
		e = nf.getGroup("/entry1", false);
		gAttr = g.getAttribute("NX_class");
		eAttr = e.getAttribute("NX_class");
		assertNotNull(gAttr);
		assertArrayEquals(gAttr.getValue().getShape(), new int[] {1});
		assertEquals(gAttr.getValue().getString(0), "NXnote");
		assertNotNull(eAttr);
		assertArrayEquals(eAttr.getValue().getShape(), new int[] {1});
		assertEquals(eAttr.getValue().getString(0), "NXentry");
	}
}
