package uk.ac.gda.client.closeactions.contactinfo;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.authenticator.LdapMixin;

public class LdapEmail {

	private final static Logger logger = LoggerFactory.getLogger(LdapEmail.class);

	private LdapMixin ldap = new LdapMixin();

	/**
	 * Retrieves email from ldap server for a given fedID
	 * Can return empty string if ldap search fails
	 */
	public String forFedID(String fedID) {
		NamingEnumeration<SearchResult> users = null;
		try {
			users = ldap.searchLdapForUser(fedID);
			if (users != null && users.hasMore()) {
				String email = extractAttributes(users.next(), "mail");
				return email;
			}
		} catch (NamingException e) {
			logger.error("Failed to retrieve email for user:" + fedID + e.getMessage());
		} finally {
			if (users != null) {
				try {
					users.close();
				} catch (NamingException e) {
					logger.error("Failed to close ldap user search");
				}
			}
		}
		return "";
	}

	private String extractAttributes(SearchResult sr, String attributeName) {
		try {
			//should only return a single email, as we're searching by individual fedID. 
			return sr.getAttributes().get(attributeName).get().toString();
		} catch (NamingException e) {
			logger.error("Failed to find attribute " + attributeName + " in ldap for user: " + e.getMessage());
		}
		return "";
	}
}
