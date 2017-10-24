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

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.jython.authenticator.LdapMixin;

/**
 * Extension to the FileAuthoriser which fetches information from an ldap server if the account is not listed in the xml
 * files.
 */
public class LdapAuthoriser extends FileAuthoriser implements Authoriser {

	/**
	 * The java property to use to define which class of Authenticator to use
	 */
	public static final String LDAPSTAFF_PROPERTY = "gda.jython.authoriser.ldap.staff_group";

	private static final Logger logger = LoggerFactory.getLogger(LdapAuthoriser.class);

	private final String staffRole = LocalProperties.get(LDAPSTAFF_PROPERTY, "DLSLTD_Staff");

	private LdapMixin ldap = new LdapMixin();

	@Override
	public boolean hasAuthorisationLevel(String username) {
		// use the xml file as an override
		if (super.hasAuthorisationLevel(username)) {
			return true;
		}
		try {
			NamingEnumeration<SearchResult> users = ldap.searchLdapForUser(username, "cn", "memberOf", "sn", "givenName", "title");
			return users != null && users.hasMore();
		} catch (NamingException e) {
			logger.error("Failed to find user {} in ldap or xml file", username, e);
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
			 users = ldap.searchLdapForUser(username, "memberOf");
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
			logger.error("Failed to find attribute {} in ldap for user",  attributeName, e);
		}
		return "";
	}
}
