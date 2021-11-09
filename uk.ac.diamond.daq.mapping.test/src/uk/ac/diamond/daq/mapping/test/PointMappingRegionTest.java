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

import static java.lang.Math.abs;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalMatchers.eq;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.CALC_POINTS;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.X_POSITION;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.Y_POSITION;

import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PointROI;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;
import uk.ac.diamond.daq.mapping.region.PointMappingRegion;

@RunWith(MockitoJUnitRunner.class)
public class PointMappingRegionTest {

	@Mock
	private PropertyChangeSupport pcs;

	private PointMappingRegion region;

	private void fullVerify(double x, double y, int noOfPointsCalculations) {
		verify(pcs, times(1)).firePropertyChange(X_POSITION, 0.0, x);
		verify(pcs, times(1)).firePropertyChange(Y_POSITION, 0.0, y);
		verify(pcs, times(noOfPointsCalculations)).firePropertyChange(CALC_POINTS, 0, 1);
	}

	@Before
	public void setup() {
		region = new PointMappingRegion();
		region.usePCS(pcs);
	}

	@Test
	public void updateWithSettersSucceeds() {
		double x = -10.23;
		double y = 12.3;

		region.setxPosition(x);
		region.setyPosition(y);

		assertThat(region.getxPosition(), is(x));
		assertThat(region.getyPosition(), is(y));
		fullVerify(x, y, 2);
	}

	@Test
	public void testUpdatingFromROI() {
		double x = 10.23;
		double y = -12.3;

		// Create ROI
		PointROI pointROI = new PointROI(x, y);

		// Update region using ROI
		region.updateFromROI(pointROI);

		// Check values
		assertEquals(X_POSITION, x, region.getxPosition(), x * 1e-8);
		assertEquals(Y_POSITION, y, region.getyPosition(), y* 1e-8);
		verify(pcs, times(1)).firePropertyChange(eq(X_POSITION), eq(0.0), eq(x, abs(x * 1e-8)));
		verify(pcs, times(1)).firePropertyChange(eq(Y_POSITION),  eq(0.0), eq(y, abs(y * 1e-8)));
		verify(pcs, times(1)).firePropertyChange(CALC_POINTS, 0, 1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUpdatingFromInvalidROIType() {
		// Create ROI
		CircularROI circularROI = new CircularROI();

		// Update region using ROI should throw
		region.updateFromROI(circularROI);
	}

	@Test
	public void testCopy() {
		region.updateFromPropertiesMap(Map.of(X_POSITION, 3.6, Y_POSITION, 1.5));

		final IMappingScanRegionShape copy = region.copy();

		assertThat(copy, is(equalTo(region)));
		assertThat(copy, is(not(sameInstance(region))));
	}

	@Test
	public void testCentre() {
		region.centre(4, -3.5);
		assertThat(region.getxPosition(), is(4.0));
		assertThat(region.getyPosition(), is(-3.5));
		fullVerify(4.0, -3.5, 1);
	}

	@Test
	public void updateFromPropertiesIgnoresThoseNotInValueSwitcher() {
		region.updateFromPropertiesMap(Map.of(X_POSITION, 10.0, "X__STOP", 0.0, "Flange", List.of(1, 2, 3), Y_POSITION, 3.0, "Y_STOP", 6.0, "Banana", "Fridge"));

		assertThat(region.getxPosition(), is(10.0));
		assertThat(region.getyPosition(), is(3.0));
		fullVerify(10.0, 3.0, 1);
	}

}
