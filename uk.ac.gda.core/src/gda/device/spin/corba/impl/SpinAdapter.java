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

package gda.device.spin.corba.impl;

import gda.device.Device;
import gda.device.DeviceException;
import gda.device.ISpin;
import gda.device.Scannable;
import gda.device.corba.CorbaDeviceException;
import gda.device.scannable.ScannableUtils;
import gda.device.scannable.corba.impl.ScannableAdapter;
import gda.device.spin.corba.CorbaSpin;
import gda.device.spin.corba.CorbaSpinHelper;
import gda.factory.Findable;
import gda.factory.corba.util.NetService;

import java.io.Serializable;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;
import org.python.core.PyObject;

/**
 * A client side implementation of the adapter pattern for the motor class
 */
public class SpinAdapter extends ScannableAdapter implements ISpin,Scannable, Device, Findable {

	private CorbaSpin corbaSpin;

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
	public SpinAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		super(obj, name, netService);
		corbaSpin = CorbaSpinHelper.narrow(obj);
		this.netService = netService;
		this.name = name;
	}

	@Override
	public void off() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaSpin.off();
				return;
			} catch (COMM_FAILURE cf) {
				corbaSpin = CorbaSpinHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaSpin = CorbaSpinHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void on() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaSpin.on();
				return;
			} catch (COMM_FAILURE cf) {
				corbaSpin = CorbaSpinHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaSpin = CorbaSpinHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double getSpeed() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaSpin.getSpeed();
			} catch (COMM_FAILURE cf) {
				corbaSpin = CorbaSpinHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaSpin = CorbaSpinHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public String getState() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaSpin.getState();
			} catch (COMM_FAILURE cf) {
				corbaSpin = CorbaSpinHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaSpin = CorbaSpinHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setSpeed(double speed) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaSpin.setSpeed(speed);
				return;
			} catch (COMM_FAILURE cf) {
				corbaSpin = CorbaSpinHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaSpin = CorbaSpinHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void asynchronousMoveTo(Object position) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				if (position instanceof PyObject) {
					position = ScannableUtils.convertToJava((PyObject) position);
				}
				org.omg.CORBA.Any any = org.omg.CORBA.ORB.init().create_any();
				any.insert_Value((Serializable) position);
				corbaSpin.asynchronousMoveTo(any);
				return;
			} catch (COMM_FAILURE cf) {
				corbaSpin = CorbaSpinHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaSpin = CorbaSpinHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public Object getPosition() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				org.omg.CORBA.Any any = corbaSpin.getPosition();
				return any.extract_Value();
			} catch (COMM_FAILURE cf) {
				corbaSpin = CorbaSpinHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaSpin = CorbaSpinHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

}
