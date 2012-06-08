/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

/**
 * Class to hold user credentials.
 * 
 */
public class UserAuthentication {
	private static boolean authInfoSupplied = false;
	private static boolean useOSAuthentication = false;
	private static String username = "";
	private static String password = "";

	/**
	 * @return Flag to indicate if the values are those entered by the user
	 */
	public static boolean isAuthInfoSupplied() {
		return authInfoSupplied;
	}

	/**
	 * @return Flag to indicate if the user is the value obtained from System.getProperty("user.name") in which case the
	 *         password is empty
	 */
	public static boolean isUseOSAuthentication() {
		return useOSAuthentication;
	}

	/**
	 * @return Username
	 */
	public static String getUsername() {
		return username;
	}

	/**
	 * @return Password - non-empty if isUseOSAuthentication returns false
	 */
	public static String getPassword() {
		return password;
	}

	/**
	 * Set to use OSAuthenitcation
	 */
	public static void setToUseOSAuthentication() {
		UserAuthentication.authInfoSupplied = true;
		UserAuthentication.useOSAuthentication = true;
		UserAuthentication.username = System.getProperty("user.name");
		UserAuthentication.password = "";
	}

	/**
	 * 
	 */
	public static void clearValues() {
		UserAuthentication.authInfoSupplied = false;
		UserAuthentication.useOSAuthentication = false;
		UserAuthentication.username = "";
		UserAuthentication.password = "";
	}

	/**
	 * @param username
	 * @param password
	 */
	public static void setToNotUseOSAuthentication(String username, String password) {
		UserAuthentication.authInfoSupplied = true;
		UserAuthentication.useOSAuthentication = false;
		UserAuthentication.username = username;
		UserAuthentication.password = password;
	}

	/**
	 * @return true if the user is authenticated
	 * @throws Exception - if an exception during authentication
	 */
	public static boolean isAuthenticated() throws Exception {
		if (isUseOSAuthentication()) {
			return true;
		}

		// get the authenticator defined by java property
		Authenticator authenticator = AuthenticatorProvider.getAuthenticator();
		return authenticator.isAuthenticated(username, password);
	}
}
