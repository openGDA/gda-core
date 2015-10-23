package gda.data.nexus;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
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
import org.eclipse.dawnsci.analysis.tree.impl.TreeFileImpl;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.impl.NexusNodeFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public abstract class AbstractNexusFileTestBase {

	protected NexusNodeFactory nexusNodeFactory;

	private static String testScratchDirectoryName;

	private String FILE_PATH;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		testScratchDirectoryName = TestUtils.generateDirectorynameFromClassname(AbstractNexusFileTestBase.class.getCanonicalName());
		TestUtils.makeScratchDirectory(testScratchDirectoryName);
	}

	@Before
	public void setUp() {
		nexusNodeFactory = new NexusNodeFactory();
		FILE_PATH = testScratchDirectoryName + getFilename();
	}

	@After
	public void tearDown() {
		nexusNodeFactory = null;
	}

	protected abstract String getFilename();

	protected abstract NXroot createNXroot();

	private TreeFile createNexusTree() {
		final TreeFileImpl treeFile = nexusNodeFactory.createTreeFile(FILE_PATH);
		final NXroot root = createNXroot();
		treeFile.setGroupNode(root);

		return treeFile;
	}

	@Test
	public void testNexusFile() throws Exception {
		TreeFile originalNexusTree = createNexusTree();
		NexusUtils.saveNexusFile(originalNexusTree);

		TreeFile loadedTree = NexusUtils.loadNexusFile(FILE_PATH, true);
		assertNexusTreesEqual(originalNexusTree, loadedTree);
	}

	private void assertNexusTreesEqual(final TreeFile tree1, final TreeFile tree2) throws Exception {
		assertGroupNodesEqual(tree1.getGroupNode(), tree2.getGroupNode());
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
		// check number of attributes same (i.e. group2 has no additional attributes)
		// additional attribute "target" is allowed. This is added when loading a file with >1 hard link to same node
		int expectedNumAttributes = group1.getNumberOfAttributes();
		if (group2.containsAttribute("target")) {
			expectedNumAttributes++;
		}
		assertEquals(expectedNumAttributes, group2.getNumberOfAttributes());

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

	private void assertAttributesEquals(final Attribute attr1, final Attribute attr2) {
		assertEquals(attr1.getName(), attr2.getName());
		assertEquals(attr1.getTypeName(), attr2.getTypeName());
		assertEquals(attr1.getFirstElement(), attr2.getFirstElement());
		assertEquals(attr1.getSize(), attr2.getSize());
		assertEquals(attr1.getRank(), attr2.getRank());
		assertArrayEquals(attr1.getShape(), attr2.getShape());
		assertDatasetsEqual(attr1.getValue(), attr2.getValue());
	}

	private void assertDataNodesEqual(final DataNode dataNode1, final DataNode dataNode2) {
		// check attributes same
		Iterator<String> attributeNameIterator = dataNode1.getAttributeNameIterator();
		while (attributeNameIterator.hasNext()) {
			String attributeName = attributeNameIterator.next();
			Attribute attr1 = dataNode1.getAttribute(attributeName);
			Attribute attr2 = dataNode2.getAttribute(attributeName);
			assertNotNull(attr2);
			assertAttributesEquals(attr1, attr2);
		}
		// check number of attributes same (i.e. dataset2 has no additional attributes)
		// additional attribute "target" is allowed. This is added when loading a file with >1 hard link to same node
		int expectedNumAttributes = dataNode1.getNumberOfAttributes();
		if (dataNode2.containsAttribute("target")) {
			expectedNumAttributes++;
		}
		assertEquals(expectedNumAttributes, dataNode2.getNumberOfAttributes());

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

	private void assertDatasetsEqual(final ILazyDataset dataset1, final ILazyDataset dataset2) {
		// Note: dataset names can be different, as long as the containing data node names are the same
		// assertEquals(dataset1.getName(), dataset2.getName());
		// assertEquals(dataset1.getClass(), dataset2.getClass());
		assertEquals(dataset1.elementClass(), dataset2.elementClass());
		assertEquals(dataset1.getElementsPerItem(), dataset2.getElementsPerItem());
		assertEquals(dataset1.getSize(), dataset2.getSize());
		if (dataset1.getRank() == 0) {
			// TODO: special case for scalar datasets. This could be fixed in future by marking the
			// dataset as scalar in the HDF5 file
			assertEquals(1, dataset2.getRank());
			assertArrayEquals(new int[] { 1 }, dataset2.getShape());
		} else {
			assertEquals(dataset1.getRank(), dataset2.getRank());
			assertArrayEquals(dataset1.getShape(), dataset2.getShape());
		}

		assertDatasetDataEqual(dataset1, dataset2);

		// TODO: in future also check metadata
	}

	private void assertDatasetDataEqual(final ILazyDataset dataset1, final ILazyDataset dataset2) {
		if (dataset1 instanceof Dataset && dataset2 instanceof Dataset) {
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
					assertEquals(dataset1Slice.getFloat(position), dataset2Slice.getFloat(position), 1e-7);
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
