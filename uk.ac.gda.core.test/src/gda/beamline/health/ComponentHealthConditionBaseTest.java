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

import org.junit.jupiter.api.BeforeEach;

import gda.factory.FactoryException;
import org.junit.jupiter.api.Test;

public class ComponentHealthConditionBaseTest {

	private static final String COMPONENT_NAME = "Test component";

	private ComponentCondition componentCondition;

	@BeforeEach
	public void setUp() {
		componentCondition = new ComponentCondition();
	}

	@Test
	public void testDefaultErrorMessage() throws FactoryException {
		componentCondition.configure();
		assertEquals(COMPONENT_NAME + " is in an invalid state", componentCondition.getErrorMessage());
	}

	@Test
	public void testExplicitlySetErrorMessage() throws FactoryException {
		final String errorMessage = "The test condition is not satisfied";
		componentCondition.setErrorMessage(errorMessage);
		componentCondition.configure();
		assertEquals(errorMessage, componentCondition.getErrorMessage());
	}

	@Test
	public void testStateCalculationsWhenDisabled() throws FactoryException {
		componentCondition.setEnabled(false);
		componentCondition.configure();
		assertEquals("not checked", componentCondition.getCurrentState());
		assertEquals(BeamlineHealthState.NOT_CHECKED, componentCondition.getHealthState());
	}

	private static class ComponentCondition extends ComponentHealthConditionBase {

		public ComponentCondition() {
			setDescription(COMPONENT_NAME);
		}

		@Override
		public String readCurrentState() {
			return null;
		}

		@Override
		public BeamlineHealthState calculateHealthState() {
			return null;
		}
	}
}
