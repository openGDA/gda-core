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

package gda.device.bpm.corba.impl;

import gda.device.BPM;
import gda.device.DeviceException;
import gda.device.bpm.corba.CorbaBPMPOA;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceImpl;
import gda.factory.corba.CorbaFactoryException;

/**
 * A server side implementation for a distributed Bpm class
 */
public class BpmImpl extends CorbaBPMPOA {
	private BPM bpm;

	private DeviceImpl deviceImpl;

	private org.omg.PortableServer.POA poa;

	/**
	 * Create server side implementation to the CORBA package.
	 * 
	 * @param bpm
	 *            the Bpm implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public BpmImpl(BPM bpm, org.omg.PortableServer.POA poa) {
		this.bpm = bpm;
		this.poa = poa;
		deviceImpl = new DeviceImpl(bpm, poa);
	}

	/**
	 * Get the implementation object
	 * 
	 * @return the Bpm implementation object
	 */
	public BPM _delegate() {
		return bpm;
	}

	/**
	 * Set the implementation object.
	 * 
	 * @param bpm
	 *            set the Bpm implementation object
	 */
	public void _delegate(BPM bpm) {
		this.bpm = bpm;
	}

	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	//
	// gda.device.bpm Class Methods.
	//

	@Override
	public double getX() throws CorbaDeviceException {
		try {
			return bpm.getX();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public double getY() throws CorbaDeviceException {
		try {
			return bpm.getY();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	//
	// gda.device.Device Class Methods.
	//

	@Override
	public void setAttribute(String attributeName, org.omg.CORBA.Any value) throws CorbaDeviceException {
		deviceImpl.setAttribute(attributeName, value);
	}

	@Override
	public org.omg.CORBA.Any getAttribute(String attributeName) throws CorbaDeviceException {
		return deviceImpl.getAttribute(attributeName);
	}

	@Override
	public void reconfigure() throws CorbaFactoryException {
		deviceImpl.reconfigure();
	}

	@Override
	public void close() throws CorbaDeviceException {
		deviceImpl.close();
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
