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

package uk.ac.diamond.daq.mapping.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;
import uk.ac.diamond.daq.mapping.region.PolygonMappingRegion;
import uk.ac.diamond.daq.mapping.region.PolygonMappingRegion.MutablePoint;

public class PolygonMappingRegionTest {

	@Test
	public void testCopy() {
		final PolygonMappingRegion original = new PolygonMappingRegion();
		original.setPoints(Arrays.asList(
				new MutablePoint(0.0, 1.5),
				new MutablePoint(32, -4),
				new MutablePoint(11, 1.4)));

		final IMappingScanRegionShape copy = original.copy();

		assertThat(copy, is(equalTo(original)));
		assertThat(copy, is(not(sameInstance(original))));
	}

	@Test
	public void testCentre() {
		// using an unweighted centre i.e. centre of the bounding box
		PolygonMappingRegion region = new PolygonMappingRegion();
		region.setPoints(Arrays.asList(
							new MutablePoint(0, 0),
							new MutablePoint(1, 0),
							new MutablePoint(1, 1)));
		// centre of the bounding box = (0.5, 0.5)
		// lengths of bounding box = (1, 1)

		region.centre(-5, 5);

		List<MutablePoint> shiftedPoints = region.getPoints();
		assertThat(shiftedPoints.get(0).getX(), is(-5.5));
		assertThat(shiftedPoints.get(0).getY(), is(4.5));

		assertThat(shiftedPoints.get(1).getX(), is(-4.5));
		assertThat(shiftedPoints.get(1).getY(), is(4.5));

		assertThat(shiftedPoints.get(2).getX(), is(-4.5));
		assertThat(shiftedPoints.get(2).getY(), is(5.5));
	}

}
