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

package uk.ac.gda.client.microfocus.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import gda.util.TestUtils;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import uk.ac.gda.beans.BeansFactory;
import uk.ac.gda.beans.IRichBean;
import uk.ac.gda.beans.xspress.XspressROI;

@Ignore("2010/03/04 Test ignored as MicroFocusMappableDataProvider now calls getExtensionRegistry, so this needs to be converted to a Plug-in Test.")
public class XspressMFMappableDataProviderTest {
	final static String testScratchDirectoryName =
		TestUtils.generateDirectorynameFromClassname(XspressMFMappableDataProviderTest.class.getCanonicalName());
	private static XspressMFMappableDataProvider XspressDataProvider;
	@SuppressWarnings("unchecked")
	@BeforeClass
	public static void beforeClass() throws Exception{
		TestUtils.makeScratchDirectory(testScratchDirectoryName);
		Class<?> c = Class.forName("uk.ac.gda.beans.xspress.XspressParameters");
		Class<?> c1 = Class.forName("uk.ac.gda.beans.vortex.VortexParameters");
		BeansFactory.setClasses((Class<? extends IRichBean>[]) new Class<?>[]{c, c1});
		XspressDataProvider = new XspressMFMappableDataProvider();
		XspressDataProvider.setXScannableName("sc_MicroFocusSampleX");
		XspressDataProvider.setYScannableName("sc_MicroFocusSampleY");
		XspressDataProvider.setZScannableName("sc_sample_z");
		XspressDataProvider.setTrajectoryScannableName(new String[]{"realX"});
		XspressDataProvider.setSelectedElement("fe");
	}
	@Test
	public void testGetWindowsfromBean()
	{
		XspressDataProvider.setBeanFilePath("testfiles/uk/ac/gda/client/microfocus/util/Xspress_Parameters.xml");
		XspressDataProvider.loadBean();
		List<XspressROI>[] rois = XspressDataProvider.getElementRois();
		assertEquals(9, rois.length);
		assertEquals("fe",rois[0].get(0).getRoiName());
		assertEquals(3, rois[0].size());
		
	}
	
	@Test
	public void testLoadData()
	{
		//XspressDataProvider.loadData(PackageUtils.getTestPath(getClass(), "test")+"i18-284.nxs");
		XspressDataProvider.loadData("testfiles/uk/ac/gda/client/microfocus/util/vortex_map_1_8472.nxs");
		Double[] x = XspressDataProvider.getXarray();
		assertEquals(11, x.length);
		Double[] y = XspressDataProvider.getYarray();
		assertEquals(11, y.length);
		assertArrayEquals(new Double[]{0.5, 0.55, 0.6, 0.6500000000000001, 0.7000000000000001, 0.75, 0.8, 0.8500000000000001, 0.9000000000000001, 0.9500000000000001, 1.0},x);
		assertArrayEquals(new Double[]{ 3.0, 3.0500000000000003, 3.100000000000001, 3.1500000000000004, 3.2, 3.250000000000001, 3.3000000000000003, 3.35, 3.4000000000000004, 3.45, 3.5}, y);
	}
	
	
	/*@Test
	public void testGetElementData()
	{
		double d[][][] = XspressDataProvider.getElementData(0);
		assertEquals(3, d.length);
		assertEquals(8, d[0].length);
		assertEquals(4096, d[0][0].length);
		assertEquals(4096, d[2][2].length);
		assertEquals(11138.487848666526, d[0][0][156],0.0);
		d = XspressDataProvider.getElementData(3);
		assertEquals(14144.11949196154, d[0][0][156],0.0);
		
	}*/
	
	@Test
	public void testConstructMappableDatafromXspress()
	{
		double d[][] = XspressDataProvider.constructMappableData();
		assertEquals(9370.0, d[0][0], 0.0);
		assertEquals(11, d.length);
		assertEquals(11, d[0].length);
		assertEquals(10125.0, d[1][2], 0.0);
	}
	
	
	@Test
	public void testHasPlottableData()
	{
		assertTrue(XspressDataProvider.hasPlotData("fe"));
		assertFalse(XspressDataProvider.hasPlotData("unused"));
	}

}
