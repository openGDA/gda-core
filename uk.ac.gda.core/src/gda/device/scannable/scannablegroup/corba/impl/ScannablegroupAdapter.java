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

package gda.device.scannable.scannablegroup.corba.impl;

import gda.device.Device;
import gda.device.Scannable;
import gda.device.scannable.corba.impl.ScannableAdapter;
import gda.device.scannable.scannablegroup.IScannableGroup;
import gda.device.scannable.scannablegroup.corba.CorbaScannablegroup;
import gda.device.scannable.scannablegroup.corba.CorbaScannablegroupHelper;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.factory.corba.util.NetService;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.Object;
import org.omg.CORBA.TRANSIENT;
import org.python.core.PyObject;
import org.python.expose.ExposedMethod;
import org.python.expose.ExposedType;

/**
 * A client side implementation of the adapter pattern for the ScannableGroup class
 */
@ExposedType(name = "scannablegroupadapter")
public class ScannablegroupAdapter extends ScannableAdapter implements IScannableGroup, Findable, Device, Scannable {

	private CorbaScannablegroup corbaScannableGroup;

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
	public ScannablegroupAdapter(Object obj, String name, NetService netService) {
		super(obj, name, netService);
		corbaScannableGroup = CorbaScannablegroupHelper.narrow(obj);
	}

	@Override
	public void addGroupMemberName(String groupMemberName) {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaScannableGroup.addGroupMemberName(groupMemberName);
				return;
			} catch (COMM_FAILURE cf) {
				corbaScannableGroup = CorbaScannablegroupHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaScannableGroup = CorbaScannablegroupHelper.narrow(netService.reconnect(name));
			}
		}
		return;
	}

	@Override
	public String[] getGroupMemberNames() {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaScannableGroup.getGroupMemberNames();
			} catch (COMM_FAILURE cf) {
				corbaScannableGroup = CorbaScannablegroupHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaScannableGroup = CorbaScannablegroupHelper.narrow(netService.reconnect(name));
			}
		}
		return null;
	}

	@Override
	public PyObject __findattr_ex__(String name) {
		return scannalegroupadapter___findattr__(name);
	}

	/**
	 * expose your custom attribute lookup logic as __getattribute__ to ensure attribute lookups from Python code should
	 * yield the same results as lookup from Java code
	 * 
	 * @param arg
	 */
	@ExposedMethod
	final PyObject scannalegroupadapter___getattribute__(PyObject arg) {
		String name = asName(arg);
		PyObject ret = scannalegroupadapter___findattr__(name);
		if (ret == null)
			noAttributeError(name); // throw Py.AttributeError
		return ret;
	}

	// We put here what we would normally write inside ``__findattr_ex__``.
	// The goal is to avoid changing the behavior of the exposed
	// ``__getattribute__`` if ``__findattr_ex__`` is overriden on a
	// subclass.
	final PyObject scannalegroupadapter___findattr__(String name) {
		// first test if this is a method call
		PyObject test = super.__findattr_ex__(name);

		if (test != null) {
			return test;
		}

		// else find the member's name and return it
		for (String member : this.getGroupMemberNames()) {
			if (member.compareTo(name) == 0) {
				return (PyObject) Finder.getInstance().find(member);
			}
		}
		return null;
	}
}
