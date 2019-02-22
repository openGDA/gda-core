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
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.jscience.physics.quantities.Length;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import gda.device.DeviceException;
import gda.device.ScannableMotionUnits;
import gda.observable.IObserver;

public class TwoJawSlitPositionTest {
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

	private TwoJawSlitPosition slitPosition;

	@Before
	public void setUp() throws Exception {
		firstJaw = createJaw(FIRST_JAW_NAME, FIRST_JAW_LOWER_LIMIT, FIRST_JAW_UPPER_LIMIT);
		secondJaw = createJaw(SECOND_JAW_NAME, SECOND_JAW_LOWER_LIMIT, SECOND_JAW_UPPER_LIMIT);

		slitPosition = new TwoJawSlitPosition();
		slitPosition.setName(SLIT_POSITION_NAME);
		slitPosition.setFirstJaw(firstJaw);
		slitPosition.setSecondJaw(secondJaw);
		slitPosition.setTolerances(new Double[] { FP_TOLERANCE, FP_TOLERANCE });
		slitPosition.configure();
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
		final String[] inputNames = slitPosition.getInputNames();
		assertEquals(1, inputNames.length);
		assertEquals(SLIT_POSITION_NAME, inputNames[0]);

		// Jaws' hardware units are ignored
		assertEquals(USER_UNITS, slitPosition.getHardwareUnitString());
		assertEquals(USER_UNITS, slitPosition.getUserUnits());

		final Double[] lowerLimits = slitPosition.getLowerGdaLimits();
		assertEquals(1, lowerLimits.length);
		assertEquals((FIRST_JAW_LOWER_LIMIT + SECOND_JAW_LOWER_LIMIT) / 2.0, lowerLimits[0], FP_TOLERANCE);

		final Double[] upperLimits = slitPosition.getUpperGdaLimits();
		assertEquals(1, upperLimits.length);
		assertEquals((FIRST_JAW_UPPER_LIMIT + SECOND_JAW_UPPER_LIMIT) / 2.0, upperLimits[0], FP_TOLERANCE);
	}

	@Test
	public void testMoveToOneTry() throws DeviceException {
		// Test moveTo() when only one attempt is permitted
		final double firstJawInitialPosition = 0.5;
		final double secondJawInitialPosition = -0.3;

		final double targetPosition = 1.2;
		final double currentGap = Math.abs(firstJawInitialPosition - secondJawInitialPosition);
		final double firstJawExpectedTarget = targetPosition + (currentGap / 2.0);
		final double secondJawExpectedTarget = targetPosition - (currentGap / 2.0);

		when(firstJaw.getPosition()).thenReturn(firstJawInitialPosition);
		when(secondJaw.getPosition()).thenReturn(secondJawInitialPosition);

		slitPosition.setNumberTries(1);
		slitPosition.moveTo(targetPosition);

		verifyMove(firstJaw, firstJawExpectedTarget);
		verifyMove(secondJaw, secondJawExpectedTarget);
	}

	@Test
	public void testMoveToMultipleTries() throws DeviceException {
		// If numberTries is set > 1, the moveTo() goes through a completely different code path
		// from the "one attempt" case
		final double firstJawInitialPosition = 0.5;
		final double secondJawInitialPosition = -0.3;

		final double targetPosition = 1.2;
		final double currentGap = Math.abs(firstJawInitialPosition - secondJawInitialPosition);
		final double firstJawExpectedTarget = targetPosition + (currentGap / 2.0);
		final double secondJawExpectedTarget = targetPosition - (currentGap / 2.0);

		// The code gets the position three times before making the move, so the jaws
		// must return the initial position three times
		when(firstJaw.getPosition()).thenReturn(firstJawInitialPosition)
			.thenReturn(firstJawInitialPosition)
			.thenReturn(firstJawInitialPosition)
			.thenReturn(firstJawExpectedTarget);
		when(secondJaw.getPosition()).thenReturn(secondJawInitialPosition)
			.thenReturn(secondJawInitialPosition)
			.thenReturn(secondJawInitialPosition)
			.thenReturn(secondJawExpectedTarget);

		slitPosition.setNumberTries(2);
		slitPosition.moveTo(targetPosition);

		verifyMove(firstJaw, firstJawExpectedTarget);
		verifyMove(secondJaw, secondJawExpectedTarget);
	}

