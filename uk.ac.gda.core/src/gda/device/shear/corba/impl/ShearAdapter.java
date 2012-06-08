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

package gda.device.shear.corba.impl;

import gda.device.DeviceException;
import gda.device.Shear;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceAdapter;
import gda.device.shear.corba.CorbaShear;
import gda.device.shear.corba.CorbaShearHelper;
import gda.factory.corba.util.NetService;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.TRANSIENT;

/**
 * A client side implementation of the adapter pattern for the Shear class
 */
public class ShearAdapter extends DeviceAdapter implements Shear {
	private CorbaShear corbaShear;

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
	public ShearAdapter(org.omg.CORBA.Object obj, String name, NetService netService) {
		super(obj, name, netService);
		corbaShear = CorbaShearHelper.narrow(obj);
		this.netService = netService;
		this.name = name;
	}

	@Override
	public double getThickness() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaShear.getThickness();
			} catch (COMM_FAILURE cf) {
				corbaShear = CorbaShearHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaShear = CorbaShearHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double getRadius() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaShear.getRadius();
			} catch (COMM_FAILURE cf) {
				corbaShear = CorbaShearHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaShear = CorbaShearHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double getShearRate() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaShear.getShearRate();
			} catch (COMM_FAILURE cf) {
				corbaShear = CorbaShearHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaShear = CorbaShearHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double getAmplitude() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaShear.getAmplitude();
			} catch (COMM_FAILURE cf) {
				corbaShear = CorbaShearHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaShear = CorbaShearHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double getTorque() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaShear.getTorque();
			} catch (COMM_FAILURE cf) {
				corbaShear = CorbaShearHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaShear = CorbaShearHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void continuousShear(double gamma) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaShear.continuousShear(gamma);
				return;
			} catch (COMM_FAILURE cf) {
				corbaShear = CorbaShearHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaShear = CorbaShearHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void oscillatoryShear(double gamma, double amplitude) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaShear.oscillatoryShear(gamma, amplitude);
				return;
			} catch (COMM_FAILURE cf) {
				corbaShear = CorbaShearHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaShear = CorbaShearHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void stopShear() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaShear.stopShear();
				return;
			} catch (COMM_FAILURE cf) {
				corbaShear = CorbaShearHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaShear = CorbaShearHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public void setTorque(double current) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaShear.setTorque(current);
				return;
			} catch (COMM_FAILURE cf) {
				corbaShear = CorbaShearHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaShear = CorbaShearHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}
}
