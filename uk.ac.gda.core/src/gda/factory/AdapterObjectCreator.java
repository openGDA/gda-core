/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.factory;

import gda.factory.corba.util.AdapterFactory;
import gda.factory.corba.util.NetService;

/**
 * An object creator that uses an {@link AdapterFactory} to create CORBA
 * adapters.
 * 
 * @deprecated Using Spring is now the preferred method for instantiating
 * objects. Use {@code <corba:import namespace="..." />} to import remote
 * objects into a Spring application context; this allows the remote objects
 * to be referenced using the {@code ref="..."} attribute.
 */
@Deprecated
public class AdapterObjectCreator implements IObjectCreator {
	
	private String name;

	/**
	 * Sets the name to use when creating the adapter factory.
	 * 
	 * @param name the name
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public AdapterFactory getFactory() throws FactoryException {
		NetService netService = NetService.getInstance();
		AdapterFactory adapterFactory = new AdapterFactory(name, netService);
		return adapterFactory;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[name=" + name + "]";
	}

	@Override
	public boolean isLocal() {
		// This object creator creates an AdapterFactory, containing adapters
		// to remote CORBA objects
		return false;
	}
}