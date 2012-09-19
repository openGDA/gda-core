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

import org.junit.Before;
import org.junit.Test;

public class XmapBufferedHdf5FileLoaderTest {
	
	XmapBufferedHdf5FileLoader xMapLoader;
	@Before
	public void setUp()throws Exception
	{
		String testfile1 = "testfiles/gda/device/detector/xmap/util/i18-2309-0-HTXmapMca.h5";//"testfiles/gda/device/detector/xmap/util/vortex-fast-raster-exp2.h5";
		 xMapLoader = new XmapBufferedHdf5FileLoader(testfile1);
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

}
