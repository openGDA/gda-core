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

import org.junit.Test;

import gda.TestHelpers;
import gda.configuration.properties.LocalProperties;
import gda.device.detector.BufferedDetector;
import gda.device.detector.DummyBufferedDetector;
import gda.device.monitor.DummyMonitor;
import gda.device.scannable.DummyContinuouslyScannable;
import gda.device.scannable.DummyScannable;
import gda.jython.InterfaceProvider;

/**
 *
 */
public class ContinuousScanTest {

	private static final int ScanBaseFirstScanNumber = 100;
	/**
	 *
	 */

	private DummyBufferedDetector detector;
	private DummyContinuouslyScannable scannable;

	/**
	 *
	 */
	public Scan beforeEachTest(){
		LocalProperties.set(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, "SrsDataFile");
		detector = new DummyBufferedDetector();
		detector.setName("det1");

		scannable = new DummyContinuouslyScannable();
		scannable.setName("scannable");
		scannable.addObserver(detector); // acts as a virtual trigger

		return new ContinuousScan(scannable, 50., 200., 10, 0.1, new BufferedDetector[] { detector });

	}

	/**
	 * @throws Exception
	 *
	 */
	@Test
	public void simpleScanScanBaseSetsScanNumber() throws Exception {

		String dir = TestHelpers.setUpTest(ContinuousScanTest.class, "simpleScanScanBaseSetsScanNumber", true);
		LocalProperties.setScanSetsScanNumber(true);
		LocalProperties.set("gda.scanbase.firstScanNumber", Integer.toString(ScanBaseFirstScanNumber));
		Scan scan = beforeEachTest();
		assertEquals(-1,scan.getScanNumber());
		scan.runScan();
		assertEquals(ScanBaseFirstScanNumber,scan.getScanNumber());

		// check that the detector has been operated the expected number of times.
		int[][] data = (int[][]) detector.readAllFrames();
		assertEquals(10, data.length);
		IScanDataPoint point = InterfaceProvider.getScanDataPointProvider().getLastScanDataPoint();
		assertEquals(ScanBaseFirstScanNumber, point.getScanIdentifier());
		assertEquals(dir + "/Data/" + ScanBaseFirstScanNumber + ".dat",point.getCurrentFilename());
	}

	@Test
	public void simpleScanDataWriterSetsScanNumber() throws Exception {

		String dir = TestHelpers.setUpTest(ContinuousScanTest.class, "simpleScanDataWriterSetsScanNumber", true);
		LocalProperties.setScanSetsScanNumber(false);
		Scan scan = beforeEachTest();
		assertEquals(-1,scan.getScanNumber());
		scan.runScan();
		Thread.sleep(5000);
		assertEquals(1, scan.getScanNumber());

		// check that the detector has been operated the expected number of times.
		int[][] data = (int[][]) detector.readAllFrames();
		assertEquals(10, data.length);
		IScanDataPoint point = InterfaceProvider.getScanDataPointProvider().getLastScanDataPoint();
		assertEquals(1, point.getScanIdentifier());
		assertEquals(dir + "/Data/1.dat", scan.getDataWriter().getCurrentFileName());
	}

	/**
	 * @throws Exception
	 *
	 */
	@Test
	public void multiDimensionalScan() throws Exception {
		TestHelpers.setUpTest(ContinuousScanTest.class, "multiDimensionalScan", true);
		beforeEachTest();

		DummyBufferedDetector detector = new DummyBufferedDetector();
		detector.setName("det1");

		DummyContinuouslyScannable scannable = new DummyContinuouslyScannable();
		scannable.setName("scannable");
		scannable.addObserver(detector); // acts as a virtual trigger

		ContinuousScan scan = new ContinuousScan(scannable, 50., 200., 10, 0.1, new BufferedDetector[] { detector });

		DummyScannable temp01 = new DummyScannable();
		temp01.setName("temp01");

		DummyMonitor mon01 = new DummyMonitor();
		mon01.setName("mon01");

		ConcurrentScan mainScan = new ConcurrentScan(new Object[] { temp01, 10, 20, 5, scan, mon01 });

		mainScan.runScan();

		// check that the detector only has 50 entries left in its buffer as its should be emptied every sub scan
		int[][] data = (int[][]) detector.readAllFrames();
		assertEquals(10, data.length);
	}

