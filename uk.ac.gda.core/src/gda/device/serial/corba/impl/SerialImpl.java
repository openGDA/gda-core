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

package gda.device.serial.corba.impl;

import gda.device.DeviceException;
import gda.device.Serial;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceImpl;
import gda.device.serial.corba.CorbaSerialPOA;
import gda.factory.corba.CorbaFactoryException;

/**
 * A server side implementation for a distributed Serial class
 */
public class SerialImpl extends CorbaSerialPOA {
	//
	// Private reference to implementation object
	//
	private Serial serial;

	private DeviceImpl deviceImpl;

	//
	// Private reference to POA
	//
	private org.omg.PortableServer.POA poa;

	/**
	 * Create server side implementation to the CORBA package.
	 * 
	 * @param serial
	 *            the Serial implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public SerialImpl(Serial serial, org.omg.PortableServer.POA poa) {
		this.serial = serial;
		this.poa = poa;
		deviceImpl = new DeviceImpl(serial, poa);
	}

	/**
	 * Get the implementation object
	 * 
	 * @return the Serial implementation object
	 */
	public Serial _delegate() {
		return serial;
	}

	/**
	 * Set the implementation object.
	 * 
	 * @param serial
	 *            set the Serial implementation object
	 */
	public void _delegate(Serial serial) {
		this.serial = serial;
	}

	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	@Override
	public void setBaudRate(int baud) throws CorbaDeviceException {
		try {
			serial.setBaudRate(baud);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setByteSize(int byteSize) throws CorbaDeviceException {
		try {
			serial.setByteSize(byteSize);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setParity(String parity) throws CorbaDeviceException {
		try {
			serial.setParity(parity);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setStopBits(int stopBits) throws CorbaDeviceException {
		try {
			serial.setStopBits(stopBits);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setFlowControl(String flowControl) throws CorbaDeviceException {
		try {
			serial.setFlowControl(flowControl);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void close() throws CorbaDeviceException {
		try {
			serial.close();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public int getReadTimeout() throws CorbaDeviceException {
		try {
			return serial.getReadTimeout();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public char readChar() throws CorbaDeviceException {
		try {
			return serial.readChar();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void flush() throws CorbaDeviceException {
		try {
			serial.flush();
			return;
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setReadTimeout(int time) throws CorbaDeviceException {
		try {
			serial.setReadTimeout(time);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void writeChar(char c) throws CorbaDeviceException {
		try {
			serial.writeChar(c);
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
	public int getProtectionLevel() throws CorbaDeviceException {
		return deviceImpl.getProtectionLevel();
	}

	@Override
	public void setProtectionLevel(int newLevel) throws CorbaDeviceException {
		deviceImpl.setProtectionLevel(newLevel);
	}
}
