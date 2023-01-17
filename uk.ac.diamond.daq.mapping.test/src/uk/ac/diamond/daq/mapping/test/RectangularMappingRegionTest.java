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
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.UPDATE_COMPLETE;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.X_START;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.X_STOP;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.Y_START;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.Y_STOP;

import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;
import uk.ac.diamond.daq.mapping.region.RectangularMappingRegion;

@RunWith(MockitoJUnitRunner.class)
public class RectangularMappingRegionTest {

	@Mock
	private PropertyChangeSupport pcs;

	private RectangularMappingRegion rectangularMappingRegion;

	private void fullVerify(double xStart, double xStop, double yStart, double yStop, int noOfPointsCalculations) {
		verify(pcs, times(1)).firePropertyChange(X_START, 0.0, xStart);
		verify(pcs, times(1)).firePropertyChange(X_STOP,  1.0, xStop);
		verify(pcs, times(1)).firePropertyChange(Y_START, 0.0, yStart);
		verify(pcs, times(1)).firePropertyChange(Y_STOP,  1.0, yStop);
		verify(pcs, times(noOfPointsCalculations)).firePropertyChange(UPDATE_COMPLETE, 0, 1);
	}

	@Before
	public void setup() {
		rectangularMappingRegion = new RectangularMappingRegion();
		rectangularMappingRegion.usePCS(pcs);
	}

	@Test
	public void testGettingBoundingRectangleXStartLessThanXStopAndYStartLessThanYStop() {
		double xStart = 13.335;
		double xStop = 41.27;
		double yStart = -35.2;
		double yStop = 0.0;

		rectangularMappingRegion.updateFromPropertiesMap(Map.of(X_START, xStart, X_STOP, xStop, Y_START, yStart, Y_STOP, yStop));

		// getPoint() returns bottom left ie min x and min y, getEndPoint() returns top right ie max x max y
		// epsilon is relative to the values * 1e-8 accept a small floating point error
		double epsilon = Math.abs(xStart + xStop + yStart + yStop) * 0.25 * 1e-8;
		assertArrayEquals(new double[] { xStart, yStart }, rectangularMappingRegion.toROI().getBounds().getPoint(), epsilon);
		assertArrayEquals(new double[] { xStop, yStop }, rectangularMappingRegion.toROI().getBounds().getEndPoint(), epsilon);
		fullVerify(xStart, xStop, yStart, yStop, 1);
	}

	@Test
	public void testGettingBoundingRectangleXStopLessThanXStartAndYStartLessThanYStop() {
		double xStart = 13.335;
		double xStop = -41.27;
		double yStart = -35.2;
		double yStop = 0.0;

		rectangularMappingRegion.setxStart(xStart);		// Set individual ordinate values which will generate a points cal
		rectangularMappingRegion.setxStop(xStop);		// event for each hence need 4 as last param to fullVerify
		rectangularMappingRegion.setyStart(yStart);
		rectangularMappingRegion.setyStop(yStop);

		// getPoint() returns bottom left ie min x and min y, getEndPoint() returns top right ie max x max y
		// epsilon is relative to the values * 1e-8 accept a small floating point error
		double epsilon = Math.abs(xStart + xStop + yStart + yStop) * 0.25 * 1e-8;
		assertArrayEquals(new double[] { xStop, yStart }, rectangularMappingRegion.toROI().getBounds().getPoint(), epsilon);
		assertArrayEquals(new double[] { xStart, yStop }, rectangularMappingRegion.toROI().getBounds().getEndPoint(), epsilon);
		fullVerify(xStart, xStop, yStart, yStop, 4);
	}

	@Test
	public void testGettingBoundingRectangleXStartLessThanXStopAndYStopLessThanYStart() {
		double xStart = 13.335;
		double xStop = 41.27;
		double yStart = -45.2;
		double yStop = -58.5;

		rectangularMappingRegion.updateFromPropertiesMap(Map.of(X_START, xStart, X_STOP, xStop, Y_START, yStart, Y_STOP, yStop));

		// getPoint() returns bottom left ie min x and min y, getEndPoint() returns top right ie max x max y
		// epsilon is relative to the values * 1e-8 accept a small floating point error
		double epsilon = Math.abs(xStart + xStop + yStart + yStop) * 0.25 * 1e-8;
		assertArrayEquals(new double[] { xStart, yStop }, rectangularMappingRegion.toROI().getBounds().getPoint(), epsilon);
		assertArrayEquals(new double[] { xStop, yStart }, rectangularMappingRegion.toROI().getBounds().getEndPoint(), epsilon);
		fullVerify(xStart, xStop, yStart, yStop, 1);
	}

