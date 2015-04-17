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

package gda.data.nexus.napi;

import gda.data.nexus.NexusUtils;

import java.net.URI;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyWriteableDataset;
import org.eclipse.dawnsci.analysis.api.dataset.Slice;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.LazyWriteableDataset;
import org.eclipse.dawnsci.hdf5.nexus.NexusFile;
import org.junit.Assert;
import org.junit.Test;

public class NexusFileTest {

	@Test
	public void testNexusFile() throws Exception {
		String name = "test-scratch/test.nxs";
//		File f = new File(name);
//		if (f.exists())
//			f.delete();

		NexusFile nf = NexusUtils.createNXFile(name);

		nf.createAndOpenToWrite();

		GroupNode g = nf.getGroup("/e/a/b", true);
		int[] shape = new int[] {2, 34};
		int[] mshape = new int[] {ILazyWriteableDataset.UNLIMITED, 34};
		LazyWriteableDataset d = new LazyWriteableDataset("d", Dataset.INT16, shape, mshape, null, null);
		nf.createData(g, d);

		Dataset a = DatasetFactory.createFromObject("world");
		a.setName("hello");

		nf.addAttribute(g, nf.createAttribute("b", a));

		a = DatasetFactory.createFromObject(-1.5);
		a.setName("value");
		nf.addAttribute(g.getDataNode(d.getName()), nf.createAttribute(d.getName(), a));

		nf.close();

		SliceND slice = new SliceND(shape, new Slice(2), new Slice(10, 11));
		d.setSlice(DatasetFactory.zeros(slice.getShape(), Dataset.INT16).fill(-5), slice);

		nf.openToRead();
		g = nf.getGroup("/e/a/b", false);
		checkGroup(g);

		DataNode n = nf.getData("/e/a/b/d");
		checkData(n, shape);
		nf.close();

		nf.openToWrite(false);
		nf.link("/e/a/b", "/f/c");

		nf.linkExternal(new URI("nxfile://./"+name+"#/e/a/b/d"), "/g", false);
		nf.close();

		nf.openToRead();
		g = nf.getGroup("/f/c", false);
		checkGroup(g);

		n = g.getDataNode("d");
		checkData(n, shape);

		n = nf.getData("/g");
		Assert.assertNull(n);
		nf.close();
	}

	private void checkGroup(GroupNode g) {
		Assert.assertTrue(g.containsAttribute("hello"));
		Assert.assertEquals("world", g.getAttribute("hello").getValue().getString());
		Assert.assertTrue(g.isPopulated() && g.containsDataNode("d"));
	}

	private void checkData(DataNode n, int[] shape) {
		Assert.assertTrue(n.containsAttribute("value"));
		Assert.assertEquals(-1.5, n.getAttribute("value").getValue().getDouble(), 1e-15);
		ILazyDataset b = n.getDataset();
		Assert.assertTrue(b.elementClass().equals(Short.class));
		Assert.assertArrayEquals(shape, b.getShape());
		IDataset bs = b.getSlice();
		Assert.assertEquals(0, bs.getLong(0, 0));
		Assert.assertEquals(-5, bs.getLong(0, 10));
	}
}
