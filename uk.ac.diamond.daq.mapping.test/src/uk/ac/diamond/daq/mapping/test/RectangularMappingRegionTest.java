package uk.ac.diamond.daq.mapping.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.junit.Test;

import uk.ac.diamond.daq.mapping.region.RectangularMappingRegion;

public class RectangularMappingRegionTest {

	@Test
	public void testGettingBoundingRectangeNoSwapsRequired() {
		double xStart = 13.335;
		double xStop = 41.27;
		double yStart = -35.2;
		double yStop = 0.0;

		RectangularMappingRegion rectangularMappingRegion = new RectangularMappingRegion();
		rectangularMappingRegion.setxStart(xStart);
		rectangularMappingRegion.setxStop(xStop);
		rectangularMappingRegion.setyStart(yStart);
		rectangularMappingRegion.setyStop(yStop);

		// getPoint() returns bottom left ie min x and min y, getEndPoint() returns top right ie max x max y
		// epsilon is relative to the values * 1e-8 accept a small floating point error
		double epsilon = Math.abs(xStart + xStop + yStart + yStop) * 0.25 * 1e-8;
		assertArrayEquals(new double[] { xStart, yStart }, rectangularMappingRegion.toROI().getBounds().getPoint(), epsilon);
		assertArrayEquals(new double[] { xStop, yStop }, rectangularMappingRegion.toROI().getBounds().getEndPoint(), epsilon);
	}

	@Test
	public void testGettingBoundingRectangeXSwapRequired() {
		double xStart = 13.335;
		double xStop = -41.27;
		double yStart = -35.2;
		double yStop = 0.0;

		RectangularMappingRegion rectangularMappingRegion = new RectangularMappingRegion();
		rectangularMappingRegion.setxStart(xStart);
		rectangularMappingRegion.setxStop(xStop);
		rectangularMappingRegion.setyStart(yStart);
		rectangularMappingRegion.setyStop(yStop);

		// getPoint() returns bottom left ie min x and min y, getEndPoint() returns top right ie max x max y
		// epsilon is relative to the values * 1e-8 accept a small floating point error
		double epsilon = Math.abs(xStart + xStop + yStart + yStop) * 0.25 * 1e-8;
		assertArrayEquals(new double[] { xStop, yStart }, rectangularMappingRegion.toROI().getBounds().getPoint(), epsilon);
		assertArrayEquals(new double[] { xStart, yStop }, rectangularMappingRegion.toROI().getBounds().getEndPoint(), epsilon);

	}

	@Test
	public void testGettingBoundingRectangeYSwapRequired() {
		double xStart = 13.335;
		double xStop = 41.27;
		double yStart = -45.2;
		double yStop = -58.5;

		RectangularMappingRegion rectangularMappingRegion = new RectangularMappingRegion();
		rectangularMappingRegion.setxStart(xStart);
		rectangularMappingRegion.setxStop(xStop);
		rectangularMappingRegion.setyStart(yStart);
		rectangularMappingRegion.setyStop(yStop);

		// getPoint() returns bottom left ie min x and min y, getEndPoint() returns top right ie max x max y
		// epsilon is relative to the values * 1e-8 accept a small floating point error
		double epsilon = Math.abs(xStart + xStop + yStart + yStop) * 0.25 * 1e-8;
		assertArrayEquals(new double[] { xStart, yStop }, rectangularMappingRegion.toROI().getBounds().getPoint(), epsilon);
		assertArrayEquals(new double[] { xStop, yStart }, rectangularMappingRegion.toROI().getBounds().getEndPoint(), epsilon);

	}

	@Test
	public void testGettingBoundingRectangeXandYSwapRequired() {
		double xStart = 13.335;
		double xStop = -41.27;
		double yStart = -45.2;
		double yStop = -58.5;

		RectangularMappingRegion rectangularMappingRegion = new RectangularMappingRegion();
		rectangularMappingRegion.setxStart(xStart);
		rectangularMappingRegion.setxStop(xStop);
		rectangularMappingRegion.setyStart(yStart);
		rectangularMappingRegion.setyStop(yStop);

		// getPoint() returns bottom left ie min x and min y, getEndPoint() returns top right ie max x max y
		// epsilon is relative to the values * 1e-8 accept a small floating point error
		double epsilon = Math.abs(xStart + xStop + yStart + yStop) * 0.25 * 1e-8;
		assertArrayEquals(new double[] { xStop, yStop }, rectangularMappingRegion.toROI().getBounds().getPoint(), epsilon);
		assertArrayEquals(new double[] { xStart, yStart }, rectangularMappingRegion.toROI().getBounds().getEndPoint(), epsilon);
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
		RectangularMappingRegion rectangularMappingRegion = new RectangularMappingRegion();

		// Update region using ROI
		rectangularMappingRegion.updateFromROI(rectangularROI);

		// Check values
		assertEquals("xStart", xStart, rectangularMappingRegion.getxStart(), xStart * 1e-8);
		assertEquals("xStop", xStop, rectangularMappingRegion.getxStop(), xStop * 1e-8);
		assertEquals("yStart", yStart, rectangularMappingRegion.getyStart(), yStart * 1e-8);
		assertEquals("yStop", yStop, rectangularMappingRegion.getyStop(), yStop * 1e-8);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUpdatingFromInvalidROIType() {
		// Create ROI
		CircularROI circularROI = new CircularROI();

		// Create Region
		RectangularMappingRegion rectangularMappingRegion = new RectangularMappingRegion();

		// Update region using ROI should throw
		rectangularMappingRegion.updateFromROI(circularROI);
	}

}
