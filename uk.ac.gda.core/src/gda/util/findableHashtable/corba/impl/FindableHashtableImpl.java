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

package gda.util.findableHashtable.corba.impl;

import gda.device.corba.CorbaDeviceException;
import gda.util.findableHashtable.FindableHashtable;
import gda.util.findableHashtable.corba.CorbaFindableHashtablePOA;

import java.io.Serializable;

import org.omg.CORBA.Any;

/**
 * A server side implementation for a distributed Metadata class
 */
public class FindableHashtableImpl extends CorbaFindableHashtablePOA {
	private FindableHashtable hashtable;

	private org.omg.PortableServer.POA poa;

	/**
	 * Create server side implementation to the CORBA package.
	 * 
	 * @param hashtable
	 *            the FindableHashtable implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public FindableHashtableImpl(FindableHashtable hashtable, org.omg.PortableServer.POA poa) {
		this.hashtable = hashtable;
		this.poa = poa;
	}

	/**
	 * Get the implementation object
	 * 
	 * @return the Metadata implementation object
	 */
	public FindableHashtable _delegate() {
		return hashtable;
	}

	/**
	 * Set the implementation object.
	 * 
	 * @param hashtable
	 *            set the FindaleHashtable implementation object
	 */
	public void _delegate(FindableHashtable hashtable) {
		this.hashtable = hashtable;
	}

	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	// Implementation of Findable interface.

	@Override
	public boolean getBoolean(String name) throws CorbaDeviceException {
		boolean booleanValue = false;

		booleanValue = hashtable.getBoolean(name);

		return booleanValue;
	}

	@Override
	public void putBoolean(String name, boolean value) throws CorbaDeviceException {
		hashtable.putBoolean(name, value);
	}

	@Override
	public int getInt(String name) throws CorbaDeviceException {
		int intValue;

		intValue = hashtable.getInt(name);

		return intValue;
	}

	@Override
	public void putInt(String name, int value) throws CorbaDeviceException {
		hashtable.putInt(name, value);
	}

	@Override
	public long getLong(String name) throws CorbaDeviceException {
		long longValue;

		longValue = hashtable.getLong(name);

		return longValue;
	}

	@Override
	public void putLong(String name, long value) throws CorbaDeviceException {
		hashtable.putLong(name, value);
	}

	@Override
	public float getFloat(String name) throws CorbaDeviceException {
		float floatValue;

		floatValue = hashtable.getFloat(name);

		return floatValue;
	}

	@Override
	public void putFloat(String name, float value) throws CorbaDeviceException {
		hashtable.putFloat(name, value);
	}

	@Override
	public double getDouble(String name) throws CorbaDeviceException {
		double doubleValue;

		doubleValue = hashtable.getDouble(name);

		return doubleValue;
	}

	@Override
	public void putDouble(String name, double value) throws CorbaDeviceException {
		hashtable.putDouble(name, value);
	}

	@Override
	public String getString(String name) throws CorbaDeviceException {
		String stringValue;

		stringValue = hashtable.getString(name);

		return stringValue;
	}

	@Override
	public void putString(String name, String value) throws CorbaDeviceException {
		hashtable.putString(name, value);
	}

	@Override
	public Any get(String name) throws CorbaDeviceException {
		Object object;

		object = hashtable.get(name);
		org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
		any.insert_Value((Serializable) object);

		return any;
	}

	@Override
	public void put(String name, Any any) throws CorbaDeviceException {
		hashtable.put(name, any.extract_Value());
	}
}
