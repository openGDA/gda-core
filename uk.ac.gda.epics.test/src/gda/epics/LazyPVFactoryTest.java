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

package gda.epics;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import gda.epics.PV.PVValues;
import gda.epics.connection.EpicsController;
import gov.aps.jca.CAException;
import gov.aps.jca.CAStatus;
import gov.aps.jca.Channel;
import gov.aps.jca.Monitor;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.dbr.DBR_Int;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import gov.aps.jca.event.PutEvent;
import gov.aps.jca.event.PutListener;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

/**
 * NOTE: Only tests PVs associated with Integers.
 */
public class LazyPVFactoryTest {

	private PV<Integer> pv;
	private EpicsController mockEpicsController;
	private Channel mockChannel;
	private DBR_Int mockIntDBR;
	private NoCallbackPV<Integer[]> pvArray;
	private DBR_Enum mockEnumDBR;
	private PV<TestEnum> enumPV;
	private PV<Boolean> binaryPV;
	private MonitorEvent mockEvent;
	private DBR_Int mockEventDBR;

	@Before
	public void setUp() throws Exception {
		mockEpicsController = mock(EpicsController.class);
		LazyPVFactory.setEPICS_CONTROLLER(mockEpicsController);
		mockChannel = mock(Channel.class);
		mockIntDBR = mock(DBR_Int.class);
		mockEnumDBR = mock(DBR_Enum.class);
		mockEvent = mock(MonitorEvent.class);
		mockEventDBR = mock(DBR_Int.class);
		when(mockEvent.getDBR()).thenReturn(mockEventDBR);
		setUpIntAndIntArray();
	}

	private void setUpIntAndIntArray() throws Exception {
		when(mockChannel.getFieldType()).thenReturn(DBRType.INT);
		when(mockEpicsController.createChannel("full:pv:name.ext")).thenReturn(mockChannel);
		when(mockEpicsController.getDBR(mockChannel, DBRType.INT)).thenReturn(mockIntDBR);
		pv = LazyPVFactory.newIntegerPV("full:pv:name.ext");
		pvArray = LazyPVFactory.newIntegerArrayPV("full:pv:name.ext");
	}

	@Test
	public void testConstruction() {
		ReadOnlyPV<Integer> ropv = LazyPVFactory.newReadOnlyIntegerPV("full:pv:name.ext");
		assertEquals("full:pv:name.ext", ropv.getPvName());
		assertFalse(ropv instanceof PV);
		assertFalse(pv.isValueMonitoring());
	}

	@Test
	public void testConstructionIsLazy() {
		pv = LazyPVFactory.newIntegerPV("full:pv:name.ext");
		verifyZeroInteractions(mockEpicsController);
	}

	@Test
	public void testGet() throws Exception {
		when(mockIntDBR.getIntValue()).thenReturn(new int[] { 1 });
		assertEquals((Integer) 1, pv.get());
		when(mockIntDBR.getIntValue()).thenReturn(new int[] { 2 });
		assertEquals((Integer) 2, pv.get());
		verify(mockEpicsController, times(1)).createChannel(anyString());
	}

	@Test
	public void testSetValueMonitoring_True() throws Exception {
		pv.setValueMonitoring(true);
		pv.setValueMonitoring(true);
		verify(mockEpicsController, times(1)).setMonitor(eq(mockChannel), eq(DBRType.INT), eq(Monitor.VALUE),
				any(MonitorListener.class)); // vague test

	}

	@Test
	public void testSetValueMonitoring_TrueFalse() throws Exception {
		Monitor mockMonitor = mock(Monitor.class);
		when(
				mockEpicsController.setMonitor(eq(mockChannel), eq(DBRType.INT), eq(Monitor.VALUE),
						any(MonitorListener.class))).thenReturn(mockMonitor);

		pv.setValueMonitoring(true);
		pv.setValueMonitoring(false);
		verify(mockEpicsController, times(1)).clearMonitor(mockMonitor);

	}

