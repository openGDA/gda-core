/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package gda.beamline.health;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class BeamlineHealthMonitorTest {

	private static final String[] DESCRIPTIONS = { "Component 1", "Component 2", "Server 3" };
	private static final String[] CURRENT_STATES = { "pos1", "42.3", "Running" };
	private static final String[] ERROR_MESSAGES = { "Component 1 not in position", "Component 2 not in position", "Server 3 is not running" };

	private ComponentHealthCondition condition1;
	private ComponentHealthCondition condition2;
	private ComponentHealthCondition condition3;

	private BeamlineHealthMonitor monitor;

	@BeforeEach
	public void setUp() {
		condition1 = createMockCondition(DESCRIPTIONS[0], CURRENT_STATES[0], ERROR_MESSAGES[0]);
		condition2 = createMockCondition(DESCRIPTIONS[1], CURRENT_STATES[1], ERROR_MESSAGES[1]);
		condition3 = createMockCondition(DESCRIPTIONS[2], CURRENT_STATES[2], ERROR_MESSAGES[2]);

		monitor = new BeamlineHealthMonitor();
		monitor.setConditions(Arrays.asList(condition1, condition2, condition3));
	}

	private static ComponentHealthCondition createMockCondition(String description, String currentState, String errorMessage) {
		final ComponentHealthCondition condition = mock(ComponentHealthCondition.class);
		when(condition.getDescription()).thenReturn(description);
		when(condition.getCurrentState()).thenReturn(currentState);
		when(condition.getErrorMessage()).thenReturn(errorMessage);
		when(condition.getHealthState()).thenReturn(BeamlineHealthState.OK);
		return condition;
	}

	@Test
	public void testAllComponentsOk() {
		final BeamlineHealthResult beamlineResult = monitor.getState();

		// Test individual states
		final List<BeamlineHealthComponentResult> componentResults = beamlineResult.getComponentResults();
		for (int i = 0; i < componentResults.size(); i++) {
			final BeamlineHealthComponentResult componentResult = componentResults.get(i);
			assertEquals(DESCRIPTIONS[i], componentResult.getComponentName());
			assertEquals(CURRENT_STATES[i], componentResult.getCurrentState());
			assertEquals(ERROR_MESSAGES[i], componentResult.getErrorMessage());
			assertEquals(BeamlineHealthState.OK, componentResult.getComponentHealthState());
		}

		// Test overall state
		assertEquals(BeamlineHealthState.OK, beamlineResult.getBeamlineHealthState());
		assertEquals("Beamline is ready", beamlineResult.getMessage());
	}

	@Test
	public void testWarning() {
		when(condition2.getHealthState()).thenReturn(BeamlineHealthState.WARNING);

		final BeamlineHealthResult beamlineResult = monitor.getState();
		assertEquals(BeamlineHealthState.WARNING, beamlineResult.getBeamlineHealthState());
		assertEquals(ERROR_MESSAGES[1], beamlineResult.getMessage());
	}

	@Test
	public void testWarningAndError() {
		when(condition2.getHealthState()).thenReturn(BeamlineHealthState.WARNING);
		when(condition3.getHealthState()).thenReturn(BeamlineHealthState.ERROR);

		// An error state takes precedence over a warning
		final BeamlineHealthResult beamlineResult = monitor.getState();
		assertEquals(BeamlineHealthState.ERROR, beamlineResult.getBeamlineHealthState());
		assertEquals(ERROR_MESSAGES[2], beamlineResult.getMessage());
	}

	@Test
	public void testMultipleErrors() {
		when(condition1.getHealthState()).thenReturn(BeamlineHealthState.ERROR);
		when(condition3.getHealthState()).thenReturn(BeamlineHealthState.ERROR);

		// The overall error state is that of the first error encountered
		final BeamlineHealthResult beamlineResult = monitor.getState();
		assertEquals(BeamlineHealthState.ERROR, beamlineResult.getBeamlineHealthState());
		assertEquals(ERROR_MESSAGES[0], beamlineResult.getMessage());
	}

	@Test
	public void testDisabledConditions() {
		// Disabled conditions should not affect the overall beamline health
		when(condition2.getCurrentState()).thenReturn("not checked");
		when(condition2.getHealthState()).thenReturn(BeamlineHealthState.NOT_CHECKED);
		assertEquals(BeamlineHealthState.OK, monitor.getState().getBeamlineHealthState());

		when(condition3.getHealthState()).thenReturn(BeamlineHealthState.ERROR);
		assertEquals(BeamlineHealthState.ERROR, monitor.getState().getBeamlineHealthState());
	}
}
