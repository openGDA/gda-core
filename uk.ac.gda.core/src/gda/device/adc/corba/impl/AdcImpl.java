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

package gda.device.adc.corba.impl;

import gda.device.Adc;
import gda.device.DeviceException;
import gda.device.adc.corba.CorbaAdcPOA;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceImpl;
import gda.factory.corba.CorbaFactoryException;

/**
 * A server side implementation for a distributed Adc class
 */
public class AdcImpl extends CorbaAdcPOA {
	//
	// Private reference to implementation object
	//
	private Adc adc;

	private DeviceImpl deviceImpl;

	//
	// Private reference to POA
	//
	private org.omg.PortableServer.POA poa;

	/**
	 * Create server side implementation to the CORBA package.
	 * 
	 * @param adc
	 *            the dataLogger implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public AdcImpl(Adc adc, org.omg.PortableServer.POA poa) {
		this.adc = adc;
		this.poa = poa;
		deviceImpl = new DeviceImpl(adc, poa);
	}

	/**
	 * Get the implementation object
	 * 
	 * @return the Adc implementation object
	 */
	public Adc _delegate() {
		return adc;
	}

	/**
	 * Set the implementation object.
	 * 
	 * @param adc
	 *            set the Adc implementation object
	 */
	public void _delegate(Adc adc) {
		this.adc = adc;
	}

	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	@Override
	public double getVoltage(int channel) throws CorbaDeviceException {
		try {
			return adc.getVoltage(channel);
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public double[] getVoltages() throws CorbaDeviceException {
		try {
			return adc.getVoltages();
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void setRange(int channel, int range) throws CorbaDeviceException {
		try {
			adc.setRange(channel, range);
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public int getRange(int channel) throws CorbaDeviceException {
		try {
			return adc.getRange(channel);
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void setUniPolar(int channel, boolean polarity) throws CorbaDeviceException {
		try {
			adc.setUniPolar(channel, polarity);
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public int[] getRanges() throws CorbaDeviceException {
		try {
			return adc.getRanges();
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public boolean isUniPolarSettable() throws CorbaDeviceException {
		try {
			return adc.isUniPolarSettable();
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void setSampleCount(int count) throws CorbaDeviceException {
		try {
			adc.setSampleCount(count);
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
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
