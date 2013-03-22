/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.device.detector.nxdetector.plugin.areadetector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import gda.device.detector.areadetector.v18.NDStatsPVs;
import gda.device.detector.areadetector.v18.NDStatsPVs.BasicStat;
import gda.device.detector.areadetector.v18.NDStatsPVs.Stat;
import gda.device.detector.nxdetector.ADStatsROIPair;
import gda.device.detector.nxdetector.roi.ImutableRectangularROI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ADStatsROIPairTest {

	@Mock
	private ADRectangularROIPlugin roiPlugin;
	
	@Mock
	private ADTimeSeriesStatsPlugin statsPlugin;
	
	private ADStatsROIPair pair;

	@Before
	public void setUp() {
		pair = new ADStatsROIPair("roi1", roiPlugin, statsPlugin);
	}

	@Test
	public void testPrepareForCollectionAndWillRequireCallbacksOff() throws Exception {
		when(roiPlugin.getRoi()).thenReturn(null);
		when(statsPlugin.getEnabledStats()).thenReturn(new ArrayList<Stat>());
		assertFalse(pair.willRequireCallbacks());
		pair.prepareForCollection(1, null);
	}
	
	@Test(expected=IllegalStateException.class)
	public void testPrepareForCollectionAndWillRequireCallbacksOn() throws Exception {
		when(roiPlugin.getRoi()).thenReturn(new ImutableRectangularROI(0, 0, 0, 0, "name"));
		List<Stat> stats = new ArrayList<NDStatsPVs.Stat>();
		stats.add(BasicStat.MaxValue);
		when(statsPlugin.getEnabledStats()).thenReturn(stats);
		assertTrue(pair.willRequireCallbacks());
		pair.prepareForCollection(1, null);
	}

	@Test(expected=IllegalStateException.class)
	public void testPrepareForCollectionAndWillRequireCallbacksInconsitant1() throws Exception {
		when(roiPlugin.getRoi()).thenReturn(null);
		List<Stat> stats = new ArrayList<NDStatsPVs.Stat>();
		stats.add(BasicStat.MaxValue);
		when(statsPlugin.getEnabledStats()).thenReturn(stats);
		assertFalse(pair.willRequireCallbacks());
		pair.prepareForCollection(1, null);
	}
	
	@Test
	public void testPrepareForCollectionAndWillRequireCallbacksInconsitant2() throws Exception {
		when(roiPlugin.getRoi()).thenReturn(new ImutableRectangularROI(0, 0, 0, 0, "name"));
		List<Stat> stats = new ArrayList<NDStatsPVs.Stat>();
		when(statsPlugin.getEnabledStats()).thenReturn(stats);
		assertFalse(pair.willRequireCallbacks());
		pair.prepareForCollection(1, null);
	}

	@Test
	public void testGetInputStreamNamesOff() {
		when(roiPlugin.getRoi()).thenReturn(new ImutableRectangularROI(0, 0, 0, 0, "middle"));
		when(statsPlugin.getInputStreamNames()).thenReturn(Arrays.asList("maxvalue", "total"));
		assertEquals(Arrays.asList("middle_maxvalue", "middle_total"), pair.getInputStreamNames());
	}

}
