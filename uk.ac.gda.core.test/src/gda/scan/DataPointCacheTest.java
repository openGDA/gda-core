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

package gda.scan;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

public class DataPointCacheTest {
	private DataPointCache cache;

	private IScanDataPoint firstPoint;
	private IScanDataPoint nextPoint;

	@Before
	public void setup() {
		firstPoint = mock(IScanDataPoint.class);
		nextPoint = mock(IScanDataPoint.class);
		when(firstPoint.getCurrentPointNumber()).thenReturn(0);
		when(nextPoint.getCurrentPointNumber()).thenReturn(1);
		cache = mock(DataPointCache.class, CALLS_REAL_METHODS);
	}

	@Test
	public void testInitialiseCalledForFirstPoint() throws Exception {
		cache.update(null, firstPoint);
		verify(cache).initialise(firstPoint);
	}

	@Test
	public void testInitialiseOnlyCalledForFirstPoint() throws Exception {
		cache.update(null, nextPoint);
		verify(cache, never()).initialise(any());
	}

	@Test
	public void testAllPointsAreAdded() throws Exception {
		cache.update(null, firstPoint);
		cache.update(null, nextPoint);
		verify(cache).addDataPoint(firstPoint);
		verify(cache).addDataPoint(nextPoint);
	}

	@Test
	public void testNullUpdatesAreIgnored() throws Exception {
		cache.update(null, null);
		verify(cache, never()).initialise(any());
		verify(cache, never()).addDataPoint(any());
	}

	@Test
	public void testNonSDPUpdatesAreIgnored() throws Exception {
		cache.update(null, "This is not an ISDP");
		verify(cache, never()).initialise(any());
		verify(cache, never()).addDataPoint(any());
	}
}
