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

public class SimpleFilePathConverterTest {

	@Test
	public void testConverttoInternal() throws Exception {
		SimpleFilePathConverter converter = new SimpleFilePathConverter();
		converter.setUserSubString("/dls/i13-1/data");
		converter.setInternalSubString("/internalmount/test");
		String internal = converter.converttoInternal("/dls/i13-1/data/2011/0-0/");
		assertEquals("/internalmount/test/2011/0-0/", internal);
	}

	@Test
	public void testConverttoExternal() throws Exception {
		SimpleFilePathConverter converter = new SimpleFilePathConverter();
		converter.setUserSubString("/dls/i13-1/data");
		converter.setInternalSubString("/internalmount/test");
		String external = converter.converttoExternal("/internalmount/test/2011/0-0/");
		assertEquals("/dls/i13-1/data/2011/0-0/", external);
	}
}
