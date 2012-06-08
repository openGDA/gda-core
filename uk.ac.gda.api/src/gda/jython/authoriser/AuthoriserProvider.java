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

/**
 * Provides a method to get the correct Authoriser type as defined by the java property
 */

public abstract class AuthoriserProvider {

	/**
	 * @return Authoriser - type is defined by the java property
	 * @throws ClassNotFoundException
	 */
	public static Authoriser getAuthoriser() throws ClassNotFoundException {
		Authoriser authoriser = null;
		String authoriserTypeName = LocalProperties.get(Authoriser.AUTHORISERCLASS_PROPERTY,
				Authoriser.DEFAULT_AUTHORISER);
		try {
			Class<?> authoriserType = Class.forName(authoriserTypeName);
			authoriser = (Authoriser) authoriserType.newInstance();
		} catch (Exception e) {
			throw new ClassNotFoundException("Cannot authorise as class not found: " + authoriserTypeName);
		}
		return authoriser;

	}

}
