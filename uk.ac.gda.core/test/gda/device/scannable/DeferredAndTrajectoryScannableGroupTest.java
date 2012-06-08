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
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.continuouscontroller.TrajectoryMoveController;
import gda.device.scannable.scannablegroup.DeferredAndTrajectoryScannableGroup;
import gda.factory.FactoryException;

import org.junit.Test;

public class DeferredAndTrajectoryScannableGroupTest extends DeferredScannableGroupTest {

	DeferredAndTrajectoryScannableGroup trajgroup;
	
	private TrajectoryMoveController controller;

	
	@Override
	public void setUp() throws DeviceException, FactoryException {
		controller = mock(TrajectoryMoveController.class);
		when(controller.getNumberAxes()).thenReturn(3);
		super.setUp();
	}
	
	@Override
	DeferredAndTrajectoryScannableGroup getGroup(){
		return trajgroup;
	}
	
	@Override
	void createGroup() {
		trajgroup = new DeferredAndTrajectoryScannableGroup();
		getGroup().setGroupMembers(new Scannable[] { rawa, rawb, rawc });
		getGroup().setDeferredControlPoint(mockedControlPoint);
		getGroup().setContinuousMoveController(controller);
	}

	@Test
	public void testTrajectoryScanOperation() throws Exception {
		getGroup().setOperatingContinuously(true);
		getGroup().asynchronousMoveTo(new double[] {1.1, 1.2, 1.3});
		assertFalse(getGroup().isBusy());
		getGroup().waitWhileBusy();
		getGroup().asynchronousMoveTo(new double[] {2.1, 2.2, 2.3});
		assertFalse(getGroup().isBusy());
		getGroup().waitWhileBusy();
		
		verify(rawa, never()).asynchronousMoveTo(anyObject());
		verify(rawb, never()).asynchronousMoveTo(anyObject());
		verify(rawc, never()).asynchronousMoveTo(anyObject());
		verify(mockedControlPoint, never()).setValue(anyDouble());
		verify(controller).addPoint(new Double[] {1.1, 1.2, 1.3});
		verify(controller).addPoint(new Double[] {2.1, 2.2, 2.3});
		getGroup().setOperatingContinuously(false);

	}
}
