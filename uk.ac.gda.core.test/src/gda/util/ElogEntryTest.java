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

import static org.junit.Assert.fail;

import java.net.URISyntaxException;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;


/**
 * @author zhb16119
 * Tests that wrong input parameters do not result in an ELog being sent.
 *
 */
@Ignore("2010/06/09 Test ignored since it floods the Elog server GDA-2354")
public class ElogEntryTest {
	static String testfile1 = null;
	static String testfile2 = null;
	static String testfile2wrong = null;

	/**
	 * Determines the absolute path to the test files.
	 * @throws URISyntaxException
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws URISyntaxException {
		testfile1 = ElogEntryTest.class.getResource("TestFiles/Elog1.jpeg").toURI().getPath();
		testfile2 = ElogEntryTest.class.getResource("TestFiles/Elog2.jpeg").toURI().getPath();
		testfile2wrong = ElogEntryTest.class.getResource("TestFiles/").toURI().getPath() + "Elog2wrong.jpeg";
	}

	/**
	 * Tests that when the correct parameters are used, the ELog will successfully send.
	 */
	@Test
	public void testElogEntryOK() {

		String
		visit = "aa34bg",
		logID = "OPR",
		groupID = "DA",
		userID = "gda",
		title = "java elogger - should work",
		content = "please ignore this elog";

		String[] fileLocations = {(testfile1), (testfile2)};

		try {
			ElogEntry.post(title, content, userID, visit, logID, groupID, fileLocations);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

	/**
	 * Tests that when an invalid groupID is used then the ELog will not send.
	 */
	@Test
	public void testElogEntryFailGroupID() {

		String
		visit = "aa34bg",
		logID = "OPR",
		groupID = "mx305-6",
		userID = "gda",
		title = "java multipart try - should fail",
		content = "please ignore - should fail due to an invalid catergory";

		String[] fileLocations = {(testfile1), (testfile2)};

		try {
			ElogEntry.post(title, content,userID, visit, logID, groupID, fileLocations);
			fail("Should have thrown exception - should fail catergory");
		} catch (Exception e) {
		}
	}


	/**
	 * Tests that when an invalid logID is entered then the ELog will not send.
	 */
	@Test
	public void testElogEntryFailLogID() {

		String
		visit = "aa34bg",
		logID = "OPRRRRRRRRRRRRR",
		groupID = "DA",
		userID = "gda",
		title = "java multipart try - should fail",
		content = "please ignore - should fail due to an invalid operation";

		String[] fileLocations = {(testfile1), (testfile2)};

		try {
			ElogEntry.post(title, content,userID, visit, logID, groupID, fileLocations);
			fail("Should have thrown exception - should fail operation");
		} catch (Exception e) {
		}
	}


	/**
	 * Tests that when an invalid userID is entered then the ELog will not send.
	 */
	@Test
	public void testElogEntryFailUserID() {

		String
		visit = "aa34bg",
		logID = "OPR",
		groupID = "DA",
		userID = "gdaaaaaaaaa",
		title = "java multipart try - should fail",
		content = "please ignore - should fail due to an invalid ID";

		String[] fileLocations = {(testfile1), (testfile2)};

		try {
			ElogEntry.post(title, content,userID, visit, logID, groupID, fileLocations);
			fail("Should have thrown exception - should fail user id");
		} catch (Exception e) {
		}
	}


	/**
	 * Tests that when an invalid file names or directories are entered then the ELog will not send.
	 */
	@Test
	public void testElogEntryFailImages() {

		String
		visit = "aa34bg",
		logID = "OPR",
		groupID = "DA",
		userID = "gda",
		title = "java multipart try - should fail",
		content = "please ignore - should fail due to invalid file names";

		String[] fileLocations = {(testfile1), (testfile2wrong)};

		try {
			ElogEntry.post(title, content,userID, visit, logID, groupID, fileLocations);
			fail("Should have thrown exception - should fail images");
		} catch (Exception e) {
		}
	}

	/**
	 * Tests that when fileLocations is null, i.e no images are to be sent. The text only ELog will still send.
	 */
	@Test
	public void testElogEntryNoImages() {

		String
		visit = "aa34bg",
		logID = "OPR",
		groupID = "DA",
		userID = "gda",
		title = "java multipart try - no images",
		content = "please ignore - should be post with no images";

		try {
			ElogEntry.post(title, content,userID, visit, logID, groupID, null);
		} catch (Exception e) {
			fail("Should have sent ELog without any images");
		}
	}

	/**
	 * Tests that an asynchronous ELog entry works.
	 */
	@Test
	public void testElogEntryAsyn() {

		String
		visit = "aa34bg",
		logID = "OPR",
		groupID = "DA",
		userID = "gda",
		title = "java multipart in seperate thread",
		content = "please ignore";

		String[] fileLocations = {(testfile1), (testfile2)};

		ElogEntry.postAsyn(title, content,userID, visit, logID, groupID, fileLocations);
	}

	@Test
	public void testElogEntryNonStaticOK() {
		String
		visit = "aa34bg",
		logID = "OPR",
		groupID = "DA",
		userID = "gda",
		title = "java multipart posting non-static post";

		try {
			ElogEntry log = new ElogEntry(title, userID, visit, logID, groupID);
			log.addText("Please ignore this string");
			log.addText(new String[] {"and this one", "this one as well"});
			log.addHtml("<p>This html should be ignored</p>");
			log.addHtml(new String[] {"<p>list of <em>ignored</em> html strings</p>", "<p> as Above</p>"});
			log.addImage(testfile1, "Caption of test1");
			log.addImage(testfile2);
			log.post();
		} catch (ELogEntryException e) {
			fail("Non static post should be successful");
		}
	}
}
