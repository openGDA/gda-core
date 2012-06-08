/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.scan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import gda.MockFactory;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.DummyCallable;
import gda.device.scannable.PositionCallableProvider;

import java.util.concurrent.Callable;

import junitx.framework.ArrayAssert;

import org.junit.Test;
import org.mockito.InOrder;

@SuppressWarnings("rawtypes")
public class MultithreadedScanDataPointPipelineTest extends BasicScanDataPointPipelineTest {

	interface PositionCallableProvidingScannable extends PositionCallableProvider, Scannable {
	}

	interface PositionCallableProvidingDetector extends PositionCallableProvider, Detector {
	}

	private MultithreadedScanDataPointPipeline multithreadedPipeline;
	private PositionCallableProvidingScannable scnc;
	private PositionCallableProvidingScannable scnd;
	private Callable<Object> callablec1;
	private Callable<Object> callablec2;
	private Callable<Object> callabled1;
	private Callable<Object> callabled2;
	private Object posc1;
	private Object posc2;
	private Object posc3;
	private Object posd1;
	private Object posd2;
	private Object posd3;
	public Exception caughtException;
	private ScanDataPointPublisher mockSDPPublisher;
	public volatile boolean reached1 = false;
	public volatile boolean reached2 = false;
	public volatile boolean reached3 = false;
	private DummyCallable dummyCallablec1;
	private DummyCallable dummyCallablec2;
	private DummyCallable dummyCallablec3;
	private DummyCallable dummyCallabled1;
	private DummyCallable dummyCallabled2;
	private DummyCallable dummyCallabled3;
	private ScanDataPoint point1;
	private ScanDataPoint point2;
	private ScanDataPoint point3;
	private Callable callablec3;
	private Callable callabled3;
	private PositionCallableProvidingDetector detb;
	private NamedObject datab1;
	private NamedObject datab2;
	private NamedObject datab3;
	private Callable callabledetb1;
	private Callable callabledetb2;
	private Callable callabledetb3;

	@Override
	protected void configurePipeline() {
		multithreadedPipeline = new MultithreadedScanDataPointPipeline(new ScanDataPointPublisher(mockDataWriter,
				mockScan), 10, 10, "scan-name");
		// multithreadedPipeline = new MultithreadedScanDataPointPipeline(mockSDP, 10, 10);
	}

	@Override
	public ScanDataPointPipeline getBasicPipeline() {
		return multithreadedPipeline;
	}

	@Override
	public ScanDataPointPipeline getPipeline() {
		return multithreadedPipeline;
	}

	@SuppressWarnings("unchecked")
	protected void configureMockScannablesAndDetectorsWithCallableProviders() throws Exception {
		scnc = MockFactory.createMockScannable(PositionCallableProvidingScannable.class, "scnc",
				new String[] { "scnc" }, new String[] {}, new String[] { "formatc" }, 5, null);

		// Ccannable c
		posc1 = new NamedObject("posc1");
		posc2 = new NamedObject("posc2");
		posc2 = new NamedObject("posc3");
		callablec1 = mock(Callable.class);
		callablec2 = mock(Callable.class);
		callablec3 = mock(Callable.class);
		when(callablec1.call()).thenReturn(posc1);
		when(callablec2.call()).thenReturn(posc2);
		when(callablec3.call()).thenReturn(posc3);
		when(scnc.getPositionCallable()).thenReturn(callablec1, callablec2, callablec3);

		scnd = MockFactory.createMockScannable(PositionCallableProvidingScannable.class, "scnd",
				new String[] { "scnd" }, new String[] {}, new String[] { "formatd" }, 5, null);
		posd1 = new NamedObject("posd1");
		posd2 = new NamedObject("posd2");
		posd2 = new NamedObject("posd3");
		callabled1 = mock(Callable.class);
		callabled2 = mock(Callable.class);
		callabled3 = mock(Callable.class);
		when(callabled1.call()).thenReturn(posd1);
		when(callabled2.call()).thenReturn(posd2);
		when(callabled3.call()).thenReturn(posd3);
		when(scnd.getPositionCallable()).thenReturn(callabled1, callabled2, callabled3);

		// Detector b
		detb = mock(PositionCallableProvidingDetector.class);
		datab1 = new NamedObject("datab1");
		datab2 = new NamedObject("datab2");
		datab3 = new NamedObject("datab3");
		callabledetb1 = mock(Callable.class);
		callabledetb2 = mock(Callable.class);
		callabledetb3 = mock(Callable.class);
		when(callabledetb1.call()).thenReturn(datab1);
		when(callabledetb2.call()).thenReturn(datab2);
		when(callabledetb3.call()).thenReturn(datab3);
		when(detb.getPositionCallable()).thenReturn(callabledetb1, callabledetb2, callabledetb3);
		when(detb.getOutputFormat()).thenReturn(new String[] { "%s" });

		when(detb.getInputNames()).thenReturn(new String[] {});
		when(detb.getExtraNames()).thenReturn(new String[] { "fred" });
	}

