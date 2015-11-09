/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.util.beans.BeansFactory;
import uk.ac.gda.util.beans.xml.XMLRichBean;


public class MicroFocusNexusReaderTest {
	private static MicroFocusNexusReader rdr;
	private static MicroFocusNexusReader rdr2;
	private static MicroFocusNexusReader rdr3;

	@SuppressWarnings("unchecked")
	@BeforeClass
	public static void beforeClass() throws Exception{
		Class<? extends XMLRichBean> c = (Class<? extends XMLRichBean>) Class.forName("uk.ac.gda.beans.xspress.XspressParameters");
		Class<? extends XMLRichBean> c1 = (Class<? extends XMLRichBean>) Class.forName("uk.ac.gda.beans.vortex.VortexParameters");
		BeansFactory.setClasses(new Class<?>[]{c, c1});
		rdr = new MicroFocusNexusReader();
		rdr.setXScannableName("MicroFocusSampleX");
		rdr.setYScannableName("MicroFocusSampleY");
		rdr2 = new MicroFocusNexusReader();
		rdr2.setXScannableName("MicroFocusSampleX");
		rdr2.setYScannableName("MicroFocusSampleY");
		rdr2.setDetectorName("counterTimer01");
		rdr3 = new MicroFocusNexusReader();
		rdr3.setXScannableName("MicroFocusSampleX");
		rdr3.setYScannableName("MicroFocusSampleY");
		rdr3.setDetectorName("xmapMca");
	}
	@SuppressWarnings("unused")
	@Test
	public void testGetWindowsfromBean() throws Exception
	{

		List<DetectorROI>[] rois = rdr.getWindowsfromBean("testfiles/uk/ac/gda/client/microfocus/util/Xspress_Parameters.xml");
		assertEquals(9, rois.length);
		assertEquals("fe",rois[0].get(0).getRoiName());
		assertEquals(3, rois[0].size());

	}


	@SuppressWarnings("unused")
	public void testGetElementData() throws Exception
	{
		double d[][][] = rdr.getElementData(0);
		assertEquals(3, d.length);
		assertEquals(5, d[0].length);
		assertEquals(4096, d[0][0].length);
		assertEquals(4096, d[2][4].length);
		assertEquals(-1060.304462163443, d[0][0][156],0.0);
		d = rdr.getElementData(3);
		assertEquals(14251.949956114078, d[0][0][156],0.0);

	}


	@SuppressWarnings("unused")
	@Test
	public void testLoadData2() throws Exception
	{
		rdr2.loadData("testfiles/uk/ac/gda/client/microfocus/util/i18-130.nxs");
		Double[] x = rdr2.getXValues();
		assertEquals(5, x.length);
		Double[] y = rdr2.getYValues();
		assertEquals(3, y.length);
		assertArrayEquals(new Double[]{0.0, 1.0, 2.0, 3.0, 4.0}, x);
		assertArrayEquals(new Double[]{ 0.0, 2.0, 4.0}, y);
	}


	@SuppressWarnings("unused")
	@Test
	public void testConstructMappableDatafromCounter() throws Exception
	{
		rdr2.loadData("testfiles/uk/ac/gda/client/microfocus/util/i18-130.nxs");
		double d[][] = rdr2.constructMappableDatafromCounter("I0");
		assertEquals(134803.0, d[0][0], 0.0);
		assertEquals(3, d.length);
		assertEquals(5, d[0].length);
		assertEquals(105263.0, d[1][4], 0.0);
	}

	@SuppressWarnings("unused")
	@Test
	public void testLoadData3() throws Exception
	{
		rdr3.loadData("testfiles/uk/ac/gda/client/microfocus/util/i18-173.nxs");
		Double[] x = rdr3.getXValues();
		assertEquals(6, x.length);
		Double[] y = rdr3.getYValues();
		assertEquals(2, y.length);
		assertArrayEquals(new Double[]{1.5, 1.6, 1.7000000000000002, 1.8000000000000007, 1.9000000000000004, 2.0000000000000004}, x);
		assertArrayEquals(new Double[]{ 1.5, 3.0}, y);
	}


	@SuppressWarnings("unused")
	@Test
	public void testConstructMappableDatafromXmap() throws Exception
	{
		rdr3.loadData("testfiles/uk/ac/gda/client/microfocus/util/i18-173.nxs");
		List<DetectorROI>[] roi = rdr3.getWindowsfromVortexBean("testfiles/uk/ac/gda/client/microfocus/util/Vortex_Parameters.xml");
		assertEquals(roi.length, 4, 0);
		double d[][] = rdr3.constructMappableDatafromXmap("Pb");
		assertEquals(1.8190637E7, d[0][0], 0.0);
		assertEquals(2, d.length);
		assertEquals(6, d[0].length);
		assertEquals(1.6492962E7, d[1][4], 0.0);
	}

}
