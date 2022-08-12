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

package gda.jython.authenticator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.AfterEach;

import gda.configuration.properties.LocalProperties;
import org.junit.jupiter.api.Test;

/**
 * Test case for the JaasAuthenticator class
 */
public class JaasAuthenticatorTest {

	@AfterEach
	public void tearDown() {
		LocalProperties.clearProperty(Authenticator.AUTHENTICATORCLASS_PROPERTY);
		LocalProperties.clearProperty(JaasAuthenticator.GDA_ACCESSCONTROL_JAAS_REALM);
		LocalProperties.clearProperty(JaasAuthenticator.GDA_ACCESSCONTROL_JAAS_KDC);
		LocalProperties.clearProperty(JaasAuthenticator.GDA_ACCESSCONTROL_JAAS_CONFFILE);
	}

	@Test
	public void testAuthenticatorCreation() throws ClassNotFoundException {
		LocalProperties.set(Authenticator.AUTHENTICATORCLASS_PROPERTY, JaasAuthenticator.class.getName());
		LocalProperties.set(JaasAuthenticator.GDA_ACCESSCONTROL_JAAS_REALM, "FED.CCLRC.AC.UK");
		LocalProperties.set(JaasAuthenticator.GDA_ACCESSCONTROL_JAAS_KDC, "fed.cclrc.ac.uk");
		LocalProperties.set(JaasAuthenticator.GDA_ACCESSCONTROL_JAAS_CONFFILE, "/path/to/a/file");
		@SuppressWarnings("unused")
		JaasAuthenticator authenticator = (JaasAuthenticator) AuthenticatorProvider.getAuthenticator();
	}

	@Test
	public void testIsAuthenticated() throws ClassNotFoundException {
		// we need to use the test user authenticator
		LocalProperties.set(Authenticator.AUTHENTICATORCLASS_PROPERTY, TestUserAuthenticator.class.getName());
		TestUserAuthenticator authenticator = (TestUserAuthenticator) AuthenticatorProvider.getAuthenticator();
		assertTrue(authenticator.isAuthenticated("test-prefix", "any.password"));
		assertFalse(authenticator.isAuthenticated("prefix-not-test", "any.password"));
	}

}
