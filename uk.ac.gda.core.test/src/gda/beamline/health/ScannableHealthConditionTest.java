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

import org.junit.Before;
import org.junit.Test;

import gda.factory.FactoryException;
import uk.ac.diamond.daq.beamcondition.BeamCondition;

public class ScannableHealthConditionTest {

	private static final String SCANNABLE_NAME = "Test scannable";

	private ScannableCondition scannableCondition;

	@Before
	public void setUp() throws FactoryException {
		scannableCondition = new ScannableCondition();
		scannableCondition.configure();
	}

	@Test
	public void testDefaultErrorMessage() {
		assertEquals(SCANNABLE_NAME + " is not in a valid position", scannableCondition.getErrorMessage());
	}

	@Test
	public void testGetHealthState() {
		final BeamCondition beamCondition = mock(BeamCondition.class);
		scannableCondition.setCondition(beamCondition);

		// Condition satisfied
		when(beamCondition.beamOn()).thenReturn(true);
		assertEquals(BeamlineHealthState.OK, scannableCondition.getHealthState());

		// Condition not satisfied, but not critical
		when(beamCondition.beamOn()).thenReturn(false);
		scannableCondition.setCritical(false);
		assertEquals(BeamlineHealthState.WARNING, scannableCondition.getHealthState());

		// Condition not satisfied and is critical
		scannableCondition.setCritical(true);
		assertEquals(BeamlineHealthState.ERROR, scannableCondition.getHealthState());
	}

	private static class ScannableCondition extends ScannableHealthCondition {

		private BeamCondition beamCondition;

		public ScannableCondition() {
			setDescription(SCANNABLE_NAME);
		}

		@Override
		public String readCurrentState() {
			return null;
		}

		public void setCondition(BeamCondition beamCondition) {
			this.beamCondition = beamCondition;
		}

		@Override
		protected BeamCondition getCondition() {
			return beamCondition;
		}
	}
}