	protected ScanDataPoint createScanDataPointWithCallableProviders(String uniqueName) throws DeviceException, InterruptedException {
		ScanDataPoint point = new ScanDataPoint();
		point.setUniqueName(uniqueName);
		point.addScannable(scna);
		point.addScannable(scnc);
		point.addScannable(scnb);
		point.addScannable(scnd);
		point.addDetector(deta);
		point.addDetector(detb);
		ScanBase.populateScannablePositions(point);
		ScanBase.populateDetectorData(point);
		return point;
	}

	@Test
	public void testPopulatePositionsAndDataAndBroadcastWithCallables() throws Exception {
		configureMockScannablesAndDetectors();
		configureMockScannablesAndDetectorsWithCallableProviders();
		ScanDataPoint point1 = createScanDataPointWithCallableProviders("point1");
		ScanDataPoint point2 = createScanDataPointWithCallableProviders("point2");
		getPipeline().put(point1);
		getPipeline().put(point2);
		getPipeline().shutdown(1000);

		InOrder inOrderTestThread = inOrder(scna, scnc, scnb, scnd, deta, detb);
		inOrderTestThread.verify(scna).getPosition();
		inOrderTestThread.verify(scnc).getPositionCallable();
		inOrderTestThread.verify(scnb).getPosition();
		inOrderTestThread.verify(scnd).getPositionCallable();
		inOrderTestThread.verify(deta).readout();
		inOrderTestThread.verify(scna).getPosition();
		inOrderTestThread.verify(scnc).getPositionCallable();
		inOrderTestThread.verify(scnb).getPosition();
		inOrderTestThread.verify(scnd).getPositionCallable();
		inOrderTestThread.verify(deta).readout();

		InOrder inOrderBroadcastThread = inOrder(mockDataWriter, mockJythonServerNotifer);
		inOrderBroadcastThread.verify(mockDataWriter).addData(point1);
		inOrderBroadcastThread.verify(mockJythonServerNotifer).notifyServer(mockScan, point1);
		inOrderBroadcastThread.verify(mockDataWriter).addData(point2);
		inOrderBroadcastThread.verify(mockJythonServerNotifer).notifyServer(mockScan, point2);

		ArrayAssert.assertEquals(new Object[] { posa1, posc1, posb1, posd1 }, point1.getScannablePositions().toArray());
		ArrayAssert.assertEquals(new Object[] { dataa1, datab1 }, point1.getDetectorData().toArray());
		ArrayAssert.assertEquals(new Object[] { posa2, posc2, posb2, posd2 }, point2.getScannablePositions().toArray());
		ArrayAssert.assertEquals(new Object[] { dataa2, datab2 }, point2.getDetectorData().toArray());
	}

