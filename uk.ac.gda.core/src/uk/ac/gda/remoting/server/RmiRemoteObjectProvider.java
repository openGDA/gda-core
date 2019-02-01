/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.remoting.server;

import java.util.Set;

/**
 * This defines the interface to be used to obtain dynamically exported RMI objects.
 *
 * @author James Mudd
 * @since GDA 9.12
 */
public interface RmiRemoteObjectProvider {

	/**
	 * Requests a object to be available via RMI the implementer can block during this method to perform the export if
	 * required. It will return a RmiObjectInfo which will specify details about the remote RMI object and how to
	 * connect.
	 *
	 * @param name
	 *            the name of the object to access
	 * @return a RmiObjectInfo specifying how to connect to the remote object if available or <code>null</code> if the
	 *         object is not available
	 */
	RmiObjectInfo getRemoteObject(String name);

	/**
	 * Asks the implementer to return the names of all the objects which implement the requested interface. Note this
	 * returns the names of objects which could be available remotely not necessarily those which currently are. However
	 * calling {@link #getRemoteObject(String)} with any of the returned names should return a non-null result.
	 *
	 * @param clazz
	 *            the fully qualified class name of interest
	 * @return a set of names of objects available remotely that implement the requested interface
	 */
	Set<String> getRemoteObjectNamesImplementingType(String clazz);

}
