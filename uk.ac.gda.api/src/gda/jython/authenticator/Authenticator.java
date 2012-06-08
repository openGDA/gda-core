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


/**
 * An interface for objects providing an authentication service for the GDA Command Server.
 * 
 */
public interface Authenticator {

	/**
	 * The java property to use to define which class of Authenticator to use. If not set, OS authentication is used.
	 */
	public static final String AUTHENTICATORCLASS_PROPERTY = "gda.gui.AcquisitionGUI.authenticationMethod";

	/**
	 * The default java property if none defined.
	 */
	public static final String DEFAULT_AUTHENTICATOR = "gda.jython.authenticator.LdapAuthenticator";

	/**
	 * The name this object should use in the xml configuration file. Such an object should be in the same Object Server
	 * as the JythonServer object.
	 */
	public static final String NAME = "authenticator";

	/**
	 * Returns true if the username and password can be authenticated.
	 * 
	 * @param username
	 * @param password
	 * @return boolean
	 */
	public boolean isAuthenticated(String username, String password);

}
