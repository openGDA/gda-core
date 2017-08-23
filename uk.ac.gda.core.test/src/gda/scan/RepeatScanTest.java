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

package gda.scan;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import gda.TestHelpers;
import gda.configuration.properties.LocalProperties;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.RepScanScannable;
import gda.device.scannable.PositionCallableProvider;
import gda.jython.commands.ScannableCommands;

public class RepeatScanTest {

	private class DetectorFrameControllerImpl implements RepScanScannable {
		int numberOfFrames=0;
		@Override
		public void atRepScanStart(int numberOfFrames) throws DeviceException {
			this.numberOfFrames = numberOfFrames;
		}
		public int getNumberOfFrames() {
			return numberOfFrames;
		}
	}


	public class CallableImpl implements Callable<Double> {

		private final Double val;

		public CallableImpl(Double val) {
			this.val = val;
		}

		@Override
		public Double call() throws Exception {
			Thread.sleep(10);
			return val;
		}

	}

	private static final String ScanBaseFirstScanNumber = "1";
	Scannable s1;
	Detector d1;
	DetectorFrameControllerImpl d2;
	DetectorFrameControllerImpl d3;
	@SuppressWarnings("rawtypes")
	@Before
	public void setUp() throws Exception {
		LocalProperties.setScanSetsScanNumber(true);
		LocalProperties.set("gda.scanbase.firstScanNumber", ScanBaseFirstScanNumber);
		s1 = mock(Scannable.class);
		when(s1.getName()).thenReturn("s1");
		when(s1.getInputNames()).thenReturn(new String[] { "s1" });
		when(s1.getExtraNames()).thenReturn(new String[] { });
		when(s1.getOutputFormat()).thenReturn(new String[] { "%5.1f" });
		when(s1.getPosition()).thenReturn(0.);

		d1 = mock(Detector.class);
		when(d1.getName()).thenReturn("d1");
		when(d1.readout()).thenReturn(0.);


		d2 = mock(DetectorFrameControllerImpl.class, Mockito.withSettings().extraInterfaces(Detector.class));
		Detector d2det = (Detector)d2;
		when(d2det.getName()).thenReturn("d2");
		when(d2det.readout()).thenReturn(1.);
		Mockito.doCallRealMethod().when(d2).atRepScanStart(Matchers.anyInt());
		Mockito.doCallRealMethod().when(d2).getNumberOfFrames();

		d3 = mock(DetectorFrameControllerImpl.class, Mockito.withSettings().extraInterfaces(Detector.class, PositionCallableProvider.class));
		Detector d3det = (Detector)d3;
		when(d3det.getName()).thenReturn("d3");
		when(d3det.readout()).thenReturn(1.);
		Mockito.doCallRealMethod().when(d3).atRepScanStart(Matchers.anyInt());
		Mockito.doCallRealMethod().when(d3).getNumberOfFrames();
		PositionCallableProvider d3PositionCallableProvider = (PositionCallableProvider)d3;
		when(d3det.readout()).thenThrow(new DeviceException("readout should not be called as d3 supports PositionCallableProvider"));
		Mockito.doCallRealMethod().when(d3).atRepScanStart(Matchers.anyInt());
		Mockito.doCallRealMethod().when(d3).getNumberOfFrames();
		when(d3PositionCallableProvider.getPositionCallable()).thenReturn(new CallableImpl(new Double(1.0)));

	}

	@Test
	public void testNullArgs() throws Exception {
		setupTest("testNullArgs");
		try{
			RepeatScan.create_repscan((Object[])null);
			fail("Error in args not reported");
		}catch(IllegalArgumentException ex){
			//ok
		}
	}

	@Test
	public void testSingleScannable() throws Exception {
		setupTest("testSingleScannable");
		ConcurrentScan scan = RepeatScan.create_repscan(s1);
		assertTrue(1 == scan.getNumberPoints());
		assertNull(scan.getChild());
		assertTrue(2 == scan.allScannables.size());
		assertEquals("scan index FrameProvider [totalFrames=1] s1", scan.command);
		ExplicitScanObject explicitScanObject = (ExplicitScanObject)scan.allScanObjects.get(0);
		assertEquals( 1, explicitScanObject.points.size());
	}

	@Test
	public void testSingleDetector() throws Exception {
		setupTest("testSingleDetector");
		ConcurrentScan scan = RepeatScan.create_repscan(d1);
		assertTrue(1 == scan.getNumberPoints());
		assertNull(scan.getChild());
		assertTrue(1 == scan.allScannables.size());
		assertTrue(1 == scan.allDetectors.size());
		assertEquals("scan index FrameProvider [totalFrames=1] d1", scan.command);
		ExplicitScanObject explicitScanObject = (ExplicitScanObject)scan.allScanObjects.get(0);
		assertEquals( 1, explicitScanObject.points.size());
	}

