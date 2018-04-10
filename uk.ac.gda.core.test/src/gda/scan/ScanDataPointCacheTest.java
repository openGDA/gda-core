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

import static gda.scan.ScanDataPointProvider.getPoint;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class ScanDataPointCacheTest {

	private ScanDataPointCache cache;

	@Before
	public void setup() {
		cache = new ScanDataPointCache();
	}

	@Test
	public void testNormalOperation() throws Exception {
		cache.update(null, getPoint(0, 3, asList(0.1), asList(3.1)));
		cache.update(null, getPoint(1, 3, asList(0.2), asList(3.2)));
		cache.update(null, getPoint(2, 3, asList(0.3), asList(3.3)));
		assertEquals(asList(0.1, 0.2, 0.3), cache.getPositionsFor("scan0"));
		assertEquals(asList(3.1, 3.2, 3.3), cache.getPositionsFor("det0"));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testChangingNumberOfScannablesThrows() throws Exception {
		cache.update(null, getPoint(0, 3, asList(0.1), asList(3.1)));
		cache.update(null, getPoint(1, 3, asList(0.2), asList()));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNullScannableNamesAreHandledCorrectly() throws Exception {
		cache.update(null, getPoint(0, 3, asList(0.1), asList(3.1)));
		cache.getPositionsFor(null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testMissingScannableNameThrows() throws Exception {
		cache.update(null, getPoint(0, 3, asList(0.1), asList(3.1)));
		cache.getPositionsFor("missing scannable");
	}
}

