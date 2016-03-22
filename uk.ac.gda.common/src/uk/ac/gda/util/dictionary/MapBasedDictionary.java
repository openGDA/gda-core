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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.util.Assert;

/**
 * Wrapper to allow a Map to be used in contexts where a Dictionary is needed.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 * @deprecated Use {@link Hashtable} instead
 */
@Deprecated
public class MapBasedDictionary<K, V> extends Dictionary<K, V> implements Map<K, V> {

	private Map<K, V> map;

	public void setMap(Map<K, V> map) {
		this.map = map;
	}

	/**
	 * Enumeration wrapper around an Iterator.
	 *
	 * @author Costin Leau
	 *
	 */
	private static class IteratorBasedEnumeration<E> implements Enumeration<E> {

		private Iterator<E> it;

		public IteratorBasedEnumeration(Iterator<E> it) {
			Assert.notNull(it);
			this.it = it;
		}

		public IteratorBasedEnumeration(Collection<E> col) {
			this(col.iterator());
		}

		@Override
		public boolean hasMoreElements() {
			return it.hasNext();
		}

		@Override
		public E nextElement() {
			return it.next();
		}

	}

	public MapBasedDictionary(Map<K, V> map) {
		this.map = (map == null ? new LinkedHashMap<K, V>() : map);
	}

	/**
	 * Default constructor.
	 *
	 */
	public MapBasedDictionary() {
		this.map = new LinkedHashMap<K, V>();
	}

	public MapBasedDictionary(int initialCapacity) {
		this.map = new LinkedHashMap<K, V>(initialCapacity);
	}

	/**
	 * Constructor for dealing with existing Dictionary. Will copy the content
	 * into the inner Map.
	 *
	 * @param dictionary
	 */
	public MapBasedDictionary(Dictionary<K, V> dictionary) {
		this(new LinkedHashMap<K, V>(), dictionary);
	}

	public MapBasedDictionary(Map<K, V> map, Dictionary<K, V> dictionary) {
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
	public Set<Entry<K, V>> entrySet() {
		return map.entrySet();
	}

	@Override
	public V get(Object key) {
		if (key == null)
			throw new NullPointerException();
		return map.get(key);
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public Set<K> keySet() {
		return map.keySet();
	}

	@Override
	public V put(K key, V value) {
		if (key == null || value == null)
			throw new NullPointerException();

		return map.put(key, value);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> t) {
		map.putAll(t);
	}

	public void putAll(Dictionary<K, V> dictionary) {
		if (dictionary != null)
			// copy the dictionary
			for (Enumeration<K> enm = dictionary.keys(); enm.hasMoreElements();) {
			K key = enm.nextElement();
				map.put(key, dictionary.get(key));
			}
	}

	@Override
	public V remove(Object key) {
		if (key == null)
			throw new NullPointerException();

		return map.remove(key);
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public Collection<V> values() {
		return map.values();
	}

	@Override
	public Enumeration<V> elements() {
		return new IteratorBasedEnumeration<V>(map.values());
	}

	@Override
	public Enumeration<K> keys() {
		return new IteratorBasedEnumeration<K>(map.keySet());
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
