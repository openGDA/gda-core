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

import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import uk.ac.diamond.daq.beamcondition.BeamCondition;
import uk.ac.diamond.daq.beamcondition.BeamConditionBase;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BeamConditionBase.class)
public class BeamConditionBaseTest {
	BeamCondition condition = spy(BeamConditionBase.class);

	@Before
	public void setup() throws Exception {
		PowerMockito.mockStatic(Thread.class);
		PowerMockito.doNothing().when(Thread.class);
		Thread.sleep(anyLong());

		when(condition.beamOn())
				.thenReturn(false)
				.thenReturn(false)
				.thenReturn(false)
				.thenReturn(false)
				.thenReturn(false)
				.thenReturn(false)
				.thenReturn(false)
				.thenReturn(false)
				.thenReturn(true);
	}

	@Test(timeout = 100) // Shouldn't actually wait
	public void testWaitForBeam() throws Exception {
		condition.waitForBeam();
		verify(condition, times(9)).beamOn();
		PowerMockito.verifyStatic(times(8));
		Thread.sleep(50);
	}

	@Test
	public void testNoWaitIfBeamIsOn() throws Exception {
		when(condition.beamOn()).thenReturn(true);
		condition.waitForBeam();
		verify(condition, times(1)).beamOn();
		PowerMockito.verifyNoMoreInteractions(Thread.class);
	}

	@Test(expected = InterruptedException.class)
	public void testInterruptionsAreRaised() throws Exception {
		PowerMockito.doThrow(new InterruptedException()).when(Thread.class);
		Thread.sleep(anyLong());
		condition.waitForBeam();
	}
}
