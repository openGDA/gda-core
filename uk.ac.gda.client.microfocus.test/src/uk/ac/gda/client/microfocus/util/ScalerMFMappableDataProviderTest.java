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
import static org.junit.Assert.fail;
import gda.util.TestUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.gda.util.beans.BeansFactory;
import uk.ac.gda.util.beans.xml.XMLRichBean;

public class ScalerMFMappableDataProviderTest {


	private static ScalerMFMappableDataProvider scalerDataProvider;
	static String TestFileFolder;

	@SuppressWarnings("unchecked")
	@BeforeClass
	public static void beforeClass() throws Exception{
		TestFileFolder = TestUtils.getGDALargeTestFilesLocation();
		Class<?> c = Class.forName("uk.ac.gda.beans.xspress.XspressParameters");
		Class<?> c1 = Class.forName("uk.ac.gda.beans.vortex.VortexParameters");
		Class<?> c2 = Class.forName("uk.ac.gda.beans.exafs.DetectorParameters");
		BeansFactory.setClasses((Class<? extends XMLRichBean>[]) new Class<?>[]{c, c1, c2});
	}

	@SuppressWarnings("unused")
	@Before
	public void setUp()throws Exception
	{
		scalerDataProvider = new ScalerMFMappableDataProvider();
		scalerDataProvider.setXScannableName("sc_MicroFocusSampleX");
		scalerDataProvider.setYScannableName("sc_MicroFocusSampleY");
		scalerDataProvider.setZScannableName("sc_sample_z");
		scalerDataProvider.setSelectedElement("I0");
		scalerDataProvider.setBeanFilePath("testfiles/uk/ac/gda/client/microfocus/util/ScalerMFMappableDataProviderTest/Detector_Parameters.xml");
		scalerDataProvider.loadBean();

	}


	@SuppressWarnings("unused")
	@Test
	public void testLoadBean() throws Exception
	{
		String[] elementNames = scalerDataProvider.getElementNames();
		assertEquals(3, elementNames.length);
		assertEquals(elementNames[0], "I0");
		assertEquals("Iref", elementNames[2]);
	}

	@SuppressWarnings("unused")
	@Test
	public void testLoadData() throws Exception
	{
		if( TestFileFolder == null){
			fail("TestUtils.getGDALargeTestFilesLocation() returned null - test aborted");
		}
		scalerDataProvider.loadData(TestFileFolder + "uk.ac.gda.client.microfocus.util/vortex_map_1_8472.nxs");
		Double[] x = scalerDataProvider.getXarray();
		assertEquals(11, x.length);
		Double[] y = scalerDataProvider.getYarray();
		assertEquals(11, y.length);
		assertArrayEquals(new Double[]{0.5, 0.55, 0.6, 0.6500000000000001, 0.7000000000000001, 0.75, 0.8, 0.8500000000000001, 0.9000000000000001, 0.9500000000000001, 1.0},x);
		assertArrayEquals(new Double[]{ 3.0, 3.0500000000000003, 3.100000000000001, 3.1500000000000004, 3.2,
				3.250000000000001, 3.3000000000000003, 3.35, 3.4000000000000004, 3.45, 3.5}, y);
		}


	@SuppressWarnings("unused")
	@Test
	public void testConstructMappableDatafromCounter() throws Exception
	{
		if( TestFileFolder == null){
			fail("TestUtils.getGDALargeTestFilesLocation() returned null - test aborted");
		}
		scalerDataProvider.loadData(TestFileFolder + "uk.ac.gda.client.microfocus.util/i18-284.nxs");
		double d[][] = scalerDataProvider.constructMappableData();
		assertEquals(99379.0, d[0][0], 0.0);
		assertEquals(3, d.length);
		assertEquals(8, d[0].length);
		assertEquals(99358.0, d[1][4], 0.0);
		double d1[][] = scalerDataProvider.constructMappableData();
		assertEquals(99379.0, d1[0][0], 0.0);
	}


	@SuppressWarnings("unused")
	@Test
	public void testHasPlottableData() throws Exception
	{
		assertTrue(scalerDataProvider.hasPlotData("I0"));
		assertFalse(scalerDataProvider.hasPlotData("Idrain"));
	}

	@SuppressWarnings("unused")
	@After
	public void tearDown()throws Exception
	{
		scalerDataProvider = null;

	}
}
