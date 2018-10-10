/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.junit.Test;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;
import uk.ac.diamond.daq.mapping.region.LineMappingRegion;
import uk.ac.diamond.daq.mapping.region.SnappedLineMappingRegion;

public class SnappedLineMappingRegionTest {

	@Test
	public void testUpdatingFromRoi_SnapHorizontal() {
		double xStart = 10.23;
		double xStop = 34.25;
		double yStart = 11.4;
		double yStop = -12.3;

		// Create ROI
		LinearROI linearROI = new LinearROI(new double[] { xStart, yStart }, new double[] { xStop, yStop });

		// Create Region
		SnappedLineMappingRegion lineMappingRegion = new SnappedLineMappingRegion();

		// Update region using ROI
		lineMappingRegion.updateFromROI(linearROI);

		// Check values
		assertEquals("xStart", xStart, lineMappingRegion.getxStart(), xStart * 1e-8);
		assertEquals("xStop", xStop, lineMappingRegion.getxStop(), xStop * 1e-8);
		assertEquals("yStart", yStart, lineMappingRegion.getyStart(), yStart * 1e-8);
		assertEquals("yStop", yStart, lineMappingRegion.getyStop(), yStart * 1e-8); // yStart, as line is snapped to horizontal

		// Check ROI has been updated
		assertArrayEquals(new double[] { xStart, yStart }, linearROI.getPoint(), 1e-8);
		assertArrayEquals(new double[] { xStop, yStart }, linearROI.getEndPoint(), 1e-8);
	}

	@Test
	public void testUpdatingFromRoi_SnapVertical() {
		double xStart = 10.23;
		double xStop = 14.25;
		double yStart = -12.3;
		double yStop = 11.4;

		// Create ROI
		LinearROI linearROI = new LinearROI(new double[] { xStart, yStart }, new double[] { xStop, yStop });

		// Create Region
		SnappedLineMappingRegion lineMappingRegion = new SnappedLineMappingRegion();

		// Update region using ROI
		lineMappingRegion.updateFromROI(linearROI);

		// Check values
		assertEquals("xStart", xStart, lineMappingRegion.getxStart(), xStart * 1e-8);
		assertEquals("xStop", xStart, lineMappingRegion.getxStop(), xStart * 1e-8); // xStart, as line is snapped to vertical
		assertEquals("yStart", yStart, lineMappingRegion.getyStart(), yStart * 1e-8);
		assertEquals("yStop", yStop, lineMappingRegion.getyStop(), yStop * 1e-8);

		// Check ROI has been updated
		assertArrayEquals(new double[] { xStart, yStart }, linearROI.getPoint(), 1e-8);
		assertArrayEquals(new double[] { xStart, yStop }, linearROI.getEndPoint(), 1e-8);
	}

	@Test
	public void testUpdatingFromRoi_SnapHorizontal_RightToLeftLine() {
		double xStart = 34.25;
		double xStop = 10.23;
		double yStart = -12.3;
		double yStop = 11.4;

		// Create ROI
		LinearROI linearROI = new LinearROI(new double[] { xStart, yStart }, new double[] { xStop, yStop });

		// Create Region
		SnappedLineMappingRegion lineMappingRegion = new SnappedLineMappingRegion();

		// Update region using ROI
		lineMappingRegion.updateFromROI(linearROI);

		// Check values
		assertEquals("xStart", xStart, lineMappingRegion.getxStart(), xStart * 1e-8);
		assertEquals("xStop", xStop, lineMappingRegion.getxStop(), xStop * 1e-8);
		assertEquals("yStart", yStart, lineMappingRegion.getyStart(), yStart * 1e-8);
		assertEquals("yStop", yStart, lineMappingRegion.getyStop(), yStart * 1e-8); // yStart, as line is snapped to horizontal

		// Check ROI has been updated
		assertArrayEquals(new double[] { xStart, yStart }, linearROI.getPoint(), 1e-8);
		assertArrayEquals(new double[] { xStop, yStart }, linearROI.getEndPoint(), 1e-8);
	}

	@Test
	public void testUpdatingFromRoi_SnapVertical_BottomToTop() {
		double xStart = 10.23;
		double xStop = 14.25;
		double yStart = -12.3;
		double yStop = 11.4;

		// Create ROI
		LinearROI linearROI = new LinearROI(new double[] { xStart, yStart }, new double[] { xStop, yStop });

		// Create Region
		SnappedLineMappingRegion lineMappingRegion = new SnappedLineMappingRegion();

		// Update region using ROI
		lineMappingRegion.updateFromROI(linearROI);

		// Check values
		assertEquals("xStart", xStart, lineMappingRegion.getxStart(), xStart * 1e-8);
		assertEquals("xStop", xStart, lineMappingRegion.getxStop(), xStart * 1e-8); // xStart, as line is snapped to vertical
		assertEquals("yStart", yStart, lineMappingRegion.getyStart(), yStart * 1e-8);
		assertEquals("yStop", yStop, lineMappingRegion.getyStop(), yStop * 1e-8);

		// Check ROI has been updated
		assertArrayEquals(new double[] { xStart, yStart }, linearROI.getPoint(), 1e-8);
		assertArrayEquals(new double[] { xStart, yStop }, linearROI.getEndPoint(), 1e-8);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUpdatingFromInvalidROIType() {
		// Create ROI
		RectangularROI rectangularROI = new RectangularROI();

		// Create Region
		LineMappingRegion lineMappingRegion = new LineMappingRegion();

		// Update region using ROI should throw
		lineMappingRegion.updateFromROI(rectangularROI);
	}

	@Test
	public void testCopy() {
		final SnappedLineMappingRegion original = new SnappedLineMappingRegion();
		LinearROI roi = new LinearROI();
		original.updateFromROI(roi);

		final IMappingScanRegionShape copy = original.copy();

		assertThat(copy, is(equalTo(original)));
		assertThat(copy, is(not(sameInstance(original))));
	}

}