	@Test
	public void testGetWithValueMonitoringNoMonitoredValues() throws Exception {
		// should go out once to get the first value and then no more
		pv.setValueMonitoring(true);
		when(mockIntDBR.getIntValue()).thenReturn(new int[] { 1 });
		assertEquals((Integer) 1, pv.getLast());
		when(mockIntDBR.getIntValue()).thenReturn(new int[] { 9999 }); // should not be used!
		assertEquals((Integer) 1, pv.getLast());
		verify(mockEpicsController, times(1)).createChannel(anyString());
	}

	@Test
	public void testGetWithValueMonitoringWithMonitoredValues() throws Exception {
		MonitorListener monitorListener = setupMonitoring();

		when(mockIntDBR.getIntValue()).thenReturn(new int[] { 1 });
		assertEquals((Integer) 1, pv.getLast()); // gone out to get

		when(mockEventDBR.getIntValue()).thenReturn(new int[] { 2 });
		monitorListener.monitorChanged(mockEvent);
		assertEquals((Integer) 2, pv.getLast());
		assertEquals((Integer) 2, pv.getLast());

		when(mockEventDBR.getIntValue()).thenReturn(new int[] { 3 });
		monitorListener.monitorChanged(mockEvent);
		assertEquals((Integer) 3, pv.getLast());

	}

	protected MonitorListener setupMonitoring() throws IOException, CAException {
		ArgumentCaptor<MonitorListener> monitorArgument = ArgumentCaptor.forClass(MonitorListener.class);
		pv.setValueMonitoring(true);
		verify(mockEpicsController, times(1)).setMonitor(eq(mockChannel), eq(DBRType.INT), eq(Monitor.VALUE),
				monitorArgument.capture());
		MonitorListener monitorListener = monitorArgument.getValue();
		return monitorListener;
	}

