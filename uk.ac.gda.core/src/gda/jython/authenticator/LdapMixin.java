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

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains LDAP-related methods.
 */
public class LdapMixin {

	private static final Logger logger = LoggerFactory.getLogger(LdapMixin.class);
	
	private HostnameResolver resolver = new InetAddressHostnameResolver();
	
	public void setResolver(HostnameResolver resolver) {
		this.resolver = resolver;
	}
	
	/**
	 * Returns the LDAP URLs that should be used.
	 */
	public List<String> getUrlsToTry() {
		
		// Option 1: use the old URL property if it is set
		if (LocalProperties.contains(LdapAuthenticator.LDAPURL_PROPERTY)) {
			final String url = LocalProperties.get(LdapAuthenticator.LDAPURL_PROPERTY);
			return Collections.singletonList(url);
		}
		
		// Option 2: use the new hosts property
		if (LocalProperties.contains(LdapAuthenticator.LDAP_HOSTS_PROPERTY)) {
			final String hostList = LocalProperties.get(LdapAuthenticator.LDAP_HOSTS_PROPERTY);
			
			StringTokenizer st = new StringTokenizer(hostList, " ");
			List<String> urls = new ArrayList<String>();
			while (st.hasMoreTokens()) {
				final String host = st.nextToken();
				try {
					final List<String> urlsForThisHost = buildUrlsForHost(host);
					urls.addAll(urlsForThisHost);
				} catch (UnknownHostException e) {
					logger.error("Unknown host '" + host + "'", e);
				}
			}
			return urls;
		}
		
		// Option 3: use the default hostname
		try {
			final List<String> urls = buildUrlsForHost(LdapAuthenticator.DEFAULT_LDAP_HOST);
			return urls;
		} catch (UnknownHostException e) {
			logger.error("Unknown host '" + LdapAuthenticator.DEFAULT_LDAP_HOST + "'", e);
		}
		
		return Collections.emptyList();
	}
	
	/**
	 * Builds the LDAP URLs for the given host.
	 */
	List<String> buildUrlsForHost(String host) throws UnknownHostException {
		host = host.trim();
		List<String> urls = new ArrayList<String>();
		List<String> ips = resolver.resolveHostname(host);
		for (String ip : ips) {
			final String url = hostToLdapUrl(ip);
			urls.add(url);
		}
		return urls;
	}
	
	static String hostToLdapUrl(String host) {
		return String.format("ldap://%s:389", host);
	}
	
}
