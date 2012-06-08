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

import gda.device.DeviceException;

/**
 * An extended hashtable that can be 'found' and accessed via CORBA.
 */
public interface Hashtable {
	/**
	 * Add a boolean entry to the hashtable.
	 * 
	 * @param key
	 *            Key for entry in hashtable.
	 * @param value
	 *            Value for entry in hashtable.
	 * @throws DeviceException
	 */
	public void putBoolean(String key, boolean value) throws DeviceException;

	/**
	 * Get the value of a boolean entry from the hashtable.
	 * 
	 * @param key
	 *            Key for entry in hashtable.
	 * @return Value for entry in hashtable.
	 * @throws DeviceException
	 */
	public boolean getBoolean(String key) throws DeviceException;

	/**
	 * Add an integer entry to the hashtable.
	 * 
	 * @param key
	 *            Key for entry in hashtable.
	 * @param value
	 *            Value for entry in hashtable.
	 * @throws DeviceException
	 */
	public void putInt(String key, int value) throws DeviceException;

	/**
	 * Get the value of an integer entry from the hashtable.
	 * 
	 * @param key
	 *            Key for entry in hashtable.
	 * @return Value for entry in hashtable.
	 * @throws DeviceException
	 */
	public int getInt(String key) throws DeviceException;

	/**
	 * Add a long entry to the hashtable.
	 * 
	 * @param key
	 *            Key for entry in hashtable.
	 * @param value
	 *            Value for entry in hashtable.
	 * @throws DeviceException
	 */
	public void putLong(String key, long value) throws DeviceException;

	/**
	 * Get the value of a long entry from the hashtable.
	 * 
	 * @param key
	 *            Key for entry in hashtable.
	 * @return Value for entry in hashtable.
	 * @throws DeviceException
	 */
	public long getLong(String key) throws DeviceException;

	/**
	 * Add a float entry to the hashtable.
	 * 
	 * @param key
	 *            Key for entry in hashtable.
	 * @param value
	 *            Value for entry in hashtable.
	 * @throws DeviceException
	 */
	public void putFloat(String key, float value) throws DeviceException;

	/**
	 * Get the value of a float entry from the hashtable.
	 * 
	 * @param key
	 *            Key for entry in hashtable.
	 * @return Value for entry in hashtable.
	 * @throws DeviceException
	 */
	public float getFloat(String key) throws DeviceException;

	/**
	 * Add a double entry to the hashtable.
	 * 
	 * @param key
	 *            Key for entry in hashtable.
	 * @param value
	 *            Value for entry in hashtable.
	 * @throws DeviceException
	 */
	public void putDouble(String key, double value) throws DeviceException;

	/**
	 * Get the value of a double entry from the hashtable.
	 * 
	 * @param key
	 *            Key for entry in hashtable.
	 * @return Value for entry in hashtable.
	 * @throws DeviceException
	 */
	public double getDouble(String key) throws DeviceException;

	/**
	 * Add a string entry to the hashtable.
	 * 
	 * @param key
	 *            Key for entry in hashtable.
	 * @param value
	 *            Value for entry in hashtable.
	 * @throws DeviceException
	 */
	public void putString(String key, String value) throws DeviceException;

	/**
	 * Get the value of a string entry from the hashtable.
	 * 
	 * @param key
	 *            Key for entry in hashtable.
	 * @return Value for entry in hashtable.
	 * @throws DeviceException
	 */
	public String getString(String key) throws DeviceException;

	/**
	 * Add an entry to the hashtable. The object needs to implement {@link java.io.Serializable}.
	 * 
	 * @param key
	 *            Key for entry in hashtable.
	 * @param value
	 *            Value for entry in hashtable.
	 * @throws DeviceException
	 */
	public void put(String key, Object value) throws DeviceException;

	/**
	 * Get the value of an entry from the hashtable.
	 * 
	 * @param key
	 *            Key for entry in hashtable.
	 * @return Value for entry in hashtable.
	 * @throws DeviceException
	 */
	public Object get(String key) throws DeviceException;
}
