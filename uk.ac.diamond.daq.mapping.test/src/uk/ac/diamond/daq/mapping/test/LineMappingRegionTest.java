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

	@Test
	public void testCopy() {
		final LineMappingRegion original = new LineMappingRegion();
		final IMappingScanRegionShape copy = original.copy();

		assertThat(copy, is(equalTo(original)));
		assertThat(copy, is(not(sameInstance(original))));
	}

	@Test
	public void testCentre() {
		LineMappingRegion region = new LineMappingRegion();
		region.setxStart(12);
		region.setxStop(16); // length in x = 4
		region.setyStart(5);
		region.setyStop(-5); // length in y = 10

		double centreX = -7;
		double centreY = 1;

		region.centre(centreX, centreY);

		assertThat(region.getxStart(), is(-9.0)); // centreX - length in x / 2
		assertThat(region.getxStop(), is(-5.0));  // centreX + length in x / 2

		assertThat(region.getyStart(), is(6.0));  // centreY + length in x / 2 (because start > stop!)
		assertThat(region.getyStop(), is(-4.0));  // centreY - length in x / 2
	}

}
