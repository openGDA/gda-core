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
import static gda.scan.ScanDataPointProvider.getPointWithDuplicatedHeader;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScanDataPointCacheTest {

	private ScanDataPointCache cache;

	@BeforeEach
	public void setup() {
		cache = new ScanDataPointCache();
	}

	@Test
	void testNormalOperation() throws Exception {
		cache.update(null, getPoint(0, 3, List.of(0.1), List.of(3.1)));
		cache.update(null, getPoint(1, 3, List.of(0.2), List.of(3.2)));
		cache.update(null, getPoint(2, 3, List.of(0.3), List.of(3.3)));
		assertThat(cache.getPositionsFor("scan0"), contains(0.1, 0.2, 0.3));
		assertThat(cache.getPositionsFor("det0"), contains(3.1, 3.2, 3.3));
	}

	@Test
	void testChangingNumberOfScannablesThrows() throws Exception {
		cache.update(null, getPoint(0, 3, List.of(0.1), List.of(3.1)));
		assertThrows(IllegalArgumentException.class,
				() -> cache.update(null, getPoint(1, 3, List.of(0.2), emptyList())));
	}

	@Test
	void testMoreScannablesThanCachedThrows() throws Exception {
		cache.update(null, getPoint(0, 3, List.of(0.1), List.of(3.1)));
		assertThrows(IllegalArgumentException.class,
				() -> cache.update(null, getPoint(1, 3, List.of(0.2, 0.5), List.of(3.2))));
	}

	@Test
	void testPointWithDuplicatedHeader() throws Exception {
		cache.update(null, getPointWithDuplicatedHeader(0, 3, List.of(0.1), List.of(3.1)));
		cache.update(null, getPointWithDuplicatedHeader(1, 3, List.of(0.2, 0.5), List.of(3.2)));
		assertThat(cache.getPositionsFor("scan"), contains(0.1, 0.2));
	}

	@Test
	void testNullScannableNamesAreHandledCorrectly() throws Exception {
		cache.update(null, getPoint(0, 3, List.of(0.1), List.of(3.1)));
		assertThrows(IllegalArgumentException.class, () -> cache.getPositionsFor(null));
	}

	@Test
	void testMissingScannableNameThrows() throws Exception {
		cache.update(null, getPoint(0, 3, List.of(0.1), List.of(3.1)));
		assertThrows(IllegalArgumentException.class, () -> cache.getPositionsFor("missing scannable"));
	}

}

