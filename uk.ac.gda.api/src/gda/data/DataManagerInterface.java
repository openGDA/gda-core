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

package gda.data;

import gda.data.generic.IGenericData;

import java.util.Vector;

/**
 * <p>
 * <b>Title: </b>Interface class for manager of memory resident data objects.
 * </p>
 * <p>
 * <b>Description: </b>This interface can be used to define a manager class to create, destroy and provide access to
 * memory resident objects. Currently this is used to manage GenericData objects but could be made more generic later
 * on. Concrete implementations of this class should keep a searchable list of the objects and their names.
 * </p>
 */

public interface DataManagerInterface {
	/**
	 * Create a new <code>GenericData</code> object and give it a name.
	 * 
	 * @param name
	 *            The name of the object to create.
	 * @return A reference to the object.
	 */
	public IGenericData create(String name);

	/**
	 * Add an object to the list
	 * 
	 * @param name
	 *            The name of the object to add.
	 * @param data
	 *            The object.
	 */
	public void add(String name, IGenericData data);

	/**
	 * Remove this object from the list.
	 * 
	 * @param name
	 *            The name of the object to remove.
	 */
	public void remove(String name);

	/**
	 * Return a Vector of strings containing the names of all the objects in the list.
	 * 
	 * @return The names of the objects.
	 */
	public Vector<String> list();

	/**
	 * Returns a reference to the object of this name.
	 * 
	 * @param name
	 *            The name of the object.
	 * @return The object reference.
	 */
	public IGenericData get(String name);

}