	@Test
	public void testScanScannableAndDetector() throws Exception {
		setupTest("testScanScannableAndDetector");
		ConcurrentScan scan = RepeatScan.create_repscan(s1,d1);
		assertTrue(1 == scan.getNumberPoints());
		assertNull(scan.getChild());
		assertTrue(2 == scan.allScannables.size());
		assertTrue(1 == scan.allDetectors.size());
		assertEquals("scan index FrameProvider [totalFrames=1] s1 d1", scan.command);
		ExplicitScanObject explicitScanObject = (ExplicitScanObject)scan.allScanObjects.get(0);
		assertEquals( 1, explicitScanObject.points.size());
	}

	@Test
	public void test10Frames() throws Exception {
		setupTest("test10Frames");
		ConcurrentScan scan = RepeatScan.create_repscan(10, s1, d1);
		assertTrue(10 == scan.getNumberPoints());
		assertNull(scan.getChild());
		assertTrue(2 == scan.allScannables.size());
		assertTrue(1 == scan.allDetectors.size());
		assertEquals("scan index FrameProvider [totalFrames=10] s1 d1", scan.command);
		ExplicitScanObject explicitScanObject = (ExplicitScanObject)scan.allScanObjects.get(0);
		assertEquals( 10, explicitScanObject.points.size());
	}

	@Test
	public void testCDetectorFrameController() throws Exception {
		setupTest("testCDetectorFrameController");
		//check frames are set by the creation of the scan
		assertEquals( 0,d2.getNumberOfFrames());
		ConcurrentScan scan = RepeatScan.create_repscan(10, s1, d2);
		assertTrue(10 == scan.getNumberPoints());
		assertNull(scan.getChild());
		assertTrue(2 == scan.allScannables.size());
		assertTrue(1 == scan.allDetectors.size());
		assertEquals("scan index FrameProvider [totalFrames=10] s1 d2", scan.command);
		ExplicitScanObject explicitScanObject = (ExplicitScanObject)scan.allScanObjects.get(0);
		assertEquals( 10, explicitScanObject.points.size());
		scan.runScan();
		assertEquals( 10,d2.getNumberOfFrames());
	}

	@Test
	public void testPositionCallableProvider() throws Exception {
		setupTest("testPositionCallableProvider");
		//check frames are set by the creation of the scan
		assertEquals( 0,d2.getNumberOfFrames());
		ConcurrentScan scan = RepeatScan.create_repscan(10, s1, d2, d3);
		assertTrue(10 == scan.getNumberPoints());
		assertNull(scan.getChild());
		assertTrue(2 == scan.allScannables.size());
		assertTrue(2 == scan.allDetectors.size());
		assertEquals("scan index FrameProvider [totalFrames=10] s1 d2 d3", scan.command);
		ExplicitScanObject explicitScanObject = (ExplicitScanObject)scan.allScanObjects.get(0);
		assertEquals( 10, explicitScanObject.points.size());
		scan.runScan();
		assertEquals( 10,d2.getNumberOfFrames());
		assertEquals( 10,d3.getNumberOfFrames());
		assertEquals( 1, scan.getPositionCallableThreadPoolSize());
		assertEquals( 10, scan.getScanDataPointQueueLength());
	}

	@Test
	public void testOuterScan() throws Exception {
		String testScratchDirectoryName = setupTest("testOuterScan");
		//check frames are set by the creation of the scan
		assertEquals( 0,d3.getNumberOfFrames());
		ConcurrentScan scan = RepeatScan.create_repscan(10, d3);
		assertTrue(10 == scan.getNumberPoints());
		assertNull(scan.getChild());
		assertTrue(1 == scan.allScannables.size());
		assertTrue(1 == scan.allDetectors.size());
		ConcurrentScan outer = ScannableCommands.createConcurrentScan(s1, 1, 10, 1, scan);
		outer.runScan();
		assertEquals( 10,d3.getNumberOfFrames());
		//value increased by createConcurrentScan to 3
		assertEquals( 3, outer.getPositionCallableThreadPoolSize());
		assertEquals( 10, outer.getScanDataPointQueueLength());

		assertArrayEquals(Files.readAllBytes(Paths.get(TestFileFolder + "testOuterScan/Data/expected.dat")),
				Files.readAllBytes(Paths.get(testScratchDirectoryName + "/Data/" + ScanBaseFirstScanNumber + ".dat")));

	}
	final static String TestFileFolder = "testfiles/gda/scan/RepeatScanTest/";

	private String setupTest(String name) throws Exception{
		String scratchDir = TestHelpers.setUpTest(RepeatScanTest.class,name , true);
		LocalProperties.set(LocalProperties.GDA_DATA_SCAN_DATAWRITER_DATAFORMAT, "SrsDataFile");
		return scratchDir;
	}
	@Test
	public void testInnerScan() throws Exception {
		setupTest("testInnerScan");
		try{
			RepeatScan.create_repscan(10, d3, s1, 1, 10, 1);
			fail("Error with inner scan not reported");
		}catch(IllegalArgumentException ex){
			//ok
		}
	}

}
