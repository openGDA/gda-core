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

package gda.device.continuouscontroller;

import static org.mockito.Matchers.anyDouble;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.anyInt;
import gda.device.BlockingMotor;
import gda.device.ControlPoint;
import gda.device.DeviceException;
import gda.device.MotorException;
import gda.device.MotorStatus;
import gda.device.continuouscontroller.EpicsTrajectoryMoveControllerAdapter;
import gda.device.continuouscontroller.EpicsTrajectoryScanController;
import gda.device.scannable.ContinuouslyScannableViaController;
import gda.device.scannable.ScannableMotor;
import gda.device.scannable.scannablegroup.DeferredAndTrajectoryScannableGroup;
import gda.factory.FactoryException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * An integration test is required because the required functionality has spread between
 * DeferredAndTrajectoryScannableGroup and EpicsTrajectoryMoveControllerAdapter.
 * <p>
 * These classes should be to push the tangles logic down into a controller that contains EpicsMotor and
 * EpicsTrajectoryMoveControllerAdapter controller.
 */
@RunWith(MockitoJUnitRunner.class)
public class EpicsTrajectoryMoveControllerAdapterIntegrationTest {


	@Mock
	ControlPoint mockedControlPoint;
	@Mock
	BlockingMotor motora; // EpicsMotor implements this
	@Mock
	BlockingMotor motorb;
	@Mock
	BlockingMotor motorc;
	
	ScannableMotor scna;
	ScannableMotor scnb;
	ScannableMotor scnc;

	private EpicsTrajectoryMoveControllerAdapter controller;
	private EpicsTrajectoryScanController mockedController;

	private DeferredAndTrajectoryScannableGroup trajgroup;

	private ScannableMotor scnaNoOffset;

	private ScannableMotor scnbNoOffset;

	private ScannableMotor scncNoOffset;

	private DeferredAndTrajectoryScannableGroup trajgroupNoOffset;
	
	@Before
	public void setUp() throws DeviceException, FactoryException {

		scna = mockScannableMotor(motora, "a");
		scnb = mockScannableMotor(motorb, "b");
		scnc = mockScannableMotor(motorc, "c");
		scnaNoOffset = mockScannableMotor(motora, "a");
		scnbNoOffset = mockScannableMotor(motorb, "b");
		scncNoOffset = mockScannableMotor(motorc, "c");
		mockedControlPoint = mock(ControlPoint.class);
		when(mockedControlPoint.getPosition()).thenReturn(0);

		mockedController = mock(EpicsTrajectoryScanController.class);
		when(mockedController.getMaximumNumberMotors()).thenReturn(8);
		controller = new EpicsTrajectoryMoveControllerAdapter();
		controller.setController(mockedController);
		controller.setAxisMotorOrder(new int[] {4, 3, 2});
		controller.setAxisNames(new String[] {"a", "b", "c" });

		trajgroupNoOffset = new DeferredAndTrajectoryScannableGroup();
		trajgroupNoOffset.setGroupMembers(new ScannableMotor[] { scnaNoOffset, scnbNoOffset, scncNoOffset });
		trajgroupNoOffset.setDeferredControlPoint(mockedControlPoint);
		trajgroupNoOffset.setContinuousMoveController(controller);
		trajgroupNoOffset.configure();

		trajgroup = new DeferredAndTrajectoryScannableGroup();
		trajgroup.setGroupMembers(new ScannableMotor[] { scna, scnb, scnc });
		trajgroup.setDeferredControlPoint(mockedControlPoint);
		trajgroup.setContinuousMoveController(controller);
		trajgroup.configure();
		
		controller.setScannableForMovingGroupToStart(trajgroupNoOffset);
	}

	private ScannableMotor mockScannableMotor(BlockingMotor motor, String name) throws FactoryException, MotorException {
		when(motor.getMinPosition()).thenReturn(-100.);
		when(motor.getMaxPosition()).thenReturn(100.);
		when(motor.getStatus()).thenReturn(MotorStatus.READY);
		when(motor.getName()).thenReturn("motor" + name);
		
		ScannableMotor scn = new ScannableMotor();
		scn.setName("scn" + name);
		scn.setMotor(motor);
		scn.configure();
		
		return scn;
	}