	@Test
	public void testGettingBoundingRectangleXStopLessThanXStartAndYStopLessThanYStart() {
		double xStart = 13.335;
		double xStop = -41.27;
		double yStart = -45.2;
		double yStop = -58.5;

		rectangularMappingRegion.updateFromPropertiesMap(Map.of(X_START, xStart, X_STOP, xStop, Y_START, yStart, Y_STOP, yStop));

		// getPoint() returns bottom left ie min x and min y, getEndPoint() returns top right ie max x max y
		// epsilon is relative to the values * 1e-8 accept a small floating point error
		double epsilon = Math.abs(xStart + xStop + yStart + yStop) * 0.25 * 1e-8;
		assertArrayEquals(new double[] { xStop, yStop }, rectangularMappingRegion.toROI().getBounds().getPoint(), epsilon);
		assertArrayEquals(new double[] { xStart, yStart }, rectangularMappingRegion.toROI().getBounds().getEndPoint(), epsilon);
		fullVerify(xStart, xStop, yStart, yStop, 1);
	}

	@Test
	public void testUpdatingFromROI() {
		double xStart = 10.23;
		double xStop = 34.25;
		double yStart = -12.3;
		double yStop = 11.4;

		// Create ROI
		RectangularROI rectangularROI = new RectangularROI(xStart, yStart, (xStop - xStart), (yStop - yStart), 0);

		// Update region using ROI
		rectangularMappingRegion.updateFromROI(rectangularROI);

		// Check values
		assertEquals(X_START, xStart, rectangularMappingRegion.getxStart(), xStart * 1e-8);
		assertEquals(X_STOP, xStop, rectangularMappingRegion.getxStop(), xStop * 1e-8);
		assertEquals(Y_START, yStart, rectangularMappingRegion.getyStart(), yStart * 1e-8);
		assertEquals(Y_STOP, yStop, rectangularMappingRegion.getyStop(), yStop * 1e-8);
		verify(pcs, times(1)).firePropertyChange(eq(X_START), eq(0.0), eq(xStart, abs(xStart * 1e-8)));
		verify(pcs, times(1)).firePropertyChange(eq(X_STOP),  eq(1.0), eq(xStop, abs(xStop * 1e-8)));
		verify(pcs, times(1)).firePropertyChange(eq(Y_START), eq(0.0), eq(yStart, abs(yStart * 1e-8)));
		verify(pcs, times(1)).firePropertyChange(eq(Y_STOP),  eq(1.0), eq(yStop, abs(yStop * 1e-8)));
		verify(pcs, times(1)).firePropertyChange(UPDATE_COMPLETE, 0, 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUpdatingFromInvalidROIType() {
		// Create ROI
		CircularROI circularROI = new CircularROI();

		// Update region using ROI should throw
		rectangularMappingRegion.updateFromROI(circularROI);
	}

	@Test
	public void testCopy() {
		rectangularMappingRegion.updateFromPropertiesMap(Map.of(X_START, 3.6, X_STOP, 6.2, Y_START, 1.5, Y_STOP, -8.2));

		final IMappingScanRegionShape copy = rectangularMappingRegion.copy();

		assertThat(copy, is(equalTo(rectangularMappingRegion)));
		assertThat(copy, is(not(sameInstance(rectangularMappingRegion))));
	}

	@Test
	public void testCentre() {
		rectangularMappingRegion.centre(-1, 1);

		assertThat(rectangularMappingRegion.getxStart(), is(-1.5));
		assertThat(rectangularMappingRegion.getxStop(), is(-0.5));
		assertThat(rectangularMappingRegion.getyStart(), is(0.5));
		assertThat(rectangularMappingRegion.getyStop(), is(1.5));
		fullVerify(-1.5, -0.5, 0.5, 1.5, 1);
	}

	@Test
	public void updateFromPropertiesIgnoresThoseNotInValueSwitcher() {
		rectangularMappingRegion.updateFromPropertiesMap(Map.of(X_START, 10.0, "X__STOP", 0.0, "Flange", List.of(1, 2, 3), Y_START, 3.0, "Y_STOP", 6.0, "Banana", "Fridge"));

		assertThat(rectangularMappingRegion.getxStart(), is(10.0));
		assertThat(rectangularMappingRegion.getyStart(), is(3.0));
		verify(pcs, times(1)).firePropertyChange(X_START, 0.0, 10.0);
		verify(pcs, times(1)).firePropertyChange(Y_START, 0.0, 3.0);
		verify(pcs, times(1)).firePropertyChange(UPDATE_COMPLETE, 0, 1);
	}
}
