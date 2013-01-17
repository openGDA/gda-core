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

import static org.junit.Assert.*;

import java.util.List;

import gda.device.DeviceException;
import gda.factory.FactoryException;
import gda.util.OutOfRangeException;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import junitx.framework.ArrayAssert;
import static org.mockito.Mockito.*;

public class EpicsTrajectoryMoveControllerAdapterTest {

	private final class SleepStub implements Answer<Void> {
		@Override
		public Void answer(InvocationOnMock invocation) throws Throwable {
			Thread.sleep(2000);
			return null;
		}
	}

	private EpicsTrajectoryMoveControllerAdapter adapter;
	private EpicsTrajectoryScanControllerDev812 mockedController;

	@Before
	public void setUp() {
		mockedController = mock(EpicsTrajectoryScanControllerDev812.class);
		when(mockedController.getMaximumNumberMotors()).thenReturn(8);
		adapter = new EpicsTrajectoryMoveControllerAdapter();
		adapter.setController(mockedController);
		adapter.setAxisMotorOrder(new int[] {4, 3, 2, 1});
		adapter.setAxisNames(new String[] {"a", "b", "c", "d" });
	}

	@Test
	public void testConfigure() throws FactoryException {
		adapter.configure();
	}

	@Test
	public void testMotorFromIndex() {
		adapter.setAxisMotorOrder(null);
		assertEquals(adapter.motorFromIndex(0), 1);
		assertEquals(adapter.motorFromIndex(1), 2);
		assertEquals(adapter.motorFromIndex(2), 3);
	}
	
	@Test
	public void testMotorFromIndexWithAxisMotorOrder() {
		assertEquals(adapter.motorFromIndex(0), 4);
		assertEquals(adapter.motorFromIndex(1), 3);
		assertEquals(adapter.motorFromIndex(2), 2);
		assertEquals(adapter.motorFromIndex(3), 1);
	}
	
	@Test
	public void testStopAndReset() throws DeviceException, InterruptedException {
		adapter.stopAndReset();
		// some internal changes
		verify(mockedController).stop();
		verify(mockedController).setMMove(1, false);
		verify(mockedController).setMMove(2, false);
		verify(mockedController).setMMove(3, false);
		verify(mockedController).setMMove(4, false);
		verify(mockedController).setMMove(5, false);
		verify(mockedController).setMMove(6, false);
		verify(mockedController).setMMove(7, false);
		verify(mockedController).setMMove(8, false);
	}

	@Test
	public void testSetAxisTrajectory() throws DeviceException, InterruptedException {
		adapter.setAxisTrajectory(0, new double[] {1.,2.,3.});
		verify(mockedController).setMTraj(4, new double[] {1.,2.,3.});
	}
	
	@Test
	public void getNumberAxis() {
		assertEquals(adapter.getNumberAxes(), 4);
	}
	
	@Test(expected=DeviceException.class)
	public void testAddPointWrongSize() throws DeviceException {
		adapter.addPoint(new Double[]{1., 2., 3.}); // 4 expected
	}

	@Test
	public void testAddPointAndGetLastPointAdded() throws DeviceException {
		adapter.addPoint(new Double[]{1., 2., null, 4.});
		adapter.addPoint(new Double[]{1.1, 2.1, null, 4.1});
		ArrayAssert.assertEquals(new Double[]{1.1, 2.1, null, 4.1}, adapter.getLastPointAdded());
	}


