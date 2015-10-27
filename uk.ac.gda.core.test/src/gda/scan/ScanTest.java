/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.scan;

import static org.junit.Assert.assertTrue;
import gda.configuration.properties.LocalProperties;
import gda.device.Scannable;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.util.ObjectServer;
import gda.util.TestUtils;
import gda.util.findableHashtable.FindableHashtable;
import gda.util.findableHashtable.Hashtable;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

/**
 * A Class for performing TestNG unit tests on ....... Separate Java properties and object server XML files are
 * associated with and used by this test. Their location is constructed within the test. By default the tests are
 * assumed to be under the users home directory under ${user.home}/gda/src... unless the Java property gda.tests is set
 * AS A VM ARGUMENT when running the test. In the latter case the test's Java property and XML files are assumed to be
 * under ${gda.tests}/gda/src...
 */

@Ignore("2010/06/03 Test class ignored since the setup code needs re-writing GDA-3277")
public class ScanTest {
	private static Finder finder = null;

	private static Hashtable hashtable = null;

	private static String testDir = null;

	private static String separator = System.getProperty("file.separator");

	/**
	 * Test suite setup.
	 * @throws FactoryException
	 */
	@BeforeClass()
	public static void setup() throws Exception {
		// TODO: Make sure all GDA files required/written by this test are all
		// stored
		// relative to the test directory. Some properties may need changing?
		/*
		 * The following line is required to ensure that files created within this test (e.g. motor position files) are
		 * created in a sensible location.
		 */
		testDir = TestUtils.createClassScratchDirectory(ScanTest.class).getAbsolutePath();
		System.setProperty("gda.testDir", testDir);

		/*
		 * The following line is required to ensure that the default LocalProperties are obtained from the test's Java
		 * properties file. The property gda.propertiesFile must be set BEFORE LocalProperties is used and thus it's
		 * static block is invoked.
		 */
		System.setProperty("gda.propertiesFile", TestUtils.getResourceAsFile(ScanTest.class, "scanTest.properties").getAbsolutePath());

		configureLogging();
		ObjectServer.createLocalImpl(TestUtils.getResourceAsFile(ScanTest.class, "scanTest_server.xml").getAbsolutePath());

		finder = Finder.getInstance();
		hashtable = (gda.util.findableHashtable.Hashtable) finder.find("GDAHashtable");

		cleanUp();

		/*
		 * TODO: Repeat test scan with different combinations of metadata and srb store settings. This may well be best
		 * done having @BeforeTest methods, performing different scan before particular tests. Or have several scans
		 * done from here and test the expected outputs.
		 */

		// TODO: Run scan with different data writers and check, correct files
		// appear.
		performScan();
	}

	private static String[] getDirectoryContents(String dirName, final String start, final String content) {
		String[] contents;

		File dir = new File(dirName);

		contents = dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				boolean ok;

				if (start == null && content == null) {
					ok = !name.startsWith(".");
				} else if (start != null) {
					ok = name.startsWith(start);
				} else {
					ok = !name.startsWith(".") && name.contains(content);
				}

				return ok;
			}
		});

		return contents;
	}

	private static void cleanUp() {
		deleteFiles(LocalProperties.get("gda.data.scan.scanCompleteDir"), "scan_complete", null);
		deleteFiles(LocalProperties.get(LocalProperties.GDA_DATAWRITER_DIR), null, ".nxs");
		deleteFiles(LocalProperties.get(LocalProperties.GDA_DATAWRITER_DIR), null, ".filelist");
		// TODO: Think this next line needs to be better?
		deleteFiles(testDir, null, ".i03");
	}

	private static void deleteFiles(String directory, String start, String content) {
		if (!directory.endsWith(separator)) {
			directory += separator;
		}

		String[] filenames = getDirectoryContents(directory, start, content);
		File file = null;

		if (filenames != null) {
			if (filenames.length > 0) {
				for (String filename : filenames) {
					filename = directory + filename;
					file = new File(filename);
					System.out.println("Deleting " + filename);
					file.delete();
				}
			}
		}
	}

	private static void configureLogging() {
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

		JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(lc);
		lc.reset();
		try {
			configurator.doConfigure(LocalProperties.get("gda.server.logging.xml"));
		} catch (JoranException e) {
			e.printStackTrace();
		}
	}

	private static void printTime(String prepender) {
		Date now;
		DateFormat tf = DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.GERMAN);
		now = new Date();
		System.out.println(prepender + " " + tf.format(now));
	}

	private static void performScan() {
		// TODO: Speed up motor moves, to reduce overall test time.

		// TODO: Try and get the data contents of the scans to be deterministic
		// e.g.by using a gaussian dummy with not noise. If can do then check
		// contents of data ok in files.

		Scannable scannable;
		try {
			scannable = new gda.device.scannable.DummyScannable("TableFrontHoriz");
			GridScan scan = new GridScan(scannable, "0.0", "1.0", "0.2", "100.0", "mm");
			hashtable.putBoolean(FindableHashtable.NEXUS_METADATA, true);
			hashtable.putBoolean(FindableHashtable.SRB_STORE, true);
			printTime("Starting a scan");
			scan.run();
			printTime("Completed a scan");
		} catch (Exception e) {
			System.out.println("Error in scan " + e.getMessage());
		}
	}

	/**
	 *
	 */
	@Test()
	public void testDatafilePositions() {
		// TODO: Check positions in datafiles are ok. Correct number of them and
		// values within limits.
	}

	/**
	 *
	 */
	@Test()
	public void testDatafileData() {
		// TODO: Check data in datafiles are ok. Correct number of them and if
		// can
		// make Gaussian the values within limits.
	}

	/**
	 *
	 */
	@Test()
	public void testDatafiles() {
		// TODO: Test that data files which should have been created by the
		// scans
		// have been so.
	}

	/**
	 *
	 */
	@Test()
	public void testDdhDropFile() {
		String[] contents = getDirectoryContents(LocalProperties.get("gda.data.scan.scanCompleteDir"), "scan_complete",
				null);

		assertTrue("DDH drop file not created", 0 != contents.length);
		assertTrue("Scan complete directory should only contain 1 scan complete file after a scan",
				1 == contents.length);
	}

	/**
	 * Test metadata in the Nexus files.
	 */
	@Test()
	public void testMetadata() {
		// TODO: Test data files by reading of metadata from Nexus files.

		// TODO: Could this be done by converting Nexus to XML and validating
		// the
		// output against a schema.
	}
}
