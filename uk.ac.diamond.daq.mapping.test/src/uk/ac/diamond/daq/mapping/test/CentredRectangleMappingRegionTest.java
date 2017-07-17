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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.junit.Test;

import uk.ac.diamond.daq.mapping.region.CentredRectangleMappingRegion;

public class CentredRectangleMappingRegionTest {

	@Test
	public void testGettingBoundingRectangleNoSwapsRequired() {
		double xStart = 13.335;
		double xStop = 41.27;
		double yStart = -35.2;
		double yStop = 0.0;

		CentredRectangleMappingRegion rectangularMappingRegion = createCentredRectangleRegion(xStart, xStop, yStart, yStop);

		double epsilon = Math.abs(xStart + xStop + yStart + yStop) * 0.25 * 1e-8;
		assertArrayEquals(new double[] { xStart, yStart }, rectangularMappingRegion.toROI().getBounds().getPoint(), epsilon);
		assertArrayEquals(new double[] { xStop, yStop }, rectangularMappingRegion.toROI().getBounds().getEndPoint(), epsilon);
	}

	@Test
	public void testGettingBoundingRectangleXSwapRequired() {
		double xStart = 13.335;
		double xStop = -41.27;
		double yStart = -35.2;
		double yStop = 0.0;

		CentredRectangleMappingRegion rectangularMappingRegion = createCentredRectangleRegion(xStart, xStop, yStart, yStop);

		double epsilon = Math.abs(xStart + xStop + yStart + yStop) * 0.25 * 1e-8;
		assertArrayEquals(new double[] { xStop, yStart }, rectangularMappingRegion.toROI().getBounds().getPoint(), epsilon);
		assertArrayEquals(new double[] { xStart, yStop }, rectangularMappingRegion.toROI().getBounds().getEndPoint(), epsilon);
	}

	@Test
	public void testGettingBoundingRectangleYSwapRequired() {
		double xStart = 13.335;
		double xStop = 41.27;
		double yStart = -45.2;
		double yStop = -58.5;

		CentredRectangleMappingRegion rectangularMappingRegion = createCentredRectangleRegion(xStart, xStop, yStart, yStop);

		double epsilon = Math.abs(xStart + xStop + yStart + yStop) * 0.25 * 1e-8;
		assertArrayEquals(new double[] { xStart, yStop }, rectangularMappingRegion.toROI().getBounds().getPoint(), epsilon);
		assertArrayEquals(new double[] { xStop, yStart }, rectangularMappingRegion.toROI().getBounds().getEndPoint(), epsilon);
	}

	@Test
	public void testGettingBoundingRectangleXandYSwapRequired() {
		double xStart = 13.335;
		double xStop = -41.27;
		double yStart = -45.2;
		double yStop = -58.5;

		CentredRectangleMappingRegion rectangularMappingRegion = createCentredRectangleRegion(xStart, xStop, yStart, yStop);

		double epsilon = Math.abs(xStart + xStop + yStart + yStop) * 0.25 * 1e-8;
		assertArrayEquals(new double[] { xStop, yStop }, rectangularMappingRegion.toROI().getBounds().getPoint(), epsilon);
		assertArrayEquals(new double[] { xStart, yStart }, rectangularMappingRegion.toROI().getBounds().getEndPoint(), epsilon);
	}

	private CentredRectangleMappingRegion createCentredRectangleRegion(double xStart, double xStop, double yStart, double yStop) {
		double xRange = Math.abs(xStop - xStart);
		double xCentre = Math.min(xStart, xStop) + xRange / 2;
		double yRange = Math.abs(yStop - yStart);
		double yCentre = Math.min(yStart, yStop) + yRange / 2;

		CentredRectangleMappingRegion rect = new CentredRectangleMappingRegion();
		rect.setxCentre(xCentre);
		rect.setxRange(xRange);
		rect.setyCentre(yCentre);
		rect.setyRange(yRange);

		return rect;
	}

	@Test
	public void testUpdatingFromROI() {
		double xStart = 10.23;
		double xStop = 34.25;
		double yStart = -12.3;
		double yStop = 11.4;

		// Create ROI
		RectangularROI rectangularROI = new RectangularROI(xStart, yStart, (xStop - xStart), (yStop - yStart), 0);

		// Create Region
		CentredRectangleMappingRegion rectangularMappingRegion = new CentredRectangleMappingRegion();

		// Update region from ROI
		rectangularMappingRegion.updateFromROI(rectangularROI);

		double xRange = Math.abs(xStop - xStart);
		double xCentre = Math.min(xStart, xStop) + xRange / 2;
		double yRange = Math.abs(yStop - yStart);
		double yCentre = Math.min(yStart, yStop) + yRange / 2;

		// Check values
		assertEquals("xCentre", xCentre, rectangularMappingRegion.getxCentre(), xCentre * 1e-8);
		assertEquals("xRange", xRange, rectangularMappingRegion.getxRange(), xRange * 1e-8);
		assertEquals("yCentre", yCentre, rectangularMappingRegion.getyCentre(), yCentre * 1e-8);
		assertEquals("yRange", yRange, rectangularMappingRegion.getyRange(), yRange * 1e-8);
	}

}
