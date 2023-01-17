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
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.X_CENTRE;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.X_RANGE;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.Y_CENTRE;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.Y_RANGE;

import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;
import uk.ac.diamond.daq.mapping.region.CentredRectangleMappingRegion;

@RunWith(MockitoJUnitRunner.class)
public class CentredRectangleMappingRegionTest {

	@Mock
	private PropertyChangeSupport pcs;

	private CentredRectangleMappingRegion createCentredRectangleRegion() {
		CentredRectangleMappingRegion rect = new CentredRectangleMappingRegion();
		rect.usePCS(pcs);
		return rect;
	}

	private void fullVerify(double xCentre, double xRange, double yCentre, double yRange, int noOfPointsCalculations) {
		verify(pcs, times(1)).firePropertyChange(X_RANGE,  1.0, xRange);
		verify(pcs, times(1)).firePropertyChange(Y_RANGE,  1.0, yRange);
		centreVerify(xCentre, yCentre, noOfPointsCalculations);
		}

	private void centreVerify(double xCentre, double yCentre, int noOfPointsCalculations) {
		verify(pcs, times(1)).firePropertyChange(X_CENTRE, 0.5, xCentre);
		verify(pcs, times(1)).firePropertyChange(Y_CENTRE, 0.5, yCentre);
		verify(pcs, times(noOfPointsCalculations)).firePropertyChange(UPDATE_COMPLETE, 0, 1);
	}

	private double getStart(double centre, double range, boolean startLessThanStop) {
		if (startLessThanStop) return centre - range / 2;
		return centre + range / 2;
	}

	@Test
	public void testGettingBoundingRectangleXStartLessThanXStopAndYStartLessThanYStop() {
		double xCentre = 13.335;
		double xRange = 41.27;
		double yCentre = -35.2;
		double yRange = 1.0;
		double xStart = getStart(xCentre,  xRange, true);
		double yStart = getStart(yCentre, yRange, true);

		CentredRectangleMappingRegion rectangularMappingRegion = createCentredRectangleRegion();
		rectangularMappingRegion.updateFromPropertiesMap(Map.of(X_CENTRE, xCentre, X_RANGE, xRange, Y_CENTRE, yCentre, Y_RANGE, yRange));

		double epsilon = abs(2 * xStart + xRange + 2 * yStart + yRange) * 0.25 * 1e-8;
		assertArrayEquals(new double[] {xStart, yStart}, rectangularMappingRegion.toROI().getBounds().getPoint(), epsilon);
		assertArrayEquals(new double[] {xStart + xRange, yStart + yRange}, rectangularMappingRegion.toROI().getBounds().getEndPoint(), epsilon);
		fullVerify(xCentre, xRange, yCentre, yRange, 1);
	}

	@Test
	public void testGettingBoundingRectangleXStopLessThanXStartAndYStartLessThanYStop() {
		double xCentre = 13.335;
		double xRange = 41.27;
		double yCentre = -35.2;
		double yRange = 1.0;
		double xStart = getStart(xCentre,  xRange, false);
		double yStart = getStart(yCentre, yRange, true);

		CentredRectangleMappingRegion rectangularMappingRegion = createCentredRectangleRegion();
		rectangularMappingRegion.setxCentre(xCentre);
		rectangularMappingRegion.setyCentre(yCentre);
		rectangularMappingRegion.setxRange(xRange);
		rectangularMappingRegion.setyRange(yRange);

		double epsilon = abs(2 * xStart - xRange + 2 * yStart + yRange) * 0.25 * 1e-8;
		assertArrayEquals(new double[] {xStart - xRange, yStart}, rectangularMappingRegion.toROI().getBounds().getPoint(), epsilon);
		assertArrayEquals(new double[] {xStart, yStart + yRange}, rectangularMappingRegion.toROI().getBounds().getEndPoint(), epsilon);
		fullVerify(xCentre, xRange, yCentre, yRange, 4);
	}

	@Test
	public void testGettingBoundingRectangleXStartLessThanXStopAndYStopLessThanYStart() {
		double xCentre = 13.335;
		double xRange = 41.27;
		double yCentre = -45.2;
		double yRange = 58.5;
		double xStart = getStart(xCentre,  xRange, true);
		double yStart = getStart(yCentre, yRange, false);

		CentredRectangleMappingRegion rectangularMappingRegion = createCentredRectangleRegion();
		rectangularMappingRegion.updateFromPropertiesMap(Map.of(X_CENTRE, xCentre, X_RANGE, xRange, Y_CENTRE, yCentre, Y_RANGE, yRange));

		double epsilon = abs(2 * xStart + xRange + 2 * yStart - yRange) * 0.25 * 1e-8;
		assertArrayEquals(new double[] {xStart, yStart - yRange}, rectangularMappingRegion.toROI().getBounds().getPoint(), epsilon);
		assertArrayEquals(new double[] {xStart + xRange, yStart}, rectangularMappingRegion.toROI().getBounds().getEndPoint(), epsilon);
		fullVerify(xCentre, xRange, yCentre, yRange, 1);
	}

