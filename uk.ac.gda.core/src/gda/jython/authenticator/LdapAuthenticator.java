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

import gda.configuration.properties.LocalProperties;

import java.util.Hashtable;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses an ldap server for authentication of a username and password
 */
public class LdapAuthenticator implements Authenticator, PasswordAuthenticator {
	
	private static final Logger logger = LoggerFactory.getLogger(LdapAuthenticator.class);
	
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
	
	final String ldapURL = LocalProperties.get(LDAPURL_PROPERTY, "ldap://130.246.132.94:389");
	final String ldapContext = LocalProperties.get(LDAPCONTEXT_PROPERTY, "com.sun.jndi.ldap.LdapCtxFactory");
	final String adminName = LocalProperties.get(LDAPADMIN_PROPERTY, ",OU=DLS,DC=fed,DC=cclrc,DC=ac,DC=uk");
	
	@Override
	public boolean isAuthenticated(String fedId, String password) {
		
		//must have a password!
		if (password.trim().equals("")){
			return false;
		}
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
		} catch (NamingException e) {
			logger.error("LDAP NamingException: " + e.getMessage());
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
	
	@Override
	public Object authenticate(String username, String password, ServerSession session) {
		return (isAuthenticated(username, password)) ? username : null;
	}
}
