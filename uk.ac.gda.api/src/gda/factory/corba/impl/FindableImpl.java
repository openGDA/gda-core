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

package gda.factory.corba.impl;

import gda.factory.Findable;
import gda.factory.corba.CorbaFindablePOA;

/**
 * A server side implementation for a distributed Findable class
 */
public class FindableImpl extends CorbaFindablePOA {
	//
	// Private reference to implementation object
	//
	private Findable findable;

	//
	// Private reference to POA
	//
	private org.omg.PortableServer.POA poa;

	/**
	 * Create server side implementation to the CORBA package.
	 * 
	 * @param findable
	 *            the Findable implementation object
	 */
	public FindableImpl(Findable findable) {
		this.findable = findable;
	}

	/**
	 * Create server side implementation to the CORBA package.
	 * 
	 * @param findable
	 *            the Findable implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public FindableImpl(Findable findable, org.omg.PortableServer.POA poa) {
		this.findable = findable;
		this.poa = poa;
	}

	/**
	 * Get the implementation object
	 * 
	 * @return the Findable implementation object
	 */
	public Findable _delegate() {
		return findable;
	}

	/**
	 * Set the implementation object.
	 * 
	 * @param findable
	 *            set the Findable implementation object
	 */
	public void _delegate(Findable findable) {
		this.findable = findable;
	}

	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}
}