	@Test
	public void testPrepareForMove() throws DeviceException, OutOfRangeException, InterruptedException, CAException, TimeoutException {
		adapter.addPoint(new Double[]{1., 2., 3., 4.});
		adapter.addPoint(new Double[]{1.1, 2.1, 3.1, 4.1});
		adapter.setTriggerPeriod(2);
		when(mockedController.getMaximumNumberElements()).thenReturn(4);
		when(mockedController.getMTraj(4)).thenReturn(new double[]{1., 1.1, 0., 0.});
		when(mockedController.getMTraj(3)).thenReturn(new double[]{2., 2.1, 0., 0.});
		when(mockedController.getMTraj(2)).thenReturn(new double[]{3., 3.1, 0., 0.});
		when(mockedController.getMTraj(1)).thenReturn(new double[]{4., 4.1, 0., 0.});
		when(mockedController.getMTraj(5)).thenReturn(new double[]{0., 0., 0., 0.});
		when(mockedController.getMTraj(6)).thenReturn(new double[]{0., 0., 0., 0.});
		when(mockedController.getMTraj(7)).thenReturn(new double[]{0., 0., 0., 0.});
		when(mockedController.getMTraj(8)).thenReturn(new double[]{0., 0., 0., 0.});
		when(mockedController.getNumberOfElements()).thenReturn(2);
		when(mockedController.getStartPulseElement()).thenReturn(1);
		when(mockedController.getStopPulseElement()).thenReturn(2);
		when(mockedController.getTrajectoryTime()).thenReturn(2.);
		when(mockedController.isMMove(4)).thenReturn(true);
		when(mockedController.isMMove(3)).thenReturn(true);
		when(mockedController.isMMove(2)).thenReturn(true);
		when(mockedController.isMMove(1)).thenReturn(true);
		
		adapter.prepareForMove();
		verify(mockedController).setMTraj(4, new double[]{1., 1.1});
		verify(mockedController).setMTraj(3, new double[]{2., 2.1});
		verify(mockedController).setMTraj(2, new double[]{3., 3.1});
		verify(mockedController).setMTraj(1, new double[]{4., 4.1});
		verify(mockedController).setMTraj(5, new double[]{});
		verify(mockedController).setMTraj(6, new double[]{});
		verify(mockedController).setMTraj(7, new double[]{});
		verify(mockedController).setMTraj(8, new double[]{});
		verify(mockedController).setMMove(4, true);
		verify(mockedController).setMMove(3, true);
		verify(mockedController).setMMove(2, true);
		verify(mockedController).setMMove(1, true);
		verify(mockedController).setMMove(5, false);
		verify(mockedController).setMMove(6, false);
		verify(mockedController).setMMove(7, false);
		verify(mockedController).setMMove(8, false);
		
		verify(mockedController).setNumberOfElements(2);
		verify(mockedController).setNumberOfPulses(2);
		verify(mockedController).setTrajectoryTime(2.);
		verify(mockedController).build();
		
	}
	@Test
	public void testPrepareForMoveWithNonMovingAxes() throws DeviceException, OutOfRangeException, InterruptedException, CAException, TimeoutException {
		adapter.addPoint(new Double[]{1., null, null, 4.});
		adapter.addPoint(new Double[]{1.1, null, null, 4.1});
		adapter.setTriggerPeriod(2.);
		
		when(mockedController.getMaximumNumberElements()).thenReturn(4);
		when(mockedController.getMTraj(4)).thenReturn(new double[]{1., 1.1, 0., 0.});
		when(mockedController.getMTraj(3)).thenReturn(new double[]{0., 0., 0., 0.});
		when(mockedController.getMTraj(2)).thenReturn(new double[]{0., 0., 0., 0.});
		when(mockedController.getMTraj(1)).thenReturn(new double[]{4., 4.1, 0., 0.});
		when(mockedController.getMTraj(5)).thenReturn(new double[]{0., 0., 0., 0.});
		when(mockedController.getMTraj(6)).thenReturn(new double[]{0., 0., 0., 0.});
		when(mockedController.getMTraj(7)).thenReturn(new double[]{0., 0., 0., 0.});
		when(mockedController.getMTraj(8)).thenReturn(new double[]{0., 0., 0., 0.});
		when(mockedController.getNumberOfElements()).thenReturn(2);
		when(mockedController.getStartPulseElement()).thenReturn(1);
		when(mockedController.getStopPulseElement()).thenReturn(2);
		when(mockedController.getTrajectoryTime()).thenReturn(2.);
		when(mockedController.isMMove(4)).thenReturn(true);
		when(mockedController.isMMove(1)).thenReturn(true);
		
		adapter.prepareForMove();
		verify(mockedController).setMTraj(4, new double[]{1., 1.1});
		verify(mockedController).setMTraj(3, new double[]{});
		verify(mockedController).setMTraj(2, new double[]{});
		verify(mockedController).setMTraj(1, new double[]{4., 4.1});
		verify(mockedController).setMTraj(5, new double[]{});
		verify(mockedController).setMTraj(6, new double[]{});
		verify(mockedController).setMTraj(7, new double[]{});
		verify(mockedController).setMTraj(8, new double[]{});
		verify(mockedController).setMMove(4, true);
		verify(mockedController).setMMove(3, false);
		verify(mockedController).setMMove(2, false);
		verify(mockedController).setMMove(1, true);
		verify(mockedController).setMMove(5, false);
		verify(mockedController).setMMove(6, false);
		verify(mockedController).setMMove(7, false);
		verify(mockedController).setMMove(8, false);
		verify(mockedController).setNumberOfElements(2);
		verify(mockedController).setNumberOfPulses(2);
		verify(mockedController).setTrajectoryTime(2.);
		verify(mockedController).build();
	}
	@Test
	public void testPrepareForMoveNoPoints() throws DeviceException, OutOfRangeException, InterruptedException {
		adapter.setTriggerPeriod(1);
		
		when(mockedController.getMaximumNumberElements()).thenReturn(4);
		when(mockedController.getMTraj(4)).thenReturn(new double[]{0., 0., 0., 0.});
		when(mockedController.getMTraj(3)).thenReturn(new double[]{0., 0., 0., 0.});
		when(mockedController.getMTraj(2)).thenReturn(new double[]{0., 0., 0., 0.});
		when(mockedController.getMTraj(1)).thenReturn(new double[]{0., 0., 0., 0.});
		when(mockedController.getMTraj(5)).thenReturn(new double[]{0., 0., 0., 0.});
		when(mockedController.getMTraj(6)).thenReturn(new double[]{0., 0., 0., 0.});
		when(mockedController.getMTraj(7)).thenReturn(new double[]{0., 0., 0., 0.});
		when(mockedController.getMTraj(8)).thenReturn(new double[]{0., 0., 0., 0.});
		when(mockedController.getNumberOfElements()).thenReturn(0);
		when(mockedController.getStartPulseElement()).thenReturn(1);
		when(mockedController.getStopPulseElement()).thenReturn(0);
		when(mockedController.getTrajectoryTime()).thenReturn(0.);
		
		adapter.prepareForMove();
		verify(mockedController).setMTraj(4, new double[]{});
		verify(mockedController).setMTraj(3, new double[]{});
		verify(mockedController).setMTraj(2, new double[]{});
		verify(mockedController).setMTraj(1, new double[]{});
		verify(mockedController).setMTraj(5, new double[]{});
		verify(mockedController).setMTraj(6, new double[]{});
		verify(mockedController).setMTraj(7, new double[]{});
		verify(mockedController).setMTraj(8, new double[]{});
		verify(mockedController).setMMove(4, false);
		verify(mockedController).setMMove(3, false);
		verify(mockedController).setMMove(2, false);
		verify(mockedController).setMMove(1, false);
		verify(mockedController).setMMove(5, false);
		verify(mockedController).setMMove(6, false);
		verify(mockedController).setMMove(7, false);
		verify(mockedController).setMMove(8, false);
		verify(mockedController).setNumberOfElements(0);
		verify(mockedController).setNumberOfPulses(0);
		verify(mockedController).setTrajectoryTime(0.);
		verify(mockedController).build(); // Would throw an exception with real controller
	}
	
	
	@Ignore
	@Test
	public void testSetTriggerDeltas() throws DeviceException {
		adapter.setTriggerDeltas(new double[] {1., 2.});
	}


