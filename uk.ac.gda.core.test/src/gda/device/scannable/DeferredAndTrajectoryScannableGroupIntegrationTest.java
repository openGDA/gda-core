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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import gda.device.ControlPoint;
import gda.device.DeviceException;
import gda.device.Motor;
import gda.device.MotorException;
import gda.device.MotorStatus;
import gda.device.Scannable;
import gda.device.continuouscontroller.TrajectoryMoveController;
import gda.device.scannable.scannablegroup.DeferredAndTrajectoryScannableGroup;
import gda.factory.FactoryException;

/**
 * An integration test is required because the required functionality has spread between
 * DeferredAndTrajectoryScannableGroup and EpicsTrajectoryMoveControllerAdapter.
 * <p>
 * These classes should be to push the tangles logic down into a controller that contains EpicsMotor and
 * EpicsTrajectoryMoveControllerAdapter controller.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DeferredAndTrajectoryScannableGroupIntegrationTest {

	DeferredAndTrajectoryScannableGroup trajgroup;

	private TrajectoryMoveController controller;

	@Mock
	ControlPoint mockedControlPoint;
	@Mock
	Motor motora; // EpicsMotor implements this
	@Mock
	Motor motorb;
	@Mock
	Motor motorc;
	ScannableMotor scna;
	ScannableMotor scnb;
	ScannableMotor scnc;

	@BeforeEach
	public void setUp() throws DeviceException, FactoryException {
		controller = mock(TrajectoryMoveController.class);
		when(controller.getNumberAxes()).thenReturn(3);
		scna = mockScannableMotor(motora, "a");
		scnb = mockScannableMotor(motorb, "b");
		scnc = mockScannableMotor(motorc, "c");
		mockedControlPoint = mock(ControlPoint.class);

		createGroup();
		trajgroup.configure();
	}

	private ScannableMotor mockScannableMotor(Motor motor, String name) throws FactoryException, MotorException {
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

	void createGroup() throws FactoryException {
		trajgroup = new DeferredAndTrajectoryScannableGroup();
		trajgroup.setGroupMembersWithArray(new ScannableMotor[] { scna, scnb, scnc });
		trajgroup.setDeferredControlPoint(mockedControlPoint);
		trajgroup.setContinuousMoveController(controller);
	}

	@Test
	public void testAtCommandFailure() throws DeviceException {

		InOrder inOrder = inOrder(mockedControlPoint, motora, motorb, motorc);
		trajgroup.atCommandFailure();
		inOrder.verify(motora).stop();
		inOrder.verify(motorb).stop();
		inOrder.verify(motorc).stop();
		inOrder.verify(mockedControlPoint).setValue(0);
	}

	@Test
	public void testStop() throws DeviceException {
		InOrder inOrder = inOrder(mockedControlPoint, motora, motorb, motorc);
		trajgroup.stop();
		inOrder.verify(motora).stop();
		inOrder.verify(motorb).stop();
		inOrder.verify(motorc).stop();
		inOrder.verify(mockedControlPoint).setValue(0);
	}

	@Test
	public void testAsynchronousMoveTo() throws DeviceException {
		InOrder inOrder = inOrder(mockedControlPoint, motora, motorb, motorc);
		trajgroup.asynchronousMoveTo(new Double[] { 1., 2., 3. });
		inOrder.verify(mockedControlPoint).setValue(1);
		inOrder.verify(motora).moveTo(1.);
		inOrder.verify(motorb).moveTo(2.);
		inOrder.verify(motorc).moveTo(3.);
		inOrder.verify(mockedControlPoint).setValue(0);
	}

	@Test
	public void testGetPosition() throws DeviceException {
		when(motora.getPosition()).thenReturn(1.);
		when(motorb.getPosition()).thenReturn(2.);
		when(motorc.getPosition()).thenReturn(3.);
		Object pos = trajgroup.getPosition();
		assertArrayEquals(new Object[] { 1., 2., 3. }, (Object[]) pos);
	}

	@Test
	public void testSetDefer() throws DeviceException {
		trajgroup.setDefer(false);
		verify(mockedControlPoint).setValue(0.);
		trajgroup.setDefer(true);
		verify(mockedControlPoint).setValue(1.);
	}

	@Test
	public void testAsynchronousMoveToViaElements() throws DeviceException {
		Scannable c = (trajgroup.getGroupMembers().get(2));
		Scannable a = (trajgroup.getGroupMembers().get(0));
		InOrder inOrder = inOrder(mockedControlPoint, motora, motorb, motorc);

		a.atLevelMoveStart();
		c.atLevelMoveStart();
		a.asynchronousMoveTo(1.);
		c.asynchronousMoveTo(3.);

		inOrder.verify(mockedControlPoint).setValue(1.);
		inOrder.verify(motora).moveTo(1.);
		inOrder.verify(motorc).moveTo(3.);
		inOrder.verify(mockedControlPoint).setValue(0.);
	}

	@Test
	public void testIsBusy() throws DeviceException {
		when(motora.getStatus()).thenReturn(MotorStatus.READY);
		when(motorb.getStatus()).thenReturn(MotorStatus.BUSY);
		assertTrue(trajgroup.isBusy());
	}

	@Test
	public void testTrajectoryScanOperation() throws Exception {
		trajgroup.setOperatingContinuously(true);
		trajgroup.asynchronousMoveTo(new double[] { 1.1, 1.2, 1.3 });
		assertFalse(trajgroup.isBusy());
		trajgroup.waitWhileBusy();
		trajgroup.asynchronousMoveTo(new double[] { 2.1, 2.2, 2.3 });
		assertFalse(trajgroup.isBusy());
		trajgroup.waitWhileBusy();

		verify(motora, never()).moveTo(anyDouble());
		verify(motorb, never()).moveTo(anyDouble());
		verify(motorc, never()).moveTo(anyDouble());
		verify(mockedControlPoint, never()).setValue(anyDouble());
		verify(controller).addPoint(new Double[] { 1.1, 1.2, 1.3 });
		verify(controller).addPoint(new Double[] { 2.1, 2.2, 2.3 });
		trajgroup.setOperatingContinuously(false);
	}

	@Test
	public void testTrajectoryScanOperationViaElement() throws Exception {
		ContinuouslyScannableViaController wrapperaa = (ContinuouslyScannableViaController) trajgroup.getGroupMembers().get(0);
		ContinuouslyScannableViaController wrapperac = (ContinuouslyScannableViaController) trajgroup.getGroupMembers().get(2);

		wrapperaa.setOperatingContinuously(true);
		wrapperac.setOperatingContinuously(true);
		wrapperaa.atLevelMoveStart();
		wrapperac.atLevelMoveStart();

		wrapperaa.asynchronousMoveTo(1.1);
		wrapperac.asynchronousMoveTo(1.3);
		assertFalse(trajgroup.isBusy());
		trajgroup.waitWhileBusy();

		verify(motora, never()).moveTo(anyDouble());
		verify(motorb, never()).moveTo(anyDouble());
		verify(motorc, never()).moveTo(anyDouble());
		verify(mockedControlPoint, never()).setValue(anyDouble());
		verify(controller).addPoint(new Double[] { 1.1, null, 1.3 });
	}

	@Test
	public void testOffsetsAppliedDuringTrajectoryScan() throws Exception {
		scna.setOffset(10);
		scnb.setOffset(20);
		scnc.setOffset(30);
		trajgroup.setOperatingContinuously(true);
		trajgroup.asynchronousMoveTo(new double[] { 1.1, 1.2, 1.3 });

		verify(controller).addPoint(new Double[] { -8.9, -18.8, -28.7 });
	}
	@Test
	public void testOffsetsAppliedDuringTrajectoryScanViaElement() throws Exception {
		scna.setOffset(10);
		Scannable wrapperaa = trajgroup.getGroupMembers().get(0);

		((ContinuouslyScannableViaController) wrapperaa).setOperatingContinuously(true);
		wrapperaa.atLevelMoveStart();
		wrapperaa.asynchronousMoveTo(1.1);

		verify(controller).addPoint(new Double[] { -8.9, null, null });
	}

	@Test
	public void testTrajectoryScanOperationViolatedScannableLimit() throws Exception {
		scna.setUpperGdaLimits(0.);
		trajgroup.setOperatingContinuously(true);
		assertThrows(DeviceException.class, () -> trajgroup.asynchronousMoveTo(new double[] { 1.1, 1.2, 1.3 }));
	}

	@Test
	public void testTrajectoryScanOperationViolatedScannableLimit2() throws Exception {
		scnb.setUpperGdaLimits(0.);
		trajgroup.setOperatingContinuously(true);
		assertThrows(DeviceException.class, () -> trajgroup.asynchronousMoveTo(new double[] { 1.1, 1.2, 1.3 }));
	}

	@Test
	public void testTrajectoryScanOperationViolatesMotorLimit() throws Exception {
		when(motora.getMaxPosition()).thenReturn(0.);
		trajgroup.setOperatingContinuously(true);
		assertThrows(DeviceException.class, () -> trajgroup.asynchronousMoveTo(new double[] { 1.1, 1.2, 1.3 }));
	}

	@Test
	public void testTrajectoryScanOperationViolatedScannableLimitViaElement() throws Exception {
		scna.setUpperGdaLimits(0.);

		ContinuouslyScannableViaController wrapperaa = (ContinuouslyScannableViaController) trajgroup.getGroupMembers().get(0);
		wrapperaa.setOperatingContinuously(true);
		wrapperaa.atLevelMoveStart();
		assertThrows(DeviceException.class, () -> wrapperaa.asynchronousMoveTo(1.1));
	}

	@Test
	public void testTrajectoryScanOperationViolatedScannableLimitViaElement2() throws Exception {
		scnb.setUpperGdaLimits(0.);

		ContinuouslyScannableViaController wrapperab = (ContinuouslyScannableViaController) trajgroup.getGroupMembers().get(1);
		wrapperab.setOperatingContinuously(true);
		wrapperab.atLevelMoveStart();
		assertThrows(DeviceException.class, () -> wrapperab.asynchronousMoveTo(1.1));
	}

	@Test
	public void testTrajectoryScanOperationViolatesMotorLimitViaElement() throws Exception {
		scna.setUpperGdaLimits(0.);

		ContinuouslyScannableViaController wrapperaa = (ContinuouslyScannableViaController) trajgroup.getGroupMembers().get(0);
		wrapperaa.setOperatingContinuously(true);
		wrapperaa.atLevelMoveStart();
		assertThrows(DeviceException.class, () -> wrapperaa.asynchronousMoveTo(1.1));
	}
}
