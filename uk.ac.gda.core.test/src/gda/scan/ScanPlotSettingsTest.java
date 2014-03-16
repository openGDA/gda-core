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

package gda.scan;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

/**
 *
 */
public class ScanPlotSettingsTest {

	/**
	 * @throws Exception
	 */
	@Test
	public void testJustX() throws Exception{
		ScanPlotSettings expected = new ScanPlotSettings();
		expected.setXAxisName("x");
		assertEquals(expected, ScanPlotSettingsUtils.createSettings(
				Arrays.asList( new String [] {"x"}),
				null,
				0,
				null,
				null));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testCompletePositiveIndex() throws Exception{
		ScanPlotSettings expected = new ScanPlotSettings();
		expected.setXAxisName("x0");
		expected.setYAxesShown(new String [] {"y0"});
		expected.setYAxesNotShown(new String [] {"y1","y2"});
		assertEquals(expected, ScanPlotSettingsUtils.createSettings(
				Arrays.asList( new String [] {"x0","x1"}),
				Arrays.asList( new String [] {"y0","y1","y2"}),
				0,
				Arrays.asList( new Integer [] {0}),
				Arrays.asList( new Integer [] {1,2})
				));
	}
}