	@Test
	public void testPrepareForCollectionMovesGroupToStartPositionTrajectoryScanOperation() throws Exception {
		trajgroup.setOperatingContinuously(true);
		trajgroup.asynchronousMoveTo(new double[] { 1.1, 1.2, 1.3 });
		trajgroup.asynchronousMoveTo(new double[] { 2.1, 2.2, 2.3 });

		verify(motora, never()).moveTo(anyDouble());
		verify(motorb, never()).moveTo(anyDouble());
		verify(motorc, never()).moveTo(anyDouble());
		verify(mockedControlPoint, never()).setValue(anyDouble());
		
		when(mockedController.getNumberOfElements()).thenReturn(2);
		when(mockedController.getStartPulseElement()).thenReturn(1);
		when(mockedController.getStopPulseElement()).thenReturn(2);
		when(mockedController.getMTraj(anyInt())).thenReturn(new double[]{});
		when(mockedController.getTrajectoryTime()).thenReturn(1.);
		when(mockedController.isMMove(2)).thenReturn(true);
		when(mockedController.isMMove(3)).thenReturn(true);
		when(mockedController.isMMove(4)).thenReturn(true);
		
		controller.prepareForMove();
		InOrder inOrder = inOrder(mockedControlPoint, motora, motorb, motorc);
		inOrder.verify(mockedControlPoint).setValue(1);
		inOrder.verify(motora).moveTo(1.1);
		inOrder.verify(motorb).moveTo(1.2);
		inOrder.verify(motorc).moveTo(1.3);
		inOrder.verify(mockedControlPoint).setValue(0);
	}

	@Test
	public void testPrepareForCollectionMovesGroupToStartPositionTrajectoryScanOperationWithOffset() throws Exception {
		scna.setOffset(10);
		trajgroup.setOperatingContinuously(true);
		trajgroup.asynchronousMoveTo(new double[] { 1.1, 1.2, 1.3 });
		trajgroup.asynchronousMoveTo(new double[] { 1.2, 2.2, 2.3 });
		
		verify(motora, never()).moveTo(anyDouble());
		verify(motorb, never()).moveTo(anyDouble());
		verify(motorc, never()).moveTo(anyDouble());
		verify(mockedControlPoint, never()).setValue(anyDouble());
		
		when(mockedController.getNumberOfElements()).thenReturn(2);
		when(mockedController.getStartPulseElement()).thenReturn(1);
		when(mockedController.getStopPulseElement()).thenReturn(2);
		when(mockedController.getMTraj(anyInt())).thenReturn(new double[]{});
		when(mockedController.getTrajectoryTime()).thenReturn(1.);
		when(mockedController.isMMove(2)).thenReturn(true);
		when(mockedController.isMMove(3)).thenReturn(true);
		when(mockedController.isMMove(4)).thenReturn(true);

		controller.prepareForMove();
		
		InOrder inOrder = inOrder(mockedControlPoint, motora, motorb, motorc);
		inOrder.verify(mockedControlPoint).setValue(1);
		inOrder.verify(motora).moveTo(-8.9);
		inOrder.verify(motorb).moveTo(1.2);
		inOrder.verify(motorc).moveTo(1.3);
		inOrder.verify(mockedControlPoint).setValue(0);
	}
	
	@Test(expected=IllegalStateException.class)
	public void testPrepareForCollectionMovesGroupToStartPositionTrajectoryScanOperationWithOffsetFails() throws Exception {
		
		controller.setScannableForMovingGroupToStart(trajgroup);
		testPrepareForCollectionMovesGroupToStartPositionTrajectoryScanOperationWithOffset();
	}

	
	@Test
	public void testPrepareForCollectionMovesGroupToStartPositionTrajectoryScanOperationWithOffsetViaElement() throws Exception {
		scna.setOffset(10);
		ContinuouslyScannableViaController wrapperaa = (ContinuouslyScannableViaController) trajgroup.getGroupMembers().get(0);
		ContinuouslyScannableViaController wrapperac = (ContinuouslyScannableViaController) trajgroup.getGroupMembers().get(2);
		
		wrapperaa.setOperatingContinuously(true);
		wrapperac.setOperatingContinuously(true);
		wrapperaa.atLevelMoveStart();
		wrapperac.atLevelMoveStart();

		wrapperaa.asynchronousMoveTo(1.1);
		wrapperac.asynchronousMoveTo(1.3);
		wrapperaa.atLevelMoveStart();
		wrapperac.atLevelMoveStart();
		wrapperaa.asynchronousMoveTo(2.1);
		wrapperac.asynchronousMoveTo(2.3);
		
		verify(motora, never()).moveTo(anyDouble());
		verify(motorb, never()).moveTo(anyDouble());
		verify(motorc, never()).moveTo(anyDouble());
		verify(mockedControlPoint, never()).setValue(anyDouble());
		
		when(mockedController.getNumberOfElements()).thenReturn(2);
		when(mockedController.getStartPulseElement()).thenReturn(1);
		when(mockedController.getStopPulseElement()).thenReturn(2);
		when(mockedController.getMTraj(anyInt())).thenReturn(new double[]{});
		when(mockedController.getTrajectoryTime()).thenReturn(1.);
		when(mockedController.isMMove(2)).thenReturn(true);
		when(mockedController.isMMove(4)).thenReturn(true);
		
		controller.prepareForMove();
		InOrder inOrder = inOrder(mockedControlPoint, motora, motorb, motorc);
		inOrder.verify(mockedControlPoint).setValue(1);
		inOrder.verify(motora).moveTo(-8.9);
		inOrder.verify(motorc).moveTo(1.3);
		inOrder.verify(mockedControlPoint).setValue(0);
	}

}
