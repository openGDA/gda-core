/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.data.generic;

import java.io.Serializable;
import java.util.Map;
import java.util.Vector;

public interface IGenericData extends Map<String, Object>, Serializable {

	/**
	 * This produces a formatted representation of the map contents. Each element of the returned <code>Vector</code>
	 * will contain "key : value"
	 * 
	 * @return Vector<String>
	 */
	public Vector<String> print();

	/**
	 * This method will return a new object that has the same contents as this object. This is meant to be a substitute
	 * for Jython copy.deepcopy()
	 * 
	 * @return GenericData
	 */
	public IGenericData deepcopy();

	/**
	 * This method will return a <code>Vector</code> containing the key/value pairs in a string representation. This is
	 * meant to replicate the Jython dictionary method items()
	 * 
	 * @return Vector<String>
	 */
	public Vector<String> items();

	/**
	 * This will return a <code>Vector</code> containing the key in a string representation. This is meant to replicate
	 * the Jython dictionary method keys()
	 * 
	 * @return Vector<String>
	 */
	public Vector<String> keys();

	/**
	 * Returns true if this map contains a mapping for the specified key.
	 * 
	 * @param key
	 * @return boolean
	 */
	public boolean containsKey(String key);

	/**
	 * Returns the value to which this map maps the specified key.
	 * 
	 * @param key
	 * @return Object
	 */
	public Object get(String key);

}