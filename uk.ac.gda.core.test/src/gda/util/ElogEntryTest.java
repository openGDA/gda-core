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

package gda.util;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;

import gda.util.ElogEntry.ElogException;

/**
 * @author zhb16119
 * Tests that wrong input parameters do not result in an ELog being sent.
 *
 */
public class ElogEntryTest {
	static String testfile1 = null;
	static String testfile2 = null;
	static String testfile2wrong = null;

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	/**
	 * Determines the absolute path to the test files.
	 */
	@BeforeAll
	public static void setUpBeforeClass() {
		testfile1 = new File("testfiles/gda/util/ElogEntryTest/Elog1.jpeg").getPath();
		testfile2 = new File("testfiles/gda/util/ElogEntryTest/Elog1.jpeg").getPath();
		testfile2wrong = new File("testfiles/gda/util/ElogEntryTest/").getPath() + "Elog2wrong.jpeg";
	}

	/**
	 * Tests that when the correct parameters are used, the ELog will successfully send.
	 * @throws ElogException
	 */
	@Test
	@Disabled // Don't flood elog
	public void testElogEntryOK() throws ElogException {

		String
		visit = "aa34bg",
		logID = "OPR",
		groupID = "DA",
		userID = "gda",
		title = "java elogger - should work",
		content = "please ignore this elog";

		String[] fileLocations = {(testfile1), (testfile2)};

		ElogEntry.post(title, content, userID, visit, logID, groupID, fileLocations);
	}

	/**
	 * Tests that when an invalid groupID is used then the ELog will not send.
	 * @throws ElogException
	 */
	@Test(expected=ElogEntry.ElogException.class)
	@Disabled // Don't flood elog
	public void testElogEntryFailGroupID() throws ElogException {

		String
		visit = "aa34bg",
		logID = "OPR",
		groupID = "mx305-6",
		userID = "gda",
		title = "java multipart try - should fail",
		content = "please ignore - should fail due to an invalid category";

		String[] fileLocations = {(testfile1), (testfile2)};

		ElogEntry.post(title, content,userID, visit, logID, groupID, fileLocations);
	}


	/**
	 * Tests that when an invalid logID is entered then the ELog will not send.
	 * @throws ElogException
	 */
	@Test(expected=ElogEntry.ElogException.class)
	@Disabled // Don't flood elog
	public void testElogEntryFailLogID() throws ElogException {

		String
		visit = "aa34bg",
		logID = "OPRRRRRRRRRRRRR",
		groupID = "DA",
		userID = "gda",
		title = "java multipart try - should fail",
		content = "please ignore - should fail due to an invalid operation";

		String[] fileLocations = {(testfile1), (testfile2)};

		ElogEntry.post(title, content,userID, visit, logID, groupID, fileLocations);
	}


	/**
	 * Tests that when an invalid userID is entered then the ELog will not send.
	 * @throws ElogException
	 */
	@Test(expected=ElogEntry.ElogException.class)
	@Disabled // Don't flood elog
	public void testElogEntryFailUserID() throws ElogException {

		String
		visit = "aa34bg",
		logID = "OPR",
		groupID = "DA",
		userID = "gdaaaaaaaaa",
		title = "java multipart try - should fail",
		content = "please ignore - should fail due to an invalid ID";

		String[] fileLocations = {(testfile1), (testfile2)};

		ElogEntry.post(title, content,userID, visit, logID, groupID, fileLocations);
	}


	/**
	 * Tests that when an invalid file names or directories are entered then the ELog will not send.
	 * @throws ElogException
	 */
	@Test(expected=ElogEntry.ElogException.class)
	@Disabled // Don't flood elog
	public void testElogEntryFailImages() throws ElogException {

		String
		visit = "aa34bg",
		logID = "OPR",
		groupID = "DA",
		userID = "gda",
		title = "java multipart try - should fail",
		content = "please ignore - should fail due to invalid file names";

		String[] fileLocations = {(testfile1), (testfile2wrong)};

		ElogEntry.post(title, content,userID, visit, logID, groupID, fileLocations);
	}

