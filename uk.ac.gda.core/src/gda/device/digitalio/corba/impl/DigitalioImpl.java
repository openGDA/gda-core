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

package gda.device.digitalio.corba.impl;

import gda.device.DeviceException;
import gda.device.DigitalIO;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceImpl;
import gda.device.digitalio.corba.CorbaDigitalIOPOA;
import gda.factory.corba.CorbaFactoryException;

/**
 * A server side implementation for a distributed DigitalIO class
 */
public class DigitalioImpl extends CorbaDigitalIOPOA {
	//
	// Private reference to implementation object
	//
	private DigitalIO digitalio;

	private DeviceImpl deviceImpl;

	//
	// Private reference to POA
	//
	private org.omg.PortableServer.POA poa;

	/**
	 * Create server side implementation to the CORBA package.
	 * 
	 * @param digitalio
	 *            the DigitalIO implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public DigitalioImpl(DigitalIO digitalio, org.omg.PortableServer.POA poa) {
		this.digitalio = digitalio;
		this.poa = poa;
		deviceImpl = new DeviceImpl(digitalio, poa);
	}

	/**
	 * Get the implementation object
	 * 
	 * @return the DigitalIO implementation object
	 */
	public DigitalIO _delegate() {
		return digitalio;
	}

	/**
	 * Set the implementation object.
	 * 
	 * @param digitalio
	 *            set the DigitalIO implementation object
	 */
	public void _delegate(DigitalIO digitalio) {
		this.digitalio = digitalio;
	}

	@Override
	public org.omg.PortableServer.POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	@Override
	public int getState(String channelName) throws CorbaDeviceException {
		try {
			return digitalio.getState(channelName);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setState(String channelName, int state) throws CorbaDeviceException {
		try {
			digitalio.setState(channelName, state);
			return;
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setNegativeEdgeSync(String channelName) throws CorbaDeviceException {
		try {
			digitalio.setNegativeEdgeSync(channelName);
			return;
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setPositiveEdgeSync(String channelName) throws CorbaDeviceException {
		try {
			digitalio.setPositiveEdgeSync(channelName);
			return;
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setNegative2LineSync(String inputChannelName, String outputChannelName) throws CorbaDeviceException {
		try {
			digitalio.setNegative2LineSync(inputChannelName, outputChannelName);
			return;
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setPositive2LineSync(String inputChannelName, String outputChannelName) throws CorbaDeviceException {
		try {
			digitalio.setPositive2LineSync(inputChannelName, outputChannelName);
			return;
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public int getTwoLineSyncTimeout() throws CorbaDeviceException {
		try {
			return digitalio.getTwoLineSyncTimeout();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setTwoLineSyncTimeout(int msecs) throws CorbaDeviceException {
		try {
			digitalio.setTwoLineSyncTimeout(msecs);
			return;
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public int getEdgeSyncDelayTime() throws CorbaDeviceException {
		try {
			return digitalio.getEdgeSyncDelayTime();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setEdgeSyncDelayTime(int msecs) throws CorbaDeviceException {
		try {
			digitalio.setEdgeSyncDelayTime(msecs);
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
