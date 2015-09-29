package org.eclipse.dawnsci.nexus;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import gda.data.nexus.NexusUtils;
import gda.util.TestUtils;

import java.util.Iterator;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.analysis.dataset.impl.AbstractDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.PositionIterator;
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.analysis.tree.impl.TreeFileImpl;
import org.eclipse.dawnsci.hdf5.nexus.NexusException;
import org.eclipse.dawnsci.hdf5.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.impl.NXentryImpl;
import org.eclipse.dawnsci.nexus.impl.NXobjectFactory;
import org.eclipse.dawnsci.nexus.impl.NXrootImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public abstract class AbstractNexusFileTest {

	protected NXobjectFactory nxObjectFactory;

	private static String testScratchDirectoryName;

	private String FILE_PATH;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testScratchDirectoryName = TestUtils.generateDirectorynameFromClassname(AbstractNexusFileTest.class.getCanonicalName());
		TestUtils.makeScratchDirectory(testScratchDirectoryName);
	}

	@Before
	public void setUp() {
		nxObjectFactory = new NXobjectFactory();
		FILE_PATH = testScratchDirectoryName + getFilename();
	}

	@After
	public void tearDown() {
		nxObjectFactory = null;
	}

	protected abstract String getFilename();

	protected abstract NXroot createNXroot();

	private TreeFile createNexusTree() {
		final TreeFileImpl treeFile = nxObjectFactory.createTreeFile(FILE_PATH);
		final NXroot root = createNXroot();
		treeFile.setGroupNode(root);

		return treeFile;
	}

	@Test
	public void testNexusFile() throws Exception {
		TreeFile originalNexusTree = createNexusTree();

		saveFile(originalNexusTree);
		TreeFile loadedTree = loadFile();

		assertNexusTreesEqual(originalNexusTree, loadedTree);

		// TODO close loaded tree
	}

	private void saveFile(TreeFile nexusTree) throws Exception {
		NexusFile nexusFile = NexusUtils.createNexusFile(nexusTree.getFilename());
		try {
			nexusFile.addNode("/", nexusTree.getGroupNode());
		} finally {
			nexusFile.flush();
			nexusFile.close();
		}
	}

	private TreeFile loadFile() throws NexusException {
		NexusFile nexusFile = NexusUtils.openNexusFileReadOnly(FILE_PATH);
		assertNotNull(nexusFile);
		GroupNode rootNode = nexusFile.getGroup("/", false);
		assertNotNull(rootNode);
		assertTrue(rootNode instanceof NXrootImpl);

		// GroupNode entry = nexusFile.getGroup("/entry", false);
		GroupNode entry = rootNode.getGroupNode("entry");
		assertNotNull(entry);
		assertEquals("NXentry", entry.getAttribute(NexusFile.NXCLASS).getFirstElement());
		assertTrue(entry instanceof NXentryImpl);

		TreeFile treeFile = TreeFactory.createTreeFile(0l, FILE_PATH);
		treeFile.setGroupNode(rootNode);

		return treeFile;
	}

	private void assertNexusTreesEqual(final TreeFile tree1, final TreeFile tree2) throws Exception {
		// TODO
		// assertGroupNodesEqual(tree1.getGroupNode(), tree2.getGroupNode());
	}

	private void assertGroupNodesEqual(final GroupNode group1, final GroupNode group2) throws Exception {
		if (group1 == group2) {
			return;
		}

		if (group1 instanceof NXobject) {
			assertTrue(group2 instanceof NXobject);
			assertEquals(((NXobject) group1).getNXclass(), ((NXobject) group2).getNXclass());
		}

		// check attributes same
		Iterator<String> attributeNameIterator = group1.getAttributeNameIterator();
		while (attributeNameIterator.hasNext()) {
			String attributeName = attributeNameIterator.next();
			Attribute attr1 = group1.getAttribute(attributeName);
			Attribute attr2 = group2.getAttribute(attributeName);
			assertNotNull(attr2);
			assertAttributesEquals(attr1, attr2);
		}
		assertEquals(group1.getNumberOfAttributes(), group2.getNumberOfAttributes());

		// check child nodes same
		final Iterator<String> nodeNameIterator = group1.getNodeNameIterator();
		while (nodeNameIterator.hasNext()) {
			String nodeName = nodeNameIterator.next();
			// node is either a group node or data node
			if (group1.containsGroupNode(nodeName)) {
				assertTrue(group2.containsGroupNode(nodeName));
				assertGroupNodesEqual(group1.getGroupNode(nodeName), group2.getGroupNode(nodeName));
			} else {
				// node is a data node
				assertTrue(group1.containsDataNode(nodeName));
				assertTrue(group2.containsDataNode(nodeName));
				assertDataNodesEqual(group1.getDataNode(nodeName), group2.getDataNode(nodeName));
			}
		}

		assertEquals(group1.getNumberOfDataNodes(), group2.getNumberOfDataNodes());
		assertEquals(group1.getNumberOfGroupNodes(), group2.getNumberOfGroupNodes());
	}

	private void assertAttributesEquals(final Attribute attr1, final Attribute attr2) throws Exception {
		assertEquals(attr1.getName(), attr2.getName());
		assertEquals(attr1.getTypeName(), attr2.getTypeName());
		assertEquals(attr1.getFirstElement(), attr2.getFirstElement());
		assertEquals(attr1.getSize(), attr2.getSize());
		assertEquals(attr1.getRank(), attr2.getRank());
		assertArrayEquals(attr1.getShape(), attr2.getShape());
		assertDatasetsEqual(attr1.getValue(), attr2.getValue());
	}

	private void assertDataNodesEqual(final DataNode dataNode1, final DataNode dataNode2) throws Exception {
		// check attributes same
		Iterator<String> attributeNameIterator = dataNode1.getAttributeNameIterator();
		while (attributeNameIterator.hasNext()) {
			String attributeName = (String) attributeNameIterator.next();
			Attribute attr1 = dataNode1.getAttribute(attributeName);
			Attribute attr2 = dataNode2.getAttribute(attributeName);
			assertNotNull(attr2);
			assertAttributesEquals(attr1, attr2);
		}
		assertEquals(dataNode1.getNumberOfAttributes(), dataNode2.getNumberOfAttributes());

		assertEquals(dataNode1.getTypeName(), dataNode2.getTypeName());
		assertEquals(dataNode1.isAugmented(), dataNode2.isAugmented());
		assertEquals(dataNode1.isString(), dataNode2.isString());
		assertEquals(dataNode1.isSupported(), dataNode2.isSupported());
		assertEquals(dataNode1.isUnsigned(), dataNode2.isUnsigned());
		assertEquals(dataNode1.getMaxStringLength(), dataNode2.getMaxStringLength());
		assertArrayEquals(dataNode1.getMaxShape(), dataNode2.getMaxShape());
		assertArrayEquals(dataNode1.getChunkShape(), dataNode2.getChunkShape());

		assertEquals(dataNode1.getString(), dataNode2.getString());
		assertDatasetsEqual(dataNode1.getDataset(), dataNode2.getDataset());
	}

	private void assertDatasetsEqual(final ILazyDataset dataset1, final ILazyDataset dataset2) throws Exception {
		assertEquals(dataset1.getName(), dataset2.getName());
		assertEquals(dataset1.getClass(), dataset2.getClass());
		assertEquals(dataset1.elementClass(), dataset2.elementClass());
		assertEquals(dataset1.getElementsPerItem(), dataset2.getElementsPerItem());
		assertEquals(dataset1.getRank(), dataset2.getRank());
		assertArrayEquals(dataset1.getShape(), dataset2.getShape());
		assertEquals(dataset1.getSize(), dataset2.getSize());

		assertDatasetDataEqual(dataset1, dataset2);

		// TODO check metadata

	}

	private void assertDatasetDataEqual(final ILazyDataset dataset1, final ILazyDataset dataset2) throws Exception {
		if (dataset1 instanceof AbstractDataset) {
			assertEquals(dataset1, dataset2);
		} else {
			// getSlice() with no args loads whole dataset if a lazy dataset
			IDataset dataset1Slice = dataset1.getSlice();
			IDataset dataset2Slice = dataset2.getSlice();

			final int datatype = AbstractDataset.getDType(dataset1);
			PositionIterator positionIterator = new PositionIterator(dataset1.getShape());
			while (positionIterator.hasNext()) {
				int[] position = positionIterator.getPos();
				switch (datatype) {
				case Dataset.BOOL:
					assertEquals(dataset1Slice.getBoolean(position), dataset2Slice.getBoolean(position));
					break;
				case Dataset.INT8:
					assertEquals(dataset1Slice.getByte(position), dataset2Slice.getByte(position));
					break;
				case Dataset.INT32:
					assertEquals(dataset1Slice.getInt(position), dataset2Slice.getInt(position));
					break;
				case Dataset.INT64:
					assertEquals(dataset1Slice.getLong(position), dataset2Slice.getLong(position));
					break;
				case Dataset.FLOAT32:
					assertEquals(dataset1Slice.getFloat(position), dataset2Slice.getFloat(), 1e-7);
					break;
				case Dataset.FLOAT64:
					assertEquals(dataset1Slice.getDouble(position), dataset2Slice.getDouble(position), 1e-15);
					break;
				case Dataset.STRING:
				case Dataset.DATE:
					assertEquals(dataset1Slice.getString(position), dataset2Slice.getString(position));
					break;
				case Dataset.COMPLEX64:
				case Dataset.COMPLEX128:
				case Dataset.OBJECT:
					assertEquals(dataset1Slice.getObject(position), dataset2Slice.getObject(position));
					break;
				}
			}
		}
	}


}
