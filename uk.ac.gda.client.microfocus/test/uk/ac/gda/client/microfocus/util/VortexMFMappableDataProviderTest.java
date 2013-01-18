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

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import uk.ac.gda.beans.BeansFactory;
import uk.ac.gda.beans.IRichBean;
import uk.ac.gda.beans.vortex.RegionOfInterest;
import uk.ac.gda.util.PackageUtils;

//@Ignore("2010/03/04 Test ignored as MicroFocusMappableDataProvider now calls getExtensionRegistry, so this needs to be converted to a Plug-in Test.")
public class VortexMFMappableDataProviderTest {
	final static String testScratchDirectoryName =
		TestUtils.generateDirectorynameFromClassname(VortexMFMappableDataProviderTest.class.getCanonicalName());
	private static VortexMFMappableDataProvider vortexDataProvider;
	@SuppressWarnings("unchecked")
	@BeforeClass
	public static void beforeClass() throws Exception{
		TestUtils.makeScratchDirectory(testScratchDirectoryName);
		Class<?> c = Class.forName("uk.ac.gda.beans.xspress.XspressParameters");
		Class<?> c1 = Class.forName("uk.ac.gda.beans.vortex.VortexParameters");
		BeansFactory.setClasses((Class<? extends IRichBean>[]) new Class<?>[]{c, c1});
		
	}
	
	@SuppressWarnings("unused")
	@Before
	public void beforeMethod() throws Exception
	{
		vortexDataProvider = new VortexMFMappableDataProvider();
		vortexDataProvider.setXScannableName("sc_MicroFocusSampleX");
		vortexDataProvider.setYScannableName("sc_MicroFocusSampleY");
		vortexDataProvider.setZScannableName("sc_sample_z");
		vortexDataProvider.setSelectedElement("Pb");
		vortexDataProvider.setBeanFilePath(PackageUtils.getTestPath(getClass(), "test")+"Vortex_Parameters.xml");
		vortexDataProvider.loadBean();
	}
	
	@Test
	public void testGetWindowsFromBean()
	{
		
		List<RegionOfInterest>[] roi = vortexDataProvider.getElementRois();
		assertEquals(roi.length, 4, 0);
	}
	@SuppressWarnings("unused")
	@Test
	public void testLoadData() throws Exception
	{		
		vortexDataProvider.loadData(PackageUtils.getTestPath(getClass(), "test")+"i18-286.nxs");
		Double[] x = vortexDataProvider.getXarray();
		assertEquals(4, x.length);
		Double[] y = vortexDataProvider.getYarray();
		assertEquals(9, y.length);
		assertArrayEquals(new Double[]{0.10000000000000009, 0.2, 0.30000000000000004, 0.4}, x);
		assertArrayEquals(new Double[]{ 0.0, 0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0}, y);
	}
	
	

	@SuppressWarnings("unused")
	@Test
	public void testConstructMappableDatafromXmap() throws Exception
	{	
		vortexDataProvider.loadData(PackageUtils.getTestPath(getClass(), "test")+"i18-286.nxs");		
		
		double d[][] = vortexDataProvider.constructMappableData();
		assertEquals(6.295973882E9, d[0][0], 0.0);
		assertEquals(9, d.length);
		assertEquals(4, d[0].length);
		assertEquals(6.181605967E9, d[1][3], 0.0);
		double d1[][] = vortexDataProvider.constructMappableData();
		assertEquals(6.295973882E9, d1[0][0], 0.0);
	}

	@SuppressWarnings("unused")
	@Test
	public void testConstructMappableDatafromXmap2() throws Exception
	{	
		vortexDataProvider.loadData(PackageUtils.getTestPath(getClass(), "test")+"i18-286.nxs");		
		vortexDataProvider.setSelectedElement("Pb");
		double d[][] = vortexDataProvider.constructMappableData();
		assertEquals(6.295973882E9, d[0][0], 0.0);
		assertEquals(6.314794994E9, d[2][0], 0.0);
		assertEquals(9, d.length);
		assertEquals(4, d[0].length);
		vortexDataProvider.setSelectedElement("ROI2");
		d = vortexDataProvider.constructMappableData();
		assertEquals(6.15153942E9, d[0][0], 0.0);
		assertEquals(6.075139354E9, d[1][0], 0.0);
		
	}
	
	@SuppressWarnings("unused")
	@Test
	public void testGetSpectrum() throws Exception
	{
		vortexDataProvider.setBeanFilePath(PackageUtils.getTestPath(getClass(), "test")+"Vortex_Parameters.xml");
		vortexDataProvider.loadBean();
		vortexDataProvider.loadData(PackageUtils.getTestPath(getClass(), "test")+"i18-286.nxs");
		vortexDataProvider.setSelectedElement("Pb");
		//vortexDataProvider.constructMappableData();
		double[] d = vortexDataProvider.getSpectrum(0, 0, 0);
		assertEquals(1024, d.length, 0.0);
		double []d1 = vortexDataProvider.getSpectrum(0, 2, 3);
		assertEquals(8132554.0, d1[3], 0.0);
		d1 = vortexDataProvider.getSpectrum(2, 2, 3);
		assertEquals(9847832.0, d1[4], 0.0);
	}
	@SuppressWarnings("unused")
	@Test
	public void testHasPlottableData() throws Exception
	{
		assertTrue(vortexDataProvider.hasPlotData("Pb"));
		assertFalse(vortexDataProvider.hasPlotData("unused"));
	}
	
@SuppressWarnings("unused")
@After
	public void afterMethod()throws Exception
	{
		vortexDataProvider = null;
	}
}