	@Test(expected = DeviceException.class)
	public void testMoveToMultipleTriesFails() throws DeviceException {
		final double firstJawInitialPosition = 0.5;
		final double secondJawInitialPosition = -0.3;
		final double targetPosition = 1.2;

		// Jaws stay in their initial positions
		when(firstJaw.getPosition()).thenReturn(firstJawInitialPosition);
		when(secondJaw.getPosition()).thenReturn(secondJawInitialPosition);

		slitPosition.setNumberTries(2);
		slitPosition.moveTo(targetPosition);
	}

	// Verify that the jaw has been told to move to the expected position
	private void verifyMove(ScannableMotionUnits jaw, double expectedPosition) throws DeviceException {
		final ArgumentCaptor<Object> positionCaptor = ArgumentCaptor.forClass(Object.class);
		verify(jaw).asynchronousMoveTo(positionCaptor.capture());
		assertEquals(expectedPosition, ((Length) positionCaptor.getValue()).getAmount(), FP_TOLERANCE);
	}

	@Test
	public void testCheckPositionValid() throws DeviceException {
		final String FIRST_INVALID = "first jaw invalid";
		final String SECOND_INVALID = "second jaw invalid";

		// Both jaw positions valid
		when(firstJaw.checkPositionValid(any(Object.class))).thenReturn(null);
		when(secondJaw.checkPositionValid(any(Object.class))).thenReturn(null);
		assertNull(slitPosition.checkPositionValid(1.5));

		// First jaw position invalid
		when(firstJaw.checkPositionValid(any(Object.class))).thenReturn(FIRST_INVALID);
		assertEquals(FIRST_INVALID, slitPosition.checkPositionValid(1.5));

		// Both jaw positions invalid - function checks first jaw and & reports it invalid
		when(secondJaw.checkPositionValid(any(Object.class))).thenReturn(SECOND_INVALID);
		assertEquals(FIRST_INVALID, slitPosition.checkPositionValid(1.5));

		// Second jaw position (only) invalid
		when(firstJaw.checkPositionValid(any(Object.class))).thenReturn(null);
		assertEquals(SECOND_INVALID, slitPosition.checkPositionValid(1.5));
	}

	@Test
	public void testRawGetPosition() throws DeviceException {
		final double firstJawInitialPosition = 0.5;
		final double secondJawInitialPosition = -0.3;

		when(firstJaw.getPosition()).thenReturn(firstJawInitialPosition);
		when(secondJaw.getPosition()).thenReturn(secondJawInitialPosition);

		final double expectedPosition = (firstJawInitialPosition + secondJawInitialPosition) / 2.0;
		assertEquals(expectedPosition, (double) slitPosition.rawGetPosition(), FP_TOLERANCE);
	}

	@Test
	public void testUpdateStatusIdle() {
		final IObserver observer = mock(IObserver.class);
		slitPosition.addIObserver(observer);
		slitPosition.update(null, ScannableStatus.IDLE);

		// Idle status is reported to observers
		final ArgumentCaptor<Object> sourceCaptor = ArgumentCaptor.forClass(Object.class);
		final ArgumentCaptor<Object> argumentCaptor = ArgumentCaptor.forClass(Object.class);
		verify(observer).update(sourceCaptor.capture(), argumentCaptor.capture());
		assertEquals(slitPosition, sourceCaptor.getValue());
		assertEquals(ScannableStatus.IDLE, argumentCaptor.getValue());
	}

	@Test
	public void testUpdateOtherStatus() {
		final IObserver observer = mock(IObserver.class);
		slitPosition.addIObserver(observer);
		slitPosition.update(null, "Status update");

		// Other status updates are not passed on to observers
		verifyNoMoreInteractions(observer);
	}
}
