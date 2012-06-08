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

import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Localizable;

/**
 * 
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class FindableHashtable extends java.util.Hashtable implements gda.util.findableHashtable.Hashtable, Findable,
		Configurable, Localizable {
	private boolean local = false;

	/**
	 * Flag indicating whether to store metadata to nexus file
	 */
	public static final String NEXUS_METADATA = "nexusMetadata";

	/**
	 * Flag indicating whether to store files to SRB/ICAT.
	 */
	public static final String SRB_STORE = "srbStore";

	private String name = "";

	@Override
	public void putBoolean(String key, boolean value) {
		super.put(key, value);
	}

	@Override
	public boolean getBoolean(String key) {
		return (Boolean) super.get(key);
	}

	@Override
	public void putInt(String key, int value) {
		super.put(key, new Integer(value));
	}

	@Override
	public int getInt(String key) {
		return ((Integer) super.get(key));
	}

	@Override
	public void putLong(String key, long value) {
		super.put(key, new Long(value));
	}

	@Override
	public long getLong(String key) {
		return ((Long) super.get(key));
	}

	@Override
	public void putFloat(String key, float value) {
		super.put(key, new Float(value));
	}

	@Override
	public float getFloat(String key) {
		return ((Float) super.get(key));
	}

	@Override
	public void putDouble(String key, double value) {
		super.put(key, new Double(value));
	}

	@Override
	public double getDouble(String key) {
		return ((Double) super.get(key));
	}

	@Override
	public void putString(String key, String value) {
		super.put(key, value);
	}

	@Override
	public String getString(String key) {
		return ((String) super.get(key));
	}

	@Override
	public void put(String key, Object value) {
		super.put(key, value);
	}

	@Override
	public Object get(String key) {
		return (super.get(key));
	}

	// Implement Findable interface.

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	// Implements Configurable interface.

	@Override
	public void configure() throws FactoryException {
	}

	// Implements Localizable interface.

	@Override
	public boolean isLocal() {
		return local;
	}

	@Override
	public void setLocal(boolean local) {
		this.local = local;
	}
}
