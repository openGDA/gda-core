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

package gda.jython.logger;

import static org.apache.commons.io.FilenameUtils.concat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import gda.TestHelpers;
import gda.data.ObservablePathProvider;
import gda.data.PathChanged;

@RunWith( MockitoJUnitRunner.class )
public class RedirectableFileLoggerTest {

	private static final String FILENAME = "gdaterminal.log";

	@Mock
	private ObservablePathProvider mockPathProvider;

	private RedirectableFileLogger logger;

	private void createLogger(String logfile) {
		when(mockPathProvider.getPath()).thenReturn(logfile);
		logger = new RedirectableFileLogger(mockPathProvider);
		logger.configure();
	}

	@Test
	public void testConstruction() throws Exception {
		String logfile = concat(TestHelpers.setUpTest(RedirectableFileLoggerTest.class, "testConfigure", true), FILENAME);
		when(mockPathProvider.getPath()).thenReturn(logfile);

		assertFalse(new File(logfile).exists());
		logger = new RedirectableFileLogger(mockPathProvider);
		logger.configure();
		assertTrue(new File(logfile).exists());
	}

	private String readLogLine(String filepath, int i) throws IOException {
		String line;
		try {
			line = FileUtils.readLines(new File(filepath), "utf-8").get(i);
		} catch (IndexOutOfBoundsException e) {
			return "NOSUCHLINE";
		}
		return line.split("(\\| )")[1]; // split off the date preceding "| "
	}

	@Test
	public void testLog() throws Exception {
		String logfile = concat(TestHelpers.setUpTest(RedirectableFileLoggerTest.class, "testLog", true), FILENAME);
		createLogger(logfile);

		logger.log("abcd1234");
		assertEquals("abcd1234", readLogLine(logfile, 0));

	}
	@Test
	public void testMultipleLog() throws Exception {
		String logfile = concat(TestHelpers.setUpTest(RedirectableFileLoggerTest.class, "testTwice", true), FILENAME);
		createLogger(logfile);

		logger.log("abcd1234");
		logger.log("efgh4567");
		assertEquals("abcd1234", readLogLine(logfile, 0));
		assertEquals("efgh4567", readLogLine(logfile, 1));
	}

	@Test
	public void testLogMultiline() throws Exception {
		String logfile = concat(TestHelpers.setUpTest(RedirectableFileLoggerTest.class, "testLogMultiline", true), FILENAME);
		createLogger(logfile);

		logger.log("abcd1234\nefgh4567");
		assertEquals("abcd1234", readLogLine(logfile, 0));
		assertEquals("efgh4567", readLogLine(logfile, 1));
	}

	@Test
	public void testPathChangedUpdate() throws Exception {
		String scratch = TestHelpers.setUpTest(RedirectableFileLoggerTest.class, "testPathChanged", true);
		String logfile1 = concat(scratch, concat("visit1", FILENAME));
		String logfile2 = concat(scratch, concat("visit2", FILENAME));

		// visit 1
		createLogger(logfile1);
		assertEquals("NOSUCHLINE", readLogLine(logfile1, 0));
		logger.log("visit1-abcd1234");
		assertEquals("visit1-abcd1234", readLogLine(logfile1, 0));

		// visit 2
		logger.update(null, new PathChanged(logfile2));
		assertEquals(
				"<<<log moved to: test-scratch/gda/jython/logger/RedirectableFileLoggerTest/testPathChanged/visit2/gdaterminal.log>>>".replace(
						'/', File.separatorChar), readLogLine(logfile1, 1));
		assertEquals(
				"<<<log moved from: test-scratch/gda/jython/logger/RedirectableFileLoggerTest/testPathChanged/visit1/gdaterminal.log>>>".replace(
						'/', File.separatorChar), readLogLine(logfile2, 0));
		logger.log("visit2-abcd1234");
		assertEquals("visit2-abcd1234", readLogLine(logfile2, 1));

		// back to visit 1
		logger.update(null, new PathChanged(logfile1));
		assertEquals(
				"<<<log moved from: test-scratch/gda/jython/logger/RedirectableFileLoggerTest/testPathChanged/visit2/gdaterminal.log>>>".replace(
						'/', File.separatorChar), readLogLine(logfile1, 2));
		assertEquals(
				"<<<log moved to: test-scratch/gda/jython/logger/RedirectableFileLoggerTest/testPathChanged/visit1/gdaterminal.log>>>".replace(
						'/', File.separatorChar), readLogLine(logfile2, 2));
		logger.log("visit1again-abcd1234");
		assertEquals("visit1again-abcd1234", readLogLine(logfile1, 3));
	}

}
