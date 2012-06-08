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
import gda.data.fileregistrar.corba.CorbaFileRegistrarPOA;

/**
 * A server side implementation for a distributed Timer class
 */
public class FileregistrarImpl extends CorbaFileRegistrarPOA {
	// Private reference to implementation object
	private IFileRegistrar fileRegistrar;

	// Private reference to POA
	private org.omg.PortableServer.POA poa;

	/**
	 * Create server side implementation to the CORBA package.
	 * 
	 * @param fileRegistrar
	 *            the Timer implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public FileregistrarImpl(IFileRegistrar fileRegistrar, org.omg.PortableServer.POA poa) {
		this.fileRegistrar = fileRegistrar;
		this.poa = poa;
	}

	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	@Override
	public void registerFile(String arg0) {
		fileRegistrar.registerFile(arg0);
	}

	@Override
	public void registerFiles(String[] arg0) {
		fileRegistrar.registerFiles(arg0);
	}
}