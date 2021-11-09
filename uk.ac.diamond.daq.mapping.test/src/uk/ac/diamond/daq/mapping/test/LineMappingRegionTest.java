package uk.ac.diamond.daq.mapping.test;

import static java.lang.Math.abs;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalMatchers.eq;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.CALC_POINTS;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.X_START;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.X_STOP;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.Y_START;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.Y_STOP;

import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;
import uk.ac.diamond.daq.mapping.region.LineMappingRegion;


@RunWith(MockitoJUnitRunner.class)
public class LineMappingRegionTest {

	@Mock
	private PropertyChangeSupport pcs;

	private LineMappingRegion lineMappingRegion;

	private void fullVerify(double xStart, double xStop, double yStart, double yStop, int noOfPointsCalculations) {
		verify(pcs, times(1)).firePropertyChange(X_START, 0.0, xStart);
		verify(pcs, times(1)).firePropertyChange(X_STOP,  1.0, xStop);
		verify(pcs, times(1)).firePropertyChange(Y_START, 0.0, yStart);
		verify(pcs, times(1)).firePropertyChange(Y_STOP,  1.0, yStop);
		verify(pcs, times(noOfPointsCalculations)).firePropertyChange(CALC_POINTS, 0, 1);
	}

	@Before
	public void setup() {
		lineMappingRegion = new LineMappingRegion();
		lineMappingRegion.usePCS(pcs);
	}

	@Test
	public void testGettingBoundingRectangleNoSwapsRequired() {
		double xStart = 10.23;
		double xStop = 34.25;
		double yStart = -12.3;
		double yStop = 11.4;

		lineMappingRegion.updateFromPropertiesMap(Map.of(X_START, xStart, X_STOP, xStop, Y_START, yStart, Y_STOP, yStop));

		// getPoint() returns bottom left ie min x and min y, getEndPoint() returns top right ie max x max y
		// epsilon is relative to the values * 1e-8 accept a small floating point error
		double epsilon = Math.abs(xStart + xStop + yStart + yStop) * 0.25 * 1e-8;
		assertArrayEquals(new double[] { xStart, yStart }, lineMappingRegion.toROI().getBounds().getPoint(), epsilon);
		assertArrayEquals(new double[] { xStop, yStop }, lineMappingRegion.toROI().getBounds().getEndPoint(), epsilon);
		fullVerify(xStart, xStop, yStart, yStop, 1);
	}

	@Test
	public void testGettingBoundingRectangleXSwapRequired() {
		double xStart = 10.23;
		double xStop = -34.25;
		double yStart = -12.3;
		double yStop = 11.4;

		lineMappingRegion.setxStart(xStart);
		lineMappingRegion.setxStop(xStop);
		lineMappingRegion.setyStart(yStart);
		lineMappingRegion.setyStop(yStop);

		// getPoint() returns bottom left ie min x and min y, getEndPoint() returns top right ie max x max y
		// epsilon is relative to the values * 1e-8 accept a small floating point error
		double epsilon = Math.abs(xStart + xStop + yStart + yStop) * 0.25 * 1e-8;
		assertArrayEquals(new double[] { xStop, yStart }, lineMappingRegion.toROI().getBounds().getPoint(), epsilon);
		assertArrayEquals(new double[] { xStart, yStop }, lineMappingRegion.toROI().getBounds().getEndPoint(), epsilon);
		fullVerify(xStart, xStop, yStart, yStop, 4);
	}

	@Test
	public void testGettingBoundingRectangleYSwapRequired() {
		double xStart = 10.23;
		double xStop = 34.25;
		double yStart = -12.3;
		double yStop = 11.4;

		lineMappingRegion.updateFromPropertiesMap(Map.of(X_START, xStart, X_STOP, xStop, Y_START, -yStart, Y_STOP, yStop));

		// getPoint() returns bottom left ie min x and min y, getEndPoint() returns top right ie max x max y
		// epsilon is relative to the values * 1e-8 accept a small floating point error
		double epsilon = Math.abs(xStart + xStop + yStart + yStop) * 0.25 * 1e-8;
		assertArrayEquals(new double[] { xStart, yStop }, lineMappingRegion.toROI().getBounds().getPoint(), epsilon);
		assertArrayEquals(new double[] { xStop, -yStart }, lineMappingRegion.toROI().getBounds().getEndPoint(), epsilon);
		fullVerify(xStart, xStop, -yStart, yStop, 1);
	}

