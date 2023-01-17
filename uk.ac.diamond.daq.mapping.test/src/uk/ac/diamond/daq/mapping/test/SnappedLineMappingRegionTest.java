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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.UPDATE_COMPLETE;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.CONSTANT;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.START;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.STOP;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.X_START;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.X_STOP;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.Y_START;
import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.Y_STOP;

import java.beans.PropertyChangeSupport;

import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;
import uk.ac.diamond.daq.mapping.region.SnappedLineMappingRegion;

@RunWith(MockitoJUnitRunner.class)
public class SnappedLineMappingRegionTest {

	@Mock
	private PropertyChangeSupport pcs;

	private SnappedLineMappingRegion lineMappingRegion;

	private void fullVerify(double xStart, double xStop, double yStart, double yStop, boolean isHoriz) {
		verify(pcs, times(1)).firePropertyChange(START, 0.0, isHoriz ? xStart : yStart);
		verify(pcs, times(1)).firePropertyChange(STOP,  1.0, isHoriz ? xStop : yStop);
		verify(pcs, times(1)).firePropertyChange(CONSTANT, 0.0, isHoriz ? yStart : xStart);
		verify(pcs, times(1)).firePropertyChange(UPDATE_COMPLETE, 0, 1);
	}

	@Before
	public void setup() {
		lineMappingRegion = new SnappedLineMappingRegion();
		lineMappingRegion.usePCS(pcs);
	}

	@Test
	public void testUpdatingFromRoi_SnapHorizontal() {
		double xStart = 10.23;
		double xStop = 34.25;
		double yStart = 11.4;
		double yStop = -12.3;

		// Create ROI
		LinearROI linearROI = new LinearROI(new double[]{xStart, yStart}, new double[]{xStop, yStop});

		// Update region using ROI
		lineMappingRegion.updateFromROI(linearROI);

		// Check values
		assertEquals(X_START, xStart, lineMappingRegion.getxStart(), xStart * 1e-8);
		assertEquals(X_STOP, xStop, lineMappingRegion.getxStop(), xStop * 1e-8);
		assertEquals(Y_START, yStart, lineMappingRegion.getyStart(), yStart * 1e-8);
		assertEquals(Y_STOP, yStart, lineMappingRegion.getyStop(), yStart * 1e-8); // yStart, as line is snapped to horizontal

		// Check ROI has been updated
		assertArrayEquals(new double[]{xStart, yStart}, linearROI.getPoint(), 1e-8);
		assertArrayEquals(new double[]{xStop, yStart}, linearROI.getEndPoint(), 1e-8);
		fullVerify(xStart, xStop, yStart, yStop, true);
	}

	@Test
	public void testUpdatingFromRoi_SnapVertical() {
		double xStart = 10.23;
		double xStop = 14.25;
		double yStart = -12.3;
		double yStop = 11.4;

		// Create ROI
		LinearROI linearROI = new LinearROI(new double[] { xStart, yStart }, new double[] { xStop, yStop });

		// Update region using ROI
		lineMappingRegion.updateFromROI(linearROI);

		// Check values
		assertEquals(X_START, xStart, lineMappingRegion.getxStart(), xStart * 1e-8);
		assertEquals(X_STOP, xStart, lineMappingRegion.getxStop(), xStart * 1e-8); // xStart, as line is snapped to vertical
		assertEquals(Y_START, yStart, lineMappingRegion.getyStart(), yStart * 1e-8);
		assertEquals(Y_STOP, yStop, lineMappingRegion.getyStop(), yStop * 1e-8);

		// Check ROI has been updated
		assertArrayEquals(new double[]{xStart, yStart}, linearROI.getPoint(), 1e-8);
		assertArrayEquals(new double[]{xStart, yStop}, linearROI.getEndPoint(), 1e-8);
		fullVerify(xStart, xStop, yStart, yStop, false);
	}

	@Test
	public void testUpdatingFromRoi_SnapHorizontal_RightToLeftLine() {
		double xStart = 34.25;
		double xStop = 10.23;
		double yStart = -12.3;
		double yStop = 11.4;

		// Create ROI
		LinearROI linearROI = new LinearROI(new double[]{xStart, yStart}, new double[]{xStop, yStop});

		// Update region using ROI
		lineMappingRegion.updateFromROI(linearROI);

		// Check values
		assertEquals(X_START, xStart, lineMappingRegion.getxStart(), xStart * 1e-8);
		assertEquals(X_STOP, xStop, lineMappingRegion.getxStop(), xStop * 1e-8);
		assertEquals(Y_START, yStart, lineMappingRegion.getyStart(), yStart * 1e-8);
		assertEquals(Y_STOP, yStart, lineMappingRegion.getyStop(), yStart * 1e-8); // yStart, as line is snapped to horizontal

		// Check ROI has been updated
		assertArrayEquals(new double[]{xStart, yStart}, linearROI.getPoint(), 1e-8);
		assertArrayEquals(new double[]{xStop, yStart}, linearROI.getEndPoint(), 1e-8);
		fullVerify(xStart, xStop, yStart, yStop, true);
	}

	@Test
	public void testUpdatingFromRoi_SnapVertical_BottomToTop() {
		double xStart = 10.23;
		double xStop = 14.25;
		double yStart = -12.3;
		double yStop = 11.4;

		// Create ROI
		LinearROI linearROI = new LinearROI(new double[] { xStart, yStart }, new double[] { xStop, yStop });

		// Update region using ROI
		lineMappingRegion.updateFromROI(linearROI);

		// Check values
		assertEquals(X_START, xStart, lineMappingRegion.getxStart(), xStart * 1e-8);
		assertEquals(X_STOP, xStart, lineMappingRegion.getxStop(), xStart * 1e-8); // xStart, as line is snapped to vertical
		assertEquals(Y_START, yStart, lineMappingRegion.getyStart(), yStart * 1e-8);
		assertEquals(Y_STOP, yStop, lineMappingRegion.getyStop(), yStop * 1e-8);

		// Check ROI has been updated
		assertArrayEquals(new double[] { xStart, yStart }, linearROI.getPoint(), 1e-8);
		assertArrayEquals(new double[] { xStart, yStop }, linearROI.getEndPoint(), 1e-8);
		fullVerify(xStart, xStop, yStart, yStop, false);
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
		LinearROI roi = new LinearROI();
		lineMappingRegion.updateFromROI(roi);

		final IMappingScanRegionShape copy = lineMappingRegion.copy();

		assertThat(copy, is(equalTo(lineMappingRegion)));
		assertThat(copy, is(not(sameInstance(lineMappingRegion))));
	}

	@Test
	public void testCentre() {
		lineMappingRegion.centre(-5, 12);
		assertThat(lineMappingRegion.getxStart(), is(-5.5));
		assertThat(lineMappingRegion.getxStop(), is(-4.5));
		assertThat(lineMappingRegion.getyStart(), is(12.0));
		assertThat(lineMappingRegion.getyStop(), is(12.0));
		fullVerify(-5.5, -4.5, 12.0, 12.0, true);
	}
}
