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

import org.dawnsci.common.richbeans.beans.BeansFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.util.beans.xml.XMLRichBean;


public class VortexMFMappableDataProviderTest {
	final static String testScratchDirectoryName = TestUtils
			.generateDirectorynameFromClassname(VortexMFMappableDataProviderTest.class.getCanonicalName());
	private static VortexMFMappableDataProvider vortexDataProvider;
	
	private final String filename = "testfiles/uk/ac/gda/client/microfocus/util/190_testing_1.nxs";

	@SuppressWarnings("unchecked")
	@BeforeClass
	public static void beforeClass() throws Exception {
		TestUtils.makeScratchDirectory(testScratchDirectoryName);
		Class<?> c = Class.forName("uk.ac.gda.beans.xspress.XspressParameters");
		Class<?> c1 = Class.forName("uk.ac.gda.beans.vortex.VortexParameters");
		BeansFactory.setClasses((Class<? extends XMLRichBean>[]) new Class<?>[] { c, c1 });

	}

	@SuppressWarnings("unused")
	@Before
	public void beforeMethod() throws Exception {
		vortexDataProvider = new VortexMFMappableDataProvider();
		vortexDataProvider.setXScannableName("sc_MicroFocusSampleX");
		vortexDataProvider.setYScannableName("sc_MicroFocusSampleY");
		vortexDataProvider.setZScannableName("sc_sample_z");
		vortexDataProvider.setSelectedElement("Pb");
		vortexDataProvider.setSelectedChannel(0);
		vortexDataProvider.setBeanFilePath("testfiles/uk/ac/gda/client/microfocus/util/Vortex_Parameters.xml");
		vortexDataProvider.loadBean();
	}

	@Test
	public void testGetWindowsFromBean() {

		List<DetectorROI>[] roi = vortexDataProvider.getElementRois();
		assertEquals(roi.length, 4, 0);
	}

	@SuppressWarnings("unused")
	@Test
	public void testLoadData() throws Exception {
		vortexDataProvider.loadData(filename);
		Double[] x = vortexDataProvider.getXarray();
		assertEquals(9, x.length);
		Double[] y = vortexDataProvider.getYarray();
		assertEquals(3, y.length);
		assertArrayEquals(new Double[] { 0.0, 0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0 }, x);
		assertArrayEquals(new Double[] { 0.0, 1.5, 3.0 }, y);
	}

	@SuppressWarnings("unused")
	@Test
	public void testConstructMappableDatafromXmap() throws Exception {
		vortexDataProvider.loadData(filename);

		double d[][] = vortexDataProvider.constructMappableData();
		assertEquals(9.25067484E8, d[0][0], 0.0);
		assertEquals(3, d.length);
		assertEquals(9, d[0].length);
		assertEquals(6.14512929E8, d[1][3], 0.0);
		double d1[][] = vortexDataProvider.constructMappableData();
		assertEquals(9.25067484E8, d1[0][0], 0.0);
	}

	@SuppressWarnings("unused")
	@Test
	public void testConstructMappableDatafromXmap2() throws Exception {
		vortexDataProvider.loadData(filename);
		vortexDataProvider.setSelectedElement("Pb");
		double d[][] = vortexDataProvider.constructMappableData();
		assertEquals(9.25067484E8, d[0][0], 0.0);
		assertEquals(2.0913034E7, d[2][0], 0.0);
		assertEquals(3, d.length);
		assertEquals(9, d[0].length);
		vortexDataProvider.setSelectedElement("Ba");
		d = vortexDataProvider.constructMappableData();
		assertEquals(8.66487699E8, d[0][0], 0.0);
		assertEquals(2.147483647E9, d[1][0], 0.0);

	}

	@SuppressWarnings("unused")
	@Test
	public void testGetSpectrum() throws Exception {
		vortexDataProvider.setBeanFilePath("testfiles/uk/ac/gda/client/microfocus/util/Vortex_Parameters.xml");
		vortexDataProvider.loadBean();
		vortexDataProvider.loadData(filename);
		vortexDataProvider.setSelectedElement("Pb");
		// vortexDataProvider.constructMappableData();
		double[] d = vortexDataProvider.getSpectrum(0, 0, 0);
		assertEquals(1024, d.length, 0.0);
		double[] d1 = vortexDataProvider.getSpectrum(0, 3, 2);
		assertEquals(1.242868791225867E7, d1[3], 0.0);
		d1 = vortexDataProvider.getSpectrum(2, 3, 2);
		assertEquals(530351.9950189762, d1[4], 0.0);
	}

	@SuppressWarnings("unused")
	@Test
	public void testHasPlottableData() throws Exception {
		assertTrue(vortexDataProvider.hasPlotData("Pb"));
		assertFalse(vortexDataProvider.hasPlotData("unused"));
	}

	@SuppressWarnings("unused")
	@After
	public void afterMethod() throws Exception {
		vortexDataProvider = null;
	}

}
