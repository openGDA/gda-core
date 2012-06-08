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

import gda.configuration.properties.LocalProperties;
import gda.jython.authenticator.LdapAuthenticator;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension to the FileAuthoriser which fetches information from an ldap server if the account is not listed in the xml
 * files.
 */
public class LdapAuthoriser extends FileAuthoriser implements Authoriser {

	/**
	 * The java property to use to define which class of Authenticator to use
	 */
	public static final String LDAPSTAFF_PROPERTY = "gda.jython.authoriser.ldap.staff_group";

	/**
	 * The java property to use to define which class of Authenticator to use
	 */
	public static final String LDAPSTAFFCONTEXT_PROPERTY = "gda.jython.authoriser.ldap.staff_context";

	private static final Logger logger = LoggerFactory.getLogger(LdapAuthoriser.class);

	private final String ldapURL = LocalProperties.get(LdapAuthenticator.LDAPURL_PROPERTY, "ldap://130.246.132.94:389");
	private final String ldapContext = LocalProperties.get(LdapAuthenticator.LDAPCONTEXT_PROPERTY,
			"com.sun.jndi.ldap.LdapCtxFactory");
	private final String staffRole = LocalProperties.get(LDAPSTAFF_PROPERTY, "DLSLTD_Staff");
	private final String staffContext = LocalProperties.get(LDAPSTAFFCONTEXT_PROPERTY, "DC=fed,DC=cclrc,DC=ac,DC=uk");

	@Override
	public boolean hasAuthorisationLevel(String username) {
		// use the xml file as an override
		if (super.hasAuthorisationLevel(username)) {
			return true;
		}
		try {
			NamingEnumeration<SearchResult> users = searchLdapForUser(username);
			return users != null && users.hasMore();
		} catch (NamingException e) {
			logger.error("Failed to find user " + username + " in ldap or xml file: " + e.getMessage());
			return false;
		}
	}

	@Override
	public int getAuthorisationLevel(String username) {
		// use the xml file as an override
		if (super.hasAuthorisationLevel(username)) {
			return super.getAuthorisationLevel(username);
		}
		// else use defaults
		if (isLocalStaff(username)) {
			return LocalProperties.getInt(FileAuthoriser.DEFAULTSTAFFLEVELPROPERTY, 2);
		}
		return LocalProperties.getInt(FileAuthoriser.DEFAULTLEVELPROPERTY, 1);

	}

	@Override
	public boolean isLocalStaff(String username) {
		// use the xml file as an override
		if (super.isLocalStaff(username)) {
			return true;
		}
		NamingEnumeration<SearchResult> users = null;
		try {
			 users = searchLdapForUser(username);
			if (users != null && users.hasMore()) {
				String groups = extractAttributes(users.next(), "memberOf");
				return groups.contains(staffRole);
			}
		} catch (NamingException e) {
			//
		} finally {
			if (users !=null){
				try {
					users.close();
				} catch (NamingException e) {
					//
				}
			}
		}
		return false;
	}

	private InitialLdapContext getContext() throws NamingException {
		Hashtable<String, String> env = new Hashtable<String, String>();

		env.put(Context.INITIAL_CONTEXT_FACTORY, ldapContext);
		env.put(Context.SECURITY_AUTHENTICATION, "none");
		env.put(Context.PROVIDER_URL, ldapURL);

		return new InitialLdapContext(env, null);
	}

	private NamingEnumeration<SearchResult> searchLdapForUser(String fedId) {
		InitialLdapContext ctx = null;
		try {
			if( fedId == null || fedId.isEmpty())
				return null;
			// Set up criteria on which to search
			// e.g. (&(objectClass=groupOfUniqueNames)(uniqueMember=uid=ifx999,ou=People,dc=esrf,dc=fr))
			String filter = "(&(objectClass=user)(cn=" + fedId + "))";

			// Set up search constraints
			String returnedAtts[] = { "cn", "memberOf", "sn", "givenName", "title" };
			SearchControls cons = new SearchControls();
			cons.setSearchScope(SearchControls.SUBTREE_SCOPE);
			cons.setReturningAttributes(returnedAtts);

			// Search
			ctx = getContext(/*fedId*/);
			return ctx.search(staffContext, filter, cons);

		} catch (NamingException e) {
			logger.error("Error searchingLdapForUser for fedid `"+ fedId + "`", e);
			return null;
		} finally {
			if (ctx != null) {
				try {
					ctx.close();
				} catch (NamingException e) {
				}
			}
		}

	}

	private String extractAttributes(SearchResult sr, String attributeName) {
		try {
			// loop through all parts of the attribute whose name matches attributeName
			NamingEnumeration<?> parts = sr.getAttributes().get(attributeName).getAll();
			String results = new String();
			while (parts.hasMore()) {
				String part = (String) parts.next();
				String h1[] = part.split(",");
				String h2[] = h1[0].split("=");
				part = h2[h2.length - 1];

				results += part + ",";
			}
			return results;
		} catch (NamingException e) {
			logger.error("Failed to find attribute " + attributeName + " in ldap for user: " + e.getMessage());
		}
		return "";
	}
}
