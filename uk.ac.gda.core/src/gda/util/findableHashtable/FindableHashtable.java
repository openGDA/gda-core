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

package gda.util.findableHashtable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import gda.factory.FindableConfigurableBase;
import uk.ac.gda.api.remoting.ServiceInterface;

@ServiceInterface(Hashtable.class)
public class FindableHashtable extends FindableConfigurableBase implements Hashtable {

	/**
	 * Flag indicating whether to store metadata to nexus file
	 */
	public static final String NEXUS_METADATA = "nexusMetadata";

	/**
	 * Flag indicating whether to store files to SRB/ICAT.
	 */
	public static final String SRB_STORE = "srbStore";

	private Map<Object, Object> hashTable = new ConcurrentHashMap<>();

	@Override
	public void putBoolean(String key, boolean value) {
		hashTable.put(key, value);
	}

	@Override
	public boolean getBoolean(String key) {
		return (Boolean) hashTable.get(key);
	}

	@Override
	public void putInt(String key, int value) {
		hashTable.put(key, value);
	}

	@Override
	public int getInt(String key) {
		return (Integer) hashTable.get(key);
	}

	@Override
	public void putLong(String key, long value) {
		hashTable.put(key, value);
	}

	@Override
	public long getLong(String key) {
		return (Long) hashTable.get(key);
	}

	@Override
	public void putFloat(String key, float value) {
		hashTable.put(key, value);
	}

	@Override
	public float getFloat(String key) {
		return (Float) hashTable.get(key);
	}

	@Override
	public void putDouble(String key, double value) {
		hashTable.put(key, value);
	}

	@Override
	public double getDouble(String key) {
		return (Double) hashTable.get(key);
	}

	@Override
	public void putString(String key, String value) {
		hashTable.put(key, value);
	}

	@Override
	public String getString(String key) {
		return (String) hashTable.get(key);
	}

	@Override
	public void put(String key, Object value) {
		hashTable.put(key, value);
	}

	@Override
	public Object get(String key) {
		return hashTable.get(key);
	}
}
