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

package gda.device.amplifier.corba.impl;

import gda.device.Amplifier;
import gda.device.DeviceException;
import gda.device.amplifier.corba.CorbaAmplifierPOA;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceImpl;
import gda.factory.corba.CorbaFactoryException;

import org.omg.CORBA.Any;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A server side implementation for the distributed Amplifier class
 */
public class AmplifierImpl extends CorbaAmplifierPOA {
	private static final Logger logger = LoggerFactory.getLogger(AmplifierImpl.class);

	//
	// Private reference to implementation object
	//
	private Amplifier amplifier;

	private DeviceImpl deviceImpl;

	//
	// Private reference to POA
	//
	private org.omg.PortableServer.POA poa;

	/**
	 * Create server side implementation to the CORBA package.
	 * 
	 * @param amplifier
	 *            the Amplifier implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public AmplifierImpl(Amplifier amplifier, org.omg.PortableServer.POA poa) {
		this.amplifier = amplifier;
		this.poa = poa;
		logger.debug("Amplifier Impl created");
		deviceImpl = new DeviceImpl(amplifier, poa);

	}

	/**
	 * Get the implementation object
	 * 
	 * @return the Amplifier implementation object
	 */
	public Amplifier _delegate() {
		return amplifier;
	}

	/**
	 * Set the implementation object.
	 * 
	 * @param amplifier
	 *            set the Amplifier implementation object
	 */
	public void _delegate(Amplifier amplifier) {
		this.amplifier = amplifier;
	}

	/**
	 * _default_POA method
	 * 
	 * @return the POA
	 */
	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	@Override
	public void autoCurrentSuppress() throws CorbaDeviceException {
		try {
			amplifier.autoCurrentSuppress();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}

	}

	@Override
	public void autoZeroCorrect() throws CorbaDeviceException {
		try {
			amplifier.autoZeroCorrect();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}

	}

	@Override
	public double getCurrentSuppressValue() throws CorbaDeviceException {
		try {
			return amplifier.getCurrentSuppressValue();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public double getFilterRiseTime() throws CorbaDeviceException {
		try {
			return amplifier.getFilterRiseTime();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public double getGain() throws CorbaDeviceException {
		try {
			return amplifier.getGain();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public String getStatus() throws CorbaDeviceException {
		try {
			return amplifier.getStatus();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public double getVoltageBias() throws CorbaDeviceException {
		try {
			return amplifier.getVoltageBias();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setAutoFilter(boolean onOff) throws CorbaDeviceException {
		try {
			amplifier.setAutoFilter(onOff);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}

	}

	@Override
	public void setCurrentSuppress(boolean onOff) throws CorbaDeviceException {
		try {
			amplifier.setCurrentSuppress(onOff);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}

	}

	@Override
	public void setCurrentSuppressionParams(double value) throws CorbaDeviceException {
		try {
			amplifier.setCurrentSuppressionParams(value);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}

	}

	@Override
	public void setCurrentSuppressionParams2(double value, int range) throws CorbaDeviceException {
		try {
			amplifier.setCurrentSuppressionParams(value, range);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}

	}

	@Override
	public void setEnlargeGain(boolean onOff) throws CorbaDeviceException {
		try {
			amplifier.setEnlargeGain(onOff);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}

	}

	@Override
	public void setFilter(boolean onOff) throws CorbaDeviceException {
		try {
			amplifier.setFilter(onOff);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}

	}

	@Override
	public void setFilterRiseTime(int level) throws CorbaDeviceException {
		try {
			amplifier.setFilterRiseTime(level);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}

	}

	@Override
	public void setGain(int level) throws CorbaDeviceException {
		try {
			amplifier.setGain(level);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}

	}

	@Override
	public void setVoltageBias(boolean voltageBias) throws CorbaDeviceException {
		try {
			amplifier.setVoltageBias(voltageBias);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}

	}

	@Override
	public void setVoltageBias2(double value) throws CorbaDeviceException {
		try {
			amplifier.setVoltageBias(value);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}

	}

	@Override
	public void setZeroCheck(boolean onOff) throws CorbaDeviceException {
		try {
			amplifier.setZeroCheck(onOff);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
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
