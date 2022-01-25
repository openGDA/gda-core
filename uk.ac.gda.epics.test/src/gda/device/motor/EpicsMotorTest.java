/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package gda.device.motor;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import gda.device.MotorException;
import gda.device.MotorStatus;
import gda.device.motor.EpicsMotor.MissedTargetLevel;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.event.MonitorListener;
import gov.aps.jca.event.PutListener;

public class EpicsMotorTest {

	private EpicsMotor motor;
	private EpicsController mockController;
	private EpicsChannelManager mockEpicsChannelManager;
	private Channel mockChannel = mock(Channel.class);
	private Channel mockValChannel = mock(Channel.class);
	private Channel mockDeadbandChannel = mock(Channel.class);
	private Channel mockReadbackChannel = mock(Channel.class);
	private Channel mockPrecisionChannel = mock(Channel.class);
	private String pvName = "test";
	private int precision = 3;

	@Before
	public void setUp() throws FactoryException, CAException, TimeoutException, InterruptedException {
		mockController = mock(EpicsController.class);
		mockEpicsChannelManager = mock(EpicsChannelManager.class);

		when(mockController.cagetInt(eq(mockPrecisionChannel))).thenReturn(precision);

		when(mockEpicsChannelManager.createChannel(anyString(), anyBoolean())).thenReturn(mockChannel);
		when(mockEpicsChannelManager.createChannel(eq(pvName + ".VAL"), anyBoolean())).thenReturn(mockValChannel);
		when(mockEpicsChannelManager.createChannel(eq(pvName + ".RDBD"), anyBoolean())).thenReturn(mockDeadbandChannel);
		when(mockEpicsChannelManager.createChannel(eq(pvName + ".RBV"), any(MonitorListener.class), any(), anyBoolean())).thenReturn(mockReadbackChannel);
		when(mockEpicsChannelManager.createChannel(eq(pvName + ".PREC"), anyBoolean())).thenReturn(mockPrecisionChannel);

		motor = new EpicsMotor(mockController, m -> mockEpicsChannelManager);
		motor.setPvName(pvName);
		motor.createChannelAccess();
	}

	@Test
	public void testWhenMovedToTargetThenNewPositionSentToValPV() throws MotorException, CAException, InterruptedException {
		double testPosition = 10.0;
		motor.moveTo(testPosition);
		verify(mockController, times(1)).caput(eq(mockValChannel), eq(testPosition), any(PutListener.class));
	}

	@Test
	public void testGivenDeadbandofNanWhenCheckDeadbandCalledThenStatusUnchanged() throws TimeoutException, CAException, InterruptedException, MotorException {
		motor.setMissedTargetLevel(MissedTargetLevel.FAULT);
		when(mockController.cagetDouble(eq(mockDeadbandChannel))).thenReturn(Double.NaN);
		MotorStatus status = motor.checkTarget(MotorStatus.BUSY);
		assertEquals(MotorStatus.BUSY, status);
	}

	@Test
	public void testGivenDeadbandofOneAndPositionTargetDifferenceOfTwoWhenCheckDeadbandCalledThenStatusFault()
			throws TimeoutException, CAException, InterruptedException, MotorException {
		motor.setMissedTargetLevel(MissedTargetLevel.FAULT);
		when(mockController.cagetDouble(eq(mockDeadbandChannel))).thenReturn(1.0);
		when(mockController.cagetDouble(eq(mockReadbackChannel))).thenReturn(2.0);
		motor.moveTo(0.0);

		assertEquals(MotorStatus.FAULT, motor.checkTarget(MotorStatus.BUSY));
	}

	@Test
	public void testGivenDeadbandofOneAndPositionTargetDifferenceOfTwoPlusBelowPrecErrorWhenCheckDeadbandCalledThenStatusUnchanged()
			throws TimeoutException, CAException, InterruptedException, MotorException {
		motor.setMissedTargetLevel(MissedTargetLevel.FAULT);
		when(mockController.cagetDouble(eq(mockDeadbandChannel))).thenReturn(1.0);
		when(mockController.cagetDouble(eq(mockReadbackChannel))).thenReturn(1.0009);
		motor.moveTo(0.0);

		assertEquals(MotorStatus.BUSY, motor.checkTarget(MotorStatus.BUSY));
	}

	@Test
	public void testGivenDeadbandofOneAndPositionTargetDifferenceOfTwoPlusJustAbovePrecErrorWhenCheckDeadbandCalledThenStatusFault()
			throws TimeoutException, CAException, InterruptedException, MotorException {
		motor.setMissedTargetLevel(MissedTargetLevel.FAULT);
		when(mockController.cagetDouble(eq(mockDeadbandChannel))).thenReturn(1.0);
		when(mockController.cagetDouble(eq(mockReadbackChannel))).thenReturn(1.0011);
		motor.moveTo(0.0);

		assertEquals(MotorStatus.FAULT, motor.checkTarget(MotorStatus.BUSY));
	}

	@Test
	public void testGivenDeadbandofOneAndPositionTargetDifferenceOfTwoPlusPrecErrorWhenCheckDeadbandCalledThenStatusFault()
			throws TimeoutException, CAException, InterruptedException, MotorException {
		motor.setMissedTargetLevel(MissedTargetLevel.FAULT);
		when(mockController.cagetDouble(eq(mockDeadbandChannel))).thenReturn(1.0);
		when(mockController.cagetDouble(eq(mockReadbackChannel))).thenReturn(1.001);
		motor.moveTo(0.0);

		assertEquals(MotorStatus.FAULT, motor.checkTarget(MotorStatus.BUSY));
	}
}
