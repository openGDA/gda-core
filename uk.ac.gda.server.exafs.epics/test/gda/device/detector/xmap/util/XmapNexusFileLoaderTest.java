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

package gda.device.detector.xmap.util;

import static org.junit.Assert.assertEquals;
import gda.device.detector.xmap.util.XmapNexusFileLoader;

import org.junit.Before;
import org.junit.Test;

public class XmapNexusFileLoaderTest {
	XmapNexusFileLoader xMapLoader;
	@Before
	public void setUp()throws Exception
	{
		String testfile1 = "testfiles/gda/device/detector/xmap/util/i18-6777-HTXmapMca.h5";		
		xMapLoader = new XmapNexusFileLoader(testfile1);
		xMapLoader.loadFile();
	
	}
	@Test
	public void testgetData() throws Exception {		
		short  [][]result = xMapLoader.getData(0);
		assertEquals(result[0][513], (short)0);
	}
	
}
