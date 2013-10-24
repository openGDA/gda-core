/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

import org.junit.Test;

public class XmapNexusFileLoaderTest {

	@Test
	public void testRead4ElementFile() throws Exception {
		String testfile1 = "testfiles/gda/device/detector/xmap/util/i18-6777-HTXmapMca.h5";
		XmapNexusFileLoader xMapLoader = new XmapNexusFileLoader(testfile1,4);
		xMapLoader.loadFile();
		assertEquals(58,xMapLoader.getNumberOfDataPoints());
		short[][] result = xMapLoader.getData(0);
		assertEquals(4, result.length);
		assertEquals(2048, result[0].length);
		
	}
	
	@Test
	public void testRead10ElementFile() throws Exception {
		String testfile1 = "testfiles/gda/device/detector/xmap/util/i18-26092-0-raster_xmap.h5";
		XmapNexusFileLoader xMapLoader = new XmapNexusFileLoader(testfile1,10);
		xMapLoader.loadFile();
		assertEquals(401,xMapLoader.getNumberOfDataPoints());
		short[][] result = xMapLoader.getData(0);
		assertEquals(10, result.length);
		assertEquals(2048, result[0].length);
	}

}
