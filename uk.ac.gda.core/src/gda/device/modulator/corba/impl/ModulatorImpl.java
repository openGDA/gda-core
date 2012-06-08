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

package gda.device.modulator.corba.impl;

import gda.device.DeviceException;
import gda.device.Modulator;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceImpl;
import gda.device.modulator.corba.CorbaModulatorPOA;
import gda.factory.corba.CorbaFactoryException;

/**
 * A server side implementation for a distributed Modulator class
 */
public class ModulatorImpl extends CorbaModulatorPOA {
	//
	// Private reference to implementation object
	//
	private Modulator modulator;

	private DeviceImpl deviceImpl;

	//
	// Private reference to POA
	//
	private org.omg.PortableServer.POA poa;

	/**
	 * Create server side implementation to the CORBA package.
	 * 
	 * @param modulator
	 *            the Modulator implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public ModulatorImpl(Modulator modulator, org.omg.PortableServer.POA poa) {
		this.modulator = modulator;
		this.poa = poa;
		deviceImpl = new DeviceImpl(modulator, poa);
	}

	/**
	 * Get the implementation object
	 * 
	 * @return the Modulator implementation object
	 */
	public Modulator _delegate() {
		return modulator;
	}

	/**
	 * Set the implementation object.
	 * 
	 * @param modulator
	 *            set the Modulator implementation object
	 */
	public void _delegate(Modulator modulator) {
		this.modulator = modulator;
	}

	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	@Override
	public String getWaveLength() throws CorbaDeviceException {
		try {
			return modulator.getWaveLength().toString();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setWaveLength(double waveLength) throws CorbaDeviceException {
		try {
			modulator.setWaveLength(waveLength);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public int getRetardation() throws CorbaDeviceException {
		try {
			return modulator.getRetardation();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setRetardation(double retardation) throws CorbaDeviceException {
		try {
			modulator.setRetardation(retardation);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void reset() throws CorbaDeviceException {
		try {
			modulator.reset();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setEcho(boolean echo) throws CorbaDeviceException {
		try {
			modulator.setEcho(echo);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public String readFrequency(int noOfTimes) throws CorbaDeviceException {
		try {
			return modulator.readFrequency(noOfTimes).toString();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setInhibit(boolean inhibit) throws CorbaDeviceException {
		try {
			modulator.setInhibit(inhibit);
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
