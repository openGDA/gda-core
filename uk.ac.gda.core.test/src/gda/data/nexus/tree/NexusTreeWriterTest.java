/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

import org.eclipse.dawnsci.hdf5.nexus.NexusFileHDF5;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.junit.Assert;
import org.junit.Test;

import gda.TestHelpers;
import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;

/**
 *
 */
public class NexusTreeWriterTest {
	/**
	 * @throws Exception
	 */
	@Test
	public void testWriteSimpleNode() throws Exception {
		String testScratchDirectoryName = TestHelpers.setUpTest(NexusTreeWriterTest.class, "testWriteSimpleNode", true);
		NexusTreeNode tree_in = new NexusTreeNode(NexusExtractor.topName,NexusExtractor.topClass,null);
		tree_in.addChildNode( new NexusTreeNode("entry1",NexusExtractor.NXEntryClassName,tree_in));
		NexusTreeNode entry2 = new NexusTreeNode("entry2",NexusExtractor.NXEntryClassName,tree_in);
		tree_in.addChildNode( entry2);
		entry2.addChildNode( new NexusTreeNode("units",NexusExtractor.AttrClassName,entry2, new NexusGroupData("\u212B")));
		String filename = testScratchDirectoryName+"/out.nxs";
		NexusFile file = NexusFileHDF5.createNexusFile(filename);
		file.setDebug(true);
		NexusTreeWriter.writeHere(file, file.getGroup("/", true), tree_in);
		file.close();
		INexusTree tree_out = NexusTreeBuilder.getNexusTree(filename, NexusTreeNodeSelection.createTreeForAllData());

		( new NexusTreeSplicer()).MergeTwo(tree_in, tree_out);
		Assert.assertEquals(tree_out.getName(), tree_in.getName());
	}
}