	@Test
	public void testGettingBoundingRectangleXStopLessThanXStartAndYStopLessThanYStart() {
		double xCentre = 13.335;
		double xRange = 41.27;
		double yCentre = -45.2;
		double yRange = 58.5;
		double xStart = getStart(xCentre,  xRange, false);
		double yStart = getStart(yCentre, yRange, false);

		CentredRectangleMappingRegion rectangularMappingRegion = createCentredRectangleRegion();
		rectangularMappingRegion.updateFromPropertiesMap(Map.of(X_CENTRE, xCentre, X_RANGE, xRange, Y_CENTRE, yCentre, Y_RANGE, yRange));

		double epsilon = abs(2 * xStart - xRange + 2 * yStart - yRange) * 0.25 * 1e-8;
		assertArrayEquals(new double[] {xStart - xRange, yStart - yRange}, rectangularMappingRegion.toROI().getBounds().getPoint(), epsilon);
		assertArrayEquals(new double[] {xStart, yStart}, rectangularMappingRegion.toROI().getBounds().getEndPoint(), epsilon);
		fullVerify(xCentre, xRange, yCentre, yRange, 1);
	}

	@Test
	public void testUpdatingFromROI() {
		double xCentre = 10.23;
		double xRange = 34.25;
		double yCentre = -12.3;
		double yRange = 11.4;
		double xStart = getStart(xCentre,  xRange, true);
		double yStart = getStart(yCentre, yRange, true);

		// Create ROI
		RectangularROI rectangularROI = new RectangularROI(xStart, yStart, xRange, yRange, 0);

		// Create Region
		CentredRectangleMappingRegion rectangularMappingRegion = createCentredRectangleRegion();

		// Update region from ROI
		rectangularMappingRegion.updateFromROI(rectangularROI);

		// Check values
		assertEquals(X_CENTRE, xCentre, rectangularMappingRegion.getxCentre(), xCentre * 1e-8);
		assertEquals(X_RANGE, xRange, rectangularMappingRegion.getxRange(), xRange * 1e-8);
		assertEquals(Y_CENTRE, yCentre, rectangularMappingRegion.getyCentre(), yCentre * 1e-8);
		assertEquals(Y_RANGE, yRange, rectangularMappingRegion.getyRange(), yRange * 1e-8);
		verify(pcs, times(1)).firePropertyChange(eq(X_CENTRE), eq(0.5), eq(xCentre, abs(xCentre * 1e-8)));
		verify(pcs, times(1)).firePropertyChange(eq(X_RANGE),  eq(1.0), eq(xRange, abs(xRange * 1e-8)));
		verify(pcs, times(1)).firePropertyChange(eq(Y_CENTRE), eq(0.5), eq(yCentre, abs(yCentre * 1e-8)));
		verify(pcs, times(1)).firePropertyChange(eq(Y_RANGE),  eq(1.0), eq(yRange, abs(yRange * 1e-8)));
		verify(pcs, times(1)).firePropertyChange(UPDATE_COMPLETE, 0, 1);
	}

	@Test
	public void testCopy() {
		double xCentre = 13.335;
		double xRange = 41.27;
		double yCentre = -45.2;
		double yRange = 58.5;

		CentredRectangleMappingRegion original = createCentredRectangleRegion();
		original.updateFromPropertiesMap(Map.of(X_CENTRE, xCentre, X_RANGE, xRange, Y_CENTRE, yCentre, Y_RANGE, yRange));

		final IMappingScanRegionShape copy = original.copy();

		assertThat(copy, is(equalTo(original)));
		assertThat(copy, is(not(sameInstance(original))));
		fullVerify(xCentre, xRange, yCentre, yRange, 1);
	}

	@Test
	public void testCentre() {
		CentredRectangleMappingRegion region = createCentredRectangleRegion();
		region.centre(50.0, 40.0);

		assertThat(region.getxCentre(), is(50.0));
		assertThat(region.getyCentre(), is(40.0));

		// ranges should be unaffected
		assertThat(region.getxRange(), is(1.0));
		assertThat(region.getyRange(), is(1.0));
		verify(pcs, times(1)).firePropertyChange(X_CENTRE, 0.5, 50.0);
		verify(pcs, times(1)).firePropertyChange(Y_CENTRE, 0.5, 40.0);
		verify(pcs, times(1)).firePropertyChange(UPDATE_COMPLETE, 0, 1);
	}

	@Test
	public void updateFromPropertiesIgnoresThoseNotInValueSwitcher() {
		CentredRectangleMappingRegion rectangularMappingRegion = new CentredRectangleMappingRegion();
		rectangularMappingRegion.usePCS(pcs);

		rectangularMappingRegion.updateFromPropertiesMap(Map.of(X_CENTRE, 10.0, "X__RANGE", 0.0, "Flange", List.of(1, 2, 3), Y_CENTRE, 3.0, "Y_RANGE", 6.0, "Banana", "Fridge"));

		assertThat(rectangularMappingRegion.getxCentre(), is(10.0));
		assertThat(rectangularMappingRegion.getyCentre(), is(3.0));
		verify(pcs, times(1)).firePropertyChange(X_CENTRE, 0.5, 10.0);
		verify(pcs, times(1)).firePropertyChange(Y_CENTRE, 0.5, 3.0);
		verify(pcs, times(1)).firePropertyChange(UPDATE_COMPLETE, 0, 1);
	}
}