	/**
	 * @throws Exception
	 *
	 */
	@Test
	public void concurrentScan() throws Exception {
		String dir = TestHelpers.setUpTest(ContinuousScanTest.class, "concurrentScan", true);
		LocalProperties.setScanSetsScanNumber(false);
		beforeEachTest();

		DummyScannable temp01 = new DummyScannable();
		temp01.setName("temp01");

		DummyMonitor mon01 = new DummyMonitor();
		mon01.setName("mon01");

		ConcurrentScan mainScan = new ConcurrentScan(new Object[] { temp01, 10, 20, 5, mon01 });
		assertEquals(-1,mainScan.getScanNumber());
		mainScan.runScan();
		assertEquals(1, mainScan.getScanNumber());
		IScanDataPoint point = InterfaceProvider.getScanDataPointProvider().getLastScanDataPoint();
		assertEquals(1, point.getScanIdentifier());
		assertEquals(dir + "/Data/1.dat", point.getCurrentFilename());

	}



	/**
	 * @throws Exception
	 *
	 */
	@Test
	public void anotherMultiDimensionalScan() throws Exception {
		TestHelpers.setUpTest(ContinuousScanTest.class, "anotherMultiDimensionalScan", true);
		beforeEachTest();

		DummyBufferedDetector detector = new DummyBufferedDetector();
		detector.setName("det1");

		DummyContinuouslyScannable scannable = new DummyContinuouslyScannable();
		scannable.setName("scannable");
		scannable.addObserver(detector); // acts as a virtual trigger

		ContinuousScan scan = new ContinuousScan(scannable, 50., 200., 10, 0.01, new BufferedDetector[] { detector });

		//1st dimension
		DummyScannable temp01 = new DummyScannable();
		temp01.setName("temp01");

		//2nd dimension
		DummyScannable temp02 = new DummyScannable();
		temp02.setName("temp02");

		// should be moved to 3 and stay there
		DummyScannable temp03 = new DummyScannable();
		temp03.setName("temp03");

		DummyMonitor mon01 = new DummyMonitor();
		mon01.setName("mon01");

		ConcurrentScan mainScan = new ConcurrentScan(new Object[] { temp01, 10, 20, 5, temp02, 1, 2, .5,scan, mon01, temp03,3 });

		mainScan.runScan();

		assertEquals(10,detector.getNumberFrames());
		assertEquals(3.0,temp03.getPosition());
	}

	/**
	 * @throws Exception
	 *
	 */
	@Test
	public void multiDimensionalScanCount() throws Exception{
		String dir = TestHelpers.setUpTest(ContinuousScanTest.class, "multiDimensionalScanCount", true);
		LocalProperties.setScanSetsScanNumber(true);
		LocalProperties.set(ScanBase.GDA_SCANBASE_FIRST_SCAN_NUMBER_FOR_TEST, Integer.toString(ScanBaseFirstScanNumber));
		beforeEachTest();

		DummyBufferedDetector detector = new DummyBufferedDetector();
		detector.setName("det1");

		DummyContinuouslyScannable scannable = new DummyContinuouslyScannable();
		scannable.setName("scannable");
		scannable.addObserver(detector); // acts as a virtual trigger

		ContinuousScan scan = new ContinuousScan(scannable, 50., 200., 10, 0.1, new BufferedDetector[] { detector });

		DummyScannable temp01 = new DummyScannable();
		temp01.setName("temp01");

		DummyMonitor mon01 = new DummyMonitor();
		mon01.setName("mon01");

		ConcurrentScan mainScan = new ConcurrentScan(new Object[] { temp01, 10, 20, 5, scan, mon01 });

		assertEquals(-1,mainScan.getScanNumber());
		mainScan.runScan();
		assertEquals(ScanBaseFirstScanNumber,mainScan.getScanNumber());
		// but check that the detector has been triggered 150 times
//		verify(detector, times(30)).addPoint();
		assertEquals(10,detector.getNumberFrames());

		IScanDataPoint point = InterfaceProvider.getScanDataPointProvider().getLastScanDataPoint();
		assertEquals(ScanBaseFirstScanNumber, point.getScanIdentifier());
		assertEquals(dir + "/Data/" + ScanBaseFirstScanNumber + ".dat", point.getCurrentFilename());

	}