	@Test
	public void testWaitForValue() throws Exception {
		MonitorListener monitorListener = setupMonitoring();

		when(mockIntDBR.getIntValue()).thenReturn(new int[] { 1 });

		final Predicate<Integer> greaterThanTen = new Predicate<Integer>() {
			@Override
			public boolean apply(Integer integer) {
				return integer > 10;
			}
		};
		FutureTask<Integer> futureTask = new FutureTask<Integer>(new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return pv.waitForValue(greaterThanTen, 0);
			}
		});
		(new Thread(futureTask)).start();

		Thread.sleep(500);
		assertFalse(futureTask.isDone());

		when(mockEventDBR.getIntValue()).thenReturn(new int[] { 4 });
		monitorListener.monitorChanged(mockEvent);
		Thread.sleep(500);
		assertFalse(futureTask.isDone());

		when(mockEventDBR.getIntValue()).thenReturn(new int[] { 11 });
		monitorListener.monitorChanged(mockEvent);
		assertEquals((Integer) 11, futureTask.get(10, TimeUnit.SECONDS));
	}

	@Test
	public void testWaitForValueTimesOut() throws Exception {
		MonitorListener monitorListener = setupMonitoring();

		when(mockIntDBR.getIntValue()).thenReturn(new int[] { 1 });
		when(mockEventDBR.getIntValue()).thenReturn(new int[] { 1 });

		final Predicate<Integer> neverTrue = new Predicate<Integer>() {
			@Override
			public boolean apply(Integer integer) {
				return false;
			}
		};
		FutureTask<Integer> futureTask = new FutureTask<Integer>(new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return pv.waitForValue(neverTrue, 2);
			}
		});

		(new Thread(futureTask)).start();
		Thread.sleep(250);
		assertFalse(futureTask.isDone());

		monitorListener.monitorChanged(mockEvent);
		Thread.sleep(250);
		assertFalse(futureTask.isDone());

		try {
			futureTask.get();
		} catch (ExecutionException e) {
			assertTrue(e.getCause() instanceof java.util.concurrent.TimeoutException);
		}

	}

	@Test
	public void testGetArray() throws Exception {
		PV<Integer[]> pvArray = LazyPVFactory.newIntegerArrayPV("full:pv:name.ext");
		when(mockIntDBR.getIntValue()).thenReturn(new int[] { 1, 2, 3, 4, 5 });
		assertArrayEquals(new Integer[] { 1, 2, 3, 4, 5 }, pvArray.get());
		when(mockIntDBR.getIntValue()).thenReturn(new int[] { 5, 4, 3, 2, 1 });
		assertArrayEquals(new Integer[] { 5, 4, 3, 2, 1 }, pvArray.get());
		verify(mockEpicsController, times(1)).createChannel("full:pv:name.ext");
	}

	@Test
	public void testPut() throws Exception {
		pv.putNoWait(1);
		pv.putNoWait(2);
		InOrder inOrder = inOrder(mockEpicsController);
		inOrder.verify(mockEpicsController, times(1)).createChannel("full:pv:name.ext");
		inOrder.verify(mockEpicsController).caput(mockChannel, 1);
		inOrder.verify(mockEpicsController).caput(mockChannel, 2);
	}

	@Test
	public void testPutArray() throws Exception {
		pvArray.putNoWait(new Integer[] { 1, 2, 3, 4, 5 });
		pvArray.putNoWait(new Integer[] { 5, 4, 3, 2, 1 });
		InOrder inOrder = inOrder(mockEpicsController);
		inOrder.verify(mockEpicsController, times(1)).createChannel("full:pv:name.ext");
		inOrder.verify(mockEpicsController).caput(mockChannel, new int[] { 1, 2, 3, 4, 5 });
		inOrder.verify(mockEpicsController).caput(mockChannel, new int[] { 5, 4, 3, 2, 1 });
	}

	// pv.setValueMonitoring(true);
	// verify(mockEpicsController, times(1)).setMonitor(eq(mockChannel), eq(DBRType.INT), eq(Monitor.VALUE),
	// monitorArgument.capture());
	// MonitorListener monitorListener = monitorArgument.getValue();
	// return monitorListener;

	@Test
	public void testStartPutCallback() throws Exception {
		ArgumentCaptor<PutListener> putListenerArgument = ArgumentCaptor.forClass(PutListener.class);
		pv.putAsyncStart(1);
		verify(mockEpicsController, times(1)).caput(eq(mockChannel), eq(1), putListenerArgument.capture());
	}

	@Test
	public void testWaitForPutCallback() throws Exception {
		ArgumentCaptor<PutListener> putListenerArgument = ArgumentCaptor.forClass(PutListener.class);
		pv.putAsyncStart(1);
		verify(mockEpicsController, times(1)).caput(eq(mockChannel), eq(1), putListenerArgument.capture());
		PutListener putListener = putListenerArgument.getValue();

		PutEvent mockPutEvent = mock(PutEvent.class);
		when(mockPutEvent.getStatus()).thenReturn(CAStatus.NORMAL);

		FutureTask<Void> futureTask = new FutureTask<Void>(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				pv.putAsyncWait();
				return null;
			}
		});

		(new Thread(futureTask)).start();

		Thread.sleep(500);
		assertFalse(futureTask.isDone());

		putListener.putCompleted(mockPutEvent);
		futureTask.get();

	}

	@Test
	public void testPutCallback() throws Exception {
		ArgumentCaptor<PutListener> putListenerArgument = ArgumentCaptor.forClass(PutListener.class);

		PutEvent mockPutEvent = mock(PutEvent.class);
		when(mockPutEvent.getStatus()).thenReturn(CAStatus.NORMAL);

		FutureTask<Void> futureTask = new FutureTask<Void>(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				pv.putWait(1, 5);
				return null;
			}
		});

		(new Thread(futureTask)).start();

		Thread.sleep(500);
		verify(mockEpicsController, times(1)).caput(eq(mockChannel), eq(1), putListenerArgument.capture());
		PutListener putListener = putListenerArgument.getValue();
		assertFalse(futureTask.isDone());

		putListener.putCompleted(mockPutEvent);
		futureTask.get();

	}

	@Test
	public void testPutCallbackReturn() throws Exception {
		@SuppressWarnings("unchecked")
		final PV<Integer> mockIntPV = mock(PV.class);
		when(mockIntPV.get()).thenReturn(123);
		ArgumentCaptor<PutListener> putListenerArgument = ArgumentCaptor.forClass(PutListener.class);
		
		PutEvent mockPutEvent = mock(PutEvent.class);
		when(mockPutEvent.getStatus()).thenReturn(CAStatus.NORMAL);
		
		FutureTask<PVValues> futureTask = new FutureTask<PVValues>(new Callable<PVValues>() {
			@Override
			public PVValues call() throws Exception {
				return pv.putWait(1, mockIntPV);
			}
		});
		
		(new Thread(futureTask)).start();
		
		Thread.sleep(500);
		verify(mockEpicsController, times(1)).caput(eq(mockChannel), eq(1), putListenerArgument.capture());
		PutListener putListener = putListenerArgument.getValue();
		assertFalse(futureTask.isDone());
		
		putListener.putCompleted(mockPutEvent);
		PVValues putCallbackResult = futureTask.get();
		assertEquals((Integer) 123, putCallbackResult.get(mockIntPV));
		
	}
	
	@Test
	public void testPutCallbackNoTimeout() {

	}

	public enum TestEnum {
		ZERO, ONE, TWO
	}

	private void setUpEnum() throws Exception {
		when(mockChannel.getFieldType()).thenReturn(DBRType.ENUM);
		when(mockEpicsController.createChannel("full:pv:name.ext")).thenReturn(mockChannel);
		when(mockEpicsController.getDBR(mockChannel, DBRType.ENUM)).thenReturn(mockEnumDBR);
		enumPV = LazyPVFactory.newEnumPV("full:pv:name.ext", TestEnum.class);
	}

	@Test
	public void testPutEnum() throws Exception {
		setUpEnum();
		enumPV.putNoWait(TestEnum.ZERO);
		enumPV.putNoWait(TestEnum.TWO);
		InOrder inOrder = inOrder(mockEpicsController);
		inOrder.verify(mockEpicsController, times(1)).createChannel("full:pv:name.ext");
		inOrder.verify(mockEpicsController).caput(mockChannel, 0);
		inOrder.verify(mockEpicsController).caput(mockChannel, 2);
	}

	@Test
	public void testGetEnum() throws Exception {
		setUpEnum();
		when(mockEnumDBR.getEnumValue()).thenReturn(new short[] { 1 });
		assertEquals(TestEnum.ONE, enumPV.get());
		when(mockEnumDBR.getEnumValue()).thenReturn(new short[] { 2 });
		assertEquals(TestEnum.TWO, enumPV.get());
		verify(mockEpicsController, times(1)).createChannel(anyString());
	}

	private void setUpBinaryFromInteger() throws Exception {
		when(mockChannel.getFieldType()).thenReturn(DBRType.INT);
		when(mockEpicsController.createChannel("full:pv:name.ext")).thenReturn(mockChannel);
		when(mockEpicsController.getDBR(mockChannel, DBRType.ENUM)).thenReturn(mockIntDBR);
		binaryPV = LazyPVFactory.newBooleanFromIntegerPV("full:pv:name.ext");
	}

	@Test
	public void testPutWithBinaryFromInteger() throws Exception {
		setUpBinaryFromInteger();
		binaryPV.putNoWait(true);
		binaryPV.putNoWait(false);
		InOrder inOrder = inOrder(mockEpicsController);
		inOrder.verify(mockEpicsController, times(1)).createChannel("full:pv:name.ext");
		inOrder.verify(mockEpicsController).caput(mockChannel, 1);
		inOrder.verify(mockEpicsController).caput(mockChannel, 0);
	}

	@Test
	public void testGetWithBinaryFromInteger() throws Exception {
		setUpBinaryFromInteger();
		when(mockIntDBR.getIntValue()).thenReturn(new int[] { 1 });
		assertTrue(binaryPV.get());
		when(mockIntDBR.getIntValue()).thenReturn(new int[] { 0 });
		assertFalse(binaryPV.get());
		verify(mockEpicsController, times(1)).createChannel(anyString());
	}

}
