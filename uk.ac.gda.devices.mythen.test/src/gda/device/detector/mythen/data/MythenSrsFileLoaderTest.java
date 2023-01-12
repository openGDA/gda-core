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

package gda.device.detector.mythen.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;
import static org.junit.Assert.*;

public class MythenSrsFileLoaderTest {
	
	@Test
	public void testWithEmptyFile() {
		String contents = "";
		MythenSrsFileLoader loader = new MythenSrsFileLoader();
		try {
			loader.read(new BufferedReader(new StringReader(contents)));
			fail("Didn't catch exception when trying to load empty file");
		} catch (IOException e) {
			// expected
		}
	}
	
	@Test
	public void testWithFileWithoutEndLine() {
		String contents = "First line\n" + 
			"Second line\n";
		MythenSrsFileLoader loader = new MythenSrsFileLoader();
		try {
			loader.read(new BufferedReader(new StringReader(contents)));
			fail("Didn't catch exception when trying to load file without &END line");
		} catch (IOException e) {
			// expected
		}
	}
	
	@Test
	public void testWithFileWithHeaderButNoColumnLine() {
		String contents = "&END\n";
		MythenSrsFileLoader loader = new MythenSrsFileLoader();
		try {
			loader.read(new BufferedReader(new StringReader(contents)));
			fail("Didn't catch exception when trying to load file without column list");
		} catch (IOException e) {
			// expected
		}
	}
	
	@Test
	public void testWithFileWithListOfColumnsExcludingMythen() {
		String contents = "&END\n" +
			"a\tb\tc\n";
		MythenSrsFileLoader loader = new MythenSrsFileLoader();
		try {
			loader.read(new BufferedReader(new StringReader(contents)));
			fail("Didn't catch exception when trying to load file without 'mythen' column");
		} catch (IOException e) {
			// expected
		}
	}
	
	@Test
	public void testWithValidFile() throws IOException {
		String contents = "&END\n" +
			"a\tmythen\tb\n" +
			"123\tfilename1\t456\n" +
			"123\tfilename2\t456\n";
		MythenSrsFileLoader loader = new MythenSrsFileLoader();
		String[] filenames = loader.read(new BufferedReader(new StringReader(contents)));
		assertEquals(2, filenames.length);
		assertEquals("filename1", filenames[0]);
		assertEquals("filename2", filenames[1]);
	}

}
