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

package gda.data.fileregistrar.corba.impl;

import gda.data.fileregistrar.IFileRegistrar;
import gda.data.fileregistrar.corba.CorbaFileRegistrar;
import gda.data.fileregistrar.corba.CorbaFileRegistrarHelper;
import gda.factory.corba.util.NetService;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;

/**
 * A client side implementation of the adapter pattern for the timer class
 */
public class FileregistrarAdapter implements IFileRegistrar {
	private CorbaFileRegistrar corbaFileRegistrar;

	private NetService netService;

	private String name;

	/**
	 * Create client side interface to the CORBA package.
	 * 
	 * @param obj
	 *            the CORBA object
	 * @param name
	 *            the name of the object
	 * @param netService
	 *            the CORBA naming service
	 */
	public FileregistrarAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		corbaFileRegistrar = CorbaFileRegistrarHelper.narrow(obj);
		this.netService = netService;
		this.name = name;
	}

	@Override
	public void registerFile(String fileName) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaFileRegistrar.registerFile(fileName);
				return;
			} catch (COMM_FAILURE cf) {
				corbaFileRegistrar = CorbaFileRegistrarHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaFileRegistrar = CorbaFileRegistrarHelper.narrow(netService.reconnect(name));
			}
		}
		//TODO error handling, logging
	}

	@Override
	public void registerFiles(String[] fileNames) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaFileRegistrar.registerFiles(fileNames);
				return;
			} catch (COMM_FAILURE cf) {
				corbaFileRegistrar = CorbaFileRegistrarHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaFileRegistrar = CorbaFileRegistrarHelper.narrow(netService.reconnect(name));
			}
		}
		//TODO error handling, logging
	}

	@Override
	public void setName(String name) {
		// see bugzilla bug #443
	}

	@Override
	public String getName() {
		return name;
	}
}
