/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.device.detector.xmap.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import java.nio.file.Paths;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import gda.util.TestUtils;

public class XmapBufferedHdf5FileLoaderTest {

	static String TestFileFolder;
	private XmapBufferedHdf5FileLoader xMapLoader;

	@BeforeClass
	public static void beforeClass() {
		TestFileFolder = TestUtils.getGDALargeTestFilesLocation();
		if( TestFileFolder == null){
			fail("TestUtils.getGDALargeTestFilesLocation() returned null - test aborted");
		}
	}

	@Before
	public void setUp()throws Exception
	{
		String testfile1 = TestFileFolder + "/uk.ac.gda.devices.xmap.epics.test/i18-2309-0-HTXmapMca.h5";
		xMapLoader = new XmapBufferedHdf5FileLoader(testfile1);
		xMapLoader.setAttributeDataGroup("/entry/instrument/detector/NDAttributes/");
		xMapLoader.loadFile();

	}
	@Test
	public void testgetData() {
		short  [][]result = xMapLoader.getData(0);
		assertEquals(result[0][513], (short)0);
	}

	@Test
	public void testgetTrigger()  {
		double result = xMapLoader.getTrigger(0, 0);
		assertEquals(601.0,result,  0.0);
	}
	@Test
	public void testgetRealTime()  {
		double result = xMapLoader.getRealTime(0, 0);
		assertEquals(result,0.020898240000000002, 0.0);
	}

	@Test
	public void testgetLiveTime(){
		double result = xMapLoader.getLiveTime(0, 0);
		assertEquals(result,0.02067104, 0.0);
	}
	@Test
	public void testgetEvents(){
		double result = xMapLoader.getEvents(0, 0);
		assertEquals(588, result, 0.0);
	}

	@Test
	public void testReadB18File() throws Exception {
		String filepath = "testfiles/gda/device/detector/xmap/test-400887-0-qexafs_xmap.h5";
		int totalNumDataPoints = 90;
		int numDetectorElements = 4;
		int numMcaChannels = 2048;

		String testfile1 = Paths.get(filepath).toAbsolutePath().toString();
		XmapBufferedHdf5FileLoader xMapLoader = new XmapBufferedHdf5FileLoader(testfile1);
		xMapLoader.setAttributeDataGroup("/entry/instrument/NDAttributes");

		xMapLoader.loadFile();
		assertEquals(totalNumDataPoints, xMapLoader.getNumberOfDataPoints()); // num points in scan
		// get MCA data for first data point and check the shape
		short[][] result = xMapLoader.getData(0);
		assertEquals(numDetectorElements, result.length); // num detector elements
		assertEquals(numMcaChannels, result[0].length); // num MCA channels

		// Get MCA data for 10 frames, check the shape
		int numFramesToRead = 10;
		short[][][] results = xMapLoader.getData(1, numFramesToRead);
		assertEquals(numFramesToRead, results.length); // num detector elements
		assertEquals(numDetectorElements, results[0].length); // num detector elements
		assertEquals(numMcaChannels, results[0][1].length); // num MCA channels

		// Check that all the attribute data can be read
		for(int i=0; i<totalNumDataPoints; i++) {
			for(int j=0; j<numDetectorElements; j++) {
				String indices = String.format("(%d,%d)", i, j);
				assertNotEquals("getEvents"+indices, Double.NaN, xMapLoader.getEvents(i,  j));
				assertNotEquals("getLiveTime"+indices, Double.NaN, xMapLoader.getLiveTime(i,  j));
				assertNotEquals("getRealTime"+indices, Double.NaN, xMapLoader.getRealTime(i,  j));
				assertNotEquals("getTrigger"+indices, Double.NaN, xMapLoader.getTrigger(i,  j));
			}
		}
	}
}
