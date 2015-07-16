/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.util.dictionary;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.util.Assert;


@SuppressWarnings("rawtypes")
public class MapBasedDictionary extends Dictionary implements Map {

	private Map map;

	public void setMap(Map map) {
		this.map = map;
	}

	/**
	 * Enumeration wrapper around an Iterator.
	 *
	 * @author Costin Leau
	 *
	 */
	private static class IteratorBasedEnumeration implements Enumeration {

		private Iterator it;

		public IteratorBasedEnumeration(Iterator it) {
			Assert.notNull(it);
			this.it = it;
		}

		public IteratorBasedEnumeration(Collection col) {
			this(col.iterator());
		}

		@Override
		public boolean hasMoreElements() {
			return it.hasNext();
		}

		@Override
		public Object nextElement() {
			return it.next();
		}

	}

	public MapBasedDictionary(Map map) {
		this.map = (map == null ? new LinkedHashMap() : map);
	}

	/**
	 * Default constructor.
	 *
	 */
	public MapBasedDictionary() {
		this.map = new LinkedHashMap();
	}

	public MapBasedDictionary(int initialCapacity) {
		this.map = new LinkedHashMap(initialCapacity);
	}

	/**
	 * Constructor for dealing with existing Dictionary. Will copy the content
	 * into the inner Map.
	 *
	 * @param dictionary
	 */
	public MapBasedDictionary(Dictionary dictionary) {
		this(new LinkedHashMap(), dictionary);
	}

	public MapBasedDictionary(Map map, Dictionary dictionary) {
		this(map);
		if (dictionary != null)
			putAll(dictionary);
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	@Override
	public Set entrySet() {
		return map.entrySet();
	}

	@Override
	public Object get(Object key) {
		if (key == null)
			throw new NullPointerException();
		return map.get(key);
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public Set keySet() {
		return map.keySet();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object put(Object key, Object value) {
		if (key == null || value == null)
			throw new NullPointerException();

		return map.put(key, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void putAll(Map t) {
		map.putAll(t);
	}

	@SuppressWarnings("unchecked")
	public void putAll(Dictionary dictionary) {
		if (dictionary != null)
			// copy the dictionary
			for (Enumeration enm = dictionary.keys(); enm.hasMoreElements();) {
				Object key = enm.nextElement();
				map.put(key, dictionary.get(key));
			}
	}

	@Override
	public Object remove(Object key) {
		if (key == null)
			throw new NullPointerException();

		return map.remove(key);
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public Collection values() {
		return map.values();
	}

	@Override
	public Enumeration elements() {
		return new IteratorBasedEnumeration(map.values());
	}

	@Override
	public Enumeration keys() {
		return new IteratorBasedEnumeration(map.keySet());
	}

	@Override
	public String toString() {
		return map.toString();
	}

	@Override
	public boolean equals(Object obj) {
		// this should work nicely since the Dictionary implementations inside
		// the JDK are Maps also
		return map.equals(obj);
	}

	@Override
	public int hashCode() {
		return map.hashCode();
	}

}
