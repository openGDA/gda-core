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
	 * @throws ClassNotFoundException
	 */
	public static Authenticator getAuthenticator() throws ClassNotFoundException {
		String authenticatorTypeName = LocalProperties.get(Authenticator.AUTHENTICATORCLASS_PROPERTY, Authenticator.DEFAULT_AUTHENTICATOR);
		try {
			@SuppressWarnings("unchecked")
			Class<? extends Authenticator> authenticatorType = (Class<? extends Authenticator>) Class.forName(authenticatorTypeName);
			return authenticatorType.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new ClassNotFoundException(String.format("Unable to find class %s, will not be able to authenticate", authenticatorTypeName));
		}

	}

}
