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

package gda.data.metadata.icat;

import gda.configuration.properties.LocalProperties;

import java.lang.reflect.Constructor;

/**
 * Provides the singleton Icat object. Only this class should be called from other packages.
 * <p>
 * To define which provider to use, set the java property Icat.ICAT_TYPE_PROP {@link Icat#ICAT_TYPE_PROP}.
 * <p>
 * Implementations of the Icat should be registered as extensions of Extension Point id "uk.ac.gda.core.icat"
 */
public class IcatProvider {

	private static Icat theInstance = null;

	/**
	 * Returns the singleton instance. If not already defined then uses the given IcatConnectionDetails when creating
	 * the singleton.
	 * 
	 * @return a new Icat instance based on the given connection details
	 * @throws Exception
	 */
	public static synchronized Icat getInstance() throws Exception {

		if (theInstance != null) {
			return theInstance;
		}

		String icatType = LocalProperties.get(Icat.ICAT_TYPE_PROP);

		try {

			// if property not defined then use the default class NullIcat
			if (icatType == null || icatType.isEmpty() || icatType.equals("null")) {
				theInstance = new NullIcat();
				return theInstance;
			}

			Class<?> c = Class.forName(icatType);
			Constructor<?> constructor = c.getConstructor((Class[]) null);
			theInstance = (Icat) constructor.newInstance((Object[]) null);

		} catch (Exception e) {
			throw new Exception("Exception trying to create Icat of type " + icatType + ". (Defined by property "
					+ Icat.ICAT_TYPE_PROP + ")", e);
		}

		
		return theInstance;
	}

	/**
	 * clears the singleton instance, if it exists. Used to re-initialise the environment for testing.
	 * 
	 */
	public static synchronized void deleteInstance() {
		if (theInstance != null) {
			theInstance = null;
		}
	}

}
