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

package uk.ac.gda.util.map;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.gda.util.list.ListUtils;

public class MapUtils {

	/**
	 * Create a map from the two list of strings.
	 * @param list1
	 * @param list2
	 * @return Map
	 */
	public static Map<String, String> createMap(String[] list1, String[] list2) {
		final Map<String,String> ret = new HashMap<String, String>(list1.length);
		for (int i = 0; i < list1.length; i++) {
			ret.put(list1[i].trim(), list2[i].trim());
		}
		return ret;
	}

	/**
	 * Like createMap(...) but uses a LinkedHashMap to maintian the items in the
	 * same order as they are in list1.
	 * @param list1
	 * @param list2
	 * @return ordered map.
	 */
	public static Map<String, String> createLinkedMap(String[] list1, String[] list2) {
		final Map<String,String> ret = new LinkedHashMap<String,String>();
		for (int i = 0; i < list1.length; i++) {
			ret.put(list1[i].trim(), list2[i].trim());
		}
		return ret;
	}
	
	/**
	 * Adds the map add into data but generates unique keys
	 * 
	 * @param data
	 * @param add
	 */
	public static <V> void putAllUniqueStrings(final Map<String,V> data, final Map<String,V> add) {
		
		for (String key : add.keySet()) {
		    V value = add.get(key);
			if (data.containsKey(key)) {
				key = getUniqueKey(data.keySet(), key);
			}
			data.put(key, value);
		}
	}

	/**
	 * 
	 * @param data
	 * @param key
	 * @param value
	 */
	public static <V> void putUnique(Map<String, V> data, String key, V value) {
		if (data.containsKey(key)) {
			key = getUniqueKey(data.keySet(), key);
		}
		data.put(key, value);
	}

	/**
	 * 
	 * @param keySet
	 * @param key
	 * @return unique string
	 */
	private static String getUniqueKey(Set<String> keySet, String key) {
		
        int num = 1;
        while(keySet.contains(key+num)) num++;
        return key+num;
	}
	
	
	/**
	 * 
	 * @param value
	 * @return v
	 */
	public static String getString(final Map<String,String> value) {
		if (value == null)   return null;
		if (value.isEmpty()) return null;
		final String line = value.toString();
		return line.substring(1,line.length()-1); // key=value, key1=value1, ...
	}
	
	/**
	 * 
	 * @param value
	 * @return v
	 */
	public static Map<String,String> getMap(final String value) {
		
		if (value == null)           return null;
		if ("".equals(value.trim())) return null;
		final List<String> lines = ListUtils.getList(value);
		if (lines==null)     return null;
		if (lines.isEmpty()) return Collections.emptyMap();
		
		final Map<String,String> ret = new LinkedHashMap<String, String>(lines.size());
		for (String line : lines) {
			final String[] kv = line.split("=");
			if (kv==null||kv.length!=2) continue;
			ret.put(kv[0].trim(), kv[1].trim());
		}
		return ret;
	}

	public static void main(String[] args) {
		String test = "key=value, key1=value1, key2=value2";
		Map<String,String> v = getMap(test);
		System.out.println(v);
		
		test = " ";
		v = getMap(test);
		System.out.println(v);
		
		test = null;
		v = getMap(test);
		System.out.println(v);

		test = ",";
		v = getMap(test);
		System.out.println(v);
		
		test = "=";
		v = getMap(test);
		System.out.println(v);

		test = ",=,";
		v = getMap(test);
		System.out.println(v);
		
		test = ",,key=value";
		v = getMap(test);
		System.out.println(v);

	}

}
