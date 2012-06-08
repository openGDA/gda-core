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

/**
 * Provides a method to get the correct Authenticator type as defined by the java property
 */
public abstract class AuthenticatorProvider {

	/**
	 * @return Authenticator - type is defined by the java property
	 * @throws Exception
	 */
	public static Authenticator getAuthenticator() throws Exception {
		Authenticator authenticator = null;
		String authenticatorTypeName = LocalProperties.get(Authenticator.AUTHENTICATORCLASS_PROPERTY,
				Authenticator.DEFAULT_AUTHENTICATOR);
		try {
			Class<?> authenticatorType = Class.forName(authenticatorTypeName);
			authenticator = (Authenticator) authenticatorType.newInstance();
		} catch (Exception e) {
			throw new Exception("AuthenticatorProvider cannot instantiate : " + authenticatorTypeName, e);
		}
		return authenticator;

	}

}
