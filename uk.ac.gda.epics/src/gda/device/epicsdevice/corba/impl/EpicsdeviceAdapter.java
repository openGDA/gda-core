/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.device.epicsdevice.corba.impl;

import gda.device.Device;
import gda.device.DeviceException;
import gda.device.corba.impl.DeviceAdapter;
import gda.device.epicsdevice.EpicsDeviceCorbaAdapter;
import gda.device.epicsdevice.IEpicsChannel;
import gda.device.epicsdevice.IEpicsDevice;
import gda.device.epicsdevice.ReturnType;
import gda.factory.Findable;
import gda.factory.corba.util.NetService;

/**
 * A client side implementation of the adapter pattern for the ControlPoint class
 */
public class EpicsdeviceAdapter extends DeviceAdapter implements Findable, Device, IEpicsDevice {
	private EpicsDeviceCorbaAdapter epicsDeviceCorbaAdapter;

	// private NetService netService;
	// private String name;

	/**
	 * Create client side interface to the CORBA package.
	 * 
	 * @param obj
	 *            the CORBA object
	 * @param name
	 *            the name of the object
	 * @param netService
	 *            the CORBA naming service
	 */
	public EpicsdeviceAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		super(obj, name, netService);
		// this.netService = netService;
		// this.name = name;
		epicsDeviceCorbaAdapter = new EpicsDeviceCorbaAdapter(this, this);
	}

	/**
	 * @param returnType
	 * @param record
	 * @param field
	 * @return Object - value
	 * @throws DeviceException
	 */
	public Object getValue(ReturnType returnType, String record, String field) throws DeviceException {
		return epicsDeviceCorbaAdapter.getValue(returnType, record, field);
	}

	/**
	 * @param type
	 * @param record
	 * @param field
	 * @param putTimeout
	 * @param value
	 * @throws DeviceException
	 */
	public void setValue(Object type, String record, String field, double putTimeout, Object value)
			throws DeviceException {
		epicsDeviceCorbaAdapter.setValue(type, record, field, putTimeout, value);
	}

	@Override
	public void notifyIObservers(Object theObserved, Object theArgument) {
		/*
		 * if theArgument is of type EpicsDeviceEvent then look for it in the registration list. If an entry is found
		 * that notify it
		 */
		epicsDeviceCorbaAdapter.notifyOfEpicsDeviceEvent(theObserved, theArgument);

	}

	/**
	 * @param theObserved
	 * @param theArgument
	 */
	public void notifyOfOtherEvents(Object theObserved, Object theArgument) {
		/*
		 * if theArgument is of type EpicsDeviceEvent then look for it in the registration list. If an entry is found
		 * that notify it
		 */
		super.notifyIObservers(theObserved, theArgument);

	}

	@Override
	public IEpicsChannel createEpicsChannel(ReturnType returnType, String record, String field) {
		return epicsDeviceCorbaAdapter.createEpicsChannel(returnType, record, field);
	}

	@Override
	public IEpicsChannel createEpicsChannel(ReturnType returnType, String record, String field, double putTimeout) {
		return epicsDeviceCorbaAdapter.createEpicsChannel(returnType, record, field, putTimeout);
	}

	@Override
	public void dispose() {
		epicsDeviceCorbaAdapter.dispose();

	}

	@Override
	public void closeUnUsedChannels() throws DeviceException {
		epicsDeviceCorbaAdapter.closeUnUsedChannels();

	}

	// make all access to underlying device that may try to create and
	// EpicsChannel synchronized as I think Corba access to a device is
	// synchronized and I do not want to block up other Corba connections
	// whilst the device is trying to create the channel
	// another solution is to increase jacorb.poa.thread_pool_max
	@Override
	public synchronized Object getAttribute(String attributeName) throws DeviceException {
		return super.getAttribute(attributeName);
	}

	@Override
	public synchronized void setAttribute(String attributeName, Object value) throws DeviceException {
		super.setAttribute(attributeName, value);
	}
}