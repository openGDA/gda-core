/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package gda.device.detector;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IntegerDataset;
import org.junit.Test;

import gda.data.nexus.extractor.NexusGroupData;

public class NXDetectorDataTest {

	@Test
	public void testMerge() {
		NXDetectorData detectorData1 = new NXDetectorData(new String[] {"a", "b"},
				new String[0],
				"");

		NXDetectorData detectorData2 = new NXDetectorData(new String[] {"c"},
				new String[] {"%4.3g"},
				"");

		NexusGroupData groupData1 = NexusGroupData.createFromDataset(DatasetFactory.createRange(IntegerDataset.class, 5));
		NexusGroupData groupData2 = NexusGroupData.createFromDataset(DatasetFactory.createRange(IntegerDataset.class, 10));

		detectorData1.addData("detector1", groupData1);
		detectorData2.addData("detector2", groupData2);

		detectorData1.setPlottableValue("a", Double.valueOf(0));
		detectorData1.setPlottableValue("b", Double.valueOf(1));

		detectorData2.setPlottableValue("c", Double.valueOf(5));

		GDANexusDetectorData mergedDetectorData = detectorData1.mergeIn(detectorData2);

		// test nexus tree
		assertEquals(2, mergedDetectorData.getNexusTree().getNumberOfChildNodes());
		assertEquals(groupData1, mergedDetectorData.getNexusTree().getChildNode(0).getChildNode(0).getData());
		assertEquals(groupData2, mergedDetectorData.getNexusTree().getChildNode(1).getChildNode(0).getData());

		// test double vals
		assertArrayEquals(
				new Double[] {Double.valueOf(0), Double.valueOf(1), Double.valueOf(5)},
				mergedDetectorData.getDoubleVals());

		// test output format
		assertArrayEquals(
				new String[] {"%5.5g", "%4.3g"},
				mergedDetectorData.getOutputFormat());

		// test extra names
		assertArrayEquals(
				new String[] {"a", "b", "c"},
				mergedDetectorData.getExtraNames());
	}

	@Test
	public void defaultOutputFormatWhenNoneSpecified() {
		GDANexusDetectorData data = new NXDetectorData(new String[] {"a"}, null, "detector1");
		assertArrayEquals(new String[] {"%5.5g"}, data.getOutputFormat());
	}

}
