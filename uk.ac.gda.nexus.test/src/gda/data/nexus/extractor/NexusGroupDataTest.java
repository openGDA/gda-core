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

package gda.data.nexus.extractor;


import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.ShapeUtils;
import org.eclipse.january.dataset.StringDataset;
import org.junit.Assert;
import org.junit.Test;

public class NexusGroupDataTest {

	@Test
	public void testShapes() {
		Assert.assertArrayEquals(new int[0], ShapeUtils.getShapeFromObject(null));

		Assert.assertArrayEquals(new int[] {0}, ShapeUtils.getShapeFromObject(new int[]{}));
		Assert.assertArrayEquals(new int[] {0}, ShapeUtils.getShapeFromObject(new double[]{}));

		Assert.assertArrayEquals(new int[] {2}, ShapeUtils.getShapeFromObject(new int[]{2, 3}));

		Assert.assertArrayEquals(new int[] {2, 3}, ShapeUtils.getShapeFromObject(new int[][]{{2,}, {3, 4, 5}}));

		int[][][] obj = new int[][][] { { {2}, {3, 4}, {4} }, {{1, 2, 3}, {5, 6, 7, 8}} };
		Assert.assertArrayEquals(new int[] {2, 3, 4}, ShapeUtils.getShapeFromObject(obj));
	}

	@Test
	public void testStrings() {

		StringDataset strings = DatasetFactory.createFromObject(StringDataset.class, new String[] { "Hello", "world", "!", "How", "are", "you?" }, 2, 3);
		NexusGroupData ngd = NexusGroupData.createFromDataset(strings);

		ngd.setMaxStringLength(40);
		byte[] bdata = (byte[]) ngd.getBuffer(true);

		NexusGroupData bngd = new NexusGroupData(bdata);
		ngd = bngd.asChar();
		ngd.setMaxStringLength(40);
		Dataset s = ngd.toDataset();
		s.setShape(2, 3);
		Assert.assertEquals(strings, s);

		bngd = new NexusGroupData(new int[] {2, 3, 40}, bdata);
		ngd = bngd.asChar();
		ngd.setMaxStringLength(40);
		s = ngd.toDataset();
		Assert.assertEquals(strings, s);
	}
}
