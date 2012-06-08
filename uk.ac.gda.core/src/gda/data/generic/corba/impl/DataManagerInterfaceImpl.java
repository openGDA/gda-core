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

package gda.data.generic.corba.impl;

import gda.data.DataManagerInterface;
import gda.data.generic.IGenericData;
import gda.data.generic.corba.CorbaDataException;
import gda.data.generic.corba.CorbaDataManagerInterfacePOA;

import java.util.Vector;

/**
 * A server side implementation for a distributed DataManagerInterface class
 */
public class DataManagerInterfaceImpl extends CorbaDataManagerInterfacePOA {

	private DataManagerInterface dataMan;

	private org.omg.PortableServer.POA poa;

	/**
	 * Create server side implementation to the CORBA package.
	 * 
	 * @param data
	 *            the DataManagerInterface implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public DataManagerInterfaceImpl(DataManagerInterface data, org.omg.PortableServer.POA poa) {
		this.dataMan = data;
		this.poa = poa;
	}

	/**
	 * Get the implementation object
	 * 
	 * @return the DataManagerInterface implementation object
	 */
	public DataManagerInterface _delegate() {
		return this.dataMan;
	}

	/**
	 * Set the implementation object.
	 * 
	 * @param data
	 *            set the DataManagerInterface implementation object
	 */
	public void _delegate(DataManagerInterface data) {
		this.dataMan = data;
	}

	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	@Override
	public org.omg.CORBA.Any create(String name) throws CorbaDataException {
		org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
		IGenericData temp = this.dataMan.create(name);
		any.insert_Value(temp);
		return any;
	}

	@Override
	public void add(String name, org.omg.CORBA.Any data) {
		IGenericData temp = (IGenericData) data.extract_Value();
		this.dataMan.add(name, temp);
	}

	@Override
	public void remove(String name) {
		this.dataMan.remove(name);
	}

	@Override
	public String[] list() {
		Vector<String> vec = this.dataMan.list();
		return vec.toArray(new String[vec.size()]);
	}

	@Override
	public org.omg.CORBA.Any get(String name) {
		org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
		any.insert_Value(this.dataMan.get(name));
		return any;
	}
}
