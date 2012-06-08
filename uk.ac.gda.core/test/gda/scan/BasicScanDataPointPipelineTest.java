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
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import gda.data.scan.datawriter.DataWriter;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.jython.IJythonServerNotifer;
import junitx.framework.ArrayAssert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

/**
 * Tests use real ScanDataPoints as code in ScanDataPoint is not tested. Also uses a real DataPointBroadcaster.
 */
public class BasicScanDataPointPipelineTest {

	class NamedObject {
		private String name;

		NamedObject(String name) {
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

	private ScanDataPointPipeline basicPipeline;

	protected void configurePipeline() {
		basicPipeline = new BasicScanDataPointPipeline(new ScanDataPointPublisher(mockDataWriter, mockScan));
	}

	public ScanDataPointPipeline getBasicPipeline() {
		return basicPipeline;
	}

	public ScanDataPointPipeline getPipeline() {
		return basicPipeline;
	}

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
		ScanDataPoint point = new ScanDataPoint();
		point.addScannable(scna);
		point.addScannable(scnb);
		point.addDetector(deta);
		return point;
	}

	@Test
	public void testPopulatePositionsAndDataAndBroadcast() throws Exception {
		configureMockScannablesAndDetectors();
		ScanDataPoint point1 = createScanDataPoint();
		ScanDataPoint point2 = createScanDataPoint();
		getPipeline().put(point1);
		getPipeline().put(point2);
		getPipeline().shutdown(1000);

		InOrder inOrderBroadcastThread = inOrder(mockDataWriter, mockJythonServerNotifer);
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
		DeviceException expected = new DeviceException("expected");
		when(scnb.getPosition()).thenThrow(expected);
		ScanDataPoint point = createScanDataPoint();
		try {
			getPipeline().put(point);
		} catch (DeviceException e) {
			assertEquals(expected, e.getCause());
		}
	}

	@Test
	public void testGetDataWriter() {
		assertEquals(mockDataWriter, getPipeline().getDataWriter());
	}

	@Test
	public void testShutdown() throws Exception {
		// no exceptions thrown by DataWriter.completeCollection
		getPipeline().shutdown(5000);
		verify(mockDataWriter).completeCollection();
	}

	@Test
	public void testShutdownNow() throws Exception {
		// no exceptions thrown by DataWriter.completeCollection
		getPipeline().shutdownNow();
		verify(mockDataWriter).completeCollection();
	}

}
