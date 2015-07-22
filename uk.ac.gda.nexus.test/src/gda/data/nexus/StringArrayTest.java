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
import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyWriteableDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.hdf5.nexus.NexusFile;
import org.junit.Test;

/**
 * Small test class for writing String Arrays.
 */
public class StringArrayTest {

	@Test
	public void testSimpleStringArray() throws Exception{
		try (NexusFile nf = NexusUtils.createNexusFile("/tmp/stringarray.nxs")) {
			GroupNode g = nf.getGroup("/entry1:NXentry", true);
			NexusUtils.write(nf, g, "stringarray", new String[] {"String", "String Å"});
		}

		try (NexusFile nf = NexusUtils.openNexusFile("/tmp/stringarray.nxs")) {
			DataNode d = nf.getData("/entry1/stringarray");
			IDataset ds = d.getDataset().getSlice();
			int[] shape = ds.getShape();
			assertArrayEquals(new int[] {2}, shape);
		}
	}

	@Test
	public void testSimpleString() throws Exception {
		try (NexusFile nf = NexusUtils.createNexusFile("/tmp/stringfile.nxs")) {
			GroupNode g = nf.getGroup("/note:NXnote", true);
			NexusUtils.write(nf, g, "somestring", "MyString");
		}

		try (NexusFile nf = NexusUtils.openNexusFileReadOnly("/tmp/stringfile.nxs")) {
			DataNode d = nf.getData("/note/somestring");
			IDataset ds = d.getDataset().getSlice();
			int[] shape = ds.getShape();
			assertArrayEquals(new int[] {1}, shape);
			assertEquals("MyString", ds.getString(0));
		}
	}

	@Test
	public void test() throws Exception {
		int nPoints = 10;

		try (NexusFile nf = NexusUtils.createNexusFile("/tmp/file.nxs")) {
			GroupNode g = nf.getGroup("/test:NXnote", true);
			ILazyWriteableDataset lazy = NexusUtils.createLazyWriteableDataset("stringarray", Dataset.STRING, new int[] {ILazyWriteableDataset.UNLIMITED}, null, null);
			nf.createData(g, lazy);

			for (int i = 0; i < nPoints; i++) {
				lazy.setSlice(null, DatasetFactory.createFromObject("file" + i), SliceND.createSlice(lazy, new int[] {i}, new int[] {i+1}));
			}
		}

		try (NexusFile f = NexusUtils.openNexusFileReadOnly("/tmp/file.nxs")) {
			DataNode d = f.getData("/test/stringarray");
			IDataset ds = d.getDataset().getSlice();
			System.err.println(ds);
			int[] shape = ds.getShape();
			System.err.println(Arrays.toString(shape));
			for (int i = 0; i < nPoints; i++) {
				System.err.println(i + "/" + nPoints);
				System.out.println(ds.getString(i));
			}
		}
	}
}
