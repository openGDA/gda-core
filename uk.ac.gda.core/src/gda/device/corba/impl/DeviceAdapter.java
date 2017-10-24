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
import gda.device.corba.CorbaDevice;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.CorbaDeviceHelper;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.corba.CorbaFactoryException;
import gda.factory.corba.util.EventService;
import gda.factory.corba.util.EventSubscriber;
import gda.factory.corba.util.NameFilter;
import gda.factory.corba.util.NetService;
import gda.factory.corba.util.RbacEnabledAdapter;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gda.util.LoggingConstants;

import java.io.Serializable;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;
import org.python.core.PyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A client side implementation of the adapter pattern for the Device class
 */
public class DeviceAdapter extends PyObject implements Device, EventSubscriber, Findable, RbacEnabledAdapter {
	private static final Logger logger = LoggerFactory.getLogger(DeviceAdapter.class);

	protected CorbaDevice corbaDevice;

	protected NetService netService;

	protected String name;

	protected ObservableComponent observableComponent = new ObservableComponent();

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
	public DeviceAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		corbaDevice = CorbaDeviceHelper.narrow(obj);
		this.netService = netService;
		this.name = name;

		EventService eventService = EventService.getInstance();
		if (eventService != null) {
			eventService.subscribe(this, new NameFilter(name, this.observableComponent));
		}
	}

	@Override
	public org.omg.CORBA.Object getCorbaObject() {
		return corbaDevice;
	}

	@Override
	public NetService getNetService() {
		return netService;
	}

	@Override
	public void inform(Object message) {
		logger.debug(LoggingConstants.FINEST, "DeviceAdapter: Received event for " + ((message != null) ? message.getClass() : "NULL"));

		notifyIObservers(this, message);

		logger.debug(LoggingConstants.FINEST, "DeviceAdapter: Notified observers");
	}

	@Override
	public void setAttribute(String attributeName, java.lang.Object value) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
				any.insert_Value((Serializable) value);
				corbaDevice.setAttribute(attributeName, any);
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaDevice = CorbaDeviceHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaDevice = CorbaDeviceHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public java.lang.Object getAttribute(String attributeName) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				org.omg.CORBA.Any any = corbaDevice.getAttribute(attributeName);
				return any.extract_Value();
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaDevice = CorbaDeviceHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaDevice = CorbaDeviceHelper.narrow(netService.reconnect(name));
			} catch (Exception ex) {
				throw new DeviceException("Could not get attribute '{}'", attributeName, ex);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void reconfigure() throws FactoryException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaDevice.reconfigure();
				return;
			} catch (CorbaFactoryException ex) {
				throw new FactoryException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaDevice = CorbaDeviceHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaDevice = CorbaDeviceHelper.narrow(netService.reconnect(name));
			}
		}
		throw new FactoryException("Communication failure: retry failed");
	}

	@Override
	public void close() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaDevice.close();
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaDevice = CorbaDeviceHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaDevice = CorbaDeviceHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setName(String name) {
		// see bugzilla bug #443
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void addIObserver(IObserver observer) {
		observableComponent.addIObserver(observer);
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		observableComponent.deleteIObserver(observer);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}

	/**
	 * Notify observers of this class.
	 *
	 * @param source the observed object
	 * @param arg the changed code
	 */
	public void notifyIObservers(Object source, Object arg) {
		observableComponent.notifyIObservers(source, arg);
	}

	/**
	 * @see org.python.core.PyObject#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o != null) {
			if (o == this) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int getProtectionLevel() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaDevice.getProtectionLevel();
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaDevice = CorbaDeviceHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaDevice = CorbaDeviceHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setProtectionLevel(int newLevel) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaDevice.setProtectionLevel(newLevel);
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (COMM_FAILURE cf) {
				corbaDevice = CorbaDeviceHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaDevice = CorbaDeviceHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}
}
