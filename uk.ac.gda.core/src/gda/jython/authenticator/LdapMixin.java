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

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;

/**
 * Contains LDAP-related methods.
 */
public class LdapMixin {
	/**
	 * The LDAP environment property used to define the timeout for connecting to a socket
	 */
    public static final String LDAP_ENV_CONNECT_TIMEOUT =
        "com.sun.jndi.ldap.connect.timeout";

	/**
	 * The LDAP environment property used to define the timeout for reading from a socket
	 */
    public static final String LDAP_ENV_READ_TIMEOUT =
        "com.sun.jndi.ldap.read.timeout";

	private static final Logger logger = LoggerFactory.getLogger(LdapMixin.class);

	private final String ldapContext = LocalProperties.get(LdapAuthenticator.LDAPCONTEXT_PROPERTY,
			"com.sun.jndi.ldap.LdapCtxFactory");

	/**
	 * The java property to use to define which class of Authenticator to use
	 */
	public static final String LDAPSTAFFCONTEXT_PROPERTY = "gda.jython.authoriser.ldap.staff_context";
	private final String staffContext = LocalProperties.get(LDAPSTAFFCONTEXT_PROPERTY, "DC=fed,DC=cclrc,DC=ac,DC=uk");

	private HostnameResolver resolver = new InetAddressHostnameResolver();

	public void setResolver(HostnameResolver resolver) {
		this.resolver = resolver;
	}

	public NamingEnumeration<SearchResult> searchLdapForUser(String fedId, String... requiredAtts) {

		final List<String> urls = getUrlsToTry();
		logger.debug("LDAP URLs: {}", urls);

		if (urls.isEmpty()) {
			logger.error("No LDAP servers defined");
			return null;
		}

		try {
			return searchLdapServersForUser(urls, fedId, requiredAtts);
		} catch (Exception e) {
			logger.error("Unable to connect to any LDAP server, servers tried: {}", urls, e);
			return null;
		}
	}

	/**
	 * @param urls list of server (typically IP) addresses which LDAP uses internally to find an available server
	 */
	private NamingEnumeration<SearchResult> searchLdapServersForUser(List<String> urls, String fedId, String... returnedAtts) throws NamingException {

		LdapContext ctx = null;
		try {
			if(fedId == null || fedId.isEmpty())
				return null;
			// Set up criteria on which to search
			// e.g. (&(objectClass=groupOfUniqueNames)(uniqueMember=uid=ifx999,ou=People,dc=esrf,dc=fr))
			String filter = "(&(objectClass=user)(cn=" + fedId + "))";

			// Set up search constraints
			SearchControls cons = new SearchControls();
			cons.setSearchScope(SearchControls.SUBTREE_SCOPE);
			cons.setReturningAttributes(returnedAtts);
			cons.setTimeLimit(getReadTimeoutMs());

			// Search
			ctx = createContext(urls);
			return ctx.search(staffContext, filter, cons);

		} finally {
			if (ctx != null) {
				try {
					ctx.close();
				} catch (NamingException e) {
				}
			}
		}
	}

	/**
	 * Creates and connects to a new LDAP context given a set of URLs
	 * @param ldapURLs The set of URLs to try
	 * @return A new {@link LdapContext}
	 * @throws NamingException
	 */
	public LdapContext createContext(List<String> ldapURLs) throws NamingException {
		return new InitialLdapContext(generateLdapEnv(ldapURLs), null);
	}

	/**
	 * Generates environment variables for an {@link LdapContext} to use for
	 * authentication. Must be a {@link Hashtable} due to requirements by a commonly
	 * used class implementing {@link LdapContext}, {@link InitialLdapContext}
	 * @param ldapURLs List of URLs to include in the environment
	 * @return A new {@link Hashtable}
	 */
	public Hashtable<String, String> generateLdapEnv(List<String> ldapURLs) {
		Hashtable<String, String> env = new Hashtable<>();

		// Setup parameters from Context
		env.put(Context.INITIAL_CONTEXT_FACTORY, ldapContext);
		env.put(Context.SECURITY_AUTHENTICATION, "none");
		env.put(Context.PROVIDER_URL, String.join(" ", ldapURLs));

		// Set timeouts
		env.put(LDAP_ENV_CONNECT_TIMEOUT,
				Integer.toString(getConnectTimeoutMs()));
		env.put(LDAP_ENV_READ_TIMEOUT,
				Integer.toString(getReadTimeoutMs()));

		return env;
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
			List<String> urls = new ArrayList<>();
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
		return resolver.resolveHostname(host.trim())
				.stream()
				.map(LdapMixin::hostToLdapUrl)
				.collect(Collectors.toList());
	}

	static String hostToLdapUrl(String host) {
		return String.format("ldap://%s:389", host);
	}

	private int getConnectTimeoutMs() {
		return LocalProperties
				.getInt(LdapAuthenticator.LDAPCONNECT_TIMEOUT_PROPERTY,
						LdapAuthenticator.DEFAULT_LDAP_CONNECT_TIMEOUT_MS);
	}

	private int getReadTimeoutMs() {
		return LocalProperties
				.getInt(LdapAuthenticator.LDAPREAD_TIMEOUT_PROPERTY,
						LdapAuthenticator.DEFAULT_LDAP_READ_TIMEOUT_MS);
	}

}
