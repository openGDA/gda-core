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

package gda.util.findableHashtable.corba.impl;

import gda.device.DeviceException;
import gda.device.corba.CorbaDeviceException;
import gda.factory.Findable;
import gda.factory.corba.util.NetService;
import gda.util.findableHashtable.corba.CorbaFindableHashtable;
import gda.util.findableHashtable.corba.CorbaFindableHashtableHelper;

import java.io.Serializable;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;

/**
 * A client side implementation of the adapter pattern for the FindableHashtable class
 */

public class FindableHashtableAdapter implements Findable, gda.util.findableHashtable.Hashtable {
	private CorbaFindableHashtable corbaFindableHashtable;

	private NetService netService;

	private String name;

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
	public FindableHashtableAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		corbaFindableHashtable = CorbaFindableHashtableHelper.narrow(obj);
		this.netService = netService;
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		// see bugzilla bug #443
	}

	@Override
	public boolean getBoolean(String key) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaFindableHashtable.getBoolean(key);
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaFindableHashtable = CorbaFindableHashtableHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaFindableHashtable = CorbaFindableHashtableHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void putBoolean(String key, boolean value) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaFindableHashtable.putBoolean(key, value);
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (org.omg.CORBA.TRANSIENT ct) {
				corbaFindableHashtable = CorbaFindableHashtableHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaFindableHashtable = CorbaFindableHashtableHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public int getInt(String key) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaFindableHashtable.getInt(key);
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaFindableHashtable = CorbaFindableHashtableHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaFindableHashtable = CorbaFindableHashtableHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void putInt(String key, int value) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaFindableHashtable.putInt(key, value);
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaFindableHashtable = CorbaFindableHashtableHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaFindableHashtable = CorbaFindableHashtableHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public long getLong(String key) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaFindableHashtable.getLong(key);
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaFindableHashtable = CorbaFindableHashtableHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaFindableHashtable = CorbaFindableHashtableHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void putLong(String key, long value) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaFindableHashtable.putLong(key, value);
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaFindableHashtable = CorbaFindableHashtableHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaFindableHashtable = CorbaFindableHashtableHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public float getFloat(String key) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaFindableHashtable.getFloat(key);
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaFindableHashtable = CorbaFindableHashtableHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaFindableHashtable = CorbaFindableHashtableHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void putFloat(String key, float value) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaFindableHashtable.putFloat(key, value);
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaFindableHashtable = CorbaFindableHashtableHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaFindableHashtable = CorbaFindableHashtableHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double getDouble(String key) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaFindableHashtable.getDouble(key);
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaFindableHashtable = CorbaFindableHashtableHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaFindableHashtable = CorbaFindableHashtableHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void putDouble(String key, double value) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaFindableHashtable.putDouble(key, value);
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaFindableHashtable = CorbaFindableHashtableHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaFindableHashtable = CorbaFindableHashtableHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public String getString(String key) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaFindableHashtable.getString(key);
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaFindableHashtable = CorbaFindableHashtableHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaFindableHashtable = CorbaFindableHashtableHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void putString(String key, String value) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaFindableHashtable.putString(key, value);
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaFindableHashtable = CorbaFindableHashtableHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaFindableHashtable = CorbaFindableHashtableHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public Object get(String key) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				org.omg.CORBA.Any any = corbaFindableHashtable.get(key);
				return any.extract_Value();
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaFindableHashtable = CorbaFindableHashtableHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaFindableHashtable = CorbaFindableHashtableHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void put(String key, Object value) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
				any.insert_Value((Serializable) value);
				corbaFindableHashtable.put(key, any);
				return;
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			} catch (TRANSIENT ct) {
				corbaFindableHashtable = CorbaFindableHashtableHelper.narrow(netService.reconnect(name));
			} catch (COMM_FAILURE cf) {
				corbaFindableHashtable = CorbaFindableHashtableHelper.narrow(netService.reconnect(name));
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}
}
