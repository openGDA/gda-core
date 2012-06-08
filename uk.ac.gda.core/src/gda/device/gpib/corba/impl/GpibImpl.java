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

package gda.device.gpib.corba.impl;

import gda.device.DeviceException;
import gda.device.Gpib;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceImpl;
import gda.device.gpib.corba.CorbaGpibPOA;
import gda.factory.corba.CorbaFactoryException;

/**
 * A server side implementation for a distributed gpib class
 */
public class GpibImpl extends CorbaGpibPOA {
	//
	// Private reference to implementation object
	//
	private Gpib gpib;

	private DeviceImpl deviceImpl;

	//
	// Private reference to POA
	//
	private org.omg.PortableServer.POA poa;

	/**
	 * Create server side implementation to the CORBA package.
	 * 
	 * @param gpib
	 *            the Gpib implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public GpibImpl(Gpib gpib, org.omg.PortableServer.POA poa) {
		this.gpib = gpib;
		this.poa = poa;
		deviceImpl = new DeviceImpl(gpib, poa);
	}

	/**
	 * Get the implementation object
	 * 
	 * @return the Gpib implementation object
	 */
	public Gpib _delegate() {
		return gpib;
	}

	/**
	 * Set the implementation object.
	 * 
	 * @param gpib
	 *            set the Gpib implementation object
	 */
	public void _delegate(Gpib gpib) {
		this.gpib = gpib;
	}

	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	@Override
	public int findDevice(String deviceName) throws CorbaDeviceException {
		try {
			return gpib.findDevice(deviceName);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public int getSerialPollByte(String deviceName) throws CorbaDeviceException {
		try {
			return gpib.getSerialPollByte(deviceName);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void sendDeviceClear(String deviceName) throws CorbaDeviceException {
		try {
			gpib.sendDeviceClear(deviceName);
			return;
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void sendInterfaceClear(String interFaceName) throws CorbaDeviceException {
		try {
			gpib.sendInterfaceClear(interFaceName);
			return;
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setTimeOut(String deviceName, int timeout) throws CorbaDeviceException {
		try {
			gpib.setTimeOut(deviceName, timeout);
			return;
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public int getTimeOut(String deviceName) throws CorbaDeviceException {
		try {
			return gpib.getTimeOut(deviceName);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setTerminator(String deviceName, char term) throws CorbaDeviceException {
		try {
			gpib.setTerminator(deviceName, term);
			return;
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public char getTerminator(String deviceName) throws CorbaDeviceException {
		try {
			return gpib.getTerminator(deviceName);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setReadTermination(String deviceName, boolean terminate) throws CorbaDeviceException {
		try {
			gpib.setReadTermination(deviceName, terminate);
			return;
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setWriteTermination(String deviceName, boolean terminate) throws CorbaDeviceException {
		try {
			gpib.setWriteTermination(deviceName, terminate);
			return;
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public boolean getReadTermination(String deviceName) throws CorbaDeviceException {
		try {
			return gpib.getReadTermination(deviceName);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public boolean getWriteTermination(String deviceName) throws CorbaDeviceException {
		try {
			return gpib.getWriteTermination(deviceName);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public String read(String deviceName) throws CorbaDeviceException {
		try {
			return gpib.read(deviceName);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public String read2(String deviceName, int strLength) throws CorbaDeviceException {
		try {
			return gpib.read(deviceName, strLength);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void write(String deviceName, String buffer) throws CorbaDeviceException {
		try {
			gpib.write(deviceName, buffer);
			return;
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

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
