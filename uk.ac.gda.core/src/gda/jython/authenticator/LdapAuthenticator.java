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

import java.util.Hashtable;
import java.util.List;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;

/**
 * Uses an ldap server for authentication of a username and password
 */
public class LdapAuthenticator implements Authenticator {

	private static final Logger logger = LoggerFactory.getLogger(LdapAuthenticator.class);

	/**
	 * Property that holds a space-separated list of LDAP hostnames and/or IP addresses.
	 */
	public static final String LDAP_HOSTS_PROPERTY = "gda.jython.authenticator.ldap.hosts";

	public static final String DEFAULT_LDAP_HOST = "altfed.cclrc.ac.uk";

	/**
	 * The java property to use to define the url of the ldap server
	 */
	public static final String LDAPURL_PROPERTY = "gda.jython.authenticator.ldap.url";

	/**
	 * The java property to use to define the class of the initial context factory to use
	 */
	public static final String LDAPCONTEXT_PROPERTY = "gda.jython.authenticator.ldap.context";

	/**
	 * The java property to use to define the string of the SECURITY_PRINCIPAL to use
	 */
	public static final String LDAPADMIN_PROPERTY = "gda.jython.authenticator.ldap.admin";

	final String ldapContext = LocalProperties.get(LDAPCONTEXT_PROPERTY, "com.sun.jndi.ldap.LdapCtxFactory");
	final String adminName = LocalProperties.get(LDAPADMIN_PROPERTY, ",OU=DLS,DC=fed,DC=cclrc,DC=ac,DC=uk");

	private LdapMixin ldap = new LdapMixin();

	@Override
	public boolean isAuthenticated(String fedId, String password) {

		//must have a password!
		if (password.trim().equals("")){
			return false;
		}

		final List<String> urls = ldap.getUrlsToTry();
		logger.debug("LDAP URLs: " + urls);

		if (urls.isEmpty()) {
			logger.error("No LDAP servers defined");
			return false;
		}

		Exception lastException = null;

		for (String url : urls) {
			try {
				return checkAuthenticatedUsingServer(url, fedId, password);
			} catch (Exception e) {
				// try the next server
				lastException = e;
				logger.info("Unable to use LDAP server with URL '{}' - will try next server", url, e);
			}
		}

		logger.error("Unable to connect to any LDAP server", lastException);
		return false;
	}

	private boolean checkAuthenticatedUsingServer(String ldapURL, String fedId, String password) throws NamingException {

		InitialLdapContext ctx = null;
		try {
			Hashtable<String, String> env = new Hashtable<String, String>();
			String principal = "CN=" + fedId + adminName;
			env.put(Context.INITIAL_CONTEXT_FACTORY, ldapContext);
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			env.put(Context.SECURITY_PRINCIPAL, principal);
			env.put(Context.SECURITY_CREDENTIALS, password);
			env.put(Context.PROVIDER_URL, ldapURL);
			ctx = new InitialLdapContext(env, null);
			//if no exception then password is OK
			return true;
		} catch (AuthenticationException ae) {
			logger.error("LDAP AuthenticationException: " + StringEscapeUtils.escapeJava(ae.getMessage()));
		} finally {
			if (ctx != null){
				try {
					ctx.close();
				} catch (NamingException e) {
				}
			}
		}
		return false;
	}
}
