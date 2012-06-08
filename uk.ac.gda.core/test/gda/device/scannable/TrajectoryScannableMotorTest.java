/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import gda.device.Motor;
import gda.device.continuouscontroller.TrajectoryMoveController;
import gda.factory.FactoryException;

import org.junit.Before;
import org.junit.Test;

public class TrajectoryScannableMotorTest {

	TrajectoryScannableMotor tsm;
	
	private TrajectoryMoveController controller;

	private Motor motor;

	@Before
	public void setUp() throws FactoryException {
		controller = mock(TrajectoryMoveController.class);
		when(controller.getNumberAxes()).thenReturn(4);
		motor = mock(Motor.class);
		tsm = new TrajectoryScannableMotor();
		tsm.setMotor(motor);
		tsm.setContinuousMoveController(controller);
		tsm.setControllerMotorIndex(2);
		tsm.configure();
	}

	@Test
	public void testTrajectoryScanOperation() throws Exception {
		tsm.setOperatingContinuously(true);
		tsm.asynchronousMoveTo(1.1);
		assertFalse(tsm.isBusy());
		tsm.waitWhileBusy();
		tsm.asynchronousMoveTo(2.1);
		assertFalse(tsm.isBusy());
		tsm.waitWhileBusy();
		
		verify(motor, never()).moveTo(anyDouble());
		verify(controller).addPoint(new Double[] {null, null, 1.1, null});
		verify(controller).addPoint(new Double[] {null, null, 2.1, null});
		tsm.setOperatingContinuously(false);

	}
}
