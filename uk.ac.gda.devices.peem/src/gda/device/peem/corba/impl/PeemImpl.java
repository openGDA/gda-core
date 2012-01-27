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

package gda.device.peem.corba.impl;

import gda.device.DeviceException;
import gda.device.PEEM;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceImpl;
import gda.device.peem.corba.CorbaPEEMPOA;
import gda.factory.corba.CorbaFactoryException;

import org.omg.CORBA.Any;

/**
 * A server side implementation for a distributed PEEM class
 */
public class PeemImpl extends CorbaPEEMPOA {
	// Private reference to implementation object
	private PEEM peem;

	private DeviceImpl deviceImpl;

	// Private reference to POA
	private org.omg.PortableServer.POA poa;

	/**
	 * Create server side implementation to the CORBA package.
	 * 
	 * @param peem
	 *            the PEEM implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public PeemImpl(PEEM peem, org.omg.PortableServer.POA poa) {
		this.peem = peem;
		this.poa = poa;
		deviceImpl = new DeviceImpl(peem, poa);
	}

	/**
	 * Get the implementation object
	 * 
	 * @return the PEEM implementation object
	 */
	public PEEM _delegate() {
		return peem;
	}

	/**
	 * Set the implementation object.
	 * 
	 * @param peem
	 *            set the PEEM implementation object
	 */
	public void _delegate(PEEM peem) {
		this.peem = peem;
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

	/*
	 * Common block public double DoIt() throws CorbaDeviceException { try{ return leem.doit(); } catch (DeviceException
	 * e){ throw new CorbaDeviceException(e.getMessage()); } }
	 */
	@Override
	public boolean connect() throws CorbaDeviceException {
		try {
			return peem.connect();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public boolean disconnect() throws CorbaDeviceException {
		try {
			return peem.disconnect();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public String modules() throws CorbaDeviceException {
		try {
			return peem.modules();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public int getModuleNumber() throws CorbaDeviceException {
		try {
			return peem.getModuleNumber();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public int getModuleIndex(String moduleName) throws CorbaDeviceException {
		try {
			return peem.getModuleIndex(moduleName);
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public String getPSName(int index) throws CorbaDeviceException {
		try {
			return peem.getPSName(index);
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public float getPSValue(int index) throws CorbaDeviceException {
		try {
			return (float) peem.getPSValue(index);
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public boolean setPSValue(int index, float value) throws CorbaDeviceException {
		try {
			return peem.setPSValue(index, value);
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public String getPreset() throws CorbaDeviceException {
		try {
			return peem.getPreset();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public int setPhi(float angle) throws CorbaDeviceException {
		try {
			return peem.setPhi(angle);
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public void getMicrometerValue(org.omg.CORBA.FloatHolder xcoord, org.omg.CORBA.FloatHolder ycoord)
			throws CorbaDeviceException {
		try {
			double[] coord = peem.getMicrometerValue();
			xcoord.value = (float) coord[0];
			ycoord.value = (float) coord[1];
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public float getVacuumGaugeValue() throws CorbaDeviceException {
		try {
			return (float) peem.getVacuumGaugeValue();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public String getVacuumGaugeLabel() throws CorbaDeviceException {
		try {
			return peem.getVacuumGaugeLabel();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
	}

	@Override
	public boolean isInitDone() throws CorbaDeviceException {
		try {
			return peem.isInitDone();
		} catch (DeviceException e) {
			throw new CorbaDeviceException(e.getMessage());
		}
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