	@Test
	public void testPopAndBroadcastThrowsGetPositionExceptionWithCallablesOnNextPut() throws Exception {
		configureMockScannablesAndDetectors();
		configureMockScannablesAndDetectorsWithCallableProviders();
		DeviceException expected = new DeviceException("expected");
		when(callablec1.call()).thenThrow(expected);
		ScanDataPoint point = createScanDataPointWithCallableProviders("point");
		getPipeline().put(point);
		Thread.sleep(1000); // make sure the bad point has been computed
		// Should get exception on next put

		try {
			getPipeline().put(mock(ScanDataPoint.class));
			fail("DeviceException expected");
		} catch (DeviceException e) {
			assertEquals(expected, e.getCause().getCause());
		}
	}

	@Test
	public void testPopAndBroadcastThrowsGetPositionExceptionWithCallablesOnShutdown() throws Exception {
		configureMockScannablesAndDetectors();
		configureMockScannablesAndDetectorsWithCallableProviders();
		DeviceException expected = new DeviceException("expected");
		when(callablec1.call()).thenThrow(expected);
		ScanDataPoint point = createScanDataPointWithCallableProviders("point");
		getPipeline().put(point);
		Thread.sleep(1000); // make sure the bad point has been computed
		// Should get exception on next put

		try {
			getPipeline().shutdown(1000);
			fail("DeviceException expected");
		} catch (DeviceException e) {
			assertEquals(expected, e.getCause().getCause());
		}
	}

	@Test
	public void testPopAndBroadcastOnShutdownPipeline() throws Exception {
		getPipeline().shutdown(1000);
		configureMockScannablesAndDetectors();
		ScanDataPoint point = createScanDataPoint();

		try {
			getPipeline().put(point);
			fail("DeviceException expected");
		} catch (DeviceException e) {
			assertEquals("Could not add new point to MultithreadedScanDataPointPipeline as it is shutdown.", e
					.getMessage());
		}
	}

	class ScanLikeRun implements Runnable {

		private final ScanDataPoint _point1;
		private final ScanDataPoint _point2;
		private final ScanDataPoint _point3;

		public ScanLikeRun(ScanDataPoint point1, ScanDataPoint point2, ScanDataPoint point3) {
			this._point1 = point1;
			this._point2 = point2;
			this._point3 = point3;
		}

		@Override
		public void run() {
			try {
				getPipeline().put(_point1);
				System.out.println("point1 put to pipeline");
				reached1 = true;
				getPipeline().put(_point2);
				reached2 = true;
				System.out.println("point2 put to pipeline");

				if (_point3 != null) {
					getPipeline().put(_point3);
					reached3 = true;
					System.out.println("point3 put to pipeline");
				}
				getPipeline().shutdown(30000);
				System.out.println("shutdown complete");
			} catch (Exception e) {
				caughtException = e;
			}
		}
	}

	@Test
	public void testScanThreadBlocksWhenPointsAdded_Length2() throws Exception {
		configureDummyCallableTest();
		multithreadedPipeline = new MultithreadedScanDataPointPipeline(mockSDPPublisher, 10, 2, "scan-name");
		ScanLikeRun scanLikeRun = new ScanLikeRun(point1, point2, point3);
		Thread scanThread = new Thread(scanLikeRun);
		scanThread.start();

		Thread.sleep(1000);
		// pipeline should now be full
		assertTrue(reached1);
		assertTrue(reached2);
		assertFalse("point3 put should still be blocking", reached3);

		// complete processing point1
		dummyCallablec1.makeReady();
		dummyCallabled1.makeReady();
		Thread.sleep(1000);
		assertTrue("point 3 should now be in pipeline", reached3);
		verify(mockSDPPublisher).publish(point1);

		// complete processing point2
		dummyCallablec2.makeReady();
		dummyCallabled2.makeReady();
		Thread.sleep(1000);
		verify(mockSDPPublisher).publish(point2);

		// complete processing point3
		dummyCallablec3.makeReady();
		dummyCallabled3.makeReady();
		Thread.sleep(1000);
		verify(mockSDPPublisher).publish(point3);

		scanThread.join(5000);
		if (scanThread.isAlive()) {
			fail("scanThread did not finish within 5s");
		}
		if (caughtException != null) {
			throw new Exception("Problem in scan thread:", caughtException);
		}

		ArrayAssert.assertEquals(new Object[] { posa1, posc1, posb1, posd1 }, point1.getScannablePositions().toArray());
		ArrayAssert.assertEquals(new Object[] { dataa1, datab1 }, point1.getDetectorData().toArray());
		ArrayAssert.assertEquals(new Object[] { posa2, posc2, posb2, posd2 }, point2.getScannablePositions().toArray());
		ArrayAssert.assertEquals(new Object[] { dataa2, datab2 }, point2.getDetectorData().toArray());
		ArrayAssert.assertEquals(new Object[] { posa3, posc3, posb3, posd3 }, point3.getScannablePositions().toArray());
		ArrayAssert.assertEquals(new Object[] { dataa3, datab3 }, point3.getDetectorData().toArray());
	}

