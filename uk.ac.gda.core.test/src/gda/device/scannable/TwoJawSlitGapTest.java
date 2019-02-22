/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package gda.device.scannable;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jscience.physics.quantities.Length;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import gda.device.DeviceException;
import gda.device.ScannableMotionUnits;

/**
 * This tests the functions that are different from {@link TwoJawSlitPosition}, namely the calculation of
 * <ul>
 * <li>limits</li/>
 * <li>position</li/>
 * <li>target positions</li/>
 * </ul>
 */
public class TwoJawSlitGapTest {
	// Tolerance allowed for inaccuracies in floating point calculations
	private static final double FP_TOLERANCE = 0.000001;

	private static final String FIRST_JAW_NAME = "firstJaw";
	private static final String SECOND_JAW_NAME = "secondJaw";
	private static final String SLIT_POSITION_NAME = "slitPosition";

	private static final String HARDWARE_UNITS = "mm";
	private static final String USER_UNITS = "µm";

	private static final double FIRST_JAW_LOWER_LIMIT = -5.0;
	private static final double FIRST_JAW_UPPER_LIMIT = 10.0;
	private static final double SECOND_JAW_LOWER_LIMIT = -7.0;
	private static final double SECOND_JAW_UPPER_LIMIT = 5.0;

	private static ScannableMotionUnits firstJaw;
	private static ScannableMotionUnits secondJaw;

	private TwoJawSlitGap slitGap;

	@Before
	public void setUp() throws Exception {
		firstJaw = createJaw(FIRST_JAW_NAME, FIRST_JAW_LOWER_LIMIT, FIRST_JAW_UPPER_LIMIT);
		secondJaw = createJaw(SECOND_JAW_NAME, SECOND_JAW_LOWER_LIMIT, SECOND_JAW_UPPER_LIMIT);

		slitGap = new TwoJawSlitGap();
		slitGap.setName(SLIT_POSITION_NAME);
		slitGap.setFirstJaw(firstJaw);
		slitGap.setSecondJaw(secondJaw);
		slitGap.setTolerances(new Double[] { FP_TOLERANCE, FP_TOLERANCE });
		slitGap.configure();
	}

	private static ScannableMotionUnits createJaw(String name, double lowerLimit, double upperLimit) throws DeviceException {
		final ScannableMotionUnits jaw = mock(ScannableMotionUnits.class);
		when(jaw.getName()).thenReturn(name);
		when(jaw.getHardwareUnitString()).thenReturn(HARDWARE_UNITS);
		when(jaw.getUserUnits()).thenReturn(USER_UNITS);
		when(jaw.getLowerGdaLimits()).thenReturn(new Double[] { lowerLimit });
		when(jaw.getUpperGdaLimits()).thenReturn(new Double[] { upperLimit });
		when(jaw.getPosition()).thenReturn(0.0);
		return jaw;
	}

	@Test
	public void testInitialState() {
		final String[] inputNames = slitGap.getInputNames();
		assertEquals(1, inputNames.length);
		assertEquals(SLIT_POSITION_NAME, inputNames[0]);

		// Jaws' hardware units are ignored
		assertEquals(USER_UNITS, slitGap.getHardwareUnitString());
		assertEquals(USER_UNITS, slitGap.getUserUnits());

		final Double[] lowerLimits = slitGap.getLowerGdaLimits();
		assertEquals(1, lowerLimits.length);
		assertEquals(0.0, lowerLimits[0], FP_TOLERANCE); // minimum never goes below zero

		final Double[] upperLimits = slitGap.getUpperGdaLimits();
		assertEquals(1, upperLimits.length);
		assertEquals(FIRST_JAW_UPPER_LIMIT - SECOND_JAW_LOWER_LIMIT, upperLimits[0], FP_TOLERANCE);
	}

	/**
	 * Test {@link TwoJawSlitGap#moveTo(Object)} assuming the current motor positions correspond to the beam centre
	 * <p>
	 * In theory, the beam centre can be read from an XML file, but there is currently no example of this on any
	 * beamline, so it is not tested here.
	 *
	 * @throws DeviceException
	 */
	@Test
	public void testMoveTo() throws DeviceException {
		final double firstJawInitialPosition = 0.5;
		final double secondJawInitialPosition = -0.3;

		final double targetPosition = 1.2;
		final double currentGap = Math.abs(firstJawInitialPosition - secondJawInitialPosition);
		final double delta = (targetPosition - currentGap) / 2.0;
		final double firstJawExpectedTarget = firstJawInitialPosition + delta;
		final double secondJawExpectedTarget = secondJawInitialPosition - delta;

		when(firstJaw.getPosition()).thenReturn(firstJawInitialPosition);
		when(secondJaw.getPosition()).thenReturn(secondJawInitialPosition);

		slitGap.setNumberTries(1);
		slitGap.moveTo(targetPosition);

		verifyMove(firstJaw, firstJawExpectedTarget);
		verifyMove(secondJaw, secondJawExpectedTarget);
	}

	// Verify that the jaw has been told to move to the expected position
	private void verifyMove(ScannableMotionUnits jaw, double expectedPosition) throws DeviceException {
		final ArgumentCaptor<Object> positionCaptor = ArgumentCaptor.forClass(Object.class);
		verify(jaw).asynchronousMoveTo(positionCaptor.capture());
		assertEquals(expectedPosition, ((Length) positionCaptor.getValue()).getAmount(), FP_TOLERANCE);
	}

	@Test
	public void testRawGetPosition() throws DeviceException {
		final double firstJawInitialPosition = 0.5;
		final double secondJawInitialPosition = -0.3;

		when(firstJaw.getPosition()).thenReturn(firstJawInitialPosition);
		when(secondJaw.getPosition()).thenReturn(secondJawInitialPosition);

		final double currentGap = Math.abs(firstJawInitialPosition - secondJawInitialPosition);
		assertEquals(currentGap, (double) slitGap.rawGetPosition(), FP_TOLERANCE);
	}
}
