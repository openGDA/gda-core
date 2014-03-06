/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

import gda.configuration.properties.LocalProperties;

import java.util.List;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

public class LdapMixinTest {
	
	private static HostnameResolver resolver;
	
	@BeforeClass
	public static void createTestResolver() {
		TestHostnameResolver r = new TestHostnameResolver();
		r.addEntry(LdapAuthenticator.DEFAULT_LDAP_HOST, "1.1.1.1", "2.2.2.2");
		r.addEntry("ldap.example.com", "3.3.3.3", "4.4.4.4");
		resolver = r;
	}
	
	@Test
	public void testHostToLdapUrl() {
		final String host = "hostname";
		final String url = LdapMixin.hostToLdapUrl(host);
		Assert.assertEquals("ldap://hostname:389", url);
	}
	
	@Test
	public void testBuildUrlsForHostWithHostname() throws Exception {
		LdapMixin ldap = new LdapMixin();
		ldap.setResolver(resolver);
		List<String> urls = ldap.buildUrlsForHost("localhost");
		assertUrlsEqual(urls, "ldap://127.0.0.1:389", "ldap://[::1]:389");
	}
	
	@Test
	public void testBuildUrlsForHostWithIpAddress() throws Exception {
		LdapMixin ldap = new LdapMixin();
		List<String> urls = ldap.buildUrlsForHost("240.0.0.0");
		assertUrlsEqual(urls, "ldap://240.0.0.0:389");
	}
	
	@Test
	public void testGetUrlsToTryWithOldUrlPropertySet() {
		try {
			final String url = "this_should_be_a_url";
			LocalProperties.set(LdapAuthenticator.LDAPURL_PROPERTY, url);
			LdapMixin ldap = new LdapMixin();
			List<String> urls = ldap.getUrlsToTry();
			assertUrlsEqual(urls, url);
		} finally {
			LocalProperties.clearProperty(LdapAuthenticator.LDAPURL_PROPERTY);
		}
	}
	
	@Test
	public void testGetUrlsToTryWithNewHostPropertySetToSingleHost() {
		try {
			LocalProperties.set(LdapAuthenticator.LDAP_HOSTS_PROPERTY, "ldap.example.com");
			LdapMixin ldap = new LdapMixin();
			ldap.setResolver(resolver);
			List<String> urls = ldap.getUrlsToTry();
			assertUrlsEqual(urls, "ldap://3.3.3.3:389", "ldap://4.4.4.4:389");
		} finally {
			LocalProperties.clearProperty(LdapAuthenticator.LDAP_HOSTS_PROPERTY);
		}
	}
	
	@Test
	public void testGetUrlsToTryWithNewHostPropertySetToMultipleHosts() {
		try {
			final String propValue = String.format("    %s   ldap.example.com ", LdapAuthenticator.DEFAULT_LDAP_HOST);
			LocalProperties.set(LdapAuthenticator.LDAP_HOSTS_PROPERTY, propValue);
			LdapMixin ldap = new LdapMixin();
			ldap.setResolver(resolver);
			List<String> urls = ldap.getUrlsToTry();
			assertUrlsEqual(urls, "ldap://1.1.1.1:389", "ldap://2.2.2.2:389", "ldap://3.3.3.3:389", "ldap://4.4.4.4:389");
		} finally {
			LocalProperties.clearProperty(LdapAuthenticator.LDAP_HOSTS_PROPERTY);
		}
	}
	
	@Test
	public void testGetUrlsToTryWithNoPropertiesSet() {
		LdapMixin ldap = new LdapMixin();
		ldap.setResolver(resolver);
		List<String> urls = ldap.getUrlsToTry();
		assertUrlsEqual(urls, "ldap://1.1.1.1:389", "ldap://2.2.2.2:389");
	}
	
	private static void assertUrlsEqual(List<String> actualUrls, String... expectedUrls) {
		Assert.assertEquals(expectedUrls.length, actualUrls.size());
		for (String u : expectedUrls) {
			Assert.assertTrue("missing URL: '" + u + "'", actualUrls.contains(u));
		}
	}
	
}
