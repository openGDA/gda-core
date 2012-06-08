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

package gda.data.generic;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * <b>Title: </b>Generic data structure.
 * </p>
 * <p>
 * <b>Description: </b>Instances of this class can be used to hold data for access by other objects, data produced by a
 * scan, or for data access from GDA Jython scripting. This class implements the <code>java.util.Map</code> interface,
 * but uses it's own internal data structure. Additional methods are provided that replicate a Jython dictionary.
 * </p>
 * <p>
 * <b>Notes for use in Jython: </b>This object will behave very similar to a Jython dictionary, except that pure Jython
 * dictionary methods will not function (unless they are implemented with the same name in this class). The use of
 * copy.deepcopy() does not apply for Java objects in Jython, so a new 'deepcopy' method is implemeted here that will
 * provide a new object that is identical to the original.
 * </p>
 * <p>
 * <b>Notes on the internal data structure: </b>If a <code>java.util.concurrent.ConcurrentHashMap</code> is used then
 * this is already thread safe in an efficient manner (the lock is not on the whole object). If the structure is changed
 * then be sure to make the modifier/access methods of this class <code>synchronised</code>.
 * </p>
 * <p>
 * <b>NOTE: </b>This generic data structure does not allow <code>null</code> keys or values.
 * </p>
 * 
 * @see java.util.concurrent.ConcurrentHashMap
 */

public class GenericData implements IGenericData {

	/** Private reference to the internal data structure. */
	private ConcurrentHashMap<String, Object> mMap = null;

	/**
	 * Constructor.
	 */
	public GenericData() {
		mMap = new ConcurrentHashMap<String, Object>();
	}

	/**
	 * This produces a formatted representation of the map contents. Each element of the returned <code>Vector</code>
	 * will contain "key : value"
	 * 
	 * @return Vector<String>
	 */
	@Override
	public Vector<String> print() {
		Vector<String> output = new Vector<String>();
		for (java.util.Map.Entry<String, Object> i : mMap.entrySet()) {
			output.add(i.getKey() + " : " + i.getValue());
		}
		return output;
	}

	/**
	 * This method will return a new object that has the same contents as this object. This is meant to be a substitute
	 * for Jython copy.deepcopy()
	 * 
	 * @return GenericData
	 */
	@Override
	public IGenericData deepcopy() {
		GenericData newCopy = new GenericData();
		newCopy.putAll(mMap);
		return newCopy;
	}

	/**
	 * This method will return a <code>Vector</code> containing the key/value pairs in a string representation. This
	 * is meant to replicate the Jython dictionary method items()
	 * 
	 * @return Vector<String>
	 */
	@Override
	public Vector<String> items() {
		Vector<String> javaList = new Vector<String>();
		for (String keys : mMap.keySet()) {
			javaList.add(new String("(\'" + keys + "\', " + this.get(keys) + ")"));
		}
		return javaList;
	}

	/**
	 * This will return a <code>Vector</code> containing the key in a string representation. This is meant to
	 * replicate the Jython dictionary method keys()
	 * 
	 * @return Vector<String>
	 */
	@Override
	public Vector<String> keys() {
		Vector<String> javaList = new Vector<String>();
		for (String keys : this.keySet()) {
			javaList.add(keys);
		}
		return javaList;
	}

	/**
	 * Returns true if this map contains a mapping for the specified key.
	 * 
	 * @param key
	 * @return boolean
	 */
	@Override
	public boolean containsKey(Object key) {
		return mMap.containsKey(key);
	}

	/**
	 * Returns the value to which this map maps the specified key.
	 * 
	 * @param key
	 * @return Object
	 */
	@Override
	public Object get(Object key) {
		return mMap.get(key);
	}

	/**
	 * Removes all mappings from this map (optional operation).
	 */
	@Override
	public void clear() {
		mMap.clear();
	}

	/**
	 * Returns true if this map contains a mapping for the specified key.
	 * 
	 * @param key
	 * @return boolean
	 */
	@Override
	public boolean containsKey(String key) {
		return mMap.containsKey(key);
	}

	/**
	 * Returns true if this map maps one or more keys to the specified value.
	 * 
	 * @param value
	 * @return boolean
	 */
	@Override
	public boolean containsValue(Object value) {
		return mMap.containsValue(value);
	}

	/**
	 * Returns a set view of the mappings contained in this map.
	 * 
	 * @return Set<Map.Entry<String, Object>>
	 */
	@Override
	public Set<Map.Entry<String, Object>> entrySet() {
		return mMap.entrySet();
	}

	/**
	 * Returns the value to which this map maps the specified key.
	 * 
	 * @param key
	 * @return Object
	 */
	@Override
	public Object get(String key) {
		return mMap.get(key);
	}

	/**
	 * Returns true if this map contains no key-value mappings.
	 * 
	 * @return boolean
	 */
	@Override
	public boolean isEmpty() {
		return mMap.isEmpty();
	}

	/**
	 * Returns a set view of the keys contained in this map.
	 * 
	 * @return Set
	 */
	@Override
	public Set<String> keySet() {
		return mMap.keySet();
	}

	/**
	 * Associates the specified value with the specified key in this map (optional operation).
	 * 
	 * @param key
	 * @param value
	 * @return Object
	 */
	@Override
	public Object put(String key, Object value) {
		return mMap.put(key, value);
	}

	/**
	 * Copies all of the mappings from the specified map to this map (optional operation).
	 * 
	 * @param t
	 */
	@Override
	public void putAll(Map<? extends String, ? extends Object> t) {
		mMap.putAll(t);
	}

	/**
	 * Removes the mapping for this key from this map if it is present (optional operation).
	 * 
	 * @param key
	 * @return Object
	 */
	@Override
	public Object remove(Object key) {
		return mMap.remove(key);
	}

	/**
	 * Returns the number of key-value mappings in this map.
	 * 
	 * @return int
	 */
	@Override
	public int size() {
		return mMap.size();
	}

	/**
	 * Returns a collection view of the values contained in this map.
	 * 
	 * @return Collection
	 */
	@Override
	public Collection<Object> values() {
		return mMap.values();
	}

}
