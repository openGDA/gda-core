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

package gda.device.shear.corba.impl;

import gda.device.DeviceException;
import gda.device.Shear;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceImpl;
import gda.device.shear.corba.CorbaShearPOA;
import gda.factory.corba.CorbaFactoryException;

/**
 * A server side implementation for a distributed Shear class
 */
public class ShearImpl extends CorbaShearPOA {
	//
	// Private reference to implementation object
	//
	private Shear shear;

	private DeviceImpl deviceImpl;

	//
	// Private reference to POA
	//
	private org.omg.PortableServer.POA poa;

	/**
	 * Create server side implementation to the CORBA package.
	 * 
	 * @param shear
	 *            the Shear implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public ShearImpl(Shear shear, org.omg.PortableServer.POA poa) {
		this.shear = shear;
		this.poa = poa;
		deviceImpl = new DeviceImpl(shear, poa);
	}

	/**
	 * Get the implementation object
	 * 
	 * @return the Shear implementation object
	 */
	public Shear _delegate() {
		return shear;
	}

	/**
	 * Set the implementation object.
	 * 
	 * @param shear
	 *            set the Shear implementation object
	 */
	public void _delegate(Shear shear) {
		this.shear = shear;
	}

	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	@Override
	public double getThickness() throws CorbaDeviceException {
		try {
			return shear.getThickness();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public double getRadius() throws CorbaDeviceException {
		try {
			return shear.getRadius();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public double getShearRate() throws CorbaDeviceException {
		try {
			return shear.getShearRate();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public double getAmplitude() throws CorbaDeviceException {
		try {
			return shear.getAmplitude();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public double getTorque() throws CorbaDeviceException {
		try {
			return shear.getTorque();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void continuousShear(double gamma) throws CorbaDeviceException {
		try {
			shear.continuousShear(gamma);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void oscillatoryShear(double gamma, double amplitude) throws CorbaDeviceException {
		try {
			shear.oscillatoryShear(gamma, amplitude);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void stopShear() throws CorbaDeviceException {
		try {
			shear.stopShear();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setTorque(double current) throws CorbaDeviceException {
		try {
			shear.setTorque(current);
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
