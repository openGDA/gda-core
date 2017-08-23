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

import static gda.jython.InterfaceProvider.setJythonServerNotiferForTesting;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import gda.AbstractTestBase;
import gda.MockFactory;
import gda.data.scan.datawriter.DataWriter;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.PositionCallableProvider;
import gda.jython.IJythonServerNotifer;

public class MultithreadedScanDataPointPipelineTest extends AbstractTestBase {

	interface PositionCallableProvidingScannable extends PositionCallableProvider<Object>, Scannable {
	}

	interface PositionCallableProvidingDetector extends PositionCallableProvider<Object>, Detector {
	}

	class NamedObject {
		private final String name;

		NamedObject(final String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	protected ScanBase mockScan;
	protected DataWriter mockDataWriter;
	protected IJythonServerNotifer mockJythonServerNotifer;
	protected Scannable scna;
	protected Scannable scnb;
	protected Scannable zie;
	protected Detector deta;
	protected NamedObject posa1;
	protected NamedObject posb1;
	protected NamedObject dataa1;
	protected NamedObject posa2;
	protected NamedObject posb2;
	protected NamedObject dataa2;
	protected NamedObject posa3;
	protected NamedObject posb3;
	protected NamedObject dataa3;

	private MultithreadedScanDataPointPipeline pipeline;
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
	public volatile boolean reached1 = false;
	public volatile boolean reached2 = false;
	public volatile boolean reached3 = false;
	private Callable<Object> callablec3;
	private Callable<Object> callabled3;
	private PositionCallableProvidingDetector detb;
	private NamedObject datab1;
	private NamedObject datab2;
	private NamedObject datab3;
	private Callable<Object> callabledetb1;
	private Callable<Object> callabledetb2;
	private Callable<Object> callabledetb3;

	@Override
	@Before
	public void setUp() {
		mockDataWriter = mock(DataWriter.class);
		mockScan = mock(ScanBase.class);
		configurePipeline();
		mockJythonServerNotifer = mock(IJythonServerNotifer.class);
		setJythonServerNotiferForTesting(mockJythonServerNotifer);
	}

	protected void configureMockScannablesAndDetectors() throws DeviceException {
		scna = gda.MockFactory.createMockScannable("scna");
		scnb = gda.MockFactory.createMockScannable("scnb");
		zie = gda.MockFactory.createMockZieScannable("zie", 5);
		deta = mock(Detector.class);
		posa1 = new NamedObject("posa1");
		posb1 = new NamedObject("posb1");
		dataa1 = new NamedObject("data1");
		posa2 = new NamedObject("posa2");
		posb2 = new NamedObject("posb2");
		dataa2 = new NamedObject("data2");
		posa3 = new NamedObject("posa3");
		posb3 = new NamedObject("posb3");
		dataa3 = new NamedObject("data3");
		when(scna.getPosition()).thenReturn(posa1, posa2, posa3);
		when(scnb.getPosition()).thenReturn(posb1, posb2, posb3);
		when(deta.readout()).thenReturn(dataa1, dataa2, dataa3);
		when(scna.getOutputFormat()).thenReturn(new String[] { "formata" });
		when(scnb.getOutputFormat()).thenReturn(new String[] { "formatb" });
		when(deta.getInputNames()).thenReturn(new String[] { "inputname" });
		when(deta.getOutputFormat()).thenReturn(new String[] { "detinputformat", "detoutputformat" });
	}

	protected ScanDataPoint createScanDataPoint() {
		final ScanDataPoint point = new ScanDataPoint();
		point.addScannable(scna);
		point.addScannable(scnb);
		point.addDetector(deta);
		return point;
	}

	@Test
	public void testPopulatePositionsAndDataAndBroadcast() throws Exception {
		configureMockScannablesAndDetectors();
		final ScanDataPoint point1 = createScanDataPoint();
		final ScanDataPoint point2 = createScanDataPoint();
		pipeline.put(point1);
		pipeline.put(point2);
		pipeline.shutdown(true);

		final InOrder inOrderBroadcastThread = inOrder(mockDataWriter, mockJythonServerNotifer);
		// verify(mockDataWriter).addData(point1);
		// verify(mockJythonServerNotifer).notifyServer(mockScan, point1);
		// verify(mockDataWriter).addData(point2);
		// verify(mockJythonServerNotifer).notifyServer(mockScan, point2);
		inOrderBroadcastThread.verify(mockDataWriter).addData(point1);
		inOrderBroadcastThread.verify(mockJythonServerNotifer).notifyServer(mockScan, point1);
		inOrderBroadcastThread.verify(mockDataWriter).addData(point2);
		inOrderBroadcastThread.verify(mockJythonServerNotifer).notifyServer(mockScan, point2);

	}

	@Test
	public void testPopulatePositionsAndDataAndBroadcastThrowsGetPositionException() throws Exception {
		configureMockScannablesAndDetectors();
		final DeviceException expected = new DeviceException("expected");
		when(scnb.getPosition()).thenThrow(expected);
		final ScanDataPoint point = createScanDataPoint();
		try {
			pipeline.put(point);
		} catch (final DeviceException e) {
			assertEquals(expected, e.getCause());
		}
	}

	@Test
	public void testGetDataWriter() {
		assertEquals(mockDataWriter, pipeline.getDataWriter());
	}

	@Test
	public void testShutdown() throws Exception {
		// no exceptions thrown by DataWriter.completeCollection
		pipeline.shutdown(true);
		verify(mockDataWriter).completeCollection();
	}

	/*
	 * @Test public void testShutdownNow() throws Exception { // no exceptions thrown by DataWriter.completeCollection
	 * pipeline.shutdownNow(); verify(mockDataWriter).completeCollection(); }
	 */

	protected void configurePipeline() {
		pipeline = new MultithreadedScanDataPointPipeline(new ScanDataPointPublisher(mockDataWriter, mockScan), 10, 10,
				"scan-name");
		// multithreadedPipeline = new MultithreadedScanDataPointPipeline(mockSDP, 10, 10);
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

	protected ScanDataPoint createScanDataPointWithCallableProviders(final String uniqueName) throws DeviceException,
			InterruptedException {
		final ScanDataPoint point = new ScanDataPoint();
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
		final ScanDataPoint point1 = createScanDataPointWithCallableProviders("point1");
		final ScanDataPoint point2 = createScanDataPointWithCallableProviders("point2");
		pipeline.put(point1);
		pipeline.put(point2);
		pipeline.shutdown(true);

		final InOrder inOrderTestThread = inOrder(scna, scnc, scnb, scnd, deta, detb);
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

		final InOrder inOrderBroadcastThread = inOrder(mockDataWriter, mockJythonServerNotifer);
		inOrderBroadcastThread.verify(mockDataWriter).addData(point1);
		inOrderBroadcastThread.verify(mockJythonServerNotifer).notifyServer(mockScan, point1);
		inOrderBroadcastThread.verify(mockDataWriter).addData(point2);
		inOrderBroadcastThread.verify(mockJythonServerNotifer).notifyServer(mockScan, point2);

		assertArrayEquals(new Object[] { posa1, posc1, posb1, posd1 }, point1.getScannablePositions().toArray());
		assertArrayEquals(new Object[] { dataa1, datab1 }, point1.getDetectorData().toArray());
		assertArrayEquals(new Object[] { posa2, posc2, posb2, posd2 }, point2.getScannablePositions().toArray());
		assertArrayEquals(new Object[] { dataa2, datab2 }, point2.getDetectorData().toArray());
	}

	@Test
	public void testPopAndBroadcastThrowsGetPositionExceptionWithCallablesOnNextPut() throws Exception {
		configureMockScannablesAndDetectors();
		configureMockScannablesAndDetectorsWithCallableProviders();
		final DeviceException expected = new DeviceException("expected");
		when(callablec1.call()).thenThrow(expected);
		final ScanDataPoint point = createScanDataPointWithCallableProviders("point");
		pipeline.put(point);
		Thread.sleep(1000); // make sure the bad point has been computed
		// Should get exception on next put

		try {
			pipeline.put(mock(ScanDataPoint.class));
			fail("DeviceException expected");
		} catch (final DeviceException e) {
			assertEquals(expected, e.getCause());
		}
	}

	@Test
	public void testPopAndBroadcastThrowsGetPositionExceptionWithCallablesOnShutdown() throws Exception {
		configureMockScannablesAndDetectors();
		configureMockScannablesAndDetectorsWithCallableProviders();
		final DeviceException expected = new DeviceException("expected");
		when(callablec1.call()).thenThrow(expected);
		final ScanDataPoint point = createScanDataPointWithCallableProviders("point");
		pipeline.put(point);
		Thread.sleep(1000); // make sure the bad point has been computed
		// Should get exception on next put

		try {
			pipeline.shutdown(true);
			fail("DeviceException expected");
		} catch (final DeviceException e) {
			assertEquals(expected, e.getCause());
		}
	}

	@Test
	public void testPopAndBroadcastOnShutdownPipeline() throws Exception {
		pipeline.shutdown(true);
		configureMockScannablesAndDetectors();
		final ScanDataPoint point = createScanDataPoint();

		try {
			pipeline.put(point);
			fail("DeviceException expected");
		} catch (final DeviceException e) {
			assertEquals("Could not add new point to MultithreadedScanDataPointPipeline as it is shutdown.",
					e.getMessage());
		}
	}

	class ScanLikeRun implements Runnable {

		private final ScanDataPoint _point1;
		private final ScanDataPoint _point2;
		private final ScanDataPoint _point3;

		public ScanLikeRun(final ScanDataPoint point1, final ScanDataPoint point2, final ScanDataPoint point3) {
			this._point1 = point1;
			this._point2 = point2;
			this._point3 = point3;
		}

		@Override
		public void run() {
			try {
				pipeline.put(_point1);
				System.out.println("point1 put to pipeline");
				reached1 = true;
				pipeline.put(_point2);
				reached2 = true;
				System.out.println("point2 put to pipeline");

				if (_point3 != null) {
					pipeline.put(_point3);
					reached3 = true;
					System.out.println("point3 put to pipeline");
				}
				pipeline.shutdown(true);
				System.out.println("shutdown complete");
			} catch (final Exception e) {
				caughtException = e;
			}
		}
	}

}