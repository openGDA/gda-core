/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

/**
 * Provides authentication within the Authenticator interface using JAAS. 
 */
public class JaasAuthenticator implements Authenticator, CallbackHandler {

	private String username;

	private String password;

	private boolean authenticated = false;

	public static final String GDA_ACCESSCONTROL_JAAS_REALM = "gda.accesscontrol.jaas.realm";
	
	public static final String GDA_ACCESSCONTROL_JAAS_KDC = "gda.accesscontrol.jaas.kdc";
	
	public static final String GDA_ACCESSCONTROL_JAAS_CONFFILE = "gda.accesscontrol.jaas.confFile";
	
	/**
	 * Constructor. Throws exception if java properties defining the Kerberos authentication are missing.
	 * 
	 * @throws Exception
	 */
	public JaasAuthenticator() throws Exception {
		String realm = LocalProperties.get(GDA_ACCESSCONTROL_JAAS_REALM);
		String kdc = LocalProperties.get(GDA_ACCESSCONTROL_JAAS_KDC);
		String confFile = LocalProperties.get(GDA_ACCESSCONTROL_JAAS_CONFFILE);
		
		if (realm == null || kdc == null || confFile == null){
			throw new Exception("Missing java properties for Jass configuration!");
		}
		 
		System.setProperty("java.security.krb5.realm", realm);
		System.setProperty("java.security.krb5.kdc", kdc);
		System.setProperty("java.security.auth.login.config", confFile);
	}

	@Override
	public synchronized boolean isAuthenticated(String username, String password) {

		authenticated = false;
		this.username = username;
		this.password = password;
		try {
			LoginContext lc = new LoginContext("JaasAuth", this);

			// if this fails, a LoginException will be thrown
			lc.login();
			
			authenticated = true;
		} catch (LoginException e) {
			authenticated = false;
		}

		return authenticated;
	}

	@Override
	public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
		for (int i = 0; i < callbacks.length; i++) {
			if (callbacks[i] instanceof NameCallback) {
				final NameCallback nc = (NameCallback) callbacks[i];
				nc.setName(username);
			} else if (callbacks[i] instanceof PasswordCallback) {
				final PasswordCallback pc = (PasswordCallback) callbacks[i];
				pc.setPassword(password.toCharArray());
			} else {
				throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
			}
		}
	}
}
