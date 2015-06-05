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

package gda.device.detector.nxdata;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import gda.data.nexus.tree.INexusTree;
import gda.device.detector.NXDetectorData;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("unused") // to avoid warnings about un-thrown exceptions
public class NXDetectorDataNexusTreeProviderAppenderTest {

	private static final String FIRST_DETECTOR_NAME = "NexusDetector";
	private static final String SECOND_DETECTOR_NAME = "ChildDetector";
	private static final String[] ONE_EXTRA_NAME = new String[] { "First extra name" };
	private static final String[] TWO_EXTRA_NAMES = new String[] { "First extra name", "Second extra name" };
	private static final String[] ONE_OUTPUT_FORMAT = new String[] { "%5.5g" };
	private static final String[] TWO_OUTPUT_FORMATS = new String[] { "%5.5g", "%.8f" };
	private static final Double PLOTTABLE_VALUE = Double.valueOf(7.45);

	private NXDetectorDataNexusTreeProviderAppender appender;
	private NXDetectorData dataToAppend;
	private NXDetectorData parentData;

	@Before
	public void setUp() throws Exception {
		parentData = new NXDetectorData(TWO_EXTRA_NAMES, TWO_OUTPUT_FORMATS, FIRST_DETECTOR_NAME);
		dataToAppend = new NXDetectorData(ONE_EXTRA_NAME, ONE_OUTPUT_FORMAT, SECOND_DETECTOR_NAME);
		dataToAppend.addData(SECOND_DETECTOR_NAME, "Data value", 1.426, null);
		// Deliberately not setting plottable values at this point

		appender = new NXDetectorDataNexusTreeProviderAppender(dataToAppend);
	}

	@After
	public void tearDown() throws Exception {
		appender = null;
	}

	@Test
	public void nexusTreeShouldBeAppendedCorrectly() throws Exception {
		appender.appendTo(parentData, FIRST_DETECTOR_NAME);
		INexusTree childNode = parentData.getDetTree(FIRST_DETECTOR_NAME).getChildNode(0);
		assertThat(childNode, is(equalTo(dataToAppend.getNexusTree())));
	}

	@Test
	public void withNoPlottableValuesInChildParentShouldHaveNoPlottableValues() throws Exception {
		appender.appendTo(parentData, FIRST_DETECTOR_NAME);
		for (Double value : parentData.getDoubleVals()) {
			assertNull(value);
		}
	}

	@Test
	public void onePlottableValueShouldBeCopiedCorrectly() throws Exception {
		dataToAppend.setPlottableValue("First extra name", PLOTTABLE_VALUE);
		appender.appendTo(parentData, FIRST_DETECTOR_NAME);
		assertThat(parentData.getDoubleVals()[0], is(equalTo(PLOTTABLE_VALUE)));
	}
}
