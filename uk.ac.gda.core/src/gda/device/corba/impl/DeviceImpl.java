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

package gda.device.corba.impl;

import gda.device.Device;
import gda.device.DeviceException;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.CorbaDevicePOA;
import gda.factory.FactoryException;
import gda.factory.corba.CorbaFactoryException;
import gda.factory.corba.util.EventDispatcher;
import gda.factory.corba.util.EventService;
import gda.observable.IObserver;

import java.io.Serializable;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;

/**
 * A server side implementation for a distributed Device class
 */
public class DeviceImpl extends CorbaDevicePOA implements IObserver {

	//
	// Private reference to implementation object
	//
	private Device device;

	//
	// Private reference to POA
	//
	private POA poa;

	private EventDispatcher dispatcher;

	private String name;

	/**
	 * Create server side implementation to the CORBA package.
	 *
	 * @param device
	 *            the Device implementation object
	 * @param poa
	 *            the portable object adapter
	 */
	public DeviceImpl(Device device, POA poa) {
		this.device = device;
		this.poa = poa;
		name = device.getName();
		dispatcher = EventService.getInstance().getEventDispatcher();
		device.addIObserver(this); //FIXME: potential race condition
	}

	/**
	 * Get the implementation object
	 *
	 * @return the device implementation object
	 */
	public Device _delegate() {
		return device;
	}

	/**
	 * Set the implementation object.
	 *
	 * @param device
	 *            set the Device implementation object
	 */
	public void _delegate(Device device) {
		this.device = device;
	}

	@Override
	public POA _default_POA() {
		return (poa != null) ? poa : super._default_POA();
	}

	@Override
	public void update(Object source, Object arg) {
		dispatcher.publish(name, arg);
	}

	@Override
	public void setAttribute(String attributeName, Any any) throws CorbaDeviceException {
		try {
			device.setAttribute(attributeName, any.extract_Value());
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils.getFullStackMsg(ex));
		}
	}

	@Override
	public Any getAttribute(String attributeName) throws CorbaDeviceException {
		Any any = ORB.init().create_any();
		try {
			Object obj = device.getAttribute(attributeName);
			any.insert_Value((Serializable) obj);
		} catch (Exception ex) {
			throw new CorbaDeviceException(gda.util.exceptionUtils.getFullStackMsg(ex));
		}
		return any;
	}

	@Override
	public void reconfigure() throws CorbaFactoryException {
		try {
			device.reconfigure();
		} catch (FactoryException ex) {
			throw new CorbaFactoryException(ex.getMessage());
		}
	}

	@Override
	public void close() throws CorbaDeviceException {
		try {
			device.close();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public int getProtectionLevel() throws CorbaDeviceException {
		try {
			return device.getProtectionLevel();
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}

	@Override
	public void setProtectionLevel(int newLevel) throws CorbaDeviceException {
		try {
			device.setProtectionLevel(newLevel);
		} catch (DeviceException ex) {
			throw new CorbaDeviceException(ex.getMessage());
		}
	}
}
