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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchResult;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import gda.configuration.properties.LocalProperties;
import gda.jython.authenticator.LdapMixin;
import gda.util.TestUtils;

public class LdapAuthoriserTest {

	private static final String STAFF_FED_ID = "staffFedID";
	private static final String NON_STAFF_FED_ID = "nonStaffFedID";
	private static final String NON_STAFF_GROUP = "non-staff-group";
	private static final String STAFF_GROUP = "mock-staff-group";
	private static LdapAuthoriser authoriser;
	private static String testScratchDirectoryName;
	private static FileAuthoriser fileAuthoriser;

	private static MockedConstruction<LdapMixin> ldapMixinMock;

	/**
	 * @throws Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		NamingEnumeration<SearchResult> localStaffResults = mockResultsForGroup(STAFF_GROUP);
		NamingEnumeration<SearchResult> nonStaffResults = mockResultsForGroup(NON_STAFF_GROUP);
		ldapMixinMock = Mockito.mockConstruction(LdapMixin.class, (mock, context) -> {
			when(mock.searchLdapForUser(eq(STAFF_FED_ID), any())).thenReturn(localStaffResults);
			when(mock.searchLdapForUser(eq(NON_STAFF_FED_ID), any())).thenReturn(nonStaffResults);
		});

		// create the underlying xml file
		testScratchDirectoryName = TestUtils
				.generateDirectorynameFromClassname(LdapAuthoriserTest.class.getCanonicalName());
		TestUtils.makeScratchDirectory(testScratchDirectoryName + "xml");
		System.setProperty(LocalProperties.GDA_CONFIG, testScratchDirectoryName);
		LocalProperties.set(Authoriser.AUTHORISERCLASS_PROPERTY, "gda.jython.authoriser.FileAuthoriser");
		fileAuthoriser = (FileAuthoriser) AuthoriserProvider.getAuthoriser();
		fileAuthoriser.addEntry("testUserNotInLdap", 1, false);
		fileAuthoriser.addEntry("testStaffNotInLdap", 2, true);
		LocalProperties.set(Authoriser.DEFAULT_LEVEL_PROPERTY, "3");
		LocalProperties.set(Authoriser.DEFAULT_STAFF_LEVEL_PROPERTY, "5");
		LocalProperties.set(LdapAuthoriser.LDAPSTAFF_PROPERTY, STAFF_GROUP);

		// then switch to using ldap
		LocalProperties.set(Authoriser.AUTHORISERCLASS_PROPERTY, "gda.jython.authoriser.LdapAuthoriser");
		authoriser = new LdapAuthoriser();

	}

	@AfterClass
	public static void tearDown() {
		ldapMixinMock.close();
		LocalProperties.clearProperty(Authoriser.DEFAULT_LEVEL_PROPERTY);
		LocalProperties.clearProperty(Authoriser.DEFAULT_STAFF_LEVEL_PROPERTY);
		LocalProperties.clearProperty(LdapAuthoriser.LDAPSTAFF_PROPERTY);
		LocalProperties.clearProperty(Authoriser.AUTHORISERCLASS_PROPERTY);
		System.clearProperty(LocalProperties.GDA_CONFIG);
	}

	private static NamingEnumeration<SearchResult> mockResultsForGroup(String group) throws NamingException {
		@SuppressWarnings("unchecked")
		NamingEnumeration<SearchResult> results = mock(NamingEnumeration.class);
		SearchResult user = mock(SearchResult.class);
		Attributes atts = new BasicAttributes("memberOf", group);
		when(user.getAttributes()).thenReturn(atts);
		when(results.hasMore()).thenReturn(true);
		when(results.next()).thenReturn(user);
		return results;
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