	@Test
	public void testGettingBoundingRectangleXAndYSwapRequired() {
		double xStart = 10.23;
		double xStop = 34.25;
		double yStart = -12.3;
		double yStop = 11.4;

		lineMappingRegion.updateFromPropertiesMap(Map.of(X_START, xStart, X_STOP, -xStop, Y_START, -yStart, Y_STOP, yStop));

		// getPoint() returns bottom left ie min x and min y, getEndPoint() returns top right ie max x max y
		// epsilon is relative to the values * 1e-8 accept a small floating point error
		double epsilon = Math.abs(xStart + xStop + yStart + yStop) * 0.25 * 1e-8;
		assertArrayEquals(new double[] { -xStop, yStop }, lineMappingRegion.toROI().getBounds().getPoint(), epsilon);
		assertArrayEquals(new double[] { xStart, -yStart }, lineMappingRegion.toROI().getBounds().getEndPoint(), epsilon);
		fullVerify(xStart, -xStop, -yStart, yStop, 1);
	}

	@Test
	public void testUpdatingFromROI() {
		double xStart = 10.23;
		double xStop = 34.25;
		double yStart = -12.3;
		double yStop = 11.4;

		// Create ROI
		LinearROI linearROI = new LinearROI(new double[] { xStart, yStart }, new double[] { xStop, yStop });

		// Update region using ROI
		lineMappingRegion.updateFromROI(linearROI);

		// Check values
		assertEquals(X_START, xStart, lineMappingRegion.getxStart(), xStart * 1e-8);
		assertEquals(X_STOP, xStop, lineMappingRegion.getxStop(), xStop * 1e-8);
		assertEquals(Y_START, yStart, lineMappingRegion.getyStart(), yStart * 1e-8);
		assertEquals(Y_STOP, yStop, lineMappingRegion.getyStop(), yStop * 1e-8);
		verify(pcs, times(1)).firePropertyChange(eq(X_START), eq(0.0), eq(xStart, abs(xStart * 1e-8)));
		verify(pcs, times(1)).firePropertyChange(eq(X_STOP),  eq(1.0), eq(xStop, abs(xStop * 1e-8)));
		verify(pcs, times(1)).firePropertyChange(eq(Y_START), eq(0.0), eq(yStart, abs(yStart * 1e-8)));
		verify(pcs, times(1)).firePropertyChange(eq(Y_STOP),  eq(1.0), eq(yStop, abs(yStop * 1e-8)));
		verify(pcs, times(1)).firePropertyChange(CALC_POINTS, 0, 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUpdatingFromInvalidROIType() {
		// Create ROI
		RectangularROI rectangularROI = new RectangularROI();

		// Update region using ROI should throw
		lineMappingRegion.updateFromROI(rectangularROI);
	}

	@Test
	public void testCopy() {
		lineMappingRegion.updateFromPropertiesMap(Map.of(X_START, 3.6, X_STOP, 6.2, Y_START, 1.5, Y_STOP, -8.2));

		final IMappingScanRegionShape copy = lineMappingRegion.copy();

		assertThat(copy, is(equalTo(lineMappingRegion)));
		assertThat(copy, is(not(sameInstance(lineMappingRegion))));
	}

	@Test
	public void testCentre() {
		lineMappingRegion.centre(-7, 1);

		assertThat(lineMappingRegion.getxStart(), is(-7.5)); // centreX - length in x / 2
		assertThat(lineMappingRegion.getxStop(), is(-6.5));  // centreX + length in x / 2

		assertThat(lineMappingRegion.getyStart(), is(0.5));  // centreY + length in x / 2 (because start > stop!)
		assertThat(lineMappingRegion.getyStop(), is(1.5));  // centreY - length in x / 2
		fullVerify( -7.5, -6.5, 0.5, 1.5, 1);
	}

	@Test
	public void updateFromPropertiesIgnoresThoseNotInValueSwitcher() {
		lineMappingRegion.updateFromPropertiesMap(Map.of(X_START, 10.0, "X__STOP", 0.0, "Flange", List.of(1, 2, 3), Y_START, 3.0, "Y_STOP", 6.0, "Banana", "Fridge"));

		assertThat(lineMappingRegion.getxStart(), is(10.0));
		assertThat(lineMappingRegion.getyStart(), is(3.0));
		verify(pcs, times(1)).firePropertyChange(X_START, 0.0, 10.0);
		verify(pcs, times(1)).firePropertyChange(Y_START, 0.0, 3.0);
		verify(pcs, times(1)).firePropertyChange(CALC_POINTS, 0, 1);
	}
}
