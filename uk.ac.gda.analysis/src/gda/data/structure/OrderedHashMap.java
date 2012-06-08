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

package gda.data.structure;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a HashMap which can be indexed by integer (i.e. the order in which they were added). Its basically a vector
 * which stores the keys as the key,value pairs are added. When you look for the index you use the key at index i in the
 * vector to get the value The put and remove methods or hashmap are overridden to add the keys to the vector and a
 * get(int index) method is added and put(index,value) are added and so we can get a value by index or by key E.g.
 * OrderHashMap<String,Double> mymap = new OrderHashMap<String,Double>(); mymap.put("area",1.0);
 * mymap.put("position",2.0); mymap.put("width",5.0); Message.debug("First Value added\t"+mymap.get(0));
 * Message.debug("First Value added\t"+mymap.); I originally wrote this so that you could get functions or function
 * parameters by name or index in a neat way for the curve fitting codes but it can be used for anything....... Generic
 * Types
 * 
 * @param <K>
 * @param <V>
 */
public class OrderedHashMap<K, V> extends HashMap<K, V> {
	private static final Logger logger = LoggerFactory.getLogger(OrderedHashMap.class);

	/**
	 * A vector containing a list of keys As data is added to the hashmap this vector is used to store the keys. It is
	 * then used as a record of the order in which keys are entered.
	 */
	private Vector<K> keyList = null;

	/**
	 * Constructor
	 */
	public OrderedHashMap() {
		super();
		keyList = new Vector<K>();
	}

	/**
	 * @param index
	 * @return Object at index
	 */
	public V get(int index) {
		// Return the object at index by using the key
		// at index in the keyList and looking up the hashtable
		if (index >= this.size() || index < 0) {
			throw new IllegalArgumentException("Index\t" + index + " is out or range");
		}
		return super.get(keyList.get(index));
	}

	@Override
	public V put(K key, V value) {
		// Add the key to the keylist
		if (!keyList.contains(key)) {
			keyList.add(key);
		}
		// put the key,value pair in the hashmap
		return super.put(key, value);
	}

	/**
	 * @param index
	 * @param value
	 * @return previous value associated with specified index, or <tt>null</tt> if there was no mapping for index. A
	 *         <tt>null</tt> return can also indicate that the HashMap previously associated <tt>null</tt> with the
	 *         specified key.
	 */
	public V put(int index, V value) {
		// is the index in the range of existing values ?
		if (index >= this.size() || index < 0) {
			throw new IllegalArgumentException("Index\t" + index + " is out or range");
		}
		// return the value
		return super.put(keyList.get(index), value);
	}

	@Override
	public V remove(Object arg0) {
		// Remove the key from the keylist vector
		keyList.remove(arg0);
		// Remove the key,value pair from the hashmap
		return super.remove(arg0);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> arg0) {
		// List the entries
		for (Iterator<? extends K> it = arg0.keySet().iterator(); it.hasNext();) {
			K key = it.next();
			V value = arg0.get(key);
			this.put(key, value);
			if (!keyList.contains(key)) {
				keyList.add(key);
			}
		}
	}

	@Override
	public void clear() {
		// Clear the keylist vector
		keyList.clear();
		// Clear the hashmap
		super.clear();
	}

	/**
	 * @param index
	 * @return The key at index
	 */
	public K getKey(int index) {
		checkIndex(index);
		return keyList.get(index);
	}

	/**
	 * @param arg0
	 * @return The key of Object, returns null if object is not is map
	 */
	public K getKey(Object arg0) {
		// List the entries
		for (Iterator<? extends K> it = this.keySet().iterator(); it.hasNext();) {
			K key = it.next();
			V value = this.get(key);
			if (value.equals(arg0))
				return key;
		}
		return null;
	}

	/**
	 * @param arg0
	 * @return The key of Object, returns null if object is not is map
	 */
	public K getValue(Object arg0) {
		// List the entries
		for (Iterator<? extends K> it = this.keySet().iterator(); it.hasNext();) {
			K key = it.next();
			V value = this.get(key);
			if (value.equals(arg0))
				return key;
		}
		return null;
	}

	/**
	 * Check the index is in range.
	 * 
	 * @param index
	 */
	public void checkIndex(int index) {
		// Message.debug("this size\t"+this.size());
		// Message.debug("this size\t"+keyList.size());
		// Message.debug("this size\t"+super.size());

		if (index >= this.size() || index < 0) {
			throw new IllegalArgumentException("Index\t" + index + " is out or range");
		}
	}

	/**
	 * Main method used for testing.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// A simple test of the orderedhashmap
		OrderedHashMap<String, Double> mymap = new OrderedHashMap<String, Double>();
		mymap.put("area", 1.0);
		mymap.put("position", 2.0);
		mymap.put("width", 5.0);
		for (int i = 0; i < mymap.size(); i++) {
			logger.debug("Map index\t" + mymap.get(i));
		}
	}

}
