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

package gda.device.epicsdevice.corba.impl;

import gda.device.epicsdevice.*;
import org.omg.CORBA.Any;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.CorbaDevicePOA;

import gda.device.corba.impl.DeviceImpl;
import gda.factory.corba.CorbaFactoryException;

/**
 * A server side implementation for a distributed EpicsDevice class
 */
public class EpicsdeviceImpl extends CorbaDevicePOA {
	//
	// Private reference to implementation object
	//
	private DeviceImpl deviceImpl;

	//
	// Private reference to POA
	//
	private org.omg.PortableServer.POA poa;

	/**
	 * Create server side implementation to the CORBA package.
	 * 
	 * @param epicsdevice
	 *            the Controlpoint implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public EpicsdeviceImpl(FindableEpicsDevice epicsdevice, org.omg.PortableServer.POA poa) {
		this.poa = poa;
		deviceImpl = new DeviceImpl(epicsdevice, poa);
	}

	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	@Override
	public void setAttribute(String attributeName, Any value) throws CorbaDeviceException {
		deviceImpl.setAttribute(attributeName, value);
	}

	@Override
	public Any getAttribute(String attributeName) throws CorbaDeviceException {
		return deviceImpl.getAttribute(attributeName);
	}

	@Override
	public void close() throws CorbaDeviceException {
		deviceImpl.close();
	}

	@Override
	public void reconfigure() throws CorbaFactoryException {
		deviceImpl.reconfigure();
	}
	
	@Override
	public int getProtectionLevel() throws CorbaDeviceException {
		return deviceImpl.getProtectionLevel();
	}

	@Override
	public void setProtectionLevel(int newLevel) throws CorbaDeviceException {
		deviceImpl.setProtectionLevel(newLevel);
	}

}
