/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.jython.authoriser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;

import gda.configuration.properties.LocalProperties;
import gda.util.TestUtils;
import gda.util.exceptionUtils;

/**
 *
 */
public class LdapAuthoriserTest {

	private static LdapAuthoriser authoriser;
	private static String testScratchDirectoryName;
	private static FileAuthoriser fileAuthoriser;

	/**
	 * @throws Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		//create the underlying xml file
		testScratchDirectoryName = TestUtils.generateDirectorynameFromClassname(LdapAuthoriserTest.class
				.getCanonicalName());
		try {
			TestUtils.makeScratchDirectory(testScratchDirectoryName + "xml");
		} catch (Exception e) {
			fail(exceptionUtils.getFullStackMsg(e));
		}
		System.setProperty(LocalProperties.GDA_CONFIG, testScratchDirectoryName);
		LocalProperties.set(Authoriser.AUTHORISERCLASS_PROPERTY, "gda.jython.authoriser.FileAuthoriser");
		fileAuthoriser = (FileAuthoriser) AuthoriserProvider.getAuthoriser();
		fileAuthoriser.addEntry("testUserNotInLdap", 1, false);
		fileAuthoriser.addEntry("testStaffNotInLdap", 2, true);
		LocalProperties.set(FileAuthoriser.DEFAULTLEVELPROPERTY, "3");
		LocalProperties.set(FileAuthoriser.DEFAULTSTAFFLEVELPROPERTY, "5");

		//then switch to using ldap
		LocalProperties.set(Authoriser.AUTHORISERCLASS_PROPERTY, "gda.jython.authoriser.LdapAuthoriser");
		authoriser = new LdapAuthoriser();

	}

	/**
	 *
	 */
	@Test
	public void testGetAuthorisationLevel() {
		assertEquals(3,authoriser.getAuthorisationLevel("a_username_in_xml_or_ldap"));
		assertEquals(1,authoriser.getAuthorisationLevel("testUserNotInLdap"));
		assertEquals(2,authoriser.getAuthorisationLevel("testStaffNotInLdap"));
		assertEquals(3,authoriser.getAuthorisationLevel("mzp47"));
		assertEquals(5,authoriser.getAuthorisationLevel("bmn54829"));
	}

	/**
	 *
	 */
	@Test
	public void testIsLocalStaff() {
		assertFalse(authoriser.isLocalStaff("a_username_in_xml_or_ldap"));
		assertFalse(authoriser.isLocalStaff("testUserNotInLdap"));
		assertTrue(authoriser.isLocalStaff("testStaffNotInLdap"));
		assertFalse(authoriser.isLocalStaff("mzp47"));
		assertTrue(authoriser.isLocalStaff("bmn54829"));
	}

	/**
	 *
	 */
	@Test
	public void testHasAuthorisationLevel() {
		assertTrue(authoriser.hasAuthorisationLevel("testUserNotInLdap"));
		assertTrue(authoriser.hasAuthorisationLevel("testStaffNotInLdap"));
		assertTrue(authoriser.hasAuthorisationLevel("mzp47"));
		assertTrue(authoriser.hasAuthorisationLevel("bmn54829"));
		assertFalse(authoriser.hasAuthorisationLevel("a_username_in_xml_or_ldap"));
	}

}