	/**
	 * @throws Exception
	 *
	 */
	@Test
	public void multiDimensionalScanCountDataWriterSetsScanId() throws Exception{
		String dir = TestHelpers.setUpTest(ContinuousScanTest.class, "multiDimensionalScanCountDataWriterSetsScanId", true);
		LocalProperties.setScanSetsScanNumber(false);
		beforeEachTest();

		DummyBufferedDetector detector = new DummyBufferedDetector();
		detector.setName("det1");

		DummyContinuouslyScannable scannable = new DummyContinuouslyScannable();
		scannable.setName("scannable");
		scannable.addObserver(detector); // acts as a virtual trigger

		ContinuousScan scan = new ContinuousScan(scannable, 50., 200., 10, 0.1, new BufferedDetector[] { detector });

		DummyScannable temp01 = new DummyScannable();
		temp01.setName("temp01");

		DummyMonitor mon01 = new DummyMonitor();
		mon01.setName("mon01");

		ConcurrentScan mainScan = new ConcurrentScan(new Object[] { temp01, 10, 20, 5, scan, mon01 });

		assertEquals(-1,mainScan.getScanNumber());
		mainScan.runScan();
		assertEquals(1, mainScan.getScanNumber());
		// but check that the detector has been triggered 150 times
//		verify(detector, times(30)).addPoint();
		assertEquals(10,detector.getNumberFrames());
		IScanDataPoint point = InterfaceProvider.getScanDataPointProvider().getLastScanDataPoint();
		assertEquals(1, point.getScanIdentifier());
		assertEquals(dir + "/Data/1.dat", point.getCurrentFilename());
	}

	@Test
	public void testBiDirectionalModeInMultidimensionScan() throws Exception{
		String dir = TestHelpers.setUpTest(ContinuousScanTest.class, "testBiDirectionalModeInMultidimensionScan", true);
		LocalProperties.setScanSetsScanNumber(false);
		beforeEachTest();

		DummyBufferedDetector detector = new DummyBufferedDetector();
		detector.setName("det1");

		DummyContinuouslyScannable scannable = new DummyContinuouslyScannable();
		scannable.setName("test");
		scannable.addObserver(detector); // acts as a virtual trigger

		ContinuousScan scan = new ContinuousScan(scannable, 50., 200., 10, 0.1, new BufferedDetector[] { detector });
		scan.setBiDirectional(true);

		DummyScannable temp01 = new DummyScannable();
		temp01.setName("temp01");

		ConcurrentScan mainScan = new ConcurrentScan(new Object[] { temp01, 10, 20, 5, scan });

		assertEquals(-1,mainScan.getScanNumber());
		mainScan.runScan();
		assertEquals(1, mainScan.getScanNumber());
		// but check that the detector has been triggered 150 times
//		verify(detector, times(30)).addPoint();
		assertEquals(10,detector.getNumberFrames());
		IScanDataPoint point = InterfaceProvider.getScanDataPointProvider().getLastScanDataPoint();
		assertEquals(1, point.getScanIdentifier());
		assertEquals(dir + "/Data/1.dat", point.getCurrentFilename());

	}
}
