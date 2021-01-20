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

package gda.jython;

import java.util.Set;

import gda.device.DeviceException;
import gda.factory.Findable;

/**
 * For objects with access to the Jython Server namespace.
 */
public interface IJythonNamespace {
	/**
	 * Pass a copy of an object to the Jython interpreter. This object must be relatively simple otherwise it will not
	 * be passed over CORBA successfully. So the object must be a native type (or only contain native types) and must
	 * not have any object references inside.
	 * 
	 * @param objectName
	 * @param obj
	 */
	public void placeInJythonNamespace(String objectName, Object obj);

	/**
	 * Get a copy of an object from the Jython interpreter. Note that the retrieved object will have to be cast by the
	 * local code. As the object will be passed over CORBA, this method should only be used on native types or classes
	 * containing only native types.
	 * 
	 * @param objectName
	 * @return Object
	 */
	public Object getFromJythonNamespace(String objectName);
	
	/**
	 * Returns all names for an Object in the Jython namespace
	 *
	 * @param obj
	 * @return All names that refer to the Object in the Jython namespace
	 * @throws DeviceException
	 */
	public Set<String> getAllNamesForObject(Object obj) throws DeviceException;

	/**
	 * As a Collection<String>, this should be able to be passed over any serialisation method and is therefore available
	 * to client code. We may have e.g. Scannables written entirely in Jython that we may not be able to pass across,
	 * meaning the similar method {@code LocalJython#getAllObjectsOfType} is limited to server side.
	 *
	 * @param clazz
	 *            a Class extending Findable
	 * @return a set of all names of all objects of a type in the Jython namespace.
	 */

	public <F extends Findable> Set<String> getAllNamesForType(Class<F> clazz);

}
