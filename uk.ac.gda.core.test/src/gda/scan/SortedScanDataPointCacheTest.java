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
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SortedScanDataPointCacheTest {

	private SortedScanDataPointCache cache;

	@BeforeEach
	public void setup() {
		cache = new SortedScanDataPointCache();
	}

	@Test
	void testSortedPointsReturnSortedValues() {
		cache.update(null, getPoint(0, 3, List.of(2.0), List.of(6.3)));
		cache.update(null, getPoint(1, 3, List.of(3.0), List.of(6.7)));
		cache.update(null, getPoint(2, 3, List.of(4.0), List.of(6.45)));

		assertThat(cache.getPositionsFor("scan0"), contains(2.0, 3.0, 4.0));
		assertThat(cache.getPositionsFor("det0"), contains(6.3, 6.7, 6.45));
	}

	@Test
	void testUnsortedPointsReturnSortedValues() {
		cache.update(null, getPoint(0, 3, List.of(3.0), List.of(6.3)));
		cache.update(null, getPoint(1, 3, List.of(2.0), List.of(6.7)));
		cache.update(null, getPoint(2, 3, List.of(4.0), List.of(6.45)));

		assertThat(cache.getPositionsFor("scan0"), contains(2.0, 3.0, 4.0));
		assertThat(cache.getPositionsFor("det0"), contains(6.7, 6.3, 6.45));
	}

	@Test
	void testGridOfPointsReturnSortedValues() {
		cache.update(null, getPoint(0, 4, List.of(3.0, 1.0), List.of(6.3)));
		cache.update(null, getPoint(1, 4, List.of(3.0, 1.5), List.of(6.7)));
		cache.update(null, getPoint(2, 4, List.of(4.0, 1.0), List.of(6.45)));
		cache.update(null, getPoint(3, 4, List.of(4.0, 1.5), List.of(6.13)));

		assertThat(cache.getPositionsFor("scan0"), contains(3.0, 3.0, 4.0, 4.0));
		assertThat(cache.getPositionsFor("scan1"), contains(1.0, 1.5, 1.0, 1.5));
		assertThat(cache.getPositionsFor("det0"), contains(6.3, 6.7, 6.45, 6.13));
	}

	@Test
	void testUnsortedGridOfPointsReturnSortedValues() {
		cache.update(null, getPoint(0, 4, List.of(3.0, 1.5), List.of(6.7)));
		cache.update(null, getPoint(1, 4, List.of(4.0, 1.5), List.of(6.13)));
		cache.update(null, getPoint(2, 4, List.of(3.0, 1.0), List.of(6.3)));
		cache.update(null, getPoint(3, 4, List.of(4.0, 1.0), List.of(6.45)));

		assertThat(cache.getPositionsFor("scan0"), contains(3.0, 3.0, 4.0, 4.0));
		assertThat(cache.getPositionsFor("scan1"), contains(1.0, 1.5, 1.0, 1.5));
		assertThat(cache.getPositionsFor("det0"), contains(6.3, 6.7, 6.45, 6.13));
	}

	@Test
	void testEmptyPointsReturnEmptyValues() {
		cache.update(null, getPoint(0, 3, emptyList(), emptyList()));
		cache.update(null, getPoint(1, 3, emptyList(), emptyList()));
		cache.update(null, getPoint(2, 3, emptyList(), emptyList()));

		assertThrows(IllegalArgumentException.class, () -> cache.getPositionsFor("scan0"));
	}

	@Test
	void testGetInvalidScannable() {
		cache.update(null, getPoint(0, 3, List.of(3.0), List.of(6.3)));
		cache.update(null, getPoint(1, 3, List.of(2.0), List.of(6.7)));
		cache.update(null, getPoint(2, 3, List.of(4.0), List.of(6.45)));

		assertThrows(IllegalArgumentException.class, () -> cache.getPositionsFor("non-existant detector"));
	}

	@Test
	void testZeroPoints() {
		assertThrows(IllegalArgumentException.class, () -> cache.getPositionsFor("scan0"));
	}

	@Test
	void testOnlyCurrentScanPointsUsed() {
		cache.update(null, getPoint(0, 2, List.of(1.0), List.of(5.7)));
		cache.update(null, getPoint(1, 2, List.of(2.0), List.of(5.45)));
		cache.update(null, getPoint(0, 2, List.of(3.0), List.of(6.7))); // scan point 0 should reset scan
		cache.update(null, getPoint(1, 2, List.of(4.0), List.of(6.45)));

		assertThat(cache.getPositionsFor("scan0"), contains(3.0, 4.0));
		assertThat(cache.getPositionsFor("det0"), contains(6.7, 6.45));
	}

}