	@Test
	public void testScanThreadBlocksWhenPointsAdded_Length1() throws Exception {
		configureDummyCallableTest();
		multithreadedPipeline = new MultithreadedScanDataPointPipeline(mockSDPPublisher, 10, 1, "scan-name");
		ScanLikeRun scanLikeRun = new ScanLikeRun(point1, point2, null);
		Thread scanThread = new Thread(scanLikeRun);
		scanThread.start();

		Thread.sleep(1000);
		// pipeline should now be full
		assertTrue(reached1);
		assertFalse("point2 put should still be blocking", reached2);

		// complete processing point1
		dummyCallablec1.makeReady();
		dummyCallabled1.makeReady();
		Thread.sleep(1000);
		assertTrue("point 2 should now be in pipeline", reached2);
		verify(mockSDPPublisher).publish(point1);

		// complete processing point2
		dummyCallablec2.makeReady();
		dummyCallabled2.makeReady();
		Thread.sleep(1000);
		verify(mockSDPPublisher).publish(point2);

		scanThread.join(5000);
		if (scanThread.isAlive()) {
			fail("scanThread did not finish within 5s");
		}
		if (caughtException != null) {
			throw new Exception("Problem in scan thread:", caughtException);
		}

		ArrayAssert.assertEquals(new Object[] { posa1, posc1, posb1, posd1 }, point1.getScannablePositions().toArray());
		ArrayAssert.assertEquals(new Object[] { dataa1, datab1 }, point1.getDetectorData().toArray());
		ArrayAssert.assertEquals(new Object[] { posa2, posc2, posb2, posd2 }, point2.getScannablePositions().toArray());
		ArrayAssert.assertEquals(new Object[] { dataa2, datab2 }, point2.getDetectorData().toArray());
	}

	private void configureDummyCallableTest() throws DeviceException, Exception {
		configureMockScannablesAndDetectors();
		configureMockScannablesAndDetectorsWithCallableProviders();
		dummyCallablec1 = new DummyCallable("callablec1", posc1, false);
		dummyCallablec2 = new DummyCallable("callablec2", posc2, false);
		dummyCallablec3 = new DummyCallable("callablec3", posc3, false);
		dummyCallabled1 = new DummyCallable("callabled1", posd1, false);
		dummyCallabled2 = new DummyCallable("callabled2", posd2, false);
		dummyCallabled3 = new DummyCallable("callabled3", posd3, false);
		when(scnc.getPositionCallable()).thenReturn(dummyCallablec1, dummyCallablec2, dummyCallablec3);
		when(scnd.getPositionCallable()).thenReturn(dummyCallabled1, dummyCallabled2, dummyCallabled3);
		point1 = createScanDataPointWithCallableProviders("point1");
		point2 = createScanDataPointWithCallableProviders("point2");
		point3 = createScanDataPointWithCallableProviders("point3");
		mockSDPPublisher = mock(ScanDataPointPublisher.class);
	}
}