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
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchResult;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import gda.configuration.properties.LocalProperties;
import gda.jython.authenticator.LdapMixin;
import gda.util.TestUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest(LdapAuthoriser.class)
public class LdapAuthoriserTest {

	private static final String STAFF_FED_ID = "staffFedID";
	private static final String NON_STAFF_FED_ID = "nonStaffFedID";
	private static final String NON_STAFF_GROUP = "non-staff-group";
	private static final String STAFF_GROUP = "mock-staff-group";
	private static LdapAuthoriser authoriser;
	private static String testScratchDirectoryName;
	private static FileAuthoriser fileAuthoriser;

	private static LdapMixin mockLdap;

	/**
	 * @throws Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		mockLdap = mock(LdapMixin.class);
		PowerMockito.whenNew(LdapMixin.class)
			.withNoArguments()
			.thenReturn(mockLdap);

		makeLocalStaff(STAFF_FED_ID);
		makeNonStaff(NON_STAFF_FED_ID);

		//create the underlying xml file
		testScratchDirectoryName = TestUtils.generateDirectorynameFromClassname(LdapAuthoriserTest.class
				.getCanonicalName());
		TestUtils.makeScratchDirectory(testScratchDirectoryName + "xml");
		System.setProperty(LocalProperties.GDA_CONFIG, testScratchDirectoryName);
		LocalProperties.set(Authoriser.AUTHORISERCLASS_PROPERTY, "gda.jython.authoriser.FileAuthoriser");
		fileAuthoriser = (FileAuthoriser) AuthoriserProvider.getAuthoriser();
		fileAuthoriser.addEntry("testUserNotInLdap", 1, false);
		fileAuthoriser.addEntry("testStaffNotInLdap", 2, true);
		LocalProperties.set(FileAuthoriser.DEFAULTLEVELPROPERTY, "3");
		LocalProperties.set(FileAuthoriser.DEFAULTSTAFFLEVELPROPERTY, "5");
		LocalProperties.set(LdapAuthoriser.LDAPSTAFF_PROPERTY, STAFF_GROUP);

		//then switch to using ldap
		LocalProperties.set(Authoriser.AUTHORISERCLASS_PROPERTY, "gda.jython.authoriser.LdapAuthoriser");
		authoriser = new LdapAuthoriser();

	}

	private static void makeLocalStaff(String username) throws Exception {
		mockLdapUser(username, STAFF_GROUP);
	}

	private static void makeNonStaff(String username) throws Exception {
		mockLdapUser(username, NON_STAFF_GROUP);
	}

	private static void mockLdapUser(String username, String group) throws NamingException {
		@SuppressWarnings("unchecked")
		NamingEnumeration<SearchResult> results = mock(NamingEnumeration.class);
		SearchResult user = mock(SearchResult.class);
		Attributes atts = new BasicAttributes("memberOf", group);
		when(user.getAttributes()).thenReturn(atts);
		when(mockLdap.searchLdapForUser(eq(username), anyVararg())).thenReturn(results);
		when(results.hasMore()).thenReturn(true);
		when(results.next()).thenReturn(user);
	}

	@Test
	public void testGetAuthorisationLevel() {
		assertEquals(3,authoriser.getAuthorisationLevel("a_username_in_xml_or_ldap"));
		assertEquals(1,authoriser.getAuthorisationLevel("testUserNotInLdap"));
		assertEquals(2,authoriser.getAuthorisationLevel("testStaffNotInLdap"));
		assertEquals(3,authoriser.getAuthorisationLevel(NON_STAFF_FED_ID));
		assertEquals(5,authoriser.getAuthorisationLevel(STAFF_FED_ID));
	}

	@Test
	public void testIsLocalStaff() {
		assertFalse(authoriser.isLocalStaff("a_username_in_xml_or_ldap"));
		assertFalse(authoriser.isLocalStaff("testUserNotInLdap"));
		assertTrue(authoriser.isLocalStaff("testStaffNotInLdap"));
		assertFalse(authoriser.isLocalStaff(NON_STAFF_FED_ID));
		assertTrue(authoriser.isLocalStaff(STAFF_FED_ID));
	}

	@Test
	public void testHasAuthorisationLevel() {
		assertTrue(authoriser.hasAuthorisationLevel("testUserNotInLdap"));
		assertTrue(authoriser.hasAuthorisationLevel("testStaffNotInLdap"));
		assertTrue(authoriser.hasAuthorisationLevel(NON_STAFF_FED_ID));
		assertTrue(authoriser.hasAuthorisationLevel(STAFF_FED_ID));
		assertFalse(authoriser.hasAuthorisationLevel("a_username_in_xml_or_ldap"));
	}

}