	/**
	 * Tests that when fileLocations is null, i.e no images are to be sent. The text only ELog will still send.
	 * @throws ElogException
	 */
	@Test
	@Disabled // Don't flood elog
	public void testElogEntryNoImages() throws ElogException {

		String
		visit = "aa34bg",
		logID = "OPR",
		groupID = "DA",
		userID = "gda",
		title = "java multipart try - no images",
		content = "please ignore - should be post with no images";

		ElogEntry.post(title, content,userID, visit, logID, groupID, null);
	}

	/**
	 * Tests that an asynchronous ELog entry works.
	 */
	@Test
	@Disabled // Don't flood elog
	public void testElogEntryAsyn() {

		String visit = "aa34bg";
		String logID = "OPR";
		String groupID = "DA";
		String userID = "gda";
		String title = "java multipart in seperate thread";
		String content = "please ignore";

		String[] fileLocations = {(testfile1), (testfile2)};

		ElogEntry.postAsyn(title, content,userID, visit, logID, groupID, fileLocations);
	}

	@Test
	@Disabled // Don't flood elog
	public void testElogEntryNonStaticOK() throws ElogException {
		String visit = "aa34bg";
		String logID = "OPR";
		String groupID = "DA";
		String userID = "gda";
		String title = "java multipart posting non-static post";

		ElogEntry log = new ElogEntry(title, userID, visit, logID, groupID);
		log.addText("Please ignore this string");
		log.addText(new String[] {"and this one", "this one as well"});
		log.addHtml("<p>This html should be ignored</p>");
		log.addHtml(new String[] {"<p>list of <em>ignored</em> html strings</p>", "<p> as Above</p>"});
		log.addImage(testfile1, "Caption of test1");
		log.addImage(testfile2);
		log.post();
	}

	@Test // Only active test as it write to temporary file
	public void testWriteToFile() throws ElogException, IOException {
		String visit = "aa34bg";
		String logID = "OPR";
		String groupID = "DA";
		String userID = "gda";
		String title = "java multipart posting non-static post";

		ElogEntry log = new ElogEntry(title, userID, visit, logID, groupID)
				.addText("Please ignore this string")
				.addText(new String[] {"and this one", "this one as well"})
				.addHtml("<p>This html should be ignored</p>")
				.addHtml(new String[] {"<p>list of <em>ignored</em> html strings</p>", "<p> as Above</p>"})
				.addImage(testfile1, "Caption of test1");
		File testFile = tempFolder.newFile("testElogFile.html");
		log.postToFile(testFile.getAbsolutePath());
		String expectedPostText = "\n\n<!-- ==== Start of Elog Entry Content ==== -->\n" +
				"<p>Please ignore this string</p>\n" +
				"<p>and this one</p>\n" +
				"<p>this one as well</p>\n" +
				"\n" +
				"<!-- html submitted as is - not rendered by ElogEntry -->\n" +
				"<p>This html should be ignored</p><br>\n" +
				"\n" +
				"<!-- html submitted as is - not rendered by ElogEntry -->\n" +
				"<p>list of <em>ignored</em> html strings</p><br>\n" +
				"\n" +
				"<!-- html submitted as is - not rendered by ElogEntry -->\n" +
				"<p> as Above</p><br>\n" +
				"\n" +
				"<div style=\"display:inline-block;\">\n" +
				"<img alt=\"" + testfile1 + "\" src=\"" + testfile1 + "\"/><br>\n" +
				"<span style=\"max-width:600px; margin: 0 auto; text-align:inherited; word-wrap:break-word;\">Caption of test1</span>\n" +
				"</div><br><br>\n" +
				"\n" +
				"<!-- ==== End of Elog Entry Content ==== -->\n\n";
		String actual = String.join("\n", Files.readAllLines(testFile.toPath(), Charset.forName(ElogEntry.ENCODING)));
		assertEquals("Incorrect ElogEntry written to file", expectedPostText, actual);
	}
}