	@Test
	public void testStartMove() throws DeviceException, InterruptedException {
		doAnswer(new SleepStub()).when(mockedController).execute();
		
		long startMillis = System.currentTimeMillis();
		adapter.startMove();
		adapter.waitWhileMoving();
		assertTrue((System.currentTimeMillis() - startMillis) >= 2000);
		
		verify(mockedController).execute();
		
	}
	
	@Test
	public void testIsMoving() throws DeviceException,  InterruptedException{
		doAnswer(new SleepStub()).when(mockedController).execute();

		assertFalse(adapter.isMoving());
		adapter.startMove();
		assertTrue(adapter.isMoving()); // lazy test subject to fail if jvm busy
		adapter.waitWhileMoving();
		assertFalse(adapter.isMoving());
	}

	@Test
	public void testReadActualPositionsFromHardware() throws Exception {
		when(mockedController.getMActual(1)).thenReturn(new double[] {1., 1.1});
		when(mockedController.getMActual(2)).thenReturn(new double[] {2., 2.1});
		when(mockedController.getMActual(3)).thenReturn(new double[] {3., 3.1});
		when(mockedController.getMActual(4)).thenReturn(new double[] {4., 4.1});
		List<double[]> positions = adapter.readActualPositionsFromHardware();
		verify(mockedController).read();
		ArrayAssert.assertEquals(positions.get(0), new double[] {4., 3., 2., 1.}, .001);
		ArrayAssert.assertEquals(positions.get(1), new double[] {4.1, 3.1, 2.1, 1.1}, .001);
	}

}
