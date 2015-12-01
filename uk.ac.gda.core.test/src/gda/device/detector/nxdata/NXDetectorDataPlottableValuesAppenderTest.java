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
import static org.junit.Assert.assertThat;
import gda.device.detector.NXDetectorData;

import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("unused") // to avoid warnings about un-thrown exceptions
public class NXDetectorDataPlottableValuesAppenderTest {

	private static final String DETECTOR_NAME = "NexusDetector";
	private static final String[] ONE_EXTRA_NAME = new String[] { "First extra name" };
	private static final String[] TWO_EXTRA_NAMES = new String[] { "First extra name", "Second extra name" };
	private static final String[] ONE_OUTPUT_FORMAT = new String[] { "%5.5g" };
	private static final String[] TWO_OUTPUT_FORMATS = new String[] { "%5.5g", "%.8f" };
	private static final Double[] ONE_PLOTTABLE_VALUE = new Double[] { Double.valueOf(7.45) };
	private static final Double[] TWO_PLOTTABLE_VALUES = new Double[] { Double.valueOf(7.45), Double.valueOf(-176.3) };

	private NXDetectorData dataToAppend;
	private NXDetectorData parentData;

	@Before
	public void setUp() throws Exception {
		parentData = new NXDetectorData(TWO_EXTRA_NAMES, TWO_OUTPUT_FORMATS, DETECTOR_NAME);
	}

	@Test
	public void onePlottableValueShouldBeCopiedCorrectly() throws Exception {
		NXDetectorDataPlottableValuesAppender appender = new NXDetectorDataPlottableValuesAppender(ONE_EXTRA_NAME, ONE_PLOTTABLE_VALUE);
		appender.appendTo(parentData, DETECTOR_NAME);
		assertThat(parentData.getDoubleVals()[0], is(equalTo(ONE_PLOTTABLE_VALUE[0])));
	}

	@Test
	public void twoPlottableValuesShouldBeCopiedCorrectly() throws Exception {
		NXDetectorDataPlottableValuesAppender appender = new NXDetectorDataPlottableValuesAppender(TWO_EXTRA_NAMES, TWO_PLOTTABLE_VALUES);
		appender.appendTo(parentData, DETECTOR_NAME);
		assertThat(parentData.getDoubleVals()[0], is(equalTo(TWO_PLOTTABLE_VALUES[0])));
		assertThat(parentData.getDoubleVals()[1], is(equalTo(TWO_PLOTTABLE_VALUES[1])));
	}
}
