/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package gda.device.detector.xmap;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import gda.configuration.properties.LocalProperties;

public class XmapFileUtilsTest {

	private XmapBufferedDetector xmap;

	@Before
	public void prepare() {
		xmap = new XmapBufferedDetector();
		LocalProperties.set(LocalProperties.GDA_INSTRUMENT, "b18");
	}

	@Test
	public void testConvertToWindowsPath() {
		xmap.setWindowsPathPrefix("X:/");
		assertEquals("X:/", xmap.convertPathToWindows("/dls/b18/data"));
		assertEquals("X:/2020/cm1234", xmap.convertPathToWindows("/dls/b18/data/2020/cm1234"));
		assertEquals("X:/2020/cm1234/xmapData", xmap.convertPathToWindows("/dls/b18/data/2020//cm1234/xmapData//"));

		xmap.setWindowsPathPrefix("X:/data/");
		assertEquals("X:/data/cm1234/xmapData", xmap.convertPathToWindows("/dls/b18/data/cm1234/xmapData"));
	}

	@Test
	public void testConvertFromWindowsPath() {
		xmap.setWindowsPathPrefix("X:/");
		assertEquals("/dls/b18/data/2020/cm1234", xmap.convertPathFromWindows("X:/2020/cm1234"));
		assertEquals("/dls/b18/data/2020/cm1234", xmap.convertPathFromWindows("X:/2020/cm1234\\"));
		assertEquals("/dls/b18/data/2020/cm1234", xmap.convertPathFromWindows("X:\\2020\\cm1234\\"));

		xmap.setWindowsPathPrefix("X:/data");
		assertEquals("/dls/b18/data/cm1234", xmap.convertPathFromWindows("X:/data/cm1234"));

	}
}
