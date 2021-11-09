package uk.ac.diamond.daq.mapping.test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.CALC_POINTS;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.RADIUS;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.X_CENTRE;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.Y_CENTRE;

import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;
import uk.ac.diamond.daq.mapping.region.CircularMappingRegion;

@RunWith(MockitoJUnitRunner.class)
public class CircularMappingRegionTest {

	@Mock
	private PropertyChangeSupport pcs;

	private CircularMappingRegion createRegion() {
		CircularMappingRegion circularMappingRegion = new CircularMappingRegion();
		circularMappingRegion.usePCS(pcs);return circularMappingRegion;
	}

	private void fullVerify(double xCentre, double yCentre, double radius, int noOfPointsCalculations) {
		verify(pcs, times(1)).firePropertyChange(RADIUS, 1.0, radius);
		centreVerify(xCentre, yCentre, noOfPointsCalculations);
	}

	private void centreVerify(double xCentre, double yCentre, int noOfPointsCalculations) {
		verify(pcs, times(1)).firePropertyChange(X_CENTRE, 0.0, xCentre);
		verify(pcs, times(1)).firePropertyChange(Y_CENTRE, 0.0, yCentre);
		verify(pcs, times(noOfPointsCalculations)).firePropertyChange(CALC_POINTS, 0, 1);
	}

	@Test
	public void testGettingBoundingRectange() {
		double xCentre = 15.25;
		double yCentre = -23.65;
		double radius = 5.64;

		CircularMappingRegion circularMappingRegion = createRegion();
		circularMappingRegion.setxCentre(xCentre);
		circularMappingRegion.setyCentre(yCentre);
		circularMappingRegion.setRadius(radius);

		// getPoint() returns bottom left ie min x and min y, getEndPoint() returns top right ie max x max y
		// epsilon is relative to the values * 1e-8 accept a small floating point error
		double epsilon = Math.abs(xCentre + yCentre + radius) * 0.33 * 1e-8;
		assertArrayEquals(new double[] { xCentre - radius, yCentre - radius }, circularMappingRegion.toROI().getBounds().getPoint(), epsilon);
		assertArrayEquals(new double[] { xCentre + radius, yCentre + radius }, circularMappingRegion.toROI().getBounds().getEndPoint(), epsilon);
		fullVerify(xCentre, yCentre, radius, 3);
	}

	@Test
	public void testUpdatingFromROI() {
		double xCentre = 15.25;
		double yCentre = -23.65;
		double radius = 5.64;

		// Create ROI
		CircularROI circularROI = new CircularROI(radius, xCentre, yCentre);

		// Create Region
		CircularMappingRegion circularMappingRegion = createRegion();

		// Update region using ROI
		circularMappingRegion.updateFromROI(circularROI);

		// Check values
		assertEquals(X_CENTRE, xCentre, circularMappingRegion.getxCentre(), xCentre * 1e-8);
		assertEquals(Y_CENTRE, yCentre, circularMappingRegion.getyCentre(), yCentre * 1e-8);
		assertEquals(RADIUS, radius, circularMappingRegion.getRadius(), radius * 1e-8);
		fullVerify(xCentre, yCentre, radius, 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUpdatingFromInvalidROIType() {
		// Create ROI
		RectangularROI rectangularROI = new RectangularROI();

		// Create Region
		CircularMappingRegion circularMappingRegion = createRegion();

		// Update region using ROI should throw
		circularMappingRegion.updateFromROI(rectangularROI);
	}

	@Test
	public void testCopy() {
		final CircularMappingRegion original = createRegion();
		original.updateFromPropertiesMap(Map.of(X_CENTRE, 5.0, Y_CENTRE, 6.2, RADIUS, 7.1));

		final IMappingScanRegionShape copy = original.copy();

		assertThat(copy, is(equalTo(original)));
		assertThat(copy, is(not(sameInstance(original))));
		fullVerify(5.0, 6.2, 7.1, 1);
	}

	@Test
	public void testCentre() {
		CircularMappingRegion region = createRegion();

		double targetX = 23.4;
		double targetY = 12.3;

		region.centre(targetX, targetY);

		assertThat(region.getxCentre(), is(targetX));
		assertThat(region.getyCentre(), is(targetY));
		assertThat(region.getRadius(), is(1.0)); // unaffected
		centreVerify(targetX, targetY, 1);
	}

	@Test
	public void updateFromPropertiesIgnoresThoseNotInValueSwitcher() {
		CircularMappingRegion region = createRegion();
		region.updateFromPropertiesMap(Map.of(X_CENTRE, -12.0, "X__STOP", 0.0, "Flange", List.of(1, 2, 3), Y_CENTRE, 3.0, "Y_STOP", 6.0, "Banana", "Fridge"));

		assertThat(region.getxCentre(), is(-12.0));
		assertThat(region.getyCentre(), is(3.0));
		centreVerify(-12.0, 3.0, 1);
	}
}
