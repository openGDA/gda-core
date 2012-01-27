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

package gda.device.peem.corba.impl;

import gda.device.Device;
import gda.device.DeviceException;
import gda.device.PEEM;
import gda.device.corba.CorbaDeviceException;
import gda.device.corba.impl.DeviceAdapter;
import gda.device.peem.corba.CorbaPEEM;
import gda.device.peem.corba.CorbaPEEMHelper;
import gda.factory.Findable;
import gda.factory.corba.util.NetService;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.Object;
import org.omg.CORBA.TRANSIENT;

/**
 * A client side implementation of the adapter pattern for the PEEM class
 */
public class PeemAdapter extends DeviceAdapter implements PEEM, Findable, Device {
	private CorbaPEEM corbaPeem;

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
	public PeemAdapter(Object obj, String name, NetService netService) {
		super(obj, name, netService);
		corbaPeem = CorbaPEEMHelper.narrow(obj);
		this.netService = netService;
		this.name = name;
	}

	@Override
	public boolean connect() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaPeem.connect();
			} catch (COMM_FAILURE cf) {
				corbaPeem = CorbaPEEMHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaPeem = CorbaPEEMHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public boolean disconnect() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaPeem.disconnect();
			} catch (COMM_FAILURE cf) {
				corbaPeem = CorbaPEEMHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaPeem = CorbaPEEMHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public String modules() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaPeem.modules();
			} catch (COMM_FAILURE cf) {
				corbaPeem = CorbaPEEMHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaPeem = CorbaPEEMHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public int getModuleNumber() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaPeem.getModuleNumber();
			} catch (COMM_FAILURE cf) {
				corbaPeem = CorbaPEEMHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaPeem = CorbaPEEMHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public int getModuleIndex(String moduleName) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaPeem.getModuleIndex(moduleName);
			} catch (COMM_FAILURE cf) {
				corbaPeem = CorbaPEEMHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaPeem = CorbaPEEMHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public String getPSName(int index) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaPeem.getPSName((short) index);
			} catch (COMM_FAILURE cf) {
				corbaPeem = CorbaPEEMHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaPeem = CorbaPEEMHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double getPSValue(int index) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaPeem.getPSValue((short) index);
			} catch (COMM_FAILURE cf) {
				corbaPeem = CorbaPEEMHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaPeem = CorbaPEEMHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public boolean setPSValue(int index, double value) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaPeem.setPSValue((short) index, (float) value);
			} catch (COMM_FAILURE cf) {
				corbaPeem = CorbaPEEMHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaPeem = CorbaPEEMHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public String getPreset() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaPeem.getPreset();
			} catch (COMM_FAILURE cf) {
				corbaPeem = CorbaPEEMHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaPeem = CorbaPEEMHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public int setPhi(double angle) throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaPeem.setPhi((float) angle);
			} catch (COMM_FAILURE cf) {
				corbaPeem = CorbaPEEMHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaPeem = CorbaPEEMHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double[] getMicrometerValue() throws DeviceException {
		double[] coord = new double[2];
		org.omg.CORBA.FloatHolder xcoord = new org.omg.CORBA.FloatHolder();
		org.omg.CORBA.FloatHolder ycoord = new org.omg.CORBA.FloatHolder();

		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				corbaPeem.getMicrometerValue(xcoord, ycoord);
				coord[0] = xcoord.value;
				coord[1] = ycoord.value;

			} catch (COMM_FAILURE cf) {
				corbaPeem = CorbaPEEMHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaPeem = CorbaPEEMHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public double getVacuumGaugeValue() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaPeem.getVacuumGaugeValue();
			} catch (COMM_FAILURE cf) {
				corbaPeem = CorbaPEEMHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaPeem = CorbaPEEMHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public String getVacuumGaugeLabel() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaPeem.getVacuumGaugeLabel();
			} catch (COMM_FAILURE cf) {
				corbaPeem = CorbaPEEMHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaPeem = CorbaPEEMHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}

	@Override
	public boolean isInitDone() throws DeviceException {
		for (int i = 0; i < NetService.RETRY; i++) {
			try {
				return corbaPeem.isInitDone();
			} catch (COMM_FAILURE cf) {
				corbaPeem = CorbaPEEMHelper.narrow(netService.reconnect(name));
			} catch (TRANSIENT ct) {
				corbaPeem = CorbaPEEMHelper.narrow(netService.reconnect(name));
			} catch (CorbaDeviceException ex) {
				throw new DeviceException(ex.message);
			}
		}
		throw new DeviceException("Communication failure: retry failed");
	}
}
