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

package uk.ac.diamond.daq.beamcondition.test;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.ac.diamond.daq.beamcondition.BeamCondition;
import uk.ac.diamond.daq.beamcondition.BeamConditionBase;

public class BeamConditionBaseTest {
	BeamCondition condition = spy(BeamConditionBase.class);

	@BeforeEach
	public void setup() throws Exception {

		//Thread.sleep(anyLong());

		when(condition.beamOn())
				.thenReturn(false)
				.thenReturn(false)
				.thenReturn(false)
				.thenReturn(true);
	}

	@Test
	public void testNoWaitIfBeamIsOn() throws Exception {
		when(condition.beamOn()).thenReturn(true);
		condition.waitForBeam();
		verify(condition, times(1)).beamOn();
	}

	@Test(expected = InterruptedException.class)
	public void testInterruptionsAreRaised() throws Exception {
		Thread.currentThread().interrupt();
		condition.waitForBeam();
	}

	@Test
	public void testWaitForBeam() throws Exception {
		// This isn't ideal as it actually waits but should be < 1s
		condition.waitForBeam();
		verify(condition, times(4)).beamOn();
	}
}
