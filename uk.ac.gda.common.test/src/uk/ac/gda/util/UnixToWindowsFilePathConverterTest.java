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

package uk.ac.gda.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UnixToWindowsFilePathConverterTest {


	@Test
	public void testConverttoInternal() throws Exception {
		UnixToWindowsFilePathConverter converter = new UnixToWindowsFilePathConverter();
		converter.setUnixSubString("/dls/i13-1/data");
		converter.setWindowsSubString("z:\\data");

		String internal = converter.converttoInternal("/dls/i13-1/data/2011/0-0/");
		assertEquals("z:\\data\\2011\\0-0\\", internal);
	}

	@Test
	public void testConverttoExternal() throws Exception {
		UnixToWindowsFilePathConverter converter = new UnixToWindowsFilePathConverter();
		converter.setUnixSubString("/dls/i13-1/data");
		converter.setWindowsSubString("z:\\data");

		String external = converter.converttoExternal("z:\\data\\2011\\0-0\\");
		assertEquals("/dls/i13-1/data/2011/0-0/", external);
	}

}
