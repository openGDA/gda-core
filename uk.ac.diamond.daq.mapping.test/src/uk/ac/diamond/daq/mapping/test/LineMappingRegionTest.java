package uk.ac.diamond.daq.mapping.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.junit.Test;

import uk.ac.diamond.daq.mapping.region.LineMappingRegion;

public class LineMappingRegionTest {

	@Test
	public void testGettingBoundingRectangeNoSwapsRequired() {
		double xStart = 10.23;
		double xStop = 34.25;
		double yStart = -12.3;
		double yStop = 11.4;

		LineMappingRegion lineMappingRegion = new LineMappingRegion();
		lineMappingRegion.setxStart(xStart);
		lineMappingRegion.setxStop(xStop);
		lineMappingRegion.setyStart(yStart);
		lineMappingRegion.setyStop(yStop);

		// getPoint() returns bottom left ie min x and min y, getEndPoint() returns top right ie max x max y
		// epsilon is relative to the values * 1e-8 accept a small floating point error
		double epsilon = Math.abs(xStart + xStop + yStart + yStop) * 0.25 * 1e-8;
		assertArrayEquals(new double[] { xStart, yStart }, lineMappingRegion.toROI().getBounds().getPoint(), epsilon);
		assertArrayEquals(new double[] { xStop, yStop }, lineMappingRegion.toROI().getBounds().getEndPoint(), epsilon);
	}

	@Test
	public void testGettingBoundingRectangeXSwapRequired() {
		double xStart = 10.23;
		double xStop = -34.25;
		double yStart = -12.3;
		double yStop = 11.4;

		LineMappingRegion lineMappingRegion = new LineMappingRegion();
		lineMappingRegion.setxStart(xStart);
		lineMappingRegion.setxStop(xStop);
		lineMappingRegion.setyStart(yStart);
		lineMappingRegion.setyStop(yStop);

		// getPoint() returns bottom left ie min x and min y, getEndPoint() returns top right ie max x max y
		// epsilon is relative to the values * 1e-8 accept a small floating point error
		double epsilon = Math.abs(xStart + xStop + yStart + yStop) * 0.25 * 1e-8;
		assertArrayEquals(new double[] { xStop, yStart }, lineMappingRegion.toROI().getBounds().getPoint(), epsilon);
		assertArrayEquals(new double[] { xStart, yStop }, lineMappingRegion.toROI().getBounds().getEndPoint(), epsilon);
	}

	@Test
	public void testGettingBoundingRectangeYSwapRequired() {
		double xStart = 10.23;
		double xStop = 34.25;
		double yStart = -12.3;
		double yStop = 11.4;

		LineMappingRegion lineMappingRegion = new LineMappingRegion();
		lineMappingRegion.setxStart(xStart);
		lineMappingRegion.setxStop(xStop);
		lineMappingRegion.setyStart(-yStart);
		lineMappingRegion.setyStop(yStop);

		// getPoint() returns bottom left ie min x and min y, getEndPoint() returns top right ie max x max y
		// epsilon is relative to the values * 1e-8 accept a small floating point error
		double epsilon = Math.abs(xStart + xStop + yStart + yStop) * 0.25 * 1e-8;
		assertArrayEquals(new double[] { xStart, yStop }, lineMappingRegion.toROI().getBounds().getPoint(), epsilon);
		assertArrayEquals(new double[] { xStop, -yStart }, lineMappingRegion.toROI().getBounds().getEndPoint(), epsilon);
	}

	@Test
	public void testGettingBoundingRectangeZAndYSwapRequired() {
		double xStart = 10.23;
		double xStop = 34.25;
		double yStart = -12.3;
		double yStop = 11.4;

		LineMappingRegion lineMappingRegion = new LineMappingRegion();
		lineMappingRegion.setxStart(xStart);
		lineMappingRegion.setxStop(-xStop);
		lineMappingRegion.setyStart(-yStart);
		lineMappingRegion.setyStop(yStop);

		// getPoint() returns bottom left ie min x and min y, getEndPoint() returns top right ie max x max y
		// epsilon is relative to the values * 1e-8 accept a small floating point error
		double epsilon = Math.abs(xStart + xStop + yStart + yStop) * 0.25 * 1e-8;
		assertArrayEquals(new double[] { -xStop, yStop }, lineMappingRegion.toROI().getBounds().getPoint(), epsilon);
		assertArrayEquals(new double[] { xStart, -yStart }, lineMappingRegion.toROI().getBounds().getEndPoint(), epsilon);
	}

	@Test
	public void testUpdatingFromROI() {
		double xStart = 10.23;
		double xStop = 34.25;
		double yStart = -12.3;
		double yStop = 11.4;

		// Create ROI
		LinearROI linearROI = new LinearROI(new double[] { xStart, yStart }, new double[] { xStop, yStop });

		// Create Region
		LineMappingRegion lineMappingRegion = new LineMappingRegion();

		// Update region using ROI
		lineMappingRegion.updateFromROI(linearROI);

		// Check values
		assertEquals("xStart", xStart, lineMappingRegion.getxStart(), xStart * 1e-8);
		assertEquals("xStop", xStop, lineMappingRegion.getxStop(), xStop * 1e-8);
		assertEquals("yStart", yStart, lineMappingRegion.getyStart(), yStart * 1e-8);
		assertEquals("yStop", yStop, lineMappingRegion.getyStop(), yStop * 1e-8);
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

}
