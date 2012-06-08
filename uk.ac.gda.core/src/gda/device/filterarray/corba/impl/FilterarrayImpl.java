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

package gda.device.filterarray.corba.impl;

import gda.device.DeviceException;
import gda.device.FilterArray;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceImpl;
import gda.device.filterarray.corba.CorbaFilterArrayPOA;
import gda.factory.corba.CorbaFactoryException;

import org.omg.CORBA.Any;

/**
 * A server side implementation for a distributed FilterArray class
 */
public class FilterarrayImpl extends CorbaFilterArrayPOA {
	//
	// Private reference to implementation object
	//
	private FilterArray filterarray;

	private DeviceImpl deviceImpl;

	//
	// Private reference to POA
	//
	private org.omg.PortableServer.POA poa;

	/**
	 * Create server side implementation to the CORBA package.
	 * 
	 * @param filterarray
	 *            the FilterArray implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public FilterarrayImpl(FilterArray filterarray, org.omg.PortableServer.POA poa) {
		this.filterarray = filterarray;
		this.poa = poa;
		deviceImpl = new DeviceImpl(filterarray, poa);
	}

	/**
	 * Get the implementation object
	 * 
	 * @return the FilterArray implementation object
	 */
	public FilterArray _delegate() {
		return filterarray;
	}

	/**
	 * Set the implementation object.
	 * 
	 * @param filterarray
	 *            set the FilterArray implementation object
	 */
	public void _delegate(FilterArray filterarray) {
		this.filterarray = filterarray;
	}

	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	@Override
	public double getAbsorption() throws CorbaDeviceException {
		try {
			return filterarray.getAbsorption();
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void setAbsorption(double absorption) throws CorbaDeviceException {
		try {
			filterarray.setAbsorption(absorption);
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public double getTransmission() throws CorbaDeviceException {
		try {
			return filterarray.getTransmission();
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void setTransmission(double transmission) throws CorbaDeviceException {
		try {
			filterarray.setTransmission(transmission);
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public double getCalculationEnergy() throws CorbaDeviceException {
		try {
			return filterarray.getCalculationEnergy();
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void setCalculationEnergy(double energy) throws CorbaDeviceException {
		try {
			filterarray.setCalculationEnergy(energy);
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public double getCalculationWavelength() throws CorbaDeviceException {
		try {
			return filterarray.getCalculationWavelength();
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void setCalculationWavelength(double wavelength) throws CorbaDeviceException {
		try {
			filterarray.setCalculationWavelength(wavelength);
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public boolean isUsingMonoEnergy() throws CorbaDeviceException {
		try {
			return filterarray.isUsingMonoEnergy();
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
	}

	@Override
	public void setUseMonoEnergy(boolean useEnergy) throws CorbaDeviceException {
		try {
			filterarray.setUseMonoEnergy(useEnergy);
		} catch (DeviceException de) {
			throw new CorbaDeviceException(de.getMessage());
		}
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
