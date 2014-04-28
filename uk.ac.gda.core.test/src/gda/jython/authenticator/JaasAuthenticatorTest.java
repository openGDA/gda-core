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

import static org.junit.Assert.*;
import gda.configuration.properties.LocalProperties;

import org.junit.Test;

/**
 * Test case for the JaasAuthenticator class
 */
public class JaasAuthenticatorTest {

	/**
	 * @throws Exception
	 */
	@Test
	public void testAuthenticatorCreation() throws Exception {
		LocalProperties.set(Authenticator.AUTHENTICATORCLASS_PROPERTY, "gda.jython.authenticator.JaasAuthenticator");
		LocalProperties.set(JaasAuthenticator.GDA_ACCESSCONTROL_JAAS_REALM, "FED.CCLRC.AC.UK");
		LocalProperties.set(JaasAuthenticator.GDA_ACCESSCONTROL_JAAS_KDC, "fed.cclrc.ac.uk");
		LocalProperties.set(JaasAuthenticator.GDA_ACCESSCONTROL_JAAS_CONFFILE, "/home/rjw82/workspaces/trunk/i02/properties/jaas.cclrc.conf");
		@SuppressWarnings("unused")
		JaasAuthenticator authenticator = (JaasAuthenticator) AuthenticatorProvider.getAuthenticator();
	}

	/**
	 * @throws Exception 
	 */
	@Test
	public void testIsAuthenticated() throws Exception {
		// we need to use the test user authenticator
		LocalProperties.set(Authenticator.AUTHENTICATORCLASS_PROPERTY, "gda.jython.authenticator.TestUserAuthenticator");
		TestUserAuthenticator authenticator = (TestUserAuthenticator) AuthenticatorProvider.getAuthenticator();
		assertTrue(authenticator.isAuthenticated("test-prefix", "any.password"));
		assertFalse(authenticator.isAuthenticated("prefix-not-test", "any.password"));
	}

}
